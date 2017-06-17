#!/usr/bin/env ruby

abort 'Usage: scripts/create_database.rb <database_file>' if ARGV.empty?

require_relative 'models/dictionary_database'
require_relative 'models/kanji_source_reader'
require_relative 'models/words_source_reader'

WORDS_SOURCE_FILE = File.expand_path('data/jmdict_e.xml', __dir__)
KANJI_SOURCE_FILE = File.expand_path('data/kanjidic2.xml', __dir__)
KANJI_STROKES_FILE = File.expand_path('data/kanjivg.xml', __dir__)

database = DictionaryDatabase.new(ARGV[0], reset: true)

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
