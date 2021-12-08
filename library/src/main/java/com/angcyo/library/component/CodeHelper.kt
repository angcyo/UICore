package com.angcyo.library.component

import android.widget.TextView
import androidx.core.view.ViewCompat

/**
 * 验证码发送助手
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/17
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class CodeHelper : Runnable {

    /**倒计时时长*/
    var countDownDuration = 60

    /**结束回调*/
    var codeTimeEnd: () -> Unit = {}

    //是否已经发送过
    var _isSend = false

    //之前的文本信息
    var _text: CharSequence? = null

    //视图
    var _view: TextView? = null

    //当前时间
    var _time: Int = -1

    /**发送验证码*/
    fun sendCode(textView: TextView?) {
        if (_isSend || textView == null) {
            return
        }
        _isSend = true
        _view = textView
        _text = textView.text

        _time = countDownDuration
        run()
    }

    fun finish() {
        _view?.apply {
            removeCallbacks(this@CodeHelper)
            isClickable = true
            isEnabled = true
            text = _text
            _isSend = false
            codeTimeEnd()
        }
    }

    override fun run() {
        _time -= 1
        _view?.apply {
            if (_time < 0) {
                finish()
            } else {
                isClickable = false
                isEnabled = false
                text = "${_time}s"
                if (ViewCompat.isAttachedToWindow(this)) {
                    postDelayed(this@CodeHelper, 1_000)
                }
            }
        }
    }
}