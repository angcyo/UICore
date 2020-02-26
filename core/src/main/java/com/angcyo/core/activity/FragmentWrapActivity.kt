package com.angcyo.core.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.angcyo.DslAHelper
import com.angcyo.base.dslFHelper
import com.angcyo.core.fragment.BaseTitleFragment
import com.angcyo.library.L
import com.angcyo.library.component.RBackground

/**
 * Fragment容器Activity
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class FragmentWrapActivity : BaseCoreAppCompatActivity() {
    companion object {
        const val KEY_TARGET_FRAGMENT = "key_target_fragment"

        /**[targetIntent] 跳转的真实目标*/
        fun jump(context: Context, targetIntent: Intent, singTask: Boolean = true) {

            if (targetIntent.component == null) {
                L.w("需要设置启动的组件[component]")
                return
            }

            DslAHelper(context).apply {
                start(Intent(context, FragmentWrapActivity::class.java)) {
                    intent.putExtra(KEY_TARGET_FRAGMENT, targetIntent)

                    if (context !is Activity) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    if (singTask) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                }
                doIt()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    override fun onHandleIntent(intent: Intent, fromNew: Boolean) {
        super.onHandleIntent(intent, fromNew)
        val targetIntent: Intent? =
            intent.getParcelableExtra(FragmentWrapActivity.KEY_TARGET_FRAGMENT)
        targetIntent?.also { target ->
            target.component?.run {
                //跳转到真正目标
                dslFHelper {
                    try {
                        show(Class.forName(className) as Class<out Fragment>)
                        configFragment {
                            arguments = target.extras
                            if (this is BaseTitleFragment) {
                                enableBackItem = RBackground.stack.size() > 1
                            }
                        }
                    } catch (e: Exception) {
                        L.w(e)
                    }
                }
            }
        }
    }
}