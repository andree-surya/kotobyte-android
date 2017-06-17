
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
EOS

DELIMITER_L1 = '⋮'
DELIMITER_L2 = '¦'
DELIMITER_L3 = '‡'

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

    @insert_word ||= @database.prepare <<-EOS
      INSERT INTO words VALUES (?, ?, ?, ?);
    EOS

    @insert_word.execute(pack_word_for_recording(word))
  end

  def insert_kanji(kanji)

    @insert_kanji ||= @database.prepare <<-EOS
      INSERT INTO kanji VALUES (?, ? , ?, ?, ?, ?);
    EOS

    @insert_kanji.execute(pack_kanji_for_recording(kanji))
  end

  def optimize_space
    @database.execute 'VACUUM'
  end

  private

    def pack_word_for_recording(word)
      [
        word.literals&.join,
        word.readings&.join,

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

    def pack_kanji_for_recording(kanji)
      [
        kanji.literal,
        kanji.readings&.join(DELIMITER_L1),
        kanji.meanings&.join(DELIMITER_L1),
        kanji.jlpt,
        kanji.grade,
        kanji.strokes&.join(DELIMITER_L1)
      ]
    end
end
