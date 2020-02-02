package com.angcyo.picker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.activity.showDebugInfoView
import com.angcyo.base.dslAHelper
import com.angcyo.base.dslFHelper
import com.angcyo.base.setNavigationBarColor
import com.angcyo.base.setStatusBarColor
import com.angcyo.core.component.dslPermissions
import com.angcyo.library.ex.isDebug
import com.angcyo.library.toast
import com.angcyo.loader.LoaderConfig
import com.angcyo.viewmodel.vm

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            vm<PickerViewModel>().loaderConfig.value = config
            checkPermission()
        }
    }

    override fun onShowDebugInfoView(show: Boolean) {
        showDebugInfoView(show, isDebug(), Gravity.RIGHT or Gravity.BOTTOM)
    }

    fun checkPermission() {
        dslPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            if (it) {
                onPermissionGranted()
            } else {
                onBackPressed()
                toast("请允许权限.")
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
                            exitAnim = R.anim.lib_picker_exit_anim
                            enterAnim = R.anim.lib_picker_other_enter_anim
                            resultCode = Activity.RESULT_CANCELED
                            resultData = Intent().apply {
                                //putParcelableArrayListExtra(KEY_SELECTOR_MEDIA_LIST, mutableListOf<LoaderMedia>())
                            }
                        }
                    }
                }
            }
        }
    }
}