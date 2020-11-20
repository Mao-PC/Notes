[toc]



# Redis 实战



## Redis 实现定时消息通知



类似 MQ 的功能



> 简单定时任务通知: 利用 redis 的 keyspace notification (即: 键过期后事件通知机制)



**开启方法**

修改 server.conf 文件, 找到 `notify-keyspace-events`, 修改为 `Ex` 或者 使用 cli 命令: `redis-cli config set notify-keyspace-events Ex`