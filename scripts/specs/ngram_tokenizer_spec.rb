
require_relative '../models/ngram_tokenizer'

describe NGramTokenizer do
  let(:string) { '冷たいお茶' }
  let(:n1_tokens) { ['冷', 'た', 'い', 'お', '茶'] }
  let(:n2_tokens) { ['冷た', 'たい', 'いお', 'お茶'] }
  let(:n3_tokens) { ['冷たい', 'たいお', 'いお茶'] }

  it 'should be able to process N of size 1' do
    tokens = NGramTokenizer.new(size: 1).tokenize(string)

    expect(tokens).to include(*n1_tokens)
  end

  it 'should be able to process N of size 2' do
    tokens = NGramTokenizer.new(size: 2).tokenize(string)

    expect(tokens).to include(*n1_tokens)
    expect(tokens).to include(*n2_tokens)
  end

  it 'should be able to process N of size 3' do
    tokens = NGramTokenizer.new(size: 3).tokenize(string)

    expect(tokens).to include(*n1_tokens)
    expect(tokens).to include(*n2_tokens)
    expect(tokens).to include(*n3_tokens)
  end
end
