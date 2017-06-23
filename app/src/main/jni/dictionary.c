//
// Created by Andree Surya on 2017/06/04.
//

#include "tokenizers.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>

static const char *BUILD_INDEXES_SQL = \
        "CREATE VIRTUAL TABLE literals_fts USING fts5(text, content='literals', tokenize='unicode61');"
        "CREATE VIRTUAL TABLE senses_fts USING fts5(text, content='senses', tokenize='porter unicode61');"
        "CREATE VIRTUAL TABLE kanji_fts USING fts5(character, content='kanji', tokenize='unicode61');"

        "INSERT INTO literals_fts (literals_fts) VALUES ('rebuild');"
        "INSERT INTO senses_fts (senses_fts) VALUES ('rebuild');"
        "INSERT INTO kanji_fts (kanji_fts) VALUES ('rebuild');";

static const char *SEARCH_LITERALS_SQL = \
        "WITH search_results AS (SELECT word_id FROM literals_fts(?) INNER JOIN literals ON (literals_fts.rowid = literals.rowid) GROUP BY word_id ORDER BY MIN(rank * priority) LIMIT ?), "
        "word_literals AS (SELECT word_id, GROUP_CONCAT(priority || text, ']') text FROM literals WHERE word_id IN search_results GROUP BY word_id), "
        "word_senses AS (SELECT word_id, GROUP_CONCAT(text || '}' || IFNULL(categories, '') || '}' || IFNULL(origins, '') || '}' || IFNULL(labels, '') || '}' || IFNULL(notes, ''), '>') text FROM senses WHERE word_id IN search_results GROUP BY word_id) "

        "SELECT (search_results.word_id || '_' || word_literals.text || '_' || word_senses.text) text FROM search_results "
        "INNER JOIN word_literals ON (search_results.word_id = word_literals.word_id) "
        "INNER JOIN word_senses ON (search_results.word_id = word_senses.word_id)";

static const char *SEARCH_SENSES_SQL = \
        "WITH search_results AS (SELECT word_id FROM senses_fts(?) INNER JOIN senses ON (senses_fts.rowid = senses.rowid) GROUP BY word_id ORDER BY MIN(rank) LIMIT ?), "
        "word_literals AS (SELECT word_id, GROUP_CONCAT(priority || text, ']') text FROM literals WHERE word_id IN search_results GROUP BY word_id), "
        "word_senses AS (SELECT word_id, GROUP_CONCAT(text || '}' || IFNULL(categories, '') || '}' || IFNULL(origins, '') || '}' || IFNULL(labels, '') || '}' || IFNULL(notes, ''), '>') text FROM senses WHERE word_id IN search_results GROUP BY word_id) "

        "SELECT (search_results.word_id || '_' || word_literals.text || '_' || word_senses.text) text FROM search_results "
        "INNER JOIN word_literals ON (search_results.word_id = word_literals.word_id) "
        "INNER JOIN word_senses ON (search_results.word_id = word_senses.word_id)";

static const char *SEARCH_KANJI_SQL = \
        "SELECT kanji.id || '_' || kanji.character || '_' || IFNULL(readings, '') || '_' || IFNULL(meanings, '') || '_' || IFNULL(jlpt, '') || '_' || IFNULL(grade, '') || '_' || IFNULL(strokes, '') text "
        "FROM kanji_fts(?) "
        "INNER JOIN kanji ON (kanji_fts.rowid = kanji.rowid) "
        "ORDER BY rank "
        "LIMIT ?";


typedef struct {
    sqlite3 *database;

    sqlite3_stmt *searchLiteralsStatement;
    sqlite3_stmt *searchSensesStatement;
    sqlite3_stmt *searchKanjiStatement;

} DictionaryContext;

static jfieldID dictionaryContextID;
static jfieldID searchResultsCountID;
static jfieldID searchResultsBufferID;
static short searchResultsLimit;


jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env;
    (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6);

    jclass thisClass = (*env)->FindClass(env, "com/kotobyte/models/db/DictionaryDatabase");
    jfieldID searchResultsLimitID = (*env)->GetStaticFieldID(env, thisClass, "SEARCH_RESULTS_LIMIT", "S");

    dictionaryContextID = (*env)->GetFieldID(env, thisClass, "mDictionaryContext", "J");
    searchResultsCountID = (*env)->GetFieldID(env, thisClass, "mSearchResultsCount", "S");
    searchResultsBufferID = (*env)->GetFieldID(env, thisClass, "mSearchResultsBuffer", "[Ljava/lang/String;");
    searchResultsLimit = (*env)->GetStaticShortField(env, thisClass, searchResultsLimitID);

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

static void setSearchResultsCount(JNIEnv *env, jobject instance, short search_results_count) {
    (*env)->SetShortField(env, instance, searchResultsCountID, search_results_count);
}

static jobjectArray getSearchResultsBuffer(JNIEnv *env, jobject instance) {
    return (*env)->GetObjectField(env, instance, searchResultsBufferID);
}

static void openDatabaseConnection(JNIEnv *env, jobject instance, jstring databasePath, jboolean readOnly) {

    sqlite3 *database = NULL;
    fts5_api *FTS5API = NULL;
    sqlite3_stmt *FTS5QueryStatement = NULL;

    const char *databasePathUTF = (*env)->GetStringUTFChars(env, databasePath, NULL);
    int flags = readOnly == JNI_TRUE ? SQLITE_OPEN_READONLY : SQLITE_OPEN_READWRITE;

    if (SQLITE_OK != sqlite3_open_v2(databasePathUTF, &database, flags, NULL)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_prepare_v2(database, "SELECT fts5()", -1, &FTS5QueryStatement, 0)) {
        goto throwDatabaseError;
    }

    if (SQLITE_ROW != sqlite3_step(FTS5QueryStatement)) {
        goto throwDatabaseError;
    }

    memcpy(&FTS5API, sqlite3_column_blob(FTS5QueryStatement, 0), sizeof(FTS5API));

    if (SQLITE_OK != FTS5API->xCreateTokenizer(FTS5API, "japanese_literal", (void *) FTS5API, &JapaneseWordTokenizer, NULL)) {
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
    sqlite3_finalize(FTS5QueryStatement);
    (*env)->ReleaseStringUTFChars(env, databasePath, databasePathUTF);
}

static void closeDatabaseConnection(JNIEnv *env, jobject instance) {

    DictionaryContext *context = getContext(env, instance);

    if (SQLITE_OK != sqlite3_close_v2(context->database)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_finalize(context->searchLiteralsStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_finalize(context->searchSensesStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_finalize(context->searchKanjiStatement)) {
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

static int executeSearch(JNIEnv *env, jobject instance, sqlite3_stmt *statement, jstring query) {

    int returnCode;
    const char *queryUTF = (*env)->GetStringUTFChars(env, query, NULL);

    returnCode = sqlite3_bind_text(statement, 1, queryUTF, -1, NULL);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = sqlite3_bind_int(statement, 2, searchResultsLimit);

    if (SQLITE_OK != returnCode) {
        goto cleanUp;
    }

    returnCode = sqlite3_step(statement);
    short searchResultsCount = 0;

    jobjectArray searchResultsBuffer = getSearchResultsBuffer(env, instance);

    while (SQLITE_ROW == returnCode) {

        const unsigned char *encodedObjectUTF = sqlite3_column_text(statement, 0);
        jstring encodedObject = (*env)->NewStringUTF(env, (char *) encodedObjectUTF);

        (*env)->SetObjectArrayElement(env, searchResultsBuffer, searchResultsCount, encodedObject);

        searchResultsCount += 1;
        returnCode = sqlite3_step(statement);
    }

    setSearchResultsCount(env, instance, searchResultsCount);

    cleanUp:
    sqlite3_reset(statement);
    sqlite3_clear_bindings(statement);

    (*env)->ReleaseStringUTFChars(env, query, queryUTF);
    return SQLITE_DONE == returnCode ? SQLITE_OK : returnCode;
}


static void searchWordsByLiterals(JNIEnv *env, jobject instance, jstring query) {

    DictionaryContext *context = getContext(env, instance);
    int returnCode = SQLITE_OK;

    if (! context->searchLiteralsStatement) {
        returnCode = sqlite3_prepare_v2(context->database, SEARCH_LITERALS_SQL, -1, &context->searchLiteralsStatement, NULL);
    }

    if (SQLITE_OK == returnCode) {
        returnCode = executeSearch(env, instance, context->searchLiteralsStatement, query);
    }

    if (SQLITE_OK != returnCode) {
        throwError(env, sqlite3_errmsg(context->database));
    }
}

static void searchWordsBySenses(JNIEnv *env, jobject instance, jstring query) {

    DictionaryContext *context = getContext(env, instance);
    int returnCode = SQLITE_OK;

    if (! context->searchSensesStatement) {
        returnCode = sqlite3_prepare_v2(context->database, SEARCH_SENSES_SQL, -1, &context->searchSensesStatement, NULL);
    }

    if (SQLITE_OK == returnCode) {
        returnCode = executeSearch(env, instance, context->searchSensesStatement, query);
    }

    if (SQLITE_OK != returnCode) {
        throwError(env, sqlite3_errmsg(context->database));
    }
}

static void searchKanji(JNIEnv *env, jobject instance, jstring query) {

    DictionaryContext *context = getContext(env, instance);
    int returnCode = SQLITE_OK;

    if (! context->searchKanjiStatement) {
        returnCode = sqlite3_prepare_v2(context->database, SEARCH_KANJI_SQL, -1, &context->searchKanjiStatement, NULL);
    }

    if (SQLITE_OK == returnCode) {
        returnCode = executeSearch(env, instance, context->searchKanjiStatement, query);
    }

    if (SQLITE_OK != returnCode) {
        throwError(env, sqlite3_errmsg(context->database));
    }
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
    searchWordsByLiterals(env, instance, query);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchWordsBySenses(JNIEnv *env, jobject instance, jstring query) {
    searchWordsBySenses(env, instance, query);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchKanji(JNIEnv *env, jobject instance, jstring query) {
    searchKanji(env, instance, query);
}
