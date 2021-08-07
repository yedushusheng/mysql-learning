# *概述*

SQL_MODE是MySQL中的一个系统变量（variable），可由多个MODE组成，每个MODE控制一种行为，如是否允许除数为0，日期中是否允许'0000-00-00'值。

在MySQL 5.6中，SQL_MODE的默认值为"NO_ENGINE_SUBSTITUTION"，非严格模式。

在这种模式下，在进行数据变更操作时，如果涉及的列中存在无效值（如日期不存在，数据类型不对，数据溢出），只会提示"Warning"，并不会报错。

如果要规避上述问题，需开启SQL_MODE的严格模式。

 

参考：

https://mp.weixin.qq.com/s/-UwrdZDerW3XAGB2242ktw

 

# *模式*

不同版本默认的SQL_MODE

MySQL 5.5：空

MySQL 5.6：NO_ENGINE_SUBSTITUTION

MySQL 5.7：

ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,

NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER, NO_ENGINE_SUBSTITUTION

MySQL 8.0：

ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,

NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION

 

## *严格模式*

所谓的严格模式，即SQL_MODE中开启了STRICT_ALL_TABLES或STRICT_TRANS_TAB LES。

*STRICT_ALL_TABLES与STRICT_TRANS_TABLES的区别*

STRICT_TRANS_TABLES只对事务表开启严格模式，STRICT_ALL_TABLES是对所有表开启严格模式，不仅仅是事务表，还包括非事务表。

 

# *操作*

SQL_MODE既可在全局级别修改，又可在会话级别修改。可指定多个MODE，MODE之间用逗号隔开。

全局级别

set global sql_mode='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES';

会话级别

set session sql_mode='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES';

 

# *总结*

SQL_MODE在非严格模式下，会出现很多意料不到的结果。建议线上开启严格模式。但对于线上老的环境，如果一开始就运行在非严格模式下，切忌直接调整，毕竟两者的差异性还是相当巨大。

官方默认的SQL_MODE一直在发生变化，MySQL 5.5, 5.6, 5.7就不尽相同，但总体是趋严的，在对数据库进行升级时，其必须考虑默认的SQL_MODE是否需要调整。

在进行数据库迁移时，可通过调整SQL_MODE来兼容其它数据库的语法。

 

 