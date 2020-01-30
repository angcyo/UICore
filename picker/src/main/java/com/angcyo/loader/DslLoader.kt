package com.angcyo.loader

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.angcyo.library.L
import com.angcyo.library.LTime
import com.angcyo.library.ex.isDebug

/**
 * 媒体加载器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/29
 */
class DslLoader {

    companion object {
        init {
            LoaderManager.enableDebugLogging(isDebug())
        }

        const val LOADER_ID = 0x8899

        //查询所有媒体的uri
        internal val ALL_QUERY_URI = MediaStore.Files.getContentUri("external")

        internal const val WIDTH = "width"
        internal const val HEIGHT = "height"
        internal const val LATITUDE = "latitude"    //纬度
        internal const val LONGITUDE = "longitude"  //经度
        //有些字段 高版本才提供
        internal const val ORIENTATION = "orientation"
        internal const val DURATION = "duration"

        /**
         * 全部媒体数据 - PROJECTION
         * 需要返回的数据库字段
         */
        private val ALL_PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            WIDTH,
            HEIGHT,
            LATITUDE,
            LONGITUDE,
            DURATION,
            ORIENTATION
        )
    }

    var _loaderManager: LoaderManager? = null
    var _activity: FragmentActivity? = null
    var _loaderConfig: LoaderConfig? = null

    /**加载完成回调*/
    var onLoaderFinish: (List<LoaderMedia>) -> Unit = {}

    /**load回调*/
    val _loaderCallback: LoaderManager.LoaderCallbacks<Cursor> =
        object : LoaderManager.LoaderCallbacks<Cursor> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                return CursorLoader(
                    _activity!!, ALL_QUERY_URI, ALL_PROJECTION,
                    _loaderConfig?.getMimeTypeSelectorSelection() + _loaderConfig?.getFileSelectorSelection(),
                    null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
                )
            }

            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                val count = data?.count ?: 0
                val allMedias = mutableListOf<LoaderMedia>()
                if (data != null && count > 0) {
                    LTime.tick()
                    data.moveToFirst()
                    do {
                        val mimeType = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[4]))

                        val path = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[1]))
                        val displayName =
                            data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[2]))
                        val addTime =
                            data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[3]))
                        val size = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[5]))
                        val width = data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[6]))
                        val height = data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[7]))
                        val latitude =
                            data.getDouble(data.getColumnIndexOrThrow(ALL_PROJECTION[8]))
                        val longitude =
                            data.getDouble(data.getColumnIndexOrThrow(ALL_PROJECTION[9]))
                        val duration =
                            data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[10]))
                        val orientation =
                            data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[11]))

                        val loaderMedia = LoaderMedia().apply {
                            this.localPath = path ?: ""
                            this.displayName = displayName ?: ""
                            this.addTime = addTime
                            this.mimeType = mimeType ?: ""
                            this.fileSize = size
                            this.width = width
                            this.height = height
                            this.latitude = latitude
                            this.longitude = longitude
                            this.duration = duration
                            this.orientation = orientation
                        }

                        allMedias.add(loaderMedia)
                        L.i(loaderMedia)
                    } while (data.moveToNext())
                    L.w("耗时:${LTime.time()} $count")

                    onLoaderFinish.invoke(allMedias)
                }
            }

            override fun onLoaderReset(loader: Loader<Cursor>) {
                L.w("loader is reset $loader")
            }
        }

    fun startLoader(activity: FragmentActivity?, loaderConfig: LoaderConfig) {
        if (activity == null) {
            L.w("activity is null")
            return
        }
        _activity = activity
        _loaderConfig = loaderConfig
        if (_loaderManager == null) {
            _loaderManager = LoaderManager.getInstance(activity)
            _loaderManager?.run {
                initLoader(LOADER_ID, null, _loaderCallback)
            }
        } else {
            restartLoader()
        }
    }

    fun restartLoader() {
        _loaderManager?.restartLoader(LOADER_ID, null, _loaderCallback)
    }
}