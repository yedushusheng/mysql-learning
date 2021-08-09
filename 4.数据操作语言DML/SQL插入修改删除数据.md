# INSERT

## **语法**

INSERT INTO tb() VALUES();

INSERT INTO tb1 SELECT FROM tb2;

 

参考（MySQL实现Oracle upset功能）：

https://www.jianshu.com/p/b9b7f2b5db24

INSERT INTO ON DUPLICATE KEY UPDATE：

可以在INSERT INTO…..后面加上ON DUPLICATE KEY UPDATE方法来实现upset功能。如果您指定了ON DUPLICATE KEY UPDATE，并且插入行后会导致在一个UNIQUE索引或PRIMARY KEY中出现重复值，则执行旧行UPDATE。

例如，如果列a被定义为UNIQUE，并且包含值1，则以下两个语句具有相同的效果：

INSERT INTO `table` (`a`, `b`, `c`) VALUES (1, 2, 3) ON DUPLICATE KEY UPDATE `c`=`c`+1; 

UPDATE `table` SET `c`=`c`+1 WHERE `a`=1;

如果行作为新记录被插入，则受影响行的值为1；如果原有的记录被更新，则受影响行的值为2。
	***\*注释：\****如果列b也是唯一列，则INSERT与此UPDATE语句相当：

UPDATE `table` SET `c`=`c`+1 WHERE `a`=1 OR `b`=2 LIMIT 1;

 

## **锁**

## **应用**

### **INSERT INTO SELECT**

insert into select性能问题：

https://mp.weixin.qq.com/s/HbRrvQwW_QmKlxhZG5x0Xw

#### 背景

存在这样的应用场景：需要将表A的数据迁移到表B中去做一个备份。如果采用查询出来，然后批量插入的方式，会消耗大量的网络I/O，是否可以采用insert into select，这样不通过网络I/O，直接利用数据库自身I/O完成？

#### 问题

***\*订单表\****

CREATE TABLE `order_today` (
 `id` varchar(32) NOT NULL COMMENT '主键',
 `merchant_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商户编号',
 `amount` decimal(15,2) NOT NULL COMMENT '订单金额',
 `pay_success_time` datetime NOT NULL COMMENT '支付成功时间',
 `order_status` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '支付状态 S：支付成功、F：订单支付失败',
 `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
 `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间 -- 修改时自动更新',
 PRIMARY KEY (`id`) USING BTREE,
 KEY `idx_merchant_id` (`merchant_id`) USING BTREE COMMENT '商户编号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

***\*订单记录表\****

CREATE TABLE order_record like order_today;

 

***\*模拟迁移：\****

INSERT INTO order_record SELECT
	* 
	FROM
	order_today 
	WHERE
	pay_success_time < '2020-03-08 00:00:00';

实际应用发现，少量数据插入采用insert into select没有问题，但是大量数据迁移的时候，会出现卡死。在迁移的过程中，有小部分用户出现支付失败，随后反应大批用户出现支付失败的情况，以及初始化订单失败的情况。

 

#### 分析

在默认的事务隔离级别下：insert into order_record select * from order_today 加锁规则是：order_record表锁，order_today逐步锁（扫描一个锁一个）。

分析执行过程：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps30A0.tmp.jpg) 

通过观察迁移sql的执行情况你会发现order_today是全表扫描，也就意味着在执行insert into select from 语句时，mysql会从上到下扫描order_today内的记录并且加锁，这样一来不就和直接锁表是一样了。

这也就可以解释，为什么一开始只有少量用户出现支付失败，后续大量用户出现支付失败，初始化订单失败等情况，因为一开始只锁定了少部分数据，没有被锁定的数据还是可以正常被修改为正常状态。由于锁定的数据越来越多，就导致出现了大量支付失败。最后全部锁住，导致无法插入订单，而出现初始化订单失败。

 

#### 解决方案

由于查询条件会导致order_today全表扫描，什么能避免全表扫描，给pay_success_time字段添加一个idx_pay_suc_time索引就可以了，由于走索引查询，就不会出现扫描全表的情况而锁表了，只会锁定符合条件的记录。

最终SQL：

INSERT INTO order_record SELECT
	* 
	FROM
	order_today FORCE INDEX (idx_pay_suc_time)
	WHERE
	pay_success_time <= '2020-03-08 00:00:00';

执行过程：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps30A1.tmp.jpg) 

#### 总结

使用insert into tablA select * from tableB语句时，一定要确保tableB后面的where，order或者其他条件，都需要有对应的索引，来避免出现tableB全部记录被锁定的情况。

 

INSERT INTO SELECT性能分析拓展：

http://blog.itpub.net/7728585/viewspace-2215202/

[https://mp.weixin.qq.com/s?__biz=MjM5NzAzMTY4NQ==&mid=2653933861&idx=1&sn=9e9ef3aca804a34b55db537245c75d29&chksm=bd3b4b4f8a4cc2594bc384e5f609adb673f6e78ff89df6f743f83184d028eaceceb2d6c00639&mpshare=1&scene=24&srcid=&sharer_sharetime=1591749192443&sharer_shareid=33f795d236f19ac7c128b2e279563f84#rd](#rd)

 

### **RR模式下insert..select sending data**

sending data是什么意思。隔离级别为RR，语句为insert..select。

#### 关于sending data

以前就说过这个问题，实际上sending data可能包含如下：

Innodb层数据的定位返回给MySQL层

Innodb层数据的查询返回给MySQL层

Innodb层数据的修改(如果是insert..select)

Innodb层加锁以及等待

等待进入Innodb层(innodb_thread_concurrency参数)

MySQL层发送数据给客户端

 

#### 原因总结

RR模式下对于insert..selcet处于sending data的原因总结：

1、RR模式下insert..select的select表会上S行锁，如果这行处于X锁则会出现sending data状态

2、insert..selcet中insert记录如果处于堵塞（唯一性检查）状态会处于sending data状态

3、整个过程如果需要操作的数据量较大，处于sending data状态。

 

#### 每行数据处理方式

929 T@4: | | | | | | THD::enter_stage: 'Sending data' /cdh/mysqldebug/percona-server-5.7.29-32/sql/sql_executor.cc:202
  930 T@4: | | | | | | >PROFILING::status_change
  931 T@4: | | | | | | <PROFILING::status_change 391
  932 T@4: | | | | | | info: Sending data
  933 T@4: | | | | | | >do_select
  934 T@4: | | | | | | | >sub_select
  935 T@4: | | | | | | | | >init_read_record
  936 T@4: | | | | | | | | | info: using rr_sequential
  937 T@4: | | | | | | | | | >ha_rnd_init
  938 T@4: | | | | | | | | | | >change_active_index
  939 T@4: | | | | | | | | | | | >innobase_get_index
  940 T@4: | | | | | | | | | | | <innobase_get_index 10117
  941 T@4: | | | | | | | | | | <change_active_index 10241
  942 T@4: | | | | | | | | | <ha_rnd_init 3111
  943 T@4: | | | | | | | | | >innobase_trx_init
  944 T@4: | | | | | | | | | <innobase_trx_init 3109
  945 T@4: | | | | | | | | <init_read_record 349
  946 T@4: | | | | | | | | >handler::ha_rnd_next
  947 T@4: | | | | | | | | | >rnd_next
  948 T@4: | | | | | | | | | | >index_first
  949 T@4: | | | | | | | | | | | >index_read
  950 T@4: | | | | | | | | | | | | >row_search_mvcc
  951 T@4: | | | | | | | | | | | | | >row_sel_store_mysql_rec
  952 T@4: | | | | | | | | | | | | | | >row_sel_store_mysql_field_func
  953 T@4: | | | | | | | | | | | | | | <row_sel_store_mysql_field_func 3275
  954 T@4: | | | | | | | | | | | | | | >row_sel_store_mysql_field_func
  955 T@4: | | | | | | | | | | | | | | <row_sel_store_mysql_field_func 3275
  956 T@4: | | | | | | | | | | | | | <row_sel_store_mysql_rec 3465
  957 T@4: | | | | | | | | | | | | <row_search_mvcc 6574
  958 T@4: | | | | | | | | | | | <index_read 10042
  959 T@4: | | | | | | | | | | <index_first 10430
  960 T@4: | | | | | | | | | <rnd_next 10531
  961 T@4: | | | | | | | | <handler::ha_rnd_next 3172
  962 T@4: | | | | | | | | >evaluate_join_record
  963 T@4: | | | | | | | | | enter: join: 0x7ffef8019970 join_tab index: 0 table: testlock cond: 0x0
  964 T@4: | | | | | | | | | counts: evaluate_join_record join->examined_rows++: 1
  965 T@4: | | | | | | | | | >end_send
  966 T@4: | | | | | | | | | | >Query_result_insert::send_data
  967 T@4: | | | | | | | | | | | >fill_record
  968 T@4: | | | | | | | | | | | | >Item_field::save_in_field_inner
  969 T@4: | | | | | | | | | | | | <Item_field::save_in_field_inner 6720
  970 T@4: | | | | | | | | | | | | >Item_field::save_in_field_inner
  971 T@4: | | | | | | | | | | | | <Item_field::save_in_field_inner 6720
  972 T@4: | | | | | | | | | | | <fill_record 9801
  973 T@4: | | | | | | | | | | | >write_record
  974 T@4: | | | | | | | | | | | | >init_alloc_root
  975 T@4: | | | | | | | | | | | | | enter: root: 0x7fffe8e48c20
  976 T@4: | | | | | | | | | | | | <init_alloc_root 100
  977 T@4: | | | | | | | | | | | | >COPY_INFO::set_function_defaults
  978 T@4: | | | | | | | | | | | | <COPY_INFO::set_function_defaults 135
  979 T@4: | | | | | | | | | | | | >handler::ha_write_row
  980 T@4: | | | | | | | | | | | | | >ha_innobase::write_row
  981 T@4: | | | | | | | | | | | | | | >row_ins
  982 T@4: | | | | | | | | | | | | | | | row_ins: table: test/testbb
  983 T@4: | | | | | | | | | | | | | | | >row_ins_index_entry_step
  984 T@4: | | | | | | | | | | | | | | | | >row_ins_clust_index_entry
  985 T@4: | | | | | | | | | | | | | | | | | >row_ins_clust_index_entry_low
  986 T@4: | | | | | | | | | | | | | | | | | | >btr_cur_search_to_nth_level
  987 T@4: | | | | | | | | | | | | | | | | | | <btr_cur_search_to_nth_level 2092
  988 T@4: | | | | | | | | | | | | | | | | | | >thd_report_row_lock_wait
  989 T@4: | | | | | | | | | | | | | | | | | | <thd_report_row_lock_wait 4280
  990 T@4: | | | | | | | | | | | | | | | | | <row_ins_clust_index_entry_low 2692
  991 T@4: | | | | | | | | | | | | | | | | <row_ins_clust_index_entry 3337
  992 T@4: | | | | | | | | | | | | | | | <row_ins_index_entry_step 3619
  993 T@4: | | | | | | | | | | | | | | <row_ins 3763
  994 T@4: | | | | | | | | | | | | | | >thd_mark_transaction_to_rollback
  995 T@4: | | | | | | | | | | | | | | <thd_mark_transaction_to_rollback 4147
  996 T@4: | | | | | | | | | | | | | <ha_innobase::write_row 8895
  997 T@4: | | | | | | | | | | | | <handler::ha_write_row 8565

及RR模式下insert select的逻辑大概为查询一行加锁（RC下没有加锁步骤）一行插入一行，直到所有行处理完成。整个过程处于'Sending data'状态下面。因此insert select和普通的insert操作有较大的区别。

关于sending data扩展阅读，参考：

http://blog.itpub.net/7728585/viewspace-2215202/

 

# ***\*UPDATE\****

## **语法**

UPDATE tb SET ... WHERE...

互换字段的值：

update common_cbd a, common_cbd b set a.latitude= b.longitude, a.longitude= b.latitude  where a.id = b.id

 

## **原理**

基于row模式时，server层匹配到要更新的记录，发现新值和旧值一致，不做更新，就直接返回，也不记录binlog（***\*这也是为什么row模式binlog会占用磁盘小的一个原因\****）。

基于statement或者mixed格式，MySQL执行 update 语句，并把更新语句记录到binlog。

 

## **加锁**

## **应用**

### **UPDATE卡顿**

参考：

[https://mp.weixin.qq.com/s?__biz=MjM5MDAxOTk2MQ==&mid=2650285587&idx=1&sn=cc9492e89dd01c9544267bde86f8cfe4&chksm=be47be058930371347bd06294db4be26fc17d74c92dc8006ec24befa40373292b502ae5f8d7d&mpshare=1&scene=24&srcid=0711LnyyRGkZaNOYkl7zIqU9&sharer_sharetime=1594424547396&sharer_shareid=33f795d236f19ac7c128b2e279563f84#rd](#rd)

注：在GoldenDB分布式数据库中，对于非乐观锁的情况下，update需要先下发select for update加行锁，然后执行update操作，如果select中where条件查询索引失效会导致全表扫描，成本太高，所以做了一个优化，即下发语句使用force index强制索引，这样可以避免全表扫描。

 

### **互换表中字段的值**

update common_cbd a, common_cbd b set a.latitude= b.longitude, a.longitude= b.latitude  where a.id = b.id

 

# DELETE

## **语法**

***\*单表语法：\****

DELETE [LOW_PRIORITY] [QUICK] [IGNORE] FROM tbl_name

  [WHERE where_definition]

  [ORDER BY ...]

  [LIMIT row_count]

***\*多表语法：\****

DELETE [LOW_PRIORITY] [QUICK] [IGNORE]

  tbl_name[.*] [, tbl_name[.*] ...]

  FROM table_references

  [WHERE where_definition]

或：

DELETE [LOW_PRIORITY] [QUICK] [IGNORE]

  FROM tbl_name[.*] [, tbl_name[.*] ...]

  USING table_references

  [WHERE where_definition]

tbl_name中有些行满足由where_definition给定的条件。DELETE用于删除这些行，并返回被删除的记录的数目。

如果您编写的DELETE语句中没有WHERE子句，则所有的行都被删除。当您不想知道被删除的行的数目时，有一个更快的方法，即使用TRUNCATE TABLE。

如果您删除的行中包括用于AUTO_INCREMENT列的最大值，则该值被重新用于BDB表，但是不会被用于MyISAM表或InnoDB表。如果您在AUTOCOMMIT模式下使用DELETE FROM tbl_name（不含WHERE子句）删除表中的所有行，则对于所有的表类型（除InnoDB和MyISAM外），序列重新编排。对于InnoDB表，此项操作有一些例外。

对于MyISAM和BDB表，您可以把AUTO_INCREMENT次级列指定到一个多列关键字中。在这种情况下，从序列的顶端被删除的值被再次使用，甚至对于MyISAM表也如此。

 

DELETE语句支持以下修饰符：

·如果您指定LOW_PRIORITY，则DELETE的执行被延迟，直到没有其它客户端读取本表时再执行。

·对于MyISAM表，如果您使用QUICK关键词，则在删除过程中，存储引擎不会合并索引端结点，这样可以加快部分种类的删除操作的速度。

·在删除行的过程中，IGNORE关键词会使MySQL忽略所有的错误。（在分析阶段遇到的错误会以常规方式处理。）由于使用本选项而被忽略的错误会作为警告返回。

 

## **原理**

在InnoDB中，delete操作并不会真的把数据删除，mysql实际上只是给删除的数据打了个标记，标记为删除，因此使用delete删除表中的数据，表文件在磁盘上所占空间不会变小，我们这里暂且称之为假删除。

 

这些被删除的记录行，只是被标记删除，是可以被复用的，下次有符合条件的记录是可以直接插入到这个被标记的位置的。

比如我们在id为300-600之间的记录中删除一条id=500 的记录，这条记录就会被标记为删除，等下一次如果有一条id=400的记录要插入进来，那么就可以复用id=500被标记删除的位置，这种情况叫行记录复用。

还有一种情况是数据页复用，就是指整个数据页都被标记删除了，于是这整个数据页都可以被复用了，和行记录复用不同的是，数据页复用对要插入的数据几乎没有条件限制。

假如要插入的记录是id=1000，那么就不能复用id=500 这个位置了，但如果有一整个数据页可复用的话，那么无论id值为多少都可以被复用在这个页上。

这些被标记删除的记录，其实就是一个空洞，有种占着茅坑不拉屎的感觉，浪费空间不说，还会影响查询效率。

因为你要知道，mysql在底层是以数据页为单位来存储和读取数据的，每次向磁盘读一次数据就是读一个数据页，然而每访问一个数据页就对应一次磁盘IO操作，磁盘IO相对内存访问速度是相当慢的。

如果一个表上存在大量的数据空洞，原本只需一个数据页就保存的数据，由于被很多空洞占用了空间，不得不需要增加其他的数据页来保存数据，相应的，mysql在查询相同数据的时候，就不得不增加磁盘IO操作，从而影响查询速度。

其实不仅仅是删除操作会造成数据空洞，插入和更新同样也会造成空洞。

因此，一个数据表在经过大量频繁的增删改之后，难免会产生数据空洞，浪费空间并影响查询效率，通常在生产环境中会直接表现为原本很快的查询会变得越来越慢。

对于这种情况，我们通常可以使用下面这个命令就能解决数据空洞问题：

optimize table t

这个命令的原理就是重建表，就是建立一个临时表B，然后把表A（存在数据空洞的表）中的所有数据查询出来，接着把数据全部重新插入到临时表B中，最后再用临时表B替换表A即可，这就是重建表的过程。

 

## **应用**

### 删除重复数据

参考：

https://blog.csdn.net/n950814abc/article/details/82284838

https://m.jb51.net/article/116677.htm

https://www.cnblogs.com/qlqwjy/p/8270011.html

 

#### 原因

​	产生重复数据的原因：

1、 人为原因：如重复录入数据，重复提交等

2、 系统原因：由于系统升级或设计的原因使原来可以重复的数据变为不能重复的

#### 查询

​	如何查询数据是否重复？

​	使用GROUP BY和HAVING从句处理

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps30B2.tmp.jpg) 

注：GROUP BY分组后相同值的数据放在一起，HAVING过滤结果集大于1的（即存在多个相同的分组数据，只有一个的不需要过滤）。

 

##### 查询全部重复记录

1、查找所有重复标题的记录：

select title,count(*) as count from user_table group by title having count>1;

SELECT * FROM t_info a WHERE ((SELECT COUNT(*) FROM t_info WHERE Title = a.Title) > 1) ORDER BY Title DESC;

 

##### 过滤重复记录

2、过滤重复记录(只显示一条)

Select * From HZT Where ID In (Select Max(ID) From HZT Group By Title)

注：此处显示ID最大一条记录

 

###### 方法一

SELECT

​	*

FROM

​	dept

WHERE

​	dname IN (

​		SELECT

​			dname

​		FROM

​			dept

​		GROUP BY

​			dname

​		HAVING

​			COUNT(1) > 1

​	)

AND deptno NOT IN (

​	SELECT

​		MIN(deptno)

​	FROM

​		dept

​	GROUP BY

​		dname

​	HAVING

​		COUNT(1) > 1

)

上面这种写法正确，但是查询的速度太慢，可以试一下下面这种方法：

###### 方法二

根据dname分组，查找出deptno最小的。然后再查找deptno不包含刚才查出来的。这样就查询出了所有的重复数据（除了deptno最小的那行）

SELECT *

FROM

​	dept

WHERE

​	deptno NOT IN (

​		SELECT

​			dt.minno

​		FROM

​			(

​				SELECT

​					MIN(deptno) AS minno

​				FROM

​					dept

​				GROUP BY

​					dname

​			) dt

​	)

###### 方法三

SELECT 

\* 

FROM

table_name AS ta 

WHERE

ta.唯一键 <> ( SELECT max( tb.唯一键 ) FROM table_name AS tb WHERE ta.判断重复的列 = tb.判断重复的列 );

#### 删除

​	删除重复数据（name重复），对于相同数据保留ID最大的：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps30B3.tmp.jpg) 

##### 删除全部重复记录

1、删除全部重复记录（慎用）

delete 表 where 重复字段 in (select 重复字段 from 表 group by 重复字段 having count(*)>1)

举例：

DELETE

FROM

dept

WHERE

dname IN (

SELECT

dname

FROM

dept

GROUP BY

dname

HAVING

count(1) > 1

)

会出现如下错误：[Err] 1093 - You can't specify target table 'dept' for update in FROM clause

原因是：更新这个表的同时又查询了这个表，查询这个表的同时又去更新了这个表，可以理解为死锁。mysql不支持这种更新查询同一张表的操作

 

解决办法：把要更新的几列数据查询出来做为一个第三方表，然后筛选更新。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps30B4.tmp.jpg) 

 

##### 保留一条重复记录

2、保留一条（这个应该是大多数人所需要的）

delete from HZT Where ID not in (select max(ID) from HZT group by Title)

注：此处保留ID最大一条记录

 

###### 方法一

DELETE

FROM

​	dept

WHERE

​	dname IN (

​		SELECT

​			t.dname

​		FROM

​			(

​				SELECT

​					dname

​				FROM

​					dept

​				GROUP BY

​					dname

​				HAVING

​					count(1) > 1

​			) t

​	)

AND deptno NOT IN (

SELECT

​	dt.mindeptno

FROM

​	(

​		SELECT

​			min(deptno) AS mindeptno

​		FROM

​			dept

​		GROUP BY

​			dname

​		HAVING

​			count(1) > 1

​	) dt

)

###### 方法二

DELETE

FROM

​	dept

WHERE

​	deptno NOT IN (

​		SELECT

​			dt.minno

​		FROM

​			(

​				SELECT

​					MIN(deptno) AS minno

​				FROM

​					dept

​				GROUP BY

​					dname

​			) dt

​	)

###### 方法三

DELETE 

FROM

​	table_name AS ta 

WHERE

​	ta.唯一键 <> (

SELECT

​	t.maxid 

FROM

​	( SELECT max( tb.唯一键 ) AS maxid FROM table_name AS tb WHERE ta.判断重复的列 = tb.判断重复的列 ) t 

​	);

##### rowid删除

1、查找表中多余的重复记录，重复记录是根据单个字段（peopleId）来判断

select * from people where peopleId in (select peopleId from people group by peopleId having count(peopleId) > 1)

2、删除表中多余的重复记录，重复记录是根据单个字段（peopleId）来判断，只留有rowid最小的记录

delete from people where peopleId in (select peopleId from people group by peopleId having count(peopleId) > 1) and rowid not in (select min(rowid) from people group by peopleId having count(peopleId )>1)

3、查找表中多余的重复记录（多个字段）

select * from vitae a where (a.peopleId,a.seq) in (select peopleId,seq from vitae group by peopleId,seq having count(*) > 1)

4、删除表中多余的重复记录（多个字段），只留有rowid最小的记录

delete from vitae a where (a.peopleId,a.seq) in (select peopleId,seq from vitae group by peopleId,seq having count(*) > 1) and rowid not in (select min(rowid) from vitae group by peopleId,seq having count(*)>1)

5、查找表中多余的重复记录（多个字段），不包含rowid最小的记录

select * from vitae a where (a.peopleId,a.seq) in (select peopleId,seq from vitae group by peopleId,seq having count(*) > 1) and rowid not in (select min(rowid) from vitae group by peopleId,seq having count(*)>1)

 

### **安全delete巨大量数据行**

#### delete分批删除

根据前辈多年的删表经验来说，删除大量数据时一定要分批缓慢删除，否则很容易阻塞整个表，还有可能因为产生的 binlog 过大造成从库同步出问题。

delete * from where create_time <= ? limit ?;

#### pt-archiver删除

确定删除方案后，我们就可以使用pt-archiver进行删除，这个工具不只可以用个归档，删除数据也是行家。

下面这介绍两种方案，比较有局限性，但对业务可以停的场景有用：

1、mysqldump备份出来需要的数据，然后drop table，导入；

2、mysqldump备份出来需要的数据，然后truncate table，导入。

明显都会造成表一段时间的不可用。同时还会引起IO飙升的风险。

如果这张大表仍然还有被高频的访问，直接drop table&truncate显然是无法接受的！

**使用pt-archiver进行分批缓慢删除**

##### 参数

pt-archiver --help 
	--progress 每多少行打印进度信息
	--limit 限制select返回的行数
	--sleep 指定select语句休眠时间
	--txn-size 指定多少行提交一次事务
	--bulk-delete 用单个DELETE语句批量删除每个行块。该语句删除块的第一行和最后一行之间的每一行，隐含--commit-each
	--dry-run 打印查询，不做任何操作后退出

##### 删除数据

分三步：

1、打印查询

2、打开会话保持功能screen（防止窗口意外断开造成程序中断；笔者曾经因为忘记打开会话保持在机器面前守了半天；因为10分钟没操作堡垒机会断线）

3、执行删除

\# 打印查询
	$ pt-archiver --source h=10.186.65.19,P=3306,u=root,p='123',D=sbtest,t=sbtest1 --purge --charset=utf8mb4 --where "id <= 400000" --progress=200 --limit=200 --sleep=1 --txn-size=200 --statistics --dry-run
	# 解释：删除sbtest库，sbtest1表数据，字符集为utf8mb4，删除条件是id <= 400000，每次取出200行进行处理，每处理200行则进行一次提交，每完成一次处理sleep 1s
	SELECT /*!40001 SQL_NO_CACHE */ `id`,`k`,`c`,`pad` FROM `sbtest`.`sbtest1` FORCE INDEX(`PRIMARY`) WHERE (id <= 400000) AND (`id` < '23132073') ORDER BY `id` LIMIT 200
	SELECT /*!40001 SQL_NO_CACHE */ `id`,`k`,`c`,`pad` FROM `sbtest`.`sbtest1` FORCE INDEX(`PRIMARY`) WHERE (id <= 400000) AND (`id` < '23132073') AND ((`id` >= ?)) ORDER BY `id` LIMIT 200
	DELETE FROM `sbtest`.`sbtest1` WHERE (`id` = ?)
	***\*# 打开会话保持功能\*******\*
\****	screen -S archiver
	***\*# 执行删除\*******\*
\****	$ pt-archiver --source h=10.186.65.19,P=3306,u=root,p='123',D=sbtest,t=sbtest1 --purge --charset=utf8mb4 --where "id <= 400000" --progress=200 --limit=200 --sleep=1 --txn-size=200 --statistics
	......
	2021-02-16T17:52:24  2115 398200
	2021-02-16T17:52:25  2116 398400
	2021-02-16T17:52:26  2117 398600
	2021-02-16T17:52:27  2118 398800
	2021-02-16T17:52:28  2119 399000
	2021-02-16T17:52:29  2120 399200
	2021-02-16T17:52:30  2121 399400
	2021-02-16T17:52:31  2123 399600
	2021-02-16T17:52:32  2124 399800
	2021-02-16T17:52:33  2125 400000
	2021-02-16T17:52:33  2125 400000
	Started at 2021-02-16T17:17:08, ended at 2021-02-16T17:52:34
	Source: A=utf8mb4,D=sbtest,P=3306,h=10.186.65.19,p=...,t=sbtest1,u=root
	SELECT 400000
	INSERT 0
	DELETE 400000
	Action    Count    Time    Pct
	sleep     2000 2003.1843   94.22
	deleting   400000  88.6074    4.17
	select     2001   2.9120    0.14
	commit     2001   1.4004    0.07
	other       0  30.0424    1.41

***\*在删除数据后的处理：\****

MySQL的机制下delete后磁盘不会立即释放，在业务空闲时间进行分析表以便真正从磁盘上移除数据解除空间占用（极端情况可能需要重启释放），非必做（一般可不做）；视场景而定。

如需要可以研究一下 optimize table：

https://dev.mysql.com/doc/refman/8.0/en/optimize-table.html

 

# 闪回

参考：

[https://mp.weixin.qq.com/s?__biz=MzIzOTA2NjEzNQ==&mid=2454780189&idx=1&sn=2013c2f62cbdffa7f274f6ccf60cbd09&chksm=fe8b9465c9fc1d7373226dfeb4bf7cb27cece721140819d30118af9b8dcddce12e6c365490a5&mpshare=1&scene=24&srcid=0315yBplX63k706DextFWkT7&sharer_sharetime=1615768697494&sharer_shareid=33f795d236f19ac7c128b2e279563f84#rd](#rd)

 

# RENAME

# TRUNCATE

## **语法**

## **原理**

## **应用**

### **清空数据库全部表**

参考：

https://m.php.cn/article/53312.html

 

Mysql清空表是很重要的操作，也是最常见的操作之一，下面详细介绍Mysql清空表的实现方法。

#### 重建库和表

***\*1、\*******\*只导出表结构\****

导出整个数据库结构（不包含数据）
	mysqldump -h localhost -uroot -p123456 -d database > dump.sql

导出单个数据表结构（不包含数据）
	mysqldump -h localhost -uroot -p123456 -d database table > dump.sql

 

***\*2、\*******\*只导出表数据\****

导出整个数据库数据

mysqldump -h localhost -uroot -p123456 -t database > dump.sql

 

***\*3、\*******\*导出结构+数据\****

导出整个数据库结构和数据
	mysqldump -h localhost -uroot -p123456 database > dump.sql

导出单个数据表结构和数据
	mysqldump -h localhost -uroot -p123456 database table > dump.sql



 

#### 生成清空所有表的SQL

mysql -N -s information_schema -e "SELECT CONCAT('TRUNCATE TABLE ',TABLE_NAME,';') FROM TABLES WHERE TABLE_SCHEMA='eab12'"

输出结果如下：
	TRUNCATE TABLE AUTHGROUPBINDINGS;
	TRUNCATE TABLE AUTHGROUPS;
	TRUNCATE TABLE AUTHUSERS;
	TRUNCATE TABLE CORPBADCUSTOMINFO;
	TRUNCATE TABLE CORPSMSBLACKLISYInfo;
	TRUNCATE TABLE CORPSMSFILTERINFO;
	TRUNCATE TABLE CORPSMSINFO;
	TRUNCATE TABLE EABASEREGINFOS;
	TRUNCATE TABLE EACORPBLOB;
	TRUNCATE TABLE EACORPINFO;
	....
	....
	这样就更完善了：
	mysql -N -s information_schema -e "SELECT CONCAT('TRUNCATE TABLE ',TABLE_NAME,';') FROM TABLES WHERE TABLE_SCHEMA='eab12'" | mysql eab12
	即清空eab12中所有的表。
	但是如果有外键的话，很可能会报错。因此还需要加个-f：

mysql -N -s information_schema -e "SELECT CONCAT('TRUNCATE TABLE ',TABLE_NAME,';') FROM TABLES WHERE TABLE_SCHEMA='eab12'" | mysql -f eab12
	多执行几次，直到不报错。

 

# 分布式数据库实践

在分布式数据库GoldenDB中，对于配置为悲观锁的情况，对于update/delete操作都是先执行select for update加锁（乐观锁的情况是不断重试），然后再真正去执行update/delete操作。

这样存在一个问题，如果索引失效极易造成全表锁，这样对于其他业务影响很大，所以我们做了一个优化，即下发的select添加force index，这样可以强制使用索引，减少加锁范围。