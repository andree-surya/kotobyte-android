
require_relative 'sense'

class Word
  attr_accessor :id
  attr_accessor :priority
  attr_accessor :literals
  attr_accessor :readings
  attr_accessor :senses

  def initialize
    yield self if block_given?
  end
end
