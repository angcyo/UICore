package com.angcyo.qrcode

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.angcyo.rcode.ScanFragment

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/28
 */

open class CodeScanFragment : ScanFragment() {

    override fun getLayoutId(): Int {
        return R.layout.fragment_code_scan_layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initFragment(view: View, savedInstanceState: Bundle?) {
        super.initFragment(view, savedInstanceState)
        //闪光灯切换
        view.findViewById<CompoundButton>(R.id.light_switch_view)?.setOnClickListener {
            openFlashlight((it as CompoundButton).isChecked)
        }
    }

    override fun handleDecode(data: String?) {
        super.handleDecode(data)
    }
}