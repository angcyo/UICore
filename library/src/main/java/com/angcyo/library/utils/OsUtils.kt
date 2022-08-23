package com.angcyo.library.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/23
 */
object OsUtils {

    //@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    fun atLeastT(): Boolean {
        return Build.VERSION.SDK_INT >= 33
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    fun atLeastS(): Boolean {
        return Build.VERSION.SDK_INT >= 31
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
    fun atLeastR(): Boolean {
        return Build.VERSION.SDK_INT >= 30
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
    fun atLeastQ(): Boolean {
        return Build.VERSION.SDK_INT >= 29
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    fun atLeastP(): Boolean {
        return Build.VERSION.SDK_INT >= 28
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    fun atLeastO(): Boolean {
        return Build.VERSION.SDK_INT >= 26
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
    fun atLeastN(): Boolean {
        return Build.VERSION.SDK_INT >= 24
    }
}
