package com.xiaofeidev.delegatedemo.demo

import kotlin.properties.Delegates

class Example {
    //属性委托
    var p: String? by Delegate()
}

class User {
    var name: String by Delegates.observable("<no name>") {
            prop, old, new ->
        println("$old -> $new")
    }
}

class Student(val map: Map<String, Any?>) {
    val name: String by map
    val age: Int     by map
}

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
    val e = Example()
    e.p = "hehe"
    println(e.p)
//
    //延迟计算属性的值，lambda 表达式中的逻辑只会执行一次并记录结果，后续调用 get() 只是返回记录的结果
    val lazyValue: String by lazy {
        println("computed!")
        "Hello"
    }
    println(lazyValue)
    println(lazyValue)
//
    val user = User()
    user.name = "first"
    user.name = "second"
//
    val student = Student(mapOf(
        "name" to "xiaofei",
        "age"  to 25
    ))

    println(student.name)
    println(student.age)
//
    val printer = Printer()
    printer.print()
}

