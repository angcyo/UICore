package com.angcyo.core.component.accessibility.parse

import com.angcyo.http.POST
import com.angcyo.http.base.jsonObject
import com.angcyo.http.post
import com.angcyo.http.rx.observer
import com.angcyo.library.L
import com.angcyo.library.utils.UrlParse
import io.reactivex.disposables.Disposable

/**
 * [AutoParseAction]完成后, 或者[AutoParseInterceptor]流程结束后, 要提交的表单数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

data class FormBean(
    //服务器地址, 尽量使用完整的地址
    var url: String? = null,
    //查询参数, a=2&b=3.3&c=name&d=true
    var query: String? = null,
    //提交方法, 目前只支持[POST]
    var method: Int = POST,
    //数据提交的类型
    var contentType: Int = CONTENT_TYPE_FORM
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

/**发送表单请求*/
fun FormBean.request(configParams: (params: HashMap<String, Any>) -> Unit = {}): Disposable? {
    return if (url.isNullOrEmpty()) {
        L.w("form url is null/empty.")
        null
    } else {
        //从url中, 获取默认参数
        val urlParams = UrlParse.getUrlQueryParams(query)

        post {
            url = this@request.url!!

            //请求参数
            val requestParams = HashMap<String, Any>().apply {
                putAll(urlParams)
                configParams(this)
            }

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
        }.observer()
    }
}