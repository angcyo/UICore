package com.angcyo.loader

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.angcyo.coroutine.launch
import com.angcyo.coroutine.onBack
import com.angcyo.library.L
import com.angcyo.library.LTime
import com.angcyo.library.ex.fd
import com.angcyo.library.ex.isDebug
import kotlinx.coroutines.async

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

        val VOLUME_EXTERNAL = "external"

        //查询所有媒体的uri
        val ALL_QUERY_URI = MediaStore.Files.getContentUri(VOLUME_EXTERNAL)

        const val WIDTH = "width"
        const val HEIGHT = "height"
        //有些字段 高版本才提供
        const val ORIENTATION = "orientation"
        const val DURATION = "duration"

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
            DURATION,
            ORIENTATION
        )
    }

    var _loaderManager: LoaderManager? = null
    var _activity: FragmentActivity? = null
    var _loaderConfig: LoaderConfig? = null

    /**查询语句创建器*/
    var selectionCreator = SelectionCreator()
    var folderCreator = FolderCreator()

    var loaderExif: Boolean = false

    var onLoaderStart: () -> Unit = {}

    /**加载完成回调*/
    var onLoaderResult: (List<LoaderFolder>) -> Unit = {}

    /**load回调*/
    val _loaderCallback: LoaderManager.LoaderCallbacks<Cursor> =
        object : LoaderManager.LoaderCallbacks<Cursor> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                onLoaderStart()
                return CursorLoader(
                    _activity!!, ALL_QUERY_URI, ALL_PROJECTION,
                    selectionCreator.createSelection(_loaderConfig!!),
                    null,
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
                )
            }

            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                val count = data?.count ?: 0
                var allFolder = listOf<LoaderFolder>()
                launch {
                    onBack {
                        val allMedias = mutableListOf<LoaderMedia>()
                        if (data != null && count > 0) {
                            LTime.tick()
                            data.moveToFirst()
                            do {
                                val id = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[0]))
                                val uri = MediaStore.Files.getContentUri(VOLUME_EXTERNAL, id)

                                val mimeType =
                                    data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[4]))

                                val path =
                                    data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[1]))
                                val displayName =
                                    data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[2]))
                                val addTime =
                                    data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[3]))
                                val size =
                                    data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[5]))
                                val width =
                                    data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[6]))
                                val height =
                                    data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[7]))

                                //经纬度Android Q中查询会崩溃, 需要通过Exif查询
                                var latitude = 0.0
                                var longitude = 0.0
                                var orientation =
                                    data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[9]))

                                val duration =
                                    data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[8]))

                                val loaderMedia = LoaderMedia().apply {
                                    this.id = id
                                    this.localUri = uri
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

                                if (loaderExif) {
                                    async {
                                        try {
                                            val exif = ExifInterface(uri.fd(_activity)!!)
                                            exif.latLong?.run {
                                                latitude = this[0]
                                                longitude = this[0]
                                            }
                                            val orientationAttr: Int = exif.getAttributeInt(
                                                ExifInterface.TAG_ORIENTATION,
                                                ExifInterface.ORIENTATION_NORMAL
                                            )
                                            if (orientationAttr == ExifInterface.ORIENTATION_NORMAL || orientationAttr == ExifInterface.ORIENTATION_UNDEFINED) {
                                                orientation = 0
                                            } else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_90) {
                                                orientation = 90
                                            } else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_180) {
                                                orientation = 180
                                            } else if (orientationAttr == ExifInterface.ORIENTATION_ROTATE_270) {
                                                orientation = 270
                                            }
                                            loaderMedia.latitude = latitude
                                            loaderMedia.longitude = longitude
                                            loaderMedia.orientation = orientation
                                        } catch (e: Exception) {
                                            L.w(path, " ", e)
                                        }
                                    }
                                }

                                allMedias.add(loaderMedia)
                                //L.i(loaderMedia, " ", path.file().canRead())
                            } while (data.moveToNext())
                            L.w("耗时:${LTime.time()} $count")
                            allFolder = folderCreator.creatorFolder(_loaderConfig!!, allMedias)
                        }
                    }.await()
                    onLoaderResult.invoke(allFolder)
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

    fun destroyLoader() {
        _loaderManager?.destroyLoader(LOADER_ID)
    }
}