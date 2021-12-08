package com.angcyo.base

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.angcyo.base.IFragmentResult.Companion.KEY_RESULT
import com.angcyo.base.IFragmentResult.Companion._doResult
import com.angcyo.base.IFragmentResult.Companion._setRef
import kotlin.collections.set

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IFragmentResult {

    companion object {
        val KEY_RESULT = "key_fragment_result"

        //弱引用保存回调实例
        val resultRefMap = HashMap<Int, IFragmentResult?>()

        fun _clearRef(key: Int) {
            resultRefMap[key] = null
        }

        fun _setRef(key: Int, result: IFragmentResult) {
            _clearRef(key)
            resultRefMap[key] = result
        }

        fun _doResult(key: Int, resultCode: Int, data: Any?) {
            try {
                resultRefMap[key]?.onFragmentResult(resultCode, data)
            } finally {
                _clearRef(key)
            }
        }
    }

    /**
     * [resultCode] [Activity.RESULT_OK] [Activity.RESULT_CANCELED]
     * */
    fun onFragmentResult(resultCode: Int, data: Any?)
}

/**将对象打入[bundle]*/
fun Bundle.putFragmentResultKey(key: Int) {
    putInt(KEY_RESULT, key)
}

/**从对象[bundle]获取*/
fun Bundle.getFragmentResultKey(): Int = getInt(KEY_RESULT, -1)

/**监听[Fragment]返回*/
fun Fragment.onFragmentResult(result: (resultCode: Int, data: Any?) -> Unit) {
    val key = hashCode()
    val ref = object : IFragmentResult {
        override fun onFragmentResult(resultCode: Int, data: Any?) {
            result(resultCode, data)
        }
    }
    _setRef(key, ref)
    if (arguments == null) {
        arguments = Bundle()
    }
    arguments?.putFragmentResultKey(key)
}

/**设置[Fragment]返回数据*/
fun Fragment.setFragmentResult(
    data: Any?,
    resultCode: Int = if (data == null) Activity.RESULT_CANCELED else Activity.RESULT_OK
) {
    arguments?.getFragmentResultKey()?.apply {
        if (this != -1) {
            _doResult(this, resultCode, data)
        }
    }
}