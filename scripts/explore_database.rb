#!/usr/bin/env ruby

require_relative 'models/dictionary_database'
require 'pry'

abort 'Please provide dictionary file name.' if ARGV[0].nil?

database = DictionaryDatabase.new(ARGV[0])

binding.pry
