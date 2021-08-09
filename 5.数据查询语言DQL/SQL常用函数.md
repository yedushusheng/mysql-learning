# **概述**

事先提供好的一些功能可以直接使用，函数可以用在SELECT语句及其子句，也可以在UPDATE，DELETE语句当中。

# **分类**

## **字符函数**

| 名称                | 描述                                                         |
| ------------------- | ------------------------------------------------------------ |
| CHAR_LENGTH(str)    |                                                              |
| concat()            | 字符连接                                                     |
| concat_ws()         | 使用指定的分隔符进行字符连接                                 |
| format()            | 数字格式化                                                   |
| lower()/lcase()     | 转换成小写字母                                               |
| upper()/ucase()     | 转换成大写字母                                               |
| left()              | 获取左侧字符                                                 |
| right()             | 获取右侧字符                                                 |
| LPAD(s1,len,s2)     | LPAD(s1,len,s2)返回字符串s1，其左边由字符串s2填补到len字符长度。假如s1的长度大于len，则返回值被缩短至len字符。 |
| RPAD(s1,len,s2)     | RPAD(s1,len,s2)返回字符串sl，其右边被字符串s2填补至len字符长度。假如字符串s1的长度大于len，则返回值被缩短到len字符长度。 |
| length()            | 获取字符串长度                                               |
| ltrim()             | 删除前导空格                                                 |
| rtrim()             | 删除后续空格                                                 |
| trim()              | 删除前导和后续空格                                           |
| substring()         | 字符串截取                                                   |
| [NOT] LIKE          | 模式匹配                                                     |
| replace()           | 字符串替换                                                   |
| INSERT(s1,x,len,s2) | INSERT(s1,x,len,s2)返回字符串s1，其子字符串起始于x位置和被字符串s2取代的len字符。如果x超过字符串长度，则返回值为原始字符串。假如len的长度大于其他字符串的长度，则从位置x开始替换。若任何一个参数为NULL，则返回值为NULL。 |

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB756.tmp.jpg) 

### **CONCAT**

CONCAT(S1,S2,…Sn)函数：把传入的参数连接成为一个字符串。

### LOWER/UPPER

LOWER(str)和UPPER(str)函数：把字符串转换成小写或大写。

### LEFT/RIGHT

LEFT(str,x)和RIGHT(str,x)函数：分别返回字符串最左边的x个字符和最右边的x个字符。如果第二个参数是NULL，那么将不返回任何字符串。

### LPAD/RPAD

LPAD(str,n,pad)和RPAD(str,n,pad)函数：用字符串pad对str最左边和最右边进行填充，直到长度为n个字符长度。

### LTRIM/RTRIM

LTRIM(str)和RTRIM(str)函数：去掉字符串str左侧和右侧空格。

### **REPEAT**

REPEAT(str,x)函数：返回str重复x次的结果。

### **REPLACE**

REPLACE(str,a,b)函数：用字符串b替换字符串str中所有出现的字符串a。

### **STRCMP**

STRCMP(s1,s2)函数：比较字符串s1和s2的ASCII码值的大小。

### **TRIM**

TRIM(str)函数：去掉目标字符串的开头和结尾的空格。

### **SUBSTRING**

SUBSTRING(str,x,y)函数：返回从字符串str中的第x位置起y个字符长度的字串。

## **数值函数**

数学函数主要用来处理数值数据，主要的数学函数有绝对值函数、三角函数（包括正弦函数、余弦函数、正切函数、余切函数等）、对数函数、随机数函数等。在有错误产生时，数学函数将会返回空值NULL。

| 名称              | 描述                                                         |
| ----------------- | ------------------------------------------------------------ |
| ceil()/ceiling(x) | 进一取整（返回不小于x的最小整数值，返回值转化为一个BIGINT）  |
| floor()           | 舍一取整                                                     |
| div()             | 整数除法                                                     |
| mod()             | 取余数（取模）                                               |
| power()           | 幂运算                                                       |
| EXP(x)            | e的x乘方后的值                                               |
| round()           | 四舍五入                                                     |
| truncate()        | 数字截取                                                     |
| rand()/rand(x)    | 随机数RAND(x)返回一个随机浮点值v，范围在0到1之间（0 ≤ v ≤ 1.0）。若已指定一个整数参数x，则它被用作种子值，用来产生重复序列。 |
| ABS(x)            | X的绝对值                                                    |
| SQRT(x)           | 非负数x的二次方根                                            |
| SIGN(x)           | x的值为负、零或正时返回结果依次为-1、0或1                    |

### **ABS**

ABS(x)函数：返回x的绝对值。

### **CEIL**

CEIL(x)函数：返回大于x的最小整数。

### **FLOOR**

FLOOR(x)函数：返回小于x的最大整数，和CEIL的用法刚好相反。

### **DIV**

### **MOD**

MOD(x,y)函数：返回x/y的模。

### **RAND**

RAND()函数：返回0～1内的随机值。

### **ROUND**

ROUND(x,y)函数：返回参数x的四舍五入的有y位小数的值。

### **TRUNCATE**

TRUNCATE(x,y)函数：返回数字x截断为y位小数的结果。

注意：TRUNCATE和ROUND的区别在于TRUNCATE仅仅是截断，而不进行四舍五入。

 

## **比较运算符与函数**

| 名称                      | 描述               |
| ------------------------- | ------------------ |
| [NOT] BETWEEN ... AND ... | [不]在范围内       |
| [NOT] IN()                | [不]在列出值范围内 |
| IS [NOT] NULL             | [不]为空           |

 

## **日期和时间函数**

| 名称          | 描述           |
| ------------- | -------------- |
| now()         | 当前日期和时间 |
| curdate()     | 当前日期       |
| curtime()     | 当前时间       |
| date_add()    | 日期变化       |
| datediff()    | 日期差值       |
| date_format() | 日期格式化     |

### **CURDATE**

CURDATE()函数：返回当前日期，只包含年月日。

### **CURTIME**

CURTIME()函数：返回当前时间，只包含时分秒。

### **NOW**

NOW()函数：返回当前的日期和时间，年月日时分秒全都包含。

### **UNIX_TIMESTAMP**

UNIX_TIMESTAMP(date)函数：返回日期date的UNIX时间戳。

### **FROM_UNIXTIME**

FROM_UNIXTIME(unixtime)函数：返回 UNIXTIME 时间戳的日期值，和UNIX_TIMESTAMP(date)互为逆操作。

### **WEEK****/****YEAR**

WEEK(DATE)和 YEAR(DATE)函数：前者返回所给的日期是一年中的第几周，后者返回所给的日期是哪一年。

### **HOUR****/****MINUTE**

HOUR(time)和MINUTE(time)函数：前者返回所给时间的小时，后者返回所给时间的分钟。

### **MONTHNAME**

MONTHNAME(date)函数：返回date的英文月份名称。

### **DATE_FORMAT**

DATE_FORMAT(date,fmt)函数：按字符串 fmt 格式化日期 date 值，此函数能够按指定的格式显示日期，可以用到的格式符如表5-4所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB767.tmp.jpg) 

### **DATE_ADD**

DATE_ADD(date,INTERVAL expr type)函数：返回与所给日期date相差 INTERVAL时间段的日期。其中INTERVAL是间隔类型关键字，expr是一个表达式，这个表达式对应后面的类型， type是间隔类型，MySQL提供了13种间隔类型，如表5-5所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB768.tmp.jpg) 

### **DATEDIFF**

DATEDIFF（date1，date2）函数：用来计算两个日期之间相差的天数。

## **信息函数**

| 名称             | 描述               |
| ---------------- | ------------------ |
| connection_id()  | 连接ID             |
| datebase()       | 当前数据库         |
| last_insert_id() | 最后插入记录的ID号 |
| user()           | 当前用户           |
| version()        | 版本信息           |

 

## **聚合函数**

聚合函数aggregation function又称为组函数，默认情况下，聚合函数对当前所在表当作一个组进行统计。

聚合函数的特点：

1、每个组函数接收一个参数（字段名或者表达式），统计结果中默认忽略字段为NULL的记录

**2、*****\*要想列举值为NULL的列也参与组函数的极端，必须使用IFNULL函数对NULL值做转换\****

3、不允许出现嵌套，比如sum(max(xx))

4、聚合函数中，方差和标准差函数会对数值参数返回DOUBLE值；SUM()和AVG()对精确值参数（integer或DECIMAL）返回DECIMAL值，而对近似值参数（FLOAT或DOUBLE）返回DOUBLE值

5、时间类型的参数对SUM()和AVG()无效

它们会把时间类型的值转换成数字，丢弃第一个非数字字符后的所有信息。如果要解决这个问题，先要将时间类型的值转换为合适的数值单元，在执行聚合操作后，再转换回时间值。

6、BIT_AND()，BIT_OR()和BIT_XOR()聚合函数执行位操作

它们需要BIGINT（64位整数）参数并返回BIGINT值。其他类型的参数将转换为BIGINT并可能发生截断。而在MySQL 8.0中，允许位操作采用二进制字符串类型参数（BINARY，VARBINARY和BLOB类型）。

 

***\*常见聚合函数：\****

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB769.tmp.jpg)

| 名称    | 描述   |
| ------- | ------ |
| acg()   | 平均值 |
| count() | 计数   |
| max()   | 最大值 |
| min()   | 最小值 |
| sum()   | 求和   |

### **AVG**

AVG([DISTINCT] expr)函数返回expr的平均值。

DISTINCT则用于返回expr的不同值的平均值。如果没有匹配的行，AVG()返回null。

 

### **COUNT**

COUNT(expr)返回SELECT语句检索的行中expr的非NULL值的计数。返回结果是BIGINT值。如果没有匹配的行，count()返回0.

 

### **COUNT(DISTINCT ...)**

COUNT(DISTINCT expr,[expr...])函数返回不相同且非NULL的expr值的行数。如果没有匹配的行，则COUNT(DISTINCT)返回0。

　　在MySQL中，您可以通过提供表达式列表，来获取不包含NULL的不同表达式组合的数量。而在标准表达式中，必须在COUNT(DISTINCT ...)中对所有表达式进行连接。

 

### **GROUP_CONCAT()**

语法格式：

GROUP_CONCAT([DISTINCT] expr [,expr ...]

​       [ORDER BY {unsigned_integer | col_name | expr}

​         [ASC | DESC] [,col_name ...]]

​       [SEPARATOR str_val])

group_concat()的结果将截断为group_concat_max_len系统变量所设置的最大长度，该变量的默认值为1024。

​	而返回值是非二进制或二进制字符串，具体取决于参数是非二进制还是二进制字符串。

​	返回的结果类型为TEXT或BLOB，除非group_concat_max_len小于或等于512，这种情况下，结果类型为VARCHAR或VARBINARY。

 

### **JSON_ARRAYAGG**

JSON_ARRAYAGG(col or expr)将结果集聚合为单个JSON数组，其元素由参数列的值组成。此数组中元素的顺序未定义。该函数作用于计算为单个值的列或表达式，异常返回NULL。

 

### **SON_OBJECTAGG**

JSON_OBJECTAGG(key,value)两个列名或表达式作为参数，第一个用作键，第二个用作值，并返回包含键值对的JSON对象。

如果结果不包含任何行，或者出现错误，则返回NULL。如果任何键名称为NULL或参数数量不等于2，则会发生错误。

 

## **加密函数**

| 名称       | 描述         |
| ---------- | ------------ |
| md5()      | 信息摘要算法 |
| password() | 密码算法     |

password(plainText)：旧版（OLD_PASSWORD()）加密后长度16位，新版41位select length(password("123456"))可以用来查看加密后的字符串的长度。这种加密方法依赖数据库，需要保持连接状态，即有一定的网络开销。

md5(plainText)：加密后长度32位，该加密算法不可逆，使用的是信息摘要算法，如果拿来做压缩亦为有损压缩。理论上即使有反向算法也无法恢复信息原样。常被用来检验下载数据的完整性。

sha(plainText)：

sha1(plainText)：

encode(plainText,key)和decode(cipherText)：

AES_ENCRYPT(plainText,key)：返回用密钥key对明文利用高级加密算法加密后的结果，结果是一个二进制字符串，以BLOB类型存储。

AES_DECRYPT(cipherText,key)：针对上一个函数的解密算法

ENCRYPT（plainText,key）：使用UNIXcrypt()函数，用关键词salt（一个可以唯一确定口令的字符串，类似密钥）加密明文。

## **流程函数**

流程函数也是很常用的一类函数，用户可以使用这类函数在一个 SQL 语句中实现条件选择，这样做能够提高语句的效率。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB76A.tmp.jpg) 

注：Oracle中的流程函数decode。

### **IF**

### **IFNULL**

### **CASE WHEN**

 

## **其他函数**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB77B.tmp.jpg) 

### **DATABASE()**

DATABASE()函数：返回当前数据库名。

### **VERSION()**

VERSION()函数：返回当前数据库版本。

### **USER()**

USER()函数：返回当前登录用户名。

### **INET_ATON(IP)**

INET_ATON(IP)函数：返回IP地址的网络字节序表示。

### **INET_NTOA(num)**

INET_NTOA(num)函数：返回网络字节序代表的IP地址。

### **PASSWORD(str)**

PASSWORD(str)函数：返回字符串str的加密版本，一个41位长的字符串。

### **MD5(str)**

MD5(str)函数：返回字符串str的MD5值，常用来对应用中的数据进行加密。

 

# **Oracle函数**

## **数字函数**

## **字符串函数**

### **LENGTH**

LENGTH（str）返回值为字符串的字节长度。

### **CONCAT**

CONCAT（s1,s2）返回结果为连接参数产生的字符串。

### **INSTR**

INSTR（s,x）返回x字符在字符串s的位置。

### **LOWER**

LOWER（str）可以将字符串str中的字母字符全部转换成小写字母。

### **UPPER**

UPPER（str）可以将字符串str中的字母字符全部转换成大写字母。

### **INITCAP**

INITCAP（str）将输入的字符串单词的首字母转换成大写。如果不是两个字母连在一起，则认为是新的单词，例：a_b、a,b、a b，类似前面这些情况，a和b都会转换成大写。

### **SUBSTR**

SUBSTR（s,m,n）函数获取指定的字符串。其中参数s代表字符串，m代表截取的位置，n代表截取长度。

### **REPLACE**

REPLACE（s1,s2,s3）是一个替换字符串的函数。其中参数s1表示搜索的目标字符串，S2表示在目标字符串中要搜索的字符串。s3是可选参数，用它替换被搜索到的字符串，如果该参数不用，表示从s1字符串中删除搜索到的字符串。

### **LTRIM**

LTRIM（s,n）函数将删除指定的左侧字符，其中s是目标字符串，n是需要查找的字符。如果n不指定，则表示删除左侧的空格。

### **RTRIM**

RTRIM（s,n）函数将删除指定的右侧字符，其中s是目标字符串，n是需要查找的字符。如果n不指定，则表示删除右侧的空格。

### **TRIM**

TRIM函数将删除指定的前缀或者后缀的字符，默认删除空格。具体的语法格式如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB77C.tmp.jpg) 

其中，LEADING删除trim_source的前缀字符；TRAILING删除trim_source的后缀字符；BOTH删除trim_source的前缀和后缀字符；trim_character删除指定字符，默认删除空格；trim_source指被操作的源字符串。

### **NLS_CHARSET_ID**

NLS_CHARSET_ID（string）函数可以得到字符集名称对应的ID。参数string表示字符集的名称。

### **NLS_CHARSET_NAME**

NLS_CHARSET_NAME（number）函数可以得到字符集ID对应的名称。参数number表示字符集的ID。

## **日期和时间函数**

### **SYSDATE**

SYSDATE（）函数获取当前系统日期。

### **DBTIMEZONE**

DBTIMEZONE函数返回数据库所在的时区。

### **LAST_DAY**

LAST_DAY（date）函数返回参数指定日期对应月份的最后一天。

### **NEXT_DAY**

NEXT_DAY（date,char）函数获取当前日期向后的一周对应日期，char表示是星期几，全称和缩写都允许，但必须是有效值。

### **EXTRACT**

EXTRACT（datetime）函数可以从指定的时间中提取特定部分，例如提取年份、月份或者时等。

### **MONTHS_BETWEEN**

MONTHS_BETWEEN（date1,date2）函数返回date1和date2之间的月份数。

## **转换函数**

### **ASCIISTR**

ASCIISTR（char）函数可以将任意字符串转换为数据库字符集对应的ASCII字符串。char为字符类型。

### **BIN_TO_NUM**

BIN_TO_NUM（）函数可以实现将二进制转换成对应的十进制。

### **CAST**

在Oracle中，用户如果想把数字转化为字符或者字符转化为日期，通常使用CAST（expr astype_name）函数来完成。

### **TO_CHAR**

TO_CHAR函数将一个数值型参数转换成字符型数据。具体语法格式如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB77D.tmp.jpg) 

其中参数n代表数值型数据。参数ftm代表要转换成字符的格式。nlsparam参数代表指定fmt的特征，包括小数点字符、组分隔符和本地钱币符号。

### **TO_DATE**

TO_DATE函数将一个字符型数据转换成日期型数据。具体语法格式如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB77E.tmp.jpg) 

其中参数char代表需要转换的字符串，参数ftm代表要转换成字符的格式，nlsparam参数控制格式化时使用的语言类型。

### **TO_NUMBER**

TO_NUMBER函数将一个字符型数据转换成数字数据。具体语法格式如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB78E.tmp.jpg) 

其中参数expr代表需要转换的字符串。参数ftm代表要转换成数字的格式，nlsparam参数指定fmt的特征，包括小数点字符、组分隔符和本地钱币符号。

 

## **系统信息函数**

### **USER**

USER函数返回当前会话的登录名。

### **USERENV**

USERENV函数返回当前会话的信息。使用的语法格式如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB78F.tmp.jpg) 

当参数为Language时，返回会话对应的语言、字符集等。当参数为SESSION，可返回当前会话的ID。当参数为ISDBA，可以返回当前用户是否为DBA。

 

## **其他函数**

### **decode**

decode()函数用于多值判断。其执行过程类似于解码操作。该函数最常见的应用为，实现类似if else的功能。

### **nvl**

nvl()函数用于处理某列的值。该函数有两个参数，第一个参数为要处理的列。如果其值为空，则返回第二个参数的值，否则，将返回列值。

### **cast**

cast()函数最常用的场景是转换列的数据类型，以创建新表。

 