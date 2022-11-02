package com.angcyo.fragment

import android.app.Activity

/**
 * [androidx.activity.OnBackPressedDispatcher]
 * [androidx.activity.OnBackPressedCallback]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/02
 */
interface IBackPressed {

    /**
     * [Activity] 的 [Activity.onBackPressed] 回调.
     *
     * @return true 允许关闭当前的Fragment
     */
    fun onBackPressed(): Boolean

}