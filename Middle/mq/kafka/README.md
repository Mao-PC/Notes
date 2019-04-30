[TOC]

# Kafka

## 简介

官网: http://kafka.apache.org/intro.html

Kafka 是 linkedin 使用 Scala 开发的具有高水平扩展和高吞吐量的分布式消息系统

Kafka 对消息保存时根据 Topic 归类, 发送消息者成为 Producer, 消息接收者成为 Consumer, 此外 Kafka 集群由多个 Kafka 实例组成, 每个实例(server)成为 Broker

无论是 Kafka 集群, 还是 producer 和 consumer 都依赖于 ZooKeeper 来保证系统可用性, 为集群保存一些 meta 信息

主流 MQ 的对比

| -             | ActiveMQ                      | RabbitMQ               | Kafka           |
| ------------- | ----------------------------- | ---------------------- | --------------- |
| 所属社区/公司 | Apache                        | Mozilla Public License | Apache LinkedIN |
| 开发语言      | Java                          | ErLang                 | Scala           |
| 支持的协议    | OpenWire,STOPM,REST,XMPP,AMQP | AMQP                   | 仿 AMQP         |
| 事务          | 支持                          | 不支持                 | 不支持          |
| 集群          | 支持                          | 支持                   | 支持            |
| 负载均衡      | 支持                          | 支持                   | 支持            |
| 动态扩容      | 不支持                        | 不支持                 | (zk)支持        |

**Kafka 的主要功能**

Kafka 是一个分布式流处理平台

流处理平台特性:

-   可以让发布和订阅流式的记录, 这一方面和消息队列或企业消息系统类似
-   可以存储流式的记录, 并且有较好 的容错性
-   可以在流式记录产生时就进行处理

适用场景:

-   构造实时流处理管道, 它可以在系统或应用直接可靠的获取数据(相当于消息队列)
-   构造实时流式应用程序, 对这些流数据进行转换或者影响

## Kafka 的核心概念

-   Kafka 作为一个集群运行在一个或者多个服务器上
-   Kafka 通过 topic 对存储的流数据进行分类
-   每条记录包含一个 key, 一个 value 和一个时间戳(timestamp)

**四个核心 API**

-   **Producer API** 允许一个应用程序发布一串流式数据到一个或多个 Kafka topic
-   **Consumer API** 允许一个程序订阅一个或多个 topic, 并且对发布给他们的流式数据进行处理
-   **Streams API** 允许一个应用程序作为一个流处理器, 消费一个或多个 topic 产生的输入流, 然后产生一个一个输出流到一个或多个 topic 中去, 在输入输出流中进行有效的交换

-   **Connector API** 允许构建并运行可重用的生产者和消费者, 将 Kafka topics 连接到已存在的应用程序或数据系统. 比如连接到一个关系型数据库, 捕捉表(table)的所有变更内容

![核心API](http://kafka.apache.org/22/images/kafka-apis.png)

**相关概念**

-   [AMQP 协议](<../rabbitmq/README.md##\ AMQP\ 协议>)
-   **Topics 和 Logs**

    是数据主题, 是数据记录发布的地方, 可以用来区分业务系统. Kafka 中的 Topics 是多订阅者模式, 一个 Topic 可以拥有一个或者多个消费者来订阅它的数据

    对于每一个 topic, Kafka 集群都会维持一个分区日志(log), 如图:

    ![topic结构](http://kafka.apache.org/22/images/log_anatomy.png)![partition结构](http://kafka.apache.org/22/images/log_consumer.png)

-   **Distribution**

    log 的分区被分布到集群的多个服务器上, 每个服务器处理它分到的分区, 根据配置每个分区还可以复制到其他服务器作为备份容错

    每个分区有一个 leader, 零或多个 follower. Leader 处理此分区所有的读写请求, 而 follower 被动的复制数据. 如果 leader 宕机, 其他的一个 follower 会被推举为一个新的 leader. 一台服务器可能是一个分区的 leader, 另一个分区的 follower. 这样可以平衡负载, 避免所有的请求都只让一台或几台服务器处理

-   **Producer**

    生产者向某个 topic 上发送消息, 生产者也选择发送到 topic 上的哪个分区. 最简单的方式是从分区列表上轮流选择, 也可以根据某种算法依照权重选择分区. 开发者负责选择分区算法

-   **Consumer**

    消费者使用一个消费组名称来进行标识发布到 topic 中的每条记录被分配给订阅者组中的一个消费者实例. 消费者可以分布在多个进程中或者多个机器上.

    -   如果所有的消费者实例在同一个消费组中, 消费记录或负载平衡到每一个消费者实例中
    -   如果所有的消费者实例在不同的消费组中, 每条消息记录会广播到所有的消费者进程

    ![消费者](http://kafka.apache.org/22/images/consumer-groups.png)

**Kafka 整体架构**

![整体架构](res/整体架构.png)

**Replication(备份)**: 为保证分布式可靠性, Kafka 0.8 开始对每个分区的数据进行备份(不同的 Broker 上), 防止一个 Broker 宕机造成分区数据不可用
