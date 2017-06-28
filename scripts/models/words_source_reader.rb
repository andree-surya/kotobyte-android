
require_relative 'word'
require_relative 'sense'
require_relative 'literal'
require 'nokogiri'

class WordsSourceReader
  PRIORITY_CODES = ['ichi', 'news', 'spec', 'gai']
  IRREGULAR_CODES = ['iK', 'ik', 'oK', 'ok', 'io']

  PRIORITY = { low: 0, default: 1, high: 2 }

  def initialize(source_xml: '<JMdict />')
    @xml = source_xml
  end

  def read_one
    read_all.first
  end

  def read_all
    words = []

    read_each do |word|
      words << word
    end

    words
  end

  def read_each(&word_handler)

    Nokogiri::XML::Reader(@xml).each do |node|
      is_start_element = node.node_type == Nokogiri::XML::Reader::TYPE_ELEMENT
      is_end_element = node.node_type == Nokogiri::XML::Reader::TYPE_END_ELEMENT

      if is_start_element
        handle_start_element(node)

      elsif is_end_element
        handle_end_element(node, &word_handler)
      end
    end
  end

  private

    def handle_start_element(node)

      case node.name
      when 'entry'
        @current_word = Word.new

      when 'ent_seq'
        @current_word.id = node.inner_xml.to_i

      when 'k_ele'
        @current_word.literals ||= []
        @current_word.literals << Literal.new
        @current_word.literals.last.priority = PRIORITY[:default]

      when 'r_ele'
        @current_word.readings ||= []
        @current_word.readings << Literal.new
        @current_word.readings.last.priority = PRIORITY[:default]

      when 'keb'
        @current_word.literals.last.text = node.inner_xml

      when 'reb'
        @current_word.readings.last.text = node.inner_xml

      when 'ke_pri'
        handle_literal_priority(@current_word.literals.last, node)

      when 're_pri'
        handle_literal_priority(@current_word.readings.last, node)

      when 'ke_inf'
        handle_literal_irregularity(@current_word.literals.last, node)

      when 're_inf'
        handle_literal_irregularity(@current_word.readings.last, node)

      when 'sense'
        @current_word.senses ||= []
        @current_word.senses << Sense.new

      when 'trans'
        @current_word.senses ||= []
        @current_word.senses << Sense.new
        @current_word.senses.last.categories = ['n']

      when 'pos'
        category = clean_xml_entity(node.inner_xml)

        @current_word.senses.last.categories ||= []
        @current_word.senses.last.categories << category

      when 'field', 'dial', 'misc', 'name_type'
        entity = clean_xml_entity(node.inner_xml)

        if entity != 'unclass'
          @current_word.senses.last.labels ||= []
          @current_word.senses.last.labels << entity
        end

      when 's_inf'
        @current_word.senses.last.notes ||= []
        @current_word.senses.last.notes << node.inner_xml

      when 'lsource'
        origin = node.attributes['lang'] || 'eng'
        origin += ":#{node.inner_xml}" unless node.inner_xml.empty?

        @current_word.senses.last.origins ||= []
        @current_word.senses.last.origins << origin

      when 'gloss', 'trans_det'
        @current_word.senses.last.texts ||= []
        @current_word.senses.last.texts << node.inner_xml
      end
    end

    def handle_end_element(node, &word_handler)

      case node.name
      when 'entry'
        yield @current_word
        @current_word = nil

      when 'sense'
        current_sense = @current_word.senses[-1]
        preceeding_sense = @current_word.senses[-2]

        if current_sense.categories&.empty?
          current_sense.categories = preceeding_sense.categories
        end
      end
    end

    def handle_literal_priority(literal, node)
      priority_code = clean_xml_entity(node.inner_xml).gsub(/\d/, '')

      if PRIORITY_CODES.include? priority_code
        literal.priority = PRIORITY[:high]
      end
    end

    def handle_literal_irregularity(literal, node)
      irregular_code = clean_xml_entity(node.inner_xml)

      if IRREGULAR_CODES.include? irregular_code
        literal.priority = PRIORITY[:low]
      end
    end

    def clean_xml_entity(text)
      text.tr('\&\;-', '')
    end
end
