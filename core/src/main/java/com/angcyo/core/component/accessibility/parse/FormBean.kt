package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.AutoParseInterceptor
import com.angcyo.core.component.accessibility.action.ActionException
import com.angcyo.core.component.accessibility.action.ActionInterruptedException
import com.angcyo.core.component.accessibility.action.ActionInterruptedNextException
import com.angcyo.http.GET
import com.angcyo.http.POST
import com.angcyo.http.base.jsonObject
import com.angcyo.http.get
import com.angcyo.http.post
import com.angcyo.http.rx.observer
import com.angcyo.library.L
import com.angcyo.library.ex.isDebugType
import com.angcyo.library.ex.string
import com.angcyo.library.utils.UrlParse
import com.google.gson.JsonElement
import io.reactivex.disposables.Disposable
import retrofit2.Response
import kotlin.collections.set

/**
 * [AutoParseAction]完成后, 或者[AutoParseInterceptor]流程结束后, 要提交的表单数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

data class FormBean(
    //用于之后的停止子任务
    var formId: Long = -1,
    //服务器地址, 尽量使用完整的地址
    var url: String? = null,
    //查询参数, a=2&b=3.3&c=name&d=true
    var query: String? = null,
    //提交方法, 目前支持[POST] [GET]
    var method: Int = POST,
    //数据提交的类型
    var contentType: Int = CONTENT_TYPE_FORM,
    //不需要传递给后台的key集合
    var ignoreKeyList: List<String>? = null
) {
    companion object {
        //form data 形式
        const val CONTENT_TYPE_FORM = 1

        //json形式
        const val CONTENT_TYPE_JSON = 1

        //表单提交必备的上传字段
        const val KEY_CODE = "resultCode" //200..299 表示本地执行成功
        const val KEY_MSG = "resultMsg"   //成功或者失败时, 提示信息
        const val KEY_DATA = "resultData" //一些信息
    }
}

/**表单参数收集*/
fun FormBean.handleParams(configParams: (params: HashMap<String, Any>) -> Unit = {}): HashMap<String, Any> {
    //从url中, 获取默认参数
    val urlParams = UrlParse.getUrlQueryParams(query)

    //请求参数
    val requestParams = HashMap<String, Any>().apply {
        putAll(urlParams)
        configParams(this)
    }

    //remove key
    ignoreKeyList?.forEach {
        if (it.isNotEmpty()) {
            requestParams.remove(it)
        }
    }

    return requestParams
}

/**发送表单请求*/
fun FormBean.request(
    result: (
        data: Response<JsonElement>?,
        error: Throwable?
    ) -> Unit = { _, _ -> },
    configParams: (params: HashMap<String, Any>) -> Unit = {}
): Disposable? {
    return if (url.isNullOrEmpty()) {
        L.w("form url is null/empty.")

        if (isDebugType()) {
            val requestParams = handleParams(configParams)
            L.i("表单参数:", requestParams)
        }
        null
    } else {
        if (method == GET) {
            get {
                url = this@request.url!!

                //请求参数
                query = handleParams(configParams)
            }
        } else {
            post {
                url = this@request.url!!

                //请求参数
                val requestParams = handleParams(configParams)

                //请求体
                if (contentType == FormBean.CONTENT_TYPE_FORM) {
                    formMap = requestParams
                } else {
                    body = jsonObject {
                        requestParams.forEach { entry ->
                            add(entry.key, entry.value)
                        }
                    }
                }
            }
        }.observer {
            onObserverEnd = { data, error ->
                error?.let {
                    //错误日志, 写入acc
                    AutoParseInterceptor.log(it.string())
                }
                result(data, error)
                L.d(data, error)
            }
        }
    }
}

/**错误信息的赋值等*/
fun HashMap<String, Any>.bindErrorCode(error: ActionException?) {
    this[FormBean.KEY_CODE] = when (error) {
        null -> 200 //本地执行成功
        is ActionInterruptedNextException -> 301 //本地执行中断, 但是需要继续任务
        is ActionInterruptedException -> 300 //本地执行中断, 任务终止.
        else -> 500 //本地执行错误, 任务终止.
    }
}