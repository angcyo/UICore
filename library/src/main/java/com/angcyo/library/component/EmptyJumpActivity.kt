package com.angcyo.library.component

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.angcyo.library.ex.baseConfig

/**
 * 空的跳转Jump跳板界面
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/10
 */
class EmptyJumpActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}

/**启动一个跳板Activity
 * [EmptyJumpActivity]*/
fun startJumpActivity(context: Context = lastContext) {
    val intent = Intent(context, EmptyJumpActivity::class.java)
    intent.baseConfig(context)
    context.startActivity(intent)
}