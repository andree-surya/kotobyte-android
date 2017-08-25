#!/usr/bin/env ruby

require_relative 'models/dictionary_database'
require_relative 'models/words_source_reader'
require_relative 'models/kanji_source_reader'
require_relative 'models/sentences_source_reader'
require 'yaml'

WORDS_SOURCE_FILE = File.expand_path('data/jmdict_e.xml', __dir__)
KANJI_SOURCE_FILE = File.expand_path('data/kanjidic2.xml', __dir__)
KANJI_STROKES_FILE = File.expand_path('data/kanjivg.xml', __dir__)
SENTENCES_SOURCE_FILE = File.expand_path('data/sentences.csv', __dir__)
SENTENCES_INDICES_FILE = File.expand_path('data/sentences_idx.csv', __dir__)
EXTRAS_FILE = File.expand_path('data/extras.yaml', __dir__)

abort 'Please provide dictionary file name.' if ARGV[0].nil?

database = DictionaryDatabase.new(ARGV[0], reset: true)

words_source_reader = WordsSourceReader.new(
    source_xml: IO.read(WORDS_SOURCE_FILE)
)

kanji_source_reader = KanjiSourceReader.new(
    source_xml: IO.read(KANJI_SOURCE_FILE),
    strokes_xml: IO.read(KANJI_STROKES_FILE)
)

sentences_source_reader = SentencesSourceReader.new(
    source_csv: IO.read(SENTENCES_SOURCE_FILE),
    indices_csv: IO.read(SENTENCES_INDICES_FILE)
)

extras = YAML.load_file(EXTRAS_FILE)

database.transaction do |db|

  words_source_reader.read_each { |w| db.insert_word(w) }
  kanji_source_reader.read_all.each { |k| db.insert_kanji(k) }
  sentences_source_reader.read_all.each { |s| db.insert_sentence(s) }

  extras['labels'].each { |k, v| db.insert_label(k, v) }
  extras['languages'].each { |k, v| db.insert_language(k, v) }
  extras['jlpt'].each { |k, v| db.insert_jlpt(k, v) }
  extras['grades'].each { |k, v| db.insert_grade(k, v) }

  database.build_indexes
end

database.optimize
