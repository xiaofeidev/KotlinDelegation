package com.xiaofeidev.delegatedemo.base

import com.xiaofeidev.delegatedemo.delegates.SPDelegates

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
    var contentSomething: String by SPDelegates(CONTENT_SOMETHING, "我是一个 SP 存储项，点击编辑我")
}

