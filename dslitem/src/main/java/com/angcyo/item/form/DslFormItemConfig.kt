package com.angcyo.item.form

import com.angcyo.item.style.IEditItem
import com.angcyo.library.L

/**
 * 表单[IFormItem]配置信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class DslFormItemConfig {

    /**数据key*/
    var formKey: String? = null

    /**表单是否必填. 为true, 将会在 label 前面绘制 红色`*` */
    var formRequired: Boolean = false

    /**获取表单的值*/
    var onGetFormValue: (params: DslFormParams) -> Any? = {
        when (val item = it._formAdapterItem) {
            is IEditItem -> item.itemEditText
            else -> null
        }
    }

    /**获取form item对应的表单数据, 附件会自动解析
     * [end] 异步获取数据结束之后的回调*/
    var formObtain: (params: DslFormParams, end: (error: Throwable?) -> Unit) -> Unit =
        { params, end ->
            val key = formKey
            if (key.isNullOrEmpty()) {
                end(null)
            } else {
                val formValue = onGetFormValue(params)
                if (formValue != null) {
                    params.put(key, formValue)
                    end(null)
                } else {
                    L.w("跳过空值formKey[$key]")
                    end(null)
                }
            }
        }

    /**获取form item数据之前, 进行的检查. 返回false, 会终止数据获取, 并且提示错误*/
    var formCheck: (params: DslFormParams, end: (error: Throwable?) -> Unit) -> Unit =
        { params, end ->
            if (formRequired) {
                val value = onGetFormValue(params)
                if (value == null) {
                    end(IllegalArgumentException("无效的值"))
                } else if (value is String && value.isEmpty()) {
                    end(IllegalArgumentException("无效的值"))
                } else if (value is Collection<*> && value.isEmpty()) {
                    end(IllegalArgumentException("无效的值"))
                } else {
                    end(null)
                }
            } else {
                end(null)
            }
        }
}