# 2020/01/16

## coroutine 协程

https://www.kotlincn.net/docs/reference/coroutines/coroutines-guide.html

## Android 上的 Kotlin 协程

https://developer.android.google.cn/kotlin/coroutines

## 将 Kotlin 协程与生命周期感知型组件一起使用

https://developer.android.google.cn/topic/libraries/architecture/coroutines

## 异步流

https://www.kotlincn.net/docs/reference/coroutines/flow.html

```kotlin
suspend fun performRequest(request: Int): String {
    delay(1000) // 模仿长时间运行的异步工作
    return "response $request"
}

fun main() = runBlocking<Unit> {
    (1..3).asFlow() // 一个请求流
        .map { request -> performRequest(request) }
        .collect { response -> println(response) }
}
```

## 通道

https://www.kotlincn.net/docs/reference/coroutines/channels.html

```kotlin
val channel = Channel<Int>()
launch {
    // 这里可能是消耗大量 CPU 运算的异步逻辑，我们将仅仅做 5 次整数的平方并发送
    for (x in 1..5) channel.send(x * x)
}
// 这里我们打印了 5 次被接收的整数：
repeat(5) { println(channel.receive()) }
println("Done!")
```

## 共享的可变状态与并发

https://www.kotlincn.net/docs/reference/coroutines/shared-mutable-state-and-concurrency.html

```kotlin
suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 100  // 启动的协程数量
    val k = 1000 // 每个协程重复执行同一动作的次数
    val time = measureTimeMillis {
        coroutineScope { // 协程的作用域
            repeat(n) {
                launch {
                    repeat(k) { action() }
                }
            }
        }
    }
    println("Completed ${n * k} actions in $time ms")    
}
```

### 线程安全的数据结构 

```kotlin
val counter = AtomicInteger()

fun main() = runBlocking {
    withContext(Dispatchers.Default) {
        massiveRun {
            counter.incrementAndGet()
        }
    }
    println("Counter = $counter")
}
```

### 以粗粒度限制线程

```kotlin
val counterContext = newSingleThreadContext("CounterContext")
var counter = 0

fun main() = runBlocking {
    // 将一切都限制在单线程上下文中
    withContext(counterContext) {
        massiveRun {
            counter++
        }
    }
    println("Counter = $counter")
}
```

### 互斥

```kotlin
val mutex = Mutex()
var counter = 0

fun main() = runBlocking {
    withContext(Dispatchers.Default) {
        massiveRun {
            // 用锁保护每次自增
            mutex.withLock {
                counter++
            }
        }
    }
    println("Counter = $counter")
}
```