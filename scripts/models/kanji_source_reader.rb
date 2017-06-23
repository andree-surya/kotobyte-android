
require_relative 'kanji'
require 'nokogiri'

class KanjiSourceReader

  def initialize(source_xml: '<kanjidic2 />', strokes_xml: '<kanjivg />')
    @source_xml = source_xml
    @strokes_xml = strokes_xml
  end

  def read_one
    read_all.first
  end

  def read_all
    kanji_hash = {}

    read_each_from_source_xml do |kanji|
      kanji_hash[kanji.id] = kanji
    end

    read_each_from_strokes_xml do |kanji_id, strokes|
      kanji_hash[kanji_id]&.strokes = strokes
    end

    kanji_hash.values
  end

  private

    def read_each_from_source_xml(&kanji_handler)

      Nokogiri::XML::Reader(@source_xml).each do |node|

        if node.node_type == Nokogiri::XML::Reader::TYPE_ELEMENT
          handle_source_xml_start_element(node)

        elsif node.node_type == Nokogiri::XML::Reader::TYPE_END_ELEMENT
          handle_source_xml_end_element(node, &kanji_handler)
        end
      end
    end

    def read_each_from_strokes_xml(&kanji_strokes_handler)

      Nokogiri::XML::Reader(@strokes_xml).each do |node|

        if node.node_type == Nokogiri::XML::Reader::TYPE_ELEMENT
          handle_strokes_xml_start_element(node)

        elsif node.node_type == Nokogiri::XML::Reader::TYPE_END_ELEMENT
          handle_strokes_xml_end_element(node, &kanji_strokes_handler)
        end
      end
    end

    def handle_source_xml_start_element(node)

      case node.name
      when 'character'
        @current_kanji = Kanji.new

      when 'cp_value'
        if node.attributes['cp_type'] == 'ucs'
          @current_kanji.id = node.inner_xml.to_i(16)
        end

      when 'literal'
        @current_kanji.character = node.inner_xml

      when 'grade'
        @current_kanji.grade = node.inner_xml.to_i

      when 'jlpt'
        @current_kanji.jlpt = node.inner_xml.to_i

      when 'reading'
        type = node.attributes['r_type']

        if type == 'ja_on' || type == 'ja_kun'
          @current_kanji.readings ||= []
          @current_kanji.readings << node.inner_xml
        end

      when 'meaning'

        if node.attributes['m_lang'].nil?
          @current_kanji.meanings ||= []
          @current_kanji.meanings << node.inner_xml
        end
      end
    end

    def handle_source_xml_end_element(node, &kanji_handler)

      if node.name == 'character'
        yield @current_kanji

        @current_kanji = nil
      end
    end

    def handle_strokes_xml_start_element(node)

      case node.name
      when 'kanji'
        @current_kanji_id = node.attributes['id'][-5..-1].to_i(16)
        @current_strokes = []

      when 'path'
        @current_strokes << node.attributes['d']
      end
    end

    def handle_strokes_xml_end_element(node, &kanji_strokes_handler)

      if node.name == 'kanji'
        yield @current_kanji_id, @current_strokes

        @current_kanji_id = nil
        @current_strokes = nil
      end
    end
end
