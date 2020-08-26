package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.back
import com.angcyo.core.component.accessibility.parse.ConstraintBean.Companion.ACTION_GET_TEXT

/**
 * 参数严格的约束
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class ConstraintBean(

    /**约束的文本, 这个文本可以是对应的id, 或者node上的文本内容
     * 文本需要全部命中.
     * 优先匹配[wordTextIndexList]对应的数据.
     * null会匹配除[textList]约束的其他约束规则的node
     * 空字符会匹配所有包含文本的node,
     * 如果所有匹配规则都是null, 则会返回[rootNode]
     * */
    var textList: List<String>? = null,

    /**
     * [textList]优先从[wordList]集合中取值.
     * 支持表达式:
     * $N $0将会替换为[wordList]索引为0的值.最大支持10000
     * 1-4 取索引为[1-4]的值
     * 0--1 取索引为[0-倒数第1个]的值
     * -1 取倒数第1个的值
     * */
    var wordTextIndexList: List<String>? = null,

    /**
     * 上述[textList]字段, 对应的是否是id, 否则就是文本.一一对应的关系.
     * 可以是 完整的id, 也可以是 gj4.
     * 完整的id应该是: com.ss.android.ugc.aweme:id/gj4
     * ids 列表中, 只要满足任意一个约束条件, 即视为发现目标
     * [1] 表示[textList]对应索引位置的文本是id
     * */
    var idList: List<Int>? = null,

    /**类名约束, 和[textList]为一一对应的关系.为空, 表示不约束类名
     * 匹配规则时包含, 只要当前设置的cls包含视图中的cls就算命中.
     * 空字符会命中所有
     * */
    var clsList: List<String>? = null,

    /**此约束需要执行的指令, 不指定坐标. 将随机产生. 小于1的数, 表示比例
     * [click] 触发当前节点的点击事件
     * [click2] 在当前节点区域双击
     * [longClick] 触发当前节点的长按事件
     * [touch:10,10] 在屏幕坐标x=10dp y=10dp的地方点击
     * [double:20,30] 在屏幕坐标x=20dp y=30dp的地方双击
     * [move:10,10-100,100] 从屏幕坐标x=10dp y=10dp的地方移动到100dp 100dp的地方
     * [fling:10,10-100,100]
     * [back] 执行返回操作
     * [getText] 获取文本内容
     * [setText] 设置文本内容
     * [random] 随机执行
     * ...参考下面的静态声明
     *
     * 空字符会进行随机操作.
     * null 默认是click操作
     *
     * 未知指令, 直接完成处理
     * */
    var actionList: List<String>? = null,

    /**
     * [setText]时的输入数据集合, 随机从里面取一个.
     * 如果为null, 则从代码中随机产生
     * */
    var inputList: List<String>? = null,

    /**
     * [setText]时的输入数据在[wordList]集合的索引集合, 随机从里面取一个.
     * 如果为null, 则从[inputList]读取. 如果[inputList]也为null, 则从代码中随机产生
     * 支持表达式:
     * $N $0将会替换为[wordList]索引为0的值.最大支持10000
     * 1-4 取索引为[1-4]的值
     * 0--1 取索引为[0-倒数第1个]的值
     * -1 取倒数第1个的值
     * */
    var wordInputIndexList: List<String>? = null,

    /**忽略此次[Action]操作的返回值, 不忽略的话, 如果action返回true, 则可能会执行[doActionFinish].*/
    var ignore: Boolean = false,

    /**此次[Action] [操作成功]之后, 是否跳过之后的[handle]约束处理.
     * [stateList] 中的[finish]操作, 拥有相同效果
     * */
    var jump: Boolean = false,

    /**和[textList]为一一对应的关系.
     * 坐标矩形约束. 格式10,10-100,100 小于1的数, 表示比例否则就是dp.
     * 空字符只要宽高大于0, 就命中.
     * 只要满足一组矩形约束, 就算命中
     *
     * 如果只设置了1个点坐标, 则目标Rect包含这个点就算命中.
     * 如果设置了2个点坐标(左上,右下), 则目标Rect相交这个矩形就算命中.
     *
     * 注意系统状态栏和导航栏对坐标的影响, 参考的是根节点的宽高
     * */
    var rectList: List<String>? = null,

    /**状态约束,只有全部满足状态才能命中. 和[textList]非一一对应的关系
     * [clickable] 具备可点击
     * [not_clickable] 具备不可点击
     * [focusable] 具备可获取交点
     * [selected] 具备选中状态
     * [unselected] 具备选未中状态
     * [focused] 具备焦点状态
     * [unfocused] 具备无焦点状态
     * [finish] 直接完成操作
     * ...参考下面的静态声明
     * */
    var stateList: List<String>? = null,

    /**和[textList]为一一对应的关系. null和空字符表示匹配自己
     * 约束路径, 通过上述条件找到node之后, 再使用路径查找到真正的目标
     * 格式: +1 -2 >3 <4
     * [+1] 获取自身的下1个兄弟节点
     * [-2] 获取自身的上2个兄弟节点
     * [>3] 获取第3个子节点(非索引值)
     * [<4] 获取第4个parent
     * */
    var pathList: List<String>? = null,

    /**当以上规则匹配到很多节点时, 挑出指定索引的节点执行[actionList]. 不指定默认所有节点
     * index>=0, 正向取索引
     * index<0, 倒数第几个*/
    var handleNodeList: List<Int>? = null,

    /**[ACTION_GET_TEXT]获取文本时, 需要过滤的文本正则, 非一一对应,
     * 如果匹配到, 则中断后续的匹配规则. 如果全部未匹配到, 则直接使用本身 */
    var getTextRegexList: List<String>? = null,

    /**筛选条件, 满足任意一条即可通过
     * 通过上述约束条件, 获取到的节点集合, 再次通过此条件约束,筛选出符合条件的节点
     * 在[after]之前过滤, [after]会使用筛选后的节点集合
     * */
    var conditionList: List<ConditionBean>? = null,

    /**当节点集合不为空, 但是通过条件约束后变为空集合后, 触发的动作集合.
     * [actionList]*/
    var noActionList: List<String>? = null,

    /**上述匹配规则, 匹配之后, 获取到的节点列表当做根节点, 再一次匹配.
     * 只有匹配规则会生效, 非控制匹配规则的属性不会生效
     * */
    var after: ConstraintBean? = null,

    /**id标识, 用于[enable]参数*/
    var constraintId: Long = -1,

    /**是否激活此约束*/
    var enable: Boolean = true
) {
    companion object {

        //可以执行的操作 [action]
        const val ACTION_CLICK = "click" //触发当前节点的点击事件, null 默认是click操作
        const val ACTION_CLICK2 = "click2" //在当前节点区域双击(手势双击) [:0.1,0.1]指定目标区域
        const val ACTION_CLICK3 = "click3" //在当前节点区域点击(手势点击)
        const val ACTION_LONG_CLICK = "longClick" //触发当前节点的长按事件
        const val ACTION_DOUBLE = "double" //[double:20,30] 在屏幕坐标x=20dp y=30dp的地方双击
        const val ACTION_TOUCH = "touch" //[touch:10,10] 在屏幕坐标x=10dp y=10dp的地方点击
        const val ACTION_MOVE = "move" //[move:10,10-100,100] 从屏幕坐标x=10dp y=10dp的地方移动到100dp 100dp的地方
        const val ACTION_FLING = "fling" //[fling:10,10-100,100]
        const val ACTION_BACK = "back" //执行返回操作
        const val ACTION_HOME = "home" //回到桌面
        const val ACTION_GET_TEXT = "getText" //获取文本内容
        const val ACTION_SET_TEXT = "setText" //设置文本内容 [inputList]
        const val ACTION_RANDOM = "random" //随机执行, 空字符会进行随机操作.
        const val ACTION_FINISH = "finish" //直接完成操作
        const val ACTION_ERROR = "error" //直接失败操作, [:xxx]失败信息
        const val ACTION_START = "start" //[start:com.xxx.xxx]启动应用程序 [:main]本机 [:target]目标(空或null)
        const val ACTION_COPY = "copy" //复制文本 [inputList], [:xxx]复制指定的文本'xxx'
        const val ACTION_KEY = "key" //发送按键事件[key:66] KEYCODE_ENTER=66 发送回车按键. (test)
        const val ACTION_FOCUS = "focus" //请求焦点
        const val ACTION_SCROLL_FORWARD = "scrollForward" //向前滚动
        const val ACTION_SCROLL_BACKWARD = "scrollBackward" //向后滚动

        /**
         * 跳转指令, 配合[ignore]一起使用, 防止index不准确
         * [:1:3] 跳转到第1个action,共跳转3次
         * [:-1] 跳转到倒数第1个action,默认10次
         * [:<2] 跳转到前2个action
         * [:>3] 跳转到后3个action
         * [:actionId;actionId;:5] id支持指定多个,依次匹配寻找,找到则终止后续. 跳转到指定的actionId,未找到直接完成当前的action
         * */
        const val ACTION_JUMP = "jump"

        /**
         * 指定下一个[action]执行的时间间隔, 当[checkEvent]通过时, 此指令无效
         * 格式[:5000,500,5]
         * [com/angcyo/core/component/accessibility/parse/ActionBean.kt:61]
         * */
        const val ACTION_SLEEP = "sleep"

        /**
         * 隐藏浮窗, 可以指定需要隐藏多久, 如果不指定隐藏时长, 则下次触发显示时立马会显示
         * [:1] 表示隐藏浮窗到下一个action. 超过actionList数量时, 就会变成了毫秒时间
         * [:5000] 表示隐藏浮窗5秒
         * */
        const val ACTION_HIDE_WINDOW = "hideWindow"

        /**禁止当前的[ConstraintBean]
         * [:3,4,5] 禁用[ConstraintBean]id为3,4,5的对象*/
        const val ACTION_DISABLE = "disable"

        /**[ACTION_DISABLE]*/
        const val ACTION_ENABLE = "enable"

        //需要指定的状态 [state]
        const val STATE_CLICKABLE = "clickable" //具备可点击
        const val STATE_NOT_CLICKABLE = "not_clickable" //具备不可点击
        const val STATE_FOCUSABLE = "focusable" //具备可获取交点
        const val STATE_FOCUSED = "focused" //具备焦点状态
        const val STATE_UNFOCUSED = "unfocused" //具备选未中状态
        const val STATE_SELECTED = "selected" //具备选中状态
        const val STATE_UNSELECTED = "unselected" //具备选未中状态
        const val STATE_SCROLLABLE = "scrollable" //具备可滚动状态
        const val STATE_LONG_CLICKABLE = "longClickable" //具备长按状态
        const val STATE_NOT_LONG_CLICKABLE = "not_longClickable" //具备不可长按状态
    }
}

/**是否是空约束*/
fun ConstraintBean.isConstraintEmpty(): Boolean {
    return textList == null &&
            wordTextIndexList == null &&
            clsList == null &&
            rectList == null &&
            stateList == null &&
            pathList == null
}

/**仅是path约束, 那么直接取RootNode进行判断*/
fun ConstraintBean.isOnlyPathConstraint(): Boolean {
    return textList == null &&
            wordTextIndexList == null &&
            clsList == null &&
            rectList == null &&
            stateList == null &&
            pathList != null
}