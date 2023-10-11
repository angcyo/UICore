package com.angcyo.core.component

import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import com.angcyo.base.requestSdCardPermission
import com.angcyo.core.component.file.writeToLog
import com.angcyo.core.vmApp
import com.angcyo.http.base.toJson
import com.angcyo.http.rx.runRx
import com.angcyo.library.*
import com.angcyo.library.component.RBackground
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.lastName
import com.angcyo.library.ex.nowTimeString
import com.angcyo.library.ex.saveToFolder
import com.angcyo.library.ex.shareFile
import com.angcyo.library.ex.zip
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.fileNameTime
import com.angcyo.library.utils.logFileName
import com.angcyo.library.utils.logPath
import com.angcyo.library.utils.storage.SD
import com.angcyo.library.utils.storage.haveSdCardPermission
import com.angcyo.viewmodel.vmDataOnce
import com.orhanobut.hawk.HawkValueParserHelper
import java.util.*


/**
 * 截屏通知, 截图分享
 *
 * ```
 * //截图分享
 *  vmApp<ScreenShotModel>().apply {
 *    startListen()
 *    screenShotPathData.observeForever { path->
 *       if (!path.isNullOrBlank() && !RBackground.isBackground()) {
 *              renderLayout(R.layout.core_screen_shot_share_layout) {
 *                  renderLayoutAction = {
 *                      img(R.id.lib_image_view)?.loadImage(path)
 *                      click(R.id.lib_close_view) {
 *                      DslLayout.hide(this@renderLayout)
 *                   }
 *                 clickItem {
 *                    DslLayout.hide(this@renderLayout)
 *                    shareEngraveLog()
 *                 }
 *               }
 *           }
 *       }
 *     }
 *  }
 * ```
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/11/12
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
@TargetApi(17)
class ScreenShotModel : ViewModel() {

    companion object {

        private val DATE_TAKEN = "datetaken"

        private val MEDIA_PROJECTIONS =
            arrayOf(MediaStore.Images.Media._ID, MediaStore.MediaColumns.DATA, DATE_TAKEN)

        private val MEDIA_PROJECTIONS_API_16 = arrayOf(
            MediaStore.Images.Media._ID,
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

        /**获取基础分享日志的路径集合*/
        fun getBaseLogShareList(): List<String> {
            val result = mutableListOf<String>()
            result.add(logPath())

            //http log
            result.add(libAppFile(logFileName(), Constant.HTTP_FOLDER_NAME).absolutePath)
            //crash log
            result.add(
                libAppFile(
                    fileNameTime(suffix = ".log"),
                    Constant.CRASH_FOLDER_NAME
                ).absolutePath
            )

            Library.hawkPath?.let {
                val map = HawkValueParserHelper.parseFromXml(it)
                map.toJson()?.let {
                    libCacheFile("Hawk2.json").apply {
                        writeText(it)
                        result.add(absolutePath)
                    }
                }
            }
            return result
        }

        /**截屏之后, 触发日志分享*/
        fun startListenScreenShotShareLog(getLogListAction: () -> List<String> = { emptyList() }) {
            vmApp<ScreenShotModel>().apply {
                ignorePermission = true
                startListen()
                screenShotPathData.observeForever { path ->
                    if (!path.isNullOrBlank() && !RBackground.isBackground()) {
                        toastQQ("请稍等...")
                        runRx({
                            val logList = mutableListOf<String>()
                            logList.addAll(getBaseLogShareList())
                            logList.addAll(getLogListAction())

                            logList.zip(libCacheFile(buildString {
                                append(getAppName())
                                append("_${getAppVersionCode()}")
                                append("_${Build.MODEL}")
                                append("_")
                                append(nowTimeString("yyyy-MM-dd_HH-mm-ss"))
                                append(".zip")
                            }).absolutePath)?.shareFile()
                        })
                    }
                }
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

    /**是否忽略权限请求, 只要回调*/
    var ignorePermission = false

    /**开始监听
     * [context] 如果需要自动处理sd卡权限, 请使用[Activity]上下文*/
    @MainThread
    fun startListen(context: Context = lastContext) {
        if (internalObserver != null) {
            return
        }
        assertInMainThread()
        startListenTime = System.currentTimeMillis()
        MediaContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, uiHandler).let {
            internalObserver = it
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI, true,
                it
            )
        }
        MediaContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, uiHandler).let {
            externalObserver = it
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
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
            cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val queryArgs = Bundle()
                /*queryArgs.putString(
                    ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                    MediaStore.MediaColumns.DATE_ADDED
                )*/
                queryArgs.putStringArray(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(MediaStore.MediaColumns.DATE_ADDED)
                )//排序列
                queryArgs.putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )//查询方向, 逆序
                queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, 1)//查询数量相知
                context.contentResolver.query(contentUri, MEDIA_PROJECTIONS_API_16, queryArgs, null)
            } else {
                context.contentResolver.query(
                    contentUri,
                    if (Build.VERSION.SDK_INT < 16) MEDIA_PROJECTIONS else MEDIA_PROJECTIONS_API_16,
                    null as String?,
                    null as Array<String?>?,
                    "${MediaStore.MediaColumns.DATE_ADDED} desc limit 1"
                )
            }
            if (cursor == null) {
                L.e("Deviant logic.")
                return
            }
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val dateTakenIndex = cursor.getColumnIndex(DATE_TAKEN)
                var widthIndex = -1
                var heightIndex = -1
                if (Build.VERSION.SDK_INT >= 16) {
                    widthIndex = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH)
                    heightIndex = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
                }
                val id = cursor.getLong(idIndex)
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

                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.getContentUri("external"),
                    id
                )

                //这里数据流转存到cache目录下, 在vivo手机上虽然有SD卡权限, 但是还是无法直接加载这个路径
                val name = data.lastName()
                val cachePath = uri.saveToFolder(fileName = name)

                handleMediaRowData(cachePath, dateTaken, width, height)
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

    private inner class MediaContentObserver(val uri: Uri, handler: Handler?) :
        ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val permissions = SD.mediaPermissions(true)
            val haveSdCardPermission = haveSdCardPermission(permissions)
            "onChange[$haveSdCardPermission]:$selfChange $uri".writeToLog(logLevel = L.INFO)

            val context = lastContext
            if (haveSdCardPermission || ignorePermission) {
                handleMediaContentChange(context, uri)
            } else {
                context.requestSdCardPermission(permissions) {
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
