
class Literal
  attr_accessor :text
  attr_accessor :priority

  def initialize
    yield self if block_given?
  end
end
