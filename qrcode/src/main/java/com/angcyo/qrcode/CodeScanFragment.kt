package com.angcyo.qrcode

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.angcyo.library.L
import com.angcyo.rcode.ScanFragment

/**
 * 二维码扫描界面
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

    /**扫码识别结束回调*/
    override fun handleDecode(data: String?) {
        super.handleDecode(data)
        L.i("扫码结果:$data")

        //继续下一次扫码
        //scanAgain(160)
    }
}