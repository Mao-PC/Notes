[TOC]

# RocketMQ

## 环境搭建

```sh
# 获取RocketMQ
wget http://mirrors.tuna.tsinghua.edu.cn/apache/rocketmq/4.5.0/rocketmq-all-4.5.0-bin-release.zip

# 解压
unzip -d /usr rocketmq-all-4.5.0-bin-release.zip

mv /usr/rocketmq-all-4.5.0-bin-release/ /usr/rocketmq
```

RocketMQ 的 NameServer 默认占用 4G, Broker 占用 8G, 启动服务是一定要注意, 如果不修改启动参数, 很有可能出现问题

修改配置文件 `runserver.sh`, 根据自己的服务器大小修改 :

```sh
#===========================================================================================
# JVM Configuration
#===========================================================================================
JAVA_OPT="${JAVA_OPT} -server -Xms500m -Xmx500m -Xmn500m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
```

修改 `runbroker.sh`:

```sh
#===========================================================================================
# JVM Configuration
#===========================================================================================
JAVA_OPT="${JAVA_OPT} -server -Xms500m -Xmx500m -Xmn500m"
```

停止和启动命令:

```sh
cd /usr/rocketmq
# 启动nameserver
nohub sh bin/mqnamesrv > logs/rocketmqlogs/namesrv.log 2>&1 &

#启动broker
nohub sh bin/mqbroker -n localhost:9876 > logs/rocketmqlogs/broker.log 2>&1 &

# 停止broker
bin/mqshutdown broker

# 停止nameserver
bin/mqshutdown namesrv
```

测试是否启动成功:

```sh
# 发送消息
export NAMESRV_ADDR=localhost:9876
bin/tools.sh org.apache.rocketmq.example.quickstart.Producer

# 接收消息
sh bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer
```

[代码示例](https://github.com/Mao-PC/Notes/tree/master/%E8%B5%84%E6%96%99/subject-2-mq-master/rocketmq-demo1/src/main/java/com/study/rocketmq)

## RocketMQ 架构方案及角色详解

**架构方案**

![架构方案](res/架构方案.png)

**NameServer Cluster**

提供轻量级服务发现和路由, 每个名称服务器记录完整的路由信息, 提供相应的读写服务, 并支持快速存储扩展.
