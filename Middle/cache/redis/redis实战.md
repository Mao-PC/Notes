[toc]



# Redis 实战



## Redis 实现定时消息通知



类似 MQ 的功能



> 简单定时任务通知: 利用 redis 的 keyspace notification (即: 键过期后事件通知机制)



**开启方法**

修改 server.conf 文件, 找到 `notify-keyspace-events`, 修改为 `Ex` 



或者 使用 cli 命令: `config set notify-keyspace-events Ex`, 这个不会更改配置文件, 如果重启需要重新设置

**配置参考:**

| 参数 | 含义                                                      |
| :--: | :-------------------------------------------------------- |
|  K   | keyspace事件，事件以 `__keyspace@<db>__` 为前缀进行发布   |
|  E   | keyevent事件，事件以 `__keyevent@<db>__` 为前缀进行发布； |
|  g   | 一般性的，非特定类型的命令，比如del，expire，rename等；   |
|  $   | 字符串特定命令；                                          |
|  l   | 列表特定命令；                                            |
|  s   | 集合特定命令；                                            |
|  h   | 哈希特定命令；                                            |
|  z   | 有序集合特定命令；                                        |
|  x   | 过期事件，当某个键过期并删除时会产生该事件；              |
|  e   | 驱逐事件，当某个键因maxmemore策略而被删除时，产生该事件； |
|  A   | `g$lshzxe` 的别名，因此”AKE”意味着所有事件。              |



