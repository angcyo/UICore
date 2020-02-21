package com.angcyo.ucrop

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.angcyo.base.enableLayoutFullScreen
import com.angcyo.base.setNavigationBarColor
import com.angcyo.base.setStatusBarColor
import com.angcyo.picker.R
import com.angcyo.widget.base.getChildOrNull
import com.yalantis.ucrop.UCropActivity

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/20
 */
class RCropActivity : UCropActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableLayoutFullScreen()
        setStatusBarColor()
        setNavigationBarColor(com.angcyo.library.ex.getColor(R.color.picker_bottom_bar_bg_color))

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar?.fitsSystemWindows = true


        findViewById<ViewGroup>(R.id.layout_aspect_ratio)?.run {
            //默认选中1:1
            getChildOrNull(0)?.performClick()
        }
    }

    override fun cropAndSaveImage() {
        super.cropAndSaveImage()
    }

    override fun finish() {
        super.finish()
        _animOverride()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        _animOverride()
    }

    var exitAnim = R.anim.lib_picker_exit_anim
    var enterAnim = R.anim.lib_picker_other_enter_anim

    fun _animOverride() {
        if (enterAnim != -1 || exitAnim != -1) {
            overridePendingTransition(enterAnim, exitAnim)
        }
    }
}