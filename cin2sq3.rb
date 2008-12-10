# CIN To Sqlite3
# Author :: Zero Cho ( itszero at gmail dot com )
require 'rubygems'
require 'sqlite3'
require 'iconv'

puts "Open database connection..."
db = SQLite3::Database.new "cin.db"

# Check if table exists
begin
	db.execute("SELECT * FROM bpmf LIMIT 1")
	puts "Table exists, drop it"
	db.execute("DROP TABLE bpmf")
rescue SQLite3::SQLException => ex
	# Table is not exist
	puts "Table is not exist"
end
puts "Create table bpmf"
db.execute("CREATE TABLE bpmf ( key varchar(10), val varchar(10) )")
db.transaction

f = File.open("bpmf.cin")
cin_start = false
f.each do |row|
	row.strip!
	if row == "%chardef  begin"
		cin_start = true 
		next
	end
	next if !cin_start
	break if row == "%chardef  end"
	
	#row = Iconv.iconv('UTF-16//ignore', 'big5//ignore', row).to_s
	data = row.split(' ')
	
	print "#{data.join(' ')}\n"
	db.execute("INSERT INTO bpmf (key, val) VALUES (? ,?)", data[0], data[1])
end
f.close
db.execute("CREATE TABLE android_metadata (locale TEXT)")
db.commit