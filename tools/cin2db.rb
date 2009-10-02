#!/usr/bin/ruby
# CIN To Sqlite3
# Author :: Zero Cho ( itszero at gmail dot com )
# License :: MIT License
#
require 'rubygems'
require 'sqlite3'
require 'iconv'

if ARGV.size < 2
  puts "Usage: #{__FILE__} [cin file] [output db file]"
  exit
end

puts "C2db (.cin to SQLite3 db)"
puts "Input file: #{ARGV[0]} Output DB: #{ARGV[1]}"

puts "Open database connection..."
@db = SQLite3::Database.new ARGV[1]

def create_table(name, table_def)
  # Check if table exists
  begin
    @db.execute("SELECT * FROM #{name} LIMIT 1")
    puts "Table exists, drop it"
    @db.execute("DROP TABLE #{name}")
  rescue SQLite3::SQLException => ex
    # Table is not exist
    puts "Table is not exist"
  end
  puts "Create table #{name}"
  @db.execute("CREATE TABLE #{name} ( #{table_def} )")
end
 
f = File.open(ARGV[0])
cin_start = false
key_start = false
table_name = nil
f.each do |row|
  row = row.strip.split.join(" ")
  if row == "%chardef begin"
    if !table_name
      puts "[WARN] %ename not found, go ahead with table name \"ime\""
      table_name = "ime"
      create_table(table_name, "key varchar(10), val varchar(10)")
    end
    cin_start = true 
    @db.transaction
    next
  elsif row == "%chardef end" 
    @db.commit
    break
  elsif row == "%keyname begin"
    create_table("keyname", "key varchar(10), val varchar(10)")
    key_start = true
    @db.transaction
    next
  elsif row == "%keyname end"
    key_start = false
    @db.commit
    next
  elsif row.include? "%ename"
    table_name = row.split[1]
    create_table(table_name, "key varchar(10), val varchar(10)")
    next
  end

  data = row.split(" ")  
  if key_start
    puts "Importing key mapping #{data.join(' => ')}..."
    @db.execute("INSERT INTO keyname (key, val) VALUES (? ,?)", data[0], data[1])
  elsif cin_start
    # Detect if its a CJK EXT-B char, skip if it's since Droid Sans does not contains EXT-B chars.
    ucs = data[1].unpack("U*")
    if ucs[0] >= 131072
      puts "CJK EXT-B char detected, skip! #{data.join(' => ')}..."
      next
    end
    puts "Importing char definition #{data.join(' => ')}..."
    @db.execute("INSERT INTO #{table_name} (key, val) VALUES (? ,?)", data[0], data[1])
  else
    next
  end
end
f.close
puts "Create dummy android_metadata..."
create_table("android_metadata", "locale TEXT")
puts "Indexing data..."
@db.execute("CREATE INDEX key_index on #{table_name} (key)")
puts "Done. :)"