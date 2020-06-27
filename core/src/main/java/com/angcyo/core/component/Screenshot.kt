package com.angcyo.core.component

import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.app.KeyguardManager.KeyguardDismissCallback
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import com.angcyo.core.component.accessibility.density
import com.angcyo.core.component.accessibility.displayRealSize
import com.angcyo.library.L
import com.angcyo.library.utils.invokeMethod
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screenshot.java
 * Description : 5.0后, 系统API截屏
 *
 * 1: 创建对象
 * com.angcyo.core.component.Screenshot.Companion.capture
 *
 * 2: 开始捕捉,触发截屏权限, 捕捉屏幕
 * com.angcyo.core.component.Screenshot.startCapture(android.app.Activity, int)
 *
 * 3: 开始处理, 权限允许之后, 才能截图
 * com.angcyo.core.component.Screenshot.onActivityResult(int, android.content.Intent)
 *
 * 4: 关闭了自动捕捉[autoCapture], 需要手动调用
 * com.angcyo.core.component.Screenshot.startToShot
 *
 * 5: [captureListener]会回调截屏信息
 *
 * Created by MixtureDD on 2017/6/21 19:16.
 * Copyright © 2017 MixtureDD. All rights reserved.
 */
class Screenshot private constructor(val application: Context) {

    companion object {
        val TAG = Screenshot::class.java.name
        var mediaProjectionManager: MediaProjectionManager? = null

        /**
         * 1: 创建对象, 设置回调监听
         */
        fun capture(context: Context, listener: OnCaptureListener): Screenshot {
            return Screenshot(context.applicationContext).apply { captureListener = listener }
        }

        /**
         * 返回屏幕是否亮屏状态
         */
        fun isScreenOn(context: Context): Boolean {
            // 获取电源管理器对象
            val pm =
                context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val screenOn: Boolean
            screenOn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                pm.isInteractive
            } else {
                pm.isScreenOn
            }
            return screenOn
        }

        /**
         * 唤醒手机屏幕并解锁, 点亮屏幕,解锁手机
         */
        @SuppressLint("MissingPermission")
        @JvmOverloads
        fun wakeUpAndUnlock(
            context: Context,
            wakeLock: Boolean = true /*亮屏(并解锁) or 灭屏*/,
            succeededAction: Runnable? = null
        ) {
            // 获取电源管理器对象
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val screenOn: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                pm.isInteractive
            } else {
                pm.isScreenOn
            }
            if (wakeLock) {
                if (!screenOn) {
                    // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
                    val wl = pm.newWakeLock( /*PowerManager.FULL_WAKE_LOCK |*/
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                                PowerManager.SCREEN_DIM_WAKE_LOCK /*PowerManager.SCREEN_BRIGHT_WAKE_LOCK*/,
                        context.packageName + ":bright"
                    )
                    wl.acquire(10000) // 点亮屏幕
                    wl.release() // 释放
                } else {
                    //屏幕已经是亮的
                    if (succeededAction != null) {
                        val handler = Handler(Looper.getMainLooper())
                        handler.post(succeededAction)
                    }
                }
                // 屏幕解锁
                val keyguardManager =
                    context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (keyguardManager.isKeyguardLocked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context is Activity) {
                        //在未设置密码的情况下, 可以解锁
                        keyguardManager.requestDismissKeyguard(
                            context,
                            object : KeyguardDismissCallback() {
                                override fun onDismissError() {
                                    super.onDismissError()
                                    //如果设备未锁屏的情况下, 调用此方法.会回调错误
                                    L.i("onDismissError")
                                }

                                override fun onDismissSucceeded() {
                                    super.onDismissSucceeded()
                                    //输入密码解锁成功
                                    L.i("onDismissSucceeded")
                                    if (succeededAction != null) {
                                        val handler =
                                            Handler(Looper.getMainLooper())
                                        handler.post(succeededAction)
                                    }
                                }

                                override fun onDismissCancelled() {
                                    super.onDismissCancelled()
                                    //弹出密码输入框之后, 取消了会回调
                                    L.i("onDismissCancelled")
                                }
                            })
                    } else {
                        val keyguardLock = keyguardManager.newKeyguardLock("unLock")
                        // 屏幕锁定
                        keyguardLock.reenableKeyguard()
                        keyguardLock.disableKeyguard() // 解锁
                        L.i("reenableKeyguard -> disableKeyguard")
                        if (succeededAction != null) {
                            val handler =
                                Handler(Looper.getMainLooper())
                            handler.post(succeededAction)
                        }
                    }
                }
            } else {
                if (screenOn) {
//                PowerManager.WakeLock wl = pm.newWakeLock(
//                        PowerManager.PARTIAL_WAKE_LOCK,
//                        context.getPackageName() + ":bright");
//                wl.acquire();
//                wl.release();
                    //android.permission.DEVICE_POWER
                    pm.invokeMethod("goToSleep", SystemClock.uptimeMillis(), 0, 0)
                }
            }
        }
    }

    var mMediaProjection: MediaProjection? = null
    var mVirtualDisplay: VirtualDisplay? = null
    var imageReader: ImageReader? = null
    var windowWidth = 0
    var windowHeight = 0
    var mScreenDensity = 0
    var captureListener: OnCaptureListener? = null

    /**
     * 是否需要保存到文件
     */
    var saveToFile = false

    /**保存后的截图文件路径*/
    var saveFilePath: String? = null

    /**
     * 连续捕捉
     */
    var alwaysCapture = false

    /**
     * 自动捕捉,如果为false. 关闭自动截图, 需要手动调用 [.startToShot] 才会截图
     */
    var autoCapture = true
    var captureDelay: Long = 60
    var compressFormat = CompressFormat.JPEG
    var compressQuality = 80
    val handler = Handler()

    init {
        createVirtualEnvironment()
    }

    /**
     * 手动触发截图
     */
    fun startToShot() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                startCapture()
                if (autoCapture) {
                    if (alwaysCapture && mVirtualDisplay != null) {
                        handler.postDelayed(this, captureDelay)
                    } else {
                        destroy()
                    }
                } else {
                    //手动模式
                }
            }
        }, captureDelay)
    }

    /**
     * 2:触发截屏权限, 捕捉屏幕
     */
    fun startCapture(activity: Activity, requestCode: Int) {
        activity.startActivityForResult(
            mediaProjectionManager!!.createScreenCaptureIntent(),
            requestCode
        )
    }

    /**
     * 3:开始处理, 权限允许之后, 才能截图
     * end.
     */
    fun onActivityResult(resultCode: Int, data: Intent?) {
        onActivityResult(resultCode, data, null)
    }

    fun onActivityResult(
        resultCode: Int,
        data: Intent?,
        onSucceed: Runnable?
    ): MediaProjection? {
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
        }
        mMediaProjection = mediaProjectionManager!!.getMediaProjection(resultCode, data!!)
        L.i("mMediaProjection defined")
        if (mMediaProjection != null) {
            mMediaProjection!!.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    L.i("MediaProjection Stop")
                }
            }, null)
            setUpVirtualDisplay()
            if (autoCapture) {
                startToShot()
            }
            onSucceed?.run()
        }
        return mMediaProjection
    }

    private fun setUpVirtualDisplay() {
        L.i("Setting up a VirtualDisplay: " + windowWidth + "x" + windowHeight + " (" + mScreenDensity + ")")
        imageReader = ImageReader.newInstance(
            windowWidth,
            windowHeight,
            0x1,
            2
        ) //ImageFormat.RGB_565
        imageReader!!.setOnImageAvailableListener({
            //Log.v(TAG, "onImageAvailable");
        }, null)
        mVirtualDisplay = mMediaProjection!!.createVirtualDisplay(
            "ScreenCapture",
            windowWidth, windowHeight, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface, null, null
        )
    }

    private fun createVirtualEnvironment() {
        val realSize = application.displayRealSize()
        windowWidth = realSize.x
        windowHeight = realSize.y
        mScreenDensity = application.density().toInt()
        mediaProjectionManager =
            application.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private fun startCapture() {
        val image = imageReader?.acquireLatestImage() ?: return
        val width = image.width
        val height = image.height
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        var bitmap =
            Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap!!.copyPixelsFromBuffer(buffer)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        image.close()
        //Toast.makeText(application, "正在保存截图", Toast.LENGTH_SHORT).show();
        if (bitmap != null && saveToFile) {
            val dateFormat = SimpleDateFormat("yyyyMMdd-hhmmss")
            val strDate = "Screenshot_" + dateFormat.format(Date())
            val pathImage =
                Environment.getExternalStorageDirectory().path + "/Pictures/Screenshots/"
            saveFilePath = "$pathImage$strDate.png"
            L.i("image name is : $saveFilePath")
            L.i("bitmap  create success ")
            try {
                val fileFolder = File(pathImage)
                if (!fileFolder.exists()) {
                    fileFolder.mkdirs()
                }
                val file = File(saveFilePath)
                if (!file.exists()) {
                    L.e("file create success ")
                    file.createNewFile()
                }
                val out = FileOutputStream(file)
                bitmap.compress(compressFormat, compressQuality, out)
                out.flush()
                out.close()
                val media = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri = Uri.fromFile(file)
                media.data = contentUri
                application.sendBroadcast(media)
                L.i("screen image saved")
                //Toast.makeText(application, "截图保存成功", Toast.LENGTH_SHORT).show();
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                L.e(e.toString())
                e.printStackTrace()
            }
        }
        if (captureListener != null && bitmap != null) {
            captureListener?.onCapture(bitmap, saveFilePath)
        }
    }

    /**
     * 释放资源
     */
    fun destroy() {
        L.i("onDestroy()")
        tearDownMediaProjection()
    }

    private fun tearDownMediaProjection() {
        stopCapture()
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
        L.i("mMediaProjection undefined")
    }

    /**
     * 停止捕捉
     */
    fun stopCapture() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        mVirtualDisplay = null
        L.i("virtual display stopped")
    }

    interface OnCaptureListener {
        /**
         * 主线程回调
         */
        fun onCapture(bitmap: Bitmap, filePath: String?)
    }
}