package com.angcyo.item.form

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.http.base.JsonBuilder
import com.angcyo.http.base.jsonBuilder
import com.angcyo.http.base.toJson

/**
 * [FormHelper] 用到一些约定参数
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/16
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class DslFormParams {

    companion object {
        /**表单数据获取来源: http请求
         * 仅获取可见item的数据*/
        const val FORM_SOURCE_HTTP = 1

        /**表单数据获取来源: 本地存储
         * 会获取所有item的数据*/
        const val FORM_SOURCE_SAVE = 2

        fun from(jsonBuilder: JsonBuilder, useFilterList: Boolean = true) = DslFormParams().apply {
            this.jsonBuilder = jsonBuilder
            formSource = if (useFilterList) {
                FORM_SOURCE_HTTP
            } else {
                FORM_SOURCE_SAVE
            }
        }

        fun fromSave() = DslFormParams().apply {
            formSource = FORM_SOURCE_SAVE
        }

        fun fromHttp() = DslFormParams().apply {
            formSource = FORM_SOURCE_HTTP
        }
    }

    /**获取表单数据时, 是否使用过滤后的数据源. [DslAdapter]*/
    var useFilterList: Boolean = true

    /**表单json数据构建, 可以通过修改此属性, 获取分组多个表单数据*/
    var jsonBuilder: JsonBuilder = jsonBuilder()

    /**表单数据存储*/
    var dataMap: LinkedHashMap<String, Any?> = linkedMapOf()

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
            //数据保存, 应该要获取所有表单item
            useFilterList = value != FORM_SOURCE_SAVE
        }

    /**当前正在检查/正在获取数据的[DslAdapterItem]*/
    var _formAdapterItem: DslAdapterItem? = null
}

/**请求保存表单数据*/
fun DslFormParams.isSave() = formSource == DslFormParams.FORM_SOURCE_SAVE

fun DslFormParams.isHttp() = formSource == DslFormParams.FORM_SOURCE_HTTP

/**获取json数据*/
fun DslFormParams.json() = if (dataMap.isEmpty()) {
    jsonBuilder.get()
} else {
    dataMap.toJson { } ?: "{}"
}


/**将数据放置在[dataMap]中
 * [key] 支持[xxx.xxx]的格式
 * */
fun DslFormParams.put(key: String, value: Any?) {
    val keyList = key.split(".")

    var map = dataMap
    keyList.forEachIndexed { index, k ->
        if (index == keyList.lastIndex) {
            map[k] = value
        } else {
            val v = map[k]
            val vMap: LinkedHashMap<String, Any?>? = when (v) {
                null -> linkedMapOf()
                is LinkedHashMap<*, *> -> v as LinkedHashMap<String, Any?>?
                else -> throw IllegalArgumentException("key[${key}]的值类型不匹配")
            }
            if (vMap != null) {
                map[k] = vMap
                map = vMap
            }
        }
    }
}
