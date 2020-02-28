package com.angcyo.qrcode

import android.os.Bundle
import com.angcyo.base.translucentNavigationBar
import com.angcyo.rcode.ScanActivity

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
        translucentNavigationBar(true)
    }

    override fun handleDecode(data: String): Boolean {
        return super.handleDecode(data)
    }
}