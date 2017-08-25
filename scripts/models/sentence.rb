
class Sentence
  attr_accessor :id
  attr_accessor :original
  attr_accessor :tokenized
  attr_accessor :translated

  def initialize
    yield self if block_given?
  end
end
