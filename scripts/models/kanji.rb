
class Kanji
  attr_accessor :id
  attr_accessor :literal
  attr_accessor :readings
  attr_accessor :meanings
  attr_accessor :jlpt
  attr_accessor :grade
  attr_accessor :strokes

  def initialize
    yield self if block_given?
  end
end
