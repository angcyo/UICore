package com.angcyo.core.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.angcyo.DslAHelper
import com.angcyo.activity.logActivityInfo
import com.angcyo.base.dslAHelper
import com.angcyo.noAnim

/**
 * 跳板Activity
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class JumpActivity : Activity() {

    companion object {
        const val KEY_JUMP_TARGET = "key_jump_target"

        /**[targetIntent] 跳转的真实目标*/
        fun jump(context: Context, targetIntent: Intent) {
            DslAHelper(context).apply {
                start(Intent(context, JumpActivity::class.java)) {
                    noAnim()
                    intent.putExtra(KEY_JUMP_TARGET, targetIntent)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                doIt()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        logActivityInfo()

        intent?.run {
            val targetIntent: Intent? = getParcelableExtra(KEY_JUMP_TARGET)
            targetIntent?.also { intent ->
                //跳转到真正目标
                dslAHelper {
                    start(intent)
                }
            }
        }
        finish()
        overridePendingTransition(0, 0)
    }
}