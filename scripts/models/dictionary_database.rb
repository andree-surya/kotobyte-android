
require_relative 'word'
require_relative 'kanji'
require 'mojinizer'
require 'sqlite3'

CREATE_TABLES = <<-EOS

  CREATE TABLE literals (
    word_id INTEGER NOT NULL,
    priority INTEGER NOR NULL,
    text TEXT NOT NULL
  );

  CREATE TABLE senses (
    word_id INTEGER NOT NULL,
    text TEXT NOT NULL,
    categories TEXT,
    origins TEXT,
    labels TEXT,
    notes TEXT
  );

  CREATE TABLE kanji (
    id INTEGER PRIMARY KEY,
    character TEXT NOT NULL,
    readings TEXT,
    meanings TEXT,
    jlpt INTEGER,
    grade INTEGER,
    strokes TEXT
  );

  CREATE INDEX literals_word_id ON literals(word_id);
  CREATE INDEX senses_word_id ON senses(word_id);
EOS

BUILD_INDEXES = <<-EOS
  CREATE INDEX literals_word_id ON literals(word_id);
  CREATE INDEX senses_word_id ON senses(word_id);

  CREATE VIRTUAL TABLE literals_fts USING fts5(text, content='literals');
  CREATE VIRTUAL TABLE senses_fts USING fts5(text, content='senses', tokenize='porter unicode61');
  CREATE VIRTUAL TABLE kanji_fts USING fts5(character, content='kanji');

  INSERT INTO literals_fts (literals_fts) VALUES ('rebuild');
  INSERT INTO senses_fts (senses_fts) VALUES ('rebuild');
  INSERT INTO kanji_fts (kanji_fts) VALUES ('rebuild');
EOS

SEARCH_LITERALS = <<-EOS
  SELECT word_id FROM literals_fts(:query) INNER JOIN literals ON (literals_fts.rowid = literals.rowid) GROUP BY word_id ORDER BY MIN(rank * priority) LIMIT :limit
EOS

SEARCH_SENSES = <<-EOS
  SELECT word_id FROM senses_fts(:query) INNER JOIN senses ON (senses_fts.rowid = senses.rowid) GROUP BY word_id ORDER BY MIN(rank) LIMIT :limit
EOS

SEARCH_AND_ENCODE_WORD = <<-EOS
  WITH
    search_results AS (%s),
    word_literals AS (SELECT word_id, GROUP_CONCAT(priority || text, ']') text FROM literals WHERE word_id IN search_results GROUP BY word_id),
    word_senses AS (SELECT word_id, GROUP_CONCAT(text || '}' || IFNULL(categories, '') || '}' || IFNULL(origins, '') || '}' || IFNULL(labels, '') || '}' || IFNULL(notes, ''), '>') text FROM senses WHERE word_id IN search_results GROUP BY word_id )

  SELECT (search_results.word_id || '_' || word_literals.text || '_' || word_senses.text) text
  FROM search_results
  INNER JOIN word_literals ON (search_results.word_id = word_literals.word_id)
  INNER JOIN word_senses ON (search_results.word_id = word_senses.word_id)
EOS

SEARCH_AND_ENCODE_KANJI = <<-EOS
  SELECT kanji.id || '_' || kanji.character || '_' || IFNULL(readings, '') || '_' || IFNULL(meanings, '') || '_' || IFNULL(jlpt, '') || '_' || IFNULL(grade, '') || '_' || IFNULL(strokes, '') text
  FROM kanji_fts(:query)
  INNER JOIN kanji ON (kanji_fts.rowid = kanji.rowid)
  ORDER BY rank
  LIMIT :limit
EOS

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
    @database.execute_batch CREATE_TABLES if should_reset_structures
  end

  def transaction
    @database.transaction { yield self }
  end

  def build_indexes
    @database.execute_batch BUILD_INDEXES
  end

  def insert_word(word)
    @insert_literal ||= @database.prepare('INSERT INTO literals VALUES (?, ?, ?)')
    @insert_sense ||= @database.prepare('INSERT INTO senses VALUES (?, ?, ?, ?, ?, ?)')

    word.literals.each do |literal|
      @insert_literal.execute([
        word.id,
        literal.priority,
        literal.text,
      ])
    end

    word.senses.each do |sense|
      @insert_sense.execute([
        word.id,
        sense.texts.join(']'),
        sense.categories&.join(']'),
        sense.origins&.join(']'),
        sense.labels&.join(']'),
        sense.notes&.join(']')
      ])
    end
  end

  def insert_kanji(kanji)
    @insert_kanji ||= @database.prepare('INSERT INTO kanji VALUES (?, ?, ?, ?, ?, ?, ?)')

    @insert_kanji.execute([
      kanji.id,
      kanji.character,
      kanji.readings&.join(']'),
      kanji.meanings&.join(']'),
      kanji.jlpt,
      kanji.grade,
      kanji.strokes&.join(']')
    ])
  end

  def search_words(query)
    results = []

    if query.contains_japanese?
      statement = @search_literals ||= @database.prepare(SEARCH_AND_ENCODE_WORD % SEARCH_LITERALS)
    else
      statement = @search_senses ||= @database.prepare(SEARCH_AND_ENCODE_WORD % SEARCH_SENSES)
    end

    statement.execute(query: query, limit: 50).each_hash { |r| results << r['text'] }

    results
  end

  def search_kanji(query)
    results = []

    @search_kanji ||= @database.prepare(SEARCH_AND_ENCODE_KANJI)
    @search_kanji.execute(query: query, limit: 50).each_hash { |r| results << r['text'] }

    results
  end
end
