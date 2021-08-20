# **背景**

由于数据库的缓存一般是针对查询的内容，而且粒度也比较小，一般只有表中的数据没有发生变动的时候，数据库的缓存才会产生作用。

但这并不能减少业务逻辑对数据库的增删改操作的 IO 压力，因此缓存技术应运而生，该技术实现了对热点数据的高速缓存，可以大大缓解后端数据库的压力。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C4E.tmp.jpg) 

客户端在对数据库发起请求时，先到缓存层查看是否有所需的数据，如果缓存层存有客户端所需的数据，则直接从缓存层返回，否则进行穿透查询，对数据库进行查询。

如果在数据库中查询到该数据，则将该数据回写到缓存层，以便下次客户端再次查询能够直接从缓存层获取数据。

 

# **概述**

Redis是一个***\*远程\****（客户端和服务端可以部署在不同机器）***\*内存\****数据库（***\*非关系型数据库\****），性能强劲，具有复制特性以及解决问题而生的独一无二的数据模型。

注：与MySQL相比较，Redis不需要定义数据字典（非关系型数据库）。

它可以存储键值对与5种不同类型的值之间的映射，可以将存储在内存的键值对数据持久化到硬盘，可以使用复制特性来扩展读性能。

Redis还可以使用客户端分片来扩展写性能，内置了复制（replication），LUA 脚本（Lua scripting），LRU驱动事件（LRU eviction），事务（transactions） 和不同级别的磁盘持久化（persistence）。

并通过Redis哨兵（Sentinel）和自动分区（Cluster）提供高可用性（High Availability）。

 

***\*说明：\****

1、非关系型的键值对数据库，可以根据键以O(1)时间复杂度取出或插入关联值；

2、Redis的数据是存在内存中的；

3、键值对中键的类型可以是字符串，证书，浮点型等，并且键是唯一的；

4、键值对中值的类型可以是string，hash，list，set，sorted set等；

5、Redis内置了复制，磁盘持久化，LUA脚本，事物，SSL，客户端代理等功能；

6、通过Redis哨兵和自动分区提高可用性。

 

## **原理**

Redis发布与发布功能（Pub/Sub）是基于事件作为基本的通信机制，是目前应用比较普遍的通信模型，它的目的主要是解除消息的发布者与订阅者之间的耦合关系。

Redis作为消息发布和订阅之间的服务器，起到桥梁的作用，在Redis里面有一个channel的概念，也就是频道，发布者通过指定发布到某个频道，然后只要有订阅者订阅了该频道，该消息就会发送给订阅者，原理图如下所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C4F.tmp.jpg) 

Redis同时也可以使用list类型实现消息队列。

Redis的发布与订阅的功能应用还是比较广泛的，它的应用场景有很多。比如：最常见的就是实现实时聊天的功能，还是有就是博客的粉丝文章的推送，当博主推送原创文章的时候，就会将文章实时推送给博主的粉丝。

## **工作模式**

### **单线程**

**Redis是单进程阻塞式。**

注：Redis在同一时刻只能处理一个请求，后来的请求需要排队等待。

首先，我们明确一点，Redis6之前的**Redis4，Redis5并不是单线程程序**。通常我们说的Redis的单线程，是指Redis接受链接，接收数据并解析协议，发送结果等命令的执行，都是在主线程中执行的。

Redis之前之所以将这些都放在主线程中执行，主要有以下几方面的原因：

**1、Redis的主要瓶颈不在cpu，而在内存和网络IO**

2、使用单线程设计，可以简化数据库结构的设计

**3、可以减少多线程锁带来的性能损耗**

既然Redis的主要瓶颈不在CPU，为什么又要引入IO多线程？Redis的整体处理流程如下图：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C50.tmp.png) 

结合上图可知，当socket中有数据时，Redis会通过系统调用将数据从内核态拷贝到用户态，供Redis 解析用。这个拷贝过程是阻塞的，术语称作“同步阻塞IO”，数据量越大拷贝的延迟越高，解析协议时间消耗也越大，糟糕的是这些操作都是在主线程中处理的，特别是链接数特别多的情况下，这种情况更加明显。基于以上原因，Redis作者提出了Thread/IO线程，既将接收与发送数据来使用多线程并行处理，从而降低主线程的等待时间。

### **IO多路复用**

Linux多路复用技术，就是多个进程的IO可以注册到同一个管道上，这个管道会统一和内核进行交互。当管道中的某一个请求需要的数据准备好之后，进程再把对应的数据拷贝到用户空间中。

也就是说，通过一个线程来处理多个IO流。IO多路复用在Linux下包括了三种，select、poll、epoll，抽象来看，他们功能是类似的，但具体细节各有不同。

其实，Redis的IO多路复用程序的所有功能都是通过包装操作系统的IO多路复用函数库来实现的。每个IO多路复用函数库在Redis源码中都有对应的一个单独的文件。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C61.tmp.jpg) 

在Redis中，每当一个套接字准备好执行连接应答、写入、读取、关闭等操作时，就会产生一个文件事件。因为一个服务器通常会连接多个套接字，所以多个文件事件有可能会并发地出现。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C62.tmp.jpg) 

一旦有请求到达，就会交给 Redis 线程处理，这就实现了一个 Redis 线程处理多个 IO 流的效果。

所以，Redis选择使用多路复用IO技术来提升I/O利用率。

而之所以Redis能够有这么高的性能，不仅仅和采用多路复用技术和单线程有关，此外还有以下几个原因：

1、完全基于内存，绝大部分请求是纯粹的内存操作，非常快速。

2、数据结构简单，对数据操作也简单，如哈希表、跳表都有很高的性能。

3、采用单线程，避免了不必要的上下文切换和竞争条件，也不存在多进程或者多线程导致的切换而消耗CPU

4、使用多路I/O复用模型

 

 

Thread/IO整体实现思路：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C63.tmp.jpg) 

1、创建一组大小为io线程个数的等待队列，用来存储客户端的网络套接字。

2、分均分配客户端网络套接字到等待队列中

3、等待线程组接收解协议完毕或者发送数据完毕

4、执行后续操作，然后跳转到第2步继续执行

 

***\*选用策略：\****

1、因地制宜，优先选择时间复杂度为O(1)的I/O多路复用函数作为底层实现。

2、由于Select要遍历每一个IO，所以其时间复杂度为O(n)，通常被作为保底方案。

3、基于React设计模式监听I/O事件。

 

### **多线程**

2020年5月份，Redis正式推出了6.0版本，这个版本中有很多重要的新特性，其中多线程特性引起了广泛关注。

但是，需要提醒大家的是，Redis 6.0中的多线程，也只是针对处理网络请求过程采用了多线程，而数据的读写命令，仍然是单线程处理的。

但是，不知道会不会有人有这样的疑问：Redis不是号称单线程也有很高的性能么？不是说多路复用技术已经大大的提升了IO利用率了么，为啥还需要多线程？

主要是因为我们对Redis有着更高的要求。根据测算，Redis将所有数据放在内存中，内存的响应时长大约为100纳秒，对于小数据包，Redis服务器可以处理80,000到100,000 QPS，这么高的对于80%的公司来说，单线程的Redis已经足够使用了。但随着越来越复杂的业务场景，有些公司动不动就上亿的交易量，因此需要更大的QPS。为了提升QPS，很多公司的做法是部署Redis集群，并且尽可能提升Redis机器数。但是这种做法的资源消耗是巨大的。而经过分析，限制Redis的性能的主要瓶颈出现在网络IO的处理上，虽然之前采用了多路复用技术。但是我们前面也提到过，多路复用的IO模型本质上仍然是同步阻塞型IO模型。

下面是多路复用IO中select函数的处理过程：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C74.tmp.jpg) 

从上图我们可以看到，在多路复用的IO模型中，在处理网络请求时，调用 select（其他函数同理）的过程是阻塞的，也就是说这个过程会阻塞线程，如果并发量很高，此处可能会成为瓶颈。

虽然现在很多服务器都是多个CPU核的，但是对于Redis来说，因为使用了单线程，在一次数据操作的过程中，有大量的CPU时间片是耗费在了网络IO的同步处理上的，并没有充分的发挥出多核的优势。

如果能采用多线程，使得网络处理的请求并发进行，就可以大大的提升性能。多线程除了可以减少由于网络I/O等待造成的影响，还可以充分利用CPU的多核优势。

所以，Redis 6.0采用多个IO线程来处理网络请求，网络请求的解析可以由其他线程完成，然后把解析后的请求交由主线程进行实际的内存读写。提升网络请求处理的并行度，进而提升整体性能。

但是，Redis的多IO线程只是用来处理网络请求的，对于读写命令，Redis仍然使用单线程来处理。

那么，在引入多线程之后，如何解决并发带来的线程安全问题呢？

这就是为什么我们前面多次提到的"Redis 6.0的多线程只用来处理网络请求，而数据的读写还是单线程"的原因。Redis 6.0只有在网络请求的接收和解析，以及请求后的数据通过网络返回给时，使用了多线程。而数据读写操作还是由单线程来完成的，所以，这样就不会出现并发问题了。

 

## **速度快**

Redis 的效率很高，官方给出的数据是100000+QPS，这是因为：

1、Redis **完全基于内存**，绝大部分请求是纯粹的内存操作，执行效率高。

2、Redis 使用**单进程单线程**模型的（K，V）数据库，将数据存储在内存中，存取均不会受到硬盘IO的限制，因此其执行速度极快。

另外单线程也能处理高并发请求（IO多路复用），还可以**避免频繁上下文切换和锁的竞争**，如果想要多核运行也可以启动多个实例。

3、数据结构简单，对数据操作也简单，Redis不使用表，不会强制用户对各个关系进行关联，不会有复杂的关系限制，其存储结构就是键值对，类似于 HashMap，HashMap最大的优点就是存取的时间复杂度为O(1)。

4、Redis使用多路I/O复用模型，为非阻塞IO。

注：Redis采用的I/O多路复用函数：epoll/kqueue/evport/select（这才是其速度快的根本原因，所谓的单进程减少IPC成本，多线程减少线程切换都是次要因素）。

Redis利用了多路 I/O复用机制，处理客户端请求时，不会阻塞主线程；Redis 单纯执行（大多数指令）一个指令不到 1微秒，如此，单核 CPU 一秒就能处理 1 百万个指令（大概对应着几十万个请求吧），用不着实现多线程（网络才是瓶颈）。

**选用策略：**

·因地制宜，优先选择时间复杂度为O(1)的I/O多路复用函数作为底层实现。

·由于Select要遍历每一个IO，所以其时间复杂度为O(n)，通常被作为保底方案。

·基于React设计模式监听I/O事件。

 

## **特点**

1、Redis是一个高性能key/value内存型数据库

2、Redis支持丰富的数据类型

3、Redis支持持久化，内存数据持久化到硬盘中

4、Redis单进程，单线程，效率高，Redis实现分布式锁

### **优点**

无需处理并发问题，降低系统复杂度。

### **缺点**

不适合缓存大尺寸对象（超过100KB）

 

## **Redis VS Memcache**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C75.tmp.png) 

Memcache的代码层类似Hash，特点如下：

支持简单数据类型

***\*不支持数据持久化存储\****

***\*不支持主从\****

***\*不支持分片\****

 

Redis 特点如下：

数据类型丰富

支持数据磁盘持久化存储

支持主从

支持分片

 

注：

Memcache是内存对象缓存系统，设计目标为通过缓解数据库的压力来加快web应用的响应速度。

Redis数据类型丰富，如果仅仅当做缓存来使用，memcache更加合适。

 

## **Redis VS MySQL**

参考：

http://mp.weixin.qq.com/s?__biz=MzAxMjEwMzQ5MA==&mid=2448894296&idx=1&sn=2f6f1efc18091faffe2087d472cfa58f&chksm=8fb57375b8c2fa6397d66efdcd9dfcbe775edf74227f751257f4a72f48a429be71f18ea55426&mpshare=1&scene=24&srcid=1008QU1JJUfhU6Q9aEoOTUme&sharer_sharetime=1602156838927&sharer_shareid=33f795d236f19ac7c128b2e279563f84#rd

http://mp.weixin.qq.com/s?__biz=MzI3NDA4OTk1OQ==&mid=2649904431&idx=1&sn=b572cb449ba87284c9047e8bb272605d&chksm=f31fa7a7c4682eb1c39435f07c24b85e378352632e83b9931834f92875cdb558b19791f6c706&mpshare=1&scene=24&srcid=&sharer_sharetime=1593664174131&sharer_shareid=33f795d236f19ac7c128b2e279563f84#rd

 

# **5.0新特性**

## **Stream数据类型**

## **Timers and Cluster API**

## **RDB**

RDB现在存储LFU和LRU信息

## **集群管理器**

从Ruby移植到C

## **新sorted_set命令**

ZPOPMIN/MAX和阻塞变种

## **主动碎片整理**

## **增强HyperLogLog**

## **更好内存统计报告**

## **HELP子命令**

## **客户端连接性能**

客户端经常连接和断开连接时性能更好

## **错误修复和改进**

## **Jemalloc升级到5.1**

# **架构**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C85.tmp.jpg) 

客户端在对数据库发起请求时，先到缓存层查看是否有所需的数据，如果缓存层存有客户端所需的数据，则直接从缓存层返回，否则进行穿透查询，对数据库进行查询。

如果在数据库中查询到该数据，则将该数据回写到缓存层，以便下次客户端再次查询能够直接从缓存层获取数据。

# **部署方式**

# **数据类型**

Redis对应的数据类型，即K-V键值对中Value的数据类型。

 

Redis的数据模型如下图：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C86.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C87.tmp.jpg) 

## **String**

在Redis中String是可以修改的，称为动态字符串(Simple Dynamic String简称SDS)。说是字符串但它的内部结构更像是一个ArrayList，内部维护着一个字节数组，并且在其内部预分配了一定的空间，以减少内存的频繁分配。

最基本的数据类型，***\*其值最大可存储512M\****，二进制安全（Redis的String可以包含任何二进制数据，包含jpg对象等）。

### **内存模型**

内存存储模型：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C88.tmp.jpg) 

Redis的内存分配机制是这样：

1、当字符串的长度小于1MB时，每次扩容都是加倍现有的空间。

2、如果字符串长度超过1MB时，每次扩容时只会扩展1MB的空间。

这样既保证了内存空间够用，还不至于造成内存的浪费，字符串最大长度为 512MB。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C99.tmp.jpg) 

上图就是字符串的基本结构，其中content里面保存的是字符串内容，0x\0作为结束字符不会被计算len中。

分析一下字符串的数据结构：

struct SDS{

 T capacity;    //数组容量

 T len;       //实际长度

 byte flages;  //标志位,低三位表示类型

 byte[] content;  //数组内容

}

capacity和len两个属性都是泛型，为什么不直接用int类型？因为Redis内部有很多优化方案，为更合理的使用内存，不同长度的字符串采用不同的数据类型表示，且在创建字符串的时候len会和capacity一样大，不产生冗余的空间，所以String值可以是字符串、数字（整数、浮点数) 或者二进制。

### **应用场景**

存储key-value键值对。

注：如果重复写入key相同的键值对，后写入的会将之前写入的覆盖。

 

### **常用命令**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C9A.tmp.jpg) 

字符串（String）常用的命令：

set  [key]  [value]  给指定key设置值（set 可覆盖老的值）

get  [key]  获取指定key 的值

del  [key]  删除指定key

exists  [key]  判断是否存在指定key

mset  [key1]  [value1]  [key2]  [value2] ...... 批量存键值对

mget  [key1]  [key2] ......  批量取key

expire [key]  [time]   给指定key 设置过期时间  单位秒

setex   [key]  [time]  [value]  等价于 set + expire 命令组合

setnx  [key]  [value]  如果key不存在则set 创建，否则返回0

incr  [key]      如果value为整数 可用 incr命令每次自增1

incrby  [key] [number]  使用incrby命令对整数值 进行增加 number

## **Hash**

String元素组成的字典，适用于存储对象。

注：PHP关联数组，python字典。

 

Redis中的Hash和Java的HashMap更加相似，都是数组+链表的结构，当发生hash碰撞时将会把元素追加到链表上，值得注意的是在Redis的Hash中value只能是字符串.

hset books java "Effective java" (integer) 1

hset books golang "concurrency in go" (integer) 1

hget books java "Effective java"

hset user age 17 (integer) 1

hincrby user age 1  

\#单个key可以进行计数和incr命令基本一致 (integer) 18

Hash和String都可以用来存储用户信息 ，但不同的是Hash可以对用户信息的每个字段单独存储；String存的是用户全部信息经过序列化后的字符串，如果想要修改某个用户字段必须将用户信息字符串全部查询出来，解析成相应的用户信息对象，修改完后在序列化成字符串存入。而hash可以只对某个字段修改，从而节约网络流量，不过hash内存占用要大于String，这是hash的缺点。

 

### **内存模型**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4C9B.tmp.jpg) 

### **应用场景**

购物车：hset [key] [field] [value]命令，可以实现以用户Id，商品Id为field，商品数量为value，恰好构成了购物车的3个要素。

存储对象：hash类型的(key, field, value)的结构与对象的(对象id, 属性, 值)的结构相似，也可以用来存储对象。

 

### **常用命令**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CAC.tmp.jpg) 

hash常用的操作命令：

hset  [key]  [field] [value]   新建字段信息

hget  [key]  [field]   获取字段信息

hdel [key] [field]  删除字段

hlen  [key]  保存的字段个数

hgetall  [key]  获取指定key 字典里的所有字段和值（字段信息过多,会导致慢查询慎用：亲身经历 曾经用过这个这个指令导致线上服务故障）

hmset  [key]  [field1] [value1] [field2] [value2] ......  批量创建

hincr  [key] [field]  对字段值自增

hincrby [key] [field] [number] 对字段值增加number

## **List**

列表，按照String元素插入顺序排序。其顺序为后进先出。由于其具有栈的特性，所以可以实现如“最新消息排行榜”这类的功能。

注：python元组。

### **内存模型**

内存存储模型：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CAD.tmp.jpg) 

Redis中的list和Java中的LinkedList很像，底层都是一种链表结构，list的插入和删除操作非常快，时间复杂度为0(1)，不像数组结构插入、删除操作需要移动数据。

但是redis中的list底层可不是一个双向链表那么简单。

当数据量较少的时候它的底层存储结构为一块连续内存，称之为ziplist(压缩列表)，它将所有的元素紧挨着一起存储，分配的是一块连续的内存；当数据量较多的时候将会变成quicklist(快速链表)结构。

可单纯的链表也是有缺陷的，链表的前后指针 prev和next会占用较多的内存，会比较浪费空间，而且会加重内存的碎片化。在redis 3.2之后就都改用ziplist+链表的混合结构，称之为quicklist(快速链表)。

ziplist(压缩列表)

先看一下ziplist的数据结构，

struct ziplist<T>{

  int32 zlbytes;       //压缩列表占用字节数

  int32 zltail_offset;   

//最后一个元素距离起始位置的偏移量,用于快速定位到最后一个节点

  int16 zllength;       //元素个数

  T[] entries;       //元素内容

  int8 zlend;         //结束位 0xFF

}

int32 zlbytes： 压缩列表占用字节数

int32 zltail_offset： 

最后一个元素距离起始位置的偏移量,用于快速定位到最后一个节点

`int16 zllength`：元素个数

`T[] entries`：元素内容

`int8 zlend`：结束位 0xFF

压缩列表为了支持双向遍历，所以才会有 ztail_offset 这个字段，用来快速定位到最后一个元素，然后倒着遍历

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CAE.tmp.jpg) 

entry的数据结构：

struct entry{

  int<var> prevlen;       //前一个 entry 的长度

  int<var> encoding;       //元素类型编码

  optional byte[] content;   //元素内容

}

entry它的 prevlen 字段表示前一个 entry 的字节长度，当压缩列表倒着遍历时，需要通过这个字段来快速定位到下一个元素的位置。

### **应用场景**

由于list它是一个按照插入顺序排序的列表，所以应用场景相对还较多的，例如：

消息队列：lpop和rpush（或者反过来，lpush和rpop）能实现队列的功能

朋友圈的点赞列表、评论列表、排行榜：lpush命令和lrange命令能实现最新列表的功能，每次通过lpush命令往列表里插入新的元素，然后通过lrange命令读取最新的元素列表。

 

### **常用命令**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CAF.tmp.jpg) 

list操作的常用命名：

rpush  [key] [value1] [value2] ......   链表右侧插入

rpop  [key]  移除右侧列表头元素，并返回该元素

lpop  [key]   移除左侧列表头元素，并返回该元素

llen   [key]   返回该列表的元素个数

lrem [key] [count] [value]  删除列表中与value相等的元素，count是删除的个数。count>0表示从左侧开始查找，删除count个元素，count<0表示从右侧开始查找，删除count个相同元素，count=0表示删除全部相同的元素

注：index代表元素下标，index可以为负数，index=表示倒数第一个元素，同理index=-2表示倒数第二个元素。

lindex [key] [index]  获取list指定下标的元素（需要遍历，时间复杂度为O(n)）

lrange [key]  [start_index] [end_index]  获取list区间内的所有元素 （时间复杂度为O(n)）

ltrim  [key]  [start_index] [end_index]  保留区间内的元素，其他元素删除（时间复杂度为O(n)）

## **Set**

String元素组成的无序集合，通过哈希表实现（增删改查时间复杂度为 O(1)），不允许重复。

另外，当我们使用Smembers遍历Set中的元素时，其顺序也是不确定的，是通过 Hash 运算过后的结果。

Redis还对集合提供了求交集、并集、差集等操作，可以实现如同共同关注，共同好友等功能。

 

Redis中的set和Java中的HashSet有些类似，它内部的键值对是无序的、唯一的。它的内部实现相当于一个特殊的字典，字典中所有的value都是一个值 NULL。当集合中最后一个元素被移除之后，数据结构被自动删除，内存被回收。

### **内存模型**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CBF.tmp.jpg) 

### **应用场景**

好友、关注、粉丝、感兴趣的人集合：

1、sinter命令可以获得A和B两个用户的共同好友；

2、sismember命令可以判断A是否是B的好友；

3、scard命令可以获取好友数量；

4、关注时，smove命令可以将B从A的粉丝集合转移到A的好友集合

首页展示随机：美团首页有很多推荐商家，但是并不能全部展示，set类型适合存放所有需要展示的内容，而srandmember命令则可以从中随机获取几个。

存储某活动中中奖的用户ID ，因为有去重功能，可以保证同一个用户不会中奖两次。

 

### **常用命令**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CC0.tmp.jpg) 

sadd  [key]  [value]  向指定key的set中添加元素

smembers [key]   获取指定key集合中的所有元素

sismember [key] [value]  判断集合中是否存在某个value

scard [key]   获取集合的长度

spop  [key]  弹出一个元素

srem [key] [value]  删除指定元素

 

## **Sorted Set**

通过分数来为集合中的成员进行从小到大的排序。

***\*Redis中的sort set就采用了跳表实现\****。

 

zset也叫SortedSet一方面它是个set ，保证了内部value的唯一性，另方面它可以给每个value赋予一个score，代表这个value的排序权重。它的内部实现用的是一种叫作“跳跃列表”的数据结构。

 

### **内存模型**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CC1.tmp.jpg) 

### **应用场景**

zset可以用做排行榜，但是和list不同的是zset它能够实现动态的排序，例如：可以用来存储粉丝列表，value值是粉丝的用户ID，score是关注时间，我们可以对粉丝列表按关注时间进行排序。

zset还可以用来存储学生的成绩，value值是学生的ID，score是他的考试成绩。我们对成绩按分数进行排序就可以得到他的名次。

 

### **常用命令**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CC2.tmp.jpg) 

zadd [key] [score] [value] 向指定key的集合中增加元素

zrange [key] [start_index] [end_index] 获取下标范围内的元素列表，按score 排序输出

zrevrange [key] [start_index] [end_index]  获取范围内的元素列表 ，按score排序 逆序输出

zcard [key]  获取集合列表的元素个数

zrank [key] [value]  获取元素再集合中的排名

zrangebyscore [key] [score1] [score2]  输出score范围内的元素列表

zrem [key] [value]  删除元素

zscore [key] [value] 获取元素的score

 

更高级的Redis类型

用于计数的 HyperLogLog、用于支持存储地理位置信息的 Geo。

## **Stream**

Redis5.0新增数据类型。

1、其它5种数据结构不能实现的需求，可直接用stream实现；

2、直接贴近业务需求，提升开发效率；

3、物联网，各种传感器产生时间序列数据，定位未来。

# **操作**

String：set、get、mset、mget

Hash：hset、hget、hmset、hmget

## **数据库操作**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CD3.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CD4.tmp.jpg) 

## **Key操作**

### **DEL**

### **EXISTS**

### **EXPIRE**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CD5.tmp.jpg) 

### **KEYS**

### **MOVE**

### **PEXPIRE**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CD8.tmp.jpg) 

### PEXPIREAT

### **TTL**

### **PTTL**

### **RANDOMKEY**

### **RENAME**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CE6.tmp.jpg) 

### **TYPE**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CEA.tmp.jpg) 

## **数据操作**

## **消息操作**

### **发布消息**

Redis中发布消息的命令是publish，具体使用如下所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CEB.tmp.jpg) 

PUBLISH test "haha"：test表示频道的名称，haha表示发布的内容，这样就完成了一个一个消息的发布，后面的返回（integer）0表示0人订阅。

 

### **订阅频道**

与此同时再启动一个窗口，这个窗口作为订阅者，订阅者的命令subscribe，使用SUBSCRIBE test就表示订阅了test这个频道

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CF9.tmp.jpg) 

订阅后返回的结果中由三条信息，第一个表示类型、第二个表示订阅的频道，第三个表示订阅的数量。接着在第一个窗口进行发布消息：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CFE.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4CFF.tmp.jpg) 

可以看到发布者发布的消息，订阅者都会实时的接收到，并发订阅者收到的信息中也会出现三条信息，分别表示：返回值的类型、频道名称、消息内容。

 

### **取消订阅**

若是想取消之前的订阅可以使用unsubscribe命令，格式为：

unsubscribe  频道名称

// 取消之前订阅的test频道

unsubscribe  test

输入命令后，返回以下结果：

[root@pinyoyougou-docker src]# ./redis-cli 

127.0.0.1:6379> UNSUBSCRIBE test

\1) "unsubscribe"

\2) "test"

\3) (integer) 0

它分别表示：返回值的类型、频道的名称、该频道订阅的数量。

 

### **按模式订阅**

除了直接以特定的名城进行订阅，还可以按照模式进行订阅，模式的方式进行订阅可以一次订阅多个频道，按照模式进行订阅的命令为psubscribe，具体格式如下：

psubscribe  模式

// 表示订阅名称以ldc开头的频道

psubscribe  ldc*

输入上面的命令后，返回如下结果：

127.0.0.1:6379> PSUBSCRIBE ldc*

Reading messages... (press Ctrl-C to quit)

\1) "psubscribe"

\2) "ldc*"

\3) (integer) 1

这个也是非常简单，分别表示：返回的类型（表示按模式订阅类型）、订阅的模式、订阅数。

 

### **取消按模式订阅**

假如你想取消之前的按模式订阅，可以使用punsubscribe来取消，具体格式：

punsubscribe 模式

// 取消频道名称按照ldc开头的频道

punsubscribe ldc*

他的返回值，如下所示：

127.0.0.1:6379> PUNSUBSCRIBE ldc*

\1) "punsubscribe"

\2) "ldc*"

\3) (integer) 0

这个就不多说了，表示的意思和上面的一样，可以看到上面的命令都是有规律的订阅SUBSCRIBE，取消就是UNSUBSCRIBE，前面加前缀UN，按模式订阅也是。

 

### **查看订阅消息**

（1）你想查看某一个模式下订阅数是大于零的频道，可以使用如下格式的命令进行操作：

pubsub channels 模式

// 查看频道名称以ldc模式开头的订阅数大于零的频道

pubsub channels ldc*

（2）假如你想查看某一个频道的订阅数，可以使用如下命令：

pubsub numsub 频道名称

（3）查看按照模式的订阅数，可以使用如下命令进行操作：

pubsub numpat

# **内存**

Redis内存如果满了，怎么办？

## **内存大小设置**

Redis是基于内存的key-value数据库，因为系统的内存大小有限，所以我们在使用Redis的时候可以配置Redis能使用的最大的内存大小。

### **通过配置文件配置**

通过在Redis安装目录下面的redis.conf配置文件中添加以下配置设置内存大小：

//设置Redis最大占用内存大小为100M

maxmemory 100mb

redis的配置文件不一定使用的是安装目录下面的redis.conf文件，启动redis服务的时候是可以传一个参数指定redis的配置文件的。

### **通过命令修改**

Redis支持运行时通过命令动态修改内存大小

  //设置Redis最大占用内存大小为100M

  127.0.0.1:6379> config set maxmemory 100mb

  //获取设置的Redis能使用的最大内存大小

  127.0.0.1:6379> config get maxmemory

如果不设置最大内存大小或者设置最大内存大小为0，在64位操作系统下不限制内存大小，在32位操作系统下最多使用3GB内存。

 

## **内存淘汰**

既然可以设置Redis最大占用内存大小，那么配置的内存就有用完的时候。那在内存用完的时候，还继续往Redis里面添加数据不就没内存可用了吗？

实际上Redis定义了几种策略用来处理这种情况：

noeviction(默认策略) ：对于写请求不再提供服务，直接返回错误（DEL请求和部分特殊请求除外）

allkeys-lru：从所有key中使用LRU算法进行淘汰

volatile-lru：从设置了过期时间的key中使用LRU算法进行淘汰

allkeys-random：从所有key中随机淘汰数据

volatile-random：从设置了过期时间的key中随机淘汰

volatile-ttl：在设置了过期时间的key中，根据key的过期时间进行淘汰，越早过期的越优先被淘汰

当使用 volatile-lru、volatile-random、volatile-ttl这三种策略时，如果没有key可以被淘汰，则和noeviction一样返回错误。

 

Redis的数据淘汰策略如下图：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D0C.tmp.png) 

Redis的数据淘汰内部实现如下图：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D10.tmp.jpg) 

### **获取内存淘汰策略**

获取当前内存淘汰策略：

127.0.0.1:6379> config get maxmemory-policy

通过配置文件设置淘汰策略（修改redis.conf文件）：

maxmemory-policy allkeys-lru

通过命令修改淘汰策略：

127.0.0.1:6379> config set maxmemory-policy allkeys-lru

### **LRU算法**

LRU(Least Recently Used)，即最近最少使用，是一种***\*缓存置换算法\****。在使用内存作为缓存的时候，缓存的大小一般是固定的。当缓存被占满，这个时候继续往缓存里面添加数据，就需要淘汰一部分老的数据，释放内存空间用来存储新的数据。这个时候就可以使用LRU算法了。

其核心思想是：如果一个数据在最近一段时间没有被用到，那么将来被使用到的可能性也很小，所以就可以被淘汰掉。

 

***\*LRU在Redis中的实现\****

1、近似LRU算法

Redis使用的是近似LRU算法，它跟常规的LRU算法还不太一样。近似LRU算法通过***\*随机采样法淘汰数据\****，每次随机出5（默认）个key，从里面淘汰掉最近最少使用的key（MySQL是在5/7位置）。

可以通过maxmemory-samples参数修改采样数量：例：maxmemory-samples 10 maxmenory-samples配置的越大，淘汰的结果越接近于严格的LRU算法。

Redis为了实现近似LRU算法，给每个key额外增加了一个24bit的字段，用来存储该key最后一次被访问的时间。

2、Redis3.0对近似LRU的优化

Redis3.0对近似LRU算法进行了一些优化。新算法会维护一个***\*候选池\****（大小为16），池中的数据根据访问时间进行排序，第一次随机选取的key都会放入池中，随后每次随机选取的key只有在访问时间小于池中最小的时间才会放入池中，直到候选池被放满。当放满后，如果有新的key需要放入，则将池中最后访问时间最大（最近被访问）的移除。

当需要淘汰的时候，则直接从池中选取最近访问时间最小（最久没被访问）的key淘汰掉就行。

 

### **LFU算法**

LFU算法是Redis4.0里面新加的一种淘汰策略。它的全称是Least Frequently Used，它的核心思想是根据key的最近被访问的***\*频率\****进行淘汰，很少被访问的优先被淘汰，被访问的多的则被留下来。

***\*LFU算法能更好的表示一个key被访问的\*******\*热度\****。假如你使用的是LRU算法，一个key很久没有被访问到，只刚刚是偶尔被访问了一次，那么它就被认为是热点数据，不会被淘汰，而有些key将来是很有可能被访问到的则被淘汰了。如果使用LFU算法则不会出现这种情况，因为使用一次并不会使一个key成为热点数据。

LFU一共有两种策略：

volatile-lfu：在设置了过期时间的key中使用LFU算法淘汰key

allkeys-lfu：在所有的key中使用LFU算法淘汰数据

注：要注意的一点是这两种策略只能在Redis4.0及以上设置，如果在Redis4.0以下设置会报错。

 

# **持久化**

持久化，即将数据持久存储，而不因断电或其他各种复杂外部环境影响数据的完整性。

由于Redis将数据存储在内存而不是磁盘中，所以内存一旦断电，Redis中存储的数据也随即消失，这往往是用户不期望的，所以Redis有持久化机制来保证数据的安全性。

 

***\*Redis 如何做持久化？\****

Redis目前有两种持久化方式，即***\*RDB和AOF\****，RDB是通过保存某个时间点的***\*全量数据快照\****实现数据的持久化，当恢复数据时，直接通过RDB文件中的快照，将数据恢复。

 

## **持久化方案**

一般我们在生产上采用的持久化策略为：

***\*1、master关闭持久化\****

***\*2、slave开RDB即可，必要的时候AOF和RDB都开启\****

该策略能够适应绝大部分场景，绝大部分集群架构。

 

***\*为什么是绝大部分场景？\****

因为这套策略***\*存在部分的数据丢失可能性\****。redis的主从复制是异步的，master执行完客户端请求的命令后会立即返回结果给客户端，然后异步的方式把命令同步给slave。因此master可能还未来得及将命令传输给slave，就宕机了，此时slave变为master，数据就丢了。

幸运的是，绝大部分业务场景，都能容忍数据的部分丢失。假设，真的遇到缓存雪崩的情况，代码中也有熔断器来进行资源保护，不至于所有的请求都转发到数据库上，导致我们的服务崩溃！

注：这里的缓存雪崩是指同一时间来了一堆请求，请求的key在redis中不存在，导致请求全部转发到数据库上。

 

***\*为什么是绝大部分集群架构？\****

因为在集群中存在redis读写分离的情况，就不适合这套方案了。

幸运的是，由于采用redis读写分离架构，就必须要考虑主从同步的延迟性问题，徒增系统复杂度。

目前业内采用redis读写分离架构的项目，真的太少了。

 

***\*master关闭持久化\****

原因很简单，因为无论哪种持久化方式都会影响redis的性能，哪一种持久化都会造成CPU卡顿，影响对客户端请求的处理。***\*为了保证读写最佳性能，将master的持久化关闭！\****

注：Redis这种主从复制的方式与MySQL不同，因为它基于内存，所以提供高效的读写服务是根本，主节点本身承担了读写操作，所以应该在备节点持久化，而MySQL是主节点持久化之后再去备节点持久化。

 

***\*slave开RDB即可，必要的时候AOF和RDB都开启\****

首先，我先说明一下，我不推荐单开AOF的原因是，基于AOF的数据恢复太慢。

你要想，我们已经做了主从复制，数据已经实现备份，为什么slave还需要开持久化?

因为某一天可能因为某某工程，把机房的电线挖断了，就会导致master和slave机器同时宕机。

那么这个时候，我们需要迅速恢复集群，而RDB文件文件小、恢复快，因此灾难恢复常用RDB文件。

其次，官网也不推荐单开AOF，地址如下:

https://redis.io/topics/persistence

所以，如果实在对数据安全有一定要求，将AOF和RDB持久化都开启。

另外，做好灾难备份。利用linux的scp命令，定期将rdb文件拷贝到云服务器上。

注：scp是secure copy的简写，用于在Linux下进行远程拷贝文件的命令，和它类似的命令有cp，不过cp只是在本机进行拷贝不能跨服务器，而且scp传输是加密的。

 

## **RDB（快照）持久化**

RDB持久化是将当前进程中的数据生成***\*快照\****保存到硬盘(因此也称作快照持久化)，保存的文件后缀是rdb；当Redis重新启动时，可以读取快照文件恢复数据。这是Redis默认开启的持久化方式。

RDB持久化会在某个特定的间隔保存那个时间点的***\*全量数据的快照\****。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D1D.tmp.jpg) 

### **原理**

RDB 配置文件，redis.conf：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D1E.tmp.jpg) 

#### **客户端方式**

1、RDB的创建与载入

***\*SAVE：\*******\*阻塞Redis的服务器进程，直到RDB文件被创建完毕\****。SAVE命令很少被使用，因为其会阻塞主线程来保证快照的写入，由于Redis是使用一个主线程来接收所有客户端请求，这样会阻塞所有客户端请求。

***\*BGSAVE：\****该指令会fork出一个子进程来创建RDB文件，不阻塞服务器进程，子进程接收请求并创建RDB快照，父进程继续接收客户端的请求。

子进程在完成文件的创建时会向父进程发送信号，父进程在接收客户端请求的过程中，在一定的时间间隔通过轮询来接收子进程的信号。

我们也可以通过使用lastsave指令来查看BGSAVE是否执行成功，lastsav可以返回最后一次执行成功BGSAVE的时间。

##### **SAVE**

客户端还可以使用SAVE命令来创建一个快照，接收到SAVE命令的redis服务器在快照创建完毕之前将不再响应任何其他的命令。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D1F.tmp.jpg) 

##### **BGSAVE**

客户端可以使用BGSAVE命令来创建一个快照，当接收到客户端的BGSAVE命令时，redis会调用fork来创建一个子进程，然后子进程负责将快照写入磁盘中，而父进程则继续处理命令请求。

***\*BGSAVE的原理\****

那么RDB持久化的过程，相当于在执行bgsave命令。该命令执行过程如下图所示：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D23.tmp.jpg) 

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D31.tmp.jpg) 

***\*启动：\****

检查是否存在子进程正在执行AOF或者RDB的持久化任务。如果有则返回 false。主线程需要调用系统函数fork()，构建出一个子进程进行持久化！很不幸的是，在构建子进程的过程中，父进程就会阻塞，无法响应客户端的请求！

调用Redis源码中的rdbSaveBackground方法，方法中执行fork()产生子进程执行 RDB 操作。

注：在测试中发现，fork函数在虚拟机上较慢，真机上较快。考虑到现在都是部署在docker容器中，很少部署在真机上，为了性能，master不建议打开RDB持久化！

 

关于fork()中的Copy-On-Write。

fork()在Linux中创建子进程采用Copy-On-Write（写时拷贝技术），即如果有多个调用者同时要求相同资源（如内存或磁盘上的数据存储）。

他们会共同获取相同的指针指向相同的资源，直到某个调用者试图修改资源的内容时，系统才会真正复制一份专用副本给调用者，而其他调用者所见到的最初的资源仍然保持不变。

注：fork当一个进程创建子进程的会后，底层的操作系统会创建该进程的一个副本，在类Unix系统中创建子进程的操作会进行优化：在刚开始的时候，父子进程共享相同内存，直到父进程或子进程对内存进行了写操作之后，对被写入的内存的共享才会结束服务，即Copy On Write。

 

#### **服务端配置**

2、自动化触发RDB持久化的方式

自动化触发RDB持久化的方式如下：

根据 redis.conf配置里的SAVE m n定时触发（实际上使用的是BGSAVE）。

主从复制时，主节点自动触发。

执行Debug Reload。

执行Shutdown且没有开启AOF持久化。

##### **满足配置自动触发**

如果用户在redis.conf中设置了save配置选项，redis会在save选项条件满足之后自动触发一次BGSAVE命令，如果设置多个save配置选项，当任意一个save配置选项条件满足，redis也会触发一次BGSAVE命令。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D32.tmp.jpg) 

##### **服务器接收客户端shutdown指令**

当redis通过shutdown指令接收到关闭服务器的请求时，会执行一个save命令，阻塞所有的客户端，不再执行客户端执行发送的任何命令，并且在save命令执行完毕之后关闭服务器。

 

### **配置**

### **特点**

RDB优点：***\*全量\****数据快照，***\*文件小，恢复快\****。

RDB缺点：***\*无法保存最近一次快照之后的数据\****。

 

RDB持久化方式的缺点如下：

内存数据***\*全量同步\****，***\*数据量大的状况下，会由于I/O而严重影响性能\****。

可能会因为Redis宕机而丢失从当前至最近一次快照期间的数据。

 

## **AOF持久化**

对于快照方式持久化，存在这样一个问题，如果刚执行完一次快照持久化，客户端已有其他操作，但是还没有满足下一次快照持久化的条件，这期间如果Redis宕机，则会出现数据丢失。此时，引入AOF持久化。

 

这种方式可以将所有客户端执行的写命令记录到日志文件中，AOF持久化会将被执行的写命令写到AOF的文件末尾，以此来记录数据发生的变化，因此只要redis从头到尾执行一次AOF文件所包含的所有写命令，就可以恢复AOF文件的记录数据集。

 

AOF持久化（保存写状态）是通过***\*保存Redis的写状态\****来记录数据库的。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D36.tmp.jpg) 

相对RDB来说，RDB持久化是通过备份数据库的状态来记录数据库，而AOF持久化是备份数据库接收到的指令：

AOF记录除了查询以外的所有变更数据库状态的***\*指令\****。

以***\*增量\****的形式追加保存到 AOF 文件中。

 

### **日志重写**

AOF重写是用来在一定程度上减小AOF文件的体积。

 

#### **触发重写方式**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D44.tmp.jpg) 

#### **原理**

注意：重写AOF文件的操作，并没有读取旧的AOF文件，而是将整个内存中的数据库内容用命令的方式重写了一个新的AOF文件，替换原有的文件，这点与快照类似。

1、redis调用fork，现在有父子两个进程，子进程根据内存中的数据库快照，往临时文件中写入重建数据库状态的命令

2、父进程继续处理客户端请求，除了把写命令写入到原来的AOF文件中，同时把收到的写命令缓存起来，这样就能保证如果子进程重写失败的话并不会出问题

3、当子进程把快照内容已命令方式写入临时文件中后，子进程发信号通知父进程，然后父进程把缓存的写命令也写入到临时文件

4、现在父进程可以使用临时文件替换老的AOF文件，并重命名，后面收到的写命令也开始往新的AOF文件中追加。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D47.tmp.jpg) 

#### **AOF文件增长问题**

日志重写解决AOF文件不断增大：AOF的方式带来这样的问题，持久化文件会变得越来越大。例如我们调用incr test命令100次，文件中必须保存全部的100条命令，其实有99条是多余的。因为要恢复数据库的状态其实文件中保存一条set test 100就够了。为了压缩AOF的持久化文件，Redis提供了AOF重写机制。

 

随着写操作的不断增加，AOF文件会越来越大。于是redis有一套rewrite机制，来缩小AOF文件的体积。然而，在rewrite的过程中也是需要父进程来fork出一个子进程进行rewrite操作。至于fork函数的影响，前面已提及。

还有一个就是刷盘策略fsync，这个值推荐是配everysec,也就是Redis会默认每隔一秒进行一次fsync调用，将缓冲区中的数据写到磁盘。

然而，如果磁盘性能不稳定，fsync的调用时间超过1秒钟。此时主线程进行AOF的时候会对比上次fsync成功的时间；如果距上次不到2s，主线程直接返回；如果超过2s，则主线程阻塞直到fsync同步完成。

因此AOF也是会影响redis的性能的。

注：linux函数中，wrtie函数将数据写入文件的时候，是将数据写入操作系统的缓冲区，还并未刷入磁盘。而fsync函数，可以强制让操作系统将缓冲区数据刷入磁盘。

综上所述，我们为了保证读写性能最大化，将master的持久化关闭。

 

假设递增一个计数器100次，如果使用RDB持久化方式，我们只要保存最终结果100即可。

而AOF持久化方式需要记录下这100次递增操作的指令，而事实上要恢复这条记录，只需要执行一条命令就行，所以那一百条命令实际可以精简为一条。

Redis支持这样的功能，在不中断前台服务的情况下，可以重写AOF文件，同样使用到了COW（写时拷贝）。

***\*重写过程如下：\****

调用 fork()，创建一个子进程。

子进程把新的 AOF 写到一个临时文件里，不依赖原来的 AOF 文件。

主进程持续将新的变动同时写到内存和原来的 AOF 里。

主进程获取子进程重写 AOF 的完成信号，往新 AOF 同步增量变动。

使用新的 AOF 文件替换掉旧的 AOF 文件。

### **配置**

#### **开启AOF持久化**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D48.tmp.jpg) 

1、打开 redis.conf 配置文件，将appendonly属性改为yes。

2、修改 appendfsync属性，该属性可以接收三种参数，分别是always，everysec，no。

always表示总是即时将缓冲区内容写入AOF文件当中，everysec表示每隔一秒将缓冲区内容写入AOF文件，no表示将写入文件操作交由操作系统决定。

 

一般来说，操作系统考虑效率问题，会等待缓冲区被填满再将缓冲区数据写入 AOF 文件中。

 appendonly yes

 \#appendsync always

 appendfsync everysec

 \# appendfsync no

 

#### **日志追加频率**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D56.tmp.jpg) 

#### **修改同步频率**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D59.tmp.jpg) 

### **特点**

AOF优点：可读性高，适合保存***\*增量\****数据，***\*数据不易丢失\****。

AOF缺点：***\*文件体积大，恢复时间长\****。

 

## **RDB-AOF混合持久化方式**

Redis 4.0之后推出了此种持久化方式，***\*RDB作为全量备份，AOF作为增量备份，并且将此种方式作为默认方式使用\****。

在上述两种方式中，RDB方式是将全量数据写入RDB文件，这样写入的特点是文件小，恢复快，但无法保存最近一次快照之后的数据，AOF则将Redis指令存入文件中，这样又会造成文件体积大，恢复时间长等弱点。

在RDB-AOF方式下，持久化策略首先将缓存中数据以RDB方式全量写入文件，再将写入后新增的数据以AOF的方式追加在RDB数据的后面，在下一次做RDB持久化的时候将 AOF 的数据重新以RDB的形式写入文件。

这种方式既可以提高读写和恢复效率，也可以减少文件大小，同时可以保证数据的完整性。

在此种策略的持久化过程中，子进程会通过管道从父进程读取增量数据，在以 RDB 格式保存全量数据时，也会通过管道读取数据，同时不会造成管道阻塞。

可以说，在此种方式下的持久化文件，前半段是RDB格式的全量数据，后半段是AOF格式的增量数据。此种方式是目前较为推荐的一种持久化方式。

 

## **总结**

两种持久化方案既可以同时使用（AOF），又可以单独使用，在某种情况下也可以都不使用，具体使用哪种持久化方案取决于用户的数据和应用。

无论采用AOF还是快照机制持久化，将数据持久化到磁盘都是必要的，除了持久化外，用户还应该对持久化的文件进行备份（最好备份在多个不同的地方）。

## **性能优化**

Redis的一项重要功能就是持久化，也就是把数据复制到硬盘上。基于持久化，才有了Redis的数据恢复等功能。

但维护这个持久化的功能，也是有性能开销的。

首先说，RDB全量持久化。

这种持久化方式把 Redis中的全量数据打包成rdb文件放在硬盘上。但是执行RDB持久化过程的是原进程fork出来一个子进程，而fork这个系统调用是需要时间的，根据Redis Lab 6年前做的实验，在一台新型的AWS EC2 m1.small^13 上，fork一个内存占用1GB的Redis进程，需要700+毫秒，而这段时间，redis是无法处理请求的。

虽然现在的机器应该都会比那个时候好，但是fork的开销也应该考虑吧。为此，***\*要使用合理的RDB持久化的时间间隔，不要太频繁\****。

接下来，我们看另外一种持久化方式：AOF增量持久化。

这种持久化方式会把你发到redis server的指令以文本的形式保存下来（格式遵循 redis protocol），这个过程中，会调用两个系统调用，一个是 write(2)，同步完成，一个是 fsync(2)，异步完成。

这两部都可能是延时问题的原因：

1、write可能会因为输出的buffer满了，或者kernal正在把buffer中的数据同步到硬盘，就被阻塞了。

2、fsync的作用是确保write写入到aof文件的数据落到了硬盘上，在一个7200转/分的硬盘上可能要延时20毫秒左右，消耗还是挺大的。更重要的是，在fsync进行的时候，write可能会被阻塞。

其中，write的阻塞貌似只能接受，因为没有更好的方法把数据写到一个文件中了。但对于fsync，Redis允许三种配置，选用哪种取决于你对备份及时性和性能的平衡：

1、always：当把appendfsync设置为always，fsync会和客户端的指令同步执行，因此最可能造成延时问题，但备份及时性最好。

2、everysec：每秒钟异步执行一次fsync，此时redis的性能表现会更好，但是fsync依然可能阻塞write，算是一个折中选择。

3、no：redis不会主动出发fsync（并不是永远不fsync，那是不太可能的），而由kernel决定何时fsync。

 

## **恢复**

Redis的持久化方式如下图：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D5A.tmp.jpg) 

 

RDB和AOF文件共存情况下的恢复流程如下图：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D5B.tmp.jpg) 

从图可知，Redis启动时会先检查AOF是否存在，如果AOF存在则直接加载AOF，如果不存在AOF，则直接加载RDB文件。

 

# **事务**

## **概述**

Redis事务可以一次执行多个命令（允许在一次单独的步骤中执行一组命令），并且带有以下两个重要的保证：

批量操作在发送eXEC命令前被放入队列缓存；

收到EXEC命令后进入事务执行，事务中任意命令执行失败，其余的命令依然被执行；

在事务执行过程中，其他客户端提交的命令请求不会插入到事务执行命令序列中。

1、Redis会将一个事务中的所有命令序列化，然后按顺序地串行化执行

2、执行中不会被其他命令插入，不允许加塞。

 

## **命令**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D6D.tmp.jpg) 

一个事务从开始到执行会经历以下三个阶段：

1、开始事务

2、命令入队

3、执行事务

注：redis事务的实现机制比mysql的简单多，即串行化。

## **应用场景**

### **MULTI EXEC**

### **DISCARD放弃队列运行**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D6E.tmp.jpg) 

### **事务错误处理**

### **事务WATCH**

# **Pipeline**

Pipeline和Linux的管道类似，它可以让Redis批量执行指令。

Redis基于请求/响应模型，单个请求处理需要一一应答。如果需要同时执行大量命令，则每条命令都需要等待上一条命令执行完毕后才能继续执行，这中间不仅仅多了RTT，还多次使用了系统IO。

Pipeline由于可以批量执行指令，所以可以节省多次IO和请求响应往返的时间。但是如果指令之间存在依赖关系，则建议分批发送指令。

 

# **LUA**

## **背景**

Redis采用单进程，这样可以避免多进程带来的问题。如果多个客户端同时向Redis服务端发送消息，可能会存在结果集交集的问题：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D6F.tmp.jpg) 

Redis提供了事务支持，但是具备复杂性。此时需要将客户端的多个操作打包发送给服务端执行，这就引入LUA。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D7F.tmp.jpg) 

## **使用**

## **应用**

### **分布式锁**

### **列表分片**

# **主从复制**

## **概述**

主从复制架构仅仅用来解决数据的冗余备份，从节点仅仅用来同步数据。

注：当主节点宕机时，从节点是不能接管主节点的，它仅仅是备份数据。

 

Redis一般是使用一个Master节点来进行写操作，而若干个Slave节点进行读操作，Master和Slave分别代表了一个个不同的Redis Server实例。

另外定期的数据备份操作也是单独选择一个Slave去完成，这样可以最大程度发挥Redis的性能，为的是保证数据的***\*弱一致性和最终一致性\****。

另外，Master和Slave的数据不是一定要即时同步的，但是在一段时间后Master和Slave的数据是趋于同步的，这就是最终一致性。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D80.tmp.jpg) 

## **原理**

***\*全同步过程如下：\****

Slave 发送 Sync 命令到 Master。

Master 启动一个后台进程，将 Redis 中的数据快照保存到文件中。

Master 将保存数据快照期间接收到的写命令缓存起来。

Master 完成写文件操作后，将该文件发送给 Slave。

使用新的 AOF 文件替换掉旧的 AOF 文件。

Master 将这期间收集的增量写命令发送给 Slave 端。

 

***\*增量同步过程如下：\****

Master 接收到用户的操作指令，判断是否需要传播到 Slave。

将操作记录追加到 AOF 文件。

将操作传播到其他 Slave：对齐主从库；往响应缓存写入指令。

将缓存中的数据发送给 Slave。

 

## **搭建环境**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D81.tmp.jpg) 

# **Redis Sentinel（哨兵）**

## **概述**

Sentinel（哨兵）是Redis的高可用解决方案：由***\*一个或者多个（一般建议搭建多个哨兵，防止出现脑裂\****，单个哨兵不知道具体谁是master，如果多个哨兵则可以投票）Sentinel实例组成的Sentinel系统可以监视任意多个主服务器，以及这些主服务器属下的所有从服务器，并在被监视的主服务器进入下线状态时，自动将下线主服务器属下的某个从服务器升级为新的主服务器。简单地说，哨兵就是带有自动故障转移功能的主从架构。

注：***\*搭建哨兵架构首先需要是主从架构，哨兵是带有自动故障转移的主从架构\****。

 

主从模式弊端：当Master宕机后，Redis集群将不能对外提供写入操作。Redis Sentinel可解决这一问题。

解决主从同步Master宕机后的主从切换问题：

***\*监控：\****检查主从服务器是否运行正常。

***\*提醒：\****通过API向管理员或者其它应用程序发送故障通知。

***\*自动故障迁移：\****主从切换（在Master宕机后，将其中一个Slave转为Master，其他的Slave从该节点同步数据）。

***\*哨兵无法解决的问题：\****

1、单节点并发压力大

2、单节点内存和磁盘物理上限

## **原理**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D91.tmp.jpg) 

哨兵主要工作就是不断给master和slave发送心跳包，定时发送定时响应。

发现master宕机的时候，通知slave停止复制，然后选举一个新的slave节点。

## **搭建环境**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D92.tmp.jpg) 

## **通过springboot操作哨兵**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4D93.tmp.jpg) 

# **集群**

哨兵虽然可以解决主从复制的节点切换问题，但是存在单节点瓶颈和故障，因此引入集群解决该问题。

## **概述**

Redis在3.0开始支持Cluster模式，目前redis的集群支持节点的自动发现，支持slave-master选举和容错，支持在线分片（sharding shard）等特性。

## **架构图**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DA2.tmp.jpg) 

集群细节：

1、所有的redis节点彼此互联（PING-PONG机制），内部使用二进制协议优化传输速率和带宽；

2、节点的fail是通过集群中超过半数的节点检测失效时才生效；

3、客户端与redis节点直连，不需要中间proxy层，客户端不需要连接集群所有节点，连接集群中任意一个可用节点即可；

4、redis-cluster把所有的物理节点映射到[0-16383]slot上，cluster负责维护node<->slot<->value

## **快速查找**

***\*如何从海量数据里快速找到所需？\****

### **分片**

1、分片

按照某种规则去划分数据，分散存储在多个节点上。通过将数据分到多个Redis服务器上，来减轻单个Redis服务器的压力。

### **一致性hash算法**

2、一致性Hash算法

既然要将数据进行分片，那么通常的做法就是获取节点的Hash值，然后根据节点数求模。

但这样的方法有明显的弊端，当Redis节点数需要动态增加或减少的时候，会造成大量的Key无法被命中。所以Redis中引入了一致性Hash算法。

该算法对2^32取模，将Hash值空间组成虚拟的圆环，整个圆环按顺时针方向组织，每个节点依次为0、1、2…2^32-1。

之后将每个服务器进行Hash运算，确定服务器在这个Hash环上的地址，确定了服务器地址后，对数据使用同样的Hash算法，将数据定位到特定的Redis服务器上。

如果定位到的地方没有Redis服务器实例，则继续顺时针寻找，找到的第一台服务器即该数据最终的服务器位置。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DA3.tmp.jpg) 

### **Hash环数据倾斜**

3、Hash环的数据倾斜问题

Hash环在服务器节点很少的时候，容易遇到服务器节点不均匀的问题，这会造成数据倾斜，数据倾斜指的是被缓存的对象大部分集中在Redis集群的其中一台或几台服务器上。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DA4.tmp.jpg) 

如上图，一致性Hash算法运算后的数据大部分被存放在A节点上，而B节点只存放了少量的数据，久而久之A节点将被撑爆。

针对这一问题，可以引入***\*虚拟节点\****解决。简单地说，就是为每一个服务器节点计算多个Hash，每个计算结果位置都放置一个此服务器节点，称为虚拟节点，可以在服务器IP或者主机名后放置一个编号实现。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DA5.tmp.jpg) 

例如上图：将NodeA和NodeB两个节点分为Node A#1-A#3，NodeB#1-B#3。

# **缓存设计原则**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DB5.tmp.png) 

# **性能分析**

## **优化网络延时**

Redis的官方博客在几个地方都说，性能瓶颈更可能是网络，那么我们如何优化网络上的延时呢？

首先，如果你们使用单机部署（应用服务和Redis在同一台机器上）的话，使用Unix进程间通讯来请求Redis服务，速度比localhost局域网（学名loopback）更快。官方文档是这么说的，想一想，理论上也应该是这样的。

但很多公司的业务规模不是单机部署能支撑的，所以还是得用TCP。

Redis客户端和服务器的通讯一般使用***\*TCP长链接\****。如果客户端发送请求后需要等待Redis返回结果再发送下一个指令，客户端和Redis的多个请求就构成下面的关系：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DB6.tmp.jpg) 

（备注：如果不是你要发送的key特别长，一个TCP包完全能放下Redis指令，所以只画了一个push包）

这样这两次请求中，客户端都需要经历一段网络传输时间。

但如果有可能，完全可以使用multi-key类的指令来合并请求，比如两个GET key可以用MGET key1 key2合并。这样在实际通讯中，请求数也减少了，延时自然得到好转。

如果不能用multi-key指令来合并，比如一个SET，一个GET无法合并。怎么办？

Redis中有至少这样两个方法能合并多个指令到一个request中，一个是 MULTI/EXEC，一个是script。前者本来是构建Redis事务的方法，但确实可以合并多个指令为一个request，它到通讯过程如下。至于script，最好利用缓存脚本的sha1 hash key来调起脚本，这样通讯量更小。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DB7.tmp.jpg) 

这样确实更能减少网络传输时间，不是么？但如此以来，就必须要求这个transaction/script中涉及的key在同一个node上，所以要酌情考虑。

如果上面的方法我们都考虑过了，还是没有办法合并多个请求，我们还可以考虑合并多个responses。比如把2个回复信息合并：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DB8.tmp.jpg) 

这样，理论上可以省去1次回复所用的网络传输时间。这就是pipeline做的事情。举个ruby客户端使用pipeline的例子：

require 'redis'
	@redis = Redis.new()
	@redis.pipelined do
  	@redis.get 'key1'
  	@redis.set 'key2' 'some value'
	end
	# => [1, 2]

据说，有些语言的客户端，甚至默认就使用pipeline来优化延时问题，比如 node_redis。

另外，不是任意多个回复信息都可以放进一个TCP包中，如果请求数太多，回复的数据很长（比如get一个长字符串），TCP还是会分包传输，但使用 pipeline，依然可以减少传输次数。

pipeline 和上面的其他方法都不一样的是，它不具有原子性。所以在 cluster 状态下的集群上，实现 pipeline 比那些原子性的方法更有可能。

***\*小结一下：\****

使用 unix 进程间通信，如果单机部署

使用 multi-key 指令合并多个指令，减少请求数，如果有可能的话

使用 transaction、script 合并 requests 以及 responses

使用 pipeline 合并 response

## **警惕执行时间长的操作**

在大数据量的情况下，有些操作的执行时间会相对长，比如 KEYS *，LRANGE mylist 0 -1，以及其他算法复杂度为 O(n) 的指令。因为 Redis 只用一个线程来做数据查询，如果这些指令耗时很长，就会阻塞 Redis，造成大量延时。

尽管官方文档中说 KEYS * 的查询挺快的，（在普通笔记本上）扫描 1 百万个 key，只需 40 毫秒(参见：https://redis.io/commands/keys)，但几十 ms 对于一个性能要求很高的系统来说，已经不短了，更何况如果有几亿个 key（一台机器完全可能存几亿个 key，比如一个 key 100字节，1 亿个 key 只有 10GB），时间更长。

所以，尽量不要在生产环境的代码使用这些执行很慢的指令，这一点 Redis 的作者在博客中也提到了。另外，运维同学查询 Redis 的时候也尽量不要用。甚至，Redis Essential 这本书建议利用 rename-command KEYS '' 来禁止使用这个耗时的指令。

除了这些耗时的指令，Redis 中 transaction，script，因为可以合并多个 commands 为一个具有原子性的执行过程，所以也可能占用 Redis 很长时间，需要注意。

如果你想找出生产环境使用的「慢指令」，那么可以利用 SLOWLOG GET count 来查看最近的 count 个执行时间很长的指令。至于多长算长，可以通过在 redis.conf 中设置 slowlog-log-slower-than 来定义。

除此之外，在很多地方都没有提到的一个可能的慢指令是 DEL，但 redis.conf 文件的注释中倒是说了。长话短说就是 DEL 一个大的 object 时候，回收相应的内存可能会需要很长时间（甚至几秒），所以，建议用 DEL 的异步版本：UNLINK。后者会启动一个新的 thread 来删除目标 key，而不阻塞原来的线程。

更进一步，当一个 key 过期之后，Redis 一般也需要同步的把它删除。其中一种删除 keys 的方式是，每秒 10 次的检查一次有设置过期时间的 keys，这些 keys 存储在一个全局的 struct 中，可以用 server.db->expires 访问。检查的方式是：

1、从中随机取出 20 个 keys

2、把过期的删掉。

3、如果刚刚 20 个 keys 中，有 25% 以上（也就是 5 个以上）都是过期的，Redis 认为，过期的 keys 还挺多的，继续重复步骤 1，直到满足退出条件：某次取出的 keys 中没有那么多过去的 keys。

这里对于性能的影响是，如果真的有很多的 keys 在同一时间过期，那么 Redis 真的会一直循环执行删除，占用主线程。

对此，Redis 作者的建议[10]是警惕 EXPIREAT 这个指令，因为它更容易产生 keys 同时过期的现象。我还见到过一些建议是给 keys 的过期时间设置一个随机波动量。最后，redis.conf 中也给出了一个方法，把 keys 的过期删除操作变为异步的，即，在 redis.conf 中设置 lazyfree-lazy-expire yes。

## **优化数据结构、使用正确的算法**

一种数据类型（比如 string，list）进行增删改查的效率是由其底层的存储结构决定的。

我们在使用一种数据类型时，可以适当关注一下它底层的存储结构及其算法，避免使用复杂度太高的方法。举两个例子：

1、ZADD 的时间复杂度是 O(log(N))，这比其他数据类型增加一个新元素的操作更复杂，所以要小心使用。

2、若 Hash类型的值的 fields 数量有限，它很有可能采用 ziplist 这种结构做存储，而 ziplist的查询效率可能没有同等字段数量的 hashtable 效率高，在必要时，可以调整Redis的存储结构。

除了时间性能上的考虑，有时候我们还需要节省存储空间。比如上面提到的 ziplist 结构，就比hashtable 结构节省存储空间（Redis Essentials 的作者分别在 hashtable 和 ziplist 结构的 Hash 中插入 500 个 fields，每个 field 和 value 都是一个15位左右的字符串，结果是 hashtable 结构使用的空间是 ziplist 的 4 倍）。但节省空间的数据结构，其算法的复杂度可能很高。所以，这里就需要在具体问题面前做出权衡。

如何做出更好的权衡？我觉得得深挖 Redis 的存储结构才能让自己安心。这方面的内容我们下次再说。

 

以上这三点都是编程层面的考虑，写程序时应该注意啊。下面这几点，也会影响 Redis 的性能，但解决起来，就不只是靠代码层面的调整了，还需要架构和运维上的考虑。

## **考虑操作系统和硬件是否影响性能**

Redis运行的外部环境，也就是操作系统和硬件显然也会影响 Redis 的性能。在官方文档中，就给出了一些例子：

CPU：Intel 多种 CPU 都比 AMD 皓龙系列好

虚拟化：实体机比虚拟机好，主要是因为部分虚拟机上，硬盘不是本地硬盘，监控软件导致 fork 指令的速度慢（持久化时会用到 fork），尤其是用 Xen 来做虚拟化时。

内存管理：在 linux 操作系统中，为了让 translation lookaside buffer，即 TLB，能够管理更多内存空间（TLB 只能缓存有限个 page），操作系统把一些 memory page 变得更大，比如 2MB 或者 1GB，而不是通常的 4096 字节，这些大的内存页叫做 huge pages。同时，为了方便程序员使用这些大的内存 page，操作系统中实现了一个 transparent huge pages（THP）机制，使得大内存页对他们来说是透明的，可以像使用正常的内存 page 一样使用他们。但这种机制并不是数据库所需要的，可能是因为 THP 会把内存空间变得紧凑而连续吧，就像mongodb 的文档[11]中明确说的，数据库需要的是稀疏的内存空间，所以请禁掉 THP 功能。Redis 也不例外，但 Redis 官方博客上给出的理由是：使用大内存 page 会使 bgsave 时，fork 的速度变慢；如果 fork 之后，这些内存 page 在原进程中被修改了，他们就需要被复制（即 copy on write），这样的复制会消耗大量的内存（毕竟，人家是huge pages，复制一份消耗成本很大）。所以，请禁止掉操作系统中的transparent huge pages 功能。

交换空间：当一些内存page被存储在交换空间文件上，而Redis又要请求那些数据，那么操作系统会阻塞Redis进程，然后把想要的page，从交换空间中拿出来，放进内存。这其中涉及整个进程的阻塞，所以可能会造成延时问题，一个解决方法是禁止使用交换空间（Redis Essentials中如是建议，如果内存空间不足，请用别的方法处理）。

## **考虑持久化带来的开销**

## **使用分布式架构—读写分离、数据分片**

以上，我们都是基于单台，或者单个Redis服务进行优化。下面，我们考虑当网站的规模变大时，利用分布式架构来保障Redis性能的问题。

首先说，哪些情况下不得不（或者最好）使用分布式架构：

1、数据量很大，单台服务器内存不可能装得下，比如1个T这种量级

2、需要服务高可用

3、单台的请求压力过大

解决这些问题可以采用数据分片或者主从分离，或者两者都用（即，在分片用的cluster节点上，也设置主从结构）。

这样的架构，可以为性能提升加入新的切入点：

1、把慢速的指令发到某些从库中执行

2、把持久化功能放在一个很少使用的从库上

3、把某些大list分片

其中前两条都是根据Redis单线程的特性，用其他进程（甚至机器）做性能补充的方法。

当然，使用分布式架构，也可能对性能有影响，比如请求需要被转发，数据需要被不断复制分发。

 

# **优化**

## **业务层面**

业务层面主要是开发人员需要关注，也就是开发人员在写业务代码时，如何合理地使用Redis。开发人员需要对Redis有基本的了解，才能在合适的业务场景使用Redis，从而避免业务层面导致的延迟问题。

在开发过程中，业务层面的优化建议如下：

·key的长度尽量要短，在数据量非常大时，过长的key名会占用更多的内存

·一定避免存储过大的数据（大value），过大的数据在分配内存和释放内存时耗时严重，***\*会阻塞主线程\****

·Redis 4.0以上建议开启lazy-free机制，释放大value时异步操作，不阻塞主线程

·建议设置过期时间，把Redis当做缓存使用，尤其在数量很大的时，不设置过期时间会导致内存的无限增长

·不使用复杂度过高的命令，例如SORT、SINTER、SINTERSTORE、ZUNIONSTORE、ZINTERSTORE，使用这些命令耗时较久，会阻塞主线程

·查询数据时，一次尽量获取较少的数据，在不确定容器元素个数的情况下，避免使用LRANGE key 0 -1，ZRANGE key 0 -1这类操作，应该设置具体查询的元素个数，推荐一次查询100个以下元素

·写入数据时，一次尽量写入较少的数据，例如HSET key value1 value2 value3...，控制一次写入元素的数量，推荐在100以下，大数据量分多个批次写入

·批量操作数据时，用MGET/MSET替换GET/SET、HMGET/MHSET替换HGET/HSET，减少请求来回的网络IO次数，降低延迟，对于没有批量操作的命令，推荐使用pipeline，一次性发送多个命令到服务端

·禁止使用KEYS命令，需要扫描实例时，建议使用SCAN，线上操作一定要控制扫描的频率，避免对Redis产生性能抖动

·避免某个时间点集中过期大量的key，集中过期时推荐增加一个随机时间，把过期时间打散，降低集中过期key时Redis的压力，避免阻塞主线程

·根据业务场景，选择合适的淘汰策略，通常随机过期要比LRU过期淘汰数据更快

·使用连接池访问Redis，并配置合理的连接池参数，避免短连接，TCP三次握手和四次挥手的耗时也很高

·只使用db0，不推荐使用多个db，使用多个db会增加Redis的负担，每次访问不同的db都需要执行SELECT命令，如果业务线不同，建议拆分多个实例，还能提高单个实例的性能

·读的请求量很大时，推荐使用读写分离，前提是可以容忍从节数据更新不及时的问题

·写请求量很大时，推荐使用集群，部署多个实例分摊写压力

## **运维层面**

运维层面主要是DBA需要关注的，目的是合理规划Redis的部署和保障Redis的稳定运行，主要优化如下：

·不同业务线部署不同的实例，各自独立，避免混用，推荐不同业务线使用不同的机器，根据业务重要程度划分不同的分组来部署，避免某一个业务线出现问题影响其他业务线

·保证机器有足够的CPU、内存、带宽、磁盘资源，防止负载过高影响Redis性能

·以master-slave集群方式部署实例，并分布在不同机器上，避免单点，slave必须设置为readonly

·master和slave节点所在机器，各自独立，不要交叉部署实例，通常备份工作会在slave上做，做备份时会消耗机器资源，交叉部署会影响到master的性能

·推荐部署哨兵节点增加可用性，节点数量至少3个，并分布在不同机器上，实现故障自动故障转移

·提前做好容量规划，一台机器部署实例的内存上限，最好是机器内存的一半，主从全量同步时会占用最多额外一倍的内存空间，防止网络大面积故障引发所有master-slave的全量同步导致机器内存被吃光

·做好机器的CPU、内存、带宽、磁盘监控，在资源不足时及时报警处理，Redis使用Swap后性能急剧下降，网络带宽负载过高访问延迟明显增大，磁盘IO过高时开启AOF会拖慢Redis的性能

·设置最大连接数上限，防止过多的客户端连接导致服务负载过高

·单个实例的使用内存建议控制在20G以下，过大的实例会导致备份时间久、资源消耗多，主从全量同步数据时间阻塞时间更长

·设置合理的slowlog阈值，推荐10毫秒，并对其进行监控，产生过多的慢日志需要及时报警

·设置合理的复制缓冲区repl-backlog大小，适当调大repl-backlog可以降低主从全量复制的概率

·设置合理的slave节点client-output-buffer-limit大小，对于写入量很大的实例，适当调大可以避免主从复制中断问题

·备份时推荐在slave节点上做，不影响master性能

·不开启AOF或开启AOF配置为每秒刷盘，避免磁盘IO消耗降低Redis性能

·当实例设置了内存上限，需要调大内存上限时，先调整slave再调整master，否则会导致主从节点数据不一致

·对Redis增加监控，监控采集info信息时，使用长连接，频繁的短连接也会影响Redis性能

·线上扫描整个实例数时，记得设置休眠时间，避免扫描时QPS突增对Redis产生性能抖动

·做好Redis的运行时监控，尤其是expired_keys、evicted_keys、latest_fork_usec指标，短时间内这些指标值突增可能会阻塞整个实例，引发性能问题

 

# **应用**

## **分布式缓存**

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DC9.tmp.jpg) 

有上述可以，内存访问速度要远远大于磁盘访问速度，所以可以通过redis提高访问速度，避免对数据库的并发访问。

## **会话缓存**

可以使用Redis来统一存储多台应用服务器的会话信息。当应用服务器不再存储用户的会话信息，也就不再具有状态，一个用户可以请求任意一个应用服务器，从而更容易实现高可用性以及可伸缩性。

## **分布式锁**

分布式锁是控制分布式系统之间共同访问共享资源的一种锁的实现。如果一个系统，或者不同系统的不同主机之间共享某个资源时，往往需要互斥，来排除干扰，满足数据一致性。

***\*分布式锁需要解决的问题如下：\****

***\*互斥性\****：任意时刻只有一个客户端获取到锁，不能有两个客户端同时获取到锁。

***\*安全性\****：锁只能被持有该锁的客户端删除，不能由其他客户端删除。

***\*死锁\****：获取锁的客户端因为某些原因而宕机继而无法释放锁，其他客户端再也无法获取锁而导致死锁，此时需要有特殊机制来避免死锁。

***\*容错\****：当各个节点，如某个Redis节点宕机的时候，客户端仍然能够获取锁或释放锁。

 

***\*如何使用Redis实现分布式锁\****

使用SETNX实现，SETNX key value：如果Key不存在，则创建并赋值。

该命令时间复杂度为O(1)，如果设置成功，则返回1，否则返回0。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DCA.tmp.jpg) 

由于SETNX指令操作简单，且是原子性的，所以初期的时候经常被人们作为分布式锁，我们在应用的时候，可以在某个共享资源区之前先使用SETNX指令，查看是否设置成功。

如果设置成功则说明前方没有客户端正在访问该资源，如果设置失败则说明有客户端正在访问该资源，那么当前客户端就需要等待。

但是如果真的这么做，就会存在一个问题，因为SETNX是长久存在的，所以假设一个客户端正在访问资源，并且上锁，那么当这个客户端结束访问时，该锁依旧存在，后来者也无法成功获取锁，这个该如何解决呢？

由于SETNX并不支持传入EXPIRE参数，所以我们可以直接使用EXPIRE指令来对特定的Key来设置过期时间。

用法：

EXPIRE key seconds

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DCB.tmp.jpg) 

***\*程序：\****

RedisService redisService = SpringUtils.getBean(RedisService.class);

long status = redisService.setnx(key,"1");

if(status == 1){

 redisService.expire(key,expire);

 doOcuppiedWork();

}

这段程序存在的问题：假设程序运行到第二行出现异常，那么程序来不及设置过期时间就结束了，则Key会一直存在，等同于锁一直被持有无法释放。

出现此问题的根本原因为：原子性得不到满足。

***\*解决：\****从Redis 2.6.12版本开始，我们就可以使用Set操作，将SETNX和EXPIRE融合在一起执行，具体做法如下：

EX second：设置键的过期时间为Second秒。

PX millisecond：设置键的过期时间为MilliSecond毫秒。

NX：只在键不存在时，才对键进行设置操作。

XX：只在键已经存在时，才对键进行设置操作。

SET KEY value [EX seconds] [PX milliseconds] [NX|XX]

注：SET操作成功完成时才会返回OK，否则返回 nil。

有了SET我们就可以在程序中使用类似下面的代码实现分布式锁了：

RedisService redisService = SpringUtils.getBean(RedisService.class);

String result = redisService.set(lockKey,requestId,SET_IF_NOT_EXIST,SET_WITH_EXPIRE_TIME,expireTime);

if("OK.equals(result)"){

 doOcuppiredWork();

}

## **异步队列**

1、使用Redis中的List作为队列

使用上文所说的Redis的数据结构中的List作为队列rpush生产消息，lpop消费消息。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DDC.tmp.jpg) 

此时我们可以看到，该队列是使用Rpush生产队列，使用LPOP消费队列。

在这个生产者-消费者队列里，当LPOP没有消息时，证明该队列中没有元素，并且生产者还没有来得及生产新的数据。

***\*缺点：\****LPOP不会等待队列中有值之后再消费，而是直接进行消费。

***\*弥补：\****可以通过在应用层引入Sleep机制去调用LPOP重试。

 

2、使用BLPOP key [key…] timeout

BLPOP key [key …] timeout：阻塞直到队列有消息或者超时。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DDD.tmp.jpg) 

***\*缺点：\****按照此种方法，我们生产后的数据只能提供给各个单一消费者消费。能否实现生产一次就能让多个消费者消费呢？

3、Pub/Sub：主题订阅者模式

发送者（Pub）发送消息，订阅者（Sub）接收消息。订阅者可以订阅任意数量的频道。

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DDE.tmp.jpg) 

Pub/Sub模式的缺点：消息的发布是无状态的，无法保证可达。对于发布者来说，消息是“即发即失”的。

此时如果某个消费者在生产者发布消息时下线，重新上线之后，是无法接收该消息的，要解决该问题需要使用专业的消息队列，如 Kafka…此处不再赘述。

 

## **数据存储**

需要具备完整的持久化机制。

## **计数器与数量控制**

版本1：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DDF.tmp.jpg) 

版本2：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DEF.tmp.jpg) 

 

***\*应用场景：\****

1、频率控制：接口防刷，密码尝试次数限制；

2、数量统计：请求量统计；

3、数量控制：商品抢购，奖励额度控制。

 

## **分布式ID生成**

利用自增特性，一次请求一个大一点的步长如incr 2000，缓存在本地使用，用完再请求。

## **海量数据查询**

假设Redis中有十亿条Key，如何从这么多Key中找到固定前缀的Key？
	***\*方法1：\****使用 Keys [pattern]：查找所有符合给定模式Pattern的Key

使用Keys [pattern]指令可以找到所有符合Pattern条件的Key，但是Keys会一次性返回所有符合条件的Key，所以会造成Redis的卡顿。

假设Redis此时正在生产环境下，使用该命令就会造成隐患，另外如果一次性返回所有Key，对内存的消耗在某些条件下也是巨大的。

***\*例：\****

keys test* //返回所有以test为前缀的key

***\*方法2：\****使用 SCAN cursor [MATCH pattern] [COUNT count]

***\*注：\****

cursor：游标

MATCH pattern：查询Key的条件

Count：返回的条数

SCAN是一个基于游标的迭代器，需要基于上一次的游标延续之前的迭代过程。

SCAN以0作为游标，开始一次新的迭代，直到命令返回游标 0 完成一次遍历。

此命令并不保证每次执行都返回某个给定数量的元素，甚至会返回0个元素，但只要游标不是0，程序都不会认为SCAN命令结束，但是返回的元素数量大概率符合Count参数。另外，SCAN支持模糊查询。

***\*例：\****

SCAN 0 MATCH test* COUNT 10 //每次返回10条以test为前缀的key

 

## **海量数据统计**

位图（bitmap）：存储是否参加某次活动，是否已读某篇文章，用户是否为会员，日活统计。

## **签到功能**

现在的网站和app开发中，签到是一个很常见的功能，如微博签到送积分，签到排行榜。

用户签到是提高用户粘性的有效手段，用的好能事半功倍！

下面我们从技术方面看看常用的实现手段：

***\*方案一：直接存到数据库MySQL\****

用户表如下：

![img](file:///C:\Users\大力\AppData\Local\Temp\ksohtml\wps4DF0.tmp.jpg) 

last_checkin_time 上次签到时间

checkin_count 连续签到次数

记录每个用户签到信息

***\*签到流程\****

1、用户第一次签到

last_checkin_time = time()

checkin_count=1

2、用户非第一次签到，且当天已签到

什么也不做，返回已签到。

3、用户非第一次签到，且当天还未签到

a、昨天也有签到

last_checkin_time = time()

checkin_count= checkin_count+1

b、昨天没有签到

last_checkin_time = time()

checkin_count=1

使用yii实现的代码如下：

//0点

$today_0 = strtotime(date('y-m-d'));

//昨天0点

$yesterday_0 = $today_0-24*60*60;

$last_checkin_time = 	$model->last_checkin_time;if(empty($last_checkin_time)){

 //first checkin

 $model->last_checkin_time = time();

 $model->checkin_count = 1;  

}else{

 if($today_0 < $last_checkin_time){

 //checkin ed 当天已签到过

 return json_encode(['code' => 0, 'msg' => '已签到成功']);

 } //昨天签到过 

if($last_checkin_time < $today_0 && $last_checkin_time > $yesterday_0){

 $model->last_checkin_time = time();

 $model->checkin_count = $model->checkin_count + 1; 

 }else{

 //昨天没签到过，重新计数

 $model->last_checkin_time = time();

 $model->checkin_count = 1;

 }}$rs = $model->save();

***\*方案二：redis实现方案\****

使用bitmap来实现，bitmap是redis 2.2版本开始支持的功能，一般用于标识状态。

另外，用bitmap进行当天有多少人签到非常的方便，使用bitcount

count = redis->BITCOUNT($key);

***\*签到流程：\****

设置两个bitmap：

一个以每天日期为key ，每个uid为偏移量

一个以用户uid为key ，当天在一年中的索引为偏移量

这样记录一个用户一年的签到情况仅需要365*1bit

以下是签到代码

//每天一个key

 $key = 'checkin_' . date('ymd');

 if($redis->getbit($key, $uid)){

  //已签到  return json_encode(['code' => 0, 'msg' => '已签到成功']);

 }else{

  //签到  $redis->setbit($key, $uid, 1);

  $redis->setbit('checkin_'.$uid , date('z'), 1);

 }

以下是用户连续签到计算

public static function getUserCheckinCount($uid){

 $key =  'checkin_'.$uid;

 $index = date('z');

 $n = 0;

 for($i = $index; $i>=0;$i--){

  $bit = Yii::$app->redis->getbit($key, $i);  if($bit == 0) break;

  $n++;  }  return $n;

 }

以下是计算一天签到用户数

$key = 'checkin_' . date('ymd');

$redis = Yii::$app->redis;$count = $redis->BITCOUNT($key);

***\*优缺点比较\****

1、直接MySQL

思路简单，容易实现；

缺点：占用空间大，表更新比较多，影响性能，数据量大时需要用cache辅助；

2、Redis bitmap

优点：

占用空间很小，纯内存操作，速度快；

缺点：

记录的信息有限，只有一个标识位；

偏移量不能大于2^32，512M；大概可以标识5亿个bit位，绝大多数的应用都是够用的；

偏移量很大的时候可能造成Redis服务器被阻塞；所以要考虑切分。

## **秒杀系统**