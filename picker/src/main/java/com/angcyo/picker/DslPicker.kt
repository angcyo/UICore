package com.angcyo.picker

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.angcyo.DslAHelper
import com.angcyo.loader.LoaderConfig
import com.angcyo.loader.LoaderMedia
import com.angcyo.picker.DslPicker.picker
import com.angcyo.picker.PickerActivity.Companion.KEY_LOADER_CONFIG
import com.angcyo.picker.PickerActivity.Companion.KEY_SELECTOR_MEDIA_LIST
import com.angcyo.picker.PickerActivity.Companion.PICKER_REQUEST_CODE

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/31
 */
object DslPicker {

    /**开始选择器*/
    fun picker(context: Context?, config: LoaderConfig) {
        context?.run {
            DslAHelper(this).apply {
                start(PickerActivity::class.java) {
                    requestCode = PICKER_REQUEST_CODE
                    intent.putExtra(KEY_LOADER_CONFIG, config)
                    enterAnim = R.anim.lib_picker_enter_anim
                    exitAnim = R.anim.lib_picker_other_exit_anim
                }
                doIt()
            }
        }
    }

    /**开始选择器*/
    fun picker(context: Context?, action: LoaderConfig.() -> Unit) {
        val config = LoaderConfig()
        config.action()
        picker(context, config)
    }

    /**获取选择的媒体数据*/
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): List<LoaderMedia>? {
        if (data == null || resultCode != Activity.RESULT_OK || requestCode != PICKER_REQUEST_CODE) {
            return null
        }
        return data.getParcelableArrayListExtra(KEY_SELECTOR_MEDIA_LIST)
    }
}

/**使用[LoaderConfig]启动媒体选择器*/
fun Fragment.dslPicker(config: LoaderConfig) {
    picker(context, config)
}

/**配置启动媒体选择器*/
fun Fragment.dslPicker(action: LoaderConfig.() -> Unit) {
    picker(context, action)
}