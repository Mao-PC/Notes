[TOC]

# Sharding-JDBC (Sharding-Sphere)

官网: https://shardingsphere.apache.org/index_zh.html

文档: https://shardingsphere.apache.org/document/current/cn/overview/

## 简介

ShardingSphere 是一套开源的分布式数据库中间件解决方案组成的生态圈，它由 Sharding-JDBC、Sharding-Proxy 和 Sharding-Sidecar（计划中）这 3 款相互独立的产品组成。 他们均提供标准化的数据分片、分布式事务和数据库治理功能，可适用于如 Java 同构、异构语言、容器、云原生等各种多样化的应用场景。

![](https://shardingsphere.apache.org/document/current/img/shardingsphere-scope_cn.png)

**Sharding-JDBC** 的工作原理:

![](https://shardingsphere.apache.org/document/current/img/sharding-jdbc-brief.png)

**Sharding-Proxy** 的工作原理:

就是代理模式下的数据库中间件 (和 MyCat 类似), 目前只有 MySQL 版本的

![](https://shardingsphere.apache.org/document/current/img/sharding-proxy-brief_v2.png)

Sharding-Sidecar: 目前只是计划开发, 这里不做分析

**三个组件对比**

| Sharding-JDBC | Sharding-Proxy | Sharding-Sidecar |
| ------------- | -------------- | ---------------- |
| 数据库        | 任意           | MySQL            | MySQL |
| 连接消耗数    | 高             | 低               | 高 |
| 异构语言      | 仅 Java        | 任意             | 任意 |
| 性能          | 损耗低         | 损耗略高         | 损耗低 |
| 无中心化      | 是             | 否               | 是 |
| 静态入口      | 无             | 有               | 无 |

**混合架构**

Sharding-JDBC 采用无中心化架构，适用于 Java 开发的高性能的轻量级 OLTP (联机事务处理) 应用；Sharding-Proxy 提供静态入口以及异构语言的支持，适用于 OLAP (联机分析处理) 应用以及对分片数据库进行管理和运维的场景。

ShardingSphere 是多接入端共同组成的生态圈。 通过混合使用 Sharding-JDBC 和 Sharding-Proxy，并采用同一注册中心统一配置分片策略，能够灵活的搭建适用于各种场景的应用系统，架构师可以更加自由的调整适合于当前业务的最佳系统架构。

![](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid.png)

ShardingSphere 的 3 个产品的数据分片主要流程是完全一致的。 核心由 `SQL 解析 => 执行器优化 => SQL 路由 => SQL 改写 => SQL 执行 => 结果归并`的流程组成。

![](https://shardingsphere.apache.org/document/current/img/sharding/sharding_architecture_cn.png)

其他的笔记请参考: https://github.com/Mao-PC/Notes/tree/master/Middle/db/4-%E6%95%B0%E6%8D%AE%E5%BA%93%E4%B8%AD%E9%97%B4%E4%BB%B6/Sharding-JDBC%E8%A1%A5%E5%85%85%E8%B5%84%E6%96%99
