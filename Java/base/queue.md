## 队列解析

### Queue - API

方法|左右|描述|特殊说明
-|-|-|-
add|增加一个元素|如果队列已满，抛出IllegalStateException
remove|移除并返回队列头部元素|如果队列为空，抛出NoSuchElementException
element|返回队列头部元素|如果队列为空，抛出NoSuchElementException
offer|添加一个元素并返回true|如果队列已满，就返回false
poll|移除并返回队列头部元素|如果队列为空则返回null
peek|返回队列头部元素|如果队列为空则返回null
put|添加一个元素|如果队列已满，则阻塞|阻塞队列特有
take|移除并返回队列头部元素|如果队列为空，则阻塞|阻塞队列特有

### 常用的 Queue

基本用法（ArrayBlockingQueue）：

```java
package juc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ArrayBlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        // 构造时需要指定容量,可以选择是否需要公平（最先进入阻塞的，先操作）,默认
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(3, false);
        // 1秒消费数据一个
        new Thread(() -> {
            while (true) {
                try {
                    System.out.println("取到数据：" + queue.poll()); // poll非阻塞
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
            }
        }).start();

        TimeUnit.SECONDS.sleep(3);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    // put阻塞(如果当前的队列已经塞满了数据，线程不会继续往下执行，等待其他线程把 队列的数据拿出去 )
                    queue.put(Thread.currentThread().getName());
                    // offer非阻塞，满了返回false
                    // queue.offer(Thread.currentThread().getName()); 
                    System.out.println(Thread.currentThread() + "塞入完成");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}

```

$注意：$<font color='red'>阻塞队列也不是所有方法都是阻塞的，只是提供了阻塞的方法。非阻塞队列没有提供阻塞方法</font>

常用的**阻塞队列：**
- ArrayBlockingQueue：基于数组实现
- LinkedBlockingQueue：基于链表实现
- SynchronousQueue：用来传递数据的队列，内部最多只有一条数据，直到这个队列中的这条数据被消费

常用的**非阻塞队列：**
- ConcurrentLinkedQueue：
    1. 无锁，使用CAS操作队列
    2. 批量操作不提供原子保证  addAll, removeAll, retainAll, containsAll, equals, toArray
    3. 坑： size()方法每次都是遍历整个链表，最好不要频繁调用
    4. 如果没有阻塞要求，用这个挺好的（堆积数据）

### PriorityQueue 和 PriorityBlockingQueue

**重点：**

1. 这是一组带优先级的队列，而不是FIFO队列
2. 元素按优先级顺序被移除，该队列也没有上限
3. 没有容量限制的，自动扩容。虽然此队列逻辑上是无界的，但是由于资源被耗尽，所以试图执行添加操作可能会导致 OutOfMemoryError）
4. PriorityBlockingQueue 如果队列为空，那么取元素的操作take就会阻塞，所以它的检索操作take是受阻的。因为没有上限，所以put方法不会阻塞。
5. 这组队列中的元素要具有比较能力，在put或者offer时，调用compare比较。

代码示例：
```java
public class PriorityQueueDemo {
    public static void main(String[] args) {
        // 可以设置比对方式
        PriorityQueue<String> priorityQueue = new PriorityQueue<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                // 实际就是 元素之间的 比对。
                return 0;
            }
        });
        priorityQueue.add("c");
        priorityQueue.add("a");
        priorityQueue.add("b");

        System.out.println(priorityQueue.poll());
        System.out.println(priorityQueue.poll());
        System.out.println(priorityQueue.poll());

        PriorityQueue<MessageObject> MessageObjectQueue = new PriorityQueue<>(new Comparator<MessageObject>() {
            @Override
            public int compare(MessageObject o1, MessageObject o2) {
                return o1.order > o2.order ? -1 : 1;
            }
        });
    }
}

class MessageObject {
    String content;
    int order;
}
```

### DelayQueue

1. 一个延迟队列，基于PriorityQueue来实现的
2. 只有在延迟期满时才能从中提取元素。该队列的头部是延迟期满后保存时间最长的 Delayed 元素。
3. 如果延迟都还没有期满，则队列没有头部，并且poll将返回null。
4. 当一个元素的 getDelay(TimeUnit.NANOSECONDS) 方法返回一个小于或等于零的值时，则出现期满，poll就以移除这个元素了。此队列不允许使用 null 元素。

代码示例：

```java
package juc;

import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        DelayQueue<Message> queue = new DelayQueue<>();
        // 3秒后执行
        Message message = new Message("message - 001", new Date(System.currentTimeMillis() + 3000L));
        queue.offer(message);

        while (true) {
            System.out.println(queue.poll());
            TimeUnit.SECONDS.sleep(1);
        }
    }

}

class Message implements Delayed {

    String content;
    Date sendTime;

    @Override
    public long getDelay(TimeUnit unit) {
        // 默认纳秒
        long duration = this.sendTime.getTime() - System.currentTimeMillis();
        return TimeUnit.NANOSECONDS.convert(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return o.getDelay(TimeUnit.NANOSECONDS) > this.getDelay(TimeUnit.NANOSECONDS) ? 1 : -1;
    }

    public Message(String content, Date sendTime) {
        this.content = content;
        this.sendTime = sendTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", sendTime=" + sendTime +
                '}';
    }

}
```

应用场景：ScheduledThreadPoolExecutor - 定时任务



---

[基础解析](./README.md)  
[Java](../README.md)  
[主页](/)
