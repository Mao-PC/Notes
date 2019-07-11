[TOC]

# RPC

## RPC 是什么

RPC (remote producer call) : 远程**过程**调用

> 这里的过程是: 业务处理, 计算任务, 更直白的讲就是程序. (就行调用本地方法一样调用远程的过程)

RPC 采用 C/S (Client-Server) 结构, 通过 request-response 消息模式实现

**RPC 和 RMI**

RMI (remote method invocation) 远程方法调用, 是在 OOP 领域中 RPC 的一种实现

**webservice, restfull 接口调用时 RPC 吗?**

都是 RPC, 仅仅是消息的组织方式和消息协议不同

**RPC 和本地调用有什么不同?**

- 速度相对较慢
- 可靠性弱

## PRC 的流程

![RPC流程](res/流程.png)

1. 客户端处理过程中**调用** Client stub (就行调用本地方法一样), 传达参数
2. Client stub 将参数**编组**为消息, 然后通过系统调用向服务端发送消息
3. 客户端本地操作系统将消息从客户端**发送**到服务端机器
4. 服务端操作系统将接受到的数据包**传递**给 Server stub
5. Server stub**解组**消息为参数
6. Server stub **再调用**服务端的过程, 过程执行结果以反方向的相同步骤相应给客户端

**在这整个流程中, 需要处理哪些问题?**

1. Client stub 和 Server stub 的开发
2. 参数如何编组为消息
3. 消息如何发送
4. 过程结果如何表示, 异常情况如何处理
5. 如何实现安全的访问控制

## RPC 协议

RPC 调用过程中需要将参数编组为消息进行发送, 接受方需要解组消息为参数, 处理过程结果同样需要编组/解组. 消息由哪些部分构成及消息的表示形式就构成了消息协议. **RPC 调用过程就采用的消息协议就是 RPC 协议**

> RPC 协议规定请求/响应消息的格式
> 在 TCP (网络传输控制协议) 上可选用或自定义消息协议来完成 RPC 消息交互
> 我们可以选用通用的标准协议 (如: http, https), 也可以根据自身的需要来自定义消息协议

---

[分布式](../README.md)

[主页](https://github.com/Mao-PC/Notes/blob/master/README.md)
