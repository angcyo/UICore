package com.angcyo.core.component

import android.annotation.TargetApi
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.angcyo.base.requestSdCardPermission
import com.angcyo.library.*
import com.angcyo.library.component.lastContext
import com.angcyo.library.utils.storage.haveSdCardPermission
import com.angcyo.viewmodel.vmDataOnce
import java.util.*

/**
 * 截屏通知
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/11/12
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
@TargetApi(17)
class ScreenShotModel : ViewModel() {

    companion object {

        private val DATE_TAKEN = "datetaken"

        private val MEDIA_PROJECTIONS = arrayOf(MediaStore.MediaColumns.DATA, DATE_TAKEN)

        private val MEDIA_PROJECTIONS_API_16 = arrayOf(
            MediaStore.MediaColumns.DATA,
            DATE_TAKEN,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT
        )

        private val KEYWORDS = arrayOf(
            "screenshot",
            "screen_shot",
            "screen-shot",
            "screen shot",
            "screencapture",
            "screen_capture",
            "screen-capture",
            "screen capture",
            "screencap",
            "screen_cap",
            "screen-cap",
            "screen cap"
        )

        private fun assertInMainThread() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                val elements = Thread.currentThread().stackTrace
                var methodMsg: String? = null
                if (elements.size >= 4) {
                    methodMsg = elements[3].toString()
                }
                throw IllegalStateException("Call the method must be in main thread: $methodMsg")
            }
        }
    }

    private var startListenTime: Long = 0
    private var internalObserver: MediaContentObserver? = null
    private var externalObserver: MediaContentObserver? = null
    private val uiHandler = Handler(Looper.getMainLooper())

    /**截图的图片监听, 需要SD卡权限才能读取[Bitmap]*/
    /**路径监听*/
    val screenShotPathData = vmDataOnce<String?>(null)

    /**路径监听*/
    var screenShotListener: OnScreenShotListener? = null

    /**开始监听
     * [context] 如果需要自动处理sd卡权限, 请使用[Activity]上下文*/
    @MainThread
    fun startListen(context: Context = lastContext) {
        if (internalObserver != null) {
            return
        }
        assertInMainThread()
        startListenTime = System.currentTimeMillis()
        MediaContentObserver(context, MediaStore.Images.Media.INTERNAL_CONTENT_URI, uiHandler).let {
            internalObserver = it
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI, false,
                it
            )
        }
        MediaContentObserver(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, uiHandler).let {
            externalObserver = it
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false,
                it
            )
        }
    }

    /**停止监听*/
    @MainThread
    fun stopListen(context: Context = lastContext) {
        assertInMainThread()
        internalObserver?.let {
            try {
                context.contentResolver.unregisterContentObserver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            internalObserver = null
        }
        externalObserver?.let {
            try {
                context.contentResolver.unregisterContentObserver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            externalObserver = null
        }
        startListenTime = 0L
    }

    /**处理监听的uri, 判断是否是截图
     * Permission Denial: reading com.android.providers.media.MediaProvider uri content://media/external/images/media from pid=13343, uid=10121
     * requires android.permission.READ_EXTERNAL_STORAGE, or grantUriPermission()
     * */
    private fun handleMediaContentChange(context: Context, contentUri: Uri) {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                contentUri,
                if (Build.VERSION.SDK_INT < 16) MEDIA_PROJECTIONS else MEDIA_PROJECTIONS_API_16,
                null as String?,
                null as Array<String?>?,
                "date_added desc limit 1"
            )
            if (cursor == null) {
                L.e("Deviant logic.")
                return
            }
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val dateTakenIndex = cursor.getColumnIndex(DATE_TAKEN)
                var widthIndex = -1
                var heightIndex = -1
                if (Build.VERSION.SDK_INT >= 16) {
                    widthIndex = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                    heightIndex = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
                }
                val data = cursor.getString(index)
                val dateTaken = cursor.getLong(dateTakenIndex)
                val width: Int
                val height: Int
                if (widthIndex >= 0 && heightIndex >= 0) {
                    width = cursor.getInt(widthIndex)
                    height = cursor.getInt(heightIndex)
                } else {
                    val size = getImageSize(data)
                    width = size.x
                    height = size.y
                }
                handleMediaRowData(data, dateTaken, width, height)
                return
            }
            L.i("Cursor no data.")
        } catch (e: Exception) {
            e.printStackTrace()
            return
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
    }

    private fun getImageSize(imagePath: String): Point {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        return Point(options.outWidth, options.outHeight)
    }

    /**处理截图的数据, 回调监听.
     * [String]
     * [Bitmap]*/
    private fun handleMediaRowData(path: String, dateTaken: Long, width: Int, height: Int) {
        if (checkScreenShot(path, dateTaken, width, height)) {
            L.i("ScreenShot: path = $path; size = $width * $height; date = $dateTaken")
            screenShotListener?.onScreenshot(path)
            screenShotPathData.postValue(path)
        } else {
            //ScreenShotListenManager Media content changed,
            // but not screenshot: path = /storage/emulated/0/Pictures/Screenshots/Screenshot_20221112-125620.jpg;
            // size = 1080 * 1920; date = 1668228980981
            L.i("Media content changed, but not screenshot: path = $path; size = $width * $height; date = $dateTaken")
        }
    }

    /**检查是否是截图的文件路径*/
    private fun checkScreenShot(path: String, dateTaken: Long, width: Int, height: Int): Boolean {
        var _path = path
        val currentTime = System.currentTimeMillis() - dateTaken
        val realSize = _realSize
        return if (dateTaken >= startListenTime && currentTime <= 20000L) {
            if (width <= realSize.x && height <= realSize.y ||
                height <= realSize.x && width <= realSize.y
            ) {
                if (TextUtils.isEmpty(_path)) {
                    false
                } else {
                    _path = _path.lowercase(Locale.getDefault())
                    val var11 = KEYWORDS
                    val var10 = KEYWORDS.size
                    for (var9 in 0 until var10) {
                        val keyWork = var11[var9]
                        if (_path.contains(keyWork)) {
                            return true
                        }
                    }
                    false
                }
            } else {
                false
            }
        } else {
            false
        }
    }

    private inner class MediaContentObserver(
        val context: Context,
        val uri: Uri,
        handler: Handler?
    ) : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)

            if (haveSdCardPermission(context)) {
                handleMediaContentChange(context, uri)
            } else {
                context.requestSdCardPermission {
                    if (it) {
                        handleMediaContentChange(context, uri)
                    }
                }
            }
        }
    }

    /**[screenShotListener]
     * [screenShotData]*/
    interface OnScreenShotListener {
        fun onScreenshot(path: String?)
    }
}
