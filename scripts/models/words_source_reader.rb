
require_relative 'word'
require 'nokogiri'

class WordsSourceReader
  PRIORITY_PREFIXES = ['ichi', 'news', 'spec', 'gai']
  IRREGULAR_CODES = ['iK', 'ik', 'oK', 'ok', 'io']

  PRIORITY = { default: 1, mid: 1.5, high: 2 }

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
        @current_word.priority = PRIORITY[:default]

      when 'ent_seq'
        @current_word.id = node.inner_xml.to_i

      when 'k_ele'
        @current_word.literals ||= []
        @current_word.literals << '='

      when 'r_ele'
        @current_word.readings ||= []
        @current_word.readings << '='

      when 'keb'
        @current_word.literals.last[1..-1] = node.inner_xml

      when 'reb'
        @current_word.readings.last[1..-1] = node.inner_xml

      when 'ke_pri'
        @current_word.literals.last[0] = '+'
        process_priority(node.inner_xml)

      when 're_pri'
        @current_word.readings.last[0] = '+'
        process_priority(node.inner_xml)

      when 'ke_inf'
        if IRREGULAR_CODES.include? clean_xml_entity(node.inner_xml)
          @current_word.literals.last[0] = '-'
        end

      when 're_inf'
        if IRREGULAR_CODES.include? clean_xml_entity(node.inner_xml)
          @current_word.readings.last[0] = '-'
        end

      when 'sense'
        @current_word.senses ||= []
        @current_word.senses << Sense.new

      when 'trans'
        @current_word.senses ||= []
        @current_word.senses << Sense.new
        @current_word.senses.last.categories = ['n']

      when 'pos'
        @current_word.senses.last.categories ||= []
        @current_word.senses.last.categories << clean_xml_entity(node.inner_xml)

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
        source = node.attributes['lang'] || 'eng'
        source += ":#{node.inner_xml}" unless node.inner_xml.empty?

        @current_word.senses.last.sources ||= []
        @current_word.senses.last.sources << source

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

        if current_sense.categories&.empty? && preceeding_sense != nil?
          current_sense.categories = preceeding_sense.categories
        end
      end
    end

    def clean_xml_entity(text)
      text.tr('\&\;', '')
    end

    def process_priority(code)
      PRIORITY_PREFIXES.each do |prefix|

       if code.start_with? prefix
         priority_class = code.sub(prefix, '').to_i

         if priority_class == 1
           priority = PRIORITY[:high]
         else
           priority = PRIORITY[:mid]
         end

         if @current_word.priority < priority
           @current_word.priority = priority
         end
       end
     end
    end
end
