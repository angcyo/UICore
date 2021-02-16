package com.angcyo.acc2.bean

/**
 * 条件约束, 定义一些需要满足的条件. 所有声明的条件, 都必须满足.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/02
 */
data class ConditionBean(

    /**随机是否激活*/
    var random: Boolean = false,

    /**当[com.angcyo.acc2.bean.TaskBean.textMap]包含指定key对应的值时. 列表中均要满足
     * 则满足条件!*/
    var textMapList: List<String>? = null,

    /**当指定[com.angcyo.acc2.bean.ActionBean.actionId]的[ActionBean]执行成功时. 列表中均要满足
     * 则满足条件*/
    var actionResultList: List<Long>? = null,

    /**当指定[com.angcyo.acc2.bean.ActionBean.actionId]的[ActionBean]执行次数满足条件时.
     * 则满足条件. 列表中均要满足
     * [100:>=9] [120:<2] [130:<2] 如果对应的id不存在, 则忽略
     * [.:>=3] 当前的action运行次数
     * [.:%3>=2] 当前的action运行次数模3>=2
     * */
    var actionRunList: List<String>? = null,

    /**当指定的[ActionBean]满足约束的运行时长时
     * [.:>=3000] 当前的action运行时长超过3秒时满足条件
     * [actionRunList]*/
    var actionTimeList: List<String>? = null,

    /**跳转次数*/
    var actionJumpList: List<String>? = null,

    /**如果指定[com.angcyo.acc2.bean.ActionBean.actionId]的[ActionBean]激活之后, 则满足条件
     * 列表中均要满足
     * [100] [120] [130] 如果对应的id不存在, 则忽略
     * */
    var actionEnableList: List<Long>? = null,

    /**系统信息约束. 多个用;号分割,并且的关系; 用 !or! 分割或者的关系
     * [code>=1000]
     * [w>10;h<=10!or!code>=1000]
     * */
    var system: String? = null,

    /**对应目标应用程序的信息约束
     * [system]*/
    var app: String? = null,
)