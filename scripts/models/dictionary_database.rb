
require_relative 'word'
require_relative 'kanji'
require 'mojinizer'
require 'sqlite3'

SEARCH_RESULTS_LIMIT = 50

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
  CREATE VIRTUAL TABLE literals_fts USING fts5(text, content='literals');
  CREATE VIRTUAL TABLE senses_fts USING fts5(text, content='senses', tokenize='porter');
  CREATE VIRTUAL TABLE kanji_fts USING fts5(character, content='kanji');

  INSERT INTO literals_fts (literals_fts) VALUES ('rebuild');
  INSERT INTO senses_fts (senses_fts) VALUES ('rebuild');
  INSERT INTO kanji_fts (kanji_fts) VALUES ('rebuild');
EOS

RESET_SEARCH_RESULTS = <<-EOS
  CREATE TEMP TABLE IF NOT EXISTS search_results (
    id INTEGER PRIMARY KEY, score REAL);

  DELETE FROM search_results;
EOS

SEARCH_WORDS_BY_LITERALS = <<-EOS
  INSERT INTO search_results
    SELECT word_id, MIN(rank * priority) score
    FROM literals l JOIN literals_fts(?) lf ON (l.rowid = lf.rowid)
    GROUP BY word_id ORDER BY score LIMIT ?;
EOS

SEARCH_WORDS_BY_SENSES = <<-EOS
  INSERT INTO search_results
    SELECT word_id, MIN(rank) rank
    FROM senses s JOIN senses_fts(?) sf ON (s.rowid = sf.rowid)
    GROUP BY word_id ORDER BY rank LIMIT ?;
EOS

ENCODE_WORD_SEARCH_RESULTS = <<-EOS
  WITH
    word_literals AS (
      SELECT id, GROUP_CONCAT(priority || text, ']') text
      FROM literals l JOIN search_results sr ON (l.word_id = sr.id) GROUP BY id),

    word_senses AS (
      SELECT id, GROUP_CONCAT(text || '<' || IFNULL(categories, '') || '<' || IFNULL(origins, '') || '<' || IFNULL(labels, '') || '<' || IFNULL(notes, ''), '>') text
      FROM senses s JOIN search_results sr ON (s.word_id = sr.id) GROUP BY id)

  SELECT (sr.id || '_' || wl.text || '_' || ws.text) text
    FROM search_results sr JOIN word_literals wl ON (sr.id = wl.id) JOIN word_senses ws ON (sr.id = ws.id)
    ORDER BY score;
EOS

SEARCH_AND_ENCODE_KANJI = <<-EOS
  SELECT kanji.id || '_' || kanji.character || '_' || IFNULL(readings, '') || '_' || IFNULL(meanings, '') || '_' || IFNULL(jlpt, '') || '_' || IFNULL(grade, '') || '_' || IFNULL(strokes, '') text
    FROM kanji_fts(?) JOIN kanji ON (kanji_fts.rowid = kanji.rowid)
    ORDER BY rank LIMIT ?;
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

    @database.execute_batch RESET_SEARCH_RESULTS

    if query.contains_japanese?
      @search_literals ||= @database.prepare(SEARCH_WORDS_BY_LITERALS)
      @search_literals.execute(query, SEARCH_RESULTS_LIMIT)
    else
      @search_senses ||= @database.prepare(SEARCH_WORDS_BY_SENSES)
      @search_senses.execute(query, SEARCH_RESULTS_LIMIT)
    end

    @encode_words ||= @database.prepare(ENCODE_WORD_SEARCH_RESULTS)
    @encode_words.execute(SEARCH_RESULTS_LIMIT).each_hash { |r| results << r['text'] }

    results
  end

  def search_kanji(query)
    results = []

    @search_kanji ||= @database.prepare(SEARCH_AND_ENCODE_KANJI)
    @search_kanji.execute(query, SEARCH_RESULTS_LIMIT).each_hash { |r| results << r['text'] }

    results
  end

  def execute(sql)
    @database.execute(sql)
  end
end
