package com.angcyo.item.form

import com.angcyo.dsladapter.item.IDslItem
import com.angcyo.dsladapter.item.IDslItemConfig
import com.angcyo.item.style.IEditItem
import com.angcyo.library.L

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IFormItem : IDslItem {

    companion object {
        var DEFAULT_SELECTOR_HINT = "请选择"
        var DEFAULT_INPUT_HINT = "请输入"
    }

    /**表单相关信息, 在此对象中配置*/
    var itemFormConfig: FormItemConfig
}

var IFormItem.formItemConfig: FormItemConfig
    get() = itemFormConfig
    set(value) {
        itemFormConfig = value
    }

var IFormItem.formRequired: Boolean
    get() = itemFormConfig.formRequired
    set(value) {
        itemFormConfig.formRequired = value
    }

var IFormItem.formCanEdit: Boolean
    get() = itemFormConfig.formCanEdit
    set(value) {
        itemFormConfig.formCanEdit = value
        if (this is IEditItem) {
            editItemConfig.itemNoEditModel = !value
        }
    }

/**全忽略*/
var IFormItem.formIgnore: Boolean
    get() = itemFormConfig.formIgnore
    set(value) {
        itemFormConfig.formIgnore = value
    }

/**忽略check*/
var IFormItem.formIgnoreCheck: Boolean
    get() = itemFormConfig.formIgnoreCheck
    set(value) {
        itemFormConfig.formIgnoreCheck = value
    }

/**忽略数据获取*/
var IFormItem.formIgnoreObtain: Boolean
    get() = itemFormConfig.formIgnoreObtain
    set(value) {
        itemFormConfig.formIgnoreObtain = value
    }

var IFormItem.formErrorTip: String
    get() = itemFormConfig.formErrorTip
    set(value) {
        itemFormConfig.formErrorTip = value
    }

var IFormItem.formKey: String?
    get() = itemFormConfig.formKey
    set(value) {
        itemFormConfig.formKey = value
    }

/**需要调用[end]结束操作*/
var IFormItem.formObtain: (params: DslFormParams, end: (error: Throwable?) -> Unit) -> Unit
    get() = itemFormConfig.formObtain
    set(value) {
        itemFormConfig.formObtain = value
    }

/**
 * 表单[IFormItem]配置信息
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FormItemConfig : IDslItemConfig {

    /**数据key*/
    var formKey: String? = null

    /**忽略此表单, 只做展示使用*/
    var formIgnore: Boolean = false
        set(value) {
            field = value
            if (value) {
                formRequired = false
            }
            formIgnoreCheck = value
            formIgnoreObtain = value
        }

    /**单独忽略check*/
    var formIgnoreCheck: Boolean = false

    /**单独忽略obtain*/
    var formIgnoreObtain: Boolean = false

    /**表单是否必填. 为true, 将会在 label 前面绘制 红色`*` */
    var formRequired: Boolean = false
        set(value) {
            field = value
            if (value) {
                formIgnore = false
            }
        }

    /**异常时的错误提示*/
    var formErrorTip: String = "无效的值"

    /**表单是否可编辑*/
    var formCanEdit: Boolean = true

    /**获取表单的值*/
    var onGetFormValue: (params: DslFormParams) -> Any? = {
        when (val item = it._formAdapterItem) {
            is IEditItem -> item.editItemConfig.itemEditText
            else -> null
        }
    }

    /**获取form item对应的表单数据, 附件会自动解析
     * [end] 异步获取数据结束之后的回调*/
    var formObtain: (params: DslFormParams, end: (error: Throwable?) -> Unit) -> Unit =
        { params, end ->
            if (formIgnoreObtain) {
                end(null)
            } else {
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
        }

    /**获取form item数据之前, 进行的检查. 返回false, 会终止数据获取, 并且提示错误*/
    var formCheck: (params: DslFormParams, end: (error: Throwable?) -> Unit) -> Unit =
        { params, end ->
            if (formIgnoreCheck) {
                end(null)
            } else if (formRequired) {
                val value = onGetFormValue(params)
                if (value == null) {
                    end(IllegalArgumentException(formErrorTip))
                } else if (value is String && value.isEmpty()) {
                    end(IllegalArgumentException(formErrorTip))
                } else if (value is Collection<*> && value.isEmpty()) {
                    end(IllegalArgumentException(formErrorTip))
                } else {
                    end(null)
                }
            } else {
                end(null)
            }
        }
}