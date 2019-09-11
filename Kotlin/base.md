# 基础语法

## 常量和变量

常量: `val <标识符> : <类型> = <初始化值>`
变量: `var <标识符> : <类型> = <初始化值>`

Kotlin 会自动推断声明类型

```kotlin
var i: Int = 1
var name = "张三"
var num: Int
num = 0
```

在确定类型后赋给不同类型的值会编译报错

```kotlin
var i = 0;
i = "tset" // 编译报错
```
