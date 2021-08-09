# 8.0新特性

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9986.tmp.jpg) 

# 账户与安全

## **用户创建和授权**

​	MySQL8.0用户创建和授权分开执行：

​	create user ‘tony’@’%’ identified by ‘Tony@2018’

​	grant all privileges on *.* to ‘tony’@’%’;

​	MySQL5.7用户创建和授权可以使用grant语句一次性完成：

​	grant all privileges on *.* to ‘tony’@’%’ identified by ‘passwd’;

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9987.tmp.jpg) 

## **认证插件更新**

​	MySQL8.0中默认的身份认证插件是caching_sha2_password，替代了之前的mysql_native_password。

​	用户可以通过defalt_authentication_plugin或mysql.user表看到这种变化：

​	MySQL5.7：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9998.tmp.jpg) 

​	MySQL8.0：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9999.tmp.jpg) 

​	注：如果服务端mysql升级了，但是客户端没有升级，可能使用新的插件会导致连接错误。可以修改系统配置文件修改为原来的认证。

## **密码管理**

​	MySQL8.0开始允许限制重复使用以前的密码：

​	password_history=3	//新密码不能和最近3次使用的相同

​	password_reuse_internal=90	//新密码不能和最近90天使用的密码相同

​	password_require_current=ON	//修改密码时需要提供当前密码

## **角色管理**

​	MySQL8.0提供了角色管理的新功能，角色是一组权限的集合。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps999A.tmp.jpg) 

在MySQL 8.0数据库中，角色可以看成是一些权限的集合，为用户赋予统一的角色，权限的修改直接通过角色来进行，无须为每个用户单独授权。

操作步骤：

创建角色，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps999B.tmp.jpg) 

给角色授予权限，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99AC.tmp.jpg) 

创建用户myuser1，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99AD.tmp.jpg) 

为用户myuser1赋予角色role_tt，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99AE.tmp.jpg) 

给角色role_tt增加insert权限，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99AF.tmp.jpg) 

给角色role_tt删除insert权限，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99BF.tmp.jpg) 

查看默认角色信息，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99C0.tmp.jpg) 

查看角色与用户关系，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99C1.tmp.jpg) 

删除角色，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99C2.tmp.jpg) 

 

# 全局变量持久化

在MySQL数据库中，全局变量可以通过SET GLOBAL语句来设置。例如，设置服务器语句超时的限制，可以通过设置系统变量max_execution_time来实现：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99C3.tmp.jpg) 

使用SET GLOBAL语句设置的变量值只会临时生效。数据库重启后，服务器又会从MySQL配置文件中读取变量的默认值。

MySQL 8.0版本新增了SET PERSIST命令。例如，设置服务器的最大连接数为1000：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99D4.tmp.jpg) 

MySQL会将该命令的配置保存到数据目录下的mysqld-auto.cnf文件中，下次启动时会读取该文件，用其中的配置来覆盖默认的配置文件。

# Atomic DDL

参考：

https://mp.weixin.qq.com/s/yym9E9gkrxqflL5dOTU6BA

在MySQL-8.0之前DDL是不能做到Crash Safe的。主要的问题有3个:

1、Server层的Metadata和InnoDB的Metadata/数据不一致。

Server层的Metadata是记录在文件中的。例如表结构信息保存在frm文件中。InnoDB中也有一份Metadata存储在表中。Crash可能导致server层的Metadata和InnoDB中Metadata、甚至表中的数据不一致。比如Server层存在表的frm文件，认为表存在。但是InnoDB的ibd数据文件不存在了，InnoDB认为表不存在。

2、InnoDB的Metadata和数据不一致。

3、Binlog和数据的不一致

比如Crash重启后，表已经存在了但是CREATE TABLE却没有记录到Binlog中。

MySQL-8.0中为了实现Atomic DDL，彻底解决以上的问题做了三个方面的改动：

1、去掉Server层的Metadata文件，所有Metadata统一存放到InnoDB表中。

这些表被称为Data Dictionary。用户、Server层、引擎都可以通过DD的访问接口查询或者更新Metadata。

2、DDL_log

InnoDBL中实现了DDL_log表，在DDL操作过程中会记录一些DDL的操作日志。InnoDB通过DDL log来保证DDL中的文件操作和Metadata操作的原子性。

3、Binlog DDL Crash Safe

在DDL的Binlog Event中会记录DDL操作的XID，通过XID来保证binlog的crash safe。

 

# 检查约束

参考：https://mp.weixin.qq.com/s/9nEDzURmOh5OytCssGO7sA

 

检查约束就是在INSERT或UPDATE操作之前，会根据指定条件CHECK要INSERT或UPDATE的字段值是否满足约束。

MySQL在8.0.16之后支持check constraint作为新特性，语法为：

CREATE TABLE t1( c1 INT CHECK (c1 > 10), c2 INT CHECK (c2 < 100) );

在早期版本中，该语法依旧支持，但不会起作用，也就是会被解析，但不会被存储层引用。

 

# 临时表

在MySQL 8.0中，用户可以把数据库和表归组到逻辑和物理表空间中，这样做可以提高资源的利用率。

MySQL 8.0使用CREATE TABLESPACE语句来创建一个通用表空间。这个功能可以让用户自由地选择表和表空间之间的映射。例如，创建表空间和设置这个表空间应该含有什么样的表。这也让在同一个表空间的用户对所有的表分组，因此在文件系统一个单独的文件内持有他们所有的数据，同时为通用表空间实现了元数据锁。

优化普通SQL临时表性能是MySQL 8.0的目标之一。首先，通过优化临时表在磁盘中的不必要步骤，使得临时表的创建和移除成为一个轻量级的操作。将临时表移动到一个单独的表空间中，恢复临时表的过程就变得非常简单，就是在启动时重新创建临时表的单一过程。

MySQL 8.0去掉了临时表中不必要的持久化。临时表仅仅在连接和会话内被创建，然后通过服务的生命周期绑定它们。通过移除不必要的UNDO和REDO日志，改变缓冲和锁，从而为临时表做了优化操作。

MySQL 8.0增加了UNDO日志一个额外的类型，这个类型的日志被保存在一个单独的临时表空间中，在恢复期间不会被调用，而是在回滚操作中才会被调用。

MySQL 8.0为临时表设定了一个特别类型，称之为“内在临时表”。内在临时表和普通临时表很像，只是内在临时表使用宽松的ACID和MVCC语义。

MYSQL 8.0为了提高临时表相关的性能，对临时表相关的部分进行了大幅修改，包括引入新的临时表空间（ibtmp1）；对于临时表的DDL，不持久化相关表定义；对于临时表的DML，不写redo、关闭change buffer等。

InnoDB临时表元数据不再存储于InnoDB系统表，而是存储在INNODB_TEMP_TABLE_INFO中，包含所有用户和系统创建的临时表信息。该表在第一次运行select时被创建，下面举例说明。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99D5.tmp.jpg) 

MySQL 8.0使用了独立的临时表空间来存储临时表数据，但不能是压缩表。临时表空间在实例启动的时候进行创建、shutdown的时候进行删除，即为所有非压缩的innodb临时表提供一个独立的表空间。默认的临时表空间文件为ibtmp1，位于数据目录中。通过innodb_temp_data_file_path参数可指定临时表空间的路径和大小，默认为12MB。只有重启实例才能回收临时表空间文件ibtmp1的大小。create temporary table和using temporary table将共用这个临时表空间。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99D6.tmp.jpg) 

在MySQL 8.0中，临时表在连接断开或者数据库实例关闭的时候会进行删除，从而提高了性能。只有临时表的元数据使用了redo保护，保护元数据的完整性，以便异常启动后进行清理工作。

临时表的元数据在MySQL 8.0之后使用了一个独立的表（innodb_temp_table_info）进行保存，不用使用redo保护，元数据也只保存在内存中。但这有一个前提，即必须使用共享的临时表空间，如果使用file-per-table，仍然需要持久化元数据，以便异常恢复清理。临时表需要undo log，用于MySQL运行时的回滚。

在MySQL 8.0中，新增一个系统选项internal_tmp_disk_storage_engine，可定义磁盘临时表的引擎类型，默认为InnoDB，可选MyISAM。在这以前，只能使用MyISAM。在MySQL 5.6.3以后新增的参数default_tmp_storage_engine是控制create temporary table创建的临时表存储引擎，在以前默认是MEMORY。

查看结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99E7.tmp.jpg) 

 

# 日志

在MySQL 8.0版本中，日志分类将更加详细。例如，在错误信息中添加了错误信息编号[MY-010311]和错误所属子系统[Server]。在MySQL 5.7版本中，部分错误日志如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99E8.tmp.jpg) 

在MySQL 8.0版本中，部分错误日志如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99E9.tmp.jpg) 

# 缓存

## **取消Query cache**

MySQL之前有一个查询缓存Query Cache,从8.0开始，不再使用这个查询缓存。

MySQL查询缓存是查询结果缓存。它将以SEL开头的查询与哈希表进行比较，如果匹配，则返回上一次查询的结果。进行匹配时，查询必须逐字节匹配，例如SELECT * FROM t1; 不等于select * from t1;，此外，一些不确定的查询结果无法被缓存，任何对表的修改都会导致这些表的所有缓存无效。因此，适用于查询缓存的最理想的方案是只读，特别是需要检查数百万行后仅返回数行的复杂查询。如果你的查询符合这样一个特点，开启查询缓存会提升你的查询性能。

随着技术的进步，经过时间的考验，MySQL的工程团队发现启用缓存的好处并不多。

首先，查询缓存的效果取决于缓存的命中率，只有命中缓存的查询效果才能有改善，因此无法预测其性能。

其次，查询缓存的另一个大问题是它受到单个互斥锁的保护。在具有多个内核的服务器上，大量查询会导致大量的互斥锁争用。

 

通过基准测试发现，大多数工作负载最好禁用查询缓存（5.6的默认设置）：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99EA.tmp.jpg) 

如果你认为会从查询缓存中获得好处，请按照实际情况进行测试。

数据写的越多，好处越少

缓冲池中容纳的数据越多，好处越少

查询越复杂，扫描范围越大，则越受益

MySQL8.0取消查询缓存的另外一个原因是，研究表明，缓存越靠近客户端，获得的好处越大。关于这份研究请参考：	https://proxysql.com/blog/scaling-with-proxysql-query-cache/

除此之外，MySQL8.0新增加了对性能干预的工具，例如，现在可以利用查询重写插件，在不更改应用程序的同时，插入优化器提示语句。另外，还有像ProxySQL这样的第三方工具，它们可以充当中间缓存。

综合以上原因，MySQL8.0不再提供对查询缓存的支持，如果用户从5.7版本升级至8.0，考虑使用查询重写或其他缓存。

 

# 优化器索引

## **隐藏索引**

​	MySQL8.0开始支持隐藏索引（invisible index），不可见索引。

​	隐藏索引不会被优化器使用，但仍然需要进行维护。

​	应用场景：软删除、灰度发布。

​	软删除：比如我们需要测试不使用某个索引对系统性能的影响，以前的方案是删除当前索引，如果有影响，需要这个索引就必须重新建立，这样维护成本太高了。在8.0版本可以将这个待测试的索引设置为隐藏索引，这样实际查询中就不会使用这个索引了，测试后如果真需要删除再执行删除索引操作。

## **降序索引**

​	在MySQL 8.0之前，MySQL在语法上已经支持降序索引，但实际上创建的仍然是升序索引。MySQL8.0开始真正支持降序索引（descending index）。

​	只有InnoDB存储引擎支持降序索引，只支持BTREE降序索引。

​	MySQL8.0不再对GROUP BY操作进行隐式排序。

​	MySQL5.7：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99FA.tmp.jpg) 

​	MySQL8.0：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99FB.tmp.jpg) 

 

分别在MySQL 5.7版本和MySQL 8.0版本中创建数据表ts1，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99FC.tmp.jpg) 

在MySQL 5.7版本中查看数据表ts1的结构，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps99FD.tmp.jpg) 

从结果可以看出，索引仍然是默认的升序。

在MySQL 8.0版本中查看数据表ts1的结构，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A0E.tmp.jpg) 

从结果可以看出，索引已经是降序了。下面继续测试降序索引在执行计划中的表现。

分别在MySQL 5.7版本和MySQL 8.0版本中的数据表ts1中插入8万条随机数据，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A0F.tmp.jpg) 

在MySQL 5.7版本中查看数据表ts1的执行计划，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A10.tmp.jpg) 

从结果可以看出，执行计划中扫描数为79999，而且使用了Using filesort。

***\*提示\*******\*：\****Using filesort是MySQL里一种速度比较慢的外部排序，如果能避免是最好的结果。多数情况下，管理员可以通过优化索引来尽量避免出现Using filesort，从而提高数据库执行速度。

在MySQL 8.0版本中查看数据表ts1的执行计划，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A11.tmp.jpg) 

从结果可以看出，执行计划中扫描数为5，而且没有使用Using filesort。

***\*注意\*******\*：\****降序索引只是对查询中特定的排序顺序有效，如果使用不当，反而查询效率更低。例如上述查询排序条件改为“order by a desc, b desc”，MySQL 5.7的执行计划要明显好于MySQL 8.0。

将排序条件修改为“order by a desc, b desc”后，下面来对比不同版本中执行计划的效果。在MySQL 5.7版本中查看数据表ts1的执行计划，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A12.tmp.jpg) 

在MySQL 8.0版本中查看数据表ts1的执行计划，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A22.tmp.jpg) 

从结果可以看出，修改后MySQL 5.7的执行计划要明显好于MySQL 8.0。

## **函数索引**

​	MySQL8.0.13开始支持在索引中使用函数（表达式）的值。

​	支持降序索引，支持JSON数据的索引。

​	函数索引基于虚拟列功能实现。

参考：https://blog.csdn.net/horses/article/details/85059678

 

## **直方图**

MySQL 8.0实现了统计直方图。利用直方图，用户可以对一张表的一列做数据分布的统计，特别是针对没有索引的字段。这可以帮助查询优化器找到更优的执行计划。

在数据库中，查询优化器负责将SQL转换成最有效的执行计划。有时候，查询优化器会找不到最优的执行计划，导致花费了更多不必要的时间。造成这种情况的主要原因是，查询优化器有时无法准确地知道以下几个问题的答案：

●　每个表有多少行？

●　每一列有多少不同的值？

●　每一列的数据分布情况如何？

例如，销售表production包括id、tm、count三个字段，分别表示编号、销售时间和销售数量。对比以下两个查询语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A23.tmp.jpg) 

如果销售时间大部分集中在上午8点到12点，在查询销售情况时，第一个查询语句耗费的时间会远远大于第二个查询语句。

因为没有统计数据，优化器会假设tm的值是均匀分配的。如何才能使查询优化器知道数据的分布情况呢？一个解决方法就是在列上建立统计直方图。

直方图能近似获得一列的数据分布情况，从而让数据库知道它含有哪些数据。直方图有多种形式，MySQL支持了两种：等宽直方图（singleton）和等高直方图（equi-height）。直方图的共同点是，它们都将数据分到了一系列的buckets中去。MySQL会自动将数据划到不同的buckets中，也会自动决定创建哪种类型的直方图。

创建直方图的语法格式如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A24.tmp.jpg) 

buckets的默认值是100。统计直方图的信息存储在数据字典表"column_statistcs"中，可以通过视图information_schema.COLUMN_STATISTICS访问。直方图以灵活的JSON格式存储。ANALYZETABLE会基于表大小自动判断是否要进行取样操作。ANALYZE TABLE也会基于表中列的数据分布情况以及bucket的数量来决定是否要建立等宽直方图（singleton）还是等高直方图（equi-height）。

创建用于测试的数据表production，语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A25.tmp.jpg) 

在数据表production的字段tm上创建直方图，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A36.tmp.jpg) 

buckets的值必须指定，可以设置为1到1024，默认值是100。设置buckets值时，可以先设置低一些，如果没有满足需求，可以再往上增大。

对于不同的数据集合，buckets的值取决于以下几个因素：

●　这列有多少不同的值。

●　数据的分布情况。

●　需要多高的准确性。

在数据表production的字段tm和字段count上创建直方图，执行语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A37.tmp.jpg) 

再次创建直方图时，将会将上一个直方图重写。

如果需要删除已经创建的直方图，用DROP HISTOGRAM就可以实现：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A38.tmp.jpg) 

直方图统计了表中某些字段的数据分布情况，为优化选择高效的执行计划提供参考。直方图与索引有着本质的区别：维护一个索引有代价，每一次的INSERT、UPDATE、DELETE都会需要更新索引，会对性能有一定的影响；而直方图一次创建永不更新，除非明确去更新它，所以不会影响INSERT、UPDATE、DELETE的性能。

建立直方图的时候，MySQL服务器会将所有数据读到内存中，然后在内存中进行操作，包括排序。如果对一个很大的表建立直方图，可能会需要将几百兆的数据都读到内存中。为了规避这种风险，MySQL会根据给定的histogram_generation_max_mem_size的值计算该将多少行数据读到内存中。

设置histogram_generation_max_mem_size值的方法如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A39.tmp.jpg) 

# 锁

## **锁读取**

参考：https://blog.51cto.com/fengfeng688/2147171

MySQL8.0 InnoDB支持 NOWAIT和SKIP LOCKED选项SELECT ... FOR SHARE以及SELECT ... FOR UPDATE锁定读取语句。 NOWAIT如果请求的行被另一个事务锁定，则会立即返回该语句。SKIP LOCKED从结果集中删除锁定的行。

## **共享锁FOR SHARE**

FOR SHARE 语法是 MySQL 8.0 时加入的，FOR SHARE 和 LOCK IN SHARE MODE 是等价的，但，FOR SHARE 用于替代 LOCK IN SHARE MODE，不过，为了向后兼容，LOCK IN SHARE MODE依然可用。

 

## **行锁观测方式**

参考：https://mp.weixin.qq.com/s/w7OovGTZe6ypw6obKtZaMg

MySQL5.7及之前，可以通过information_schema.innodb_locks查看事务的锁情况，但，只能看到阻塞事务的锁；如果事务并未被阻塞，则在该表中看不到该事务的锁情况。

 

MySQL8.0删除了information_schema.innodb_locks，添加了performance_schema.data_locks，可以通过performance_schema.data_locks查看事务的锁情况，和MySQL5.7及之前不同，performance_schema.data_locks不但可以看到阻塞该事务的锁，还可以看到该事务所持有的锁，也就是说即使事务并未被阻塞，依然可以看到事务所持有的锁（不过，正如文中最后一段所说，performance_schema.data_locks并不总是能看到全部的锁）。表名的变化其实还反映了8.0的performance_schema.data_locks更为通用了，即使你使用InnoDB之外的存储引擎，你依然可以从performance_schema.data_locks看到事务的锁情况。

 

全新的MySQL 8.0新增了全新的锁观测方式，在performance_schema下新增了data_locks表和data_lock_waits表。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A3A.tmp.jpg) 

### **data_locks表**

mysql> show create table data_locks\G
*************************** 1. row ***************************
    Table: data_locks
Create Table: CREATE TABLE `data_locks` (
 `ENGINE` varchar(32) NOT NULL,
 `ENGINE_LOCK_ID` varchar(128) NOT NULL,
 `ENGINE_TRANSACTION_ID` bigint unsigned DEFAULT NULL,
 `THREAD_ID` bigint unsigned DEFAULT NULL,
 `EVENT_ID` bigint unsigned DEFAULT NULL,
 `OBJECT_SCHEMA` varchar(64) DEFAULT NULL,
 `OBJECT_NAME` varchar(64) DEFAULT NULL,
 `PARTITION_NAME` varchar(64) DEFAULT NULL,
 `SUBPARTITION_NAME` varchar(64) DEFAULT NULL,
 `INDEX_NAME` varchar(64) DEFAULT NULL,
 `OBJECT_INSTANCE_BEGIN` bigint unsigned NOT NULL,
 `LOCK_TYPE` varchar(32) NOT NULL,
 `LOCK_MODE` varchar(32) NOT NULL,
 `LOCK_STATUS` varchar(32) NOT NULL,
 `LOCK_DATA` varchar(8192) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
 PRIMARY KEY (`ENGINE_LOCK_ID`,`ENGINE`),
 KEY `ENGINE_TRANSACTION_ID` (`ENGINE_TRANSACTION_ID`,`ENGINE`),
 KEY `THREAD_ID` (`THREAD_ID`,`EVENT_ID`),
 KEY `OBJECT_SCHEMA` (`OBJECT_SCHEMA`,`OBJECT_NAME`,`PARTITION_NAME`,`SUBPARTITION_NAME`)
) ENGINE=PERFORMANCE_SCHEMA DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
1 row in set (0.00 sec)

说明：

ENGINE：持有或请求锁定的存储引擎

ENGINE_LOCK_ID：存储引擎持有或请求的锁的ID，锁ID格式是内部的，随时可能更改。

ENGINE_TRANSACTION_ID：请求锁定的事务存储引擎内部ID，可以将其视为锁的所有者

THREAD_ID：对应事务的线程ID，如果需要获取更详细的信息，需要关联threads表的THREAD_ID

EVENT_ID：指明造成锁的EVENT_ID，THREAD_ID+EVENT_ID对应parent EVENT，可以在以下几张表内获得信息

events_waits_xx表查看等待事件

events_stages_xxx查看到了哪个阶段

events_statements_xx表查看对应的SQL语句

events_transactions_current对应查看事务信息

OBJECT_SCHEMA：对应锁表的schema名称

OBJECT_NAME：对应锁的表名

PARTITION_NAME：对应锁的分区名

SUBPARTITION_NAME：对应锁的子分区名

INDEX_NAME：锁对应的索引名称，InnoDB表不会为NULL

OBJECT_INSTANCE_BEGIN：锁对应的内存地址

LOCK_TYPE：对应的锁类型，对InnoDB而言，可为表锁或者行锁

LOCK_MODE：锁模式，对应值可能为S[,GAP], X[, GAP], IS[,GAP], IX[,GAP], AUTO_INC和UNKNOWN

LOCK_STATUS：锁状态，可能为GRANTED或者WAITING

LOCK_DATA：锁对应的数据，例如如果锁定的是主键，那么该列对应的就是加锁的主键值

 

### **data_lock_waits表**

mysql> show create table data_lock_waits\G
*************************** 1. row ***************************
    Table: data_lock_waits
Create Table: CREATE TABLE `data_lock_waits` (
 `ENGINE` varchar(32) NOT NULL,
 `REQUESTING_ENGINE_LOCK_ID` varchar(128) NOT NULL,
 `REQUESTING_ENGINE_TRANSACTION_ID` bigint unsigned DEFAULT NULL,
 `REQUESTING_THREAD_ID` bigint unsigned DEFAULT NULL,
 `REQUESTING_EVENT_ID` bigint unsigned DEFAULT NULL,
 `REQUESTING_OBJECT_INSTANCE_BEGIN` bigint unsigned NOT NULL,
 `BLOCKING_ENGINE_LOCK_ID` varchar(128) NOT NULL,
 `BLOCKING_ENGINE_TRANSACTION_ID` bigint unsigned DEFAULT NULL,
 `BLOCKING_THREAD_ID` bigint unsigned DEFAULT NULL,
 `BLOCKING_EVENT_ID` bigint unsigned DEFAULT NULL,
 `BLOCKING_OBJECT_INSTANCE_BEGIN` bigint unsigned NOT NULL,
 KEY `REQUESTING_ENGINE_LOCK_ID` (`REQUESTING_ENGINE_LOCK_ID`,`ENGINE`),
 KEY `BLOCKING_ENGINE_LOCK_ID` (`BLOCKING_ENGINE_LOCK_ID`,`ENGINE`),
 KEY `REQUESTING_ENGINE_TRANSACTION_ID` (`REQUESTING_ENGINE_TRANSACTION_ID`,`ENGINE`),
 KEY `BLOCKING_ENGINE_TRANSACTION_ID` (`BLOCKING_ENGINE_TRANSACTION_ID`,`ENGINE`),
 KEY `REQUESTING_THREAD_ID` (`REQUESTING_THREAD_ID`,`REQUESTING_EVENT_ID`),
 KEY `BLOCKING_THREAD_ID` (`BLOCKING_THREAD_ID`,`BLOCKING_EVENT_ID`)
) ENGINE=PERFORMANCE_SCHEMA DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
1 row in set (0.00 sec)

***\*说明：\****

ENGINE：请求的锁的引擎

REQUESTING_ENGINE_LOCK_ID：请求的锁在存储引擎中的锁ID

REQUESTING_ENGINE_TRANSACTION_ID：请求锁的事务对应的事务ID

REQUESTING_THREAD_ID：请求锁的线程ID

REQUESTING_EVENT_ID：请求锁的EVENT ID

REQUESTING_OBJECT_INSTANCE_BEGIN：请求的锁的内存地址

BLOCKING_ENGINE_LOCK_ID：阻塞的锁的ID，对应data_locks表的ENGINE_LOCK_ID列

BLOCKING_ENGINE_TRANSACTION_ID：锁阻塞的事务ID

BLOCKING_THREAD_ID：锁阻塞的线程ID

BLOCKING_EVENT_ID：锁阻塞的EVENT ID

BLOCKING_OBJECT_INSTANCE_BEGIN：阻塞的锁内存地址

实例：

***\*主键|Lock_X\****

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A4B.tmp.jpg) 

查看data_locks表和data_lock_waits表

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A4C.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A5C.tmp.jpg) 

分析：

从data_locks表可以看出，线程ID为444的会话持有了xucl.t1表的id=1的主键排他记录锁，和表级别的IX锁

结合data_lock_waits表可以看出，线程ID为445的会话等待xucl.t1表的主键上的排他记录锁

***\*二级索引|next-key lock\****

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A5D.tmp.jpg) 

查看data_locks表和data_lock_waits表

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A5E.tmp.jpg) 

***\*分析：\****

1、从data_locks表可以看到，线程ID为444的会话持有的锁有

xucl.t1表上的IX锁

表xucl.t1索引idx_c1上的'd',4这条记录的next-key lock（这里LOCK_MODE只显示了X表示这是next-key lock）

表xucl.t1索引idx_c1上的'e',5这条记录的GAP lock（二级索引等值条件需要扫描到第一条不满足的记录，转换成GAP Lock）

表xucl.t1索引主键索引上id=4这条记录的record Lock，类型为排他

2、结合data_lock_waits表，可以看出

等待的锁为xucl.t1表上索引idx_c1上的'd',4这条记录的next-key lock，类型为排他类型

 

***\*总结：\****

区别于之前的通过innodb_lock_waits的方式，即便没有产生锁等待，data_locks也能显示出已经加锁的行，另外隐式锁能够显示，这对于DBA分析锁来说无疑是非常有帮助的。

 

# 通用表达式

​	MySQL8.0开始支持通用表表达式（CTE），即WITH子句。

通用表表达式简称为CTE（Common Table Expressions）。CTE是命名的临时结果集，作用范围是当前语句。CTE可以理解成一个可以复用的子查询，当然跟子查询还是有点区别的，CTE可以引用其他CTE，但子查询不能引用其他子查询。

CTE的语法格式如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A5F.tmp.jpg) 

使用WITH语句创建CTE的情况如下：

（1）SELECT、UPDATE、DELETE语句的开头：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A60.tmp.jpg) 

（2）在子查询的开头：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A71.tmp.jpg) 

（3）紧接SELECT，在包含SELECT声明的语句之前：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A72.tmp.jpg) 

## **非递归CTE**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A73.tmp.jpg) 

​	注：CTE更加清晰，后面可以任意实用前面定义的CTE表达式，更加方便，类似增加了一个编程功能的扩展。

## **递归CTE**

WITH子句必须以WITH RECURSIVE开头。CTE递归子查询包括两部分：seed查询和recursive查询，中间由union [all]或union distinct分隔。seed查询会被执行一次，以创建初始数据子集。recursive查询会被重复执行以返回数据子集，直到获得完整结果集。当迭代不会生成任何新行时，递归会停止。

​	递归CTE在查询中引用自己的定义，实用RECURSIVE表示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A74.tmp.jpg) 

## **递归限制**

​	递归表达式的查询中需要包含一个终止递归的条件：

​	cte_max_recursion_depth：最大递归深度/调用次数

​	max_execution_time：SQL最长执行时间（没有定义最大递归深度的时候通过这个限制）

# GROUP BY不再隐式排序

从MySQL 8.0版本开始，MySQL对GROUP BY字段不再隐式排序。如果确实需要排序，必须加上ORDER BY子句。

下面通过案例来对比不同的版本中GROUP By字段的排序情况，分别在MySQL 5.7版本和MySQL 8.0版本中创建数据表、插入数据和查询数据，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A85.tmp.jpg) 

在MySQL 5.7中查看数据表bs1的结构，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A86.tmp.jpg) 

在MySQL 8.0中查看数据表bs1的结构，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A87.tmp.jpg) 

在MySQL 5.7中分组查询，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A88.tmp.jpg) 

从结果可以看出，字段bscount按升序自动排列。

在MySQL 8.0中分组查询，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A89.tmp.jpg) 

# 加密函数

加密函数主要用来对数据进行加密和界面处理，以保证某些重要数据不被别人获取。这些函数在保证数据库安全时非常有用。

## **加密函数MD5(str)**

MD5(str)为字符串算出一个MD5 128比特校验和。该值以32位十六进制数字的二进制字符串形式返回，若参数为NULL，则会返回NULL。

## **加密函数SHA(str)**

SHA(str)从原明文密码str计算并返回加密后的密码字符串，当参数为NULL时，返回NULL。SHA加密算法比MD5更加安全。

## **加密函数SHA2(str, hash_length)**

SHA2(str, hash_length)使用hash_length作为长度，加密str。hash_length支持的值为224、256、384、512和0。其中，0等同于256。

# 窗口函数

## **概述**

​	MySQL8.0支持窗口函数（Window Function），也称分析函数。

​	窗口函数与分组聚合函数类似，但是每一行数据都生成一个结果。

​	聚合窗口函数：SUM/AVG/COUNT/MAX/MIN等等。

 

​	常规操作：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A99.tmp.jpg) 

​	窗口函数：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A9A.tmp.jpg) 

## **专用窗口函数**

​	ROW_NUMBER()/RANK()/DENSE_RANK()/PERCENT_RANK()

​	FIRST_VALUE()/LAST_VALUE()/LEAD()/LAG()

​	CUME_DIST()/NTH_VALUE()/NTILE()

## **窗口**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A9B.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A9C.tmp.jpg) 

JSON增强

MySQL是一个关系型数据库，在MySQL 8.0之前，没有提供对非结构化数据的支持，但是如果用户有这样的需求，也可以通过MySQL的BLOB来存储非结构化的数据。

举例说明：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9A9D.tmp.jpg) 

在本例中，使用BLOB来存储JSON数据，需要用户保证插入的数据是一个能够转换成JSON格式的字符串，因为MySQL并不保证任何正确性。在MySQL看来，这就是一个普通的字符串，并不会进行任何有效性检查。此外，提取JSON中的字段也需要在用户的代码中完成。

例如，在Python语言中提取JSON中的字段，代码如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AAE.tmp.jpg) 

这种方式虽然也能够实现JSON的存储，但是有诸多缺点，最为显著的缺点有：

●　需要用户保证JSON的正确性，如果用户插入的数据并不是一个有效的JSON 字符串，MySQL 并不会报错。

●　所有对JSON的操作，都需要在用户的代码里进行处理，不够友好。

●　即使只是提取JSON中的某一个字段，也需要读出整个BLOB，效率不高。

●　无法在JSON字段上建索引。

在MySQL 8.0中，已经实现了对JSON类型的支持。MySQL本身已经是一个比较完备的数据库系统，对于底层存储并不适合有太大的改动，那么MySQL是如何支持JSON格式的呢？

MySQL 8.0对支持JSON的做法是在Server层提供一些便于操作JSON的函数，简单地将JSON编码成BLOB，然后交由存储引擎层进行处理。MySQL 8.0的JSON支持与存储引擎没有关系，MyISAM存储引擎也支持JSON格式。

下面将举例说明：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AAF.tmp.jpg) 

MySQL 8.0提供了很多操作JSON的函数，都是为了提高易用性。

MySQL编码成BLOB对象，首先存放的是JSON的元素个数，然后存放的是转换成BLOB以后的字节数，接下来存放的是key pointers和value pointers。为了加快查找速度，MySQL内部会对key进行排序，以提高处理速度。

在MySQL 8.0中，key的长度只用2个字节（65535）保存，如果超过这个长度，MySQL将报错，如下所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AB0.tmp.jpg) 

在MySQL的源码中，与JSON相关的文件有：

●　json_binary.cc

●　json_binary.h

●　json_dom.cc

●　json_dom.h

●　json_path.cc

●　json_path.h

其中，json_binary.cc处理JSON的编码、解码，json_dom.cc是JSON的内存表示，json_path.cc用于将字符串解析成JSON。对于JSON的编码，入口是json_binary.cc文件中的serialize函数。

对于JSON的解码，即将BLOB解析成JSON对象，入口是json_binary.cc文件中的parse_binary函数。只要搞清楚了JSON的存储格式，这两个函数是很好理解的。

内联路径操作符

JSON聚合函数

JSON实用函数

JSON合并函数

JSON表函数

 

# InnoDB增强

## **默认字符集改为utf8mb4**

在MySQL 8.0版本之前，默认字符集为latin1，utf8字符集指向的是utf8mb3。网站开发人员在数据库设计的时候往往会将编码修改为utf8字符集。如果遗忘修改默认的编码，就会出现乱码的问题。从MySQL 8.0开始，数据库的默认编码改为utf8mb4，从而避免了上述的乱码问题。

下面通过案例来对比不同的版本中默认字符集的变化。

在MySQL 5.7版本中，查看数据库的默认编码，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AB1.tmp.jpg) 

在MySQL 5.7版本中，查看数据表的默认编码，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AC1.tmp.jpg) 

在MySQL 8.0版本中，测试数据库的默认编码，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AC2.tmp.jpg) 

在MySQL 8.0版本中，查看数据表的默认编码，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AC3.tmp.jpg) 

## **系统表全部为InnoDB表**

从MySQL 8.0开始，系统表全部换成事务型的InnoDB表，默认的MySQL实例将不包含任何MyISAM表，除非手动创建MyISAM表。

下面通过案例来对比不同的版本中系统表的变化。

在MySQL 5.7版本中查看系统表类型，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AC4.tmp.jpg) 

在MySQL 8.0版本中查看系统表类型，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AC5.tmp.jpg) 

 

## **集成数据字典**

​	MySQL8.0删除了之前版本的元数据文件，例如.frm，.opt等。

​	将系统表（mysql）和数据字典全部改为InnoDB存储引擎。

​	简化了INFORMATION_SCHEMA的实现，提高了访问性能。

​	提供了序列化字典信息（SDI）的支持，以及ibd2sdi工具。

​	数据字典使用上的差异，例如innodb_read_only影响所有的存储引擎；数据字典表不可见，不能直接查询和修改。

## **原子DDL操作**

​	MySQL8.0开始支持原子DDL操作，其中与表相关的原子DDL只支持InnoDB存储引擎。DDL操作回滚日志写入到data dictionary数据字典表mysql.innodb_ddl_log（该表是隐藏的表，通过showtables无法看到）中，用于回滚操作。通过设置参数，可将DDL操作日志打印输出到MySQL错误日志中。

​	一个原子DDL操作内容包括：更新数据字典，存储引擎层的操作，在binlog中记录DDL操作。

​	支持与表相关的DDL：数据库、表空间、表、索引的CREATE、ALTER、DROP以及TRUNCATE TABLE。

​	支持的其他DDL：存储程序、触发器、视图、UDF（用户自定义函数）的CREATE、DROP以及ALTER语句。

​	支持账户管理相关的DDL：用户和角色的CREATE、ALTER、DROP以及适用的RENAME，以及GRANT和REVOKE语句。

## **自增列持久化**

​	MySQL5.7以及早期版本，InnoDB自增列计数器（AUTO_INCREMENT）的值只存储在内存中。

在MySQL 8.0之前，自增主键AUTO_INCREMENT的值如果大于max(primary key)+1，在MySQL重启后，会重置AUTO_INCREMENT=max(primary key)+1，这种现象在某些情况下会导致业务主键冲突或者其他难以发现的问题。

​	MySQL8.0每次变化时将自增计数器的最大值写入redo log，同时在每次检查点将其写入引擎私有的系统表。

​	解决了长期以来的自增字段值可能重复的bug。

 

下面通过案例来对比不同的版本中自增变量是否持久化。

在MySQL 5.7版本中，测试步骤如下：

创建的数据表中包含自增主键的id字段，语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AD6.tmp.jpg) 

插入4个空值，执行如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AD7.tmp.jpg) 

查询数据表test1中的数据，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AD8.tmp.jpg) 

删除id为4的记录，语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AD9.tmp.jpg) 

再次插入一个空值，语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9ADA.tmp.jpg) 

查询此时数据表test1中的数据，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AEB.tmp.jpg) 

从结果可以看出，虽然删除了id为4的记录，但是再次插入空值时，并没有重用被删除的4，而是分配了5。

删除id为5的记录，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AEC.tmp.jpg) 

重启数据库，重新插入一个空值。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AED.tmp.jpg) 

再次查询数据表test1中的数据，结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AEE.tmp.jpg) 

从结果可以看出，新插入的0值分配的是4，按照重启前的操作逻辑，此处应该分配6。出现上述结果的主要原因是自增主键没有持久化。在MySQL 5.7系统中，对于自增主键的分配规则，是由InnoDB数据字典内部一个计数器来决定的，而该计数器只在内存中维护，并不会持久化到磁盘中。当数据库重启时，该计数器会通过下面这种方式初始化。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AFE.tmp.jpg) 

在MySQL 8.0版本中，上述测试步骤最后一步的结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9AFF.tmp.jpg) 

从结果可以看出，自增变量已经持久化了。下面讲述MySQL 8.0的解决方案。

MySQL 8.0将自增主键的计数器持久化到重做日志中。每次计数器发生改变，都会将其写入重做日志中。如果数据库重启，InnoDB会根据重做日志中的信息来初始化计数器的内存值。***\*为了尽量减小对系统性能的影响，计数器写入到重做日志时并不会马上刷新数据库系统\****。

 

## **全文索引加强**

MySQL 8.0支持更加灵活、更加优化的全文搜索。例如，全本索引支持外部的分析器，就像MyISAM。插件可以替代内置分析器，也可以作为一个前端来使用。MySQL 8.0实现了标记优化器，这个优化器可以将查询结果传递到InnoDB，因此InnoDB可以跳过全文检索部分。

在InnoDB上实现了支持CJK（中文、日文和韩文）的全文检索。MySQL 8.0为CJK提供了一个默认的全文分析器（N-GRAM分析器）。

在全文索引中，n-gram就是一段文字里面连续的n个字的序列。例如，用n-gram来对“春花秋月”进行分词，得到的结果如表21.18所示。其中，n由参数ngram_token_size控制，即分词的大小，默认是2。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B00.tmp.jpg) 

下面通过举例来说明全文搜索功能。

创建数据表，并设置全文检索。SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B01.tmp.jpg) 

插入演示数据，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B12.tmp.jpg) 

普通检索必须要是整个词才能检索到，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B13.tmp.jpg) 

部分词是不能检索出信息的，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B14.tmp.jpg) 

新的全文检索功能检索任意两个组合的记录，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B15.tmp.jpg) 

再次使用全文检索功能检索任意两个组合记录，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B26.tmp.jpg) 

## **动态修改InnoDB缓冲池的大小**

从MySQL 5.7.5版本起，MySQL支持在不重启系统的情况下动态调整innodb_buffer_pool_size。调整大小的过程是以innodb_buffer_pool_chunk_size为单位迁移pages到新的内存空间，迁移进度可以通过Innodb_buffer_pool_resize_status查看。当在线修改缓冲池大小的时候，以chunk为单位进行增长或收缩。

缓冲池大小是innodb_buffer_pool_chunk_size*innodb_buffer_pool_instances的倍数（128MB），如果不是，将会适当调大innodb_buffer_pool_size，以满足要求。因此，可能会出现缓冲池大小的实际分配比配置文件中指定的size要大的情况。

下面举例说明如何在线调整缓冲池的大小。

查看当前缓冲池的大小，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B27.tmp.jpg) 

查看缓冲池中实例的个数，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B28.tmp.jpg) 

动态修改缓冲池的大小为1000MB，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B29.tmp.jpg) 

查看警告信息，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B39.tmp.jpg) 

出现上述警告信息的原因是，设置1000MB不是innodb_buffer_pool_chunk_size*innodb_buffer_pool_instances的倍数，即128MB的倍数。

查看设置缓冲池的进度，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B3A.tmp.jpg) 

查看当前缓冲池的大小，SQL语句如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B4B.tmp.jpg) 

从结果可以看出，缓冲池的大小被设置成了1056964608字节（约1024MB），因为1024是128的整数倍，出现了缓冲池大小比配置文件里指定的size还大。

## **表空间数据加密**

在MySQL 8.0中，InnoDB Tablespace Encryption支持对独享表空间的InnoDB数据文件加密，其依赖keyring plugin来进行秘钥的管理。开启加密功能需要启动参数--early-plugin-load。

在PHP的配置文件my.ini中开启--early-plugin-load参数。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B4C.tmp.jpg) 

启动参数后，查看服务器是否支持加密功能：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B4D.tmp.jpg) 

创建加密表空间：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B4E.tmp.jpg) 

## **锁定实例**

从MySQL 8开始，我们可以锁定实例进行备份了，这将允许在线备份期间的DML，并阻止可能导致快照不一致的所有操作。

***\*操作步骤：\****

1、在开始备份之前，请锁定需要备份的实例：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B5E.tmp.jpg) 

2、执行备份，完成后解锁实例：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B5F.tmp.jpg) 

 

## **死锁检查控制**

​	MySQL8.0（MySQL5.7.15）增加了一个新的动态变量（innodb_deadlock_detect），用于控制系统是否执行InnoDB死锁检查。

​	对于高并发的系统，禁用死锁检查可能带来性能的提高。

​	

## **锁定语句选项****/跳过锁等待**

​	SELECT…FOR SHARE和SELECT…FOR UPDATE中支持NOWAIT、SKIP LOCKED选项：

​	对于NOWAIT，如果请求的行被其他事务锁定时，语句立即返回。

​	对于SKIP LOCKED，从返回的结果集中移除被锁定的行。

 

在MySQL 5.7版本中，SELECT...FOR UPDATE语句在执行的时候，如果获取不到锁，会一直等待，直到innodb_lock_wait_timeout超时。

在MySQL 8.0版本中，通过添加NOWAIT和SKIP LOCKED语法，能够立即返回。如果查询的行已经加锁，那么NOWAIT会立即报错返回，而SKIP LOCKED也会立即返回，只是返回的结果中不包含被锁定的行。

下面通过案例来理解MySQL 8.0版本中如何跳过锁等待，如表21.19所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps9B60.tmp.jpg) 

## **其他改进功能**

# 主从复制

## **二进制压缩**

## **MGR**

参考：

[https://mp.weixin.qq.com/s?__biz=MjM5NzAzMTY4NQ==&mid=2653935539&idx=1&sn=07f9d004eedef048716ed02220376414&chksm=bd3b4dd98a4cc4cf57c42bfda63fbc5ab5640ad495707101b9874397ab4652e16aa9bbd76c42&mpshare=1&scene=24&srcid=0204gtEiVkA6wcYGLhjQ60bA&sharer_sharetime=1612424005665&sharer_shareid=33f795d236f19ac7c128b2e279563f84#rd](#rd)

 