# 背景

数据类型在数据库中扮演着基础但又非常重要的角色。对数据类型的选择将影响与数据库交互的应用程序的性能。通常来说，**如果一个页内可以存放尽可能多的行，那么数据库的性能就越好**，因此选择一个正确的数据类型至关重要。另一方面，**如果在数据库中创建表时选择了错误的数据类型，那么后期的维护成本可能非常大，用户需要花大量时间来进行ALTER TABLE操作**。对于一张大表，可能需要等待更长的时间。因此，对于选择数据类型的应用程序设计人员，或是实现数据类型的DBA，又或者是使用这些数据类型的程序员，花一些时间深入学习数据类型、理解它们的基本原理是非常必要的。在选择数据类型时要格外谨慎，因为**在生产环境下更改数据类型可能是一种非常危险的操作**。

# 概述

MySQL支持所有标准SQL中的数值类型，其中包括严格数值类型（INTEGER、SMALLINT、DECIMAL和NUMERIC），以及近似数值数据类型（FLOAT、REAL和DOUBLE PRECISION），并在此基础上做了扩展。扩展后增加了TINYINT、MEDIUMINT和BIGINT这3种长度不同的整型，并增加了BIT类型，用来存放位数据。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51A7.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51A8.tmp.jpg) 

在整数类型中，按照取值范围和存储方式不同，分为tinyint、smallint、mediumint、int和bigint这5个类型。如果超出类型范围的操作，会发生“Out of range”错误提示。为了避免此类问题发生，在选择数据类型时要根据应用的实际情况确定其取值范围，最后根据确定的结果慎重选择数据类型。

对于整型数据，MySQL还支持在类型名称后面的小括号内指定显示宽度，例如int(5)表示当数值宽度小于5位的时候在数字前面填满宽度，如果不显示指定宽度则默认为int(11)。一般配合zerofill使用，顾名思义，zerofill就是用“0”填充的意思，也就是在数字位数不够的空间用字符“0”填满。

# 类型属性

## **UNSIGNED**

所有的整数类型都有一个可选属性UNSIGNED（无符号），如果需要在字段里面保存非负数或者需要较大的上限值时，可以用此选项，它的取值范围是正常值的下限取0，上限取原值的2倍，例如，tinyint有符号范围是-128～+127，而无符号范围是0～255。如果一个列指定为zerofill，则MySQL自动为该列添加UNSIGNED属性。

 

UNSIGNED属性就是将数字类型无符号化，与C、C++这些程序语言中的unsigned含义相同。例如，INT的类型范围是-2147483648～2147483647， INT UNSIGNED的范围类型就是0～4294967295。看起来这是一个不错的属性选项，特别是对于主键是自增长的类型，因为一般来说，用户都希望主键是非负数。然而在实际使用中，UNSIGNED可能会带来一些负面的影响，示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51B9.tmp.jpg) 

我们创建了一个表t，存储引擎为InnoDB。表t上有两个UNSIGNED的INT类型。输入（1，2）这一行数据，目前看来都没有问题，接着运行如下语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51BA.tmp.jpg) 

这时结果会是什么呢？会是-1吗？答案是不确定的，可以是-1，也可以是一个很大的正值，还可能会报错。在Mac操作系统中，MySQL数据库提示如下错误：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51BB.tmp.jpg) 

这个错误乍看起来非常奇怪，提示BIGINT UNSIGNED超出了范围，但是我们采用的类型都是INTUNSIGNED啊！而在另一台Linux操作系统中，运行的结果却是：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51BC.tmp.jpg) 

在发生上述这个问题的时候，有开发人员跑来和笔者说，他发现了一个MySQL的Bug， MySQL怎么会这么“傻”呢？在听完他的叙述之后，我写了如下的代码并告诉他，这不是MySQL的Bug，C语言同样也会这么“傻”。

 

那么，怎么获得-1这个值呢？这并不是一件难事，只要对SQL_MODE这个参数进行设置（NO_UNSIGNED_SUBTRACTION）即可，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51CC.tmp.jpg) 

笔者个人的看法是***\*尽量不要使用UNSIGNED，因为可能会带来一些意想不到的效果\****。另外，对于INT类型可能存放不了的数据，INTUNSIGNED同样可能存放不了，与其如此，还不如在数据库设计阶段将INT类型提升为BIGINT类型。

## **ZEROFILL**

下面通过SHOW CREATE TABLE命令来看一下t表的建表语句。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51CD.tmp.jpg) 

可以看到int（10），这代表什么意思呢？整型不就是4字节的吗？这10又代表什么呢？其实如果没有ZEROFILL这个属性，括号内的数字是毫无意义的。a和b列就是前面插入的数据，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51CE.tmp.jpg) 

但是在对列添加ZEROFILL属性后，显示的结果就有所不同了，例如对表t进行ALTER TABLE修改：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51CF.tmp.jpg) 

这里对a列进行了修改，为其添加了ZEROFILL属性，并且将默认的int（10）修改为int（4），这时再进行查找操作，返回的结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51E0.tmp.jpg) 

这次可以看到a的值由原来的1变为0001，这就是ZEROFILL属性的作用，如果宽度小于设定的宽度（这里的宽度为4），则自动填充0。***\*要注意的是，这只是最后显示的结果，在MySQL中实际存储的还是1\****。为什么是这样呢？我们可以用函数HEX来证明。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51E1.tmp.jpg) 

可以看到在数据库内部存储的还是1，0001只是设置了ZEROFILL属性后的一种***\*格式化输出\****而已。进一步思考，如果数据库内部存储的是0001这样的字符串，又怎么进行整型的加、减、乘、除操作呢？

大家可能会有所疑问，设置了宽度限制后，如果插入大于宽度限制的值，会不会截断或者插不进去报错？答案是肯定的：不会对插入的数据有任何影响，还是按照类型的实际精度进行保存，这时，宽度格式实际已经没有意义，左边不会再填充任何的“0”字符。

注：如果分布式数据库不支持该语法，则需要在业务侧修改格式化输出的格式。

## **AUTO_INCREMENT**

整数类型还有一个属性：AUTO_INCREMENT。在需要产生唯一标识符或顺序值时，可利用此属性，这个属性只用于整数类型。AUTO_INCREMENT值一般从1开始，每行增加1。在插入NULL到一个AUTO_INCREMENT列时，MySQL插入一个比该列中当前最大值大1 的值。一个表中最多只能有一个AUTO_INCREMENT列。对于任何想要使用AUTO_INCREMENT的列，应该定义为NOTNULL，并定义为PRIMARY KEY或定义为UNIQUE键。

例如，可按下列任何一种方式定义AUTO_INCREMENT列：

CREATE TABLE AI (ID INT AUTO_INCREMENT NOT NULL PRIMARY KEY);

CREATE TABLE AI(ID INT AUTO_INCREMENT NOT NULL ,PRIMARY KEY(ID));

CREATE TABLE AI (ID INT AUTO_INCREMENT NOT NULL ,UNIQUE(ID));

# SQL_MODE设置

SQL_MODE可能是比较容易让开发人员和DBA忽略的一个变量，默认为空。SQL_MODE的设置其实是比较冒险的一种设置，因为在这种设置下可以允许一些非法操作，比如可以将NULL插入NOTNULL的字段中，也可以插入一些非法日期，如“2012-12-32”。因此在生产环境中强烈建议开发人员将这个值设为严格模式，这样有些问题可以在数据库的设计和开发阶段就能发现，而如果在生产环境下运行数据库后发现这类问题，那么修改的代价将变得十分巨大。此外，正确地设置SQL_MODE还可以做一些约束（Constraint）检查的工作。

对于SQL_MODE的设置，可以在MySQL的配置文件如my.cnf和my.ini中进行，也可以在客户端工具中进行，并且可以分别进行全局的设置或当前会话的设置。下面的命令可以用来查看当前SQL_MODE的设置情况（select @@global.sql_mode或select global sql_mode）。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51E2.tmp.jpg) 

可以看到当前全局的SQL_MODE设置为空，而当前会话的设置为NO_UNSIGNED_SUBTRACTION。通过以下语句可以将当前的SQL_MODE设置为严格模式。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51E3.tmp.jpg) 

严格模式是指将SQL_MODE变量设置为STRICT_TRANS_TABLES或STRICT_ALL_TABLES中的至少一种。现在来看一下SQL_MODE可以设置的选项。	STRICT_TRANS_TABLES：在该模式下，如果一个值不能插入到一个事务表（例如表的存储引擎为InnoDB）中，则中断当前的操作不影响非事务表（例如表的存储引擎为MyISAM）。

ALLOW_INVALID_DATES：该选项并不完全对日期的合法性进行检查，只检查月份是否在1～12之间，日期是否在1～31之间。该模式仅对DATE和DATETIME类型有效，而对TIMESTAMP无效，因为TIMESTAMP总是要求一个合法的输入。

ANSI_QUOTES：启用ANSI_QUOTES后，不能用双引号来引用字符串，因为它将被解释为识别。示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51F4.tmp.jpg) 

ERROR_FOR_DIVISION_BY_ZERO：在INSERT或UPDATE过程中，如果数据被零除（或MOD（X，0）），则产生错误（否则为警告）。如果未给出该模式，那么数据被零除时MySQL返回NULL。如果用到INSERT IGNORE或UPDATE IGNORE中，MySQL生成被零除警告，但操作结果为NULL。

HIGH_NOT_PRECEDENCE NOT：操作符的优先顺序是表达式。例如，NOT a BETWEEN b ANDc被解释为NOT（a BETWEEN b AND c），在一些旧版本MySQL中， 前面的表达式被解释为（NOT a）BETWEEN b AND c。启用HIGH_NOT_PRECEDENCE SQL模式，可以获得以前旧版本的更高优先级的结果。示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51F5.tmp.jpg) 

0在-1到1之间，所以返回1，如果加上NOT，则返回0，过程如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps51F6.tmp.jpg) 

但是如果启用HIGH_NOT_PRECEDENCE模式，则SELECT NOT 0 BETWEEN -1 AND 1被解释为SELECT（NOT 0）BETWEEN -1 AND 1，结果就完全相反，如下所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5206.tmp.jpg) 

从上述例子中还能看出，在MySQL数据库中BETWEEN a AND b被解释为[a，b]。下面做两个简单的测试。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5207.tmp.jpg) 

IGNORE_SPACE：函数名和括号“（”之间有空格。除了增加一些烦恼，这个选项好像没有任何好处，要访问保存为关键字的数据库、表或列名，用户必须引用该选项。例如某个表中有user这一列，而MySQL数据库中又有user这个函数， user会被解释为函数，如果想要选择user这一列，则需要引用。

NO_AUTO_CREATE_USER：禁止GRANT创建密码为空的用户。

NO_AUTO_VALUE_ON_ZERO：该选项影响列为自增长的插入。在默认设置下，插入0或NULL代表生成下一个自增长值。如果用户希望插入的值为0，而该列又是自增长的，那么这个选项就有用了。

NO_BACKSLASH_ESCAPES：反斜杠“\”作为普通字符而非转义符。示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5208.tmp.jpg) 

NO_DIR_IN_CREATE：在创建表时忽视所有INDEX DIRECTORY和DATA DIRECTORY的选项。

NO_ENGINE_SUBSTITUTION：如果需要的存储引擎被禁用或未编译，那么抛出错误。默认用默认的存储引擎替代，并抛出一个异常。

NO_UNSIGNED_SUBTRACTION：之前已经介绍过，启用这个选项后两个UNSIGNED类型相减返回SIGNED类型。

NO_ZERO_DATE：在非严格模式下，可以插入形如“0000-00-0000:00:00”的非法日期，MySQL数据库仅抛出一个警告。而启用该选项后，MySQL数据库不允许插入零日期，插入零日期会抛出错误而非警告。

NO_ZERO_IN_DATE：在严格模式下，不允许日期和月份为零。如“2011-00-01”和“2011-01-00”这样的格式是不允许的。采用日期或月份为零的格式时MySQL都会直接抛出错误而非警告。示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5209.tmp.jpg) 

ONLY_FULL_GROUP_BY：对于GROUP BY聚合操作，如果在SELECT中的列没有在GROUP BY中出现，那么这句SQL是不合法的。因为a列不在GROUP BY从句中，示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps521A.tmp.jpg) 

PAD_CHAR_TO_FULL_LENGTH：对于CHAR类型字段，不要截断空洞数据。空洞数据就是自动填充值为0x20的数据。先看MySQL数据库在默认情况下的表现：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps521B.tmp.jpg) 

可以看到，在默认情况下，虽然a列是CHAR类型，但是返回的长度是1，这是因为MySQL数据库已经对后面的空洞数据进行了截断。若启用PAD_CHAR_TO_FULL_LENGTH选项，则反映的是实际存储的内容，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps521C.tmp.jpg) 

可以看到在CHAR列a中实际存储的值为0x61202020202020202020。

PIPES_AS_CONCAT：将“||”视为字符串的连接操作符而非或运算符，这和Oracle数据库是一样的，也和字符串的拼接函数Concat相类似。示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps521D.tmp.jpg) 

REAL_AS_FLOAT：将REAL视为FLOAT的同义词，而不是DOUBLE的同义词。	STRICT_ALL_TABLES：对所有引擎的表都启用严格模式。（STRICT_TRANS_TABLES只对支持事务的表启用严格模式）。在严格模式下，一旦任何操作的数据产生问题，都会终止当前的操作。对于启用STRICT_ALL_TABLES选项的非事务引擎来说，这时数据可能停留在一个未知的状态。这可能不是所有非事务引擎愿意看到的一种情况，因此需要非常小心这个选项可能带来的潜在影响。

下面的几种SQL_MODE设置是之前讨论的几种选项的组合。

ANSI：等同于REAL_AS_FLOAT、PIPES_AS_CONCAT和ANSI_QUOTES、IGNORE_SPACE的组合。

ORACLE：等同于PIPES_AS_CONCAT、 ANSI_QUOTES、IGNORE_SPACE、NO_KEY_OPTIONS、 NO_TABLE_OPTIONS、 NO_FIELD_OPTIONS和NO_AUTO_CREATE_USER的组合。

TRADITIONAL：等同于STRICT_TRANS_TABLES、 STRICT_ALL_TABLES、NO_ZERO_IN_DATE、NO_ZERO_DATE、 ERROR_FOR_DIVISION_BY_ZERO、NO_AUTO_CREATE_USER和NO_ENGINE_SUBSTITUTION的组合。

MSSQL：等同于PIPES_AS_CONCAT、 ANSI_QUOTES、 IGNORE_SPACE、NO_KEY_OPTIONS、NO_TABLE_OPTIONS和NO_FIELD_OPTIONS的组合。

DB2：等同于PIPES_AS_CONCAT、ANSI_QUOTES、 IGNORE_SPACE、NO_KEY_OPTIONS、NO_TABLE_OPTIONS和NO_FIELD_OPTIONS的组合。

MYSQL323：等同于NO_FIELD_OPTIONS和HIGH_NOT_PRECEDENCE的组合。

MYSQL40：等同于NO_FIELD_OPTIONS和HIGH_NOT_PRECEDENCE的组合。

MAXDB：等同于PIPES_AS_CONCAT、ANSI_QUOTES、IGNORE_SPACE、NO_KEY_OPTIONS、NO_TABLE_OPTIONS、NO_FIELD_OPTIONS和NO_AUTO_CREATE_USER的组合。

 

# 数字类型

## **整型**

MySQL数据库支持SQL标准支持的整型类型：INT、SMALLINT。此外MySQL数据库也支持诸如TINYINT、MEDIUMINT和BIGINT等类型。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps522E.tmp.jpg) 

前面已经介绍过ZEROFILL可以格式化显示整型，这里还需要提及的是，一旦启用ZEROFILL属性，MySQL数据库为列自动添加UNSIGNED属性，示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps522F.tmp.jpg) 

## 浮点数/非精确类型

对于小数的表示，MySQL 分为两种方式：浮点数和定点数。浮点数包括 float（单精度）和double（双精度），而***\*定点数则只有decimal一种表示\****。***\*定点数在MySQL内部以字符串形式存放，比浮点数更精确，适合用来表示货币等精度高的数据\****。

浮点数和定点数都可以用类型名称后加“(M,D)”的方式来进行表示，“(M,D)”表示该值一共显示M位数字（整数位+小数位），其中D位位于小数点后面，M和D又称为精度和标度。例如，定义为float(7,4)的一个列可以显示为-999.9999。MySQL保存值时进行四舍五入，因此如果在 float(7,4)列内插入 999.00009，近似结果是 999.0001。值得注意的是，浮点数后面跟“(M,D)”的用法是非标准用法，如果要用于数据库的迁移，则最好不要这么使用。float和double在不指定精度时，默认会按照实际的精度（由实际的硬件和操作系统决定）来显示，而decimal在不指定精度时，默认的整数位为10，默认的小数位为0。

 

MySQL数据库支持两种浮点类型：单精度的FLOAT类型及双精度的DOUBLE PRECISION类型。这两种类型都是非精确的类型，经过一些操作后并不能保证运算的正确性。例如M*G/G不一定等于M，虽然数据库内部算法已经使其尽可能的正确，但是结果还会有偏差。国内某些财务软件在其数据库内使用FLOAT类型作为工资类型，个人觉得并不是一件值得推崇的事情。

### **FLOAT**

FLOAT类型用于表示近似数值数据类型。SQL标准允许在关键字FLOAT后面的括号内用位来指定精度（但不能为指数范围）。MySQL还支持可选的只用于确定存储大小的精度规定。0到23的精度对应FLOAT列的4字节单精度，24到53的精度对应DOUBLE列的8字节双精度。

MySQL允许使用非标准语法：FLOAT（M，D）或REAL（M，D）或DOUBLE PRECISION （M，D）。这里，（M，D）表示该值一共显示M位整数，其中D位是小数点后面的位数。例如，定义为FLOAT（7，4）的一个列可以显示为-999.9999。MySQL在保存值时会进行四舍五入，因此在FLOAT（7，4）列内插入999.00009的近似结果是999.0001。

### **DOUBLE**

MySQL将DOUBLE视为DOUBLE PRECISION（非标准扩展）的同义词，将REAL视为DOUBLEPRECISION（非标准扩展）的同义词。若将MySQL服务器的模式设置为REAL_AS_FLOAT，那这时REAL将被视为FLOAT类型。

为了保证最大的可移植性，需要使用近似数值数据值存储的代码，使用FLOAT或DOUBLEPRECISION，并不规定精度或位数。

## 定点数/精确类型

DECIMAL和NUMERIC类型在MySQL中被视为相同的类型，***\*用于保存必须为确切精度的值\****。对于前面提到的工资数据类型，当声明该类型的列时，可以（并且通常必须）指定精度和标度，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5230.tmp.jpg) 

在上述例子中，5是精度，2是标度。精度表示保存值的主要位数，标度表示小数点后面可以保存的位数。

在标准SQL中，语法DECIMAL（M）等价于DECIMAL（M，0）。在MySQL 5.5中M的默认值是10，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5240.tmp.jpg) 

DECIMAL或NUMERIC的最大位数是65，但具体的DECIMAL或NUMERIC列的实际范围受具体列的精度或标度约束。如果分配给此类列的值的小数点后位数超过指定的标度允许的范围，值将按该标度进行转换。（具体操作与操作系统有关，一般结果均被截取到允许的位数）。

### **DECIMAL**

在标准SQL中，语法DECIMAL（M）等价于DECIMAL（M，0）。在MySQL 5.5中M的默认值是10。

### **NUMERIC**

DECIMAL或NUMERIC的最大位数是65，但具体的DECIMAL或NUMERIC列的实际范围受具体列的精度或标度约束。如果分配给此类列的值的小数点后位数超过指定的标度允许的范围，值将按该标度进行转换。（具体操作与操作系统有关，一般结果均被截取到允许的位数）。

# 位类型

位类型，即BIT数据类型可用来保存位字段的值。BIT（M）类型表示允许存储M位数值，M范围为1到64，占用的空间为（M+7）/8字节。如果为BIT（M）列分配的值的长度小于M位，在值的左边用0填充。例如，为BIT（6）列分配一个值b'101'，其效果与分配b'000101'相同。要指定位值，可以使用b'value'符，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5241.tmp.jpg) 

但是直接用SELECT进行查看会出现如下情况：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5242.tmp.jpg) 

这个值似乎是空的，其实不然，因为采用位的存储方式，所以不能直接查看，可能需要做类似如下的转化：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5243.tmp.jpg) 

 

对于BIT（位）类型，用于存放位字段值，BIT(M)可以用来存放多位二进制数，M范围从1～64，如果不写则默认为1位。***\*对于位字段，直接使用SELECT命令将不会看到结果，可以用bin()（显示为二进制格式）或者hex()（显示为十六进制格式）函数进行读取\****。

数据插入 bit 类型字段时，首先转换为二进制，如果位数允许，将成功插入；如果位数小于实际定义的位数，则插入失败。

# 字符类型

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5254.tmp.jpg) 

## **CHAR和VARCHAR**

CHAR和VARCHAR是最常使用的两种字符串类型。一般来说，CHAR（N）用来保存固定长度的字符串，VARCHAR（N）用来保存变长字符类型。对于CHAR类型，N的范围为0～255，对于VARCHAR类型，N的范围为0～65535。***\*CHAR（N）和VARCHAR（N）中的N都代表字符长度，而非字节长度。\****

注意：对于MySQL 4.1之前的版本，如MySQL 3.23和MySQL 4.0，CHAR（N）和VARCHAR（N）中的N代表字节长度。

 

***\*对于CHAR类型的字符串，MySQL数据库会自动对存储列的右边进行填充（Right Padded）操作，直到字符串达到指定的长度N\****。而在读取该列时，MySQL数据库会自动将填充的字符删除。有一种情况例外，那就是显式地将SQL_MODE设置为PAD_CHAR_TO_FULL_LENGTH。比如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5255.tmp.jpg) 

在上述这个例子中，先创建了一张表t，a列的类型为CHAR（10）。然后通过INSERT语句插入值“abc”，因为a列的类型为CHAR型，所以会自动在后面填充空字符串，使其长度为10。接下来在通过SELECT语句取出数据时会将a列右填充的空字符移除，从而得到值“abc”。通过LENGTH函数看到a列的字符长度为3而非10。

 

接着我们将SQL_MODE显式地设置为PAD_CHAR_TO_FULL_LENGTH。这时再通过SELECT语句进行查询时，得到的结果是“abc ”，abc右边有7个填充字符0x20，并通过HEX函数得到了验证。这次LENGTH函数返回的长度为10。需要注意的是，LENGTH函数返回的是字节长度，而不是字符长度。对于多字节字符集，CHAR（N）长度的列最多可占用的字节数为该字符集单字符最大占用字节数*N。例如，对于utf8下，CHAR（10）最多可能占用30个字节。通过对多字节字符串使用CHAR_LENGTH函数和LENGTH函数，可以发现两者的不同。例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5256.tmp.jpg) 

变量@a是gbk字符集的字符串类型，值为“MySQL技术内幕”，十六进制为0x4D7953514CBCBCCAF5C4DAC4BB，LENGTH函数返回13，即该字符串占用13字节，因为gbk字符集中的中文字符占用两个字节，因此一共占用13字节。CHAR_LENGTH函数返回9，很显然该字符长度为9。

 

VARCHAR类型存储变长字段的字符类型，与CHAR类型不同的是，其存储时需要在前缀长度列表加上实际存储的字符，该字符占用1～2字节的空间。当存储的字符串长度小于255字节时，其需要1字节的空间，当大于255字节时，需要2字节的空间。所以，对于单字节的latin1来说，CHAR（10）和VARCHAR（10）最大占用的存储空间是不同的， CHAR（10）占用10个字节这是毫无疑问的，而VARCHAR（10）的最大占用空间数是11字节，因为其需要1字节来存放字符长度。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5266.tmp.jpg) 

***\*注意：\****对于有些多字节的字符集类型，其CHAR和VARCHAR在存储方法上是一样的，同样需要为长度列表加上字符串的值。对于GBK和UTF-8这些字符类型，其有些字符是以1字节存放的，有些字符是按2或3字节存放的，因此同样需要1～2字节的空间来存储字符的长度。这在《MySQL技术内幕：InnoDB存储引擎》一书已经进行了非常详细的阐述和证明。

虽然CHAR和VARCHAR的存储方式不太相同，但是对于两个字符串的比较，都只比较其值，忽略CHAR值存在的右填充，即使将SQL _MODE设置为PAD_CHAR_TO_FULL_LENGTH也一样，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5267.tmp.jpg) 

 

CHAR和VARCHAR很类似，都用来保存MySQL中较短的字符串。二者的主要区别在于存储方式的不同：CHAR列的长度固定为创建表时声明的长度，长度可以为从0～255的任何值；而VARCHAR列中的值为可变长字符串，长度可以指定为0～255（MySQL 5.0.3版本以前）或者 65535（MySQL5.0.3版本以后）之间的值。在检索的时候，CHAR列删除了尾部的空格，而VARCHAR则保留这些空格。

 

在MySQL中，不同的存储引擎对CHAR和VARCHAR的使用原则有所不同，这里简单概括如下。

MyISAM存储引擎：建议使用固定长度的数据列代替可变长度的数据列。

MEMORY 存储引擎：目前都使用固定长度的数据行存储，因此无论使用 CHAR 或VARCHAR列都没有关系。两者都是作为CHAR类型处理。

InnoDB存储引擎：建议使用VARCHAR类型。***\*对于InnoDB数据表，内部的行存储格式没有区分固定长度和可变长度列（所有数据行都使用指向数据列值的头指针），因此在本质上，使用固定长度的CHAR列不一定比使用可变长度VARCHAR列性能要好。\****因而，主要的性能因素是数据行使用的存储总量。由于CHAR平均占用的空间多于VARCHAR，因此使用VARCHAR来最小化需要处理的数据行的存储总量和磁盘I/O是比较好的。

 

## **BINARY和VARBINARY**

BINARY和VARBINARY与前面介绍的CHAR和VARCHAR类型有点类似，不同的是BINARY和VARBINARY存储的是二进制的字符串，而非字符型字符串。也就是说，BINARY和VARBINARY没有字符集的概念，对其排序和比较都是按照二进制值进行对比。

BINARY（N）和VARBINARY（N）中的N指的是字节长度，而非CHAR（N）和VARCHAR（N）中的字符长度。对于BINARY（10），其可存储的字节固定为10，而对于CHAR（10），其可存储的字节视字符集的情况而定。例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5268.tmp.jpg) 

表t包含一个类型为BINARY（1）的列，因为BINARY（N）中N代表字节，而gbk字符集中的中文字符“我”需要占用2字节，所以在插入时给出警告，提示字符被截断。如果SQL_MODE为严格模式，则会直接报错。查看表t的内容，则可发现a中只存储了字符“我”的前一个字节，后一个字节被截断了。如果表t的a列中字符的类型为CHAR类型，则完全不会有上述问题，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5269.tmp.jpg) 

BINARY和VARBINARY对比CHAR和VARCHAR，第一个不同之处就是BINARY （N）和VARBINARY（N）中的N值代表的是***\*字节数\****，而非字符长度；第二个不同点是， CHAR和VARCHAR在进行字符比较时，比较的只是字符本身存储的字符，忽略字符后的填充字符，而对于BINARY和VARBINARY来说，由于是按照二进制值来进行比较的，因此结果会非常不同。例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps527A.tmp.jpg) 

对于CHAR和VARCHAR来说，比较的是字符值，因此第一个比较的返回值是1。对于BINARY和VARBINARY来说，比较的是二进制的值，“a”的十六进制为61，“a ”的十六进制为612020，显然不同，因此第二个比较的返回值为0。

第三个不同的是，对于BINARY字符串，其填充字符是0x00，而CHAR的填充字符为0x20。可能是因为BINARY的比较需要，0x00显然是比较的最小字符。例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps527B.tmp.jpg) 

 

## **TEXT和BLOB**

BLOB（Binary Large Object）是用来存储二进制大数据类型的。

根据存储长度的不同BLOB可细分为以下4种类型，括号中的数代表存储的字节数：

TINYBLOB（28）

BLOB（216）

MEDIUMBLOB（224）

LONGBLOB（232）

TEXT类型同BLOB一样，细分为以下4种类型：

TINYTEXT（28）

TEXT（216）

MEDIUMTEXT（224）

LONGTEXT（232）

在大多数情况下，可以将BLOB类型的列视为足够大的VARBINARY类型的列。同样，也可以将TEXT类型的列视为足够大的VARCHAR类型的列。

注：但是这种等价可能会因为业务侧的逻辑而不同，比如业务侧对于字符串类型的返回结果和其他类型结果返回不同的时候，客户端可能会得到不同的结果，这个是不是可以等价替换需要业务侧去确认。

 

然而，BLOB和TEXT在以下几个方面又不同于VARBINARY和VARCHAR：

1、在BLOB和TEXT类型的列上创建索引时，必须制定索引前缀的长度。而VARCHAR和VARBINARY的前缀长度是可选的。

2、BLOB和TEXT类型的列不能有默认值。

3、在排序时只使用列的前max_sort_length个字节。

max_sort_length默认值为1024，该参数是动态参数，任何客户端都可以在MySQL数据库运行时更改该参数的值。例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps527C.tmp.jpg) 

在数据库中，最小的存储单元是页（也可以称为块）。为了有效存储列类型为BLOB或TEXT的大数据类型，一般将列的值存放在行溢出页，而数据页存储的行数据只包含BLOB或TEXT类型数据列前一部分数据。

图2-2显示了InnoDB存储引擎对于BLOB类型列的存储方式，数据页由许多的行数据组成，每行数据由列组成，***\*对于列类型为BLOB的数据，InnoDB存储引擎只存储前20字节，而该列的完整数据则存放在BLOB的行溢出页中\****。在这种方式下，数据页中能存放大量的行数据，从而提高了数据的查询效率。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps528D.tmp.jpg) 

此外，在有些存储引擎内部，比如InnoDB存储引擎，会将大VARCHAR类型字符串（如VARCHAR（65530））自动转化为TEXT或BLOB类型，这在笔者的另一本书《MySQL技术内幕：InnoDB存储引擎》中已经有了非常详细的介绍，这里不再赘述。

 

## **ENUM和SET**

ENUM和SET类型都是集合类型，不同的是***\*ENUM类型最多可枚举65536个元素，而SET类型最多枚举64个元素\****。由于MySQL不支持传统的CHECK约束，因此通过ENUM和SET类型并结合SQL_MODE可以解决一部分问题。

例如，表中有一个“性别”列，规定域的范围只能是male和female，在这种情况下可以通过ENUM类型结合严格的SQL_MODE模式进行约束，过程如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps528E.tmp.jpg) 

可以看到，前两次的操作正确地插入了“male”和“female”值，而对于未定义的“bimale”，MySQL数据库在严格的SQL_MODE下抛出了报错警告，起到了一定的约束作用。

 

SET和ENUM类型非常类似，也是一个字符串对象，里面可以包含0～64个成员。根据成员的不同，存储上也有所不同。

1～8成员的集合，占1个字节。

9～16成员的集合，占2个字节。

17～24成员的集合，占3个字节。

25～32成员的集合，占4个字节。

33～64成员的集合，占8个字节。

SET 和 ENUM 除了存储之外，最主要的区别在于 ***\*SET 类型一次可以选取多个成员，而ENUM则只能选一个\****。

 

# 日期类型选择

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps528F.tmp.jpg) 

这些数据类型的主要区别如下：

如果要用来表示年月日，通常用DATE来表示。

***\*如果要用来表示年月日时分秒，通常用DATETIME表示\****。

如果只用来表示时分秒，通常用TIME来表示。

***\*如果需要经常插入或者更新日期为当前系统时间，则通常使用TIMESTAMP来表示\****。TIMESTAMP值返回后显示为“YYYY-MM-DD HH:MM:SS”格式的字符串，显示宽度固定为19个字符。如果想要获得数字值，应在TIMESTAMP 列添加“+0”。

如果只是表示年份，可以用YEAR来表示，它比DATE占用更少的空间。YEAR有2位或4位格式的年。默认是4位格式。在4位格式中，允许的值是1901～2155和0000。在2位格式中，允许的值是70～69，表示从1970～2069年。MySQL以YYYY格式显示YEAR值（从5.5.27开始，2位格式的year已经不被支持）。

每种日期时间类型都有一个有效值范围，如果超出这个范围，在默认的SQLMode下，系统会进行错误提示，并将以零值来进行存储。不同日期类型零值的表示如表：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps529F.tmp.jpg) 

## **DATETIME**

DATETIME占用8字节，是占用空间最多的一种日期类型。它既显示了日期，同时也显示了时间。其可以表达的日期范围为“1000-01-0100：00：00”到“9999-12-31 23：59：59”。

在MySQL数据库中，对日期和时间输入格式的要求是非常宽松的，以下的输入都可以视为日期类型。

2011-01-0100：01：10

2011/01/0100+01+10

20110101000110

11/01/0100@01@10

其中，最后一种类型中的“11”有些模棱两可，MySQL数据库将其视为2011还是1911呢？下面来做个测试：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52A0.tmp.jpg) 

可以看到数据库将其视为离现在最近的一个年份，这可能不是一个非常好的习惯。如果没有特别的条件和要求，还是在输入时按照标准的“YYYY-MM-DD HH：MM：SS”格式来进行。

在MySQL 5.5版本之前（包括5.5版本），数据库的日期类型不能精确到微秒级别，任何的微秒数值都会被数据库截断。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52A1.tmp.jpg) 

不过MySQL数据库提供了函数MICROSECOND来提取日期中的微秒值。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52A2.tmp.jpg) 

 MySQL的CAST函数在强制转换到DATETIME时会保留到微秒数，不过在插入后同样会截断。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52B3.tmp.jpg) 

从MySQL 5.6.4版本开始，MySQL增加了对秒的小数部分（fractional second）的支持，具体语法为：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52B4.tmp.jpg) 

其中，type_name的类型可以是TIME、DATETIME和TIMESTAMP。fsp表示支持秒的小数部分的精度，最大为6，表示微秒（microseconds）；默认为0，表示没有小数部分，同时也是为了兼容之前版本中的TIME、DATETIME和TIMESTAMP类型。对于时间函数，如CURTIME()、SYSDATE()和UTC_TIMESTAMP()也增加了对fsp的支持。例如；

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52B5.tmp.jpg) 

注意：MariaDB 5.3版本就对TIME、DATETIME、TIMESTAMP类型的微秒部分进行了支持。详细的说明可以从http://kb.askmonty.org/en/microseconds-in-mariadb中得到。

 

## **TIMESTAMP**

***\*TIMESTAMP和DATETIME显示的结果是一样的，都是固定的“YYYY-MM-DD HH:MM:SS”的形式\****。不同的是，***\*TIMESTAMP占用4字节，显示的范围为“1970-01-0100:00:00”UTC到“2038-01-19 03:14:07”UTC\****。其实际存储的内容为“1970-01-0100:00:00”到当前时间的毫秒数。

***\*注意：\****UTC协调世界时，又称世界统一时间、世界标准时间和国际协调时间。它从英文CoordinatedUniversal Time和法文Temps Universel Cordonné而来。

TIMESTAMP类型和DATETIME类型除了在显示时间范围上有所不同外，还有以下不同：

***\*在建表时，列为TIMESTAMP的日期类型可以设置一个默认值，而DATETIME不行\*******\*。\****

***\*在更新表时，可以设置TIMESTAMP类型的列自动更新时间为当前时间\****。

首先来看一个默认设置时间的例子。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52B6.tmp.jpg) 

接着来看一个执行UPDATE时更新为当前时间的例子。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52C7.tmp.jpg) 

可以发现在执行UPDATE操作后，b列的时间由原来的16:45:40更新为了16:47:39。如果执行了UPDATE操作，而实际上行并没有得到更新，那么是不会更新b列的，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52C8.tmp.jpg) 

可以看到执行UPDATE t SET a=2并没有改变行中的任何数据，显示Changed:0表示该行实际没有得到更新，故b列并不会进行相应的更新操作。

当然，可以在建表时将TIMESTAMP列设为一个默认值，也可以设为在更新时的时间，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52C9.tmp.jpg) 

***\*注意：\****

TIMESTAMP还有一个重要特点，就是和时区相关。当插入日期时，会先转换为本地时区后存放；而从数据库里面取出时，也同样需要将日期转换为本地时区后显示。这样，两个不同时区的用户看到的同一个日期可能是不一样的。

 

***\*TIMESTAMP 和 DATETIME 的表示方法非常类似，区别主要有以下几点\*******\*：\****

TIMESTAMP支持的时间范围较小，其取值范围从19700101080001到2038年的某个时间，而DATETIME是从 1000-01-01 00:00:00到 9999-12-31 23:59:59，范围更大。

表中的第一个TIMESTAMP列自动设置为系统时间。如果在一个TIMESTAMP列中插入 NULL ，则该列值将自动设置为当前的日期和时间。在插入或更新一行但不明确给TIMESTAMP列赋值时也会自动设置该列的值为当前的日期和时间，当插入的值超出取值范围时，MySQL认为该值溢出，使用“0000-00-00 00:00:00”进行填补。

TIMESTAMP 的插入和查询都受当地时区的影响，***\*更能反映出实际的日期\****。而DATETIME则只能反映出插入时当地的时区，其他时区的人查看数据必然会有误差的。

TIMESTAMP 的属性受 MySQL 版本和服务器 SQLMode 的影响很大，对于不同的版本可以参考相应的MySQL帮助文档。

 

 

## **DATE**

DATE占用3字节，可显示的日期范围为“1000-01-01”到“9999-12-31”。

## **TIME**

TIME类型占用3字节，显示的范围为“-838：59：59”～“838：59：59”。

有人会奇怪为什么TIME类型的时间可以大于23。因为TIME类型不仅可以用来保存一天中的时间，也可以用来保存时间间隔，同时这也解释了为什么TIME类型也可以存在负值。和DATETIME类型一样，TIME类型同样可以显示微秒时间，但是在插入时，数据库同样会进行截断操作，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52CA.tmp.jpg) 

## **YEAR**

YEAR类型占用1字节，并且在定义时可以指定显示的宽度为YEAR（4）或YEAR（2）。例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52DA.tmp.jpg) 

对于YEAR（4），其显示年份的范围为1901～2155；对于YEAR（2），其显示年份的范围为1970～2070。在YEAR（2）的设置下，00～69代表2000～2069年。TIME类型占用3字节，显示的范围为“-838：59：59”～“838：59：59”。

 

## **时间相关函数**

### **NOW、CURRENT_TIMESTAMP和SYSDATE**

这些函数都能返回当前的系统时间，它们之间有区别吗？先来看个例子。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52DB.tmp.jpg) 

从上面的例子看来，3个函数都是返回当前的系统时间，再来看下面这个例子：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52DC.tmp.jpg) 

在上面这个例子中人为地加入了SLEEP函数，让其等待2秒，这时可以发现SYSDATE返回的时间和NOW及CURRENT_TIMESTAMP是不同的，SYSDATE函数慢了2秒。究其原因是这3个函数有略微区别：

CURRENT_TIMESTAMP是NOW的同义词，也就是说两者是相同的。

SYSDATE函数返回的是执行到当前函数时的时间，而NOW返回的是执行SQL语句时的时间。

因此在上面的例子中，两次执行SYSDATE函数返回不同的时间是因为第二次调用执行该函数时等待了前面SLEEP函数2秒。而对于NOW函数，不管是在SLEEP函数之前还是之后执行，返回的都是执行这条SQL语句时的时间。

### **时间加减函数**

先来看一个例子。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52DD.tmp.jpg) 

可以看到，NOW（）函数可以返回时间，也可以返回一个数字，就看用户如何使用。

如果相对当前时间进行增加或减少，并不能直接加上或减去一个数字，而需要使用特定的函数，如DATE_ADD或DATE_SUB，前者表示增加，后者表示减少。其具体的使用方法有DATE_ADD（date，INTERVALexpr unit）和DATE_SUB（date，INTERVAL expr unit）。示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52EE.tmp.jpg) 

其中expr值可以是正值也可以是负值，因此可以使用DATE_ADD函数来完成DATE_SUB函数的工作，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52EF.tmp.jpg) 

还有一个问题，如果是闰月，那么DATE_ADD函数怎么处理呢？MySQL的默认行为是这样的：如果目标年份是闰月，那么返回的日期为2月29日；如果不是闰月，那么返回日期是2月28日。示例如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52F0.tmp.jpg) 

在上面的例子中使用了DAY和YEAR数据类型，其实也可以使用MICROSECOND、SECOND、MINUTE、HOUR、WEEK、MONTH等类型，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps52F1.tmp.jpg) 

### **DATE_FORMAT函数**

这个函数本身没有什么需要探讨的地方，其作用只是按照用户的需求格式化打印出日期，例如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5301.tmp.jpg) 

但是开发人员往往会错误地使用这个函数，导致非常严重的后果。例如在需要查询某一天的数据时，有些开发人员会写如下的语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5302.tmp.jpg) 

一般来说表中都会有一个对日期类型的索引，如果使用上述的语句，优化器绝对不会使用索引，也不可能通过索引来查询数据，因此上述查询的执行效率可能非常低。

# 选择数据类型

MySQL提供了大量的数据类型，为了优化存储、提高数据库性能，在任何情况下均应使用最精确的类型，即在所有可以表示该列值的类型中，该类型使用的存储最少。

## **整数和浮点数**

如果不需要小数部分，就使用整数来保存数据；如果需要表示小数部分，就使用浮点数类型。对于浮点数据列，存入的数值会对该列定义的小数位进行四舍五入。例如，假设列的值的范围为1~99999，若使用整数，则MEDIUMINT UNSIGNED是最好的类型；若需要存储小数，则使用FLOAT类型。浮点类型包括FLOAT和DOUBLE类型。DOUBLE类型精度比FLOAT类型高，因此要求存储精度较高时应选择DOUBLE类型。

## **浮点数和定点数**

浮点数FLOAT、DOUBLE相对于定点数DECIMAL的优势是：在长度一定的情况下，浮点数能表示更大的数据范围。由于浮点数容易产生误差，因此对精确度要求比较高时，建议使用DECIMAL来存储。DECIMAL在MySQL中是以字符串存储的，用于定义货币等对精确度要求较高的数据。在数据迁移中，float(M,D)是非标准SQL定义，数据库迁移可能会出现问题，最好不要这样使用。另外，两个浮点数进行减法和比较运算时也容易出问题，因此在进行计算的时候，一定要小心。进行数值比较时，最好使用DECIMAL类型。

 

注意：在今后关于浮点数和定点数的应用中，用户要考虑到以下几个原则：

浮点数存在误差问题；

对货币等对精度敏感的数据，应该用定点数表示或存储；

在编程中，如果用到浮点数，要特别注意误差问题，并尽量避免做浮点数比较；

要注意浮点数中一些特殊值的处理。

## **日期与时间类型**

MySQL对于不同种类的日期和时间有很多数据类型，比如YEAR和TIME。

如果只需要记录年份，则使用YEAR类型即可；如果只记录时间，则使用TIME类型。	如果同时需要记录日期和时间，则可以使用TIMESTAMP或者DATETIME类型。由于TIMESTAMP列的取值范围小于DATETIME的取值范围，因此存储范围较大的日期最好使用DATETIME。TIMESTAMP也有一个DATETIME不具备的属性。默认的情况下，当插入一条记录但并没有指定TIMESTAMP这个列值时，MySQL会把TIMESTAMP列设为当前的时间。因此当需要插入记录的同时插入当前时间时，使用TIMESTAMP是方便的。另外，TIMESTAMP在空间上比DATETIME更有效。

如果要记录年月日时分秒，并且记录的年份比较久远，那么最好使用 DATETIME，而不要使用TIMESTAMP。因为TIMESTAMP表示的日期范围比DATETIME要短得多。

如果记录的日期需要让不同时区的用户使用，那么最好使用TIMESTAMP，因为日期类型中只有它能够和实际时区相对应。

## **CHAR与VARCHAR**

CHAR和VARCHAR的区别如下：

●　CHAR是固定长度字符，VARCHAR是可变长度字符。

●　CHAR会自动删除插入数据的尾部空格，VARCHAR不会删除尾部空格。

CHAR是固定长度，所以它的处理速度比VARCHAR的速度要快，但是它的缺点是浪费存储空间，所以对存储不大但在速度上有要求的可以使用CHAR类型，反之可以使用VARCHAR类型来实现。

存储引擎对于选择CHAR和VARCHAR的影响：

●　对于MyISAM存储引擎：最好使用固定长度的数据列代替可变长度的数据列。这样可以使整个表静态化，从而使数据检索更快，用空间换时间。

●　对于InnoDB存储引擎：使用可变长度的数据列，因为InnoDB数据表的存储格式不分固定长度和可变长度，因此使用CHAR不一定比使用VARCHAR更好，但由于VARCHAR是按照实际的长度存储的，比较节省空间，所以对磁盘I/O和数据存储总量比较好。

## **ENUM和SET**

ENUM只能取单值，它的数据列表是一个枚举集合。它的合法取值列表最多允许有65535个成员。因此，在需要从多个值中选取一个时，可以使用ENUM。比如：性别字段适合定义为ENUM类型，每次只能从‘男’或‘女’中取一个值。

SET可取多值。它的合法取值列表最多允许有64个成员。空字符串也是一个合法的SET值。在需要取多个值的时候，适合使用SET类型，比如要存储一个人的兴趣爱好，最好使用SET类型。

ENUM和SET的值是以字符串形式出现的，但在内部，MySQL是以数值的形式存储它们的。

## **BLOB和TEXT**

一般在保存少量字符串的时候，我们会选择CHAR或者VARCHAR；而在保存较大文本时，通常会选择使用TEXT或者BLOB。二者之间的主要差别是BLOB能用来保存二进制数据，比如照片；而TEXT只能保存字符数据，比如一篇文章或者日记。TEXT和BLOB中又分别包括TEXT、MEDIUMTEXT、LONGTEXT和BLOB、MEDIUMBLOB、LONGBLOB三种不同的类型，它们之间的主要区别是存储文本长度不同和存储字节不同，用户应该根据实际情况选择能够满足需求的最小存储类型。

**（1）*****\*BLOB和TEXT值会引起一些性能问题，特别是在执行了大量的删除操作时\****。

删除操作会在数据表中留下很大的“空洞”，以后填入这些“空洞”的记录在插入的性能上会有影响。***\*为了提高性能，建议定期使用OPTIMIZE TABLE功能对这类表进行碎片整理，避免因为“空洞”导致性能问题\****。

（2）可以使用合成的（Synthetic）索引来提高大文本字段（BLOB或TEXT）的查询性能。

简单来说，合成索引就是根据大文本字段的内容建立一个散列值，并把这个值存储在单独的数据列中，接下来就可以通过检索散列值找到数据行了。但是，要注意这种技术只能用于精确匹配的查询（散列值对于类似“<”或“>=”等范围搜索操作符是没有用处的）。可以使用MD5()函数生成散列值，也可以使用SHA1()或CRC32()，或者使用自己的应用程序逻辑来计算散列值。请记住数值型散列值可以很高效率地存储。同样，如果散列算法生成的字符串带有尾部空格，就不要把它们存储在CHAR或VARCHAR列中，它们会受到尾部空格去除的影响。合成的散列索引对于那些BLOB或TEXT数据列特别有用。用散列标识符值查找的速度比搜索BLOB列本身的速度快很多。

（3）在不必要的时候避免检索大型的BLOB或TEXT值。

例如，SELECT *查询就不是很好的想法，除非能够确定作为约束条件的WHERE子句只会找到所需要的数据行。否则，很可能毫无目的地在网络上传输大量的值。这也是 BLOB 或TEXT标识符信息存储在合成的索引列中对用户有所帮助的例子。用户可以搜索索引列，决定需要的哪些数据行，然后从符合条件的数据行中检索BLOB或TEXT值。

（4）把BLOB或TEXT列分离到单独的表中。

在某些环境中，如果把这些数据列移动到第二张数据表中，可以把原数据表中的数据列转换为固定长度的数据行格式，那么它就是有意义的。这会减少主表中的碎片，可以得到固定长度数据行的性能优势。它还可以使主数据表在运行 SELECT *查询的时候不会通过网络传输大量的BLOB或TEXT值。

 

BLOB是二进制字符串，TEXT是非二进制字符串，两者均可存放大容量的信息。BLOB主要存储图片、音频信息等，而TEXT只能存储纯文本文件。

 

# 应用

## **生日问题**

## **重叠问题**

## **星期数的问题**

## **数字辅助表**

## **连续范围问题**

# DB2数据类型

| 数据类型  | 说明                                                         | 映射DB2类型 |
| --------- | ------------------------------------------------------------ | ----------- |
| INT       | 4字节整数                                                    | INTGEGER    |
| INTEGER   | 同INT                                                        | INTGEGER    |
| SMALLINT  | 2字节整数                                                    | SAMLLINT    |
| TINYINT   | 1字节整数                                                    | SAMLLINT    |
| MEDIUMINT | 3字节整数                                                    | INTGEGER    |
| BIGINT    | 8字节整数                                                    | BIGINT      |
| FLOAT     | 4字节单精度浮点数                                            | REAL        |
| DOUBLE    | 8字节双精度浮点数                                            | DOUBLE      |
| DECIMAL   | 数值型，n为数值总长度，范围1-65，s为小数点后长度，范围0-30，s必须不大于n | DECIMAL     |
| NUMERIC   | 同DECIMAL范围                                                | NUMERIC     |
| CHAR      | 定长字符串                                                   | CHAR        |
| VARCHAR   | 变长字符串                                                   | VARCHAR     |
| ENUM      | 枚举，其值只能从值列表values1、values2...NULL中选择一个值，最大值为65535 |             |
| SET       | 枚举，可取多值，其合法取值列表最大为64                       |             |
| BINARY    | 定长                                                         | BINARY      |
| VARBINARY | 变长                                                         | VARBINARY   |
| TEXT      | 最大长度为65535（2^16-1）字符的TEXT列                        | CLOB(64K)   |
| BLOB      | 最大长度为65535（2^16-1）字节的BLOB列                        | BLOB(64K)   |
| DATETIME  | 日期和时间，范围：10000-01-01 00::00:01.000000~9999-12-31 23:59.59.999999 | TIMESTAMP   |
| TIMESTAMP | UTC日期和时间，范围：1970-01-01 00::00:01.000000~2038-01-1903:14:07.999999 | TIMESTAMP   |
| DATE      | 日期，范围1000-01-01~9999-12-31                              | DATE        |
| TIME      | 时间，范围-838:59:59~838:59:59                               | TIME        |
| YEAR      | 两位或四位格式的年，默认是四位格式。在四位格式中，允许的值是1901到2155和0000。在两位格式中，允许的值是70-69，表示从1970到2069年 | SMALLINT    |

 

# Oracle数据类型

## **数据类型**

Oracle支持多种数据类型，主要有数值类型、日期／时间类型和字符串类型等。

（1）数值数据类型：包括整数类型和小数类型。

（2）日期／时间类型：包括DATE和TIMESTAMP。

（3）字符串类型：包括CHAR、VARCHAR2、NVARCHAR2、NCHAR和LONG 5种。

### **数值数据类型**

数值型数据类型主要用来存储数字，Oracle提供了多种数值数据类型，不同的数据类型提供不同的取值范围，可以存储的值范围越大，其所需要的存储空间也会越大。Oracle的数值类型主要通过number（m,n）类型来实现。使用的语法格式如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5313.tmp.jpg) 

其中m的取值范围为1～38，n的取值范围为－84～127。

number（m,n）是可变长的数值列，允许0、正值及负值，m是所有有效数字的位数，n是小数点以后的位数。如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5314.tmp.jpg) 

则这个字段的最大值是99.999，如果数值超出了位数限制就会被截取多余的位数。如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5315.tmp.jpg) 

但在一行数据中的这个字段输入575.316，则真正保存到字段中的数值是575.32。如：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5316.tmp.jpg) 

输入575.316，真正保存的数据是575。对于整数，可以省略后面的0，直接表示如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5327.tmp.jpg) 

有如下创建表的语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5328.tmp.jpg) 

id字段的数据类型为NUMBER（11），注意到后面的数字11，这表示的是该数据类型指定的最大长度，如果插入数值的位数大于11，则会弹出以下错误信息：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps5329.tmp.jpg) 

### **日期与时间类型**

Oracle中表示日期的数据类型，主要包括DATE和TIMESTAMP。具体含义和区别如表4-1所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps532A.tmp.jpg) 

### **字符串类型**

字符串类型用来存储字符串数据。Oracle中字符串类型指CHAR、VARCHAR2、NCHAR、NVARCHAR2和LONG。表4-2列出了Oracle中的字符串数据类型。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps532B.tmp.jpg) 

VARCHAR2、NVARCHAR2和LONG类型是变长类型，对于其存储需求取决于列值的实际长度，而不是取决于类型的最大可能尺寸。例如，一个VARCHAR2（10）列能保存最大长度为10个字符的一个字符串，实际的存储需要是字符串的长度。

## **对比**

| 数据类型  | 说明                                                         | 映射Oracle类型           |
| --------- | ------------------------------------------------------------ | ------------------------ |
| INT       | 4字节整数                                                    | NUMBER(10,0)             |
| INTEGER   | 同INT                                                        | NUMBER(10,0)             |
| SMALLINT  | 2字节整数                                                    | NUMBER(5,0)              |
| TINYINT   | 1字节整数                                                    | NUMBER(3,0)              |
| MEDIUMINT | 3字节整数                                                    | NUMBER(7,0)              |
| BIGINT    | 8字节整数                                                    | NUMBER(20,0)             |
| FLOAT     | 4字节单精度浮点数                                            | NUMBER                   |
| DOUBLE    | 8字节双精度浮点数                                            | BINARY_DOUBLE            |
| DECIMAL   | 数值型，n为数值总长度，范围1-65，s为小数点后长度，范围0-30，s必须不大于n | NUMBER([1-38],[-84-127]) |
| NUMERIC   | 同DECIMAL范围                                                | NUMERIC                  |
| CHAR      | 定长字符串                                                   | CHAR                     |
| VARCHAR   | 变长字符串                                                   | CHAR                     |
| ENUM      | 枚举，其值只能从值列表values1、values2...NULL中选择一个值，最大值为65535 |                          |
| SET       | 枚举，可取多值，其合法取值列表最大为64                       |                          |
| BINARY    | 定长                                                         | RAW(1-2000)              |
| VARBINARY | 变长                                                         | RAW(1-2000)              |
| TEXT      | 最大长度为65535（2^16-1）字符的TEXT列                        | CLOB                     |
| BLOB      | 最大长度为65535（2^16-1）字节的BLOB列                        | BLOB                     |
| DATETIME  | 日期和时间，范围：10000-01-01 00::00:01.000000~9999-12-31 23:59.59.999999 | DATE                     |
| TIMESTAMP | UTC日期和时间，范围：1970-01-01 00::00:01.000000~2038-01-1903:14:07.999999 | DATE                     |
| DATE      | 日期，范围1000-01-01~9999-12-31                              | DATE                     |
| TIME      | 时间，范围-838:59:59~838:59:59                               | DATE                     |
| YEAR      | 两位或四位格式的年，默认是四位格式。在四位格式中，允许的值是1901到2155和0000。在两位格式中，允许的值是70-69，表示从1970到2069年 | NUMBER(3,0)              |

 

## **选择**

Oracle提供了大量的数据类型，为了优化存储，提高数据库性能，在任何情况下均应使用最精确的类型，即在所有可以表示该列值的类型中，该类型使用的存储最少。

1、整数和小数

数值数据类型只有NUMBER型，但是NUMBER功能不小，它可以存储正数、负数、零、定点数和精度为30位的浮点数。格式为number（m，n），其中m为精度，表示数字的总位数，它在1～38；n为范围，表示小数点右边的数字的位数，它在-84～127。

如果不需要小数部分，则使用整数来保存数据，可以定义为number（m，0）或者number（m）。如果需要表示小数部分，则使用number（m，n）。

2、日期与时间类型

如果只需要记录日期，则可以使用DATE类型。如果需要记录日期和时间，可以使用IMESTAMP类型。特别是需要显示上午、下午或者时区时，必须使用IMESTAMP类型。

3、CHAR与VARCHAR之间选择

CHAR和VARCHAR的区别：CHAR是固定长度字符，VARCHAR是可变长度字符。CHAR会自动补齐插入数据的尾部空格，VARCHAR不会补齐尾部空格。CHAR是固定长度，所以它的处理速度比VARCHAR2的速度要快，但是它的缺点就是浪费存储空间。所以对存储不大，但在速度上有要求的可以使用CHAR类型，反之可以使用VARCHAR2类型来实现。

VARCHAR2虽然比CHAR节省空间，但是如果一个VARCHAR2列经常被修改，而且每次被修改的数据的长度不同，这会引起“行迁移”（Row Migration）现象，而这造成多余的I/O，是数据库设计和调整中要尽力避免的，在这种情况下用char代替VARCHAR2会更好一些。当然还有一种情况就是像身份证这种长度几乎不变的字段可以考虑使用CHAR，以获得更高的效率。