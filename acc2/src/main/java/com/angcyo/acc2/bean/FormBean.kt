package com.angcyo.acc2.bean

import com.angcyo.acc2.action.Action
import com.angcyo.acc2.control.AccControl
import com.angcyo.library.utils.UrlParse

/**
 * [ActionBean]执行结束后, 或者[TaskBean]流程结束后, 要提交的表单数据.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

data class FormBean(
    /**请求的服务器地址, 尽量使用完整的地址*/
    var url: String? = null,

    /**固定的查询参数, a=2&b=3.3&c=name&d=true*/
    var query: String? = null,

    /**请求方法, 目前支持[POST] [GET]*/
    var method: Int = 2,

    /**是否同步请求,
     * 如果开启了同步请求, 那么请求异常时, 会中断[TaskBean]的运行.
     * 只有请求成功后, 才会继续运行*/
    var sync: Boolean = false,

    /**是否只在[ActionBean]执行成功之后, 才进行表单请求.
     * [TaskBean]不受影响*/
    var checkSuccess: Boolean = false,

    /**数据提交的类型*/
    var contentType: Int = CONTENT_TYPE_FORM,

    /**等同于[keyList], 分割[com.angcyo.acc2.action.Action.PACKAGE_SPLIT]*/
    var params: String? = null,

    /** 需要从
     * [com.angcyo.acc2.bean.TaskBean.textMap] [com.angcyo.acc2.bean.TaskBean.textListMap]
     * 获取那些数据一并提交
     * */
    var keyList: List<String>? = null,

    /**是否调试, 如果是那么在debug模式下, 不会进行表单请求*/
    var debug: Boolean = false
) {
    companion object {
        //form data 形式
        const val CONTENT_TYPE_FORM = 1

        //json形式
        const val CONTENT_TYPE_JSON = 2

        //表单提交必备的上传字段
        const val KEY_CODE = "resultCode" //200..299 表示本地执行成功
        const val KEY_MSG = "resultMsg"   //成功或者失败时, 提示信息
        const val KEY_DATA = "resultData" //一些信息
    }
}

/**表单参数收集*/
fun FormBean.handleParams(
    control: AccControl,
    taskBean: TaskBean,
    configParams: (formBean: FormBean, params: HashMap<String, Any?>) -> Unit = { _, _ -> }
): HashMap<String, Any?> {
    //从url中, 获取默认参数
    val urlParams = UrlParse.getUrlQueryParams(query)

    //请求参数
    val requestParams = HashMap<String, Any?>().apply {
        putAll(urlParams)

        //add key
        params?.split(Action.PACKAGE_SPLIT)?.forEach { key ->
            if (key.isNotEmpty()) {
                //key
                if (key == Action.LAST_INPUT) {
                    put(key, control.accSchedule.inputTextList.lastOrNull())
                } else {
                    put(key, taskBean.getTextList(key)?.firstOrNull())
                }
            }
        }

        //add key
        keyList?.forEach { key ->
            if (key.isNotEmpty()) {
                put(key, taskBean.getTextList(key)?.firstOrNull())
            }
        }

        configParams(this@handleParams, this)
    }

    return requestParams
}