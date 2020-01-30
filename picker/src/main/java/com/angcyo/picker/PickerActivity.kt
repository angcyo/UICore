package com.angcyo.picker

import android.Manifest
import android.os.Bundle
import com.angcyo.activity.BaseAppCompatActivity
import com.angcyo.base.dslAHelper
import com.angcyo.base.dslFHelper
import com.angcyo.core.component.dslPermissions
import com.angcyo.library.toast

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
class PickerActivity : BaseAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateAfter(savedInstanceState: Bundle?) {
        super.onCreateAfter(savedInstanceState)

        dslPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            if (it) {
                onPermissionGranted()
            } else {
                dslAHelper {
                    finishSelf = true
                }
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
}