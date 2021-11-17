package com.angcyo.media.audio

/**
 * 语音播放助手
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object AudioPlayerHelper {

    val player: RPlayer = RPlayer()

    /**指定的播放路径文件是否正在播放*/
    fun isPlaying(path: String?): Boolean {
        return player.isPlayCall() && player._playingUrl == path
    }

    fun isPlaying(): Boolean {
        return player.isPlaying()
    }

    fun play(path: String?) {
        player.startPlay(path)
    }

    fun stop() {
        player.stopPlay()
    }

    fun onPlayEnd(onEnd: (Int, PlayerException?) -> Unit) {
        player.onPlayListener = object : RPlayer.OnPlayerListener {
            override fun onPreparedCompletion(duration: Int) {
            }

            override fun onPlayProgress(progress: Int, duration: Int) {
            }

            override fun onPlayCompletion(duration: Int) {
                onEnd(duration, null)
            }

            override fun onPlayError(what: Int, extra: Int) {
                onEnd(-1, PlayerException(what, extra))
            }

            override fun onPlayStateChange(playUrl: String, from: Int, to: Int) {
                if (to == RPlayer.STATE_STOP) {
                    onEnd(-1, null)
                }
            }
        }
    }

    fun clearListener() {
        player.onPlayListener = null
    }
}