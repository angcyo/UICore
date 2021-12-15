package com.angcyo.item.form

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.http.base.*
import com.angcyo.library.L
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

/**获取json 对象*/
fun DslFormParams.jsonElement() = if (dataMap.isEmpty()) {
    jsonBuilder.build()
} else {
    dataMap.toJson()?.toJsonElement()
}

/**将数据放置在[dataMap]中*/
fun DslFormParams.put(key: String, value: Any?) {
    dataMap.putDepth(key, value)
}

fun DslFormParams.get(key: String): Any? {
    return dataMap.getDepth(key)
}

/**
 * [key] 支持[xxx.xxx]的对象格式, 支持[xxx[].xxx]的数组对象格式, 支持[xxx[]]的数组格式
 * 暂不支持 [[].[].[]]的格式
 *
 * 数组操作:
 * paramExt.userFileList[].commonFileId :在数组userFileList中加入新对象, 值赋给属性commonFileId
 * paramExt.userFileList[-1].fileType :在数组userFileList最后一个对象, 值赋给fileType
 * */
fun HashMap<String, Any?>.putDepth(key: String, value: Any?) {
    val arrayFlag = "\\[-?\\d*\\]"
    val arrayFlagRegex = arrayFlag.toRegex()
    val keyList = key.split(".")

    //操作的数据
    var opData: Any = this
    var isArrayKey = false
    var arrayIndex: Int? = null //数组的索引

    val lastIndex = keyList.lastIndex
    keyList.forEachIndexed { index, subKey ->

        //剔除[-1] , 剩下干净的key
        val k = if (subKey.contains(arrayFlagRegex)) {
            isArrayKey = true
            val indexStr = subKey.patternList(arrayFlag).first()
            arrayIndex = indexStr.getLongNum()?.toInt()
            //数组
            subKey.substring(0, subKey.length - indexStr.length)
        } else {
            isArrayKey = false
            subKey
        }

        //处理
        if (index == lastIndex) {
            //最后一级, 肯定是需要赋值的
            if (isArrayKey) {
                //最后的key是数组
                if (opData is MutableList<*>) {
                    //最后一组是数组, 上一组又是数据, 二维数组
                    (opData as MutableList<Any?>).apply {
                        if (arrayIndex ?: -1 < 0) {
                            add(listOf(value))
                        } else {
                            set(arrayIndex!!, listOf(value)) //注意越界异常
                        }
                    }
                } else if (opData is HashMap<*, *>) {
                    //操作的是对象数组成员属性
                    val attr = (opData as HashMap<String, Any?>)[k]
                    val list = (attr ?: mutableListOf<Any?>()) as MutableList<Any?>

                    if (arrayIndex ?: -1 < 0) {
                        list.add(value)
                    } else {
                        list[arrayIndex!!] = value //注意越界异常
                    }

                    (opData as HashMap<String, Any?>)[k] = list
                } else {
                    L.w("无法处理: key:${key} value:${value}")
                }
            } else {
                //最后的key是属性
                if (opData is MutableList<*>) {
                    //最后一组是属性, 上一组数组, 则追加对象
                    (opData as MutableList<Any?>).apply {
                        if (arrayIndex ?: -1 < 0) {
                            add(hashMapOf(k to value))
                        } else {
                            set(arrayIndex!!, hashMapOf(k to value)) //注意越界异常
                        }
                    }
                } else if (opData is HashMap<*, *>) {
                    //操作的是对象, 则直接修改对应的属性
                    (opData as HashMap<String, Any?>)[k] = value
                } else {
                    L.w("无法处理: key:${key} value:${value}")
                }
            }
        } else {
            //非最后一组, 确定操作对象
            if (isArrayKey) {
                val newArray = mutableListOf<Any>()
                if (opData is MutableList<*>) {
                    //最后一组是数组, 上一组又是数据, 二维数组
                    (opData as MutableList<Any?>).add(hashMapOf(k to newArray))
                    opData = newArray
                } else if (opData is HashMap<*, *>) {
                    val attr = (opData as HashMap<String, Any?>)[k]
                    val list = (attr ?: newArray) as MutableList<Any?>
                    (opData as HashMap<String, Any?>)[k] = list
                    opData = list
                } else {
                    L.w("无法识别: key:${key} value:${value}")
                }
            } else {
                val newObj = hashMapOf<String, Any?>()
                if (opData is MutableList<*>) {
                    //最后一组是数组, 上一组又是数据, 二维数组
                    (opData as MutableList<Any?>).add(newObj)
                    opData = newObj
                } else if (opData is HashMap<*, *>) {
                    val attr = (opData as HashMap<String, Any?>)[k]
                    val obj = (attr ?: newObj) as HashMap<String, Any?>
                    (opData as HashMap<String, Any?>)[k] = obj
                    opData = obj
                } else {
                    L.w("无法识别: key:${key} value:${value}")
                }
            }
        }

        //处理 2
        /*if (isArrayKey) {
            if (index == keyList.lastIndex) {
                if (opData is MutableList<*>) {
                    (opData as MutableList<Any?>).apply {
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
                if (opData is HashMap<*, *>) {
                    //转移操作对象
                    val rawList = (opData as HashMap<String, Any?>)[k]
                    val list = (rawList ?: mutableListOf<Any?>()) as MutableList<Any?>

                    if (arrayIndex == null) {
                        list.add(linkedMapOf<String, Any?>().apply {
                            //转移操作对象
                            (opData as HashMap<String, Any?>)[k] = list
                            opData = this
                        })
                    } else {
                        opData = list.getOrNull2(arrayIndex!!)!! //越界异常
                    }
                } else {
                    throw IllegalStateException("key[$key]类型不匹配")
                }
            }
        } else {
            if (index == keyList.lastIndex) {
                if (opData is HashMap<*, *>) {
                    (opData as HashMap<String, Any?>)[k] = value
                } else if (opData is MutableList<*>) {
                    (opData as MutableList<Any?>).add(value)
                }
            } else {
                if (opData is HashMap<*, *>) {
                    //转移操作对象
                    ((opData as HashMap<String, Any?>)[k]
                        ?: linkedMapOf<String, Any?>()).apply {
                        (opData as HashMap<String, Any?>)[k] = this
                        opData = this
                    }
                } else {
                    throw IllegalStateException("key[$key]类型不匹配")
                }
            }
        }*/
    }
}

/**获取深度key, 对应的value*/
fun HashMap<String, Any?>.getDepth(key: String): Any? {
    val arrayFlag = "\\[-?\\d*\\]"
    val arrayFlagRegex = arrayFlag.toRegex()
    val keyList = key.split(".")

    //操作的数据
    var opData: Any = this
    var isArrayKey = false
    var arrayIndex: Int? = null //数组的索引

    var result: Any? = null

    keyList.forEachIndexed { index, subKey ->

        //剔除[-1] , 剩下干净的key
        val k = if (subKey.contains(arrayFlagRegex)) {
            val indexStr = subKey.patternList(arrayFlag).first()
            arrayIndex = indexStr.getLongNum()?.toInt()
            //数组
            subKey.substring(0, subKey.length - indexStr.length).apply {
                isArrayKey = true
            }
        } else {
            subKey.apply {
                isArrayKey = false
            }
        }

        //处理
        if (isArrayKey) {
            if (opData is HashMap<*, *>) {
                result = (opData as HashMap<*, *>)[k]

                if (arrayIndex != null) {
                    result = (result as? List<*>)?.getOrNull2(arrayIndex!!)
                }

                if (result == null) {
                    return null
                } else {
                    opData = result!!
                }
            } else if (opData is List<*>) {
                result = (opData as? List<*>)

                if (arrayIndex != null) {
                    result = (opData as? List<*>)?.getOrNull2(arrayIndex!!)
                }

                if (result == null) {
                    return null
                } else {
                    opData = result!!
                }
            } else {
                L.w("异常类型获取值:$k 在:$opData")
                return null
            }
        } else {
            if (opData is HashMap<*, *>) {
                result = (opData as HashMap<*, *>)[k]
                if (result == null) {
                    return null
                } else {
                    opData = result!!
                }
            } else {
                L.w("异常获取值:$k 在:$opData")
                return null
            }
        }
    }

    return result
}
