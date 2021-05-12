package com.angcyo.library.ex

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

//临时变量
val tempRect = Rect()

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

fun sleep(delay: Long = 160, action: Action? = null) {
    try {
        if (delay > 0) {
            Thread.sleep(delay)
        }
    } finally {
        action?.invoke()
    }
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
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
        wm.defaultDisplay.getRealSize(point)
    }
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
fun AccessibilityService.takeScreenshot(): Boolean {
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

fun AccessibilityNodeInfoCompat.id(id: String): String {
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

fun CharSequence?.des(): String {
    return if (this.isNullOrEmpty()) {
        ""
    } else {
        "($this)"
    }
}

fun CharSequence?.des2(): String {
    return if (this.isNullOrEmpty()) {
        ""
    } else {
        "[$this]"
    }
}

fun CharSequence?.appendDes(des: String?): String? {
    return if (this.isNullOrEmpty() || des.isNullOrEmpty()) {
        this?.toString()
    } else {
        "$this[$des]"
    }
}

/**[AccessibilityEvent]是否来自自定义的类名*/
fun AccessibilityEvent.isFromClass(cls: Class<*>): Boolean = className == cls.className()

fun AccessibilityEvent.isClassNameContains(other: CharSequence): Boolean =
    className?.toString()?.contains(other, true) ?: false

fun AccessibilityEvent.isFromClass(claName: CharSequence): Boolean =
    className?.toString() == claName.toString()

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
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val arguments = Bundle()
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

/**返回所有节点信息*/
fun AccessibilityNodeInfo.logAllNode(): String {
    val builder = StringBuilder()
    logNodeInfo(null, builder, false)
    return builder.toString()
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
        logFilePath.writeText("$t\n")
    }

    if (logFilePath == null && outBuilder == null) {
        L.v(t)
    }

    debugNodeInfo(0, "", logFilePath, outBuilder, logAction, refWidth, refHeight)

    if (logFilePath != null) {
        logFilePath.writeText("$b\n")
    }

    if (logFilePath == null && outBuilder == null) {
        L.v(b)
    }

    outBuilder?.appendln(b)

    return outBuilder?.toString() ?: "null"
}

fun newLineIndent(n: Int, ch: CharSequence = " "): String {
    return ch.repeat(n)
    /*val sb = StringBuilder()
    for (j in 0 until n) {
        sb.append(" ")
    }
    return sb.toString()*/
}

fun AccessibilityNodeInfo.debugNodeInfo(
    index: Int = 0 /*缩进控制*/,
    preIndex: String = "" /*child路径*/,
    logFilePath: String? = null,
    outBuilder: StringBuilder? = null,
    logAction: Boolean = false,
    refWidth: Int = _screenWidth,
    refHeight: Int = _screenHeight,
    logChild: Boolean = true
) {
    val wrap: AccessibilityNodeInfoCompat = AccessibilityNodeInfoCompat.wrap(this)

    val stringBuilder = StringBuilder("") //"|"

    stringBuilder.apply {
        append(newLineIndent(index))
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

            if (isCheckable) {
                append("checkable:$isChecked ")
            }
            if (isFocusable) {
                append("focusable:$isFocused ")
            }

            if (isPassword) {
                append("password ")
            }
            if (isVisibleToUser) {
                append("visibleToUser ")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (isDismissable) {
                    append("dismissable ")
                }
                if (isEditable) {
                    append("editable ")
                }
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
    val width = tempRect.width()
    val height = tempRect.height()
    stringBuilder.append("[${width}x$height]")
    stringBuilder.append("[${width / dp}x${height / dp}]")

    //在屏幕中的位置比例
    stringBuilder.append("(${tempRect.left * 1f / refWidth},${tempRect.top * 1f / refHeight}")
    stringBuilder.append("~")
    stringBuilder.append("${tempRect.right * 1f / refWidth},${tempRect.bottom * 1f / refHeight}")
    stringBuilder.append(" :${width * 1f / refWidth}")
    stringBuilder.append(" :${height * 1f / refHeight}")
    stringBuilder.append(")")

    //节点路径 path (2020-07-03 已经不需要了)
    //stringBuilder.append(" $preIndex")

    //可执行的action
    if (logAction) {
        stringBuilder.append(" ")
        wrap.actionStr(stringBuilder)
    }

    if (logFilePath != null) {
        logFilePath.writeText("$stringBuilder\n")
    }

    if (logFilePath == null && outBuilder == null) {
        L.v("$stringBuilder")
    }

    outBuilder?.appendln("$stringBuilder")

    if (logChild) {
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
    text ?: (contentDescription ?: (hintText ?: (paneTitle ?: tooltipText)))

/**枚举查找[AccessibilityNodeInfo]
 * [deep] 节点查询深度, >=0生效, 0:表示根节点 1:表示根节点+child
 * [currentDeep] 当前的深度, 自动控制的变量
 * [predicate] 返回1, 表示添加并继续查找; 返回0, 添加并返回,结束find; 返回-1, 不添加并继续查找; 返回-2, 不添加并结束find
 * */
fun AccessibilityNodeInfo.findNode(
    result: MutableList<AccessibilityNodeInfoCompat> = mutableListOf(),
    deep: Int = -1,
    currentDeep: Int = 0,
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
                if (deep in 0..currentDeep) {
                    //已达到深度
                    continue
                } else {
                    child.findNode(result, deep, currentDeep + 1, predicate)
                }
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

    val thisText: CharSequence? = this.text
    val contentDescription: CharSequence? = contentDescription
    val paneTitle: CharSequence? = paneTitle
    val hintText: CharSequence? = hintText
    val tooltipText: CharSequence? = tooltipText

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

//    val regex = text.toString().toRegex()

//    val tc: Boolean = thisText?.contains(regex) == true
//    val cdc: Boolean = contentDescription?.contains(regex) == true
//    val ptc: Boolean = paneTitle?.contains(regex) == true
//    val htc: Boolean = hintText?.contains(regex) == true
//    val ttc: Boolean = tooltipText?.contains(regex) == true
//
//    return tc || cdc || ptc || htc || ttc

//    return thisText?.contains(regex) == true ||
//            contentDescription?.contains(regex) == true ||
//            paneTitle?.contains(regex) == true ||
//            hintText?.contains(regex) == true ||
//            tooltipText?.contains(regex) == true

    return thisText.have(text) ||
            contentDescription.have(text) ||
            paneTitle.have(text) ||
            hintText.have(text) ||
            tooltipText.have(text)
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

fun AccessibilityNodeInfoCompat.click() = performAction(AccessibilityNodeInfo.ACTION_CLICK)
fun AccessibilityNodeInfoCompat.focus() = performAction(AccessibilityNodeInfo.ACTION_FOCUS)
fun AccessibilityNodeInfoCompat.longClick() = performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
fun AccessibilityNodeInfoCompat.scrollForward() =
    performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)

fun AccessibilityNodeInfoCompat.scrollBackward() =
    performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)

fun AccessibilityNodeInfoCompat.setNodeText(text: CharSequence?): Boolean {
    return try {//AccessibilityNodeInfoCompat.wrap(this).text = text
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val arguments = Bundle()
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
                this.text = text
                //tip("设备不支持\n设置文本", R.drawable.lib_ic_error)
                true
            }
        }
    } catch (e: Exception) {
        L.e(e)
        false
    }
}

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

fun AccessibilityNodeInfoCompat.getScrollableParent(): AccessibilityNodeInfoCompat? {
    return if (isScrollable) {
        this
    } else {
        parent?.getScrollableParent()
    }
}

fun AccessibilityNodeInfoCompat.getFocusableParent(): AccessibilityNodeInfoCompat? {
    return if (isFocusable) {
        this
    } else {
        parent?.getFocusableParent()
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

inline fun AccessibilityNodeInfoCompat.forEachChild(action: (child: AccessibilityNodeInfoCompat) -> Unit) {
    for (i in 0 until childCount) {
        action(getChild(i))
    }
}


fun AccessibilityNodeInfoCompat.getBrotherNodePrev(matchStateAction: (AccessibilityNodeInfoCompat) -> Boolean): AccessibilityNodeInfoCompat? {
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

    beforeList.reversed().forEach {
        if (matchStateAction(it)) {
            return it
        }
    }

    return null
}

fun AccessibilityNodeInfoCompat.getBrotherNodeNext(matchStateAction: (AccessibilityNodeInfoCompat) -> Boolean): AccessibilityNodeInfoCompat? {
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

    afterList.forEach {
        if (matchStateAction(it)) {
            return it
        }
    }

    return null
}

fun AccessibilityNodeInfoCompat.getParentOrChildNodeUp(matchStateAction: (AccessibilityNodeInfoCompat) -> Boolean): AccessibilityNodeInfoCompat? {
    var target: AccessibilityNodeInfoCompat? = this
    do {
        val parent = target?.parent ?: return null
        if (matchStateAction(parent)) {
            return parent
        }
        target = parent
    } while (target != null)
    return null
}

fun AccessibilityNodeInfoCompat.getParentOrChildNodeDown(matchStateAction: (AccessibilityNodeInfoCompat) -> Boolean): AccessibilityNodeInfoCompat? {
    forEachChild {
        if (matchStateAction(it)) {
            return it
        }
    }
    return null
}

//</editor-fold desc="AccessibilityNodeInfo扩展">

/**[android.view.accessibility.AccessibilityWindowInfo#typeToString]*/
fun Int.toWindowTypeStr(): String {
    return when (this) {
        AccessibilityWindowInfo.TYPE_APPLICATION -> {
            "TYPE_APPLICATION"
        }
        AccessibilityWindowInfo.TYPE_INPUT_METHOD -> {
            "TYPE_INPUT_METHOD"
        }
        AccessibilityWindowInfo.TYPE_SYSTEM -> {
            "TYPE_SYSTEM"
        }
        AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY -> {
            "TYPE_ACCESSIBILITY_OVERLAY"
        }
        AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER -> {
            "TYPE_SPLIT_SCREEN_DIVIDER"
        }
        else -> "<UNKNOWN>"
    }
}