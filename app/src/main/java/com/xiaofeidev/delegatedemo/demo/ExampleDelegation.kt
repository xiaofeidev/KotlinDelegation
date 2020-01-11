package com.xiaofeidev.delegatedemo.demo

interface Base {
    fun print()
}

class BaseImpl(val x: Int) : Base {
    override fun print() { print(x) }
}

fun main(args: Array<String>) {
    val b = BaseImpl(10)
    Derived(b).print()
}