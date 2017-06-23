
class Sense
  attr_accessor :texts
  attr_accessor :categories # Lexical categories
  attr_accessor :origins # Language origins
  attr_accessor :labels
  attr_accessor :notes

  def initialize
    yield self if block_given?
  end
end
