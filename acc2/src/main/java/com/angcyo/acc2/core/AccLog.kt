package com.angcyo.acc2.core

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/29
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class AccLog {

    //核心
    var service: AccessibilityService? = BaseAccService.lastService

    //输出日志
    var outBuilder: StringBuilder = StringBuilder()

    //是否要过滤window
    var filterWindow: (AccessibilityWindowInfo) -> Boolean = { false }

    //输出window的node信息
    var logWindowNode: Boolean = true

    /**
     * 获取所有[AccessibilityWindowInfo]的信息
     * [filter] 过滤不需要日志的[AccessibilityWindowInfo]对象
     *
     * 0->导航栏
     * AccessibilityWindowInfo[title=导航栏, id=125, type=TYPE_SYSTEM, layer=2, bounds=Rect(0, 1794 - 1080, 1920), focused=false, active=false, pictureInPicture=false, hasParent=false, isAnchored=false, hasChildren=false]
     * 1->null
     * AccessibilityWindowInfo[title=null, id=126, type=TYPE_SYSTEM, layer=1, bounds=Rect(0, 0 - 1080, 63), focused=false, active=false, pictureInPicture=false, hasParent=false, isAnchored=false, hasChildren=false]
     * 2->[Active]车队智能辅助
     * AccessibilityWindowInfo[title=车队智能辅助, id=821, type=TYPE_APPLICATION, layer=0, bounds=Rect(0, 0 - 1080, 1920), focused=true, active=true, pictureInPicture=false, hasParent=false, isAnchored=false, hasChildren=false]
     * */
    fun getAccessibilityWindowLog(): StringBuilder {
        service?.windows?.forEachIndexed { index, accessibilityWindowInfo ->
            if (filterWindow(accessibilityWindowInfo)) {
                //忽略当前窗口
            } else {
                outBuilder.append("$index->")
                if (accessibilityWindowInfo.root == service?.rootInActiveWindow) {
                    outBuilder.append("[Active]")
                }
                outBuilder.appendln("$accessibilityWindowInfo")

                if (logWindowNode) {
                    //节点日志
                    accessibilityWindowInfo.root?.let { getNodeLog(it) }
                }
            }
        }
        return outBuilder
    }

    //输出node的action信息
    var logNodeAction: Boolean = true

    //输出子node信息
    var logNodeChild: Boolean = true

    var refWidth: Int = _screenWidth
    var refHeight: Int = _screenHeight

    /**递归获取所有节点的日志*/
    fun getNodeLog(node: AccessibilityNodeInfo): StringBuilder {
        outBuilder.appendln(node.wrap().toString())
        val header =
            "╔════════════════════════════════════════════════════════════════════════════════"
        val footer =
            "╚════════════════════════════════════════════════════════════════════════════════"
        outBuilder.appendln(header)
        _getNodeLog(node, 1, "")
        outBuilder.appendln(footer)
        return outBuilder
    }

    fun _getNodeLog(
        node: AccessibilityNodeInfo,
        index: Int = 0, /*缩进控制*/
        preIndex: String = "", /*child路径*/
    ) {
        val wrapNode = node.wrap()

        outBuilder.apply {
            append(" ".repeat2(index))
            append(wrapNode.className)
            wrapNode.viewIdName()?.let {
                append("(${it})")
            }

            buildString {
                if (wrapNode.isEnabled) {
                    //append("enabled ")
                } else {
                    append("disable ")
                }
                if (wrapNode.isClickable) {
                    append("clickable ")
                }
                if (wrapNode.isLongClickable) {
                    append("longClickable ")
                }
                if (wrapNode.isScrollable) {
                    append("scrollable ")
                }
                if (wrapNode.isSelected) {
                    append("selected ")
                }
                if (wrapNode.isPassword) {
                    append("password ")
                }

                if (wrapNode.isCheckable) {
                    append("checkable:${wrapNode.isChecked} ")
                }
                if (wrapNode.isFocusable) {
                    append("focusable:${wrapNode.isFocused} ")
                }
            }.apply {
                if (this.trim().isNotEmpty()) {
                    append(" [")
                    append(this)
                    append("]")
                }
            }

//        append(" ck:${isCheckable}") //是否可以check
//        append(" ckd:${isChecked}") //是否check
//
//        append(" f:${isFocusable}") //是否可以获取焦点
//        append(" fd:${isFocused}") //是否焦点
//
//        append(" c:${isClickable}") //是否可以点击
//        append(" lc:${isLongClickable}") //是否可以长按
//        append(" sc:${isScrollable}") //是否可以滚动
//
//        append(" ed:${isEnabled}") //是否激活
//        append(" sd:${isSelected}") //是否选中
//        append(" pd:${isPassword}") //是否是密码

            val text = wrapNode.text
            val des = wrapNode.contentDescription

            if (text == null && des == null) {
                //2个都是空, 节省log数据
            } else {
                append(" [${wrapNode.text}] [${wrapNode.contentDescription}]")
            }
            wrapNode.hintText?.apply {
                append(" hintText:[${this}]")
            }
            wrapNode.paneTitle?.apply {
                append(" paneTitle:[${this}]")
            }
            wrapNode.tooltipText?.apply {
                append(" tooltipText:[${this}]")
            }

            if (wrapNode.childCount > 0) {
                append("[${wrapNode.childCount}]")
            }
        }

        //在父布局中的位置
        wrapNode.getBoundsInParent(tempRect)
        outBuilder.append(" pr:$tempRect")

        //在屏幕中的位置
        wrapNode.getBoundsInScreen(tempRect)
        outBuilder.append(" sr:$tempRect")

        //宽高
        val width = tempRect.width()
        val height = tempRect.height()
        outBuilder.append("[${width}x$height]")
        outBuilder.append("[${width / dp}x${height / dp}]")

        //在屏幕中的位置比例
        outBuilder.append("(${tempRect.left * 1f / refWidth},${tempRect.top * 1f / refHeight}")
        outBuilder.append("~")
        outBuilder.append("${tempRect.right * 1f / refWidth},${tempRect.bottom * 1f / refHeight}")
        outBuilder.append(" :${width * 1f / refWidth}")
        outBuilder.append(" :${height * 1f / refHeight}")
        outBuilder.append(")")

        //节点路径 path (2020-07-03 已经不需要了)
        //outBuilder.append(" $preIndex")

        //可执行的action
        if (logNodeAction) {
            outBuilder.append(" ")
            wrapNode.actionStr(outBuilder)
        }

        //new line
        outBuilder.appendln()

        if (logNodeChild) {
            for (i in 0 until wrapNode.childCount) {
                wrapNode.getChild(i)?.let {
                    _getNodeLog(
                        it.unwrap(),
                        index + 1,
                        "${if (preIndex.isEmpty()) preIndex else "${preIndex}_"}$i",
                    )
                }
            }
        }
    }
}