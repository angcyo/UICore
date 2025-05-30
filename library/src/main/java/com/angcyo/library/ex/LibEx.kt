package com.angcyo.library.ex

import android.app.Activity
import android.app.Application
import android.content.pm.ApplicationInfo
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Debug
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.collection.SimpleArrayMap
import com.angcyo.library.*
import com.angcyo.library.component.RBackground
import com.angcyo.library.component.ThreadExecutor.onMain
import com.angcyo.library.component.lastPackageName
import com.angcyo.library.utils.Device
import com.angcyo.library.utils.RUtils
import com.angcyo.library.utils.Reflect
import com.angcyo.library.utils.protector.EmulatorCheckUtil
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random.Default.nextInt

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**函数别名*/
typealias Action = () -> Unit
typealias Action1 = (Any?) -> Unit
typealias Action2 = (Any?, Any?) -> Unit
typealias ClickAction = (View) -> Unit
typealias ViewAction = ClickAction
typealias TouchAction = (ev: MotionEvent) -> Boolean
typealias BooleanAction = (Boolean) -> Unit
typealias IntAction = (Int) -> Unit
typealias StringAction = (text: CharSequence?) -> Unit
typealias MatrixAction = (Matrix?) -> Unit
typealias UriAction = (uri: Uri?, exception: Exception?) -> Unit

/**别名*/
typealias ResultThrowable = (error: Throwable?) -> Unit

/**反射获取[Application]对象
 * [android.app.ActivityThread#currentApplication]*/
fun currentApplication(): Application? {
    return try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val method = activityThreadClass.getMethod("currentApplication")
        method.invoke(null) as Application?
    } catch (e: Exception) {
        null
    }
}

/**
 * [android.app.ActivityThread#currentActivityThread]
 * [android.app.ActivityThread#mActivities]
 * [android.app.ActivityThread.ActivityClientRecord#paused]
 * */
fun currentActivity(): Activity? {
    val activityThreadClass: Class<*>?
    try {
        activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
        val activitiesField = activityThreadClass.getDeclaredField("mActivities")
        activitiesField.isAccessible = true
        val activities = activitiesField[activityThread] as Map<*, *>
        var lastActivity: Activity? = RBackground.lastActivityRef?.get()
        for (activityRecord in activities.values) {
            if (activityRecord != null) {
                val activityRecordClass: Class<*> = activityRecord.javaClass
                val pausedField = activityRecordClass.getDeclaredField("paused")
                pausedField.isAccessible = true

                val activityField = activityRecordClass.getDeclaredField("activity")
                activityField.isAccessible = true
                lastActivity = activityField[activityRecord] as Activity

                if (!pausedField.getBoolean(activityRecord)) {
                    //paused == false
                    return lastActivity
                }
            }
        }
        return lastActivity
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: NoSuchFieldException) {
        e.printStackTrace()
    }
    return null
}

fun threadName() = Thread.currentThread().name

fun isAndroidPlatform() = "Dalvik" == System.getProperty("java.vm.name")

//1.8
fun javaVersion() = System.getProperty("java.specification.version", "unknown")

fun isRelease(): Boolean = "release".equals(BuildConfig.BUILD_TYPE, true) && !isAppDebug()

fun isDebugType() = Library.isDebugTypeVal || "debug".equals(BuildConfig.BUILD_TYPE, true)

/**[BuildConfig.BUILD_TYPE]
 * [isBuildDebug]*/
fun String.isDebugType() = "debug".equals(this, true)

/**[BuildConfig.BUILD_TYPE]*/
fun String.isBuildDebug() = "debug".equals(this, true)

fun isMac() = getAppString("os_name")?.toLowerCase()?.contains("mac") == true

/** resValue "bool", "show_debug", "true" */
fun isShowDebug() = getAppBoolean("show_debug") == true

/** resValue "bool", "is_preview", "true" */
fun isPreview() = getAppBoolean("is_preview") == true

/** resValue "bool", "is_debug", "true" */
fun isDebugRes() = getAppBoolean("is_debug") == true

/**库打包成aar之后, [BuildConfig.DEBUG] 是 release*/
fun isDebug() = BuildConfig.DEBUG || isShowDebug() || isAppDebug() || isDebugType()

/**[ApplicationInfo.FLAG_DEBUGGABLE]*/
fun isAppDebug() = if (app().isPlaceholderApplication()) {
    false
} else {
    RUtils.isAppDebug()
}

/**[BuildConfig.BUILD_TYPE]*/
fun isAppRelease() =
    Reflect.getStaticField("${lastPackageName}.BuildConfig", "BUILD_TYPE")?.toString() == "release"

fun isRoot() = RUtils.isRoot()
fun isXposedExistByThrow() = RUtils.isXposedExistByThrow()
fun isRootUI() = RUtils.isRootUI()
fun isProxyUsed() = Device.isProxyUsed()
fun isVpnUsed() = Device.isVpnUsed()

/**判断应用是否多开*/
fun isMultiApp(packageName: String) = RUtils.checkByOriginApkPackageName(packageName = packageName)

//运行在模拟器中
fun isRunningInEmulator() = EmulatorCheckUtil.singleInstance.readSysProperty()

fun Float?.abs() = kotlin.math.abs(this ?: 0f)

fun Int.abs() = kotlin.math.abs(this)

fun Any?.hash(): String? {
    return this?.hashCode()?.run { Integer.toHexString(this) }
}

/**[DslButton(677fb48)]*/
fun Any.simpleHash(): String {
    return "${this.simpleClassName()}(${this.hash()})"
}

/**[com.angcyo.widget.DslButton(677fb48)]*/
fun Any.classHash(): String {
    return "${this.className()}(${this.hash()})"
}

/**[DslButton]*/
fun Any.simpleClassName(): String {
    if (this is Class<*>) {
        return this.simpleName
    }
    return this.javaClass.simpleName
}

/**[com.angcyo.widget.DslButton]*/
fun Any.className(): String {
    if (this is Class<*>) {
        return this.name
    }
    return this.javaClass.name
}

/**如果为空, 则执行[action].
 * 原样返回*/
fun <T> T?.elseNull(action: () -> Unit = {}): T? {
    if (this == null) {
        action()
    }
    return this
}

/**如果[this]是null或空, 则返回[or], 否则返回[this]*/
fun CharSequence?.elseBlank(or: CharSequence?): CharSequence? {
    if (this.isNullOrBlank()) {
        return or
    }
    return this
}

fun Any?.string(def: CharSequence = ""): CharSequence {
    return when {
        this == null -> return def
        this is TextView -> text ?: def
        this is CharSequence -> this
        else -> this.toString()
    }
}

fun Any?.str(def: String = ""): String {
    return if (this == null) {
        return def
    } else if (this is String) {
        this
    } else {
        this.toString()
    }
}

/**
 *
 * https://developer.android.google.cn/reference/java/util/Formatter.html#syntax
 * */
fun Any?.format(format: String): String {
    return String.format(format, this)
}

fun Throwable.string(): String {
    val stringWriter = StringWriter()
    val pw = PrintWriter(BufferedWriter(stringWriter))
    pw.use {
        printStackTrace(it)
    }
    return stringWriter.toString()
}

/**堆栈信息保存到文件*/
fun Throwable.saveTo(filePath: String, append: Boolean = true) {
    val pw = PrintWriter(BufferedWriter(FileWriter(filePath, append)))
    printStackTrace(pw)
    pw.close()
}

/**等数量each两个list*/
fun <L1, L2> each(list1: List<L1>?, list2: List<L2>?, run: (item1: L1, item2: L2) -> Unit) {
    val size1 = list1?.size ?: 0
    val size2 = list2?.size ?: 0

    for (i in 0 until min(size1, size2)) {
        run(list1!![i], list2!![i])
    }
}

/**a7503205-ab31-4e67-bc89-64a129f0540b
 * [trim] 是否要移除uuid中的[-]符号*/
fun uuid(trim: Boolean = true): String {
    val result = UUID.randomUUID().toString()
    return if (trim) result.removeAll("-") else result
}

/**判断列表是否为空, 包括内部的数据也是非空*/
fun Collection<Any?>?.isListEmpty(): Boolean {
    if ((this?.size ?: -1) > 0) {
        return false
    }
    return this?.run {
        find { it != null } == null
    } ?: true
}

fun Collection<*>?.size() = this?.size ?: 0
fun Collection<*>?.nullOrEmpty() = size() == 0
fun Collection<*>?.notEmpty() = !nullOrEmpty()
fun Array<*>?.size() = this?.size ?: 0
fun ByteArray?.size() = this?.size ?: 0
fun <T> T.toListOf() = listOf(this)
fun <T> T.toArrayListOf() = arrayListOf(this)

/**判断2个列表中的数据是否改变过*/
fun <T> Collection<T>?.isChange(other: List<T>?): Boolean {
    if (this.size() != other.size()) {
        return true
    }
    this?.forEachIndexed { index, t ->
        if (t != other?.getOrNull(index)) {
            return true
        }
    }
    return false
}

/**判断2个列表是否有共同的元素*/
fun <T> Collection<T>?.isIntersect(other: List<T>?): Boolean {
    if (other.isNullOrEmpty() || this.isNullOrEmpty()) {
        return false
    }
    val find = find { other.contains(it) }
    return find != null
}

/**判断列表中的数据是否都满足此条件*/
fun <T> Iterable<T>.isAllMatch(predicate: (T) -> Boolean): Boolean {
    var isAllMatch = true
    forEach {
        if (predicate(it)) {
            isAllMatch = true
        } else {
            isAllMatch = false
            return isAllMatch
        }
    }
    return isAllMatch
}

/**将多维数组抹平成一维数组*/
fun <T> Iterable<*>.toSingleList(): List<T> {
    val result = mutableListOf<T>()
    forEach {
        if (it is Iterable<*>) {
            result.addAll(it.toSingleList())
        } else {
            result.add(it as T)
        }
    }
    return result
}

fun <T> Stack<T>.popSafe() = if (isEmpty()) null else pop()

fun <T> List<T?>?.randomGetOnce(): T? = randomGet(1).firstOrNull()

/**随机从列表中获取一组数据*/
fun <T> List<T?>?.randomGet(count: Int = nextInt(0, min(this.size(), 1))): List<T> {
    val result = mutableListOf<T>()
    for (i in 0 until count) {
        this?.getOrNull(nextInt(0, size))?.apply {
            result.add(this)
        }
    }
    return result
}

/**随机汉字*/
fun randomString(count: Int = nextInt(1, 20)): String {
    val charArray = CharArray(count)
    for (i in 0 until count) {
        charArray[i] = getRandomChar()
    }
    return String(charArray)
}

fun getRandomChar(): Char {
    val base =
        "\u7684\u4e00\u4e86\u662f\u6211\u4e0d\u5728\u4eba\u4eec\u6709\u6765\u4ed6\u8fd9\u4e0a\u7740\u4e2a\u5730\u5230\u5927\u91cc\u8bf4\u5c31\u53bb\u5b50\u5f97\u4e5f\u548c\u90a3\u8981\u4e0b\u770b\u5929\u65f6\u8fc7\u51fa\u5c0f\u4e48\u8d77\u4f60\u90fd\u628a\u597d\u8fd8\u591a\u6ca1\u4e3a\u53c8\u53ef\u5bb6\u5b66\u53ea\u4ee5\u4e3b\u4f1a\u6837\u5e74\u60f3\u751f\u540c\u8001\u4e2d\u5341\u4ece\u81ea\u9762\u524d\u5934\u9053\u5b83\u540e\u7136\u8d70\u5f88\u50cf\u89c1\u4e24\u7528\u5979\u56fd\u52a8\u8fdb\u6210\u56de\u4ec0\u8fb9\u4f5c\u5bf9\u5f00\u800c\u5df1\u4e9b\u73b0\u5c71\u6c11\u5019\u7ecf\u53d1\u5de5\u5411\u4e8b\u547d\u7ed9\u957f\u6c34\u51e0\u4e49\u4e09\u58f0\u4e8e\u9ad8\u624b\u77e5\u7406\u773c\u5fd7\u70b9\u5fc3\u6218\u4e8c\u95ee\u4f46\u8eab\u65b9\u5b9e\u5403\u505a\u53eb\u5f53\u4f4f\u542c\u9769\u6253\u5462\u771f\u5168\u624d\u56db\u5df2\u6240\u654c\u4e4b\u6700\u5149\u4ea7\u60c5\u8def\u5206\u603b\u6761\u767d\u8bdd\u4e1c\u5e2d\u6b21\u4eb2\u5982\u88ab\u82b1\u53e3\u653e\u513f\u5e38\u6c14\u4e94\u7b2c\u4f7f\u5199\u519b\u5427\u6587\u8fd0\u518d\u679c\u600e\u5b9a\u8bb8\u5feb\u660e\u884c\u56e0\u522b\u98de\u5916\u6811\u7269\u6d3b\u90e8\u95e8\u65e0\u5f80\u8239\u671b\u65b0\u5e26\u961f\u5148\u529b\u5b8c\u5374\u7ad9\u4ee3\u5458\u673a\u66f4\u4e5d\u60a8\u6bcf\u98ce\u7ea7\u8ddf\u7b11\u554a\u5b69\u4e07\u5c11\u76f4\u610f\u591c\u6bd4\u9636\u8fde\u8f66\u91cd\u4fbf\u6597\u9a6c\u54ea\u5316\u592a\u6307\u53d8\u793e\u4f3c\u58eb\u8005\u5e72\u77f3\u6ee1\u65e5\u51b3\u767e\u539f\u62ff\u7fa4\u7a76\u5404\u516d\u672c\u601d\u89e3\u7acb\u6cb3\u6751\u516b\u96be\u65e9\u8bba\u5417\u6839\u5171\u8ba9\u76f8\u7814\u4eca\u5176\u4e66\u5750\u63a5\u5e94\u5173\u4fe1\u89c9\u6b65\u53cd\u5904\u8bb0\u5c06\u5343\u627e\u4e89\u9886\u6216\u5e08\u7ed3\u5757\u8dd1\u8c01\u8349\u8d8a\u5b57\u52a0\u811a\u7d27\u7231\u7b49\u4e60\u9635\u6015\u6708\u9752\u534a\u706b\u6cd5\u9898\u5efa\u8d76\u4f4d\u5531\u6d77\u4e03\u5973\u4efb\u4ef6\u611f\u51c6\u5f20\u56e2\u5c4b\u79bb\u8272\u8138\u7247\u79d1\u5012\u775b\u5229\u4e16\u521a\u4e14\u7531\u9001\u5207\u661f\u5bfc\u665a\u8868\u591f\u6574\u8ba4\u54cd\u96ea\u6d41\u672a\u573a\u8be5\u5e76\u5e95\u6df1\u523b\u5e73\u4f1f\u5fd9\u63d0\u786e\u8fd1\u4eae\u8f7b\u8bb2\u519c\u53e4\u9ed1\u544a\u754c\u62c9\u540d\u5440\u571f\u6e05\u9633\u7167\u529e\u53f2\u6539\u5386\u8f6c\u753b\u9020\u5634\u6b64\u6cbb\u5317\u5fc5\u670d\u96e8\u7a7f\u5185\u8bc6\u9a8c\u4f20\u4e1a\u83dc\u722c\u7761\u5174\u5f62\u91cf\u54b1\u89c2\u82e6\u4f53\u4f17\u901a\u51b2\u5408\u7834\u53cb\u5ea6\u672f\u996d\u516c\u65c1\u623f\u6781\u5357\u67aa\u8bfb\u6c99\u5c81\u7ebf\u91ce\u575a\u7a7a\u6536\u7b97\u81f3\u653f\u57ce\u52b3\u843d\u94b1\u7279\u56f4\u5f1f\u80dc\u6559\u70ed\u5c55\u5305\u6b4c\u7c7b\u6e10\u5f3a\u6570\u4e61\u547c\u6027\u97f3\u7b54\u54e5\u9645\u65e7\u795e\u5ea7\u7ae0\u5e2e\u5566\u53d7\u7cfb\u4ee4\u8df3\u975e\u4f55\u725b\u53d6\u5165\u5cb8\u6562\u6389\u5ffd\u79cd\u88c5\u9876\u6025\u6797\u505c\u606f\u53e5\u533a\u8863\u822c\u62a5\u53f6\u538b\u6162\u53d4\u80cc\u7ec6";

    return base[nextInt(base.length)]
    /**这样虽然可以生成文本, 但是不是常见字*/
    //return (0x4e00 + (Math.random() * (0x9fa5 - 0x4e00 + 1)).toInt()).toChar()
}

/**不需要返回值的 .let */
inline fun <T> T.it(block: (T) -> Unit) {
    block(this)
}

/**不需要返回值的 .apply */
inline fun <T> T.that(block: T.() -> Unit) {
    block(this)
}

/**循环, 多少次*/
fun loop(count: Int, action: () -> Unit) {
    for (i in 0 until count) {
        action()
    }
}

fun PointF.reset() {
    this.x = 0f
    this.y = 0f
}

fun Point.reset() {
    this.x = 0
    this.y = 0
}

/**支持负数索引[index]*/
fun <T> List<T?>.getOrNull2(index: Int): T? {
    if (index < 0) {
        return getOrNull(size + index)
    }
    return getOrNull(index)
}

/**如果是负数, 则反向取值
 * 如果大于size, 则取模*/
fun <T> List<T>.getSafe(index: Int): T? {
    val newIndex = if (index < 0) {
        size + index
    } else {
        index
    }
    val size = size()
    if (newIndex >= size) {
        return getOrNull(newIndex % size)
    }
    return getOrNull(newIndex)
}

/**异步代码, 同步执行. 会阻塞当前线程
 * 请主动调用[java.util.concurrent.CountDownLatch.countDown]
 * */
fun <R> sync(count: Int = 1, action: (CountDownLatch, AtomicReference<R>) -> Unit): R? {
    val latch = CountDownLatch(count)
    val result = AtomicReference<R>()
    thread(name = "sync-${nowTimeString()}") {
        action(latch, result)/*if (latch.count == 1L) {
            latch.countDown()
        }*/
    }
    latch.await()
    return result.get()
}

/**无返回值, 简单的同步方法, 阻塞当前线程
 * 请主动调用[java.util.concurrent.CountDownLatch.countDown]
 * */
fun syncSingle(count: Int = 1, action: (countDownLatch: CountDownLatch) -> Unit) {
    sync<String>(count) { countDownLatch, _ ->
        action(countDownLatch)
    }
}

/**
 * 具有返回值
 * [syncSingle]*/
fun <T> syncSingleResult(count: Int = 1, action: (countDownLatch: CountDownLatch) -> T?): T? {
    var result: T? = null
    sync<String>(count) { countDownLatch, _ ->
        result = action(countDownLatch)
    }
    return result
}

/**在子线程等待主线程的同步结果*/
fun <R> syncBack(onMainAction: () -> R?): R? {
    return sync { countDownLatch, atomicReference ->
        onMain {
            atomicReference.set(onMainAction())
            countDownLatch.countDown()
        }
    }
}

/**子线程 同步主线程执行*/
fun <R> syncToMain(onMainAction: () -> R?): R? {
    val latch = CountDownLatch(1)
    val result = AtomicReference<R>()
    onMain {
        result.set(onMainAction())
        latch.countDown()
    }
    latch.await()
    return result.get()
}

/**更新具有历史属性的[List]*/
fun <T> List<T>?.updateHistoryList(value: T?): List<T> {
    val list = this
    return if (value is String && value.isEmpty()) {
        list ?: emptyList()
    } else if (value == null) {
        list ?: emptyList()
    } else {
        (list?.toMutableList() ?: mutableListOf()).apply {
            remove(value)
            add(0, value)
        }
    }
}

fun <T> MutableCollection<T>.resetAll(new: Collection<T>?) {
    clear()
    new?.let {
        addAll(it)
    }
}

/**安全删除一个对象*/
fun <T> MutableList<T>.removeSafe(bean: T?): Boolean {
    if (bean == null || !contains(bean)) {
        return false
    }
    return remove(bean)
}

/**
 * [java.util.concurrent.CountDownLatch.await]
 * [java.util.concurrent.CountDownLatch.countDown]
 *
 * */
fun await(count: Int = 1) = CountDownLatch(count)

/**列表中, 当前[element]的前一个元素*/
fun <T> List<T>.before(element: T): T? {
    val index = indexOf(element)
    if (index <= 0) {
        return null
    }
    return getOrNull(index - 1)
}

/**当前元素[element]前面所有的元素集合*/
fun <T> List<T>.beforeList(element: T): List<T> {
    val result = mutableListOf<T>()
    val index = indexOf(element)
    if (index != -1) {
        for (i in 0..index) {
            result.add(get(i))
        }
    }
    return result
}

/**列表中, 当前[element]的后一个元素*/
fun <T> List<T>.after(element: T): T? {
    val index = indexOf(element)
    if (index >= lastIndex) {
        return null
    }
    return getOrNull(index + 1)
}

/**当前元素[element]后面所有的元素集合*/
fun <T> List<T>.afterList(element: T): List<T> {
    val result = mutableListOf<T>()
    val index = indexOf(element)
    if (index != -1) {
        for (i in index + 1..lastIndex) {
            result.add(get(i))
        }
    }
    return result
}

/**列表中, 当前[element]是否在[anchor]元素之前
 * [def] 默认, 当元素不在列表中时*/
fun <T> List<T>.isElementBeforeWith(element: T, anchor: T, def: Boolean = false): Boolean {
    val index1 = indexOf(element)
    val index2 = indexOf(anchor)
    return if (index1 != -1 && index2 != -1) {
        index1 < index2
    } else {
        def
    }
}

/**列表中, 当前[element]是否在[anchor]元素之后
 * [def] 默认, 当元素不在列表中时*/
fun <T> List<T>.isElementAfterWith(element: T, anchor: T, def: Boolean = true): Boolean {
    val index1 = indexOf(element)
    val index2 = indexOf(anchor)
    return if (index1 != -1 && index2 != -1) {
        index1 > index2
    } else {
        def
    }
}

/**使用行列的方式, 获取指定数据
 * [columnCount] 列数
 * [row] 需要获取第几行的数据
 * [column] 需要获取第几列的数据
 * */
fun <T> List<T>.getRowColumn(
    columnCount: Int, row: Int, column: Int, def: T? = null
): T? {
    val index = row * columnCount + column
    return getOrNull(index) ?: def
}

/**将当前元素, 移动到列表末尾*/
fun <T> MutableList<T>.moveToLast(element: T) {
    remove(element)
    add(element)
}

/**将指定位置的元素, 替换成新的元素.
 * 如果新的元素为空, 则仅移除旧元素*/
fun <T> MutableList<T>.replace(element: T, newElement: T?): Boolean {
    val index = indexOf(element)
    if (index != -1) {
        return if (newElement == null) {
            //remove
            remove(element)
        } else {
            set(index, newElement)
            true
        }
    }
    return false
}

/**在当前的列表[this]中, 移除[list]中没有的元素
 * @return 返回移除的数据量 */
fun <T> MutableList<T>.removeOutOf(list: List<T>): Int {
    val removeList = mutableListOf<T>()
    for (element in this) {
        if (!list.contains(element)) {
            removeList.add(element)
        }
    }
    if (removeList.isNotEmpty()) {
        removeAll(removeList)
    }
    return removeList.size()
}

/**在指定元素的后面插入新元素
 * @return -1:插入失败, index:对应的元素索引*/
fun <T> MutableList<T>.addAfterWith(with: T, element: T): Int {
    val index = indexOf(with)
    if (index != -1) {
        add(index + 1, element)
        return index + 1
    }
    return -1
}

/**在指定元素的前面插入新元素
 * @return -1:插入失败, index:对应的元素索引*/
fun <T> MutableList<T>.addBeforeWith(with: T, element: T): Int {
    val index = indexOf(with)
    if (index != -1) {
        add(index, element)
        return index
    }
    return -1
}

/**枚举[SimpleArrayMap]*/
fun <K, V> SimpleArrayMap<K, V>.each(action: (key: K, value: V?) -> Unit) {
    val size = size()
    for (i in 0 until size) {
        val k = keyAt(i)
        val v = get(k)
        action(k, v)
    }
}

/**将[value]限制在[min] [max]之间*/
fun clamp(value: Float, min: Float, max: Float): Float = min(max(value, min), max)

fun clamp(value: Int, min: Int, max: Int): Int = min(max(value, min), max)

fun clamp(value: Long, min: Long, max: Long): Long = min(max(value, min), max)

fun getValueFrom(value: Any?, valueType: Any?): Any? {
    if (value == null) {
        return null
    }
    val valueStr = value.toString()
    return if (valueType is Double) {
        valueStr.toDoubleOrNull()
    } else if (valueType is Float) {
        valueStr.toFloatOrNull()
    } else if (valueType is Long) {
        valueStr.toLongOrNull()
    } else if (valueType is Int) {
        valueStr.toIntOrNull()
    } else {
        value
    }
}

/**限制[value] 在最小值/最大值之间
 * [value] 需要限制的值
 * [valueType] 值的类型判断
 * */
fun clampValue(
    value: Any?, valueType: Any?, minValue: Any?, maxValue: Any?
): Any? {
    val valueStr = value?.toString()
    if (valueStr.isNullOrBlank()) {
        return value
    }
    if (valueType is Long) {
        val v = valueStr.toLongOrNull() ?: 0
        if (minValue != null) {
            val min = minValue.toString().toLongOrNull() ?: 0
            if (v < min) {
                return min
            }
        }
        if (maxValue != null) {
            val max = maxValue.toString().toLongOrNull() ?: 0
            if (v > max) {
                return max
            }
        }
        return value
    } else if (valueType is Int) {
        val v = valueStr.toIntOrNull() ?: 0
        if (minValue != null) {
            val min = minValue.toString().toIntOrNull() ?: 0
            if (v < min) {
                return min
            }
        }
        if (maxValue != null) {
            val max = maxValue.toString().toIntOrNull() ?: 0
            if (v > max) {
                return max
            }
        }
        return value
    } else { //float
        val v = valueStr.toFloatOrNull() ?: 0f
        if (minValue != null) {
            val min = minValue.toString().toFloatOrNull() ?: 0f
            if (v < min) {
                return min
            }
        }
        if (maxValue != null) {
            val max = maxValue.toString().toFloatOrNull() ?: 0f
            if (v > max) {
                return max
            }
        }
        return value
    }
}

/**进度[value]在[minValue]~[maxValue]中的比例
 * 返回[0~1]之间的比例*/
fun progressValueFraction(
    value: Any?, minValue: Any?, maxValue: Any?
): Float? {
    val valueStr = value?.toString()
    if (valueStr.isNullOrBlank()) {
        return null
    }
    val min = minValue?.toString()?.toFloatOrNull() ?: 0f
    val max = maxValue?.toString()?.toFloatOrNull() ?: 100f
    val fraction = value.toString().toFloatOrNull() ?: min
    return (fraction - min) / (max - min)
}

/**是否是在输入最小的浮点值*/
fun isFloatMinAdjustValue(value: Any?, minValue: Any?): Boolean {
    val valueStr = value?.toString()
    if (valueStr.isNullOrBlank()) {
        return true
    }
    val v = valueStr.toFloatOrNull() ?: 0f
    if (minValue != null) {
        val min = minValue.toString().toFloatOrNull() ?: 0f
        if (v < min) {
            if (!valueStr.contains(".") || valueStr.endsWith(".")) {
                //小数输入时, 特殊处理一下
                return true
            }
        }
    }
    return false
}

/**在同方向上取最大值
 * [target] 参与比较的值
 * [threshold] 阈值, 正值, 自动取反(负值)
 * */
fun maxOfDirection(target: Int, threshold: Int, defZero: Int = 0) = if (target == 0) defZero
else if (target > 0) max(target, threshold)
else min(target, -threshold)

/**[maxOfDirection]*/
fun maxOfDirection(target: Float, threshold: Float, defZero: Float = 0f) = if (target == 0f) defZero
else if (target > 0) max(target, threshold)
else min(target, -threshold)

/**在同方向上取最小值
 * [target] 参与比较的值
 * [threshold] 阈值, 正值, 自动取反(负值)
 * */
fun minOfDirection(target: Int, threshold: Int, defZero: Int = 0) = if (target == 0) defZero
else if (target > 0) min(target, threshold)
else max(target, -threshold)

/**[minOfDirection]*/
fun minOfDirection(target: Float, threshold: Float, defZero: Float = 0f) = if (target == 0f) defZero
else if (target > 0) min(target, threshold)
else max(target, -threshold)

/**都是正数, 或者都是负数*/
fun isSameDirection(value1: Int, value2: Int) =
    value1 == 0 || value2 == 0 || (value1 > 0 && value2 > 0) || (value1 < 0 && value2 < 0)

fun isSameDirection(value1: Float, value2: Float) =
    value1 == 0f || value2 == 0f || (value1 > 0 && value2 > 0) || (value1 < 0 && value2 < 0)

/**[java.lang.reflect.Type]*/
fun Type.toClass(): Class<*>? = if (this is Class<*>) {
    this
} else {
    try {
        Class.forName(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) typeName else toString())
    } catch (e: Exception) {
        null
    }
}

/**是否处于调试模式
 * [android.os.Debug.isDebuggerConnected]*/
fun isDebuggerConnected() = Debug.isDebuggerConnected()

/**不为空时, 追加一个字符*/
fun StringBuilder.appendSpaceIfNotEmpty(space: String = " ") {
    if (isNotEmpty()) {
        append(space)
    }
}

/**不为空时, 追加一个换行*/
fun StringBuilder.appendLineIfNotEmpty() {
    if (isNotEmpty()) {
        appendLine()
    }
}

/**创建一个类的实例*/
fun <T> Class<T>.createInstance(): T {
    return try {
        newInstance()
    } catch (e: Exception) {
        getDeclaredConstructor().newInstance()
    }
}

/**给定的值是否为空*/
fun isNil(value: Any?): Boolean {
    if (value == null) {
        return true
    }
    if (value is String) {
        return value.isEmpty()
    }
    if (value is Iterable<*>) {
        return !value.iterator().hasNext()
    }
    if (value is Map<*, *>) {
        return value.isEmpty()
    }
    if (value is Rect) {
        return value.isEmpty
    }
    if (value is RectF) {
        return value.isEmpty
    }
    return false
}

/**[listOf]*/
fun <E> stackOf(vararg elements: E): Stack<E> {
    val result = Stack<E>()
    for (element in elements) {
        result.push(element)
    }
    return result
}