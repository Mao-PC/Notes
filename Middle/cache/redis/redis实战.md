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



### 代码示例



pom 依赖

```xml
<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-data-redis</artifactId>
     <version>1.5.17.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

启动类

```java
@SpringBootApplication
@EnableScheduling
public class RedisDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisDemoApplication.class, args);
    }

}
```

配置文件

```yml
spring:
    redis:
        database: 8
        host: 192.168.9.154
        port: 6379
        jedis:
            pool:
                max-active: 8
                max-idle: 8
                min-idle: 0
                max-wait: 1
        password: xxxxxx
```

Redis 配置文件

```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Autowired
    JedisConnectionFactory jedisConnectionFactory;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        // 设置序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        // 配置 redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
```

Redis 监听器配置

```java
@Configuration
public class RedisListenerConfiguration {

    @Autowired
    RedisConnectionFactory factory;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(factory);
        return redisMessageListenerContainer;
    }

}
```

收到 Redis 消息后任务执行类 (消费者类)

```java
@Component
public class RedisTask extends KeyExpirationEventMessageListener {

    public RedisTask(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 接收事件回调
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);

        String key = new String(message.getBody(), StandardCharsets.UTF_8);
        System.out.println("key : " + key + " channel : " + channel);

        if (key.equals("str1")) {
            //TODO 业务逻辑
            System.out.println("key str1 过期 ... 开始去检查订单");
        }
    }
}
```

Redis 的消息发布类

```java
@Component
@Slf4j
public class RedisPublisher {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    public void publish(String key) {
        redisTemplate.opsForValue().set(key, new Random().nextInt(200), 10, TimeUnit.SECONDS);
    }

    @Scheduled(cron = "15 * * * * ?")
    public void ScheduledPublish() {
        log.info("定时任务执行");
        redisTemplate.opsForValue().set("cron", new Random().nextInt(200), 10, TimeUnit.SECONDS);
    }

}
```

Controller 

```java
@RestController
public class TestController {

    @Autowired
    RedisPublisher publisher;

    @GetMapping("redis/{key}")
    public String publishEvent(@PathVariable String key) {
        // 发布事件
        publisher.publish(key);
        return "ok";
    }
}
```



测试运行

```shell
# 定时任务
2020-12-01 09:46:15.001  INFO 15388 --- [   scheduling-1] c.m.redisdemo.publisher.RedisPublisher   : 定时任务执行
key : cron channel : __keyevent@8__:expired
2020-12-01 09:47:15.001  INFO 15388 --- [   scheduling-1] c.m.redisdemo.publisher.RedisPublisher   : 定时任务执行
key : cron channel : __keyevent@8__:expired
# 浏览器请求接口 http://localhost:8080/redis/str1
key : str1 channel : __keyevent@8__:expired
key str1 过期 ... 开始去检查订单
```



## 数据计数 / 订单号 生成



分布式/高并发下的 ID 生成要求

- 全局唯一
- 趋势递增
- 效率高 (生成速度快, 使用简单, 不会使索引失效)
- 控制并发



### 生成策略

- **UUID/GUID** (通用唯一识别码)

  UUID 按照开发软件基金会(OSF)知道的标准计算. 用到了以太网卡递增(MAC), 纳秒级时间, 芯片ID码和许多可能的数字

  由一下基本法的组合:

  - 当前事情和时间
  - 时钟序列
  - 全局唯一的IEEE机器识别号 (如果有网卡, 从网卡获得, 没有网卡以其他方式获得)

  示例: `87a5e05f-eadc-4c08-bfae-11c8c1e8f186`

  使用`uuid`作为主键会有一下问题:

  - 在千万级别的单台或者20条左右的数据, 自增主键是 `uuid` 主键的 2~3 倍
  - 在范围查询特别是成百上千的数据查询自增id的效率要高于`uuid`
  - 在做汇总统计时自增主键效率要高于`uuid`
  - 在存储上, 自增id所占的空间是 `uuid` 的 1/2
  - 在备份上, 自增id要优于`uuid`
  - 在写入上, 自增id的效率是`uuid`的 3~10 倍
  - 在数据量变大需要分库分表时 , `uuid` 没有规律, 操作数据库的难度剧增

- **雪花 (SnowFlake) 算法**

  是 Twitter 开源的分布式 id 生成算法。其核心思想就是：使用一个 64 bit 的 long 型的数字作为全局唯一 id。在分布式系统中的应用十分广泛，且ID 引入了时间戳，基本上保持自增的

  **缺点: 依赖与系统时间的一致性**，如果系统时间被回调，或者改变，可能会造成id冲突或者重复。

  具体在这里不做描述 ...

- **Redis 的订单号生产策略**

  Redis的所有命令操作都是单线程的, 本身提供像 `incr` 和 `increby` 这样的自增原子命令, 所以能保证生产的 ID 肯定是唯一有序的

  - **优点**: 不亦乐乎于数据库, 灵活方便, 且性能优于数据库; 数字ID天然排序, 对于分页或者需要排序的结果很有帮助
  - **缺点**: 如果系统中没有 Redis, 需要引入新的组件, 增加系统复杂度; 需要编码和配置的工作比较大

  考虑到单节点性能瓶颈, 可以使用 Redis 集群来获取更高的吞吐量



### 基于 Redis 自增



思路: 利用增长计数 API, 业务在自增长的基础上, 配合其他信息组装成一个唯一ID



Redis的 `incr(key)` 用于将 `key` 的值进行递增, 并返回增长数值. 如果 `key` 不存在, 则创建并赋值为 0

利用Redis的特性: 单线程原子操作, 自增计数API, 数据有效期机制EX

示例: `业务编码 _ 地区 + 自增数值 (9 020 0000000001)`



### 代码示例



ID/订单号生成接口

```java
public interface IOrderService {
    String orderId();
}
```

接口实现

```java
@Service
public class RedisOrderServiceLmpl implements IOrderService {

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public String orderId() {
        String key = "mao:test:order:id"; // 系统名+模块+功能+key

        long id = redisTemplate.opsForValue().increment(key);

        String idStr = getPrefix(new Date()) + id;

        System.out.println("生成的id: " + idStr);

        return idStr;
    }

    private String getPrefix(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int day = c.get(Calendar.DAY_OF_YEAR);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String dayFmt = String.format("%1$03d", day);
        String hourFmt = String.format("%1$03d", hour);
        return (year - 2000) + dayFmt + hourFmt;
    }
}
```

测试

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RedisDemoApplication.class)
class RedisDemoApplicationTests {


    @Before
    public void start() {

    }

    @After
    public void end() {

    }

    private static final int THREAD_NUM = 300;

    private CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);

    @Autowired
    private IOrderService orderService;

    @Test
    void contextLoads() {
        for (int i = 0; i < THREAD_NUM; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                try {
                    countDownLatch.await();
                    orderService.orderId();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            countDownLatch.countDown();
        }
    }
}
```

测试结果

```
生成的id: 203360111
生成的id: 203360112
生成的id: 203360113
生成的id: 203360115
...
生成的id: 2033601144
生成的id: 2033601149
生成的id: 2033601148
生成的id: 2033601147
```

<font color="red">注意: 这里要适当调高 redis 的连接池配置, 否则会造成由于并发连接数不够而报错</font>