
require_relative 'sentence'

class SentencesSourceReader

  STOP_WORDS = 'へ|か|が|の|に|と|で|や|も|わ|は|さ|よ|ね'

  def initialize(source_csv: '', indices_csv: '')
    @source_csv = source_csv
    @indices_csv = indices_csv
  end

  def read_one
    read_all.first
  end

  def read_all
    sentences = []
    raw_texts = {}

    @source_csv.each_line do |row|
      columns = row.split("\t")
      language = columns[1]

      if language == 'jpn' || language == 'eng'
        raw_texts[columns[0].to_i] = columns[2] 
      end
    end

    @indices_csv.each_line do |row|
      columns = row.split("\t")

      original_id = columns[0].to_i
      translation_id = columns[1].to_i

      original_text = raw_texts[original_id]&.strip
      translated_text = raw_texts[translation_id]&.strip
      tokenized_text = clean_tokenized_text(columns[2])

      next if original_text.nil? || translated_text.nil?

      sentences << Sentence.new do |sentence|
        sentence.id = original_id
        sentence.original = original_text
        sentence.translated = translated_text
        sentence.tokenized = tokenized_text
      end
    end

    sentences
  end

  private

    def clean_tokenized_text(text)
      text.gsub!(/([[:graph:]]+\|\d+)/, '') # Remove particles, e.g. は|1
      text.gsub!(/\([[:graph:]]+\)/, '') # Remove readings, e.g. 時(じ)
      text.gsub!(/\[\d+\]/, '') # Remove number marksers, e.g. から[01]
      text.gsub!(/\b(#{STOP_WORDS})\b/, '') # Remove Japanese stop words.
      text.gsub!(/\s+/, ' ') # Replace consecutive white spaces to a single space.
      text.gsub!('{', '[')
      text.gsub!('}', ']')
      text.gsub!('~', '')
      text.strip!

      text
    end
end
