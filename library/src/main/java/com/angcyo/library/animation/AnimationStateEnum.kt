package com.angcyo.library.animation

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/27
 *
 * 动画状态枚举
 */
enum class AnimationStateEnum {
    /**
     * 默认状态
     * */
    DEFAULT,

    /**
     * 播放中
     * */
    PLAYING,

    /**
     * 暂停中
     * */
    PAUSED,

    /**
     * 停止中
     * */
    STOPPED,

    /**
     * 播放完成
     * */
    FINISHED, ;

    val isPlaying: Boolean
        get() = when (this) {
            PLAYING -> true
            else -> false
        }

    val isPaused: Boolean
        get() = when (this) {
            PAUSED -> true
            else -> false
        }

    /**动画是否开始了*/
    val isStarted: Boolean
        get() = when (this) {
            PLAYING, PAUSED -> true
            else -> false
        }

    /**动画是否结束*/
    val isStopped: Boolean
        get() = when (this) {
            DEFAULT, STOPPED, FINISHED -> true
            else -> false
        }
}