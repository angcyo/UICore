package com.angcyo.qrcode

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.angcyo.fragment.dslBridge
import com.angcyo.library.L
import com.angcyo.rcode.ScanActivity
import com.angcyo.rcode.ScanFragment

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/28
 */

class DslCode {

    /**承载[codeFragment]的[Activity]*/
    var codeActivity: Class<out ScanActivity>? = CodeScanActivity::class.java
    /**扫码[Fragment]*/
    var codeFragment: Class<out ScanFragment>? = CodeScanFragment::class.java

    var onConfigIntent: (Intent) -> Unit = {}

    /**二维码扫码返回数据, null 表示取消, 需要使用[FragmentActivity]启动才有效*/
    var onResult: (String?) -> Unit = {}

    fun doIt(activity: Activity?) {
        if (activity == null) {
            L.w("activity is null.")
        } else if (codeActivity != null) {
            val intent = Intent(activity, codeActivity)
            intent.putExtra(ScanActivity.KEY_TARGET, codeFragment)

            onConfigIntent(intent)

            if (activity is FragmentActivity) {
                dslBridge(activity.supportFragmentManager) {
                    startActivityForResult(intent) { resultCode, data ->
                        onResult(ScanActivity.onResult(0, resultCode, data))
                    }
                }
            } else {
                activity.startActivityForResult(intent, ScanActivity.REQUEST_CODE)
            }

        } else {
            L.w("codeActivity is invalid.")
        }
    }
}

fun dslCode(activity: Activity?, action: DslCode.() -> Unit) {
    DslCode().apply {
        action()
        doIt(activity)
    }
}