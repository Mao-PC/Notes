[TOC]
# SpringBoot

文档地址: https://spring.io/projects/spring-boot

-   简化 J2EE 开发
-   整个 Spring 技术栈的整合 (SpringMVC Spring)
-   各种 J2EE 技术的大整合 (Mybatis Redis)

SpringBoot 将各个应用/第三方框架 设置成了一个个 "场景" starter, 选完之后, SpringBoot 就会将该场景所需要的所有依赖自动注入. 如: 选择了 "web", SpringBoot 就会将 web 相关的依赖 (tomcat, json) 注入

## 主配置类 @SpringBootApplication 解读

**@SpringBootApplication** 是 SpringBoot 的主配置类, 该注解包含 **@SpringBootConfiguration** 和 **@EnableAutoConfiguration** 注解

-   **@SpringBootConfiguration** : 包含 **@Configuration**, 表示 "配置类"
    -   表示该类是一个**配置类**
    -   该类会自动纳入 Spring 容器里(因为 **@Configuration** 中包含 **@Component**)
-   **@EnableAutoConfiguration** : 使 SpringBoot 可以自动配置, 包含有以下注解

    -   **@AutoConfigurationPackage** : 可以找到主配置类所在类的包, 就会将该包及所有的子包, 全部纳入 Spring 容器 (传统写法: 将 "com.example.demo" 手工写到 scan 扫描器中)
    -   **@Import({AutoConfigurationImportSelector.class})** : 引入三方依赖, **AutoConfigurationImportSelector** 类的 **selectImports** 方法可以将这些依赖注入

        ```java
        public String[] selectImports(AnnotationMetadata annotationMetadata) {
            if (!this.isEnabled(annotationMetadata)) {
                return NO_IMPORTS;
            } else {
                AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
                // 这行代码
                AutoConfigurationImportSelector.AutoConfigurationEntry autoConfigurationEntry = this.getAutoConfigurationEntry(autoConfigurationMetadata, annotationMetadata);
                return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
            }
        }

        protected AutoConfigurationImportSelector.AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata, AnnotationMetadata annotationMetadata) {
            if (!this.isEnabled(annotationMetadata)) {
                return EMPTY_ENTRY;
            } else {
                AnnotationAttributes attributes = this.getAttributes(annotationMetadata);
                // 获取候选配置
                List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
                configurations = this.removeDuplicates(configurations);
                Set<String> exclusions = this.getExclusions(annotationMetadata, attributes);
                this.checkExcludedClasses(configurations, exclusions);
                configurations.removeAll(exclusions);
                configurations = this.filter(configurations, autoConfigurationMetadata);
                this.fireAutoConfigurationImportEvents(configurations, exclusions);
                return new AutoConfigurationImportSelector.AutoConfigurationEntry(configurations, exclusions);
            }
        }
        ```

        查看其中 `getCandidateConfigurations` 方法

        ```java
        protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
            List<String> configurations = SpringFactoriesLoader.loadFactoryNames(this.getSpringFactoriesLoaderFactoryClass(), this.getBeanClassLoader());
            Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you are using a custom packaging, make sure that file is correct.");
            return configurations;
        }
        ```

        查看 `SpringFactoriesLoader.loadFactoryNames`方法

        ```java
        public static List<String> loadFactoryNames(Class<?> factoryClass, @Nullable ClassLoader classLoader) {
            String factoryClassName = factoryClass.getName();
            return (List)loadSpringFactories(classLoader).getOrDefault(factoryClassName, Collections.emptyList());
        }
        ```

        查看 `loadSpringFactories` 方法

        ```java
        private static Map<String, List<String>> loadSpringFactories(@Nullable ClassLoader classLoader) {
            MultiValueMap<String, String> result = (MultiValueMap)cache.get(classLoader);
            if (result != null) {
                return result;
            } else {
                try {
                    Enumeration<URL> urls = classLoader != null ? classLoader.getResources("META-INF/spring.factories") : ClassLoader.getSystemResources("META-INF/spring.factories");
                    LinkedMultiValueMap result = new LinkedMultiValueMap();

                    while(urls.hasMoreElements()) {
                        // TODO 遍历urls 此处省略
                    }

                    cache.put(classLoader, result);
                    return result;
                } catch (IOException var13) {
                    throw new IllegalArgumentException("Unable to load factories from location [META-INF/spring.factories]", var13);
                }
            }
        }
        ```

        综上, 可以看出 SpringBoot 在启动时, 会根据 `META-INF/spring.factories` 找到相应的三方依赖, 并引入本项目

总结, 编写项目时一般会对自己写的代码和三方依赖进行配置, 但是 Spring 可以自动进行配置

-   自己的代码 SpringBoot 通过 **@SpringBootConfiguration** 配置
-   三方依赖通过 **spring-boot-autoconfigure:2.1.6.RELEASE** jar 包中的 `META-INF/spring.factories` 进行声明开启使用

## 自动装配原理

在 `META-INF/spring.factories` 中随便找一个类名, 这里使用 `org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration` 为例

代码如下:

```java
package org.springframework.boot.autoconfigure.thymeleaf;

import ...

@Configuration
@EnableConfigurationProperties({ThymeleafProperties.class})
@ConditionalOnClass({TemplateMode.class, SpringTemplateEngine.class})
@AutoConfigureAfter({WebMvcAutoConfiguration.class, WebFluxAutoConfiguration.class})
public class ThymeleafAutoConfiguration {
    // 内容省略
}
```

-   **@Configuration** : 配置类, 纳入 Spring 容器中

-   **@EnableConfigurationProperties({ThymeleafProperties.class})** : 默认属性设置, `ThymeleafProperties` 类如下

    ```java
    @ConfigurationProperties(prefix = "spring.thymeleaf")
    public class ThymeleafProperties {
        private static final Charset DEFAULT_ENCODING;
        public static final String DEFAULT_PREFIX = "classpath:/templates/";
        public static final String DEFAULT_SUFFIX = ".html";
        private boolean checkTemplate = true;
        private boolean checkTemplateLocation = true;
        private String prefix = "classpath:/templates/";
        private String suffix = ".html";
        private String mode = "HTML";
        // ... 省略
    }
    ```

    如 `private String suffix = ".html";` 默认后缀为 `html`, 如果想修改默认属性可以在 `application.properties` 或者 `application.yml`中配置, 只要将 `@ConfigurationProperties(prefix = "spring.thymeleaf")` 中 `prefix` 的值加上想要修改的值 (suffix)即可, 这里以 `application.properties` 为例

    ```properties
    // 这里同理修改端口为80
    server.port=80
    spring.thymeleaf.suffix=.jsp
    ```

-   **@ConditionalOnXX** : 条件注解, 当满足 XX 条件时配置生效, `@ConditionalOnClass({TemplateMode.class, SpringTemplateEngine.class})` 就是当有 `TemplateMode` 和 `SpringTemplateEngine`存在配置生效, 有多个 `@ConditionalOnXX`注解时需要同时满足条件才生效

-   **@AutoConfigureAfter** : 在加载配置的类之后再加载当前类

想要看 SpringBoot 开启了哪些自动装配, 禁止了哪些自动装配: `application.properties` 中 `debug=true`

## 配置文件

### 属性注入

默认的全局配置文件 `application.properties` 和 `application.yml`

这里简单说明一下 `application.yml` 的语法:

-   k:空格 v
    -   [中括号]: 存放 list,set,数组, []可以省略
    -   {大括号}: 存放 map 或者对象, {}不能省略
-   通过 `tab` 垂直对齐来指定层次关系
-   默认可以不写引号, 但是如果需要转译符就必须使用双引号
-   支持松散语法, 如: 类中有 userName, 可以在 yaml 中写 user-name

通过 yaml 或者 properties 文件给属性注入值, 要在该类上使用 **@Component** 和 **@ConfigurationProperties(prefix = "xx")** 注解, 具体原因前面已经说明了

yaml 或者 properties 文件支持 JSR303 校验, 只需要在类上加 **@Validated** 注解开启校验, 然后在需要校验的字段上加上校验的注解, 如:

```java
@Component
@ConfigurationProperties(prefix = "student")
@Validated
public class Student {
    @Email
    private String email;
}
```

注入属性值也可以在属性上直接通过 **@Value("xx")** 来实现, 如:

```java
public class Student {
    @Value("ls")
    private String name;
    @Value("24")
    private int age;
}
```

**@Value** 不支持 JSR303 校验, 也不支持复杂类型的注入 (map, list 等等)

不同方式注入可以互补, 但是需要注意:

-   yaml 和 properties 注入属性值时, **不能**有重复注入
-   为同一个属性赋值时, 优先级为 **properties > yaml > @Value()**
-   3 种方式都可以使用占位符表达式

    ```properties
    ${random.uuid}: UUID
    ${random.value}: 随机字符串
    ${random.int}: 随机整数
    ${random.long}: 随机长整数
    ${random.int(10)}: 10以内随机整数
    ${random.int[1024, 65535]}: 指定范围内随机整数
    ```

-   **@Value("student.age")** 还可以指定 yaml 或者 properties 文件中的属性来注入, yaml 和 properties 也可以相互引用, 如: `student.name=${student.user.name}`

SpringBoot 会默认加载 `application.properties` 和 `application.yml` , 但如果需要注入其他文件的值 (如: conf.properties), 可以使用 **@PropertySource("classpath:conf.properties")** 来指定, 但是经过测试, 该注解只能加载 properties 文件, 不能加载 yaml 文件

### bean 配置

Spring 的配置文件默认会被 SpringBoot 自动装配好, 如果要自己编写配置文件, 就需要在主配置类上加上注解 **@ImportResource(locations = "配置文件路径")**, 如: 加载 classpath 下的 spring.xml 文件

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource(locations = "classpath:spring.xml")
@SpringBootApplication
public class DemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
```

但是, **不推荐**这样的配置, 最好使用注解 **@Configuration** 配置, 如下

```java
package com.example.demo.config;

import com.example.demo.service.StudentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConfig {
    @Bean
    @Scope("prototype")
    public StudentService sutServie() {
        return new StudentService();
    }
}
```

### 环境切换

**properties 文件切换 :**

SpringBoot 默认读取 application.properties 环境, 如果有多个环境, 一般命名为 application-环境名.properties, 如

-   主环境: application.properties
-   开发环境: application-dev.properties
-   测试环境: application-test.properties

如果需要指定环境就在主环境配置文件 (application.properties)中增加

```properties
# 切换为开发环境
spring.profiles.active=dev
```

**yaml 文件切换 :**

```yml
# 环境信息
server:
    port: 8881
# 指定读取配置
spring:
    profiles:
        active: test1

---
server:
    port: 8882
spring:
    profiles: dev1

---
server:
    port: 8883
spring:
    profiles: test1
```

这里需要注意: 如果配置 `spring.profiles.active=test` 的话默认会加载 `application-test.properties`文件, 这里依旧是按照 **properties > yaml** 的. 所以最好不要混用两种配置文件

**动态切换环境**

-   运行参数方式: `--spring.profiles.active=dev`
-   jvm 参数 : `java -jar xxx.jar -Dspring.profiles.active=dev`

### 配置文件位置

**项目内部配置文件 :**

SpringBoot 会默认加载 `application.properties` 和 `application.yml`, 这两个文件可以存在于 4 个位置, 配置不冲突就互补, 配置冲突 **优先级从大到小**为:

-   file:根目录/config
-   file:根目录
-   classpath:根目录/config
-   classpath:根目录 (默认)

配置项目名:

properties 文件中:

```properties
server.servlet.context-path="xxx"
```

**项目外部配置文件 :**

-   运行参数: `--spring.config.location=D:/application.properties`
-   jvm 参数: `java -jar xxx.jar --spring.config.location=D:/application.properties`

如果内部和外部配置文件同时存在, **外部>内部**

外部配置文件好处: 可以更加灵活的配置属性, 有**大量属性**需要修改, 如: 项目部署时端口冲突, 此时不用再次打包, 只需要修改外部配置文件即可

**运行参数配置 :**

如果只有**个别参数**需要改动, 可以使用这种方法来临时补救, 如修改端口号:

-   运行参数: `--seiver.port=8888`, 如果有多个可以用空格分割追加
-   jvm 参数: `java -jar xxx.jar --seiver.port=8888`

优先级: **命令参数( 运行参数 > 外部 ) > 内部 ( properties > yaml )**

详细的优先级可以参考: https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/boot-features-external-config.html

## 日志处理

官网文档参考: https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/boot-features-logging.html

SpringBoot 默认使用 slf4j, logback

级别: TRACE < DEBUG < INFO (默认) < WARN < ERROR < FATAL < OFF

自定义日志级别, 在配置文件中修改:

```properties
logging.level.主配置类所在包名=warn
```

输出到日志文件

```properties
# springboot.log 在根目录, 也可以输出到其他路径如 D:/spring.log
logging.file=springboot.log
```

也可以存放到文件夹中

```properties
# 日志输出到文件夹中, 默认文件名为 spring.log
logging.file=log/
```

指定日志格式:

-   控制台 : `logging.pattern.console=%boldMagenta(%d{yyyy-MM-dd HH:mm:ss}) [%p] %highlight(%C:%L) : %m %n`

-   log 文件 : `logging.pattern.file=%boldMagenta(%d{yyyy-MM-dd HH:mm:ss}) [%p] %highlight(%C:%L) : %m %n`

## 开发 Web 项目

SpringBoot 是一个 jar, 因此静态资源不会存放到 webapps 中, SpringBoot 使用 WebMvcAutoConfiguration 类的 addResourceHandlers() 方法指定, 代码如下:

```java
public class WebMvcAutoConfiguration {
    // 其他方法省略...
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!this.resourceProperties.isAddMappings()) {
            logger.debug("Default resource handling disabled");
        } else {
            Duration cachePeriod = this.resourceProperties.getCache().getPeriod();
            CacheControl cacheControl = this.resourceProperties.getCache().getCachecontrol().toHttpCacheControl();
            if (!registry.hasMappingForPattern("/webjars/**")) {
                this.customizeResourceHandlerRegistration(registry.addResourceHandler(new String[]{"/webjars/**"}).addResourceLocations(new String[]{"classpath:/META-INF/resources/webjars/"}).setCachePeriod(this.getSeconds(cachePeriod)).setCacheControl(cacheControl));
            }

            String staticPathPattern = this.mvcProperties.getStaticPathPattern();
            if (!registry.hasMappingForPattern(staticPathPattern)) {
                this.customizeResourceHandlerRegistration(registry.addResourceHandler(new String[]{staticPathPattern}).addResourceLocations(getResourceLocations(this.resourceProperties.getStaticLocations())).setCachePeriod(this.getSeconds(cachePeriod)).setCacheControl(cacheControl));
            }

        }
    }
}
```

可以很明显的看到 webjars, 官网 https://www.webjars.org/, webjars 是一个各种 js 打成 jar 包的框架, 以前引入 js 等静态资源是将这些资源下载并放到 webapps 目录中, 而 SpringBoot 以 webjars 引入, 如引入 jQuery:

```xml
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>jquery</artifactId>
    <version>3.4.1</version>
</dependency>
```

具体到引入时, 需要从 jar 目录的 resource/webjars 开始写 `localhost:端口/[项目名]/webjars/jquery/3.4.1/jquery.js`

自己写的静态资源如何引入 ?

-   把自己写的静态资源打成 jar, 引入方式同上 (不推荐)
-   自动扫描, SpringBoot 会自动扫描一些目录内的静态资源:
    请注意上面 WebMvcAutoConfiguration 类的 addResourceHandlers() 方法中
    `this.resourceProperties.getStaticLocations()`, 如下

    ```java
    @ConfigurationProperties(
        prefix = "spring.resources",
        ignoreUnknownFields = false
    )
    public class ResourceProperties {
        // 其他属性和方法省略
        private static final String[] CLASSPATH_RESOURCE_LOCATIONS = new String[]{"classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/"};
        private String[] staticLocations;

        public ResourceProperties() {
            this.staticLocations = CLASSPATH_RESOURCE_LOCATIONS;
            this.addMappings = true;
            this.chain = new ResourceProperties.Chain();
            this.cache = new ResourceProperties.Cache();
        }

        public String[] getStaticLocations() {
            return this.staticLocations;
        }
    }
    ```

    可以看出 SpringBoot 摸的静态资源存放路径就是

    -   classpath:/META-INF/resources/
    -   classpath:/resources/
    -   classpath:/static/
    -   classpath:/public/

    可以输入地址访问 `http://localhost:端口/[项目名/welcome.html`

    欢迎页默认为 index.html

    **设置 logo**

    一般网站中网页标签的 logo 是固定名字: favicon.ico, SpringBoot 中只需要将 favicon.ico 放到任意静态资源存放路径下即可

    自定义静态资源存放路径:

    ```properties
    # spring.resources.static-locations=路径
    spring.resources.static-locations=classpath:/res/,classpath:/static/
    ```

    自定义的静态资源存放路径会覆盖掉原来的路径

## Thymeleaf

SpringBoot 不支持 JSP, 推荐使用 Thymeleaf 模板引擎, 官网: https://www.thymeleaf.org/, 文档: https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#using-texts

要看 Thymeleaf 就先看 ThymeleafAutoConfiguration 类

```java
package org.springframework.boot.autoconfigure.thymeleaf;

@Configuration
@EnableConfigurationProperties({ThymeleafProperties.class})
@ConditionalOnClass({TemplateMode.class, SpringTemplateEngine.class})
@AutoConfigureAfter({WebMvcAutoConfiguration.class, WebFluxAutoConfiguration.class})
public class ThymeleafAutoConfiguration {
    // 内容省略
}
```

重点看 @EnableConfigurationProperties({ThymeleafProperties.class}) 设置的默认属性

```java
package org.springframework.boot.autoconfigure.thymeleaf;

@ConfigurationProperties(
    prefix = "spring.thymeleaf"
)
public class ThymeleafProperties {
    private static final Charset DEFAULT_ENCODING;
    public static final String DEFAULT_PREFIX = "classpath:/templates/";
    public static final String DEFAULT_SUFFIX = ".html";
    private boolean checkTemplate = true;
    private boolean checkTemplateLocation = true;
    private String prefix = "classpath:/templates/";
    private String suffix = ".html";
    private String mode = "HTML";
    private Charset encoding;
    private boolean cache;
    private Integer templateResolverOrder;
    private String[] viewNames;
    private String[] excludedViewNames;
    private boolean enableSpringElCompiler;
    private boolean renderHiddenMarkersBeforeCheckboxes;
    private boolean enabled;
    private final ThymeleafProperties.Servlet servlet;
    private final ThymeleafProperties.Reactive reactive;
    // 其他方法和属性省略
}
```

可以发现, Thymeleaf 默认使用 `html` 文件, 默认存放路径为 `classpath:/templates/`, 如果需要修改属性需要在配置文件中 `spring.thymeleaf.xx=xx`

使用: 官网示例

```html
<!DOCTYPE html>

<html xmlns:th="http://www.thymeleaf.org">
    <head>
        <title>Good Thymes Virtual Grocery</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="stylesheet" type="text/css" media="all" href="../../css/gtvg.css" th:href="@{/css/gtvg.css}" />
    </head>

    <body>
        <p th:text="#{home.welcome}">Welcome to our grocery store!</p>
    </body>
</html>
```

-   html 节点需要引入 `xmlns:th="http://www.thymeleaf.org"`

算了, 现在大多前后台分离, 先不弄这个了
