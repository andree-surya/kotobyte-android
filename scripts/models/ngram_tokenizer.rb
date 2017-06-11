
class NGramTokenizer

  def initialize(size:)
    @size = size
  end

  def tokenize(string)
    string = string.strip
    tokens = []

    @size.downto(1) do |token_size|
      0.upto(string.size - token_size) do |index|
        tokens << string[index, token_size]
      end
    end

    tokens
  end
end
