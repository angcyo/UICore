package com.angcyo.library

import com.angcyo.library.ex.nowTime

/**
 * 帧率计算
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/20
 */
object Fps {

    private var startTime: Long? = null
    private var frameCount = 0
    var fps = 0
        private set

    fun start() {
        startTime = nowTime()
        frameCount = 0
        fps = 0
    }

    fun fps(): Int {
        if (startTime == null) {
            start()
        }
        frameCount++
        val currentTime = nowTime()
        val elapsedTime = currentTime - startTime!!
        if (elapsedTime >= 1000) {
            fps = frameCount
            frameCount = 0
            startTime = currentTime
        }
        return fps
    }

}