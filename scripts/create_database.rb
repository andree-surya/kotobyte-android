#!/usr/bin/env ruby

require_relative 'models/dictionary_database'
require_relative 'models/kanji_source_reader'
require_relative 'models/words_source_reader'

WORDS_SOURCE_FILE = 'seeds/jmdict_e.xml'
KANJI_SOURCE_FILE = 'seeds/kanjidic2.xml'
KANJI_STROKES_FILE = 'seeds/kanjivg.xml'
DATABASE_FILE = 'temp/dictionary_v1.sqlite3'

database = DictionaryDatabase.new(DATABASE_FILE, reset: true)

words_source_reader = WordsSourceReader.new(
    source_xml: IO.read(WORDS_SOURCE_FILE)
)

kanji_source_reader = KanjiSourceReader.new(
    source_xml: IO.read(KANJI_SOURCE_FILE),
    strokes_xml: IO.read(KANJI_STROKES_FILE)
)

database.transaction do |db|
  words_source_reader.read_each { |w| db.insert_word(w) }
  kanji_source_reader.read_all.each { |k| db.insert_kanji(k) }
end

database.optimize_indexes
