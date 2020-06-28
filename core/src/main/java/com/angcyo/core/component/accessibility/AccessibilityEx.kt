package com.angcyo.core.component.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Path
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.covertToStr
import com.angcyo.http.RIo
import com.angcyo.library.L
import com.angcyo.library.ex.className
import com.angcyo.library.ex.copy

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */


@SuppressLint("MissingPermission")
fun Context.kill(packageName: String) {
    val am: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    am.killBackgroundProcesses(packageName)

//    val infos = am.runningAppProcesses
//    for (info in infos) {
//        if (info.processName == packageName) {
//            android.os.Process.killProcess(info.pid)
//        }
//    }
}

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

fun AccessibilityService.rootNodeInfo(event: AccessibilityEvent? = null): AccessibilityNodeInfo? {
    val activeWindow = windows.find { it.isActive && it.isFocused }
    return when {
        activeWindow?.root != null -> activeWindow.root
        rootInActiveWindow == null -> event?.source
        else -> rootInActiveWindow
    }
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

/**
 * [id] id/button1
 *
 * -> button1
 * */
fun AccessibilityService.findNodeById(
    id: String,
    event: AccessibilityEvent? = null
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

/**
 * 相当于按返回键
 * */
fun AccessibilityService.back() {
    //api 16
    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
}

fun AccessibilityService.home() {
    //api 16
    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
}

fun AccessibilityService.recents() {
    //api 16
    performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
}

/**锁屏*/
fun AccessibilityService.lockScreen() {
    //api 28
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        this.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
    }
}

/**屏幕截图*/
fun AccessibilityService.takeScreenShot() {
    //api 28
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
    }
}

/**执行[fling]操作*/
fun AccessibilityService.fling(path: Path, result: GestureResult = { _, _ -> }) {
    val pathsList = mutableListOf<Path>()
    pathsList.add(path)

    val paths = pathsList.toTypedArray()
    val startTImeList = mutableListOf<Long>()
    val durationList = mutableListOf<Long>()

    val DEFAULT_START_TIME = 20L
    val DEFAULT_DURATION = 1000L
    paths.mapIndexed { index, _ ->
        startTImeList.add((index + 1) * DEFAULT_START_TIME + index * DEFAULT_DURATION)
        durationList.add(DEFAULT_DURATION)
    }

    touch(paths, startTImeList.toLongArray(), durationList.toLongArray(), result)
}

fun AccessibilityService.move(
    fromX: Int, fromY: Int,
    toX: Int, toY: Int,
    result: GestureResult = { _, _ -> }
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //api 24 android 7
        move(
            fromX.toFloat(), fromY.toFloat(),
            toX.toFloat(), toY.toFloat(),
            result
        )
    } else {
        result(null, true)
    }
}

fun AccessibilityService.move(
    fromX: Float, fromY: Float,
    toX: Float, toY: Float,
    result: GestureResult = { _, _ -> }
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //api 24 android 7
        move(PointF(fromX, fromY), PointF(toX, toY), result)
    } else {
        result(null, true)
    }
}

fun AccessibilityService.move(from: PointF, to: PointF, result: GestureResult = { _, _ -> }) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //api 24 android 7
        move(Path().apply { moveTo(from.x, from.y);lineTo(to.x, to.y) }, result)
    } else {
        result(null, true)
    }
}

/**执行[move]操作*/
fun AccessibilityService.move(path: Path, result: GestureResult = { _, _ -> }) {
    val pathsList = mutableListOf<Path>()
    pathsList.add(path)

    val paths = pathsList.toTypedArray()
    val startTImeList = mutableListOf<Long>()
    val durationList = mutableListOf<Long>()

    val DEFAULT_START_TIME = 20L
    val DEFAULT_DURATION = 300L
    paths.mapIndexed { index, _ ->
        startTImeList.add((index + 1) * DEFAULT_START_TIME + index * DEFAULT_DURATION)
        durationList.add(DEFAULT_DURATION)
    }

    touch(paths, startTImeList.toLongArray(), durationList.toLongArray(), result)
}

/**
 * 通过 touch 坐标, 触发点击事件
 * */
fun AccessibilityService.touch(vararg path: Path) {
    touch(40, 160, *path)
}

/**双击*/
fun AccessibilityService.double(path: Path) {
    touch(40, 200, path, Path(path))
}

fun AccessibilityService.touch(startTime: Int, duration: Int, vararg path: Path) {
    val pathsList = mutableListOf<Path>()
    pathsList.addAll(path)

    val paths = pathsList.toTypedArray()
    val startTImeList = mutableListOf<Long>()
    val durationList = mutableListOf<Long>()

    paths.mapIndexed { index, _ ->
        startTImeList.add(((index + 1) * startTime + index * duration).toLong())
        durationList.add(duration.toLong())
    }

    //L.i("touch: $startTImeList $durationList")
    touch(paths, startTImeList.toLongArray(), durationList.toLongArray())
}

fun AccessibilityService.touch(point: Point) {
    touch(PointF(point))
}

fun AccessibilityService.touch(point: PointF) {
    touch(Path().apply {
        moveTo(point.x, point.y)
    })
}

fun AccessibilityService.touch(vararg path: Path, result: GestureResult = { _, _ -> }) {
    val pathsList = mutableListOf<Path>()
    pathsList.addAll(path)

    val paths = pathsList.toTypedArray()
    val startTImeList = mutableListOf<Long>()
    val durationList = mutableListOf<Long>()

    val DEFAULT_START_TIME = 700L
    val DEFAULT_DURATION = 300L
    paths.mapIndexed { index, _ ->
        startTImeList.add((index + 1) * DEFAULT_START_TIME + index * DEFAULT_DURATION)
        durationList.add(DEFAULT_DURATION)
    }

    touch(paths, startTImeList.toLongArray(), durationList.toLongArray(), result)
}

/**
 * @param paths 需要点击的位置
 * @param touchInterval 每次点击 间隔时长
 * @param touchDuration 每次点击持续时长
 *
 * */
fun AccessibilityService.touch(
    paths: Array<Path>,
    touchInterval: Long,
    touchDuration: Long
) {
    if (paths.isEmpty()) {
        return
    }

    val intervalList = mutableListOf<Long>()
    val durationList = mutableListOf<Long>()
    paths.mapIndexed { index, path ->
        intervalList.add(touchInterval * (index + 1) + touchDuration * index)
        durationList.add(touchDuration)
    }

    touch(paths, intervalList.toLongArray(), durationList.toLongArray())
}

/**
 * 执行手势
 * */
fun AccessibilityService.touch(
    paths: Array<Path>,
    startTime: LongArray,
    duration: LongArray,
    result: GestureResult = { _, _ -> }
) {
    if (paths.isEmpty()) {
        result(null, true)
        return
    }

    //api 24 android 7
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        GestureDescription.Builder().apply {
            paths.mapIndexed { index, path ->
                this.addStroke(
                    GestureDescription.StrokeDescription(
                        path,
                        startTime[index],
                        duration[index]
                    )
                )
            }
            this@touch.dispatchGesture(
                this.build(),
                object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        super.onCompleted(gestureDescription)
                        L.e("$gestureDescription strokeCount:${gestureDescription?.strokeCount}")
                        result(gestureDescription, false)
                    }

                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        super.onCancelled(gestureDescription)
                        L.e("$gestureDescription strokeCount:${gestureDescription?.strokeCount}")
                        result(gestureDescription, true)
                    }
                },
                null
            )
        }
    } else {
        result(null, true)
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

    return if (id.contains(packageName)) {
        id
    } else {
        "${packageName}:id/$id"
    }
}

/**[AccessibilityEvent]是否来自自定义的类名*/
fun AccessibilityEvent.isFromClass(cls: Class<*>): Boolean = className == cls.className()

fun AccessibilityEvent.isClassNameContains(other: CharSequence): Boolean =
    className?.covertToStr()?.contains(other, true) ?: false

fun AccessibilityEvent.isFromClass(claName: CharSequence): Boolean = className == claName

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

fun AccessibilityNodeInfo.setNodeText(text: CharSequence?) {
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
            AccessibilityNodeInfoCompat.wrap(this).text = text
            //tip("设备不支持\n设置文本", R.drawable.lib_ic_error)
        }
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
    outBuilder: StringBuilder? = null
): String {

    outBuilder?.appendln(wrap().toString())

    val t =
        "╔═══════════════════════════════════════════════════════════════════════════════════════"

    val b =
        "╚═══════════════════════════════════════════════════════════════════════════════════════"

    outBuilder?.appendln(t)

    if (logFilePath != null) {
        RIo.appendToFile(logFilePath, "$t\n")
    }

    if (logFilePath == null && outBuilder == null) {
        L.v(t)
    }

    debugNodeInfo(0, "", logFilePath, outBuilder)

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
    outBuilder: StringBuilder? = null
) {
    fun newLine(i: Int): String {
        val sb = StringBuilder()
        for (j in 0 until i) {
            sb.append("    ")
        }
        return sb.toString()
    }

    val wrap = AccessibilityNodeInfoCompat.wrap(this)

    val stringBuilder = StringBuilder("|")
    stringBuilder.append(newLine(index))
    stringBuilder.append(" ${wrap.className}(${wrap.viewIdResourceName})")
    stringBuilder.append(" c:${isClickable}")
    stringBuilder.append(" s:${isSelected}")
    stringBuilder.append(" ck:${isChecked}")
    stringBuilder.append(" [${wrap.text}]")
    stringBuilder.append(" $childCount")

    val rect = Rect()
    getBoundsInScreen(rect)
    stringBuilder.append(" $rect")
    stringBuilder.append(" $preIndex")

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
                outBuilder
            )
        }
    }
}

fun AccessibilityNodeInfo.wrap() = AccessibilityNodeInfoCompat.wrap(this)

fun AccessibilityNodeInfo.log() {
    val wrap = AccessibilityNodeInfoCompat.wrap(this)
    L.v(wrap.toString())
}

//</editor-fold desc="AccessibilityNodeInfo扩展">
