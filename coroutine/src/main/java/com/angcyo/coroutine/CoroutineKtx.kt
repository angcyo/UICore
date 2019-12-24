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

fun test() {
    //backTest()
    blockTest()
}

/**串行协程线程调度测试*/
fun blockTest() {
    launch {

        val i = onBlock {
            L.i("run....1..1")
            sleep()
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
    launch {

        val i = onBack {
            L.i("run....1..1")
            sleep()
            L.i("run....1..1end")
            1
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

        val j = i.await()
        val j1 = i1.await()
        val j2 = i2.await()

        L.i("all end1->${i.await()} ${i1.await()} ${i2.await()}")

        onMain {
            L.i("all end2->$j $j1 $j2")
            L.i("all end3->${i.await()} ${i1.await()} ${i2.await()}")
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
    context: CoroutineContext = Dispatchers.Main,
    action: suspend CoroutineScope.() -> Unit
): Job {
    return GlobalScope.launch(context) {
        this.action()
    }
}

///**在指定域中启动协程*/
//fun CoroutineScope.launch(
//    context: CoroutineContext = Dispatchers.Main,
//    action: suspend CoroutineScope.() -> Unit
//): Job {
//    return this.launch(context) {
//        this.action()
//    }
//}

/**在协程中使用, 用于在[IO]线程中并发*/
fun <T> CoroutineScope.onBack(
    context: CoroutineContext = Dispatchers.IO,
    action: suspend CoroutineScope.() -> T
) = async(context) { this.action() }

/**在协程中使用, 用于在[IO]线程中调度*/
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