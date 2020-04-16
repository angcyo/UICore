package com.angcyo.loader

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.angcyo.coroutine.launchGlobal
import com.angcyo.coroutine.onBack
import com.angcyo.library.L
import com.angcyo.library.LTime
import com.angcyo.library.ex.*
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.isImage
import kotlinx.coroutines.async
import java.io.File

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
        val ALL_PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            WIDTH,
            HEIGHT,
            DURATION,
            ORIENTATION,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
    }

    var _loaderManager: LoaderManager? = null
    lateinit var _activity: FragmentActivity
    lateinit var _loaderConfig: LoaderConfig

    /**查询语句创建器*/
    var selectionCreator = SelectionCreator()
    var folderCreator = FolderCreator()

    var onLoaderStart: () -> Unit = {}

    /**加载完成回调*/
    var onLoaderResult: (List<LoaderFolder>) -> Unit = {}

    /**load回调*/
    val _loaderCallback: LoaderManager.LoaderCallbacks<Cursor> =
        object : LoaderManager.LoaderCallbacks<Cursor> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                onLoaderStart()
                return CursorLoader(
                    _activity, ALL_QUERY_URI, ALL_PROJECTION,
                    selectionCreator.createSelection(_loaderConfig),
                    null,
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
                )
            }

            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                val count = data?.count ?: 0
                var allFolder = listOf<LoaderFolder>()
                launchGlobal {
                    onBack {
                        val allMedias = mutableListOf<LoaderMedia>()
                        if (data != null && count > 0) {
                            LTime.tick()
                            data.moveToFirst()
                            do {
                                val loaderMedia = loadFromCursor(data)
                                var latitude = 0.0
                                var longitude = 0.0
                                var orientation = loaderMedia.orientation
                                val uri = loaderMedia.localUri

                                if (loaderMedia.isImage() && _loaderConfig.loaderExif) {
                                    async {
                                        try {
                                            uri?.fd(_activity)?.run {
                                                val exif = ExifInterface(this)
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
                                            }
                                        } catch (e: Exception) {
                                            L.w(loaderMedia.localPath, " ", e)
                                        }
                                    }
                                }
                                allMedias.add(loaderMedia)
                                //L.i(loaderMedia, " ", path.file().canRead())
                            } while (data.moveToNext())
                            L.w("耗时:${LTime.time()} $count")
                            allFolder = folderCreator.creatorFolder(_loaderConfig, allMedias)
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
            _loaderManager = LoaderManager.getInstance(activity).apply {
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

    fun loadFromCursor(data: Cursor): LoaderMedia {

        val id = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[0]))
        val path = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[1]))
        val displayName = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[2]))
        val addTime = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[3]))
        val mimeType = data.getString(data.getColumnIndexOrThrow(ALL_PROJECTION[4]))

        val loaderUri = when {
            mimeType.isImageMimeType() -> ContentUris.withAppendedId(
                MediaStore.Images.Media.getContentUri(VOLUME_EXTERNAL),
                id
            )
            mimeType.isVideoMimeType() -> ContentUris.withAppendedId(
                MediaStore.Video.Media.getContentUri(VOLUME_EXTERNAL),
                id
            )
            mimeType.isAudioMimeType() -> ContentUris.withAppendedId(
                MediaStore.Audio.Media.getContentUri(VOLUME_EXTERNAL),
                id
            )
            else -> MediaStore.Files.getContentUri(VOLUME_EXTERNAL, id)
        }

        val uri = if (path.isFileExist()) {
            Uri.fromFile(File(path))
        } else {
            loaderUri
        }

        val size = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[5]))
        val width = data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[6]))
        val height = data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[7]))
        val duration = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[8]))
        val orientation = data.getInt(data.getColumnIndexOrThrow(ALL_PROJECTION[9]))
        val modifyTime = data.getLong(data.getColumnIndexOrThrow(ALL_PROJECTION[10]))

        //经纬度Android Q中查询会崩溃, 需要通过Exif查询
        val latitude = 0.0
        val longitude = 0.0

        val loaderMedia = LoaderMedia().apply {
            this.id = id
            this.localUri = uri
            this.loaderUri = loaderUri
            this.localPath = path ?: ""
            this.displayName = displayName ?: ""
            this.addTime = addTime
            this.modifyTime = modifyTime
            this.mimeType = mimeType ?: ""
            this.fileSize = size
            this.width = width
            this.height = height
            this.latitude = latitude
            this.longitude = longitude
            this.duration = duration
            this.orientation = orientation
        }

        return loaderMedia
    }
}