[toc]

# Redis哨兵高可用机制



## 核心运作流程



**服务发现和监控检查流程**:

1. 搭建Redis主从集群
2. 启动哨兵 (客户端通过哨兵发现Redis实例信息) 
3. 哨兵通过连接 master 发现主从集群内所有的实例信息  (info replication)
4. 哨兵集控Redis实例的健康状况 (ping)



**故障切换流程** :

1. 哨兵一旦发现master不能正常提供服务, 则通知其他哨兵
2. 当一定数量的哨兵都任务master挂了
3. 选举一个哨兵作为故障转移的执行者
4. 执行者在slave中选取一个作为新的master
5. 将其他的slave重新设置为新的master的从属



## 七大核心概念



哨兵的配置文件, 在sentinel运行期间是**会被动态修改**的

```properties
# 配置文件：sentinel.conf，在sentinel运行期间是会被动态修改的
# sentinel如果重启时，根据这个配置来恢复其之前所监控的redis集群的状态
# 绑定IP
bind 0.0.0.0
# 后台运行
daemonize yes
# 默认yes，没指定密码或者指定IP的情况下，外网无法访问
protected-mode no
# 哨兵的端口，客户端通过这个端口来发现redis
port 26380
# 哨兵自己的IP，手动设定也可自动发现，用于与其他哨兵通信
# sentinel announce-ip
# 临时文件夹
dir /tmp
# 日志
logfile "/usr/local/redis/logs/sentinel-26380.log"
# sentinel监控的master的名字叫做mymaster,初始地址为 192.168.100.241 6380,2代表两个及以上哨兵认定为死亡，才认为是真的死亡
sentinel monitor mymaster 192.168.100.241 6380 2
# 发送心跳PING来确认master是否存活
# 如果master在“一定时间范围”内不回应PONG 或者是回复了一个错误消息，那么这个sentinel会主观地(单方面地)认为这个master已经不可用了
sentinel down-after-milliseconds mymaster 1000
# 如果在该时间（ms）内未能完成failover操作，则认为该failover失败
sentinel failover-timeout mymaster 3000
# 指定了在执行故障转移时，最多可以有多少个从Redis实例在同步新的主实例，在从Redis实例较多的情况下这个数字越小，同步的时间越长，完成故障转移所需的时间就越长
sentinel parallel-syncs mymaster 1
```



1. **哨兵如何知道Redis主从信息 (自动发现机制)**

   哨兵配置文件中, 保存着主从集群的master信息, 可以通过`info`命令, 进行主从信息自动发现

   

2. **什么是master主观下线**

   单个哨兵自身认为Redis示例已经下线

   **检查机制** : 哨兵向Redis发送`ping`命令, `+PONG`, `-LOADING`, `-MASTERDOWN` 这三种情况视为正常, 其他回复均视为无效

   对应的配置文件的配置项 `sentinel down-after-milliseconds mymaster 1000`

   

3. **什么是客观下线**

   一定数量的哨兵认为master已经下线

   **检查机制**: 当哨兵主观认为master下线, 则会通过 `SENTINEL is-master-down-by-addr` 命令, 询问其他哨兵是否认为master已经下线, 如果达成共识(<font color="red">达到quorum个数</font>), 就会认为master节点客观下线, 开始故障转移流程

   对应的配置文件的配置项 `sentinel monitor mymaster 192.168.100.241 6380 2`

   

4. **哨兵之间如何通信 (哨兵之间的自动发现)**

   - 哨兵之间的自动发现: 哨兵通过订阅 `_sentinel_:hello`

   - 哨兵之前通过命令直接进行通信

   - 哨兵之间通过订阅发布通信

   

5. **哪个哨兵负责故障转移? (哨兵领导选举机制)**

   基于 Raft 算法实现的选举机制, 流程简述如下:

   1. 拉票阶段: 每个哨兵节点希望自己成为领导

   2. sentinel 节点收到拉票命令后, 如果没有收到或者同意过其他sentinel节点的请求, 就同意该sentinel节点的请求 (每个sentinel只持有一个同意票数)

   3. 如果sentinel节点发现自己的票数已经超过一半的数值, name它将成为领导, 去执行故障转移

   4. 投屏结束后, 如果超过 failover-timeout 的时间内, 没有进行实际的故障操作, 则重新拉票选举

      

6. **slave选举机制**

   slave 选举方案, 按照如下顺序依次筛选:

   1. **slave节点状态**, 非 `S_DOWN` (主观下线), `O_DOWN` (客观下线), `DISCONNECTED` ()

      判断规则: `(down-after-milliseconds * 10) + milliseconds_since_master_is_in_SDOWN_state`

      SENTINEL slaves mymaster

      总的来说, 就是要挑选所有slave中状态健康的

   2. **优先级**

      redis.conf 中配置的优先级: `slave-priority` 值越小, 优先级越高

   3. **数据同步情况**

      Replication offset processed

   4. 最先的 run id

      run id  的比较方案: 字典顺序, ASCII 码

   

7. **最终主从切换的过程**

   - 针对即将成为master的slave节点, 将其撤出主从集群

     自动执行:  `slaveof NO ONE`

   - 针对其他 slave 节点, 使它们成为新 master 的从属

     自动执行: `slaveof new_master_host new_master_port`



**部署建议**

- 使用哨兵要至少部署一主二从, 三个哨兵, 如果少于三个哨兵很可能会出现误报



部署一主二从也不能保证Redis完全可用, 在主服务器和从服务器网络不通的情况下, `master-1` 节点还能够执行写操作, 但是从服务器上的sentinel会认为`master-1`下线, 然后选举出现一个新的`master-2,` 这时就会出现两个master.  也正体现了Redis集群的**非强一致**. 

Redis集群只能实现**最终一致性**, 有可能两个master节点的网络又通了, 这时就会出现数据合并. 一般来说 `master-1`的数据会被丢弃, 然后同步`master-2`的数据, 因为在重新选举是`master-2`的版本会 `+1`, 也就是说 `master-2` 的版本会高于 `master-1`