package com.angcyo.activity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.angcyo.DslAHelper
import com.angcyo.fragment.R
import com.angcyo.getDataOrParcelable
import com.angcyo.library.component.DslNotify
import com.angcyo.library.component.dslNotify
import com.angcyo.library.ex.nowTimeString
import com.angcyo.putData
import kotlinx.parcelize.Parcelize

/**
 * 锁屏通知类, 支持通知消息体
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/17
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class LockNotifyActivity : BaseLockNotifyActivity() {

    lateinit var lockNotifyParams: LockNotifyParams

    init {
        activityLayoutId = R.layout.lib_activity_lock_notify_layout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lockNotifyParams = getDataOrParcelable() ?: LockNotifyParams()

        initLockLayout()
    }

    open fun initLockLayout() {
        _vh.tv(R.id.notify_time)?.text = lockNotifyParams.notifyTime ?: nowTimeString("HH:mm")

        if (lockNotifyParams.notifyLogo == -1) {
            _vh.img(R.id.notify_logo)?.setImageDrawable(appLogo)
        } else {
            _vh.img(R.id.notify_logo)?.setImageDrawable(getDrawable(lockNotifyParams.notifyLogo))
        }
        _vh.tv(R.id.notify_name)?.text = lockNotifyParams.notifyName ?: appName

        _vh.tv(R.id.notify_title)?.text = lockNotifyParams.notifyTitle
        _vh.visible(R.id.notify_title, lockNotifyParams.notifyTitle != null)

        _vh.tv(R.id.notify_content)?.text = lockNotifyParams.notifyContent
        _vh.visible(R.id.notify_content, lockNotifyParams.notifyContent != null)

        _vh.throttleClick(R.id.notify_wrap_layout) {

            //取消通知
            if (lockNotifyParams.notifyId > 0) {
                DslNotify.cancelNotify(this, lockNotifyParams.notifyId)
            }

            //intent
            lockNotifyParams.notifyActivityIntent?.apply {
                startActivity(this)
            }

            lockNotifyParams.notifyBroadcastIntent?.apply {
                sendBroadcast(this)
            }

            //解锁, 关闭界面
            disableKeyguard()
            finish()
        }
    }
}

/**锁屏通知的一些参数配置*/

@Parcelize
data class LockNotifyParams(
    //默认是程序昵称
    var notifyName: String? = null,
    //默认是程序logo, 资源id
    @DrawableRes
    var notifyLogo: Int = -1,
    //默认是当前时间
    var notifyTime: String? = null,

    var notifyTitle: String? = null,
    var notifyContent: String? = null,

    //点击之后, 触发的Activity Intent
    var notifyActivityIntent: Intent? = null,

    //点击之后, 触发的Broadcast Intent
    var notifyBroadcastIntent: Intent? = null,

    //通知的id, 点击之后, 会取消通知. 大于0才有效, 小于0会自动赋值
    var notifyId: Int = -1
) : Parcelable

/**
 * 显示通知. 如果锁屏了, 才会启动[LockNotifyActivity]*/
fun DslAHelper.lockNotify(action: LockNotifyParams.() -> Unit) {
    val params = LockNotifyParams()
    params.action()

    //不管怎样, 通知都要显示
    val id = dslNotify {
        if (params.notifyId > 0) {
            notifyId = params.notifyId
        }

        notifyTitle = params.notifyTitle
        notifyText = params.notifyContent

        if (params.notifyLogo > 0) {
            notifySmallIcon = params.notifyLogo
        }

        if (params.notifyActivityIntent != null) {
            notifyContentIntent = DslNotify.pendingActivity(context, params.notifyActivityIntent!!)
        } else if (params.notifyBroadcastIntent != null) {
            notifyContentIntent =
                DslNotify.pendingBroadcast(context, params.notifyBroadcastIntent!!)
        }
    }

    if (params.notifyId != id) {
        params.notifyId = id
    }

    if (context.isKeyguardLocked()) {
        start(LockNotifyActivity::class.java) {
            putData(params)
        }
    } /*else {
        params.notifyActivityIntent?.let {
            context.dslAHelper {
                start(it)
            }
        }

        params.notifyBroadcastIntent?.apply {
            context.sendBroadcast(this)
        }
    }*/
}