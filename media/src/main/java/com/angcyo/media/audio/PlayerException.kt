package com.angcyo.media.audio

/**
 * [MediaPlayer] 异常类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/09
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class PlayerException(val what: Int, val extra: Int) : RuntimeException() {
    override fun toString(): String {
        return "播放异常:$what:$extra"
    }
}