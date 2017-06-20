
require_relative 'ngram_tokenizer'
require_relative 'word'
require_relative 'kanji'
require 'mojinizer'
require 'sqlite3'

CREATE_TABLE_STATEMENTS = <<-EOS

  CREATE TABLE words (
      id INTEGER PRIMARY KEY,
      priority INT NOT NULL,
      encoded TEXT NOT NULL
  );

  CREATE TABLE kanji (
      id INTEGER PRIMARY KEY,
      encoded TEXT NOT NULL
  );
EOS

DELIMITER_L1 = '⋮'
DELIMITER_L2 = '¦'
DELIMITER_L3 = '†'
DELIMITER_L4 = '‡'

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

  def insert_word(word)
    @insert_word ||= @database.prepare('INSERT INTO words VALUES (?, ?, ?)')
    @insert_word.execute(pack_word_for_recording(word))
  end

  def insert_kanji(kanji)
    @insert_kanji ||= @database.prepare('INSERT INTO kanji VALUES (?, ?)')
    @insert_kanji.execute(pack_kanji_for_recording(kanji))
  end

  private

    def pack_word_for_recording(word)
      [
        word.id,
        word.priority,

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
          end.join(DELIMITER_L3)
        ].join(DELIMITER_L4)
      ]
    end

    def pack_kanji_for_recording(kanji)
      [
        kanji.id,

        [
          kanji.id,
          kanji.literal,
          kanji.readings&.join(DELIMITER_L1),
          kanji.meanings&.join(DELIMITER_L1),
          kanji.jlpt,
          kanji.grade,
          kanji.strokes&.join(DELIMITER_L1)

        ].join(DELIMITER_L4)
      ]
    end
end
