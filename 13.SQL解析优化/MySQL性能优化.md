# 优化

优化MySQL数据库是数据库管理员和数据库开发人员的必备技能。MySQL优化，一方面是找出系统的瓶颈，提高MySQL数据库整体的性能；另一方面需要合理的结构设计和参数调整，以提高用户操作响应的速度；同时还要尽可能节省系统资源，以便系统可以提供更大负荷的服务。

MySQL数据库优化是多方面的，原则是减少系统的瓶颈，减少资源的占用，增加系统的反应速度。例如，通过优化文件系统，提高磁盘I\O的读写速度；通过优化操作系统调度策略，提高MySQL在高负荷情况下的负载能力；优化表结构、索引、查询语句等使查询响应更快。在MySQL中，可以使用SHOW STATUS语句查询一些MySQL数据库的性能参数。SHOW STATUS语句的语法如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED4E.tmp.jpg) 

其中，value是要查询的参数值，一些常用的性能参数如下：

●　Connections：连接MySQL服务器的次数。

●　Uptime：MySQL服务器的上线时间。

●　Slow_queries：慢查询的次数。

●　Com_select：查询操作的次数。

●　Com_insert：插入操作的次数。

●　Com_update：更新操作的次数。

●　Com_delete：删除操作的次数。

查询MySQL服务器的连接次数，可以执行如下语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED4F.tmp.jpg) 

查询MySQL服务器的慢查询次数，可以执行如下语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED50.tmp.jpg) 

查询其他参数的方法和两个参数的查询方法相同。慢查询次数参数可以结合慢查询日志，找出慢查询语句，然后针对慢查询语句进行表结构优化或者查询语句优化。

 

数据库优化一方面是找出系统的瓶颈，提高MySQL数据库的整体性能，而另一方面需要合理的结构设计和参数调整，以提高用户的相应速度，同时还要尽可能的节约系统资源，以便让系统提供更大的负荷。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED61.tmp.jpg) 

## **概述**

注：优化有风险，修改需谨慎。

### **问题**

**优化可能带来的问题：**

优化不总是对一个单纯的环境进行，还很可能是一个复杂的已投产的系统。

优化手段本来就有很大的风险，只不过你没能力意识到和预见到。

任何的技术可以解决一个问题，但必然存在带来一个问题的风险。

对于优化来说解决问题而带来的问题，控制在可接受的范围内才是有成果。

保持现状或出现更差的情况都是失败。

 

### **需求**

**优化的需求：**

稳定性和业务可持续性，通常比性能更重要。

优化不可避免涉及到变更，变更就有风险。

优化使性能变好，维持和变差是等概率事件。

切记优化，应该是各部门协同，共同参与的工作，任何单一部门都不能对数据库进行优化。

所以优化工作，是由业务需求驱使的！

优化由谁参与？在进行数据库优化时，应由数据库管理员、业务部门代表、应用程序架构师、应用程序设计人员、应用程序开发人员、硬件及系统管理员、存储管理员等，业务相关人员共同参与。 

 

### **理论**

## **目的**

​	数据库优化的目的：

​	1、**避免出现页面访问错误**

​	由于数据库连接timeout产生页面5xx错误

​	由于慢查询造成页面无法加载

​	由于阻塞影响服务器性能，甚至造成数据无法提交

​	2、**增加数据库的稳定性**

​	很多数据库问题都是由于低效的查询引起的

​	**3、优化用户体验**

​	流畅页面的访问速度

​	良好的网站功能体验

 

## **查询过程**

在进行MySQL的优化之前必须要了解的就是MySQL的查询过程，很多的查询优化工作实际上就是遵循一些原则让MySQL的优化器能够按照预想的合理方式运行而已。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED62.tmp.jpg) 

## **诊断工具**

 

## **调优方法论**

## **分类**

优化可以分为两大类：软优化和硬优化。软优化一般是操作数据库即可，而硬优化则是操作服务器硬件及参数设置。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED63.tmp.jpg) 

### **软优化**

#### 大批量插入数据优化

1、对于MyISAM存储引擎的表，可以使用：DISABLE KEYS和ENABLE KEYS用来打开或者关闭MyISAM表非唯一索引的更新。

ALTER TABLE tbl_name DISABLE KEYS;

loading the data

ALTER TABLE tbl_name ENABLE KEYS;

2、对于InnoDB引擎，有以下几种优化措施：

① 导入的数据按照主键的顺序保存：这是因为InnoDB引擎表示按照主键顺序保存的，如果能将插入的数据提前按照排序好自然能省去很多时间。

比如bulk_insert.txt文件是以表user主键的顺序存储的，导入的时间为15.23秒

mysql> load data infile 'mysql/bulk_insert.txt' into table user;

Query OK, 126732 rows affected (15.23 sec)

Records: 126732 Deleted: 0 Skipped: 0 Warnings: 0

没有按照主键排序的话，时间为：26.54秒

mysql> load data infile 'mysql/bulk_insert.txt' into table user;

Query OK, 126732 rows affected (26.54 sec)

Records: 126732 Deleted: 0 Skipped: 0 Warnings: 0

② 导入数据前执行SET UNIQUE_CHECKS=0，关闭唯一性校验，带导入之后再打开设置为1：校验会消耗时间，在数据量大的情况下需要考虑。

③ 导入前设置SET AUTOCOMMIT=0，关闭自动提交，导入后结束再设置为1：这是因为自动提交会消耗部分时间与资源，虽然消耗不是很大，但是在数据量大的情况下还是得考虑。

 

#### INSERT优化

1、尽量使用多个值表的INSERT语句，这种方式将大大缩减客户端与数据库之间的连接、关闭等消耗。（同一客户的情况下），即：

INSERT INTO tablename values(1,2),(1,3),(1,4)

实验：插入8条数据到user表中（使用navicat客户端工具）

insert into user values(1,'test',replace(uuid(),'-',''));

insert into user values(2,'test',replace(uuid(),'-',''));

insert into user values(3,'test',replace(uuid(),'-',''));

insert into user values(4,'test',replace(uuid(),'-',''));

insert into user values(5,'test',replace(uuid(),'-',''));

insert into user values(6,'test',replace(uuid(),'-',''));

insert into user values(7,'test',replace(uuid(),'-',''));

insert into user values(8,'test',replace(uuid(),'-',''));

得到反馈：

[SQL] insert into user values(1,'test',replace(uuid(),'-',''));

受影响的行: 1

时间: 0.033s

[SQL] 

insert into user values(2,'test',replace(uuid(),'-',''));

受影响的行: 1

时间: 0.034s

[SQL] 

insert into user values(3,'test',replace(uuid(),'-',''));

受影响的行: 1

时间: 0.056s

[SQL] 

insert into user values(4,'test',replace(uuid(),'-',''));

受影响的行: 1

时间: 0.008s

[SQL] 

insert into user values(5,'test',replace(uuid(),'-',''));

受影响的行: 1

时间: 0.008s

[SQL] 

insert into user values(6,'test',replace(uuid(),'-',''));

受影响的行: 1

时间: 0.024s

[SQL] 

insert into user values(7,'test',replace(uuid(),'-',''));

受影响的行: 1

时间: 0.004s

[SQL] 

insert into user values(8,'test',replace(uuid(),'-',''));

受影响的行: 1

时间: 0.004s

总共的时间为0.171秒，接下来使用多值表形式：

insert into user values

(9,'test',replace(uuid(),'-','')),

(10,'test',replace(uuid(),'-','')),

(11,'test',replace(uuid(),'-','')),

(12,'test',replace(uuid(),'-','')),

(13,'test',replace(uuid(),'-','')),

(14,'test',replace(uuid(),'-','')),

(15,'test',replace(uuid(),'-','')),

(16,'test',replace(uuid(),'-',''));

得到反馈：

[SQL] insert into user values

(9,'test',replace(uuid(),'-','')),

(10,'test',replace(uuid(),'-','')),

(11,'test',replace(uuid(),'-','')),

(12,'test',replace(uuid(),'-','')),

(13,'test',replace(uuid(),'-','')),

(14,'test',replace(uuid(),'-','')),

(15,'test',replace(uuid(),'-','')),

(16,'test',replace(uuid(),'-',''));

受影响的行: 8

时间: 0.038s

得到时间为0.038，这样一来可以很明显节约时间优化SQL

2、如果在不同客户端插入很多行，可使用INSERT DELAYED语句得到更高的速度，DELLAYED含义是让INSERT语句马上执行，其实数据都被放在内存的队列中。并没有真正写入磁盘。LOW_PRIORITY刚好相反。

3、将索引文件和数据文件分在不同的磁盘上存放（InnoDB引擎是在同一个表空间的）。

4、如果批量插入，则可以增加bluk_insert_buffer_size变量值提供速度（只对MyISAM有用）

5、当从一个文本文件装载一个表时，使用LOAD DATA INFILE，通常比INSERT语句快20倍。

 

#### 查询语句优化

##### 如何分析SQL查询？

​	使用MySQL提供的sakila数据库，可以通过以下的URL获取这个演示数据库：

​	http://dev/mysql.com/doc/index-other.html

​	sakila数据库的表结构信息可以通过以下网站查看：

​	http://dev/mysql.com/doc/sakila/en/sakila-installation.html

​	数据库基于MySQL5.5版本

###### explain

筛选出有问题的SQL，我们可以使用MySQL提供的explain查看SQL执行计划情况（关联表，表查询顺序、索引使用情况等）。

 

***\*用法：\****

explain select * from category;

返回结果：

mysql> explain select * from category;

+----+-------------+----------+------------+------+---------------+------+---------+------+------+----------+-------+

| id | select_type | table   | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra |

+----+-------------+----------+------------+------+---------------+------+---------+------+------+----------+-------+

|  1 | SIMPLE    | category | NULL    | ALL  | NULL      | NULL | NULL   | NULL |   1 |  100.00 | NULL  |

+----+-------------+----------+------------+------+---------------+------+---------+------+------+----------+-------+

1 row in set, 1 warning (0.00 sec)

字段解释：

**id**

\1) id：select查询序列号（该语句的唯一标识）。id相同，执行顺序由上至下；id不同，id值越大优先级越高，越先被执行。

有几个select就有几个id，并且id的顺序是按select出现的顺序增长的。MySQL将select查询分为简单查询和复杂查询。复杂查询分为三类：简单子查询、派生表（from语句中的子查询）、union查询。

**select_type**

\2) select_type：查询数据的操作类型，其值如下：

SIMPLE：简单查询，不包含子查询或union

PRIMARY：包含复杂的子查询，最外层查询标记为该值

SUBQUERY：在select或where包含子查询（子查询的第一个SELECT），被标记为该值

DEPENDENT SUBQUERY：子查询中的第一个 SELECT，依赖了外面的查询

UNCACHEABLE SUBQUERY：子查询，结果无法缓存，必须针对外部查询的每一行重新评估(一个子查询的结果不能被缓存，必须重新评估外链接的第一行)

MATERIALIZED：物化子查询

DERIVED：在from列表中包含的子查询被标记为该值，MySQL会递归执行这些子查询，把结果放在临时表。MySQL内部将其称为是Derived table（派生表），因为该临时表是从子查询派生出来的。

DEPENDENT DERIVED：派生表，依赖了其他的表

UNION：若第二个select出现在union之后，则被标记为该值。若union包含在from的子查询中，外层select被标记为derived

DEPENDENT UNION(UNION中的第二个或后面的SELECT语句，取决于外面的查询)

UNCACHEABLE UNION：UNION属于UNCACHEABLE SUBQUERY的第二个或后面的查询

UNION RESULT：从union表获取结果的select

**table**

\3) table：显示该行数据是关于哪张表

表示explain的一行正在访问哪个表。当from子句中有子查询时，table列是<derivenN> 格式，表示当前查询依赖id=N的查询，于是先执行id=N的查询。当有union时，UNIONRESULT的table列的值为<union1,2>，1和2表示参与union的select行id。

**partitions**

\4) partitions：匹配的分区

**type**

\5) type：表的连接类型，MySQL在表中找到所需行的方式，又称“访问类型”。其值，性能由高到底排列如下：

***\*NULL:\**** MySQL在优化过程中分解语句，执行时甚至不用访问表或索引，例如从一个索引列里选取最小值可以通过单独索引查找完成

 

***\*system：\****表只有一行记录，相当于系统表，是const的特例

 

***\*const：\****通过索引一次就找到，只匹配一行数据

注：const、system: 当MySQL对查询某部分进行优化，并转换为一个常量时，使用这些类型访问。如将主键置于where列表中，MySQL就能将该查询转换为一个常量,system是const类型的特例，当查询的表只有一行的情况下，使用system

 

***\*eq_ref：\****唯一性索引扫描，对于每个索引键，表中只有一条记录与之匹配。常用于主键或唯一索引扫描

***\*当使用了索引的全部组成部分，并且索引是PRIMARY KEY或UNIQUE NOT NULL 才会使用该类型，性能仅次于system及const\*******\*。\****

注：类似ref，区别就在使用的索引是唯一索引，对于每个索引键值，表中只有一条记录匹配，简单来说，就是多表连接中使用primary key或者 unique key作为关联条件

 

***\*ref：\****非唯一性索引扫描，返回匹配某个单独值的所有行。用于=、< 或 > 操作符带索引的列

***\*当满足索引的最左前缀规则，或者索引不是主键也不是唯一索引时才会发生。如果使用的索引只会匹配到少量的行，性能也是不错的\****。

注：表示上述表的连接匹配条件，即哪些列或常量被用于查找索引列上的值。相比eq_ref，不使用唯一索引，而是使用普通索引或者唯一性索引的部分前缀，索引要和某个值相比较，可能会找到多个符合条件的行

 

***\*fulltext：\****全文索引


	***\*ref_or_null：\****该类型类似于ref，但是MySQL会额外搜索哪些行包含了NULL。这种类型常见于解析子查询

SELECT * FROM ref_table  WHERE key_column=expr OR key_column IS NULL;


	***\*index_merge：\****此类型表示使用了索引合并优化，表示一个查询里面用到了多个索引


	***\*unique_subquery：\****该类型和eq_ref类似，但是使用了IN查询，且子查询是主键或者唯一索引。例如：

value IN (SELECT primary_key FROM single_table WHERE some_expr)

 

***\*index_subquery：\****和unique_subquery类似，只是子查询使用的是非唯一索引

value IN (SELECT key_column FROM single_table WHERE some_expr)

 

***\*range：\****只检索给定范围的行，使用一个索引来选择行。一般使用between、>、<情况

 

***\*index：\****Full Index Scan，index与ALL区别为index类型只遍历索引树。

当查询仅使用索引中的一部分列时，可使用此类型。有两种场景会触发：

1、如果索引是查询的覆盖索引，并且索引查询的数据就可以满足查询中所需的所有数据，则只扫描索引树。此时，explain的Extra 列的结果是Using index。index通常比ALL快，因为索引的大小通常小于表数据。

2、按索引的顺序来查找数据行，执行了全表扫描。此时，explain的Extra列的结果不会出现Uses index。

 

***\*ALL：\****Full Table Scan，全表扫描，性能最差

注：前5种情况都是理想情况的索引使用情况。通常优化至少到range级别，最好能优化到 ref

***\*possible_keys\****

\6) possible_keys：显示查询可能使用哪些索引来查找，指出MySQL使用哪个索引在该表找到行记录。如果该值为 NULL，说明没有使用索引，可以建立索引提高性能

注：explain时可能出现possible_keys有列，而key显示NULL的情况，这种情况是因为表中数据不多，MySQL认为索引对此查询帮助不大，选择了全表查询。 

如果该列是NULL，则没有相关的索引。在这种情况下，可以通过检查where子句看是否可以创造一个适当的索引来提高查询性能，然后用explain查看效果。

***\*key\****

\7) key：显示MySQL实际使用的索引。如果为NULL，则没有使用索引查询

注：如果没有选择索引，键是NULL。要想强制MySQL使用或忽视possible_keys列中的索引，在查询中使用FORCE INDEX、USE INDEX或者IGNORE INDEX。

***\*key_len\****

\8) key_len：表示索引中使用的字节数，通过该列计算查询中使用的索引的长度（key_len显示的值为索引字段的最大可能长度，并非实际使用长度，即key_len是根据表定义计算而得，不是通过表内检索出的）。

在不损失精确性的情况下，长度越短越好显示的是索引字段的最大长度，并非实际使用长度

key_len计算公式： https://www.cnblogs.com/gomysql/p/4004244.html

 

***\*ref\****

\9) ref：显示该表的索引字段关联了哪张表的哪个字段

表示上述表的连接匹配条件，即哪些列或常量被用于查找索引列上的值

***\*rows\****

\10) rows：根据表统计信息及选用情况，大致估算出找到所需的记录或所需读取的行数，数值越小越好

表示MySQL根据表统计信息及索引选用情况，估算的找到所需的记录所需要读取的行数，这个不是结果集里的行数。

实际上explain的rows是MySQL预估的行数，是根据查询条件、索引和limit综合考虑出来的预估行数。

***\*filtered\****

\11) filtered：返回结果的行数占读取行数的百分比，值越大越好

用rows × filtered可获得和下一张表连接的行数。

例如rows = 1000，filtered = 50%，则和下一张表连接的行数是500。

TIPS

● 在MySQL 5.7之前，想要显示此字段需使用explain extended命令；

● MySQL.5.7及更高版本，explain默认就会展示filtered

 

***\*extra\****

\12) extra：包含不合适在其他列中显示但十分重要的额外信息，常见的值如下：

using filesort：说明 MySQL会对数据使用一个外部的索引排序，而不是按照表内的索引顺序进行读取。出现该值，应该优化SQL

using temporary：使用了临时表保存中间结果，MySQL在对查询结果排序时使用临时表。常见于排序order by和分组查询 group by。出现该值，应该优化 SQL

using index：表示相应的 select 操作使用了覆盖索引，避免了访问表的数据行，效率不错

using where：where 子句用于限制哪一行

using join buffer：使用连接缓存

distinct：发现第一个匹配后，停止为当前的行组合搜索更多的行

注意：出现前 2 个值，SQL 语句必须要优化。

###### show warnings

参考：

[https://mp.weixin.qq.com/s?__biz=MzU2MTk3MTAwNw==&mid=2247486128&idx=2&sn=76e1bb7a22d8c7088c2dc05fcd34c50b&chksm=fc71e0e0cb0669f688e73a88ff1f5be99fd07940bccd5304947f542a192c7940ac620697d88a&mpshare=1&srcid=0715pracj4wgNWGBZTFHiOXt&sharer_sharetime=1594785863754&sharer_shareid=0ebbb22ff076b0e8b143bed8d7993d6e&from=timeline&scene=2&subscene=1&clicktime=1594795308&enterid=1594795308&ascene=2&devicetype=android-27&version=27000f51&nettype=ctnet&abtest_cookie=AAACAA%3D%3D&lang=zh_CN&exportkey=AVHcRW3sW4aidcClVT6%2F17g%3D&pass_ticket=nZwdLUJJ5M4J2BpVxNlHWAfKBtXk2T1gVLASyBhQWXZ3ZysYTo9nkjk77ZyxDOmp&wx_header=1](https://mp.weixin.qq.com/s?__biz=MzU2MTk3MTAwNw==&mid=2247486128&idx=2&sn=76e1bb7a22d8c7088c2dc05fcd34c50b&chksm=fc71e0e0cb0669f688e73a88ff1f5be99fd07940bccd5304947f542a192c7940ac620697d88a&mpshare=1&srcid=0715pracj4wgNWGBZTFHiOXt&sharer_sharetime=1594785863754&sharer_shareid=0ebbb22ff076b0e8b143bed8d7993d6e&from=timeline&scene=2&subscene=1&clicktime=1594795308&enterid=1594795308&ascene=2&devicetype=android-27&version=27000f51&nettype=ctnet&abtest_cookie=AAACAA==&lang=zh_CN&exportkey=AVHcRW3sW4aidcClVT6/17g=&pass_ticket=nZwdLUJJ5M4J2BpVxNlHWAfKBtXk2T1gVLASyBhQWXZ3ZysYTo9nkjk77ZyxDOmp&wx_header=1)

 

EXPLAIN可产生额外的扩展信息，可通过在EXPLAIN语句后紧跟一条SHOW WARNING语句查看扩展信息。

TIPS

● 在MySQL 8.0.12及更高版本，扩展信息可用于SELECT、DELETE、INSERT、REPLACE、UPDATE语句；在MySQL 8.0.12之前，扩展信息仅适用于SELECT语句；

● 在MySQL 5.6及更低版本，需使用EXPLAIN EXTENDED xxx语句；而从MySQL 5.7开始，无需添加EXTENDED关键词。

 

###### *profile*

使用 profiling 命令可以了解 SQL 语句消耗资源的详细信息（每个执行步骤的开销）。

***\*查看profile开启情况\****

select @@profiling;

返回结果：

mysql> select @@profiling;

+-------------+

| @@profiling |

+-------------+

|      0 |

+-------------+

1 row in set, 1 warning (0.00 sec)

0 表示关闭状态,1 表示开启

 

***\*启用profile\****

set profiling = 1;  

返回结果：

mysql> set profiling = 1;  

Query OK, 0 rows affected, 1 warning (0.00 sec)

mysql> select @@profiling;

+-------------+

| @@profiling |

+-------------+

|      1 |

+-------------+

1 row in set, 1 warning (0.00 sec)

在连接关闭后，profiling 状态自动设置为关闭状态。

 

***\*查看执行的SQL列表\****

show profiles;

返回结果：

mysql> show profiles;

+----------+------------+------------------------------+

| Query_ID | Duration  | Query             |

+----------+------------+------------------------------+

|     1 | 0.00062925 | select @@profiling      |

|     2 | 0.00094150 | show tables          |

|     3 | 0.00119125 | show databases        |

|     4 | 0.00029750 | SELECT DATABASE()       |

|     5 | 0.00025975 | show databases        |

|     6 | 0.00023050 | show tables          |

|     7 | 0.00042000 | show tables          |

|     8 | 0.00260675 | desc role           |

|     9 | 0.00074900 | select name,is_key from role |

+----------+------------+------------------------------+

9 rows in set, 1 warning (0.00 sec)

该命令执行之前，需要执行其他 SQL 语句才有记录。

 

***\*查询指定ID的执行详细信息\****

show profile for query Query_ID;

返回结果：

mysql> show profile for query 9;

+----------------------+----------+

| Status        | Duration |

+----------------------+----------+

| starting       | 0.000207 |

| checking permissions | 0.000010 |

| Opening tables    | 0.000042 |

| init         | 0.000050 |

| System lock      | 0.000012 |

| optimizing      | 0.000003 |

| statistics      | 0.000011 |

| preparing       | 0.000011 |

| executing       | 0.000002 |

| Sending data     | 0.000362 |

| end          | 0.000006 |

| query end       | 0.000006 |

| closing tables    | 0.000006 |

| freeing items     | 0.000011 |

| cleaning up      | 0.000013 |

+----------------------+----------+

15 rows in set, 1 warning (0.00 sec)

每行都是状态变化的过程以及它们持续的时间。Status 这一列和 show processlist 的 State 是一致的。因此，需要优化的注意点与上文描述的一样。

 

***\*获取CPU、Block IO等信息\****

show profile block io,cpu for query Query_ID;

show profile cpu,block io,memory,swaps,context switches,source for query Query_ID;

show profile all for query Query_ID;

##### explain查看执行计划

我们可以用EXPLAIN或DESCRIBE(简写:DESC)命令分析一条查询语句的执行信息。

例:

DESC SELECT * FROM `user`

显示:

clipboard.png

其中会显示索引和查询数据读取数据条数等信息.

 

**explain返回各列的含义：**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED73.tmp.jpg) 

table：显示这一行的数据是关于哪张表的

type：这是重要的列，显示连接使用了何种类型，从最好到最差的连接类型为const、eq_reg、ref、range、index和ALL

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED74.tmp.jpg) 

注：

const常数时间查找，一般对于主键或唯一索引都是常数时间查找

eq_reg范围查找，一般是主键或唯一索引的范围查找

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED75.tmp.jpg) 

possible_key：显示可能应用在这张表中的索引，如果为空，没有可能的索引

key：实际使用的索引，如果为NULL，则没有使用索引

key_len：使用的索引的长度，在不损失精确性的情况下，长度越短越好

ref：显示索引的哪一列被使用了，如果可能的话，是一个常数最好

rows：MySQL认为必须检查的用来返回请求数据的行数

extra：列需要注意的返回值

Using filesort：看到这个的时候，查询就需要优化了，MySQL需要进行额外的步骤来发现如何对返回的行排序，它根据连接类型以及存储排序键值和匹配条件的全部行的行指针来排序全部行

Using temporary：看到这个的时候，查询需要优化，这里MySQL需要创建一个临时表来存储结果，这通常发生在对不同的列集进行ORDER BY上，而不是GROUP BY上

 

##### 如何发现有问题的SQL？

​	使用MySQL慢查询日志对有效率问题的SQL进行监控：

​	show variables like ‘slow_query_log’

​	set global show_query_log_file = ‘/home/mysql/sql_log/mysql-slow.log’

​	set global log_queries_not_usring_indexs =on//没使用索引的SQL是否记录到慢查询日志

​	set global long_query_time =1

###### 查看运行的线程

执行命令：

show processlist

返回结果：

mysql> show processlist;

+----+------+-----------+------+---------+------+----------+------------------+

| Id | User | Host    | db  | Command | Time | State   | Info       |

+----+------+-----------+------+---------+------+----------+------------------+

|  9 | root | localhost | test | Query  |   0 | starting | show processlist |

+----+------+-----------+------+---------+------+----------+------------------+

1 row in set (0.00 sec)

从返回结果中我们可以了解该线程执行了什么命令/SQL 语句以及执行的时间。实际应用中，查询的返回结果会有 N 条记录。

其中，返回的 State 的值是我们判断性能好坏的关键，其值出现如下内容，则该行记录的 SQL 语句需要优化：

Converting HEAP to MyISAM # 查询结果太大时，把结果放到磁盘，严重

Create tmp table #创建临时表，严重

Copying to tmp table on disk  #把内存临时表复制到磁盘，严重

locked #被其他查询锁住，严重

loggin slow query #记录慢查询

Sorting result #排序

###### 开启慢查询日志

在配置文件my.cnf中的[mysqld]一行下边添加两个参数：

slow_query_log = 1

slow_query_log_file=/var/lib/mysql/slow-query.log

long_query_time = 2

 

log_queries_not_using_indexes = 1

其中，slowquerylog = 1表示开启慢查询；slowquerylogfile表示慢查询日志存放的位置；longquerytime = 2表示查询 >=2秒才记录日志；logqueriesnotusing_indexes = 1记录没有使用索引的 SQL 语句。

注意：slowquerylog_file的路径不能随便写，否则MySQL服务器可能没有权限将日志文件写到指定的目录中。建议直接复制上文的路径。

 

修改保存文件后，重启MySQL服务。在/var/lib/mysql/目录下会创建slow-query.log 日志文件。连接MySQL服务端执行如下命令可以查看配置情况。

show variables like 'slow_query%';

show variables like 'long_query_time';

测试慢查询日志：

mysql> select sleep(2);

+----------+

| sleep(2) |

+----------+

|     0 |

+----------+

1 row in set (2.00 sec)

打开慢查询日志文件

[root@localhost mysql]# vim /var/lib/mysql/slow-query.log

/usr/sbin/mysqld, Version: 5.7.19-log (MySQL Community Server (GPL)). started with:

Tcp port: 0  Unix socket: /var/lib/mysql/mysql.sock

Time         Id Command   Argument

\# Time: 2017-10-05T04:39:11.408964Z

\# User@Host: root[root] @ localhost []  Id:   3

\# Query_time: 2.001395  Lock_time: 0.000000 Rows_sent: 1  Rows_examined: 0

use test;

SET timestamp=1507178351;

select sleep(2);

我们可以看到刚才执行了2秒的SQL语句被记录下来了。

虽然在慢查询日志中记录查询慢的SQL信息，但是日志记录的内容密集且不易查阅。因此，我们需要通过工具将SQL筛选出来。

MySQL提供mysqldumpslow工具对日志进行分析。我们可以使用mysqldumpslow --help 查看命令相关用法。

常用参数如下：

  -s：排序方式，后边接着如下参数

​    c：访问次数

​    l：锁定时间

​    r：返回记录

​    t：查询时间

  al：平均锁定时间

  ar：平均返回记录书

  at：平均查询时间

  -t：返回前面多少条的数据

  -g：翻遍搭配一个正则表达式，大小写不敏感

案例：

获取返回记录集最多的10个sql

mysqldumpslow -s r -t 10 /var/lib/mysql/slow-query.log

获取访问次数最多的10个sql

mysqldumpslow -s c -t 10 /var/lib/mysql/slow-query.log

获取按照时间排序的前10条里面含有左连接的查询语句

mysqldumpslow -s t -t 10 -g "left join" /var/lib/mysql/slow-query.log

 

###### *系统层面分析*

cpu方面：

vmstat、sar top、htop、nmon、mpstat

内存：

free、ps -aux

IO设备（磁盘、网络）：

iostat、ss、netstat、iptraf、iftop、lsof

 

***\*问题一：cpu负载高，IO负载低\****

内存不够

磁盘性能差

SQL问题-->去数据库层，进一步排查sql问题

IO出问题了（磁盘到临界了、raid设计不好、raid降级、锁、在单位时间内tps过高）

tps过高: 大量的小数据IO、大量的全表扫描

 

***\*问题二：IO负载高，cpu负载低\****

大量小的IO写操作：

autocommit，产生大量小IO；

IO/PS,磁盘的一个定值，硬件出厂的时候，厂家定义的一个每秒最大的IO次数。

大量大的IO写操作；

SQL问题的几率比较大；

 

***\*问题三：IO和cpu负载都很高\****

硬件不够了或sql存在问题

 

##### *慢查询*

​	通过show processlist或开启慢查询，获取有问题的SQL。

慢查询记录的依据：

long_query_time：如果执行时间超过本参数设置记录慢查询。

log_queries_not_using_indexes：如果语句未使用索引记录慢查询。

log_slow_admin_statements：是否记录管理语句。(如ALTER TABLE,ANALYZE TABLE, CHECK TABLE, CREATE INDEX, DROP INDEX, OPTIMIZE TABLE, and REPAIR TABLE.)

***\*long_query_time参数的具体含义\*******\*：\****

如果我们将语句的执行时间定义为如下：

实际消耗时间 = 实际执行时间+锁等待消耗时间

那么long_query_time实际上界定的是实际执行时间，所以有些情况下虽然语句实际消耗的时间很长但是是因为锁等待时间较长而引起的，那么实际上这种语句也不会记录到慢查询。

我们看一下log_slow_applicable函数的代码片段：

res= cur_utime - thd->utime_after_lock;

if(res > thd->variables.long_query_time)

  thd->server_status|= SERVER_QUERY_WAS_SLOW;

else

  thd->server_status&= ~SERVER_QUERY_WAS_SLOW;

这里实际上清楚的说明了上面的观点，是不是慢查询就是通过这个函数进行的判断的，非常重要。我可以清晰的看到如下公式：

res (实际执行时间) = cur_utime(实际消耗时间) - thd->utime_after_lock(锁等待消耗时间) 实际上在慢查询中记录的正是

Query_time：实际消耗时间

Lock_time：锁等待消耗时间

但是是否是慢查询其评判标准却是实际执行时间及Query_time - Lock_time

其中锁等待消耗时间( Lock_time)我现在已经知道的包括：

MySQL层MDL LOCK等待消耗的时间。(Waiting for table metadata lock)

MySQL层MyISAM表锁消耗的时间。(Waiting for table level lock)

InnoDB层行锁消耗的时间。

 

###### 开启慢查询

①查看慢查询日志是否开启

SHOWVARIABLESLIKE'%slow_query_log%';

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED86.tmp.jpg) 

②如果没有开启，通过命令开启慢查询日志

SETGLOBAL slow_query_log=1;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED87.tmp.jpg) 

③设置慢查询日志的时间，这里的单位是秒，意思是只要是执行时间超过X秒的查询语句被记录到这个日志中。这里的X就是你要设置的。（下面的例子设置的是3秒）

SETGLOBAL long_query_time=3;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED88.tmp.jpg) 

④查看多少 SQL 语句是超过查询阀值的（3秒）

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED89.tmp.jpg) 

 

###### 慢查询日志

​	慢查询日志所包含的内容：

​	**执行SQL的主机信息：**

​	#User@Host：root[root] @localhost []

​	**SQL****的执行信息：**

​	#Query_time：0.000024 LOCK_time：0.000000 Rows_sent：0 

​	Rows_examinned:0

​	**SQL****执行时间：**

​	SET timestamp=****

​	**SQL的内容：**

​	select CONCAT(‘storage engine:’,@@storage_engine) AS INFO；

###### 分析工具

***\*mysqldumpslow\****

​	MySQL官方的分析工具（安装MySQL后默认安装）：

​	mysqldumpslow [OPTS…] [LOGS…]

​	--verbose verbose

​	--debug debug

​	--help	write this text to standard output

​	输出信息包括：

​	Count：SQL执行次数

​	Time：SQL执行时间

​	Lock：SQL锁定时间

​	Rows：涉及行数

​	User@ip：哪个用户在哪个机器上执行的

***\*pt-query-digest\****

​	使用mysqldumpslow的输出信息相对较少，可以采用另一个pt-query-digest：

​	输出到文件：

​	pt-query-digest slow-log > slow_log.report

​	输出到数据库表：

​	pt-query-digest slow.log –review	\

​		h=127.0.0.1,D=test,p=root,P=3306,u=root,t=query_review	\

​		--create-reviewtable	\

​		--review-history	t=hostname_slow

###### 发现问题

​	如何通过慢查询日志发现有问题的SQL？

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED9A.tmp.jpg) 

1、 查询次数多且每次查询占用时间长的SQL

通常为pt-query-digest分析的前几个查询

2、 IO大的SQL

注意pt-query-digest分析中的Rows_examine项（即涉及行数）

3、 未命中索引的SQL

注意pt-query-digest分析中Rows_examine和Rows_Send的对比

##### SELECT务必指明字段名

SELECT * 增加很多不必要的消耗（cpu、io、内存、网络带宽）；增加了使用覆盖索引的可能性；当表结构发生改变时，字段也需要更新。

所以要求直接在select后面接上字段名。

##### 聚合函数优化

###### max()函数优化

​	查找最后支付时间：

​	select max(payment_date) from payment;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED9B.tmp.jpg) 

​	可以通过在查询字段上建索引优化：

​	create index inx_payment_date on payment(payment_date);

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED9C.tmp.jpg) 

###### count()函数优化

​	在一条SQL中同时查出2006年和2007年电影的数量

​	错误的方式：

​	SELECT COUNT(release_year=’2006’ OR release_year=’2007’) FROM film;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsED9D.tmp.jpg) 

​	无法分开计算2006和2007年的电影数量：

​	SELECT COUNT(*) FROM film WHERE release_year=’2006’ AND release_year=’2007’;

​	realese_year不可能同时为2006和2007，因此有逻辑错误。

​	正确的SQL：

​	SELECT COUNT(release_year=’2006’ OR NULL) AS ‘2006年电影数量’,

​			COUNT(release_year=’2007’ OR NULL) AS ‘2007年电影数量’

​	FROM film;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDAD.tmp.jpg) 

​	注：count(*)包括内容为NULL的行，count(id)不包括为NULL的行。

##### WHERE子句

WHERE子句里面的列尽量被索引，只是“尽量”，并不是说所有的列。因地制宜，根据实际情况进行调整，因为有时索引太多也会降低性能。

 

###### 避免对字段进行运算

避免在where子句中对字段进行表达式操作。

比如：

select user_id,user_project from table_name where age*2=36;

中对字段就行了算术运算，这会造成引擎放弃使用索引，建议改成

select user_id,user_project from table_name where age=36/2;

###### 避免对字段进行NULL判断

***\*where子句中考虑使用默认值代替null\****。

反例：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDAE.tmp.jpg) 

正例：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDAF.tmp.jpg) 

理由：

并不是说使用了is null或者is not null 就会不走索引了，这个跟mysql版本以及查询成本都有关。

如果mysql优化器发现，走索引比不走索引成本还要高，肯定会放弃索引，这些条件 ！=，>isnull，isnotnull经常被认为让索引失效，其实是因为一般情况下，查询的成本高，优化器自动放弃索引的。

如果把null值，换成默认值，很多时候让走索引成为可能，同时，表达意思会相对清晰一点。

 

如果字段类型是字符串，where时一定用引号括起来，否则索引失效

反例：

select * from user where userid =123;

正例：

select * from user where userid ='123';

理由：

为什么第一条语句未加单引号就不走索引了呢？这是因为不加单引号时，是字符串跟数字的比较，它们类型不匹配，MySQL会做隐式的类型转换，把它们转换为浮点数再做比较。

###### 避免隐式类型转换

where子句中出现column字段的类型和传入的参数类型不一致的时候发生的类型转换，建议先确定where中的参数类型（尤其是字符类型不加’’的情况）。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDB0.tmp.jpg) 

###### 不建议使用%前缀模糊匹配

例如LIKE “%name”或者LIKE “%name%”，这种查询会导致索引失效而进行全表扫描。但是可以使用LIKE “name%”。

那如何查询%name%？

如下图所示，虽然给secret字段添加了索引，但在explain结果果并没有使用

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDC1.tmp.jpg) 

那么如何解决这个问题呢，答案：使用全文索引

在我们查询中经常会用到select id,fnum,fdst from table_name where user_name like '%zhangsan%'; 。这样的语句，普通索引是无法满足查询需求的。庆幸的是在MySQL中，有全文索引来帮助我们。

创建全文索引的sql语法是：

ALTER TABLE `table_name` ADD FULLTEXT INDEX `idx_user_name` (`user_name`);

使用全文索引的sql语句是：

select id,fnum,fdst from table_name where match(user_name) against('zhangsan' in boolean mode);

注意：在需要创建全文索引之前，请联系DBA确定能否创建。同时需要注意的是查询语句的写法与普通索引的区别。

 

###### 运算符优化

比较运算符能用 “=”就不用“<>”，“=”增加了索引的使用几率。

###### BETWEEN AND优化

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDC2.tmp.jpg) 

速度也很快，id上有主键索引，这个采用的上面介绍的范围查找可以快速定位目标数据。

但是如果范围太大，跨度的page也太多，速度也会比较慢，如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDC3.tmp.jpg) 

上面id的值跨度太大，1所在的页和200万所在页中间有很多页需要读取，所以比较慢。

所以使用between and的时候，区间跨度不要太大。

###### OR优化

***\*如果限制条件中其他字段没有索引，尽量少用or\****

or两边的字段中，如果有一个不是索引字段，而其他条件也不是索引字段，会造成该查询不走索引的情况。

***\*优化方案：\****

很多时候使用 union all 或者是union(必要的时候)的方式来代替“or”会得到更好的效果。

 

当MySQL使用OR查询时，如果要利用索引的话，必须每个条件列都使独立索引，而不是复合索引（多列索引），才能保证使用到查询的时候使用到索引。

 

比如我们新建一张用户信息表user_info

mysql> select*from user_info;

+---------+--------+----------+-----------+

| user_id | idcard | name   | address   |

+---------+--------+----------+-----------+

|    1 | 111111 | Zhangsan | Kunming  |

|    2 | 222222 | Lisi   | Beijing  |

|    3 | 333333 | Wangwu  | Shanghai  |

|    4 | 444444 | Lijian  | Guangzhou |

+---------+--------+----------+-----------+

4 rows in set

之后创建ind_name_id(user_id, name)复合索引、id_index(id_index)独立索引，idcard主键索引三个索引。

mysql> show index from user_info;

+-----------+------------+-------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+

| Table   | Non_unique | Key_name   | Seq_in_index | Column_name | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment |

+-----------+------------+-------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+

| user_info |      0 | PRIMARY   |       1 | idcard    | A     |      4 | NULL   | NULL  |    | BTREE    |     |        |

| user_info |      1 | ind_name_id |       1 | user_id   | A     |      4 | NULL   | NULL  |    | BTREE    |     |        |

| user_info |      1 | ind_name_id |       2 | name     | A     |      4 | NULL   | NULL  | YES  | BTREE    |     |        |

| user_info |      1 | id_index   |       1 | user_id   | A     |      4 | NULL   | NULL  |    | BTREE    |     |        |

+-----------+------------+-------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+

4 rows in set

 

测试一：OR连接两个有单独索引的字段，整个SQL查询才会用到索引(index_merge)，并且我们知道OR实际上是把每个结果最后UNION一起的。

mysql> explain select*from user_info where user_id=1 or idcard='222222';

+----+-------------+-----------+------------+-------------+------------------------------+---------------------+---------+------+------+----------+----------------------------------------------------+

| id | select_type | table   | partitions | type     | possible_keys         | key         | key_len | ref  | rows | filtered | Extra                        |

+----+-------------+-----------+------------+-------------+------------------------------+---------------------+---------+------+------+----------+----------------------------------------------------+

|  1 | SIMPLE    | user_info | NULL    | index_merge | PRIMARY,ind_name_id,id_index | ind_name_id,PRIMARY | 4,62   | NULL |   2 |    100 | Using sort_union(ind_name_id,PRIMARY); Using where |

+----+-------------+-----------+------------+-------------+------------------------------+---------------------+---------+------+------+----------+----------------------------------------------------+

1 row in set

 

测试二：OR使用复合索引的字段name，与没有索引的address，整个SQL都是ALL全表扫描的

mysql> explain select*from user_info where name='Zhangsan' or address='Beijing';

+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+

| id | select_type | table   | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra    |

+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+

|  1 | SIMPLE    | user_info | NULL    | ALL  | NULL      | NULL | NULL   | NULL |   4 |   43.75 | Using where |

+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+

1 row in set

交换OR位置并且使用另外的复合索引的列，也是ALL全表扫描：

mysql> explain select*from user_info where address='Beijing' or user_id=1;

+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+

| id | select_type | table   | partitions | type | possible_keys     | key  | key_len | ref  | rows | filtered | Extra    |

+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+

|  1 | SIMPLE    | user_info | NULL    | ALL  | ind_name_id,id_index | NULL | NULL   | NULL |   4 |   43.75 | Using where |

+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+

1 row in set

 

##### 优化子查询

***\*在MySQL中，尽量使用JOIN来代替子查询\****。因为子查询需要嵌套查询，***\*嵌套查询时会建立一张临时表，临时表的建立和删除都会有较大的系统开销\****，而***\*连接查询不会创建临时表，而且join连接查询还可以在o\*******\*n\*******\*条件中使用索引\****，因此效率比嵌套子查询高。

注：***\*子查询开销主要在临时表和未使用索引上！\****

说明：在实际中遇到IN(select from 分区表 where 索引=’’)这种语句及其慢的情况，有两个原因：1）where条件没有分区键，导致顺序扫描所有分区；2）IN子查询会建临时表，比较耗时。

 

​	explain SELECT title,realease_year,LENGTH

​	FROM film

​	WHERE film_id IN(

​		SELECT film_id FROM film_actor WHERE actor_id IN(

​			SELECT actor_id FROM actor WHERE first_name = ‘sandra’));

 

​	通常情况下，需要***\*把子查询优化为join查询，但在优化时需要注意关联键是否有一对多的关系，如果\*******\*存在\*******\*1对\*******\*多的关系，则需\*******\*要注意重复数据，可以使用distinct去重\****。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDD3.tmp.jpg) 

 

###### IN优化

***\*SQL语句中IN包含的值不应过多\****。

MySQL对于IN做了相应的优化，即将IN中的常量全部存储在一个数组里面，而且这个数组是排好序的。但是如果数值较多，产生的消耗也是比较大的。再例如：select id from table_name where num in(1,2,3) 对于连续的数值，能用 between 就不要用 in 了；再或者使用连接来替换。

***\*优化方法：\****

1、尝试是否可以转换为between

2、尝试使用JOIN连接操作

 

***\*exist&in的合理利用\****

假设表A表示某企业的员工表，表B表示部门表，查询所有部门的所有员工，很容易有以下SQL:

select * from A where deptId in (select deptId from B);

这样写等价于：

先查询部门表B

select deptId from B

再由部门deptId，查询A的员工

select * from A where A.deptId = B.deptId

可以抽象成这样的一个循环：  

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDD4.tmp.jpg) 

显然，除了使用in，我们也可以用exists实现一样的查询功能，如下：

select * from A where exists (select 1 from B where A.deptId = B.deptId);

因为exists查询的理解就是，***\*先执行主查询，获得数据后，再放到子查询中做条件验证，根据验证结果（true或者false），来决定主查询的数据结果是否得意保留\****。

那么，这样写就等价于：

select * from A,先从A表做循环

select * from B where A.deptId = B.deptId,再从B表做循环.

同理，可以抽象成这样一个循环：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDD5.tmp.jpg) 

数据库最费劲的就是跟程序链接释放。假设链接了两次，每次做上百万次的数据集查询，查完就走，这样就只做了两次；相反建立了上百万次链接，申请链接释放反复重复，这样系统就受不了了。即mysql优化原则，就是小表驱动大表，小的数据集驱动大的数据集，从而让性能更优。

因此，***\*我们要选择最外层循环小的，也就是，如果B的数据量小于A，适合使用in，如果B的数据量大于A，即适合选择exist\****。

 

###### EXIST优化

***\*区分in和exists,not in和not exists：\****

select * from 表A where id in (select id from 表B)

上面sql语句相当于：

select * from 表A where exists(select * from 表B where 表B.id=表A.id)

区分in和exists主要是造成了驱动顺序的改变（这是性能变化的关键），如果是exists，那么以外层表为驱动表，先被访问，如果是IN，那么先执行子查询。所以IN适合于外表大而内表小的情况；EXISTS适合于外表小而内表大的情况。

关于not in和not exists，推荐使用not exists，不仅仅是效率问题，not in可能存在逻辑问题。如何高效的写出一个替代not exists的sql语句？

原sql语句：

select colname … from A表 where a.id not in (select b.id from B表)

高效的sql语句：

select colname … from A表 Left join B表 on where a.id = b.id where b.id is null

取出的结果集如下图表示，A表不在B表中的数据：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDD6.tmp.jpg) 

 

##### group by优化

为了提高group by语句的效率，可以在执行到该语句前，把不需要的记录过滤掉。

反例：

select job,avg(salary) from employee group by job having job ='president' or job = 'managent'

正例：

select job,avg(salary) from employee where job ='president' or job = 'managent' group by job；

 

MySQL通过索引来优化GROUPBY查询。在无法使用索引的时候，会使用两种策略优化：临时表和文件排序分组。

可以通过两个参数SQL_BIG_RESULT和SQL_SMALL_RESULT提升其性能。

这两个参数只对Select语句有效。它们告诉优化器对GROUPBY查询使用临时表及排序。

SQL_SMALL_RESULT告诉优化器结果集会很小，可以将结果集放在内存中的索引临时表，以避免排序操作。

如果是SQL_BIG_RESULT，则告诉优化器结果集可能会非常大，建议使用磁盘临时表做排序操作。

例如：

SelectSQL_BUFFER_RESULTfield1, count(*) from table1 groupby field1

假设两个表做关联查询，选择查询表中的标识列（主键）分组效率会高。

例如 actor 表和 film 表通过 actorId 做关联，查询如下：

Select actor.FirstName, actor.LastName,count(*) from film inner join actor using(actorId)

Group by actor.FirstName,actor.LastName

就可以修改为：

Select actor.FirstName, actor.LastName, count(*) from film inner join actor using(actorId)

Group by film.actorId

 

在默认情况下，MySQL中的GROUP BY语句会对其后出现的字段进行默认排序（非主键情况），就好比我们使用ORDER BY col1,col2,col3…所以我们在后面跟上具有相同列（与GROUP BY后出现的col1,col2,col3…相同）ORDER BY子句并没有影响该SQL的实际执行性能。

那么就会有这样的情况出现，我们对查询到的结果是否已经排序不在乎时，可以使用ORDER BY NULL禁止排序达到优化目的。

在user_1中执行select id, sum(money) form user_1 group by name时，会默认排序（注意group by后的column是非index才会体现group by的排序，如果是primary key，那之前说过了InnoDB默认是按照主键index排好序的）

mysql> select*from user_1;

+----+----------+-------+

| id | name   | money |

+----+----------+-------+

|  1 | Zhangsan |   32 |

|  2 | Lisi   |   65 |

|  3 | Wangwu  |   44 |

|  4 | Lijian  |  100 |

+----+----------+-------+

4 rows in set

不禁止排序，即不使用ORDER BY NULL时：有明显的Using filesort。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDD7.tmp.jpg) 

当使用ORDER BY NULL禁止排序后，Using filesort不存在

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDE8.tmp.jpg) 

 

​	explain SELECT actor.first_name.actor.last_name,COUNT(*)

​	FROM sakila.film_actor

​	INNER JOIN sakila.actor USING(actor_id)

​	GROUP BY film_actor.actor_id;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDE9.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDEA.tmp.jpg) 

​	为了避免临时表和文件排序，可以采用如下优化：

explain SELECT actor.first_name.actor.last_name,c.cnt

​	FROM sakila.film_actor

​	INNER JOIN (

SELECT actor.id, COUNT(*) AS cnt FROM sakila.film_actor GROUP BY actor_id

) AS c USING(actor_id);

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDEB.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDFC.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDFD.tmp.jpg) 

​	注：如果需要在关联表中某一列采用group by，最好选择同一表的列进行group by。

 

##### order by优化

ORDER BY的列如果被索引，性能也会更好。

MySQL可以使用一个索引来满足ORDER BY子句的排序，而不需要额外的排序，但是需要满足以下几个条件：

1、WHERE条件和OREDR BY使用相同的索引：即key_part1与key_part2是复合索引，where中使用复合索引中的key_part1

SELECT*FROM user WHERE key_part1=1 ORDER BY key_part1 DESC, key_part2 DESC;

2、而且ORDER BY顺序和索引顺序相同：

SELECT*FROM user ORDER BY key_part1, key_part2;

3、并且要么都是升序要么都是降序：

SELECT*FROM user ORDER BY key_part1 DESC, key_part2 DESC;

 

但以下几种情况则不使用索引：

1、ORDER BY中混合ASC和DESC：

SELECT*FROM user ORDER BY key_part1 DESC, key_part2 ASC;

2、查询行的关键字与ORDER BY所使用的不相同，即WHERE后的字段与ORDER BY后的字段是不一样的

SELECT*FROM user WHERE key2 =‘xxx’ORDER BY key1;

3、ORDER BY对不同的关键字使用，即ORDER BY后的关键字不相同

SELECT*FROM user ORDER BY key1, key2;

4、***\*不要使用ORDER BY RAND()\****

select id from `table_name` order by rand() limit 1000;

上面的sql语句，可优化为：

select id from `table_name` t1 join (select rand() * (select max(id) from `table_name`) as nid) t2 ont1.id > t2.nid limit 1000;

 

##### limit查询优化

明知只有一条查询结果，那请使用“LIMIT 1”，“LIMIT 1”可以避免全表扫描，找到对应结果就不会再继续扫描了。

 

​	limit常用于分页处理，时常会伴随order by从句使用，因此大多时候会使用filesorts这样会造成大量的IO问题。

​	SELECT film_id,description FROM sakila.film ORDER BY title LIMIT 50,5;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDFE.tmp.jpg) 

​	优化limit查询：

1、 使用有索引的列或主键进行order by操作

SELECT film_id,description FROM sakila.film ORDER BY film_id LIMIT 50,5;

注：使用主键排序，不会再使用文件排序，会使用索引，避免很多IO操作，扫描的行数比之前少了很多。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEDFF.tmp.jpg) 

​	但是上述方法存在一个问题，分页查询的越大，则扫描的行越多。

2、 记录上次返回的主键，在下次查询时使用主键过滤，避免数据量大时扫描大量的记录。

SELECT film_id,description FROM sakila.film WHERE film_id>55 AND film_id<=60 ORDER BY film_id LIMIT 1,5;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE0F.tmp.jpg) 

​	注：使用这种方式的缺点就是要求主键顺序增长排序且连续的，如果出现了空缺的某几行，可能会出现最后的结果不足5行的情况。

##### DISTINCT优化

慎用distinct关键字

distinct 关键字一般用来过滤重复记录，以返回不重复的记录。在查询一个字段或者很少字段的情况下使用时，给查询带来优化效果。但是在字段很多的时候使用，却会大大降低查询效率。

反例：

SELECT DISTINCT * from user;

正例：

select DISTINCT name from user;

理由：

带distinct的语句cpu时间和占用时间都高于不带distinct的语句。因为当查询很多字段时，如果使用distinct，数据库引擎就会对数据进行比较，过滤掉重复数据，然而这个比较、过滤的过程会占用系统资源，cpu时间。

##### JOIN子句优化

JOIN 子句里面的列尽量被索引。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE10.tmp.png) 

•LEFT JOIN A表为驱动表

•INNER JOIN MySQL会自动找出那个数据少的表作用驱动表

•RIGHT JOIN B表为驱动表

注意：MySQL中没有full join，可以用以下方式来解决

select * from A left join B on B.name = A.name 

where B.name is null

union all

select * from B;

 

###### 尽量使用inner join，避免left join

参与联合查询的表至少为2张表，一般都存在大小之分。如果连接方式是inner join，在没有其他过滤条件的情况下MySQL会自动选择小表作为驱动表，但是left join在驱动表的选择上遵循的是左边驱动右边的原则，即left join左边的表名为驱动表。

Inner join、left join、right join，优先使用Inner join，如果是left join，左边表结果尽量小

Inner join内连接，在两张表进行连接查询时，只保留两张表中完全匹配的结果集

left join在两张表进行连接查询时，会返回左表所有的行，即使在右表中没有匹配的记录。

right join在两张表进行连接查询时，会返回右表所有的行，即使在左表中没有匹配的记录。

都满足SQL需求的前提下，推荐优先使用Inner join（内连接），如果要使用left join，左边表数据结果尽量小，如果有条件的尽量放到左边处理。

 

###### 合理利用索引

被驱动表的索引字段作为on的限制字段。

 

###### 利用小表去驱动大表

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE11.tmp.jpg) 

从原理图能够直观的看出如果能够减少驱动表的话，减少嵌套循环中的循环次数，以减少 IO总量及CPU运算的次数。

 

###### 巧用STRAIGHT_JOIN

inner join是由mysql选择驱动表，但是有些特殊情况需要选择另个表作为驱动表，比如有group by、order by等Using filesort、Using temporary时。STRAIGHT_JOIN来强制连接顺序，在STRAIGHT_JOIN左边的表名就是驱动表，右边则是被驱动表。在使用STRAIGHT_JOIN有个前提条件是该查询是内连接，也就是inner join。其他链接不推荐使用STRAIGHT_JOIN，否则可能造成查询结果不准确。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE12.tmp.png) 

这个方式有时可能减少3倍的时间。

 

##### UNION语句优化

使用UNION ALL代替UNION，如果结果集允许重复的话。因为UNION ALL不去重，效率高于UNION。

union和union all的差异主要是前者需要将结果集合并后再进行唯一性过滤操作，这就会涉及到排序，增加大量的CPU运算，加大资源消耗及延迟。当然，union all的前提条件是两个结果集没有重复数据。

说明：对于WHERE ... OR ...的这种查询会导致索引失效，可以使用SELECT WHERE UNION SELECT WHERE优化，这样索引不会失效。

 

##### 嵌套查询优化

使用嵌套查询有时候可以使用更有效的JOIN连接代替，这是因为MySQL中不需要在内存中创建临时表完成SELECT子查询与主查询两部分查询工作。但是并不是所有的时候都成立，最好是在on关键字后面的列有索引的话，效果会更好！

比如在表major中major_id是有索引的：

select * from student u left join major m on u.major_id=m.major_id where m.major_id is null;

而通过嵌套查询时，在内存中创建临时表完成SELECT子查询与主查询两部分查询工作，会有一定的消耗：

select * from student u where major_id not in (select major_id from major);

 

##### 使用SQL提示

SQL提示（SQL HINT）是优化数据库的一个重要手段，就是往SQL语句中加入一些人为的提示来达到优化目的。下面是一些常用的SQL提示：

###### USE INDEX

1、USE INDEX：使用USE INDEX是希望MySQL去参考索引列表，就可以让MySQL不需要考虑其他可用索引，其实也就是possible_keys属性下参考的索引值

mysql> explain select* from user_info use index(id_index,ind_name_id) where user_id>0;

+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+

| id | select_type | table   | partitions | type | possible_keys     | key  | key_len | ref  | rows | filtered | Extra    |

+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+

|  1 | SIMPLE    | user_info | NULL    | ALL  | ind_name_id,id_index | NULL | NULL   | NULL |   4 |    100 | Using where |

+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+

1 row in set

 

mysql> explain select* from user_info use index(id_index) where user_id>0;

+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+

| id | select_type | table   | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra    |

+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+

|  1 | SIMPLE    | user_info | NULL    | ALL  | id_index    | NULL | NULL   | NULL |   4 |    100 | Using where |

+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+

1 row in set

###### IGNORE INDEX

2、IGNORE INDEX忽略索引

我们使用user_id判断，用不到其他索引时，可以忽略索引。即与USE INDEX相反，从possible_keys中减去不需要的索引，但是实际环境中很少使用。

mysql> explain select* from user_info ignore index(primary,ind_name_id,id_index) where user_id>0;

+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+

| id | select_type | table   | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra    |

+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+

|  1 | SIMPLE    | user_info | NULL    | ALL  | NULL      | NULL | NULL   | NULL |   4 |   33.33 | Using where |

+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+

1 row in set

###### FORCE INDEX

3、FORCE INDEX强制索引

比如where user_id > 0，但是user_id在表中都是大于0的，自然就会进行ALL全表搜索，但是使用FORCE INDEX虽然执行效率不是最高（where user_id > 0条件决定的）但MySQL还是使用索引。

mysql> explain select* from user_info where user_id>0;

+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+

| id | select_type | table   | partitions | type | possible_keys     | key  | key_len | ref  | rows | filtered | Extra    |

+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+

|  1 | SIMPLE    | user_info | NULL    | ALL  | ind_name_id,id_index | NULL | NULL   | NULL |   4 |    100 | Using where |

+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+

1 row in set

之后强制使用独立索引id_index(user_id)：

mysql> explain select* from user_info force index(id_index) where user_id>0;

+----+-------------+-----------+------------+-------+---------------+----------+---------+------+------+----------+-----------------------+

| id | select_type | table   | partitions | type  | possible_keys | key    | key_len | ref  | rows | filtered | Extra         |

+----+-------------+-----------+------------+-------+---------------+----------+---------+------+------+----------+-----------------------+

|  1 | SIMPLE    | user_info | NULL    | range | id_index    | id_index | 4    | NULL |   4 |    100 | Using index condition |

+----+-------------+-----------+------------+-------+---------------+----------+---------+------+------+----------+-----------------------+

1 row in set

 

#### UPDATE/DELETE/INSERT语句优化

将大的DELETE，UPDATE or INSERT 查询变成多个小查询。为了达到更好的性能以及更好的数据控制，你可以将他们变成多个小查询。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE23.tmp.jpg) 

如果插入数据过多，考虑批量插入。

反例：

for(User u :list){

 INSERT into user(name,age)values(#name#,#age#)

}

正例：

//一次500批量插入，分批进行

insert into user(name,age) values

<foreach collection="list" item="item" index="index" separator=",">   

(#{item.name},#{item.age})

</foreach>

理由：

批量插入性能好，更加省时间

 

#### 使用索引

##### 如何选择合适的列建立索引？

**1、** ***\*在where从句，group\**** ***\*by从句，order\**** ***\*by\*******\*从句，on从句中出现的列\****；

2、 索引字段越小越好；

注：数据库中数据存储是以页为单位，如果在一页中存储的数据越多（索引字段越小则一页能存储的数据就越多），那么一次IO操作获取的数据量就越大，这样IO效率更高一些。

**3、** ***\*离散度大的列放到联合索引的前面\****；

SELECT * FROM payment WHERE staff_id=2 AND customer_id=584;

是index(staff_id,customer_id)好还是index(customer_id,staff_id)好？

首先，判断字段的离散程度：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE24.tmp.jpg) 

​	判断离散程度可以对列进行count统计操作，唯一值越多则离散程度越好。

​	由于customer_id的离散度更大，所以应该使用index(customer_id,staff_id)

##### 索引优化方法

使用索引的三大注意事项：

1、LIKE关键字匹配'%'开头的字符串,不会使用索引.

2、OR关键字的两个字段必须都是用了索引,该查询才会使用索引.

3、***\*使用多列索引必须满足最左匹配.\****

###### 重复及冗余索引

*重复索引*

​	重复索引是指相同的列以相同的顺序建立的同类型的索引，如下表中primary_key和ID列上的索引都是重复索引：

​	CREATE TABLE test(

​		id int not null primary key,

​		name varchar(10) not null,

​		title varchar(50) not null,

​		unique(id)

​	)engine=innodb;

​	注：主键已经是唯一索引，所以这里id的唯一索引是多余的。

***\*冗余索引\****

​	冗余索引是指多个索引的前缀列相同，或是在联合索引中包含了主键的索引，下面的这个列子中key(name,id)就是一个冗余索引。

​	CREATE TABLE test(

​		id int not null primary key,

​		name varchar(10) not null,

​		title varchar(50) not null,

​		key(name,id)

​	)engine=innodb;

###### 查找重复及冗余索引

​	在information_schema库中执行SQL会显示重复的索引：

​	SELECT a.TABLE_SCHEMA AS ‘数据名’

​		a.table_name AS ‘表名’

​		,a.index_name AS ‘索引1’

​		,b.INDEX_NAME AS ‘索引2’

​		,a.COLUMN_NAME AS ‘重复列名’

​	FROM STATISTICS a JOIN STATISTICS b ON

​	a.TABLE_SCHEMA=b.TABLE_SCHEMA  AND 

a.TABLE_NAME=b.table_name

AND a.SEQ_IN_INDEX=b.SEQ_IN_INDEX AND

a.COLUMN_NAME=b.COLUMN_NAME WHERE a.SEQ_IN_INDEX=1 AND

a.INDEX_NAME<>b.INDEX_NAME

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE25.tmp.jpg) 

还可以使用pt-duplicate-key-checker工具检查重复及冗余索引：

pt-duplicate-key-checker	\

-uroot	\

-p ‘’		\

-h 127.0.0.1	

 

##### 索引维护方法

​	**删除不用索引**

​	目前MySQL中还没有记录索引的使用方法，但是在PerconMySQL和MariaDB中可以通过INDEX_STATISTICS表来查看哪些索引未使用，但在MySQL中目前只能通过慢查询日志配合pt-index-usage工具来进行索引使用情况的分析：

​	pt-index-usage	、

​	-uroot –p’’	\

​	mysql-show.log

##### 注意事项

索引是提高数据库查询速度最重要的方法之一，使用索引的三大注意事项:

***\*1、LIKE关键字匹配'%'开头的字符串,不会使用索引.\****

***\*2、OR关键字的两个字段必须都是用了索引,该查询才会使用索引.\****

***\*3、使用多列索引必须满足最左匹配.\****

#### 选择合适的存储引擎

***\*参考条件：\****

1、事务

2、备份( Innobd免费在线备份)

3、崩溃恢复

4、存储引擎的特有特性

***\*总结:\**** Innodb好。

***\*注意:\**** 尽量别使用混合存储引擎，比如回滚会出问题在线热备问题。

 

#### 选择合适的锁机制

MySQL的锁有以下几种形式：

❑ 表级锁：开销小，加锁快；不会出现死锁；锁定粒度大，发生锁冲突的概率最高，并发度最低。MyISAM引擎属于这种类型。

❑ 行级锁：开销大，加锁慢；会出现死锁；锁定粒度最小，发生锁冲突的概率最低，并发度也最高。InnoDB引擎属于这种类型。

❑ 页面锁：开销和加锁时间界于表锁和行锁之间；会出现死锁；锁定粒度界于表锁和行锁之间，并发度一般。NDB属于这种类型。

##### MyISAM锁

MyISAM存储引擎只支持表锁，所以对MyISAM表进行操作，会存在以下情况：

❑ 对MyISAM表的读操作（加读锁），不会阻塞其他进程对同一表的读请求，但会阻塞对同一表的写请求。只有当读锁释放后，才会执行其他进程的写操作。

❑ 对MyISAM表的写操作（加写锁），会阻塞其他进程对同一表的读和写操作，只有当写锁释放后，才会执行其他进程的读写操作。

##### InnoDB锁

InnoDB存储引擎是通过给索引上的索引项加锁来实现的，这就意味着：只有通过索引条件检索数据，InnoDB才会使用行级锁，否则，InnoDB将使用表锁。

##### 性能对比

InnoDB和MyISAM作为MySQL数据库中两种最主要、最常用的存储引擎，各有所长。在MySQL5.5之前的版本中，MyISAM是MySQL中默认的存储引擎，而在MySQL5.5之后，MySQL中默认的存储引擎则改为了InnoDB。对于这两种存储引擎的选择，要根据项目应用特点来权衡，而对于复杂的应用系统，也可以根据实际情况来选择多种存储引擎的组合。不过，还是建议尽量不要混合使用多种存储引擎，这样容易带来更复杂的问题。

MyISAM支持全文索引，这是一种基于分词创建的索引，支持一些比较复杂的查询，但不是事务安全的，而且不支持外键。每张MyISAM表存放在3个文件中：frm文件存放表格定义；数据文件是MYD（MYData）；索引文件是MYI（MYIndex）。对于MyISAM表，可以手工或者自动执行检查或修复操作，这一点要注意跟InnoDB的事物恢复区分开来。

InnoDB是事务型引擎，支持回滚，具有崩溃恢复能力，多版本并发控制（MVCC）、支持ACID事务、支持行级锁定（InnoDB表的行锁不是绝对的，如果执行一个SQL语句没有使用到索引，InnoDB表同样会锁全表）。

InnoDB的工作原理：就是把数据捞到内存中，被用户读写，这样大大增加了性能，因为从内存进行读写比磁盘要快得多。当数据全部加载到内存中，这时的性能是最好的。它的设计理论是充分利用内存，减少磁盘I/O使用率，每次版本升级时，改善最多的就是这些方面。它有两种表空间管理模式：一种是共享表空间，一种是独立表空间。共享表空间的数据文件是ibdata1...ibdataN，这个数据文件里保存着元数据、数据、索引、插入合并缓冲、undo log回滚日志，ib_logfile0...3里保存着redolog重做事务日志。独立表空间把数据、索引、插入合并缓冲从ibdata1拆分了出去，保存在．ibd这种格式的文件里，因为在MySQL5.5里，一些新的特性都是基于独立表空间的。在MySQL5.6里，undo log回滚日志也可以从共享表空间拆分出去，保存在另一块硬盘上，这样做的目的是，充分利用多块磁盘的I/O，比如，可以把undo log放入SSD磁盘上。

MyISAM和InnoDB之间的主要区别有以下几点：

❑ MyISAM是非事务安全型的，而InnoDB是事务安全型的，也就是ACID事务支持；

❑ MyISAM锁是表级锁，锁开销最小，而InnoDB支持行级锁定，锁管理开销大，支持更好的并发写操作；

❑ MyISAM支持全文索引，而InnoDB不支持全文索引，但在最新的5.6版本中已提供支持；

❑ MyISAM相对简单，管理方便，因此在效率上要优于InnoDB，小型应用可以考虑使用MyISAM；

❑ MyISAM表是保存成文件的形式，在跨平台的数据转移中使用MyISAM存储会省去不少的麻烦；

❑ InnoDB表比MyISAM表更安全，可以在保证数据不会丢失的情况下，切换非事务表到事务表。

MyISAM存储引擎的读锁和写锁是互斥的，读写操作是串行的。试想一下，一个进程请求某个MyISAM表的读锁，同时另一个进程也请求同一表的写锁，MySQL该如何处理呢？答案是写进程先获得锁。不仅如此，即使读请求先到锁等待队列，写请求后到，写锁也会插到读锁请求之前！这是因为MySQL认为写请求一般要比读请求重要。这也正是MyISAM表不太适合有大量更新操作和查询操作应用的原因，因为大量的更新操作会造成查询操作很难获得读锁，从而可能永远阻塞。

InnoDB用于事务处理应用程序，具有众多特性，包括支持ACID事务、行锁等。如果应用中需要执行大量的读写操作，则应该使用InnoDB，这样可以提高多用户并发操作的性能。对于MyISAM引擎，在MySQL5.5版本里Oracle公司支持的已经很少了，以后内存数据库是一种趋势，所以建议优先选择InnoDB引擎。

​	注：参考《MySQL管理之道：性能调优、高可用与监控》

#### 选择合适的事务隔离级别

像其他数据库一样，MySQL在进行事务处理的时候使用的是日志先行的方式来保证事务可快速和持久运行的，也就是在写数据前，需要先写日志。当开始一个事务时，会记录该事务的一个LSN日志序列号；当执行事务时，会往InnoDB_Log_Buffer日志缓冲区里插入事务日志（redo log）；当事务提交时，会将日志缓冲区里的事务日志刷入磁盘。这个动作是由innodb_flush_log_at_trx_commit这个参数控制的。

##### redo log刷新

innodb_flush_log_at_trx_commit=0，表示每个事务提交时，每隔一秒，把事务日志缓存区的数据写到日志文件中，以及把日志文件的数据刷新到磁盘上；它的性能是最好的，同样安全性也是最差的。当系统宕机时，会丢失1秒钟的数据。

innodb_flush_log_at_trx_commit=1，表示每个事务提交时，把事务日志从缓存区写到日志文件中，并且刷新日志文件的数据到磁盘上。

innodb_flush_log_at_trx_commit=2，表示每个事务提交时，把事务日志数据从缓存区写到日志文件中；每隔一秒，刷新一次日志文件，但不一定刷新到磁盘上，而是取决于操作系统的调度。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE36.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE37.tmp.jpg) 

使用命令“show innodb status\G; ”可以看到当前刷新事务日志的情况：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE38.tmp.jpg) 

除了记录事务日志以外，数据库还会记录一定量的撤销日志（undo log）, undo与redo正好相反，在对数据进行修改时，由于某种原因失败了，或者人为执行了rollback回滚语句，就可以利用这些撤销日志将数据回滚到修改之前的样子，就如前面举的那个ATM取款机取钱的例子。redo日志保存在ib_logfile0/1/2里，而undo日志保存在ibdata1里，在MySQL5.6里还可以把undo日志单拆分出去。

在默认情况下，事务都是自动提交的。如果想在程序里自己控制事务，那么开始一个事务前，要写明BEGIN或START TRANSACTION，执行完以后再COMMIT提交。

##### 隔离级别

在数据库操作中，为了有效保证并发读取数据的正确性，提出了事务隔离级别的概念。

InnoDB有四种隔离级别：Read Uncommitted、Read Committed、Repeatable Read、Serializable。

ORACLE/SQL SERVER的默认隔离级别是Read Committed（读提交），而MySQL的默认隔离级别是Repeatable Read（可重复读）。

 

###### 问题

数据库是要被广大客户所共享访问的，那么在数据库操作过程中很可能出现以下几种不确定情况。

❑ 更新丢失（Lost update）：两个事务都同时更新一行数据，但是第二个事务却中途失败退出，导致对数据的两个修改都失效了。这是因为系统没有执行任何的锁操作，因此并发事务并没有被隔离开来。

❑ 脏读（Dirty Reads）：一个事务开始读取了某行数据，另外一个事务已经更新了此数据但没有能够及时提交。这是相当危险的，因为很可能所有的操作都被回滚。

❑ 不可重复读（Non-repeatable Reads）：一个事务对同一行数据重复读取两次，但是却得到了不同的结果。例如，在两次读取的中途，有另外一个事务对该行数据进行了修改，并提交。

❑ 两次更新问题（Second lost updates problem）：无法重复读取的特例。有两个并发事务同时读取同一行数据，然后其中一个对它进行修改提交，而另一个也进行了修改提交。这就会造成第一次写操作失效。

❑ 幻读（Phantom Reads）：事务在操作过程中进行两次查询，第二次查询的结果包含了第一次查询中未出现的数据（这里并不要求两次查询的SQL语句相同）。这是因为在两次查询过程中有另外一个事务插入数据。

###### 选择隔离级别

为了避免出现上面几种情况，在标准SQL规范中，定义了4个事务隔离级别，不同的隔离级别对事务的处理不同。如下所示：

❑ 未授权读取，也称为读未提交（Read Uncommitted）：允许脏读取，但不允许更新丢失。如果一个事务已经开始写数据，则另外一个数据则不允许同时进行写操作，但允许其他事务读此行数据。该隔离级别可以通过“排他写锁”实现。

❑ 授权读取，也称为读提交（Read Committed）：允许不可重复读取，但不允许脏读取。这可以通过“瞬间共享读锁”和“排他写锁”实现。读取数据的事务允许其他事务继续访问该行数据，但是未提交的写事务将会禁止其他事务访问该行。

❑ 可重复读取（Repeatable Read）：禁止不可重复读取和脏读取，但是有时可能出现幻影数据。这可以通过“共享读锁”和“排他写锁”实现。读取数据的事务将会禁止写事务（但允许读事务），写事务则禁止任何其他事务。

❑ 序列化（Serializable）：提供严格的事务隔离。它要求事务序列化执行，事务只能一个接着一个地执行，不能并发执行。如果仅仅通过“行级锁”是无法实现事务序列化的，必须通过其他机制保证新插入的数据不会被刚执行查询操作的事务访问到。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE39.tmp.jpg) 

隔离级别越高，越能保证数据的完整性和一致性，但是对并发性能的影响也越大。对于多数应用程序来说，可以优先考虑把数据库系统的隔离级别设为Read Committed，它能够避免脏读取，而且具有较好的并发性能。尽管它会导致不可重复读、虚读和第二类丢失更新这些并发问题，一般来说，还是可以接受的，因为读到的是已经提交的数据，本身并不会带来很大的问题。

说明：虽然串行化隔离性最好，但是其效率较低，不一定适合业务场景，需要根据实际的情况选择不同隔离级别。如果某种业务，比如非核心业务跑批，对实时性要求不那么高，允许短时间的脏读，可以采用UR提高效率，但是如果是核心/个贷等业务，对于实时性和一致性要求很高，则还是需要设置读已提交或可重复读。

#### 数据库结构优化

##### 选择合适的数据类型

​	数据类型的选择，重点在于**合适**，如何确定选择的数据类型是否合适？

1、 使用可以存下你的数据的最小的数据类型，使用ENUM而不是VARCHAR；

ENUM类型是非常快和紧凑的。在实际上，其保存的是TINYINT，但其外表上显示为字符串。这样一来，用这个字段来做一些选项列表变得相当的完美。如果我们有一个字段，比如“性别”，“国家”，“民族”，“状态”或“部门”，我们知道这些字段的取值是有限而且固定的，那么，我们应该使用ENUM而不是VARCHAR。

2、 使用简单的数据类型，int要比varchar类型在MySQL处理上简单；

3、 尽可能的使用not null定义字段；

4、 尽量少用text类型，非用不可时最好考虑分表。

 

使用int来存储日期时间，利用FROM_UNIXTIME()，UNIX_TIMESTAMP()两个函数来进行转换：

CREATE TABLE test(id INT AUTO_INCREMENT NOT NULL

,timestr INT,PRIMARY KEY(id));

​	INSERT INTO test(timestr) VALUES(UNIX_TIMESTAMP(‘’));

​	SELECT  FROM_UNIXTIME(timestr) FROM test;

 

​	使用bigint来存储IP地址，利用INET_ATON()，INET_NTOA()两个函数来进行转换：

CREATE TABLE sessions(id INT AUTO_INCREMENT NOT NULL

,ipaddress BIGINT,PRIMARY KEY(id));

​	INSERT INTO sessions(ipaddress) VALUES(INET_ATON (‘’));

​	SELECT INET_NTOA(apaddress) FROM sessions;

 

##### 范式化优化

​	范式化是指数据库设计的规范，***\*目前说到范式化一般是指第三设计范式\****，也就是要求数据表中不存在非关键字段。对任意候选关键字段的传递函数依赖则符合第三范式。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE49.tmp.jpg) 

​	存在以下传递函数依赖关系：

​	（商品名称）à（分类）à（分类描述）

​	也就是说存在非关键字段“分类描述”对关键字段“商品名称”的传递函数依赖。

不符合第三范式要求的表存在下列问题：

1、 数据冗余：（分类，分类描述）对于每一个商品都会进行记录

2、 数据的插入异常

3、 数据的更新异常

4、 数据的删除异常

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE4A.tmp.jpg) 

##### 反范式化优化

​	***\*反范式化是指为了查询效率的考虑把原来符合第三范式的表适当的增加冗余，以达到优化查询效率的目的\****。反范式化是一种以空间换取时间的操作。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE4B.tmp.jpg) 

##### 垂直拆分

​	所谓的垂直拆分，就是把原来一个有很多列的表拆分为多个表，这解决了表的宽度问题。通常垂直拆分可以按以下原则进行：

1、 把不常用的字段单独存放到一个表中；

2、 把大字段独立存放到一个表中；

3、 把经常一起使用的字段存放到一起。

操作：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE4C.tmp.jpg) 

该表垂直拆分为下面的两张表：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE5D.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE5E.tmp.jpg) 

##### 水平拆分

​	常用的水平拆分方法为：

1、 对customer_id进行hash运算，如果要拆分成5个表则使用mod(customer_id,5)取出0~4个值；

2、 针对不同的hashID把数据存放到不同的表中。

挑战：

1、 跨分区表进行数据查询；

2、 统计及后台报表操作。

表的水平拆分是为了解决单标的数据量多大的问题，水平拆分的表每一个表的结构都是完全一致的。以下面的payment表为例：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE5F.tmp.jpg) 

#### 分解表

对于字段较多的表，如果某些字段使用频率较低，此时应当，将其分离出来从而形成新的表。

***\*大表的特点\*******\*：\****

1、记录行数巨大，***\*单表超千万\****

2、表数据文件巨大，***\*超过10个G\****

***\*大表的危害\*******\*：\****

1、慢查询：很难在短时间内过滤出需要的数据查询字区分度低->要在大数据量的表中筛选出来其中一部分数据会产生大量的磁盘io ->降低磁盘效率

2、对DDL影响：

建立索引需要很长时间：

MySQL-v<5.5建立索引会锁表

MySQL-v>=5.5建立索引会造成主从延迟（mysql建立索引，先在组上执行，再在库上执行）

修改表结构需要长时间的锁表：会造成长时间的主从延迟('480秒延迟')

***\*如何处理数据库上的大表\*******\*？\****

分库分表把一张大表分成多个小表

***\*难点：\****

1、分表主键的选择

2、分表后跨分区数据的查询和统计

说明：在建行某项目中，就是采用将某一个大表拆分为128张表的形式来实现大表的处理。

 

#### 中间表

对于将***\*大量连接查询的表可以创建中间表\****，从而减少在查询时造成的连接耗时。

#### 增加冗余字段

类似于创建中间表，增加冗余也是为了减少连接查询。

#### 分析表/检查表/优化表

分析表主要是分析表中关键字的分布，检查表主要是检查表中是否存在错误，优化表主要是消除删除或更新造成的表空间浪费。

##### 分析表

1、分析表: 使用ANALYZE关键字,如ANALYZE TABLE user;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE60.tmp.jpg) 

Op:表示执行的操作.

Msg_type:信息类型,有status,info,note,warning,error.

Msg_text:显示信息.

##### 检查表

2、检查表: 使用CHECK关键字，如CHECK TABLE user [option]

option 只对MyISAM有效,共五个参数值:

QUICK:不扫描行,不检查错误的连接.

FAST:只检查没有正确关闭的表.

CHANGED:只检查上次检查后被更改的表和没被正确关闭的表.

MEDIUM:扫描行,以验证被删除的连接是有效的,也可计算各行关键字校验和.

EXTENDED:最全面的的检查,对每行关键字全面查找.

##### 优化表

3、优化表:使用OPTIMIZE关键字,如OPTIMIZE [LOCAL|NO_WRITE_TO_BINLOG] TABLE user;

LOCAL|NO_WRITE_TO_BINLOG都是表示不写入日志，优化表只对VARCHAR,BLOB和TEXT有效，通过OPTIMIZE TABLE语句可以消除文件碎片，在执行过程中会加上只读锁。

### **硬优化**

定位问题点顺序：硬件>系统>应用>数据库>架构（高可用、读写分离、分库分表）。

处理方向：明确优化目标、性能和安全的折中、防患未然。

#### 硬件配置

优化MySQL所在服务器内核(此优化一般由运维人员完成)。

**硬件三件套：**

1、配置多核心和频率高的cpu，多核心可以执行多个线程。

2、配置大内存，提高内存，即可提高缓存区容量,因此能减少磁盘I/O时间，从而提高响应速度。

3、配置高速磁盘或合理分布磁盘：高速磁盘提高I/O，分布磁盘能提高并行操作的能力。

 

**常用的指令：**

CPU方面：vmstat、sar top、htop、nmon、mpstat。

内存：free、ps-aux。

IO设备（磁盘、网络）：iostat、ss、netstat、iptraf、iftop、lsof。

**vmstat命令说明：**

Procs：r显示有多少进程正在等待CPU时间。b 显示处于不可中断的休眠的进程数量。在等待I/O。

Memory：swpd 显示被交换到磁盘的数据块的数量。未被使用的数据块，用户缓冲数据块，用于操作系统的数据块的数量。

Swap：操作系统每秒从磁盘上交换到内存和从内存交换到磁盘的数据块的数量。s1 和 s0 最好是0。

IO：每秒从设备中读入b1的写入到设备b0的数据块的数量。反映了磁盘I/O。

System：显示了每秒发生中断的数量（in）和上下文交换（cs）的数量。

CPU：显示用于运行用户代码，系统代码，空闲，等待 I/O 的 CPU 时间。

**iostat命令说明：**

实例命令：iostat -dk 1 5；iostat -d -k -x 5 （查看设备使用率（%util）和响应时间（await））。

TPS：该设备每秒的传输次数。“一次传输”意思是“一次 I/O 请求”。多个逻辑请求可能会被合并为“一次 I/O 请求”。

iops ：硬件出厂的时候，厂家定义的一个每秒最大的 IO 次数。

"一次传输"请求的大小是未知的。

KB_read/s：每秒从设备（drive expressed）读取的数据量。

KB_wrtn/s：每秒向设备（drive expressed）写入的数据量。

KB_read：读取的总数据量。

KB_wrtn：写入的总数量数据量；这些单位都为 Kilobytes。

 

##### 主机方面

​	根据数据库类型，主机CPU选择、内存容量选择、磁盘选择

​	平衡内存和磁盘资源

​	随机的I/O和顺序的I/O

​	主机 RAID卡的BBU(Battery Backup Unit)关闭

 

##### 选择CPU

现实世界中，CPU的技术发展得很快，一颗CPU上往往集成了4/6/8个核，由于多核很少会全部利用到，所以一般会在生产机器上部署多实例，以充分利用CPU资源。还可以更进一步，使用CPU绑定技术将进程或线程绑定到一个CPU或一组CPU上，这样做可以提升CPU缓存的效率，提升CPU访问内存的性能。对于NUMA架构的系统，也可以提高内存访问的局部性，从而也能提高性能。

CPU利用率衡量的是在某个时间段，CPU忙于执行操作的时间的百分比，但是，许多人不知道的是，CPU利用率高并不一定是在执行操作，而很可能是在等待内存I/O。CPU执行指令，需要多个步骤，这其中，内存访问是最慢的，可能需要几十个时钟周期来读写内存。所以CPU缓存技术和内存总线技术是非常重要的。

我们对CPU时钟频率这个主要的指标可能有一些误解。如果CPU利用率高，那么更快的CPU不一定能够提升性能。也就是说，如果CPU的大部分时间是在等待锁、等待内存访问，那么使用更快的CPU不一定能够提高吞吐。

**关于容量规划。**

对于访问模式比较固定的应用，比如一些传统制造业的生产系统，则比较容易对CPU进行容量规划，可以按照未来的访问请求或访问客户端数量，确定CPU需要扩容的幅度，你可以监控当前系统的CPU利用率，估算每个客户端/每个访问请求的CPU消耗，进而估算CPU 100%利用率时的吞吐，安排扩容计划。由于互联网业务，负荷往往变化比较大，多实例有时会导致CPU的容量模型更为复杂，我们更多地依靠监控的手段提前进行预警，在CPU到达一定利用率，负载到达一定阈值时，进行优化或扩容。

**如何选购CPU。**

对于企业用户来说，CPU的性能并不是最重要的，最重要的是性价比，新上市的CPU往往价格偏贵，一般来说建议选择上市已经有一定时间的CPU。而对于大规模采购，你需要衡量不同CPU的价格及测试验证实际业务的吞吐，进而能够得出一个预算成本比较合适的方案，可能你还需要综合平衡各种其他硬件的成本以确定选购的CPU型号。

###### 优化

系统的性能一般取决于系统所有组件中最弱的短板，CPU、内存、I/O、网络都可能会成为瓶颈所在。现实中，一般是CPU瓶颈或I/O瓶颈，I/O瓶颈也可能是由于内存不够所导致的。

**避免大量运算**

CPU的瓶颈一般是大量运算和内存读取所导致的，比如加密操作、索引范围查找、全表扫描等。生产环境中出现CPU瓶颈往往是因为大量的索引范围查找或连接了太多表。I/O瓶颈往往是因为内存已经不能保存住数据库的热数据，因此读写操作必须访问实际的物理磁盘，从而导致过多的物理读。

实际生产环境中，更多的会碰到I/O瓶颈，而不是CPU瓶颈，你可以使用top或mpstat判断数据库服务器是否存在CPU瓶颈。

**绑核**

由于MySQL在多CPU主机上的扩展性有限，不能充分利用多CPU的主机，所以生产中可能会在同一个主机上部署多个实例。有时我们会绑定MySQL实例到某个CPU节点上。

如果想要优化性能，那么我们更倾向于选取速度更快的CPU，而不是增加CPU。从理论上来说，**如果操作比较集中于一些资源对象，瓶颈多是因为锁和队列等待，那么这个时候应该选取更强劲的CPU**。而如果操作分散于诸多不相干的资源上，那么并发程度可以更高，可以倾向于使用更多的CPU，但能否使用更多的CPU、并发多线程执行操作，还要受制于存储引擎的设计。就目前来说，InnoDB的扩展性还是不佳。

 

绑定到1/10核：

numactl -C 1,10 /home/bin/mysqld --default-file=/*/my.cnf

绑定到1~10核：

numactl -C 1-10 /home/bin/mysqld --default-file=/*/my.cnf

 

**安装irqbalance**

irqbalance用于对CPU中断进行负载均衡。

注：麒麟操作系统默认是不启动irqbalance进程，需要手动启动。

 

**中断亲和性**

自动平衡：可通过irqbalance服务实现。

手动平衡：

1、确定需要平衡中断的设备，从CentOS7.5开始，系统会自动为某些设备及其驱动程序配置最佳的中断关联性。不能再手动配置其亲和性。目前已知的有使用be2iscsi驱动的设备，以及NVMe设置；

2、对于其他设备，可查询其芯片手册，是否支持分发中断，若不支持，则该设备的所有中断会路由到同一个CPU上，无法对其进行修改。若支持，则计算smp_affinity掩码并设置对应的配置文件。

 

**开启NUMA**

麒麟操作系统默认是启动NUMA的，测试发现启动NUMA的性能要远远好于不开NUMA。

修改文件/boot/grub/grub.cfg,numa=on/off，修改需要重启机器。

注意：一般情况下都是关闭的。

**电源管理**

下面我们来看看CPU的高级特性。

PC Server上有一种节能模式，一般是处于关闭的状态，这种电源管理技术可以在负载低的时候，调低CPU的时钟速度，降低能耗，但这种技术并不能和突发的负荷协作得很好，有时会来不及调整时钟以响应突然的高并发流量。

还有另外一种电源管理技术，它通过分析当前CPU的负载情况，智能地完全关闭一些用不上的核心，而把能源留给正在使用的核心，并使它们的运行频率更高，从而进一步提升性能。相反，需要多个核心时，应动态开启相应的核心，智能调整频率。这样，就可以在不影响CPU的TDP（热功耗设计）的情况下，把核心工作频率调得更高。这种加速技术可能会破坏我们的性能规划，因为系统的行为并不是“线性”的了。

 

**动态节能技术**

cpufreq是一个动态调整CPU频率的模块，可支持五种模式。为保证服务性能应选用performance模式，将CPU频率固定工作在其支持的最高运行频率上，不进行动态调节，操作指令为cpupower frequency-set --governor performance。

 

###### 总结

尽量选择频率高的处理器，关闭节能模式。

 

CPU的两个关键因素：核数、主频

根据不同的业务类型进行选择：

cpu密集型：***\*计算\****比较多，OLTP，主频很高的cpu、核数还要多

IO密集型：***\*查询\****比较，OLAP，核数要多，主频不一定高的

 

在服务器的BIOS设置中，调整如下配置：

\1) 选择Performance Per Watt Optimized（DAPC）模式，发挥CPU最大性能

\2) 关闭C1E和C States等选项，提升CPU效率

\3) Memory Frequency（内存频率）选择Maximum Performance

**4)** ***\*内存设置菜单中，启用Node Interleaving，避免NUMA问题\****

 

​	思考：是选择单核更快的CPU还是选择核数更多的CPU？

1、 MySQL有一些工作只能使用到单核CPU

复制Replicate…..

2、 MySQL对CPU核数的支持并不是越多越快

**MySQL5.5使用的服务器不要超过32核**

 

**CPU负载高，IO负载低：**

1、内存不够

2、磁盘性能差

3、SQL问题：去数据库层，进一步排查SQL问题

4、IO出问题了（磁盘到临界了、raid设计不好、raid降级、锁、在单位时间内TPS过高）

5、TPS过高：大量的小数据IO、大量的全表扫描

**IO负载高，CPU负载低：**

1、大量小的IO写操作

2、autocommit，产生大量小IO；IO/PS，磁盘的一个定值，硬件出厂的时候，厂家定义的一个每秒最大的IO次数

3、大量大的IO写操作：SQL问题的几率比较大

**IO和CPU负载都很高：**

硬件不够了或SQL存在问题

##### 内存方面

我们需要了解CPU、内存、固态硬盘及普通机械硬盘访问速度的差异，比如内存为几十纳秒（ns），而固态硬盘大概是25μs（25000ns），而机械硬盘大概是6毫秒（6000000ns），它们差得不是一两个数量级，机械硬盘对比内存差了五六个数量级，所以内存访问比磁盘访问要快得多，所以总会有许多人想尽办法优化数据的访问，尽量在内存当中来访问数据。

内存往往是影响性能最重要的因素，你应该确保热点数据存储在内存中，较少的内存往往意味着更多的I/O压力。许多应用一般是有热点数据的，且热点数据并不大，可以保存在内存中。对于MySQL来说，应将innodb_buffer_pool_size设置得大于我们的热点数据，否则可能会出现某个MySQL实例InnoDB的缓冲不够大，从而产生过多的物理读，进而导致I/O瓶颈。

数据库服务器应该只部署数据库服务，以免被其他程序影响，有时其他程序也会导致内存压力，如占据大量文件的系统缓存，就会导致可用内存不够。

###### 优化

**如何避免使用swap**

这里我们仅仅讨论Linux系统下的swap（交换）。其他系统，如Solaris，会有一些区别。

简单地说，swap指的是将最近不常使用的内存移动到下一级存储里（硬盘），在需要的时候，再载入到主内存中。

swap空间一般是指我们磁盘上的预先配置的一个分区，也可以是文件，用于将内存中的数据交换到磁盘上。物理内存和swap空间之和就是我们可用的虚拟内存的大小。当我们的内存不够了或应用程序消耗了太多的内存，操作系统会把不需要立即使用的数据传输到磁盘，以释放内存空间，如果以后需要了，再从磁盘上复制回内存，这样一个过程也称为交换（swap out/swap in）。通过这样一个交换的动作，增加了实际可用的内存，可以提高系统的吞吐能力，但是数据的交换如果太频繁，就会大大增加磁盘的延时时间，可能会导致严重的性能问题。一般来说，数据库负载，需要尽量避免使用到swap。我们可以使用free、vmstat、sar等命令查看swap使用的统计信息。

**通过free命令，如果我们看到了一小部分swap空间被使用，那么这一般是正常的，不需要额外关注，我们需要关注的是是否有正在进行的swap in/swap out操作。**

一些人建议将swap分区设置为物理内存的大小，对于Linux系统来说，这个建议有一定的意义，为了不浪费过多的硬盘空间，建议使用如下的策略。

□ 如果MEM<2GB，那么SWAP=MEM×2，否则SWAP=MEM+2GB。

□ 对于内存非常大的系统，如32GB、64GB，我们可以使用0.5×内存大小。

MySQL避免使用swap的一些方法如下。

**设置memlock**

可在参数文件中设置memlock，将MySQL InnoDB buffer锁定到内存，永不使用swap，但这是有风险的。如果内存不够大，MySQL会被操作系统的OOM机制杀掉。如果因为物理内存故障导致内存总量变少，那么它可能还会导致系统无法顺利启动，因为MySQL会不断申请内存。

**使用大内存页**

可以设置MySQL使用Linux系统的大内存页（操作系统和MySQL都需要设置）。***\*Linux系统的大内存页是不会被交换出去的\****。

**设置vm.swappiness**

可以设置vm.swappiness=0，以减少使用swap的可能。

swappiness参数，它可以在运行时进行调优。这个参数决定了，将应用程序移动到交换空间而不是移动到正在减少的高速缓存和缓冲区中的可能性，降低swappiness可以提高交互式应用程序的响应能力，但是会降低系统的总体吞吐量。

**禁用或调整NUMA**

在生产环境中你可能会碰到在没有内存压力的情况下，也发生swap in/swap out的情况，导致不定时出现的性能问题。尤其是在使用了大的buffer pool size的情况下，这一般是因为使用了NUMA技术，需要考虑禁用NUMA或更改程序分配内存的方式，numactl命令可以实现这个目的，使用方式为：numactl--interleave all command，例如，/usr/bin/numactl--interleave=all mysqld。

注：这是因为在使用大内存的时候，本地节点不能满足，需要跨节点获取内存，这是非常耗时的操作。

**禁用swap分区**

命令：swapoff -a

注意：不要去禁用swap，并不是所有内核在swap分区被禁用的情况下都能工作得很好，这可能会导致服务异常，某个服务在禁用swap的时候能够工作得很好，并不代表所有程序都能很好地工作。而且内存不够的概率更高了，当使用了过多的内存时，程序更容易被操作系统的OOM机制杀掉。我们需要意识到，swap分区为我们处理问题留了一个缓冲，给我们争取到了处理问题的时间，所以我们不要把swap分区设置得过小，相对于你所获得的收益，“浪费”一些磁盘空间是值得的。

说明：在实际测试麒麟操作系统的时候，采用禁用swap分区，有一定效果。一般在内存足够大的情况下，可以关闭swap分区，因为基本不会用到了。

 

***\*NUMA\****

**概述**

NUMA（Non-Uniform Memory Access，非一致性内存访问）NUMA服务器的基本特征是Linux将系统的硬件资源划分为多个节点（Node），每个节点上有单独的CPU、内存和I/O槽口等。CPU访问自身Node内存的速度将远远高于访问远地内存（系统内其它节点的内存）的速度，这也是非一致内存访问NUMA的由来。

关于 NUMA 的误区：

numactl命令未找到，numa就是未开启吗？

不是，numactl是Linux提供的一个对NUMA进行手工调优的命令（默认不安装），可以用numactl命令查看系统的NUMA状态和对NUMA进行控制。

 

**架构**

从系统架构来说，目前的主流企业服务器可以分为3类：SMP（Symmetric Multi Processing，对称多处理架构）、NUMA（Non-Uniform Memory Access，非一致存储访问架构）和MPP（Massive Parallel Processing，海量并行处理架构）。下面我们来看下SMP和NUMA架构。

1、SMP

如图18-2所示的是一个SMP系统。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE70.tmp.jpg) 

在这样的系统中，所有的CPU共享全部资源，如总线、内存和I/O系统等，多CPU之间没有区别，均可平等地访问内存和外部资源。因为CPU共享相同的物理内存，每个CPU访问内存中的任何地址所需要的时间也是相同的，因此SMP也被称为一致存储器访问结构（Uniform Memory Access，UMA），尤其是在和NUMA架构对比的时候。***\*对于SMP服务器而言，每一个共享的环节都可能是瓶颈所在。\****由于所有处理器都共享系统总线，所以当处理器的数目增多时，系统总线的竞争冲突也会加大，系统总线成为了性能瓶颈，所以其扩展性有限，这种架构已经被逐步淘汰，但在CPU内部还有应用，单个CPU的所有核共享访问该CPU的本地内存。

如图18-3所示的是NUMA系统。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE71.tmp.jpg) 

在这种架构中，每颗CPU有自己独立的本地内存，CPU节点之间通过互联模块进行连接，***\*访问本地内存的开销很小，延时比访问远端内存（系统内其他节点的内存）小得多\****。这也是非一致存储访问NUMA的由来。

综上所述可以得知，NUMA对内存访问密集型的业务更有好处，NUMA系统提升了内存访问的局部性，从而提高了性能。

**命令**

关于CPU信息，我们可以查看/proc/cpuinfo。

对于NUMA的访问统计，我们可以使用numastat命令进行检查，也可以查看/sys/devices/system/node/node*/numastat文件。

**输出含义**

如图所示，NUMA使用了default策略，这将导致内存分配的不均衡，numastat命令的输出如下。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE72.tmp.jpg) 

各项输出的含义如下。

□ numa_hit：在此节点分配内存而且成功的次数。

□ numa_miss：由于内存不够，在此节点分配内存失败转而在其他节点分配内存的次数。

□ numa_foreign：预期在另一个节点分配内存，但最终在此节点分配的次数。

□ interleave_hit：交错分布策略分配内存成功的次数。

□ local_node：一个运行在某个节点的进程，在同一个节点分配内存的次数。

□ other_node：运行在其他节点的进程，在此节点分配内存的次数。

 

**内存分配策略**

在Linux上NUMA API支持4种内存分配策略，具体如下。

□ 缺省（default）：总是在本地节点分配（分配在当前线程运行的节点上）。

□ 绑定（bind）：分配到指定节点上。

□ 交织（interleave）：在所有节点或指定的节点上交织分配。

□ 优先（preferred）：在指定节点上分配，失败后在其他节点上分配。

绑定和优先的区别是，在指定节点上分配失败时（如无足够内存），绑定策略会报告分配失败，而优先策略会尝试在其他节点上进行分配。强制使用绑定有可能会导致前期的内存短缺，并引起大量换页。

我们可以检查程序具体的内存分配信息，假设pid是mysqld的进程ID，通过查看/proc/pid/numa_maps这个文件，我们可以看到所有mysqld所做的分配操作。各字段的显示如下。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE83.tmp.jpg) 

各字段及其解析如下。

□ 2aaaaad3e000：内存区域的虚拟地址。实际上可以把这个当作该片内存的唯一ID。

□ default：这块内存所用的NUMA策略。

□ anon=number：映射的匿名页面的数量。

□ dirty=number：由于被修改而被认为是脏页的数量。

□ swapcache=number：被交换出去，但是由于被交换出去，所以没有被修改的页面的数量。这些页面可以在需要的时候被释放，但是此刻它们仍然在内存中。

□ active=number：“激活列表”中的页面的数量。

□ N0=number and N1=number：节点0和节点1上各自分配的页面的数量。

我们可以使用numactl命令显示可用的节点。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE84.tmp.jpg) 

如上命令告诉我们，系统有两个CPU节点：node0、node1。每个节点分配了64GB的内存。

distance衡量了访问内存的成本，系统认为访问本地节点内存的成本是10，访问远端内存的成本是20。

NUMA架构存在的一个问题是，对于NUMA架构，Linux默认的内存分配方案是优先在请求线程当前所处的CPU的本地内存上尝试分配空间，一般是node0。如果内存不够，系统就会把node0上已经分配的内存交换出去，以释放部分node0的内存，尽管node1上还有剩余的内存，但是系统不会选择向node1去申请内存。显然，***\*swap的成本远比访问远端内存的成本高，这将导致不定时地出现性能问题\****。

 

MySQL是单进程多线程架构数据库，当NUMA采用默认内存分配策略时，MySQL进程会被并且仅仅被分配到NUMA的一个节点上去。

假设这个节点的本地内存为1GB，而MySQL配置2GB内存，超出节点本地内存部分（2GB-1GB），Linux会使用swap而不是使用其他节点的物理内存。在这种情况下，能观察到虽然系统总的可用内存还未用完，但是MySQL进程已经开始使用swap了。

**关闭**

解决办法具体如下：

1）关闭NUMA。

如果是单机单实例，则建议关闭NUMA，关闭的方法有如下两种。

□ 硬件层，在BIOS中设置关闭。

□ OS内核，启动时设置numa=off。

可用类似如下的方式进行修改。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE85.tmp.jpg) 

确认NUMA是否关闭，检查numactl--show的输出信息。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE86.tmp.jpg) 

关闭之前这个命令会显示多个节点的信息，输出结果如下所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE97.tmp.jpg) 

而关闭之后则只会显示一个节点的信息，nodebind项只有一个值0。我们也可以检查启动信息dmesg|grep-i numa。

2）使用numactl命令将内存分配策略修改为interleave（交叉）或绑定CPU。

可通过修改单实例启动脚本mysql.server或多实例启动脚本mysqld_multi，例如，修改msyqld_multi脚本（MySQL 5.1）320行将$com="$mysqld"更改为$com="/usr/bin/numactl--interleave all$mysqld";

也可以修改启动脚本和参数，绑定MySQL的各个实例到固定的CPU节点，笔者更推荐使用这种方式。下面的例子，在节点0的CPU上运行名为program的程序，并且只在节点0和1上分配内存。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE98.tmp.jpg) 

下面的例子，在节点1上运行$MYSQLD程序，只在节点内分配内存。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEE99.tmp.jpg) 

3）设置参数memlock。

MySQL进行初始化启动的时候，就已经预先把InnoDB缓冲池的内存锁住了，即设置参数memlock等于1，设置这个参数，也有一定的风险，如果内存不够，可能会导致系统启动不正常，因为MySQLServer会不断申请内存。

4）使用大内存页。

还有一些其他的辅助手段。

配置vm.zone_reclaim_mode=0使得内存不足时倾向于向其他节点申请内存。echo-15>/proc/<pid_of_mysqld>/oom_adj，将MySQL进程被OOM_killer强制kill的可能性调低。

 

**性能**

NUMA的内存分配策略对于进程来说，并不是乐观的。***\*因为NUMA默认是使用CPU亲和的内存分配策略，即请求进程会从当前所处的CPU的Node请求分配内存\****。当某个需要消耗大量内存的进程耗尽了所处的Node的内存时，就会导致产生swap，不会从远程Node分配内存，这就是***\*swap insanity现象\****。

MySQL数据库是单进程多线程的架构，在开启的NUMA 服务器中，内存被分配到各NUMA Node上，而MySQL进程只能消耗所在节点的内存。所以在开启NUMA的服务器上，某些特殊场景中容易出现系统拥有空闲内存但发生 SWAP导致性能问题的情况。

比如专用的MySQL单实例服务器，物理内存为40GB，MySQL进程所在节点的本地内存为20G，而MySQL配置30GB内存，超出节点本地内存部分会被SWAP到磁盘上，而不是使用其他节点的物理内存，引发性能问题。

 

**参数**

MySQL在5.6.27、5.7.9引入了innodb_numa_interleave参数，MySQL自身解决了内存分类策略的问题，需要服务器支持numa。

根据官方文档的描述：

当启用innodb_numa_interleave时，mysqld进程的NUMA内存策略被设置为MPOL_INTERLEAVE；InnoDB缓冲池分配完毕后，NUMA内存策略又被设置为MPOL_DEFAULT。当然innodb_numa_interleave参数生效，MySQL必须是在启用NUMA的Linux系统上编译安装。从 MySQL 5.7.17 开始，CMake 编译软件新增了 WITH_NUMA 参数，可以在支持 NUMA 的 Linux 系统上编译 MySQL。

需要注意 innodb_numa_interleave 参数在 MySQL5.7.17 的二进制包中是不支持的。

经过测试：

1、系统若不支持 numa，-DWITH_NUMA=ON 会导致 CMake 编译失败；

2、MySQL5.7.19+ 的免编译的二进制包开始支持 innodb_numa_interleave 参数。

若是专用的 MySQL 服务器，可以直接在 BIOS 层或者 OS 内核层关闭 NUMA；

若希望其他进程使用 NUMA 特性，可以选择合适的 MySQL 版本开启 innodb_numa_interleave 参数。

 

***\*开启巨页\****

X86（包括X86-32和X86-64）架构的CPU默认使用4KB大小的内存页面，但是它们也支持较大的内存页，如X86-64系统就支持2MB大小的大页（huge page）。Linux2.6及以上的内核都支持huge page。

如果在系统中使用了huge page，则内存页的数量会减少，从而需要更少的页表（page table），节约了页表所占用的内存数量，并且所需的地址转换也减少了，TLB缓存失效的次数就减少了，从而提高了内存访问的性能。

另外，由于抵制转换所需的信息一般都保存在CPU的缓存中，huge page的使用让抵制转换信息减少，从而减少了CPU缓存的使用，减轻了CPU缓存的压力，让CPU缓存能够更多地用于应用程序的数据缓存，也能够在整体上提升系统的性能。

在实际应用中，为了使用大页面，还需要将应用程序与库libhugetlb连接在一起。libhugetlb库对malloc/free等常用的内存相关的库函数进行了重载，以使得应用程序的数据可以放置在采用大页面的内存区域中，以提高内存性能。

 

查看当前页大小：getconf PAGESIZE

查看巨页使用情况：

cat /proc/meminfo | grep -i huge

打开巨页：

echo 1000 > /proc/sys/vm/nr_hugepages，分配1000个大页

 

***\*共享内存\****

查看共享内存使用情况：

systemctl -a | grep kernel | grep shm

参数说明：

shmmax：配置了最大的内存segment的大小，是的那个段允许使用的大小

shmmin：最小的内存segment的大小

shmmni：整个系统的内存segment的总个数

shmall：是全部允许使用的共享内存大小

shmall：是全部允许使用的共享内存大小，shmmax是单个段允许使用的大小。这两个可以设置为内存的90%。

修改共享内存参数：

echo xx > /proc/sys/kenel/shmmax

echo xx > /proc/sys/kernel/shmall

 

***\*页缓存清除\****

当DB所在的系统因页缓存占据过大，操作系统会在某个阶段释放page cache来提高free memory，此时会导致系统瞬间撑高、系统SYS瞬间增大，影响DB的业务进行。

推荐定期清除页缓存，避免因操作系统释放页缓存影响业务。

echo 1 > /proc/sys/vm/drop_caches

 

###### 总结

OLAP类型数据库，需要更多内存，和数据获取量级有关。

使用电池供电的RAM。

OLTP类型数据一般内存是cpu核心数量的2倍到4倍，没有最佳实践。

 

##### 存储方面

根据存储数据种类的不同，选择不同的存储设备（尽量使用SSD）

配置合理的RAID级别(raid5、raid10、热备盘)

 

  对与操作系统来讲，不需要太特殊的选择，最好做好冗余（raid1）（ssd、sas 、sata）

  raid卡：主机raid卡选择：

  实现操作系统磁盘的冗余（raid1）

　　平衡内存和磁盘资源

　　随机的I/O和顺序的I/O

主机 RAID卡的BBU(Battery Backup Unit)要关闭。

 

###### 磁盘选择

***\*传统磁盘\****

传统磁盘本质上是一种机械装置，影响磁盘的关键因素是磁盘服务时间，即磁盘完成一个I/O请求所花费的时间，它由寻道时间、旋转延迟和数据传输时间三部分构成。

一般读取磁盘的时候，步骤如下。

1）寻道：磁头移动到数据所在的磁道。

2）旋转延迟：盘片旋转将请求数据所在的扇区移至读写磁头下方。

3）传输数据。

一般随机读写取决于前两个步骤，而大数据顺序读写更多地取决于第3）个步骤，由于固态硬盘消除了前两个步骤，所以在随机读写上会比传统机械硬盘的IOPS高得多。

优化传统磁盘随机读写，也就是优化寻道时间和旋转延迟时间，一些可供考虑的措施有缓存、分离负载到不同的磁盘、硬件优化减少延时及减少震荡等。比如，操作系统和数据库使用的是不同的盘，我们需要了解读写比率，如果大部分是读的负载，那么我们加缓存会更有效；而如果大部分是写的负载，那么我们增加磁盘提高吞吐会更有意义。对于Web访问，由于本身就可能有几百毫秒的延时，那么100毫秒的磁盘延时也许并不是问题；而对于数据库应用，对于延时则要求很苛刻，那么我们需要优化或使用延时更小的磁盘。

对于数据库类应用，传统磁盘一般做了RAID，那么RAID卡自身也可能会成为整个系统的瓶颈，也需要考虑优化。

 

***\*关于RAID\****

几种常用的RAID类型如下。

□ RAID0：将两个以上的磁盘串联起来，成为一个大容量的磁盘。在存放数据时，数据被分散地存储在这些磁盘中，因为读写时都可以并行处理，所以在所有的级别中，RAID 0的速度是最快的。但是RAID 0既没有冗余功能，也不具备容错能力，如果一个磁盘（物理）损坏，那么所有的数据都会丢失。

□ RAID1：RAID 1就是镜像，其原理为在主硬盘上存放数据的同时也在镜像硬盘上写一样的数据。当主硬盘（物理）损坏时，镜像硬盘则代替主硬盘工作。因为有镜像硬盘做数据备份，所以RAID 1的数据安全性在所有的RAID级别上来说是最好的。理论上读取速度等于硬盘数量的倍数，写入速度有微小的降低。

□ Raid10：指的是RAID1+0，RAID1提供了数据镜像功能，保证数据安全，RAID0把数据分布到各个磁盘，提高了性能。

□ RAID5：是一种性能、安全和成本兼顾的存储解决方案。RAID 5至少需要3块硬盘，RAID 5不是对存储的数据进行备份，而是把数据和相对应的奇偶校验信息存储到组成RAID5的各个磁盘上。当RAID5的一个磁盘数据被损坏后，可以利用剩下的数据和相应的奇偶校验信息去恢复被损坏的数据。

几种RAID的区别如下。

1）RAID10理论上可以提供比RAID5更好的读写性能因为它不需要进行奇偶性校验。RAID 5具有和RAID 0相近似的数据读取速度，只是因为多了一个奇偶校验信息，写入数据的速度相对单独写入一块硬盘的速度略慢。

2）RAID10提供了更高的安全性。RAID5只能坏一块盘，RAID10视情况而定，最多可以坏一半数量的硬盘。

3）RAID5成本更低，也就是说空间利用率更高。RAID 5可以理解为是RAID 0和RAID 1的折中方案。RAID 5可以为系统提供数据安全保障，但保障程度要比镜像低而磁盘空间利用率要比镜像高，存储成本相对较便宜。

以上的区别是一些理论上的说明，实际情况可能还会因为算法、缓存的设计而不同。

我们是使用多个RAID，还是使用一个大RAID，将取决于我们是否有足够多的磁盘。如果我们有许多盘，比如超过10多块盘，那么我们使用多个阵列，是可取的；而如果你只有几块盘，比如6块盘，那么单独使用两块盘来做一个RAID1用于存放操作系统，就不太可取了。

***\*RAID卡有两种写入策略：Write Through和Write Back。\****

□ Write Through：将数据同步写入缓存（若有Cache的情况）和后端的物理磁盘。

□ Write Back：将数据写入缓存，然后再批量刷新到后端的物理磁盘。

一般情况下，对于带电池模块的RAID卡，我们将写入策略设置为Write Back。写缓存可以大大提高I/O性能，但由于掉电会丢失数据，所以需要用带电池的RAID卡。

如果电池模块异常，那么为了数据安全，会自动将写入策略切换为Write Through，由于无法利用缓存写操作，因此写入性能会大大降低。一般的RAID卡电池模块仅仅保证在服务器掉电的情况下，Cache中的数据不会丢失，在电池模块电量耗尽前需要启动服务器让缓存中的数据写盘。

如果我们碰到I/O瓶颈，我们需要更强劲的存储。普通的PC服务器加传统磁盘RAID（一般是RAID1+0）加带电池的RAID卡，是一种常见的方案。

在RAID的设置中，我们需要关闭预读，磁盘的缓存也需要被关闭。同样的，你需要关闭或减少操作系统的预读。

 

***\*关于SSD\****

SSD也称为固态硬盘，目前SSD设备主要分为两类，基于PCI-E的SSD和普通SATA接口的SSD，PCE-E SSD卡性能高得多，可以达到几十万IOPS，容量可以达到几个TB以上，常用的品牌有Fusion-io，而普通的SSD虽然才几千IOPS，但性价比更好，常用的品牌有Intel等。PCI-E相对来说稳定性、可靠性都更好，由于I/O资源的极大富余，可以大大节省机架。普通SSD，基于容量和安全的考虑，许多公司仍然使用了RAID技术，随着SSD容量的增大和可靠性的提升，RAID技术不再显得重要甚至不再被使用。

由于许多公司的SSD的I/O资源往往运行不饱和，因此SSD的稳定、性能一致、安全、寿命显得更重要，而性能可能不是最需要考虑的因素。依据笔者的使用经验，许多SSD的设备故障，其原因并不在于SSD设备本身，而在于SSD设备和传统电器组件之间的连接出现了问题，主机搭载传统机械硬盘的技术已经非常成熟，而在主机上搭载SSD，仍然需要时间来提高其可靠性。所以我们在选购主机的时候，SSD在其上运行的可靠性也是一个要考虑的因素。我们对于磁盘RAID也应该加以监控，防止因为磁盘RAID异常而导致数据文件损毁。

传统的机械硬盘，瓶颈往往在于I/O，而在使用了固态硬盘之后，整个系统的瓶颈开始转向CPU，甚至在极端情况下，还会出现网络瓶颈。由于固态硬盘的性能比较优越，DBA不再像以前那样需要经常进行I/O优化，可以把更多的时间和精力放在业务逻辑的设计上，固态硬盘的成本降低了，也可以节省内存的使用，热点数据不一定需要常驻内存，即使有时需要从磁盘上访问，也能够满足响应的需求了。传统的I/O隔离和虚拟化难度较高，很重要的原因是I/O资源本身就比较紧缺，本身就紧缺的资源，难以进行分割和共享，而高性能的PCI-E SSD卡使得虚拟化更可能落地。

传统的文件系统已经针对传统的机械磁盘阵列有许多优化，所以想在其上再做一些软件层的优化和算法设计，很可能会费力不讨好，但是如果是SSD设备，则另当别论，用好了SSD设备，可能可以大大减少SSD设备的故障率，充分利用它的潜能，随着固态硬盘大规模的使用，未来将需要在文件系统和数据库引擎上都做出相应的优化，以减少使用SSD的成本。

 

更***\*多\****的磁盘空间等于更快的速度。

较小的硬盘比较大的硬盘快，尤其是在RAID配置的情况下。

更好更快的磁盘。

使用SAS（注：Serial Attached SCSI，即串行连接SCSI）代替SATA（注：SATA，即串口硬盘）。

 

###### Disk IO/RAID优化

磁盘I/O相关

\1) 使用SSD或PCle SSD设备，至少获得数百倍甚至万倍的IOPS提升

\2) 购置阵列卡同时配备CACHE及BBU模块，可以明显提升IOPS

\3) 尽可能选用RAID-10，而非RAID-5

 

​	常用RAID级别：

​	RAID0：也称为***\*条带\****，就是把多个磁盘链接成一个硬盘使用，这个级别IO最好

​	RAID1：也称为***\*镜像\****，要求至少有两个磁盘，每组磁盘存储的数据相同

​	RAID5：也是把多个（最少3个）硬盘合并成1个逻辑盘使用，数据读写时会建立奇偶校验信息，并且就校验信息和相对应的数据分别存储于不同的磁盘上。当RAID5的一个磁盘数据发生损坏后，利用剩下的数据和相应的奇偶校验信息去恢复被损坏的数据。

​	RAID1+0：就是RAID1和RAID0的结合，同时具备两个级别的优缺点，一般建议数据库使用这个级别。

***\*总结：\****

避免RAID5，确保数据库完整性的校验是要付出代价的。

使用高级的RAID，最好是RAID10或更高。

使用电池支持的高速缓存RAID控制器。避免使用软件磁盘阵列。

###### Swap调整

***\*MySQL尽量避免使用swap。\****

/proc/sys/vm/swappiness的内容改成0（临时）

/etc/sysctl.conf上添加vm.swappiness=0（永久）

​	这个参数决定了Linux是倾向于使用swap，还是倾向于释放文件系统cache。在内存紧张的情况下，数值越低越倾向于释放文件系统cache。

当然，这个参数只能减少使用swap的概率，并不能避免Linux使用swap。

修改MySQL的配置参数innodb_flush_method，开启O_DIRECT模式。

这种情况下，InnoDB的buffer pool会直接绕过文件系统cache来访问磁盘，但是redo log依旧会使用文件系统cache。

值得注意的是，Redo log是覆写模式的，即使使用了文件系统的cache，也不会占用太多。

 

###### IO调度策略

\#echo deadline>/sys/block/sda/queue/scheduler  临时修改为deadline

永久修改

vi /boot/grub/grub.conf

更改到如下内容:

kernel /boot/vmlinuz-2.6.18-8.el5 ro root=LABEL=/ elevator=deadline rhgb quiet

###### SAN/NAT

​	思考：SAN和NAT是否适合数据库？

1、 常用于高可用解决方案；

2、 顺序读写效率高，但是随即读写不如人意；

3、 数据库随机读写比率很高。

##### 网络方面

​	使用流量支持更高的网络设备（交换机、路由器、网线、网卡、HBA卡）

选用万兆网卡。

注意：以上这些规划应该在初始设计系统时就应该考虑好。

##### 总结

1、配置多核心和频率高的cpu，多核心可以执行多个线程.

2、配置大内存，提高内存，即可提高缓存区容量，因此能减少磁盘I/O时间，从而提高响应速度。

3、配置高速磁盘或合理分布磁盘：高速磁盘提高I/O，分布磁盘能提高并行操作的能力。

注：硬件优化成本高，效果不太好，稍微了解一下即可。

#### 服务器硬件优化

服务器硬件优化关键点：

1、物理状态灯；

2、自带管理设备：远程控制卡（FENCE设备：ipmi ilo idarc）、开关机、硬件监控；

3、第三方的监控软件、设备（snmp、agent）对物理设施进行监控；

4、存储设备：自带的监控平台。EMC2（HP 收购了）、 日立（HDS）、IBM 低端 OEM HDS、高端存储是自己技术，华为存储。

 

#### 操作系统配置

生效系统配置：

sysctl [-n] [-e] -p <filename> (default /etc/sysctl.conf)

--p 从指定的文件加载系统参数，如不指定即从/etc/sysctl.conf中加载

 

##### 高核绑定

绑定到1/10核

numactl -C 1,10 /home/mysql/bin/mysqld --defaults-file=/home/mysql/etc/my.cnf

绑定到1~10核

numactl -C 1-10 /home/mysql/bin/mysqld --defaults-file=/home/mysql/etc/my.cnf

 

##### 内存管理

###### 开启巨页

X86（包括X86-32和X86-64）架构的CPU默认使用4KB大小的内存页面，但是它们也支持较大的内存页，如X86-64系统就支持2MB大小的大页/巨页（huge page）。Linux2.6及以上的内核都支持巨页。

如果在系统中使用了huge page，则内存页的数量会减少，从而需要更少的页表（page table），节约了页表所占用的内存数量，并且所需的地址转换也减少了，TLB缓存失效的次数就减少了，从而提高了内存访问的性能。

另外，由于地址转换所需的信息一般保存在CPU的缓存中，huge page的使用让地址转换信息减少，从而减少了CPU缓存的使用，减轻了CPU缓存的压力，让CPU缓存能更多的用于应用程序的数据缓存，也能够在整体上提升系统的性能。

在实际应用中，为了使用大页面，还需要将应用程序与库libhugetlb链接在一起，libhugetlb库对malloc/free等常用的内存相关的库函数进行了重载，以使得应用程序的数据可以被放置在采用大页面的内存区域中，以提高内存性能。

 

查看当前页大小：getconf  PAGESIZE

查看巨页使用情况：cat /proc/meminfo | grep -i huge或cat /proc/meminfo | grep HugePage

打开巨页：echo 1000 > /proc/sys/vm/nr_hugepages，分配1000个大页

###### 共享内存

查看当前内存使用情况：sysctl -a | grep kernel | grep shm

输出：

kernel.shmmax = ****

kernel.shmall = ****

kernel.shmmni = ****

kernel.shm_rmid_forced = ****

shmmax：配置了最大的内存segment大小，是单个段允许使用的大小

shmmin：最小的内存segment的大小

shmmni：整个系统的内存segment的总个数

shmall：是全部允许使用的共享内存大小

shmall是全部允许使用的共享内存大小，shmmax是单个段允许使用的大小。这两个可以设置为内存的90%。

设置命令：

echo *** > /proc/sys/kernel/shmmax

echo *** > /proc/sys/kernel/shmall

 

###### 清除页缓存

当mysql所在的系统因为页缓存占据过大的时候，操作系统会在某个阶段释放page cache来提高free memory，此时会导致系统IO瞬间增高、系统sys瞬间增大，影响数据库的业务运行。

推荐定期清除页缓存，避免因操作系统释放页缓存影响业务。

 

##### SWAP

MySQL尽量避免使用swap。

 

Swap调整(不使用swap分区)

/proc/sys/vm/swappiness的内容改成0（临时）

/etc/sysctl.conf上添加vm.swappiness=0（永久）

这个参数决定了Linux是倾向于使用swap，还是倾向于释放文件系统cache。在内存紧张的情况下，数值越低越倾向于释放文件系统cache。

当然，这个参数只能减少使用swap的概率，并不能避免Linux使用swap。

 

##### IO

I/O往往是数据库应用最需要关注的资源。作为数据库管理人员，你需要做好磁盘I/O的监控，持续优化I/O的性能，以免I/O资源成为整个系统的瓶颈。

□ 逻辑I/O：可以理解为是应用发送给文件系统的I/O指令。

□ 物理I/O：可以理解为是文件系统发送给磁盘设备的I/O指令。

□ 磁盘IOPS：每秒的输入输出量（或读写次数），是衡量磁盘性能的主要指标之一。IOPS是指单位时间内系统能处理的I/O请求数量，一般以每秒处理的I/O请求数量为单位，I/O请求通常为读或写数据操作的请求。OLTP应用更看重IOPS。

□ 磁盘吞吐：指单位时间内可以成功传输的数据数量。OLAP应用更看重磁盘吞吐。

 

实践当中，我们要关注的磁盘I/O的基本指标有磁盘利用率、平均等待时间、平均服务时间等。如果磁盘利用率超过60%，则可能导致性能问题，磁盘利用率往往是大家容易忽视的一个指标，认为磁盘利用率没有达到100%，就可以接受，其实，磁盘利用率在超过60%的时候，就应该考虑进行优化了。对于磁盘利用率的监控，在生产中，往往也会犯一个错误，由于监控的粒度太大，比如10分钟、2分钟一次，因此会没有捕捉到磁盘高利用率的场景。

###### I/O调度算法

Linux有4种I/O调度算法：CFQ、Deadline、Anticipatory和NOOP，CFQ是默认的I/O调度算法。在完全随机的访问环境下，CFQ与Deadline、NOOP的性能差异很小，但是一旦有大的连续I/O，CFQ可能会造成小I/O的响应延时增加，数据库环境可以修改为Deadline算法，表现也将更稳定。

如下命令将实时修改I/O调度算法：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEA9.tmp.jpg) 

如果你需要永久生效，则可以把命令写入/etc/rc.local文件内，或者写入grub.conf文件中。

raid、no lvm、ext4或xfs、ssd、IO调度策略

修改MySQL的配置参数innodb_flush_method，开启O_DIRECT模式。

这种情况下，InnoDB的buffer pool会直接绕过文件系统cache来访问磁盘，但是redo log依旧会使用文件系统cache。

值得注意的是，Redo log是覆写模式的，即使使用了文件系统的cache，也不会占用太多。

***\*IO调度策略\****

\#echo deadline>/sys/block/sda/queue/scheduler  临时修改为deadline

永久修改

vi /boot/grub/grub.conf

更改到如下内容:

kernel /boot/vmlinuz-2.6.18-8.el5 ro root=LABEL=/ elevator=deadline rhgb quiet

 

###### 优化

我们的生产环境一般是OLTP应用，I/O瓶颈一般来自于随机读写，随机读的消除和写的缓解主要靠缓存，所以我们要确保MySQL的缓冲区能够缓存大部分的热点数据。当然，也没有必要缓存所有的热点数据，可以接受一定的缓存未命中（cache miss）。注意，传统的一个调优方法是基于命中率进行调优，更靠谱的方案是基于缓存未命中的情况进行调优，虽然有时命中率很高了，但只要缓存未命中次数达到一定的频率，你就会碰到I/O瓶颈。

数据库引擎比操作系统或RAID更了解数据，能够更高效地访问数据，文件系统和RAID层面的预读要关掉，因为它们帮不上什么忙，应该交给数据库以更智能地判断数据的读取。

内存的随机读写速度比硬盘的随机读写速度快了几个数量级，所以如果有I/O的性能问题，那么添加内存会是最简便的方案。数据库缓冲是调优的重点，我们需要确保数据库缓冲能够缓存大部分的热点数据，理论上来说，如果数据库缓冲已经不够了，那么文件系统或RAID缓冲也没有什么用，因为它们要小得多，且不了解数据，缓存应该考虑在更接近用户的地方进行优化，由于应用比数据库更了解数据，所以对于高并发的业务，客户端/应用程序的本地内存或缓存服务（如Memcached）会比MySQL更有效率，提供更好的扩展性。

顺序读写无论是在内存还是在磁盘中，都比随机读写更快。一般是不用考虑特殊的缓存策略。对于机械硬盘，由于磁盘的工作原理，顺序读写的速度比随机读写速度快得多，我们需要着重优化随机读写，尽量减少随机读，以提高吞吐。对于SSD，虽然顺序读写也很快，相对而言，随机读写并没有差太多，而且优化随机读写也不是那么迫切，但是还是有必要优化大量随机读写的SQL，因为随着访问量的上升，贡献大量随机读写的SQL，将会很快导致整个系统出现瓶颈。

随机读写往往来自质量不高的SQL，这些SQL往往是因为索引策略不佳或表连接过多，从应用层优化或进行索引优化，会更有效果，也更具可行性。

文件碎片也可能会导致更多的随机I/O，尽管数据库是顺序访问数据的，但是I/O却不是顺序的，MySQL自身并没有提供工具来检查数据文件是否碎片很多，我们也不建议频繁地进行表的重建和优化，但是在进行了大批量数据操作之后，比如大量删除数据之后，在不影响服务的前提下，优化一下表（OPTIMIZE TABLE）还是可取的。

对于OLAP应用，I/O调优和OLTP有些相似，也是要先考虑应用调优和SQL调优，尽量减少I/O操作，如果必须要执行大量的I/O操作，那么应该尽量将其转换为顺序读写。

***\*选择合适的I/O大小\****

一般来说，MySQL的块大小是操作系统块的整数块，你可以通过命令getconf PAGESIZE来检查操作系统的块大小，更大的I/O大小，意味着更大的吞吐，尤其是对于传统的机械硬盘，一次更大的I/O，意味着不需要进行多次I/O，可以减少寻道的时间。对于数据库，由于往往是一些随机记录的检索，因此并不需要一次性读取大量的记录，所以一次I/O不需要太大。许多人把默认的数据库的块大小调整为8KB，以获得更高的性能。

***\*日志缓冲如何刷新到磁盘\****

对于数据库的I/O性能调整，需要在性能和数据的安全性上求得平衡。如果生产环境有严重的I/O性能问题，那么它往往是由程序的不良设计造成的。一个应用级别的SQL调整，可能就能解决了问题。而从操作系统的I/O层面可能就会无解。

InnoDB使用了数据缓冲和事务日志，数据缓冲大小、日志大小、日志缓冲、InnoDB如何刷新数据和缓冲，都会对性能产生影响。

InnoDB的脏数据并不是马上写入数据缓冲（数据文件）的，而是会先写日志缓冲（日志文件），将脏数据暂时保留在数据缓冲区中，这是一种常见的数据库持久化的技术，这些日志记录了数据变更，可以用来做故障恢复。数据的读写一般是随机读写，而日志的写入，是顺序写入，日志写入的要效率高得多，通过延缓数据的持久化，可以将数据更高效率地写入到磁盘中。

InnoDB在缓冲区满的情况下会将日志缓冲区刷新到磁盘，一般不需要调整日志缓冲区的大小（innodb_log_buffer_size），除非有很多有BLOB字段的记录，innodb_log_buffer_size的大小默认是1MB，建议是1~8MB。

我们通过配置innodb_flush_log_at_trx来控制如何将日志缓冲刷新到磁盘，innodb_flush_log_at_trx的值可设置为0、1、2，默认为1。

设置为1的情况下，每个事务提交都要写入磁盘，这是最安全的做法，而设置为其他值时，可能会丢失事务。一般机械磁盘受磁盘旋转和寻道的限制，最多只能达到几百次IO/每秒，所以这个设置会严重降低事务并发，如果你数据库的安全性要求很高，那么设置innodb_flush_log_at_trx为1，这时你可能要把日志文件放在更好的磁盘设备上，如SSD设备或带电池的磁盘阵列上。

如果将innodb_flush_log_at_trx设置为2，那么每次事务提交时会将日志缓冲写到操作系统缓存中，但不实际刷新到磁盘中，每秒再刷新日志缓冲到磁盘中，这样做可以减轻I/O负荷，如果不存在极端的情况，理论上宕机最多只会丢失最近1秒的事务。

如果innodb_flush_log_at_trx设置为0，那么每秒都会将日志缓冲写到日志文件中，且将日志文件刷新到磁盘，但在事务提交的时候并不会将日志缓冲写到日志文件中，一般不建议将其设置为0，在设为0时，如果mysqld进程崩溃，那么停留在日志缓冲区的数据将被丢失，因此你会丢失事务。当为2时，进程虽然会崩溃，但每次事务提交，都写入了日志，只是暂时没有被刷新到磁盘，所以不会丢失事务，因为操作系统负责把这些数据写入文件，当然，如果宕机了，那么你的数据还是会被丢失的。

设置为1将会更安全，但每次事务提交时都会伴随磁盘I/O，受机械硬盘的寻道和旋转延迟限制，可能会成为系统瓶颈，在确认可以满足I/O性能的前提下，可将其设置为1。

建议在生产环境中将innodb_flush_log_at_trx设置为2。

***\*事务日志\****

如果日志文件里记录的相关数据并未写入数据文件，那么这个日志文件是不能被覆盖的。日志文件如果过小，那么可能会过多地检查点操作，增加I/O操作，而如果日志文件过大，则会增加实例崩溃的恢复时间。一般建议在生产系统中将其大小设置为256~512MB，以平衡恢复时间和性能。你也可以定量分析实际需要的事务日志大小，方法是衡量一段时间（如0.5~2个小时）内写入的日志记录（innodb_os_log_written）的大小，你所分配的多个日志的总计大小应能确保保留此段时间的日志。

事务日志一般没有必要和数据文件分离，除非你有许多（20+）盘。如果只有几个盘，却专门使用独立的盘来存放二进制日志、事务日志，则有些浪费。在有足够多的盘的情况下，磁盘I/O分离才有意义，不然成本就会太高了，且无法充分利用有限的资源。建议将日志文件和数据文件放在同一个盘/卷的另一个原因是日志文件和数据文件放在一起，可以做LVM快照。

***\*二进制日志\****

如果分离二进制日志和数据文件，可能会带来一点性能上的提升，但分离的主要目的不是性能，而是为了日志的安全。如果没有带电池的RAID卡，那么分离就是有必要的。如果有带电池的RAID卡，那么一般情况下就没有必要进行分离，即使有许多顺序日志写入，RAID卡也可以合并这些操作，最终只会看到不多的一些顺序I/O。

如果将二进制日志存放在独立的盘上，那么即使我们的数据文件损坏了，我们也可以利用备份和日志做时间点恢复。

***\*InnoDB如何打开和刷新数据、日志文件\****

InnoDB有几种方式和文件系统进行交互，默认是以fdatasync的方式读写文件的，生产环境中推荐设置为O_DIRECT。

以下将简单介绍这两种方式。

□ fdatasync：默认InnoDB使用fsync()刷新数据和日志。使用默认设置没有什么问题，但也许发挥不了你硬件的最高性能。

□ O_DIRECT：对于数据文件，MySQL Server也是调用fsync()刷新文件到磁盘的，但是不使用操作系统的缓存和预读机制，以避免双重缓冲，如果你有带电池的RAID卡，则可以配合这个选项一起使用。注意RAID卡需要开启写缓存，默认策略是Write Back。

***\*InnoDB共享表空间和独立表空间\****

InnoDB表空间不仅仅可以存储表和索引数据，还有UNDO（可以理解为数据前像）、insertbuffer、doublewrite buffer等其他内部数据结构。

目前有两种表空间的管理方式，共享表空间和独立表空间。默认的是共享表空间的管理方式，InnoDB表空间的管理比较简单，并没有Oracle那样丰富的特性。如果使用默认的共享表空间的话，数据和索引就是放在一起的，所有数据都存储在innodb_data_file_path参数设置的数据文件里。

我们可以通过innodb_data_file_path设置多个InnoDB数据文件，一般将最后一个文件设置为可自动扩展的，以减少数据文件的大小，你也可以将数据文件分离到不同的磁盘中。由于数据文件不能收缩，所以使用共享表空间存在的一个严重的问题是空间的释放。如果你增加了数据文件，那么你还需要重启数据库实例，这些都加大了管理开销。

当自动扩展的数据文件被填满之时，每次扩展默认为8MB，我们可以调整为更大的值，如32MB、64MB，这个选项可以在运行时作为全局系统变量而改变。因为每次分配小空间，代价都会比较大，所以预分配一个较大的文件是有道理的。

另一种方式是独立表空间，我们需要将innodb_file_per_table设置为1。

这个选项可以将每个InnoDB表和它的索引存储在它自己的文件中，由于每个表都有自己的表空间，所以又称为独立表空间。UNDO、各种数据字典等其他数据仍然存储在共享表空间内。你可以通过操作系统命令比较直观地看到数据大小，也方便删除表释放空间，所以许多有经验的DBA都设置MySQL实例为独立表空间，从而可以更方便地释放空间和减少文件系统的I/O争用。

InnoDB也支持在裸设备上存储，通过这种方式，你也许可以得到少许的性能提升，但由于管理难度比较大，因此很少有人使用这种方式管理数据库文件。

***\*UNDO暴涨的可能性\****

有时我们的共享表空间会暴涨，其实是由于UNDO空间发生了暴涨，UNDO空间暴涨的原因主要有如下两点。

□ 存在长时间未提交的事务，因为未提交的事务需要使用发布查询时刻的UNDO的数据，所以共享表空间内的这部分UNDO数据不能被清除，将会积累得越来越多。

□ 也许是负载太高，清理线程还来不及清除UNDO，这种情况下，性能将会急剧下降。

***\*关于doublewrite buffer\****

InnoDB可使用doublewrite buffer来确保数据安全，以避免块损坏。doublewrite buffer是表空间的一个特殊的区域，可顺序写入。当InnoDB从缓冲池刷新数据到磁盘时，它首先会写入doublewritebuffer，然后写入实际的数据文件。InnoDB检查每个页块的校验和，以判断是否坏块，如果写入doublewrite buffer的是坏块，那么显然还没有写入实际数据文件，那么就用实际数据文件的块来恢复doublewrite buffer。如果写入了doublewrite buffer，但是数据文件写的是坏块，那么就用doublewrite buffer的块来重写数据文件，这也是MySQL灾难恢复的一个基本步骤。如果操作系统本身支持写入安全，不会导致坏块，那么我们可以禁用这个特性。

***\*数据库文件分类\****

可以考虑把二进制日志文件、InnoDB数据文件的物理文件分布到不同的磁盘中，这样做主要考虑的是把顺序I/O和随机I/O进行分离。你也可以把顺序I/O放到机械硬盘上，把随机I/O放到SSD上，如果有带电池的RAID卡且开启了写缓存，那么顺序I/O的操作一般是很快的。具体如何放置文件，还需要综合考虑性能、成本和维护性等多个因素。笔者的做法是，如果没有性能问题，就把所有文件都放在一个盘上，这样维护起来将会更方便。

如下是按照顺序I/O和随机I/O对数据库文件做了下分类。

（1）随机I/O

□ 表数据文件（*.ibd）：启用了独立表空间（innodb_file_per_table=1）。

□ UNDO区域（ibdata）：UNDO里存储了数据前像，MySQL为了满足MVCC，需要读取存储在UNDO里的前像数据，这将导致随机读，如果你要运行一个需要很长时间的事务或一个时间很长的查询，那么可能会导致很多随机读，因为长事务或未提交的事务将有更多的可能性读取前像数据。

（2）顺序I/O

□ 事务日志（ib_logfile*）。

□ 二进制日志（binlog.xxxxxxx）。

□ doublewrite buffer(ibdata)。□ insert buffer(ibdata)。

□ 慢查询日志、错误日志、通用日志等。

***\*何时运行OPTIMIZE TABLE\****

有些人会建议定时运行一些OPTIMIZE TABLE之类的命令，以优化性能，这点与Oracle类似，也总会有些人建议你定时运行重建索引的操作。一般来说，除非在进行了大量会影响数据分布的操作之后，比如删除了大量的数据、导入数据等，一般情况下是不需要重整表的。定时地运行OPTIMIZE TABLE命令不现实，还可能会导致生产系统的不可用。

OPTIMIZED TABLE命令会优化InnoDB主键的物理组织，使之有序、紧凑，但是其他索引仍然会和以前一样未被优化。哪一个索引对性能更重要呢？也许从来没有基于主键的查询条件。其实，数据、索引的分布也是需要一个过程的，随着时间的演变，自然而然会达到一个平衡。强制优化之后，过一段时间，它又会回到原来的不好不坏的状态。

所以MySQL 5.1的官方文档中才会建议：如果您已经删除了表的一大部分，或者如果您已经对含有可变长度行的表（含有VARCHAR、BLOB或TEXT列的表）进行了很多更改，则应使用OPTIMIZETABLE。

***\*MySQL磁盘空间\****

磁盘空间如果出现瓶颈，往往是因为数据库规划失误，前期没有进行足够的调研，也有小部分原因是因为业务发展得太快了，数据呈现爆炸式增长。大部分业务，一般预留1到2年的数据增长空间就已经足够了，如果你预计数据未来会有一个海量的规模，那么提前进行分库分表则是有必要考虑的。

你需要尽可能地了解占据数据库总体空间比重较大的一些数据，清楚哪些表是可以被清理或归档的，许多情况下，我们并不需要这么多的数据，或者许多数据是不需要保留很久的，是完全可以清除的，你越了解数据，就越能够和研发团队一起制定合理的数据保留策略。

在系统上线之前，你就需要制订好将数据进行批量清理和归档的方案，可以使用定期任务删除数据，你也可以利用分区表删除旧的历史数据。

当数据库实例的数据变得很大，单台机器已经很难保存所有数据的时候，你可以考虑将实例、数据库分离到其他的机器。

由于处理器和高速缓存存储器速度的提升超过磁盘存储设备速度的提升，许多业务将受磁盘空间所累。一些业务拥有海量数据，但大部分都是冷数据，你又不能进行简单的归档处理，这个时候数据压缩就派上用场了。

目前的数据库主机，CPU资源往往过剩，数据压缩可以减少数据库的大小，减少I/O和提高吞吐量，而压缩仅仅只会消耗部分CPU成本。MySQL 5.5开始提供了InnoDB表压缩的功能，在MySQL 5.6中InnoDB表压缩的功能得到了进一步的完善，真正可以用于生产环境了。

对于真正海量高并发的应用，内存为王，你应该在内存中尽可能地保证热点数据和索引，更多的索引和数据可以放在一个内存块中，那么查询的响应也将更快，表是压缩的也意味着你需要更少的存储空间和更小、更少的I/O操作。对于MySQL 5.5、5.6，你需要配置为独立的表空间才能使用表的压缩功能，对于MySQL 5.7，你也可以不使用独立表空间。

由于固态硬盘一般比传统机械硬盘要小，且成本更高，所以压缩对固态硬盘尤其有意义。

不同的内容压缩率将会不一样，如果你需要将表修改为压缩表，那么你需要在更改之前进行测试验证，以确认压缩率和转换表的时间，一般来说，设置KEY_BLOCK_SIZE为8KB可以适用于大部分情况，8KB意味着将每个页压缩为8KB，你也可以将标准的16KB页压缩为4KB或2KB，但可能会导致过多的性能损耗而压缩率并不能得到提升。

***\*注意：\****笔者不推荐读者对生产环境的参数做大的调整，也不推荐使用各种不常用的手段去优化硬件资源的利用率，压榨硬件的性能。保持一个维护性更好的数据库，使用通用的参数，可以让工作变得更简单些，笔者认为这才是更重要的。但是，作为DBA，一定要熟悉各种调优的手段，因为你可能会碰到极端的场景。

###### 总结

按照IOPS性能提升的幅度排序，对于磁盘I/O可优化的一些措施：

1、使用SSD或者PCIe SSD设备，至少获得数百倍甚至万倍的IOPS提升；

2、购置阵列卡同时配备CACHE及BBU模块，可明显提升IOPS（主要是指机械盘，SSD或PCIe SSD除外。同时需要定期检查CACHE及BBU模块的健康状况，确保意外时不至于丢失数据）；

3、有阵列卡时，设置阵列写策略为WB，甚至FORCE WB（若有双电保护，或对数据安全性要求不是特别高的话），严禁使用WT策略。并且闭阵列预读策略，基本上是鸡肋，用处不大；

4、尽可能选用RAID-10，而非RAID-5；

5、使用机械盘的话，尽可能选择高转速的，例如选用15KRPM，而不是7.2KRPM的盘，不差几个钱的；

##### 文件系统

###### 概述

文件系统是一种向用户提供底层数据访问的机制。它将设备中的空间划分为特定大小的块（扇区），一般每块有512B。数据存储在这些块中，由文件系统软件来负责将这些块组织为文件和目录，并记录哪些块被分配给了哪个文件，以及哪些块没有被使用。以下仅针对文件系统和MySQL相关的部分做一些说明。

常用的文件系统有ext3、ext4、XFS等，你可以检查Linux系统的/etc/fstab文件，以确定当前分区使用的是什么文件系统，ext3即第三代扩展文件系统，是一个日志文件系统，低版本的Linux发行版将会使用到这种文件系统，它存在的一个问题是删除大文件时比较缓慢，这可能会导致严重的I/O问题。ext4即第四代扩展文件系统，是ext3文件系统的后继版本。2008年12月25日，Linux 2.6.29版公开发布之后，ext4成为了Linux官方建议的默认的文件系统。ext4改进了大文件的操作效率，使删除大的数据文件不再可能导致严重的I/O性能问题，一个100多GB的文件，也只需要几秒就可以被删除掉。

文件系统使用缓存来提升读性能，使用缓冲来提升写性能。在我们调整操作系统和数据库的时候，要注意批量写入数据的冲击，一些系统会缓冲写数据几十秒，然后合并刷新到磁盘中，这将表现为时不时的I/O冲击。

默认情况下，Linux会记录文件最近一次被读取的时间信息，我们可以在挂载文件系统的时候使用noatime来提升性能。为了保证数据的安全，Linux默认在进行数据提交的时候强制底层设备刷新缓存，对于能够在断电或发生其他主机故障时保护缓存中数据的设备，应该以nobarrier选项挂载XFS文件系统，也就是说，如果我们使用带电池的RAID卡，或者使用Flash卡，那么我们可以使用nobarrier选项挂载文件系统，因为带电池的RAID卡和FLASH卡本身就有数据保护的机制。还有其他的一些挂载参数也会对性能产生影响，你需要衡量调整参数所带来的益处是否值得，笔者一般不建议调整这些挂载参数。它们对性能的提升并不显著。

你可能需要留意文件系统的碎片化，碎片化意味着文件系统上的文件数据块存放得不那么连续，而是以碎片化的方式进行分布，那么顺序I/O将得不到好的性能，会变成多次随机I/O。

所以在某些情况下，使用大数据块和预先分配连续的空间是有道理的，但你也需要知道，文件碎片是一个常态，最开始的表没有什么碎片，但随着你更新和删除数据，数据表会变得碎片化，这会是一个长期的过程，而且在绝大多数情况下，你会感觉不到表的碎片对于性能的影响，因此，除非你能够证明表的碎片化已经严重影响了性能，否则不建议进行表的整理，比如运行OPTIMIZE TABLE命令。

Direct I/O允许应用程序在使用文件系统的同时绕过文件系统的缓存。你可以用Direct I/O执行文件备份，这样做可以避免缓存那些只被读取一次的数据。如果应用或数据库，已经实现了自己的缓存，那么使用Direct I/O可以避免双重缓存。

许多人期望使用mmap的方式来解决文件系统的性能问题，***\*mmap的方式有助于我们减少一些系统调用，但是，如果我们碰到的是磁盘I/O瓶颈，那么减少一些系统调用的开销，对于提升整体性能/吞吐的贡献将会很少\****。因为主要的瓶颈，主要花费的时间是在I/O上。许多NoSQL的数据库使用了mmap的方式来持久化数据，在I/O写入量大的时候，其性能急剧下降就是这个道理。

一般来说，文件系统缓存，对于MySQL的帮助不大，可以考虑减小文件系统缓存，如vm.dirty_ratio=5。

我们推荐在Linux下使用XFS文件系统，它是一种高性能的日志文件系统，特别擅长处理大文件，对比ext3、ext4，MySQL在XFS上一般会有更好的性能，更高的吞吐。Red Hat Enterprise Linux 7默认使用XFS文件系统。Red Hat Enterprise Linux 5、6的内核完整支持XFS，但未包含创建和使用XFS的命令行工具，你需要自行安装。

###### 总结

如果可以的话，使用noatime和nodirtime挂载文件系统，没有理由更新访问数据库文件的修改时间。

使用XFS文件系统（一种比ext3更快、更小的文件系统），并且有许多日志选项， 而且ext3已被证实与MySQL有双缓冲问题。

调整XFS文件系统日志和缓冲变量，为了最高性能标准。

 

在文件系统层，下面几个措施可明显提升IOPS性能：

1、使用deadline/noop这两种I/O调度器，千万别用CFQ（它不适合跑DB类服务）；

2、使用xfs文件系统，千万别用ext3；ext4勉强可用，但业务量很大的话，则一定要用xfs；

3、文件系统mount参数中增加：noatime, nodiratime, nobarrier几个选项（nobarrier是xfs文件系统特有的）。

 

##### *最多用户*

用户限制参数（MySQL可以不设置以下配置）：

vim/etc/security/limits.conf 

\* soft nproc 65535

\* hard nproc 65535

\* soft nofile 65535

\* hard nofile 65535

 

##### 网络

###### 概述

对于数据库应用来说，网络一般不会成为瓶颈（注：对于分布式数据库而言，网络很可能是一个普遍存在的性能瓶颈！），CPU和I/O更容易成为瓶颈。网络的瓶颈一般表现为流量超过物理极限，如果超过了单块网卡的物理极限，那么你可以考虑使用网卡绑定的技术增加网络带宽，同时也能提高可用性，如果是超过了运营商的限制，那么你需要快速定位流量大的业务，以减少流量，而请运营商调整带宽在短时间内可能难以完成。

网络瓶颈也可能因为网络包的处理而导致CPU瓶颈。交换机和路由器通过微处理器处理网络包，它们也可能会成为瓶颈，对于主机来说，如果对于网络包的处理没有一个CPU负载均衡策略，那么网卡流量只能被一个CPU处理，CPU也可能会成为瓶颈。

网络端口，也可能会成为瓶颈所在，不过这种情况很少见，即使是有大量短连接的场合。首先你需要优化连接，减少短连接，或者使用连接池，如果实在优化不下去了，可以考虑修改系统的内核参数net/ipv4/ip_local_port_range，调整随机端口范围，或者减少net/ipv4/tcp_fin_timeout的值，或者使用多个逻辑IP扩展端口的使用范围。

在进行网络优化之前，我们需要清楚自己的网络架构，了解你应用的网络数据流路径，比如是否经过了DNS服务器，你需要使用网络监控工具比如Cacti监控流量，在超过一定阈值或有丢包的情况下及时预警。

你需要意识到，跨IDC的网络完全不能和IDC内网的质量相比，且速度也可能会成为问题，跨IDC复制，其实本质上是为了安全，是为了在其他机房中有一份数据，而不是为了实时同步，也不能要求必须是实时同步。你需要确保应用程序能够处理网络异常，如果两个节点间距离3000英里，光速是186000英里/秒，那么单程需要16毫秒，来回就需要32毫秒，然后节点之间还有各种设备（路由器、交换机、中继器），它们都可能影响到网络质量。

所以，如果你不得不进行跨IDC的数据库同步，或者让应用程序远程访问数据库，那么你需要确保你的应用程序能够处理网络异常，你需要确认由于跨IDC网络异常导致的复制延时不致影响到业务。由于网络异常，Web服务器可能连接数暴涨、挂死、启动缓慢（由于需要初始化连接池），这些都是潜在的风险，你需要小心处理。

当有新的连接进来时，MySQL主线程需要花一些时间（尽管很少）来检查连接并启动一个新的线程，MySQL有一个参数back_log来指定在停止响应新请求前在短时间内可以堆起多少请求，你可以将其理解为一个新连接的请求队列，如果你需要在短时间内允许大量连接，则可以增加该数值。Linux操作系统也有类似的参数net.core.somaxconn、tcp_max_syn_backlog，你可能需要增大它们。

###### 总结

​	数据库时基于操作系统的，目前大多数MySQL都是安装在Linux系统之上，所以对于操作系统的一些参数配置也会影响到MySQL的性能，下面列举一些常用的系统配置：

​	网络的配置，要修改/etc/sysctl.conf文件：

​	#增加tcp支持的队列数

​	net.ipv4.tcp_max_syn_backlog = 65535

​	#减少断开连接时，资源回收

​	net.ipv4.tcp_max_tw_buckets = 8000

​	net.ipv4.tcp_ tw_reuse = 1 

​	net.ipv4.tcp_max_tw_recycle = 1

​	net.ipv4.tcp_fin_timeout = 10

​	注：上述参数是加速连接是time_wait的回收速度。

​	打开文件数的限制，可以使用ulimit –a查看目录的各位限制，可以修改/etc/security/limit.conf文件，增加以下内容修改打开文件数量的限制：

​	* soft nofile 65535

​	* hard nofile 65535

​	除此之外，最好在MySQL服务器上关闭iptables，selinux等防火墙软件。

 

##### 端口/最大句柄

vim/etc/sysctl.conf

net.ipv4.ip_local_port_range = 1024 65535：# 用户端口范围

net.ipv4.tcp_max_syn_backlog = 4096 

net.ipv4.tcp_fin_timeout = 30 

fs.file-max=65535：# 系统最大文件句柄，控制的是能打开文件最大数量  

 

##### 总结

内核参数总结：

针对关键内核参数设定合适的值，目的是为了减少swap的倾向，并且让内存和磁盘I/O不会出现大幅波动，导致瞬间波峰负载：

1、将vm.swappiness设置为5-10左右即可，甚至设置为0（RHEL 7以上则慎重设置为0，除非你允许OOM kill发生），以降低使用SWAP的机会；

2、将vm.dirty_background_ratio设置为5-10，将vm.dirty_ratio设置为它的两倍左右，以确保能持续将脏数据刷新到磁盘，避免瞬间I/O写，产生严重等待（和MySQL中的innodb_max_dirty_pages_pct类似）；

3、将net.ipv4.tcp_tw_recycle、net.ipv4.tcp_tw_reuse都设置为1，减少TIME_WAIT，提高TCP效率；

4、至于网传的read_ahead_kb、nr_requests这两个参数，我经过测试后，发现对读写混合为主的OLTP环境影响并不大（应该是对读敏感的场景更有效果），不过没准是我测试方法有问题，可自行斟酌是否调整；

#### 应用优化

1、应用程序稳定性

2、SQL 语句性能

3、串行访问资源

4、性能欠佳会话管理

5、这个应用适不适合用 MySQL

 

业务应用和数据库应用独立。

防火墙：iptables、selinux 等其他无用服务（关闭）：

  chkconfig --level 23456 acpid off

  chkconfig --level 23456 anacron off

  chkconfig --level 23456 autofs off

  chkconfig --level 23456 avahi-daemon off

  chkconfig --level 23456 bluetooth off

  chkconfig --level 23456 cups off

  chkconfig --level 23456 firstboot off

  chkconfig --level 23456 haldaemon off

  chkconfig --level 23456 hplip off

  chkconfig --level 23456 ip6tables off

  chkconfig --level 23456 iptables  off

  chkconfig --level 23456 isdn off

  chkconfig --level 23456 pcscd off

  chkconfig --level 23456 sendmail  off

  chkconfig --level 23456 yum-updatesd  off

安装图形界面的服务器不要启动图形界面 runlevel 3。 

另外，思考将来我们的业务是否真的需要 MySQL，还是使用其他种类的数据库。用数据库的最高境界就是不用数据库。

##### 程序访问调优

如果能够满足以下几个方面的要求，那么程序的访问调优会更顺利。

###### 好的架构和程序逻辑

最好是能够通过架构层面尽量避免性能问题的发生。如果你的物理部署无法满足预期的负载要求，或者应用软件的功能架构无法充分利用计算资源，那么，你无论怎么“调优”都无法带来理想的性能提升和扩展性。

生产实践中的性能问题更多地归根于系统的架构设计和应用程序的程序逻辑。运行较长时间之后，MySQL经过了高度优化，性能往往已经很好了，由于数据库的查询只占据了总体响应时间的很小一部分，优化数据库对于整体用户体验的改善并无太大用处，而更改业务逻辑往往是最直接、最有效的。

一个应用的功能模块图对于我们的调优将会很有帮助，通过查看应用的模块图和物理部署图，从数据流、数据交互的角度去理解和分析问题，往往能够发现架构中存在的问题。

1、缓存

互联网应用往往有多级缓存，比如用户访问网站，可能要经过浏览器缓存、应用程序缓存、Web服务器缓存、Memcached之类的缓存产品、数据库缓存等。缓存可以加速我们从更慢的存储设备中获取数据，可以改善用户体验，提高系统吞吐。对于多级缓存来说，越是靠近用户，越是靠近应用，就越有效，也就是说，缓存要靠近数据的使用者，靠近工作被完成的环节，显然，在浏览器的缓存中读取图片会比到Web服务器中读取图片高效得多。到Squid缓存服务器中获取数据比实际去后端的Web服务器中获取数据要高效得多。

缓存的存在应该限定为保护不容易水平扩展的资源，如数据库的大量读取，或者提升用户体验，或者在靠近用户的城市部署图片缓存节点。如果资源易被水平扩展，那么添加缓存层可能不是一个好主意。

缓存的设计目标是，用更快的存储介质存储更慢的存储介质上的数据，以加速响应。比如把磁盘的数据存放在内存中。我们这里仅仅关注被放置在数据库前端的缓存产品，比如Redis、Memcached等产品。我们所使用的缓存产品主要是为了扩充读能力，对于写入，则并没有多少帮助。

MySQL有InnoDB缓冲区，但是其更多地只是属于数据库的一个组件，它的功能是把热点数据缓存在内存中，如果要访问数据，则存在解析开销，也可能需要从磁盘中去获取数据，因为缓存中的内容会因为不常被使用而被剔除出缓存。由于InnoDB缓冲的最小单元是页，而不是基于记录，因此要缓存一条记录，可能要同时缓存许多不相干的记录，这样就会导致内存缓存的利用率比较差。

而对于Memcached之类的记录级的缓存来说，因为应用程序有目的性地缓存了自己所需要的数据，所示其效率一般来说是要高于基于页的InnoDB缓存。而且分布式的Memcached集群可以配置成一个超大的缓存，相对于单个实例内的InnoDB缓存，其扩展性也无疑好得多了。

现实中，由于Memcached的引入可能会导致开发的复杂度上升，所以在项目初期，往往并没有引入Memcached等缓存产品，一般的单机MySQL实例也可以扛得住所有流量，当项目规模扩大之后，读请求的处理可能会成为瓶颈，这个时候可以选择增加从库，或者使用Memcached等缓存产品来突破读的瓶颈。

缓存的指标有缓存命中率、缓存失效速率等。

缓存命中率指命中次数与总的访问次数的比值。

缓存失效速率指每秒的缓存未命中次数。

缓存命中率和性能的关系如图17-1所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEAA.tmp.jpg) 

由图17-1可以得知缓存命中率和性能的关系，98%~99%命中率的性能提升远远大于10%~11%命中率的性能提升。这种非线性的图形，主要是因为缓存命中（cache hit）和缓存未命中（cachemiss）所访问的存储的速度差异比较大而形成的，比如内存和磁盘。由于这样一个非线性的图形，我们在模拟缓存故障的情况下要留意缓存的命中率，如果缓存的命中率不高，那么即使缓存挂了，对后端数据库的冲击也不会很大，但是如果缓存命中率很高，那么如果缓存挂了，可能就会对后端的数据库造成很大冲击。

缓存失效率（cache miss rate），即每秒的非命中次数，由于没有命中缓存，此时需要从更慢的存储上去获取数据。这个指标比较直观，有利于我们分析当应用没有命中缓存时，我们的存储系统能否承受冲击。如果缓存命中率显得很高，但是每秒缓存未命中的次数也很高，那么性能一样会很差。

当我们使用缓存产品，我们要清楚以下几点。

□ 缓存的内容。

□ 缓存的数据量。

□ 设定合适的过期策略。

□ 设置合适的缓存粒度，建议对单个记录、单个元素进行缓存而不是对一个大集合进行缓存，如果要将整个集合对象数据进行缓存的话，获得其中某个具体元素的性能将会受到严重的影响。

为了提高吞吐率，减少网络回返，建议一次获取多条记录，如Memcached的mget方法。

稳定的性能和快速的性能往往一样重要。我们在设计缓存的时候，要考虑到未命中的时候，生成结果的代价，如果会导致偶尔访问的用户响应慢，那么请不要牺牲这部分很小比例的用户。

一般来说，Memcached属于被动缓存，我们也可以采取主动缓存的策略，预先生成一些访问最多的，生成代价最昂贵的内容到Memcached中。

由于序列化和反序列化需要一定的资源开销，当处于高并发高负载的情况下，可能要消耗大量的CPU资源，对于一些序列化的操作一定要慎重，尤其是在处理复杂数据类型时，可能序列化的开销会成为整个系统的瓶颈。

注意事项具体如下。

如果缓存挂了怎么办？或者因为调整缓存，清空缓存，对数据库产生了冲击怎么办？

假设你的业务逻辑是，如果缓存挂了，就去后端的数据库中获取数据。那么很可能短时间内的流量远远超过了数据库的处理能力，导致数据库不能提供服务。所以就需要考虑对于后端数据库的保护，你可能需要对你的应用服务降级使用，即关闭掉不重要的模块，以确保核心功能，或者对数据库进行限流。更友好的方式是，在应用程序里通过锁的机制控制对数据库的并发访问。

限流指的是应用对请求有排队机制，如果队列超过了一定的长度就会触发限流，就会随机抛弃掉一些请求。

2、非结构化数据的存储

不要在数据库里存储非结构化的数据，如视频、音乐、图片等，可以考虑把这些文件存储在分布式文件系统上，数据库中存储地址即可。

3、隔离大任务

批量事务一般应该和实时事务相分离，因为MySQL不太擅长同时处理这两类任务。

有时我们会运行一些定时任务，这些任务很耗费资源，我们需要注意调度，减少对生产环境的影响，比如更新Sphinx索引，需要定期去数据库中扫描大量记录，可能短时间内会造成数据库负荷过高的问题。

对数据库进行的一些大操作，我们可以通过小批量操作的方式减少操作对生产系统的影响，比如下面的这个删除大量数据的例子。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEAB.tmp.jpg) 

执行SQL会删除千万级别的记录，由于删除的记录过多，可能会导致执行计划变为全表扫描，从而导致不能写入数据，影响生产环境。

优化方案具体步骤如下。

1）程序每次获取1万条符合条件的记录，SQL为“SELECT id FROM table_name WHEREctime<'2014-12-12'limit 0,10000”。

2）根据主键id删除记录。每批100条，然后线程休眠100毫秒，直到删除完步骤1）中查询到的所有id。

3）重复步骤1）和步骤2），直到执行“SELECT id FROM table_name WHERE ctime<'2014-12-12'limit 0,10000”返回空结果集为止。

休眠100ms是为了限制删除的速率，减少操作对生产环境的影响。

有时这些大操作无法完全消除，但又占据了大量的资源，这时我们就可以通过系统资源控制的方式对应用进行限制。

4、应用程序相关数据库优先注意事项

以下将列举一些应用程序相关的数据库优化注意事项。

□ 检查应用程序是否需要获取那么多的数据，是否必须扫描大量的记录，是否做了多余的操作。

□ 评估某些操作是应该放在数据库中实现还是在应用中实现？不要在数据库中进行复杂的运算操作，比如应用就更适合做正则的匹配。

□ 应用程序中是否有复杂的查询？有时将复杂的查询分解为多个小查询，效率会更高，可以得到更高的吞吐率。

□ 有时框架中会使用许多无效的操作，比如检测数据库连接是否可用。应该设置相关参数减少这类查询。

###### 好的监控系统和可视化工具

解决性能问题如果是临时的、紧急的，特别是在生产繁忙的时候，你往往会难以下手，因为在压力之下，可能会遗漏一些问题，或者没有得到最好的解决方案。系统应该能够及时预警，在性能问题爆发之前就能够发现问题。所以，你应该有一个好的监控系统。能够监控到数据流各个环节的延时响应。

应用服务需要考虑维护性，可以进行性能统计，了解哪些操作占据了最多资源，通过可视化工具检查性能，可以让我们能够观察应用正在做什么事情，如何优化应用以减少不需要的工作。

###### 良好的灰度发布和降级功能

系统越来越复杂，各个组件之间互相调用，可能某个模块会导致整个系统不能提供服务。我们有必要区分核心的业务和非核心的业务，理清各种模块之间的关系，如果能够有针对性地停止或上线功能/模块/应用，屏蔽掉性能有问题的模块，保证核心基础功能的正常运行，那么我们的整体系统将会更加稳健，可以更快地从故障中恢复。所以，你最好有良好的灰度发布和降级功能，这点对大系统尤为重要。

###### 合理地拆分代码

进行架构调整常用的一个技术是垂直拆分，这里不会严格区分拆分代码、垂直拆分和拆库。

对于复杂的业务，如果出现了因代码而导致的性能问题，那么可以先从物理上隔离服务再考虑代码优化，如部署更多独立的Web服务器或数据库副本以提供服务。将数据库数据拆分到独立的实例（垂直拆分）或增加读库。

垂直拆分需要慎重，因为跨表的连接会变得很难。所以还是要看业务逻辑，如果表之间的关联很多很紧密，那么可能拆分数据库就不是一个好的方案。

拆分业务之后，需要把用户引导到新的程序或服务上。有诸多方式可以使用，如更改域名、应用前端分发、302跳转，或者有些客户端有更多的功能，可以接受云端的指令，修改访问不同功能的域名。

拆分代码需要慎重，因为分离的多套代码，可能会需要更多的接口调用，需要更多的交互，增加了复杂性，应该视后续发展而定。如果代码之间的划分并不是很清晰，一个需求来了，要互相提供接口，更改多套代码，那样往往就增加了复杂性，会影响开发效率。所以具体拆分代码还是要看看后续的项目发展，想清楚应该如何拆分。

许多时候，是业务优先的，因此要先保证业务，增加更多的资源用于突发的负荷。对于数据库中数据的拆分，可能也不是一步到位的，需要兼顾业务的正常运行，因为对数据的拆分，往往需要先进行代码/模块的拆分。可以考虑的一种措施是，克隆一份数据的副本，先用拆分出来的模块读写副本，等代码稳定后，再删除不需要的数据。

模块降级是需要考虑的，模块的各自监控也是需要的。这样在发生故障的时候可以及时关闭一些出问题的模块。保证核心的业务服务。升级的时候，也可以进行灰度升级，逐步打开一些功能开关。

##### 应用服务器调优

数据库的前端一般是应用服务器，如果应用服务器得到了优化，也可以减少数据库的压力，使得整个系统的性能更好、可扩展性更好。应用服务器的调优是一个很大的主题，本章节只是介绍一些基本的指引。

在调优之前，必须要清楚应用服务器的响应过程，细分为各个阶段，哪个阶段耗费的时间最多，就首先从那个部分着手进行优化。

每个应用服务器都有许多参数配置，我们在进行调优的时候，应尽量逐个对参数加以调整优化，这样可以更好地衡量调整的效果，如果参数优化没有效果，那就恢复原来的配置。

修改参数的数值，可以逐步调整参数的值，如果一次性调整得过多，那么可能你得不到一个最优的配置，或者推翻你自己的判断，逐步调整参数，你有更大的可能性最终得到一个性能良好的系统。

在应用程序所在的软硬件环境发生变动的时候，你应该重新审视以前所做的优化配置，看它们是否依然能够工作。

你应该清楚应用服务器的一些参数会如何影响到数据库的负荷，清楚哪些参数会导致数据库的连接数增加，由于一些连接池或框架的行为，大量的连接会导致过多地检测数据库的命令，因此也需要加以留意。

***\*注意：\****应用程序调优往往是最见效的方式，通过减少不必要的工作或让工作执行得更快，我们可以大幅度地提升性能。应用程序访问调优更多的是软件架构的范畴。

#### 优化数据库参数

衡量数据库性能的指标，一般衡量数据库的性能有两个指标：响应时间和吞吐率。响应时间又包括等待时间和执行时间。我们进行优化的主要目的是降低响应时间，提高吞吐率。

下面我们来看下MySQL是如何执行优化和查询的？

大致的步骤如下所示。

1）客户端发送SQL语句给服务器。

2）如果启用了Query Cache，那么MySQL将检查Query Cache，如果命中，就返回Query Cache里的结果集，否则，执行下一个步骤。

3）MySQL Parser解析SQL，MySQL优化器生成执行计划。

4）MySQL查询处理引擎执行此执行计划。

MySQL性能优化显然是要对以上的部分环节或所有环节进行优化，尽量降低各个环节的时间，以提高吞吐率。对于性能的优化，正确的策略是衡量各个环节的开销，优化开销大的环节，而不是使用网上的一些所谓的参数调优和脚本调优，因为他们并不是针对你的特定情况而进行的调优，只是一些泛泛的建议，往往帮助不大。

对于客户端来说，发送SQL语句的开销一般很小，如果是响应缓慢的网络，网络延时较高，那么可以考虑使用长连接或连接池等手段进行加速，或者一次发送多条语句，或者使用存储过程等手段减少网络包的往返次数。

本书主要聚焦于后面的3个步骤，我们需要关注Query Cache是如何加速响应；如何进行查询优化，生成良好的执行计划；实际查询处理过程中对于I/O、CPU、内存等资源的使用是怎样的。我们需要尽量确保高效地利用资源，突破资源的限制。

 

对MySQL配置参数进行优化（my.cnf/my.ini）此优化需要进行压力测试来进行参数调整。

下面列出性能影响较大的几个参数：

1、key_buffer_size：索引缓冲区大小

2、table_cache：能同时打开表的个数

3、query_cache_size和query_cache_type：前者是查询缓冲区大小，后者是前面参数的开关，0表示不使用缓冲区，1表示使用缓冲区,但可以在查询中使用SQL_NO_CACHE表示不要使用缓冲区，2表示在查询中明确指出使用缓冲区才用缓冲区，即SQL_CACHE

4、sort_buffer_size：排序缓冲区

 

***\*MySQL参数优化\*******\*：\****

1、MySQL默认的最大连接数为100，可以在mysql客户端使用以下命令查看

mysql> show variables like 'max_connections';

2、***\*查看当前访问Mysql的线程\****

mysql> show processlist;

3、***\*设置最大连接数\****

mysql>set globle max_connections = 5000;

最大可设置16384,超过没用

4、***\*查看当前被使用的connections\****

mysql>show globle status like 'max_user_connections'

 

##### MySQL配置文件优化

###### 概述

​	MySQL可以通过启动时指定配置参数和使用配置文件两种方式进行配置，在大多数情况下配置文件位于/etc/my.conf或/etc/mysql/my.cnf。在Windows系统配置文件可以是位于C:/windows/my.ini文件，MySQL查找配置文件的顺序可以通过以下方式获得：

​	/usr/sbin/mysqld –verbose –help | grep –A 1 ‘Default options’

​	注意：如果存在多个位置存在配置文件，则后面的会覆盖前面的。

***\*per_thread_buffers优化\****

对于per_thread_buffers，可以将其理解为Oracle的PGA，为每个连接到MySQL的用户进程分配的内存。其包含如下几个参数：

read_buffer_size

read_rnd_buffer_size

sort_buffer_size

thread_stack

join_buffer_size

binlog_cache_size

max_connections

***\*global_buffers优化\****

对于global_buffers，可以理解为Oracle的SGA，用于在内存中缓存从数据文件中检索出来的数据块，可以大大提高查询和更新数据的性能。它主要由以下几个参数组成：

innodb_buffer_pool_size

innodb_additional_mem_pool_size

innodb_log_buffer_size

key_buffer_size

query_cache_size

 

###### 常用配置项

| 配置名称                                                     | 说明                                 |
| ------------------------------------------------------------ | ------------------------------------ |
| sync_binlog = 1                                              |                                      |
| performance_schema = ON                                      |                                      |
| thread_poll_size = 48thread_pool_idle_timeout = 60thread_pool_oversubscribe = 16thread_pool_high_prio_mode = transactionsthread_pool_stall_limit = 100 |                                      |
| innodb_buffer_pool_size = 600Minnodb_buffer_pool_instances = 8innodb_flush_log_at_trx_commit = 1 | innodb_buffer_pool_size：总内存的70% |
| innodb_log_buffer_size                                       |                                      |
| innodb_log_file_size                                         | 事务日志大小                         |
| innodb_log_files_in_group                                    |                                      |

优化数据库参数可以提高资源利用率，从而提高MySQL服务器性能。MySQL服务的配置参数都在my.cnf或my.ini，下面列出性能影响较大的几个参数。

***\*innodb_buffer_pool_size\****

innodb_buffer_pool_size：非常重要的参数，用于配置InnoDB的缓冲池，如果数据库中只有InnoDB表，则推荐配置量为总内存的75%。

这个参数是InnoDB存储引擎的核心参数，默认为128 MB，这个参数要设置为物理内存的60%～70%。

***\*innodb_buffer_pool_instances\****

innodb_buffer_pool_instances：MySQL5.5中新增加参数，可以控制缓冲池的个数，默认情况下只有一个缓冲池

***\*innodb_log_file_size\****

***\*innodb_log_buffer_size\****

innodb_log_buffer_size：InnoDB log缓冲的大小，由于日志最长每秒钟就会刷新，所以一般不用太大。

事务日志所使用的缓冲区。InnoDB在写事务日志的时候，为了提高性能，先将信息写入Innodb LogBuffer中，当满足innodb_flush_log_trx_commit参数所设置的相应条件（或者日志缓冲区写满）时，再将日志写到文件（或者同步到磁盘）中。可以通过innodb_log_buffer_size参数设置其可以使用的最大内存空间。默认为8 MB，一般设置为16～64 MB即可。

***\*innodb_flush_log_at_trx_commit\****

innodb_flush_log_at_trx_commit：关键参数，对InnoDB的IO效率影响很大。默认是1，可以取0,1,2三个值，一般建议设置为2，但如果数据安全性要求比较高则使用默认值1

***\*innodb_read_io_thread/innodb_write_io_threads\****

innodb_read_io_thread/innodb_write_io_threads：决定了InnoDB读写的IO进程数，默认为4

***\*innodb_file_per_table\****

innodb_file_per_table：关键参数，控制InnoDB每一个表使用独立的表空间，默认为OFF，也就是所有表都会建立在共享表空间中

innodb_stats_on_metadata：决定了MySQL在什么情况下会刷新innodb表的统计信息

 

传送门:更多参数

https://www.mysql.com/cn/why-mysql/performance/index.html

 

***\*如何为缓存池分配内存：\****

Innodb_buffer_pool_size，定义了Innodb所使用缓存池的大小，对其性能十分重要，必须足够大，但是过大时，使得Innodb 关闭时候需要更多时间把脏页从缓冲池中刷新到磁盘中；

总内存-（每个线程所需要的内存*连接数）-系统保留内存

key_buffer_size，定义了MyISAM所使用的缓存池的大小，由于数据是依赖存储操作系统缓存的，所以要为操作系统预留更大的内存空间；

select sum(index_length) from information_schema.talbes where engine='myisam'

***\*注意：\****即使开发使用的表全部是Innodb表，也要为MyISAM预留内存，因为MySQL系统使用的表仍然是MyISAM表。

max_connections控制允许的最大连接数，一般2000更大。

不要使用外键约束保证数据的完整性。

***\*innodb_additional_mem_pool_size\****

该参数用来存储数据字典信息和其他内部数据结构。表越多，需要在这里分配的内存越多。如果InnoDB用光了这个池内的内存，InnoDB开始从操作系统分配内存，并且往MySQL错误日志中写警告信息，默认值是8 MB，当发现错误日志中已经有相关的警告信息时，就应该适当地增加该参数的大小。一般设置为16 MB即可。

 

###### 内存配置相关参数

1、确定可以使用的内存上限。

2、内存的使用上限不能超过物理内存，否则容易造成内存溢出；（对于32位操作系统，MySQL只能试用3G以下的内存。）

3、确定MySQL的每个连接单独使用的内存。

 

***\*table_cache\****

table_cache:能同时打开表的个数

为所有线程打开表的数量。增加该值能增加mysqld要求的文件描述符的数量。MySQL对每个唯一打开的表需要2个文件描述符。默认数值是64，我把它改为512。

 

***\*thread_cache_size\****

可以复用的保存在中的线程的数量。如果有，新的线程从缓存中取得，当断开连接的时候如果有空间，客户的线置在缓存中。如果有很多新的线程，为了提高 性能可 以这个变量值。通过比较Connections和Threads_created状态的变量，可以看到这个变量的作用。我把它设置为80。

***\*thread_stack\****

该参数表示每个线程的堆栈大小。默认为192 KB。如果是64位操作系统，设置为256 KB即可，这个参数不要设置过大。

***\*query_cache_size\****

缓存select语句和结果集大小的参数。

query_cache_size和query_cache_type:前者是查询缓冲区大小,后者是前面参数的开关,0表示不使用缓冲区,1表示使用缓冲区,但可以在查询中使用SQL_NO_CACHE表示不要使用缓冲区,2表示在查询中明确指出使用缓冲区才用缓冲区,即SQL_CACHE.

***\*record_buffer\****

每个进行一个顺序扫描的线程为其扫描的每张表分配这个大小的一个缓冲区。如果你做很多顺序扫描，你可能想要增加该值。默认数值是131072(128K)，我把它改为16773120 (16M)。

 

***\*sort_buffer\****

每个需要进行排序的线程分配该大小的一个缓冲区。增加这值加速ORDER BY或GROUP BY操作。默认数值是2097144(2M)，我把它改为16777208 (16M)。

 

***\*key_buffer_size\****

key_buffer_size:索引缓冲区大小

索引块是缓冲的并且被所有的线程共享。key_buffer_size是用于索引块的缓冲区大小，增加它可得到更好处理的索引(对所有读和多重写)，到你能负担得起那样多。如果你使它太大，系统将开始换页并且真的变慢了。默认数值是8388600(8M)，我的MySQL主机有2GB内存，所以我把它改为402649088(400MB)。

 

该参数用来缓存MyISAM存储引擎的索引参数。MySQL5.5默认为InnoDB存储引擎，所以这个参数可以设置小一些，64 MB即可。

***\*sort_buffer_size\****

sort_buffer_size #定义了每个线程排序缓存区的大小，MySQL在有查询、需要做排序操作时才会为每个缓冲区分配内存（直接分配该参数的全部内存）；

在表进行order by和group by排序操作时，由于排序的字段没有索引，会出现Using filesort，为了提高性能，可用此参数增加每个线程分配的缓冲区大小。默认为2 MB。这个参数不要设置过大，一般在128 ～256 KB即可。另外，一般出现Using filesort的时候，要通过增加索引来解决。比如，图5-100所示的这个例子：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEBC.tmp.jpg) 

***\*join_buffer_size\**** 

join_buffer_size #定义了每个线程所使用的连接缓冲区的大小，如果一个查询关联了多张表，MySQL会为每张表分配一个连接缓冲，导致一个查询产生了多个连接缓冲；

表进行join连接操作时，如果关联的字段没有索引，会出现Using join buffer，为了提高性能，可用此参数增加每个线程分配的缓冲区大小。默认为128 KB。这个参数不要设置过大，一般在128 ～256KB即可。一般出现Using join buffer的时候，要通过增加索引来解决。比如，图5-101所示的这个例子：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEBD.tmp.jpg) 

 

***\*read_buffer_size\****

read_buffer_size #定义了当对一张MyISAM进行全表扫描时所分配读缓冲池大小，MySQL有查询需要时会为其分配内存，其必须是4k的倍数；

该参数用于表的随机读取，表示每个线程分配的缓冲区大小。比如，按照一个非索引字段做order by排序操作时，就会利用这个缓冲区来暂存读取的数据。默认为256 KB。这个参数不要设置过大，一般在128 ～256 KB即可。

***\*read_rnd_buffer_size\****

read_rnd_buffer_size #索引缓冲区大小，MySQL有查询需要时会为其分配内存，只会分配需要的大小。

该参数用于表的随机读取，表示每个线程分配的缓冲区大小。比如，按照一个非索引字段做order by排序操作时，就会利用这个缓冲区来暂存读取的数据。默认为256 KB。这个参数不要设置过大，一般在128 ～256 KB即可。

注意：sort_buffer_size、join_buffer_size、read_buffer_size、read_rnd_buffer_size以上四个参数是为一个线程分配的，如果有100个连接，那么需要×100。

***\*binlog_cache_size\****

binlog_cache_size一般来说，如果数据库中没有什么大事务，写入也不是特别频繁，将其设置为1 ～2 MB是一个合适的选择。如果有很大的事务，可以适当增加这个缓存值，以获得更好的性能。

 

***\*MySQL数据库实例：\****

1、MySQL是单进程多线程（而oracle是多进程），也就是说 MySQL实例在系统上表现就是一个服务进程，即进程；

2、MySQL实例是线程和内存组成，实例才是真正用于操作数据库文件的；

一般情况下一个实例操作一个或多个数据库；集群情况下多个实例操作一个或多个数据库。

 

###### Cache层的操作与调优

***\*开启query cache\****

在my.cnf里mysqld下添加：

**Query_cache_size**

Query_cache_size = 268435456

使用的内存大小， 这个值必须是1024的整数倍

Query_cache_type = 1

此字段值可以0,1,2 三个值

0,代表关闭

1代表给所有的select语句做cache

当语句select no_no_cache * from A;执行时不做cache

2代表开启query cache功能，但只有执行

语句select sql_cache * from A; 时才做cache

***\*注意：\****

OLAP类型数据库，需要重点加大此内存缓存，但是一般不会超过GB；
对于经常被修改的数据，缓存会立马失效；

我们可以实用内存数据库（redis、memecache），替代他的功能。

 

**Query_cache_limit**

Query_cache_limit = 1048576

单条语句的最大容量限制，超过此容量的sql语句讲不被cache

当做cache时需注意，只有完全相同的sql语句才被认为是相同的，此时才能够从缓存当中取数据，增加sql执行速度。

如果cache不合理，会导致大量的清缓存，加cache的动作，不但不会增加sql执行速度，反而会降低效率。如：当某表中有大量的插入，删除，修改等操作时，就不适合做cache。

***\*query cache 运行状态分析\****

show status like ‘%qcache%’

qcache_free_blocks:数目大说明有碎片

qcache_free_memory:缓存中的空闲内存

qcache_hits:命中次数，每次查询在缓存中命中就增加

qcache_inserts:缓存中插入查询次数，每次插入就增加

qcache_lowmem_prunes:这个数字增长，表明碎片多或内存少

qcache_total_blocks:缓存中块的总数量

***\*计算\****

Query_cache命中率=query_hits/(qcache_hits+qcache_inserts)

缓存碎片率=qcache_free_blocks/qcache_total_blocks*100%

碎片率超过20%时，可用flush query cache整理缓存碎片

缓存利用率=（query_cache_size-qcache_free_memory）/query_cache_size*100%

***\*qchche优化\****

整理所有查询的sql，讲所有需要返回结果相同以及查询方法相同的sql整理后写成一模一样的，或使用mybatis框架，把所有的sql写到配置文件中，使用的时候调用。

原因是，只有一模一样的sql语句，才会在cache中取结果。

 

###### 连接层

设置合理的连接客户和连接方式

max_connections     # 最大连接数，看交易笔数设置   

max_connect_errors  # 最大错误连接数，能大则大

connect_timeout      # 连接超时

wait_timeout         #非交互超时时间，默认8小时

interactive_timeout    #交互超时时间，默认8小时

max_user_connections # 最大用户连接数

skip-name-resolve    # 跳过域名解析

back_log            # 可以在堆栈中的连接数量

***\*max_connections\****

该参数用来设置最大连接数，默认为100。一般设置为512～1000即可。

MySQL的最大连接数，如果服务器的并发请求量比较大，可以调高这个值。当然这是要建立在机器能够支撑的情况下，因为如果连接数越来越多，mysql会为每个连接提供缓冲区，就会开销越多的内存，所以需要适当的调整该值，不能随便去提高设值。

允许的同时客户的数量。增加该值增加mysqld要求的文件描述符的数量。这个数字应该增加，否则，你将经常看到Too many connections错误。默认数值是100。

 

***\*修改方式如下（/etc/my.cnf）：\****

Max_connections = 1024

一般对于MySQL而言，单机几百个，最多几千个，这个需要根据业务预估。

***\*使用建议：\****

1、开启数据库时，我们可以临时设置一个比较大的测试值；

2、观察show status like ‘Max_connections’变化；

3、如果Max_used_connections跟max_connection相同，那么就是max_connections设置过低或者超过服务器的负载上限了，低于10%则设置过大。

达到最大的连接数会报错：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEBE.tmp.jpg) 

 

***\*max_\*******\*used_\*******\*connections\****

表示自数据库启动以来，最大的连接数是多少（这个可以当做预估最大连接数的依据）。

 

***\*connect_timeout\****

***\*wait_timeout\****

指的是mysql在关闭一个非交互的连接之前所要等待的秒数。

服务器在关闭它之前在一个连接上等待行动的秒数。默认数值是28800。

注：参数的调整可以通过修改/etc/my.cnf 文件并重启MySQL实现。这是一个比较谨慎的工作，上面的结果也仅仅是我的一些看法，你可以根据你自己主机的硬件情况(特别是内存大小)进一步修改。

 

***\*interactive_timeout\****

指的是mysql在关闭一个交互的连接之前所需要等待的秒数，比如我们在终端上进行mysql管理，使用的即使交互的连接，这时候，如果没有操作的时间超过了interactive_time设置的时间就会自动的断开，默认的是28800，可调优为7200。

一个交互的客户被定义为对 mysql_real_connect()使用CLIENT_INTERACTIVE 选项的客户。默认数值是28800，我把它改为7200。

 

***\*设置建议：\****

如果设置太大，容易造成连接打开时间过长，在show processlist时候，能看到很多的连接，一般希望wait timeout尽可能低。

***\*修改方式：\****

wait_timeout=60

interactive_timeout=1200

长连接的应用，为了不去反复的回收和分配资源，降低额外的开销。一般我们会将wait_timeout设定比较小，interactive_timeout要和应用开发人员沟通长链接的应用是否很多。如果他需要长链接，那么这个值可以不需要调整。

 

***\*max_user_connections\****

***\*back_log\****

要求MySQL能有的连接数量。当主要MySQL线程在一个很短时间内得到非常多的连接请求，这就起作用，然后主线程花些时间(尽管很短)检查连接并且启动一个新线程。

MySQL能暂存的连接数量，当主要mysql线程在一个很短时间得到非常多的连接请求时它就会起作用，如果mysql的连接数据达到max_connection时候，新来的请求将会被存在堆栈中，等待某一连接释放资源，该堆栈的数量及back_log，如果等待连接的数量超过back_log，将不被授予连接资源。

back_log值指出在MySQL暂时停止回答新请求之前的短时间内多少个请求可以被存在堆栈中。只有如果期望在一个短时间内有很多连接，你需要增加它，换句话说，这值对到来的TCP/IP连接的侦听队列的大小。你的操作系统在这个队列大小上有它自己的限制。试图设定back_log高于你的操作系统的限制将是无效的。

***\*判断依据：\****

show full processlist

发现大量的待连接进程时，就需要加大back_log或者加大max_connections的值。

修改方式（/etc/my.cnf）：

back_log = 1024

 

当你观察你的主机进程列表，发现大量264084 | unauthenticated user | xxx.xxx.xxx.xxx | NULL | Connect | NULL | login | NULL 的待连接进程时，就要加大 back_log 的值了。默认数值是50，我把它改为500。

 

 

###### 特殊配置

| 配置名称                                | 说明                                 |
| --------------------------------------- | ------------------------------------ |
| binlog_group_commit_sync_delay          | 调整组提交性能，强制组提交等待       |
| binlog_group_commit_sync_no_delay_count | 调整组提交性能，强制组提交等待事务数 |
| binlog_spin_wait_delay                  | 解决spinlock导致sys过高              |
| innodb_sync_spin_loops                  | 解决spinlock导致sys过高              |
| sync_master_info                        | 提高主备复制性能                     |
| sync_relay_log                          |                                      |
| sync_relay_log_info                     |                                      |

 

###### 总结

建议调整下面几个关键参数以获得较好的性能（可使用本站提供的my.cnf生成器生成配置文件模板）：

1、选择Percona或MariaDB版本的话，强烈建议启用thread pool特性，可使得在高并发的情况下，性能不会发生大幅下降。此外，还有extra_port功能，非常实用， 关键时刻能救命的。还有另外一个重要特色是 QUERY_RESPONSE_TIME 功能，也能使我们对整体的SQL响应时间分布有直观感受；

2、设置default-storage-engine=InnoDB，也就是默认采用InnoDB引擎，强烈建议不要再使用MyISAM引擎了，InnoDB引擎绝对可以满足99%以上的业务场景；

3、调整innodb_buffer_pool_size大小，如果是单实例且绝大多数是InnoDB引擎表的话，可考虑设置为物理内存的50% ~ 70%左右；

4、根据实际需要设置innodb_flush_log_at_trx_commit、sync_binlog的值。如果要求数据不能丢失，那么两个都设为1。如果允许丢失一点数据，则可分别设为2和10。而如果完全不用care数据是否丢失的话（例如在slave上，反正大不了重做一次），则可都设为0。这三种设置值导致数据库的性能受到影响程度分别是：高、中、低，也就是第一个会另数据库最慢，最后一个则相反；

5、设置innodb_file_per_table = 1，使用独立表空间，我实在是想不出来用共享表空间有什么好处了；

6、设置innodb_data_file_path = ibdata1:1G:autoextend，千万不要用默认的10M，否则在有高并发事务时，会受到不小的影响；

7、设置innodb_log_file_size=256M，设置innodb_log_files_in_group=2，基本可满足90%以上的场景；

8、设置long_query_time = 1，而在5.5版本以上，已经可以设置为小于1了，建议设置为0.05（50毫秒），记录那些执行较慢的SQL，用于后续的分析排查；

9、根据业务实际需要，适当调整max_connection（最大连接数）、max_connection_error（最大错误数，建议设置为10万以上，而open_files_limit、innodb_open_files、table_open_cache、table_definition_cache这几个参数则可设为约10倍于max_connection的大小；

10、常见的误区是把tmp_table_size和max_heap_table_size设置的比较大，曾经见过设置为1G的，这2个选项是每个连接会话都会分配的，因此不要设置过大，否则容易导致OOM发生；其他的一些连接会话级选项例如：sort_buffer_size、join_buffer_size、read_buffer_size、read_rnd_buffer_size等，也需要注意不能设置过大；

11、由于已经建议不再使用MyISAM引擎了，因此可以把key_buffer_size设置为32M左右，并且强烈建议关闭query cache功能。

 

##### 第三方配置工具

​	网址：www.tools.percona.com/wizard

##### 存储引擎层优化

innodb 基础优化参数：

default-storage-engine

innodb_buffer_pool_size  # 没有固定大小，50%测试值，看看情况再微调。但是尽量设置不要超过物理内存70%

innodb_file_per_table=(1,0)

innodb_flush_log_at_trx_commit=(0,1,2) # 1是最安全的，0是性能最高，2折中

binlog_sync=(0,1,n)  #多少个事物刷新一次binlog，0性能最高

Innodb_flush_method=(O_DIRECT, fdatasync)

innodb_log_buffer_size     # 100M以下

innodb_log_file_size      # 100M 以下

innodb_log_files_in_group   # 5个成员以下,一般2-3个够用（iblogfile0-N）

innodb_max_dirty_pages_pct  # 达到百分之75的时候刷写 内存脏页到磁盘。

log_bin

binlog_format = ROW    #row模式占用资源多，但安全性高

expire_logs_days = 7     #binlog保留7天，之前的过期删除

max_binlog_cache_size     # 可以不设置

max_binlog_size        # 可以不设置

innodb_additional_mem_pool_size   #小于2G内存的机器，推荐值是20M。32G内存以上100M

#### 分库分表

因为数据库压力过大，首先一个问题就是高峰期系统性能可能会降低，因为数据库负载过高对性能会有影响。另外一个，压力过大把你的数据库给搞挂了怎么办？所以此时你必须得对系统做分库分表 + 读写分离，也就是把一个库拆分为多个库，部署在多个数据库服务上，这时作为主库承载写入请求。然后每个主库都挂载至少一个从库，由从库来承载读请求。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEECF.tmp.jpg) 

 

#### 缓存集群

如果用户量越来越大，此时你可以不停的加机器，比如说系统层面不停加机器，就可以承载更高的并发请求。然后数据库层面如果写入并发越来越高，就扩容加数据库服务器，通过分库分表是可以支持扩容机器的，如果数据库层面的读并发越来越高，就扩容加更多的从库。

但是这里有一个很大的问题：数据库其实本身不是用来承载高并发请求的，所以通常来说，数据库单机每秒承载的并发就在几千的数量级，而且数据库使用的机器都是比较高配置，比较昂贵的机器，成本很高。如果你就是简单的不停的加机器，其实是不对的。所以在高并发架构里通常都有缓存这个环节，缓存系统的设计就是为了承载高并发而生。所以单机承载的并发量都在每秒几万，甚至每秒数十万，对高并发的承载能力比数据库系统要高出一到两个数量级。所以你完全可以根据系统的业务特性，对那种写少读多的请求，引入缓存集群。具体来说，就是在写数据库的时候同时写一份数据到缓存集群里，然后用缓存集群来承载大部分的读请求。这样的话，通过缓存集群，就可以用更少的机器资源承载更高的并发。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEED0.tmp.jpg) 

 

# 优化思路

在数据库优化上有两个主要方面：

安全：数据可持续性。

性能：数据的高性能访问。

基本分析思路：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEED1.tmp.jpg) 

​	首先，需要通过脚本不断刷新服务器的状态，查看是不是周期性出现问题。而不是先查看SQL语句，应该先把服务器的性能瓶颈摸清楚。

 

## **优化范围**

1、硬件à系统à应用à数据库à架构（高可用、读写分离、分库分表）

2、SQL优化方向：

执行计划、索引、SQL改写

3、架构优化方向：

高可用架构、高性能架构、分库分表

 

***\*存储、主机和操作系统方面：\****

1、主机架构稳定性

2、I/O规划及配置

3、Swap交换分区

4、OS内核参数和网络问题

***\*应用程序方面：\****

1、应用程序稳定性

2、SQL语句性能

3、串行访问资源

4、性能欠佳会话管理

5、这个应用适不适合用 MySQL

***\*数据库优化方面：\****

1、内存

2、数据库结构（物理&逻辑）

3、实例配置

***\*说明：\****不管是设计系统、定位问题还是优化，都可以按照这个顺序执行。

 

## **优化选择**

优化成本：硬件>系统配置>数据库表结构>SQL 及索引。

优化效果：硬件<系统配置<数据库表结构

 

## **观察服务器状态**

​	通过命令show status可以显示数据库的状态信息，其中比较有用的信息：

​	Queries：当前发生的查询次数

​	Threads_connected：有多少线程已连接

​	Thread_running：有多少线程正在工作

​	可以借助awk工具，对结果进行分析：

​	mysqladmin –uroot ext | awk ‘/Queries/{printf(“%d”,$4)}’

 

​	**设计实验：**

​	总数据3w以上，50个并发，每秒请求500~1000次。请求结果缓存在memcache，生命周期为5分钟，观察mysql连接数，每秒请求数的周期变化。

 

## **缓存**

### **缓存穿透**

### **缓存击穿**

### **缓存雪崩**

## **观察mysql进程状态**

***\*数据库层面的优化：\****

一般应急调优的思路：针对突然的业务办理卡顿，无法进行正常的业务处理，需要马上解决的场景。

1、show processlist

2、explain  select id ,name from stu where name='clsn'; # ALL  id name age  sex

  select id,name from stu  where id=2-1 函数 结果集>30;

　 show index from table;

3、通过执行计划判断，索引问题（有没有、合不合理）或者语句本身问题

4、show status  like '%lock%';  #查询锁状态

　 kill SESSION_ID;  # 杀掉有问题的session

常规调优思路：针对业务周期性的卡顿，例如在每天10-11点业务特别慢，但是还能够使用，过了这段时间就好了。

1、查看slowlog，分析slowlog，分析出查询慢的语句；

2、按照一定优先级，一个一个排查所有慢语句；

3、分析top SQL，进行explain调试，查看语句执行时间；

4、调整索引或语句本身。

### **show** **processlists**

​	通过命令show processlists获取有多少连接，当前连接的库以及连接的时间和状态：

​	show processlist\G | grep Satete: | uniq –c | sort -rn

​	几个典型状态：

Converting HEAP to MyISAM：查询结果太大时，把结果放在磁盘。

注：如果服务器频繁出现converting HEAP to MyISAM说明

1、 SQL有问题，取出的结果或者中间结果过大，内存临时表存放不下；

2、 服务器配置的临时表内存参数过小：

tmp_table_size

max_heap_table_size

 

**什么情况下产生临时表？**

1、 group by的列和order by的列不同时，2表查询时，取A表的内容，group/order by另外表的列；

2、 distinct和order by一起使用时；

3、 开启了SQL_SMALL_RESULT选项。

 

**什么情况下临时表写到磁盘上？**

1、 取出的列含有text/blob类型时，内存表存储不了text/blob类型；

2、 在group by或distinct的列中存在>512字节的string列；

3、 Select中含有>512字节的string列，同时又使用了union或union all语句。

 

***\*值得注意的mysql进程状态：\****

create tmp table：创建临时表（如group时存储中间结果）。

Copying to tmp table on disk：把内存临时表复制到磁盘

locked：被其他查询锁住

logging slow query：记录慢查询

注：重现前把临时表内存变小，这样方便复现出问题。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEE1.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEE2.tmp.jpg) 

​	注：Sending data表示发送数据，如果查询数据结果集过大，则这个数据会很大，即大部分时间都浪费在发送数据上。

Sorting result应该尽量避免排序，这个非常耗时。

### **profile**

参考：

https://www.cnblogs.com/mydriverc/p/7086523.html

 

​	查看状态：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEE3.tmp.jpg) 

​	注：profiling表示是否开启，profiling_history_size表示历史记录查询语句个数。

​	开启：set profiling = on;

 

语法：SHOW PROFILE [type] FOR QUERY N;

type是可选的，取值范围可以如下：

ALL 显示所有性能信息

BLOCK IO 显示块IO操作的次数

CONTEXT SWITCHES 显示上下文切换次数，不管是主动还是被动

CPU 显示用户CPU时间、系统CPU时间

IPC 显示发送和接收的消息数量

MEMORY [暂未实现]

PAGE FAULTS 显示页错误数量

SOURCE 显示源码中的函数名称与位置

SWAPS 显示SWAP的次数

​	

查看历史查询语句：show profiles

​	查看某一条语句的分析：show profile for query 1

注：SHOW PROFILES显示最近发给服务器的多条语句，条数根据会话变量profiling_history_size定义，默认是15，最大值为100。设为0等价于关闭分析功能。

SHOW PROFILE FOR QUERY n，这里的n就是对应SHOW PROFILES输出中的Query_ID。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsEEE4.tmp.jpg) 

​	注：通过这个指令可以分析查询语句在启动、打开表、系统锁、表锁、初始化、优化器、准备、执行、发送数据等耗时。这里显然发送数据耗时大，查询语句过大。

## **表优化和列类型选择**

​	1、在各个类型都满足需要的时候，字段类型优先级：整型>data，time>char，varchar>blob

​	原因：整型，time运算快，节省空间

​	char/varchar要考虑字符集的转换与排序时的校对集，速度慢

​	blob无法使用内存临时表。

​	2、够用就行，不要慷慨（如smallint，varchar(N)）

​	原因：太大的字段浪费内存，影响速度

​	以varchar(10)，varchar(300)存储的内容相同，但在表连接查询时，varchar(300)要花更多内存

​	3、尽量避免使用NULL()

​	原因：NULL不利于索引，要用特殊的字节来标准

​	在磁盘上占据的空间其实更大。

 

ENUM列的说明：

1、ENUM列在内部是用整型来储存的；

2、ENUM列与ENUM列相关联速度最快；

3、ENUM列与(var)char相比较：

劣势：当char非常长时，ENUM依然是整型固定长度

优势：当查询的数据量越大时，ENUM的优势越明显

4、ENUM与char/varchar关联，因为要转换，速度要比ENUM->ENUM，char->char慢

​	详细内容参考规范章节—数据库字段设计规范。

## **索引优化**

​	详细内容参考规范章节—索引设计规范。

# 优化工具

数据库层面

***\*检查问题常用的 12 个工具：\****

MySQL

## **mysqladmin**

mysqladmin：MySQL 客户端，可进行管理操作

## **mysqlshow**

mysqlshow：功能强大的查看 shell 命令

## **SHOW**

SHOW [SESSION | GLOBAL] variables：查看数据库参数信息

SHOW [SESSION | GLOBAL] STATUS：查看数据库的状态信息

## **information_schema**

information_schema：获取元数据的方法

## **SHOW ENGINE INNODB STATUS**

SHOW ENGINE INNODB STATUS：Innodb 引擎的所有状态

## **SHOW PROCESSLIST**

SHOW PROCESSLIST：查看当前所有连接的 session 状态

## **explain**

explain：获取查询语句的执行计划

## **show index**

show index：查看表的索引信息

## **slow-log**

slow-log：记录慢查询语句

## **mysqldumpslow**

mysqldumpslow：分析 slowlog 文件的工具

 

***\*不常用但好用的 7 个工具：\****

## **Zabbix**

Zabbix：监控主机、系统、数据库（部署 Zabbix 监控平台）

## **pt-query-digest**

pt-query-digest：分析慢日志

## **MySQL slap**

MySQL slap：分析慢日志

## **sysbench**

sysbench：压力测试工具

## **MySQL profiling**

MySQL profiling：统计数据库整体状态工具   

## **Performance Schema**

Performance Schema：MySQL 性能状态统计的数据

## **Sys**

## **workbench**

workbench：管理、备份、监控、分析、优化工具（比较费资源）

关于 Zabbix 参考：http://www.cnblogs.com/clsn/p/7885990.html

# 规范

## 数据库命名规范

所有数据库对象名称必须使用小写字母并用下划线分割

所有数据库对象名称禁止使用MySQL保留关键字（如果表名中包含关键字查询时，需要将其用单引号括起来）

数据库对象的命名要能做到见名识意，并且最后不要超过32个字符

临时库表必须以tmp_为前缀并以日期为后缀，备份表必须以bak_为前缀并以日期 (时间戳) 为后缀

所有存储相同数据的列名和列类型必须一致（一般作为关联列，如果查询时关联列类型不一致会自动进行数据类型隐式转换，会造成列上的索引失效，导致查询效率降低）

## **数据库基本设计规范**

1、所有表必须使用 Innodb 存储引擎

没有特殊要求（即 Innodb 无法满足的功能如：列存储，存储空间数据等）的情况下，所有表必须使用Innodb存储引擎（MySQL5.5之前默认使用Myisam，5.6以后默认的为Innodb）。

Innodb 支持事务，支持行级锁，更好的恢复性，高并发下性能更好。

2、***\*数据库和表的字符集统一使用UTF8\****

兼容性更好，统一字符集可以避免由于字符集转换产生的乱码，不同的字符集进行比较前需要进行转换会造成索引失效，如果数据库中有存储emoji表情的需要，字符集需要采用 utf8mb4字符集。

3、***\*所有表和字段都需要添加注释\****

使用 comment 从句添加表和列的备注，从一开始就进行数据字典的维护

4、尽量控制单表数据量的大小，***\*建议控制在500万以内\****。

500万并不是MySQL数据库的限制，过大会造成修改表结构，备份，恢复都会有很大的问题。

可以用历史数据归档（应用于日志数据），分库分表（应用于业务数据）等手段来控制数据量大小

5、***\*谨慎使用MySQL分区表\****

分区表在物理上表现为多个文件，在逻辑上表现为一个表；

谨慎选择分区键，跨分区查询效率可能更低；

建议采用物理分表的方式管理大数据。

6、尽量做到冷热数据分离，减小表的宽度

MySQL 限制每个表最多存储 4096 列，并且每一行数据的大小不能超过65535字节。

减少磁盘 IO，保证热数据的内存缓存命中率（表越宽，把表装载进内存缓冲池时所占用的内存也就越大，也会消耗更多的 IO）；

更有效的利用缓存，避免读入无用的冷数据；

经常一起使用的列放到一个表中（避免更多的关联操作）。

7、禁止在表中建立预留字段

预留字段的命名很难做到见名识义。

预留字段无法确认存储的数据类型，所以无法选择合适的类型。

对预留字段类型的修改，会对表进行锁定。

8、禁止在数据库中存储图片,文件等大的二进制数据

通常文件很大，会短时间内造成数据量快速增长，数据库进行数据库读取时，通常会进行大量的随机 IO 操作，文件很大时，IO 操作很耗时。

通常存储于文件服务器，数据库只存储文件地址信息

9、禁止在线上做数据库压力测试

10.、禁止从开发环境，测试环境直接连接生成环境数据库

 

## **数据库字段设计规范**

\1. ***\*优先选择符合存储需要的最小的数据类型\****

原因：

列的字段越大，建立索引时所需要的空间也就越大，这样一页中所能存储的索引节点的数量也就越少也越少，在遍历时所需要的IO次数也就越多，索引的性能也就越差。

方法：

a.将字符串转换成数字类型存储，如:将IP地址转换成整形数据

MySQL提供了两个方法来处理ip地址

inet_aton把ip转为无符号整型 (4-8位)inet_ntoa把整型的ip转为地址

插入数据前，先用inet_aton把ip地址转为整型，可以节省空间，显示数据时，使用 inet_ntoa 把整型的 ip 地址转为地址显示即可。

b.对于非负型的数据 (如自增 ID,整型 IP) 来说,要优先使用无符号整型来存储

原因：

无符号相对于有符号可以多出一倍的存储空间

SIGNED INT -2147483648~2147483647

UNSIGNED INT 0~4294967295

VARCHAR(N)中的N代表的是字符数，而不是字节数，使用UTF8存储255个汉字 Varchar(255)=765个字节。过大的长度会消耗更多的内存。

\2. ***\*避免使用TEXT\*******\*，\*******\*BLOB数据类型，最常见的TEXT类型可以存储64k的数据\****

a. 建议把 BLOB或是TEXT列分离到单独的扩展表中

MySQL内存临时表不支持 TEXT、BLOB这样的大数据类型，如果查询中包含这样的数据，在排序等操作时，就不能使用内存临时表，必须使用磁盘临时表进行。而且对于这种数据，MySQL还是要进行二次查询，会使sql性能变得很差，但是不是说一定不能使用这样的数据类型。

如果一定要使用，建议把BLOB或是TEXT列分离到单独的扩展表中，查询时一定不要使用select * 而只需要取出必要的列，不需要TEXT列的数据时不要对该列进行查询。

2、TEXT或BLOB类型只能使用前缀索引

因为MySQL对索引字段长度是有限制的，所以TEXT类型只能使用前缀索引，并且TEXT列上是不能有默认值的

3、避免使用ENUM类型

修改ENUM值需要使用ALTER语句

ENUM类型的ORDER BY操作效率低，需要额外操作

禁止使用数值作为ENUM的枚举值

ENUM列的说明：

a. enum列在内部是用整型来存储的；

b. enum列与enum列相关联速度最快

c. enum列比(var)char的弱势：在碰到与char关联时要转换，耗费时间

d. 优势在于，当cahr非常长时，enum依然是整型固定长度

e. enum与char/varchar关联，因为要转换，速度要比enum->enum，char->char要慢，但有时也这样用，就是在数据量特别大时，可以节省IO。

4、尽可能把所有列定义为 NOT NULL

原因：

索引NULL列需要额外的空间来保存，所以要占用更多的空间

进行比较和计算时要对NULL值做特别的处理

5、使用TIMESTAMP(4个字节) 或DATETIME类型 (8 个字节) 存储时间

TIMESTAMP存储的时间范围1970-01-01 00:00:01 ~ 2038-01-19-03:14:07

TIMESTAMP占用 4 字节和INT相同，但比INT可读性高

超出 TIMESTAMP取值范围的使用DATETIME类型存储

经常会有人用字符串存储日期型的数据（不正确的做法）

缺点 ：

1：无法用日期函数进行计算和比较缺点 

2：用字符串存储日期要占用更多的空间

6、***\*同财务相关的金额类数据必须使用decimal类型\****

非精准浮点：float,double精准浮点：decimal

Decimal类型为精准浮点数，在计算时不会丢失精度

占用空间由定义的宽度决定，每4个字节可以存储9位数字，并且小数点要占用一个字节

可用于存储比bigint更大的整型数据

 

## **索引设计规范**

注：markdown对代码块的语法是开始和结束行都要添加：```,其中 ` 为windows键盘左上角

1、限制每张表上的索引数量,建议单张表索引不超过5个

索引并不是越多越好！索引可以提高效率同样可以降低效率。

索引可以增加查询效率，但同样也会降低插入和更新的效率，甚至有些情况下会降低查询效率。

因为MySQL优化器在选择如何优化查询时，会根据统一信息，对每一个可以用到的索引来进行评估，以生成出一个最好的执行计划，如果同时有很多个索引都可以用于查询，就会增加MySQL优化器生成执行计划的时间，同样会降低查询性能。

2、禁止给表中的每一列都建立单独的索引

5.6 版本之前，一个sql只能使用到一个表中的一个索引，5.6以后，虽然有了合并索引的优化方式，但是还是远远没有使用一个联合索引的查询方式好。

3、每个Innodb表必须有个主键

Innodb是一种索引组织表：数据的存储的逻辑顺序和索引的顺序是相同的。每个表都可以有多个索引，但是表的存储顺序只能有一种。

Innodb是按照主键索引的顺序来组织表的

不要使用更新频繁的列作为主键，不适用多列主键（相当于联合索引）不要使用 UUID,MD5,HASH,字符串列作为主键（无法保证数据的顺序增长）主键建议使用自增ID值

4、常见索引列建议

出现在SELECT、UPDATE、DELETE语句的WHERE从句中的列包含在ORDER BY、GROUP BY、DISTINCT中的字段并不要将符合1和2中的字段的列都建立一个索引， 通常将1、2中的字段建立联合索引效果更好多表join的关联列

5、如何选择索引列的顺序

建立索引的目的是：希望通过索引进行数据查找，减少随机IO，增加查询性能 ，索引能过滤出越少的数据，则从磁盘中读入的数据也就越少。

区分度最高的放在联合索引的最左侧（区分度=列中不同值的数量/列的总行数）尽量把字段长度小的列放在联合索引的最左侧（因为字段长度越小，一页能存储的数据量越大，IO 性能也就越好）使用最频繁的列放到联合索引的左侧（这样可以比较少的建立一些索引）

6、避免建立冗余索引和重复索引（增加了查询优化器生成执行计划的时间）

重复索引示例：primary key(id)、index(id)、unique index(id)冗余索引示例：index(a,b,c)、index(a,b)、index(a)

7、对于频繁的查询优先考虑使用覆盖索引

覆盖索引：就是包含了所有查询字段(where/select/ordery by/group by包含的字段) 的索引。

覆盖索引的好处：

***\*避免 Innodb 表进行索引的二次查询\****: Innodb是以聚集索引的顺序来存储的，对于 Innodb来说，二级索引在叶子节点中所保存的是行的主键信息，如果是用二级索引查询数据的话，在查找到相应的键值后，还要通过主键进行二次查询才能获取我们真实所需要的数据。而在覆盖索引中，二级索引的键值中可以获取所有的数据，避免了对主键的二次查询 ，减少了IO操作，提升了查询效率。可以把随机IO变成顺序IO加快查询效率: 由于覆盖索引是按键值的顺序存储的，对于IO密集型的范围查找来说，对比随机从磁盘读取每一行的数据IO要少的多，因此利用覆盖索引在访问时也可以把磁盘的随机读取的IO转变成索引查找的顺序IO。

8.索引SET规范

尽量避免使用外键约束

***\*不建议使用外键约束（foreign key）\****，但一定要在表与表之间的关联键上建立索引外键可用于保证数据的参照完整性，但建议在业务端实现外键会影响父表和子表的写操作从而降低性能。

 

## **数据库SQL开发规范**

1、建议使用预编译语句进行数据库操作

预编译语句可以重复使用这些计划，减少 SQL 编译所需要的时间，还可以解决动态 SQL 所带来的 SQL 注入的问题。

只传参数，比传递 SQL 语句更高效。

相同语句可以一次解析，多次使用，提高处理效率。

2、避免数据类型的隐式转换

隐式转换会导致索引失效如:

select name,phone from customer where id = '111';

\3. 充分利用表上已经存在的索引

避免使用双%号的查询条件。如：a like '%123%'，（如果无前置%,只有后置%，是可以用到列上的索引的）

一个SQL只能利用到复合索引中的一列进行范围查询。如：有a,b,c列的联合索引，在查询条件中有a列的范围查询，则在b,c列上的索引将不会被用到。

在定义联合索引时，如果 a 列要用到范围查找的话，就要把 a 列放到联合索引的右侧，使用 left join 或 not exists 来优化 not in 操作，因为 not in 也通常会使用索引失效。

4、数据库设计时，应该要对以后扩展进行考虑

5、程序连接不同的数据库使用不同的账号，进制跨库查询

为数据库迁移和分库分表留出余地降低业务耦合度避免权限过大而产生的安全风险

6、禁止使用 SELECT * 必须使用 SELECT <字段列表> 查询

原因：

消耗更多的CPU和IO以网络带宽资源无法使用覆盖索引可减少表结构变更带来的影响

7、禁止使用不含字段列表的INSERT语句

如：

insert into values ('a','b','c');

应使用：

insert into t(c1,c2,c3) values ('a','b','c');

8、***\*避免使用子查询，可以把子查询优化为join操作\****

通常子查询在in子句中，且子查询中为简单SQL(不包含union、group by、order by、limit 从句) 时,才可以把子查询转化为关联查询进行优化。

子查询性能差的原因：

***\*子查询的结果集无法使用索引，通常子查询的结果集会被存储到临时表中，不论是内存临时表还是磁盘临时表都不会存在索引，所以查询性能会受到一定的影响。\****特别是对于返回结果集比较大的子查询，其对查询性能的影响也就越大。

***\*由于子查询会产生大量的临时表也没有索引，所以会消耗过多的CPU和IO资源，产生大量的慢查询\****。

9、***\*避免使用JOIN关联太多的表\****

对于MySQL来说，是存在关联缓存的，缓存的大小可以由join_buffer_size参数进行设置。

***\*在MySQL中，对于同一个SQL多关联（join）一个表，就会多分配一个关联缓存，如果在一个SQL中关联的表越多，所占用的内存也就越大\****。

如果程序中大量的使用了多表关联的操作，同时join_buffer_size设置的也不合理的情况下，就容易造成服务器内存溢出的情况，就会影响到服务器数据库性能的稳定性。

同时对于关联操作来说，会产生临时表操作，影响查询效率，MySQL最多允许关联61 个表，建议不超过5个。

10、减少同数据库的交互次数

数据库更适合处理批量操作，合并多个相同的操作到一起，可以提高处理效率。

11、***\*对应同一列进行or判断时，使用in代替or\****

in 的值不要超过500个，***\*in操作可以更有效的利用索引，or大多数情况下很少能利用到索引\****。

12、***\*禁止使用 order by rand() 进行随机排序\****

order by rand()会把表中所有符合条件的数据装载到内存中，然后在内存中对所有数据根据随机生成的值进行排序，并且可能会对每一行都生成一个随机值，如果满足条件的数据集非常大，就会消耗大量的CPU和IO及内存资源。

推荐在程序中获取一个随机值，然后从数据库中获取数据的方式。

13、***\*WHERE从句中禁止对列进行函数转换和计算\****

对列进行函数转换或计算时会导致无法使用索引

不推荐：

where date(create_time)='20190101'

推荐：

where create_time >= '20190101' and create_time < '20190102'

\14. 在明显不会有重复值时使用UNION ALL而不是UNION

UNION会把两个结果集的所有数据放到临时表中后再进行去重操作UNION ALL不会再对结果集进行去重操作

15、拆分复杂的大SQL为多个小 SQL

大SQL逻辑上比较复杂，需要占用大量CPU进行计算的 SQLMySQL中，一个SQL只能使用一个CPU进行计算SQL拆分后可以通过并行执行来提高处理效率.

## **数据库操作行为规范**

1、***\*超100万行的批量写 (UPDATE,DELETE,INSERT) 操作,要分批多次进行操作\****

大批量操作可能会造成严重的主从延迟

主从环境中,大批量操作可能会造成严重的主从延迟，大批量的写操作一般都需要执行一定长的时间， 而只有当主库上执行完成后，才会在其他从库上执行，所以会造成主库与从库长时间的延迟情况

binlog日志为row格式时会产生大量的日志

大批量写操作会产生大量日志，特别是对于 row 格式二进制数据而言，由于在 row 格式中会记录每一行数据的修改，我们一次修改的数据越多，产生的日志量也就会越多，日志的传输和恢复所需要的时间也就越长，这也是造成主从延迟的一个原因

避免产生大事务操作

大批量修改数据，一定是在一个事务中进行的，这就会造成表中大批量数据进行锁定，从而导致大量的阻塞，阻塞会对MySQL的性能产生非常大的影响。

特别是长时间的阻塞会占满所有数据库的可用连接，这会使生产环境中的其他应用无法连接到数据库，因此一定要注意大批量写操作要进行分批

***\*注：\*******\*在分布式数据库中需要对结果进行分包处理，就是考虑主从延迟，binlog，大事务的问题。\****

2、对于大表使用pt-online-schema-change修改表结构

避免大表修改产生的主从延迟避免在对表字段进行修改时进行锁表

对大表数据结构的修改一定要谨慎，会造成严重的锁表操作，尤其是生产环境，是不能容忍的。

pt-online-schema-change它会首先建立一个与原表结构相同的新表，并且在新表上进行表结构的修改，然后再把原表中的数据复制到新表中，并在原表中增加一些触发器。把原表中新增的数据也复制到新表中，在行所有数据复制完成之后，把新表命名成原表，并把原来的表删除掉。把原来一个DDL操作，分解成多个小的批次进行。

3、禁止为程序使用的账号赋予super权限

当达到最大连接数限制时，还运行1个有super权限的用户连接super权限只能留给 DBA处理问题的账号使用

4、对于程序连接数据库账号,遵循权限最小原则

程序使用数据库账号只能在一个DB下使用，不准跨库程序使用的账号原则上不准有drop权限

 

# 经验

对MySQL语句性能优化的16条经验

**1、*****\*为查询缓存优化查询\****

大多数的MySQL服务器都开启了查询缓存。这是提高性能最有效的方法之一，而且这是被MySQL引擎处理的。当有很多相同的查询被执行了多次的时候，这些查询结果会被放入一个缓存中，这样后续的相同查询就不用操作而直接访问缓存结果了。

这里最主要的问题是，对于我们程序员来说，这个事情是很容易被忽略的。因为我们某些查询语句会让MySQL不使用缓存，示例如下：

1：SELECT username FROM user WHERE signup_date >= CURDATE()

2：SELECT username FROM user WHERE signup_date >= '2014-06-24‘

上面两条SQL语句的差别就是 CURDATE() ，MySQL的查询缓存对这个函数不起作用。所以，像 NOW() 和 RAND() 或是其它的诸如此类的SQL函数都不会开启查询缓存，因为这些函数的返回是会不定的易变的。所以，你所需要的就是用一个变量来代替MySQL的函数，从而开启缓存。

2、EXPLAIN我们的SELECT查询(可以查看执行的行数)

使用EXPLAIN关键字可以使我们知道MySQL是如何处理SQL语句的，这样可以帮助我们分析我们的查询语句或是表结构的性能瓶颈；EXPLAIN的查询结果还会告诉我们索引主键是如何被利用的，数据表是如何被被搜索或排序的....等等。

**3、*****\*当只要一行数据时使用LIMIT 1\****

加上LIMIT 1可以增加性能。MySQL数据库引擎会在查找到一条数据后停止搜索，而不是继续往后查询下一条符合条件的数据记录。

**4、*****\*为搜索字段建立索引\****

索引不一定就是给主键或者是唯一的字段，如果在表中，有某个字段经常用来做搜索，需要将其建立索引。

**5、*****\*在Join表的时候使用相当类型的列，并将其索引\****

6、千万不要 ORDER BY RAND ()

7、避免SELECT *

从数据库里读出越多的数据，那么查询就会变得越慢。并且，如果我们的数据库服务器和WEB服务器是两台独立的服务器的话，这还会增加网络传输的负载。 所以，我们应该养成一个需要什么就取什么的好的习惯。

Hibernate性能方面就会差，它不用*，但它将整个表的所有字段全查出来。

8、永远为每张表设置一个ID

我们应该为数据库里的每张表都设置一个ID做为其主键，而且最好的是一个INT型的（推荐使用UNSIGNED），并设置上自动增加的 AUTO_INCREMENT标志。 

9、可以使用ENUM而不要VARCHAR

**10、*****\*尽可能的使用NOT NULL\****

如果不是特殊情况，尽可能的不要使用NULL。在MYSQL中对于INT类型而言，EMPTY是0，而NULL是空值。而在Oracle中NULL和EMPTY的字符串是一样的。NULL也需要占用存储空间，并且会使我们的程序判断时更加复杂。现实情况是很复杂的，依然会有些情况下，我们需要使用NULL值。 

**11、*****\*固定长度的表会更快\****

12、垂直分割

13、拆分打的DELETE或INSERT语句

14、越小的列会越快

15、选择正确的存储引擎

16、小心 "永久链接"

“永久链接”的目的是用来减少重新创建MySQL链接的次数。当一个链接被创建了，它会永远处在连接的状态，就算是数据库操作已经结束了。而且，自从我们的Apache开始重用它的子进程后——也就是说，下一次的HTTP请求会重用Apache的子进程，并重用相同的 MySQL 链接。

而且，Apache 运行在极端并行的环境中，会创建很多很多的了进程。这就是为什么这种“永久链接”的机制工作地不好的原因。在我们决定要使用“永久链接”之前，我们需要好好地考虑一下我们的整个系统的架构。

# 运维

关于MySQL的管理维护的其他建议有：

1、通常地，单表物理大小不超过10GB，单表行数不超过1亿条，行平均长度不超过8KB，如果机器性能足够，这些数据量MySQL是完全能处理的过来的，不用担心性能问题，这么建议主要是考虑ONLINE DDL的代价较高；

2、不用太担心mysqld进程占用太多内存，只要不发生OOM kill和用到大量的SWAP都还好；

3、在以往，单机上跑多实例的目的是能最大化利用计算资源，如果单实例已经能耗尽大部分计算资源的话，就没必要再跑多实例了；

4、定期使用pt-duplicate-key-checker检查并删除重复的索引。定期使用pt-index-usage工具检查并删除使用频率很低的索引；

5、定期采集slow query log，用pt-query-digest工具进行分析，可结合Anemometer系统进行slow query管理以便分析slow query并进行后续优化工作；

6、可使用pt-kill杀掉超长时间的SQL请求，Percona版本中有个选项 innodb_kill_idle_transaction 也可实现该功能；

7、使用pt-online-schema-change来完成大表的ONLINE DDL需求；

8、定期使用pt-table-checksum、pt-table-sync来检查并修复mysql主从复制的数据差异；

# 应用

## **SQL执行慢**

 

## **MySQL性能瓶颈/负载高**

### **OS层面**

***\*1、首先我们进行OS层面的检查确认\****

登入服务器后，我们的目的是首先要确认当前到底是哪些进程引起的负载高，以及这些进程卡在什么地方，瓶颈是什么。

通常来说，服务器上最容易成为瓶颈的是磁盘I/O子系统，因为它的读写速度通常是最慢的。即便是现在的PCIe SSD，其随机I/O读写速度也是不如内存来得快。当然了，引起磁盘I/O慢得原因也有多种，需要确认哪种引起的。

第一步，我们一般先看整体负载如何，负载高的话，肯定所有的进程跑起来都慢。

可以执行指令w或者sar -q 1来查看负载数据，例如（横版查看）：

[####@####:~ ]# w

 11:52:58 up 702 days, 56 min,  1 user,  load average: 7.20, 6.70, 6.47

USER   TTY   FROM   LOGIN@  IDLE  JCPU  PCPU WHAT

root   pts/0   1.xx.xx.xx    11:51   0.00s  0.03s  0.00s w

或者sar -q的观察结果（横版查看）：

[####@####:~]# sar -q 1

Linux 2.6.32-431.el6.x86_64 (yejr.imysql.com) 01/13/2016 _x86_64_ (24 CPU)

02:51:18 PM  runq-sz  plist-sz  ldavg-1  ldavg-5  ldavg-15  blocked

02:51:19 PM  4    2305    6.41    6.98    7.12     3

02:51:20 PM  2    2301    6.41    6.98    7.12     4

02:51:21 PM  0    2300    6.41    6.98    7.12     5

02:51:22 PM  6    2301    6.41    6.98    7.12     8

02:51:23 PM  2    2290    6.41    6.98    7.12     8

load average大意表示当前CPU中有多少任务在排队等待，等待越多说明负载越高，跑数据库的服务器上，一般load值超过5的话，已经算是比较高的了。

 

***\*引起load高的原因也可能有多种：\****

1、某些进程/服务消耗更多CPU资源（服务响应更多请求或存在某些应用瓶颈）；

2、发生比较严重的swap（可用物理内存不足）；

3、发生比较严重的中断（因为SSD或网络的原因发生中断）；

4、磁盘I/O比较慢（会导致CPU一直等待磁盘I/O请求）；

 

这时我们可以执行下面的命令来判断到底瓶颈在哪个子系统（横版查看）：

[####@####:~]# top

top - 11:53:04 up 702 days, 56 min,  1 user,  load average: 7.18, 6.70, 6.47

Tasks: 576 total,  1 running, 575 sleeping,  0 stopped,  0 zombie

Cpu(s):  7.7%us,  3.4%sy,  0.0%ni, 77.6%id, 11.0%wa,  0.0%hi,  0.3%si,  0.0%st

Mem:  49374024k total, 32018844k used, 17355180k free,  115416k buffers

Swap: 16777208k total,  117612k used, 16659596k free,  5689020k cached

 PID USER  PR  NI  VIRT  RES  SHR S %CPU %MEM   TIME+  COMMAND

14165 mysql   20  0 8822m 3.1g 4672 S 162.3  6.6  89839:59 mysqld

40610 mysql   20  0 25.6g  14g 8336 S 121.7 31.5 282809:08 mysqld

49023 mysql   20  0 16.9g 5.1g 4772 S  4.6 10.8  34940:09 mysqld

很明显是前面两个mysqld进程导致整体负载较高。

而且，从Cpu(s)这行的统计结果也能看的出来，%us和%wa的值较高，表示当前比较大的瓶颈可能是在用户进程消耗的CPU以及磁盘I/O等待上。

 

我们先分析下磁盘I/O的情况。

执行 sar -d 确认磁盘I/O是否真的较大（横版查看）：

[yejr@imysql.com:~ ]# sar -d 1

Linux 2.6.32-431.el6.x86_64 (yejr.imysql.com) 01/13/2016 _x86_64_ (24 CPU)

11:54:32 AM  dev8-0  5338.00 162784.00  1394.00  30.76  5.24  0.98  0.19  100.00

11:54:33 AM  dev8-0  5134.00 148032.00  32365.00  35.14 6.93 1.34  0.19   100.10

11:54:34 AM dev8-0  5233.00 161376.00  996.00  31.03  9.77  1.88  0.19   100.00

11:54:35 AM  dev8-0  4566.00 139232.00  1166.00  30.75 5.37 1.18  0.22   100.00

11:54:36 AM dev8-0  4665.00 145920.00  630.00 31.41  5.94  1.27  0.21   100.00

11:54:37 AM  dev8-0  4994.00 156544.00  546.00  31.46 7.07  1.42  0.20   100.00

再利用 iotop 确认到底哪些进程消耗的磁盘I/O资源最多（横版查看）：

[####@####:~ ]# iotop

Total DISK READ: 60.38 M/s | Total DISK WRITE: 640.34 K/s

TID  PRIO USER DISK READ DISK WRITE SWAPIN IO>  COMMAND

16397 be/4 mysql    8.92 M/s   0.00 B/s  0.00 % 94.77 % mysqld --basedir=/usr/local/m~og_3320/mysql.sock --port=3320

 7295 be/4 mysql    10.98 M/s   0.00 B/s  0.00 % 93.59 % mysqld --basedir=/usr/local/m~og_3320/mysql.sock --port=3320

14295 be/4 mysql    10.50 M/s   0.00 B/s  0.00 % 93.57 % mysqld --basedir=/usr/local/m~og_3320/mysql.sock --port=3320

14288 be/4 mysql    14.30 M/s   0.00 B/s  0.00 % 91.86 % mysqld --basedir=/usr/local/m~og_3320/mysql.sock --port=3320

14292 be/4 mysql    14.37 M/s   0.00 B/s  0.00 % 91.23 % mysqld --basedir=/usr/local/m~og_3320/mysql.sock --port=3320

可以看到，端口号是3320的实例消耗的磁盘I/O资源比较多，那就看看这个实例里都有什么查询在跑吧。

 

### **MySQL层面**

***\*2、MySQL层面检查确认\****

首先看下当前都有哪些查询在运行（横版查看）：

[####@####:~]> mysqladmin pr|grep -v Sleep

+----+----+----------+----+-------+-----+--------------+-----------------------------------------------------------------------------------------------+

| Id |User| Host   | db |Command|Time | State     | Info                                              |

+----+----+----------+----+-------+-----+--------------+-----------------------------------------------------------------------------------------------+

| 25 | x | 10.x:8519 | db | Query | 68  | Sending data | select max(Fvideoid) from (select Fvideoid from t where Fvideoid>404612 order by Fvideoid) t1 |

| 26 | x | 10.x:8520 | db | Query | 65  | Sending data | select max(Fvideoid) from (select Fvideoid from t where Fvideoid>484915 order by Fvideoid) t1 |

| 28 | x | 10.x:8522 | db | Query | 130 | Sending data | select max(Fvideoid) from (select Fvideoid from t where Fvideoid>404641 order by Fvideoid) t1 |

| 27 | x | 10.x:8521 | db | Query | 167 | Sending data | select max(Fvideoid) from (select Fvideoid from t where Fvideoid>324157 order by Fvideoid) t1 |

| 36 | x | 10.x:8727 | db | Query | 174 | Sending data | select max(Fvideoid) from (select Fvideoid from t where Fvideoid>324346 order by Fvideoid) t1 |

+----+----+----------+----+-------+-----+--------------+-----------------------------------------------------------------------------------------------+

可以看到有不少慢查询还未完成，从slow query log中也能发现，这类SQL发生的频率很高。

这是一个非常低效的SQL写法，导致需要对整个主键进行扫描，但实际上只需要取得一个最大值而已，从slow query log中可看到：

Rows_sent: 1  Rows_examined: 5502460

每次都要扫描500多万行数据，却只为读取一个最大值，效率非常低。

经过分析，这个SQL稍做简单改造即可在个位数毫秒级内完成，原先则是需要150-180秒才能完成，提升了N次方。

改造的方法是：对查询结果做一次倒序排序，取得第一条记录即可。而原先的做法是对结果正序排序，取最后一条记录。

 

### **总结**

在这个例子中，产生瓶颈的原因比较好定位，SQL优化也不难，实际线上环境中，通常有以下几种常见的原因导致负载较高：

一次请求读写的数据量太大，导致磁盘I/O读写值较大，例如一个SQL里要读取或更新几万行数据甚至更多，这种最好是想办法减少一次读写的数据量；

SQL查询中没有适当的索引可以用来完成条件过滤、排序（ORDER BY）、分组（GROUP BY）、数据聚合（MIN/MAX/COUNT/AVG等），添加索引或者进行SQL改写吧；

瞬间突发有大量请求，这种一般只要能扛过峰值就好，保险起见还是要适当提高服务器的配置，万一峰值抗不过去就可能发生雪崩效应；

因为某些定时任务引起的负载升高，比如做数据统计分析和备份，这种对CPU、内存、磁盘I/O消耗都很大，最好放在独立的slave服务器上执行；

服务器自身的节能策略发现负载较低时会让CPU降频，当发现负载升高时再自动升频，但通常不是那么及时，结果导致CPU性能不足，抗不过突发的请求；

使用raid卡的时候，通常配备BBU（cache模块的备用电池），早期一般采用锂电池技术，需要定期充放电（DELL服务器90天一次，IBM是30天），我们可以通过监控在下一次充放电的时间前在业务低谷时提前对其进行放电，不过新一代服务器大多采用电容式电池，也就不存在这个问题了。

文件系统采用ext4甚至ext3，而不是xfs，在高I/O压力时，很可能导致%util已经跑到100%了，但iops却无法再提升，换成xfs一般可获得大幅提升；

内核的io scheduler策略采用cfq而非deadline或noop，可以在线直接调整，也可获得大幅提升。

 

## **千万级大表优化**

### **方案**

**方案一：优化现有MySQL数据库。**

优点：不影响现有业务，源程序不需要修改代码，成本最低。

缺点：有优化瓶颈，数据量过亿就玩完了。

**方案二：升级数据库类型，换一种100%兼容MySQL的数据库。**

优点：不影响现有业务，源程序不需要修改代码，你几乎不需要做任何操作就能提升数据库性能。

缺点：多花钱。

**方案三：一步到位，大数据解决方案，更换newSQL/noSQL数据库。**

优点：没有数据容量瓶颈。

缺点：需要修改源程序代码，影响业务，总成本最高。

以上三种方案，按顺序使用即可，数据量在亿级别一下的没必要换noSQL，开发成本太高。

 

### **优化现有MySQL数据库**

​	优化现有数据库总结如下：

1、数据库设计和表创建时就要考虑性能

2、SQL的编写需要注意优化

3、分区

4、分表

5、分库

#### 数据库设计和表创建时就要考虑性能

MySQL数据库本身高度灵活，造成性能不足，严重依赖开发人员能力。也就是说开发人员能力高，则MySQL性能高。

 

##### 设计表时要注意的东西

表字段避免null值出现，null值很难查询优化且占用额外的索引空间，推荐默认数字0代替null。

尽量使用INT而非BIGINT，如果非负则加上UNSIGNED（这样数值容量会扩大一倍），当然能使用TINYINT、SMALLINT、MEDIUM_INT更好。

使用枚举或整数代替字符串类型

尽量使用TIMESTAMP而非DATETIME

单表不要有太多字段，建议在20以内

用整型来存IP

##### 索引

索引并不是越多越好，要根据查询有针对性的创建，考虑在WHERE和ORDER BY命令上涉及的列建立索引，可根据EXPLAIN来查看是否用了索引还是全表扫描

应尽量避免在WHERE子句中对字段进行NULL值判断，否则将导致引擎放弃使用索引而进行全表扫描

值分布很稀少的字段不适合建索引，例如"性别"这种只有两三个值的字段

字符字段只建前缀索引

字符字段最好不要做主键

不用外键，由程序保证约束

尽量不用UNIQUE，由程序保证约束

使用多列索引时主意顺序和查询条件保持一致，同时删除不必要的单列索引

简言之就是使用合适的数据类型,选择合适的索引

 

**选择合适的数据类型:**

使用可存下数据的最小的数据类型，整型 < date,time < char,varchar < blob

使用简单的数据类型，整型比字符处理开销更小，因为字符串的比较更复杂。如，int类型存储时间类型，bigint类型转ip函数

使用合理的字段属性长度，固定长度的表会更快。使用enum、char而不是varchar

尽可能使用not null定义字段

尽量少用text，非用不可最好分表

 

**选择合适的索引列:**

（1）查询频繁的列，在where，group by，order by，on从句中出现的列

（2）where条件中<，<=，=，>，>=，between，in，以及like 字符串+通配符（%）出现的列

（3）长度小的列，索引字段越小越好，因为数据库的存储单位是页，一页中能存下的数据越多越好

（4）离散度大（不同的值多）的列，放在联合索引前面。查看离散度，通过统计不同的列值来实现，count越大，离散程度越高：

原开发人员已经跑路，该表早已建立，我无法修改，故：该措辞无法执行，放弃！

#### SQL的编写需要注意优化

1、使用limit对查询结果的记录进行限定

2、避免select *，将需要查找的字段列出来

3、使用连接（join）来代替子查询

4、拆分大的delete或insert语句

5、可通过开启慢查询日志来找出较慢的SQL

6、不做列运算：SELECT id WHERE age + 1 = 10，任何对列的操作都将导致表扫描，它包括数据库教程函数、计算表达式等等，查询时要尽可能将操作移至等号右边

7、SQL语句尽可能简单：一条SQL只能在一个cpu运算；大语句拆小语句，减少锁时间；一条大SQL可以堵死整个库

8、OR改写成IN：OR的效率是n级别，IN的效率是log(n)级别，in的个数建议控制在200以内

9、不用函数和触发器，在应用程序实现

10、避免%xxx式查询

11、少用JOIN

12、使用同类型进行比较，比如用'123'和'123'比，123和123比

13、尽量避免在WHERE子句中使用!=或<>操作符，否则将引擎放弃使用索引而进行全表扫描

14、对于连续数值，使用BETWEEN不用IN：SELECT id FROM t WHERE num BETWEEN 1 AND 5

15、列表数据不要拿全表，要使用LIMIT来分页，每页数量也不要太大

 

原开发人员已经跑路，程序已经完成上线，我无法修改SQL，故：该措辞无法执行，放弃！

#### 引擎选择

目前广泛使用的是MyISAM和InnoDB两种引擎：

##### MyISAM

MyISAM引擎是MySQL 5.1及之前版本的默认引擎，它的特点是：

不支持行锁，读取时对需要读到的所有表加锁，写入时则对表加排它锁

不支持事务

不支持外键

不支持崩溃后的安全恢复

在表有读取查询的同时，支持往表中插入新纪录

支持BLOB和TEXT的前500个字符索引，支持全文索引

支持延迟更新索引，极大提升写入性能

对于不会进行修改的表，支持压缩表，极大减少磁盘空间占用

##### InnoDB

InnoDB在MySQL 5.5后成为默认索引，它的特点是：

支持行锁，采用MVCC来支持高并发

支持事务

支持外键

支持崩溃后的安全恢复

不支持全文索引

总体来讲，MyISAM适合SELECT密集型的表，而InnoDB适合INSERT和UPDATE密集型的表。

MyISAM速度可能超快，占用存储空间也小，但是程序要求事务支持，故InnoDB是必须的，故该方案无法执行，放弃！

#### 分区

MySQL在5.1版引入的分区是一种简单的水平拆分，用户需要在建表的时候加上分区参数，对应用是透明的无需修改代码

对用户来说，分区表是一个独立的逻辑表，但是底层由多个物理子表组成，实现分区的代码实际上是通过对一组底层表的对象封装，但对SQL层来说是一个完全封装底层的黑盒子。MySQL实现分区的方式也意味着索引也是按照分区的子表定义，没有全局索引

用户的SQL语句是需要针对分区表做优化，SQL条件中要带上分区条件的列，从而使查询定位到少量的分区上，否则就会扫描全部分区，可以通过EXPLAIN PARTITIONS来查看某条SQL语句会落在那些分区上，从而进行SQL优化，我测试，查询时不带分区条件的列，也会提高速度，故该措施值得一试。

 

##### 分区的好处

可以让单表存储更多的数据

分区表的数据更容易维护，可以通过清楚整个分区批量删除大量数据，也可以增加新的分区来支持新插入的数据。另外，还可以对一个独立分区进行优化、检查、修复等操作

部分查询能够从查询条件确定只落在少数分区上，速度会很快

分区表的数据还可以分布在不同的物理设备上，从而搞笑利用多个硬件设备

可以使用分区表赖避免某些特殊瓶颈，例如InnoDB单个索引的互斥访问、ext3文件系统的inode锁竞争

可以备份和恢复单个分区

##### 分区的限制和缺点

一个表最多只能有1024个分区

如果分区字段中有主键或者唯一索引的列，那么所有主键列和唯一索引列都必须包含进来

分区表无法使用外键约束

NULL值会使分区过滤无效

所有分区必须使用相同的存储引擎

##### 分区的类型

RANGE分区：基于属于一个给定连续区间的列值，把多行分配给分区

LIST分区：类似于按RANGE分区，区别在于LIST分区是基于列值匹配一个离散值集合中的某个值来进行选择

HASH分区：基于用户定义的表达式的返回值来进行选择的分区，该表达式使用将要插入到表中的这些行的列值进行计算。这个函数可以包含MySQL中有效的、产生非负整数值的任何表达式

KEY分区：类似于按HASH分区，区别在于KEY分区只支持计算一列或多列，且MySQL服务器提供其自身的哈希函数。必须有一列或多列包含整数值

具体关于MySQL分区的概念请自行google或查询官方文档，我这里只是抛砖引玉了。

我首先根据月份把上网记录表RANGE分区了12份，查询效率提高6倍左右，效果不明显，故：换id为HASH分区，分了64个分区，查询速度提升显著。问题解决！

结果如下：PARTITION BY HASH (id)PARTITIONS 64

select count(*) from readroom_website; --11901336行记录

/* 受影响行数: 0  已找到记录: 1  警告: 0  持续时间 1 查询: 5.734 sec. */  

select * from readroom_website where month(accesstime) =11 limit 10;

/* 受影响行数: 0  已找到记录: 10  警告: 0  持续时间 1 查询: 0.719 sec. */

#### 分表

分表就是把一张大表，按照如上过程都优化了，还是查询卡死，那就把这个表分成多张表，把一次查询分成多次查询，然后把结果组合返回给用户。

分表分为垂直拆分和水平拆分，通常以某个字段做拆分项。比如以id字段拆分为100张表： 表名为 tableName_id%100

但是，分表需要修改源程序代码，会给开发带来大量工作，极大的增加了开发成本，因此，只适合在开发初期就考虑到了大量数据存在，做好了分表处理，不适合应用上线了再做修改，成本太高！而且选择这个方案，都不如选择我提供的第二第三个方案的成本低！故不建议采用。

 

#### 分库

把一个数据库分成多个，建议做个读写分离就行了，真正的做分库也会带来大量的开发成本，得不偿失！不推荐使用。

 

### **升级数据库**

升级数据库：换一个100%兼容MySQL的数据库

MySQL性能不行，为保证源程序代码不修改，保证现有业务平稳迁移，故需要换一个100%兼容MySQL的数据库。

#### 开源选择

tiDB pingcap/tidb

Cubrid Open Source Database With Enterprise Features

开源数据库会带来大量的运维成本且其工业品质和MySQL尚有差距，有很多坑要踩，如果你公司要求必须自建数据库，那么选择该类型产品。

 

#### 云数据选择

##### 阿里云POLARDB

POLARDB是阿里云自研的下一代关系型分布式云原生数据库，100%兼容MySQL，存储容量最高可达100T，性能最高提升至MySQL的6倍。POLARDB 既融合了商业数据库稳定、可靠、高性能的特征，又具有开源数据库简单、可扩展、持续迭代的优势，而成本只需商用数据库的1/10。

支持免费MySQL的数据迁移，无操作成本，性能提升在10倍左右，价格跟rds相差不多，是个很好的备选解决方案！

 

##### 阿里云OcenanBase

淘宝使用的，扛得住双十一，性能卓著，在公测中。

 

##### 阿里云HybridDB for MySQL 

云数据库HybridDB for MySQL（原名PetaData）是同时支持海量数据在线事务（OLTP）和在线分析（OLAP）的HTAP（Hybrid Transaction/Analytical Processing）关系型数据库。

是一个olap和oltp兼容的解决方案，但是价格太高，每小时高达10块钱，用来做存储太浪费了，适合存储和分析一起用的业务。

 

##### 腾讯云DCDB

DCDB又名TDSQL，一种兼容MySQL协议和语法，支持自动水平拆分的高性能分布式数据库——即业务显示为完整的逻辑表，数据却均匀的拆分到多个分片中；每个分片默认采用主备架构，提供灾备、恢复、监控、不停机扩容等全套解决方案，适用于TB或PB级的海量数据场景。

 

### **去掉MySQL,换大数据引擎处理数据**

数据量过亿了，就无法采用上述方案了，只能上大数据了。

#### 开源解决方案

Hadoop家族：HBase/Hive，但是有很高的运维成本。

#### 云解决方案

这个可选方案比较多，也是一种未来趋势，大数据由专业的公司提供专业的服务，小公司或个人购买服务，大数据就像水/电等公共设施一样，存在于社会的方方面面。

国内做的最好的当属阿里云,阿里云的MaxCompute配合DataWorks，使用超级舒服，按量付费，成本极低。

MaxCompute可以理解为开源的Hive，提供SQL/MapReduce/AI算法/python脚本/shell脚本等方式操作数据，数据以表格的形式展现，以分布式方式存储，采用定时任务和批处理的方式处理数据。DataWorks提供了一种工作流的方式管理你的数据处理任务和调度监控。

当然你也可以选择阿里云HBase等其他产品，这里主要是离线处理，故选择MaxCompute，基本都是图形界面操作，大概写了300行SQL，费用不超过100块钱就解决了数据处理问题。

 

# 分布式数据库实践

## **TDSQL**

## **OceanBase**

## **TiDB**

## **GoldenDB**