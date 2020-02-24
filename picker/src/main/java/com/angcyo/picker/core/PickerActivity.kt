package com.angcyo.picker.core

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.activity.showDebugInfoView
import com.angcyo.base.dslAHelper
import com.angcyo.base.dslFHelper
import com.angcyo.base.setNavigationBarColor
import com.angcyo.base.setStatusBarColor
import com.angcyo.core.component.dslPermissions
import com.angcyo.dialog.hideLoading
import com.angcyo.dialog.loading
import com.angcyo.library.L
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.havePermissions
import com.angcyo.library.ex.isDebug
import com.angcyo.library.toast
import com.angcyo.loader.LoaderConfig
import com.angcyo.luban.dslLuban
import com.angcyo.picker.R
import com.angcyo.viewmodel.VMProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
class PickerActivity : BaseAppCompatActivity() {

    companion object {
        const val KEY_LOADER_CONFIG = "key_loader_config"
        const val KEY_SELECTOR_MEDIA_LIST = "key_selector_media_list"
        const val PICKER_REQUEST_CODE = 0x9089

        /**关闭界面发送数据*/
        fun send(fragment: Fragment?) {
            val activity = fragment?.activity
            if (activity is PickerActivity) {
                activity.send()
            } else {
                L.w("activity is not PickerActivity!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //setTheme()
        super.onCreate(savedInstanceState)
        //requestedOrientation
        setStatusBarColor()
        setNavigationBarColor(com.angcyo.library.ex.getColor(R.color.picker_bottom_bar_bg_color))
    }

    override fun onCreateAfter(savedInstanceState: Bundle?) {
        super.onCreateAfter(savedInstanceState)

        val config: LoaderConfig? = intent.getParcelableExtra(KEY_LOADER_CONFIG)
        if (config == null) {
            toast("数据异常.")
            onBackPressed()
        } else {
            //使用[ViewModel]在同一个Activity的多个Fragment共享数据
            pickerViewModel.loaderConfig.value = config
            pickerViewModel.selectorMediaList.value?.addAll(config.selectorMediaList)
            checkPermission()
        }
    }

    override fun onShowDebugInfoView(show: Boolean) {
        showDebugInfoView(show, isDebug(), Gravity.RIGHT or Gravity.BOTTOM)
    }

    fun checkPermission() {

        if (havePermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            onPermissionGranted()
        } else {
            dslPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                if (it) {
                    onPermissionGranted()
                } else {
                    onBackPressed()
                    toast("请允许权限.")
                }
            }
        }
    }

    fun onPermissionGranted() {
        dslFHelper {
            removeAll()
            restore(PickerImageFragment())
        }
    }

    override fun onBackPressed() {
        if (onBackPressedDispatcher()) {
            dslFHelper {
                if (back()) {
                    dslAHelper {
                        finish {
                            exitAnim =
                                R.anim.lib_picker_exit_anim
                            enterAnim =
                                R.anim.lib_picker_other_enter_anim
                        }
                    }
                }
            }
        }
    }

    /**通过[PickerViewModel]在[Fragment]之间共享数据*/
    val pickerViewModel: PickerViewModel by VMProperty(
        PickerViewModel::class.java
    )

    /**发送数据*/
    fun send() {
        if (pickerViewModel.selectorOrigin.value == true) {
            //原图
            _sendInner()
        } else {
            //压缩
            if (pickerViewModel.selectorMediaList.value.isNullOrEmpty()) {
                _sendInner()
            } else {
                dslLuban(this) {
                    targetMediaList = pickerViewModel.selectorMediaList.value!!

                    onCompressStart = {
                        loading("正在压缩...") {
                            cancel()
                        }
                    }

                    onCompressEnd = {
                        hideLoading()
                        _sendInner()
                    }
                }
            }
        }
    }

    private fun _sendInner() {
        dslAHelper {
            finish {
                exitAnim = R.anim.lib_picker_exit_anim
                enterAnim = R.anim.lib_picker_other_enter_anim

                //数据
                pickerViewModel.selectorMediaList.value?.apply {

                    if (L.debug) {
                        L.i(buildString {
                            appendln()
                            this@apply.forEachIndexed { index, loaderMedia ->
                                append(index + 1)
                                append("->")
                                append(loaderMedia.mimeType)
                                append(" 本地:${loaderMedia.localPath}(${loaderMedia.localPath.fileSizeString()})")
                                append(" 本地Uri:${loaderMedia.localUri}")
                                appendln()
                                append("先剪裁:${loaderMedia.cropPath}(${loaderMedia.cropPath.fileSizeString()})")
                                appendln()
                                append("后压缩:${loaderMedia.compressPath}(${loaderMedia.compressPath.fileSizeString()})")
                                appendln()
                                appendln()
                            }
                        })
                    }

                    resultCode = Activity.RESULT_OK
                    resultData = Intent().run {
                        putParcelableArrayListExtra(
                            KEY_SELECTOR_MEDIA_LIST,
                            ArrayList(this@apply)
                        )
                    }
                }
            }
        }
    }
}