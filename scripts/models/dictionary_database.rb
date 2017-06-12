
require_relative 'ngram_tokenizer'
require_relative 'word'
require_relative 'kanji'
require 'mojinizer'
require 'sqlite3'

CREATE_TABLE_STATEMENTS = <<-EOS

  CREATE TABLE words (
      literals TEXT,
      readings TEXT NOT NULL,
      senses TEXT NOT NULL,
      priority INT NOT NULL
  );

  CREATE TABLE kanji (
      literal TEXT NOT NULL,
      readings TEXT,
      meanings TEXT,
      jlpt INTEGER,
      grade INTEGER,
      strokes TEXT
  );

  CREATE VIRTUAL TABLE words_japanese_fts5
      USING fts5(literals, readings, content='');

  CREATE VIRTUAL TABLE words_alphabet_fts5
      USING fts5(readings, senses, content='');

  CREATE VIRTUAL TABLE kanji_japanese_fts5
      USING fts5(literal, content='');

  INSERT INTO words_japanese_fts5
      (words_japanese_fts5, rank) VALUES ('rank', 'bm25(2, 1)');

  INSERT INTO words_alphabet_fts5
      (words_alphabet_fts5, rank) VALUES ('rank', 'bm25(2, 1)');
EOS

DELIMITER_L1 = '⋮'
DELIMITER_L2 = '¦'
DELIMITER_L3 = '¶'

class DictionaryDatabase

  def initialize(database_path, reset: false)
    should_reset_structures = true

    if File.exists? database_path
      if reset
        File.delete database_path
      else
        should_reset_structures = false
      end
    end

    @database = SQLite3::Database.new(database_path)
    @japanese_tokenizer = NGramTokenizer.new(size: 3)

    if should_reset_structures
      @database.execute_batch CREATE_TABLE_STATEMENTS
    end
  end

  def transaction
    @database.transaction { yield self }
  end

  def optimize_indexes
    @database.execute_batch <<-EOS
        INSERT INTO words_japanese_fts5 (words_japanese_fts5) VALUES ('optimize');
        INSERT INTO words_alphabet_fts5 (words_alphabet_fts5) VALUES ('optimize');
        INSERT INTO kanji_japanese_fts5 (kanji_japanese_fts5) VALUES ('optimize');
    EOS
  end

  def insert_word(word)

    @insert_word ||= @database.prepare <<-EOS
      INSERT INTO words
          (rowid, literals, readings, senses, priority) VALUES (?, ?, ?, ?, ?);
    EOS

    @index_word_in_japanese ||= @database.prepare <<-EOS
      INSERT INTO words_japanese_fts5
          (rowid, literals, readings) VALUES (?, ?, ?);
    EOS

    @index_word_in_alphabet ||= @database.prepare <<-EOS
      INSERT INTO words_alphabet_fts5
          (rowid, readings, senses) VALUES (?, ?, ?);
    EOS

    @insert_word.execute(pack_word_for_recording(word))
    @index_word_in_japanese.execute(pack_word_for_japanese_indexing(word))
    @index_word_in_alphabet.execute(pack_word_for_alphabet_indexing(word))
  end

  def insert_kanji(kanji)

    @insert_kanji ||= @database.prepare <<-EOS
      INSERT INTO kanji
          (rowid, literal, readings, meanings, jlpt, grade, strokes)
          VALUES (?, ?, ? , ?, ?, ?, ?);
    EOS

    @index_kanji_in_japanese ||= @database.prepare <<-EOS
      INSERT INTO kanji_japanese_fts5
          (rowid, literal) VALUES (?, ?);
    EOS

    @insert_kanji.execute(pack_kanji_for_recording(kanji))
    @index_kanji_in_japanese.execute(pack_kanji_for_indexing(kanji))
  end

  def search_words(query)

    if query.contains_japanese?
      rows = search_words_with_japanese(query)
    else
      rows = search_words_with_alphabet(query)
    end

    rows.map { |row| unpack_word_from_record(row) }
  end

  def search_kanji(query)
    tokens = query.chars.select { |c| c.kanji? }
    results = []

    unless tokens.empty?
      @search_kanji ||= @database.prepare <<-EOS
        SELECT kanji.rowid, kanji.*, rank AS match_score
            FROM kanji INNER JOIN kanji_japanese_fts5
            ON kanji.rowid = kanji_japanese_fts5.rowid
            WHERE kanji_japanese_fts5 MATCH ?
            ORDER BY match_score
            LIMIT 100;
      EOS

      rows = @search_kanji.execute(escape_and_join_query_tokens(tokens))
      results = rows.map { |row| unpack_kanji_from_record(row) }
    end

    results
  end

  private

    def pack_word_for_recording(word)
      [
        word.id,
        word.literals&.join(DELIMITER_L1),
        word.readings&.join(DELIMITER_L1),

        word.senses.map do |s|
          [
            s.texts&.join(DELIMITER_L1),
            s.categories&.join(DELIMITER_L1),
            s.sources&.join(DELIMITER_L1),
            s.labels&.join(DELIMITER_L1),
            s.notes&.join(DELIMITER_L1)

          ].join(DELIMITER_L2)
        end.join(DELIMITER_L3),

        word.priority
      ]
    end

    def unpack_word_from_record(record)
      Word.new do |w|
        w.id = record[0]
        w.literals = record[1]&.split(DELIMITER_L1)
        w.readings = record[2]&.split(DELIMITER_L1)

        w.senses = record[3]&.split(DELIMITER_L3).map do |sense_row|
          sense_row = sense_row.split(DELIMITER_L2)

          Sense.new do |s|
            s.texts = sense_row[0]&.split(DELIMITER_L1)
            s.categories = sense_row[1]&.split(DELIMITER_L1)
            s.sources = sense_row[2]&.split(DELIMITER_L1)
            s.labels = sense_row[3]&.split(DELIMITER_L1)
            s.notes = sense_row[4]&.split(DELIMITER_L1)
          end
        end

        w.priority = record[4]
        w.match_score = record[5]
      end
    end

    def pack_word_for_japanese_indexing(word)
      [
        word.id,
        word.literals&.map { |l| @japanese_tokenizer.tokenize(l[1..-1]) }&.join(DELIMITER_L1),
        word.readings&.map { |l| @japanese_tokenizer.tokenize(l[1..-1]) }&.join(DELIMITER_L1)
      ]
    end

    def pack_word_for_alphabet_indexing(word)
      [
        word.id,
        word.readings&.map { |r| r.romaji }&.join(DELIMITER_L1),
        word.senses&.map { |s| s.texts&.join(DELIMITER_L1) }&.join(DELIMITER_L2)
      ]
    end

    def pack_kanji_for_recording(kanji)
      [
        kanji.id,
        kanji.literal,
        kanji.readings&.join(DELIMITER_L1),
        kanji.meanings&.join(DELIMITER_L1),
        kanji.jlpt,
        kanji.grade,
        kanji.strokes&.join(DELIMITER_L1)
      ]
    end

    def unpack_kanji_from_record(record)
      Kanji.new do |k|
        k.id = record[0]
        k.literal = record[1]
        k.readings = record[2]&.split(DELIMITER_L1)
        k.meanings = record[3]&.split(DELIMITER_L1)
        k.jlpt = record[4]
        k.grade = record[5]
        k.strokes = record[6]&.split(DELIMITER_L1)
        k.match_score = record[7]
      end
    end

    def pack_kanji_for_indexing(kanji)
      [
        kanji.id,
        kanji.literal
      ]
    end

    def search_words_with_japanese(query)
      tokens = @japanese_tokenizer.tokenize(query)

      @search_words_with_japanese ||= @database.prepare <<-EOS
        SELECT words.rowid, words.*, rank * words.priority AS match_score
            FROM words INNER JOIN words_japanese_fts5
            ON words.rowid = words_japanese_fts5.rowid
            WHERE words_japanese_fts5 MATCH ?
            ORDER BY match_score
            LIMIT 100;
      EOS

      @search_words_with_japanese.execute(escape_and_join_query_tokens(tokens))
    end

    def search_words_with_alphabet(query)
      tokens = query.gsub(/[^a-z]/i, ' ').split(' ')

      @search_words_with_alphabet ||= @database.prepare <<-EOS
        SELECT words.rowid, words.*, rank * words.priority AS match_score
            FROM words INNER JOIN words_alphabet_fts5
            ON words.rowid = words_alphabet_fts5.rowid
            WHERE words_alphabet_fts5 MATCH ?
            ORDER BY match_score
            LIMIT 100;
      EOS

      @search_words_with_alphabet.execute(escape_and_join_query_tokens(tokens))
    end

    def escape_and_join_query_tokens(tokens)

      tokens.map do |token|

        # Escape double quotes and surround token with double quotes.
        "\"#{token.tr('"', '""')}\""

      end.join(' OR ')
    end
end
