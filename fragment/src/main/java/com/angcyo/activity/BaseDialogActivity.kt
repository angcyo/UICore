package com.angcyo.activity

import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager

/**
 * 对话框形式的[Activity], 默认内容[WRAP_CONTENT]居中显示
 *
 * 使用主题:[LibDialogActivity]
 *
 * ```
 * android:enabled="true"
 * android:excludeFromRecents="true"
 * android:exported="true"
 * android:noHistory="true"
 * android:theme="@style/LibDialogActivity"
 * ```
 *
 * [com.angcyo.download.version.VersionUpdateActivity]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/28
 */
abstract class BaseDialogActivity : BaseAppCompatActivity() {

    /**对话框的宽高*/
    var dialogWidth: Int = WindowManager.LayoutParams.MATCH_PARENT

    var dialogHeight: Int = WindowManager.LayoutParams.WRAP_CONTENT

    /**Gravity*/
    var dialogGravity: Int = Gravity.CENTER

    /**点击窗口外是否可以关闭*/
    var closeOnTouchOutside: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateAfter(savedInstanceState: Bundle?) {
        configWindow()
        super.onCreateAfter(savedInstanceState)
    }

    /**需要放在[com.angcyo.activity.BaseAppCompatActivity.onCreate]之后调用*/
    open fun configWindow() {
        //点击窗口外是否关闭
        setFinishOnTouchOutside(closeOnTouchOutside)
        window.setLayout(dialogWidth, dialogHeight)

        //window.setDecorFitsSystemWindows(true)

        //对话框变暗指数, [0~1] [不变暗~全暗]
        //window.setDimAmount(0f)

        //gravity
        window.attributes = window.attributes.apply {
            gravity = dialogGravity
        }
    }

    override fun finish() {
        super.finish()
        onFinish()
    }

    open fun onFinish() {
        //去掉动画
        overridePendingTransition(0, 0)
    }
}