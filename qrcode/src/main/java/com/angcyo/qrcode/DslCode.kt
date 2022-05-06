package com.angcyo.qrcode

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.angcyo.fragment.dslBridge
import com.angcyo.library.L
import com.angcyo.library.ex.dpi
import com.angcyo.rcode.RCode
import com.angcyo.rcode.ScanActivity
import com.angcyo.rcode.ScanFragment
import com.google.zxing.BarcodeFormat

/**
 * 二维码扫码界面, 并获取扫码结果
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

/**快速启动二维码扫一扫界面, 并且获取返回值*/
fun dslCode(activity: Activity?, action: DslCode.() -> Unit) {
    DslCode().apply {
        action()
        doIt(activity)
    }
}

/**扩展*/
fun Fragment?.dslCode(action: DslCode.() -> Unit) {
    dslCode(this?.activity, action)
}

/**使用字符串, 创建二维码*/
fun CharSequence.createQRCode(
    size: Int = 100 * dpi,
    foregroundColor: Int = Color.BLACK,
    backgroundColor: Int = Color.WHITE,
    logo: Bitmap? = null
): Bitmap? = RCode.syncEncodeQRCode(this.toString(), size, foregroundColor, backgroundColor, logo)

/**使用字符串, 创建条形码, 不支持中文*/
fun CharSequence.createBarCode(
    width: Int = 300 * dpi,
    height: Int = 100 * dpi,
    foregroundColor: Int = Color.BLACK,
    backgroundColor: Int = Color.WHITE,
    logo: Bitmap? = null
): Bitmap? = RCode.syncEncodeCode(
    this.toString(),
    width,
    height,
    foregroundColor,
    backgroundColor,
    logo,
    BarcodeFormat.CODE_128
)