package com.angcyo.acc2.action

import java.net.URLDecoder

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

    /**文本解析时, 默认的值对应的key*/
    const val DEF = "def"

    /**特殊文本, 对应目标程序的名字
     * [com.angcyo.acc2.bean.FindBean.textList]*/
    const val APP_NAME = "appName"

    /**特殊文本, 当前时间精确到毫秒*/
    const val NOW_TIME = "nowTime"

    /**特殊文本, 最后一次输入的文本内容
     * "message:$[lastInput]"
     * */
    const val LAST_INPUT = "lastInput"

    /**当前活跃的窗口*/
    const val PACKAGE_ACTIVE = "active"

    /**坐标分隔符*/
    const val POINT_SPLIT = "~"

    /**多文本分隔符[com.angcyo.acc2.bean.TaskBean.textMap]*/
    const val TEXT_SPLIT = "|"

    const val OP = "><=%≈"

    /**依赖的Action标识
     * "jump:rely"
     * */
    const val RELY = "rely"

    /**表示当前的对象*/
    const val CURRENT = "."

    /**随便标识*/
    const val RANDOM = "random"

    /**使用返回的节点信息
     * [com.angcyo.acc2.bean.HandleBean.rootNode]*/
    const val RESULT = "result"

    /**顺序输入文本
     * [ACTION_INPUT]*/
    const val ORDER = "order"

    /**所有标识*/
    const val ALL = "*"

    /**取父节点*/
    const val PARENT = "parent"

    /**不取父节点*/
    const val NOT_PARENT = "not_parent"

    /**标识: 是否清空统计*/
    const val CLEAR = "clear"

    /**async*/
    const val ASYNC = "async"

    /**是否要跳过当前的整个组
     * [com.angcyo.acc2.action.PassAction]*/
    const val GROUP = "group"

    /**触发当前节点的点击事件, null 默认是click操作.
     * [:STATE_UNSELECTED] 支持状态参数. 表示, 只在节点满足状态时, 才点击
     * [:STATE_UNSELECTED:5] 自身要满足且5个parent内有节点满足状态, 才触发点击
     * 如果包含[*], 表示所有节点都必须满足状态条件, 否则只要有一个满足状态条件即可
     * [click:0.1,0.1] 效果等同于[ACTION_CLICK3]
     * 支持[NOT_PARENT]
     * [STATE_UNSELECTED]
     * [STATE_UNCHECKED]
     * */
    const val ACTION_CLICK = "click"

    /**在当前节点区域双击(手势双击) [:0.1,0.1]指定目标区域*/
    const val ACTION_CLICK2 = "click2"

    /**在当前节点区域点击(手势点击) [:0.1,0.1]指定目标区域*/
    const val ACTION_CLICK3 = "click3"

    /**
     * 支持[NOT_PARENT]*/
    const val ACTION_LONG_CLICK = "longClick" //触发当前节点的长按事件

    /**
     *[double:20,30] 在屏幕坐标x=20dp y=30dp的位置双击
     *[double:0.3,0.5] 在屏幕宽度0.3, 高度0.5的位置双击
     *[double:0.1,0.1~0.9,0.9] 随机在屏幕宽度[0.1,0.9], 高度[0.1,0.9]的位置双击
     * */
    const val ACTION_DOUBLE = "double" //[double:20,30] 在屏幕坐标x=20dp y=30dp的地方双击
    const val ACTION_TOUCH = "touch" //[touch:10,10] 在屏幕坐标x=10dp y=10dp的地方点击
    const val ACTION_MOVE = "move" //[move:10,10~100,100] 从屏幕坐标x=10dp y=10dp的地方移动到100dp 100dp的地方
    const val ACTION_FLING = "fling" //[fling:10,10~100,100]

    /**获取元素文本保存到
     * [com.angcyo.acc2.bean.TaskBean.textMap]
     * [getText:key regex:\\d+]
     * 默认key:getText
     * regex:文本需要通过正则过滤的表达式.
     *
     * 只会获取节点列表中第一个节点的文本
     */
    const val ACTION_GET_TEXT = "getText"

    /**获取元素文本保存到
     * [com.angcyo.acc2.bean.TaskBean.textMap]
     * [appendText:key regex:xxx]
     * 默认key:appendText
     * regex:\\d+ 文本需要使用正则过滤取值
     * [ACTION_INPUT]
     *
     * 支持[decode:true], 使用[URLDecoder]解码字符串
     *
     * 获取节点列表中第一个节点文本不为空的文本
     */
    const val ACTION_PUT_TEXT = "putText"

    /**获取元素文本追加保存到
     * [com.angcyo.acc2.bean.TaskBean.textListMap]
     * [appendText:key regex:xxx]
     * 默认key:appendText
     * regex:\\d+ 文本需要使用正则过滤取值
     * [ACTION_INPUT]
     */
    const val ACTION_APPEND_TEXT = "appendText"

    /**设置输入框文本内容
     * [input:$0] 支持文本变量 [com.angcyo.acc2.bean.TaskBean.wordList]
     * [input:$0~$-1] [com.angcyo.acc2.bean.TaskBean.wordList]
     * [input:$0~$-1 key:xxx] key用来累加输入计数
     * [input:$[xxx] regex:xxx] [com.angcyo.acc2.bean.TaskBean.textListMap]
     * [input:$[xxx] $0~$-1 regex:xxx] 从[com.angcyo.acc2.bean.TaskBean.textListMap]中获取, 再获取
     * regex:\\d+ 文本需要使用正则过滤取值
     * [input:$[xxx] index:1]如果$[xxx]返回了一个列表,那么[index]参数, 就是获取列表中的第几个.
     * 不指定[index]表示随机获取列表中的数据,使用[index:[ORDER]]表示顺序获取.
     * [index:$[xxx]] index参数也支持文本变量
     *
     * 不指定index, 则随机从解析集合结果中获取一个.
     *
     * "input:$0~$-1 key:comment index:order",
     * "input:$[searchName] index:order"
     * */
    const val ACTION_INPUT = "input"

    /**
     * 移除存在在map中的输入的文本, 默认key, [Action.DEF]
     * [com.angcyo.acc2.control.AccSchedule.inputTextMap]
     * "removeInputTextMap:searchWord"
     * */
    const val ACTION_REMOVE_INPUT_TEXT_MAP = "removeInputTextMap"

    /**
     * 保存指定的值, 到
     * [com.angcyo.acc2.control.AccSchedule.inputTextMap]
     * "saveInputTextMap:$[lastInput] key:xxx"
     * */
    const val ACTION_SAVE_INPUT_TEXT_MAP = "saveInputTextMap"

    /**将指定的文本设置给[com.angcyo.acc2.bean.TaskBean.textMap]
     * [com.angcyo.acc2.bean.TaskBean.textListMap]
     * [setText:key xxx]
     * */
    const val ACTION_SET_TEXT = "setText"

    /**清理[com.angcyo.acc2.bean.TaskBean.textMap]数据
     * [clearText:*] 清理所有
     * [clearText:k1 k2 k3] 清理指定key
     * */
    const val ACTION_CLEAR_TEXT = "clearText"

    /**清除指定id的[com.angcyo.acc2.bean.ActionBean]的运行次数
     * [clearRunCount] 清理当前的[com.angcyo.acc2.bean.ActionBean]
     * [clearRunCount:xxx xxx]
     * [ACTION_JUMP]
     * [ACTION_CLEAR_RUN_TIME]
     * [ACTION_CLEAR_JUMP_COUNT]
     * */
    const val ACTION_CLEAR_RUN_COUNT = "clearRunCount"

    /**清除[com.angcyo.acc2.bean.ActionBean]的运行时长
     * [ACTION_CLEAR_RUN_COUNT]
     * [clearRunTime:rely]
     * [clearRunTime:.]
     * [clearRunTime:1 2 3 4]
     * */
    const val ACTION_CLEAR_RUN_TIME = "clearRunTime"

    /**清除指定id的[com.angcyo.acc2.bean.ActionBean]的跳转次数
     * [clearJumpCount:xxx xxx]
     * [ACTION_JUMP]
     * */
    const val ACTION_CLEAR_JUMP_COUNT = "clearJumpCount"

    /**回退按钮
     * [back:>1000]连续back操作, 满足条件才触发.否则失败*/
    const val ACTION_BACK = "back"

    /**截屏*/
    const val ACTION_SCREENSHOT = "screenshot"

    /**回到桌面*/
    const val ACTION_HOME = "home"

    /**跳转到指定的actionId执行
     * [jump:100] 下一个执行actionId为100的action
     * [jump:100 120 130] 找到第一个存在的action进行跳转
     * [jump] 跳过当前, 指定下一个
     * [jump:rely] 从[com.angcyo.acc2.bean.ActionBean.relyList]列表中获取一个可以跳转的action
     * [jump:rely 2] 从[com.angcyo.acc2.bean.ActionBean.relyList]列表中获取索引为2可以跳转的action
     * */
    const val ACTION_JUMP = "jump"

    /**[pass] 直接返回true
     * [pass group]跳过同一组中的所有[com.angcyo.acc2.bean.ActionBean]
     * [com.angcyo.acc2.action.Action.ACTION_TRUE]
     * [com.angcyo.acc2.action.Action.ACTION_FALSE]*/
    const val ACTION_PASS = "pass"

    /**激活指定actionId的[com.angcyo.acc2.bean.ActionBean]
     * [enable 100 120 200 220]*/
    const val ACTION_ENABLE = "enable"

    /**禁用, 参考[ACTION_ENABLE]*/
    const val ACTION_DISABLE = "disable"

    /**禁用当前的[com.angcyo.acc2.bean.HandleBean]*/
    const val ACTION_DISABLE_HANDLE = "disableHandle"

    /**用来触发请求表单
     * [requestForm] 智能根据顺序获取需要请求的表单
     * [requestForm:task]
     * [requestForm:action]
     * [requestForm:handle]
     * [requestForm:operate]
     * [requestForm:xxx]
     * [form:form1] 支持从[com.angcyo.acc2.bean.TaskBean.formMap]中获取[com.angcyo.acc2.bean.FormBean]
     * */
    const val ACTION_REQUEST_FORM = "requestForm"

    /**[ACTION_REQUEST_FORM]别名*/
    const val ACTION_FORM = "form"

    /**异常, 并中断整个任务, 支持替换文本变量
     * [error:reason]
     * */
    const val ACTION_ERROR = "error"

    /**[sleep:1000]线程休眠1000毫秒
     * 支持时间格式
     * 支持文本变量
     * [com.angcyo.acc2.parse.AccParse.parseTime]*/
    const val ACTION_SLEEP = "sleep"

    /**直接完成任务, 支持替换文本变量
     * [finish:reason]*/
    const val ACTION_FINISH = "finish"

    /**停止控制器, 释放资源, 支持替换文本变量
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

    /**
     * 使用原生的[Intent]打开界面
     * [intent:android.settings.SETTINGS]
     * */
    const val ACTION_INTENT = "intent"

    /**使用目标程序, 打开指定url
     * [url:http://www.baidu.com] 默认使用目标程序打开
     * [url:xxx pkg:$[xxx]] pkd:指定程序的包名, 支持文本变量
     * */
    const val ACTION_URL = "url"

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
     * [com.angcyo.acc2.action.FalseAction]
     * [com.angcyo.acc2.parse.HandleResult.forceFail]*/
    const val ACTION_FALSE = "false"

    /**随机进行失败处理, 这样同一个[com.angcyo.acc2.bean.ActionBean]就有可能多次执行
     * [ACTION_FALSE]
     * [randomFalse:<=30] 参数可以接上概率. 如果概率命中, 那么功能和[ACTION_FALSE]一样
     * [com.angcyo.acc2.bean.ActionBean.randomAmount]*/
    const val ACTION_RANDOM_FALSE = "randomFalse"

    /**弹出toast默认是[ACTION_TOAST_QQ], 支持替换文本变量
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

    /**设置全屏浮窗不接受touch事件
     * [notTouchable] 跟随[com.angcyo.acc2.bean.TaskBean.notTouchable]的设置
     * [notTouchable:true]
     * [notTouchable:true wait:3000] 等待3000毫秒后执行
     * [notTouchable async] 异步指定
     * */
    const val ACTION_NOT_TOUCHABLE = "notTouchable"

    /**
     * 隐藏浮窗, 可以指定需要隐藏多久, 如果不指定隐藏时长, 则下次触发显示时立马会显示
     * [:1] 表示隐藏浮窗到下一个action. 超过actionList数量时, 就会变成了毫秒时间
     * [:5000] 表示隐藏浮窗5秒
     * */
    const val ACTION_HIDE_WINDOW = "hideWindow"

    /**当满足条件时,中断后面的[actionList]执行
     * [interrupt:find key:findKey exp:<=100] //中断查找次数小于100次的时候
     * [interrupt:handle key:handleKey exp:<=100] //中断前100次处理
     * */
    const val ACTION_INTERRUPT = "interrupt"

    /**计数, 可以被其他条件引用判断
     * [count:key +1 -2 +3]
     * [count:key clear]
     * */
    const val ACTION_COUNT = "count"

    /**动态代码执行Action
     * [com.angcyo.acc2.dynamic.IHandleActionDynamic]*/
    const val ACTION_CLS = "cls"

    /*---------------------------------------状态匹配--------------------------------------*/

    /**需要指定的状态
     * [com.angcyo.acc2.bean.FindBean.stateList]*/

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

    const val STATE_VISIBLE = "visible" //具备isVisibleToUser状态
    const val STATE_UN_VISIBLE = "unVisible" //不具备isVisibleToUser状态
    const val STATE_DISMISSABLE = "dismissable" //isDismissable
    const val STATE_UN_DISMISSABLE = "unDismissable"
    const val STATE_EDITABLE = "editable" //isEditable
    const val STATE_UN_EDITABLE = "unEditable"
    const val STATE_MULTILINE = "multiLine" //isMultiLine
    const val STATE_UN_MULTILINE = "unMultiLine"
}