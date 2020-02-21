package com.angcyo.ucrop

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.angcyo.fragment.FragmentBridge
import com.angcyo.library.L
import com.angcyo.library.ex._color
import com.angcyo.picker.R
import com.yalantis.ucrop.UCrop

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/20
 */

class DslUcrop {

    /**需要剪裁的图片uri*/
    var cropUri: Uri? = null

    /**需要保存剪裁的图片uri*/
    var cropSaveUri: Uri? = null

    /**圆形暗层,默认是矩形*/
    var cropDimmedCircle = false

    var enterAnim = R.anim.lib_picker_enter_anim
    var exitAnim = R.anim.lib_picker_other_exit_anim

    var onConfigUcrop: (UCrop) -> Unit = {}

    var onConfigOption: (UCrop.Options) -> Unit = {}

    var onResult: (resultCode: Int, data: Intent?) -> Unit = { _, _ -> }

    fun doIt(fragment: Fragment) {
        doIt(fragment.context!!, fragment.parentFragmentManager)
    }

    fun doIt(context: Context, fragmentManager: FragmentManager) {

        if (cropUri == null) {
            L.w("crop uri is null.")
            return
        }

        val intent = UCrop.of(cropUri!!, cropSaveUri!!).run {
            //指定剪切框的比例, 这样就看不到其他比例选项了. 0,0可以恢复默认
            //withAspectRatio(1f, 1f)
            //最大输出尺寸
            //withMaxResultSize(100, 100)

            withOptions(UCrop.Options().apply {
                setCircleDimmedLayer(cropDimmedCircle)
                //withAspectRatio(1f, 1f)
                setToolbarColor(_color(R.color.picker_title_bar_bg_color)) //标题栏背景颜色
                //setStatusBarColor(Color.TRANSPARENT)
                //setToolbarTitle("")
                //setActiveWidgetColor(Color.RED)
                setActiveControlsWidgetColor(_color(R.color.colorAccent))//底部控件图标颜色
                setToolbarWidgetColor(Color.WHITE) //标题栏图标颜色, 文本颜色
                //setRootViewBackgroundColor()//底部控制栏背景, 内容布局背景颜色
                onConfigOption(this)
            })

            onConfigUcrop(this)

            val intent = Intent().apply {
                setClass(context, RCropActivity::class.java)
                fragment.arguments?.let { putExtras(it) }
            }

            intent
        }

        FragmentBridge.install(fragmentManager)
            .startActivityForResult(intent, observer = onResult)

        //anim
        if (context is Activity) {
            if (enterAnim != -1 || exitAnim != -1) {
                context.overridePendingTransition(enterAnim, exitAnim)
            }
        }
    }
}

fun dslUcrop(fragment: Fragment, action: DslUcrop.() -> Unit) {
    DslUcrop().apply {
        action()
        doIt(fragment)
    }
}