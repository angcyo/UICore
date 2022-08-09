package com.angcyo.library.component

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import com.angcyo.library.L
import com.angcyo.library.ex.classHash
import com.angcyo.library.ex.simpleHash

/**
 * 基类Service
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/26
 */
abstract class BaseService : Service() {

    companion object {

        fun bindService(
            context: Context,
            intent: Intent,
            result: (ServiceConnection, IBinder?) -> Unit
        ) {
            context.bindService(intent, object : ServiceConnection {
                override fun onServiceDisconnected(name: ComponentName) {
                    result(this, null)
                }

                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    result(this, service)
                }
            }, BIND_AUTO_CREATE)
        }

        fun unbindService(context: Context, conn: ServiceConnection) {
            context.unbindService(conn)
        }
    }

    override fun onCreate() {
        super.onCreate()
        L.d(this.classHash())
    }

    /** 调用startService()启动服务时回调.
     * [startId] 会随着[调用startService]调用次数的增加而增加
     * [flags] 则是系统传递过来的[START_STICKY]
     * */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        L.d("${this.simpleHash()} $flags $startId $intent")
        intent?.apply {
            handleIntent(this)
        }
        return START_STICKY
    }

    /**处理Intent*/
    open fun handleIntent(intent: Intent) {

    }

    //<editor-fold desc="Binder支持">

    val innerBinder: InnerBinder by lazy { InnerBinder() }

    /** 通过bindService()绑定到服务的客户端 */
    override fun onBind(intent: Intent?): IBinder? = innerBinder

    /** 通过unbindService()解除所有客户端绑定时调用 */
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    /** 通过bindService()将客户端绑定到服务时调用*/
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun unbindService(conn: ServiceConnection) {
        super.unbindService(conn)
    }

    inner class InnerBinder : Binder() {
        fun service() = this@BaseService
    }

    //</editor-fold desc="Binder支持">

}