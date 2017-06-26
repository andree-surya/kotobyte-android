
require_relative 'word'
require_relative 'kanji'
require 'json'
require 'mojinizer'
require 'sqlite3'

CREATE_TABLES = <<-EOS

  create table words (
    id integer primary key,
    json text not null
  );

  create table kanji (
    id integer primary key,
    json text not null
  );
EOS

BUILD_INDEXES = <<-EOS
  create virtual table literals_fts using fts5(text, word_id unindexed, priority unindexed, prefix='1 2 3 4 5');
  create virtual table senses_fts using fts5(text, word_id unindexed, tokenize='porter');
  create virtual table kanji_fts using fts5(text, kanji_id unindexed);

  insert into literals_fts select substr(value, 2), words.id, substr(value, 1, 1) from words, json_each(words.json, '$[0]') where type = 'text';
  insert into literals_fts select substr(value, 2), words.id, substr(value, 1, 1) from words, json_each(words.json, '$[1]') where type = 'text';
  insert into senses_fts select json_extract(value, '$[0]'), words.id from words, json_each(words.json, '$[2]');
  insert into kanji_fts select json_extract(json, '$[0]'), kanji.id from kanji;
EOS

SEARCH_LITERALS = <<-EOS
  select word_id, highlight(literals_fts, 0, '{', '}') highlight, rank * priority score
    from literals_fts(?) order by score limit ?
EOS

SEARCH_SENSES = <<-EOS
  select word_id, highlight(senses_fts, 0, '{', '}') highlight, rank score
    from senses_fts(?) order by score limit ?
EOS

SEARCH_WORDS = <<-EOS
  with search_results as (%s)
    select id, json, group_concat(highlight, ';') highlights, min(score) score
    from words join search_results on (id = word_id) group by id order by score;
EOS

SEARCH_KANJI = <<-EOS
  select id, json from kanji join kanji_fts(?) on (id = kanji_id) limit ?;
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
    @database.results_as_hash = true

    @database.execute_batch CREATE_TABLES if should_reset_structures
  end

  def transaction
    @database.transaction { yield self }
  end

  def build_indexes
    @database.execute_batch BUILD_INDEXES
  end

  def optimize
    @database.execute 'VACUUM'
  end

  def insert_word(word)
    @insert_word ||= @database.prepare('insert into words values (?, ?)')
    @insert_word.execute(word.id, json_from_word(word))
  end

  def insert_kanji(kanji)
    @insert_kanji ||= @database.prepare('insert into kanji values (?, ?)')
    @insert_kanji.execute(kanji.id, json_from_kanji(kanji))
  end

  def search_words(query)

    if query.contains_japanese?
      results = search_words_by_literals(query, 50)

    else
      results = search_words_by_senses(query, 50)

      if results.size <= 10
        literal_results = []

        literal_results += search_words_by_literals(query.hiragana, 20)
        literal_results += search_words_by_literals(query.katakana, 20)
        literal_results.sort! { |r1, r2| r1['score'] <=> r2['score'] }

        results += literal_results
      end
    end

    results.each { |r| r['json'] = r['json'][0...32] }
    results
  end

  def search_kanji(query, limit = 5)
    results = []

    tokens = query.chars.select { |c| c.kanji? }

    unless query.empty?
      @search_kanji ||= @database.prepare(SEARCH_KANJI)
      @search_kanji.execute(tokens.join(' OR '), 10).each { |h| results << h }
    end

    results
  end

  private

    def search_words_by_literals(query, limit)
      results = []

      query = query.chars.select { |c| c.kanji? || c.kana? }.join
      tokens = 1.upto(query.size).map { |l| query[0...l] << '*' } << query

      @search_literals ||= @database.prepare(SEARCH_WORDS % SEARCH_LITERALS)
      @search_literals.execute(tokens.join(' OR '), limit).each { |h| results << h }

      results
    end

    def search_words_by_senses(query, limit)
      results = []

      @search_senses ||= @database.prepare(SEARCH_WORDS % SEARCH_SENSES)
      @search_senses.execute(query, limit).each { |h| results << h }

      results
    end

    def json_from_word(word)
      [
        word.literals&.map { |l| "#{l.priority}#{l.text}" } || 0,
        word.readings.map { |r| "#{r.priority}#{r.text}" },

        word.senses.map do |s|
          [
            s.texts.join(', '),
            s.categories&.join(';') || 0,
            s.origins&.join(';') || 0,
            s.labels&.join(';') || 0,
            s.notes&.join(';') || 0
          ]
        end

      ].to_json
    end

    def json_from_kanji(kanji)
      [
        kanji.character,
        kanji.readings&.join(';') || 0,
        kanji.meanings&.join(';') || 0,
        kanji.jlpt || 0,
        kanji.grade || 0,
        kanji.strokes&.join(';') || 0

      ].to_json
    end
end
