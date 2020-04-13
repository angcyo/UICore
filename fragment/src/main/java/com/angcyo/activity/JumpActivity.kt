package com.angcyo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.angcyo.DslAHelper
import com.angcyo.base.dslAHelper
import com.angcyo.library.ex.fillFrom
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

        /**需要跳转的目标[Intent]*/
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
            //清空Extra数据, 防止在[fillFrom]时, 死循环.
            putExtra(KEY_JUMP_TARGET, null as Parcelable?)

            targetIntent?.also { tIntent ->

//                if (tIntent.extras?.isEmpty == true) {
//                    if (extras?.isEmpty == true) {
//                    } else {
//                        extras?.run { tIntent.putExtras(this) }
//                    }
//                }

                //只需要填充[mExtras]即可
                tIntent.fillFrom(this, 0)

                //跳转到真正目标
                dslAHelper {
                    start(tIntent)
                }
            }
        }
        finish()
        overridePendingTransition(0, 0)
    }
}