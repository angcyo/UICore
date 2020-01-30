package com.angcyo.picker

import android.os.Bundle
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.loader.DslLoader
import com.angcyo.loader.LoaderConfig

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */
class PickerImageFragment : BaseDslFragment() {
    val loader = DslLoader()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loader.startLoader(activity, LoaderConfig())
    }
}