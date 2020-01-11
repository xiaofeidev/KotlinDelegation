package com.xiaofeidev.delegatedemo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.xiaofeidev.delegatedemo.R
import com.xiaofeidev.delegatedemo.base.SpBase
import kotlinx.android.synthetic.main.activity_main.*

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
