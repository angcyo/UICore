package com.angcyo.acc2.bean

import com.angcyo.library.utils.UrlParse

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
    var method: Int = 2,
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