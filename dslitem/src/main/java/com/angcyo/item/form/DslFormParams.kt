package com.angcyo.item.form

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.http.base.JsonBuilder
import com.angcyo.http.base.jsonBuilder

/**
 * [FormHelper] 用到一些约定参数
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class DslFormParams {

    companion object {
        /**表单数据获取来源: http请求*/
        const val FORM_SOURCE_HTTP = 1

        /**表单数据获取来源: 本地存储*/
        const val FORM_SOURCE_SAVE = 2

        fun from(jsonBuilder: JsonBuilder, useFilterList: Boolean = true) = DslFormParams().apply {
            this.useFilterList = useFilterList
            this.jsonBuilder = jsonBuilder
        }

        fun fromSave() = DslFormParams().apply {
            formSource = FORM_SOURCE_SAVE
        }
    }

    /**获取表单数据时, 是否使用过滤后的数据源. [DslAdapter]*/
    var useFilterList: Boolean = true

    /**表单json数据构建, 可以通过修改此属性, 获取分组多个表单数据*/
    var jsonBuilder: JsonBuilder = jsonBuilder()

    /**是否需要跳过[FormHelper]的[formCheck]检查*/
    var skipFormCheck: Boolean = false

    /**检查[formCheck]指定[DslAdapterItem]执行之前回调
     * 返回[true], 跳过当前[item]的检查
     * */
    var formCheckBeforeAction: (DslAdapterItem) -> Boolean = { false }

    /**获取[formObtain]指定[DslAdapterItem]执行之前回调
     * 返回[true], 跳过当前[item]的数据获取
     * */
    var formObtainBeforeAction: (DslAdapterItem) -> Boolean = { false }

    /**获取[formObtain]指定[DslAdapterItem]执行之后回调.
     * 如果[formObtainBeforeAction]跳过了, 那么此回调也不会执行
     * */
    var formObtainAfterAction: (DslAdapterItem) -> Unit = { }

    /**表单数据来源用途. 请在[IFormItem]判断*/
    var formSource: Int = FORM_SOURCE_HTTP
        set(value) {
            field = value
            if (value == FORM_SOURCE_SAVE) {
                //数据保存, 应该要获取所有表单item
                useFilterList = false
            }
        }
}

/**请求保存表单数据*/
fun DslFormParams.isSave() = formSource == DslFormParams.FORM_SOURCE_SAVE