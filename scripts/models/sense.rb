
class Sense
  attr_accessor :texts
  attr_accessor :categories # Lexical categories
  attr_accessor :origins # Language origins
  attr_accessor :labels
  attr_accessor :notes

  def initialize
    yield self if block_given?
  end

  def as_hash
    {
      t: texts,
      c: categories,
      o: origins,
      l: labels,
      n: notes
      
    }.delete_if { |k, v| v.nil? }
  end
end
