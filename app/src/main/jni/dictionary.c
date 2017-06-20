//
// Created by Andree Surya on 2017/06/04.
//

#include "sqlite3.h"

#include <stdio.h>
#include <jni.h>

static const char *BUILD_DATABASE_INDEXES_SQL = \
        "DROP TABLE IF EXISTS words_english_fts5;"
        "DROP TABLE IF EXISTS words_japanese_fts5;"
        "DROP TABLE IF EXISTS kanji_japanese_fts5;"

        "CREATE VIRTUAL TABLE words_english_fts5 USING fts5 (encoded, content='words');"
        "CREATE VIRTUAL TABLE words_japanese_fts5 USING fts5 (encoded, content='words');"
        "CREATE VIRTUAL TABLE kanji_japanese_fts5 USING fts5 (encoded, content='kanji');"

        "INSERT INTO words_english_fts5 (words_english_fts5) VALUES ('rebuild');"
        "INSERT INTO words_english_fts5 (words_english_fts5) VALUES ('optimize');"
        "INSERT INTO words_japanese_fts5 (words_japanese_fts5) VALUES ('rebuild');"
        "INSERT INTO words_japanese_fts5 (words_japanese_fts5) VALUES ('optimize');"
        "INSERT INTO kanji_japanese_fts5 (kanji_japanese_fts5) VALUES ('rebuild');"
        "INSERT INTO kanji_japanese_fts5 (kanji_japanese_fts5) VALUES ('optimize');";

static const char *SEARCH_WORDS_BY_ENGLISH_SQL = \
        "SELECT words.encoded FROM words JOIN words_english_fts5 ON (words.rowid = words_english_fts5.rowid) "
        "WHERE words_english_fts5 MATCH ? ORDER BY rank * priority LIMIT ?";

static const char *SEARCH_WORDS_BY_JAPANESE_SQL = \
        "SELECT words.encoded FROM words JOIN words_japanese_fts5 ON (words.rowid = words_japanese_fts5.rowid) "
        "WHERE words_japanese_fts5 MATCH ? ORDER BY rank * priority LIMIT ?";

static const char *SEARCH_KANJI_BY_JAPANESE_SQL = \
        "SELECT encoded FROM kanji_japanese_fts5 WHERE kanji_japanese_fts5 MATCH ? ORDER BY rank LIMIT ?";


static jclass this_class;
static jfieldID this_database;
static jfieldID this_search_results_count;
static jfieldID this_search_results_buffer;
static short search_results_limit;


jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env;
    (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6);

    this_class = (*env)->FindClass(env, "com/kotobyte/models/db/DictionaryDatabase");
    this_database = (*env)->GetFieldID(env, this_class, "mDatabaseRef", "J");
    this_search_results_count = (*env)->GetFieldID(env, this_class, "mSearchResultsCount", "S");
    this_search_results_buffer = (*env)->GetFieldID(env, this_class, "mSearchResultsBuffer", "[Ljava/lang/String;");

    jfieldID search_results_limit_id = (*env)->GetStaticFieldID(env, this_class, "SEARCH_RESULTS_LIMIT", "S");
    search_results_limit = (*env)->GetStaticShortField(env, this_class, search_results_limit_id);

    return JNI_VERSION_1_6;
}

static sqlite3 *get_database(JNIEnv *env, jobject instance) {
    return (sqlite3 *) (*env)->GetLongField(env, instance, this_database);
}

static void set_database(JNIEnv *env, jobject instance, sqlite3 *database) {
    (*env)->SetLongField(env, instance, this_database, (long) database);
}

static void set_search_results_count(JNIEnv *env, jobject instance, short search_results_count) {
    (*env)->SetShortField(env, instance, this_search_results_count, search_results_count);
}

static jobjectArray get_search_results_buffer(JNIEnv *env, jobject instance) {
    return (*env)->GetObjectField(env, instance, this_search_results_buffer);
}

static void throw_error(JNIEnv *env, jobject instance, const char *message) {
    (*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/RuntimeException"), message);
}

static void open_database_connection(JNIEnv *env, jobject instance, jstring file_path, jboolean read_only) {

    sqlite3 *database;

    const char *file_path_utf = (*env)->GetStringUTFChars(env, file_path, NULL);
    int flags = read_only == JNI_TRUE ? SQLITE_OPEN_READONLY : SQLITE_OPEN_READWRITE;

    if (sqlite3_open_v2(file_path_utf, &database, flags, NULL) != SQLITE_OK) {
        throw_error(env, instance, sqlite3_errmsg(database));

        sqlite3_close_v2(database);
    }

    (*env)->ReleaseStringUTFChars(env, file_path, file_path_utf);

    set_database(env, instance, database);
}

static void close_database_connection(JNIEnv *env, jobject instance) {

    sqlite3 *database = get_database(env, instance);

    if (sqlite3_close_v2(database) != SQLITE_OK) {
        throw_error(env, instance, sqlite3_errmsg(database));
    }

    set_database(env, instance, NULL);
}

static void build_database_indexes(JNIEnv *env, jobject instance) {

    sqlite3 *database = get_database(env, instance);
    char *error_message = NULL;

    if (sqlite3_exec(database, BUILD_DATABASE_INDEXES_SQL, NULL, NULL, &error_message) != SQLITE_OK) {
        throw_error(env, instance, error_message);

        sqlite3_free(error_message);
    }
}

static void execute_search(JNIEnv *env, jobject instance, const char* sql, jstring query) {

    sqlite3 *database = get_database(env, instance);
    sqlite3_stmt *statement;

    const char *query_utf = (*env)->GetStringUTFChars(env, query, NULL);

    if (sqlite3_prepare_v2(database, sql, -1, &statement, NULL) != SQLITE_OK) {
        goto throw_database_error;
    }

    if (sqlite3_bind_text(statement, 1, query_utf, -1, NULL) != SQLITE_OK) {
        goto throw_database_error;
    }

    if (sqlite3_bind_int(statement, 2, search_results_limit) != SQLITE_OK) {
        goto throw_database_error;
    }

    int step_status = sqlite3_step(statement);
    short search_results_count = 0;

    jobjectArray search_results_buffer = get_search_results_buffer(env, instance);

    while (step_status == SQLITE_ROW) {

        const unsigned char *encoded_object_utf = sqlite3_column_text(statement, 0);
        jstring encoded_object = (*env)->NewStringUTF(env, (char *) encoded_object_utf);

        (*env)->SetObjectArrayElement(env, search_results_buffer, search_results_count, encoded_object);

        search_results_count += 1;
        step_status = sqlite3_step(statement);
    }

    set_search_results_count(env, instance, search_results_count);

    if (step_status == SQLITE_DONE) {
        goto clean_up;
    }

    throw_database_error:
    throw_error(env, instance, sqlite3_errmsg(database));

    clean_up:
    sqlite3_finalize(statement);
    (*env)->ReleaseStringUTFChars(env, query, query_utf);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeOpenConnection(JNIEnv *env, jobject instance, jstring file_path, jboolean read_only) {
    open_database_connection(env, instance, file_path, read_only);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeCloseConnection(JNIEnv *env, jobject instance) {
    close_database_connection(env, instance);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeBuildIndexes(JNIEnv *env, jobject instance) {
    build_database_indexes(env, instance);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchWordsByEnglish(JNIEnv *env, jobject instance, jstring query) {
    execute_search(env, instance, SEARCH_WORDS_BY_ENGLISH_SQL, query);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchWordsByJapanese(JNIEnv *env, jobject instance, jstring query) {
    execute_search(env, instance, SEARCH_WORDS_BY_JAPANESE_SQL, query);
}

JNIEXPORT void JNICALL
Java_com_kotobyte_models_db_DictionaryDatabase_nativeSearchKanjiByJapanese(JNIEnv *env, jobject instance, jstring query) {
    execute_search(env, instance, SEARCH_KANJI_BY_JAPANESE_SQL, query);
}
