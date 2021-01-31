package com.angcyo.acc2.action

import com.angcyo.library.ex.patternList
import java.net.URLEncoder

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/31
 */
object Action {

    const val base64Image = "data:image/jpeg;base64,"

    /**参数分隔符*/
    const val ARG_SPLIT = ":"

    const val OP = "><=%≈"

    const val RELY = "rely"

    /**点击[WebElement]*/
    const val ACTION_CLICK = "click"

    /**输入文本
     * [input:$0]
     * [input:$[xxx]]
     * */
    const val ACTION_INPUT = "input"

    /**可以用于清除输入框的内容*/
    const val ACTION_CLEAR = "clear"

    /**获取元素文本保存到
     * [com.angcyo.acc2.bean.TaskBean.textMap]
     * [getText:key]
     * 默认key:getText
     */
    const val ACTION_GET_TEXT = "getText"

    /**获取元素属性保存到 [x:x]
     * [com.angcyo.acc2.bean.TaskBean.textMap]
     * [getAttr[xxx]:key]
     * 默认key:getAttr
     * */
    const val ACTION_GET_ATTR = "getAttr"

    /**获取元素样式保存到
     * [com.angcyo.acc2.bean.TaskBean.textMap]
     * [getCss[xxx]:key]
     * 默认key:getCss
     * */
    const val ACTION_GET_CSS = "getCss"

    /**回退网页*/
    const val ACTION_BACK = "back"

    /**前进网页*/
    const val ACTION_FORWARD = "forward"

    /**跳转网页*/
    const val ACTION_TO = "to"

    /**刷新网页*/
    const val ACTION_REFRESH = "refresh"

    /**将文本进行验证码识别,
     * 并且将解析结果保存到 [com.angcyo.acc2.bean.TaskBean.textMap]
     * [code:$[key]:key:type]
     * 正则参数解析:
     * [code:x1 key:x2 type:x3]
     * x1:需要解析的验证么base64图片数据
     * x2:解析结果保存在map中的key值
     * x3:验证码的识别类型
     * 默认key:imageCode
     * */
    const val ACTION_CODE = "code"

    /**弹出输入框, 手动输入验证码
     * [putCode:x1 key:x2]
     * 默认key:imageCode
     * */
    const val ACTION_PUT_CODE = "putCode"

    /**弹出输入框, 手动输入文本
     * [putText title:对话框标题 key:xxx label:对话框label prompt:输入框提示 tip:提示]
     * [com.angcyo.acc2.bean.TaskBean.textMap]
     * 默认key:putText
     * */
    const val ACTION_PUT_TEXT = "putText"

    /**弹出确认对话框, 手动确认之后, 才能继续后续执行
     * [confirm title:对话框标题 tip:提示]
     * */
    const val ACTION_CONFIRM = "confirm"

    /**移除指定元素的指定属性
     * [removeAttr:css:attr]
     * [css] 选择器, 选择指定元素
     * [attr] 需要移除的属性*/
    const val ACTION_REMOVE_ATTR = "removeAttr"

    /**设置指定元素的属性值
     * [setAttr:css:attr=value]*/
    const val ACTION_SET_ATTR = "setAttr"

    /**获取截图
     * 如果选中了元素, 则浏览器截图之后, 剪切元素位置的图片
     * 如果未选中元素, 则表示整个网页
     * 可以指定矩形参数
     * [com.angcyo.acc2.bean.SelectorBean.rectList]
     *
     * [screenshot key:imageCode l:10 t:10 w:100 h:100] 获取屏幕截图 左上角10,10 宽高100x100的图片放到[com.angcyo.acc2.bean.TaskBean.textMap]的[imageCode]中
     * [css:#id] 如果元素在屏幕外, 则通过js获取元素的坐标需要指定选择器
     * */
    const val ACTION_SCREENSHOT = "screenshot"

    /**同名[ACTION_SCREENSHOT]*/
    const val ACTION_SHOT = "shot"

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

    /**
     * 从字符串中获取模版参数 key:value
     * 特殊字符请使用[URLEncoder.encode]加码后的字符
     * [str] 数据字符串 "code:$[ke+y+] k:key type:30400 \b last line"
     * [key] 键值 code 或k 或type
     */
    fun patternArg(str: String, key: String, delimiters: Char = ':'): String? {
        return str.patternList("(?<=$key$delimiters)(\\S+)").firstOrNull()
    }
}