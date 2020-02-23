package com.angcyo.media.audio

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2017/10/25 16:20
 */
abstract class SimplePlayerListener : RPlayer.OnPlayerListener {
    override fun onPreparedCompletion(duration: Int) {
    }

    /** [com.angcyo.media.audio.RPlayer.OnPlayerListener.onPlayProgress] */
    override fun onPlayProgress(progress: Int, duration: Int) {
    }

    override fun onPlayCompletion(duration: Int) {
    }

    override fun onPlayError(what: Int, extra: Int) {
    }

    override fun onPlayStateChange(playUrl: String, from: Int, to: Int) {
    }
}