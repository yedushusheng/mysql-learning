# 概述

正确的用法：

select distinct column1,column2 from table;

这样就可以查询出来column1和column2不重复的行。

错误的用法：

select column1,distinct column2 from table;

这样是错误用法，distinct只能用在最左边。

# 原理

distinct oracle改进后使用hash unique算法，效率已经有了很大提高。但是仍然会对结果排序，所以性能有一定的影响，所以在非必要的情况下不建议使用。

# 优化

https://blog.csdn.net/shengsummer/article/details/31010219

## group by

使用group by代替distinct：

http://www.itpub.net/thread-1392256-1-1.html

## exists

使用exists代替distinct：

http://blog.itpub.net/10856805/viewspace-1000690

## 其他

 