
require_relative '../models/sentences_source_reader'
require_relative 'fixtures/constants'

describe SentencesSourceReader do

  it 'should parse test data without error' do
    reader = SentencesSourceReader.new(
        source_csv: IO.read(SENTENCES_SOURCE_FILE),
        indices_csv: IO.read(SENTENCES_INDICES_FILE)
    )

    sentences = reader.read_all
    expect(sentences.count).to eq(6)

    sentences.each do |sentence|
      expect(sentence.id).not_to be_nil
      expect(sentence.original).not_to be_nil
      expect(sentence.tokenized).not_to be_nil
      expect(sentence.translated).not_to be_nil
    end
  end

  it 'should map Japanese text to English' do

    source_csv = <<~EOS
      77243	jpn	老齢人口は、健康管理にますます多くの出費が必要となるだろう。
      326476	eng	An aging population will require more spending on health care.
      79614	jpn	野原で踊りたい気分です。
      324104	eng	I feel like dancing in the fields.
    EOS

    indices_csv = <<~EOS
      77243	326476	老齢 人口[01] は|1 健康管理 に 益々{ますます} 多く の 出費 が 必要 となる だろう
      79614	324104	野原 で 踊る{踊り} たい[01] 気分 です
    EOS

    reader = SentencesSourceReader.new(source_csv: source_csv, indices_csv: indices_csv)
    sentences = reader.read_all

    expect(sentences.count).to eq(2)
    expect(sentences[0].id).to eq(77243)
    expect(sentences[0].original).to start_with('老齢人口は')
    expect(sentences[0].tokenized).to start_with('老齢 人口')
    expect(sentences[0].translated).to start_with('An aging population')
    expect(sentences[1].id).to eq(79614)
    expect(sentences[1].original).to start_with('野原で踊りたい')
    expect(sentences[1].tokenized).to start_with('野原')
    expect(sentences[1].translated).to start_with('I feel like')
  end

  it 'should skip entry if text mapping is not successful' do

    indices_csv = <<~EOS
      77243	326476	老齢 人口[01] は|1 健康管理 に 益々{ますます} 多く の 出費 が 必要 となる だろう
    EOS

    reader = SentencesSourceReader.new(indices_csv: indices_csv)
    sentences = reader.read_all
    
    expect(sentences).to be_empty
  end

  it 'should remove and adjust annotations in tokenized text' do
    source_csv = <<~EOS
      76974	jpn	「道」という漢字の総画数は何画ですか
      77243	jpn	老齢人口は、健康管理にますます多くの出費が必要となるだろう。
      326746	eng	How many strokes does the kanji for "michi" have?
      326476	eng	An aging population will require more spending on health care.
    EOS

    indices_csv = <<~EOS
      76974	326746	道(みち) と言う{という} 漢字 の 総画数~ は|1 何[01] 画(かく)~ ですか
      77243	326476	老齢 人口[01] は|1 健康管理 に 益々{ますます} 多く の 出費 が 必要 となる だろう
    EOS

    reader = SentencesSourceReader.new(source_csv: source_csv, indices_csv: indices_csv)
    sentences = reader.read_all

    expect(sentences.count).to eq(2)
    expect(sentences[0].tokenized).to eq('道(みち) と言う[という] 漢字 総画数 何 画(かく) ですか')
    expect(sentences[1].tokenized).to eq('老齢 人口 健康管理 益々[ますます] 多く 出費 必要 となる だろう')
  end
end
