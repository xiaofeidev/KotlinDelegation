package com.xiaofeidev.delegatedemo.demo

class Derived(b: Base) : Base by b

/*
class Derived(val delegate: Base) : Base {
    override fun print() {
        delegate.print()
    }
}

class Derived(val delegate: Base){
    fun print() {
        delegate.print()
    }
}*/
