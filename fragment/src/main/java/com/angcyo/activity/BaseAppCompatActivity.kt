package com.angcyo.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.angcyo.fragment.R
import com.angcyo.widget.DslViewHolder

/**
 * [Activity] 基类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
abstract class BaseAppCompatActivity : AppCompatActivity() {
    lateinit var baseDslViewHolder: DslViewHolder

    open fun getActivityLayoutId() = R.layout.lib_activity_main_layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseDslViewHolder = DslViewHolder(window.decorView)
        onCreateAfter(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { onHandleIntent(it, true) }
    }

    /**布局设置之后触发*/
    open fun onCreateAfter(savedInstanceState: Bundle?) {
        enableLayoutFullScreen()
        with(getActivityLayoutId()) {
            if (this > 0) {
                setContentView(this)
            }
        }
        intent?.let { onHandleIntent(it) }
    }

    /**
     * @param fromNew [onNewIntent]
     * */
    open fun onHandleIntent(intent: Intent, fromNew: Boolean = false) {

    }
}