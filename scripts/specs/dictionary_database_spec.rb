
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

  describe '#search_words' do
    let(:database) { DictionaryDatabase.new(database_file) }

    before(:each) do
      words_source_reader = WordsSourceReader.new(source_xml: IO.read(WORDS_SOURCE_FILE))

      database.transaction do |db|
        words_source_reader.read_each { |word| db.insert_word(word) }
      end

      database.optimize_indexes
    end

    it 'should be able to search with Japanese literals' do
      words = database.search_words('やっと実行したか！')

      expect(words).not_to be_empty
      expect(words.first.literals.join).to include('実行')
    end

    it 'should be able to search with Japanese readings' do
      words = database.search_words('ことばって、なんのことば？')

      expect(words).not_to be_empty
      expect(words.first.readings.join).to include('ことば')
    end

    it 'should be able to search with Romanized Japanese readings' do
      words = database.search_words('So, what do you mean by "Gendou"?')

      expect(words).not_to be_empty
      expect(words.first.readings.first).to include('げんどう')
    end

    it 'should be able to search with English meanings' do
      words = database.search_words('Let\'s put this into action!!')

      expect(words).not_to be_empty
      expect(words.first.senses.first.texts.first).to include('action')
    end
  end

  describe '#search_kanji' do
    let(:database) { DictionaryDatabase.new(database_file) }

    before(:each) do
      kanji_source_reader = KanjiSourceReader.new(
          source_xml: IO.read(KANJI_SOURCE_FILE),
          strokes_xml: IO.read(KANJI_STROKES_FILE)
      )

      database.transaction do |db|
        kanji_source_reader.read_all.map { |kanji| db.insert_kanji(kanji) }
      end
    end

    it 'should be able to look up Kanji characters in the given query' do
      kanji_list = database.search_kanji('あいつ露西亜に行ったんだ。')

      expect(kanji_list).not_to be_empty
      expect(kanji_list.first.literal).to eq('亜')
    end
  end
end
