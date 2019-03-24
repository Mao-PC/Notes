## MySQL 引擎

-   InnoDB  
     支持事务，面向在线事务处理（OLTP）  
     特点：行锁，支持外键，默认读取操作不会产生锁，5.5.8 版本后的默认引擎  
     InnoDB 将数据放在一个逻辑表空间中，在 4.1 之后可以将每个 InnoDB 的表单独存放到一个独立的 idb 文件中  
     通过多版本并发控制（MVCC）来获得高并发性，实现了 SQL 标准的 4 级隔离级别，默认为 REPEATABLE。同时使用 next-key-locking 来避免幻读（phantom）。还提供了插入缓存（insert buffer）、二次写（double write）、自适应 hash 索引（adaptive hash index）、预读（read ahead）等功能。  
     对于表中存储的数据，采用了聚集（clustered）的方式，因此每张表的存储都是按照主键的顺序存放。InnoDB 的表必须有主键，如果没有显示定义主键，那么会为每一行生产一个 6 字节的 ROWID 作为主键。
-   MyISAM
    不支持事务、表锁，支持全文索引，主要面向 OLAP 数据库应用。在 5.5.8 版本之前是 MySQL 的默认引擎。  
    MyISAM 由 MYD 和 MYI 组成，MYD 用来存放数据文件，MYI 用来存放索引文件可以通过 myisampack 来压缩和解压数据文件，压缩后的表是只读的。  
    在 MySQL5.0 之前，MyISAM 默认支持表最大为 4G，5.0 之后支持 256TB
-   NDB
    集群存储引擎，数据全部存放于内存里（从 5.1 开始，可以将非索引文件放到硬盘上），主键查找速度极快。
    由于 NDB 的连接(jion)操作是在数据库层完成因此查询速度慢。

---

[MySQL 基础](./README.md)  
[MySQL 总结](../README.md)  
[主页](../../../)
