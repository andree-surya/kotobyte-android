
require_relative '../models/dictionary_database'
require_relative '../models/words_source_reader'
require_relative 'fixtures/constants'

describe DictionaryDatabase do
  let(:database_file) { ':memory:' }

  describe '#initialize' do
    let(:database_file) { File.expand_path('../temp/db_test.sqlite3', __dir__) }

    before(:each) do
        File.delete(database_file) if File.exists? database_file
    end

    it 'should open database file without modification by default' do
      dummy_text = 'This is a dummy text'
      IO.write(database_file, dummy_text)

      DictionaryDatabase.new(database_file)
      expect(IO.read(database_file)).to eq(dummy_text)
    end

    it 'should reset database file if requested' do
      dummy_text = 'This is a dummy text'
      IO.write(database_file, dummy_text)

      DictionaryDatabase.new(database_file, reset: true)
      expect(IO.read(database_file)).not_to eq(dummy_text)
    end
  end

  describe '#insert_word' do
    let(:database) { DictionaryDatabase.new(database_file) }

    it 'should insert all words in the test source file without error' do

      words_source_reader = WordsSourceReader.new(
          source_xml: IO.read(WORDS_SOURCE_FILE)
      )

      database.transaction do |db|
        words_source_reader.read_each { |word| db.insert_word(word) }
      end
    end
  end

  describe '#insert_kanji' do
    let(:database) { DictionaryDatabase.new(database_file) }

    it 'should insert all Kanji in the test source file without error' do

      kanji_source_reader = KanjiSourceReader.new(
          source_xml: IO.read(KANJI_SOURCE_FILE),
          strokes_xml: IO.read(KANJI_STROKES_FILE)
      )

      database.transaction do |db|
        kanji_source_reader.read_all.map { |kanji| db.insert_kanji(kanji) }
      end
    end
  end
end
