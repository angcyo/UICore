package com.angcyo.coroutine

import android.os.SystemClock
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 协程相关封装
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 */

//<editor-fold desc="GlobalScope">

/**默认的协程调度器
 * [java.util.concurrent.Executors.newSingleThreadExecutor]
 * [ContextCompat.getMainExecutor(this)]
 * */
fun dispatcherExecutor() = Dispatchers.Default.asExecutor()

/**在全局域中启动协程*/
@DelicateCoroutinesApi
fun <T> launchMain(onBack: CoroutineScope.() -> T, onMain: (T) -> Unit = {}): Job {
    return GlobalScope.launch(Dispatchers.Main) {
        val deferred = async(Dispatchers.IO) {
            this.onBack()
        }
        onMain(deferred.await())
    }
}

/**在全局域中启动协程
 * [kotlinx.coroutines.runBlocking]
 * [kotlinx.coroutines.delay]
 * job.join() // 等待直到子协程执行结束*/
@DelicateCoroutinesApi
fun launchGlobal(
    context: CoroutineContext = Dispatchers.Main + CoroutineErrorHandler(),
    action: suspend CoroutineScope.() -> Unit
): Job {
    return GlobalScope.launch(context) {
        /*coroutineScope {
            launch {
                delay(500L)
            }
            repeat(100_00) {

            }
        }*/
        this.action()
    }
}

//</editor-fold desc="GlobalScope">

//<editor-fold desc="CoroutineScope">

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
 * [action]内发生的异常,可以在[launchGlobal]启动协程时用[CoroutineExceptionHandler]捕捉
 * [try] [await] 方法也能获取到异常, 但无法阻止异常冒泡
 * */
fun <T> CoroutineScope.onBack(
    context: CoroutineContext = Dispatchers.IO,
    action: suspend CoroutineScope.() -> T
) = async(context) { this.action() }

/**[onBack]*/
fun <T> CoroutineScope.withBack(
    context: CoroutineContext = Dispatchers.IO,
    action: suspend CoroutineScope.() -> T
) = async(context) { this.action() }

//</editor-fold desc="CoroutineScope">

//<editor-fold desc="suspend">

/**
 * 在协程中使用, 用于在[IO]线程中调度
 * [action]内发生的异常, 需要在内部try捕捉.
 * 或者在[launchGlobal]启动协程时用[CoroutineExceptionHandler]捕捉
 * 并且协程会立即中断,后续代码不会被执行
 * */
suspend fun <T> withBlock(
    context: CoroutineContext = Dispatchers.IO,
    action: suspend CoroutineScope.() -> T
) = withContext(context) { this.action() }

/**在协程中使用, 用于在[Main]线程中调度*/
suspend fun <T> withMain(
    context: CoroutineContext = Dispatchers.Main,
    action: suspend CoroutineScope.() -> T
) = withContext(context) { this.action() }

fun sleep(ms: Long = 300) {
    SystemClock.sleep(ms)
}

/**同步等待一个异步结果
 * [CancellableContinuation.cancel] 取消等待, 但是不会取消协程, 协程会继续执行
 * [CancellableContinuation.resume] 恢复等待
 * [CancellableContinuation.resumeWithException] 异常恢复等待
 * */
suspend inline fun <T> syncWait(crossinline block: (CancellableContinuation<T>) -> Unit): T {
    return suspendCancellableCoroutine<T> {
        block(it)
        //it.cancel()
        //it.resume()
        //it.resumeWithException()
    }
    //suspendCoroutine<> {  }
}

//</editor-fold desc="suspend">

//<editor-fold desc="lifecycleScope">

/**在生命周期中启动一个协程*/
fun LifecycleOwner.launchLifecycle(
    context: CoroutineContext = Dispatchers.Main + CoroutineErrorHandler(),
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job = lifecycleScope.launch(context, start, block)

//</editor-fold desc="lifecycleScope">