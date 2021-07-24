# Mysql

**mysql命令行-e命令，如何获取其影响多少行？结果去除字段名**

1、大多时候，我们需要使用mysql -e "" database的方式，执行一些命令，select 它会返回包含字段名在内的行，我们还得手工将其去除 mysql -e "select name" database 2>/dev/null | sed -e '/name/d'

其实不必这样麻烦，可以直接这样用：
mysql --skip-column-names[-N] -e "select name" database 2>/dev/null
2、在update,insert,delete的时候，我们需要知道，它影响了多少行；可以这样使用 ：

mysql --skip-column-names(-N) -e "update table set field=xx limit 10;select row_count();" database

3、在select的时候，我们更需要知道，它找到多少行；可以这样使用：

mysql --skip-column-names(-N) -e "update table set field=xx limit 10;select found_rows();" database

4、在insert的时候，我们更需要知道，它insert的id；可以这样使用：

mysql --skip-column-names(-N) -e "update table set field=xx limit 10;select last_insert_id();" database

 

但是会碰到一个问题，只要是concat里面有一个是null的就会返回null，而且，每个中间都要有 '#'，也比较麻烦

mysql="select concat_ws( '###',replace( ifnull(cid,''),' ','' ),replace( ifnull(username,''),' ','' ),replace( ifnull(password,''),' ','' ),replace( ifnull(comName,''),' ','' ),replace( ifnull(scomname,''),' ','' ),replace( ifnull(l_person,''),' ','' ),replace( ifnull(l_mobile,''),' ','' ),replace( ifnull(telephone,''),' ','' ),replace( ifnull(remark,''),' ','' ),replace( ifnull(status,''),' ','' ) ) from company order by cid asc limit 0,15";

用concat_ws(delimiter,'xx','yy')==xx#yy

用ifnull( field ,replace_statement)来做替换，消除null

replace( field,' ','') 消除干扰for in的空格

 