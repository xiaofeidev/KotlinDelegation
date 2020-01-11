package com.xiaofeidev.delegatedemo.demo

import kotlin.reflect.KProperty

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