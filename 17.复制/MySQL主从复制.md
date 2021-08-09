# 背景

MySQL从3.23版本开始提供复制的功能。复制是指将主数据库的DDL和DML操作通过二进制日志传到复制服务器（也叫从库）上，然后在从库上对这些日志重新执行（也叫重做），从而使得从库和主库的数据保持同步。

MySQL支持一台主库同时向多台从库进行复制，从库同时也可以作为其他服务器的主库，实现链状的复制。

MySQL复制的优点主要包括以下3个方面：

1、如果主库出现问题，可以快速切换到从库提供服务；

2、可以在从库上执行查询操作，降低主库的访问压力；

3、可以在从库上执行备份，以避免备份期间影响主库的服务。

***\*注意：\****由于 MySQL 实现的是异步的复制，所以主从库之间存在一定的差距，在从库上进行的查询操作需要考虑到这些数据的差异，一般只有更新不频繁的数据或者对实时性要求不高的数据可以通过从库查询，实时性要求高的数据仍然需要从主数据库获得。

 

***\*为什么要做主从复制？\****

1、在业务复杂的系统中，有这么一个情景，有一句sql语句需要锁表，导致暂时不能使用读的服务，那么就很影响运行中的业务，使用主从复制，让主库负责写，从库负责读，这样，即使主库出现了锁表的情景，通过读从库也可以保证业务的正常运作。

2、从库可以做数据的热备

3、架构的扩展。业务量越来越大，I/O访问频率过高，单机无法满足，此时做多库的存储，降低磁盘I/O访问的频率，提高单个机器的I/O性能。

随着网站业务的扩展、数据不断增加、用户也越来越多，单台数据库的压力也就越来越大，只通过数据库参数调整或者SQL优化基本已无法满足要求，这时可以采用读/写分离的策略来改变现状。

## **单点故障**

单点故障（SPOF）指某个系统的一部分，如果它停止工作了，将会导致整个系统停止工作。在我们的架构设计中，要尽量避免单点故障。

要避免单点故障，我们首先应找到可能导致整个系统失效的关键的组件，综合评估，在满足我们可用性的要求下，应该如何避免单点故障，或者减少单点故障爆发的可能性。

一般我们靠增加冗余的方式来解决单点故障，冗余的级别和方式不一。从设备的角度，我们可以对主机的单个组件进行冗余，比如使用多个网卡，我们也可以对整个主机所有的关键部件进行冗余，在更高的级别上，我们可以对整个主机进行冗余，或者对整个IDC机房进行冗余。从组织管理的角度，我们还可以对维护数据库的人员进行冗余。MySQL的主从架构本质上也是增加一个冗余的从节点来提高可用性。

如下将详细介绍一些常用的解决单点故障的技术。

1）使用负载均衡软硬件设备，比如对于一组读库，我们可以在前端放置一个负载均衡设备，以解决后端某个从库异常的故障，你可能还需要考虑负载均衡设备自身的高可用性。

2）使用共享存储、网络文件系统、分布式文件系统或复制的磁盘（DRBD）。

传统的数据库产品，如Oracle RAC使用的是基于SAN的共享存储存放数据，数据库的多个实例并发访问共享存储存取数据，应用通过配置在数据库主机上的虚拟IP访问数据库，如果某个数据库主机宕机，其他数据库实例接管虚拟IP，那么应用仍然可以访问到数据库。

MySQL官方介绍了一个实现网络RAID的方案DRBD。也有人使用网络文件系统NFS或分布式文件系统存储共享的数据库文件。

使用共享存储是比较传统的做法，由于成本比较高，且共享存储自身可能也会成为单点，因此互联网架构中很少使用这类方案，有些人为了确保主库数据的安全性，把二进制日志存放到共享存储中，这也是一种可以接受的做法。

使用网络RAID，即DRBD虽然是可行的，但现实中用得并不多，主要原因在于目前的SSD已经足够快了，DRBD自身会成为整个系统的瓶颈，而且会导致主机的浪费，因为只有一半的主机可用。因此作为折中的方案，可以只用DRBD复制二进制日志。

笔者没有使用过NFS存储共享的数据库文件，网络文件系统难以实现高吞吐，NFS更适用的场景是存放一些共享的备份文件。有些人选择使用分布式文件系统来存放数据库文件，由于分布式文件系统本身的复杂性，你需要考虑它的维护成本及团队人员的技能等因素，如果传统的方法可以存放数据文件，那么不建议使用这么一个“笨重”的方案。

3）基于主从复制的数据库切换。

目前MySQL使用最多的高可用方案是MySQL数据库主从切换，也就是说，基于主从复制的冗余。通过对主库增加一个或多个副本（备库），在发生故障的情况下，把生产流量切换到副本上，以确保服务的正常运行。随着主机性能的发展，基于主机之间的高可用是主流也是趋势。

## **MySQL故障切换**

基于数据库复制架构的切换是目前MySQL高可用的主流解决方案。我们把数据库成双成对地设置成主从架构，应用平时只访问主库，如果主库宕机了，从库可以替补使用，且满足一定的条件，那么我们可以把应用的流量切换到从库，使服务仍然可用。

由于数据库切换依赖的是MySQL的主从复制架构，所以你需要深刻了解MySQL的复制原理和机制，确保MySQL的同步一直是可用的。你需要尽可能地保证数据已经同步到了从库，以免丢失数据。

数据库可以配置成主从架构，也可以配置成主主架构。我们建议使用主从架构，这是最稳健、最可靠的方案。有些人把数据库配置成主主架构的原因是，他们认为这样做可以更便于切换及回切。配置成主主架构的时候，你需要小心处理主键冲突等复制问题，在从库上进行操作时需要非常小心，因为错误的操作也会同步到主库。配置成主主架构只是为了方便切换，现实中，仍然需要确保仅有一个主库提供服务，另一个节点可作为备用。

除了单点故障，有时我们也可以为了其他目的进行切换，比如在大表中修改表结构，为了避免影响业务，临时把所有流量切换到从库。这种情况下，配置成主主架构会更方便。

为了简化容量管理，以确保切换数据库流量之后，数据库主机能够正常提供服务，应该确保主备机器的软硬件配置尽量一致。由于数据库从库的数据一般并未“预热”，热点数据也没有被加载到内存，所以在进行流量切换的初始时刻，可能会难以接受其性能，你可以预先运行一些SQL预热数据。

对于写入事务比较多的业务，在发生故障的情况下进行主从切换，可能会丢失数据和导致主从不一致，一般情况下，互联网业务的可用性会高于数据一致性，丢失很少的事务是可以接受的。一些数据也是允许丢失的，比如丢失一些评论是可以接受的，如果需要绝对的不能丢失数据，那么你的方案的实现成本会很高，比如为了确保不丢失主库的日志，你可能需要共享存储来存储主库的日志，还可能需要使用全同步或半同步的技术确保数据的变更已经被传送到了从库。

对于数据库的切换，我们有如下的一些方式。

1）通过修改程序的配置文件实现切换。程序配置文件里有数据库的路由信息，我们可以修改程序的配置文件实现数据库流量的切换，在大多数情况下，我们需要重启应用。比如JAVA服务，默认配置下，我们需要重新启动应用服务。在服务非常多的情况下，也有把数据库配置信息存储在数据库中的。

2）修改内网DNS。

我们可以在生产环境中配置内网DNS，通过修改内网DNS指向的数据库服务器的IP，实现主库在故障情况下的切换，这种方式，往往也需要重启应用服务。由于内网DNS可能不归属于DBA团队掌控，DNS服务器的维护和高可用也需要成本，而且更改内网DNS也需要时间，所以这种方式用得比较少。

3）修改主机的hosts文件。

/etc/hosts里可以配置与数据库服务器的域名对应的IP，但是还不够理想。而且在有很多应用服务器的时候，维护一份统一的hosts文件的成本也会比较高。

4）一些能够实现高可用的工具集，如MHA、MMM，它们用于监控数据库节点的存活状况，通过修改配置文件或漂移主库IP的方式来实现数据库的高可用。MMM通过漂移虚拟IP的方式处理单点故障，但许多生产实践证明，其作为一套自动切换方案并不是很可靠，如果需要使用，建议只使用手动切换的功能。MHA是Perl编写的一套MySQL故障切换工具，支持通过修改全局配置和漂移虚拟IP两种方式处理单点故障，已经在许多生产环境中得到了验证，是值得考虑的方案。

你也可以自己编写脚本监控数据库节点的可用性，漂移虚拟机IP实现切换，需要留意的是，漂移IP的方式存在一个缺陷，其严重依赖硬件的可靠性，需要主机、网络设备的配合工作。在生产环境中，可能会因为网络硬件的原因导致虚拟IP不能正常漂移。

5）对于大规模的数据库集群，需要更智能地处理单点切换，应该尽量不依赖自己无法控制的因素，我们可以使用独立的Proxy代理的方式实现单点切换。所有的流量都经过Proxy，Proxy智能地处理后端的数据库主节点宕机故障，需要留意的是，你还需要处理好Proxy自身的高可用性。实现Proxy的成本很高，一些互联网公司已经有自己成熟的数据库Proxy。理论上，Proxy是可以代理本地IDC的流量的，也可以代理其他IDC的数据库流量，但由于网络延时和安全的考虑，一般建议仅代理本地IDC的流量。如果需要配置跨IDC的数据库切换，更可靠的方案是，在应用层切换流量，也就是说，让用户去访问正常IDC的应用服务器。

6）通过客户端、框架配合实现单点切换，相对于使用Proxy的方式，这种方式更轻量级。

 

# 概述

复制（replication）功能可以将一个MySQL数据库服务器（主库）中的数据复制到一个或多个 MySQL 数据库服务器（从库）。默认情况下，复制是异步的；从库不需要永久连接以接收来自主库的更新。你可以将其配置为复制所有数据库、复制指定的数据库，甚至还可以配置为复制数据库中指定的表。

​	复制是指将主数据库的DDL和DML操作通过***\*二进制日志\****传到复制服务器（也叫从库）上，然后在从库上对这些日志***\*重新执行\****（也叫***\*重做\****），从而使得从库和主库的数据保持同步。

MySQL支持一台主库同时向若干台从库进行复制，从库同时也可以作为其他服务器的主库，实现链式的复制。

 

## **新特性**

### **二进制日志压缩**

参考：MySQL8.0最新特性

[https://mp.weixin.qq.com/s?__biz=MzU2NzgwMTg0MA==&mid=2247487973&idx=1&sn=25ce534b828b7c13b5dbdef9d8b2a7ad&chksm=fc96f37acbe17a6cce03450da1d27d94e05f59d8070acd2250ab9f692b1a7d959842bf0ffecb&mpshare=1&srcid=&sharer_sharetime=1589964812234&sharer_shareid=42ba259b81cc8809b4e0103fdee44043&from=timeline&scene=2&subscene=1&clicktime=1589967951&enterid=1589967951&ascene=2&devicetype=android-27&version=27000e9a&nettype=cmnet&abtest_cookie=AAACAA%3D%3D&lang=zh_CN&exportkey=AVZz7LaftCLbo9btde%2B93P4%3D&pass_ticket=L%2FYk6R1Y8eflwNYpNejcBafosZ7dXwgdzkj04gMn%2FbbE8uTD8KpPkVGkdltct8ae&wx_header=1](https://mp.weixin.qq.com/s?__biz=MzU2NzgwMTg0MA==&mid=2247487973&idx=1&sn=25ce534b828b7c13b5dbdef9d8b2a7ad&chksm=fc96f37acbe17a6cce03450da1d27d94e05f59d8070acd2250ab9f692b1a7d959842bf0ffecb&mpshare=1&srcid=&sharer_sharetime=1589964812234&sharer_shareid=42ba259b81cc8809b4e0103fdee44043&from=timeline&scene=2&subscene=1&clicktime=1589967951&enterid=1589967951&ascene=2&devicetype=android-27&version=27000e9a&nettype=cmnet&abtest_cookie=AAACAA==&lang=zh_CN&exportkey=AVZz7LaftCLbo9btde+93P4=&pass_ticket=L/Yk6R1Y8eflwNYpNejcBafosZ7dXwgdzkj04gMn/bbE8uTD8KpPkVGkdltct8ae&wx_header=1)

 

MySQL8.0复制增强功能：二进制日志压缩。以下是此版本中的内容列表：

1、二进制日志压缩（WL＃3549）。LuísSoares所做的这项工作使用了流行的压缩算法ZSTD，实现了二进制日志压缩。压缩是基于每个事务完成的（不支持非事务引擎）。压缩后的事务以压缩状态有效负载在复制流中发送到从库（MGR架构中为组member）或客户端（例如mysqlbinlog）。

在服务器之间复制时，它们仍保持压缩状态。这意味着在磁盘上存储和通过网络传输的二进制日志将消耗较少的存储空间和网络带宽。

2、控制从服务器的主键检查（WL＃13239）。Pedro Gomes所做的这项工作使用户可以控制是否允许应用线程（回放线程）：

创建或更改表时不使用主键。

执行CREATE TABLE或ALTER TABLE时不具有更改@@ session.sql_require_primary_key的权限。这意味着DBA可以在与主服务器不同的责任域中操作从服务器，DBA可以独立于上游主服务器上的设置来调整从服务器上的主键策略。

***\*说明：\****

1、压缩功能以事务为单位进行压缩，不支持非事务引擎。

2、仅支持对ROW模式的binlog进行压缩。

3、目前仅支持 ZSTD 压缩算法，但是，底层设计是开放式的，因此后续官方可能会根据需要添加其他压缩算法（例如zlib或lz4）。

4、压缩动作是并行进行的，并且发生在binlog落盘之前的缓存步骤中。

5、压缩过程占用本机CPU及内存资源。在主从延迟的场景中，如果性能瓶颈时，网络带宽、压缩功能可以有效缓解主从延迟；但是如果性能瓶颈是本机自身处理能力，那么压缩功能反而可能加大主从延迟。

### **Slave从库多线程复制**

***\*MySQL 5.5版本里是单进程串行复制，通过sql_thread线程来恢复主库推送过来的binlog，这样会产生一个问题，主库上有大量的写操作，从库就有可能出现延迟\****。

MySQL 5.6的slave从库多线程复制是基于库（schema）的，设置slave_parallel_workers参数，开启基于库的多线程复制。默认是0，不开启，最大并发数为1024个线程。如果用户的MySQL数据库实例中存在多个库，对于slave从库复制的速度可以有比较大的帮助，因为不同的库slave在执行并行复制时，互相没有关联，数据不会不一致。2个库slave就有2个IO/SQL线程，3个库slave就有2个IO/SQL线程，依次类推。

MySQL 5.7的slave从库多线程复制是基于表的，实现的原理基于binlog组提交，简单来说，多个并发提交的事务加入一个队列里，对这个队列里的事务，利用一次I/O合并提交。如果主库上1秒内有10个事务，那么合并一个I/O提交一次，并在binlog里增加一个last_committed标记（MariaDB的binlog标记是cid），当last_committed（cid）的值一样时， slave就可以进行并行复制，通过设置多个sql_thread线程将这10个事务并行恢复，如图2-155所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps350E.tmp.jpg) 

上述last_committed为0的事务有10个，表示组提交时提交了10个事务，假如设置slave_parallel_workers =12（并行复制线程数，根据CPU核数设置），那么这10个事务在slave从库上通过12个线程进行恢复。

通过设置slave-parallel-type = LOGICAL_CLOCK（基于表的组提交并行复制），默认是slave-parallel-type =DATABASE（基于库的并行复制）。

MariaDB 10.0/10.1多线程复制的实现原理同MySQL 5.7的，不同的是在binlog里增加了一个cid标记，如图2-156所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps350F.tmp.jpg) 

上述cid为486的事务有10个，表示组提交时提交了10个事务，假如设置slave_parallel_threads=12（并行复制线程数，根据CPU核数设置），那么这10个事务在slave从库上通过12个线程进行恢复。

### **Slave支持多源复制**

在MySQL 5.7或MariaDB 10.0/10.1版本里，一个从库可以支持多个主库，如图2-158所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps351F.tmp.jpg) 

多源复制架构的适用场景：实现数据分析部门的需求，将多个系统的数据汇聚到一台服务器上进行查询。

## **优点**

​	MySQL复制的优点：

1、如果主库出现问题，可以快速切换到从库提供服务；

2、可以在从库上执行查询操作，降低主库的访问压力；

3、可以在从库上执行备份，以避免备份期间影响主库的服务。

**注意：**

由于MySQL实现的是***\*异步的复制\****，所以主从库之间存在一定的差距，在从库上进行的查询操作需要考虑到这些数据的差异，***\*一般只有更新不频繁的数据或者对实时性要求不高的数据可以通过从库查询，实时性要求高的数据仍然需要从主数据库获得\****。

 

复制有如下优点：

（摘自 https：//dev.mysql.com/doc/refman/8.0/en/replication.html）。

1、水平解决方案：将负载分散到多个从库以提高性能。在此环境中，所有的写入和更新都必须在主库上进行。但是，读操作可能发生在一个或多个从库上。该模式可以提高写入的性能（因为主库专门用于更新），同时对于不断增加的从库也能显著加快其读取速度。

2、数据安全性：因为数据被复制到从库，而且从库可以暂停复制过程，所以可以在从库上运行备份服务而不会损坏相应的主库数据。

3、分析：在主库上可以实时创建数据，而对信息的分析可以在从库上进行，不会影响主库的性能。

4、远程数据分发：你可以使用复制为远程服务器站点创建本地数据的副本，无须永久访问主库。

 

做数据的热备，作为后备数据库，主数据库服务器故障后，可切换到从数据库继续工作，避免数据丢失。

架构的扩展。业务量越来越大，I/O访问频率过高，单机无法满足，此时做多库的存储，降低磁盘I/O访问的评率，提高单个机器的I/O性能。

读写分离，使数据库能支持更大的并发。在报表中尤其重要。由于部分报表sql语句非常的慢，导致锁表，影响前台服务。如果前台使用master，报表使用slave，那么报表sql将不会造成前台锁，保证了前台速度。

 

### **实现服务器负载均衡**

***\*好处一:实现服务器负载均衡\****

通过服务器复制功能，可以在主服务器和从服务器之间实现负载均衡。即可以通过在主服务器和从服务器之间切分处理客户查询的负荷，从而得到更好地客户相应时间。通常情况下，数据库管理员会有两种思路。

一是在主服务器上只实现数据的更新操作。包括数据记录的更新、删除、新建等等作业。而不关心数据的查询作业。数据库管理员将数据的查询请求全部 转发到从服务器中。这在某些应用中会比较有用。如某些应用，像基金净值预测的网站。其数据的更新都是有管理员更新的，即更新的用户比较少。而查询的用户数 量会非常的多。此时就可以设置一台主服务器，专门用来数据的更新。同时设置多台从服务器，用来负责用户信息的查询。将数据更新与查询分别放在不同的服务器 上进行，即可以提高数据的安全性，同时也缩短应用程序的响应时间、提高系统的性能。

二是在主服务器上与从服务器切分查询的作业。在这种思路下，主服务器不单单要完成数据的更新、删除、插入等作业，同时也需要负担一部分查询作 业。而从服务器的话，只负责数据的查询。当主服务器比较忙时，部分查询请求会自动发送到从服务器重，以降低主服务器的工作负荷。当然，像修改数据、插入数 据、删除数据等语句仍然会发送到主服务器中，以便主服务器和从服务器数据的同步。

### **通过复制实现数据的异地备份**

***\*好处二：通过复制实现数据的异地备份\****

可以定期的将数据从主服务器上复制到从服务器上，这无疑是先了数据的异地备份。在传统的备份体制下，是将数据备份在本地。此时备份 作业与数据库服务器运行在同一台设备上，当备份作业运行时就会影响到服务器的正常运行。有时候会明显的降低服务器的性能。同时，将备份数据存放在本地，也 不是很安全。如硬盘因为电压等原因被损坏或者服务器被失窃，此时由于备份文件仍然存放在硬盘上，数据库管理员无法使用备份文件来恢复数据。这显然会给企业 带来比较大的损失。

而如果使用复制来实现对数据的备份，就可以在从服务器上对数据进行备份。此时不仅不会干扰主服务气的正常运行，而且在备份过程中主服务器可以继 续处理相关的更新作业。同时在数据复制的同时，也实现了对数据的异地备份。除非主服务器和从服务器的两块硬盘同时损坏了，否则的话数据库管理员就可以在最 短时间内恢复数据，减少企业的由此带来的损失。

 

### **提高数据库系统的可用性**

***\*好处三：提高数据库系统的可用性\****

数据库复制功能实现了主服务器与从服务器之间数据的同步，增加了数据库系统的可用性。当主服务器出现问题时，数据库管理员可以马上让从服务器作为主服务器，用来数据的更新与查询服务。然后回过头来再仔细的检查主服务器的问题。此时一般数据库管理员也会采用两种手段。

一是主服务器故障之后，虽然从服务器取代了主服务器的位置，但是对于主服务器可以采取的操作仍然做了一些限制。如仍然只能够进行数据的查询，而 不能够进行数据的更新、删除等操作。这主要是从数据的安全性考虑。如现在一些银行系统的升级，在升级的过程中，只能够查询余额而不能够取钱。这是同样的道理。

二是从服务器真正变成了主服务器。当从服务器切换为主服务器之后，其地位完全与原先的主服务器相同。此时可以实现对数据的查询、更新、删除等操 作。为此就需要做好数据的安全性工作。即数据的安全策略，要与原先的主服务器完全相同。否则的话，就可能会留下一定的安全隐患。

 

## **缺点**

主从复制也带来其他一系列性能瓶颈问题：

1、写入无法扩展

2、写入无法缓存

***\*3、复制延时\****

***\*4、锁表率上升\****

***\*5、表变大，缓存率下降\****

针对上述问题，引入数据库垂直分区和水平分区解决。

针对复制延时问题，数据库引入最大可用、最大性能等策略，但是无法权衡安全和高效，即如果实现RPO=0则必然效率会低。阿里OceanBase能够做到RPO=0。

还有一个比价棘手的问题需要重点关注，就是如果备机落后主机较多，加入到主机集群后，会自动追加主机数据，这样会导致大量的网络流量，可能会超过生产环境的限制。

 

# 拓扑结构

​	复制存在多种拓扑结构：一主库多备库，主-主复制，环形复制，树或金字塔形等，3种常见的架构包括：一主多从复制架构、多级复制架构和双主复制/Dual Master架构。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3520.tmp.jpg) 

通常，我们在创建另一个从库时都是从从库中获取备份副本的。

大致方案如下：

1、在主库上启用二进制日志记录。

2、在主库上创建一个复制用户。

3、在从库上设置唯一的server_id。

4、从主库中取得备份。

5、恢复从库上的备份。

6、执行CHANGE MASTER TO命令。

7、开始复制。

具体的步骤如下：

1、在主库上，启用二进制日志记录并设置SERVER_ID。

2、在主库上，创建一个复制用户。从库使用此账户连接到主库：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3521.tmp.jpg) 

3、在从库上，设置唯一的SERVER_ID选项（它应该与你在主库上设置的不同）：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3532.tmp.jpg) 

4、在从库上，通过远程连接从主库进行备份。可以使用mysqldump或mydumper。不能使用mysqlpump，因为二进制日志的位置不一致。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3533.tmp.jpg) 

从另一个从库进行备份时，必须设置-slave-dump选项。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3534.tmp.jpg) 

5、在从库上，待备份完成后恢复此备份。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3535.tmp.jpg) 

6、在从库上，恢复备份后，必须执行以下命令：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3546.tmp.jpg) 

mydumper：＜log_file_name＞和＜position＞存储在元数据文件中：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3547.tmp.jpg) 

如果你从一个从库或主库进行备份来设置另一个从库，则必须使用来自SHOW SLAVE STATUS的位置。

如果要设置链式复制，则可以使用SHOW MASTER STATUS中的位置。

1、在从库上，执行START SLAVE命令：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3548.tmp.jpg) 

2、可以通过执行以下命令来检查复制的状态

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3558.tmp.jpg) 

你应该查看Seconds_Behind_Master的值，它代表的是复制的延迟情况。如果它的值为0，则意味着从库与主库同步；如果为非零值，则表示延迟的秒数；如果为NULL，则表示未复制。

## **一主多从复制架构**

​	在主库读取请求压力非常大的场景下，可以通过配置一主多从复制架构实现读写分离，把大量对实时性要求不是特别高的读请求通过负载均衡分布到多个从库上，降低主库的读取压力。

​	在主库出现异常宕机的情况下，可以把一个从库切换为主库继续提供服务。

## **多级复制架构**

​	一主多从的架构能够解决大部分读请求压力特别大的场景的需求，考虑到MySQL的复制是主库“推送”Binlog日志到从库，主库的I/O压力和网络压力会随着从库的增加而增长（每个从库都会在主库上有一个独立的Binlog Dump线程来发送事件），而多级复制架构解决了一主多从场景下，主库额外的I/O和网络压力。

​	对比一主多从的架构，多级复制架构仅仅是在主库master1复制到从库slave1、slave2、slave3的中间增加一个二级主库master2，主库只需要给一个从库master2“推送”binlog日志即可，减轻主库master1的压力。二级主库master2再“推送”binlog日志给从库slave1、slave2、slave3。

​	多级复制架构解决了一主多从场景下，主库的I/O负载和网络压力，其缺点：MySQL的复制是异步复制，多级复制场景下主库的数据是经历两次复制才到达从库的，期间的延时比一主多从复制场景下只经历一次复制的要大。

#### 将从库由主从复制切换到链式复制

如果设置了主从复制，则服务器B和C将从服务器A中复制：服务器A→（服务器B、服务器C）。如果希望把服务器C作为服务器B的从库，则必须先在服务器B和服务器C上停止复制，然后使用 STARTSLAVE UNTIL 命令将它们置于相同的主库日志位置。之后，就可以从服务器B中获取主库日志坐标，并且在服务器C上执行CHANGE MASTER TO命令了。

操作步骤：

1、在服务器 C 上，停止从库的运行，并记下 Relay_Master_Log_File 和Exec_Master_Log_Pos在SHOW SLAVE STATUS\G命令中的位置：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3559.tmp.jpg) 

2、在服务器B上，停止从库的运行，并记下Relay_Master_Log_File和Exec_Master_Log_Pos在SHOW SLAVE STATUS\G命令中的日志位置：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps355A.tmp.jpg) 

3、将服务器B的日志位置与服务器C进行比较，找出哪一个是服务器A的最新同步。通常，由于先在服务器C上停止了从库的运行，服务器B的日志位置会更靠前。在本例中，二者的日志位置如下所示。

服务器C：（server_A-bin.000023，2604）

服务器B：（server_A-bin.000023，8250241）服务器B的日志位置更靠前，所以必须将服务器C置于服务器B的日志位置。

4、在服务器C上，使用START SLAVE UNTIL语句将其同步到服务器B的日志位置：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps355B.tmp.jpg) 

5、在服务器C上，检查SHOW SLAVE STATUS的输出中的Exec_Master_Log_Pos和Until_Log_Pos（两者应该相同）来等待服务器C同步：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps356C.tmp.jpg) 

6、在服务器B上，找出主库状态，启动从库，并确保它在复制：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps356D.tmp.jpg) 

7、在服务器C上，停止从库的运行，执行CHANGE MASTER TO命令，并指向服务器B。必须使用在前面的步骤中获得的日志位置：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps356E.tmp.jpg) 

8、在服务器C上，启动复制并验证从库的状态：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps357F.tmp.jpg) 

#### 将从库由链式复制切换到主从复制

如果你的服务器设置的是链式复制（例如服务器A→服务器B→服务器C），并且希望将服务器C作为服务器A的直接从库，则必须停止服务器B上的复制，让服务器C追上B，然后查找服务器A对应于服务器B停下来的位置的坐标。使用这些坐标，你就可以在服务器C上执行CHANGE MASTER TO命令并使其成为服务器A的从库。

操作步骤：

1、在服务器B上，停止从库的运行，并记下主库状态：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3580.tmp.jpg) 

2、在服务器C上，确保从库的延迟已被追上。Relay_Master_Log_File和Exec_Master_Log_Pos应该等于服务器B上主库状态的输出。一旦延迟被追上，就停止从库的运行：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3581.tmp.jpg) 

3、在服务器B上，从SHOW SLAVE STATUS输出中获取服务器A的日志坐标（记下Relay_Master_Log_File和Exec_Master_Log_Pos的值），并启动从库：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3582.tmp.jpg) 

4、在服务器C上，停止从库的运行，并执行CHANGE MASTER TO命令，指向服务器 A。使用在前面的步骤中记下的位置信息（server_A-bin.000023 和16497695）。最后，启动从库并验证从库的状态：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3583.tmp.jpg) 

## 主主复制/双主复制/Dual Master复制

​	双主/Dual Master架构特别适用于DBA做维护等需要主从切换的场景，通过双主/Dual Master架构避免了重复搭建从库的麻烦。

***\*操作步骤：\****

假设主库是master1和master2。

具体步骤如下。

1、在master1和master2之间设置复制。

2、设置master2为只读：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3593.tmp.jpg) 

3、在master2上，检查当前二进制日志的坐标。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3594.tmp.jpg) 

根据上面的信息，你可以从位置为 473 的 server1.000017 文件处开始在master1上复制。

4、从第3步中获取的位置开始，在master1上执行CHANGE MASTER TO命令：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3595.tmp.jpg) 

5、在master1上开启Slave模式：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3596.tmp.jpg) 

6、最后，你可以设置master2为“可读/写”，这样应用程序就可以开始对它写入数据了。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35A7.tmp.jpg) 

## **多源复制**

MySQL多源复制使从库能够同时接收来自多个源的事务。多源复制可用于将多台服务器备份到单台服务器，合并表分片，以及将多台服务器中的数据整合到单台服务器。多源复制在应用事务时不会执行任何冲突检测或解析，并且如果需要的话，这些任务将留给应用程序来处理。在多源复制拓扑中，从库为每个主库创建一个复制通道，以便从中接收事务。

操作步骤：

假设你打算将server3设置为server1和server2的从库。你需要创建从server1到server3的传统复制通道，并创建另一个从server2到server3的通道。为保证数据在从库上保持一致，请确保复制不同的数据库，或应用程序可以处理复制时的冲突。

在开始之前，从server1上获取备份并在server3上进行恢复；同样，从server2上获取备份并在server3上进行恢复。

1、在server3上，将复制存储库从FILE修改为TABLE。可以运行以下命令来动态地更改它：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35A8.tmp.jpg) 

还要在配置文件中进行一些更改：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35A9.tmp.jpg) 

2、在server3上，执行CHANGE MASTER TO命令，使其通过名为master-1的通道成为server1的从库。你可以为它取任何名字：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35AA.tmp.jpg) 

3、在server3上，执行CHANGE MASTER TO命令，使其通过名为master-2的通道成为server2的从库：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35BA.tmp.jpg) 

4、为每个通道执行START SLAVE FOR CHANNEL语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35BB.tmp.jpg) 

5、通过执行SHOW SLAVE STATUS语句来验证从库状态：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35BC.tmp.jpg) 

6、要获取特定通道的从库状态，请执行：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35BD.tmp.jpg) 

7、这是使用performance schema来监控指标的另一种方式：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35BE.tmp.jpg) 

## **跨IDC复制**

跨IDC复制架构的部署与单机房部署链式复制（级联复制）的从库并没有区别，但由于网络的不稳定，可能会导致复制的不稳定，维护代价较高，而且可能需要外网IP才能进行复制，降低了安全性。但现实中，这种架构也有人使用，相对于使用应用程序实现的数据同步，数据库在某种程度上成本更低，也更容易确保数据的一致性。

下面将简述一些跨IDC进行复制的注意事项。

£ 跨IDC的复制，建议还是采用普通的主从架构，而不要采用链式的复制架构，简单的主从架构更稳健。

£ 尽量只在中心主库进行写入，其他机房只用于读，这样既可以简化架构，也可以避免多点写入带来的维护一致性的难题。如果是M-M的架构，也应该将一个机房作为备用（Standby），仅作容灾。

□ 数据量较大的时候，网络可能会成为瓶颈，建议使用混合日志的复制模式。可在从库中设置slave_compressed_protocol=1压缩传输数据，此选项可进行动态设置。

□ 由于跨IDC的主从复制，重新搭建代价比较大，在明确知道数据库出现何种错误时，可以忽略此错误，可使用“slave-skip-errors=error_code1,error_code2...|all”，但不要滥用，否则容易导致主从不一致而不自知。

□ 由于跨IDC的复制，网络可能会不稳定，应用程序应该处理网络延时对用户体验的影响。

 

有时我们需要部署IDC级别的冗余，在另一个IDC中部署数据库的从库，由于网络层的不稳定，你很难实现很高的可用性，除非你对数据的延时和数据的一致性要求不高。你需要意识到，距离越远，网络越不可靠；中间的环节越多，网络越不可靠，所以尽量不要进行跨越数据中心的实时操作。如果部署了跨IDC的数据库访问，比如部署了读写分离的架构，在一个IDC中集中地处理所有的写请求，把读请求分担到各个IDC，那么你需要在应用层友好地处理网络异常，或者复制问题导致的复制延时问题，如果有比较多的远程写入，那么还需要处理网络问题导致的写入失败。

不仅仅是主从同步，只要距离足够远，网络质量就难以得到保证，就需要留意同步对应用的影响，你可能需要尽可能地减少节点之间的数据交互，及时调度用户访问其他节点，甚至使用专用的高质量网络。

参考：《MySQL DBA修炼之道》

# 复制类型

​	存在两种复制类型：

## 基于binlog二进制日志的复制

参考：

https://blog.csdn.net/weixin_33743703/article/details/92275388

 

***\*master用户写入数据，生成event记到binary log中\****。slave接收master上传来的binlog，然后按顺序应用，重现master上的操作。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35CF.tmp.jpg) 

传统的复制基于(filepos)，当主从发生宕机，切换的时候有问题。

slave保存的是原master上的(filepos)，无法直接指向新master上的(filepos)。

 

### **基于语句的复制**

### **基于行的复制**

### **区别**

## **使用GTID完成基于事务的复制**

### **概述**

全局事务标识符（Global Transaction Identifier，GTID）是在程序中创建的唯一标识符，并与主库上提交的每个事务相关联。此标识符是唯一的，不仅在其主库上，在给定的复制设置中的所有数据库上，它都是唯一的。***\*所有事务和所有GTID之间都是一对一的映射关系\****。GTID用一对坐标表示，用冒号（：）分隔：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35D0.tmp.jpg) 

source_id是主库的标识。通常，服务器的server_uuid选项就代表此标识。transaction_id是一个序列号，由在该服务器上提交事务的顺序决定。例如，提交的第一个事务，transaction_id为1；在同一个主库上提交的第10个事务的transaction_id为10。

 

​	主从切换后，在传统方式里，需要找到binlog和POS点，然后执行change master to指向新的主库。对于不是很有经验的运维人员来说，往往会找错，造成主从同步复制报错，在MySQL5.6版本里，无须再找binlog和POS点，只需要知道master的IP、端口、账号和密码即可，因为***\*同步复制是自动的，MySQL会通过内部机制GTID（Global\**** ***\*Transaction ID\*******\*）自动找点同步\****。

#### 优点

1、一个事务对应一个唯一ID，***\*一个GTID在一个服务器上只会执行一次\****

2、GTID是用来代替传统复制的方法，***\*GTID复制与普通复制模式的最大不同就是不需要指定二进制文件名和位置\****

3、减少手工干预和降低服务故障时间，当主机挂了之后通过软件从众多的备机中提升一台备机为主机

注：如果采用binlog的方式需要人工判断需要恢复的位置，而GTID则只需要指定主机的IP地址即可自动完成，这个可以实现快速自动的故障切换。

#### 限制

1、不支持非事务引擎

2、不支持create table ... select 语句复制(主库直接报错)

原理：会生成两个sql，一个是DDL创建表SQL，一个是insert into插入数据的sql。由于DDL会导致自动提交，所以这个sql至少需要两个GTID，但是GTID模式下，只能给这个sql生成一个GTID。 

注：所以在实际应用中如果业务涉及这种操作，在做主备主备的恢复时需要考虑是采用binlog还是GTID。

3、不允许一个SQL同时更新一个事务引擎表和非事务引擎表

4、在一个复制组中，必须要求统一开启GTID或者是关闭GTID

5、开启GTID需要重启（5.7除外）

6、开启GTID后，就不再使用原来的传统复制方式

7、对于create temporary table 和 drop temporary table语句不支持

8、不支持sql_slave_skip_counter

注：基于上述这些限制，GoldenDB分布式数据库仍然采用传统的binlog模式复制。

 

### **原理**

***\*GTID的工作原理：\****

1、master更新数据时，会***\*在事务前产生GTID，一同记录到binlog日志中\****。

2、slave端的i/o线程将变更的binlog，写入到本地的relay log中。

3、***\*sql线程从relay log中获取GTID，然后对比slave端的binlog是否有记录\****。

4、如果有记录，说明该GTID的事务已经执行，slave会忽略。

5、如果没有记录，slave就会从relay log中执行该GTID的事务，并记录到binlog。

6、在解析过程中会判断是否有主键，如果没有就用二级索引，如果没有就用全部扫描。

### **操作步骤**

如果已在服务器之间设置过复制，请按照下列步骤操作。

1、在my.cnf中启用GTID：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35D1.tmp.jpg) 

2、将主库设置为只读，并确保所有从库都能与主库同步。这一点非常重要，因为主库和从库之间不应该有任何数据不一致：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35E2.tmp.jpg) 

3、重新启动所有的从库使GTID生效。由于在配置文件中给出了skip_slave_start，所以只有在执行了START SLAVE命令之后，从库才会启动。如果启动从库，它将因为下面的错误而启动失败：Thereplication receiver thread cannot start because the master has GTID_MODE=OFF and thisserver has GTID_MODE=ON：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35E3.tmp.jpg) 

4、重新启动主库。当重新启动主库时，它将以读/写模式开始运行，并开始接受以GTID模式写入：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35E4.tmp.jpg) 

5、执行CHANGE MASTER TO命令来设置GTID复制：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35E5.tmp.jpg) 

你可能观察到这里没有给出二进制日志文件和日志位置，而是给出了MASTER_AUTO_POSITION，它会自动找到执行的GTID。

6、在所有从库上执行START SLAVE：

7、确认从库正在复制：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35E6.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35F6.tmp.jpg) 

## **区别**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35F7.tmp.jpg) 

主从复制，默认是通过pos复制(postion)方式，将用户进行的每一项操作都进行编号(pos)，每一个event都有一个起始编号，一个终止编号。GTID就是类似于pos的一个作用，全局通用并且日志文件里事件的GTID值是一致的。

pos与GTID在日志里是一个标识符，在slave里已不同的方式展现。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35F8.tmp.jpg) 

GTID的生成受gtid_next控制。

在Master上，gtid_next是默认的AUTOMATIC，即GTID在每次事务提交时自动生成。它从当前已执行的GTID集合(即gtid_executed)中，找一个大于0的未使用的最小值作为下个事务GTID。同时将GTID写入到binlog(set gtid_next记录)，在实际的更新事务记录之前。

在Slave上，从binlog先读取到主库的GTID(即set gtid_next记录)，而后执行的事务采用该GTID。

 

# 原理

​	MySQL复制原理大致如下：

1、 首先，MySQL主库在事务提交时会把数据变更作为事件Events记录在二进制日志文件binlog中；***\*MySQL主库上的sync_binlog参数控制binlog日志刷新到磁盘\****；

**2、** ***\*主库推送二进制日志文件binlog中的事件到从库的中继日志Relay\**** ***\*Log\****，之后从库根据中继日志Relay Log重做数据变更操作，通过逻辑复制以此来达到主库和从库的数据一致。

 

MySQL通过3个线程来完成主从库间的数据复制：其中Binlog Dump线程跑在主库上，I/O线程和SQL线程跑在从库上。当在从库上启动复制（START SLAVE）时，首先创建I/O线程连接主库，主库随后创建Binlog Dump线程读取数据库事件并发送给I/O线程，I/O线程获取到事件数据后更新到从库的中继日志Relay Log中去，之后从库上的SQL线程读取中继日志Relay Log中更新的数据库事件并应用。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps35F9.tmp.jpg) 

***\*从库有两个线程IO线程和SQL线程\****

1、从库的IO线程向主库的主进程发送请求，主库验证从库，交给主库IO线程负责数据传输；

2、主库IO线程对比从库发送过来的master.info里的信息，将binlog文件信息，偏移量和binlog文件名等发送给从库；

3、从库接收到信息后，将binlog信息保存到relay-bin中，同时更新master.info的偏移量和binlog文件名；

4、从库的SQL线程不断的读取relay-bin的信息，同时将读到的偏移量和文件名写到relay-log.info文件，binlog信息写进自己的数据库，一次同步操作完成；

5、完成上次同步后，从库IO线程不断的向主库IO线程要binlog信息；

6、从库如果也要做主库，也要打开log_bin 和log-slave-update参数。

 

## **日志文件**

参考：

https://database.51cto.com/art/202009/626540.htm

 

### **二进制文件Binlog**

​	二进制文件（Binlog）会把MySQL中的所有数据修改操作以二进制的形式记录到日志文件中，包括Create、Drop、Insert、Update、Delete等操作，但二进制日志文件（binlog）不会记录select操作，因为select操作并不修改数据。

​	可以通过show variables查看binlog格式，binlog支持statement、row、mixed三种格式，也对应了MySQL的三种复制技术。

​	二进制日志文件binlog的格式有以下3种：

#### Statement

​	Statement：基于SQL语句级别的binlog，每条修改数据的SQL都会保存在binlog中，***\*存储日志量是最小的\****。

#### Row

​	Row：基于行级别，记录每一行数据的变化，也就是将每行数据的变化都记录到binlog中，记录得非常仔细，但并不记录原始SQL，***\*存储event数据，存储日志量大\****，但是***\*不能很直接的进行读取\****；在复制的时候，并不会因为存储过程或触发器造成主从库数据不一致的问题，但是***\*记录的日志量较statement格式要大得多\****。

在MySQL的一般场景中，通常数据库运维同学会推荐，将复制格式设置为ROW格式，也就是行格式，这样所有的变更数据都会被记录到binlog中，可对数据达到较好的保护，万一发生DML误操作，可以直接在binlog中恢复。在MySQL5.7之后，ROW格式已经作为复制的默认格式。

1、使用ROW格式的复制模式时，如果存在无索引表，可能会导致主从延迟增大。

***\*原因：\****

ROW模式之所以能保证复制可靠性，是其在BINLOG里记录每一行完整记录，包括所有列的值; 在备库应用日志时，MySQL会先尝试用行里的主键去匹配自身的记录，如果没有主键，则进行全表扫描所有的行，每一行都与日志进行匹配，直到发现完全匹配的行。

所以如果在没有主键的情况下，进行一些更新或者删除的操作，在备库回放时就会产生大量的记录，同样会使得主从延迟增大。

***\*解决方法：\****

若表支持创建唯一/主键索引，则创建主键索引；若表不支持创建主键索引，创建部分列（区分度较大的几列）的联合索引。

创建自增主键

参数优化slave_rows_search_algorithms的hash_scan方式。

2、salve节点alter语句导致同步中断

***\*原因：\****

若我们需要对一个表的表结构进行修改，可以使用在线的DDL操作，或者采用percona的开源工具pt-ost。

对一个大表修改字段类型DDL,为了不影响主库业务，会先在从库上执行DDL操作，然后通过主从切换，完成最终的大表DDL。在从库执行完DDL后，这时发现复制中断了，报错信息：1677。报错原因是主从库之间同一个表如果列的类型不匹配，MySQL会尝试转码，如果转码失败（类型不兼容）则复制中断。 

***\*解决方案：\****

有损转换。可在slave节点设置slave_type_conversions=ALL_LOSSY，系统默认无损转换，设置为有损转换时，表示列类型允许丢失一些信息。设置时需注意：一定要保证从库字段足够大，能存下主库字段值，否则会导致数据不一致。

从主库直接xtrabackup备份（或者冷备系统），之后恢复作为从库提供服务。

在从库上把对应表新增的字段去掉，重新启动同步。

 

#### Mixed

​	Mixed：混合statement和row模式，默认情况下采用statement模式记录，某些情况下会切换到row模式。如果每天数据操作量很大，产生的日志比较多，可以考虑选择使用mixed格式。

​	注：Row格式比Statement格式更能保证从库数据的一致性（复制的是记录，而不是单纯操作SQL）。当然，Row格式下的Binlog的日志量很可能会增大非常多，在设置时需要考虑磁盘空间问题。

 

​	同时对应MySQL复制的3种技术：

​	binlog_format=Statement：基于SQL语句的复制，也叫Statement-Based Replication（SBR），MySQL5.1.4之前仅提供基于SQL语句的复制。

​	binlog_format=Row：基于行的复制，也叫Row-Based Replication（RBR）。

​	binlog_format=Mixed：混合复制模式，混合了基于SQL语句的复制和基于行的复制。

 

### **中继日志文件Relay** **Log**

#### 概述

​	中继日志文件Relay Log的文件格式、内容和二进制日志文件binlog一样，***\*唯一的区别在于从库上的SQL线程在执行完当前中继日志文件Relay\**** ***\*Log\*******\*中的事件之后，SQL线程会自动删除当前中继日志Relay\**** ***\*Log\*******\*，避免从库上的中继日志文件占用过多的磁盘空间\****。

​	为了保证从库Crash重启之后，从库的I/O线程和SQL线程仍然能够知道从哪里开始复制，从库上默认还会创建两个日志文件master.info和relay-log.info用来保存复制的进度。这两个文件在磁盘上以文件形式分别记录了从库的I/O线程当前读取二进制日志binlog的进度和SQL线程应用中继日志Relay Log的进度。例如，通过SHOW SLAVE STATUS命令能够看到当前从库复制的状态，如图31-2所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps360A.tmp.jpg) 

其中master.info记录的是 I/O线程连接主库的一些参数，主要包括SHOW SLAVE STATUS显示出的以下5列（master.info不止包括以下5项内容，仅说明较重要的5项）。

#### 结构

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps360B.tmp.jpg) 

relay-log的结构和binlog非常相似，只不过他多了一个master.info和relay-log.info的文件。

master.info记录了上一次读取到master同步过来的binlog的位置，以及连接master和启动复制必须的所有信息。

relay-log.info记录了文件复制的进度，下一个事件从什么位置开始，由***\*sql线程负责更新\****。

##### master.info

Master Host：主库的 IP。

Master User：主库上，主从复制使用的用户账号。

Master Port：主库MySQL的端口号。

Master_Log_File：从库的I/O线程当前正在读取的主库Binlog的文件名。

Read_Master_Log_Pos：从库I/O线程当前读取到的位置。

##### relay-log.info

而 relay-log.info记录的是SQL线程应用中继日志Relay Log的一些参数，主要包括SHOW SLAVESTATUS显示出的以下4列（同样的，relay-log.info不止包含这 4项内容，仅说明较重要的4项）。

Relay_Log_File：从库SQL线程正在读取和应用的中继日志Relay Log的文件名。

Relay_Log_Pos：从库SQL线程当前读取并应用的中继日志Relay Log的位置。

Relay_Master_Log_File：从库 SQL 线程正在读取和应用的 Relay Log 对应于主库Binlog的文件名。

Exec_Master_Log_Pos：中继日志Relay Log中Relay_Log_Pos位置对应于主库Binlog的位置。

#### 复制过程

整个复制流程的过程大概是这个样子：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps360C.tmp.jpg) 

知道binlog和relay-log的结构之后，我们重新梳理一下整个链路的流程，这里我们假定master.info和relay-log.info都是存在的情况：

1、 Master收到客户端请求语句，在语句结束之前向二进制日志写入一条记录，可能包含多个事件。

2、此时，一个Slave连接到Master，Master的dump线程从binlog读取日志并发送到Slave的IO线程。

**3、*****\*IO线程\****从master.info读取到上一次写入的最后的位置。

4、IO线程写入日志到relay-log中继日志，如果超过指定的relay-log大小，写入轮换事件，创建一个新的relay-log。

**5、*****\*更新master.info的最后位置\****

6、SQL线程从relay-log.info读取进上一次读取的位置

7、SQL线程读取日志事件

8、在数据库中执行sql

9、更新relay-log.info的最后位置

10、Slave记录自己的binlog日志

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps361D.tmp.jpg) 

但是在这里IO和SQL线程有会产生重复事件的问题，举一个场景：

1、先记录中继日志，然后更新master.info位置

2、此时服务器崩溃，写入master.info失败

服务器恢复，再次同步从master.info获取到的是上一次的位置，会导致事件重复执行。

既然会有这个问题还为什么要这样做呢？假设反过来，先更新master.info再记录中继日志，这样带来的问题就是丢失数据了。而mysql认为丢失比重复更严重，所以要先刷新日志，保大还是保小mysql帮你做了决定。

 

## **过程**

### **流程**

#### slave

处理start slave命令，进行如下处理：

1、register

slave向master发送COM_REGISTER_SLAVE

2、binlog dump

注册成功后，slave向master发送binlog_dump请求（即COM_BINLOG_DUMP_GTID）

3、apply binlog

在worker线程中进行。

#### master

master接收到binlog_dump_gtid请求消息后，创建binlog_sender对象，并根据请求消息初始化binlog_sender对象，对象打开binlog文件，发送binlog事件。如果当前发送位置与binlog结束位置相同，等待新的binlog事件触发。

### **步骤**

整个主从复制的流程可以通过以下图示理解：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps361E.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps361F.tmp.jpg) 

步骤一：主库db的更新事件(update、insert、delete)被写到binlog

步骤二：从库发起连接，连接到主库

步骤三：此时主库创建一个binlog dump thread，把binlog的内容发送到从库

步骤四：从库启动之后，创建一个I/O线程，读取主库传过来的binlog内容并写入到relay log

步骤五：还会创建一个SQL线程，从relay log里面读取内容，从`Exec_Master_Log_Pos`位置开始执行读取到的更新事件，将更新内容写入到slave的db

  注：上面的解释是解释每一步做了什么，整个mysql主从复制是异步的，不是按照上面的步骤执行的。

 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3620.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3630.tmp.jpg) 

主从复制的基础是主库记录数据库的所有变更记录到binlog。binlog是数据库服务器启动的那一刻起，保存所有修改数据库结构或内容的一个文件。

mysql主从复制是一个***\*异步\****的复制过程，主库发送更新事件到从库，从库读取更新记录，并执行更新记录，使得从库的内容与主库保持一致。

 

在主库里，只要有更新事件出现，就会被依次地写入到binlog里面，之后会推到从库中作为从库进行复制的数据源。

#### binlog输出线程

**binlog输出线程**。每当有从库连接到主库的时候，主库都会创建一个线程然后发送binlog内容到从库。

对于每一个即将发送给从库的sql事件，binlog输出线程会将其锁住。一旦该事件被线程读取完之后，该锁会被释放，即使在该事件完全发送到从库的时候，该锁也会被释放。

 

在从库里，当复制开始的时候，从库就会创建两个线程进行处理：

#### 从库I/O线程

**从库I/O线程**。当START SLAVE语句在从库开始执行之后，从库创建一个I/O线程，该线程连接到主库并请求主库发送binlog里面的更新记录到从库上。

从库I/O线程读取主库的binlog输出线程发送的更新并拷贝这些更新到本地文件，其中包括relay log文件。

#### 从库的SQL线程

**从库的SQL线程**。从库创建一个SQL线程，这个线程读取从库I/O线程写到relay log的更新事件并执行。

 

可以知道，对于每一个主从复制的连接，都有三个线程。拥有多个从库的主库为每一个连接到主库的从库创建一个binlog输出线程，每一个从库都有它自己的I/O线程和SQL线程。

从库通过创建两个独立的线程，使得在进行复制时，从库的读和写进行了分离。因此，即使负责执行的线程运行较慢，负责读取更新语句的线程并不会因此变得缓慢。比如说，如果从库有一段时间没运行了，当它在此启动的时候，尽管它的SQL线程执行比较慢，它的I/O线程可以快速地从主库里读取所有的binlog内容。这样一来，即使从库在SQL线程执行完所有读取到的语句前停止运行了，I/O线程也至少完全读取了所有的内容，并将其安全地备份在从库本地的relay log，随时准备在从库下一次启动的时候执行语句。

 

### **状态查看**

#### 查看主从复制的状态

当主从复制正在进行中时，如果想查看从库两个线程运行状态，可以通过执行在从库里执行”show slave statusG”语句，以下的字段可以给你想要的信息：

Master_Log_File — 上一个从主库拷贝过来的binlog文件

Read_Master_Log_Pos — 主库的binlog文件被拷贝到从库的relay log中的位置

Relay_Master_Log_File — SQL线程当前处理中的relay log文件

Exec_Master_Log_Pos — 当前binlog文件正在被执行的语句的位置

 

#### 查看主库状态

可以通过SHOW PROCESSLIST命令在主库上查看Binlog Dump线程，从Binlog Dump线程的状态可以看到，MySQL 的复制是主库主动推送日志到从库去的，是属于“推”日志的方式来做同步。

 

#### 查看从库状态

在从库上通过SHOW PROCESSLIST可以看到 I/O线程和SQL线程，I/O线程等待主库上的Binlog Dump线程发送事件并更新到中继日志Relay Log，SQL线程读取中继日志Relay Log并应用变更到数据库。

 

## **复制方式**

### **异步复制**

主库在执行完客户端提交的事务后会立即将结果返给给客户端，并不关心从库是否已经接收并处理，这样就会有一个问题，***\*主如果crash掉了，此时主上已经提交的事务可能并没有传到从库上，如果此时，强行将从提升为主，可能导致“数据不一致”\****。早期MySQL仅仅支持异步复制。

 

​	MySQL的复制是异步复制。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3631.tmp.jpg) 

MySQL默认的复制即是异步的，主库在执行完客户端提交的事务后会立即将结果返给给客户端，并不关心从库是否已经接收并处理，这样就会有一个问题，主如果crash掉了，此时主上已经提交的事务可能并没有传到从上，如果此时，强行将从提升为主，可能导致新主上的数据不完整。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3632.tmp.jpg) 

### **整体/部分复制**

​	MySQL复制可是对整个实例进行复制，也可以对实例中的某个库或者是某个表进行复制。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3633.tmp.jpg) 

### **全同步复制**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3644.tmp.jpg) 

指当主库执行完一个事务，所有的从库都执行了该事务才返回给客户端。因为需要等待所有从库执行完该事务才能返回，所以全同步复制的性能必然会收到严重的影响。

### **半同步复制**

#### 背景

默认情况下，复制是异步的。主库不知道写入操作是否已经到达从库。如果主库和从库之间存在延迟，并且主库崩溃，尚未到达从库的那些数据就会丢失。为了解决这种问题，你可以使用半同步复制。

 

默认情况下，MySQL 5.5/5.6/5.7和MariaDB 10.0/10.1的复制功能是异步的，异步复制可以提供最佳的性能，主库把Binlog日志发送给从库，这一动作就结束了，并不会验证从库是否接收完毕，但这同时也带来了很高的风险，这就意味着当主服务器或从服务器发生故障时，有可能从机没有接收到主机发送过来的Binlog日志，会造成主服务器/从服务器的数据不一致，甚至在恢复时会造成数据丢失。

为了解决上述问题，MySQL 5.5引入了一种半同步复制（Semi Replication）模式，该模式可以确保从服务器接收完主服务器发送的Binlog日志文件并写入自己的中继日志（Relay Log）里，然后会给主服务器一个反馈，告诉对方已经接收完毕，这时主库线程才返回给当前session告知操作完成，如图2-140所示。当出现超时情况时，源主服务器会暂时切换到异步复制模式，直到至少有一台设置为半同步复制模式的从服务器及时收到信息为止（见图2-140）。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3645.tmp.jpg) 

简言之，半同步复制在一定程度上可保证提交的事务已经传给了至少一个备库，因此，半同步复制与异步复制相比，进一步提高了数据的完整性。

***\*注意：\****半同步复制模式必须在主服务器和从服务器同时启用，否则主服务器默认使用异步复制模式。

#### 原理

​	MySQL支持半同步复制。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3646.tmp.jpg) 

​	在MySQL5.5之前，MySQL的复制是异步操作，主库和从库的数据之间存在一定的延迟，这样存在一个隐患：当在主库上写入一个事务并提交成功，而从库尚未得到主库推送的Binlog日志时，主库宕机了，例如主库磁盘损坏、内存故障等造成主库上该事务binlog丢失，此时从库可能损失这个事务，从而造成数据不一致。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3647.tmp.jpg) 

​	为了解决这个问题，MySQL5.5引入了半同步复制机制。在MySQL5.5之前的异步复制时，主库执行完commit提交操作后，在主库写入binlog日志后即可成功返回客户端，无需等待Binlog日志传送给从库。而半同步复制时，为了保证主库上的每一个binlog事务都能够被可靠的复制到从库上，***\*主库在每次事务成功提交时，并不及时反馈给前端应用用户，而是等待其中一个从库\*******\*也\*******\*接收到binlog事务并成功写入到中继日志后，主库才返回commit操作成功给客户端\****。半同步复制保证了事务成功提交后，至少有两份日志记录，一份在主库的binlog日志上，一份在至少一个从库的中继日志Relay Log上，从而更进一步保证了数据的完整性。

​	半同步复制很大程度上取决于主从库之间的网络情况，往返时延RTT（Round-Trip Time）越小决定了从库的实时性越好。通俗地说，主从库之间网络越快，从库越实时。

​	从半同步复制的流程会发现，半同步复制的“半”就体现在：虽然主库和从库的Binlog日志是同步的，但是主库并不等待从库应用这部分日志就返回提交结果，这部分操作是异步的，从库的数据并不是和主库实时同步的，所以只能称为半同步，而不是完全的实时同步。

#### 参数

半同步复制的配置参数较少，其中，在master主库上有4个相关参数，说明如下。

❑ rpl_semi_sync_master_enabled = ON：表示在master上已经开启半同步复制模式。

❑ rpl_semi_sync_master_timeout = 10000：该参数默认为10000毫秒，即10秒，不过，这个参数是动态可调的，它用来表示如果主库在某次事务中的等待时间超过10秒，则降级为异步复制模式，不再等待slave从库。如果主库再次探测到slave从库恢复，则会自动再次回到半同步复制模式。

❑ rpl_semi_sync_master_wait_no_slave：表示是否允许master每个事务提交后都要等待slave的接收确认信号。默认为on，即每一个事务都会等待。如果为off，则slave追赶上后，也不会开启半同步复制模式，需要手工开启。

❑ rpl_semi_sync_master_trace_level = 32：表示用于开启半同步复制模式时的调试级别，默认是32。

在slave从库上共有2个配置参数，如下所示。

○ rpl_semi_sync_slave_enabled = ON：表示在slave上已经开启半同步复制模式。

○ rpl_semi_sync_slave_trace_level = 32：表示用于开启半同步复制模式时的调试级别，默认是32。

##### rpl_semi_sync_master_wait_point

在半同步复制中，主库会一直等待，直到至少有一个从库接收到写入的数据。默认情况下，rpl_semi_sync_master_wait_point的值是AFTER_SYNC，这意味着主库将事务同步到二进制日志，再由从库读取使用。之后，从库向主库发送确认消息，然后主库提交事务并将结果返回给客户端。所以，写入操作能到达中继日志就足够了，从库不需要提交这个事务。你可以将变量rpl_semi_sync_master_wait_point更改为AFTER_COMMIT来改变此行为。在这种情况下，主库将事务提交给存储引擎，但不会将结果返回给客户端。一旦事务在从库上提交，主库就会收到对事务的确认消息，然后将结果返回给客户端。

 

MySQL在5.5中引入了半同步复制，主库在应答客户端提交的事务前需要保证至少一个从库接收并写到relay log中，半同步复制通过rpl_semi_sync_master_wait_point参数来控制master在哪个环节接收 slave ack，master 接收到ack后返回状态给客户端，此参数一共有两个选项 AFTER_SYNC & AFTER_COMMIT。

***\*配置为WAIT_AFTER_COMMIT\****

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3648.tmp.jpg) 

rpl_semi_sync_master_wait_point为WAIT_AFTER_COMMIT时，commitTrx的调用在engine层commit之后，如上图所示。

即在等待Slave ACK时候，虽然没有返回当前客户端，但事务已经提交，其他客户端会读取到已提交事务。如果Slave端还没有读到该事务的events，同时主库发生了crash，然后切换到备库。

那么之前读到的事务就不见了，出现了数据不一致的问题：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3658.tmp.jpg) 

如果主库永远启动不了，那么实际上在主库已经成功提交的事务，在从库上是找不到的，也就是数据丢失了。

 

***\*配置为WAIT_AFTER_SYNC\****

MySQL官方针对上述问题，在5.7.2引入了Loss-less Semi-Synchronous，在调用binlog sync之后，engine层commit之前等待Slave ACK。这样只有在确认Slave收到事务events后，事务才会提交。

如下图所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3659.tmp.jpg) 

在after_sync模式下解决了after_commit模式带来的数据不一致的问题，因为主库没有提交事务。

但也会有个问题，当主库在binlog flush并且binlog同步到了备库之后，binlog sync之前发生了abort，那么很明显这个事务在主库上是未提交成功的（由于abort之前binlog未sync完成，主库恢复后事务会被回滚掉），但由于从库已经收到了这些Binlog，并且执行成功，相当于在从库上多出了数据，从而可能造成“数据不一致”。

此外，MySQL半同步复制架构中，主库在等待备库ack时候，如果超时会退化为异步后，也可能导致“数据不一致”。

##### rpl_semi_sync_master_wait_for_slave_count

如果你希望在更多的从库上确认事务，则可以增加动态变量rpl_semi_sync_master_wait_for_slave_count的值。

##### rpl_semi_sync_master_timeout

你还可以设置主库必须等待多少毫秒才能通过动态变量rpl_semi_sync_master_timeout获取从库的确认，其默认值是10秒。

在完全同步复制中，主库会一直等待，直到所有从库都提交了事务。要实现这一点，你必须使用Galera Cluster。

 

 

#### 操作

简单来说，你要在想做半同步复制的主库和所有从库上安装并启用半同步插件。你必须重新启动从库IO线程才能使这个变动生效。可以根据你的网络情况和应用程序调整rpl_semi_sync_master_timeout的值。可以把它设置为1秒。

1、在主库上，安装rpl_semi_sync_master插件：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps365A.tmp.jpg) 

确认插件已激活：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps366B.tmp.jpg) 

2、在主库上，启用半同步复制并调整超时（例如调整为1秒）：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps366C.tmp.jpg) 

3、在从库上，安装rpl_semi_sync_slave插件：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps366D.tmp.jpg) 

4、在从库上，启用半同步复制，并重新启动从库IO线程：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps367E.tmp.jpg) 

5、你可以通过以下方式监控半同步复制的状态。

要查找以半同步连接到主库的客户端数量，请执行：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps367F.tmp.jpg) 

当发生超时且从库赶上来时，主库在异步和半同步复制之间切换。要检查主库在使用的复制类型，请查看Rpl_semi_sync_master_status的状态（on表示半同步，off表示异步）：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3680.tmp.jpg) 

可以使用此方法来验证半同步复制。

\1) 停止从库的运行：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3690.tmp.jpg) 

\2) 在主库上随便执行一条语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3691.tmp.jpg) 

你会注意到主库已切换到异步复制，因为即使在1秒之后（rpl_semi_sync_master_timeout的值为1秒），主库也没有收到从库的任何确认信息：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3692.tmp.jpg) 

\3) 启动从库：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3693.tmp.jpg) 

\4) 在主库上，你会注意到主库已切换回半同步复制：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36A4.tmp.jpg) 

# 主从复制与一致性

## **单机数据一致性**

MySQL作为一个可插拔的数据库系统，支持插件式的存储引擎，在设计上分为Server层和Storage Engine层。

在Server层，MySQL以events的形式记录数据库各种操作的Binlog二进制日志，其基本核心作用有：复制和备份。

除此之外，我们结合多样化的业务场景需求，基于Binlog的特性构建了强大的MySQL生态，如：DTS、单元化、异构系统之间实时同步等等，Binlog早已成为MySQL生态中不可缺少的模块。

而在Storage Engine层，InnoDB作为比较通用的存储引擎，其在高可用和高性能两方面作了较好的平衡，早已经成为使用MySQL的首选。

和大多数关系型数据库一样，InnoDB采用WAL技术，即InnoDB Redo Log记录了对数据文件的物理更改，并保证总是日志先行，在持久化数据文件前，保证之前的redo日志已经写到磁盘。

Binlog和InnoDB Redo Log是否落盘将直接影响实例在异常宕机后数据能恢复到什么程度。InnoDB提供了相应的参数来控制事务提交时，写日志的方式和策略，例如：

innodb_flush_method:控制innodb数据文件、日志文件的打开和刷写的方式，建议取值：fsync、O_DIRECT。

innodb_flush_log_at_trx_commit:控制每次事务提交时，重做日志的写盘和落盘策略，可取值：0，1，2。

当innodb_flush_log_at_trx_commit=1时，每次事务提交，日志写到InnoDB Log Buffer后，会等待Log Buffer中的日志写到Innodb日志文件并刷新到磁盘上才返回成功。

sync_binlog：控制每次事务提交时，Binlog日志多久刷新到磁盘上，可取值：0或者n(N为正整数)。

不同取值会影响MySQL的性能和异常crash后数据能恢复的程度。当sync_binlog=1时，MySQL每次事务提交都会将binlog_cache中的数据强制写入磁盘。

innodb_doublewrite：控制是否打开double writer功能，取值ON或者OFF。

当Innodb的page size默认16K，磁盘单次写的page大小通常为4K或者远小于Innodb的page大小时，发生了系统断电/os crash ，刚好只有一部分写是成功的，则会遇到partial page write问题，从而可能导致crash后由于部分写失败的page影响数据的恢复。InnoDB为此提供了Double Writer技术来避免partial page write的发生。

innodb_support_xa:控制是否开启InnoDB的两阶段事务提交.默认情况下，innodb_support_xa=true，支持xa两段式事务提交。

以上参数不同的取值分别影响着MySQL异常crash后数据能恢复的程度和写入性能，实际使用过程中，需要结合业务的特性和实际需求，来设置合理的配置。比如:

 

 

MySQL单实例，Binlog关闭场景：

innodb_flush_log_at_trx_commit=1，innodb_doublewrite=ON时，能够保证不论是MySQL Crash 还是OS Crash 或者是主机断电重启都不会丢失数据。

MySQL单实例，Binlog开启场景：

 默认innodb_support_xa=ON，开启binlog后事务提交流程会变成两阶段提交，这里的两阶段提交并不涉及分布式事务，mysql把它称之为内部xa事务。

 当innodb_flush_log_at_trx_commit=1，sync_binlog=1，innodb_doublewrite=ON，,innodb_support_xa=ON时，同样能够保证不论是MySQL Crash 还是OS Crash 或者是主机断电重启都不会丢失数据。

但是，当由于主机硬件故障等原因导致主机完全无法启动时，则MySQL单实例面临着单点故障导致数据丢失的风险，故MySQL单实例通常不适用于生产环境。

 

## **集群数据一致性**

MySQL集群通常指MySQL的主从复制架构。

通常使用MySQL主从复制来解决MySQL的单点故障问题，其通过逻辑复制的方式把主库的变更同步到从库，主备之间无法保证严格一致的模式，于是，MySQL的主从复制带来了主从“数据一致性”的问题。MySQL的复制分为：异步复制、半同步复制、全同步复制。

# 配置步骤

配置读写mysql主从复制的步骤：

1、在主库与从库都安装mysql数据库

2、在主库的配置文件(/etc/my.cnf)中配置server-id 和log-bin

3、在登陆主库后创建认证用户并做授权。

4、在从库的配置文件(/etc/my.cnf)中配置server-id

5、登陆从库后，指定master并开启同步开关。

需要注意的是server-id主从库的配置是不一样的。

## server-id

**server-id存在作用：**

mysql同步的数据中是包含server-id的，而server-id用于标识该语句最初是从哪个server写入的。因此server-id一定要有的。

**server-id不能相同的原因：**

每一个同步中的slave在master上都对应一个master线程，该线程就是通过slave的server-id来标识的；

每个slave在master端最多有一个master线程，如果两个slave的server-id 相同，则后一个连接成功时，slave主动连接master之后，如果slave上面执行了slave stop；则连接断开，但是master上对应的线程并没有退出；当slave start之后，master不能再创建一个线程而保留原来的线程，那样同步就可能有问题；

在mysql做主主同步时，多个主需要构成一个环状，但是同步的时候有要保证一条数据不会陷入死循环，这里就是靠server-id来实现的。

 

## **复制用户账号**

设置复制有如下几个步骤： 第一步是在主服务器和从服务器上创建与复制有关的用户账户。为了安全起见，最好不要使用现有的账户。要想创建账户，首先以超级用户（root）或具有GRANT OPTION权限的用户登录，在主服务器上输入类似下面的SQL语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36A5.tmp.jpg) 

这二个权限是用户复制服务器必需的权限。REPLICATE SLAVE权限允许用户连接到主服务器并接收主服务器的二进制日志的更新。REPLICATE CLIENT权限允许用户执行SHOW MASTER STATUS语句和SHOW SLAVE STATUS语句。在这条SQL语句中，仅允许用户账户replicant执行复制所必须的操作。用户名可以为任何名字。在SQL语句中，需要在引号内给出用户名和主机。主机名可以通过/etc/hosts解析（或在你的系统上与之等价的文件），或者是通过DNS解析的域名。若不给定主机名，你可以给定IP地址：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36A6.tmp.jpg) 

如果近期你把MySQL服务器升级到了4.x版本，但是并没有对mysql数据库升级，便不能执行上面显示的GRANT语句，因为在MySQL的老版本中并不存在这些权限。解决这个问题的更多信息，请参阅mysql_fix_privilege_tables。

现在，在从服务器上以相同的用户名和密码输入相同的GRANT语句，但是却用主服务器的主机名和IP地址：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36B7.tmp.jpg) 

在主服务器和从服务器上使用相同的用户名有一个潜在的优势： 如果主服务器出现问题而发生崩溃的话，你可以用DNS或其他一些方法把用户重定向到从服务器。当备份主服务器时，你可以用复制使主服务器上的数据保持最新，使得从服务器与前面的主服务器保持同步。然而，这是比较麻烦的，这已超出了本书的范围。更多细节信息，请参见High Performance MySQL（O,Reilly）。你应该用一些测试服务器试验并实践一下这种方法，在信任此种方法以前，应该用生产服务器。

输入下面的语句，可以查看第一条主服务器的GRANT语句的执行结果：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36B8.tmp.jpg) 

注意，顺便提及，在输出结果中已对密码做了加密。如果你没有得到类似上面的结果，那么说明GRANT语句没有执行成功。当你授予权限执行该语句时，请检查你输入的内容。如果输入的这些SQL语句内容没有错误，请确保你使用的为MySQL 4.0及以上版本，只有这些版本的MySQL才支持这二个新的权限。在每个服务器上输入SELECT VERSION( )语句，以确定所用的MySQL版本。

 

## **配置服务器**

参考：《MySQL核心技术手册》

### **slave配置**

slave-skip-error=1007,1008,1050,1060,1061,1062,1068

slave-parallel_workers=32

slave-parallel-type=LOGICAL_CLOCK

slave_perserve_commit_order=1

relay_log_recovery=1

log-slave-updates=1

skip-slave-start=1

 

### **master配置**

# 启动项

## **log-slave-updates** 

这个参数用来配置从库上的更新操作是否写二进制日志，默认是不打开的。但是，如果这个从库同时也要作为其他服务器的主库，搭建一个链式的复制，那么就需要打开这个选项，这样它的从库将获得它的二进制日志以进行同步操作。

## **master-connect-retry**

这个参数用来设置在和主库的连接丢失时重试的时间间隔，默认是60秒，即每60秒重试一次。

## **read-only**

read-only该参数用来设置从库只能接受超级用户的更新操作，从而限制应用程序错误的对从库的更新操作。

## 复制筛选器/指定复制的数据库或者表

可以使用 replicate-do-db、replicate-do-table、replicate-ignore-db、replicate-ignore-table或replicate-wild-do-table 来指定从主数据库复制到从数据库的数据库或者表。有时用户只需要将关键表备份到从库上，或者只需要将提供查询操作的表复制到从库上，这样就可以通过配置这几个参数来筛选进行同步的数据库和表。

 

你可以选择要复制哪些表或数据库。在主库上，可以使用--binlog-do-db 和--binlog-ignore-db选项来选择要记录变更的数据库，以控制二进制日志。更好的方法是控制从库。你可以使用--replicate-*选项或通过创建复制筛选器来动态执行或忽略从主库收到的语句。

***\*操作步骤：\****

你需要执行CHANGE REPLICATION FILTER语句。

***\*仅复制数据库\****

假设你只想复制db1和db2。使用以下语句来创建复制筛选器：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36B9.tmp.jpg) 

请注意，应该在括号内指定要复制的所有数据库。

***\*复制特定的表\****

可以使用REPLICATE_DO_TABLE指定要复制的表：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36C9.tmp.jpg) 

假设你想使用正则表达式来选择表，就可以使用REPLICATE_WILD_DO_TABLE选项：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36CA.tmp.jpg) 

你可以使用不同的IGNORE选项，并使用正则表达式来指定数据库或表。

***\*忽略数据库\****

就像可以选择复制某个数据库一样，也可以使用 REPLICATE_IGNORE_DB 指定你不想复制的数据库：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36DB.tmp.jpg) 

***\*忽略特定的表\****

可以使用REPLICATE_IGNORE_TABLE和REPLICATE_WILD_IGNORE_TABLE选项忽略某些表。REPLICATE_WILD_IGNORE_TABLE 选项允许使用通配符，而REPLICATE_IGNORE_TABLE只接受完整的表名：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36DC.tmp.jpg) 

## **slave-skip-errors**

在复制过程中，由于各种原因，从库可能会遇到执行BINLOG中的SQL出错的情况（比如主键冲突），默认情况下，从库将会停止复制进程，不再进行同步，等待用户介入处理。这种问题如果不能及时发现，将会对应用或者备份产生影响。此参数的作用就是用来定义复制过程中从库可以自动跳过的错误号，这样当复制过程中遇到定义中的错误号时，便可以自动跳过，直接执行后面的 SQL 语句，以此来最大限度地减少人工干预。此参数可以定义多个错误号，或者通过定义成all跳过全部的错误，具体语法如下：--slave-skip-errors=[err_code1,err_code2,. . | all]

如果从数据库主要是作为主数据库的备份，那么就不应该使用这个启动参数，设置不当，很可能造成主从数据库的数据不同步。但是，如果从数据库仅仅是为了分担主数据库的查询压力，且对数据的完整性要求不是很严格，那么这个选项的确可以减轻数据库管理员维护从数据库的工作量。

# 日常运维

## **相关指令**

CHANGE MASTER TO

LOAD DATA FROM MASTER

LOAD TABLE FROM MASTER

MASTER_POS_WAIT

PURGE MASTER LOGS

RESET MASTER

RESET SLAVE

SET GLOBALSQL_SLAVE_SKIP_COUNTER

SET SQL_LOG_BIN

SHOW BINARY LOGS

SHOW MASTERLOGS

SHOW BINLOG EVENTS

SHOW MASTER STATUS

SHOW SLAVE HOSTS

SHOWSLAVE STATUS

START SLAVE

STOP SLAVE

## **查看主从复制的状态**

当主从复制正在进行中时，如果想查看从库两个线程运行状态，可以通过执行在从库里执行”show slave statusG”语句，以下的字段可以给你想要的信息：

master_log_file — 上一个从主库拷贝过来的binlog文件

read_master_log_pos — 主库的binlog文件被拷贝到从库的relay log中的位置

relay_master_log_file — SQL线程当前处理中的relay log文件

exec_master_log_pos — 当前binlog文件正在被执行的语句的位置

 

## **提高复制性能**

# 故障处理

## **常见3种故障**

在说明最常见的3种故障之前，先来看一下异步复制和半同步复制的区别：

❑ 异步复制：简单地说就是master把binlog发送过去，不管slave是否接收完，也不管是否执行完，这一动作就结束了。

❑ 半同步复制：简单地说就是master把binlog发送过去，slave确认接收完，但不管它是否执行完，给master一个信号我这边收到了，这一动作就结束了。（半同步复制patch谷歌写的代码，MySQL5.5上正式应用。）

异步的劣势是：当master上写操作繁忙时，当前POS点，例如，是10，而slave上IO_THREAD线程接收过来的是3，此时master宕机，会造成相差7个点未传送到slave上而数据丢失。

接下来要介绍的这3种故障是在HA集群切换时产生的，由于是异步复制，且sync_binlog=0，会造成一小部分binlog没接收完，从而导致同步报错。

### **在master上删除一条记录时出现的故障**

在master上删除一条记录后，slave上因找不到该记录而报错，报错信息如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36DD.tmp.jpg) 

出现这种情况是因为主机上已将其删除了，对此，可采取从机直接跳过的方式解决，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36DE.tmp.jpg) 

对于这种情况，我写了一个脚本skip_error_replication.sh来帮助处理，该脚本默认跳过10个错误（只会针对这种情况跳，其他情况还是会输出错误结果，等待处理），这个脚本是参考maakit工具包的mk-slave-restart原理用shell写的，由于mk-slave-restart脚本是不管什么错误一律跳过，这样会造成主从数据不一致，因此我在该脚本的功能方面定义了一些自己的东西，使其不是无论什么错误都一律跳过），脚本如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36EE.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36EF.tmp.jpg) 

### **主键重复**

主从数据不一致时，slave上已经有该条记录，但我们又在master上插入了同一条记录，此时就会报错，报错信息如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps36F0.tmp.jpg) 

解决方法：在slave上使用命令“desc hcy.t1; ”先查看一下表结构，如下所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3701.tmp.jpg) 

查看该表字段信息，得到主键的字段名。

接着删除重复的主键，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3702.tmp.jpg) 

然后开启同步复制功能，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3703.tmp.jpg) 

完成上述操作后，还要在master上和slave上再分别确认一下，确保执行成功。

### **在master上更新一条记录，而slave上却找不到**

主从数据不一致时，master上已经有该条记录，但slave上没有这条记录，之后若在master上又更新了这条记录，此时就会报错，报错信息如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3704.tmp.jpg) 

解决方法：在master上，用MySQL binlog分析一下出错的binlog日志在干什么，如下所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3715.tmp.jpg) 

从上面的信息来看，是在更新一条记录。接着，在slave上查找一下更新后的那条记录，应该是不存在的。命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3716.tmp.jpg) 

然后再到master查看，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3717.tmp.jpg) 

可以看到，这里已经找到了这条记录。

最后把丢失的数据填补到在slave上，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3718.tmp.jpg) 

完成上述操作后，跳过报错即可，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3728.tmp.jpg) 

 

### 多主复制时的自增长变量冲突

冲突问题在大多数情况下，一般只使用单主复制（一台主库对一台或者多台从库）。但是在某些情况下，可能会需要使用多主复制（多台主库对一台从库）。这时，如果主库的表采用自动增长变量，那么复制到从库的同一张表后很可能会引起主键冲突，因为系统参数 auto_increment_increment和auto_increment_offset的默认值为1，这样多台主库的自增变量列迟早会发生冲突。在单主复制时，可以采用默认设置，不会有主键冲突发生。但是使用多主复制时，就需要定制auto_increment_increment 和 auto_increment_offset 的设置，保证多主之间复制到从数据库不会有重复冲突。比如，两个master的情况可以按照以下设置。

Master1上：auto_increment_increment = 2，auto_increment_offset = 1；（1,3,5,7…序列）。

Master2上：auto_increment_increment = 2，auto_increment_offset = 0；（0,2,4,6…序列）。

 

## **从库复制出错**

在某些情况下，会出现从库更新失败，这时，首先需要确定是否是从库的表与主库的不同造成的。

如果是表结构不同导致的，则修改从库的表使之与主库的相同，然后重新运行STARTSLAVE语句。如果不是表结构不同导致的更新失败，则需要确认手动更新是否安全，然后忽视来自主库的更新失败的语句。跳过来自主库的语句的命令为 SET GLOBAL SQL_SLAVE_ SKIP_COUNTER = n，其中 n的取值为 1或者2。如果来自主库的更新语句不使用 AUTO_INCREMENT 或 LAST_INSERT_ID()，n 值应为 1，否则，值应为 2。原因是使用 AUTO_INCREMENT或LAST_INSERT_ID()的语句需要从二进制日志中取两个事件。以下例子就是在从库端模拟跳过主库的两个更新语句的效果。

（1）首先，在从库端先停止复制进程，并设置跳过两个语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3729.tmp.jpg) 

（2）在主库端插入3条记录：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps373A.tmp.jpg) 

（3）从库端启动复制进程，检查测试的表，发现首先插入的两条记录被跳过了，只执行了第3条插入语句：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps373D.tmp.jpg) 

参考：《深入浅出MYSQL》

https://blog.csdn.net/a86793222/article/details/101606041

 

## **从库崩溃安全恢复**

### **MySQL 5.6/5.7从库崩溃安全恢复**

DBA经常会遇到1032（更新/删除数据找不到）和1062错误（主键冲突），这就是因为从库宕机后，relay-log是以文件形式写盘，没有事务的概念。

我们先看MySQL 5.6的relay-log.info工作原理，如图2-152所示。参数relay_log_info_repository =FILE默认是保存在文件里的。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps373E.tmp.jpg) 

sql线程每次提交一个事务，就会记录在relay.info文件里。假如在刷盘那一刻宕机，relay-log里没有记录，那么从库重启mysql进程后，就会执行两遍同样的SQL语句，造成同步复制报错。

再来看看把relay-log.info写入表里的情况，命令如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps374F.tmp.jpg) 

relay_log_info_repository = TABLE的工作原理如图2-153所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3750.tmp.jpg) 

这样sql线程执行完事务后，立即会更新***\*slave_relay_log_info\****表，如果在更新过程中宕机，则事务会回滚，slave_relay_log_info表并不会记录同步的点，下次重新同步复制时，从之前的POS点再次执行。

另外，从MySQL 5.5或MariaDB 10.0版本开始，增加了relay_log_recovery参数，这个参数的作用是：当slave从库宕机后，假如relay-log损坏，导致一部分中继日志没有处理，则自动放弃所有未执行的relay-log，并且重新从MASTER上获取日志，这样就保证了relay-log的完整性。默认情况下该功能是关闭的，将relay_log_recovery的值设置为1时，可在slave从库上开启该功能，建议开启。

 

### **MariaDB 10.0/10.1从库崩溃安全恢复**

在MariaDB 10.0.X和10.1.X上不支持relay_log_info_repository =TABLE参数，官网建议用GTID复制模式代替传统的复制模式，传统的复制模式是不支持Slave Crash-Safe的，如图2-154所示。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps375E.tmp.jpg) 

其工作原理同MySQL 5.6/5.7的relay_log_info_repository = TABLE工作原理，这里不再叙述。

## **mysql Slave_IO_Running:NO**

参考：

https://cloud.tencent.com/developer/article/1401408

https://www.jb51.net/article/27220.htm

 

## **The slave I/O thread stops because master and slave have equal MySQL server UUIDs**

参考：

https://blog.csdn.net/tpc4289/article/details/79899089

https://cloud.tencent.com/developer/article/1401407

 

## **Slave中继日志出错**

当slave意外宕机时，有可能会损坏中继日志relay-log，再次开启同步复制时，报错信息如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3762.tmp.jpg) 

解决方法：找到同步的binlog日志和POS点，然后重新进行同步，这样就可以有新的中继日志了。

下面来看个例子，这里模拟了中继日志损坏的情况，查看到的信息如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3763.tmp.jpg) 

其中，涉及几个重要参数：

❑ slave_IO_Running ：接收master的binlog信息

○ master_Log_File：正在读取master上binlog日志名。

○ Read_master_Log_Pos：正在读取master上当前binlog日志POS点。

❑ slave_SQL_Running：执行写操作。

○ Relay_master_Log_File：正在同步master上的binlog日志名。

○ Exec_master_Log_Pos：正在同步当前binlog日志的POS点。

以Relay_master_Log_File参数值和Exec_master_Log_Pos参数值为基准。Relay_master_Log_File: MySQL-bin.000010Exec_master_Log_Pos: 821接下来可以重置主从复制了，操作如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3771.tmp.jpg) 

重新建立完主从复制以后，就可以查看一下状态信息了，如下所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3775.tmp.jpg) 

通过这种方法我们已经修复了中继日志。是不是有些麻烦？其实如果你有仔细看完第1章的新特性，会发现MySQL5.5已经考虑到slave宕机中继日志损坏这一问题了，即在slave的配置文件my.cnf里要增加一个参数relay_log_recovery =1就可以了，前面已经介绍了，这里不再阐述。

参考：《MySQL管理之道》

## **主备不一致**

## **主从复制延迟**

在异步或半同步的复制结构中，从库出现延迟是一件十分正常的事。

虽出现延迟正常，但是否需要关注，则一般是由业务来评估。

如：从库上有需要较高一致性的读业务，并且要求延迟小于某个值，那么则需要关注。

***\*复制逻辑：\****

1、主库将对数据库实例的变更记录到binlog中。 

2、主库会有binlog dump线程实时监测binlog的变更并将这些新的events推给从库（Master has sent all binlog to slave; waiting for more updates）

3、从库的IO Thread接收这些events，并将其记录入relaylog。

4、从库的SQL Thread读取relaylog的events，并将这些events应用（或称为重放）到从库实例。

上述为默认的异步复制逻辑，半同步复制又有些许不同，此处不再赘述。

 

### **设置延迟复制**

有时，你需要用一个延迟的从库进行灾难恢复。假设在主库上执行了一条灾难性语句（例如DROPDATABASE命令）。你必须使用备份中的时间点恢复（point-in-time recovery）来恢复数据库。这将导致长时间停机，停机的具体时长取决于数据库的大小。为了避免出现这种情况，你可以使用一个延迟的从库（delayed slave），该从库总是比主库延迟一段时间（这个时间是可以配置的）。如果发生了灾难，并且该延迟的从库没有执行这条灾难性语句，则可以先停止从库的运行，再启动从库，一直运行到这条灾难性语句的位置，这样该语句就不会被执行。最后，把该从库提升为主库。

除了在CHANGE MASTER TO命令中指定MASTER_DELAY之外，该过程与设置传统复制的过程完全相同。

#### 度量延迟

在MySQL 8.0之前的版本中，延迟是基于Seconds_Behind_Master的值来度量的。在MySQL 8.0中，延迟是基于写入二进制日志的 original_commit_timestamp 和immediate_commit_timestamp的值来度量的。

original_commit_timestamp是自事务写入（提交）到原主库的二进制日志时以来的微秒数。

immediate_commit_timestamp 是自事务写入（提交）到直接主库的二进制日志时以来的微秒数。

#### 操作

1、停止从库的运行：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3776.tmp.jpg) 

2、执行CHANGE MASTER TO MASTER_DELAY=命令，并启动从库。假设你想要1小时的延迟，可以设置MASTER_DELAY为3600秒：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3783.tmp.jpg) 

3、在从库状态中检查以下内容。

SQL_Delay：从库必须延迟于主库的秒数。

SQL_Remaining_Delay：延迟还剩余的秒数。当保持延迟时，这个值是NULL。

Slave_SQL_Running_State：SQL线程的状态。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3787.tmp.jpg) 

请注意，延迟消除后，Seconds_Behind_Master将显示为0。

### **判断从库延迟**

此外，判断从库有延迟是十分简单的一件事：

在从库上通过SHOW SLAVE STATUS

检查Seconds_Behind_Master值即可。

 

### **原因及处理思路**

主库问题：

1、网络延迟（跨地域较明显）

2、Master上并发事务多，slave上则多为单线程回放（MySQL5.7开始，支持并行回放）

3、表结构设计不合理，MySQL5.6之前没有主键，会造成大量更新进行全表扫描，效率极低

4、业务存在大量大事务，可能导致slave无法并行回放

5、设置了半同步、强同步，降级时间设置过大

从库问题：

1、slave服务器与master服务器性能不匹配，slave服务器性能低；

2、为节省机器资源，在slave上运行多个实例，系统资源造成瓶颈；

3、Slave上运行大量低效率的只读SQL

4、Slave上逻辑备份，导致出现锁竞争

 

具体地址，产生延迟的原因及处理思路：

#### 主库DML请求频繁（tps较大）

即主库写请求较多，有大量insert、delete、update并发操作，短时间产生了大量的binlog。

***\*原因分析：\****

主库并发写入数据，而从库SQL Thread为单线程应用日志，很容易造成relaylog堆积，产生延迟。

***\*解决思路：\****

做sharding，通过scale out打散写请求。或考虑升级到MySQL 5.7+，开启基于逻辑时钟的并行复制。

 

#### 主库执行大事务

比如大量导入数据，INSERT INTO $tb1 SELECT * FROM $tb2、LOAD DATA INFILE等

比如UPDATE、DELETE了全表等

Exec_Master_Log_Pos一直未变，Slave_SQL_Running_State为Reading event from the relay log

分析主库binlog，看主库当前执行的事务也可知晓。

***\*原因分析：\****

假如主库花费200s更新了一张大表，在主从库配置相近的情况下，从库也需要花几乎同样的时间更新这张大表，此时从库延迟开始堆积，后续的events无法更新。

***\*解决思路：\****

拆分大事务，及时提交。

 

#### 主库对大表执行DDL语句

现象和主库执行大事务相近。

检查Exec_Master_Log_Pos一直未动，也有可能是在执行DDL。

分析主库binlog，看主库当前执行的事务也可知晓。

***\*原因分析：\****

1、DDL未开始，被阻塞，SHOW SLAVE STATUS检查到Slave_SQL_Running_State为waiting for table metadata lock，且Exec_Master_Log_Pos不变。

2、DDL正在执行，SQL Thread单线程应用导致延迟增加。Slave_SQL_Running_State为altering table，Exec_Master_Log_Pos不变

***\*解决思路：\****

通过processlist或information_schema.innodb_trx来找到阻塞DDL语句的查询，干掉该查询，让DDL正常在从库执行。

DDL本身造成的延迟难以避免，建议考虑：

① 业务低峰期执行

② set sql_log_bin=0后，分别在主从库上手动执行DDL（此操作对于某些DDL操作会造成数据不一致，请务必严格测试）

 

#### 主库与从库配置不一致

***\*原因分析：\****

硬件上：主库实例服务器使用SSD，而从库实例服务器使用普通SAS盘、cpu主频不一致等

配置上：如RAID卡写策略不一致，OS内核参数设置不一致，MySQL落盘策略不一致等

***\*解决思路：\****

尽量统一DB机器的配置（包括硬件及选项参数）

甚至对于某些OLAP业务，从库实例硬件配置高于主库等

 

#### 表缺乏主键或唯一索引

binlog_format=row的情况下，如果表缺乏主键或唯一索引，在UPDATE、DELETE的时候可能会造成从库延迟骤增。

此时Slave_SQL_Running_State为Reading event from the relay log。

并且SHOW OPEN TABLES WHERE in_use=1的表一直存在。

Exec_Master_Log_Pos不变。

mysqld进程的cpu几近100%（无读业务时），io压力不大

***\*原因分析：\****

做个极端情况下的假设，主库更新一张500w表中的20w行数据，该update语句需要全表扫描

而row格式下，记录到binlog的为20w次update操作，此时SQL Thread重放将特别慢，每一次update可能需要进行一次全表扫描

***\*解决思路：\****

检查表结构，保证每个表都有显式自增主键，并建立合适索引。

 

#### 从库自身压力过大

***\*原因分析：\****

从库执行大量select请求，或业务大部分select请求被路由到从库实例上，甚至大量OLAP业务，或者从库正在备份等。

此时可能造成cpu负载过高，io利用率过高等，导致SQL Thread应用过慢。

 

原因总结：

1、slave服务器与master服务器性能不匹配，slave服务器性能低；

2、为节省机器资源，在slave上运行多个实例，系统资源造成瓶颈；

3、Slave上运行大量低效率的只读SQL

4、Slave上逻辑备份，导致出现锁竞争

 

***\*解决思路：\****

建立更多从库，打散读请求，降低现有从库实例的压力。

 

#### MyISAM存储引擎

此时从库Slave_SQL_Running_State为Waiting for table level lock

***\*原因分析：\****

MyISAM只支持表级锁，并且读写不可并发操作。

主库在设置@@concurrent_insert对应值的情况下，能并发在select时执行insert，但从库SQL Thread重放时并不可并发，有兴趣可以再去看看myisam这块的实现。

***\*解决思路：\****

当然是选择原谅它了，既然选择了MyISAM，那么也应该要有心理准备。（还存在其他场景，也不推荐MyISAM在复制结构中使用）

改成InnoDB吧。

 

#### 总结

通过SHOW SLAVE STATUS与SHOW PROCESSLIST查看现在从库的情况。（顺便也可排除在从库备份时这种原因）

若Exec_Master_Log_Pos不变，考虑大事务、DDL、无主键，检查主库对应的binlog及position即可。

若Exec_Master_Log_Pos变化，延迟逐步增加，考虑从库机器负载，如io、cpu等，并考虑主库写操作与从库自身压力是否过大。

 

当然，Seconds_Behind_Master也不一定准确，存在在少部分场景下，虽Seconds_Behind_Master为0，但主从数据不一致的情况。

 

经典案例分析：

[https://mp.weixin.qq.com/s?__biz=Mzg4NjA4NTAzNQ==&mid=2247485324&idx=1&sn=9afc82915aa7347130445c32a14a25ba&chksm=cf9e4348f8e9ca5e17f4f8aa4fcb415c06fb755e4bbe53d52373d5677c6170191db7caa1e3cd&mpshare=1&&srcid=&sharer_sharetime=1575556237908&sharer_shareid=42ba259b81cc8809b4e0103fdee44043&from=timeline&scene=2&subscene=1&clicktime=1575593066&enterid=1575593066#rd](#rd)

# 分布式数据库实践

## **TDSQL**

由于数据库中记录了数据，想要通过高可用架构实现切换，数据必须是完全一致且同步的，所以数据同步技术是数据库高可用方案的基础，通常数据同步的流程如下图：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3788.tmp.jpg) 

当前，开源MySQL数据库数据复制包括异步复制、半同步复制两种。这两种复制技术的主要问题是，节点故障时，有可能导致数据丢失或错乱。而且，这类复制技术以串行复制为主，性能相对比较低。而腾讯自主研发了的基于MySQL协议的并行多线程强同步复制方案（Multi-thread Asynchronous Replication， MAR），在应用发起请求时，只有当从节点(Slave)节点返回成功信息后，主节点（Master）节点才向应用应答请求成功（如下流程图）；这样就可以确保主从节点数据完全一致。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3796.tmp.jpg) 

说明：使用“强同步”复制时，如果主库与备库自建网络中断或备库出现问题，主库也会被锁住（hang），而此时如果只有一个主库或一个备库，那么是无法做高可用方案的。（因为单一服务器服务，如果故障则直接导致部分数据完全丢失，不符合金融级数据安全要求。）因此，TDSQL在强同步技术的基础上，提供强同步可退化的方案，方案原理类似于半同步，但实现方案与google的半同步技术不同。

另外，TDSQL强同步将串行同步线程并行化，引入工作线程能力，大幅度提高性能；对比在跨可用区(IDC机房，延迟约10~20ms)同样的测试方案下，我们发现MAR技术性能优于MySQL 5.6的半同步约5倍，优于MariaDB Galera Cluster性能1.5倍，在OLTP RW(读写混合，主从架构)，是MySQL 5.7异步的1.2倍（如下由英特尔®技术团队测试的性能图）：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps379A.tmp.jpg) 

为进一步验证强同步数据一致性，我们在每秒插入2万行数据的场景下，直接杀掉主机数据库进程，并在切换备机后导出流水做对比，发现数据完全一致。

 

## **GoldenDB**

### **原理**

MySQL半同步复制过程中binary log dump线程和从库replay log SQL线程都是单线程工作，这在一定程度生会影响复制的性能。***\*GoldenDB中依然采用的是mysql半同步复制技术，不过利用多线程和并行复制技术对binlog数据同步进行了优化\****，在高并发、高网络时延下性能可以提升20%。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps379B.tmp.jpg) 

1）并行复制

原生MySQL中binlog dump thread和SQL thread是单线程的，在GoldenDB中使用多线程对此进行了优化。

2）线程池

减少线程重复创建和销毁开销，提高性能

线程池预先创建一定数量的线程，在监听到新的连接请求时，线程池直接从现有线程中分配线程提供服务。服务结束后该线程不会直接销毁，而是去处理其它请求，避免了线程和内存对象的频繁创建和销毁，减少了上下文切换，提高了资源利用率。

系统保护，防止DB雪崩

线程池技术同时也限制了并发线程数，即限制了MySQL运行的线程数。当连接或请求超过设置的最大线程数时候，新的请求需要排队，防止DB出现雪崩，保护底层DB。

3）机房间的数据同步方式

本地机房和同城机房数据采用快同步复制

本地机房和同城机房元数据采用快同步复制

本地机房/同城机房和异地机房间数据采用异步复制

本地机房/同城机房和异地机房间元数据采用快同步复制

***\*同城机房GTM采用实时同步复制\****，异地机房GTM不从主机房同步数据

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps37AA.tmp.jpg) 

说明：可以对语句设置hint信息，某些语句实现同步，某些可以异步，这样提高效率。

###  **复制优化(快同步)**

分布式数据库通过增加副本数来提高系统的可用性，为了数据的安全可靠，数据必须在满足拥有一定数量的副本之后，才返回处理结果给客户端。GoldenDB采用自研的gSync复制技术，在配置的副本同步策略满足后，主库返回操作结果给客户端；**通过线程池、非阻塞式同步、并行复制等关键技术可以实现在确保数据RPO为0的同时保证系统的吞吐量**。

### **主备一致性校验**

#### pt-table-checksum

##### 原理

pt-table-checksum将每个表按照行分成块，使用REPLACE...SELECT查询检查每块数据的一致性，通过改变块大小保证一致性校验的运行时间，每块校验的目标时间默认为0.5s。

##### 命令

项目代码生成命令：

pt-table-checksum --no-check-binlog-format --databases=

A=utf8,h=10.10.10.10.P=6666,u=username,p=’password’

--nocheck-replication-filters,

--ignore-databases=mysql,percona,dbagent

--tables=

--ignore-tables=percona.checksums

--chunk-szie-limit=2

--recursion-method=hosts 	1>/home.dbagent_guardianft/table-sync/****_checksum.log 2>&1

##### 参数

| 参数                          | 解释                                                         |
| ----------------------------- | ------------------------------------------------------------ |
| --nocheck-replication-filters | 不检查复制过滤器，建议启用。可以用--databases来指定需要检查的数据库。默认在检查到在主从复制过程中又被用ignore过滤的表，检查会中断并退出，如果想避开这个检查可以设置--no-check-replication-filters |
| --no-check-binlog-format      | 不检查复制的binlog模式，要是binlog模式是ROW，则会报错。默认会检查binlog-format，默认不是statement，就会报错，想避免检查可以设置--no-check-binlog-format |
| --replicate-check-only        | 只显示不同步的信息                                           |
| --replicate=                  | 用来指定存放计算结果的表名，默认是percona.checksums          |
| --databases=                  | 指定需要被检查的数据库，多个则用逗号隔开                     |
| A=                            | 字符集                                                       |
| u=                            | 用户名                                                       |
| h=                            | IP，主机ip必须指定，备机ip未指定默认情况下搜索DSN能搜到的所有备机，备机ip指定时只检查主机和备机的一致性，第一个ip为主机，第二个ip为备机 |
| p=                            | 密码                                                         |
| P=                            | 端口                                                         |
| --tables=                     | 指定需要被忽略的表，多个用逗号隔开                           |
| --ignore-databases=           | 检查时忽略的库                                               |
| --ignore-tables               | 检查时忽略的表，项目代码中自动配置percona.checksums          |
| --chunk-size-limit=           | 不需要对比该限制大的多（如超过该限制两倍）的块进行校验和。无索引该项默认值为2，该限制大于2时，可以对较大的块进行校验，项目代码中初始化为0，禁止超大块校验 |
| --recursion-methods           | Processlist、hosts、dsb=DSN或no。pt-tbale-checksums工具自动探测所有备机，但为以防探测失败，配置该项指定寻找备机的方法，默认为processlist，项目代码中指定为hosts。服务端口非标准端口（3306）时，hosts更优。 |

##### 退出状态（EXIT STATUS）

有三种可能的退出状态：

1、0

主备一致，没有错误告警，没有不一致校验结果，没有跳过块或表

2、255

pt-table-checksum工具致命错误

3、其他值

 

##### 返回结果

| 列名    | 解释                                                         |
| ------- | ------------------------------------------------------------ |
| TS      | 完成检查的时间                                               |
| ERRORS  | 检查时候发生错误和告警的数目                                 |
| IDFFS   | 0表示一直，1表示不一致。当指定--no-replicate-check时，会一直为0当指定--replicate-check-only会显示不同的信息 |
| ROWS    | 表的行数                                                     |
| CHUNKS  | 被划分到表中的块的数目                                       |
| SKIPPED | 由于错误或告警过大，则跳过块的数目                           |
| TIME    | 执行的时间                                                   |
| TABLE   | 被检查的表名                                                 |

 

 

#### pt-table-sync

##### 原理

对表数据进行单向或双向同步，它不同步表结构、索引或任何其他结构对象。

 

##### 命令

pt-table-sync --charset=utf8 --print --sync-to-master

A=utf8,h=10.10.10.10,P=6667,u-username,p=’password’

--ignore-databases=mysql,percona,dnagent --tables=alltype.t_all_type

\>/home/dbagent_gudianft/table-sync/****_ip_port_sql 2>&1!

##### 参数

| 参数                | 解释                                                         |
| ------------------- | ------------------------------------------------------------ |
| A=                  | 备机用户名                                                   |
| u=                  | 备机用户名                                                   |
| h=                  | 备机IP                                                       |
| p=                  | 备机密码                                                     |
| P=                  | 备机端口                                                     |
| --tables            | 指定需要被检查的表，多个逗号隔开                             |
| --ignore-databases= | 忽略的数据库，项目代码中自动配置mysql,percona,dbagent        |
| --ignore-tables     | 指定执行同步的表，多个逗号隔开                               |
| --print             | 打印修复主备一致性的SQL语句，但是不执行                      |
| --execute           | 执行命令                                                     |
| --[no]check-master  | 和--sync-to-master配合使用，验证检测的主机是否为真主机       |
| --[no]check-slave   | 检测目标服务器是否为备机，pt-table-sync默认对备机的操作告警，--no-check-slave不做备机检查有一定风险 |

 

##### 退出状态

 

### **故障判断**

***\*怎么判断发生了故障？\****

1、DBAgent主动上报DB的状态为失败，这个时候就认定故障，不需要额外进行判断

2、CM的DBAgent断链超过一定次数（默认是10s，3次），这种心跳检测只能说明CM和DBAgent断链了，还需要其他的一些判断：

1）查看该主机下面的备机是SQL/IO线程是否异常，如果异常，这个时候不需要切换

2）计算当前集群中与CM断链的总数，如果超过某个值（隐藏配置项），则认定是网络故障。

排除上述两种情况，就认定主机fail了，此时需要执行故障切换。

***\*切换条件检查\****

进入切换的一些前期检查，判断当前情况下是否可以进行切换操作？

1、当前CM的状态，是否已经启服，如果未启服，则不会执行切换

2、根据切换策略（手动同城还是自动切换），初步判断是否存在可用的DB来供选择切换，如果没有可用的DB（RDB中存储相关备机信息），则同样不会执行切换

 

***\*状态机\****

故障切换包含很多流程，每个流程会对应一个状态，目前包含的状态有：

1、PM停服

2、设定DB只读

3、查询DB的GTID

4、查询水位信息

5、切换策略判断（高可用、高一致性、补数据）

6、连接到同城DB进行补数据

7、设定PM只读启服

8、主备切换

9、新主设定读写

10、PM启服

状态转换图如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps37AB.tmp.jpg) 

各个状态机的含义：

1、PM停服

只要涉及主的切换都会首先需要到PM申请停服，当前停服也不是PM的操作，是通过PM透传给各个proxy，让proxy执行停服，proxy停服后，再接收到外部的业务就直接拒绝了，停服是group级别的，某个group的停服是不影响其他group的业务操作的，在申请停服的时候会将在哪个group哪个主机停服这些详细信息发送给proxy，停服需要所有的proxy都停止成功，如果有一个停服失败，都表示停服任务失败。

2、DB只读

设定DB的read_only配置项为ON，这个是设定该group下的所有机器，在成功情况下，所有DB（除了故障主机）本身的值就应该是只读的，这个地方重新设置一下是为了更加保险一些，设定备机只读的目的主要是防止：

1）某个proxy在与PM断链后，仍然往旧主上发送数据（脑裂）

2）用户或者其他程序直连到DB上

3、查询GTID

这个流程很简单，就是向该city下面的所有备机发送查询GTID的请求，后期通过GTID来选择新主。

注：这个与TDSQL的同步机制不同，TDSQL要求所有备机全部同步成功才认为成功，切换的时候任何一个备机都是可以的（可以设置备机的优先级，OceanBase则是通过paxos智能选主），因为肯定是数据一致的。我们只要求有一个备机返回成功即可，在切换备机的时候并不一定任何一个备机都是与主机数据一致的，所以需要查询GTID最大的那个备机。这种方式可以明显提高写操作的效率，但是会稍微降低一些切换的效率。

4、查询高低水位

在一致性检查时，需要从MDS查询高低水位信息，来判断是否有一致性副本存在，返回的有三个值，高水位（也称为正常水位），低于高水位，低于低水位，在低于低水位的情况就认定不存在一致性副本，切换失败，在高水位和低于高水位说明可能存在有一致性副本，不一定肯定存在，所以还需要结合多节点故障来判断，简单来说，在高水位的情况下，如果存在同城有DB故障，则不能切换，低于高水位就是如果存在本地或同城有DB故障，就不能切换。

5、切换策略

根据配置的策略切换，以及是否可切同城选择相应的切换策略，目前有三个策略：

1）高一致性

2）高可用性

3）补数据

6、高一致性

从名称可以看出，优先保证数据的一致性才能进行切换，在主机故障了，宁可不发生切换，也不能在可能丢失数据的情况下执行切换，这种高一致性的保证主要是通过查询高低水位和多节点故障来判断是否有一致性副本存在，如果存在一致性副本，再根据切换策略选择GTID最大的DB切换为主，在备机切主成功后，此时会向PM发送只读启服的操作，同时让原来连接在旧主上面的备机切换到新主上面。

7、高可用性

和高一致性区别就是，在高可用场景下，通过允许丢失数据来保证可以立即恢复到可用状态，处理逻辑也比较简单，就是选择一个GTID最大的DB为主就可以了。

8、补数据

这个和高一致性，高可用性的选择策略不同的就是，在查询所有备机的GTID时发现GTID值最大的在同城，这个就需要看该集群是否允许自动切换到同城，如果允许切换到同城，再继续判断是高一致性还是高可用性，如果不允许切换到同城，那就要走补数据策略，具体流程就是除了GTID值最大的DB，其他所有的备机，都先连接到该DB上同步数据，当其他备机都同步完成后，在从本地选择一台DB作为新主来进行切换。

9、DB读写

当选择一台备机作为新主，切换成功后，设定该新主的read_only为OFF，保证新主是可读写的权限。

10、PM启服

切换完成后，通知PM启服，同时告诉PM新主的IP和Port，让其在新主上启服。

 

### **主备切换**

主备切换存在两种：一种是故障切换，一种是OMM页面主动切换。

#### 异常切换流程

1、CM判断主DB异常

1）主DBAgent和CM断链（默认丢失3个心跳）

2）该DBGroup上所有备机与主机I/O线程断链

2、执行切换：

1）对该节点停服，个给该DBGroup所有DB设置为只读状态，等待所有备机回放完成（超时60s）

2）获取高低水位信息：

2.1）低于低水位时，DB不能故障切换

2.2）高低水位之间，存在FAM_S或FAM_D的DB则不能切换

2.3）高水位时，存在一个或多个FAM_D则不能切换

3）获取所有备机GTID，超过60s认为备机异常设置该备机异常

4）多节点故障场景判断

5）如果可以切换则继续，不满足一致性切换则切换失败

6）取GTID最大的DB为新主，解除只读修正备机的主备关系

 

DB切换为备机的主要操作命令：

1、清理现有的主备关系：

stop slave and reset slave all;

2、如果备机数据比主机多，需要执行以下操作：

回滚操作：

ddbbinlog --rollback-binlog-path=’/home/zxdb1/data/binlog’ 

--rollback-sql-path=’/home/zxdb1/SwitchRollbackSql/rollback_****’

--host=’’ --port=**** --user=’’ --password=’’

--rollback-gtid-set=’’

回滚结束后，执行purge：

reset master;

set @@global.gtid_purged=’’;

3、设置半同步标志

sed -i “s/^#*\<rpl_semi_sync_master_enable\>[]*=.*/rpl_semi_sync_master_enabled=OFF/g” /home/zxdb1/etc/my.cnf

sed -i “s/^#*\<rpl_semi_sync_slave_enable\>[]*=.*/rpl_semi_sync_slave_enabled=ON/g” /home/zxdb1/etc/my.cnf

4、建立主备关系

change master to MATSER_HOST=’’,

MASTER_PORT=3306,

MASTER_AUTO_POSITION=1,

MASTER_HEARTBEAT_PERIOD=2,

MASTER_CONNECT_RETRY=10;

5、启动主从复制

start slave;

6、切换成功，切换记录保存在$HOME/etc/dbagent_info/role_switch_pos.log

#### OMM页面主动切换流程

1、OMM界面下发主备切换命令

2、DBGroup禁用

3、等待新主回放完成比较新主和旧主的GTID

4、新主和旧主GTID相同则可以切换，不同则不允许切换

5、完成主备关系纠正

 

### **常见故障**

#### dbgroup只读

问题描述：dbgroup只读无法写

问题分析：

1、DB crash导致水位异常

2、主机有大事务，备机回放太慢（网络、磁盘、CPU等瓶颈），导致误认为备机挂掉，进而只读

#### 直连DB设置外键约束导致主备不一致

问题描述：通过脚本查询RDB中所有DB的IP和端口，然后脚本设置外键约束，执行完脚本后显示主备不一致

问题分析：在脚本中应该增加一个判断，即只能读取RDB中主机IP，不能再备机执行写操作。

#### 备份data后恢复失败

问题描述：备份主DB的整个data目录，恢复的时候将data目录直接拷贝到备机对应目录，然后重新启动，此时备机无法恢复

问题分析：恢复的时候需要删除备机的auto.cnf，里面存储的是serverid，这个会在备机重启的时候自动生成一个，建立主备关系，如果使用原来备机的serverid显然是不可以的。