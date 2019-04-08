## ActiveMQ

Apache 出品，JMS1.1 和 J2EE1.4 规范 JMS Provider 实现

### ActiveMQ 和 JMS

**JMS 规范：** Java 消息服务（Java Message Service）程序接口是一个 Java 平台面向消息中间件（MOM）的 API，用于在两个应用程序之间，或分布式系统中发消息，进行异步通信。是一个于平台无关的 API。

**JMS 的对象模型：**

| 实例              | 说明                       |
| ----------------- | -------------------------- |
| ConnectionFactory | 连接工厂                   |
| Connection        | 连接                       |
| Session           | 会话                       |
| Destination       | 目的                       |
| MessageProducer   | 生产者                     |
| MessageCustomer   | 消费者                     |
| Message           | 消息                       |
| Broker            | 消息中间件示例（ActiveMQ） |

**JMS 的消息模型：**

-   **P2P（Point-to-Point）/点对点：** 消息发送到指定目的地，如发短信等待
    ![jmsp2p](res/jmsp2p.png)

-   **Pub/Sub（Publish/Subscribe）/主题（发布订阅）：** 消息会传播给所有这个消息的订阅者，如在朋友圈发动态
    ![jmspubsub](res/jmspubsub.png)

**JMS 的消息结构：**
JMS 消息一般包括：消息头、消息属性、消息体

-   消息头
    ![jms消息头](res/jms消息头.png)

-   消息属性：可以理解为消息的附加消息头，属性名可以自定义  
     属性值类型：boolean、byte、int、long、float、double、String

-   消息体：
    消息体类型：
    BytesMessage：用来传递字节消息  
    MapMessage：传递 K-V 对消息
    ObjectMessage： 传递序列号对象消息
    StreamMessage：传递文件
    TextMessage：传递字符串

**ActiveMQ 特性：** 支持多种编程语言、支持多种传输协议、有多种持久化方式

---

[中间件](../README.md)  
[主页](../../../../../)
