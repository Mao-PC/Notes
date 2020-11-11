[toc]

# Redis 集群监控

## Monitor 命令



`monitor` 是一个调试命令, 返回服务器处理的每个命令. 对于发现程序的错误非常有用. 出于安全考虑, 某些特殊管理命令 CONFIG 不会记录到 MONITOR 输出



运行一个  `monitor`命令就能降低 50% 的吞吐量, 运行多个 `monitor` 命令吞吐量会更低



## Info 命令



`info` 命令以一种易于理解和阅读的格式, 返回关于 Redis 服务器的各种信息和统计数据, 可以通过 `info [section]` 返回布防信息, 如果没有任何参数时, 默认为 default



详细的 `info` 命令可以参考 [redis-info命令详解](redis-info命令详解.md) 或者 官网文档 http://www.redis.cn/commands/info.html



## 图形化的监控工具 - RedisLive



安装步骤具体可以参考 [redislive](redislive.md)