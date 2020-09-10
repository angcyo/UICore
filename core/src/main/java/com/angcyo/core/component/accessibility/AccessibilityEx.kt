package com.angcyo.core.component.accessibility

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
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
import android.view.accessibility.AccessibilityWindowInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.AccessibilityHelper.tempRect
import com.angcyo.covertToStr
import com.angcyo.http.RIo
import com.angcyo.library.L
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.*

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */


/**[Manifest.permission.KILL_BACKGROUND_PROCESSES]*/
@SuppressLint("MissingPermission")
fun Context.kill(packageName: String): Boolean {
    return try {
        val am: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.killBackgroundProcesses(packageName)
        val infos = am.runningAppProcesses
        for (info in infos) {
            if (info.processName == packageName) {
                android.os.Process.killProcess(info.pid)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun sleep(delay: Long, action: Action? = null) {
    Thread.sleep(delay)
    action?.invoke()
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

fun AccessibilityService.findNode(predicate: (node: AccessibilityNodeInfoCompat) -> Unit) {
    rootNodeInfo()?.findNode(predicate = {
        predicate(it)
        -1
    })
}

fun AccessibilityService.rootNodeInfo(event: AccessibilityEvent? = null): AccessibilityNodeInfo? {
    var maxHeightWindow: AccessibilityWindowInfo? = null
    var maxHeight = 0

    windows.forEach {
        it.getBoundsInScreen(tempRect)
        val height = tempRect.height()
        if (height > maxHeight) {
            maxHeightWindow = it
            maxHeight = height
        }
    }

    val activeWindow = windows.find { it.isActive && it.isFocused }
    return maxHeightWindow?.root ?: (activeWindow?.root ?: event?.source)
}

/**根据给定包名, 获取对应的根节点
 * [packageName] 只需要指定的包名, 空表示所有
 *
 * 此方法会带来以下警告:[android.view.accessibility.AccessibilityWindowInfo.getRoot]
 * AccessibilityInteractionClient: old interaction Id is: -1,current interaction Id is:0
 *
 * [onlyTopWindow] 是否只对应包名的顶层window中的节点信息, 否则会获取所有window中的节点信息
 * */
fun AccessibilityService.findNodeInfoList(
    packageName: List<String>? = null,
    onlyTopWindow: Boolean = false
): List<AccessibilityNodeInfo> {
    //需要返回的根节点信息
    val allNode: MutableList<AccessibilityNodeInfo> = mutableListOf()

    //保存顶层window
    val windowLayerList = mutableListOf<CharSequence>()

    windows.forEach { windowInfo ->
        windowInfo.root?.let { root ->
            if (onlyTopWindow && windowLayerList.contains(root.packageName)) {
                //只需要获取顶层window的节点信息 //已经获取了
            } else if (!allNode.contains(root)) {
                if (packageName.isNullOrEmpty() || packageName.contains(root.packageName)) {
                    allNode.add(root)
                }
                if (onlyTopWindow) {
                    //保存
                    windowLayerList.add(root.packageName)
                }
            }
        }
    }

    rootInActiveWindow?.let { node ->
        if (!allNode.contains(node)) {
            if (packageName.isNullOrEmpty() || packageName.contains(node.packageName)) {
                allNode.add(node)
            }
        }
    }

//    allNode.sortWith(Comparator { nodeInfo1, nodeInfo2 ->
//        nodeInfo1.getBoundsInScreen(tempRect)
//        val height1 = tempRect.height()
//        nodeInfo2.getBoundsInScreen(tempRect)
//        val height2 = tempRect.height()
//
//        when {
//            height1 < height2 -> 1  //节点高度越小, 放在列表的后面. 使得列表的头部是高度很高的node
//            height1 == height2 -> 0
//            else -> -1
//        }
//    })

    return allNode
}

/**在节点列表中, 过滤指定包名的节点*/
fun List<AccessibilityNodeInfo>.filter(packageName: List<String>): List<AccessibilityNodeInfo> {
    val result: MutableList<AccessibilityNodeInfo> = mutableListOf()

    forEach {
        if (packageName.contains(it.packageName)) {
            result.add(it)
        }
    }

    return result
}

/**拿到高度最高的节点*/
fun List<AccessibilityNodeInfo>.mainNode(): AccessibilityNodeInfo? {
    if (size == 1) {
        return firstOrNull()
    }

    var result: AccessibilityNodeInfo? = null
    var maxHeight = 0
    var maxWidth = 0
    forEach {
        it.getBoundsInScreen(tempRect)
        val height = tempRect.height()
        val width = tempRect.width()
        if (height >= maxHeight /*高度最高的*/ &&
            (width >= maxWidth || width >= _screenWidth / 2 /*宽度最大的, 或者宽度大于屏幕的一半*/)
        ) {
            result = it
            maxHeight = height
            maxWidth = width
        }
    }
    return result
}

/**通过给定的文本, 查找匹配的所有[AccessibilityNodeInfo]*/
fun AccessibilityService.findNodeByText(
    text: String?,
    event: AccessibilityEvent? = null
): List<AccessibilityNodeInfo> {
    val rootNodeInfo = rootNodeInfo(event)
    val nodes = mutableListOf<AccessibilityNodeInfo>()
    rootNodeInfo?.findAccessibilityNodeInfosByText(text)?.let {
        nodes.addAll(it)
    }
    return nodes
}

/**是否有指定的文本对应的[AccessibilityNodeInfo]*/
fun AccessibilityService.haveNode(
    text: String?,
    event: AccessibilityEvent? = null
): Boolean {
    return findNodeByText(text, event).isNotEmpty()
}

fun AccessibilityService.haveNodeOrText(
    text: String?,
    event: AccessibilityEvent? = null
): Boolean {
    if (text.isNullOrEmpty()) {
        return false
    }
    return haveNode(text, event) || event?.haveText(text) ?: false
}

fun AccessibilityService.getLikeText(text: String?): List<CharSequence> {
    val result = mutableListOf<CharSequence>()
    findNodeByText(text).forEach {
        result.add(it.text)
    }
    return result
}

/**
 * [id] id/button1 传入参数.
 * -> button1
 *
 * 自动根据包名补齐id: com.ss.android.ugc.aweme:id/cxw
 * */
fun AccessibilityService.findNodeById(
    id: String,
    event: AccessibilityEvent? = null
): List<AccessibilityNodeInfo> {
    val rootNodeInfo = rootNodeInfo(event)
    val idString = event?.id(id) ?: rootNodeInfo?.id(id)
    val nodes = mutableListOf<AccessibilityNodeInfo>()
    rootNodeInfo?.findAccessibilityNodeInfosByViewId(idString)?.let {
        nodes.addAll(it)
    }
    return nodes
}


/**返回 文本 node 在屏幕中的 矩形坐标*/
fun AccessibilityService.findRectByText(
    text: String,
    event: AccessibilityEvent? = null
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
fun AccessibilityService.findRectById(id: String, event: AccessibilityEvent? = null): Array<Rect> {
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
    event: AccessibilityEvent? = null
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

/**点击文本指定的[AccessibilityNodeInfo]*/
fun AccessibilityService.clickByText(
    text: String?,
    event: AccessibilityEvent? = null
): Boolean {
    val allNodeList = findNodeByText(text, event)
    val allClickNodeList = mutableListOf<AccessibilityNodeInfo>()

    allNodeList.forEach {
        if (it.isClickable) {
            allClickNodeList.add(it)
        }
    }

    return allClickNodeList.firstOrNull()?.run {
        click()
    } ?: false
}

/**点击文本指定的[AccessibilityNodeInfo]*/
fun AccessibilityService.clickById(
    id: String,
    event: AccessibilityEvent? = null
): Boolean {
    val allNodeList = findNodeById(id, event)
    val allClickNodeList = mutableListOf<AccessibilityNodeInfo>()

    allNodeList.forEach {
        if (it.isClickable) {
            allClickNodeList.add(it)
        }
    }

    return allClickNodeList.firstOrNull()?.run {
        click()
    } ?: false
}

/**
 * 相当于按返回键
 * */
fun AccessibilityService.back(): Boolean {
    //api 16
    return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
}

/**回到桌面*/
fun AccessibilityService.home(): Boolean {
    //api 16
    return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
}

/**最近*/
fun AccessibilityService.recents(): Boolean {
    //api 16
    return performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
}

/**锁屏*/
fun AccessibilityService.lockScreen(): Boolean {
    //api 28
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
    } else {
        false
    }
}

/**屏幕截图*/
fun AccessibilityService.takeScreenShot(): Boolean {
    //api 28
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
    } else {
        false
    }
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
    return packageName.id(id)
}

fun AccessibilityNodeInfo.id(id: String): String {
    if (packageName == null) {
        return id
    }
    return packageName.id(id)
}

fun CharSequence.id(id: String): String {
    return if (id.startsWith(this) || id.contains(":")) {
        id
    } else {
        "${this}:id/$id"
    }
}

/**[AccessibilityEvent]是否来自自定义的类名*/
fun AccessibilityEvent.isFromClass(cls: Class<*>): Boolean = className == cls.className()

fun AccessibilityEvent.isClassNameContains(other: CharSequence): Boolean =
    className?.covertToStr()?.contains(other, true) ?: false

fun AccessibilityEvent.isFromClass(claName: CharSequence): Boolean =
    className?.covertToStr() == claName.covertToStr()

fun AccessibilityEvent.isFromPackage(packageName: String): Boolean =
    getPackageName().toString() == packageName

/**[AccessibilityEvent]事件是否包含指定的文本信息¬*/
fun AccessibilityEvent.haveText(text: CharSequence, ignoreCase: Boolean = true): Boolean =
    getText().any { it.contains(text, ignoreCase) }

fun AccessibilityEvent.getLikeText(
    text: CharSequence,
    ignoreCase: Boolean = true
): List<CharSequence> {
    val result = mutableListOf<CharSequence>()
    getText().filterTo(result) {
        it.contains(text, ignoreCase)
    }
    return result
}

fun AccessibilityEvent.eventTypeStr(): String = AccessibilityEvent.eventTypeToString(eventType)

fun AccessibilityEvent.contentChangeTypesStr(): String = when (contentChangeTypes) {
    AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION -> "CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_SUBTREE -> "CONTENT_CHANGE_TYPE_SUBTREE"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT -> "CONTENT_CHANGE_TYPE_TEXT"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_TITLE -> "CONTENT_CHANGE_TYPE_PANE_TITLE"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_UNDEFINED -> "CONTENT_CHANGE_TYPE_UNDEFINED"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_APPEARED -> "CONTENT_CHANGE_TYPE_PANE_APPEARED"
    AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_DISAPPEARED -> "CONTENT_CHANGE_TYPE_PANE_DISAPPEARED"
    else -> Integer.toHexString(contentChangeTypes)
}


//</editor-fold desc="AccessibilityEvent扩展">

//<editor-fold desc="AccessibilityNodeInfo扩展">

fun AccessibilityNodeInfo.toRect(): Rect {
    val rect = Rect()
    getBoundsInScreen(rect)
    return rect
}

/**调用node的点击事件*/
fun AccessibilityNodeInfo.click() = performAction(AccessibilityNodeInfo.ACTION_CLICK)

/**获取焦点*/
fun AccessibilityNodeInfo.focus() = performAction(AccessibilityNodeInfo.ACTION_FOCUS)

/**[ACTION_ACCESSIBILITY_FOCUS]*/
fun AccessibilityNodeInfo.accessibilityFocus() =
    performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)

fun AccessibilityNodeInfo.longClick() = performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)

fun AccessibilityNodeInfo.setNodeText(text: CharSequence?): Boolean {
    return try {//AccessibilityNodeInfoCompat.wrap(this).text = text
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
                AccessibilityNodeInfoCompat.wrap(this).text = text
                //tip("设备不支持\n设置文本", R.drawable.lib_ic_error)
                true
            }
        }
    } catch (e: Exception) {
        L.e(e)
        false
    }
}

/**向前滚动列表*/
fun AccessibilityNodeInfo.scrollForward(): Boolean {
    return performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN)
//    } else {
//        performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
//    }
}

/**向后滚动列表*/
fun AccessibilityNodeInfo.scrollBackward(): Boolean {
    return performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP)
//    } else {
//        performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
//    }
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

fun AccessibilityNodeInfo.logNodeInfo(
    logFilePath: String? = null,
    outBuilder: StringBuilder? = null,
    logAction: Boolean = isDebugType(),
    refWidth: Int = _screenWidth,
    refHeight: Int = _screenHeight
): String {

    outBuilder?.appendln(wrap().toString())

    val t =
        "╔═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════"

    val b =
        "╚═════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════"

    outBuilder?.appendln(t)

    if (logFilePath != null) {
        RIo.appendToFile(logFilePath, "$t\n")
    }

    if (logFilePath == null && outBuilder == null) {
        L.v(t)
    }

    debugNodeInfo(0, "", logFilePath, outBuilder, logAction, refWidth, refHeight)

    if (logFilePath != null) {
        RIo.appendToFile(logFilePath, "$b\n")
    }

    if (logFilePath == null && outBuilder == null) {
        L.v(b)
    }

    outBuilder?.appendln(b)

    return outBuilder?.toString() ?: "null"
}

fun AccessibilityNodeInfo.debugNodeInfo(
    index: Int = 0 /*缩进控制*/,
    preIndex: String = "" /*child路径*/,
    logFilePath: String? = null,
    outBuilder: StringBuilder? = null,
    logAction: Boolean = false,
    refWidth: Int = _screenWidth,
    refHeight: Int = _screenHeight
) {
    fun newLine(i: Int): String {
        val sb = StringBuilder()
        for (j in 0 until i) {
            sb.append("  ")
        }
        return sb.toString()
    }

    val wrap: AccessibilityNodeInfoCompat = AccessibilityNodeInfoCompat.wrap(this)

    val stringBuilder = StringBuilder("|")

    stringBuilder.apply {
        append(newLine(index))
        append(" ${wrap.className}")
        wrap.viewIdName()?.let {
            append("(${it})")
        }

        buildString {
            if (isEnabled) {
                //append("enabled ")
            } else {
                append("disable ")
            }
            if (isClickable) {
                append("clickable ")
            }
            if (isLongClickable) {
                append("longClickable ")
            }
            if (isScrollable) {
                append("scrollable ")
            }
            if (isSelected) {
                append("selected ")
            }
            if (isPassword) {
                append("password ")
            }

            if (isCheckable) {
                append("checkable:$isChecked ")
            }
            if (isFocusable) {
                append("focusable:$isFocused ")
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

        val text = wrap.text
        val des = wrap.contentDescription

        if (text == null && des == null) {
            //2个都是空, 节省log数据
        } else {
            append(" [${wrap.text}] [${wrap.contentDescription}]")
        }
        wrap.hintText?.apply {
            append(" hintText:[${this}]")
        }
        wrap.paneTitle?.apply {
            append(" paneTitle:[${this}]")
        }
        wrap.tooltipText?.apply {
            append(" tooltipText:[${this}]")
        }

        if (childCount > 0) {
            append(" $childCount")
        }
    }

    //在父布局中的位置
    getBoundsInParent(tempRect)
    stringBuilder.append(" pr:$tempRect")

    //在屏幕中的位置
    getBoundsInScreen(tempRect)
    stringBuilder.append(" sr:$tempRect")

    //宽高
    stringBuilder.append("[${tempRect.width()}x${tempRect.height()}]")

    //在屏幕中的位置比例
    stringBuilder.append("(${tempRect.left * 1f / refWidth},${tempRect.top * 1f / refHeight}")
    stringBuilder.append("~")
    stringBuilder.append("${tempRect.right * 1f / refWidth},${tempRect.bottom * 1f / refHeight}")
    stringBuilder.append(":${tempRect.width() * 1f / refWidth}")
    stringBuilder.append(":${tempRect.height() * 1f / refHeight}")
    stringBuilder.append(")")

    //节点路径 path (2020-07-03 已经不需要了)
    //stringBuilder.append(" $preIndex")

    //可执行的action
    if (logAction) {
        stringBuilder.append(" ")
        wrap.actionStr(stringBuilder)
    }

    if (logFilePath != null) {
        RIo.appendToFile(logFilePath, "$stringBuilder\n")
    }

    if (logFilePath == null && outBuilder == null) {
        L.v("$stringBuilder")
    }

    outBuilder?.appendln("$stringBuilder")

    for (i in 0 until childCount) {
        getChild(i)?.let {
            it.debugNodeInfo(
                index + 1,
                "${if (preIndex.isEmpty()) preIndex else "${preIndex}_"}$i",
                logFilePath,
                outBuilder,
                logAction,
                refWidth,
                refHeight
            )
        }
    }
}

/**获取[AccessibilityNodeInfo]对应的id名,
 * 通常会是这样的: com.ss.android.ugc.aweme:id/afy*/
fun AccessibilityNodeInfo.viewIdName() = wrap().viewIdName()

fun AccessibilityNodeInfoCompat.viewIdName() = viewIdResourceName

fun AccessibilityNodeInfo.wrap() = AccessibilityNodeInfoCompat.wrap(this)

fun List<AccessibilityNodeInfoCompat>.toUnwrapList() =
    mapTo(ArrayList<AccessibilityNodeInfo>()) { it.unwrap() }

fun List<AccessibilityNodeInfo>.toWrapList() =
    mapTo(ArrayList<AccessibilityNodeInfoCompat>()) { it.wrap() }

/**获取有文本的文本*/
fun AccessibilityNodeInfoCompat.text() =
    text ?: (contentDescription ?: (hintText ?: (paneTitle ?: (tooltipText ?: null))))

fun AccessibilityNodeInfo.log() {
    L.v(wrap().toString())
}

/**枚举查找[AccessibilityNodeInfo]
 * [predicate] 返回1, 表示添加并继续查找; 返回0, 添加并返回,结束find; 返回-1, 不添加并继续查找; 返回-2, 不添加并结束find
 * */
fun AccessibilityNodeInfo.findNode(
    result: MutableList<AccessibilityNodeInfoCompat> = mutableListOf(),
    predicate: (node: AccessibilityNodeInfoCompat) -> Int
): List<AccessibilityNodeInfoCompat> {

    for (i in 0 until childCount) {
        val child: AccessibilityNodeInfo? = getChild(i)
        if (child != null) {
            try {
                val wrap: AccessibilityNodeInfoCompat = child.wrap()
                val check: Int = predicate(wrap)

                if (check >= 0) {
                    result.add(wrap)
                    if (check == 0) {
                        //结束find
                        return result
                    }
                }

                if (check == -2) {
                    //结束find
                    return result
                }

                //继续查找
                child.findNode(result, predicate)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    return result
}

/**根据[AccessibilityNodeInfo]出现过的文本信息查找*/
fun AccessibilityNodeInfo.findNodeByText(text: CharSequence): List<AccessibilityNodeInfoCompat> {
    return findNode {
        if (it.haveText(text)) {
            1
        } else {
            -1
        }
    }
}

fun AccessibilityNodeInfoCompat.haveText(text: CharSequence, ignoreCase: Boolean = true): Boolean {

    val thisText = this.text
    val contentDescription = contentDescription
    val paneTitle = paneTitle
    val hintText = hintText
    val tooltipText = tooltipText

//    val tc: Boolean = thisText?.contains(text, ignoreCase) == true
//    val cdc: Boolean = contentDescription?.contains(text, ignoreCase) == true
//    val ptc: Boolean = paneTitle?.contains(text, ignoreCase) == true
//    val htc: Boolean = hintText?.contains(text, ignoreCase) == true
//    val ttc: Boolean = tooltipText?.contains(text, ignoreCase) == true

//    return thisText?.contains(text, ignoreCase) == true ||
//            contentDescription?.contains(text, ignoreCase) == true ||
//            paneTitle?.contains(text, ignoreCase) == true ||
//            hintText?.contains(text, ignoreCase) == true ||
//            tooltipText?.contains(text, ignoreCase) == true

    val regex = text.toString().toRegex()

//    val tc: Boolean = thisText?.contains(regex) == true
//    val cdc: Boolean = contentDescription?.contains(regex) == true
//    val ptc: Boolean = paneTitle?.contains(regex) == true
//    val htc: Boolean = hintText?.contains(regex) == true
//    val ttc: Boolean = tooltipText?.contains(regex) == true
//
//    return tc || cdc || ptc || htc || ttc

    return thisText?.contains(regex) == true ||
            contentDescription?.contains(regex) == true ||
            paneTitle?.contains(regex) == true ||
            hintText?.contains(regex) == true ||
            tooltipText?.contains(regex) == true
}

/**返回[Node]在屏幕中的位置坐标*/
fun AccessibilityNodeInfoCompat.bounds(): Rect {
    getBoundsInScreen(tempRect)
    return tempRect
}

fun AccessibilityNodeInfoCompat.isClass(claName: CharSequence) =
    className?.toString() == claName.toString()

fun AccessibilityNodeInfoCompat.isTextView() = isClass("android.widget.TextView")
fun AccessibilityNodeInfoCompat.isEditText() = isClass("android.widget.EditText")
fun AccessibilityNodeInfoCompat.isImageView() = isClass("android.widget.ImageView")
fun AccessibilityNodeInfoCompat.isButton() = isClass("android.widget.Button")
fun AccessibilityNodeInfoCompat.isCheckBox() = isClass("android.widget.CheckBox")

fun AccessibilityNodeInfoCompat.click() = unwrap().click()
fun AccessibilityNodeInfoCompat.focus() = unwrap().focus()
fun AccessibilityNodeInfoCompat.longClick() = unwrap().longClick()
fun AccessibilityNodeInfoCompat.scrollForward() = unwrap().scrollForward()
fun AccessibilityNodeInfoCompat.scrollBackward() = unwrap().scrollBackward()
fun AccessibilityNodeInfoCompat.setNodeText(text: CharSequence?) = unwrap().setNodeText(text)

/**是一个有效的Node*/
fun AccessibilityNodeInfoCompat.isValid(): Boolean {
    var result = false
    val bound = bounds()

    if (bound.width() > 0 &&
        bound.height() > 0
    ) {
        result = true
    }

    return result
}

fun AccessibilityNodeInfoCompat.isLayout() =
    className?.toString()?.contains("Layout", true) ?: false

inline fun AccessibilityNodeInfoCompat.eachChild(action: (index: Int, child: AccessibilityNodeInfoCompat) -> Unit) {
    for (index in 0 until childCount) {
        action(index, getChild(index))
    }
}

fun AccessibilityNodeInfoCompat.getChildOrNull(index: Int): AccessibilityNodeInfoCompat? {
    return if (index in 0 until childCount) {
        getChild(index)
    } else {
        null
    }
}

/**获取当前[Node], 可以点击的[父Node]*/
fun AccessibilityNodeInfoCompat.getClickParent(): AccessibilityNodeInfoCompat? {
    return if (isClickable) {
        this
    } else {
        parent?.getClickParent()
    }
}

fun AccessibilityNodeInfoCompat.getLongClickParent(): AccessibilityNodeInfoCompat? {
    return if (isLongClickable) {
        this
    } else {
        parent?.getLongClickParent()
    }
}

/**获取自身的兄弟节点
 * [index] >0 表示获取自身下面的第几个兄弟; <0 表示获取自身上面的第几个兄弟
 * */
fun AccessibilityNodeInfoCompat.getBrotherNode(index: Int): AccessibilityNodeInfoCompat? {
    val beforeList = mutableListOf<AccessibilityNodeInfoCompat>()
    val afterList = mutableListOf<AccessibilityNodeInfoCompat>()

    var findAnchor = false

    parent?.eachChild { _, child ->
        if (child == this) {
            findAnchor = true
        } else {
            if (findAnchor) {
                afterList.add(child)
            } else {
                beforeList.add(child)
            }
        }
    }

    return when {
        index > 0 -> afterList.getOrNull(index - 1)
        index < 0 -> beforeList.getOrNull(beforeList.size + index)
        else -> this
    }
}

/**获取自身的child/parent节点
 * [index] >0 表示获取自身下面的第几个child; <0 表示获取自身上面的第几个parent
 * */
fun AccessibilityNodeInfoCompat.getParentOrChildNode(index: Int): AccessibilityNodeInfoCompat? {

    if (index > 0) {
        return getChildOrNull(index - 1)
    }

    if (index == 0) {
        return this
    }

    var target: AccessibilityNodeInfoCompat? = this
    for (i in 1..index.abs()) {
        target = target?.parent
    }
    return target
}

//</editor-fold desc="AccessibilityNodeInfo扩展">