package com.angcyo.library

import com.angcyo.library.ex.bit
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val int = 57
        println(0 and 0x01)
        println(int)
        println(int.bit(1))
        println(int.bit(2))
        println(int.bit(3))
        println("...")
        println(int shr 0)
        println((int shr 1) and 0x1)
    }

    @Test
    fun testList() {
        val list = (0..60).map { it }.toList()
        println(list)
    }
}