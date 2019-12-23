package com.angcyo.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class LogFragmentLifecycleCallbacks : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentPreCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentActivityCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        L.v("${f.javaClass.simpleName}\n${f.log()}")
    }
}