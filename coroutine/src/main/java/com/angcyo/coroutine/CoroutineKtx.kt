package com.angcyo.coroutine

import android.os.SystemClock
import com.angcyo.library.L
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * 协程相关封装
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 */

fun coroutineTest() {
    //backTest()
    //blockTest()
}

/**串行协程线程调度测试*/
fun blockTest() {
    launch {

        val i = onBlock {
            L.i("run....1..1")
            sleep()
            1 / 0
            L.i("run....1..1end")
            1
        }

        val i1 = onBlock {
            L.i("run....1..2")
            sleep(400)
            L.i("run....1..2end")
            2
        }

        val i2 = onBlock {
            L.i("run....1..3")
            sleep(500)
            L.i("run....1..3end")
            3
        }

        L.i("all end1->$i $i1 $i2")

        onMain {
            L.i("all end2->$i $i1 $i2")
        }
    }
}

/**并发协程测试*/
fun backTest() {
    launch(Dispatchers.Main + CoroutineErrorHandler {
        L.e("自定义捕捉异常:$it")
    }) {

        val i = try {
            onBack {
                L.i("run....1..1")
                sleep()
                1 / 0
                L.i("run....1..1end")
                1
            }
        } catch (e: Exception) {
            L.e("1...${e.message}")
            null
        }

        val i1 = onBack {
            L.i("run....1..2")
            sleep(400)
            L.i("run....1..2end")
            2
        }

        val i2 = onBack {
            L.i("run....1..3")
            sleep(500)
            L.i("run....1..3end")
            3
        }

        val j = try {
            i?.await()
        } catch (e: Exception) {
            L.e("2...${e.message}")
        }
        val j1 = i1.await()
        val j2 = i2.await()

        L.i("all end1->${j} ${i1.await()} ${i2.await()}")

        onMain {
            L.i("all end2->$j $j1 $j2")
            //L.i("all end3->${i?.await()} ${i1.await()} ${i2.await()}")
        }
    }
}

fun <T> launchMain(onBack: CoroutineScope.() -> T, onMain: (T) -> Unit = {}): Job {
    return GlobalScope.launch(Dispatchers.Main) {
        val deferred = async(Dispatchers.IO) {
            this.onBack()
        }
        onMain(deferred.await())
    }
}

/**在全局域中启动协程*/
fun launch(
    context: CoroutineContext = Dispatchers.Main + CoroutineErrorHandler(),
    action: suspend CoroutineScope.() -> Unit
): Job {
    return GlobalScope.launch(context) {
        this.action()
    }
}

/**在指定域中启动协程*/
fun CoroutineScope.launchSafe(
    context: CoroutineContext = Dispatchers.Main + CoroutineErrorHandler(),
    action: suspend CoroutineScope.() -> Unit
): Job {
    return this.launch(context) {
        this.action()
    }
}

/**
 * 在协程中使用, 用于在[IO]线程中并发
 * [action]内发生的异常,可以在[launch]启动协程时用[CoroutineExceptionHandler]捕捉
 * [try] [await] 方法也能获取到异常, 但无法阻止异常冒泡
 * */
fun <T> CoroutineScope.onBack(
    context: CoroutineContext = Dispatchers.IO,
    action: suspend CoroutineScope.() -> T
) = async(context) { this.action() }

/**
 * 在协程中使用, 用于在[IO]线程中调度
 * [action]内发生的异常, 需要在内部try捕捉.
 * 或者在[launch]启动协程时用[CoroutineExceptionHandler]捕捉
 * 并且协程会立即中断,后续代码不会被执行
 * */
suspend fun <T> onBlock(
    context: CoroutineContext = Dispatchers.IO,
    action: suspend CoroutineScope.() -> T
) = withContext(context) { this.action() }

/**在协程中使用, 用于在[Main]线程中调度*/
suspend fun <T> onMain(
    context: CoroutineContext = Dispatchers.Main,
    action: suspend CoroutineScope.() -> T
) = withContext(context) { this.action() }

fun sleep(ms: Long = 300) {
    SystemClock.sleep(ms)
}