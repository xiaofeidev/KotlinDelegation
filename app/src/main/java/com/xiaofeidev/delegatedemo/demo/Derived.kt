package com.xiaofeidev.delegatedemo.demo

class Derived(b: Base) : Base by b

class Derived1(val delegate: Base) : Base {
    override fun print() {
        delegate.print()
    }
}