package com.angcyo.activity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.angcyo.DslAHelper
import com.angcyo.base.dslAHelper
import com.angcyo.fragment.R
import com.angcyo.getDataOrParcelable
import com.angcyo.library.ex.nowTimeString
import com.angcyo.putData
import kotlinx.android.parcel.Parcelize

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
        _vh.tv(R.id.notify_content)?.text = lockNotifyParams.notifyContent

        _vh.throttleClick(R.id.notify_wrap_layout) {

            lockNotifyParams.notifyActivityIntent?.apply {
                startActivity(this)
            }

            lockNotifyParams.notifyBroadcastIntent?.apply {
                sendBroadcast(this)
            }

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
    var notifyBroadcastIntent: Intent? = null
) : Parcelable

/**如果锁屏了, 才会启动[LockNotifyActivity], 否则直接触发对应的[Intent]*/
fun DslAHelper.lockNotify(action: LockNotifyParams.() -> Unit) {
    val params = LockNotifyParams()
    params.action()
    if (context.isKeyguardLocked()) {
        start(LockNotifyActivity::class.java) {
            putData(params)
        }
    } else {
        params.notifyActivityIntent?.let {
            context.dslAHelper {
                start(it)
            }
        }

        params.notifyBroadcastIntent?.apply {
            context.sendBroadcast(this)
        }
    }
}