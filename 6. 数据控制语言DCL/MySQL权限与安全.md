# 概述

MySQL 的权限系统主要用来对连接到数据库的用户进行权限的验证，以此来判断此用户是否属于合法的用户，如果是合法用户则赋予相应的数据库权限。

数据库的权限和数据库的安全是息息相关的，不当的权限设置可能会导致各种各样的安全隐患，操作系统的某些设置也会对MySQL的安全造成影响。

参考：

https://www.cnblogs.com/qlqwjy/p/8022575.html

 

# 权限管理

DCL语句主要是DBA用来管理系统中的对象权限时使用，一般的开发人员很少使用。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsC617.tmp.jpg) 

## 原理

MySQL权限系统通过下面两个阶段进行认证：

对连接的用户进行身份认证，合法的用户通过认证，不合法的用户拒绝连接；

对通过认证的合法用户赋予相应的权限，用户可以在这些权限范围内对数据库做相应的操作。

对于身份的认证，MySQL是通过IP地址和用户名联合进行确认的，例如MySQL安装后默认创建的用户root@localhost 表示用户root只能从本地（localhost）进行连接才可以通过认证，此用户从其他任何主机对数据库进行的连接都将被拒绝。也就是说，同样的一个用户名，如果来自不同的IP地址，则MySQL将其视为不同的用户。

MySQL的权限表在数据库启动的时候就载入内存，当用户通过身份认证后，就在内存中进行相应权限的存取，这样，此用户就可以在数据库中做权限范围内的各种操作了。

 

## 权限表的存取

mysql数据库中的3个权限表：user 、db、 host

权限表的存取过程是：

1、先从user表中的host、 user、 password这3个字段中判断连接的IP、用户名、密码是否存在表中，存在则通过身份验证；

2、通过权限验证，进行权限分配时，按照user、db、tables_pri、columns_priv的顺序进行分配。即先检查全局权限表user，如果user中对应的权限为Y，则此用户对所有数据库的权限都为Y，将不再检查db，tables_priv，columns_priv；如果为N，则到db表中检查此用户对应的具体数据库，并得到db中为Y的权限；如果db中为N，则检查tables_priv中此数据库对应的具体表，取得表中的权限Y，以此类推。

## 权限

​	以下操作都是以root身份登陆进行grant授权，以p1@localhost身份登陆执行各种命令。

### **usage**

连接（登陆）权限，建立一个用户，就会自动授予其usage权限（默认授予）。

mysql> grant usage on *.* to ‘p1′@’localhost’ identified by ‘123′;

该权限只能用于数据库登陆，不能执行任何操作；且usage权限不能被回收，也即REVOKE用户并不能删除用户。

 

### **select**

必须有select的权限，才可以使用select table

mysql> grant select on pyt.* to ‘p1′@’localhost’;

mysql> select * from shop;

注：可以设置整个库、某个表，甚至是某些字段的权限，比如：

grant select on testdb.* to dba@localhost; -- dba可以查询testdb中的表。

grant select, insert, update, delete on testdb.orders to dba@localhost; 

grant select(user_id,username) on smp.users to mo_user@'%' identified by '123345';

 

### **create**

必须有create的权限，才可以使用create table

mysql> grant create on pyt.* to ‘p1′@’localhost’;

 

### create routine

必须具有create routine的权限，才可以使用{create |alter|drop} {procedure|function}

mysql> grant create routine on pyt.* to ‘p1′@’localhost’;

当授予create routine时，自动授予EXECUTE, ALTER ROUTINE权限给它的创建者。

 

### create temporary tables

必须有create temporary tables的权限（注意这里是tables，不是table），才可以使用create temporary tables。

mysql> grant create temporary tables on pyt.* to ‘p1′@’localhost’;

[mysql@mydev ~]$ mysql -h localhost -u p1 -p pyt

mysql> create temporary table tt1(id int);

 

### create view

必须有create view的权限，才可以使用create view

mysql> grant create view on pyt.* to ‘p1′@’localhost’;

mysql> create view v_shop as select price from shop;

### create user

要使用CREATE USER，必须拥有mysql数据库的全局CREATE USER权限，或拥有INSERT权限。

mysql> grant create user on *.* to ‘p1′@’localhost’;

或：mysql> grant insert on *.* to p1@localhost;

 

### **insert**

必须有insert的权限，才可以使用insert into ….. values….

 

### **alter**

必须有alter的权限，才可以使用alter table

alter table shop modify dealer char(15);

 

### alter routine

必须具有alter routine的权限，才可以使用{alter |drop} {procedure|function}

mysql>grant alter routine on pyt.* to ‘p1′@’ localhost ‘;

mysql> drop procedure pro_shop;

Query OK, 0 rows affected (0.00 sec)

mysql> revoke alter routine on pyt.* from ‘p1′@’localhost’;

[mysql@mydev ~]$ mysql -h localhost -u p1 -p pyt

mysql> drop procedure pro_shop;

ERROR 1370 (42000): alter routine command denied to user ‘p1′@’localhost’ for routine ‘pyt.pro_shop’

 

### **update**

必须有update的权限，才可以使用update table

mysql> update shop set price=3.5 where article=0001 and dealer=’A';

 

### **delete**

必须有delete的权限，才可以使用delete from ….where….(删除表中的记录)

 

### **drop**

必须有drop的权限，才可以使用drop database db_name; drop table tab_name;

drop view vi_name; drop index in_name;

 

### show database

通过show database只能看到你拥有的某些权限的数据库，除非你拥有全局SHOW DATABASES权限。

对于p1@localhost用户来说，没有对mysql数据库的权限，所以以此身份登陆查询时，无法看到mysql数据库：

mysql> show databases;

+——————–+

| Database |

+——————–+

| information_schema|

| pyt |

| test |

+——————–+

 

### show view

必须拥有show view权限，才能执行show create view。

mysql> grant show view on pyt.* to p1@localhost;

mysql> show create view v_shop;

 

### **index**

必须拥有index权限，才能执行[create |drop] index

mysql> grant index on pyt.* to p1@localhost;

mysql> create index ix_shop on shop(article);

mysql> drop index ix_shop on shop;

 

### **excute**

执行存在的Functions，Procedures

grant 作用在存储过程、函数上： 

grant execute on procedure testdb.pr_add to 'dba'@'localhost' 

grant execute on function testdb.fn_add to 'dba'@'localhost' 

 

mysql> call pro_shop1(0001,@a)；

+———+

| article |

+———+

| 0001 |

| 0001 |

+———+

mysql> select @a;

+——+

| @a |

+——+

| 2 |

+——+

 

### lock tables

必须拥有lock tables权限，才可以使用lock tables

mysql> grant lock tables on pyt.* to p1@localhost;

mysql> lock tables a1 read;

mysql> unlock tables;

 

### **references**

有了REFERENCES权限，用户就可以将其它表的一个字段作为某一个表的外键约束。

 

### **reload**

必须拥有reload权限，才可以执行flush [tables | logs | privileges]

mysql> grant reload on pyt.* to p1@localhost;

ERROR 1221 (HY000): Incorrect usage of DB GRANT and GLOBAL PRIVILEGES

mysql> grant reload on *.* to ‘p1′@’localhost’;

Query OK, 0 rows affected (0.00 sec)

mysql> flush tables;

 

### replication client

拥有此权限可以查询master server、slave server状态。

mysql> show master status;

ERROR 1227 (42000): Access denied; you need the SUPER,REPLICATION CLIENT privilege for this operation

mysql> grant Replication client on *.* to p1@localhost;

或：mysql> grant super on *.* to p1@localhost;

mysql> show master status;

+——————+———-+————–+——————+

| File | Position | Binlog_Do_DB | Binlog_Ignore_DB |

+——————+———-+————–+——————+

| mysql-bin.000006 | 2111 | | |

+——————+———-+————–+——————+

mysql> show slave status;

 

### replication slave

拥有此权限可以查看从服务器，从主服务器读取二进制日志。

mysql> show slave hosts;

ERROR 1227 (42000): Access denied; you need the REPLICATION SLAVE privilege for this operation

mysql> show binlog events;

ERROR 1227 (42000): Access denied; you need the REPLICATION SLAVE privilege for this operation

mysql> grant replication slave on *.* to p1@localhost;

mysql> show slave hosts;

Empty set (0.00 sec)

mysql>show binlog events;

+—————+——-+—————-+———–+————-+————–+

| Log_name | Pos | Event_type | Server_id| End_log_pos|Info | 

+—————+——-+————–+———–+————-+—————+

| mysql-bin.000005 | 4 | Format_desc | 1 | 98 | Server ver: 5.0.77-log, Binlog ver: 4 | |mysql-bin.000005|98|Query|1|197|use `mysql`; create table a1(i int)engine=myisam|

……………………………………

 

### shutdown

关闭MySQL：

[mysql@mydev ~]$ mysqladmin shutdown

重新连接：

[mysql@mydev ~]$ mysql

ERROR 2002 (HY000): Can’t connect to local MySQL server through socket ‘/tmp/mysql.sock’ (2)

[mysql@mydev ~]$ cd /u01/mysql/bin

[mysql@mydev bin]$ ./mysqld_safe &

[mysql@mydev bin]$ mysql

 

### grant option

拥有grant option，就可以将自己拥有的权限授予其他用户（仅限于自己已经拥有的权限）

mysql> grant Grant option on pyt.* to p1@localhost;

mysql> grant select on pyt.* to p2@localhost;

 

### **file**

拥有file权限才可以执行 select ..into outfile和load data infile…操作，但是不要把file, process, super权限授予管理员以外的账号，这样存在严重的安全隐患。

mysql> grant file on *.* to p1@localhost;

mysql> load data infile ‘/home/mysql/pet.txt’ into table pet;

 

为用户赋予file权限时不必指明该权限专门赋予哪一个schema，若指明schema，则会报错。

mysql> grant file on test_db.* to "test_user"@"%";

ERROR 1221 (HY000): Incorrect usage of DB GRANT and GLOBAL PRIVILEGES

若在赋予file权限时不对schema作限制，则可以赋权成功。

mysql> grant file on *.* to "test_user"@"%";

Query OK, 0 rows affected (0.00 sec)

 

### **super**

这个权限允许用户终止任何查询；修改全局变量的SET语句；使用CHANGE MASTER，PURGE MASTER LOGS。

mysql> grant super on *.* to p1@localhost;

mysql> purge master logs before ‘mysql-bin.000006′;

 

### **process**

通过这个权限，用户可以执行SHOW PROCESSLIST和KILL命令。默认情况下，每个用户都可以执行SHOW PROCESSLIST命令，但是只能查询本用户的进程。

mysql> show processlist;

+—-+——+———–+——+———+——+——-+——————+

| Id | User | Host | db | Command | Time | State | Info |

+—-+——+———–+——+———+——+——-+——————+

| 12 | p1 | localhost | pyt | Query | 0 | NULL | show processlist |

+—-+——+———–+——+———+——+——-+——————+

另外，管理权限（如 super， process， file等）不能够指定某个数据库，on后面必须跟*.*

mysql> grant super on pyt.* to p1@localhost;

ERROR 1221 (HY000): Incorrect usage of DB GRANT and GLOBAL PRIVILEGES

mysql> grant super on *.* to p1@localhost;

Query OK, 0 rows affected (0.01 sec)

## 账号管理

账号管理也是 DBA日常工作中很重要的工作之一，主要包括账号的创建、权限更改和账号的删除。用户连接数据库的第一步都从账号创建开始。

### **创建账号**

有两种方法可以用来创建账号：使用GRANT语法创建或者直接操作授权表，但更推荐使用第一种方法，因为操作简单，出错几率更少。

GRANT的常用语法如下：

GRANT priv_type [(column_list)] [, priv_type [(column_list)]] . .

ON [object_type] {tbl_name |* |*.* | db_name.*}

TO user [IDENTIFIED BY [PASSWORD] 'password']

[, user [IDENTIFIED BY [PASSWORD] 'password']] . .

[WITH GRANT OPTION]

object_type =TABLE| FUNCTION| PROCEDURE

 

### **查看账号权限**

账号创建好后，可以通过如下命令查看权限：

show grants; -- 查看当前用户（当前）权限

show grants for user@host;

 

### **更改账号权限**

可以进行权限的新增和回收。和账号创建一样，权限变更也有两种方法：使用GRANT（新增）和REVOKE（回收）语句，或者更改权限表。

revoke 跟 grant 的语法差不多，只需要把关键字 “to” 换成 “from” 即可：

grant all on *.* to dba@localhost;

revoke all on *.* from dba@localhost; 

注意：

1、grant, revoke 用户权限后，该用户只有重新连接 MySQL 数据库，权限才能生效。

2、如果想让授权的用户，也可以将这些权限 grant 给其他用户，需要选项 “grant option“

grant select on testdb.* to dba@localhost with grant option;

grant select on testdb.* to dba@localhost with grant option;

这个特性一般用不到。实际中，数据库权限最好由 DBA 来统一管理。

 

### **修改账号密码**

方法1：可以用mysqladmin命令在命令行指定密码。

shell> mysqladmin -u user_name -h host_name password "newpwd"

方法2：执行SET PASSWORD语句。

下例中将账号“jeffrey'@'%”的密码改为“biscuit”。

mysql> SET PASSWORD FOR 'jeffrey'@'%' = PASSWORD('biscuit');

如果是更改自己的密码，可以省略for语句：

mysql> SET PASSWORD = PASSWORD('biscuit');

方法3：还可以在全局级别使用GRANT USAGE语句（在“*.*”）来指定某个账户的密码而不影响账户当前的权限。

mysql> GRANT USAGE ON *.* TO 'jeffrey'@'%' IDENTIFIED BY 'biscuit';

方法4：直接更改数据库的user表。

shell> mysql -u root mysqlmysql> INSERT INTO user (Host,User,Password)-> VALUES('%','jeffrey',PASSWORD('biscuit'));

mysql> FLUSH PRIVILEGES;

shell> mysql -u root mysql

mysql> UPDATE user SET Password = PASSWORD('bagel')-> WHERE Host = '%' AND User = 'francis';

mysql> FLUSH PRIVILEGES;

方法5：以上方法在更改密码时，用的都是明文，这样就会存在安全问题，比如修改密码的机器被入侵，那么通过命令行的历史执行记录就可以很容易地得到密码。因此，在一些重要的数据库中，可以直接使用MD5密码值来对密码进行更改，如下面的例子：

GRANT USAGE ON *.* TO 'jeffrey'@'%' IDENTIFIED BY PASSWORD'23AE809DDACAF96AF0FD78ED0 4B6A265E05AA257';

或者：

set password = '23AE809DDACAF96AF0FD78ED04B6A265E05AA257'

其中的MD5密码串可以事先用其他方式获得。

注意：更改密码时，一定要注意什么时候需要使用PASSWORD函数。

 

### **删除账号**

要彻底删除账号，同样也有两种实现方法，即DROP USER命令和修改权限表。

DROP USER语法非常简单，具体如下：

DROP USER user [, user] . .

删除用户及目录：userdel -r user1

注：这是Linux的命令，不是mysql的命令。

 

### **账号资源限制**

创建 MySQL 账号时，还有一类选项前面没有提及，我们称为账号资源限制（Account ResourceLimits），这类选项的作用是限制每个账号实际具有的资源限制，这里的“资源”主要包括以下内容：

单个账号每小时执行的查询次数；

单个账号每小时执行的更新次数；

单个账号每小时连接服务器的次数；

单个账号并发连接服务器的次数。

在实际应用中，可能会发生这种情景，由于程序bug或者系统遭到攻击，使得某些应用短时间内发生了大量的点击，从而对数据库造成了严重的并发访问，造成数据库短期无法响应甚至down掉，对生产带来负面影响。为了防止这种问题的出现，我们可以通过对连接账号进行资源限制的方式来解决，比如按照日常访问量加上一定冗余设置每小时查询1万次，那么1小时内如果超过1万次查询数据库就会给出资源不足的提示，而不会再分配资源进行实际查询。

设置资源限制的语法如下：

GRANT . .with option

其中option的选项可以是以下几个。

MAX_QUERIES_PER_HOUR count：每小时最大查询次数。

MAX_UPDATES_PER_HOUR count：每小时最大更新次数。

MAX_CONNECTIONS_PER_HOUR count：每小时最大连接次数。

MAX_USER_CONNECTIONS count：最大用户连接数。

其中，MAX_CONNECTIONS_PER_HOUR count和MAX_USER_CONNECTIONS count的区别在于前者是每小时累计的最大连接次数，而后者是瞬间的并发连接数。系统还有一个全局参数MAX_USER_CONNECTIONS，它和用户MAX_USER_CONNECTIONS count的区别在于如果后者为0，则此用户的实际值应该为全局参数值，否则就按照用户MAX_USER_CONNE CTIONS count的值来设置。

 

### **plush privileges**

执行完grant赋权后，不会立即生效，需要执行flush privileges才会生效。

 

# MySQL安全问题

## 操作系统相关的安全问题

1、严格控制操作系统账号和权限

在数据库服务器上要严格控制操作系统的账号和权限，比如：

锁定mysql用户；其他任何用户都采取独立的账号登录，管理员通过mysql专有用户管理MySQL，或者通过 root su到mysql用户下进行管理；mysql用户目录下，除了数据文件目录，其他文件和目录属主都改为root。

2、尽量避免以root权限运行MySQL

MySQL安装完毕后，一般会将数据目录属主设置为mysql用户，而将MySQL软件目录的属主设置为root，这样做的目的是当使用 mysql 用户启动数据库时，可以防止任何具有 FILE权限的用户能够用root创建文件。而如果使用root用户启动数据库，则任何具有FILE权限的用户都可以读写root用户的文件，这样会给系统造成严重的安全隐患。

3、防止DNS欺骗

创建用户时，host可以指定域名或者IP地址。但是，如果指定域名，就可能带来如下安全隐患：如果域名对应的IP地址被恶意修改，则数据库就会被恶意的IP地址进行访问，导致安全隐患。

 

## 数据库相关的安全问题

1、删除匿名账号

在某些版本中，安装完毕MySQL后，会自动安装一个空账号，此账号具有对test数据库的全部权限。

普通用户只需要执行mysql命令即可登录MySQL数据库，这个时候默认使用了空用户，可以在test数据库里面做各种操作，比如可以创建一个大表，占用大量磁盘空间，这样将给系统造成安全隐患。

2、给root账号设置口令

3、设置安全密码

密码的安全体现在以下两个方面：设置安全的密码，建议使用6位以上字母、数字、下划线和一些特殊字符组合而成的字符串；使用上的安全，使用密码期间尽量保证使用过程安全，不会被别人窃取。

4、只授予账号必需的权限

all privileges里面的权限远远超过了我们一般应用所需要的权限。而且，有些权限如果误操作，将会产生非常严重的后果，比如 drop_priv 等。因此，赋予用户权限的时候越具体，则对数据库越安全。

5、除root外，任何用户不应有mysql库user表的存取权限

由于MySQL中可以通过更改mysql数据库的user表进行权限的增加、删除、变更等操作，因此，除了root以外，任何用户都不应该拥有对user表的存取权限（SELECT、UPDATE、INSERT、DELETE等），否则容易造成系统的安全隐患。

6、不要把FILE、PROCESS或SUPER权限授予管理员以外的账号

FILE权限主要有以下两种作用。

将数据库的信息通过SELECT…INTO OUTFILE…写到服务器上有写权限的目录下，作为文本格式存放。具有权限的目录也就是启动MySQL时的用户权限目录。

可以将有读权限的文本文件通过LOAD DATA INFILE…命令写入数据库表，如果这些表中存放了很重要的信息，将对系统造成很大的安全隐患。

7、LOAD DATA LOCAL带来的安全问题

LOAD DATA默认读的是服务器上的文件，但是加上LOCAL参数后，就可以将本地具有访问权限的文件加载到数据库中。这在带来方便的同时，也带来了以下安全问题。

可以任意加载本地文件到数据库。

在Web环境中，客户从Web服务器连接，用户可以使用LOAD DATA LOCAL语句来读取Web服务器进程有读访问权限的任何文件（假定用户可以运行SQL服务器的任何命令）。在这种环境中，MySQL服务器的客户实际上是Web服务器，而不是连接Web服务器的用户运行程序。

解决方法是，可以用--local-infile=0选项启动 mysqld从服务器端禁用所有 LOAD DATA LOCAL命令。

对于mysql命令行客户端，可以通过指定--local-infile[=1]选项启用LOAD DATA LOCAL，或通过--local-infile=0选项禁用。类似地，对于mysqlimport，--local或 -L选项启用本地数据文件装载。在任何情况下，成功进行本地装载需要服务器启用相关选项。

8、使用MERGE存储引擎潜藏的安全漏洞

MERGE存储引擎的表在某些版本中可能存在以下安全漏洞：

用户A赋予表T的权限给用户B；

用户B创建一个包含T的MERGE表，做各种操作；

用户A收回对T的权限。

存在的安全隐患是用户B通过MERGE表仍然可以访问表A中的数据。

9、DROP TABLE命令并不收回以前的相关访问授权

DROP表的时候，其他用户对此表的权限并没有被收回，这样导致重新创建同名的表时，以前其他用户对此表的权限会自动赋予，进而产生权限外流。因此，在删除表时，要同时取消其他用户在此表上的相应权限。

10、使用SSL

SSL（Secure Socket Layer，安全套接字层）是一种安全传输协议，最初由Netscape公司所开发，用以保障在Internet上数据传输的安全，利用数据加密（Encryption）技术，可确保数据在网络上的传输过程中不会被截取及窃听。

SSL协议提供的服务主要有：

（1）认证用户和服务器，确保数据发送到正确的客户机和服务器；

（2）加密数据以防止数据中途被窃取；

（3）维护数据的完整性，确保数据在传输过程中不被改变。

在MySQL中，要想使用SSL进行安全传输，需要在命令行中或选项文件中设置“--ssl”选项。

对于服务器，“--ssl”选项规定该服务器允许 SSL 连接。对于客户端程序，它允许客户使用SSL连接服务器。单单该选项不足以使用SSL连接，还必须指定--ssl-ca、--ssl-cert和--ssl-key选项。如果不想启用SSL，则可以将选项指定为--skip-ssl或--ssl=0。

注意：如果编译的服务器或客户端不支持SSL，则使用普通的未加密的连接。

11、如果可能，给所有用户加上访问IP限制

对数据库来说，我们希望从客户端过来的连接都是安全的，因此，就很有必要在创建用户时指定可以进行连接的服务器IP或者HOSTNAME，只有符合授权的IP或者HOSTNAME才可以进行数据库访问。

12、REVOKE命令的漏洞

 

# 其他安全设置

## old-passwords

在MySQL 4.1版本之前，PASSWORD函数生成的密码是 16位。4.1以后，MySQL改进了密码算法，生成的函数值变成了41位。

## safe-user-create

此参数如果启用，用户将不能用GRANT语句创建新用户，除非用户有mysql数据库中user表的INSERT权限。如果想让用户具有授权权限来创建新用户，应给用户授予下面的权限：

mysql > GRANT INSERT(user) ON mysql.user TO 'user_name'@'host_name';

这样确保用户不能直接更改权限列，必须使用GRANT语句给其他用户授予该权限。

## secure-authsecure-auth 

参数的作用是让 MySQL 4.1 以前的客户端无法进行用户认证。即使使用了old-passwords参数也行。此参数的作用就是为了防止低版本的客户端使用旧的密码认证方式连接高版本的服务器，从而引起安全隐患。因为4.1版本以前的客户端已经很少有人使用，因而此参数也很少被使用。

 

## skip-grant-tablesskip-grant-tables

这个选项导致服务器根本不使用权限系统，从而给每个人以完全访问所有数据库的权力。通过执行mysqladmin flush-privileges或mysqladmin reload命令，或执行 flush privileges语句，都可以让一个正在运行的服务器再次开始使用授权表。

 

## skip-network

在网络上不允许TCP/IP连接，所有到数据库的连接必须经由命名管道（Named Pipes）、共享内存（Shared Memory）或UNIX套接字（SOCKET）文件进行。这个选项适合应用和数据库共用一台服务器的情况，其他客户端将无法通过网络远程访问数据库，大大增强了数据库的安全性，但同时也带来了管理维护上的不方便。

## skip-show-database

使用skip-show-database选项，只允许有show databases权限的用户执行show databases语句，该语句显示所有数据库名。不使用该选项，允许所有用户执行show databases，但只显示用户有show databases 权限或部分数据库权限的数据库名。

 

# 分布式数据库实践

## GoldenDB

### **计算节点与数据节点权限不统一**

在GoldenDB中，由于计算节点没有真正的用户权限，生效的是DB的权限表，所以会出现这样的一种现象：

1、计算节点下发select into outfile

2、数据节点不设置file权限

但是却可以正常执行语句，原因在于计算节点会将select into outfile拆分成select下发到各个DB，然后在计算节点做into outfile的数据汇聚，而DB是有select权限的，所以可以正常执行。

解决方法：计算节点引入权限表的概念，并且DB数据节点检查发过来sql的权限类型。

### **并发创建用户产生元数据锁**

在分布式数据库中，计算节点不支持并发创建用户，比如同时复制以下语句执行会报错：

create user user1 identified by ‘12345’;

grant all on *.* to user1;

create user user2 identified by ‘12345’;

grant all on *.* to user2;

报错信息类似：other use own lock

之所以这样是因为，防止并发执行用户创建DDL造成这个分片之间的密码、权限不一致。

解决方法：

1、去元数据的数据库查询锁状态

然后su -mds用户下执行解

 