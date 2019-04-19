演示环境： Centos7、jdk8、activemq5.15.8  
下载地址： http://activemq.apache.org/activemq-5158-release.html  
解压： `tar -zxvf apache-activemq-5.15.8-bin.tar.gz -C /var`  
修改目录名称 `mv /var/apache-activemq-5.15.8/ /var/activemq/`  
启动： `./bin/activemq start`  
停止：`./bin/activemq stop`

# 操作练习

1、创建一个 systemd 服务文件：`vi /usr/lib/systemd/system/activemq.service`

2、 放入内容

```xml
[Unit]
Description=ActiveMQ service
After=network.target

[Service]
Type=forking
ExecStart=/var/activemq/bin/activemq start
ExecStop=/var/activemq/bin/activemq stop
User=root
Group=root
Restart=always
RestartSec=9
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=activemq

[Install]
WantedBy=multi-user.target
```

3、 找到 java 命令所在的目录 `whereis java`

4、设置 activemq 配置文件/var/activemq/bin/env 中的 JAVA_HOME

```shell
# Location of the java installation
# Specify the location of your java installation using JAVA_HOME, or specify the
# path to the "java" binary using JAVACMD
# (set JAVACMD to "auto" for automatic detection)
JAVA_HOME="/usr/local/java/jdk1.8.0_181"
JAVACMD="auto"
```

5、 通过 systemctl 管理 activemq 启停

-   启动 activemq 服务: `systemctl start activemq`
-   查看服务状态: `systemctl status activemq`
-   创建软件链接：`ln -s /usr/lib/systemd/system/activemq.service /etc/systemd/system/multi-user.target.wants/activemq.service`
-   开机自启: `systemctl enable activemq`
-   检测是否开启成功(enable)： `systemctl list-unit-files |grep activemq`

6、 防火墙配置，Web 管理端口默认为 8161（admin/admin），通讯端口默认为 61616

-   添加并重启防火墙

```
firewall-cmd --zone=public --add-port=8161/tcp --permanent
firewall-cmd --zone=public --add-port=61616/tcp --permanent
systemctl restart firewalld.service
```

-   或者直接关闭防火墙: `systemctl stop firewalld.service`

7、 修改 web 管理系统的部分配置,配置文件在`/var/activemq/conf`

-   端口修改

```xml
<bean id="jettyPort" class="org.apache.activemq.web.WebConsolePort" init-method="start">
  <!-- the default port number for the web console -->
  <property name="host" value="0.0.0.0"/>
  <!--此处即为管理平台的端口-->
  <property name="port" value="8161"/>
</bean>
```

-   关闭登录

```xml
<bean id="securityConstraint" class="org.eclipse.jetty.util.security.Constraint">
  <property name="name" value="BASIC" />
  <property name="roles" value="user,admin" />
  <!-- 改为false即可关闭登陆 -->
  <property name="authenticate" value="true" />
</bean>
```

-   其他配置: `/var/activemq/conf/jetty-realm.properties`

```
## ---------------------------------------------------------------------------
# 在此即可维护账号密码，格式：
# 用户名:密码,角色
# Defines users that can access the web (console, demo, etc.)
# username: password [,rolename ...]
admin: admin, admin
user: 123, user
```

8、 JAVA 客户端的使用

-   标准客户端使用

```xml
<dependency>
  <groupId>org.apache.activemq</groupId>
  <artifactId>activemq-all</artifactId>
  <version>5.15.8</version>
</dependency>
```

-   Spring 中使用: `http://spring.io/guides/gs/messaging-jms/`

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jms</artifactId>
    <version>5.1.3.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.apache.activemq</groupId>
    <artifactId>activemq-broker</artifactId>
    <version>5.15.8</version>
    <exclusions>
    <exclusion>
        <artifactId>geronimo-jms_1.1_spec</artifactId>
        <groupId>org.apache.geronimo.specs</groupId>
    </exclusion>
    </exclusions>
</dependency>
```

在 Spring 中使用示例代码:

```java
package com.acive.hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.core.JmsTemplate;

import javax.annotation.PostConstruct;

/**
 * 功能描述: 生产者
 *
 * @auther: pikaqiu
 * @date: 2019/4/19 8:06 AM
 */
@SpringBootApplication
public class Producer {

    @Autowired
    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void init() {
        jmsTemplate.convertAndSend("queue1", "hello Active");
    }

    public static void main(String[] args) {
        SpringApplication.run(Producer.class, args);
    }

}

```

```java
package com.active.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;

/**
 * 功能描述: 消费者
 *
 * @auther: pikaqiu
 * @date: 2019/4/19 8:28 AM
 */
@SpringBootApplication
@EnableJms
public class Comsumer {

    @JmsListener(destination = "queue1")
    public void receive(String msg) {
        System.out.println("收到消息: " + msg);
    }

    public static void main(String[] args) {
        SpringApplication.run(Comsumer.class, args);
    }
}

```

```yml
# activemq的yml配置
spring:
    activemq:
        broker-url: 'tcp://192.168.1.14:61616'
        user: 'admin'
        password: 'admin'
```
