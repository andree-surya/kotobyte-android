//
// Created by Andree Surya on 2017/06/04.
//

#include "tokenizers.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>

static const char *BUILD_INDEXES_SQL = \
        "CREATE VIRTUAL TABLE literals_fts USING fts5(text, content='literals', tokenize='literal');"
        "CREATE VIRTUAL TABLE senses_fts USING fts5(text, content='senses', tokenize='porter');"
        "CREATE VIRTUAL TABLE kanji_fts USING fts5(character, content='kanji', tokenize='kanji');"

        "INSERT INTO literals_fts (literals_fts) VALUES ('rebuild');"
        "INSERT INTO senses_fts (senses_fts) VALUES ('rebuild');"
        "INSERT INTO kanji_fts (kanji_fts) VALUES ('rebuild');";

static const char *RESET_SEARCH_RESULTS_SQL = \
        "CREATE TEMP TABLE IF NOT EXISTS search_results (id INTEGER PRIMARY KEY, score REAL);"
        "DELETE FROM search_results;";

static const char *SEARCH_WORDS_BY_LITERALS_SQL = \
        "INSERT INTO search_results "
        "   SELECT word_id, MIN(rank * priority) score "
        "   FROM literals l JOIN literals_fts(?) lf ON (l.rowid = lf.rowid) "
        "   GROUP BY word_id ORDER BY score LIMIT ?;";

static const char *SEARCH_WORDS_BY_SENSES_SQL = \
        "INSERT INTO search_results "
        "   SELECT word_id, MIN(rank) rank "
        "   FROM senses s JOIN senses_fts(?) sf ON (s.rowid = sf.rowid) "
        "   GROUP BY word_id ORDER BY rank LIMIT ?;";

static const char *GET_WORD_SEARCH_RESULTS_SQL = \
        "WITH "
        "word_literals AS ("
        "   SELECT id, GROUP_CONCAT(priority || text, ']') text "
        "   FROM literals l JOIN search_results sr ON (l.word_id = sr.id) GROUP BY id),"

        "word_senses AS ("
        "   SELECT id, GROUP_CONCAT(text || '<' || IFNULL(categories, '') || '<' || IFNULL(origins, '') || '<' || IFNULL(labels, '') || '<' || IFNULL(notes, ''), '>') text "
        "   FROM senses s JOIN search_results sr ON (s.word_id = sr.id) GROUP BY id) "

        "SELECT (sr.id || '_' || wl.text || '_' || ws.text) text "
        "   FROM search_results sr JOIN word_literals wl ON (sr.id = wl.id) JOIN word_senses ws ON (sr.id = ws.id) "
        "   ORDER BY score;";

static const char *SEARCH_KANJI_SQL = \
        "SELECT kanji.id || '_' || kanji.character || '_' || IFNULL(readings, '') || '_' || IFNULL(meanings, '') || '_' || IFNULL(jlpt, '') || '_' || IFNULL(grade, '') || '_' || IFNULL(strokes, '') text "
        "   FROM kanji_fts(?) INNER JOIN kanji ON (kanji_fts.rowid = kanji.rowid) "
        "   ORDER BY rank LIMIT ?";


typedef struct {
    sqlite3 *database;

    sqlite3_stmt *searchKanjiStatement;
    sqlite3_stmt *searchWordsByLiteralsStatement;
    sqlite3_stmt *searchWordsBySensesStatement;
    sqlite3_stmt *getWordSearchResultsStatement;

} DictionaryContext;

static jfieldID dictionaryContextID;
static jfieldID searchResultsCountID;
static jfieldID searchResultsBufferID;
static int searchResultsLimit;


jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env;
    (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6);

    jclass thisClass = (*env)->FindClass(env, "com/kotobyte/models/db/DictionaryDatabase");

    dictionaryContextID = (*env)->GetFieldID(env, thisClass, "mDictionaryContext", "J");
    searchResultsCountID = (*env)->GetFieldID(env, thisClass, "mSearchResultsCount", "I");
    searchResultsBufferID = (*env)->GetFieldID(env, thisClass, "mSearchResultsBuffer", "[Ljava/lang/String;");
    searchResultsLimit = (*env)->GetStaticIntField(env, thisClass, (*env)->GetStaticFieldID(env, thisClass, "SEARCH_RESULTS_LIMIT", "I"));

    return JNI_VERSION_1_6;
}

static void throwError(JNIEnv *env, const char *message) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), message);
}

static DictionaryContext *getContext(JNIEnv *env, jobject instance) {
    return (DictionaryContext *) (*env)->GetLongField(env, instance, dictionaryContextID);
}

static void setContext(JNIEnv *env, jobject instance, DictionaryContext *context) {
    (*env)->SetLongField(env, instance, dictionaryContextID, (long) context);
}

static void setSearchResultsCount(JNIEnv *env, jobject instance, int search_results_count) {
    (*env)->SetIntField(env, instance, searchResultsCountID, search_results_count);
}

static jobjectArray getSearchResultsBuffer(JNIEnv *env, jobject instance) {
    return (*env)->GetObjectField(env, instance, searchResultsBufferID);
}

static int enableCustomTokenizers(sqlite3 *database) {

    sqlite3_stmt *statement = NULL;
    fts5_api *FTS5API = NULL;
    int returnCode;

    returnCode = sqlite3_prepare_v2(database, "SELECT fts5()", -1, &statement, 0);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = sqlite3_step(statement);

    if (SQLITE_ROW != returnCode) {
        goto cleanUp;
    }

    memcpy(&FTS5API, sqlite3_column_blob(statement, 0), sizeof(FTS5API));

    returnCode = FTS5API->xCreateTokenizer(FTS5API, "literal", (void *) FTS5API, &LiteralTokenizer, NULL);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = FTS5API->xCreateTokenizer(FTS5API, "kanji", (void *) FTS5API, &KanjiTokenizer, NULL);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    cleanUp:
    sqlite3_finalize(statement);

    return returnCode;
}

static void openDatabaseConnection(JNIEnv *env, jobject instance, jstring databasePath, jboolean readOnly) {

    sqlite3 *database = NULL;

    const char *databasePathUTF = (*env)->GetStringUTFChars(env, databasePath, NULL);
    int flags = readOnly == JNI_TRUE ? SQLITE_OPEN_READONLY : SQLITE_OPEN_READWRITE;

    if (SQLITE_OK != sqlite3_open_v2(databasePathUTF, &database, flags, NULL)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != enableCustomTokenizers(database)) {
        goto throwDatabaseError;
    }

    DictionaryContext *context = calloc(1, sizeof(DictionaryContext));
    context->database = database;

    setContext(env, instance, context);

    goto cleanUp;

    throwDatabaseError:
    throwError(env, sqlite3_errmsg(database));

    sqlite3_close_v2(database);

    cleanUp:
    (*env)->ReleaseStringUTFChars(env, databasePath, databasePathUTF);
}

static void closeDatabaseConnection(JNIEnv *env, jobject instance) {

    DictionaryContext *context = getContext(env, instance);

    if (SQLITE_OK != sqlite3_finalize(context->searchKanjiStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_finalize(context->searchWordsByLiteralsStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_finalize(context->searchWordsBySensesStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_finalize(context->getWordSearchResultsStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_close_v2(context->database)) {
        goto throwDatabaseError;
    }

    goto cleanUp;

    throwDatabaseError:
    throwError(env, sqlite3_errmsg(context->database));

    cleanUp:
    free(context);

    setContext(env, instance, NULL);
}

static void buildDatabaseIndexes(JNIEnv *env, jobject instance) {

    DictionaryContext *context = getContext(env, instance);
    char *errorMessage = NULL;

    if (SQLITE_OK != sqlite3_exec(context->database, BUILD_INDEXES_SQL, NULL, NULL, &errorMessage)) {
        throwError(env, errorMessage);

        sqlite3_free(errorMessage);
    }
}

static int prepareStatement(sqlite3 *database, const char *statementSQL, sqlite3_stmt **statement) {

    int returnCode = SQLITE_OK;

    if (*statement == NULL) {
        returnCode = sqlite3_prepare_v2(database, statementSQL, -1, statement, NULL);
    }

    return returnCode;
}

static int processSearchResultsFromStatement(JNIEnv *env, jobject instance, sqlite3_stmt *statement) {

    jobjectArray searchResultsBuffer = getSearchResultsBuffer(env, instance);
    int searchResultsLimit = (*env)->GetArrayLength(env, searchResultsBuffer);

    int returnCode = sqlite3_step(statement);
    int searchResultsCount = 0;

    while (SQLITE_ROW == returnCode && searchResultsCount < searchResultsLimit) {

        const unsigned char *encodedObjectUTF = sqlite3_column_text(statement, 0);
        jstring encodedObject = (*env)->NewStringUTF(env, (char *) encodedObjectUTF);

        (*env)->SetObjectArrayElement(env, searchResultsBuffer, searchResultsCount, encodedObject);

        searchResultsCount += 1;
        returnCode = sqlite3_step(statement);
    }

    setSearchResultsCount(env, instance, searchResultsCount);

    return SQLITE_DONE == returnCode ? SQLITE_OK : returnCode;
}

static int executeSearchWordsByLiterals(DictionaryContext *context, const char *queryUTF) {

    int returnCode = prepareStatement(context->database, SEARCH_WORDS_BY_LITERALS_SQL, &context->searchWordsByLiteralsStatement);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = sqlite3_bind_text(context->searchWordsByLiteralsStatement, 1, queryUTF, -1, NULL);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = sqlite3_bind_int(context->searchWordsByLiteralsStatement, 2, searchResultsLimit);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = sqlite3_step(context->searchWordsByLiteralsStatement);

    if (SQLITE_DONE != returnCode) {
        goto cleanUp;
    }

    cleanUp:
    sqlite3_reset(context->searchWordsByLiteralsStatement);
    sqlite3_clear_bindings(context->searchWordsByLiteralsStatement);

    return SQLITE_DONE == returnCode ? SQLITE_OK : returnCode;
}

static int executeSearchWordsBySenses(DictionaryContext *context, const char *queryUTF) {

    int returnCode = prepareStatement(context->database, SEARCH_WORDS_BY_SENSES_SQL, &context->searchWordsBySensesStatement);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = sqlite3_bind_text(context->searchWordsBySensesStatement, 1, queryUTF, -1, NULL);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = sqlite3_bind_int(context->searchWordsBySensesStatement, 2, searchResultsLimit);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = sqlite3_step(context->searchWordsBySensesStatement);

    if (SQLITE_DONE != returnCode) {
        goto cleanUp;
    }

    cleanUp:
    sqlite3_reset(context->searchWordsBySensesStatement);
    sqlite3_clear_bindings(context->searchWordsBySensesStatement);

    return SQLITE_DONE == returnCode ? SQLITE_OK : returnCode;
}

static void searchWords(JNIEnv *env, jobject instance, jstring query, int (*executeSearch)(DictionaryContext *, const char *)) {

    DictionaryContext *context = getContext(env, instance);
    const char *queryUTF = (*env)->GetStringUTFChars(env, query, NULL);

    char *errorMessage = NULL;

    if (SQLITE_OK != sqlite3_exec(context->database, RESET_SEARCH_RESULTS_SQL, NULL, NULL, &errorMessage)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != prepareStatement(context->database, GET_WORD_SEARCH_RESULTS_SQL, &context->getWordSearchResultsStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != executeSearch(context, queryUTF)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != processSearchResultsFromStatement(env, instance, context->getWordSearchResultsStatement)) {
        goto throwDatabaseError;
    }

    goto cleanUp;

    throwDatabaseError:
    throwError(env, errorMessage ? errorMessage : sqlite3_errmsg(context->database));

    cleanUp:
    sqlite3_free(errorMessage);
    sqlite3_reset(context->getWordSearchResultsStatement);

    (*env)->ReleaseStringUTFChars(env, query, queryUTF);
}

static void searchKanji(JNIEnv *env, jobject instance, jstring query) {

    DictionaryContext *context = getContext(env, instance);
    const char *queryUTF = (*env)->GetStringUTFChars(env, query, NULL);

    if (SQLITE_OK != prepareStatement(context->database, SEARCH_KANJI_SQL, &context->searchKanjiStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_bind_text(context->searchKanjiStatement, 1, queryUTF, -1, NULL)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_bind_int(context->searchKanjiStatement, 2, searchResultsLimit)) {
        goto cleanUp;
    }

    if (SQLITE_OK != processSearchResultsFromStatement(env, instance, context->searchKanjiStatement)) {
        goto throwDatabaseError;
    }

    goto cleanUp;

    throwDatabaseError:
    throwError(env, sqlite3_errmsg(context->database));

    cleanUp:
    sqlite3_reset(context->searchKanjiStatement);
    sqlite3_clear_bindings(context->searchKanjiStatement);

    (*env)->ReleaseStringUTFChars(env, query, queryUTF);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeOpenConnection(JNIEnv *env, jobject instance, jstring databasePath, jboolean readOnly) {
    openDatabaseConnection(env, instance, databasePath, readOnly);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeCloseConnection(JNIEnv *env, jobject instance) {
    closeDatabaseConnection(env, instance);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeBuildIndexes(JNIEnv *env, jobject instance) {
    buildDatabaseIndexes(env, instance);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchWordsByLiterals(JNIEnv *env, jobject instance, jstring query) {
    searchWords(env, instance, query, executeSearchWordsByLiterals);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchWordsBySenses(JNIEnv *env, jobject instance, jstring query) {
    searchWords(env, instance, query, executeSearchWordsBySenses);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchKanji(JNIEnv *env, jobject instance, jstring query) {
    searchKanji(env, instance, query);
}
