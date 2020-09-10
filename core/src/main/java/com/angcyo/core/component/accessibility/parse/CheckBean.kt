package com.angcyo.core.component.accessibility.parse

/**
 * 界面元素识别, 识别处理, 异常处理, 未知处理. 等约束关键
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/18
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class CheckBean(

    /**check的id, 不参与识别逻辑*/
    var checkId: Long = 1,

    /**check的描述, 不参与识别逻辑*/
    var checkDes: String? = null,

    /**对应的程序包名.`;`分割多个包名
     * 强制指定[CheckBean]需要处理的应用包名,
     * 如果为空字符,表示需要处理任意应用程序, 这样可以突破[AutoParseInterceptor]拦截器限制的应用包名
     * 如果为null,表示跟随[AutoParseInterceptor]拦截器限制的应用包名
     * */
    var packageName: String? = null,

    /**对应的程序版本名, 空字符表示支持全线版本*/
    var versionName: String? = null,

    /**界面识别所需要解析数据bean
     * 是否需要当前事件[checkEvent]解析时的关键数据
     * */
    var event: List<ConstraintBean>? = null,

    /**界面未识别时需要解析数据bean
     * [checkEvent] 无法识别界面时, 触发的[checkOtherEvent]回调
     * */
    var other: List<ConstraintBean>? = null,

    /**识别到界面到, 触发事件的解析数据结构
     * 目中目标界面后[doAction]解析时的关键数据*/
    var handle: List<ConstraintBean>? = null,

    /**当这个界面需要被返回时, 点击关闭的数据解析结构.
     * 如果这个值不为空, 那么次Action会被添加到拦截器的[com.angcyo.core.component.accessibility.BaseAccessibilityInterceptor.getActionOtherList]
     * 当[Action]被作为回退处理时[doActionWidth]解析时的关键数据*/
    var back: List<ConstraintBean>? = null,

    /**回滚时, 需要触发的指令*/
    var rollback: List<ConstraintBean>? = null,

    /**[event]无法识别, 并且[checkOtherCount]超出了阈值时, 需要触发的指令*/
    var otherOut: List<ConstraintBean>? = null,

    /**[doAction]超限时, 需要触发的指令*/
    var doAction: List<ConstraintBean>? = null,

    /**当拦截器离开主程序界面多少次后, 触发的指令*/
    var leave: List<ConstraintBean>? = null,

    /**跳转指令超出了阈值时, 需要触发的指令
     * [com.angcyo.core.component.accessibility.parse.ConstraintBean.ACTION_JUMP]*/
    var jumpOut: List<ConstraintBean>? = null
)