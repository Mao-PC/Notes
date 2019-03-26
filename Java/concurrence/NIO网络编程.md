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

运行结果：  
![BIO_Buffer](res/BIO_Buffer.png)  

Buffer 提供了直接内存（direct 堆外）和非直接内存（heap 堆）两种实现，堆外内存获取方式:ByteBuffer buffer = ByteBuffer.allocateDirect(4)。

好处：

1. 进行网络 IO 或者问 IO 时，会比 heapBuffer 少一次拷贝（file/socket--- OS memory ---- JVM heap）。GC 会移动对象内存，写 file 或 Socket 过程中，JVM 会把数据复制到堆外然后写入。
2. 在 GC 之外，降低了 GC 的压力，实现了自动管理。DirectBuffer 有个 Clearner 对象，被 GC 前会执行 clean 方法，触发 DirectBuffer 中的 Deallocater。

建议：

1. 性能确实可观时才去使用；分配给大型、长寿命（网络传输、文件读写）
2. 通过虚拟机参数 MaxDirectMemorySize 限制，防止耗尽内存。

#### Channel 通道

![NIO-Channel](res/NIO-Channel.png)  

##### SocketChannel

用于建立TCP网络连接，类似 java.net.Socket。有两种创建SocketChannel形式：  
1. 客户端主动发起和服务端的连接
2. 服务端获取新的连接  

示例：
```java
// 客户端主动发起连接
SocketChannel socketChannel = SocketChannel.open();
// 设置为非阻塞模式
socketChannel.configureBlocking(false);
socketChannel.connect(new InetSocketAddress("httt://localhost", 80));

socketChannel.write(new ByteBuffer[0]); // 发送请求数据 - 向通道写入数据

socketChannel.read(new ByteBuffer[0]); // 读取服务端返回 - 读取缓存数据

socketChannel.close(); // 关闭连接
```

**write**：write()在尚未写入任何内容时就可能返回了，需要在循环中调用write()。  
**read**：read()方法可能直接返回而根本不读取任何数据，根据返回的int值判断读取了多少字节。

##### ServerSocketChannel

ServerSocketChannel可以监听新建的TCP连接通道，类似ServerSocket。  

示例：
```java
// 创建服务端
ServerSocketChannel server = ServerSocketChannel.open();
server.configureBlocking(false); // 设置非阻塞模式
server.socket().bind(new InetSocketAddress(8080)); // 绑定8080端口

while (true) {
    SocketChannel socketChannel = server.accept(); // 获取新的TCP连接通道

    if (socketChannel!=null) {
        // TCP请求 响应/读取
    }
}
```

**accept:** ServerSocketChannel.accept() 如果改通道处于非阻塞模式，那么如果没有挂起的连接，该方法立即返回null。必须检查返回的SocketChannel是否为null。

使用Channel修改 [TCP/UDP 和 BIO](./TCPUDP和BIO.md) 中的程序，代码如下：
```java
package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * server端
 */
public class NIOServer {
    public static void main(String[] args) {
        // 创建服务端
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            // 设置非阻塞
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(8080)); // 绑定端口
            System.out.println("启动成功！");
            while (true) {
                // 获取连接通道
                SocketChannel socketChannel = server.accept();
                // TCP请求、响应
                if (socketChannel != null) {
                    System.out.println("收到连接：" + socketChannel.getRemoteAddress());
                    socketChannel.configureBlocking(false); // 设置非阻塞
                    ByteBuffer buffer = ByteBuffer.allocate(1024); // 构建缓存区
                    while (socketChannel.isOpen() && socketChannel.read(buffer) != -1) {
                        // 长连接情况下，需要手动判断数据有没有结束（此处做了一个简单的判断：超过0字节就认为请求结束了）
                        if (buffer.position() != 0) break;
                    }
                    if (buffer.position() == 0) continue; // 如果没有数据了就不进行下一步操作
                    buffer.flip(); // 转为读取模式
                    byte[] content = new byte[buffer.limit()];
                    buffer.get(content);
                    System.out.println(new String(content));
                    System.out.println("收到连接，来自：" + socketChannel.getRemoteAddress());

                    // 返回响应结果
                    String response = "HTTP/1.1 200 OK\r\n" + "Content-Length: 11\r\n\r\n" + "Hello World";
                    ByteBuffer byteBuffer = ByteBuffer.wrap(response.getBytes());
                    while (byteBuffer.hasRemaining()) {
                        socketChannel.write(byteBuffer);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

```java
package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * 客户端
 */
public class NIOClient {
    public static void main(String[] args) {
        try (SocketChannel client = SocketChannel.open()) {
            client.configureBlocking(false);
            client.connect(new InetSocketAddress(8080));
            while (!client.finishConnect()) {
                // 没有连上就一直等待
                Thread.yield();
            }
            Scanner scanner = new Scanner(System.in);
            System.out.println("请输入：");
            String msg = scanner.nextLine();
            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
            while (buffer.hasRemaining()) {
                client.write(buffer);
            }

            // 读取响应
            System.out.println("收到服务端响应：");
            ByteBuffer response = ByteBuffer.allocate(1024);

            while (client.isOpen() && client.read(response) != -1) {
                if (response.position() > 0) break;
            }

            response.flip();
            byte[] content = new byte[response.limit()];

            response.get(content);
            System.out.println(new String(content));
            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

```
启动一个服务端和两个客户端，运行结果：  
服务端：  
![NIO_Channel_server](res/NIO_Channel_server.png)  
在一个客户端中输入，运行结果：  
客户端：  
![NIO_Channel_client](res/NIO_Channel_client.png)  
服务端：
![NIO_Channel_server1](res/NIO_Channel_server1.png)  

发现坑爹了，经过修改使用Channel后服务端不能接收多个连接了。。。  
原因是因为在服务端的代码中有一下这段代码：
``` java
while (socketChannel.isOpen() && socketChannel.read(buffer) != -1) {
    // 长连接情况下，需要手动判断数据有没有结束（此处做了一个简单的判断：超过0字节就认为请求结束了）
    if (buffer.position() != 0) break;
}
```
这段代码在第一个连接就会一直循环判断直到请求结束才会进行下一步， 相当于人工阻塞了代码。  

优化代码：  
```java

```

---

[并发](./README.md)  
[Java](../README.md)  
[主页](../../../../../)
