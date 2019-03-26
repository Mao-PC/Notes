## NIO 网络编程

JDK1.4 开始提供 IO 操作的非阻塞 API。用来取代 Java IO 和 Java Networking 相关的 API。

### 核三个心组件：

-   Buffer 缓存区
-   Channel 通道
-   Selector 选择器

#### Buffer 缓存区

是一个可写入数据的内存块（类似数组）。使用 Buffer 进行数据的读取和写入一般一下四个步骤：

-   将数据写入缓冲区
-   使用 buffer.filp()，转成读取模式
-   读取数据
-   调用 buffer.clear()或 buffer.compact()清除缓冲区

示例代码：

```java
package nio;

import java.nio.ByteBuffer;

/**
 * 功能描述:
 * Buffer示例
 *
 * @auther: pikaqiu
 * @date: 2019/3/26 7:11 AM
 */
public class BufferDemo {
    public static void main(String[] args) {
        // 构建缓冲区容量为4
        ByteBuffer buffer = ByteBuffer.allocate(4);

        System.out.println("初始化capacity：" + buffer.capacity() + " position：" + buffer.position() + " limit：" + buffer.limit());

        // 写入数据
        buffer.put((byte) 1);
        buffer.put((byte) 2);
        buffer.put((byte) 3);

        System.out.println("写入3字节数据capacity：" + buffer.capacity() + " position：" + buffer.position() + " limit：" + buffer.limit());

        // 读取数据
        buffer.flip();
        System.out.println(buffer.get());
        System.out.println(buffer.get());

        System.out.println("读取2字节数据capacity：" + buffer.capacity() + " position：" + buffer.position() + " limit：" + buffer.limit());

        // clear是清除整个缓存区，compact是清除已读数据并转成写模式
        buffer.compact();
        buffer.put((byte) 4);
        buffer.put((byte) 5);
        buffer.put((byte) 6);
        System.out.println("写入3字节数据capacity：" + buffer.capacity() + " position：" + buffer.position() + " limit：" + buffer.limit());

        // rewind() 重置position为0
        // mark() 标记position位置
        // reset() 重置position到上次mark的位置

    }
}

```

Buffer 提供了直接内存（direct 堆外）和非直接内存（heap 堆）两种实现，堆外内存获取方式:ByteBuffer buffer = ByteBuffer.allocateDirect(4)。

好处：

1. 进行网络 IO 或者问 IO 时，会比 heapBuffer 少一次拷贝（file/socket--- OS memory ---- JVM heap）。GC 会移动对象内存，写 file 或 Socket 过程中，JVM 会把数据复制到堆外然后写入。
2. 在 GC 之外，降低了 GC 的压力，实现了自动管理。DirectBuffer 有个 Clearner 对象，被 GC 前会执行 clean 方法，触发 DirectBuffer 中的 Deallocater。

建议：

1. 性能确实可观时才去使用；分配给大型、长寿命（网络传输、文件读写）
2. 通过虚拟机参数 MaxDirectMemorySize 限制，防止耗尽内存。

#### Channel 通道

![NIO-Channel](res/NIO-Channel.png)

---

[并发](./README.md)  
[Java](../README.md)  
[主页](../../../../../)
