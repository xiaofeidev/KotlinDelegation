# KotlinDelegation
Kotlin 在语言层面支持的委托模式在 Android 开发中的应用

**委托模式**被证明是一种很好的替代**继承**的方式，Kotlin 在语言层面对委托模式提供了非常优雅的支持（语法糖）。

先给大家看看我用 Kotlin 的属性委托语法糖在 Android 工程里面做的一件有用工作——`SharedPreferences` 的读写委托。

~~文中陈列的所有代码已汇总成 Demo 传至 github，[点这儿获取源码](https://github.com/xiaofei-dev/KotlinDelegation)。~~

项目主要文件结构如下：

```java
│  App.kt
│
├─base
│      SpBase.kt
│
├─delegates
│      SPDelegates.kt
│      SPUtils.kt
│
├─demo
└─ui
        MainActivity.kt
```

先来看看 delegates 包下的文件。

`SPUtils` 是个读写 `SharedPreferences`(以下简称 SP) 项的基础工具类：

```kotlin
/**
 * @author xiaofei_dev
 * @desc 读写 SP 存储项的基础工具类
 */

object SPUtils {
    val SP by lazy {
        App.instance.getSharedPreferences("default", Context.MODE_PRIVATE)
    }

    //读 SP 存储项
    fun <T> getValue(name: String, default: T): T = with(SP) {
        val res: Any = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default) ?: ""
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> throw java.lang.IllegalArgumentException()
        }
        @Suppress("UNCHECKED_CAST")
        res as T
    }

    //写 SP 存储项
    fun <T> putValue(name: String, value: T) = with(SP.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }.apply()
    }
}
```

代码主要使用泛型实现的 SP 读写，整体还是非常简洁易懂的。上下文对象使用了自定义的 `Application` 类实例。

## Kotlin 中的委托属性

下面重点来看一下 `SPDelegates` 类的定义：

```kotlin
/**
 * @author xiaofei_dev
 * @desc <p>读写 SP 存储项的轻量级委托类，如下，
 * 读 SP 的操作委托给该类对象的 getValue 方法，
 * 写 SP 操作委托给该类对象的 setValue 方法，
 * 注意这两个方法不用你显式调用，把一切交给编译器就行（还是语法糖）
 * 具体定义 SP 存储项的代码请参考 SpBase 文件</p>
 */

class SPDelegates<T>(private val key: String, private val default: T) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return SPUtils.getValue(key, default)
    }
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        SPUtils.putValue(key, value)
    }
}
```

`SPDelegates` 类实现了 Kotlin 标准库中声明的用于属性委托的 `ReadWriteProperty` 接口（这个类具体咋用后面会详细说到），从名字可以看出此接口是可读写的(适用于 `var` 声明的属性)，除此之外还有个 `ReadOnlyProperty` 接口(适用于 `val` 声明的属性)。

对于属性的委托类（以`SPDelegates`类为例），要求必须提供一个 `getValue()` 函数（和一个`setValue()`函数——对于 **var** 属性）。其`getValue` 方法的参数要求如下：

- `thisRef` —— 必须与 **属性所属类** 的类型（对于扩展属性——指被扩展的类型）相同或是它的超类型(后面会说到)；
- `property` —— 必须是类型 `KProperty<*>` 或其超类型。

对于其 `setValue` 方法，前两个参数同 `getValue`。第三个`value` 参数必须与属性同类型或是它的子类型。

以上概念暂时看不懂不要紧，下面通过委托属性的具体应用来帮助理解。

接着是具体使用到委托属性的 `SpBase` 单例类：

```kotlin
/**
 * @author xiaofei_dev
 * @desc 定义的 SP 存储项
 */
object SpBase{
    //SP 存储项的键
    private const val CONTENT_SOMETHING = "CONTENT_SOMETHING"


    // 这就定义了一个 SP 存储项
    // 把 SP 的读写操作委托给 SPDelegates 类的一个实例（使用 by 关键字，by 属于 Kotlin 的一个原语），
    // 此时访问 isBase (你可以简单把其看成 Java 里的一个静态变量)变量即是读取 SP 的操作，
    // 给 isBase 变量赋值即是写 SP 的操作，就是这么简单
    // 这里的 SPDelegates 对象 getValue 方法的 thisRef 参数的类型正是外层的 SpBase
    var contentSomething: String by SPDelegates(CONTENT_SOMETHING, "我是一个 SP 存储项，点击编辑我")
}
```

上面代码中，单例 `SpBase` 的属性 `contentSomething` 就是一个定义好的 SP 存储项。得益于语言级别的强大语法糖支持，写出来的代码可以如此简洁而优雅。读写 SP 存储项的请求通过属性委托给了一个 `SPDelegates` 对象，语法为

 `val/var <属性名>: <类型> by <表达式>`

其最后会被编译器解释成这样的代码(大致上)：

```kotlin
object SpBase{
    private const val CONTENT_SOMETHING = "CONTENT_SOMETHING"
    
    private val propDelegate = SPDelegates(CONTENT_SOMETHING, "我是一个 SP 存储项，点击编辑我")
    var contentSomething: String
        get() = propDelegate.getValue(this, this::contentSomething)
        set(value) = propDelegate.setValue(this, this::contentSomething, value)
}
```

还是比较容易理解的。下面给演示这个定义好的 SP 存储项如何使用，见 Demo 的 MainActivity 类文件：

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView(){
        //读取 SP 内容显示到界面上
        editContent.setText(SpBase.contentSomething)
        btnSave.setOnClickListener {
            //保存 SP 项
            SpBase.contentSomething = "${editContent.text}"
            Toast.makeText(this, R.string.main_save_success, Toast.LENGTH_SHORT).show()
        }
    }
}
```

整体比较简单，就是个读写 SP 存储项的过程。大家可以实际运行下 Demo 看看具体效果。

## 从零实现一个属性的委托类

上文述及的 `SPDelegates` 类实现了 Kotlin 标准库提供的 `ReadWriteProperty` 接口，我们当然也可以不借助任何接口来实现一个属性委托类，只要其提供一个`getValue()` 函数（和一个`setValue()`函数——对于 **var** 属性）并且符合我们上面讨论的参数要求就行。下面来单独定义一个平凡的属性委托类 `Delegate` （见 Demo 的 demo 包）：

```kotlin
/**
 * @author xiaofei_dev
 * @desc 不用实现任何接口的平凡属性委托类
 */

class Delegate<T> {
    private var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        println("$thisRef, thank you for delegating '${property.name}' to me! The value is $value")
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value = value
        println("$value has been assigned to '${property.name}' in $thisRef.")
    }
}
```

使用方式依旧：

```kotlin
class Example {
    //属性委托
    var p: String? by Delegate()
}

fun main(args: Array<String>) {
    val e = Example()
    e.p = "hehe"
    println(e.p)
}
```

控制台输出如下：

```html
hehe has been assigned to 'p' in com.xiaofeidev.delegatedemo.demo.Example@1fb3ebeb.
com.xiaofeidev.delegatedemo.demo.Example@1fb3ebeb, thank you for delegating 'p' to me! The value is hehe
hehe
```

你可以自己跑下试试~

## 关于委托模式

有必要单独花篇幅解释下何为**委托模式**。

简而言之，在委托模式中，有两个对象共同处理同一个请求，接受请求的对象将请求委托给另一个对象来处理：

```kotlin
//委托类
class Ink {
    fun print() {
        print("This message comes from the delegate class,Not Printer.")
    }
}

class Printer {
    //委托对象
    var ink = Ink()

    fun print() {
        //Printer 的实例会将请求委托给另一个对象（DelegateNormal 的对象）来处理
        ink.print()//调用委托对象的方法
    }
}

fun main(args: Array<String>) {
    val printer = Printer()
    printer.print()
}
```

控制台输出如下：

```html
This message comes from the delegate class,Not Printer.
```

委托模式使我们可以用聚合来代替继承，是许多其他设计模式（如状态模式、策略模式、访问者模式）的基础。

## Kotlin 的委托模式

Kotlin 可以做到零样板代码实现委托模式（而不是像上节展示的实现方式）！

比如我们现在有如下接口和类：

```kotlin
interface Base {
    fun print()
}

class BaseImpl(val x: Int) : Base {
    override fun print() { print(x) }
}
```

`Base` 接口想做的就是在控制台打印些什么东西。这没啥问题，我们已经在 `BaseImpl` 类里完整实现了 `Base` 接口。

此时我们想再给 Base 接口写一个实现可以这么做：

```kotlin
class Derived(b: Base) : Base by b
```

这其实跟下面的写法是等价的：

```kotlin
class Derived(val delegate: Base) : Base {
    override fun print() {
        delegate.print()
    }
}
```

只不过 Kotlin 通过编译器的黑魔法将许多模板代码封印在了 `by` 这样一个语言级别的原语中（又是语法糖）。使用：

```kotlin
fun main(args: Array<String>) {
    val b = BaseImpl(10)
    Derived(b).print()
}
```

控制台输出如下：

```html
10
```



## Kotlin 标准库中其他属性委托

说会属性委托，Kotlin 的标准库为一些有用的委托写好了工厂方法，下面一一列举。

### 延迟属性 Lazy

```kotlin
fun main(args: Array<String>) {
    //延迟计算属性的值，lambda 表达式中的逻辑只会执行一次(且是线程安全的)并记录结果，后续调用 get() 只是返回记录的结果
    val lazyValue: String by lazy {
        println("computed!")
        "Hello"
    }
    println(lazyValue)
    println(lazyValue)
}
```

控制台输出如下：

```html
computed!
Hello
Hello
```

### 可观察属性 Observable

`Delegates.observable()`接受两个参数：初始值与修改时处理程序。 每次给属性赋值时就会调用该处理程序（在赋值*后*执行）。其有三个参数：被赋值属性的 `KProperty` 对象、旧值与新值：

```kotlin
class User {
    var name: String by Delegates.observable("<no name>") {
            prop, old, new ->
        println("$old -> $new")
    }
}

fun main(args: Array<String>) {
    val user = User()
    user.name = "first"
    user.name = "second"
}
```

控制台输出如下：

```tex
<no name> -> first
first -> second
```

## 把属性储存在映射中

你甚至可以在一个映射（`map`）中存储属性的值。 这种情况下，你可以直接将属性委托给映射实例：

```kotlin
class Student(val map: Map<String, Any?>) {
    val name: String by map
    val age: Int     by map
}

fun main(args: Array<String>) {
    val student = Student(mapOf(
        "name" to "xiaofei",
        "age"  to 25
    ))

    println(student.name)
    println(student.age)
}
```

当然这种应用必须确保属性的名字和 `map`中的键值对应起来，不然你可能会收获一个 `NoSuchElementException` 运行时异常：

```html
java.util.NoSuchElementException: Key age1 is missing in the map.
```

言止于此，未完待续。

## 参考文献

1. 《设计模式：可复用面向对象软件的基础》
2. 《Kotlin for Android Developers》
3. 《Kotlin 极简教程》
4. 《Kotlin 核心编程》
5. 维基百科 [委托模式](https://zh.wikipedia.org/wiki/委托模式) 词条
6. [Kotlin 官方文档](https://www.kotlincn.net/docs/reference/delegation.html)

