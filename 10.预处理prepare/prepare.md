# 概述

​	prepare statement很像存储过程，是一种运行在后台的SQL语句集合，我们可以从使用prepare statement获得很多好处，无论是性能问题还是安全问题。

​	Prepare statement可以检查一些你绑定好的变量，这样可以保护你的程序不会受到“SQL注入式攻击”。当然，你也可以手动地检查这些变量，然而，手动的检查容易出问题，而且经常会被程序员遗忘。

​	在性能方面，当一个相同的查询被使用多次的时候，这会为你带来可观的性能优势。你可以给这些prepare statement定义一些参数，而MySQL只会解析一次。

​	最新版本的MySQL在传输prepare statement是使用二进制形式，所以这会使得网络传输非常有效率。

 

# 原理

JDBC使用步骤过程：

1、获取数据库连接

2、创建一个statement，要执行SQL语句，必须获得java.sql.Statement实例，Statement实例分为以下三种：

1）执行静态SQL语句，通常通过Statement实例实现

2）执行动态SQL，通常通过PreparedStatement实例实现，建议使用，不会造成SQL注入

3）执行数据库存储过程，通常使用CallableStatement实例实现

3、执行SQL语句，Statement接口提供了三种执行SQL语句的方法：executeQuery、executeUpdate和execute：

1）ResultSet executeQuery(String sqlString)：执行查询数据库的SQL语句，返回一个结果集（ResultSet）对象

2）int executeUpdate(String sqlString)：用于执行INSERT、UPDATE或DELETE语句以及SQL DDL语句，如CREATE TABLE和DROP TABLE等

3）execute(String sqlString)：用于执行返回多个结果集、多个更新计数或二者组合的语句

4）executeBatch()：批量执行，在执行批量事务的时候使用

4、获取结果，结果会返回两种情况：

1）执行更新返回的是本次操作影响到的记录数

2）执行查询返回的结果是一个ResultSet对象，ResultSet包含符合SQL语句中条件的所有行，并且它通过一套get方法提供了对这些行中数据的访问

5、关闭JDBC对象，操作完成以后把所有使用的JDBC对象全部都关闭，以释放JDBC资源，关闭顺序和声明顺序相反：先关闭记录集再关闭声明。

# 使用

 

# 分布式数据库实践

## TDSQL

## GoldenDB

### 开关

enable_stmt_flag配置为1，表示前后端均可以执行prepare模式；

enable_stmt_flag配置为0，表示只能前端prepare，对于写语句，proxy返回报错，驱动转化为普通SQL，对于select语句，prepare在proxy侧转化为普通SQL下发。

### 步骤

#### INSERT 

INSERT实现步骤如下：

1、获取数据库连接DBUtils.getConnection

2、创建一个PreparedStatement执行动态SQL语句，语句中的变量使用“？”代替

3、设置参数，参数的序号从1开始，参数的顺序和个数与“？”的个数对应

4、执行executeUpdate，返回插入成功数

5、Finally语句块中清理资源

代码：

public void insertEmp(){

​	PreparedStatement ps = null;

​	Connection conn = null;

​	

​	try{

​		conn = DBUtils.getConnection();

​		

​		// 预处理添加数据

​		ps = conn.prepareStatement("insert into tb.t1(emp_no,emp_name,job,hiredate,sal)" + "values(?,?,?,?,?)");

​		

​		// 设置参数

​		ps.setInt(1,NumberUtils.getRandomInt());

​		ps.setString(2,"zhangsan");

​		ps.setString(3,"DBA");

​		

​		DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");

​		Date myDate2 = dateFormat2.parse("2010-09-13");

​		ps.setFate(4,new java.sql.Date(myDate2.getTime()));

​		ps.setFloat(5,(float)200.3);

​		

​		// 执行更新

​		System.out.println("执行插入成功" + ps.executeUpdate() + "条");

​	} catch (SQLException e){

​		e.printStackTrace();

​	} catch (ParseException e){

​		e.printStackTrace();

​	} finially{

​		// 清理资源

​		DBUtils.close(ps);

​		DBUtils.close(conn);

​	}

}

#### UPDATE

UPDATE实现步骤如下：

1、获取数据库连接DBUtils.getConnection

2、创建一个PreparedStatement执行动态SQL语句，语句中的变量使用“？”代替

3、设置参数，参数的序号从1开始，参数的顺序和个数与“？”的个数对应

4、执行executeUpdate，返回插入成功数

5、Finally语句块中清理资源

代码：

public void updateEmp(){

​	PreparedStatement ps = null;

​	Connection conn = null;

​	

​	try{

​		conn = DBUtils.getConnection();

​		

​		// 预处理添加数据

​		ps = conn.prepareStatement("update db.t1 set sal=? where emp_name=?");

​		

​		// 设置参数

​		ps.setFloat(1,(float)5000.0);

​		ps.setString(2,"zhangsan");

​		

​		// 执行更新

​		System.out.println("执行更新成功" + ps.executeUpdate() + "条");

​	} catch (SQLException e){

​		e.printStackTrace();

​	} finially{

​		// 清理资源

​		DBUtils.close(ps);

​		DBUtils.close(conn);

​	}

}

#### DELETE 

DELETE实现步骤如下：

1、获取数据库连接DBUtils.getConnection

2、创建一个PreparedStatement执行动态SQL语句，语句中的变量使用“？”代替

3、设置参数，参数的顺序从1开始，参数的顺序和个数与“？”的个数对应

4、执行executeUpdate，返回插入成功数

5、Finally语句块中清理资源

代码：

public void deleteEmp(){

​	PreparedStatement ps = null;

​	Connection conn = null;

​	

​	try{

​		conn = DBUtils.getConnection();

​		

​		// 预处理添加数据

​		ps = conn.prepareStatement("update db.t1 set sal=? where emp_name=?");

​		

​		// 设置参数

​		ps.setFloat(1,(float)5000.0);

​		ps.setString(2,"zhangsan");

​		

​		// 执行更新

​		System.out.println("执行删除成功" + ps.executeUpdate() + "条");

​	} catch (SQLException e){

​		e.printStackTrace();

​	} finially{

​		// 清理资源

​		DBUtils.close(ps);

​		DBUtils.close(conn);

​	}

}

#### SELECT

SELECT实现步骤如下：

1、获取数据库连接DBUtils.getConnection

2、创建一个PreparedStatement执行动态SQL语句，语句中的变量使用“？”代替

3、设置参数，参数的序号从1开始，参数的顺序和个数与“？”的个数对应

4、执行executeQuery，返回的结果是一个ResultSet对象，ResultSet包含符合SQL语句中条件的所有行，并且它通过一套get方法提供了对这些行中数据的访问

5、获取结果数据

6、Finally语句块中清理资源

代码：

public void selectEmp(){

​	PreparedStatement ps = null;

​	Connection conn = null;

​	ResultSet rs = null;

​	

​	try{

​		conn = DBUtils.getConnection();

​		

​		// 预处理添加数据

​		ps = conn.prepareStatement("select * from db.t1 where id=?");

​		

​		// 设置参数

​		ps.setInt(1,0);

​		

​		rs = ps.executeQuery();

​		

​		while (rs.next()){

​			// 输出结果

​			System.out.println(rs.getString("job") + "\t" + rs.getString("emp_name"));

​		}

​	} catch (SQLException e){

​		e.printStackTrace();

​	} finially{

​		// 清理资源

​		DBUtils.close(rs);

​		DBUtils.close(ps);

​		DBUtils.close(conn);

​	}

}

 

#### 非自动提交

上述涉及的都是单条语句的执行，并且设置的都是自动提交模式，在多条SQL的场景下，需要设置autocommit为false开启非自动提交，当提交失败或在事务异常时需要补货异常提交事务回滚SQL。

代码：

Connection conn = null;

public void insertEmp() {

​	PreparedStatement ps = null;

​	try {

​		conn = DBUtils.getConnection();

​		conn.setAutoCommit(false);

​		

​		// 预处理添加数据

​		ps = conn.prepareStatement("insert into tb.t1(emp_no,emp_name,job,hiredate,sal)" + "values(?,?,?,?,?)");

​		

​		// 设置参数

​		ps.setInt(1,NumberUtils.getRandomInt());

​		ps.setString(2,"zhangsan");

​		ps.setString(3,"DBA");

​		

​		DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");

​		Date myDate2 = dateFormat2.parse("2010-09-13");

​		ps.setFate(4,new java.sql.Date(myDate2.getTime()));

​		ps.setFloat(5,(float)200.3);

​		

​		// 执行更新

​		System.out.println("执行插入成功" + ps.executeUpdate() + "条");

​		commit();

​	} catch (SQLException e){

​		e.printStackTrace();

​	} catch (ParseException e){

​		e.printStackTrace();

​	} finially{

​		// 清理资源

​		DBUtils.close(ps);

​		DBUtils.close(conn);

​	}

​	}

}

 

private void Commit() {

​	try {

​		conn.commit();

​	} catch(SQLException e) {

​		System.out.println(e);

​		try {

​			if (null != conn && conn.isValid(10)) {

​				// 回滚事务

​				conn.rollback();

​			} catch(Exception e1) {

​				e1.printStackTrace();

​				DBUtils.close(conn, null, null);

​				// 对新连接的验证

​				conn = DBUtils.getConn();

​			}

​		}

​	}

}

 

### JDBC参数

#### 配置方法

1、直接在url中配置，配置格式如下：

jdbc:mysql://[host:port],[host:port].../database=参数值[=参数值2]

例如：jdbc:mysql//10.220.22.22?characterEncoding=utf8

2、将属性写入属性列表，并在创建连接的时候使用，代码如下：

try {

​	Properties props = new Properties();

​	props.setProperty("useSSL","false");

​	props.setProperty("user",this.user);

​	props.setProperty("password",this.password);

​	

​	this.conn = DriverManager.getConnection(dbUrl,props);

} catch(Exception e){

​	e.printStackTrace();

​	fail();

}

 

#### 常用参数

| 参数名                      | 使用介绍                                                     |
| --------------------------- | ------------------------------------------------------------ |
| useSSL                      | 与服务器进行通信时使用SSL（真/假），默认值为“假”，url配置参数中需要添加该参数，否则会抛出告警信息 |
| loadBalanceStrategy         | 负载均衡的策略，默认值为“random”；其支持简写值为“random”、“bestResponseTime”（最小响应时间优先）；对于其他负载均衡策略，需要指定类的全名 |
| useServerPrepStms           | 如果服务器支持，是否使用服务器端预处理语句？，默认值为“真”   |
| cachePrepStms               | 驱动程序是否应对客户端预处理语句的PreparedStatements的解析过程执行缓存处理，是否应检查服务器端预处理语句的适用性以及服务器端预处理语句本身 |
| prepStmtCacaheSqlLiit       | 如果允许预处理语句缓存功能，驱动程序将执行解析缓冲处理的最大SQL |
| prepStmtCacaheSize          | 如果允许预处理语句缓冲功能，应缓冲处理多少条预处理语句       |
| chracterEncoding            | 如果使用“useUnicode”被设置为“真”，处理字符串时，驱动程序应使用什么字符编码？，默认为“autodetect” |
| useUnicode                  | 处理字符串时，驱动程序是否应使用Unicode字符编码？，仅应在驱动程序无法确定字符集映射，或你正在强制驱动程序使用MySQL不是固有支持的字符集时（如UTF-8）才应该使用。真/假，默认为“真”。 |
| characterSetResults         | 字符集，用于通知服务器以何种字符集返回结果                   |
| Use01dAliasMetadataBehavior | 驱动程序是否应该将旧行为用于列和表的“AS”子句，并且只返回ResultSetMetaData.getColumnName()或ResultSetMetaData.gtTableNaconvertToNullme()的别名（如果有）而不是原始列/表名。在5.0.x中，默认值为true。 |
| zeroDateTimeBehavior        | Java连接MySQL数据库，在操作值为0的timestamp类型时不能正确的处理，而是默认抛出一个异常，就是所见的：java.sql.SQLException:cannot convert value ‘0000-00-00 00:00:00’ from column 7 to TIMESTAMP。在JDBC连接串中有一项属性：zeroDateTimeBehavior，可以用来配置出现这种情况时的处理策略，该属性有下列三个属性值：exception：默认值，即抛出SQL state[S1009] Cannnot convert value...的异常；convertToNull：将日期转换成NULL值；round：替换成最近的日期即0001-01-01； |
| maxRows                     | 返回的最大行数（0，默认值表示返回所有行）                    |
| autoReconnect               | 驱动程序是否应尝试再次建立失效的和/或死链接？如果允许，对于在失效或死链接上发出的查询（属于当前事务），驱动程序将抛出异常，但在新事务的连接上发出一个查询时，将尝试再连接。不推荐使用该特性，因为当应用程序不能恰当处理SQLException时，它会造成与会话状态和数据一致性有关的副作用，设计它的目的仅用于下述情况，即，当你无法配置应用程序来恰当处理因死链接和或无效连接导致的SQLException时。作为可选方式，可将MySQL服务器配置“wait timeout”设置为较高的值，而不是默认的8小时。 |
| useCursorFetch              | 如果是从Java中连MySQL，使用PreparedStatement的话，默认情况下真正发给服务器端之前已经把？替换了。也就是跟普通的statement一样，在5.0开始虽然有了真正的PreparedStatement，开启方式是useCursorFetch=true |
| rewriteBatchedStatements    | 当设置rewriteBatchedStatements=true时，通过驱动程序调用executeBatch()时，可以实现高性能的批量操作。 |

 

### 应用

#### loadbalance

对于GoldenDB来说，proxy集群通常是多个，对于proxy集群这种“多master”架构，我们通常希望“负载均衡”、“读写分离”等高级特性，这就是load balancing协议所能解决的。Load balancing可以将read/write负载，分布在多个MySQL实例上。

LB协议基于“Failover协议”，即具备Failure特性，其URL格式：

jdbc:mysql:loadbalance://[host]:[port],[host]:[port],...[/database]?[property=<value>]&[property=<value>]

配置URL如下：

url=jdbc:mysql:loadbalance://10.5.7.13:8888,10.5.6.121:8888?characterEncoding=utf8&useSSL=false

 

#### prepare

当客户发送一条SQL语句给服务器后，服务器总是需要校验SQL语句的语法格式是否正确，然后把SQL编译成可执行的函数，最后才是执行SQL语句。齐总校验语法和编译说话的时间可能比执行SQL语句花的时间还要多。

如果我们需要执行多次select语句，但只要每次查询的值不同，MySQL服务器也是需要每次都去校验SSQL语句的语法格式，以及编译，这就浪费了太多的时间。如果使用预编译功能，那么只对SQL语句进行一次语法校验和编译，所以效率会跟高。

从Java中连接MySQL，使用PrepareStatement的话，默认情况下真正发给服务器端之前已经把“？”替换了，也就是跟普通的Statement一样：

// 预处理添加数据

ps = conn.prepareStatement("select * from db.t1 where emp_no=?");

ps.setInt(1,1);

rs = ps.executeQuery();

对select语句进行prepare模式设置，实际客户端发送给服务端的语句为:

select * from db.t1 where emp_no = 1 //对语句进行了替换

#### fetchsize

MySQL默认为从服务器一次抽出所有数据放在客户端内存中，fetch size参数不起作用，当一条SQL返回数据量较大的时候就会出现JVM OOM。

要一条SQL从服务器读取大量数据，不发生JVM OOM，可以采用以下方法之一：

1、当statement设置以下属性时，采用的是流数据接收方式，每次只从服务器接收部分数据，直到所有数据处理完毕，不会发生JVM OOM：

setResultSetType(ResultSet, TYPE_FORWARD_ONLY);

setFetchSize(Integer,MIN_VALUE);

2、调用statement的enableStreamingResults方法，实际上enableStreamingResults方法内部封装的就是第一种方式

3、设置连接属性useCursorFetch=true（5.0版本驱动开始支持），statement以TYPE_FORWARD_ONLY打开，再设置fetch size参数，表示采用服务器游标，每次从服务器取fetch_size条数据

示例：url配置

url=jdbc:mysql://10.220.22.22:3306?characterEncoding=utf8&useCursorFetch=true&useSSL=false

代码中添加红色标记的位置内容，即可以达到预取部分的功能：

public void testFetchSize()

{

​	PreparedStatement ps = null;

​	Connection conn = null;

​	ResultSet rs = null;

​	try {

​		conn = DBUtils.getConnection();

​		

​		// 预处理添加数据

​		ps = conn.prepareStatement("select * from db.t1");

​		// 设置每次预取的条数

​		ps.setFetchSize(1);

​		rs = ps.executeQuery();

​		while(rs.next()){

​			// 输出结果

​			System.out.println(rs.getString("job") + "\t" + rs.getString("emp_name"));

​		}

​	} catch(SQLException e) {

​		e.printStackTrace();

​	} finally {

​		// 清理资源

​		DBUtils.close(rs);

​		DBUtils.close(ps);

​		DBUtils.close(conn);

​	}

}

 

## PolarDB

## OceanBase

## TiDB

 