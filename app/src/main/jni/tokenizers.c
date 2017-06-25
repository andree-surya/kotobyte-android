//
// Created by Andree Surya on 2017/06/21.
//

#include "tokenizers.h"

typedef struct {
    void *context;

} Tokenizer;


static int nextUTF8CharOffset(const unsigned char *text, int offset) {

    if (*(text + offset) > 0) {
        offset++;

        // Skip bytes that are not the first byte of a UTF-8 character.
        while ((*(text + offset) & 0xc0) == 0x80) {
            offset++;
        }

        return offset;
    }

    return -1;
}

static int LiteralTokenizer_tokenize(
        Fts5Tokenizer *tokenizer, void *context, int flags, const char *text, int length,
        int (*tokenCallback)(void *, int, const char *, int, int, int)) {

    int returnCode = SQLITE_OK;
    int tokenFlags = 0;

    if ((flags & FTS5_TOKENIZE_QUERY) == FTS5_TOKENIZE_QUERY) {
        tokenFlags = tokenFlags | FTS5_TOKEN_COLOCATED;
    }

    int nextOffset = nextUTF8CharOffset((const unsigned char *) text, 0);

    while (nextOffset > 0 && nextOffset <= length) {
        returnCode = tokenCallback(context, tokenFlags, text, nextOffset, 0, nextOffset);

        if (returnCode != SQLITE_OK) {
            break;
        }

        nextOffset = nextUTF8CharOffset((const unsigned char *) text, nextOffset);
    }

    return returnCode;
}

static int KanjiTokenizer_tokenize(
        Fts5Tokenizer *tokenizer, void *context, int flags, const char *text, int length,
        int (*tokenCallback)(void *, int, const char *, int, int, int)) {

    int returnCode = SQLITE_OK;
    int tokenFlags = 0;

    if ((flags & FTS5_TOKENIZE_QUERY) == FTS5_TOKENIZE_QUERY) {
        tokenFlags = tokenFlags | FTS5_TOKEN_COLOCATED;
    }

    int prevOffset = 0;
    int nextOffset = nextUTF8CharOffset((const unsigned char *) text, prevOffset);

    while (nextOffset > 0 && nextOffset <= length) {

        int tokenLength = nextOffset - prevOffset;
        const char *token = text + prevOffset;

        returnCode = tokenCallback(context, tokenFlags, token, tokenLength, prevOffset, nextOffset);

        if (returnCode != SQLITE_OK) {
            break;
        }

        prevOffset = nextOffset;
        nextOffset = nextUTF8CharOffset((const unsigned char *) text, prevOffset);
    }

    return returnCode;
}

static int Tokenizer_create(void *context, const char **argv, int argc, Fts5Tokenizer **FTS5Tokenizer) {

    Tokenizer *tokenizer = sqlite3_malloc(sizeof(Tokenizer));
    tokenizer->context = context;

    *FTS5Tokenizer = (void *) tokenizer;

    return SQLITE_OK;
}

static void Tokenizer_delete(Fts5Tokenizer *tokenizer) {
    sqlite3_free(tokenizer);
}

fts5_tokenizer LiteralTokenizer = { Tokenizer_create, Tokenizer_delete, LiteralTokenizer_tokenize };
fts5_tokenizer KanjiTokenizer = { Tokenizer_create, Tokenizer_delete, KanjiTokenizer_tokenize };
