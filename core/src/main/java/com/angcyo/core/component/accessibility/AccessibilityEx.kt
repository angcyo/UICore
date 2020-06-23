package com.angcyo.core.component.accessibility

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.Context
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.angcyo.http.RIo
import com.angcyo.library.L
import com.angcyo.library.R
import com.angcyo.library.ex.copy
import com.angcyo.library.tip

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

fun sleep(delay: Long, action: () -> Unit) {
    Thread.sleep(delay)
    action.invoke()
}

fun Rect.toPath(): Path {
    return Path().apply {
        moveTo(this@toPath.centerX().toFloat(), this@toPath.centerY().toFloat())
    }
}

/**不包含导航栏的高度*/
fun Context.displaySize(): Point {
    val wm: WindowManager =
        this.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    wm.defaultDisplay.getSize(point)
    return point
}


/**包含导航栏的高度*/
fun Context.displayRealSize(): Point {
    val wm: WindowManager =
        this.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    wm.defaultDisplay.getRealSize(point)
    return point
}

/**没有包含导航栏*/
fun Context.displayRealRect(): Rect {
    val wm: WindowManager =
        this.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val rect = Rect()
    wm.defaultDisplay.getRectSize(rect)
    return rect
}

fun Context.density(): Float {
    val wm: WindowManager =
        this.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val dm = DisplayMetrics()
    wm.defaultDisplay.getMetrics(dm)
    return dm.density
}

/**导航栏的高度,如果隐藏了返回0*/
fun Activity.navigatorBarHeight(): Int {
    val decorRect = Rect()
    val contentRect = Rect()
    window.decorView.getGlobalVisibleRect(decorRect) //包含导航栏的高度

    //当 android:windowSoftInputMode="adjustResize" 时,bottom值=屏幕高度-导航栏高度-减去键盘的高度
    //当设置 window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 之后, top的值为0, 否则为状态栏高度.
    //同时, 键盘的高度不能拿到了.
    window.findViewById<View>(Window.ID_ANDROID_CONTENT)
        .getGlobalVisibleRect(contentRect) //不包含导航栏的高度
    if (decorRect.bottom == contentRect.bottom) {
        //没有导航栏 或者 隐藏了导航栏
        return 0
    }
    return displayRealSize().y - displaySize().y
}

//<editor-fold desc="AccessibilityService扩展">

fun AccessibilityService.rootNodeInfo(event: AccessibilityEvent?): AccessibilityNodeInfo? {
    return if (rootInActiveWindow == null) {
        event?.source
    } else {
        rootInActiveWindow
    }
}

/**通过给定的文本, 查找匹配的所有[AccessibilityNodeInfo]*/
fun AccessibilityService.findNodeByText(
    text: String?,
    event: AccessibilityEvent?
): List<AccessibilityNodeInfo> {
    val rootNodeInfo = rootNodeInfo(event)
    val nodes = mutableListOf<AccessibilityNodeInfo>()
    rootNodeInfo?.findAccessibilityNodeInfosByText(text)?.let {
        nodes.addAll(it)
    }
    return nodes
}

/**
 * [id] id/button1
 *
 * -> button1
 * */
fun AccessibilityService.findNodeById(
    id: String,
    event: AccessibilityEvent?
): List<AccessibilityNodeInfo> {
    val rootNodeInfo = rootNodeInfo(event)
    val idString = event?.id(id)
    val nodes = mutableListOf<AccessibilityNodeInfo>()
    rootNodeInfo?.findAccessibilityNodeInfosByViewId(idString)?.let {
        nodes.addAll(it)
    }
    return nodes
}


/**返回 文本 node 在屏幕中的 矩形坐标*/
fun AccessibilityService.findRectByText(
    text: String,
    event: AccessibilityEvent?
): Array<Rect> {
    val rootNodeInfo = rootNodeInfo(event)

    val nodes = rootNodeInfo?.findAccessibilityNodeInfosByText(text)
    val rectList = mutableListOf<Rect>()

    nodes?.mapIndexed { _, accessibilityNodeInfo ->
        rectList.add(accessibilityNodeInfo.toRect())
    }
    return rectList.toTypedArray()
}


/**
 * id 全路径 "com.xunmeng.pinduoduo:id/ll_tab"
 * 但是 只需要传 ll_tab 就行
 * */
fun AccessibilityService.findRectById(id: String, event: AccessibilityEvent?): Array<Rect> {
    val rootNodeInfo = rootNodeInfo(event)

    val idString = event?.id(id)

    val nodes = rootNodeInfo?.findAccessibilityNodeInfosByViewId(idString)
    val rectList = mutableListOf<Rect>()

    nodes?.mapIndexed { _, accessibilityNodeInfo ->
        val rect = Rect()
        accessibilityNodeInfo.getBoundsInScreen(rect)
        rectList.add(rect)
    }
    return rectList.toTypedArray()
}

/**返回中心点坐标*/
fun AccessibilityService.findPathByText(
    text: String,
    event: AccessibilityEvent?
): Array<Path> {
    val rectList = findRectByText(text, event)
    val pathList = mutableListOf<Path>()

    rectList.mapIndexed { _, rect ->
        val path = Path().apply {
            moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        }
        pathList.add(path)
    }

    return pathList.toTypedArray()
}

/**给定一组矩形, 返回在屏幕底部的矩形*/
fun Context.findBottomRect(rects: Array<Rect>): Rect {
    var targetRect = Rect()
    val point = displaySize()
    rects.map {
        if (it.centerY() > point.y / 2) {
            if (it.centerY() > targetRect.centerY()) {
                targetRect = it
            }
        }
    }
    return targetRect
}

/**从顶部查询*/
fun Context.findTopRect(rects: Array<Rect>): Rect {
    val point = displaySize()
    var targetRect = Rect(0, 0, point.x, point.y)
    rects.map {
        if (it.centerY() < point.y / 2) {
            if (it.centerY() < targetRect.centerY()) {
                targetRect = it
            }
        }
    }
    return targetRect
}

//</editor-fold desc="AccessibilityService扩展">

//<editor-fold desc="AccessibilityEvent扩展">

fun AccessibilityEvent.isWindowStateChanged(): Boolean =
    eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

/**此事件, 会优先于 TYPE_WINDOW_STATE_CHANGED 调用*/
fun AccessibilityEvent.isWindowContentChanged(): Boolean =
    eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED

/**获取全路径的id*/
fun AccessibilityEvent.id(id: String): String {
    if (packageName == null) {
        return id
    }

    return if (id.contains(packageName)) {
        id
    } else {
        "${packageName}:id/$id"
    }
}

//</editor-fold desc="AccessibilityEvent扩展">

//<editor-fold desc="AccessibilityNodeInfo扩展">

fun AccessibilityNodeInfo.toRect(): Rect {
    val rect = Rect()
    getBoundsInScreen(rect)
    return rect
}

/**调用node的点击事件*/
fun AccessibilityNodeInfo.click() {
    performAction(AccessibilityNodeInfo.ACTION_CLICK)
}

fun AccessibilityNodeInfo.setText(text: CharSequence?) {
    //AccessibilityNodeInfoCompat.wrap(this).text = text
    val arguments = Bundle()
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 -> {
            text?.copy()
            performAction(AccessibilityNodeInfo.ACTION_PASTE)
        }
        else -> {
            tip("设备不支持\n设置文本", R.drawable.lib_ic_error)
        }
    }
}

/**向前滚动列表*/
fun AccessibilityNodeInfo.scrollForward() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN)
    } else {
        performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }
}

/**向后滚动列表*/
fun AccessibilityNodeInfo.scrollBackward() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP)
    } else {
        performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    }
}

/**返回ListNode*/
fun AccessibilityNodeInfo.findListView(): AccessibilityNodeInfo? {
    var node: AccessibilityNodeInfo? = null
    if (className.contains("ListView") || className.contains("RecyclerView")) {
        node = this
    } else {
        for (i in 0 until childCount) {
            node = getChild(i).findListView()
            if (node != null) {
                break
            }
        }
    }
    return node
}

/**通过 0_0_1_2 (child index) 这种路径拿到Node*/
fun AccessibilityNodeInfo.nodeFromPath(path: String /*0_0_1_2 这种路径拿到Node*/): AccessibilityNodeInfo? {
    fun getNode(nodeInfo: AccessibilityNodeInfo?, index: Int): AccessibilityNodeInfo? {
        if (nodeInfo == null) {
            return null
        }
        if (nodeInfo.childCount > index) {
            return nodeInfo.getChild(index)
        }
        return null
    }

    var nodeInfo: AccessibilityNodeInfo? = this
    path.split("_").toList().map {
        nodeInfo = getNode(nodeInfo, it.toInt())
        if (nodeInfo == null) {
            return null
        }
    }
    return nodeInfo
}

/**拿到最根节点的NodeInfo*/
fun AccessibilityNodeInfo.getRootNodeInfo(): AccessibilityNodeInfo {
    if (parent == null) {
        return this
    }
    return parent.getRootNodeInfo()
}

fun AccessibilityNodeInfo.logNodeInfo(logFilePath: String? = null) {

    if (logFilePath == null) {
        L.v("╔═══════════════════════════════════════════════════════════════════════════════════════")
    } else {
        RIo.appendToFile(logFilePath, "╔═══════════════════════════\n")
    }
    debugNodeInfo(0, "", logFilePath)
    if (logFilePath == null) {
        L.v("╚═══════════════════════════════════════════════════════════════════════════════════════")
    } else {
        RIo.appendToFile(logFilePath, "╚═══════════════════════════\n")
    }
}

fun AccessibilityNodeInfo.debugNodeInfo(
    index: Int = 0 /*缩进控制*/,
    preIndex: String = "" /*child路径*/,
    logFilePath: String? = null
) {
    fun newLine(i: Int): String {
        val sb = StringBuilder()
        for (j in 0 until i) {
            sb.append("    ")
        }
        return sb.toString()
    }

    val stringBuilder = StringBuilder("|")
    stringBuilder.append(newLine(index))
    stringBuilder.append(" ${className}")
    stringBuilder.append(" c:${isClickable}")
    stringBuilder.append(" s:${isSelected}")
    stringBuilder.append(" ck:${isChecked}")
//            stringBuilder.append(" idName:")
//            stringBuilder.append(viewIdResourceName)
    stringBuilder.append(" [${text}]")
    stringBuilder.append(" $childCount")

    val rect = Rect()
    getBoundsInScreen(rect)
    stringBuilder.append(" $rect")
    stringBuilder.append(" $preIndex")

    if (logFilePath == null) {
        L.v("$stringBuilder")
    } else {
        RIo.appendToFile(logFilePath, "$stringBuilder\n")
    }

    for (i in 0 until childCount) {
        getChild(i)?.let {
            it.debugNodeInfo(
                index + 1,
                "${if (preIndex.isEmpty()) preIndex else "${preIndex}_"}$i",
                logFilePath
            )
        }
    }
}

//</editor-fold desc="AccessibilityNodeInfo扩展">
