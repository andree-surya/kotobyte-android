
require_relative '../models/words_source_reader'
require_relative 'fixtures/constants'

describe WordsSourceReader do

  it 'should parse test data without error' do
    reader = WordsSourceReader.new(source_xml: IO.read(WORDS_SOURCE_FILE))

    words = reader.read_all
    expect(words.count).to eq(5)

    words.each do |word|
      expect(word.id).not_to be_nil
      expect(word.literals).not_to be_empty
    end
  end

  it 'should parse word ID' do

    id = 1234

    xml = "<entry><ent_seq>#{id}</ent_seq></entry>"
    word = WordsSourceReader.new(source_xml: xml).read_one

    expect(word.id).to eq(id)
  end

  it 'should parse literal texts and assign priority' do

    xml = <<-EOS
      <!DOCTYPE JMdict [
        <!ENTITY iK "Irregular Kanji">
      ]>

      <JMdict>
        <entry>
          <k_ele><keb>冗談</keb></k_ele>
          <k_ele><keb>言葉</keb><ke_pri>news2</ke_pri></k_ele>
          <k_ele><keb>匂い</keb><ke_inf>&iK;</ke_inf></k_ele>
        </entry>
      </JMdict>
    EOS

    literals = WordsSourceReader.new(source_xml: xml).read_one.literals

    expect(literals.count).to eq(3)
    expect(literals[0].text).to eq('冗談')
    expect(literals[1].text).to eq('言葉')
    expect(literals[2].text).to eq('匂い')
    expect(literals[0].priority).to eq(1)
    expect(literals[1].priority).to eq(2)
    expect(literals[2].priority).to eq(0)
  end

  it 'should parse readings texts and assign priority' do

    xml = <<-EOS
      <!DOCTYPE JMdict [
        <!ENTITY ok "Outdated Kana usage">
      ]>

      <JMdict>
        <entry>
          <r_ele><reb>ノート</reb></r_ele>
          <r_ele><reb>バグ</reb><re_pri>gai1</re_pri></r_ele>
          <r_ele><reb>みず</reb><re_inf>&ok;</re_inf></r_ele>
        </entry>
      </JMdict>
    EOS

    readings = WordsSourceReader.new(source_xml: xml).read_one.readings

    expect(readings.count).to eq(3)
    expect(readings[0].text).to eq('ノート')
    expect(readings[1].text).to eq('バグ')
    expect(readings[2].text).to eq('みず')
    expect(readings[0].priority).to eq(1)
    expect(readings[1].priority).to eq(2)
    expect(readings[2].priority).to eq(0)
  end

  it 'should parse sense and translation texts' do

    xml = <<-EOS
      <entry>
        <sense>
          <gloss>declaration</gloss>
          <gloss>hearsay</gloss>
        </sense>
        <sense>
          <gloss>conduct</gloss>
        </sense>
        <trans>
          <trans_det>African National Congress</trans_det>
          <trans_det>ANC</trans_det>
        </trans>
      </entry>
    EOS

    senses = WordsSourceReader.new(source_xml: xml).read_one.senses

    expect(senses.size).to eq(3)
    expect(senses[0].texts).to eq(['declaration', 'hearsay'])
    expect(senses[1].texts).to eq(['conduct'])
    expect(senses[2].texts).to eq(['African National Congress', 'ANC'])
  end

  it 'should parse sense categories' do

    xml = <<-EOS
      <!DOCTYPE JMdict [
        <!ENTITY n "noun (common) (futsuumeishi)">
        <!ENTITY adj-i "adjective (keiyoushi)">
        <!ENTITY v5u "godan verb with 'u' ending">
      ]>

      <JMdict>
        <entry>
          <sense>
            <pos>&n;</pos>
            <pos>&adj-i;</pos>
          </sense>
          <sense>
          </sense>
          <sense>
            <pos>&v5u;</pos>
          </sense>
        </entry>
      </JMdict>
    EOS

    senses = WordsSourceReader.new(source_xml: xml).read_one.senses

    expect(senses.size).to eq(3)
    expect(senses[0].categories).to eq(['n', 'adj-i'])
    expect(senses[1].categories).to eq(nil)
    expect(senses[2].categories).to eq(['v5u'])
  end

  it 'should parse sense origins' do

    xml = <<-EOS
      <entry>
        <sense>
          <lsource xml:lang="ger">Abend</lsource>
          <lsource xml:lang="kor" />
        </sense>
        <sense>
          <lsource>Ice</lsource>
        </sense>
      </entry>
    EOS

    senses = WordsSourceReader.new(source_xml: xml).read_one.senses

    expect(senses.size).to eq(2)
    expect(senses[0].origins).to eq(['ger:Abend', 'kor'])
    expect(senses[1].origins).to eq(['eng:Ice'])
  end

  it 'should parse sense labels' do

    xml = <<-EOS
      <!DOCTYPE JMdict [
        <!ENTITY comp "computer terminology">
        <!ENTITY col "colloquialism">
        <!ENTITY on-mim "onomatopoeic">
        <!ENTITY ksb "Kansai-ben">
        <!ENTITY uK "word usually written using Kanji alone">
      ]>

      <JMdict>
        <entry>
          <sense>
            <field>&comp;</field>
            <misc>&col;</misc>
            <misc>&on-mim;</misc>
          </sense>
          <sense>
            <dial>&ksb;</dial>
            <misc>&uK;</misc>
          </sense>
        </entry>
      </JMdict>
    EOS

    senses = WordsSourceReader.new(source_xml: xml).read_one.senses

    expect(senses.size).to eq(2)
    expect(senses[0].labels).to eq(['comp', 'col', 'on-mim'])
    expect(senses[1].labels).to eq(['ksb', 'uK'])
  end

  it 'should parse sense notes' do

    xml = <<-EOS
      <entry>
        <sense>
          <s_inf>A</s_inf>
        </sense>
        <sense>
          <s_inf>B</s_inf>
          <s_inf>C</s_inf>
        </sense>
      </entry>
    EOS

    senses = WordsSourceReader.new(source_xml: xml).read_one.senses

    expect(senses.size).to eq(2)
    expect(senses[0].notes).to eq(['A'])
    expect(senses[1].notes).to eq(['B', 'C'])
  end

  it 'should assign noun category for translation' do

    xml = <<-EOS
      <JMdict>
        <entry>
          <trans>
            <trans_det>Shinagawa</trans_det>
          </trans>
        </entry>
      </JMdict>
    EOS

    senses = WordsSourceReader.new(source_xml: xml).read_one.senses
    expect(senses[0].categories).to eq(['n'])
  end

  it 'should parse translation types' do

    xml = <<-EOS
      <!DOCTYPE JMdict [
        <!ENTITY surname "family or surname">
        <!ENTITY place "place name">
        <!ENTITY unclass "unclassified name">
      ]>

      <JMdict>
        <entry>
          <trans>
            <name_type>&surname;</name_type>
            <name_type>&place;</name_type>
          </trans>
          <trans>
            <name_type>&unclass;</name_type>
          </trans>
          <trans>
          </trans>
        </entry>
      </JMdict>
    EOS

    senses = WordsSourceReader.new(source_xml: xml).read_one.senses

    expect(senses.size).to eq(3)
    expect(senses[0].labels).to eq(['surname', 'place'])
    expect(senses[1].labels).to eq(nil)
    expect(senses[2].labels).to eq(nil)
  end
end
