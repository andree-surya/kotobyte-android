//
// Created by Andree Surya on 2017/06/21.
//

#include "sqlite3.h"
#include "tokenizers.h"

#include <string.h>
#include <android/log.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "tokenizers", __VA_ARGS__)

const char *WORD_FIELDS_DELIMITER = "‡";

typedef struct {
    fts5_tokenizer unicodeTokenizer;
    Fts5Tokenizer *unicodeTokenizerData;

} TokenizerData;

typedef struct {
    void *originalContext;
    int (*originalCallback)(void *, int, const char *, int, int, int);

    int textOffset;

} JapaneseWordTokenizer_Context;


static int JapaneseWordTokenizer_tokenizeDocumentCallback(
        void *context, int tflags, const char *token, int length, int start, int end) {

    JapaneseWordTokenizer_Context *tokenizerContext = (JapaneseWordTokenizer_Context *) context;
    LOGD("Token: %.*s (%d, %d)", length, token, start, end);

    start = start + tokenizerContext->textOffset;
    end = end + tokenizerContext->textOffset;

    return tokenizerContext->originalCallback(tokenizerContext->originalContext, tflags, token, length, start, end);
}

static int JapaneseWordTokenizer_tokenizeDocument(
        Fts5Tokenizer *FTS5Tokenizer, void *context, int flags, const char *text, int length,
        int (*tokenCallback)(void *, int, const char *, int, int, int)) {

    TokenizerData *tokenizerData = (TokenizerData *) FTS5Tokenizer;
    JapaneseWordTokenizer_Context tokenizerContext = { context, tokenCallback };

    int returnCode;
    const size_t delimiterLength = strlen(WORD_FIELDS_DELIMITER);

    fts5_tokenizer unicodeTokenizer = tokenizerData->unicodeTokenizer;
    Fts5Tokenizer *unicodeTokenizerData = tokenizerData->unicodeTokenizerData;

    const char *literalsField = strstr(text, WORD_FIELDS_DELIMITER) + delimiterLength;
    int literalsFieldLength = (int) (strstr(literalsField, WORD_FIELDS_DELIMITER) - literalsField);

    tokenizerContext.textOffset = (int) (literalsField - text);

    LOGD("Tokenizing: %.*s", literalsFieldLength, literalsField);
    returnCode = unicodeTokenizer.xTokenize(
            unicodeTokenizerData, &tokenizerContext, flags, literalsField, literalsFieldLength,
            &JapaneseWordTokenizer_tokenizeDocumentCallback);

    if (returnCode != SQLITE_OK) {
        return returnCode;
    }

    const char *readingsField = literalsField + literalsFieldLength + delimiterLength;
    int readingsFieldLength = (int) (strstr(readingsField, WORD_FIELDS_DELIMITER) - readingsField);

    tokenizerContext.textOffset = (int) (readingsField - text);

    LOGD("Tokenizing: %.*s", readingsFieldLength, readingsField);
    returnCode = unicodeTokenizer.xTokenize(
            unicodeTokenizerData, &tokenizerContext, flags, readingsField, readingsFieldLength,
            &JapaneseWordTokenizer_tokenizeDocumentCallback);

    return returnCode;
}

static int JapaneseWordTokenizer_tokenizeQuery(
        Fts5Tokenizer *FTS5Tokenizer, void *context, int flags, const char *text, int length,
        int (*tokenCallback)(void *, int, const char *, int, int, int)) {

    TokenizerData *tokenizerData = (TokenizerData *) FTS5Tokenizer;

    return tokenizerData->unicodeTokenizer.xTokenize(
            tokenizerData->unicodeTokenizerData, context, flags, text, length, tokenCallback);
}

static int JapaneseWordTokenizer_tokenize(
        Fts5Tokenizer *FTS5Tokenizer, void *context, int flags, const char *text, int length,
        int (*tokenCallback)(void *, int, const char *, int, int, int)) {

    if ((flags & FTS5_TOKENIZE_DOCUMENT) != 0) {
        return JapaneseWordTokenizer_tokenizeDocument(FTS5Tokenizer, context, flags, text, length, tokenCallback);

    } else {
        return JapaneseWordTokenizer_tokenizeQuery(FTS5Tokenizer, context, flags, text, length, tokenCallback);
    }
}

static int Tokenizer_create(void *context, const char **argv, int argc, Fts5Tokenizer **FTS5Tokenizer) {

    int returnCode = 0;
    fts5_api *FTS5API = (fts5_api *) context;

    fts5_tokenizer unicodeTokenizer;
    Fts5Tokenizer *unicodeTokenizerData = NULL;

    returnCode = FTS5API->xFindTokenizer(FTS5API, "unicode61", &context, &unicodeTokenizer);

    if (SQLITE_OK != returnCode) {
        return returnCode;
    }

    returnCode = unicodeTokenizer.xCreate(context, argv, argc, &unicodeTokenizerData);

    if (SQLITE_OK != returnCode) {
        return returnCode;
    }

    TokenizerData *tokenizer = sqlite3_malloc(sizeof(TokenizerData));

    tokenizer->unicodeTokenizerData = unicodeTokenizerData;
    tokenizer->unicodeTokenizer = unicodeTokenizer;

    *FTS5Tokenizer = (Fts5Tokenizer *) tokenizer;

    return returnCode;
}

static void Tokenizer_delete(Fts5Tokenizer *FTS5Tokenizer) {

    TokenizerData *tokenizer = (TokenizerData *) FTS5Tokenizer;

    tokenizer->unicodeTokenizer.xDelete(tokenizer->unicodeTokenizerData);
    tokenizer->unicodeTokenizerData = NULL;

    sqlite3_free(tokenizer);
}

fts5_tokenizer JapaneseWordTokenizer = { Tokenizer_create, Tokenizer_delete, JapaneseWordTokenizer_tokenize };
