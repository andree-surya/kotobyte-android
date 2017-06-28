#!/usr/bin/env ruby

require_relative 'models/dictionary_database'
require_relative 'models/kanji_source_reader'
require_relative 'models/words_source_reader'
require 'yaml'

WORDS_SOURCE_FILE = File.expand_path('data/jmdict_e.xml', __dir__)
KANJI_SOURCE_FILE = File.expand_path('data/kanjidic2.xml', __dir__)
KANJI_STROKES_FILE = File.expand_path('data/kanjivg.xml', __dir__)
EXTRAS_FILE = File.expand_path('data/extras.yaml', __dir__)

abort 'Please provide dictionary file name.' if ARGV[0].nil?

database_file_name = ARGV[0]
should_build_indexes = 'index' == ARGV[1]

database = DictionaryDatabase.new(database_file_name, reset: true)

words_source_reader = WordsSourceReader.new(
    source_xml: IO.read(WORDS_SOURCE_FILE)
)

kanji_source_reader = KanjiSourceReader.new(
    source_xml: IO.read(KANJI_SOURCE_FILE),
    strokes_xml: IO.read(KANJI_STROKES_FILE)
)

extras = YAML.load_file(EXTRAS_FILE)

database.transaction do |db|

  words_source_reader.read_each { |w| db.insert_word(w) }
  kanji_source_reader.read_all.each { |k| db.insert_kanji(k) }

  extras['labels'].each { |k, v| db.insert_label(k, v) }
  extras['languages'].each { |k, v| db.insert_language(k, v) }
  extras['jlpt'].each { |k, v| db.insert_jlpt(k, v) }
  extras['grades'].each { |k, v| db.insert_grade(k, v) }

  database.build_indexes if should_build_indexes
end

database.optimize
