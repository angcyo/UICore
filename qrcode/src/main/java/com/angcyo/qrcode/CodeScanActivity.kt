package com.angcyo.qrcode

import android.os.Bundle
import com.angcyo.base.translucentNavigationBar
import com.angcyo.base.translucentStatusBar
import com.angcyo.rcode.ScanActivity
import com.angcyo.rcode.ScanFragment

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/28
 */

open class CodeScanActivity : ScanActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initScanLayout(savedInstanceState: Bundle?) {
        super.initScanLayout(savedInstanceState)
        translucentStatusBar(true)
        translucentNavigationBar(true)
    }

    /**默认的目标[CodeScanFragment]*/
    open fun getTargetFragment(): Class<out ScanFragment> = CodeScanFragment::class.java

    override fun onPermissionsGranted() {
        val intent = intent
        if (intent != null) {
            val targetClass = intent.getSerializableExtra(KEY_TARGET) as? Class<*>
            if (targetClass == null) {
                intent.putExtra(KEY_TARGET, getTargetFragment())
            }
        }
        super.onPermissionsGranted()
    }

    override fun handleDecode(data: String): Boolean {
        return super.handleDecode(data)
    }
}