package com.angcyo.base

import android.app.Activity
import android.os.Bundle
import com.angcyo.base.IFragmentResult.Companion.KEY_RESULT
import java.io.Serializable

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IFragmentResult : Serializable {

    companion object {
        val KEY_RESULT = "key_fragment_result"
    }

    /**
     * [resultCode] [Activity.RESULT_OK] [Activity.RESULT_CANCELED]
     * */
    fun onFragmentResult(resultCode: Int, data: Any?)
}

/**将对象打入[bundle]*/
fun Bundle.putFragmentResult(result: IFragmentResult?) {
    putSerializable(KEY_RESULT, result)
}

/**从对象[bundle]获取*/
fun Bundle.doFragmentResult(data: Any?, resultCode: Int = Activity.RESULT_OK) {
    (getSerializable(KEY_RESULT) as? IFragmentResult?)?.onFragmentResult(resultCode, data)
}