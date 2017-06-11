#!/usr/bin/env ruby

require_relative 'models/dictionary_database'
require 'pry'

DATABASE_FILE = 'temp/dictionary_v1.sqlite3'

database = DictionaryDatabase.new(DATABASE_FILE)
binding.pry
