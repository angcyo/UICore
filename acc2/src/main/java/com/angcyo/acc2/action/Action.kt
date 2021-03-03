package com.angcyo.acc2.action

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/31
 */
object Action {

    /**参数分隔符*/
    const val ARG_SPLIT = ":"
    const val ARG_SPLIT2 = " "

    /**多包名分隔符*/
    const val PACKAGE_SPLIT = ";"

    /**主程序*/
    const val PACKAGE_MAIN = "main"

    /**任务目标程序*/
    const val PACKAGE_TARGET = "target"

    /**当前活跃的窗口*/
    const val PACKAGE_ACTIVE = "active"

    /**坐标分隔符*/
    const val POINT_SPLIT = "~"

    /**多文本分隔符[com.angcyo.acc2.bean.TaskBean.textMap]*/
    const val TEXT_SPLIT = "|"

    const val OP = "><=%≈"

    /**依赖的Action标识*/
    const val RELY = "rely"

    /**随便标识*/
    const val RANDOM = "random"

    /**使用返回的节点信息
     * [com.angcyo.acc2.bean.HandleBean.rootNode]*/
    const val RESULT = "result"

    /**所有标识*/
    const val ALL = "*"

    /**取父节点*/
    const val PARENT = "parent"

    /**不取父节点*/
    const val NOT_PARENT = "not_parent"

    /**触发当前节点的点击事件, null 默认是click操作.
     * [:STATE_UNSELECTED] 支持状态参数. 表示, 只在节点满足状态时, 才点击
     * [:STATE_UNSELECTED:5] 自身要满足且5个parent内有节点满足状态, 才触发点击
     * 如果包含[*], 表示所有节点都必须满足状态条件, 否则只要有一个满足状态条件即可
     * [click:0.1,0.1] 效果等同于[ACTION_CLICK3]
     * 支持[NOT_PARENT]
     * */
    const val ACTION_CLICK = "click"

    const val ACTION_CLICK2 = "click2" //在当前节点区域双击(手势双击) [:0.1,0.1]指定目标区域
    const val ACTION_CLICK3 = "click3" //在当前节点区域点击(手势点击) [:0.1,0.1]指定目标区域

    /**
     * 支持[NOT_PARENT]*/
    const val ACTION_LONG_CLICK = "longClick" //触发当前节点的长按事件
    const val ACTION_DOUBLE = "double" //[double:20,30] 在屏幕坐标x=20dp y=30dp的地方双击
    const val ACTION_TOUCH = "touch" //[touch:10,10] 在屏幕坐标x=10dp y=10dp的地方点击
    const val ACTION_MOVE = "move" //[move:10,10~100,100] 从屏幕坐标x=10dp y=10dp的地方移动到100dp 100dp的地方
    const val ACTION_FLING = "fling" //[fling:10,10~100,100]

    /**获取元素文本保存到
     * [com.angcyo.acc2.bean.TaskBean.textMap]
     * [getText:key regex:\\d+]
     * 默认key:getText
     * regex:文本需要通过正则过滤的表达式
     */
    const val ACTION_GET_TEXT = "getText"

    /**设置输入框文本内容
     * [input:$0] 支持文本变量
     * [input:$[xxx] regex:xxx]
     * regex:\\d+ 文本需要使用正则过滤取值
     * */
    const val ACTION_INPUT = "input"

    /**[ACTION_INPUT]别名*/
    const val ACTION_SET_TEXT = "setText"

    /**清理[com.angcyo.acc2.bean.TaskBean.textMap]数据
     * [clearText:*] 清理所有
     * [clearText:k1 k2 k3] 清理指定key
     * */
    const val ACTION_CLEAR_TEXT = "clearText"

    /**清除指定id的[ActionBean]的运行次数
     * [clearRunCount] 清理当前的[ActionBean]
     * [clearRunCount:xxx xxx]
     * [ACTION_JUMP]
     * */
    const val ACTION_CLEAR_RUN_COUNT = "clearRunCount"

    /**清除[ActionBean]的运行时长
     * [ACTION_CLEAR_RUN_COUNT]*/
    const val ACTION_CLEAR_RUN_TIME = "clearRunTime"

    /**清除指定id的[ActionBean]的跳转次数
     * [clearJumpCount:xxx xxx]
     * [ACTION_JUMP]
     * */
    const val ACTION_CLEAR_JUMP_COUNT = "clearJumpCount"

    /**获取元素文本追加保存到
     * [com.angcyo.acc2.bean.TaskBean.textListMap]
     * [appendText:key regex:xxx]
     * 默认key:appendText
     * regex:\\d+ 文本需要使用正则过滤取值
     * [ACTION_INPUT]
     */
    const val ACTION_APPEND_TEXT = "appendText"

    /**获取元素文本保存到
     * [com.angcyo.acc2.bean.TaskBean.textMap]
     * [appendText:key regex:xxx]
     * 默认key:appendText
     * regex:\\d+ 文本需要使用正则过滤取值
     * [ACTION_INPUT]
     */
    const val ACTION_PUT_TEXT = "putText"

    /**回退按钮
     * [back:>1000]连续back操作, 满足条件才触发.否则失败*/
    const val ACTION_BACK = "back"

    /**回到桌面*/
    const val ACTION_HOME = "home"

    /**跳转到指定的actionId执行
     * [jump:100] 下一个执行actionId为100的action
     * [jump:100 120 130] 找到第一个存在的action进行跳转
     * [jump] 跳过当前, 指定下一个
     * [jump:rely] 从[com.angcyo.acc2.bean.ActionBean.relyList]列表中获取一个可以跳转的action
     * */
    const val ACTION_JUMP = "jump"

    /**激活指定actionId的[ActionBean]
     * [enable 100 120 200 220]*/
    const val ACTION_ENABLE = "enable"

    /**禁用, 参考[ACTION_ENABLE]*/
    const val ACTION_DISABLE = "disable"

    /**禁用当前的[HandleBean]*/
    const val ACTION_DISABLE_HANDLE = "disableHandle"

    /**异常, 并中断整个任务
     * [error:reason]*/
    const val ACTION_ERROR = "error"

    /**[sleep:1000]线程休眠1000毫秒*/
    const val ACTION_SLEEP = "sleep"

    /**直接完成任务
     * [finish:reason]*/
    const val ACTION_FINISH = "finish"

    /**停止控制器, 释放资源
     * [stop:reason]*/
    const val ACTION_STOP = "stop"

    /**暂停控制器*/
    const val ACTION_PAUSE = "pause"

    /**恢复控制器
     * [resume restart]*/
    const val ACTION_RESUME = "resume"

    /**启动指定的应用程序
     * [start:com.xxx.xxx]启动应用程序 [:main]本机APP [:target]目标(空或null)
     * */
    const val ACTION_START = "start"

    /**复制文本, 支持文本表达式*/
    const val ACTION_COPY = "copy"

    /**发送按键事件[key:66] KEYCODE_ENTER=66 发送回车按键. (test)*/
    const val ACTION_KEY = "key"

    /**请求焦点*/
    const val ACTION_FOCUS = "focus"

    /**向前滚动, 手指向上滑动
     * [scrollForward:parent]
     * parent是否要使用具有同状态的父节点操作
     * 支持[PARENT]
     * */
    const val ACTION_SCROLL_FORWARD = "scrollForward"

    /**向后滚动, 手指向上滑动
     * [scrollBackward:parent]
     * parent是否要使用具有同状态的父节点操作
     * 支持[PARENT]
     * */
    const val ACTION_SCROLL_BACKWARD = "scrollBackward"

    /**执行结果直接设置true 加任意参数取消强制成功
     * [com.angcyo.acc2.action.TrueAction]*/
    const val ACTION_TRUE = "true"

    /**执行结果直接设置false 加任意参数取消强制失败
     * [com.angcyo.acc2.action.FalseAction]*/
    const val ACTION_FALSE = "false"

    /**弹出toast默认是[ACTION_TOAST_QQ]
     * [toast:xxx]
     * [toastQQ:xxx]
     * [toastWX:xxx]*/
    const val ACTION_TOAST = "toast"
    const val ACTION_TOAST_QQ = "toastQQ"
    const val ACTION_TOAST_WX = "toastWX"

    /**设置浮窗全屏or小屏
     * true: 全屏
     * false: 小屏
     * 空字符: 使用[TaskBean.fullscreen]
     * */
    const val ACTION_FULLSCREEN = "fullscreen"

    /**设置全屏浮窗不接受touch事件*/
    const val ACTION_NOT_TOUCHABLE = "notTouchable"

    /**
     * 隐藏浮窗, 可以指定需要隐藏多久, 如果不指定隐藏时长, 则下次触发显示时立马会显示
     * [:1] 表示隐藏浮窗到下一个action. 超过actionList数量时, 就会变成了毫秒时间
     * [:5000] 表示隐藏浮窗5秒
     * */
    const val ACTION_HIDE_WINDOW = "hideWindow"

    /*---------------------------------------状态匹配--------------------------------------*/

    //需要指定的状态 [state]
    const val STATE_CLICKABLE = "clickable" //具备可点击
    const val STATE_UN_CLICKABLE = "unClickable" //具备不可点击
    const val STATE_FOCUSABLE = "focusable" //具备可获取交点
    const val STATE_UN_FOCUSABLE = "unFocusable" //具备不可获取交点
    const val STATE_FOCUSED = "focused" //具备焦点状态
    const val STATE_UNFOCUSED = "unfocused" //具备无焦点状态
    const val STATE_SELECTED = "selected" //具备选中状态
    const val STATE_UNSELECTED = "unselected" //具备未选中状态
    const val STATE_SCROLLABLE = "scrollable" //具备可滚动状态
    const val STATE_UN_SCROLLABLE = "unScrollable" //具备不可滚动状态
    const val STATE_LONG_CLICKABLE = "longClickable" //具备长按状态
    const val STATE_UN_LONG_CLICKABLE = "unLongClickable" //具备不可长按状态
    const val STATE_ENABLE = "enable" //具备激活状态
    const val STATE_DISABLE = "disable" //具备非激活状态
    const val STATE_PASSWORD = "password" //具备密码状态
    const val STATE_UN_PASSWORD = "unPassword" //具备非密码状态
    const val STATE_CHECKABLE = "checkable" //具备可check状态
    const val STATE_UN_CHECKABLE = "unCheckable" //具备不可check状态
    const val STATE_CHECKED = "checked" //具备check状态
    const val STATE_UNCHECKED = "unchecked" //不具备check状态
}