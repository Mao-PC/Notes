## JVM 的参数及调优

<font color='red'>$系统瓶颈核心还是在应用代码，一般情况下无需过多调优，JVM 本身在不断优化$</font>

### 调优的基本概念

在调整性能时 JVM 有三个组件：

1. 堆大小的调整
2. 垃圾收集器的调整
3. JIT 编译器

大多数调优选项都与调整堆大小和为具体情况选择最合适的垃圾回收器有关。  
JIT 编译器堆性能也有很大影响，但是很少需要使用较新版本的 JVM 进行调优。

通常调优 Java 程序时，重点是以下两个目标：

-   **响应性：** 应用程序或系统对请求的数据进行响应的速度，对于专注响应性的应用程序，长的暂停时间是不可接受的，重点是在短时间内做出反应。
-   **吞吐量：** 侧重于在特定时间段内最大化应用程序的工作量，对于专注于吞吐量的应用程序，高暂停时间是可接受的。由于高吞吐量应用程序在较长时间内专注于基础测试，因此不需要考虑快速响应时间。

### 常用的 JVM 参数

<font color='red'>版本不断更新，JVM 参数和具体说明建议需要时具体参考 oracle 官方手册</font>

| 参数                            | 说明                               |
| ------------------------------- | ---------------------------------- |
| -XX:+AlwaysPreTouch             | JVM 启动时分配内存，非使用时再分配 |
| -XX:ErrorFile=filename          | 崩溃日志                           |
| -XX:+TraceClassLoading          | 跟踪类加载信息                     |
| -XX:PrintClassHistogram         | 按下 Ctrl+Break 后，打印类的信息   |
| -Xmx -Xms                       | 最大堆和最小堆                     |
| -XX:PermSize、-XX:MetaSpaceSize | 永久代、元数据空间                 |
| -XX:+HeapDumpOnOutOfMemoryError | OOM 时导出堆到文件                 |
| -XX:+HeapDumpPath               | OOM 时堆导出的路径                 |
| -XX:OnOutOfMemoryError          | 在 OOM 时执行一个脚本              |
| -XX:+PrintFlagsFinal -version   | 打印所有-XX 参数和默认值           |

### GC 调优思路

1. 场景分析
   例如：启动速度慢；偶尔出现反应慢于平均水平或者出现卡顿
2. 确定目标
   内存占用、低延迟、吞吐量
3. 收集日志
   通过参数配置收集 GC 日志；通过 JDK 查看工具查看 GC 状态
4. 分析日志
   使用工具辅助分析日志，查看 GC 次数、GC 时间
5. 调整参数
   切换垃圾回收器或者调整垃圾回收器参数

### 同用 GC 参数

JDK1.8 的通用参数

| 参数                   | 说明                                                                                                                    |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| -XX:ParallelGCThreads  | 设置 GC 并行线程数                                                                                                      |
| -XX:ConcGCThreads      | 设置并发 GC 线程数                                                                                                      |
| -XX:MaxGCPauseMillis   | 最大停顿时间，单位毫秒；<br>GC 尽力保证回收时间不超过设定值                                                             |
| -XX:GCTimeRatio        | 0-100 范围内取值；<br>垃圾收集时间占总时间的比；<br>默认 99，即最大 1%的 GC 时间                                        |
| -XX:SurvivorRatio      | 设置 Eden 区大小和 Survivor 区大小比例；<br>8 表示：两个 Survivor：一个 Eden = 2:8，即一个 Survivor 占整个年轻代的 1/10 |
| -XX:NewRatio           | 新生代和老年代的比<br>4 表示新生代：老年代 = 1:4，即年轻代占 1/5                                                        |
| -verbose、-XX:+printGC | 打印 GC 的简要信息                                                                                                      |
| -XX:PrintGCDetails     | 打印 GC 详细信息                                                                                                        |
| -XX:PrintGCTimeStamp   | 打印 GC 发生的时间戳                                                                                                    |
| -Xloggc:log/gc.log     | 指定 GC log 的位置，以文件输出                                                                                          |
| -XX:PrintHeapAtGC      | 每一次 GC 后都打印出堆信息                                                                                              |

### 调优示例

#### 示例一

目标代码：

```java
package com.example.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


// 启动程序，模拟用户请求
// 每100毫秒钟创建1000线程，每个线程创建一个512kb的对象，最多1秒内同时存在1500线程，并发量1000/s，占用内存750m（75%），查看GC的情况
@SpringBootApplication
public class PerformanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerformanceApplication.class, args);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            new Thread(() -> {
                for (int i = 0; i < 150; i++) {
                    try {
                        // 创建 512 kb 的对象
                        byte[] bytes = new byte[1024 * 512];
                        Thread.sleep(new Random().nextInt(1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

// 打包 mvn clean package
// 服务器上运行 performance-1.0.0.jar
// 对象存活1秒左右，远超了平时接口调用响应时间，该场景为吞吐量优先
}
```

调优过程：

```shell
# 查找到 performance-1.0.0.jar 的进程号，这里假设进程号为 5755
jcmd | grep "performance-1.0.0.jar" | awk '{print $1}'

# jmap 打印出 heap 概要信息， GC 使用的算法，heap 的配置及 wiseheap 的使用情况
jmap -heap 5755

# 收集 GC 日志（日志离线分析，主要用于检测故障看是不是由于 GC 导致的程序卡顿）
# 不建议直接输出 java -Xmx1024m -XX:+PrintGCDetails -XX:+PrintGCTimeStamp -jar performance-1.0.0.jar
java -Xmx1024m -Xloggc:gc/gc.log -jar performance-1.0.0.jar

# 分析 GC 日志，
GCViewer 工具，分析 GC 日志文件 https://github.com/chewiebug/GCViewer

# 实时分析
# jstat 动态监听 GC 统计信息，间隔1000ms统计一次，每十行数据后输出列标题
jstat -gc -h10 5755 1000

```

---

[内存模型](./README.md)  
[Java](../README.md)  
[主页](../../../../../)
