# Oracle导入导出

有时会需要将Oracle数据库中的数据导出到外部存储文件中，Oracle数据库中的数据表可以导出，同样这些导出文件也可以导入到Oracle数据库中。

## 导出

### **用EXP工具导出数据**

在DOS窗口下，输入以下语句，然后根据提示即可导出表。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2BC.tmp.jpg) 

其中username是登录数据库的用户名，password为用户密码。注意这里的用户不能为SYS。

【例17.6】导出数据表books，代码如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2BD.tmp.jpg) 

这里指出了导出文件的名称和路径，然后指出导出表的名称。如果要导出多个表，可以在各个表之间用逗号隔开。

导出表空间和导出表不同，导出表空间的用户必须是数据库的管理员角色。导出表空间的命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2CE.tmp.jpg) 

其中参数username/password表示具有数据库管理员权限的用户名和密码，filename.dmp表示存放备份的表空间的数据文件，tablespaces_name表示要备份的表空间名称。

【例17.7】导出表空间MYTEM，代码如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2CF.tmp.jpg) 

### **用EXPDP导出数据**

EXPDP是从ORCALE 10g开始提供的导入导出工具，采用的是数据泵技术，该技术是在数据库之间或者数据库与操作系统之间传输数据的工具。

数据泵技术的主要特性如下：

支持并行处理导入、导出任务。

支持暂停和重启导入、导出任务。

支持通过联机的方式导出或导入远端数据库中的对象。

支持在导入时实现导入过程中自动修改对象属主、数据文件或数据所在表空间。

导入／导出时提供了非常细粒度的对象控制，甚至可以详细制定是否包含或不包含某个对象。

下面开始讲述使用EXPDP导出数据的过程。

1、创建目录对象

使用EXPDP工具之前，必须创建目录对象，具体的语法规则如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2D4.tmp.jpg) 

其中参数directory_name为创建目录的名称，file_name表示存放数据的文件夹名。

【例17.8】创建目录对象MYDIR，代码如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2E1.tmp.jpg) 

结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2E2.tmp.jpg) 

2、给使用目录的用户赋权限

新创建的目录对象不是所有用户都可以使用，只有拥有该目录权限的用户才可以使用。假设备份数据库的用户是SCOTT，那么赋予权限的具体语法如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2E3.tmp.jpg) 

其中参数directory_name表示目录的名称。

【例17.9】将目录对象MYDIR权限赋予SCOTT，代码如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2F3.tmp.jpg) 

运行结果如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2F4.tmp.jpg) 

3、导出指定的表

创建完目录后，即可使用EXPDP工具导出数据，操作也是在DOS的命令窗口中完成。指定备份表的语法格式如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA2F5.tmp.jpg) 

其中参数directory_name表示存放导出数据的目录名称。file_name表示导出数据存放的文件名。table_name表示准备导出的表名，如果导出多个表，可以用逗号隔开。

【例17.10】导出数据表BOOKS，代码如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA306.tmp.jpg) 

## 导入

### **用IMP导入数据**

导入数据是导出数据的逆过程，使用EMP导出的数据，可以使用IMP导入数据。

【例17.11】使用EXP导出fruits表，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA307.tmp.jpg) 

【例17.12】使用IMP导入fruits表，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA308.tmp.jpg) 

### **用IMPDP导入数据**

使用EXPDP导出数据后，可以使用IMPDP将数据导入。

【例17.13】使用IMPDP导入BOOKS表，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsA309.tmp.jpg) 

如果数据库中BOOKS表已经存在，此时会报错，解决方式是在上面代码后加上ignore=y即可。

 

 

 