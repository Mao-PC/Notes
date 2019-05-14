[TOC]

# Memcached

## Memcached 入门

文档地址: https://github.com/memcached/memcached/wiki

Memcached 是一个缓存系统, 通过减轻数据库负载加速动态 web 应用

-   本质就是一个内存 K-V 缓存
-   协议简单, 是基于文本行的协议
-   不支持数据库持久化, 服务器关闭之后数据全部丢失
-   简洁而强大, 上手容易
-   没有安全机制

**Memcached 的设计理念**:

-   简单的 K-V 存储, 服务器不关心数据时什么, 只管存储数据
-   服务端功能简单, 很多逻辑需要客户端实现.
    -   服务端专注如何存储, 合适清除和重用内存
    -   客户端专注如何选择读取和写入服务器, 以及无法联系到服务器时的操作
-   Memcached 实例之间没有通信机制
-   每个命令的复杂度为 O(1). 慢速机上查询应该在 1ms 以下, 高端服务器可达每秒数百万
-   缓存自动清除机制
-   缓存失效机制

具体搭建过程请参考: [memcached 单机到集群完整搭建过程](https://github.com/Mao-PC/Notes/blob/master/Middle/cache/memcached/memcached%E5%8D%95%E6%9C%BA%E5%88%B0%E9%9B%86%E7%BE%A4%E5%AE%8C%E6%95%B4%E6%90%AD%E5%BB%BA%E8%BF%87%E7%A8%8B.md.pdf)

**常用命令**

![常用命令](res/常用命令.png)

Java 客户端 (xmemcached)
