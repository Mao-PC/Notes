## SQL解析过程及优化

### sql优化

原因: 性能低, 执行时间太长, 等待时间太长, sql语句欠佳(连接查询), 索引失效, 服务器参数设置不合理(缓存区, 线程数 ...)

- 编写过程 : 
```sql
select .. from .. join .. on .. where .. group by .. having .. order by .. limit ..
```

- 解析过程 : 
```sql
from .. on .. join .. where .. group by .. having .. select .. order by .. limit ..
```

![sql解析过程](./res/sql解析过程.png)

> 具体SQL语句的解析过程可以点击[这里](http://www.cnblogs.com/myprogram/archive/2013/01/24/2874666.html), 上面的图也是复制这位博主的.  
> 以及这里 : [步步深入：MySQL架构总览->查询执行流程->SQL解析顺序](https://www.cnblogs.com/annsshadow/p/5037667.html)

---
[MySQL优化](./README.md)  
[主页](../../../../../)