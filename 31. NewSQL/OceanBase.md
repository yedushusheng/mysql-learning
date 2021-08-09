# 背景

## 集中式数据库

传统集中式数据库面临的挑战：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CB7.tmp.jpg) 

注：其实最根本的缺点还是成本高，扩展可以通过增加存储，但是那样也只是垂直扩展，成本非常高。

随着云计算和大数据时代的到来，业务流量和数据量呈井喷式的发展，对数据库的要求越来越高，传统数据库面临很多的挑战：
	第一，它们普遍采用了基于“单点高端硬件”的架构，对硬件要求很高，部署成本也很高，后期维护这些高端硬件也需要耗费大量的人力物力。除此之外，还有一个问题，随着业务量的增大，它只能纵向扩展，无法横向扩展。比如，开始业务量增加的不多，可以通过扩展数据库服务器的CPU/内存/硬盘来解决，但单机容量达到上限后，那么此时再想扩容，只能将PC服务器替换为高端的小型机等专有的硬件。这些设备的采购及服务成本每年基本都是上亿级别，不是一般用户能够承受的，更重要的问题是，这些高端硬件也是有性能上限的。

第二，传统数据库虽然能很好的应对高并发查询场景，但一旦需要访问的并发量太大，比如双十一期间的短期的大流量，突破了单点设备所能提供的存储容量上限或者计算能力上限，剧烈的资源争抢就会导致整体性能显著下降。因此，传统数据库比较适合处理数据量和访问量都比较平稳、比较有限的场景，比较难应对数据量和访问量都快速增长的场景。

## 分库分表中间件

使用数据库中间件分库分表方案依然有短板

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CB8.tmp.jpg) 

为了解决上述问题，同时也为了降低成本，传统数据库普遍引入“分库分表”中间件的方案（比如 MySQL 数据库常用的 MyCAT 中间件），利用中间件将多个单点数据库整合在一起来实现水平扩展，但从架构上讲，各个数据库之间是相互独立的，互相感知不到对方，当要做跨库事务的时候，必须依赖中间件。这种方法虽然暂时解决了扩展性问题，且技术难度和技术成本相对较低，不需要或者很少需要修改数据库引擎，但又引入了其它问题：
	1、***\*应用侵入性问题\****：为了满足中间件的使用要求，应用必须做相应的改造以实现正确的数据访问路由，这对应用具有很大的侵入性，相当于把数据库没解决的问题甩给了应用，应用开发需要做改造来适配。而每一次扩容/缩容，由于底层数据库引擎无法在线调整数据分布规则，意味着往往需要暂停业务、重新导入数据、做一次对应的路由变更，这会明显加大应用开发和运维的复杂度。

**2、*****\*诸多功能限制\****：如果应用比较简单，单表的规模也不大，所有的操作均在一个单点数据库完成，是没有太多功能的限制，应用开发者依然可以像使用单机数据库一样使用这种分库分表的中间件数据库。但一旦一些操作需要跨越不同的数据库，涉及的数据分布在多个不同的单点数据库，比如多表关联的复杂sql，由于中间件不具备分布式并行计算能力，导致它不能跨越多台机器协同执行任务。所以，很多数据库中间件会告诉应用开发者一些不支持的SQL，需要应用通过其他方式解决，影响了应用的迁移和开发。
	3、***\*不能保证数据一致性\****：当一个事务中处理的数据跨越多个不同的单点数据库时，由于数据库引擎没有分布式能力，只能通过中间件来完成分布式处理，中间件无法100%保证多台机器之间的数据一致性，尤其是中间件在执行分布式事务过程中遇到异常的时候，无法100%保证分布式事务的ACID。

造成这些问题的根本原因是这种架构的“先天不足”，“分布式”、“扩展性”等关键能力并没有内嵌在数据库引擎里实现，而是在数据库之外的中间件层面间接实现，因此当某些操作要求底层数据库具备这些能力时，中间件就显得无能为力了。 

## 分布式数据库

原生的分布式关系型数据库架构：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CC9.tmp.jpg) 

既然传统数据库有这样那样的问题，接下来我们看下，分布式数据库的基本特点，以及分布式数据库是如何解决了上述问题的。
	原生的分布式是把中间件做的很多事情下移到数据库引擎中，一般采用多副本一致性的架构。对应用来说，分布式数据库是给传统关系型数据库插上了一个分布式的翅膀，应用可以像用传统集中式数据库一样使用分布式数据库，业务不用关心其底层的分布式架构，迁移改造成本较低。 

首先，我们看下它是如何解决了传统数据库的高成本和扩展性问题。分布式数据库可以使用普通的标准PC服务器，可以很方便的在市场上买到，服务器自身无需外挂高端存储，也可以不使用Raid等可靠性技术。所以原生的分布式数据库在硬件上可以摆脱对小型机、高端存储等高价格产品的依赖，成本较低，同时也避免了用户被个别硬件厂商绑架的问题。随着业务的发展,当需要扩容时,只需将新服务器加入到数据库集群中即可,集群会自动的将业务数据迁移到新机器，等新机器追平数据后，系统自动的将流量切到新服务器，这个过程对业务是完全透明的。当需要缩容时，只需将服务器移除集群，系统会自动做反向操作，这个过程对应用也是完全透明的。如果是为了促销期间，应对短期的大流量，也可以使用阿里云公有云资源。 

那么它是否会像中间件架构一样带来很多问题么？不会的。原生的分布式数据库是在数据库引擎层面解决了分布式的问题，对应用而言是透明的，应用可以像使用传统数据库一样使用分布式数据库，不用担心是否存在跨库操作，是否有全局一致性的问题等等，这些都由数据库内部解决了。
	这种架构典型的代表，国外就是谷歌的Google Spanner数据库，国内就是OceanBase数据库。它们一般都采用了Paxos协议保证了数据高可靠和服务高可用，每一份数据都有多个副本，分别存储在集群内的不同服务器中，单个服务器的故障不影响整个业务的，以OceanBase为例，OceanBase可用性可以达到RPO=0，RTO<30秒，意思是少数节点的故障后，系统可以在30秒内自动恢复，且恢复后，不丢失任何数据。 

OceanBase和传统数据库对比：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CCA.tmp.jpg) 

总结：

在产品架构方面：集中式数据库是单点集中式，构建在高端硬件基础上，比如IBM高端服务器和EMC高端存储，OceanBase是原生的分布式架构，并采用业界最严格的Paxos协议。基于普通 PC 硬件设计。
	数据可靠性和服务高可用性方面：集中式数据库的高可用需要依赖高端硬件，普遍采用主从复制模式，主节点故障的情况下，会有数据损失的可能性（RPO>0）；且不能自动恢复服务，需要人工去判定主节点已宕机，并手工启动备节点，恢复时间通常以小时为单位计算。
	OceanBase使用普通PC服务器，主副本故障的情况下，剩余的从副本可以自动选出新的主副本（通常在30秒以内），而且剩余的两份副本依然可以进行强同步，保证 RPO=0。
	扩展性方面：对数据库来说，扩展性主要体现在2个方面，数据存储和计算能力。集中式数据库数据存储只能在单点内实现纵向扩展，在这个大数据的时代，最终必然会触达单点架构下的容量上限。计算节点通常无法扩展，少数模式下可做计算节点扩展，比如Oracle的RAC，但扩展数量也有限，且多个计算节点之间仍需访问单点共享存储，依然解决不了存储这个瓶颈。
	OceanBase在MPP架构下，每台普通的PC服务器都有自己的计算能力和存储能力，扩展时只需要扩展普通的PC服务器，可实现数据节点和计算节点的水平扩展，在网络带宽足够的前提下，可以扩充至任意数量。
	应用场景：传统数据库主要面向企业客户核心系统（金融、电信、政企等），互联网应用案例少。OceanBase已经应用阿里和蚂蚁内部众多系统，逐渐由内向外，迈向更多行业。
	使用成本：传统数据库依赖高端硬件，需要支付高端基础硬件的设备费用、高昂的软件授权费用以及产品服务费用。OceanBase依赖普通PC服务器，成本较低。
	国产化程度：主流的传统数据库均来自国外，全部研发人员在国外，中国的定位是市场和本地服务，存在安全风险。OceanBase是100%自研产品，研发人员均在中国，可以提供更加便捷全面的服务。  

 

# 概述

## 发展历程

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CCB.tmp.jpg) 

项目是2010年开始启动的，项目负责人是阳振坤老师，阳老师之前在北京大学、百度、微软等公司工作，一直从事分布式系统相关研究工作。最开始这个产品只是一个分布式存储的项目，通过API形式给应用访问。第一个业务是淘宝的收藏夹，现在也依然是OceanBase的客户，这个业务是单表非常大的业务。
	2013年，为了让OceanBase变成一个通用的关系型数据库，OceanBase开始做SQL。第一个业务是阿里妈妈，是一个报表或者批处理的业务，今天也依然跑在OceanBase上面.
	2014年，OceanBase进入蚂蚁金服，开始进入金融级的场景，第一个用户是网商银行，网商银行是纯电子化的银行，从最开始，所有核心交易都运行在OceanBase上。这一年的双十一，OceanBase也正式的接入了支付宝的部分流量。
	2016年，OceanBase正式发布了1.X 版本,增强了分布式事务等能力，支付宝 100%流量也都运行在OceanBase上了，包括交易、支付、会员、积分等核心模块。这么多年来，经过了多次“双十一”峰值流量的考验。
	2017年，开始应用到外部客户，成功应用到南京银行。 

2019年，OceanBase正式发布2.X版本，2.0版本相比1.0版本，有很多的改进，从以前只兼容MySQL到同时兼容了ORACLE，OceanBase可以实现在一个集群内同时跑两种模式的数据库。另外，OceanBase提升了HTAP能力，一套数据库同时支持OLTP和OLAP的业务。
	2020年，OceanBase成立独立公司，开展独立商业化运营，第二次参加TPC-C认证，再次打破之前的记录。 

## 核心特性

***\*主要技术特点：\****

1、采用Paxos协议，数据多副本写同步

2、写操作集中在单点update server进行，内存不落盘

3、读写分离，分布式读

4、多活数据中心

5、Mysql对外接口，读写分离及数据切分对业务透明

 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CDC.tmp.jpg) 

OceanBase能够在阿里及蚂蚁内部有着如此广泛的应用，主要因为产品的 6 个核心特性，高可靠、高性能、高可用、高兼容、高透明和多租户 6 个核心特性。
	1、高性能：OceanBase采用了很多先进的技术来提高数据库的性能。比如LSM Tree、无锁结构、消除磁盘的随机写等等，这些技术帮助我们充分使用硬件的能力，再辅以高扩展性，OceanBase就可以提供一个世界级性能的 OceanBase集群。在实际的生产系统里，OceanBase可以在峰值的时候提供6100万次每秒，单表最大容量可以到3200亿行。和高性能伴随的是低成本，因为OceanBase采用了LSM Tree结构，所以当数据落盘的时候是更有组织的，有更高的压缩比，可以有效减少磁盘空间。
	2、高透明：OceanBase支持很多创新的技术，比如全局一致性快照、全局索引、自动事务两阶段提交。使用OceanBase数据库，应用就像使用一台单机数据库一样，不需要做针对分布式数据库的特别感知和修改。
	3、高兼容：我们在一套OceanBase集群上同时提供两套生态，一套是Oracle生态，一套是MySQL生态，有效地降低业务迁移改造的成本。同时OceanBase和国内主流的操作系统、芯片也都做了互认的支持，可以有效满足技术供应链安全的需求。 

## 内部应用

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CDD.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CDE.tmp.jpg) 

OceanBase 应用到外部客户之前，已经在阿里及蚂蚁内部，广泛应用到阿多个核心系统。
	首先是支付宝核心交易，支付宝最常用的模块，如交易，支付，积分等业务的核心链路都运行在 OceanBase 上，日常每秒都有上万笔交易，双十一期间，每秒可以达到几十万笔交易。支付宝是典型的在线 OLTP 数据库场景，支付宝对 OceanBase 所有核心特性进行了验证，包括响应时间、处理速度、事务的完整性、并发量等，将 OceanBase 真正打磨成了金融级数据库。从产品成熟度上来讲，证明了 OceanBase 能够承担金融在线交易的场景。国内其他的数据库产品很少有机会能够在这么大的场景下，进行实实在在的打磨。
	“收藏夹”是典型的“写少读多”场景（一次写入、多次读取），峰值的数据读取请求量达到数百万次/秒；而且，由于淘宝巨大的活跃用户体量，这些读请求要访问几个数据量很大的表才能拿到所需数据，其中最大的表保存了数千亿条记录。OceanBase 数据库借助完备的分布式事务能力、完备的 SQL 引擎、优异的性能以及线性水平扩展等能力，很好地解决了海量数据下的在线、高并发、低延时查询等等需求，为数亿淘宝用户提供了良好的使用体验。
	网商银行虽然不是传统意义的银行，它没有个人存款的卡或者存折，但它是真正的商业银行。网商银行，创建伊始，就采用了 OceanBase 承载其所有业务流量，因此 OceanBase 承担了网商银行所有的数字资产。网商银行创新的采用三地五中心方案，无论服务器故障、机房故障还是城市灾难，都可以实现 RPO=0，RTO<30 秒的高可用性。证明了 OceanBase 能够提供最高等级的容灾方案，能够承载银行核心系统。
	Paytm，是印度的支付宝。Paytm 主站核心数据库也采用了 OceanBase 数据库。 

## 产品家族

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CDF.tmp.jpg) 

我们先从整体上看下 OceanBase 产品家族的几个主要产品：

首先，最核心的就是 OceanBase 数据库内核了，它通过 Paxos 协议保证了高可用性，它可以兼容 MySQL 和 Oracle，同时支持 HTAP，即它即可以用于 OLTP 业务，也可以用于 OLAP业务。物理上看，OceanBase 数据库运行在很多台服务器上，组成一个大的集群。
	OceanBase 给运维者提供 OCP 工具平台，图形化的界面，帮助管理员更好的完成日常的集群管理、租户管理、监控告警、性能诊断等任务。

OceanBase 给开发者提供 ODC 工具平台，图形化的界面，帮助开发者更好的完成数据库连接管理、数据库对象管理、存储过程开发调试、导入导出等任务。 

为了方便快捷的进行数据迁移，OceanBase 还提供了 OMS 数据库迁移平台，既可以从数据仓库订阅数据，也可以从异构的数据库里（比如 DB2、Oracle、MySQL）进行数据迁移、回滚等。 

# 安装部署

## 部署方式

OceanBase 支持多种部署方式。
	OceanBase 已经部署到阿里云公有云上，而且是采用两地三中心的高可用形式部署的，用户可以直接在阿里云订购服务，即买即用。
	OceanBase已经包含在阿里云专有云 3.5 以上版本，如果企业使用了阿里云的专有云，可以选择启动OceanBase。
	如果企业已经建设了自己的基础设施，OceanBase也可以独立部署。
	为了达到更好的性能，建议OceanBase采用独立部署模式。OceanBase服务器需要3台以上，最低的性能要求是32C，128G，1.2TB存储，当然为了达到更高的性能，建议配置为32C，256G，2TB存储。实验环境可以不用这么高配置的机器，如果单机运行，可以使用2C8G以上的服务器；如果集群部署，推荐使用4C16G以上服务器。
	OCP服务器是管理服务器，不需要那么多服务器，可以1台单机部署，为了追求更高的性能和可靠性，也可以3台部署。
	同时，为了更好的拥抱国产技术生态，OceanBase积极适配国产化CPU及操作系统：
	CPU方面：除了支持Intel X86系列CPU外，支持如下国产CPU（海光、海思、飞腾等）。
	操作系统方面：除了支持CentOS、Red Hat、SUSE、Debian/Ubuntu等Linux操作系统外，还支持如下国产Linux操作系统，包括AliOS、中标麒麟NeoKylin、银河麒麟Kylin等。

## 部署流程

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CEF.tmp.jpg) 

OceanBase 一般的安装部署流程是：
	第一步：首先需要对所有服务器和操作系统进行一系列的设置，使得服务器的环境适合部署 OceanBase；
	第二步：部署 OCP，OCP 部署完成后可以使用图形化的界面完成后续安装部署；
	第三步：通过 OCP 来部署 OceanBase 集群；
	第四步：通过 OCP 来部署 OB Proxy； 

第五步：创建租户，以及租户内的数据库、表等等；
	最后，可以根据业务需要部署 OMS、ODC、备份恢复等其他组件。 

## 连接工具

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CF0.tmp.jpg) 

OceanBase部署完成并创建完租户后。OceanBase支持2种客户端工具，用于连接数据库，对数据库进行日常的管理。
	一种是黑屏工具，包括MySQL客户端和OceanBase专有的客户端。OceanBase 专有的客户端工具可以同时访问 MySQL租户和 Oracle 租户，是比较推荐的黑屏客户端工具。MySQL客户端只支持访问 MySQL 租户。
	第二种是白屏工具，包括 OceanBase 云平台 OCP，OceanBase 开发者中心 ODC，通过这些图形化的工具，DBA 和开发者可以更好的连接、使用 OceanBase 数据库。 

# 架构

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3CF1.tmp.jpg) 

从管理员视角看，他创建的数据库集群有多个Zone，比如3个Zone，分别在杭州、上海和北京，每个Zone又包含了多个OceanBase服务器，一般情况下各个Zone内的机器配置和数量是一致的。所有这些就构成了各个业务需要使用的资源池。管理员可以根据业务情况，划分成不同大小的资源池授予租户使用，高性能要求的业务授予大资源池，低性能要求的业务授予小资源池。
	从应用开发者视角看，他可以基于自己的业务规划，向管理员申请租户资源池，当然这个资源池不是固定不变的，是可以根据业务发展平滑扩容的。拿到租户后，开发者可以创建数据库、表、分区等操作，满足应用对数据库的各类要求。 

 

## 集群、Zone和OB Server

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D02.tmp.jpg) 

首先是集群、Zone和OB Server之间的关系。
	一个集群由多个Zone组成，每一份数据在各个Zone上都有一份副本，而且只能有一份副本，这样一个Zone的故障不会影响业务正常运行和数据的完整性。物理上讲，不同的Zone可以对应不同的城市，如杭州、上海和深圳；也可以对应一个城市的不同机房，如杭州石桥机房、滨江机房、西湖机房等。也可以对应一个机房的不同机架，从而实现不同级别的容灾。逻辑上讲，Zone就是给集群内的一批机器打上同一个tag，打了同样tag的服务器就归属于一个Zone。
	Zone的个数一般大于 3 台，因为 OceanBase 采用 Paxos 协议，多数派要达成一致。至少 3 个 Zone 的话，当一个 Zone 故障后，剩下 2 个 Zone 内的副本依然还可以构成多数派，不影响业务。当然，如果有足够的投资，5 个 Zone 会提供更高的可靠性，比如网商银行的三地五中心五副本的方案。
	OB Server 是相对独立的，有自己的计算引擎和存储引擎，也会有部分数据。对业务而言，每台 OB Server 均是一台传统的集中式数据库，业务访问到这台 OB Server 后，如果需要访问的数据在其它 OB Server 上，它们自己会自动协商调度，对业务是无感知的。 

## RootService总控服务(RS)

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D03.tmp.jpg) 

一个集群由3个Zone以上组成，包含若干台服务器，如此庞大的系统需要一个“大脑”来统一管理，RootService总控服务就是这样一个“大脑”，它负责资源分配与调度、全局DDL、集群数据合并等全局事宜，是OceanBase的核心模块。
	比如扩容场景，新增一台服务器进入集群，原有哪个服务器应该释放哪个业务出来给这台新服务器，这些都是由RootService总控服务确定的。
	RootService无需额外的软硬件部署，一般与Zone内的一个OB Server合设，共用一台服务器。为了消除单点故障的风险，各个Zone都建议部署一个RootService总控服务，但只有一个Zone的总控服务是“主”，其他Zone内的总控服务为“备”，当“主”出现故障的时候，“备”可以自动的接管整个服务。 

## 多租户机制，资源隔离，数据隔离

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D04.tmp.jpg) 

集群多个服务器组成了一个大的资源池，系统会根据各个租户的要求，创建与之对应的虚拟资源池给租户使用，资源池包括指定规格的CPU、内存、存储、TPS、QPS等。租户之间资源是相互隔离的，***\*内存是物理隔离、CPU是逻辑隔离\****，避免租户之间争抢资源。
	一般一个应用占用一个租户。这个概念跟虚拟机比较类似，将一个庞大的物理资源虚拟成多个逻辑资源，彼此互相隔离。
	每个租户跟传统数据库实例的概念比较类似，租户可以创建自己的用户、数据库、表等对象。有自己独立的系统变量。使得租户可以更好的满足业务个性化的需求。
	系统租户保存系统表，一般ID是1000以内。剩下是业务租户，创建租户时候必须指定是MySQL模式还是Oracle模式。 

## 资源池(Resource Pool)

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D05.tmp.jpg) 

那么租户的虚拟资源池是如何分布在各个Zone及OB Server内的呢？会不会集中到某个Zone或者某个OB Server里面，当Zone故障或者这台服务器故障后，导致租户无法正常提供业务呢。
	首先，我们需要定义一个资源规格，比如资源规格U1=2C8G。这只是定义了一个2C8G的资源规格，并没有真正创建资源池。
	然后创建一个资源池并授予给租户1，这个资源池的定义是：Unit=U1，Unit数量=1。Unit=U1这个比较好理解，就是该租户的每个资源模块的规格都是 2C8G。这个“Unit数量”怎么理解呢？是指这个租户只有1个资源么？不是的，我们可以看这个图上，蓝色方块的数量是3个，说明租户1有3个资源，分布在Zone1、Zone2和Zone3，每个Zone都有1个资源。所以这个“Unit 数量=N”的意思是各个Zone内分配N个资源，而不是一共分配N个资源，若 N=1，3个Zone的话就是3个资源模块，5个Zone的话就是5个资源模块，这样单个Zone的故障不影响租户承载的业务。同理可以看下黄色模块代表的租户2和绿色模块代表的租户3，“Unit数量”分别为2和3，意味着每个Zone将为他们分配2个和3个服务器，在每个服务器上分配一个2C8G的资源模块给租户。

如果一个Zone内有很多服务器，租户的资源模块是分布在Zone内的各个Server中的，它的位置不是恒定不变的，会在Zone内各个OB Server中漂移（比如服务器故障或者有新扩容的服务器等）。
当然，这个资源大小和数量也不是静态的，可以随着业务的发展调整Unit的规格，比如从2C8G调整为4C16G，如果你把这个租户看成传统数据库的话，就是纵向扩展，或者调整Unit的数量，比如从1到3，就是横向扩展了。当然横向扩展也是有上限的，上限就是每个Zone内服务器数量的上限。
	因此，管理员需要实时了解集群资源水位情况，比如剩余多少CPU资源、剩余多少内存资源。这样当有新的业务需要租户，或者租户需要扩容时，管理员可以给出合适的建议（是使用大的资源规格+少的资源数量，还是使用小的资源规格+多的资源数量）。如果系统找不到足够的资源，资源池创建的任务会失败，管理员需要扩展扩容集群规模。 

# 存储引擎

## LSMTree

### **概述**

#### **简介**

LSM Tree（The Log-Structured Merge-Tree）
	• 将某个对象（Partition）中的数据按照“key-value”形式在磁盘上有序存储（SSTable）；
	• 数据更新先记录在MemStore中的MemTable里，然后再合并（Merge）到底层的ssTable里；
	• SSTable和MemTable之间可以有多级中间数据，同样以key-value形式保存在磁盘上，逐级向下合并。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D16.tmp.jpg) 

作为企业IT基础架构的核心部分，数据库技术一直是大家讨论的热点，也是很多人关注的领域。如果简单划分的话，数据库内核可以分为计算层和存储层，其中计算层负责接收用户发送过来的SQL语句，调用存储的功能来实现数据的存取，所以存储层的设计会直接影响计算层存取的效率，影响SQL语句的性能。
相对于传统的page based数据库存储方式，OceanBase使用了现在非常流行的LSM Tree作为存储引擎保存数据的基本数据结构，这在分布式的通用关系型数
据库当中是很少见的。
	首先需要说明的是，LSM Tree技术出现的一个最主要的原因就是磁盘的随机写速度要远远低于顺序写的速度，而数据库要面临很多写密集型的场景，所以很多数据库产品就把LSM Tree的思想引入到了数据库领域。LSM Tree，顾名思义，就是The Log-Structured Merge-Tree 的缩写。从这个名称里面可以看到几个关键的信息：

第一：log-structred，通过日志的方式来组织的 

第二：merge，可以合并的 

第三：tree，一种树形结构实际上它并不是一棵树，也不是一种具体的数据结构，它实际上是一种数据保存和更新的思想。简单的说，就是将数据按照key来进行排序（在数据库中就是表的主键），之后形成一棵一棵小的树形结构，或者不是树形结构，是一张小表也可以，这些数据通常被称为基线数据；之后把每次数据的改变（也就是log）都记录下来，也按照主键进行排序，之后定期的把log中对数据的改变合并（merge）到基线数据当中。下面的图形描述了LSM Tree的基本结构。
	图中的C0代表了缓存在内存中的数据，当内存中的数据达到了一定的阈值后，就会把数据内存中的数据排序后保存到磁盘当中，这就形成了磁盘中C1级别的增量数据（这些数据也是按照主键排序的），这个过程通常被称为转储。当C1级别的数据也达到一定阈值的时候，就会触发另外的一次合并（合并的过程可以认为是一种归并排序的过程），形成C2级别的数据，以此类推，如果这个逐级合并的结构定义了k层的话，那么最后的第k层数据就是最后的基线数据，这个过程通常被称为合并。***\*用一句话来简单描述的话，LSM Tree就是一个基于归并排序的数据存储思想\****。从上面的结构中不难看出，LSM Tree对写密集型的应用是非常友好的，因为绝大部分的写操作都是顺序的。但是对很多读操作是要损失一些性能的，因为数据在磁盘上可能存在多个版本，所以通常情况下，使
用了LSM Tree的存储引擎都会选择把很多个版本的数据存在内存中，根据查询
的需要，构建出满足要求的数据版本。在数据库领域，很多产品都使用了LSM
Tree结构来作为数据库的存储引擎，例如：OceanBase，LevelDB，HBase等。

#### **SSTable**

• 将数据按照主键或者隐含列（不可见）的顺序在磁盘上有序排列，以B+ Tree数据结构实现“key-value”存储。
	• 数据被分成2MB的固定大小宏块（Macro Block），每个宏块包含一定key值范围的数据。
	• 为了避免读取少量数据时的“读放大”，每个宏块内部又分为多个微块（Micro Block），大小一般配为16KB（可变）；微块内的记录数和具体的数据特征相关。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D17.tmp.jpg) 

OceanBase的数据文件以宏块（Macro Block）为单位组织数据，每个宏块大小为2MB。宏块内部又划分出很多个16K（压缩前的大小）大小的微块(Micro
Block)，而每个微块里面包含多个行(Row)。OceanBase内部IO的最小单位是微块。
	宏块可以合并和分裂。由于删除数据，相邻的几个宏块中的所有行可以在一个宏块中存放时，相邻的多个宏块会合并成一个宏块；由于宏块内插入和更新数
据导致空间不足，需要将数据存放到多个宏块中时，宏块会进行分裂。 

 

### **随机写问题**

传统数据库有随机写、写放大等问题：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D18.tmp.jpg) 

传统关系型数据库一般采用“堆表”的架构,磁盘上有表空间,会划分出不同的段(Extend),每个段再划分为若干页(Block），磁盘有一个根节点，段和页是随着数据的不断插入来不停分配的。在内存里，跟磁盘表空间对应的是Buffer Pool，无论应用读取数据，还是修改数据，都需要把数据从硬盘的表空间装载到内存里，在内存里完成数据的读和写，修改后的数据就是“脏数据”，这些“脏数据”最终还是要落入到硬盘表空间里，内存的Buffe Pool和硬盘的表空间是一一对应的，也就是数据从哪里读出来的，也写回到哪里。
	这就有一个问题，内存Buffer Pool的数据是相对紧凑、相对有序的。但是，在表空间上，有可能是非常离散的，比如图中所示，内存的4个页是紧凑的，但对应到硬盘上，四个页分布的非常分散。当内存中Buffer Pool的脏数据要写回硬盘表空间时，会有一个很严重的随机写的问题。这种随机写会导致严重的写放大，不仅影响写操作性能，而且会显著降低SSD的寿命，所以传统数据库一般使用高端读写型SSD。
	还有一个问题，数据的读取还是写，都是以页或者段为单位，即使要修改的数据很少，比如只是修改了1个页内的几个字节，也需要把整页更新到硬盘的表空间中，造成写放大。这种写放大是数据库架构带来的，跟SSD硬盘的写放大没有关系，即使使用传统的SATA或者SAS硬盘，依然也存在这个问题。 

准内存数据库+LSMTree存储引擎，避免随机写

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D19.tmp.jpg) 

OceanBase是一个准内存数据库的架构，存储又采用LSM Tree的架构，可以有效解决随机写和写放大的问题。
	OceanBase把内存分为了两块，一块是MemTable，用于写；一块是热点缓存，用于读。所有对数据的修改，比如insert、update等，都会先放到内存的MemTable中，所以可以认为MemTable是用于存储“脏数据”的。MemTable中的数据不像传统数据库那样不定期的进行check point到硬盘中，它会在内存中保存较长时间，当内存空间快满的时候，或者到了某个固定的时间（比如凌晨2点这种系统不太繁忙的时段），或者由人工触发，把内存的一大批脏数据批量的写回到硬盘上，这个过程叫转储，转储的数据还是增量数据，只是已经落盘了，它还需要与硬盘的SS Table的基线数据进行合并，形成新的基线数据。
	因此，OceanBase相当于把传统数据库的多频次的、每次都是小数据量的 check point变成不频繁的、每次都是大数据量的check point。这可以带来三个好处：

1、内存的脏数据批量合并之后，顺序写入SSD硬盘的，避免了随机写，提高了写性能，延长了SSD寿命; 

2、传统数据库，delete操作之后的空间不知道什么时候会再用到，容易造成磁盘碎片。OceanBase采用了LSM Tree架构，数据在磁盘上默认按主键有序排列，当内存里的大量的增量数据和磁盘基线数据合并时，会重新排序，然后以批量写的形式顺序写到硬盘上，可以很容易的把原有的碎片去掉，减少磁盘碎片，提升存储利用率；
	3、因为每次写回到硬盘的数据量都很大，可以支持对很大一批数据进行采样，因此它的压缩比传统数据库要高。
	当应用要读取数据时，不能仅仅从基线数据或者热点缓存中读取数据，因为这些数据有可能被更新了，只是还没最后合并到基线数据中。因此，OceanBase服务器还需要查看硬盘中的转储数据，以及内存中MemTable中的数据，确保读数据的强一致性。
	可能大家有个问题，这么大量的更新数据都存储在内存中而没有落盘，万一服务器断电，岂不是要丢大量的数据。OceanBase 写数据到内存 MemTable 的同时，也会同步将 RedoLog 落盘，并将 Redo-Log 同步到从副本那里。因此，即使服务器故障了，待服务器恢复后，依然可以通过 Redo-Log 恢复数据。因此，OceanBase 的“准内存数据库”+“LSM Tree 存储”的架构，不仅可以有效的提升性能，也可以极大的降低存储成本。 

### **转储和合并**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D29.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D2A.tmp.jpg) 

### **数据压缩**

LSMTree存储高数据压缩率，降低存储需求

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D2B.tmp.jpg) 

每次做数据合并的时候，写入到硬盘的数据量很大，OceanBase 可以对大数量进行采样后会有更好的压缩率。OceanBase 一般会进行两次压缩。
	第一次是 encoding，会使用字典、RLE 等算法对数据做瘦身。字典编码的思想很简单，就是把重复性较高的数据进行去重，把去重后的数据建立字典，而把原来存放数据的地方存成指向特定字典下标的引用，比如图上这个例子，Rate-ID 这一列的很多数据是一样的，可以归纳成 4 类，实际表中直接引用即可。
	第二次是通用压缩，使用 lz4 等压缩算法对 encoding 之后的数据再做一次瘦身，OceanBase 支持 snappy、lz4、lzo，zstd 等压缩算法，允许用户在压缩率和解压缩时间上做权衡。

因此，在使用相同的压缩算法下，OceanBase 通过高效的数据编码技术大大减少了存储空间。线下测试时，OceanBase 同 MySQL 最新版本 5.7 进行了对比，使用相同的块大小（16KB）、以及相同的压缩算法（lz4），同样的数据存放在 OceanBase 中，要比在 MySQL 5.7中平均节省一半的空间。数据压缩并不是以牺牲性能为代价的，OceanBase 写入（合并）性能较原有系统有了较大的提升，查询性能基本没有变化。
	实际生成系统中，支付宝的一个业务迁移到 OceanBase 后，数据由 100T 压缩到了 33T，也充分证明了 OceanBase 的数据压缩效率。 

## 内存管理

### **内存结构**

• OceanBase是支持多租户架构的准内存分布式数据库，对大容量内存的管理和使用提出了很高要求。
	• OceanBase会占据物理服务器的大部分内存并进行统一管理。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D3C.tmp.jpg) 

通过参数设定observer占用的内存上限
	• memory_limit_percentage
	• memory_limit 

 

OceanBase提供两种方式设置observer内存上限
	• 按照物理机器总内存的百分比计算observer内存上限：由memory_limit_percentage参数配置。
	• 直接设置observer内存上限：由memory_limit参数配置。
	memory_limit=0时， memory_limit_percentage决定observer内存大小；否则由memory_limit决定observer内存大小。

以100GB物理内存的机器为例， 下述表格展示了不同配置下机器上的observer内存上限： 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D3D.tmp.jpg) 

场景1：memory_limit=0，因此由memory_limit_percentage确定observer内存大小，即100GB*80% = 80GB。
	场景2：memory_limit='90GB'，因此observer内存上限就是90GB， memory_limit_percentage参数失效。 

 

OB系统内部内存
	• 每一个observer都包含多个租户（sys租户 & 非sys租户）的数据，但observer的内存并不是全部分配给租户。
	• observer中有些内存不属于任何租户，属于所有租户共享的资源，称为“系统内部内存”。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D3E.tmp.jpg) 

通过参数设定“系统内部内存”上限
	• system_memory
	租户可用的总内存
	• “observer内存上限” – “系统内部内存” 

OceanBase支持多租户架构，但是OceanBase内存上限中配置的内容并不能全部分配给租户使用。因为每一个OBServer上租户都会共享部分资源或功能，这些资源或功能所使用的内存由于并不属于任何一个普通租户，所以这类内存被归结为系统内部内存。系统内部内存可使用的内存上限是可以通过 system_memory配置参数配置的，它的含义是系统内部可使用OceanBase内存上限的百分比。
	例如，假设OceanBase内存上限为80 GB，system_memory的值为8G，可用于租户分配的内存就是剩下80-8=72 GB。

 

每个租户内部的内存总体上分为两个部分
	• 不可动态伸缩的内存：MemStore。
	• 可动态伸缩的内存：KVCache
	MemStore用来保存DML产生的增量数据，空间不可被占用； KVCache空间会被其它众多内存模块复用。  

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D3F.tmp.jpg) 

MemStore
	• 大小由参数memstore_limit_percentage决定，表示租户的 MemStore 部分占租户总内存的百分比。
	• 默认值为50，即占用租户内存的50%。
	• 当MemStore内存使用超过freeze_trigger_percentage定义的百分比时，触发冻结及后续的转储/合并等行为。
	KVCache
	• 保存来自SSTable的热数据，提高查询速度。
	• 大小可动态伸缩，会被其它各种Cache挤占。

 

OceanBase把租户内部的内存总体上分为两个部分：
	1、不可动态伸缩的内存
	2、可动态伸缩的内存
	其中，不可动态伸缩的内存主要由保存数据库增量更新的MemStore使用；可动态伸缩的内存主要由KVCache进行管理。
	可动态伸缩的KVCache会尽量使用除去不可动态伸缩后租户的全部内存。
	除去memstore和kvcache，其他模块的内存使用大小默认不超过10G

select * from gv$memory where used > 1024*1024*1024*10 and CONTEXT not
in ('OB_MEMSTORE','OB_KVSTORE_CACHE’);
	KVcache中的重要组成部分：
	sys租户：location_cache；location_cache中存放partition的leader信息；
	普通租户：user_block_cache、 block_index_cache、bf_cache、 user_row_cache；
PLANCHE
	PLANCACHE中缓存的执行计划是真实的。(explain看到的执行计划是预估的)
	用途：避免硬解析SQL语句，从CACHE中获得语句的“已分析”版本，加速语句执行。  

SQL AREA:
	执行 sql 需要的内存，主要是parser和优化器使用。
	内存模块称作 OB_SQL_ARENA。
	WORKER AREA：
	工作线程所占用的内存。
	内存模块称作 OB_WORKER_EXECUTION。 

### **内存总结**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D4F.tmp.jpg) 

OceanBase占用了服务器的大量内存（OBServer Total Memory），然后一部分用于自身系统运行（Memory Reserved For OBServer)，一部分用于划分给创建的租户（Allocatable Memory For OBServer)。 每个租户等同于传统数据库的一个实例，不同租户的内存模块组成是一样的，其内存又分为装载增量数据的MemStore（Tenant MemStore）以及KVCache缓存（Tenant Cache Memory）。 

### **常见问题**

## 内存数据落盘技术

### **转储与合并**

#### **合并**

##### 过程

OceanBase中最简单的LSM Tree只有C0层（MemTable）和C1层（SSTable）。两层数据的合并过程如下：
	• 将所有observer上的MemTable数据做大版本冻结（Major Freeze），其余内存作为新的MemTable继续使用；
	• 将冻结后的MemTable数据合并（Merge）到SSTable中，形成新的SSTable，并覆盖旧的SSTable；
	• 合并完成后，冻结的MemTable内存才可以被清空并重新使用。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D50.tmp.jpg) 

合并：合并操作（Major Freeze）是将动静态数据做归并，也就是产生新的C1层的数据，会比较费时。当转储产生的增量数据积累到一定程度时，通过Major Freeze实现大版本的合并。由于在合并的过程中为了保证数据的一致性，就需要在合并的过程中暂停正在被合并的数据上的事务，这对性能来说是会有影响的，OceanBase对合并操作进行了细化，分为增量合并，轮转合并和全量合并。 

##### 触发方式

三种合并触发方式
	• 定时合并
	• MemStore使用率达到阈值自动合并
	• 手动合并 

###### **定时合并**

• 由major_freeze_duty_time参数控制定时合并时间，可以修改参数控制合并时间：alter system set major_freeze_duty_time='02:00' 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D51.tmp.jpg) 

###### **MemStore使用率达到阈值自动合并** 

当租户的 MemStore内存使用率达到freeze_trigger_percentage参数的值， 并且转储的次数已经达到了major_compact_trigger/minor_freeze_times参数的值，会自动触发合并：
	• 通过查询(g)v$memstore视图来查看各租户的memstore内存使用情况。

• 可以修改以下参数的值来影响触发合并的时机：

alter system set freeze_trigger_percentage = 40;

alter system set major_compact_trigger = 100； 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D52.tmp.jpg) 

某个租户的memstore写到一定的比例会后会自行发起冻结，所谓 memstore，是指租户申请的内存资源中有多少可以来存放更新数据， 比如一个租户可使用的内存为8G(建立租户时，ResourceUnit的min_memory)，有一个参数memstore_limit_percentage来控制有多少内存可以用来存写入的数据(其余的内存会被用作其它用途，比如缓存)。 

假如memstore_limit_percentage为50%，即memstore的总大小为4G。当memstore写入超过一定比例 (由参数freeze_trigger_percentage控制，默认为70%)，故该租户的memstore超过4G 0.7 = 2.8G时会触发冻结。可以通过`select from oceanbase.v$memstore来查看各租户的memstore`信息。可使用show parameters like ‘minor_freeze%’查看再两次合并之间可以有多少次转储。

###### **手动合并**

• 可以在"root@sys"用户下，通过以下命令发起手动合并（忽略当前MemStore的使用率）：alter system major freeze；
	• 合并发起以后，可以在"oceanbase"数据库里用以下命令查看合并状态：select * from __all_zone;或者select * from __all_zone where name = 'merge_status'; 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D63.tmp.jpg) 

##### 轮转合并

###### **概述**

借助自身天然具备的多副本分布式架构， OceanBase引入了轮转合并机制：
	• 一般情况下， OceanBase会有3份（或更多）数据副本；可以轮流为每份副本单独做合并。
	• 当一个副本在合并时，这个副本上的业务流量可以暂时切到其它没有合并的副本上。
	• 某个副本合并完成后，将流量切回这个副本，然后以类似的方式为下一个副本做合并，直至所有副本完成合并。
	关于轮转合并的更多说明：
	• 通过参数enable_merge_by_turn开启或者关闭轮转合并。
	• 以ZONE为单位轮转合并，只有一个ZONE合并完成后才开始下一个ZONE的合并；合并整体时间变长。
	• 某一个ZONE的合并开始之前，会将这个ZONE上的Leader服务切换到其它ZONE；切换动作对长事务有影响。
	• 由于正在合并的ZONE上没有Leader，避免了合并对在线服务带来的性能影响。 

轮转合并是一种各个副本轮流进行合并的策略既可用于全量合并，也可用于增量合并。对全量合并和增量合并而言是一个正交的概念，可以配置也可以不配
置。如果不配置，则多个副本同步合并。 

###### **设置轮转合并顺序**

• 合并开始前，通过参数zone_merge_order设置合并顺序；只对轮转合并有效。
	• 场景举例
	假设集群中有三个zone，分别是z1,z2,z3，想设置轮转合并的顺序为"z1 -> z2 -> z3"，步骤如下：

alter system set enable_manual_merge = false; -- 关闭手动合并

alter system set enable_merge_by_turn = true; -- 开启轮转合并

alter system set zone_merge_order = 'z1,z2,z3'; -- 设置合并顺序

• 取消自定义的合并顺序
	alter system set zone_merge_order = ''; -- 取消自定义合并顺序 

在加zone或者减zone的情况下，用户设置的旧的zone_merge_order在每日合并的时候，不会生效，同时会报警。

###### **示例**

假设集群中的设置是zone_merge_order = 'z1,z2,z3,z4,z5'， zone_merge_concurrency =3，一次轮转合并的大概过程如下： 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D64.tmp.jpg) 

合并切主检查以后，如果zone_merge_list设置的不合理，则有可能影响每日合并的并发度。

##### 每日合并策略

可通过以下参数控制每日合并的策略：
	• enable_manual_merge: 是否开启手动合并（默认值False）；
	• enable_merge_by_turn: 是否开启自动轮转合并（默认值True）；
	• zone_merge_order: 指定自动轮转合并的合并顺序（默认值NULL）； 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D65.tmp.jpg) 

简要来说：
	手动合并：通过alter system的命令指定zone开始合并；
	自动非轮转合并：所有zone一起进行合并；不做切主操作，不受合并并发度的限制；
	自动智能轮转合并：RS按照一定策略依次调度起每个zone开始合并，直到所有zone都合并完成；

自动指定顺序的轮转合并：用户指定合并的顺序，RS 

根据这个顺序依次调度起zone进行合并；zone_merge_concurrency： 每日合并的并发度；在各种合并策略下的作用不尽相同； 

##### 合并控制

合并线程数，由参数merge_thread_count控制
	• 控制可以同时执行合并的分区个数；单分区暂不支持拆分合并，分区表可以加快合并速度。
	• 默认值为0，实际取值为min(10， cpu_cnt * 0.3)。
	• 最大取值不要超过48：值太大会占用太多CPU和IO资源，对observer的性能影响较大；而且容易触发系统报警，比如CPU使用率超过90%可能会触发主机报警。
	• 如对合并速度没有特殊要求，建议使用默认值0 。 

 

设置SSTable中保留的数据合并版本个数
	• 由参数max_kept_major_version_number控制，默认值为2。
	• 调大参数值可以保留更多历史数据，但同时占用更多的存储空间。
	• 在hint中利用frozen_version(<major_version>)指定历史版本。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D66.tmp.jpg) 

##### 合并注意事项

*合并超时时间**
*	• 由参数zone_merge_timeout定义超时阈值；默认值为'3h'（3个小时）。
	• 如果某个ZONE的合并执行超过阈值，合并状态被设置为TIMEOUT。
	*空间警告水位**
*	• 由参数data_disk_usage_limit_percentage定义数据盘空间使用阈值，默认值90。
	• 当数据盘空间使用量超过阈值后，合并任务打印ERROR警告日志，合并任务失败；需要尽快扩大数据盘物理空间，并调大data_disk_usage_limit_percentage参数的值。
	• 当数据盘空间使用量超过阈值后，禁止数据迁入。 

##### 查看合并记录和状态

#### **转储**

##### 概述

转储功能的引入，是为了解决合并操作引发的一系列问题
	• 资源消耗高，对在线业务性能影响较大。
	• 单个租户MemStore使用率高会触发集群级合并，其它租户成为受害者。
	• 合并耗时长， MemStore内存释放不及时，容易造成MemStore满而数据写入失败的情况。
	转储的基本设计思路
	• 每个MemStore触发单独的冻结（freeze_trigger_percentage）及数据合并，不影响其它租户；也可以通过命令为指定指定租户、指定observer、指定分区做转储。
	• 只和上一次转储的数据做合并，不和SSTable的数据做合并。 

 

转储的优势
	• 每个租户的转储不影响observer上其它的租户，也不会触发集群级转储，避免关联影响。
	• 资源消耗小，对在线业务性能影响较低。
	• 耗时相对较短， MemStore更快释放，降低发生MemStore写满的概率。
	转储的副作用
	• 数据层级增多，查询链路变长，查询性能下降。
	• 冗余数据增多，占用更多磁盘空间。 

##### 过程

为了解决2层LSM Tree合并时引发的问题（资源消耗大，内存释放速度慢等），引入了“转储”机制：
	• 将MemTable数据做小版本冻结（Minor Freeze）后写到磁盘上单独的转储文件里，不与SSTable数据做合并；
	• 转储文件写完之后，冻结的MemTable内存被清空并重新使用；
	• 每次转储会将MemTable数据与前一次转储的数据合并（Merge），转储文件最终会合并到SSTable中。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D77.tmp.jpg) 

OceanBase数据库采用了基于LSM Tree结构作为数据库的存储引擎，数据被分为基线数据（SSTable）和增量数据（MemTable）两部分，基线数据被保存在磁盘中，当需要读取的时候会被加载到数据库的缓存中，当数据被不断插入（或
者修改时）在内存中缓存增量数据，当增量数据达到一定阈值时，就把增量数
据刷新到磁盘上，当磁盘上的增量数据达到一定阈值时再把磁盘上的增量数据
和基线数据进行合并。
	对于LSM Tree结构，如果保存多个层次的MemTable的话，会带来很大的空间存储问题，OceanBase对LSM Tree结构进行了简化，只保留了C0层和C1层，也就是说，内存中的增量数据会被以MemTable的方式保存在磁盘中，这个过程被称之为转储（compaction），当转储了一定的次数之后，就需要把磁盘上的MemTable与基线数据进行合并（merge）。

##### 参数

major_compact_trigger /minor_freeze_times
	• 控制两次合并之间的转储次数，达到此次数则自动触发合并（Major Freeze）。
	• 设置为 0表示关闭转储，即每次租户MemStore使用率达到冻结阈值（freeze_trigger_percentage）都直接触发集群合并。
	minor_merge_concurrency
	• 并发做转储的分区个数；单个分区暂时不支持拆分转储，分区表可加快速度。
	• 并发转储的分区过少，会影响转储的性能和效果（比如MemStore内存释放不够快）。
	• 并发转储的分区过多，同样会消耗过多资源，影响在线交易的性能。 

##### 手动触发转储

##### 查看转储记录

#### **对比**

### **合并操作策略**

### **转储操作策略**

# SQL引擎

## 概述

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D78.tmp.jpg) 

目前市场上的主流应用级别都是运行在Oracle或者MySQL数据库上，为了方便应用迁移，降低迁移的技术难度和成本，OceanBase的SQL引擎可以兼容MySQL和Oracle。在创建租户的时候，我们就需要指定租户的模式是MySQL或者Oracle。
	OceanBase可以在一个数据库中，实现两种租户，对DBA来说，之前他要维护两个数据库，一个Oracle，一个MySQL。现在他只要维护一套数据库，应用开发者习惯了MySQL，就给他创建 MySQL租户，应用开发者习惯了Oracle，就给他创建Oracle租户。
	MySQL方面，由于蚂蚁内部的很多应用之前都使用MySQL数据库，为了更好的迁移应用到OceanBase上，OceanBase兼容了MySQL 5.6语法、数据结构、通信协议等等，继承开发者的开发习惯。还有一个原因是MySQL是开源的数据库，通信协议层也是完全公开的，OceanBase可以知道协议包的内容，就可以解析协议。所以，OceanBase是支持MySQL客户端的，应用开发者可以像使用MySQL一样使用OceanBase，但OceanBase内部是100%自主研发的分布式数据库，技术架构和原理与MySQL完全不一样。
	Oracle的兼容性就比较困难，OceanBase没有源码和通信协议层可以参考，只能独立研发，尽量去兼容Oralce，当前OceanBase兼容Oracle 11g语法，支持 90%的Oracle数据类型和内置函数，支持分布式执行的存储过程（PL/SQL），OceanBase也将持续投入，未来会更好的兼容Oracle。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D79.tmp.jpg) 

## SQL请求执行流程

### **Parse语法解析**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D7A.tmp.jpg) 

在语法解析器（Parser） 阶段， OceanBase使用lex进行词法分析，使用yacc进行语法分析，将SQL语句生成语法分析树。
	OceanBase既然可以同时兼容MySQL和Oracle，那语法解析阶段该如何实现？实际上OceanBase是以租户的方式提供MySQL和Oracle这两种兼容模式的（一个租户等同一个MySQL或Oracle实例，OceanBase可以混布这两种兼容模式的租户），在创建租户时指定兼容模式（不能修改），之后不同的租户走各自对应兼容模式的语法解析流程即可。 

注：GoldenDB采用的是Oracle开关，这样与之类似。

 

### **Resolver语义解析**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D8A.tmp.jpg) 

语义分析器（Resolver）相比上一阶段要复杂得多。针对不同类型的SQL语句（DML、DDL、DCL）或命令，会有不同的解析。
	这一步主要用于生成SQL语句的数据结构，其中主要包含各子句（如SELECT /FROM / WHERE）及表达式信息。

### **Transformer逻辑改写**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D8B.tmp.jpg) 

查询重写的核心思想在于保证执行结果不变的情况下将SQL语句做等价转换，以获得更优的执行效率。因为用户认为的“好”SQL，不一定是内核认为的“好”SQL，我们不能希望所有程序开发人员都在成为SQL优化的专家之后再来写SQL、生成好的执行计划让数据库高效执行，这就是查询重写模块存在的意义。
	查询重写本质上是一个模式匹配的过程，先基于规则对SQL语句进行重写（这些规则如：恒真恒假条件简化、视图合并、内连接消除、子查询提升、外连接消除等等），之后进入基于代价的重写判定。相比总能带来性能提升的基于规则的重写，基于代价的重写多了代价评估这一步（需要查询优化器参与）：基于
访问对象的统计信息以及是否有索引，在进行了重写之后会对重写前后的执行
计划进行比较，如果代价降低则接受，代价不减反增则拒绝。在迭代了基于代
价的重写之后，如果接受了重写的SQL，内部会再迭代一次基于规则的重写，生成终态的内核认为的“好”SQL并给到查询优化器模块。

### **Optimizer优化器**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D8C.tmp.jpg) 

查询优化器是数据库管理系统的“大脑”，它会枚举传入语句的执行计划，基于代价模型和统计信息对每一个执行计划算出代价，并最终选取一条代价最低的
执行计划。OceanBase的查询优化器基于System-R框架，是一个bottom-up的过程，通过选择基表访问路径、连接算法和连接顺序、最后综合一些其他算子来
计算代价，从而生成最终的执行计划。
	直到这里我们会发现，似乎与普通的单机RDBMS区别不大。诚然，作为一个已存在40多年的行业，很多理论的工程实践大同小异，但OceanBase作为NewSQL有独特的魅力。上面的查询优化部分生成的是串行执行计划，为了充分利用OceanBase的分布式架构和多核计算资源的优势，OceanBase的查询优化器随即会进入并行优化阶段：根据计划树上各个节点的数据分布，对串行执行计划进行自底向上的分析，把串行的逻辑执行计划改造成一个可以并行执行的逻辑计
划。并行优化最重要的参考信息是数据的分布信息（Location）， 即查询所需访
问的每个分区的各个副本在集群中的存储位置，这一信息由总控服务（主）
（Master Root Service）维护，为了提升访问效率该分布信息还有缓存机制。 

### **Code Generator代码生成器**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D8D.tmp.jpg) 

代码生成器是SQL编译器的最后一个步骤，其作用是把逻辑执行计划翻译成物理执行计划。
	查询优化器生成逻辑执行计划是对执行路径的逻辑表示，理论上已经具备可执行能力，但是这种逻辑表达带有过多的语义信息和优化器所需的冗余数据结构，对于执行引擎来说还是相对“偏重”。为了提高计划的执行效率，OceanBase通过代码生成器把逻辑计划树翻译成更适合查询执行引擎运行的树形结构，最终得到一个可重入的物理执行计划。 

### **Executor执行器**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D8E.tmp.jpg) 

物理执行计划的生成意味着SQL编译器的工作完成，之后交给执行引擎进行处理。需要注意的是，由于OceanBase是一个分布式数据库，分区副本遍布每一台OBServer上，我们执行的SQL语句要根据是访问本机数据、还是其他机器上的数据抑或是多台服务器上的数据来区别对待，这里就引入了调度器的概念。调度器把执行计划分为本地、远程、分布式三种作业类型，在对外提供统一接口且不侵入SQL执行引擎的同时，根据三种作业类型的特点，充分利用存储层和事务层的特性，实现了各自情况下最合适的调度策略。
	***\*本地作业：\****所有要访问的数据都位于本机的查询，就是一个本地作业。调度器对于这样的执行计划，无需多余的调度动作，直接在当前线程运行执行计划。
事务也在本地开启。如果是单语句事务，则事务的开启和提交都在本地执行，
也不会发生分布式事务。这样的执行路径和传统单机数据库类似。
	***\*远程作业：\****如果查询只涉及到一个分区组，但是这个分区组的数据位于其他服务器上，这样的执行计划就是远程作业（多半是由于切主等原因导致缓存还未
来得及更新数据的分布信息）。调度器把整个执行计划发送到数据所在机器上
执行，查询结果流式地返回给调度器，同时流式地返回给客户端。这样的流式
转发能够提供较优的响应时间。不仅如此，远程作业对于事务层的意义更大。
对于一个远程作业，如果是单语句事务，事务的开启提交等也都在数据所在服务器上执行，这样可以避免事务层的RPC，也不会发生分布式事务。
	***\*分布式作业：\****当查询涉及的数据位于多台不同的服务器时，需要作为分布式作业来处理，这种调度模式同时具有并行计算的能力。对于分布式计划，执行时
间相对较长，消耗资源也较多。对于这样的查询，我们希望能够在任务这个小
粒度上提供容灾能力。每个任务的执行结果并不会立即发送给下游，而是缓存
到本机，由调度器驱动下游的任务去拉取自己的输入。这样，当任务需要重试
时，上游的数据是可以直接获取到的。同时，对于分布式的计划，需要在调度
器所在服务器上开启事务，事务层需要协调多个分区，必要时会产生分布式事
务。  

### **Plan Cache执行计划缓存**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3D9F.tmp.jpg) 

## 执行计划快速参数化

将SQL进行参数化(即将SQL中的常量转换为参数)， 然后使用参数化的SQL文本作为键值在Plan Cache中获取执行计划， 从而达到仅参数不同的SQL能够共用相同的计划目的。
	参数化过程是指把SQL查询中的常量变成变量的过程： 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DA0.tmp.jpg) 

举例：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DA1.tmp.jpg) 

### **常量不能参数化**

常量不能参数化的场景：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DA2.tmp.jpg) 

举例：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DB3.tmp.jpg) 

由于c1作为主键列已是有序的，使用主键访问可以免去排序。 

## DML语言处理

数据操纵语言（Data Manipulation Language, DML）是SQL语言中，负责对数据库对象运行数据访问工作的指令集，以INSERT、UPDATE、DELETE三种指令为核心。
	除此之外，OceanBase还支持REPLACE和INSERT INTO...ON DUPLICATED KEY UPDATE两种DML语句，DML的主要功能是访问数据，因此其语法都是以读取与写入数据库为主，除了INSERT 以外，其他指令都可能需搭配WHERE指令来过滤数据范围，或是不加WHERE指令来访问全部的数据。 

### **INSERT**

对于INSERT/REPLACE语句而言，由于其不用读取表中的已有数据，因此， INSERT语句的执行计划相对简单，其执行计划为简单的EXPR VALUES+INSERT OP算子构成： 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DB4.tmp.jpg) 

### **UPDATE**

对于UPDATE或者DELETE语句而言，优化器会通过代价模型对WHERE条件进行访问路径的选择，或者ORDER BY数据顺序的选择：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DB5.tmp.jpg) 

### **DELETE**

对于UPDATE或者DELETE语句而言，优化器会通过代价模型对WHERE条件进行访问路径的选择，或者ORDER BY数据顺序的选择： 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DB6.tmp.jpg) 

### **一致性校验**

• DML操作的表对象每一列都有相关的约束性定义，例如列的NOT NULL约束，UNIQUE KEY约束等，写入数据前进行：
	- 约束检查
	- 类型转换
	• 约束性检查失败，需要回滚该DML语句写入的脏数据 

NOT NULL检查和类型转换通过SQL层生成的COLUMN_CONVERT表达式来完成，执行计划会为DML语句写入表中的每一列都添加该表达式，在执行算子中，数据以行的形式被流式的迭代，在迭代过程中， COLUMN_CONVERT表达式被计算，即可完成相应的类型转换和约束性检查，而UNIQUE KEY约束的检查是在存储层的data buffer中完成。

### **锁管理**

• OceanBase锁的类型
	- 只有行锁，没有表锁；在线DDL，不中断DML。
	- 只有写锁（X锁），没有读锁。
	• 与MVCC的结合
	- 读取“已提交”数据的最新版本，不需要读锁，不支持“脏读”。
	- 避免读写之间的锁互斥，实现更好的并发性。 

## DDL语言处理

• OceanBase支持传统数据库的DDL语句，自动完成全局统一的schema变更，无需用户在多节点间做schema一致性检查。
	• DDL任务由OceanBase的RootServer统一调度执行，保证全局范围内的schema一致性。
	• DDL不会产生表锁； DML根据schema信息的变更自动记录格式，对业务零影响。 

## 查询改写

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DC6.tmp.jpg) 

### **基于规则查询改写**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DC7.tmp.jpg) 

#### **视图合并**

视图合并是指将代表一个视图的子查询合并到包含该视图的查询中，视图合并后，有助于优化器增加连接顺序的选择、访问路径的选择以及进一步做其他改写操作，从而选择更优的执行计划。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DC8.tmp.jpg) 

#### **子查询展开**

##### 子查询展开为semi-join/anti-join 

子查询展开是指将where条件中子查询提升到父查询中，并作为连接条件与父查询并列进行展开。一般涉及的子查询表达式有not in、in、not exist、exist、any、all

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DC9.tmp.jpg) 

##### 子查询展开为内连接 

子查询展开是指将 where 条件中子查询提升到父查询中，并作为连接条件与父查询并列进行展开。一般涉及的子查询表达式有not in、in、not exist、exist、any、all。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DDA.tmp.jpg) 

#### **外连接消除**

外连接消除是指将外连接转换成内连接，从而可以提供更多可选择的连接路径，供优化器考虑。外连接消除需要存在“空值拒绝条件”，即where条件中，存在当内表生成的值为null时，使得输出为false的条件。
	示例： 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DDB.tmp.jpg) 

这是一个外连接，在其输出行中t2.c2可能为null。 如果加上一个条件t2.c2 > 5，则通过该条件过滤后，t2.c1输出不可能为NULL，从而可以将外连接转换为内连接：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DDC.tmp.jpg) 

### **基于代价的查询改写**

OceanBase 目前只支持基于代价的查询改写—或展开（Or-Expansion） 。
或展开（Or-Expansion）：把一个查询改写成若干个用union组成的子查询，这个改写可能会给每个子查询提供更优的优化空间，但是也会导致多个子查询的执行，所以这个改写需要基于代价去判断。
	通常来说，Or-Expansion的改写主要有如下三个作用:
	• 允许每个分支使用不同的索引来加速查询。
	• 允许每个分支使用不同的连接算法来加速查询，避免使用笛卡尔连接。
	• 允许每个分支分别消除排序，更加快速的获取top-k结果。 

 

#### **分支使用不同索引**

• 允许每个分支使用不同的索引来加速查询
	Q1: select * from t1 where t1.a = 1 or t1.b = 1;
	Q2: select * from t1 where t1.a = 1 union all select * from t1.b = 1 and lnnvl(t1.a = 1);
	示例：Q1会被改写成Q2的形式，其中Q2中的谓词lnnvl(t1.a = 1)保证了这两个子查询不会生成重复的结果。
	如果不进行改写，Q1一般来说会选择主表作为访问路径，对于Q2来说，如果t1上存在索引（a）和索引（b）， 那么该改写可能会让Q2中的每一个子查询选择索引作为访问路径：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DDD.tmp.jpg) 

 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DED.tmp.jpg) 

 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DEE.tmp.jpg) 

 

#### **分支使用不同连接算法**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DEF.tmp.jpg) 

 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DF0.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3DF1.tmp.jpg) 

#### **分支分别消除排序**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E02.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E03.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E04.tmp.jpg) 

## 执行计划

• SQL是一种“描述型”语言。与“过程型”语言不同，用户在使用SQL时，只描述了“要做什么”，而不是“怎么做”。
	• 数据库在接收到SQL查询时，必须为其生成一个“执行计划”。 OceanBase的执行计划与本质上是由物理操作符构成的一棵执行树。
	• 执行树从形状上可以分为“左深树”、“右深树”和“多枝树”三种（参见下图）。OceanBase的优化器在生成连接顺序时主要考虑左深树的连接形式。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E05.tmp.jpg) 

### **执行计划展示(EXPLAIN)**

• 通过Explain命令查看优化器针对给定SQL生成的逻辑执行计划。
	• Explain不会真正执行给定的SQL，可以放心使用该功能而不用担心在性能调试中可能给系统性能带来影响。
	• Explain命令格式如下例所示，展示的格式包括BASIC、EXTENDED、PARTITIONS等等，内容的详细程度有所区别：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E16.tmp.jpg) 

#### **计划形状与算子信息**

• Explain输出的第一部分是执行计划的树形结构展示。其中每一个操作在树中的层次通过其在OPERATOR中的缩进予以展示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E17.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E18.tmp.jpg) 

在表操作中，NAME字段会显示该操作涉及的表的名称（别名），如果是使用索引访问，还会在名称后的括号中展示该索引的名称， 例如t1(t1_c2) 表示使用了t1_c2这个索引。
	另外，如果扫描的顺序是逆序，还会在后面使用reserve关键字标识，例如t1(t1_c2,Reverse)。

#### **操作算子详细输出**

• Explain输出的第二部分是各操作算子的详细信息，包括输出表达式、过滤条件、分区信息以及各算子的独有信息，包括排序键、连接键、下压条件等等：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E19.tmp.jpg) 

• output

在执行树中每一个算子都需要向上游算子输出一些表达式以供后面的计算，也叫做投影操作，这里列出了需要进行投影的表达式（包括普通列）。
	• filters
	OB的所有算子都有执行过滤条件的能力，filters列出了该算子需要执行的过滤操作，对于表访问操作，我们会将所有过滤条件（包括回表前后的过滤条件）
都下压到存储层。
	• access
	表访问操作中需要调用存储层的接口访问具体的数据，access展示了存储层的对外输出（投影）列名。
	• is_index_back和filter_before_indexback
	OceanBase区分了哪些过滤条件可以在索引回表前执行，哪些需要在回表后执行，这个标示可以通过filter_before_indexback得到。如果表操作所有的filter都可以在回表前执行，则is_index_back为true，否则为false。
	• partitions
	OceanBase 内部支持二级分区。针对用户SQL给定的条件，优化器会将不需要访问的分区过滤掉，这一步骤我们称为分区裁剪。partitions显式了经过分
区裁剪后剩下的分区，其中如果涉及到多个连续分区，例如从分区0到分区20，
会按照“起始分区号——结束分区号”的形式展示，例如 partitions(p0-20)。 

• range_key和range
	OceanBase 中的物理表理论上都是索引组织表（包括二级索引本身），扫描的时候都会按照一定的顺序进行扫面，这个顺序就是该表的主键，体现在
range_key 中。当用户给定不同条件时，优化器会定位到最终扫描的范围，通
过 range 展示。
	可以看出要访问的表为 t1_c2 这张索引表，表的主键为 (c2, c1)， 扫描的范围是全表扫描。
	• sort_keys
	排序操作的排序键，包括其排序方向。 

举例：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E29.tmp.jpg) 

可以看出要访问的表为t1_c2这张索引表，表的主键为(c2, c1)，扫描的范围是全表扫描。

## 实时执行计划展示

• 通 过 查 询(g)v$plan_cache_plan_explain这张虚拟表来展示某条SQL在计划缓存中的执行计划。
	• 首 先 通 过(g)v$plan_cache_plan_stat虚 拟表查询到plan_id。  

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E2A.tmp.jpg) 

• v$plan_cache_plan_explain
	- 查询时必须指定tenant_id和plan_id的值。
	• gv$plan_cache_plan_explain
	- 查询时必须指定ip、port、tenant_id、plan_id这四列的值。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E2B.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E2C.tmp.jpg) 

## 执行计划缓存

• 一次完整的语法解析、语义分析、查询改写、查询优化、代码生成的SQL编译流程称为一次“硬解析”，硬解析生成执行计划的过程比较耗时（一般为毫秒级），这对于OLTP语句来说是很难接受的。
	• OceanBase通过计划缓存（Plan Cache）来避免SQL硬解析 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E2D.tmp.jpg) 

执行计划的生成过程非常复杂，优化器需要综合考虑多种因素，为SQL生成“最佳”的执行计划。因此，查询优化的过程本身也是一个比较耗时的过程，当 SQL本身执行耗时较短时，查询优化所带来的开销也变得不可忽略。一般来说，数据库在这种场景会缓存之前生成的执行计划，以便在下次执行该 SQL 时直接使用，这种策略被称为“optimize once”，即“一次优化”。
	OceanBase内置有计划缓存，会将首次优化后生成的计划缓存在内存中，随后的执行会首先访问计划缓存，查找是否有可用计划，如果有，则直接使用该计
划；否则将执行查询优化的过程。

### **缓存淘汰**

➢自动淘汰
	➢手动淘汰
	• alter system flush plan cache; 命令 

#### **自动淘汰**

➢ 当计划缓存占用的内存达到了需要淘汰计划的内存上限(即淘汰计划的高水位线)时，对计划缓存中的执行计划自动进行淘汰。
	➢ 优先淘汰最久没被使用的执行计划，影响淘汰策略的参数和变量如下：

• plan_cache_evict_interval（parameter）：检查执行计划是否需要淘汰的间隔时间。
	• ob_plan_cache_percentage（variable）：计划缓存可使用内存占租户内存的百分比 （最多可使用内存为：租户内存上限 * ob_plan_cache_percentage/100）。
	• ob_plan_cache_evict_high_percentage （variable） ： 计划缓存使用率（百分比）达到多少时，触发计划缓存的淘汰。
	• ob_plan_cache_evict_low_percentage （variable） ：计划缓存使用率（百分比）达到多少时，停止计划缓存的淘汰。 

举例：

➢如果租户内存大小为10G，并且变量设置如下：
	• ob_plan_cache_percentage ＝ 10；
	• ob_plan_cache_evict_high_percentage ＝ 90；
	• ob_plan_cache_evict_low_percentage ＝ 50；
	则：
	计划缓存内存上限绝对值 ＝ 10G * 10 / 100 = 1G;
	淘汰计划的高水位线 = 1G * 90 / 100 = 0.9G;
	淘汰计划的低水位线 ＝ 1G * 50 / 100 = 0.5G;
	➢当该租户在某个server上计划缓存使用超过0.9G时，会触发淘汰，优先淘汰最久没执行的计划；当淘汰到使用内存只由0.5G时，则停止淘汰。
	➢如果淘汰速度没有新计划生成速度快，计划缓存使用内存达到内存上限绝对值1G时，将不再往计划缓存中添加新计划，直到淘汰后使用的内存小于1G才会添加新计划到计划缓存中。 

#### **手动淘汰**

• 手动删除计划缓存中的计划，忽略当前参数/变量的设置。
	• 支持按租户、server或删除计划缓存，或者全部删除缓存：
	alter system flush plan cache [tenant_list] [global]；

其中tenant_list：tenant = 'tenant1, tenant2, tenant3….'
	其中tenant_list和 global为可选字段：
	如果tenant_list没有指定，则清空所有租户的计划缓存，否则只清空特定租户的。
	如果global没有指定，则清空本机的计划缓存，否则清空该租户所在的所有
server上的计划缓存。 

### **缓存刷新**

计划缓存中的执行计划因各种原因失效时，会将计划缓存中失效的计划进行刷新（可能会导致新的计划）：
	• SQL中涉及的表的SCHEMA进行变更时（比如添加索引，删除或增加列等），该SQL在计划缓存中对应的执行计划将被刷新；
	• SQL中涉及的表的统计信息被更新时，该SQL对应的执行计划会被刷新，由于OceanBase在合并时统一进行统计信息的收集，因此每次合并之后，计划缓存中所有的计划将被刷新；
	• SQL进行outline计划绑定变更时，该SQL对应的执行计划会被刷新，更新为按绑定的outline生成的执行计划。 

### **使用控制**

➢ 系统变量控制
	• ob_enable_plan_cache设置为ture时表示SQL请求可以使用计划缓存，设置为false时表示SQL请求不使用计划缓存。可进行session级和global级设置，默认设置为true。
	➢ Hint控制
	• /*+use_plan_cache(none)*/ , 该hint表示请求不使用计划缓存
• /*+use_plan_cache(default)*/, 该hint表示使用计划缓存 

# OBProxy路由

## 背景

OceanBase在部署完成后，应用可以采用OB提供的客户端、MySQL客户端或者其他语言的客户端来访问OceanBase，OceanBase以服务的形式提供给应用访问。OceanBase集群一般包含多个Zone，每个Zone中又包含一台或多台OBServer，集群中的每个OBServer都可以接收用户的连接并处理请求。
	• 实现一个OBProxy来接受来自应用的请求，并转发给OBServer，然后OBServer将数据返回给OBProxy，OBProxy将数据转发给应用客户端。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E3E.tmp.jpg) 

OBProxy作为OceanBase的高性能且易于运维的反向代理服务器，具有防连
接闪断、OBServer宕机或升级不影响客户端正常请求、兼容所有MySQL客户端、支持热升级和多集群功能。

## 功能

### **路由**

#### **SQLParser**

SQLParser：轻量的sql解析，判断出客户端的sql请求所涉及的表的主副本在哪台机器上，将请求路由至主副本所在的机器上。

#### **LDC路由**

LDC路由：主要对于读写分离的场景，根据observer和obproxy配置的region
（区域）和LDC（逻辑机房），将请求发送给本地的副本（后面proxy路由策略
会详细说明）。

#### **读写分离部署**

读写分离部署：对于读写分离的场景，OBProxy会把请求优先发送到本地的只读副本。

#### **黑名单**

黑名单：OBProxy在路由过程中，如果发现OBServer不可用，则把该server加入到黑名单。

### **连接管理**

在observer宕机/升级/重启时，客户端与OBProxy的连接不会断开， OBProxy可以迅速切换到正常的server上，对应用透明。

• OBProxy支持用户通过同一个OBProxy访问多个OceanBase集群
	• Server session对于每个client session独占
	• 同一个client session对应server session状态保持相同(session变量同步) 

OBProxy与OB集群（OBServer）保持长连接，客户端一般通过连接池的方式连接到OBProxy。

## 部署

## 策略

根据不同的配置，Obproxy进行综合的路由排序：
	1、LDC配置：
	1）本地：同城同机房（IDC相同）
	2）同城：同城不同机房（IDC不同，Region相同）
	3）异地：不同的地域（Region不同）
	2、Observer 状态：常态vs正在合并
	3、租户的Zone类型：读写型vs只读型
	4、路由精准度：优先精准度高的
	OBproxy中有目标partition的路由信息（PS）
	OBproxy中没有Partition的路由信息，只有租户的路由信息（TS） 

 

写请求：
	- 写请求路由到ReadWrite zone的主副本
	读请求：
	- 强一致性读
	- 弱一致性读
	⚫ 主备均衡路由策略（默认）
	⚫ 备优先读策略
	⚫ 读写分离策略 

 

## 执行流程

## 使用限制

## 使用和运维

## 常见问题

# SQL调优

## 调优方法

## 分区

## 索引

## 局部索引与全局索引

## Hint

## SQL执行性能监控

 

# 分布式事务

## ACID

分布式事务跨机执行时，OceanBase通过多种机制保证ACID：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E3F.tmp.jpg) 

首先介绍下 ACID。事务指逻辑上的一组操作，这些操作要么全部成功，要么全部不成功。典型场景就是转账，A 账户的扣减，与 B 账户的增加，必须一起完成或者都不完成。事务有ACID，四个基本要素：
	A 指原子性，需要保证操作都发生或都不发生。这在传统集中式数据库中比较容易实现，因为不存在跨机操作的风险，如果服务器故障了，所有的操作都不会完成，一般不会出现部分提交的现象。

但在分布式环境中就比较复杂，一个事务 10 条 sql 语句，涉及 8 个分区，极端情况下，这 8 个分区的主副本可能分布在 8 台服务器上，万一出现一些异常，有些完成了，有些没有完成，都会影响事务的原子性。OceanBase 是用两阶段提交协议保证原子性的。

C 是指一致性，是指事务前后数据的完整性必须保持一致。OceanBase 通过保证主键唯一，全局快照等技术保证一致性。 

I 是指隔离性，是指多个用户并发访问数据库时，数据库为每个用户开启的事务，不能被其他事务的操作所干扰。比如一个事务正在修改某个字段，这个字段是否允许其他事务读或者写呢？OceanBase 采用 MVCC 技术解决读写互斥的问题。
	D 是指持久性，一个事务一旦被成功提交，它对数据库中数据的改变就是永久性的，即使数据库发生故障也不应该对其有任何影响。这块我们之前重点讲了，OceanBase 使用 Paxos协议，通过在多副本之间同步 Redo-Log 和 Redo-Log 落盘来确保数据的持久性。

 

## 分布式两阶段提交

创新的来那个阶段，任何故障均可以保障事务的原子性：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E40.tmp.jpg) 

首先看下如何保障 A，原子性。如果一个事务有 10 条 SQL 语句，涉及 8 个分区，极端情况下，这 8 个分区可能分布到 8 台服务器上，这时就是分布式事务了。
	对于应用而言，是否需要应用知道分区具体分布到哪台服务器上，是不是需要应用采取一些技术手段，如两阶段提交，来保证事务的 ACID。答案是不需要的，对于 OceanBase 而言，OceanBase 内部是典型的分布式场景，很复杂。对外，对应用开发者来说，是一个简单的一阶段事务的场景，应用只要开启事务，执行 10 条 SQL 语句，提交事务或者回滚事务即可，跟使用一个集中式数据库差异不大。

OceanBase内部使用两阶段提交方式来保证事务的原子性，两阶段提交有两个重要角色，协调者和参与者。
	两阶段的第一阶段是投票阶段，协调者向所有参与者发送Prepare请求与事务内容，询问是否可以准备事务，并等待参与者的相应。参与者执行事务中的操作，向协调者返回事务操作的执行结果；
	两阶段的第二阶段是执行阶段，如果所有参与者都返回OK，协调者向所有参与者发送提交请求，参与者收到后，完成事务的提交，并返回给协调者。协调者收到所有参与者的消息后，完成事务，返回给应用。如果有任何一个参与者返回 no 或者超时未返回，则需要回滚。
	那么在OceanBase内部，谁是参与者？谁是协调者呢？需要澄清的是，OB Proxy不是协调者，它是无状态的，也没法持久化数据，如果让它做协调者的话，一旦它宕机了，事务也无法恢复了。数据库中间件架构中，一般中间件是协调者，下面的数据库是参与者，参与者只知道协调者而不知道其他参与者的存在，一旦中间件故障，很难恢复事务。
	OceanBase创新了二阶段提交的方案，所有的参与者和协调者都是OB Server，参与者和协调者都知道彼此的存在，参与者也知道其他参与者的存在，如图中所示，Zone2的一个服务器是协调者，当事务需要的数据在其他服务器上时，会由Zone2的服务器与Zone1和Zone3的相关服务器（这些服务器做为参与者）通信。
	这个好处是，OB Server是有高可用性保障的，事务执行期间，任何一台OB 服务器故障，即使是协调者所在的服务器故障了，其他服务器的从副本通过 Paxos协议也可以很快的自动选举新的主副本，新的主副本做为新的协调者，可以将事务恢复到宕机之前的状态，不会有状态和数据的丢失，可以确保分布式事务的强一致性，避免事务部分提交的现象。
以上图为例，如果协调者所在服务器故障了，P2从副本所在的两个服务器会选出新的主副本，新的主副本已经知晓该事务的存在（通过Redo-Log日志同步），并担任新的协调者的角色来恢复业务。

OceanBase分布式事务的能力在TPC-C测试中也得到了充分的验证： 

TPC-C测试要求40%的订单需要跨仓库执行，这个时候，如果是一个分布式事务，需要以仓库为单位把数据打散，大概率很多事务是需要跨机器执行的。如此多的事务执行完成后，如何判定有没有事务存在部分提交的现象呢？审计员会扫描所有的数据（OceanBase测试时候，大概扫了约1万亿条数据），算一个SUM值，跟其他几个值来对比，如果数值不等的话，肯定有一些分布式事务有问题，测试是不会通过的。
	为了更好的测试OceanBase的分布式事务，审计员也人工制造了一些故障，登陆一台阿里云ECS服务器后，直接把服务器shutdown，这个服务器上肯定运行了大量的事务，接下来应用会报错和重连。审计员最后会再检查完整性和一致性，一旦有不一致的，测试也不会通过。传统数据库也可以完成上述测试，但往往是依靠底层的高端硬件来实现的，而OceanBase是完全依赖自身软件实现的。 

 

## 全局快照及分布式一致性读

多版本并发控制（MVCC），解决读写互斥问题：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E51.tmp.jpg) 

采用锁机制控制读写冲突时，对数据加锁后，往往其他事务将无法读，导致读写竞争，影响读的并发性，OceanBase支持MVCC特性，可以有效解决该问题。如图上举例，张三的账户余额是100元。A1时间，T1事务想要更改张三的账户余额为50元，此时会加锁，避免数据被其他事务改写。由于事务没有正式提交，系统以时间戳为版本号，记录两个版本的数据，新版本为50，旧版本为100。

A2 时间，T2 事务想查询张三的账户余额，由于新版本数据还未正式提交，存在回滚的风险，T2 事务可以读取小于等于当前版本号的数据，且是已提交的数据，读取金额为旧版本的数据，100。

A3 时间，T1 事务完成提交，张三的余额变为了 50。

A4 时间，T3 事务想要读取张三的余额，由于 T1 事务已经提交完成，此时 T3 事务可以读取新版本的数据，读取到的金额为 50。
	总结下，OceanBase 使用 MVCC 技术解决了读写互斥的问题，提高了读的性能。同时，OceanBase 提供了全局唯一的时间戳服务，消除机器时钟差异带来的影响，方便全局管理版本号。
	当修改数据时：首先需要获取行锁，然后再修改数据。事务未提交前，数据的新旧版本共存，通过不同的版本号区分。事务提交后，只有新的数据。
	当读取数据时：不需要加锁，也不需要关注数据是否已被加锁。先获取版本号，再去查找小于等于当前版本号的已提交的数据。

 

## 事务隔离级别

保证全局事务一致性的隔离级别：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E52.tmp.jpg) 

讲解事务隔离级别前，先了解几个概念：

第一是脏读，也就是读取了未提交的数据，最后这数据又回滚到了，导致读了错的数据。
	第二是不可重复读，在一个事务内，不同的时刻读同一批数据，结果不一样。也就第二次读之前，期间被别的事务更新了数据。
	第三是幻读，指的是在同一个事务内，在操作过程中进行两次查询，第二次查询的结果包含了第一次查询中未出现的数据或者缺少了第一次查询中出现的数据，期间被别的事务插入或者删除了数据。
	OceanBase支持多种事务隔离级别，基于全局一致的数据版本号管理，以不同的版本号策略实现不同的隔离级别。
	OceanBase支持两种隔离级别，一种是Read-Committed，可以避免脏读，但存在不可重复读和幻读（默认）；另外一种是Serializable，可以避免脏读、不可重复读和幻读，这是最严格的隔离级别，但会影响性能。
	OceanBase 不支持脏读，事务能够读取的数据肯定都是已提交的数据，不可能是未提交的数据，但有可能是已提交的旧数据。 

# 数据分布

# 复制/一致性

## 多副本同步redo log

通过多副本同步redo log确保数据持久化：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E53.tmp.jpg) 

业务从OceanBase读取数据时候，只会从主副本读取数据。那么如果业务对数据库进行写操作，OceanBase是如何确保数据持久化呢？
	Paxos组成员通过Redo-Log的多数派强同步来确保数据持久化，以图中所示为例，当应用要写数据到P2分区时，应用会连接到P2分区的主副本所在的机器（Zone2-OB Server1）中，该服务器首先会将Redo-Log落盘，并将Redo-Log同步请求发送到P2从副本所在的机器（Zone1-OB Server1和Zone3-OB Server1）中，从副本完成日志落盘后，并返回消息给主副本。主副本只要收到其中一个从副本的回复后即可以返回应用（两个副本强同步已满足了多数派），而无需再等待其他从副本的反馈。 

如果哪个从副本由于故障没有落盘成功怎么办？等故障恢复后，再从主副本那里追平数据就好。
	这种机制有两个好处：
	数据更新只要 Redo-Log 落盘就好,而 Redo-Log 的落盘是像记日记一样，是顺序写的，很快。实际更新的数据存储在内存,不用马上落盘.减少了数据落盘带来的寻址、写数据等时间，快速响应应用的写请求。
	Redo-Log 日志是同时同步给 2 个以上的副本，只要多数派落盘成功，不用等所有从副本落盘成功，可以快速响应应用。 

# 备份恢复

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E54.tmp.jpg) 

OceanBase支持全量备份和增量备份，全量备份是对存储层的基线数据进行备份，增量备份是通过redo-log备份，OceanBase支持在线实时的全量和增量备份，对业务无感知。
	备份支持2种介质，一种是阿里云OSS存储，另外一种是普通的NFS。NFS需要一个公共目录，每个OB Server都可以访问，NFS服务器为每台OB Server创建子目录，用于存储备份文件。
	备份性能可以达到网卡的上限，约1G左右。恢复性能，一般也可以达到500兆左右。
	备份恢复最小粒度是租户，让备份和恢复更加灵活。
	备份恢复数据方面，支持逻辑数据（比如用户权限、表定义、系统变量、用户信息、视图信息等）和物理数据。 

## 架构

### **逻辑备份/恢复方案**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E55.tmp.jpg) 

 

### **物理备份/恢复方案**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E65.tmp.jpg) 

## 步骤

## 查看任务状态

## 常见问题

# 兼容性

# 扩展性

## 动态扩容/缩容

动态水平扩展的场景：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E66.tmp.jpg) 

业务的流量往往不是固定不变的，比如促销期间，业务流量很高，需要对数据库进行扩容，以应对洪峰流量；促销过后，业务流量降低，需要对数据库缩容，以节省成本。支付宝在每年双十一期间，均需要购买阿里云ECS服务器，把它们加入集群，共同应对流量洪峰，洪峰过后再进行缩容，恢复到扩容前的状态，这个过程对业务是透明的，业务也无需暂停。
	传统数据库的扩容和缩容非常复杂，要改规则、要改分布、要导出数据，创建新表后再导入。不仅业务要短暂停止，也需要投入大量人力进行运维。 

动态扩容和缩容技术实现：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E67.tmp.jpg) 

OceanBase 的扩容和缩容是可以自动化完成的，以上图为例，我们讲解下扩容和缩容的过程：
	扩容前是 3 个 Zone，每个 zone 都有 1 台服务器，是 1-1-1 的组网。假设一个表有 8 个分区，则每个 zone 内的 1 台服务器都有主副本和从副本。促销前，经过评估，如果要满足促销的洪峰流量，需要扩展到 2-2-2 组网，即每个 Zone 由 1 台服务器扩容到 2 台服务器。
	扩容时，首选购买阿里云 ECS 服务器，然后把服务器加入到集群里，告诉 OceanBase 现在每个 zone 可用服务器多了一个。OceanBase 会选一些副本，动态的复制到新服务器，比如 Zone1 内旧服务器有 8 个副本，OceanBase 把 P1,P4,P5,P6 从旧服务器迁移到新服务器中，其中 P1 是主副本，将承接业务，旧服务器剩余 4 个副本（P2,P3,P7,P8)，其中 P2 和 P3是主副本。所以，扩容过程中，OceanBase 除了保持各个服务器副本数量相对一致（比如扩容后每个服务器都是 4 个副本），也会将主副本打散到各个服务器中（比如扩容后每个服务器都有若干主副本），从而将流量分发到各个服务器中，共用应对洪峰流量。新服务器的副本首先会追平数据，一旦数据追平后，旧服务器的主副本和从副本将停止服务，由新服务器的对应副本接管服务，删除旧服务器的旧分区，这个过程是自动完成的，对业务是无影响的。
	洪峰流量过后，再做反向动作，告诉 OceanBase 新增加的服务器不用了，OceanBase 会自动将新服务器的副本迁回到旧服务器中，数据追平后，新服务器的副本将停止服务，继续由旧服务器独自承担所有流量。最后把新服务器移除集群，释放阿里云资源。
	因此，使用 OceanBase，整个扩容和缩容是全自动的，无需人工参与；是线性的，可以灵活的增加和减少；这是原生分布式数据库的优势。 

# 高并发

## 执行计划快速参数化

## 自动负载均衡

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E68.tmp.jpg) 

既然只有主副本供应用访问。可能有人会问，会不会选出来的主副本都在同一个Zone内，或者同一台服务器上，导致一台服务器负载太高，其他服务器只做备份，负载很低，造成忙闲不均的现象呢。答案是不会的，默认情况下，系统会自动的把主副本平均分配到3个Zone中，比如图中，Zone1有3个主，Zone2有2个主，Zone3有3个主，每个服务器也都是有主副本，有从副本。这样的好处是每个服务器都承载了部分业务的同时，也给其他服务器做着备份的工作，将业务负载均衡了。当然，在一些特殊场景下，管理员也可以通过Primary zone的功能将业务负载集中到某个zone内，以满足一些特定场景的需求。 

对于分布式系统来说，有一个问题，如果应用要访问的数据不在一个服务器上，是否需要应用自己连接多个服务器，造成业务的复杂。答案是不需要的，比如应用2，他要访问的数据分布在 P6,P7,P8上，P6的主副本在Zone2-OB Server2，P7的主副本在Zone3-OB Server2，P8的主副本在 Zone1-OB Server2 中。应用是否需要了解这些细节呢？不需要，应用不需要知道他要访问的数据在哪里，他可以连接任何一台OB Server，比如开始访问P6主副本所在的服务器（Zone2-OB Server2），该服务器会连接其他服务器获取数据后，返回给应用。这个过程对应用是透明的。 

通俗来讲，每台OB Server都是全功能的，都是相对独立的，都践行“首问责任制”和“最多访问一次”的概念，避免对业务的侵入性。 

 

## 智能路由

OB Proxy为应用提供智能路由服务，应用透明访问：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E79.tmp.jpg) 

虽然对于应用来说，如果它需要的数据位于多台OB Server上。它可以连接任何一台OB Server，由其访问其他OB Server获取应用想要的所有数据。但万一这台服务器故障后，应用需要自身实现高可用的机制，增加了应用的复杂度。
	OB Proxy可以解决这个问题，OB Proxy包含简单的SQL Parser功能，可进行轻量的SQL解析。先从客户端发出的SQL语句中解析出数据库名和表名，然后根据用户的租户名、数据库名、表名以及分区ID信息等信息查询路由表，获取分区的主/从副本所在OB Server的IP地址信息。有了这些信息之后，OB Proxy将请求路由至主副本所在的机器上（Leader位置无法确定时随机选择 OB Server），同时将副本的位置信息更新到自己的 Location Cache中，当再次访问时，会命中该Cache以加速访问。当负载均衡被打破（如机器故障、集群扩容等），导致分区分布发生变化时，location cache 也会动态更新，应用无感知。
	另外，OB Proxy不需要独立占用1台服务器，可以与OB Server共用一台服务器。单台OB Proxy会带来单点故障，可以在前面前置F5负载均衡器或DNS域名服务器，由其来做负载均衡。但这也会带来延时增加的问题，如果应用对实时性要求高，也可以将OB Proxy部署到应用服务器中，没有了F5负载均衡器以及网络的延时，可以减少时延。
	当然，OB Proxy除了支持负载均衡功能外，也可以配置为其他功能，比如读写分离、备优先读、黑名单等功能，满足业务差异化的需求。
	有人可能会问，OB Proxy跟数据库中间件很像，都是在应用和数据库服务器之间。其实是完全不一样的架构。OB Proxy是一个“无状态”的服务进程，不做数据持久化，不参与数据库引擎的计算任务。比如一台OB Proxy故障了，由于这台故障的服务器没有维护任何session的状态，也没任何数据，业务可以重连其他OB Proxy，重新开始session。 

## 设定Primary Zone

通过设定Primary Zone，将业务汇聚到特定Zone：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E7A.tmp.jpg) 

负载均衡机制可以将主副本打散到不同 Zone 内的不同 Server 中，将业务流量分到不同服务器中，提升了系统整体的可用性和性能，但这也导致出现分布式事务的概率增大，带来更大的资源消耗。

通过设置Primary Zone，如上图配置2，设置Zone优先级为Zone1>Zone2>Zone3，可以将主副本集中到Zone1中（如该表在Zone1有多个资源单元，主副本也会分布到该Zone内的多台Server中），避免跨Zone访问，适合业务量不大，同时对时延敏感的在线处理业务。
	我们也可以配置为（Zone1=Zone2）>Zone3，这时主副本会均匀分布到 Zone1 和Zone2中。这种配置比较适合三地五中心五副本方案，将业务汇聚到距离较近的城市（Zone1 和Zone2），此时虽然存在分布式事务跨 Zone 执行，但由于 Zone1 和 Zone2 城市距离较近，总体时延是可控的。此时，距离较远的城市（Zone3）将没有主副本，所以它不承接业务，只承担从副本备份的角色。

注：这个与GoldenDB的dbgroup优先级类似，一般是同城优先级高于异地灾备机房。

 

***\*Primary Zone有租户、数据库和表不同的级别\****

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E7B.tmp.jpg) 

OceanBase的Primary Zone有不同的优先级，比如我们可以设置某租户的Primary Zone的优先级为Zone1=Zone2=Zone3，主副本被打散到集群中，实现负载均衡。但某个数据库/表，对时延很敏感，可以设置该数据库或者表的Primary Zone为Zone1>Zone2>Zone3，将主副本集中到Zone1中，从而对主副本的分布实现更细粒度的管理。 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E7C.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E8C.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E8D.tmp.jpg) 

# 高可用

## 灾难恢复等级

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E8E.tmp.jpg) 

讲OB方案前，我们普及下业界公认的灾难恢复能力等级的概念，主要包括RTO和RP0，这两个指标。
	RTO指故障或灾难发生后，数据库停止工作的最高可承受时间。如果这个值为小于1小时，指灾难发生后，系统最多停止1小时的服务，1小时后可以恢复业务。比如早上8点系统故障了，那么早上9点之前系统将能够恢复。
	RPO是指当灾难发生后，数据可以恢复到的时间点，是业务系统所能容忍的数据丢失量，比如如果这个值等于1小时，如果灾难发生在9点，即使系统恢复了，可能只能恢复到8点的数据，中间1个小时的数据可能会丢失。
	基于这两个参数，业界定义了灾难恢复能力的6个等级，等级越高，灾难恢复的能力越强。
	OceanBase的RPO=0，RTO<30秒，意味着当少数派故障时，OceanBase能够在30秒内恢复业务，且不会丢失任何数据，远远达到了灾难恢复能力6级的标准。 

## 方案

### **基于通用PC服务器提供高可用**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3E8F.tmp.jpg) 

了解了标准后，我们再看看传统数据库和OceanBase的基础设施情况。因为高可用是个整体方案，除了数据库软件外，底层的硬件也同样重要。
	传统数据库一般对底层硬件有很高的要求，高端PC服务器、小型机、IPSAN 存储等等，运行在如此“豪华”的基础设施上，大量的高可用特性都由底层完成了，减轻了数据库软件的压力。
	OceanBase是运行在易损的PC服务器上的，对于一个100台服务器的集群来说，可能每周甚至每天都有服务器故障。为了在这样“简陋”的基础设施上，实现金融级的高可用性，OceanBase需要在软件层面做大量的工作，最终的结果是，OceanBase用普通硬件实现的高可用性超越了传统数据库用高端硬件实现的高可用性。 

### **自动服务接管**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3EA0.tmp.jpg) 

那么OB是如何做到这么高的可靠性呢，主要还是依靠之前讲的Paxos协议。我们用图上的例子解释下，这是一个三副本的集群，一共有3个Zone，每个Zone有2台服务器，每个Zone都有8个副本。
	我们假设Zone3-OB Server2故障了，那么它上面的4个副本都将无法提供服务了。
	其中P7是主副本，主副本故障了，业务就无法访问了。那么P7两个从副本在哪里呢？一个位于Zone1-OB Server2，另一个位于Zone2-OB Server2。虽然这两个从副本都联系不到主副本了，但他们两个依然是多数派，他们可以合法的选出一个新的主副本，并将新主副本所在服务器的IP地址更新到路由表中，应用重连后，可以访问到新主副本所在的服务器，正常完成业务。此时，虽然只有2个副本了，但所有写操作的Redo-Log日志依然可以强同步，依然可以确保RPO=0。当故障服务器恢复后，Zone3-OB Server2上的P7副本会继续加入之前的Paxos组，首先从当前主副本那里追平数据，然后可能逐步成为新的主副本，承接业务。整个主从副本切换的过程无需人工干预，对业务非常友好。
	如果只是网络故障，导致主副本无法联系到两个从副本，两个从副本也选了一个新的主副本，造成2个主副本同时存在，导致“脑裂”呢？答案是不会的，当主副本无法联系到两个从副本后，它发现自己是少数派了，它会自动卸任主副本，不再承接业务。因此，从两个从副本选出来的主副本将是唯一的主副本。

故障服务器剩余的三个副本 P5、P6、P8 都是从副本，这三个从副本本来就不承接业务，P5、P6、P8 都还都有 2 个副本可用，依然构成多数派，依然可以正常提供业务，Redo-Log日志依然可以强同步，依然可以确保 RPO=0。服务器故障恢复后，从副本从追平数据后，可以重新加入 Paxos 组。可能有人会问，那如果同时两台服务器故障呢？这个要看是哪两台服务器故障了。
	如果Zone3所在机房整体断电了，Zone3的两台服务器无法提供服务了，但Zone1和Zone2还有完整的8个副本，依然可以构成多数派，这个不影响业务。
	如果Zone3-OB Server2 和Zone2-OB Server1同时故障了，这两个服务器刚好没有重复的副本，8个副本还都有2个，依然还可以构成多数派，也不影响业务。
	除非，刚好两台有相同副本的服务器同时故障，比如Zone2-OB Server2和Zone3-OB Server2同时故障，才会影响业务。但这个对于大型集群来说，概率是非常低的。 

### **OB Server异常处理策略**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3EA1.tmp.jpg) 

一台 OB Server 故障后，系统会根据“server_permanent_offline_time”这个参数的设定来进行相应的操作。
	如果故障时间小于该参数，由于进程异常终止时间不长，异常进程可能很快就可以恢复，因此 OceanBase 暂时不做处理，以避免频繁的数据迁移。那么此时 P5-P8 只有两份副本，虽然依然满足多数派，可以保证 RPO=0，但存在风险（如果再有服务器故障）。
	如果故障时间大于该参数，也就是宕机时间比较长了，OceanBase 会将机器做“临时下线”处理，从其它 Zone 的主副本中，将缺失的数据复制到本 Zone 内剩余的机器上（需要有足够的资源），以维持副本个数。异常终止的 OB Server 进程恢复后会自动加入集群，如果已经做过“临时下线”处理，需要从本 Zone 内其它机器上（或者其它 Zone）将 unit 迁移过来。 

### **对比**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3EA2.tmp.jpg) 

上面我们介绍了 OB 的高可用模式，听起来好像跟传统数据库的主备模式差异不大，传统数据库也是依靠在主备之间同步日志来实现高可用性,那我们来对比下:
	传统数据库有“最大保护模式”,主备之间会通过网络同步 Redo-Log 日志，备数据库完成日志落盘后，告诉主数据库，主数据库反馈给业务成功。因为每份数据都有两份，一个在主数据库，一个在备数据库。这时是可以保证 RPO=0 的。但一旦备数据库故障，主数据库无法同步日志到备数据库，由于启动了“最大保护模式”，主数据库会一直等，导致业务中断，这是企业无法接受的。所以实际现场，基本没有企业会启用“最大保护模式”。
	传统数据库还有一种模式“最大性能模式”，主备之间会同步 Redo-Log 日志，主数据库将 Redo-Log 日志落盘后即通知业务完成了，同时主数据库会同步 Redo-Log 日志到备数据库，但不会等备数据库的反馈。此时，是无法保证 RPO=0 的，因为一旦日志同步不成功，同时主数据库发生灾难导致数据受损，就会造成数据丢失。
	有没有一种折中方案？有的，所以传统数据库一般会启动“最大可用模式”，正常情况下为“最大保护模式”，确保 RPO=0。

一旦备机故障或者网络抖动导致 Redo-Log 日志同步不成功，主数据库会自动切换到“最大性能模式”，保证业务不中断，但此时 RPO 肯定大于 0 了。那么如果 OB 的从副本所在服务器故障，是不是也无法保障 RPO=0 呢？OceanBase 任何从副本的故障后，主副本还有一个从副本可以同步 Redo-Log，而不用管那个故障的从副本。由于数据依然有 2 份副本在两台不同的服务器上进行强同步，依然可以保证 RPO=0。
	我们再来对比下主故障的情况。对于传统数据来说，如果主故障，会发生什么呢？无论传统数据库启用哪一种模式，主机故障后，备机都无法自动切换为主，必须需要人工干预。为什么不能自动切换呢？因为传统数据库只有主备两个，如果只是网络故障，主数据库自身并没有故障，备数据库发现联系不到主数据库后自动转正的话，可能会出现“脑裂”情况，业务会写到两个数据库里面，后期要恢复数据一般需要人工更正，这是企业最不希望看到的结果。因此需要人工干预来判定，但人工干预的话，必定要耗费大量的时间，一般需要 30 分钟以上，所以传统数据库的 RTO 一般需要大于 30 分钟。
	我们再来看下当主副本所在服务器故障后，OceanBase 是怎么做的。OceanBase 的主副本故障，剩余两个从副本会自动选出新的主来承接业务，这个过程是自动的，不需要人工干预，一般过程小于 30 秒，所以 OceanBase 可以实现 RTO<30 秒，且会降低运维难度。
	OceanBase 会不会脑裂呢？我们上一页讲了，不会的，主副本联系不到两个从副本后，它就变成少数派了，它会自动卸任主副本，不会出现两个主副本的情况。 

## 部署

### **同城两机房“主备”方案**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3EA3.tmp.jpg) 

### **两地三中心“主备”方案**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3EB4.tmp.jpg) 

### **同城三机房部署**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3EB5.tmp.jpg) 

### **三地五中心五副本**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3EB6.tmp.jpg) 

# 数据安全

# 数据压缩

# 数据迁移

# 运维/监控告警

## 用户权限管理

数据库用户管理操作包括新建用户、删除用户、修改密码、修改用户名、锁定用户、用户授权和撤销授权等。
	用户分为两类：系统租户下的用户，一般租户下的用户.
	创建用户时，如果当前会话的租户为系统租户，则新建的用户为系统租户用户，反之为一般租户下的用户。
	• 创建用户：create user ‘my_user’identified by ‘my_password’
	• 删除用户：drop user ‘my_user’
	• 给用户相应权限：grant [用户权限] on [资源对象] to username
	• 收回权限：revoke [用户权限] on [资源对象] to username
	• 查看用户：show grants for username 

## 日志查询

Observer日志： OceanBase在运行过程中会自动生成日志。维护工程师通过查看和分析日志，可以了解OceanBase的启动和运行状态
	• /home/admin/oceanbase/log
	➢ 事务/存储日志： OceanBase在事务执行过程中，会持久化事务/存储日志
	• /data/log1/集群名/ 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps3EB7.tmp.jpg) 

 

### **clog**

clog是指广义的 Commit Log，代表整个事务的所有日志信息。 

事务的日志包括： redo log, prepare log, commit log, abort log, clear log等
	• redo log记录了事务的具体操作，比如某行数据的某个字段从A修改为B
	• prepare log记录了事务的prepare状态
	• commit log表示这个事务成功commit，并记录commit信息，比如事务的全局版本号
	• clear log用于通知事务清理事务上下文
	• abort log表示这个事务被回滚所有的事务日志信息，不用于用户查看和定位系统问题 

### **ilog**

### **slog**

 

## 日常运维操作

## 数据库监控

# 故障排查

## 常见异常处理

## 灾难恢复