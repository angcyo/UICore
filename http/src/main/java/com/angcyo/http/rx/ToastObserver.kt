package com.angcyo.http.rx

import com.angcyo.http.DslHttp
import com.angcyo.http.base.fromJson
import com.angcyo.http.base.getString
import com.angcyo.http.base.readString
import com.angcyo.http.isCodeSuccess
import com.angcyo.library.toast
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import retrofit2.Response

/**
 * 带有错误Toast提示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class ToastObserver<T> : BaseObserver<T>() {

    var defaultErrorMsg = DslHttp.DEFAULT_ERROR_MSG

    //解析请求返回的json数据, 判断code是否是成功的状态, 否则走异常流程.
    var codeKey: String = DslHttp.DEFAULT_CODE_KEY
    var msgKey: String = DslHttp.DEFAULT_MSG_KEY

    /**是否显示toast提示*/
    var showToast = true

    override fun onError(t: Throwable) {
        super.onError(t)
        tipError(t.message)
    }

    override fun onNext(data: T & Any) {
        super.onNext(data)

        if (data is Response<*>) {
            var errorMsg: String? = null

            if (data.isSuccessful) {
                val bodyData = data.body()
                if (data.isSuccessful && bodyData is JsonObject) {

                    if (bodyData.has(codeKey)) {
                        if (bodyData.isCodeSuccess(codeKey)) {
                            //逻辑返回成功
                        } else {
                            errorMsg = bodyData.getString(msgKey) ?: defaultErrorMsg
                        }
                    }
                }
            } else {
                val errorBody = data.errorBody()?.readString()
                val errorJson: JsonObject? = errorBody?.fromJson()
                errorMsg = errorJson?.getString(msgKey) ?: defaultErrorMsg
            }

            if (errorMsg != null) {
                tipError(errorMsg)
            }
        }
    }

    fun tipError(msg: String?) {
        if (showToast) {
            toast(msg ?: defaultErrorMsg)
        }
    }
}

/**快速监听*/
fun <T> Observable<T>.toast(
    config: ToastObserver<T>.() -> Unit = {},
    end: (data: T?, error: Throwable?) -> Unit
): Disposable {
    return observer(ToastObserver<T>().apply {
        config()
        onObserverEnd = end
    })
}