import MySQLdb

# connect to db
conn = MySQLdb.connect("localhost","root","","test")

# create cursor
cur=conn.cursor()

name=raw_input()
# sql 
sql ="select * from student"
insert_sql="insert into student (name) values ('%s')" %(name)
# execute

cur.execute(insert_sql)

conn.commit()

cur.execute(sql)

# get the result

result=cur.fetchall()

for row in result:
	print row[0]
	print row[1]

conn.close()