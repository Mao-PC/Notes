# IOC 容器

[TOC]

## 自己实现 ioc

对于 IOC 来说最重要的就是容器, 解析用户定义的 XML 文件将各种 Bean 预先放入容器中, 控制 Bean 的生命周期. 而这个容器应该使用的应该是 Map, 以方便注入

### xml 注入

**用户定义的 XML 文件** >>> **dom4j 解析** >>> **解析 class 标签后反射得到 Bean** >>> **将 Bean 放到 Map 中**

```java
package spring.ioc;

import com.study.test.TestBeanService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyIOC {
    // 配置文件路径
    private String xmlPath;

    // 存放Bean的map
    static Map<String, Object> map = new ConcurrentHashMap<>();

    public MyIOC(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    /**
     * 获取Bean
     *
     * @param id
     * @return
     */
    public Object getBean(String id) throws Exception {

        if (id == null) throw new IllegalArgumentException("ID 不能为空");
        if (map.containsKey(id)) return map.get(id);

        List<Element> elements = readXML();
        if (elements == null) throw new Exception("不存在的 ID");
        String clzName = findByElementClass(elements, id);
        if (clzName == null) throw new Exception("不存在的类型");

        return newInstance(clzName);
    }

    public List<Element> readXML() throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(getResource(xmlPath));
        Element root = document.getRootElement();
        return root.elements();
    }

    public String findByElementClass(List<Element> elements, String beanId) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        for (Element element : elements) {
            String id = element.attributeValue("id");
            if (beanId.equals(id)) {
                String clz = element.attributeValue("class");
                map.put(id, newInstance(clz));
                return clz;
            }
        }
        return null;
    }

    private Object newInstance(String clz) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return Class.forName(clz).newInstance();
    }

    private InputStream getResource(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

    public static void main(String[] args) throws Exception {
        MyIOC myIOC = new MyIOC("spring-context.xml");
        TestBeanService testBeanService = (TestBeanService) myIOC.getBean("testBeanService");
        System.out.println(testBeanService);
        testBeanService.test();
        Object test1 = myIOC.getBean("test1");
        System.out.println(test1);
    }
}
```

测试结果

```
com.study.test.impl.TestBeanServiceImpl@506e1b77
这是一个测试
Exception in thread "main" java.lang.Exception: 不存在的类型
	at spring.ioc.MyIOC.getBean(MyIOC.java:39)
	at spring.ioc.MyIOC.main(MyIOC.java:76)

```

### 注解注入

**扫描资源路径** >>> **找到带某个特定注解** >>> **放到 map 中**

工具类 `ClassUtil` 直接复制使用即可

```java
package spring.ioc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

    /**
     * 通过包名获取包内所有类
     *
     * @param pkg
     * @return
     */
    public static List<Class<?>> getAllClassByPackageName(Package pkg) {
        String packageName = pkg.getName();
        // 获取当前包下以及子包下所以的类
        List<Class<?>> returnClassList = getClasses(packageName);
        return returnClassList;
    }

    /**
     * 通过接口名取得某个接口下所有实现这个接口的类
     */
    public static List<Class<?>> getAllClassByInterface(Class<?> c) {
        List<Class<?>> returnClassList = null;

        if (c.isInterface()) {
            // 获取当前的包名
            String packageName = c.getPackage().getName();
            // 获取当前包下以及子包下所以的类
            List<Class<?>> allClass = getClasses(packageName);
            if (allClass != null) {
                returnClassList = new ArrayList<Class<?>>();
                for (Class<?> cls : allClass) {
                    // 判断是否是同一个接口
                    if (c.isAssignableFrom(cls)) {
                        // 本身不加入进去
                        if (!c.equals(cls)) {
                            returnClassList.add(cls);
                        }
                    }
                }
            }
        }

        return returnClassList;
    }

    /**
     * 取得某一类所在包的所有类名 不含迭代
     */
    public static String[] getPackageAllClassName(String classLocation, String packageName) {
        // 将packageName分解
        String[] packagePathSplit = packageName.split("[.]");
        String realClassLocation = classLocation;
        int packageLength = packagePathSplit.length;
        for (int i = 0; i < packageLength; i++) {
            realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
        }
        File packeageDir = new File(realClassLocation);
        if (packeageDir.isDirectory()) {
            String[] allClassName = packeageDir.list();
            return allClassName;
        }
        return null;
    }

    /**
     * 从包package中获取所有的Class
     *
     * @return
     */
    private static List<Class<?>> getClasses(String packageName) {

        // 第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), recursive, classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

```java
package spring.ioc;

import com.study.test.App;
import com.study.test.TestBeanService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyAnnotationIOC {

    // 存放Bean的map
    static Map<String, Object> map = new ConcurrentHashMap<>();

    public Object getBean(String id) throws InstantiationException, IllegalAccessException {

        if (id == null) throw new IllegalArgumentException("ID 不能为空");
        if (map.containsKey(id)) return map.get(id);

        return doCreateBean(id);
    }

    private Object doCreateBean(String classId) throws IllegalAccessException, InstantiationException {

        Object o = null;

        // 这里仿照 SpringBoot 方式, 只扫描启动类下的包
        List<Class<?>> classes = ClassUtil.getAllClassByPackageName(App.class.getPackage());

        for (Class<?> clz : classes) {
            MyComponent annotation = clz.getAnnotation(MyComponent.class);
            if (annotation == null) continue;

            String name = clz.getSimpleName();

            // 获取id
            String id = name.substring(0, 1).toLowerCase() + name.substring(1);

            Object instance = clz.newInstance();

            if (id.equals(classId)) o = instance;

            map.put(id, instance);
        }

        return o;
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        MyAnnotationIOC ioc = new MyAnnotationIOC();
        TestBeanService testBeanService = (TestBeanService) ioc.getBean("testBeanServiceImpl");
        System.out.println(testBeanService);
        testBeanService.test();
    }
}
```

测试结果

```
com.study.test.impl.TestBeanServiceImpl@7a92922
这是一个测试
```

## ApplicationContext

### Spring的核心机制

**IoC 控制反转(依赖注入)的基本概念是** : 不创建对象, 但是描述它们的创建方式. 在代码中不直接与对象和服务连接, 但是在配置文件中描述每个组件和需要哪一项服务. 容器(在 Spring 中是 IOC 容器)负责将这些连接起来

IoC 的作用:

-   只是一种思想, 不是一种技术, 一个重要的面向对象的编程法则, 它能指定我们如何设计出松耦合, 更优良的程序
-   其实IoC对编程带来最大的改变 不是从代码上, 而是从思想上, 发生了"主从换位"的变化. 
-   IoC很好的体现了设计原则中的**依赖倒转**原则

#### 依赖倒转原则

依赖倒转原则: 

1. 上层模块不应该依赖底层模块, 它们都应该依赖于抽象
2. 抽象不应该依赖于细节, 细节应该依赖于抽象

依赖倒转在Spring中的表现: 

- 模块间的依赖由抽象发生, 具体的实现不发生直接的依赖关系, Spring中对所有的依赖关系都是通过接口来产生的
- Spring中对Bean的构造/获取/注入都是自动去进行构造的, 在使用Spring框架的过程中, 无需去实例化具体的抽象, 而是Spring框架去实现. 我们需要做的事仅仅是使用, 我们的代码实现就依赖于Spring通过的接口来完成
- Spring提供了**ApplicationContext**作为接口, 来完成IoC容器的依赖倒转

我们已经知道了Spring IoC会将所有的Bean存放到一个Map 中, 那Spring是如何做事件发布的呢? Spring是如何读取用户定义的`xml`或者`properties`配置文件的呢? **ApplicationContext**就是来实现这些Map实现不了的功能的. 在大多时候我们会把**ApplicationContext**翻译为上下文, 其实更合适的理解可以将**ApplicationContext**理解为Spring为了将Map中存放的Bean能够实现**依赖倒转**功能的方法容器



请到GitHub上下载spring-framework源码配合 [5.2.x分支](https://github.com/spring-projects/spring-framework/tree/5.2.x)



