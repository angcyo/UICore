package com.angcyo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultCaller
import androidx.fragment.app.Fragment
import com.angcyo.DslTargetIntent.Companion.ACTION_START_FRAGMENT
import com.angcyo.DslTargetIntent.Companion.ACTION_START_INTENT
import com.angcyo.DslTargetIntent.Companion.KEY_ORIGIN_INTENT
import com.angcyo.DslTargetIntent.Companion.KEY_TARGET_ACTION
import com.angcyo.DslTargetIntent.Companion.KEY_TARGET_FLAGS
import com.angcyo.DslTargetIntent.Companion.KEY_TARGET_FRAGMENT
import com.angcyo.DslTargetIntent.Companion.KEY_TARGET_INTENT
import com.angcyo.base.dslAHelper
import com.angcyo.base.dslFHelper
import com.angcyo.library.L
import com.angcyo.library.ex.have

/**
 * 目标[Intent]跳转处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/20
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class DslTargetIntent {

    companion object {

        /**子的Action*/
        const val KEY_TARGET_ACTION = "key_target_action"

        /**需要启动的[Fragment]类*/
        const val KEY_TARGET_FRAGMENT = "key_target_fragment"

        /**启动一个Intent*/
        const val KEY_TARGET_INTENT = "key_target_intent"

        /**原始的Intent, 用于获取数据*/
        const val KEY_ORIGIN_INTENT = "key_origin_intent"

        /**flag*/
        const val KEY_TARGET_FLAGS = "key_target_flags"

        /**启动一个[Intent]*/
        const val ACTION_START_INTENT = "action_start_intent"

        /**启动一个[Fragment]*/
        const val ACTION_START_FRAGMENT = "action_start_fragment"
    }

    /**处理了目标回调*/
    var targetIntentHandle: (targetIntent: Intent) -> Unit = {}

    /**处理参数中的目标[Intent]信息*/
    fun doIt(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            L.i("nothing to do.")
            return
        }

        val targetAction = intent.getStringExtra(KEY_TARGET_ACTION)
        if (targetAction.isNullOrEmpty()) {
            L.i("no action to do.")
        } else if (targetAction == ACTION_START_FRAGMENT) {
            if (context is ActivityResultCaller) {
                val fCls = intent.getSerializableExtra(KEY_TARGET_FRAGMENT) as? Class<Fragment>
                if (fCls == null) {
                    L.w("启动目标[KEY_TARGET_FRAGMENT]为空, 跳过操作!")
                } else {
                    val flags = intent.getIntExtra(KEY_TARGET_FLAGS, 0)
                    val singleTop = flags.have(Intent.FLAG_ACTIVITY_SINGLE_TOP)

                    context.dslFHelper {
                        if (singleTop) {
                            restore(fCls) {
                                arguments = intent.extras
                            }
                        } else {
                            show(fCls) {
                                arguments = intent.extras
                            }
                        }
                    }

                    targetIntentHandle(intent)
                }
            }
        } else if (targetAction == ACTION_START_INTENT) {
            val targetIntent: Intent? = intent.getParcelableExtra(KEY_TARGET_INTENT)
            if (targetIntent == null) {
                L.w("启动目标[KEY_TARGET_INTENT]为空, 跳过操作!")
            } else {
                context.dslAHelper {
                    start(targetIntent)
                }
                targetIntentHandle(targetIntent)
            }
        }
    }
}

/**快速处理*/
fun Context.dslTargetIntentHandle(intent: Intent?, action: DslTargetIntent.() -> Unit = {}) {
    DslTargetIntent().apply {
        action()
        doIt(this@dslTargetIntentHandle, intent)
    }
}

/**设置目标[Intent]*/
fun Intent.setTargetIntent(intent: Intent?) {
    putExtra(KEY_TARGET_INTENT, intent)
    setOriginIntent()
}

/**原始的[Intent]*/
fun Intent.setOriginIntent() {
    putExtra(KEY_ORIGIN_INTENT, this)
}

/**获取原始的[Intent]*/
fun Bundle.getOriginIntent(): Intent? = getParcelable(KEY_ORIGIN_INTENT)

/**通过一个[Activity]启动一个[Intent]*/
fun Intent.startIntent(targetIntent: Intent): Intent {
    putExtra(KEY_TARGET_ACTION, ACTION_START_INTENT)
    putExtra(KEY_TARGET_INTENT, targetIntent)
    setOriginIntent()
    return this
}

/**通过一个[Activity]启动一个[Fragment]*/
fun Intent.startFragment(cls: Class<out Fragment>, singleTask: Boolean = false): Intent {
    putExtra(KEY_TARGET_ACTION, ACTION_START_FRAGMENT)
    putExtra(KEY_TARGET_FRAGMENT, cls)
    setOriginIntent()
    if (singleTask) {
        putExtra(KEY_TARGET_FLAGS, Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    return this
}