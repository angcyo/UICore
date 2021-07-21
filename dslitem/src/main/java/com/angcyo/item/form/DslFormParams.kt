package com.angcyo.item.form

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.http.base.JsonBuilder
import com.angcyo.http.base.jsonBuilder
import com.angcyo.http.base.toJson
import com.angcyo.library.ex.getOrNull2
import com.angcyo.library.ex.patternList
import com.angcyo.library.utils.getLongNum

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
    var dataMap: HashMap<String, Any?> = linkedMapOf()

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

/**将数据放置在[dataMap]中*/
fun DslFormParams.put(key: String, value: Any?) {
    dataMap.putDepth(key, value)
}

/**
 * [key] 支持[xxx.xxx]的对象格式, 支持[xxx[].xxx]的数组对象格式, 支持[xxx[]]的数组格式
 * 暂不支持 [[].[].[]]的格式
 * */
fun HashMap<String, Any?>.putDepth(key: String, value: Any?) {
    val arrayFlag = "\\[-?\\d*\\]"
    val arrayFlagRegex = arrayFlag.toRegex()
    val keyList = key.split(".")

    //操作的数据
    var opDataMap: Any = this
    var isArray = false
    var arrayIndex: Int? = null //数组的索引
    keyList.forEachIndexed { index, subKey ->

        val k = if (subKey.contains(arrayFlagRegex)) {
            val indexStr = subKey.patternList(arrayFlag).first()
            arrayIndex = indexStr.getLongNum()?.toInt()
            //数组
            subKey.substring(0, subKey.length - indexStr.length).apply {
                isArray = true
            }
        } else {
            subKey.apply {
                isArray = false
            }
        }

        if (isArray) {
            if (index == keyList.lastIndex) {
                if (opDataMap is MutableList<*>) {
                    (opDataMap as MutableList<Any?>).apply {
                        if (arrayIndex ?: -1 < 0) {
                            add(value)
                        } else {
                            set(arrayIndex!!, value) //越界异常
                        }
                    }
                } else {
                    throw IllegalStateException("key[$key]类型不匹配")
                }
            } else {
                if (opDataMap is HashMap<*, *>) {
                    //转移操作对象
                    val rawList = (opDataMap as HashMap<String, Any?>)[k]
                    val list = (rawList ?: mutableListOf<Any?>()) as MutableList<Any?>

                    if (arrayIndex == null) {
                        list.add(linkedMapOf<String, Any?>().apply {
                            //转移操作对象
                            (opDataMap as HashMap<String, Any?>)[k] = list
                            opDataMap = this
                        })
                    } else {
                        opDataMap = list.getOrNull2(arrayIndex!!)!! //越界异常
                    }
                } else {
                    throw IllegalStateException("key[$key]类型不匹配")
                }
            }
        } else {
            if (index == keyList.lastIndex) {
                if (opDataMap is HashMap<*, *>) {
                    (opDataMap as HashMap<String, Any?>)[k] = value
                } else if (opDataMap is MutableList<*>) {
                    (opDataMap as MutableList<Any?>).add(value)
                }
            } else {
                if (opDataMap is HashMap<*, *>) {
                    //转移操作对象
                    ((opDataMap as HashMap<String, Any?>)[k]
                        ?: linkedMapOf<String, Any?>()).apply {
                        (opDataMap as HashMap<String, Any?>)[k] = this
                        opDataMap = this
                    }
                } else {
                    throw IllegalStateException("key[$key]类型不匹配")
                }
            }
        }
    }
}
