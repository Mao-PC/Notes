[TOC]

# Eureka 的配置及详解

## Eureka 配置

### 服务端

在服务端主类上加 `@EnableEurekaServer`

```yml
eureka:
    instance:
        # 服务实例名
        hostname: localhost
    client:
        # 不在注册中心注册自己
        register-with-eureka: false
        # 不检索服务, 因为自己本身是注册中心, 不用去检索其他服务
        fetch-registry: false
        service-url:
            # 其他服务注册服务的 URL
            defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
    #server:
    # 禁用自我保护机制
    #enable-self-preservation: false
```

### 客户端

在客户端主类上加 `@EnableEurekaServer`

```yml
eureka:
    client:
        service-url:
            # 注册中心的 URL
            defaultZone: http://localhost:80/eureka/
    instance:
        # eureka 信息页面显示 Instances currently registered with Eureka 的实例名
        instance-id: EurekaClient81
        # 显示 IP
        prefer-ip-address: true
```

## Eureka 的自我保护机制

如果某个微服务不可用了, Eureka 不会立刻清理, 依旧会对该微服务信息进行保存 [**好死不如赖活着**]

具体的讲: 如果 Eureka 在一定时间捏没有收到某个微服务的心跳 (**默认 90 秒**), Eureka 会注销掉该实例. 但是如果网络原因导致服务与 Eureka 之间无法正常通信, 那么注销这个服务就是非常危险了 --- 因为服务本身是**健康**的, 此时不应该注销这个服务. Eureka 通过 **自我保护** 来解决这个问题: 当服务节点在短时间内丢失过多客户端时这个节点就会进入自我保护模式. 一旦进入该模式, Eureka 就会保护服务注册表中的消息, 不再删除注册服务中的信息 (也就是不会注销任何服务), 当服务恢复后会退出自我保护模式

在自我保护模式中, Eureka 会保存所有注册表中的信息, 当它收到心跳中恢复到阈值以上时, 就会退出自我保护模式. Eureka 的设计哲学就是宁可保存错误的服务注册信息, 也不盲目注销任何可能健康的服务实例.

## Eureka 集群

3 个 Eureka 的集群, IP 分别为 192.168.1.111,192.168.1.112,192.168.1.113

IP 为 192.168.1.111

```yml
server:
    port: 8088
eureka:
    instance:
        # 服务实例名
        hostname: localhost
    client:
        # 不在注册中心注册自己
        register-with-eureka: false
        # 不检索服务, 因为自己本身是注册中心, 不用去检索其他服务
        fetch-registry: false
        service-url:
            # 其他服务注册服务的 URL
            defaultZone: http://192.168.1.112:8088/eureka/,http://192.168.1.113:8088/eureka/
```

IP 为 192.168.1.112

```yml
server:
    port: 8088
eureka:
    instance:
        # 服务实例名
        hostname: localhost
    client:
        # 不在注册中心注册自己
        register-with-eureka: false
        # 不检索服务, 因为自己本身是注册中心, 不用去检索其他服务
        fetch-registry: false
        service-url:
            # 其他服务注册服务的 URL
            defaultZone: http://192.168.1.111:8088/eureka/,http://192.168.1.113:8088/eureka/
```

IP 为 192.168.1.113

```yml
server:
    port: 8088
eureka:
    instance:
        # 服务实例名
        hostname: localhost
    client:
        # 不在注册中心注册自己
        register-with-eureka: false
        # 不检索服务, 因为自己本身是注册中心, 不用去检索其他服务
        fetch-registry: false
        service-url:
            # 其他服务注册服务的 URL
            defaultZone: http://192.168.1.111:8088/eureka/,http://192.168.1.112:8088/eureka/
```

服务消费端:

```yml
eureka:
    client:
        service-url:
            # 注册中心的 URL
            defaultZone: http://192.168.1.111:8088/eureka/,http://192.168.1.112/eureka:8088/,http://192.168.1.113:8088/eureka/
    instance:
        # eureka 信息页面显示的实例名
        instance-id: EurekaClient81
        # 显示 IP
        prefer-ip-address: true
```

在任意 EurekaServer 服务商访问 `localhost:8088` 就可以在 Eureka 信息页面 DS Replicas 看到有了另外两个集群实例

## Eureka 作为注册中心比 ZooKeeper 的优势

传统的关系型数据库 (RDBMS) : Oracle/MySQL/SQLServer 主要就是要遵循 ACID

NOSQL : Redis/MongoDB 就主要遵循 CAP

-   C: Consistency 强一致性
-   A: Availablility 可用性
-   P: Partition tolerance 分区容错性

![](https://www.wangbase.com/blogimg/asset/201807/bg2018071607.jpg)

CAP 中不可能同时满足, 因此, NoSQL 数据库根据满足 AP, CP, CA 被分成了三类:

-   CA: 单点集群, 满足一致性, 可用性系统. 通常在可扩展性不太满足. RDBMS
-   CP: 满足一致性, 分区容错性. 通常性能不是特别高. Hbase, MongoDB, Redis
-   AP: 满足可用性, 分区容错性. 通常对一致性要求不高. CouchDB, Cassandra, DynamoDB, Riak

在微服务中, 不可能所有的服务都部署到一台服务器中, 所以分区容错性是必须的, 只能在 CP 和 AP 中选择

ZooKeeper 保证 CP

当注册中心查询服务列表时, 我们可以容忍注册中心返回的是几分钟之前的信息, 但不能接受服务器直接 down 掉不可用, 也就是说服务的可用性高于一致性. 但是 zk 会出现这样的情况, 当 master 节点由于网络原因与其他节点失联时, 剩余节点会重新选举 leader. 但是有时 leader 选举时间太长(30s~120s),而且选举期间 zk 是不可用的, 这就导致在选举期间注册服务瘫痪, 由于网络原因失去 leader 是较大概率发生事件, 虽然服务最终可以恢复, 但是漫长的选举而导致注册长时间不可用是不能容忍的

Eureka 保证 AP

Eureka 的各个节点是平等的,几个节点挂掉不会影响正常节点的工作, 剩余的节点仍然以提高服务的注册和查询. 而 Eureka 在注册是如果发现连接失败,则会自动切换到其他节点, 只要有一台 Eureka 还在就能注册服务(保证可用性), 只不过查到的信息可能不是最新的(不保证强一致性), 除此之外, Eureka 的自我保护机制, 如果在 15 分钟内超过 85%的都没有正常的心跳, 那么 Eureka 就会认为客户端与注册中心出现了网络故障, 此时会出现以下几种情况:

-   Eureka 不再从注册列表中移除因为长时间没有收到心跳而应该过期的服务
-   Eureka 虽然能够新服务的注册和查询, 但是不会同步到其他节点(即保证当前节点依然可用)
-   当网络稳定时, 当前实例的新注册信息才会被同步到其他节点中

因此, Eureka 可以很好应对因为网络故障导致的部分节点失联的情况, 而不会像 ZooKeeper 那样使整个注册服务瘫痪
