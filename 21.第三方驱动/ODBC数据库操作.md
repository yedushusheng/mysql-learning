# 简介

# ODBC句柄

# ODBC程序流程

## 分配环境句柄

​	基于ODBC3.X版本的应用统一使用SQLAllocHandle来分配句柄，调用时设计不同的句柄类型就可以获取该类型的句柄。但是在API内部实现上一般重新转换为执行SQLAllocEnv（用来分配环境句柄）、SQLAllocConnect和SQLAllocStmt，这样可以达到兼容和代码重用作用。

​	举例：

​	ret = SQLAllocHandle(SQL_HANDLE_ENV, NULL, &oracleenv);

 

## 分配连接句柄

​	SQLAllocConnect用来分配连接句柄，连接句柄提供对外一些信息的访问。例如，在连接上的有效语句以及标识符句柄，以及当前是否打开一些一个事务处理。调用SQLAllocConnect函数获取连接句柄。	

​	举例：

​	ret = SQLAllocHandle(SQL_HANDLE_DBC,oracleenv,&oraclehdbc);

 

## 建立数据源

​	使用已分配的连接句柄来建立应用程序和数据源/数据库系统的连接，进行句柄和数据源的绑定。绑定也由目标数据源的ODBC驱动程序完成。

​	举例：

​	ret = SQLConnect(oraclehdbc,

​				“conn”,SQL_NTS,	//ODBC的DNS名称

​				“admin”,SQL_NTS,	//用户名

​				“1234”,SQL_NTS);	//密码

## 分配语句句柄

​	用户对DBC数据源的存取操作，都是通过SQL语句实现的。在这个过程中，应用程序将通过连接向ODBC数据库提交SQL语句，以完成用户请求的操作。即通过执行SQLAllocHandle或SQLAllocStmt来分配语句句柄，调用SQLAllocStmt函数获取语句句柄。

​	举例：

​	SQLStmt = “select * from …”

​	rc = SQLAllocStmt(hdbc, hstmt);

## 执行SQL语句

​	执行SQL语句。执行SQL语句的方法比较多，最简单明了的方法是调用SQLAllocStmt函数。

​	举例：

​	SQLstmt = “select ……”

​	rc = SQLExecDirect(hstmt, SQLstmt, Len(SQLstmt));

## 检索结果集

​	如果SQL语句被顺利提交并正确执行，那么就会产生一个结果集。检索结果集的方法有很多，最简单最直接的方法是调用SQLFeth和SQLGetData函数。

​	SQLFetch函数的功能是将结果集的当前记录指针移至下一个记录。

​	SQLGetData函数的功能是提取结果集中当前记录的某个字段值。通常可以采用一个循环以提取结果集中所有记录的所有字段值，该循环重复执行SQLFetch和SQLGetData函数，直至SQLFetch函数返回SQL_NO_DATA_FOUND，这表示已经到达结果集的末尾。

​	

## 结束应用程序

​	在应用程序完成数据库操作，退出运行之前，必须释放程序中使用的系统资源。这些系统资源包括：语句句柄、连接句柄和ODBC环境句柄。完成这个过程如下：

​	调用SQLFreeStmt函数释放语句句柄及其相关的系统资源，举例：

​	rc = SQLFreeStmt(hstmt, SQL_DROP);

​	调用SQLDisconnect函数关闭连接，举例：

​	rc = SQLDisconnect(hdbc);

​	调用SQLFreeConnect函数释放连接句柄及其相关的系统资源，举例：

​	rc = SQLFreeeConnect(hdbc);

​	调用SQLFreeEnv函数释放环境句柄及其相关的系统资源，停止ODBC操作，举例：

​	rc = SQLFreeEnv(henv);

## 错误处理

​	所有DBCAPI函数，若在执行期间发生错误，都将返回一个标准错误代码SQL_ERROR。

​	一般来讲，在每次调用ODBC_PI函数之后，都应该检查该函数返回值，确定该函数是否成功地执行，再决定是否继续后续过程。而详细的错误信息，可以调用SQLError函数获得。SQLError函数将返回下列信息：标准的ODBC错误状态码ODBC数据源提供的内部错误编码错误信息串。

# 环境搭建

1、 使用ODBC需要用到UnixODBC，一般系统安装后会自动安装好

2、 在机器上安装ODBC驱动，如mysql驱动、mariadb驱动

3、 驱动tar.gz解压后拷贝到/usr/lib64目录下

4、 设置配置文件（执行odbcinst –j可以查看使用的配置文件路径）：

 

5、 设置odbcinst.ini

 

​	注：MySQL是驱动的名字，设置前面的驱动路径即可

6、 设置odbc.ini

 

​	注：MySQL是数据源名字，填写该数据源对应的驱动名和服务器信息即可。

7、 设置好环境后，即可通过isql工具连接测试：

isql dsn

注：dsn是odbc.ini文件中设置的数据源名mysql或mariadb，可以同时安装多个驱动，对应不同名字的数据源即可。

8、 如果需要执行脚本，采用下面指令：

isql –v 可执行文件 *.sql test.txt

 

# ODBC与JDBC

## ODBC

​	ODBC（Open Database Connectivity）是一组对数据库访问的标准API，这些API通过SQL来完成发部分任务，而且它本身也支持SQL语言，支持用户发来的SQL。ODBC定义了访问数据库API的一组规范，这些API独立于形色各异的DBMS和编程语言。

​	也就是说，一个基于ODBC的应用程序，对数据库的操作不依赖于任何DBMS，不直接与DBMS打交道，所有的数据库操作由对应的DBMS的ODBC驱动程序完成。不论是SQLServer、Access还是Oracle数据库，均可用ODBC API进行访问。

​	由此可见，ODBC的最大优点是能以统一的方式处理所有的数据库。

## JDBC

​	JDBC（Java Database Connectivity）是Java与数据库的接口规范，JDNBC定义了一个支持标准SQL功能的通用底层SAPI，它由Java语言编写的类和接口组成，旨在让各数据库开发商为Java程序员提供标准的数据库API。

​	JDBC API定义了若干Java中的类，表示数据库连接、SQL指令、结果集、数据库元数据等，它允许Java程序员发送SQL指令并处理结果。

## 异同点

​	JDBC和ODBC的共同点：

1、 JDBC和ODBC都是基于X/Open的SQL调用接口；

2、 从结构来说，JDBC总体结构类似于ODBC，都有四个组件：应用程序、驱动程序管理器、驱动程序和数据源，工作原理大致相同；

3、 从内容交互来说，JDBC保持了ODBC的基本特性，也独立于特定数据库，而且都不是直接与数据库交互，而是通过驱动程序管理器。

 

JDBC与ODBC的区别：

我们知道，ODBC几乎能够在所有平台上连接几乎所有的数据库，那么为什么Java不适用ODBC？

Java可以使用ODBC，但最好以JDBC-ODBC桥的形式使用（Java连接总体分为Java直连和JDBC-ODBC桥两种形式）。

ODBC不适合直接在Java上使用，因为它使用C语言接口。从Java调用本地C代码在安全性、实现、坚固性和程序的自动等方面有许多缺点。从JDBC C API到Java API的字面翻译是不可取的。例如，Java没有指针，而ODBC却对指针用得很广泛（包括易出错的void*指针）。

另外，ODBC比较复杂，而JDBC尽量保证简单功能的简便性，同时在必要时允许使用高级功能。如果使用ODBC，就必须手动地将ODBC驱动程序管理器和驱动程序安装在每台客户机上。如果完全用Java编写JDBC驱动程序则JDBC代码在所有Java平台上（栋网络计算机到大型机）都可以自动安装、移植并保证安全性。

总之，JDBC在很大程度上借鉴了ODBC的思想，从它的基础上发展而来。JDBC保留了ODBC的基本设计特征，因此，熟悉ODBC的程序员将发现JDBC很容易使用。它们之间最大的区别是：JDBC以Java风格与优点为基础并进行优化，因此更加易于使用。