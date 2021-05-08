package com.angcyo.acc2.bean

/**
 * 操作记录类. 保存了一些键值对数据.
 * 通过获取指定的键对应的值, 存储/展示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/04
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class OperateBean(

    //<editor-fold desc="标识">

    /**标识操作开始的地方*/
    var start: Boolean = false,

    /**标识操作结束的地方*/
    var end: Boolean = false,

    //</editor-fold desc="标识">

    //<editor-fold desc="数据配置">

    /**配置一些需要记录的文本
     * [key:value key:value]的形式 多个用空格隔开
     * [value]支持文本表达式
     * [com.angcyo.acc2.bean.FindBean.textList]
     * */
    var text: String? = null,

    /**类似[text], 但是不用空格隔开了*/
    var textList: List<String>? = null,

    //</editor-fold desc="数据配置">

    //<editor-fold desc="表单请求">

    /**表单请求
     * [com.angcyo.acc2.bean.OperateBean.form]
     * [com.angcyo.acc2.bean.ActionBean.form]
     * [com.angcyo.acc2.bean.TaskBean.form]
     * */
    var form: FormBean? = null,

    //</editor-fold desc="表单请求">

    //<editor-fold desc="数据存储">

    /**最终的数据存储*/
    var map: Map<String, List<String?>?>? = null

    //</editor-fold desc="数据存储">
)

fun OperateBean.getTextList(key: String): List<String?>? = map?.get(key)

fun OperateBean.getText(key: String): String? = getTextList(key)?.firstOrNull()
