package com.xiaofeidev.delegatedemo.delegates

import android.annotation.SuppressLint
import android.content.Context
import com.xiaofeidev.delegatedemo.App

/**
 * @author xiaofei_dev
 * @desc 读写 SP 存储项的基础工具类
 */

object SPUtils {
    val SP by lazy {
        App.instance.getSharedPreferences("default", Context.MODE_PRIVATE)
    }

    //读 SP 存储项
    fun <T> getValue(key: String, default: T): T = with(SP) {
        val res: Any = when (default) {
            is Long -> getLong(key, default)
            is String -> getString(key, default) ?: ""
            is Int -> getInt(key, default)
            is Boolean -> getBoolean(key, default)
            is Float -> getFloat(key, default)
            else -> throw java.lang.IllegalArgumentException()
        }
        @Suppress("UNCHECKED_CAST")
        res as T
    }

    //写 SP 存储项
    @SuppressLint("ApplySharedPref")
    fun <T> putValue(key: String, value: T, isCommit: Boolean = false) = with(SP.edit()) {
        val editor = when (value) {
            is Long -> putLong(key, value)
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            else -> throw IllegalArgumentException("This type can't be saved into Preferences")
        }
        if (isCommit){
            editor.commit()
        } else {
            editor.apply()
        }
    }
}