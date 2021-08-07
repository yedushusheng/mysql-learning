# *概述*

## *技术演进*

### **TDDL**

DRDS前身为淘宝TDDL，是近千核心应用首选组件。

注：这里DETDDL、DRDS都不是云数据库，还是传统的MySQL中间件。

 

### **DRDS**

DRDS诞生于2008年淘宝“去IOE”时代。对于任何一个产品而言，它的出现以及能力的变化都与其面向的业务相关。对于DRDS而言，可以粗略地把业务场景分为3类。

第一类是DRDS从一开始出现就在解决的面向最终消费者的高并发业务数据库的需求，这也是DRDS能够很好解决的场景。

第二种场景就是DRDS上云提供服务之后遇到的企业级数据库需求，它希望数据库具有综合负载能力、可持续运维和7*24小时稳定性以及并发、计算和存储的扩展性。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB82E.tmp.jpg) 

如今，解决上述某几个问题的数据库大致可分为三类：

DRDS以及Sharding On MySQL数据库，主要***\*基于MySQL和分布式计算能力，使得计算存储高度可扩展，风险可控\****。

NewSQL数据库，核心特点就是***\*存储与计算分离\****。

Cloud Native DB，强调***\*存储可扩展以及全兼容的能力\****。

而通过并发控制、分布式、容灾以及计算这四个维度能够更加深入剖析数据库能力。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB83F.tmp.jpg) 

DRDS ON MySQL实现得很好，但在存储方面则让人“既爱又恨”。因此，在POLARDB上线后的第一时间，阿里云就实现了DRDS On POLARDB。DRDS和POLARDB两者的布局不同，***\*POLARDB层面侧重一写多读的能力，DRDS层面则侧重事务扩展性\****。DRDS On POLARDB解决了数据倾斜、主备数据以及RDS数据能力的问题，因此相对比较稳定并且具有面向未来的一些特性。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB840.tmp.jpg) 

DRDS标准数据库内核的发展经历了从超高并发用户侧服务逐步转向了企业级应用场景的转变。发生这样转变的驱动力也有三个，即业务场景、经典数据库理论以及Benchmark。DRDS标准数据库内核非常注重分布式的能力，比如OLTP极致算子Pushdown能力、分区键精确裁剪、多种拆分方式、统一架构的2PC和XA分布式事务、全局强一致二级索引、MPP执行引擎技术、OLTP查询加速等。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB841.tmp.jpg) 

### **AliSQL**

AliSQL是阿里云深度定制的独立MySQL分支，除了社区版的所有功能外，AliSQL提供了类似于MySQL企业版的诸多功能，如企业级备份恢复、线程池、并行查询等，并且AliSQL还提供兼容Oracle的能力，如sequence引擎等。

 

### **RDS**

阿里云关系型数据库RDS（Relational Database Service）基于阿里云分布式文件系统和SSD盘高性能存储，RDS支持MySQL、SQL Server、PostgreSQL、PPAS（高度兼容 Oracle）和MariaDB引擎，并且提供了容灾、备份、恢复、监控、迁移等方面的全套解决方案，彻底解决数据库运维的烦恼。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB852.tmp.jpg) 

 

#### **RDS MySQL**

RDS MySQL使用AliSQL内核（阿里巴巴的MySQL源码分支），仅支持InnoDB和[X-Engine](#concept-2377809)两种存储引擎。

 

 

### **X-Engine**

X-Engine是阿里云数据库产品事业部自研的联机事务处理OLTP（On-Line Transaction Processing）数据库存储引擎。作为自研数据库PolarDB的存储引擎之一，已经广泛应用在阿里集团内部诸多业务系统中，包括交易历史库、钉钉历史库等核心应用，大幅缩减了业务成本，同时也作为双十一大促的关键数据库技术，挺过了数百倍平时流量的冲击。

X-Engine的诞生是为了应对阿里内部业务的挑战，早在2010年，阿里内部就大规模部署了MySQL数据库，但是业务量的逐年爆炸式增长，数据库面临着极大的挑战：

1、极高的并发事务处理能力（尤其是双十一的流量突发式暴增）。

2、超大规模的数据存储。

这两个问题虽然可以通过扩展数据库节点的分布式方案解决，但是堆机器不是一个高效的手段，我们更想用技术的手段将数据库性价比提升到极致，实现以少量资源换取性能大幅提高的目的。

传统数据库架构的性能已经被仔细的研究过，数据库领域的泰斗，图灵奖得主Michael Stonebreaker就此写过一篇论文 《OLTP Through the Looking Glass, and What We Found There》 ，指出传统关系型数据库，仅有不到10%的时间是在做真正有效的数据处理工作，剩下的时间都浪费在其它工作上，例如加锁等待、缓冲管理、日志同步等。

造成这种现象的原因是因为近年来我们所依赖的硬件体系发生了巨大的变化，例如多核（众核）CPU、新的处理器架构（Cache/NUMA）、各种异构计算设备（GPU/FPGA）等，而架构在这些硬件之上的数据库软件却没有太大的改变，例如使用B-Tree索引的固定大小的数据页（Page）、使用ARIES算法的事务处理与数据恢复机制、基于独立锁管理器的并发控制等，这些都是为了慢速磁盘而设计，很难发挥出现有硬件体系应有的性能。

基于以上原因，阿里开发了适合当前硬件体系的存储引擎，即X-Engine。

 

之前AliSQL使用InnoDB引擎，而InnoDB存在扩展性瓶颈。***\*X-Engine引擎则采用了LSM-Tree架构\****，并进行了创新。***\*在架构最上层提供了高度并发的事务处理流水线，中间实现了无锁内存表Memtable\****。此外，为了解决读写冲突，X-Engine将每个Meta信息作为一个独立版本。***\*X-Engine对于磁盘存储层也进行了整体重构，并且还引入了FPGA作为硬件加速器\****。

***\*技术特点：\****

利用FPGA硬件加速Compaction过程，使得系统上限进一步提升。这个技术属首次将硬件加速技术应用到在线事务处理数据库存储引擎中。

通过数据复用技术减少数据合并代价，同时减少缓存淘汰带来的性能抖动。

使用多事务处理队列和流水线处理技术，减少线程上下文切换代价，并计算每个阶段任务量配比，使整个流水线充分流转，极大提升事务处理性能。相对于其他类似架构的存储引擎（例如RocksDB），X-Engine的事务处理性能有10倍以上提升。

X-Engine使用的Copy-on-write技术，避免原地更新数据页，从而对只读数据页面进行编码压缩，相对于传统存储引擎（例如InnoDB），使用X-Engine可以将存储空间降低至10%~50%。

Bloom Filter快速判定数据是否存在，Surf Filter判断范围数据是否存在，Row Cache缓存热点行，加速读取性能。

 

### **X-DB**

X-DB这个系统是一个mySQL三集群备份的系统架在了盘古的文件系统上。核心的能力之一是它们的X-Engine，一个基于LSM Tree的，多层冷热分离的的文件系统。另外上了FPGA去做compaction。

这个系统的架构既不像那种shared-disk的做法，引擎下面顶个大网盘，又不是经典意义上的shared-nothing，也就是MPP架构。而LSM Tree用到这个地方，显然是为了更低延时的写操作，而非读操作，所以这又是另外一个有意思的地方。

这里最有意思的一点是用了盘古文件系统作为底层的存储。因为这样一来mySQL三备份加底层盘古系统三备份，一条记录写下去，理论上来说被存了9份。这实在是有点消耗过大。后面X-DB肯定做了优化。

三局鼎立的形势并没有持续太久，很快就传出了阿里云数据库团队和阿里巴巴集团数据库团队要合并的消息。比如说在中国数据库协会的某次会议上就出现了PolarDB-X Powered by X-DB这样的标题。这是首次见到了PolarDB-X的名字。

 

16年，阿里内部开始了X-DB的研发。基于MySQL内核，类Aurora的存储计算分离架构。***\*聚焦于新型存储硬件NVM上的数据库架构的研究\****。

X-DB要克服副本数据一致性的问题，X-DB的团队选择自研Paxos协议来替代MySQL内置的主备同步逻辑。

 

## PolarDB

PolarDB（这里指PolarDB For MySQL）是一个基于***\*共享存储技术\****的***\*云原生数据库\****（与PolarDB-X不同，它是云原生数据库，但是不是分布式）。PolarDB在存储空间上可以做到很强的弹性能力，但***\*一般使用情况下，其计算能力、写入能力依然存在单机的上限\****。

PolarDB-X 2.0这种share-nothing的架构，使得包括计算、写入、读取、存储等在内的所有资源，都具备了可水平扩展的能力，因此不会存在单机的瓶颈上限。

但是，share-nothing的架构在单纯的数据容量的弹性上，是不如PolarDB的共享存储架构的。

 

## PolarDB-X

2003 年淘宝网成立之初采用的是经典的LAMP架构，随着用户量迅速增长，单机MySQL数据库很快便无法满足数据存储需求，之后淘宝网进行了架构升级，数据库改用Oracle。随着用户量的继续快速增长，Oracle数据库也开始成批成批的增加，即使这样，仍然没有满足业务对数据库扩展性的诉求，所以阿里巴巴内部在2009年时发起了著名的去IOE运动，PolarDB-X也开启了自己的演进之路。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB853.tmp.jpg) 

### **PolarDB-X 0.5时代**

去IOE的关键一环是实现对Oracle的替换，当时淘宝的业务体量已很难用成熟的技术产品支撑，为了避免以后出现卡脖子情况，技术的自力更生和自主可控成为一个核心诉求。一方面，随着x86技术日趋成熟，稳定性与小型机的差距不断缩小，另一方面，MySQL采用轻量化线程模型并具备高并发的支持能力，其生态逐步完善，因此新方案采用了基于Sharding技术+开源MySQL的分布式架构（TDDL + AliSQL），我们称为PolarDB-X 0.5版本时代。这代产品的特征是以解决扩展性为目标、面向系统架构使用，尚不具备产品化能力。

 

### **PolarDB-X 1.0时代**

随着这套架构的逐渐成熟，14年开始，我们基于阿里云走上了云数据库发展之路。作为分库分表技术的开创者，我们推出了DRDS + RDS的分布式云数据库服务，我们称为PolarDB-X 1.0时代。这代产品的特征是采用Share-Nothing架构、以解决存储扩展性为出发点、提供面向用户的产品化交付能力。1.0版本是国内第一家落地分布式技术的云服务，成为云市场上分布式数据库技术方向的开创者和引领者。

针对用户使用中的痛点，我们不断进行产品能力迭代，陆续支持了分布式事务、全局二级索引、异步DDL等内核特性，持续改进SQL兼容性，实现子查询展开、Join下推等复杂优化，并开发了平滑扩容、一致性备份恢复、SQL闪回、SQL审计等运维能力。这段时间我们不断扩展所谓分库分表中间件技术的能力边界，试图找到它的能力上限。这个探索的过程，一方面使我们的计算层能力更加稳定、丰富和标准化，另一方面也促使着DRDS从中间件到分布式数据库的蜕变。

 

### **PolarDB-X 2.0时代**

18年开始，我们逐渐触碰到了计算层的能力边界，比如无法提供RR隔离级别的事务能力、计算下推受限于SQL表达能力、数据查询的传输效率底下、多副本的线性一致性不可控等，这些问题像一个无法穿透的屏障，我们能看到屏障的对面是什么，能看到所有障碍都指向了同一个方向：计算层需要与存储层深度融合。

值得高兴的是，我们的AliSQL分支从诞生起就没有停止前进的步伐，通过集团业务多年的技术锤炼，基于AliSQL演化而来的X-DB数据库（包括X-Paxos 协议库、X-Engine存储引擎等），在全球三副本、低成本存储等技术有了非常好的沉淀。

与此同时，基于云原生架构理念的PolarDB，通过引入RDMA网络优化存储计算分离架构，实现一写多读的能力，并提供资源池化降低用户成本，优化并提供秒级备份恢复、秒级弹性等能力，成为公有云增速最快的数据库产品。

这些技术探索和沉淀，使PolarDB-X团队有底气开始思考基于云架构的分布式数据库应该是什么样的形态，从宏观角度来看，会有云原生、国产化、分布式、HTAP等诉求，从用户角度来看，需要满足用户使用云的一些期望，比如用户的数据库数据永远不会丢，即使主机异常宕机，这里需要有数据强一致以及高可用容灾等能力。再比如随着移动互联网和IoT的普及，数据层面会有爆炸式的增长，以及今年疫情之后有更多的企业会关注IT成本，因此高性能、低成本和可扩展的计算和存储能力也成为普适性诉求，另外类似Snowflake的按查询付费的弹性能力，也是市场的另一个诉求。因此，简单总结一下，下一代的分布式数据库需要具备：金融级高可用和容灾、水平扩展、低成本存储、按需弹性、透明分布式、HTAP混合负载、融合新硬件等。

所以，2019年PolarDB-X团队完成***\*DRDS SQL引擎和X-DB数据库存储技术的融合\****，并***\*结合PolarDB的云原生特性\****，承上启下推出了新一代的***\*云原生分布式数据库\****。***\*专注解决单机解决不好的分布式扩展性问题\****，满足分布式数据一致性要求，并支持从单机到分布式的平滑演进，利用云原生技术的优势提供低成本和弹性能力，在交付上具备线上公有云、线下专有云、轻量化等全形态输出。

 

## 应用场景

从目前市场上金融领域应用来看，云原生分布式数据库PolarDB-X还是不适合，一般是使用PolarDB。

### **按应用类型选择**

PolarDB-X产品在高并发、分布式事务、复杂SQL优化、并行计算等方面都有比较好的用户沉淀和技术发展，适用于如下场景：

对超高并发和大规模数据存储有较高要求的互联网在线事务类业务场景。

传统企业级应用因业务发展导致计算量与数据量呈爆发式增长，急需具备更强计算能力的在线事务型数据库场景。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB854.tmp.jpg) 

 

### **按容量选择**

在OLTP业务领域，数据库的容量通常关注并发度、数据存储、复杂SQL响应时间这3个维度。若当前数据库中任意一个维度出现瓶颈，或出于对业务不断高速发展提前规划数据库选型的考虑，当下选用PolarDB-X构建分布式数据库，可有效降低后期数据库的扩展及运维压力。

在业务发展初期，选择单机数据库还是分布式数据库，需要考量很多因素。但从数据库自身角度出发，业务使用的SQL语句、数据类型、事务、索引、其他功能均是确定的。对于大部分业务而言，只要SQL语法、数据类型、事务、索引支持较为完整，且具备有效手段可在各种极端场景下进行水平扩展，那么对于高速发展的业务而言，PolarDB-X即是所有分布式数据库中最具生命力及延续性的方案。

 

### **按成本选择**

对于数据库选型的成本考量，主要包括如下2个部分：

业务开发上手难度过高，往往会导致项目延期，业务效果不尽人意。对于一个新型数据库而言，如何有效兼容现有流行数据库的使用习惯和功能支持的完整度至关重要。PolarDB-X兼容MySQL生态，对于主流的客户端、驱动有着良好的兼容性，SQL语法兼容完善，业务可快速进行对接适配。

数据库长期持久的稳定性及优异的性能表现对于业务而言至关重要，因PolarDB-X将数据、负载分担至多个MySQL实例中，所以面对逐步增大的负载压力，PolarDB-X相比大规格单机数据库具备更强的稳定性。性能表现层面，因为天然支持分布式，抵御业务的超高并发是其强项，配合单机并行计算、多机DAG计算，PolarDB-X能够覆盖绝大多数在线业务的复杂计算需求。

 

### **按应用生命周期发展选择**

PolarDB-X各个拆分模式可无缝平滑打通，全方位覆盖、满足业务各个生命周期中对于数据库的扩展性诉求。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB855.tmp.jpg) 

# 架构

参考：

https://zhuanlan.zhihu.com/p/88899448

 

PolarDB-X承担着OLTP在线核心数据库的职责与定位，可与数据集成、数据传输，缓存、大数据生态配合使用。

DRDS/TDDL是典型的***\*水平扩展\****分布式数据库模型，区别于传统单机数据库share anything架构，DRDS/TDDL采用***\*share nothing\****架构，share nothing架构核心思路利用普通的服务器，***\*将单机数据拆分到底层的多个数据库实例上，通过统一的Proxy集群进行SQL解析优化、路由和结果聚合，对外暴露简单唯一的数据库链接\****。整体架构如图所示，包含DRDS服务模块、DRDS管控模块、配置中心、监控运维、数据库服务集群、域名服务模块。

## 产品架构图

PolarDB-X承担着OLTP在线核心数据库的职责与定位，可与数据集成、数据传输，缓存、大数据生态配合使用。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB865.tmp.jpg) 

## 内核架构

PolarDB-X提供1.0中间件形态，以及2.0一体化形态。

PolarDB-X 1.0中间件形态由计算层DRDS实例与存储层私有定制RDS实例组成，通过挂载多个MySQL进行分库分表水平拆分。

PolarDB-X 2.0一体化形态由多个节点构成，实例内部署多个节点进行水平扩展，每个节点闭环整合计算资源与存储资源，运维管理更加便利。

如同大多数传统单机关系型数据库，PolarDB-X分为网络层、协议层、SQL解析层、优化层和执行层，其中优化层包含逻辑优化和物理优化，执行层包含单机两阶段执行、单机并行执行和多机并行执行，应用了多种传统单机数据库优化和执行技术。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB866.tmp.jpg) 

## 部署架构

PolarDB-X服务部署在公有云上，采取了如下多种方式确保生产安全：

支持VPC、IP白名单、非对称账号密码、TDE等方式，确保数据服务安全。

使用独享高性能物理资源、实例间充分隔离、支持多可用区实例，确保数据服务稳定。

支撑运维系统采用多地域隔离部署、核心数据服务SLA与运维管控SLA解绑，确保运维体系稳定。

 

# 原理

## 实例

 

## 全局二级索引

### **功能介绍**

全局二级索引（Global Secondary Index，GSI）支持按需增加拆分维度，提供全局唯一约束。每个GSI对应一张索引表，使用XA多写保证主表和索引表之间数据强一致。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB867.tmp.jpg) 

全局二级索引支持如下功能：

增加拆分维度。

支持全局唯一索引。

XA多写，保证主表与索引表数据强一致。

支持覆盖列，减少回表操作，避免额外开销。

Online Schema Change，添加GSI不锁主表。

支持通过HINT指定索引，自动判断是否需要回表。

 

### **常见问题**

Q：全局二级索引能够解决什么问题？

A：如果查询的维度与逻辑表的拆分维度不同，会产生跨分片查询。跨分片查询的增加会导致查询卡慢、连接池耗尽等性能问题。GSI能够通过增加拆分维度来减少跨分片查询，消除性能瓶颈。

 

说明 创建GSI时需要注意选择与主表不同的分库分表键，详情请参见[使用全局二级索引](#task-1946506)。

 

Q：全局二级索引和局部索引有什么关系？

A：全局二级索引和局部索间的关系如下所示：

 

全局二级索引：不同于局部索引，如果数据行和对应的索引行保存在不同分片上，称这种索引为全局二级索引，主要用于快速确定查询涉及的数据分片。

局部索引：分布式数据库中，如果数据行和对应的索引行保存在相同分片上，称这种索引为局部索引。PolarDB-X中特指物理表上的MySQL二级索引

两者的关系：两者需要搭配使用，PolarDB-X通过GSI将查询下发到单个分片后，该分片上的局部索引能够提升分片内的查询性能。

 

## 全局binlog

基于事务日志提供数据消费或订阅能力，是很多数据库的必备特性，PolarDB-X 2.0也不例外。PolarDB-X 2.0提供了全局Binlog文件来支持消费或订阅数据。

### **工作原理**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB878.tmp.jpg) 

PolarDB-X 2.0的全局Binlog（又称逻辑Binlog），是以TSO（Timestamp Oracle）为基准，将多个数据节点（Database Node，简称DN）的物理Binlog进行归并和合并，能够保证分布式事务完整性和全局有序性的Binlog文件。其中：

合并模块会将分散到各个DN的分布式事务Binlog Event，重新合并到一个完整的事务中，并以单机事务的形式输出该事务。

归并模块会以TSO为排序依据，将所有合并后的事务进行全局排序，即把各个DN的偏序集合归并为一个全序集合。

 

### **特性**

兼容MySQL Binlog文件格式和Dump协议。

PolarDB-X 2.0的全局Binlog是在DN节点的物理Binlog基础之上生成的，剔除了分布式事务的细节，只保留了单机事务的特性。同时，全局Binlog兼容MySQL Binlog文件格式，在数据订阅方式上也完全兼容MySQL Dump协议，您可以像使用单机MySQL一样来订阅PolarDB-X 2.0的事务日志。

 

保证分布式事务的完整性和有序性。

全局Binlog不是将物理Binlog简单地汇总到一起，而是通过合并模块和归并模块保证了分布式事务的完整性和有序性，从而实现高数据一致性。

 

例如，在转账场景下，基于全局Binlog能力，接入PolarDB-X 2.0的下游MySQL，可以在任何时刻查询到一致的余额。

 

提供7x24小时服务能力，运维简单。

全局Binlog剔除了PolarDB-X 2.0的内部细节（此时您可以将PolarDB-X 2.0看作一个单机MySQL）， 来避免实例内部发生的变化对数据订阅链路的影响。PolarDB-X 2.0通过一系列的协议和算法来保证全局Binlog的服务能力，确保实例内部发生的各种变更（如HA切换、增删节点、执行Scale Out或分布式DDL等操作）不会影响数据订阅链路的正常工作。

 

 

# SQL使用

## 限制

PolarDB-X高度兼容MySQL协议和语法，但由于分布式数据库和单机数据库存在较大的架构差异，存在SQL使用限制。

### **SQL大类限制**

暂不支持自定义数据类型或自定义函数。

暂不支持存储过程、触发器、游标。

暂不支持临时表。

暂不支持BEGIN…END、LOOP…END LOOP、REPEAT…UNTIL…END REPEAT、WHILE…DO…END WHILE等复合语句。

暂不支流程控制类语句（如IF或WHILE等）。

### **小语法限制**

#### **DDL**

CREATE TABLE tbl_name LIKE old_tbl_name不支持拆分表。

CREATE TABLE tbl_name SELECT statement不支持拆分表。

暂不支持同时RENAME多表。

暂不支持ALTER TABLE修改拆分字段。

暂不支持跨Schema的DDL（例如CREATE TABLE db_name.tbl_name (... )）。

#### **DML**

暂不支持SELECT INTO OUTFILE、INTO DUMPFILE和INTO var_name。

暂不支持STRAIGHT_JOIN和NATURAL JOIN。

暂不支持在 UPDATE SET 子句中使用子查询。

暂不支持INSERT DELAYED语法。

暂不支持SQL中对于变量的引用和操作（例如SET @c=1, @d=@c+1; SELECT @c, @d）。

暂不支持在柔性事务中对广播表进行INSERT、REPLACE、UPDATE或DELETE操作。

#### **子查询**

不支持HAVING子句中的子查询，JOIN ON条件中的子查询。

等号操作行符的标量子查询（The Subquery as Scalar Operand）不支持ROW语法。

#### **数据库管理**

SHOW WARNINGS语法不支持LIMIT和COUNT的组合。

SHOW ERRORS语法不支持LIMIT和COUNT的组合。

#### **运算符**

暂不支持‘:=’赋值运算符。

#### **函数**

暂不支持[全文检索函数](https://dev.mysql.com/doc/refman/5.7/en/fulltext-search.html)。

暂不支持[XML 函数](https://dev.mysql.com/doc/refman/5.7/en/xml-functions.html)。

不支持[GTID 函数](https://dev.mysql.com/doc/refman/5.7/en/gtid-functions.html)。

不支持[企业加密函数](https://dev.mysql.com/doc/refman/5.7/en/enterprise-encryption.html)。

#### **关键字**

暂不支持MILLISECOND。

 

## 分库分表

PolarDB-X是一个支持既分库又分表的数据库服务。

### **拆分函数**

在PolarDB-X中，一张逻辑表的拆分方式由拆分函数（包括分片数目与路由算法）与拆分键（包括拆分键的MySQL数据类型）共同定义。只有当PolarDB-X使用了相同的拆分函数和拆分键时，才会被认为分库与分表使用了相同的拆分方式。相同的拆分方式让PolarDB-X可以根据拆分键的值定位到唯一的物理分库和物理分表。当一张逻辑表的分库拆分方式与分表拆分方式不一致时，若SQL查询没有同时带上分库条件与分表条件，则PolarDB-X在查询过程会进行全分库扫描或全分表扫描操作。

***\*拆分函数对分库、分表的支持情况：\****

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB879.tmp.jpg) 

***\*拆分函数对全局二级索引的支持情况：\****

PolarDB-X支持[全局二级索引](#concept-1946505)，从数据存储的角度看，每个GSI对应一张用于保存索引数据的逻辑表，称为索引表。

PolarDB-X还支持创建GSI时指定索引表的拆分方式，并且对拆分函数的支持范围与普通逻辑表相同，创建GSI的详细语法。

 

### **拆分方式(2.0)**

PolarDB-X 2.0新增按主键类型自动拆分表功能，简易地将分布式技术引入到普通DDL语法，您只需要执行简单的修改，系统将根据主键和索引键自动选择拆分键和拆分方式，完成从单机数据库到分布式数据库的切换。

#### **主键拆分**

##### *前提条件*

PolarDB-X 2.0内核小版本需为5.4.9或以上。

##### 使用限制

主键拆分表仅支持在建表时指定主键，不支持对已有的表添加或删除主键。

主键拆分表的非LOCAL索引必须指定索引名。

##### *语法*

在CREATE TABLE语法中新增了PARTITION关键字，同时，在创建索引的子句中新增了LOCAL、GLOBAL和CLUSTERED关键字，以适应主键拆分表。

CREATE PARTITION TABLE [IF NOT EXISTS] tbl_name

​        (create_definition, ...)

​        [table_options]

​        [drds_partition_options]

​        create_definition:

​        col_name column_definition

​        | mysql_create_definition

​        | [UNIQUE] [LOCAL | GLOBAL | CLUSTERED] INDEX index_name [index_type] (index_col_name,...)

​        [global_secondary_index_option]

​        [index_option] ...

LOCAL：强制指定为本地索引。

GLOBAL：[全局二级索引](#task-1946506)。

CLUSTERED：[聚簇索引](#concept-2037106)。

如果不想修改DDL，想使用普通单库单表的DDL直接创建主键拆分表，您可以通过在SQL命令行中设置用户变量的方式开启主键拆分，方法如下：

执行set @auto_partition=1;命令，开启自动主键拆分。

执行CREATE TABLE语句创建表（无需附加PARTITION关键字），该动作将被视为创建主键拆分表。

执行set @auto_partition=0;命令，关闭自动主键拆分。

##### *自动拆分规则*

如果目标表没有指定主键，PolarDB-X 2.0会启用隐式主键并将其作为拆分键，该主键为BIGINT类型的自增主键，且对用户不可见。

如果目标表指定了主键，PolarDB-X 2.0会使用该主键作为拆分键。如果为复合主键，则选择复合主键的第一列作为拆分键。

自动拆分仅拆分数据库，不拆分数据表，且拆分算法根据主键类型自动选择：

| 主键类型                                                     | 拆分算法   |
| ------------------------------------------------------------ | ---------- |
| TINYINT、SMALLINT、MEDIUMINT、INT、BIGINT、CHAR、VARCHAR     | HASH       |
| DATE、DATETIME、TIMESTAMP                                    | YYYYDD     |
| BIT、FLOAT、DOUBLE、TIME、YEAR、BLOB、ENUM、DECIMAL、BINARY、TEXT、SET、GEOMETRY | 不支持拆分 |

 

##### *索引转换规则*

如果指定了LOCAL关键字，即强制指定索引为本地索引。

对主键拆分表执行创建索引操作时，如果未指定LOCAL关键字，该操作将被自动地转变为创建无覆盖列（covering）的全局二级索引，并且按索引列的第一列进行自动拆分。如果需要建立普通的局部索引，您需要指定LOCAL关键字。

创建全局二级索引和聚簇索引时，会创建一个带_local_前缀的本地索引。如果删除全局二级索引，PolarDB-X 2.0会自动同步删除对应的本地索引。

主键拆分表可以不指定全局二级索引、聚簇索引的拆分方式，PolarDB-X 2.0会根据自动拆分原则对索引键的第一列执行拆分。

下述语句及其注释为您展示了索引的转换规则。

CREATE PARTITION TABLE `t_order` (

 `x` int,

 `order_id` varchar(20) DEFAULT NULL,

 `seller_id` varchar(20) DEFAULT NULL,

 LOCAL INDEX `l_seller` using btree (`seller_id`), -- 强制指定为本地索引

 UNIQUE LOCAL INDEX `l_order` using btree (`order_id`), -- 强制指定为本地唯一索引

 INDEX `i_seller` using btree (`seller_id`), -- 会被替换为GSI，自动拆分

 UNIQUE INDEX `i_order` using btree (`order_id`), -- 会被替换为UGSI，自动拆分

 GLOBAL INDEX `g_seller` using btree (`seller_id`), -- 自动拆分

 UNIQUE GLOBAL INDEX `g_order` using btree (`order_id`), -- 自动拆分

 CLUSTERED INDEX `c_seller` using btree (`seller_id`), -- 自动拆分聚簇

 UNIQUE CLUSTERED INDEX `c_order` using btree (`order_id`) -- 自动拆分聚簇

);

执行show create table t_order;命令，查看表结构信息。

+---------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

| Table  | Create Table                                                                               |

+---------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

| t_order | CREATE PARTITION TABLE `t_order` (

 `x` int(11) DEFAULT NULL,

 `order_id` varchar(20) DEFAULT NULL,

 `seller_id` varchar(20) DEFAULT NULL,

 UNIQUE LOCAL KEY `l_order` USING BTREE (`order_id`),

 UNIQUE LOCAL KEY `_local_i_order` USING BTREE (`order_id`),

 UNIQUE LOCAL KEY `_local_g_order` USING BTREE (`order_id`),

 UNIQUE LOCAL KEY `_local_c_order` USING BTREE (`order_id`),

 LOCAL KEY `l_seller` USING BTREE (`seller_id`),

 LOCAL KEY `_local_i_seller` USING BTREE (`seller_id`),

 LOCAL KEY `_local_g_seller` USING BTREE (`seller_id`),

 LOCAL KEY `_local_c_seller` USING BTREE (`seller_id`),

 UNIQUE CLUSTERED KEY `c_order` USING BTREE (`order_id`) DBPARTITION BY HASH(`order_id`),

 CLUSTERED INDEX `c_seller` USING BTREE(`seller_id`) DBPARTITION BY HASH(`seller_id`),

 UNIQUE GLOBAL KEY `g_order` USING BTREE (`order_id`) DBPARTITION BY HASH(`order_id`),

 GLOBAL INDEX `g_seller` USING BTREE(`seller_id`) DBPARTITION BY HASH(`seller_id`),

 UNIQUE GLOBAL KEY `i_order` USING BTREE (`order_id`) DBPARTITION BY HASH(`order_id`),

 GLOBAL INDEX `i_seller` USING BTREE(`seller_id`) DBPARTITION BY HASH(`seller_id`)

) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4  |

+---------+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------+1 row in set (0.06 sec)

 

#### **聚簇索引**

PolarDB-X 2.0新增支持聚簇索引功能，用于自动维护全局二级索引（GSI）中的覆盖列，保证聚簇索引表和主表的实时同步，所有查询均不用回表，避免因回表带来的额外开销。本文介绍如何创建并使用聚簇索引。

##### 前提条件

PolarDB-X 2.0内核小版本需为5.4.9或以上。

##### *注意事项*

聚簇索引是一种特殊的全局二级索引，相关行为和限制请参考[全局二级索引](https://help.aliyun.com/document_detail/182180.html)。

聚簇索引的覆盖列默认包含主表的所有列，并在主表的列发生变更时，自动同步修改聚簇索引表，保证聚簇索引表和主表的实时同步。

聚簇索引表也会和主表的本地索引保持同步。

##### 语法

您可以在建表或加索引的语句中，通过CLUSTERED关键字指定创建的索引为聚簇索引。

CREATE TABLE：

CREATE [SHADOW] TABLE [IF NOT EXISTS] tbl_name

  (create_definition, ...)

  [table_options]

  [drds_partition_options]

create_definition:

  [UNIQUE] CLUSTERED INDEX index_name [index_type] (index_col_name,...)

   [drds_partition_options] 

   [index_option] ...

说明：仅在主键拆分表中可省略拆分规则即[drds_partition_options]部分。

CREATE INDEX：

CREATE [UNIQUE]

  CLUSTERED INDEX index_name [index_type]

  ON tbl_name (index_col_name,...)

  [drds_partition_options]

  [index_option] ...

说明：仅在主键拆分表中可省略拆分规则即[drds_partition_options]部分。

ALTER TABLE：

ALTER TABLE tbl_name

  alter_specification

其中alter_specification支持如下规则：

alter_specification:

 | ADD [UNIQUE] CLUSTERED {INDEX|KEY} index_name 

   [index_type] (index_col_name,...)

   [drds_partition_options] 

   [index_option] ...

说明：

聚簇索引相关变更（即alter_specification部分）仅支持使用一条变更规则。

聚簇索引必须显式指定索引名。

仅在主键拆分表中可省略拆分规则（即[drds_partition_options]部分）。

 

#### **变更表类型及拆分规则**

PolarDB-X 2.0新增支持变更表的类型（即在单表、拆分表和广播表三者间进行相互转换），和变更拆分表的拆分规则（包括拆分函数或拆分列）。

##### *前提条件*

仅内核小版本为5.4.8或以上的PolarDB-X 2.0实例支持变更拆分表的拆分规则。

仅内核小版本为5.4.10或以上的PolarDB-X 2.0实例支持变更表的类型（即在单表、拆分表和广播表三者间进行相互转换）。

 

##### 注意事项

暂不支持变更带有GSI的拆分表的拆分规则。

表属性变更后，主键拆分表将变成普通表（即不再适用原主键拆分表中的自动拆分规则或索引转换规则）。

若单表设置了自增列，在变更为广播表或拆分表时，需提前为该表创建Sequence。

本文中关于变更拆分表、广播表和单表的表类型示例，均在单表t_order的基础上进行变更，t_order表的创建语句如下：

CREATE TABLE t_order (

 `id` bigint(11) NOT NULL AUTO_INCREMENT BY GROUP,

 `order_id` varchar(20) DEFAULT NULL,

 `buyer_id` varchar(20) DEFAULT NULL,

 `seller_id` varchar(20) DEFAULT NULL,

 `order_snapshot` longtext DEFAULT NULL,

 `order_detail` longtext DEFAULT NULL,

 PRIMARY KEY (`id`),

 KEY `l_i_order` (`order_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

 

##### 表类型

PolarDB-X 2.0实例支持3种类型的表：拆分表、广播表和单表。您可以通过ALTER TABLE语句在拆分表、广播表和单表间进行转换，同时还能对拆分表的拆分规则进行变更。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB87A.tmp.jpg) 

###### **拆分表**

使用drds_partition_options子句进行拆分的表。

drds_partition_options可以是如下分库或分表子句：

drds_partition_options:

  	DBPARTITION BY db_partition_algorithm

  	[TBPARTITION BY table_partition_algorithm [TBPARTITIONS number]]

其中：

db_partition_algorithm支持如下函数：

db_partition_algorithm:

  	HASH([col_name])

 	| 	{YYYYMM|YYYYWEEK|YYYYDD|YYYYMM_OPT|YYYYWEEK_OPT|YYYYDD_OPT}(col_name)

 | UNI_HASH(col_name)

 | RIGHT_SHIFT(col_name, n)

 | RANGE_HASH(col_name, col_name, n)

 

table_partition_algorithm支持如下函数：

table_partition_algorithm:

  HASH(col_name)

 | 	{MM|DD|WEEK|MMDD|YYYYMM|YYYYWEEK|YYYYDD|YYYYMM_OPT|YYYYWEEK_OPT|YYYYDD_OPT}(col_name)

 | UNI_HASH(col_name)

 | RIGHT_SHIFT(col_name, n)

 | RANGE_HASH(col_name, col_name, n)

 

###### **广播表**

通过BROADCAST子句创建的表，系统会将该表复制到每个分库上，并通过分布式事务实现数据一致性。

 

###### **单表**

未进行任何拆分或未指定BROADCAST子句的表。更多详情，请参见[单库单表](#section-u13-pzp-37v)。

 

##### 单表或广播表变为拆分表

语法

ALTER TABLE table_name drds_partition_options;

若单表设置了自增列，在变更为拆分表时，需提前为该表创建Sequence。

示例

因业务扩展，单表t_order无法承载日益增长的数据。此时，您可以使用如下语句将该单表变更为拆分表（以order_id为拆分键）：

ALTER TABLE t_order dbpartition BY hash(`order_id`);

 

##### 单表或拆分表变为广播表

语法

ALTER TABLE table_name BROADCAST;

说明 若单表设置了自增列，在变更为广播表时，需提前为该表创建Sequence。

示例

您可以使用如下语句将单表或拆分表t_order变更为广播表：

ALTER TABLE t_order BROADCAST;

 

##### 广播表或拆分表变为单表

语法

ALTER TABLE table_name SINGLE;

示例

您可以使用如下语句将广播表或拆分表t_order变更为单表：

ALTER TABLE t_order SINGLE;

##### 变更拆分表的拆分规则

语法

ALTER TABLE tbl_name drds_partition_options;

示例

假设已使用如下语句在PolarDB-X 2.0数据库中创建了一张拆分表t_order（根据order_id列进行库级拆分）：

CREATE TABLE t_order (

 `id` bigint(11) NOT NULL AUTO_INCREMENT,

 `order_id` varchar(20) DEFAULT NULL,

 `buyer_id` varchar(20) DEFAULT NULL,

 `seller_id` varchar(20) DEFAULT NULL,

 `order_snapshot` longtext DEFAULT NULL,

 `order_detail` longtext DEFAULT NULL,

 PRIMARY KEY (`id`),

 KEY `l_i_order` (`order_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8 

dbpartition BY hash(`order_id`);

现需要对t_order表的拆分规则作出如下变更：

根据order_id列进行库级拆分。

buyer_id列做表级拆分。

每个分库包含3个分表。

您可以使用如下语句实现上述变更：

ALTER TABLE t_order dbpartition BY hash(order_id) tbpartition BY hash(buyer_id) tbpartitions 3;

 

### **使用说明**

## DDL

## DML

## DAL

### **SHOW**

### **KILL**

### **USE**

## Sequence

## Outline

## Hint

### **读写分离**

### **自定义SQL超时时间**

### **指定分库执行SQL**

### **扫描全部/部分分库分表**

### **INDEX HINT**

## 函数

## 运算符

## 数据类型

## PrepareSQL

## 实用SQL

### **CHECK TABLE**

### **TRACE**

### **CHECK GLOBAL INDEX**

### **EXPLAIN**

## 多语句

## 跨Schema

## 账号和权限系统

## SQL限流

## 通过LOCALITY指定库或表的存储位置

 

# SQL审计与分析

# 分布式**事务

分布式事务通常使用二阶段提交来保证事务的原子性（Atomicity）和一致性（Consistency）。

二阶段事务会将事务分为以下两个阶段：

准备（PREPARE）阶段：在PREPARE阶段，数据节点会准备好所有事务提交所需的资源（例如加锁、写日志等）。

提交（COMMIT） 阶段：在COMMIT阶段，各个数据节点才会真正提交事务。

当提交一个分布式事务时，PolarDB-X服务器会作为事务管理器的角色，等待所有数据节点（MySQL服务器） PREPARE成功，之后再向各个数据节点发送COMMIT请求。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB88A.tmp.jpg) 

# 数据分布

## 分片键

拆分键即分库或分表字段，是水平拆分过程中用于生成拆分规则的数据表字段。PolarDB-X将拆分键值通过拆分函数计算得到一个计算结果，然后根据这个结果将数据分拆到私有定制RDS实例上。

数据表拆分的首要原则是尽可能找到数据所归属的业务逻辑实体，并确定大部分（或核心的）SQL操作或者具备一定并发的SQL都是围绕这个实体进行，然后可使用该实体对应的字段作为拆分键。

## 分片数

PolarDB-X中的水平拆分包含了分库和分表两个层次。若您在[创建数据库](#multiTask323)时，选择拆分模式为水平拆分，则PolarDB-X为默认为每个私有定制RDS实例创建8个物理分库，每个物理分库上可以创建一个或多个物理分表，而分表数通常也被称为分片数。

计算公式：

一般情况下，建议单个物理分表的总容量范围在500万~5000万行数据（若单行记录超过4KB，建议总容量范围不超过500万），同时控制B+树的深度为3~4层。

您可以先预估1~2年内的数据增长量，用估算出的总数据量除以总的物理分库数，再除以建议的单个物理分表的最大数据量（本文以500万为例），即可得出每个物理分库上需要创建的物理分表数。

物理分库上的物理分表数=向上取整（估算的总数据量/（私有定制RDS实例数 x 8）/ 5,000,000）

因此，若计算出的物理分表数等于1时，当前分库即可满足需求，您无需再进一步分表，保持当前每个物理分库上一个物理分表即可。若计算结果大于1，则建议既分库又分表，即每个物理分库上再创建多个物理分表。

# 复制/一致性

# 备份恢复

## PolarDB-X1.0

PolarDB-X 备份恢复提供实例级、数据库级的备份恢复能力。实例备份支持自动备份与手动备份，备份方式包括快速备份与一致性备份。实例恢复基于已有备份集，将数据恢复至新的 PolarDB-X 与 RDS 实例。

### **备份恢复**

***\*备份方式：\****

PolarDB-X 备份恢复针对不同的业务场景，提供快速备份与一致性备份两种备份方式以及相应的数据恢复能力。两种备份方式的对比如下表：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB88B.tmp.jpg) 

***\*限制与说明\*******\*：\****

PolarDB-X 自动备份策略默认关闭，需要您手动开启。

PolarDB-X 日志备份能力依赖下层RDS，PolarDB-X 控制台设置的日志备份策略会自动同步至下层所有RDS，设置完成后，请勿在 RDS 控制台修改，避免备份集失效。

PolarDB-X 备份恢复依赖日志备份，建议您默认开启日志备份策略，避免备份集失效。

备份过程中不要执行DDL操作，避免实例备份与恢复失败。

备份过程中，请确保 PolarDB-X 实例下层 RDS 的状态正常，避免备份失败。

一致性备份与恢复功能仅在5.3.8及以上版本支持，版本信息详见：[版本说明](https://help.aliyun.com/document_detail/49279.html)。

请确认所有的数据表都存在主键，避免影响一致性备份恢复的数据准确性。

一致性备份过程中，对 PolarDB-X 实例会进行秒级的锁定分布事务操作，锁定过程中，非事务 SQL 以及单机事务执行不受影响，分布式事务提交会被阻塞，SQL 执行 RT 存在毫秒级抖动，建议在业务低峰期进行一致性备份操作。

受 PolarDB-X 与 RDS 库存的影响，PolarDB-X 在实例恢复过程中，会为您自动调整实例的规格与可用区，请在实例恢复完成后确认并调整，避免影响业务。

恢复实例按照实际的付费类型，规格计费。

 

### **SQL闪回**

PolarDB-X SQL闪回针对SQL误操作，提供行级数据的恢复能力。

当您在PolarDB-X上执行误操作SQL（如INSERT、UPDATE或DELETE) 后，使用SQL闪回，提供相关的误操作SQL信息，即可从BINLOG中找到匹配的事件，生成对应的恢复文件，下载后根据需求恢复数据。

SQL闪回针对误操作SQL，支持模糊匹配与精确匹配两种丢失数据的定位策略，以及自动选择匹配策略的能力，请请参见[精确匹配与模糊匹配](#87605b9c)。

面向不同的使用场景，SQL闪回提供[回滚SQL与原始SQL](#83f271f9)两种方式来恢复数据。

***\*功能优势：\****

操作简单：轻松配置，填写少量误操作SQL信息，即可助您找回丢失的数据。

快速轻量：不依赖RDS的备份策略，只需在误操作SQL执行前开启RDS日志备份即可快速恢复误操作数据。

灵活的恢复方式：针对不同的场景，提供回滚SQL与原始SQL两种能力，恢复方式灵活多样。

SQL级精确匹配能力：SQL级的误操作数据精确匹配能力，提高数据恢复的精准性。

***\*限制与说明：\****

SQL闪回依赖RDS BINLOG保存时间，请开启RDS日志备份。RDS BINLOG存在保存时限，误操作数据后请尽快使用SQL闪回生成恢复文件。

SQL闪回生成的恢复文件默认保存7天，生成后请尽快下载。

SQL闪回精确匹配需要满足如下条件：

PolarDB-X实例版本在5.3.4-15378085版本及以上，关于版本信息，详情请参见[版本说明](https://help.aliyun.com/document_detail/49279.htm)。

PolarDB-X数据库使用的RDS是5.6及上版本。

执行误操作SQL前，SQL闪回精确匹配开关已开启。

提供误操作SQL的TRACE_ID信息。

为了保证数据恢复的精准性，PolarDB-X对于在5.3.4-15378085版本及以上实例新建的数据库，默认开启精确匹配的开关。开启后，RDS BINLOG中会默认带上执行SSQL的信息，增加一定的RDS存储空间。

 

### **表回收站**

PolarDB-X表回收站提供针对误删表操作的数据恢复能力。

开启PolarDB-X表回收站功能后，通过DROP TABLE指令删除的表将被移动至表回收站中不再可见，数据表移动至回收站2小时后，即被自动清理，无法恢复。您可以在表回收站中查看、恢复、清理已删除的表。

***\*限制与说明\*******\*：\****

PolarDB-X实例版本需为5.3.3-1670435或以上版本。

PolarDB-X表回收站默认关闭。

PolarDB-X表回收站不支持TRUNCATE TABLE命令删除的表。

回收站内的表在自动清理前会继续占用RDS存储空间，如需要快速释放，请前往回收站手动清理。

 

## PolarDB-X2.0

### **备份数据**

备份类型：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB88C.tmp.jpg) 

备份方式：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB88D.tmp.jpg) 

### **恢复数据**

***\*前提条件\*******\*：\****

原实例需要满足如下条件：

如果要按时间点进行恢复，需要确保日志备份功能已开启（即在设置数据备份策略时，日志备份保留时间需大于0）。

若要按备份集恢复，则原实例必须至少有一个物理备份。

***\*注意事项\*******\*：\****

PolarDB-X 2.0当前仅支持恢复数据到一个新实例。其中：

新实例的白名单设置、备份设置、参数设置和当前实例保持一致。

新实例内的数据信息与备份文件或时间点当时的信息一致。

新实例带有所使用备份文件或时间点当时的账号信息。

### **清除binlog**

PolarDB-X 2.0开启日志备份后，产生的Binlog文件会占用实例的存储空间。为避免存储空间被过多占用，您可以设置Binlog保留规则来自动清理本地Binlog。

PolarDB-X 2.0实例开启日志备份后，数据库的变动都会被记录至Binlog日志。

每当Binlog文件大小超过500 MB，会生成新的Binlog文件继续写入。

说明 Binlog文件可能小于500 MB就不再写入，例如由于写入超过6小时、系统重启等原因。Binlog文件也可能超过500 MB，例如执行大事务时不断写入Binlog。

 

***\*功能说明\*******\*：\****

开启日志备份后，PolarDB-X 2.0会自动将Binlog文件上传到备份空间，符合本地日志保留策略的Binlog将被保存在实例存储空间上。

清理本地Binlog仅减少Binlog占用的实例存储空间，不会减少Binlog的总大小，也不影响实例的数据恢复功能。

 

# 兼容性

# 扩展性

扩展性本质在于分而治之，PolarDB-X计算资源通过水平拆分（分库分表）和垂直拆分，将数据分散到多个存储资源MySQL以实现获取数据读写并发和存储容量分散的效果。

## 水平拆分（分库分表）

您可以通过一定的计算或路由规则放置数据，实现将数据分散到多个存储资源MySQL的目的，实际上PolarDB-X具备相当丰富的算法来应对各种场景。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB89E.tmp.jpg) 

## 计算扩展性

无论是水平拆分还是垂直拆分，PolarDB-X常常碰到需要对远超单机容量数据进行复杂计算的需求，例如需要执行多表JOIN、多层嵌套子查询、Grouping、Sorting、Aggregation等组合的SQL操作语句。

针对这类在线数据库上复杂SQL的处理， PolarDB-X额外扩展了单机并行处理器（Symmetric Multi-Processingy，简称SMP）和多机并行处理器（DAG）。前者完全集成在PolarDB-X内核中；而对于后者，PolarDB-X构建了一个计算集群，能够在运行时动态获取执行计划并进行分布式计算，通过增加节点提升计算能力。

 

## 扩容管理

### **平滑扩容**

PolarDB-X 平滑扩容是指通过增加 RDS 的数量以提升整体性能。当 RDS 的 IOPS、CPU、磁盘容量等指标到达瓶颈，并且 SQL 优化、RDS 升配已无法解决瓶颈（例如磁盘已升至顶配）时，可通过 PolarDB-X 水平扩容增加 RDS 数量，提升 PolarDB-X 数据库的容量。

PolarDB-X 平滑扩容通过迁移分库到新 RDS 来降低原 RDS 的压力。例如，扩容前8个库的压力集中在一个 RDS 实例上，扩容后8个库分别部署在两个 RDS 实例上，单个 RDS 实例的压力就明显降低。如下图所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB89F.tmp.jpg) 

说明：平滑扩容多次后，如果出现 RDS 数量和分库数量相等的情况，需要创建另外一个 PolarDB-X 和预期容量 RDS 的数据库，再进行数据迁移以达到更大规模数据容量扩展的目标。此过程较复杂，推荐创建 PolarDB-X 数据库时要考虑未来2-3年数据的增长预期，做好 RDS 数量规划。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8A0.tmp.jpg) 

### **热点扩容**

数据表通过分库分表进行水平拆分后，部分过热的数据会占用大部分存储空间与负载压力。

PolarDB-X热点扩容将过热数据单独迁移存放至单独的存储资源RDS实例中，来优化PolarDB-X的存储结构并提升整体数据库执行效率。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8B1.tmp.jpg) 

PolarDB-X热点扩容会将原物理分库中的热点数据迁移至新的物理分库中，建议您选择一个新的存储资源RDS实例来存放这些数据，避免对原存储资源RDS实例造成过多读写压力。

# 高并发

## 读写分离

当PolarDB-X存储资源MySQL主实例的读请求较多、读压力比较大时，您可以通过读写分离功能对读流量进行分流，减轻存储层的读压力。

PolarDB-X读写分离功能采用了对应用透明的设计。在不修改应用程序任何代码的情况下，只需在控制台中调整读权重，即可实现将读流量按自定义的权重比例在存储资源MySQL主实例与多个存储资源只读实例之间进行分流，而写流量则不做分流全部到指向主实例。

设置读写分离后，从存储资源MySQL主实例读取属于强读（即实时强一致读）；而只读实例上的数据是从主实例上异步复制而来存在毫秒级的延迟，因此从只读实例读取属于弱读（即非强一致性读）。您可以通过Hint指定那些需要保证实时性和强一致性的读SQL到主实例上执行。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8B2.tmp.jpg) 

读写分离对事务的支持

读写分离仅对显式事务（即需要显式提交或回滚的事务）以外的读请求（即查询请求）有效，写请求和显式事务中的读请求（包括只读事务）均在主实例中执行，不会被分流到只读实例。

常见的读、写请求SQL语句包括：

读请求：SELECT、SHOW、EXPLAIN、DESCRIBE。

写请求：INSERT、REPLACE、UPDATE、DELETE、CALL。

 

## MPP

# 高可用

## 切换可用域

PolarDB-X 1.0支持切换可用区服务，若出现错误选择可用区、原可用去库存不足等情况，您可以将原PolarDB-X 1.0实例迁移到其它可用区。

***\*注意事项\*******\*：\****

切换可用区过程一般持续约1~2分钟，建议在流量低峰期操作避免影响正常业务。

可用区切换时会发生秒级的连接闪断，请确保您的应用客户端具备自动重连机制。

 

# 数据安全

# 数据压缩

# 数据迁移

## 导入导出

# 性能优化

## 概述

PolarDB-X将执行时间超过1秒的SQL定义为慢SQL，包括逻辑慢SQL和物理慢SQL。

逻辑慢SQL和物理慢SQL的定义如下：

逻辑慢SQL：客户端发送到PolarDB-X的慢SQL。

物理慢SQL：PolarDB-X发送到存储层MySQL的慢SQL。

每个PolarDB-X节点最多保存5000条慢SQL明细，超过限制数量的慢SQL明细会被滚动删除。

## 基础

在使用PolarDB-X的过程中，可能出现性能不符合预期的慢SQL。SQL调优的过程，就是通过分析SQL的执行计划、各阶段运行时长等信息，找出导致SQL执行慢的原因，继而解决问题。

 

### **架构**

PolarDB-X既可以被看作一个中间件，也可以被看作一个支持计算存储分离架构的数据库产品。当一条查询SQL（称为逻辑SQL）发往PolarDB-X节点时，PolarDB-X会将其分成可下推的、和不可下推的两部分，可下推的部分也被称为物理SQL。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8B3.tmp.jpg) 

原则上，PolarDB-X会：

尽可能将用户SQL下推到MySQL上执行。

对于无法下推的部分算子，选择最优的方式来执行。

 

### **下推和执行计划**

EXPLAIN指令将会打印SQL的执行计划。它的用法非常简单：只要在SQL最前面加上EXPLAIN即可。下面通过几个例子来展示PolarDB-X的执行方式。

示例一：

\> explain select c_custkey, c_name, c_address from customer where c_custkey = 42;

LogicalView(tables="customer_2", sql="SELECT `c_custkey`, `c_name`, `c_address` FROM `customer` AS `customer` WHERE (`c_custkey` = ?)")

对于带有主键条件的查询来说，PolarDB-X只要将SQL直接下发到主键对应的分片上即可。因此，执行计划中只有一个LogicalView算子，下发的物理SQL基本和逻辑SQL是一样的。

示例二：

\> explain select c_nationkey, count(*) from customer group by c_nationkey;

HashAgg(group="c_nationkey", count(*)="SUM(count(*))")

 Gather(concurrent=true)

  LogicalView(tables="customer_[0-7]", shardCount=8, sql="SELECT `c_nationkey`, COUNT(*) AS `count(*)` FROM `customer` AS `customer` GROUP BY `c_nationkey`")

上述查询会统计：各个国家的客户数量分别有多少？考虑到分库分表，可以将它分成两个阶段来进行：

在各个分表上进行COUNT(*)统计（这一步可被下推到MySQL上执行）。

将结果汇总，将COUNT(*)的结果执行SUM()，得到最终的结果（这一步需要PolarDB-X节点来完成）。

从执行计划上，也可以看出这一点。其中：

LogicalView表示下发到各个分片的SQL按nation分组进行 COUNT(*)统计。

Gather算子表示收集各个分片的结果。

HashAgg是聚合（Aggregate）的一种实现方式，以c_nationkey作为分组键将 COUNT(*)的结果求SUM()。

 

## 调优方法

找出需调优的慢SQL后，先通过EXPLAIN查看执行计划，然后通过如下方法优化SQL：下推更多计算至存储层MySQL，适当增加索引，优化执行计划。

### **下推更多计算**

PolarDB-X会尽可能将更多的计算下推到存储层MySQL。下推计算能够减少数据传输，减少网络层和PolarDB-X层的开销，提升SQL语句的执行效率。PolarDB-X支持下推几乎所有算子，包括：

过滤条件，如WHERE或HAVING中的条件。

聚合算子，如COUNT，GROUP BY等，会分成两阶段进行聚合计算。

排序算子，如ORDER BY。

JOIN和子查询，两边JOIN Key分片方式必须一样，或其中一边为广播表。

如下示例讲解如何将更多的计算下推到MySQL来加速执行

\> EXPLAIN select * from customer, nation where c_nationkey = n_nationkey and n_regionkey = 3;

Project(c_custkey="c_custkey", c_name="c_name", c_address="c_address", c_nationkey="c_nationkey", c_phone="c_phone", c_acctbal="c_acctbal", c_mktsegment="c_mktsegment", c_comment="c_comment", n_nationkey="n_nationkey", n_name="n_name", n_regionkey="n_regionkey", n_comment="n_comment")

 BKAJoin(condition="c_nationkey = n_nationkey", type="inner")

  Gather(concurrent=true)

   LogicalView(tables="nation", shardCount=2, sql="SELECT * FROM `nation` AS `nation` WHERE (`n_regionkey` = ?)")

  Gather(concurrent=true)

   LogicalView(tables="customer_[0-7]", shardCount=8, sql="SELECT * FROM `customer` AS `customer` WHERE (`c_nationkey` IN ('?'))")

若执行计划中出现了BKAJOIN，BKAJOIN每次从左表获取一批数据，就会拼成一个IN查询取出右表相关联的行，并在最后执行JOIN操作。由于左表数据量很大，需要取很多次才能完成查询，执行很慢。

无法下推JOIN的原因是：当前情况下，nation是按主键n_nationkey切分的，而本查询的JOIN Key是c_custkey，二者不同，所以下推失败。

考虑到nation （国家）表数据量并不大、且几乎没有修改操作，可以将其重建成如下广播表：

--- 修改后 ---CREATE TABLE `nation` (

 `n_nationkey` int(11) NOT NULL,

 `n_name` varchar(25) NOT NULL,

 `n_regionkey` int(11) NOT NULL,

 `n_comment` varchar(152) DEFAULT NULL,

 PRIMARY KEY (`n_nationkey`)

) BROADCAST;  --- 声明为广播表

修改后，可以看到执行计划中不再出现JOIN，几乎所有计算都被下推到存储层MySQL执行了（LogicalView中），而上层仅仅是将结果收集并返回给用户（Gather算子），执行性能大大增强。

\> EXPLAIN select * from customer, nation where c_nationkey = n_nationkey and n_regionkey = 3;

Gather(concurrent=true)

 LogicalView(tables="customer_[0-7],nation", shardCount=8, sql="SELECT * FROM `customer` AS `customer` INNER JOIN `nation` AS `nation` ON ((`nation`.`n_regionkey` = ?) AND (`customer`.`c_nationkey` = `nation`.`n_nationkey`))")

 

### **增加索引**

如果下推SQL中出现（物理）慢SQL，可以给分表增加索引来解决，这里不再详述。

PolarDB-X自5.4.1 版本开始支持[全局二级索引](#concept-1946505)，可以通过增加GSI的方式使逻辑表拥有多个拆分维度。

下面以一个慢SQL作为例子来讲解如何通过GSI下推更多算子。

\> EXPLAIN select o_orderkey, c_custkey, c_name from orders, customer

​     where o_custkey = c_custkey and o_orderdate = '2019-11-11' and o_totalprice > 100;

 

Project(o_orderkey="o_orderkey", c_custkey="c_custkey", c_name="c_name")

 HashJoin(condition="o_custkey = c_custkey", type="inner")

  Gather(concurrent=true)

   LogicalView(tables="customer_[0-7]", shardCount=8, sql="SELECT `c_custkey`, `c_name` FROM `customer` AS `customer`")

  Gather(concurrent=true)

   LogicalView(tables="orders_[0-7]", shardCount=8, sql="SELECT `o_orderkey`, `o_custkey` FROM `orders` AS `orders` WHERE ((`o_orderdate` = ?) AND (`o_totalprice` > ?))")

执行计划中，orders按照o_orderkey拆分而customer按照c_custkey拆分，由于拆分维度不同JOIN算子不能下推。

考虑到2019-11-11当天总价高于100的订单非常多，跨分片JOIN耗时很高，需要在orders表上创建一个GSI来使得JOIN算子可以下推。

查询中使用到了orders表的o_orderkey, o_custkey, o_orderdate, o_totalprice四列，其中o_orderkey, o_custkey分别是主表和索引表的拆分键，o_orderdate, o_totalprice作为覆盖列包含在索引中用于避免回表。

\> create global index i_o_custkey on orders(`o_custkey`) covering(`o_orderdate`, `o_totalprice`)

​    DBPARTITION BY HASH(`o_custkey`) TBPARTITION BY HASH(`o_custkey`) TBPARTITIONS 4;

增加GSI并通过force index(i_o_custkey)强制使用索引后，跨分片JOIN变为MySQL上的局部JOIN （IndexScan中），并且通过覆盖列避免了回表操作，查询性能得到提升。

\> EXPLAIN select o_orderkey, c_custkey, c_name from orders force index(i_o_custkey), customer

​     where o_custkey = c_custkey and o_orderdate = '2019-11-11' and o_totalprice > 100;

 

Gather(concurrent=true)

 IndexScan(tables="i_o_custkey_[0-7],customer_[0-7]", shardCount=8, sql="SELECT `i_o_custkey`.`o_orderkey`, `customer`.`c_custkey`, `customer`.`c_name` FROM `i_o_custkey` AS `i_o_custkey` INNER JOIN `customer` AS `customer` ON (((`i_o_custkey`.`o_orderdate` = ?) AND (`i_o_custkey`.`o_custkey` = `customer`.`c_custkey`)) AND (`i_o_custkey`.`o_totalprice` > ?))")

 

### **执行计划调优**

说明 以下内容适用于PolarDB-X5.3.12或以上版本。

大多数情况下，PolarDB-X的查询优化器可以自动产生最佳的执行计划。但是，少数情况下，可能因为统计信息存在缺失、误差等，导致生成的执行计划不够好，这时，可以通过Hint来干预优化器行为，使之生成更好的执行计划。

如下示例将讲解执行计划的调优。

下面的查询，PolarDB-X查询优化器综合了JOIN两边的代价。

\> EXPLAIN select o_orderkey, c_custkey, c_name from orders, customer

​     where o_custkey = c_custkey and o_orderdate = '2019-11-15' and o_totalprice < 10;

 

Project(o_orderkey="o_orderkey", c_custkey="c_custkey", c_name="c_name")

 HashJoin(condition="o_custkey = c_custkey", type="inner")

  Gather(concurrent=true)

   LogicalView(tables="customer_[0-7]", shardCount=8, sql="SELECT `c_custkey`, `c_name` FROM `customer` AS `customer`")

  Gather(concurrent=true)

   LogicalView(tables="orders_[0-7]", shardCount=8, sql="SELECT `o_orderkey`, `o_custkey` FROM `orders` AS `orders` WHERE ((`o_orderdate` = ?) AND (`o_totalprice` < ?))")

但是，实际上2019-11-15这一天总价低于10元的订单数量很小，只有几条，这时候用BKAJOIN是比Hash JOIN更好的选择（关于BKAJOIN和Hash JOIN的介绍，请参见[JOIN与子查询的优化和执行](#task-1948108)。

通过如下/*+TDDL:BKA_JOIN(orders, customer)*/Hint强制优化器使用BKAJOIN（LookupJOIN）：

\> EXPLAIN /*+TDDL:BKA_JOIN(orders, customer)*/ select o_orderkey, c_custkey, c_name from orders, customer

​     where o_custkey = c_custkey and o_orderdate = '2019-11-15' and o_totalprice < 10;

 

Project(o_orderkey="o_orderkey", c_custkey="c_custkey", c_name="c_name")

 BKAJoin(condition="o_custkey = c_custkey", type="inner")

  Gather(concurrent=true)

   LogicalView(tables="orders_[0-7]", shardCount=8, sql="SELECT `o_orderkey`, `o_custkey` FROM `orders` AS `orders` WHERE ((`o_orderdate` = ?) AND (`o_totalprice` < ?))")

  Gather(concurrent=true)

   LogicalView(tables="customer_[0-7]", shardCount=8, sql="SELECT `c_custkey`, `c_name` FROM `customer` AS `customer` WHERE (`c_custkey` IN ('?'))")

可以选择执行加如下Hint的查询：

/*+TDDL:BKA_JOIN(orders, customer)*/ select o_orderkey, c_custkey, c_name from orders, customer where o_custkey = c_custkey and o_orderdate = '2019-11-15' and o_totalprice < 10;

以上操作加快了SQL查询速度。为了让Hint发挥作用，可以将应用中的SQL加上Hint，或者更方便的方式是使用执行计划管理（Plan Management）功能对该SQL固定执行计划。具体操作如下：

BASELINE FIX SQL /*+TDDL:BKA_JOIN(orders, customer)*/ select o_orderkey, c_custkey, c_name from orders, customer where o_custkey = c_custkey and o_orderdate = '2019-11-15';

这样一来，对于这条SQL（参数可以不同），PolarDB-X都会采用如上固定的执行计划。

 

## 调优进阶

### **查询优化器**

查询优化器通过优化逻辑计划从而输出物理计划，其主要阶段包含查询改写和计划枚举。

PolarDB-X接收到一条SQL后的执行过程大致如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8C3.tmp.jpg) 

语法解析器（Parser）将SQL文本解析成抽象语法树（AST）。

语法树被转化成基于关系代数的逻辑计划。

优化器（Optimizer）对逻辑计划进行优化得到物理计划。

执行器（Executor）执行该计划，得到查询结果并返回给客户端。

 

#### **关系代数算子**

一条SQL查询在数据库系统中通常被表示为一棵关系代数算子组成的树，有如下场景的算子：

Project：用于描述SQL中的SELECT列，包括函数计算。

Filter：用于描述SQL中的WHERE条件。

JOIN：用于描述SQL中的JOIN，其对应的物理算子有HashJoin、 BKAJoin、Nested-Loop Join、SortMergeJoin。

Agg：用于描述SQL中的Group By及聚合函数，其对应的物理算子有HashAgg、SortAgg。

Sort：用于描述SQL中的Order By及Limit，其对应的物理算子有TopN、MemSort。

LogicalView：用于描述PolarDB-X下发至存储层MySQL的SQL，其内部可能包含一个或多个逻辑算子。

Gather：代表从多个数据流汇集数据的操作，通常出现在LogicalView之上（若开启并行执行，则并行优化步骤会将其上拉）。

例如，对于如下查询SQL（修改自[TPC-H Query 3](https://examples.citusdata.com/tpch_queries.html)）：

SELECT l_orderkey, sum(l_extendedprice *(1 - l_discount)) AS revenueFROM CUSTOMER, ORDERS, LINEITEMWHERE c_mktsegment = 'AUTOMOBILE'

 and c_custkey = o_custkey

 and l_orderkey = o_orderkey

 and o_orderdate < '1995-03-13'

 and l_shipdate > '1995-03-13'GROUP BY l_orderkey;

通过如下EXPLAIN命令看到PolarDB-X的执行计划：

HashAgg(group="l_orderkey", revenue="SUM(*)")

 HashJoin(condition="o_custkey = c_custkey", type="inner")

  Gather(concurrent=true)

   LogicalView(tables="ORDERS_[0-7],LINEITEM_[0-7]", shardCount=8, sql="SELECT `ORDERS`.`o_custkey`, `LINEITEM`.`l_orderkey`, (`LINEITEM`.`l_extendedprice` * (? - `LINEITEM`.`l_discount`)) AS `x` FROM `ORDERS` AS `ORDERS` INNER JOIN `LINEITEM` AS `LINEITEM` ON (((`ORDERS`.`o_orderkey` = `LINEITEM`.`l_orderkey`) AND (`ORDERS`.`o_orderdate` < ?)) AND (`LINEITEM`.`l_shipdate` > ?))")

  Gather(concurrent=true)

   LogicalView(tables="CUSTOMER_[0-7]", shardCount=8, sql="SELECT `c_custkey` FROM `CUSTOMER` AS `CUSTOMER` WHERE (`c_mktsegment` = ?)")

用树状图表示如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8C4.tmp.jpg) 

说明：左边的LogicalView实际包含了ORDERS和LINEITEM两张表的JOIN。EXPLAIN结果中LogicalView的SQL属性也体现了这一点。

#### **查询改写（RBO）**

查询改写（SQL Rewrite）阶段输入为逻辑执行计划，输出为逻辑执行计划。这一步主要应用一些启发式规则，是基于规则的优化器（Rule-Based Optimizer，简称RBO），所以也常被称为RBO阶段。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8C5.tmp.jpg) 

查询改写这一步的主要有如下功能：

1、子查询去关联化（Subquery Unnesting）

子查询去关联化是将含有关联项的子查询（关联子查询）表示为SemiJoin或类似的算子，便于后续的各种优化，例如下推到存储层MySQL或在PolarDB-X层选择某种算法执行。在如下例子中IN子查询转化为SemiJoin算子，并最终转化成SemiHashJoin物理算子由PolarDB-X进行执行：

\> explain  select id from t1 where id in (select id from t2 where t2.name = 'hello');

SemiHashJoin(condition="id = id", type="semi")

 Gather(concurrent=true)

  LogicalView(tables="t1", shardCount=2, sql="SELECT `id` FROM `t1` AS `t1`")

 Gather(concurrent=true)

  LogicalView(tables="t2_[0-3]", shardCount=4, sql="SELECT `id` FROM `t2` AS `t2` WHERE (`name` = ?)")

 

2、算子下推

算子下推是非常关键的一步，PolarDB-X内置了如下算子的下推优化规则：

| 优化规则         | 描述                                                         |
| ---------------- | ------------------------------------------------------------ |
| 谓词下推或列裁剪 | 将Filter及Project算子下推至存储层MySQL执行，过滤掉不需要的行和列。 |
| JOIN Clustering  | 将JOIN按照拆分方式及拆分键的等值条件进行重排和聚簇，方便下一步的JOIN下推。 |
| JOIN下推         | 对于符合条件的JOIN，将其下推至存储层MySQL执行。              |
| Agg下推          | 将聚合（Agg）拆分为FinalAgg和LocalAgg两个阶段，并将LocalAgg下推至存储层MySQL。 |
| Sort下推         | 将排序（Sort）拆分为MergeSort和LocalSort两个阶段，并将LocalSort下推至存储层MySQL。 |

 

#### **查询计划枚举（CBO）**

查询改写阶段输出的逻辑执行计划会被输入到查询计划枚举（Plan Enumerator）中，并输出一个最终的物理执行计划。查询计划枚举在多个可行的查询计划中，根据预先定义的代价模型，选择出代价最低的一个。与查询改写阶段不同，在查询计划枚举中，规则可能产生更好的执行计划，也可能产生更差的执行计划，可以根据算子经过规则优化后的前后代价对比选出较优的那个，因此这也被称为基于代价的优化（Cost-based Optimizer，简称CBO）。

其核心组件有以下几个部分：

统计信息（Statistics）

基数估计（Cardinality Estimation）

转化规则（Transform Rules）

代价模型（Cost Model）

计划空间搜索引擎（Plan Space Search Engine）

逻辑上，CBO的过程包括如下几个步骤：

搜索引擎利用转化规则，对输入的逻辑执行计划进行变换，构造出物理执行计划的搜索空间。

之后，利用代价模型对搜索空间中的每一个执行计划进行代价估计，选出代价最低的物理执行计划。

而代价估计的过程离不开基数估计，它利用各个表、列的统计信息，估算出各算子的输入行数、选择率等信息，提供给算子的代价模型，从而估算出查询计划的代价。

 

### **查询改写与下推**

下推是查询改写的一项重要优化，利用PolarDB-X的拆分信息来优化执行计划，使得算子尽量下推以达到提前过滤数据、减少网络传输、并行计算等目的。

#### **背景信息**

根据PolarDB-X的SQL语句优化的基本原则，可以下推尽量更多的计算到存储层MySQL上执行。

可下推计算主要包括：

JOIN连接

过滤条件（如WHERE或HAVING中的条件）

计算（如COUNT、GROUP BY）

排序（如ORDER BY）

去重（如DISTINCT）

函数计算（如NOW()函数）

子查询

说明：通过explain optimizer + sql可以看到查询改写的具体过程。

#### **Project和Filter下推**

一条SQL的执行计划在如下生成过程中，Filter和Project被先后下推到LogicalView算子里面。

Filter和Project下推可以达到提前过滤数据，减少网络传输等效果。

\> explain optimizer select c_custkey,c_name from customer where c_custkey = 1;

c_custkey：customer的拆分键。c_name：customer的名字。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8D6.tmp.jpg) 

#### **Limit和Sort下推**

一条SQL的执行计划在如下生成过程中，Sort和Limit被先后下推到LogicalView算子里面。Sort和Limit下推可以达到提前过滤数据，减少网络传输、并行执行、减少PolarDB-X内存占用等效果。

\> explain optimizer select * from customer order by c_custkey limit 10

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8D7.tmp.jpg) 

#### **Agg下推**

一条SQL的执行计划在如下生成过程中，Agg被下推到LogicalView算子里面。

Agg下推可以达到提前过滤数据，减少网络传输，并行执行，减少PolarDB-X内存占用等效果。

\> explain optimizer select count(*) from customer group by c_nationkey;

拆分键为c_nationkey情况：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8D8.tmp.jpg) 

拆分键不为c_nationkey情况：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8D9.tmp.jpg) 

#### **JOIN下推**

JOIN下推需要满足以下条件：

t1与t2表的拆分方式一致（包括分库键、分表键、拆分函数、分库分表数目）。

JOIN条件中包含t1，t2表拆分键的等值关系。

此外，任意表JOIN广播表总是可以下推。

\> explain optimizer select * from t1, t2 where t1.id = t2.id;

一条SQL的执行计划在如下生成过程中，JOIN下推到LogicalView算子里面。JOIN下推可以达到计算离存储更近，并行执行加速的效果。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8EA.tmp.jpg) 

#### **JoinClustering**

当有多个表执行JOIN操作时，PolarDB-X会通过join clustering的优化技术将JOIN进行重排序，将可下推的JOIN放到相邻的位置，从而让它可以被正常下推。示例如下：

假设原JOIN顺序为t2、t1、l2, 经过重排序之后，t2和l2的JOIN操作依然能下推到LogicalView。

\> explain select t2.id from t2 join t1 on t2.id = t1.id join l2 on t1.id = l2.id;

Project(id="id")

 HashJoin(condition="id = id AND id = id0", type="inner")

  Gather(concurrent=true)

   LogicalView(tables="t2_[0-3],l2_[0-3]", shardCount=4, sql="SELECT `t2`.`id`, `l2`.`id` AS `id0` FROM `t2` AS `t2` INNER JOIN `l2` AS `l2` ON (`t2`.`id` = `l2`.`id`) WHERE (`t2`.`id` = `l2`.`id`)")

  Gather(concurrent=true)

   LogicalView(tables="t1", shardCount=2, sql="SELECT `id` FROM `t1` AS `t1`")

#### **子查询下推**

一条SQL的执行计划在如下生成过程中，子查询下推到LogicalView算子里面。

子查询下推可以达到计算离存储更近，并行执行加速的效果。

子查询会先被转换成Semi Join或Anti Join。

之后如果满足上节中JOIN下推的判断条件，就会将Semi Join或Anti Join下推至LogicalView。

下推后的Semi Join或Anti Join会被还原为子查询。

explain optimizer select * from t1 where id in (select id from t2);

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8EB.tmp.jpg) 

### **查询执行器**

#### **基本概念**

SQL执行器是PolarDB-X中执行逻辑层算子的组件。对于简单的点查SQL，往往可以整体下推存储层MySQL执行，因而感觉不到执行器的存在，MySQL的结果经过简单的解包封包又被回传给用户。但是对于较复杂的SQL，往往无法将SQL中的算子全部下推，这时候就需要PolarDB-X执行器执行无法下推的计算。

例如，对于如下查询SQL：

SELECT l_orderkey, sum(l_extendedprice *(1 - l_discount)) AS revenueFROM CUSTOMER, ORDERS, LINEITEMWHERE c_mktsegment = 'AUTOMOBILE'

 and c_custkey = o_custkey

 and l_orderkey = o_orderkey

 and o_orderdate < '1995-03-13'

 and l_shipdate > '1995-03-13'GROUP BY l_orderkey;

通过EXPLAIN命令看到PolarDB-X的执行计划如下：

HashAgg(group="l_orderkey", revenue="SUM(*)")

 HashJoin(condition="o_custkey = c_custkey", type="inner")

  Gather(concurrent=true)

   LogicalView(tables="ORDERS_[0-7],LINEITEM_[0-7]", shardCount=8, sql="SELECT `ORDERS`.`o_custkey`, `LINEITEM`.`l_orderkey`, (`LINEITEM`.`l_extendedprice` * (? - `LINEITEM`.`l_discount`)) AS `x` FROM `ORDERS` AS `ORDERS` INNER JOIN `LINEITEM` AS `LINEITEM` ON (((`ORDERS`.`o_orderkey` = `LINEITEM`.`l_orderkey`) AND (`ORDERS`.`o_orderdate` < ?)) AND (`LINEITEM`.`l_shipdate` > ?))")

  Gather(concurrent=true)

   LogicalView(tables="CUSTOMER_[0-7]", shardCount=8, sql="SELECT `c_custkey` FROM `CUSTOMER` AS `CUSTOMER` WHERE (`c_mktsegment` = ?)")

如下图所示，LogicalView的SQL在执行时被下发给MySQL，而不能下推的部分（除LogicalView以外的算子）由PolarDB-X执行器进行计算，得到最终用户SQL需要的结果。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8EC.tmp.jpg) 

#### **Volcano执行模型**

PolarDB-X和很多数据库一样采用Volcano执行模型。所有算子都定义了open()、next()等接口，算子根据执行计划组合成一棵算子树，上层算子通过调用下层算子的next()接口的取出结果，完成该算子的计算。最终顶层算子产生用户需要的结果并返回给客户端。

下面的例子中，假设HashJoin算子已经完成构建哈希表。当上层的Project算子请求数据时，HashJoin首先向下层Gather请求一批数据，然后查表得到JOIN结果，再返回给Project算子。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8FC.tmp.jpg) 

某些情况下，算子需要将数据全部读取并缓存在内存中，该过程被称为物化，例如，HashJoin算子需要读取内表的全部数据，并在内存中构建出哈希表。其他类似的算子还有HashAgg（聚合）、MemSort（排序）等。

由于内存资源是有限的，如果物化的数据量超出单条查询限制，或者使用的总内存超出PolarDB-X节点内存限制，将会引起内存不足（OUT_OF_MEMORY）报错。

#### **并行查询**

并行查询（Parallel Query） 指利用多线程并行执行用户的复杂查询。

说明：该功能仅在PolarDB-X标准版及企业版上提供，入门版由于硬件规格限制，不提供该项功能。

并行查询的执行计划相比原来有所改动。例如，还是以上面的查询为例，它的并行执行计划如下所示：

Gather(parallel=true)

 ParallelHashAgg(group="o_orderdate,o_shippriority,l_orderkey", revenue="SUM(*)")

  ParallelHashJoin(condition="o_custkey = c_custkey", type="inner")

   LogicalView(tables="ORDERS_[0-7],LINEITEM_[0-7]", shardCount=8, sql="SELECT `ORDERS`.`o_custkey`, `ORDERS`.`o_orderdate`, `ORDERS`.`o_shippriority`, `LINEITEM`.`l_orderkey`, (`LINEITEM`.`l_extendedprice` * (? - `LINEITEM`.`l_discount`)) AS `x` FROM `ORDERS` AS `ORDERS` INNER JOIN `LINEITEM` AS `LINEITEM` ON (((`ORDERS`.`o_orderkey` = `LINEITEM`.`l_orderkey`) AND (`ORDERS`.`o_orderdate` < ?)) AND (`LINEITEM`.`l_shipdate` > ?))", parallel=true)

   LogicalView(tables="CUSTOMER_[0-7]", shardCount=8, sql="SELECT `c_custkey` FROM `CUSTOMER` AS `CUSTOMER` WHERE (`c_mktsegment` = ?)", parallel=true)

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8FD.tmp.jpg) 

可以看出，并行执行计划中Gather算子的位置被拉高了，这也意味者Gather下方的算子都会以并行方式执行，直到Gather时才被汇总成在一起。

执行时，Gather下方的算子会实例化出多个执行实例，分别对应一个并行度。并行度默认等于单台机器的核心数，标准版实例默认并行度为8，企业版实例默认并行度为16。

#### **执行过程的诊断分析**

除了上文提到的EXPLAIN指令，还有如下几个指令能帮助分析性能问题：

EXPLAIN ANALYZE指令用于分析PolarDB-X Server中各算子执行的性能指标。

EXPLAIN EXECUTE指令用于输出MySQL的EXPLAIN结果（并汇总输出）。

如下是以上文提到的查询为例，介绍如何分析一条查询的性能问题。

执行EXPLAIN ANALYZE得到如下结果（删除了一些无关的信息）：

explain analyze select l_orderkey, sum(l_extendedprice *(1 - l_discount)) as revenue from CUSTOMER, ORDERS, LINEITEM where c_mktsegment = 'AUTOMOBILE' and c_custkey = o_custkey and l_orderkey = o_orderkey and o_orderdate < '1995-03-13' and l_shipdate > '1995-03-13' group by l_orderkey;

 

HashAgg(group="o_orderdate,o_shippriority,l_orderkey", revenue="SUM(*)")

... actual time = 23.916 + 0.000, actual rowcount = 11479, actual memory = 1048576, instances = 1 ...

 HashJoin(condition="o_custkey = c_custkey", type="inner")

 ... actual time = 0.290 + 23.584, actual rowcount = 30266, actual memory = 1048576, instances = 1 ...

  Gather(concurrent=true)

  ... actual time = 0.000 + 23.556, actual rowcount = 151186, actual memory = 0, instances = 1 ...

   LogicalView(tables="ORDERS_[0-7],LINEITEM_[0-7]", shardCount=8, sql="SELECT `ORDERS`.`o_custkey`, `ORDERS`.`o_orderdate`, `ORDERS`.`o_shippriority`, `LINEITEM`.`l_orderkey`, (`LINEITEM`.`l_extendedprice` * (? - `LINEITEM`.`l_discount`)) AS `x` FROM `ORDERS` AS `ORDERS` INNER JOIN `LINEITEM` AS `LINEITEM` ON (((`ORDERS`.`o_orderkey` = `LINEITEM`.`l_orderkey`) AND (`ORDERS`.`o_orderdate` < ?)) AND (`LINEITEM`.`l_shipdate` > ?))")

   ... actual time = 0.000 + 23.556, actual rowcount = 151186, actual memory = 0, instances = 4 ...

  Gather(concurrent=true)

  ... actual time = 0.000 + 0.282, actual rowcount = 29752, actual memory = 0, instances = 1 ...

   LogicalView(tables="CUSTOMER_[0-7]", shardCount=8, sql="SELECT `c_custkey` FROM `CUSTOMER` AS `CUSTOMER` WHERE (`c_mktsegment` = ?)")

   ... actual time = 0.000 + 0.282, actual rowcount = 29752, actual memory = 0, instances = 4 ...

其中：

actual time表示实际执行耗时（其中包含子算子的耗时），加号（+）左边表示open（准备数据）耗时，右边表示next（输出数据）耗时。

actual rowcount表示输出的行数。

actual memory表示算子使用的内存空间大小（单位为Bytes）。

instances表示实例数，非并行查询时始终为1，对于并行算子每个并行度对应一个实例。如果实例数不等于1，actual time，actual rowcount，actual memory代表多个实例并行执行的总实际执行耗时、总输出行数、总内存使用量。

说明 当使用并行查询时，上述的算子耗时、输出行数等信息均为算子多个实例的累加。例如actual time = 20，instances = 8，表示该算子有8个实例并行执行，平均耗时为2.5s。

以上面的输出为例，解读如下：

HashAgg算子open耗时为23.916s，用于获取下层HashJoin的输出、并对输出的所有数据做分组和聚合。其中的23.601s都用在了获取了下层输出上，只有约0.3s用于分组聚合。

HashJoin算子open耗时0.290s，用于拉取右表（下方的Gather）数据并构建哈希表；next耗时23.584s，用于拉取左表数据以及查询哈希表得到JOIN结果。

Gather算子仅仅用于汇总多个结果集，通常代价很低。

左侧（上方）的LogicalView拉取数据消耗了23.556s，可判断这里是查询的性能瓶颈。

右侧（下方）的LogicalView拉取数据消耗了0.282s。

综上，性能瓶颈在左边的LogicalView上。从执行计划中可以看到，它是对ORDERS、LINEITEM的JOIN查询，这条查询MySQL执行速度较慢。

您可以通过如下EXPLAIN EXECUTE语句查看MySQL EXPLAIN结果：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB8FE.tmp.png) 

上图中，红色方框对应左边的LogicalView的下推查询，蓝色方框对应右边LogicalView的下推查询。

 

### **执行计划和基本算子**

#### **背景信息**

通常SQL调优的过程离不开以下两个步骤：

分析问题，例如通过EXPLAIN命令查看执行计划，您也可以通过EXPLAIN ANALYZE查看实际执行情况来分析问题。

通过Hint控制优化器行为，修改执行计划。

#### **执行计划与EXPLAIN命令**

下述案例介绍如何通过EXPLAIN命令获取查询的执行计划。

EXPLAIN语法如下：

说明：Hint需放在EXPLAIN之后。

EXPLAIN <SQL Statement>EXPLAIN <Hint> <SQL Statement>

本文中的示例均基于以下表结构：

CREATE TABLE `sbtest1` (

 `id`  INT(10) UNSIGNED NOT NULL,

 `k`  INT(10) UNSIGNED NOT NULL DEFAULT '0',

 `c`  CHAR(120)     NOT NULL DEFAULT '',

 `pad` CHAR(60)     NOT NULL DEFAULT '',

 KEY `xid` (`id`),

 KEY `k_1` (`k`)

) dbpartition BY HASH (`id`) tbpartition BY HASH (`id`) tbpartitions 4

执行以下EXPLAIN命令，PolarDB-X将返回对应的执行计划。

mysql> explain select a.k, count(*) cnt from sbtest1 a, sbtest1 b where a.id = b.k and a.id > 1000 group by k having cnt > 1300 order by cnt limit 5, 10;

+---------------------------------------------------------------------------------------------------------------------------------------------------+

| LOGICAL PLAN                                                                    |

+---------------------------------------------------------------------------------------------------------------------------------------------------+

| MemSort(sort="cnt ASC", offset=?2, fetch=?3)                                                    |

|  Filter(condition="cnt > ?1")                                                           |

|   HashAgg(group="k", cnt="COUNT()")                                                      |

|    BKAJoin(id="id", k="k", c="c", pad="pad", id0="id0", k0="k0", c0="c0", pad0="pad0", condition="id = k", type="inner")            |

|     MergeSort(sort="k ASC")                                                          |

|      LogicalView(tables="[0000-0031].sbtest1_[000-127]", shardCount=128, sql="SELECT * FROM `sbtest1` WHERE (`id` > ?) ORDER BY `k`")     |

|     Gather(concurrent=true)                                                         |

|      LogicalView(tables="[0000-0031].sbtest1_[000-127]", shardCount=128, sql="SELECT * FROM `sbtest1` WHERE ((`k` > ?) AND (`k` IN ('?')))") |

| HitCache:false                                                                   |

+---------------------------------------------------------------------------------------------------------------------------------------------------+9 rows in set (0.01 sec)

除执行计划外，EXPLAIN结果中还会有一些额外信息，上面的例子中仅有一项HitCache（是否命中Plan Cache缓存），详细原理参见[执行计划管理](#task-1948110)。

#### **算子介绍**

PolarDB-X中支持以下算子。

| 含义             | 物理算子                                                     |
| ---------------- | ------------------------------------------------------------ |
| 下发查询         | LogicalView，LogicalModifyView，PhyTableOperation            |
| 连接（Join）     | BKAJoin，NLJoin，HashJoin，SortMergeJoin，HashSemiJoin，SortMergeSemiJoin，MaterializedSemiJoin |
| 排序             | MemSort，TopN                                                |
| 聚合（Group By） | HashAgg，SortAgg                                             |
| 数据交换         | Gather，MergeSort                                            |
| 其它             | Project，Filter, Limit，Union，Window                        |

以下介绍部分算子的含义和实现，剩余的部分在后面的章节中介绍。

LogicalView

LogicalView是从存储层MySQL数据源拉取数据的算子，类似于其他数据库中的TableScan或IndexScan，但支持更多的下推。LogicalView中包含下推的SQL语句和数据源信息，更像一个视图。其中下推的SQL可能包含多种算子，如Project、Filter、聚合、排序、Join和子查询等。下述示例为您展示EXPLAIN中LogicalView的输出信息及其含义：

\> explain select * From sbtest1 where id > 1000;

Gather(concurrent=true)

  LogicalView(tables="[0000-0031].sbtest1_[000-127]", shardCount=128, sql="SELECT * FROM `sbtest1` WHERE (`id` > ?)")

LogicalView的信息由三部分构成：

tables：存储层MySQL对应的表名，以英文句号（.）分割，英文句号（.）之前是分库对应的编号，之后是表名及其编号，如[000-127]表示表名编号从000到127的所有表。

shardCount：需访问的分表总数，该示例会访问从000到127共计128张分表。

sql：下发至存储层MySQL的SQL模版，PolarDB-X在执行时会将表名替换为物理表名，参数化的常量问号（?）替换成实际参数，详情请参见[执行计划管理](#task-1948110)。

Gather

Gather将多份数据合并成同份数据。上面的例子中，Gather将各个分表上查询到的数据合并成一份。

在不使用并行查询时，Gather通常出现在LogicalView上方，表示收集合并各个分表的数据。如果并行查询开启，Gather可能被上拉到执行计划中更高的地方，这时候Gather表示将各个Worker的计算结果收集合并。

Gather中的concurrent表示是否并发执行子算子，默认为true，表示并发拉取数据。开启并行查询时，上拉的Gather属性有所变化，显示为parallel=true。

MergeSort

MergeSort即归并排序算子，表示将有序的数据流进行归并排序，合并成一个有序的数据流。例如：

\> explain select * from sbtest1 where id > 1000 order by id limit 5,10;

MergeSort(sort="id ASC", offset=?1, fetch=?2)

 LogicalView(tables="[0000-0031].sbtest1_[000-127]", shardCount=128, sql="SELECT * FROM `sbtest1` WHERE (`id` > ?) ORDER BY `id` LIMIT (? + ?)")

MergeSort算子包含三部分内容：

sort：表示排序字段以及排列顺序，id ASC表示按照ID字段递增排序，DESC表示递减排序。

offset：表示获取结果集时的偏移量，例子中被参数化了，实际值为5。

fetch：表示最多返回的数据行数。与offset类似，同样是参数化的表示，实际对应的值为10。

Project

Project表示投影操作，即从输入数据中选择部分列输出，或者对某些列进行转换（通过函数或者表达式计算）后输出，当然也可以包含常量。

\> explain select '你好, DRDS', 1 / 2, CURTIME();

Project(你好, DRDS="_UTF-16'你好, DRDS'", 1 / 2="1 / 2", CURTIME()="CURTIME()")

Project的计划中包括每列的列名及其对应的列、值、函数或者表达式。

Filter

Filter表示过滤操作，其中包含一些过滤条件。该算子对输入数据进行过滤，若满足条件，则输出，否则丢弃。如下是一个较复杂的例子，包含了以上介绍的大部分算子。

\> explain select k, avg(id) avg_id from sbtest1 where id > 1000 group by k having avg_id > 1300;

Filter(condition="avg_id > ?1")

 Project(k="k", avg_id="sum_pushed_sum / sum_pushed_count")

  SortAgg(group="k", sum_pushed_sum="SUM(pushed_sum)", sum_pushed_count="SUM(pushed_count)")

   MergeSort(sort="k ASC")

​    LogicalView(tables="[0000-0031].sbtest1_[000-127]", shardCount=128, sql="SELECT `k`, SUM(`id`) AS `pushed_sum`, COUNT(`id`) AS `pushed_count` FROM `sbtest1` WHERE (`id` > ?) GROUP BY `k` ORDER BY `k`")

WHERE id > 1000中的条件没有对应的Filter算子，是因为这个算子最终被下推到了LogicalView中，可以在LogicalView的SQL中看到WHERE (id > ?) 。

#### **Union All与Union Distinct**

顾名思义，Union All对应UNIONALL，Union Distinct对应UNIONDISTINCT。该算子通常有2个或更多个输入，表示将多个输入的数据合并在一起。例如：

\> explain select * From sbtest1 where id > 1000 union distinct select * From sbtest1 where id < 200;

UnionDistinct(concurrent=true)

 Gather(concurrent=true)

  LogicalView(tables="[0000-0031].sbtest1_[000-127]", shardCount=128, sql="SELECT * FROM `sbtest1` WHERE (`id` > ?)")

 Gather(concurrent=true)

  LogicalView(tables="[0000-0031].sbtest1_[000-127]", shardCount=128, sql="SELECT * FROM `sbtest1` WHERE (`id` < ?)")

LogicalModifyView

如上文介绍，LogicalView表示从底层数据源获取数据的算子，与之对应的，LogicalModifyView表示对底层数据源的修改算子，其中也会记录一个SQL语句，该SQL可能是INSERT、UPDATE或者DELETE。

\> explain update sbtest1 set c='Hello, DRDS' where id > 1000;

LogicalModifyView(tables="[0000-0031].sbtest1_[000-127]", shardCount=128, sql="UPDATE `sbtest1` SET `c` = ? WHERE (`id` > ?)"

\> explain delete from sbtest1 where id > 1000;

LogicalModifyView(tables="[0000-0031].sbtest1_[000-127]", shardCount=128, sql="DELETE FROM `sbtest1` WHERE (`id` > ?)")

LogicalModifyView查询计划的内容与LogicalView类似，包括下发的物理分表，分表数以及SQL模版。同样，由于开启了执行计划缓存，对SQL做了参数化处理，SQL模版中的常量会用?替换。

PhyTableOperation

PhyTableOperation表示对某个物理分表直接执行一个操作。

说明：通常情况下，该算子仅用于INSERT语句。但当路由分发分到一个分片时，该算子也会出现在SELECT语句中。

\> explain insert into sbtest1 values(1, 1, '1', '1'),(2, 2, '2', '2');

PhyTableOperation(tables="SYSBENCH_CORONADB_1526954857179TGMMSYSBENCH_CORONADB_VGOC_0000_RDS.[sbtest1_001]", sql="INSERT INTO ? (`id`, `k`, `c`, `pad`) VALUES(?, ?, ?, ?)", params="`sbtest1_001`,1,1,1,1")

PhyTableOperation(tables="SYSBENCH_CORONADB_1526954857179TGMMSYSBENCH_CORONADB_VGOC_0000_RDS.[sbtest1_002]", sql="INSERT INTO ? (`id`, `k`, `c`, `pad`) VALUES(?, ?, ?, ?)", params="`sbtest1_002`,2,2,2,2")

示例中，INSERT插入两行数据，每行数据对应一个PhyTableOperation算子。PhyTableOperation算子的内容包括三部分：

tables：物理表名，仅有唯一一个物理表名。

sql：SQL模版，该SQL模版中表名和常量均被参数化，用?替换，对应的参数在随后的params中给出。

params：SQL模版对应的参数，包括表名和常量。

 

### **JOIN与子查询的优化和执行**

#### **基本概念**

JOIN是SQL查询中常见的操作，逻辑上说，它的语义等价于将两张表做笛卡尔积，然后根据过滤条件保留满足条件的数据。JOIN多数情况下是依赖等值条件做的JOIN，即Equi-Join，用来根据某个特定列的值连接两张表的数据。

子查询是指嵌套在SQL内部的查询块，子查询的结果作为输入，填入到外层查询中，从而用于计算外层查询的结果。子查询可以出现在SQL语句的很多地方，比如在SELECT子句中作为输出的数据，在FROM子句中作为输入的一个视图，在WHERE子句中作为过滤条件等。

#### **JOIN类型**

PolarDB-X支持Inner Join，Left Outer Join和Right Outer Join这3种常见的JOIN类型。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB90F.tmp.jpg) 

下面是几种不同类型JOIN的例子：

/* Inner Join */SELECT * FROM A, B WHERE A.key = B.key;

/* Left Outer Join */SELECT * FROM A LEFT JOIN B ON A.key = B.key;

/* Right Outer Join */SELECT * FROM A RIGHT OUTER JOIN B ON A.key = B.key;

此外，PolarDB-X还支持Semi-Join和Anti-Join。Semi Join和Anti Join无法直接用SQL语句来表示，通常由包含关联项的EXISTS或IN子查询转换得到。

下面是几个Semi-Join和Anti-Join的例子：

/* Semi Join - 1 */SELECT * FROM Emp WHERE Emp.DeptName IN (

  SELECT DeptName FROM Dept

)

 /* Semi Join - 2 */SELECT * FROM Emp WHERE EXISTS (

 SELECT * FROM Dept WHERE Emp.DeptName = Dept.DeptName

)/* Anti Join - 1 */SELECT * FROM Emp WHERE Emp.DeptName NOT IN (

  SELECT DeptName FROM Dept

)

 /* Anti Join - 2 */SELECT * FROM Emp WHERE NOT EXISTS (

 SELECT * FROM Dept WHERE Emp.DeptName = Dept.DeptName

)

#### **JOIN算法**

目前，PolarDB-X支持Nested-Loop Join、Hash Join、Sort-Merge Join和Lookup Join（BKAJoin）等JOIN算法。

Nested-Loop Join (NLJoin)

Nested-Loop Join通常用于非等值的JOIN。它的工作方式如下：

拉取内表（右表，通常是数据量较小的一边）的全部数据，缓存到内存中。

遍历外表数据，对于外表的每行：

对于每一条缓存在内存中的内表数据。

构造结果行，并检查是否满足JOIN条件，如果满足条件则输出。

如下是一个Nested-Loop Join的例子：

\> EXPLAIN SELECT * FROM partsupp, supplier WHERE ps_suppkey < s_suppkey;

NlJoin(condition="ps_suppkey < s_suppkey", type="inner")

 Gather(concurrent=true)

  LogicalView(tables="partsupp_[0-7]", shardCount=8, sql="SELECT * FROM `partsupp` AS `partsupp`")

 Gather(concurrent=true)

  LogicalView(tables="supplier_[0-7]", shardCount=8, sql="SELECT * FROM `supplier` AS `supplier`")

通常来说，Nested-Loop Join是效率最低的JOIN操作，一般只有在JOIN条件不含等值（例如上面的例子）或者内表数据量极小的情况下才会使用。

通过如下Hint可以强制PolarDB-X使用Nested-Loop Join以及确定JOIN顺序：

/*+TDDL:NL_JOIN(outer_table, inner_table)*/ SELECT ...

其中inner_table 和outer_table也可以是多张表的JOIN结果，例如：

/*+TDDL:NL_JOIN((outer_table_a, outer_table_b), (inner_table_c, inner_table_d))*/ SELECT ...

下面其他的Hint也一样。

Hash Join

Hash Join是等值JOIN最常用的算法之一。它的原理如下所示：

拉取内表（右表，通常是数据量较小的一边）的全部数据，写进内存中的哈希表。

遍历外表数据，对于外表的每行：

根据等值条件JOIN Key查询哈希表，取出0-N匹配的行（JOIN Key相同）。

构造结果行，并检查是否满足JOIN条件，如果满足条件则输出。

以下是一个Hash Join的例子：

\> EXPLAIN SELECT * FROM partsupp, supplier WHERE ps_suppkey = s_suppkey;

HashJoin(condition="ps_suppkey = s_suppkey", type="inner")

 Gather(concurrent=true)

  LogicalView(tables="partsupp_[0-7]", shardCount=8, sql="SELECT * FROM `partsupp` AS `partsupp`")

 Gather(concurrent=true)

  LogicalView(tables="supplier_[0-7]", shardCount=8, sql="SELECT * FROM `supplier` AS `supplier`")

Hash Join常出现在JOIN数据量较大的复杂查询、且无法通过索引Lookup来改善，这种情况下Hash Join是最优的选择。例如上面的例子中，partsupp表和supplier表均为全表扫描，数据量较大，适合使用HashJoin。

由于Hash Join的内表需要用于构造内存中的哈希表，内表的数据量一般小于外表。通常优化器可以自动选择出最优的JOIN顺序。如果需要手动控制，也可以通过下面的Hint。

通过如下Hint可以强制PolarDB-X使用Hash Join以及确定JOIN顺序：

/*+TDDL:HASH_JOIN(table_outer, table_inner)*/ SELECT ...

Lookup Join (BKAJoin)

Lookup Join是另一种常用的等值JOIN算法，常用于数据量较小的情况。它的原理如下：

遍历外表（左表，通常是数据量较小的一边）数据，对于外表中的每批（例如1000行）数据。

将这一批数据的JOIN Key拼成一个IN (....)条件，加到内表的查询中。

执行内表查询，得到JOIN匹配的行。

借助哈希表，为外表的每行找到匹配的内表行，组合并输出。

以下是一个Lookup Join (BKAJoin）的例子：

\> EXPLAIN SELECT * FROM partsupp, supplier WHERE ps_suppkey = s_suppkey AND ps_partkey = 123;

BKAJoin(condition="ps_suppkey = s_suppkey", type="inner")

 LogicalView(tables="partsupp_3", sql="SELECT * FROM `partsupp` AS `partsupp` WHERE (`ps_partkey` = ?)")

 Gather(concurrent=true)

  LogicalView(tables="supplier_[0-7]", shardCount=8, sql="SELECT * FROM `supplier` AS `supplier` WHERE (`s_suppkey` IN ('?'))")

Lookup Join通常用于外表数据量较小的情况，例如上面的例子中，左表partsupp由于存在ps_partkey = 123的过滤条件，仅有几行数据。此外，右表的s_suppkey IN ( ... )查询命中了主键索引，这也使得Lookup Join的查询代价进一步降低。

通过如下Hint可以强制PolarDB-X使用LookupJoin以及确定JOIN顺序：

/*+TDDL:BKA_JOIN(table_outer, table_inner)*/ SELECT ...

说明 Lookup Join的内表只能是单张表，不可以是多张表JOIN的结果。

Sort-Merge Join

Sort-Merge Join是另一种等值JOIN算法，它依赖左右两边输入的顺序，必须按JOIN Key排序。它的原理如下：

开始Sort-Merge Join之前，输入端必须排序（借助MergeSort或MemSort）。

比较当前左右表输入的行，并按以下方式操作，不断消费左右两边的输入：

如果左表的JOIN Key较小，则消费左表的下一条数据。

如果右表的JOIN Key较小，则消费右表的下一条数据。

如果左右表JOIN Key相等，说明获得了1条或多条匹配，检查是否满足JOIN条件并输出。

以下是一个Sort-Merge Join的例子：

\> EXPLAIN SELECT * FROM partsupp, supplier WHERE ps_suppkey = s_suppkey ORDER BY s_suppkey;

SortMergeJoin(condition="ps_suppkey = s_suppkey", type="inner")

 MergeSort(sort="ps_suppkey ASC")   LogicalView(tables="QIMU_0000_GROUP,QIMU_0001_GROUP.partsupp_[0-7]", shardCount=8, sql="SELECT * FROM `partsupp` AS `partsupp` ORDER BY `ps_suppkey`")

 MergeSort(sort="s_suppkey ASC")

  LogicalView(tables="QIMU_0000_GROUP,QIMU_0001_GROUP.supplier_[0-7]", shardCount=8, sql="SELECT * FROM `supplier` AS `supplier` ORDER BY `s_suppkey`")

注意上面执行计划中的 MergeSort算子以及下推的ORDER BY，这保证了Sort-Merge Join两边的输入按JOIN Key即s_suppkey (ps_suppkey)排序。

Sort-Merge Join由于需要额外的排序步骤，通常Sort-Merge Join并不是最优的。但是，某些情况下客户端查询恰好也需要按JOIN Key排序（上面的例子），这时候使用Sort-Merge Join是较优的选择。

通过如下Hint可以强制PolarDB-X使用Sort-Merge Join：

/*+TDDL:SORT_MERGE_JOIN(table_a, table_b)*/ SELECT ...

#### **JOIN顺序**

在多表连接的场景中，优化器的一个很重要的任务是决定各个表之间的连接顺序，因为不同的连接顺序会影响中间结果集的大小，进而影响到计划整体的执行代价。

例如，对于4张表JOIN（暂不考虑下推的情形），JOIN Tree可以有如下3种形式，同时表的排列又有4! = 24种，一共有72种可能的JOIN顺序。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB910.tmp.jpg) 

给定N个表的JOIN，PolarDB-X采用自适应的策略生成最佳JOIN计划：

当（未下推的）N较小时，采取Bushy枚举策略，会在所有JOIN顺序中选出最优的计划。

当（未下推的）表的数量较多时，采取Zig-Zag（锯齿状）或Left-Deep（左深树）的枚举策略，选出最优的Zig-Zag或Left-Deep执行计划，以减少枚举的次数和代价。

PolarDB-X使用基于代价的优化器（Cost-based Optimizer，CBO）选择出总代价最低的JOIN 顺序。

此外，各个JOIN算法对左右输入也有不同的偏好，例如，Hash Join中右表作为内表用于构建哈希表，因此应当将较小的表置于右侧。这些也同样会在CBO中被考虑到。

#### **子查询**

根据是否存在关联项，子查询可以分为非关联子查询和关联子查询。非关联子查询是指该子查询的执行不依赖外部查询的变量，这种子查询一般只需要计算一次；而关联子查询中存在引用自外层查询的变量，逻辑上，这种子查询需要每次带入相应的变量、计算多次。

/* 例子：非关联子查询 */SELECT * FROM lineitem WHERE l_partkey IN (SELECT p_partkey FROM part);

/* 例子：关联子查询（l_suppkey 是关联项） */SELECT * FROM lineitem WHERE l_partkey IN (SELECT ps_partkey FROM partsupp WHERE ps_suppkey = l_suppkey);

PolarDB-X子查询支持绝大多数的子查询写法，具体参见[SQL使用限制](#multiTask1408)。

对于多数常见的子查询形式，PolarDB-X可以将其改写为高效的SemiJoin或类似的基于JOIN的计算方式。这样做的好处是显而易见的。当数据量较大时，无需真正带入不同参数循环迭代，大大降低了执行代价。这种查询改写技术称为子查询的去关联化（Unnesting）。

下面是2个子查询去关联化的例子，可以看到执行计划中使用JOIN代替了子查询。

\> EXPLAIN SELECT p_partkey, (

   SELECT COUNT(ps_partkey) FROM partsupp WHERE ps_suppkey = p_partkey

   ) supplier_count FROM part;

Project(p_partkey="p_partkey", supplier_count="CASE(IS NULL($10), 0, $9)", cor=[$cor0])

 HashJoin(condition="p_partkey = ps_suppkey", type="left")

  Gather(concurrent=true)

   LogicalView(tables="part_[0-7]", shardCount=8, sql="SELECT * FROM `part` AS `part`")

  Project(count(ps_partkey)="count(ps_partkey)", ps_suppkey="ps_suppkey", count(ps_partkey)2="count(ps_partkey)")

   HashAgg(group="ps_suppkey", count(ps_partkey)="SUM(count(ps_partkey))")

​    Gather(concurrent=true)

​     LogicalView(tables="partsupp_[0-7]", shardCount=8, sql="SELECT `ps_suppkey`, COUNT(`ps_partkey`) AS `count(ps_partkey)` FROM `partsupp` AS `partsupp` GROUP BY `ps_suppkey`")

\> EXPLAIN SELECT p_partkey, (

   SELECT COUNT(ps_partkey) FROM partsupp WHERE ps_suppkey = p_partkey

   ) supplier_count FROM part;

 

Project(p_partkey="p_partkey", supplier_count="CASE(IS NULL($10), 0, $9)", cor=[$cor0])

 HashJoin(condition="p_partkey = ps_suppkey", type="left")

  Gather(concurrent=true)

   LogicalView(tables="part_[0-7]", shardCount=8, sql="SELECT * FROM `part` AS `part`")

  Project(count(ps_partkey)="count(ps_partkey)", ps_suppkey="ps_suppkey", count(ps_partkey)2="count(ps_partkey)")

   HashAgg(group="ps_suppkey", count(ps_partkey)="SUM(count(ps_partkey))")

​    Gather(concurrent=true)

​     LogicalView(tables="partsupp_[0-7]", shardCount=8, sql="SELECT `ps_suppkey`, COUNT(`ps_partkey`) AS `count(ps_partkey)` FROM `partsupp` AS `partsupp` GROUP BY `ps_suppkey`")

某些少见情形下，PolarDB-X无法将子查询进行去关联化，这时候会采用迭代执行的方式。如果外层查询数据量很大，迭代执行可能会非常慢。

下面这个例子中，由于OR l_partkey < 50的存在，导致子查询无法被去关联化，因而采用了迭代执行：

\> EXPLAIN SELECT * FROM lineitem WHERE l_partkey IN (SELECT ps_partkey FROM partsupp WHERE ps_suppkey = l_suppkey) OR l_partkey IS NOT

Filter(condition="IS(in,[$1])[29612489] OR l_partkey < ?0")

 Gather(concurrent=true)

  LogicalView(tables="QIMU_0000_GROUP,QIMU_0001_GROUP.lineitem_[0-7]", shardCount=8, sql="SELECT * FROM `lineitem` AS `lineitem`")

 

\>> individual correlate subquery : 29612489

Gather(concurrent=true)

 LogicalView(tables="QIMU_0000_GROUP,QIMU_0001_GROUP.partsupp_[0-7]", shardCount=8, sql="SELECT * FROM (SELECT `ps_partkey` FROM `partsupp` AS `partsupp` WHERE (`ps_suppkey` = `l_suppkey`)) AS `t0` WHERE (((`l_partkey` = `ps_partkey`) OR (`l_partkey` IS NULL)) OR (`ps_partkey` IS NULL))")

这种情形下，建议改写SQL去掉子查询的OR条件。

 

### **优化聚合函数与排序**

#### **基本概念**

聚合操作（Aggregate，简称Agg）语义为按照GROUP BY指定列对输入数据进行聚合的计算，或者不分组、对所有数据进行聚合的计算。PolarDB-X支持如下聚合函数：

COUNT

SUM

AVG

MAX

MIN

BIT_OR

BIT_XOR

GROUP_CONCAT

排序操作（Sort）语义为按照指定的ORDER BY列对输入进行排序。

#### **聚合（Agg）**

聚合（Agg）由两种主要的算子HashAgg和SortAgg实现。

##### HashAgg

HashAgg利用哈希表实现聚合：

根据输入行的分组列的值，通过Hash找到对应的分组。

按照指定的聚合函数，对该行进行聚合计算。

重复以上步骤直到处理完所有的输入行，最后输出聚合结果。

\> explain select count(*) from t1 join t2 on t1.id = t2.id group by t1.name,t2.name;

 

Project(count(*)="count(*)")

 HashAgg(group="name,name0", count(*)="COUNT()")

  BKAJoin(condition="id = id", type="inner")

   Gather(concurrent=true)

​    LogicalView(tables="t1", shardCount=2, sql="SELECT `id`, `name` FROM `t1` AS `t1`")

   Gather(concurrent=true)

​    LogicalView(tables="t2_[0-3]", shardCount=4, sql="SELECT `id`, `name` FROM `t2` AS `t2` WHERE (`id` IN ('?'))")

Explain结果中，HashAgg算子还包含以下关键信息：

group：表示GROUP BY字段，示例中为name,name0分别引用t1,t2表的name列，当存在相同别名会通过后缀数字区分 。

聚合函数：等号（=） 前为聚合函数对应的输出列名，其后为对应的计算方法。示例中 count(*)="COUNT()" ，第一个 count(*) 对应输出的列名，随后的COUNT()表示对其输入数据进行计数。

HashAgg对应可以通过Hint来关闭：	/*+TDDL:cmd_extra(ENABLE_HASH_AGG=false)*/。

##### SortAgg

SortAgg在输入数据已按分组列排序的情况，对各个分组依次完成聚合。

保证输入按指定的分组列排序（例如，可能会看到 MergeSort 或 MemSort）。

逐行读入输入数据，如果分组与当前分组相同，则对其进行聚合计算。

否则，如果分组与当前分组不同，则输出当前分组上的聚合结果。

相比 HashAgg，SortAgg 每次只要处理一个分组，内存消耗很小；相对的，	HashAgg 需要把所有分组存储在内存中，需要消耗较多的内存。

\> explain select count(*) from t1 join t2 on t1.id = t2.id group by t1.name,t2.name order by t1.name, t2.name;

 

Project(count(*)="count(*)")

 MemSort(sort="name ASC,name0 ASC")

  HashAgg(group="name,name0", count(*)="COUNT()")

   BKAJoin(condition="id = id", type="inner")

​    Gather(concurrent=true)

​     LogicalView(tables="t1", shardCount=2, sql="SELECT `id`, `name` FROM `t1` AS `t1`")

​    Gather(concurrent=true)

​     LogicalView(tables="t2_[0-3]", shardCount=4, sql="SELECT `id`, `name` FROM `t2` AS `t2` WHERE (`id` IN ('?'))")

SortAgg对应可以通过Hint来关闭：	/*+TDDL:cmd_extra(ENABLE_SORT_AGG=false)*/。

两阶段聚合优化

两阶段聚合，即通过将Agg拆分为部分聚合（Partial Agg）和最终聚合（Final Agg）的两个阶段，先对部分结果集做聚合，然后将这些部分聚合结果汇总，得到整体聚合的结果。

例如下面的 SQL 中，HashAgg 中拆分出的部分聚合（PartialAgg）会被下推至MySQL上的各个分表，而其中的AVG函数也被拆分成 SUM和 COUNT 以实现两阶段的计算：

\> explain select avg(age) from t2 group by name

 

Project(avg(age)="sum_pushed_sum / sum_pushed_count")

 HashAgg(group="name", sum_pushed_sum="SUM(pushed_sum)", sum_pushed_count="SUM(pushed_count)")

  Gather(concurrent=true)

   LogicalView(tables="t2_[0-3]", shardCount=4, sql="SELECT `name`, SUM(`age`) AS `pushed_sum`, COUNT(`age`) AS `pushed_count` FROM `t2` AS `t2` GROUP BY `name`")

两阶段聚合的优化能大大减少数据传输量、提高执行效率。

#### **排序（Sort）**

PolarDB-X中的排序算子主要包括 MemSort、TopN，以及 MergeSort。

##### MemSort

PolarDB-X中的通用的排序实现为MemSort算子，即内存中运行快速排序（Quick Sort）算法。

下面是一个用到MemSort算子的例子：

\> explain select t1.name from t1 join t2 on t1.id = t2.id order by t1.name,t2.name;

Project(name="name")

 MemSort(sort="name ASC,name0 ASC")

  Project(name="name", name0="name0")

   BKAJoin(condition="id = id", type="inner")

​    Gather(concurrent=true)

​     LogicalView(tables="t1", shardCount=2, sql="SELECT `id`, `name` FROM `t1` AS `t1`")

​    Gather(concurrent=true)

​     LogicalView(tables="t2_[0-3]", shardCount=4, sql="SELECT `id`, `name` FROM `t2` AS `t2` WHERE (`id` IN ('?'))")

##### TopN

当SQL中ORDER BY和LIMIT一起出现时，Sort算子和Limit算子会合并成TopN算子。

TopN算子维护一个最大或最小堆，按照排序键的值，堆中始终保留最大或最小的N行数据。当处理完全部的输入数据时，堆中留下的N个行（或小于N个）就是需要的结果。

下面是一个用到 TopN 算子的例子：

\> explain select t1.name from t1 join t2 on t1.id = t2.id order by t1.name,t2.name limit 10;

Project(name="name")

 TopN(sort="name ASC,name0 ASC", offset=0, fetch=?0)

  Project(name="name", name0="name0")

   BKAJoin(condition="id = id", type="inner")

​    Gather(concurrent=true)

​     LogicalView(tables="t1", shardCount=2, sql="SELECT `id`, `name` FROM `t1` AS `t1`")

​    Gather(concurrent=true)

​     LogicalView(tables="t2_[0-3]", shardCount=4, sql="SELECT `id`, `name` FROM `t2` AS `t2` WHERE (`id` IN ('?'))")

 

##### MergeSort

通常，只要语义允许，SQL中的排序操作会被下推到MySQL上执行，而PolarDB-X执行层只做最后的归并操作，即MergeSort。严格来说，MergeSort 不仅仅是排序，更是一种数据重分布算子（类似 Gather）。

下面的SQL是对t1表进行排序，经过PolarDB-X查询优化器的优化，Sort算子被下推至各个MySQL分片中执行，最终只在上层做归并操作。

\> explain select name from t1 order by name;

MergeSort(sort="name ASC")

 LogicalView(tables="t1", shardCount=2, sql="SELECT `name` FROM `t1` AS `t1` ORDER BY `name`")

相比 MemSort，MergeSort 算法可以减少PolarDB-X层的内存消耗，并充分利用 MySQL 层的计算能力。

 

#### **优化组合的例子**

下面是一个组合优化的例子，在这个例子中，用到了以下优化规则：

Agg下推穿过Join

Join算法选择为SortMergeJoin

Agg算法选择为SortAgg

SortMergeJoin中需要的排序利用了SortAgg输出的有序

两阶段Agg

Agg下推

Sort下推

\>  explain select count(*) from t1 join t2 on t1.name = t2.name group by t1.name;

 

Project(count(*)="count(*) * count(*)0")

 SortMergeJoin(condition="name = name", type="inner")

  SortAgg(group="name", count(*)="SUM(count(*))")

   MergeSort(sort="name ASC")

​    LogicalView(tables="t1", shardCount=2, sql="SELECT `name`, COUNT(*) AS `count(*)` FROM `t1` AS `t1` GROUP BY `name` ORDER BY `name`")

  SortAgg(group="name", count(*)="SUM(count(*))")

   MergeSort(sort="name ASC")

​    LogicalView(tables="t2_[0-3]", shardCount=4, sql="SELECT `name`, COUNT(*) AS `count(*)` FROM `t2` AS `t2` GROUP BY `name` ORDER BY `name`")

 

### **执行计划管理**

#### **背景信息**

对于每一条SQL，优化器都会生成相应执行计划。但是很多情况下，应用请求的SQL都是重复的（仅参数不同），参数化之后的SQL完全相同。这时，可以按照参数化之后的SQL构造一个缓存，将除了参数以外的各种信息（比如执行计划）缓存起来，称为执行计划缓存（Plan Cache）。

另一方面，对于较复杂的查询（例如涉及到多个表的Join），为了使其执行计划能保持相对稳定，不因为版本升级等原因发生变化。执行计划管理（Plan Management）为每个SQL记录一组执行计划，该执行计划会被持久化地保存，即使版本升级也会保留。

#### **工作流程概览**

当PolarDB-X收到一条查询SQL时，会经历以下流程：

对查询SQL进行参数化处理，将所有参数替换为占位符?。

以参数化的SQL作为Key，查找执行计划缓存中是否有缓存；如果没有，则调用优化器进行优化。

如果该SQL是简单查询，则直接执行，跳过执行计划管理相关步骤。

如果该SQL是复杂查询，则使用基线（Baseline）中固化的执行计划；如果有多个，则选择代价最低的那个。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB920.tmp.jpg) 

#### **执行计划缓存**

PolarDB-X默认开启执行计划缓存功能。EXPLAIN结果中的HitCache表示当前SQL是否命中执行计划缓存。开启执行计划缓存后，PolarDB-X会对SQL做参数化处理，参数化会将SQL中的常量用占位符?替换，并构建出相应的参数列表。在执行计划中也可以看到LogicalView算子的SQL中含有?。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB921.tmp.jpg) 

#### **执行计划管理**

对于复杂SQL，经过执行计划缓存之后，还会经过执行计划管理流程。

执行计划缓存和执行计划管理都是采用参数化后的SQL作为Key来执行计划。执行计划缓存中会缓存所有SQL的执行计划，而执行计划管理仅对复杂查询SQL进行处理。

由于受到具体参数的影响，SQL模版和最优的执行计划并非一一对应的。

在执行计划管理中，每一条SQL对应一个基线，每个基线中包含一个或多个执行计划。实际使用中，会根据当时的参数选择其中代价最小的执行计划来执行。

计划选择

当执行计划缓存中的执行计划走进执行计划管理时，SPM会操作一个流程判断该执行计划是否是已知的，是已知的话，是否代价是最小的；不是已知的话，是否需要执行一下以判断该执行计划的优化程度。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB922.tmp.jpg) 

运维指令

PolarDB-X提供了丰富的指令集用于管理执行计划，语法如下：

BASELINE (LOAD|PERSIST|CLEAR|VALIDATE|LIST|DELETE) [Signed Integer,Signed Integer....]

BASELINE (ADD|FIX) SQL (HINT Select Statemtnt)

BASELINE (ADD|FIX) SQL <HINT> <Select Statement>：将SQL以HINT修复过后的执行计划记录固定下来。

BASELINE LOAD：将系统表中指定的基线信息刷新到内存并使其生效。

BASELINE LOAD_PLAN：将系统表中指定的执行计划信息刷新到内存并使其生效。

BASELINE LIST：列出当前所有的基线信息。BASELINE PERSIST：将指定的基线落盘。

BASELINE PERSIST_PLAN：将指定的执行计划落盘。

BASELINE CLEAR：内存中清理某个基线。

BASELINE CLEAR_PLAN：内存中清理某个执行计划。

BASELINE DELETE：磁盘中删除某个基线。

BASELINE DELETE_PLAN：磁盘中删除某个执行计划。

***\*执行计划调优实战\****

数据发生变化或PolarDB-X优化器引擎升级后，针对同一条SQL，有可能会出现更好的执行计划。SPM在自动演化时会将CBO优化自动发现的更优执行计划加入到SQL的基线中。除此以外，您也可以通过SPM的指令主动优化执行计划。

例如以下的SQL：

SELECT *FROM lineitem JOIN part ON l_partkey=p_partkeyWHERE p_name LIKE '%green%';

正常EXPLAIN发现该SQL生成的执行计划使用的是Hash Join，并且在Baseline List的基线中，该SQL仅有这一个执行计划：

mysql> explain select * from lineitem join part on l_partkey=p_partkey where p_name like '%geen%';

+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

| LOGICAL PLAN                                                                                                                                                                          |

+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

| Gather(parallel=true)                                                                                                                                                                      |

|  ParallelHashJoin(condition="l_partkey = p_partkey", type="inner")                                                                                                                                               |

|   LogicalView(tables="[00-03].lineitem", shardCount=4, sql="SELECT `l_orderkey`, `l_partkey`, `l_suppkey`, `l_linenumber`, `l_quantity`, `l_extendedprice`, `l_discount`, `l_tax`, `l_returnflag`, `l_linestatus`, `l_shipdate`, `l_commitdate`, `l_receiptdate`, `l_shipinstruct`, `l_shipmode`, `l_comment` FROM `lineitem` AS `lineitem`", parallel=true) |

|   LogicalView(tables="[00-03].part", shardCount=4, sql="SELECT `p_partkey`, `p_name`, `p_mfgr`, `p_brand`, `p_type`, `p_size`, `p_container`, `p_retailprice`, `p_comment` FROM `part` AS `part` WHERE (`p_name` LIKE ?)", parallel=true)                                                           |

| HitCache:true                                                                                                                                                                          |

|                                                                                                                                                                                 |

|                                                                                                                                                                                 |

+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+7 rows in set (0.06 sec)

mysql> baseline list;

+-------------+--------------------------------------------------------------------------------+------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------+----------+

| BASELINE_ID | PARAMETERIZED_SQL                                | PLAN_ID   | EXTERNALIZED_PLAN                                                                                                                                                                                                                                                                                                                                            | FIXED | ACCEPTED |

+-------------+--------------------------------------------------------------------------------+------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------+----------+

|  -399023558 | SELECT *FROM lineitem

  JOIN part ON l_partkey = p_partkeyWHERE p_name LIKE ? | -935671684 |

Gather(parallel=true)

 ParallelHashJoin(condition="l_partkey = p_partkey", type="inner")

  LogicalView(tables="[00-03].lineitem", shardCount=4, sql="SELECT `l_orderkey`, `l_partkey`, `l_suppkey`, `l_linenumber`, `l_quantity`, `l_extendedprice`, `l_discount`, `l_tax`, `l_returnflag`, `l_linestatus`, `l_shipdate`, `l_commitdate`, `l_receiptdate`, `l_shipinstruct`, `l_shipmode`, `l_comment` FROM `lineitem` AS `lineitem`", parallel=true)

  LogicalView(tables="[00-03].part", shardCount=4, sql="SELECT `p_partkey`, `p_name`, `p_mfgr`, `p_brand`, `p_type`, `p_size`, `p_container`, `p_retailprice`, `p_comment` FROM `part` AS `part` WHERE (`p_name` LIKE ?)", parallel=true)

 |   0 |     1 |

+-------------+--------------------------------------------------------------------------------+------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------+----------+1 row in set (0.02 sec)

假如这个SQL在某些条件下采用BKA Join（Lookup Join）会有更好的性能，那么首先需要想办法利用HINT引导PolarDB-X生成符合预期的执行计划。BKA Join的HINT格式为：

/*+TDDL:BKA_JOIN(lineitem, part)*/

通过EXPLAIN [HINT] [SQL]观察出来的执行计划是否符合预期：

mysql> explain /*+TDDL:bka_join(lineitem, part)*/ select * from lineitem join part on l_partkey=p_partkey where p_name like '%geen%';

+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

| LOGICAL PLAN                                                                                                                                                                          |

+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

| Gather(parallel=true)                                                                                                                                                                      |

|  ParallelBKAJoin(condition="l_partkey = p_partkey", type="inner")                                                                                                                                               |

|   LogicalView(tables="[00-03].lineitem", shardCount=4, sql="SELECT `l_orderkey`, `l_partkey`, `l_suppkey`, `l_linenumber`, `l_quantity`, `l_extendedprice`, `l_discount`, `l_tax`, `l_returnflag`, `l_linestatus`, `l_shipdate`, `l_commitdate`, `l_receiptdate`, `l_shipinstruct`, `l_shipmode`, `l_comment` FROM `lineitem` AS `lineitem`", parallel=true) |

|   Gather(concurrent=true)                                                                                                                                                                   |

|    LogicalView(tables="[00-03].part", shardCount=4, sql="SELECT `p_partkey`, `p_name`, `p_mfgr`, `p_brand`, `p_type`, `p_size`, `p_container`, `p_retailprice`, `p_comment` FROM `part` AS `part` WHERE (`p_name` LIKE ?)")                                                                 |

| HitCache:false                                                                                                                                                                         |

|                                                                                                                                                                                 |

|                                                                                                                                                                                 |

+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

8 rows in set (0.14 sec)

注意此时由于Hint的干预，Join的算法已修正为BKA Join。但是这并不会对基线造成变动，如果想以后每次遇到这条SQL都使用上面的计划，还需要将其加入到基线中。

可以采用执行计划管理的Baseline Add指令为该SQL增加一个执行计划。这样就会同时有两套执行计划存在于该SQL的基线中，CBO优化器会根据代价选择一个执行计划执行。

mysql> baseline add sql /*+TDDL:bka_join(lineitem, part)*/ select * from lineitem join part on l_partkey=p_partkey where p_name like '%geen%';

+-------------+--------+

| BASELINE_ID | STATUS |

+-------------+--------+

|  -399023558 | OK   |

+-------------+--------+1 row in set (0.09 sec)

mysql> baseline list;

+-------------+--------------------------------------------------------------------------------+-------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------+----------+

| BASELINE_ID | PARAMETERIZED_SQL                                | PLAN_ID   | EXTERNALIZED_PLAN                                                                                                                                                                                                                                                                                                                                                   | FIXED | ACCEPTED |

+-------------+--------------------------------------------------------------------------------+-------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------+----------+

|  -399023558 | SELECT *FROM lineitem

  JOIN part ON l_partkey = p_partkeyWHERE p_name LIKE ? | -1024543942 |

Gather(parallel=true)

 ParallelBKAJoin(condition="l_partkey = p_partkey", type="inner")

  LogicalView(tables="[00-03].lineitem", shardCount=4, sql="SELECT `l_orderkey`, `l_partkey`, `l_suppkey`, `l_linenumber`, `l_quantity`, `l_extendedprice`, `l_discount`, `l_tax`, `l_returnflag`, `l_linestatus`, `l_shipdate`, `l_commitdate`, `l_receiptdate`, `l_shipinstruct`, `l_shipmode`, `l_comment` FROM `lineitem` AS `lineitem`", parallel=true)

  Gather(concurrent=true)

   LogicalView(tables="[00-03].part", shardCount=4, sql="SELECT `p_partkey`, `p_name`, `p_mfgr`, `p_brand`, `p_type`, `p_size`, `p_container`, `p_retailprice`, `p_comment` FROM `part` AS `part` WHERE (`p_name` LIKE ?)")

 |   0 |     1 |

|  -399023558 | SELECT *FROM lineitem

  JOIN part ON l_partkey = p_partkeyWHERE p_name LIKE ? |  -935671684 |

Gather(parallel=true)

 ParallelHashJoin(condition="l_partkey = p_partkey", type="inner")

  LogicalView(tables="[00-03].lineitem", shardCount=4, sql="SELECT `l_orderkey`, `l_partkey`, `l_suppkey`, `l_linenumber`, `l_quantity`, `l_extendedprice`, `l_discount`, `l_tax`, `l_returnflag`, `l_linestatus`, `l_shipdate`, `l_commitdate`, `l_receiptdate`, `l_shipinstruct`, `l_shipmode`, `l_comment` FROM `lineitem` AS `lineitem`", parallel=true)

  LogicalView(tables="[00-03].part", shardCount=4, sql="SELECT `p_partkey`, `p_name`, `p_mfgr`, `p_brand`, `p_type`, `p_size`, `p_container`, `p_retailprice`, `p_comment` FROM `part` AS `part` WHERE (`p_name` LIKE ?)", parallel=true)

​        |   0 |     1 |

+-------------+--------------------------------------------------------------------------------+-------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+-------+----------+2 rows in set (0.03 sec)

通过以上Baseline List指令展示出来的结果，可以看到基于BKA_JOIN的执行计划已增加到该 SQL的基线中。此时EXPLAIN这条SQL，发现随SQL中p_name LIKE ? 条件变化，PolarDB-X会选择不同的执行计划。如果想让PolarDB-X固定使用上述的执行计划（而非在两个中挑选一个），可以采用Baseline Fix指令强制PolarDB-X走指定的执行计划。

mysql> baseline fix sql /*+TDDL:bka_join(lineitem, part)*/ select * from lineitem join part on l_partkey=p_partkey where p_name like '%geen%';

+-------------+--------+

| BASELINE_ID | STATUS |

+-------------+--------+

|  -399023558 | OK   |

+-------------+--------+1 row in set (0.07 sec)

mysql> baseline list\G

*************************** 1. row ***************************

   BASELINE_ID: -399023558

PARAMETERIZED_SQL: SELECT *

FROM lineitem

  JOIN part ON l_partkey = p_partkey

WHERE p_name LIKE ?

​     PLAN_ID: -1024543942

EXTERNALIZED_PLAN:

Gather(parallel=true)

 ParallelBKAJoin(condition="l_partkey = p_partkey", type="inner")

  LogicalView(tables="[00-03].lineitem", shardCount=4, sql="SELECT `l_orderkey`, `l_partkey`, `l_suppkey`, `l_linenumber`, `l_quantity`, `l_extendedprice`, `l_discount`, `l_tax`, `l_returnflag`, `l_linestatus`, `l_shipdate`, `l_commitdate`, `l_receiptdate`, `l_shipinstruct`, `l_shipmode`, `l_comment` FROM `lineitem` AS `lineitem`", parallel=true)

  Gather(concurrent=true)

   LogicalView(tables="[00-03].part", shardCount=4, sql="SELECT `p_partkey`, `p_name`, `p_mfgr`, `p_brand`, `p_type`, `p_size`, `p_container`, `p_retailprice`, `p_comment` FROM `part` AS `part` WHERE (`p_name` LIKE ?)")

​      FIXED: 1

​     ACCEPTED: 1

*************************** 2. row ***************************

   BASELINE_ID: -399023558

PARAMETERIZED_SQL: SELECT *

FROM lineitem

  JOIN part ON l_partkey = p_partkey

WHERE p_name LIKE ?

​     PLAN_ID: -935671684

EXTERNALIZED_PLAN:

Gather(parallel=true)

 ParallelHashJoin(condition="l_partkey = p_partkey", type="inner")

  LogicalView(tables="[00-03].lineitem", shardCount=4, sql="SELECT `l_orderkey`, `l_partkey`, `l_suppkey`, `l_linenumber`, `l_quantity`, `l_extendedprice`, `l_discount`, `l_tax`, `l_returnflag`, `l_linestatus`, `l_shipdate`, `l_commitdate`, `l_receiptdate`, `l_shipinstruct`, `l_shipmode`, `l_comment` FROM `lineitem` AS `lineitem`", parallel=true)

  LogicalView(tables="[00-03].part", shardCount=4, sql="SELECT `p_partkey`, `p_name`, `p_mfgr`, `p_brand`, `p_type`, `p_size`, `p_container`, `p_retailprice`, `p_comment` FROM `part` AS `part` WHERE (`p_name` LIKE ?)", parallel=true)

​      FIXED: 0

​     ACCEPTED: 12 rows in set (0.01 sec)

Baseline Fix指令执行完后，可以看到BKA Join执行计划的Fix状态位已被置为1。此时就算不加HINT，任意条件下Explain这条SQL，都一定会采用这个执行计划。

mysql> explain select * from lineitem join part on l_partkey=p_partkey where p_name like '%green%';

+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

| LOGICAL PLAN                                                                                                                                                                          |

+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

| Gather(parallel=true)                                                                                                                                                                      |

|  ParallelBKAJoin(condition="l_partkey = p_partkey", type="inner")                                                                                                                                               |

|   LogicalView(tables="[00-03].lineitem", shardCount=4, sql="SELECT `l_orderkey`, `l_partkey`, `l_suppkey`, `l_linenumber`, `l_quantity`, `l_extendedprice`, `l_discount`, `l_tax`, `l_returnflag`, `l_linestatus`, `l_shipdate`, `l_commitdate`, `l_receiptdate`, `l_shipinstruct`, `l_shipmode`, `l_comment` FROM `lineitem` AS `lineitem`", parallel=true) |

|   Gather(concurrent=true)                                                                                                                                                                   |

|    LogicalView(tables="[00-03].part", shardCount=4, sql="SELECT `p_partkey`, `p_name`, `p_mfgr`, `p_brand`, `p_type`, `p_size`, `p_container`, `p_retailprice`, `p_comment` FROM `part` AS `part` WHERE (`p_name` LIKE ?)")                                                                 |

| HitCache:true                                                                                                                                                                          |

+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

8 rows in set (0.01 sec)

 

## 智能索引

索引优化通常需要依赖运维或开发人员对数据库引擎内部优化和执行原理的深入理解。为优化体验和降低操作门槛，PolarDB-X推出了基于代价优化器的索引推荐功能，可根据查询语句分析并推荐索引，帮助您降低查询耗时，提升数据库性能。

***\*注意事项\*******\*：\****

索引推荐功能仅针对您当前指定的SQL查询语句进行分析与推荐。在根据推荐的信息创建索引前，您需要评估创建该索引对其它查询的影响。

 

# HTAP

## 背景信息

PolarDB-X 2.0解决了OLTP数据库面对海量数据下的存储、并发方面的扩展性问题，但由于缺失多机并行查询加速能力和列存储等能力，无法满足对实时性计算和复杂查询都要求较高的在线业务场景，同时还面临着ETL（Extract-Transform-Loa）数据异步传输链路运维复杂度高、数据一致性和查询实时性无法严格保障等挑战。

PolarDB-X 2.0由多个节点构成计算、存储内核一体化实例，在共用一份数据的基础上避免了ETL（Extract-Transform-Load）操作，实现了在线高并发OLTP联机事务处理以及OLAP海量数据分析，即HTAP。

## 技术架构

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB933.tmp.jpg) 

***\*MPP和只读资源\****

PolarDB-X 2.0通过多组DRDS计算节点提供大规模多级并行处理能力（Massively Parallel Processing，简称MPP），针对计算节点进行Scale-out完成MPP处理能力的线性扩展。

同时通过AiSQL三节点基于Paxos构建Row-based只读Learner配合DRDS只读计算节点，提供TP、AP资源链路隔离机制。

***\*连接地址和数据源\****

PolarDB-X 2.0TP和AP请求提供了统一连接地址（Endpoint），保持SQL语义以及兼容性完全一致。

主实例提供HTAP集群地址（Cluster Endpoint）面向在线通用业务场景，提供了智能读写分离和强一致读特性。只读实例提供HTAP只读地址（Private Read Only Endpoint），专注离线拖数、跑批等资源链路隔离场景，确保只读资源可被独享。

若PolarDB-X 2.0已[添加只读实例](#task-1953878)，默认将AP workload转发至只读实例进行MPP并行加速；若未添加任何只读实例，则转发至主实例内部所有计算节点完成执行。

 

## 优势

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wpsB934.tmp.jpg) 

一份数据，一个数据源，一个Endpoint即可覆盖TP和AP业务场景，降低数据库选型成本。

支持线性水平扩展提升HTAP复杂查询加速能力，通过横向增加只读实例即可提高复杂查询速率。

避免数据异步传输，满足全局数据查询一致性，提升业务实时分析效率。

资源链路隔离，确保在线核心业务链路稳定性。

 

## 典型业务场景

PolarDB-X 2.0可满足如下典型业务场景需求：

1、在线业务联机查询

少量逻辑表关联、排序、聚合，涉及数据少量。

并发较高，实时性要求高，严格一致性要求。

2、报表BI（Business Intelligence）分析查询

多张大表关联、排序、聚合、子查询以及宽表统计查询，涉及海量数据。

数据一致性、实时性要求不高。

3、离线拖数跑批查询

大批量数据离线抽取、全表扫描、离线归档、T+1离线跑批任务，涉及多张大表，SQL较复杂。

物理资源链路需隔离，不能影响在线业务，少量业务存在INSERT或SELECT需求。

数据一致性、实时性要求不高。

4、Adhoc交互式即系查询

后台运营场景交互式标签即系查询，少量并发，少量表关联聚合，WHERE条件不固定。

数据一致性、实时性要求高。

 

# 运维管理

## 监控告警

## 高危类SQL自动保护

## 待处理事件

# 未来发展