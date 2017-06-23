//
// Created by Andree Surya on 2017/06/04.
//

#include "tokenizers.h"

#include <stdio.h>
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


static jclass thisClass;
static jfieldID thisDatabaseReference;
static jfieldID thisSearchLiteralsStatement;
static jfieldID thisSearchSensesStatement;
static jfieldID thisSearchKanjiStatement;
static jfieldID thisSearchResultsCount;
static jfieldID thisSearchResultsBuffer;
static short searchResultsLimit;


jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env;
    (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6);

    thisClass = (*env)->FindClass(env, "com/kotobyte/models/db/DictionaryDatabase");

    thisDatabaseReference = (*env)->GetFieldID(env, thisClass, "mDatabaseReference", "J");
    thisSearchLiteralsStatement = (*env)->GetFieldID(env, thisClass, "mSearchLiteralsStatement", "J");
    thisSearchSensesStatement = (*env)->GetFieldID(env, thisClass, "mSearchSensesStatement", "J");
    thisSearchKanjiStatement = (*env)->GetFieldID(env, thisClass, "mSearchKanjiStatement", "J");

    thisSearchResultsCount = (*env)->GetFieldID(env, thisClass, "mSearchResultsCount", "S");
    thisSearchResultsBuffer = (*env)->GetFieldID(env, thisClass, "mSearchResultsBuffer", "[Ljava/lang/String;");

    jfieldID searchResultsLimitID = (*env)->GetStaticFieldID(env, thisClass, "SEARCH_RESULTS_LIMIT", "S");
    searchResultsLimit = (*env)->GetStaticShortField(env, thisClass, searchResultsLimitID);

    return JNI_VERSION_1_6;
}

static void throwError(JNIEnv *env, jobject instance, const char *message) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), message);
}

static sqlite3 *getDatabase(JNIEnv *env, jobject instance) {
    return (sqlite3 *) (*env)->GetLongField(env, instance, thisDatabaseReference);
}

static void setDatabase(JNIEnv *env, jobject instance, sqlite3 *database) {
    (*env)->SetLongField(env, instance, thisDatabaseReference, (long) database);
}

static sqlite3_stmt *getStatement(JNIEnv *env, jobject instance, jfieldID statementField) {
    return (sqlite3_stmt *) (*env)->GetLongField(env, instance, statementField);
}

static void setStatement(JNIEnv *env, jobject instance, jfieldID statementField, sqlite3_stmt *statement) {
    (*env)->SetLongField(env, instance, statementField, (long) statement);
}

static void setSearchResultsCount(JNIEnv *env, jobject instance, short search_results_count) {
    (*env)->SetShortField(env, instance, thisSearchResultsCount, search_results_count);
}

static jobjectArray getSearchResultsBuffer(JNIEnv *env, jobject instance) {
    return (*env)->GetObjectField(env, instance, thisSearchResultsBuffer);
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

    setDatabase(env, instance, database);

    goto cleanUp;

    throwDatabaseError:
    throwError(env, instance, sqlite3_errmsg(database));
    sqlite3_close_v2(database);

    cleanUp:
    sqlite3_finalize(FTS5QueryStatement);
    (*env)->ReleaseStringUTFChars(env, databasePath, databasePathUTF);
}

static void closeDatabaseConnection(JNIEnv *env, jobject instance) {

    sqlite3 *database = getDatabase(env, instance);

    sqlite3_stmt *searchLiteralsStatement = getStatement(env, instance, thisSearchLiteralsStatement);
    sqlite3_stmt *searchSensesStatement = getStatement(env, instance, thisSearchSensesStatement);
    sqlite3_stmt *searchKanjiStatement = getStatement(env, instance, thisSearchKanjiStatement);

    if (SQLITE_OK != sqlite3_close_v2(database)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_finalize(searchLiteralsStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_finalize(searchSensesStatement)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_finalize(searchKanjiStatement)) {
        goto throwDatabaseError;
    }

    goto cleanUp;

    throwDatabaseError:
    throwError(env, instance, sqlite3_errmsg(database));

    cleanUp:
    setDatabase(env, instance, NULL);
    setStatement(env, instance, thisSearchLiteralsStatement, NULL);
    setStatement(env, instance, thisSearchSensesStatement, NULL);
    setStatement(env, instance, thisSearchKanjiStatement, NULL);
}

static void buildDatabaseIndexes(JNIEnv *env, jobject instance) {

    sqlite3 *database = getDatabase(env, instance);
    char *errorMessage = NULL;

    if (SQLITE_OK != sqlite3_exec(database, BUILD_INDEXES_SQL, NULL, NULL, &errorMessage)) {
        throwError(env, instance, errorMessage);

        sqlite3_free(errorMessage);
    }
}

static void executeSearch(JNIEnv *env, jobject instance, jfieldID statementID, const char *statementSQL, jstring query) {

    const char *queryUTF = (*env)->GetStringUTFChars(env, query, NULL);

    sqlite3 *database = getDatabase(env, instance);
    sqlite3_stmt *statement = getStatement(env, instance, thisSearchLiteralsStatement);

    if (statement == NULL) {

        if (SQLITE_OK != sqlite3_prepare_v2(database, statementSQL, -1, &statement, NULL)) {
            goto throwDatabaseError;
        }

        setStatement(env, instance, statementID, statement);
    }

    if (SQLITE_OK != sqlite3_bind_text(statement, 1, queryUTF, -1, NULL)) {
        goto throwDatabaseError;
    }

    if (SQLITE_OK != sqlite3_bind_int(statement, 2, searchResultsLimit)) {
        goto throwDatabaseError;
    }

    int stepStatus = sqlite3_step(statement);
    short searchResultsCount = 0;

    jobjectArray searchResultsBuffer = getSearchResultsBuffer(env, instance);

    while (stepStatus == SQLITE_ROW) {

        const unsigned char *encodedObjectUTF = sqlite3_column_text(statement, 0);
        jstring encodedObject = (*env)->NewStringUTF(env, (char *) encodedObjectUTF);

        (*env)->SetObjectArrayElement(env, searchResultsBuffer, searchResultsCount, encodedObject);

        searchResultsCount += 1;
        stepStatus = sqlite3_step(statement);
    }

    setSearchResultsCount(env, instance, searchResultsCount);

    if (stepStatus == SQLITE_DONE) {
        goto cleanUp;
    }

    throwDatabaseError:
    throwError(env, instance, sqlite3_errmsg(database));

    cleanUp:
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
    executeSearch(env, instance, thisSearchLiteralsStatement, SEARCH_LITERALS_SQL, query);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchWordsBySenses(JNIEnv *env, jobject instance, jstring query) {
    executeSearch(env, instance, thisSearchSensesStatement, SEARCH_SENSES_SQL, query);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchKanji(JNIEnv *env, jobject instance, jstring query) {
    executeSearch(env, instance, thisSearchKanjiStatement, SEARCH_KANJI_SQL, query);
}
