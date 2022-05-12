package com.angcyo.loader

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
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
        var ALL_QUERY_URI = MediaStore.Files.getContentUri(VOLUME_EXTERNAL)
        var ONLY_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var ONLY_VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        var ONLY_AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        const val WIDTH = "width"
        const val HEIGHT = "height"

        //有些字段 高版本才提供
        const val ORIENTATION = "orientation"
        const val DURATION = "duration"

        /**
         * 全部媒体数据 - PROJECTION
         * 需要返回的数据库字段
         */
        var ALL_PROJECTION = arrayOf(
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

        /**仅加载图片时的字段, 没有[MediaStore.MediaColumns.MIME_TYPE] [DURATION]字段*/
        var IMAGE_PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
            WIDTH,
            HEIGHT,
            ORIENTATION,
            MediaStore.MediaColumns.DATE_MODIFIED
        )

        /**仅加载图片时的字段, 没有[MediaStore.MediaColumns.MIME_TYPE] [ORIENTATION]字段*/
        var VIDEO_PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
            WIDTH,
            HEIGHT,
            DURATION,
            MediaStore.MediaColumns.DATE_MODIFIED
        )

        /**仅加载音频时的字段, 没有[MediaStore.MediaColumns.MIME_TYPE] [ORIENTATION] [WIDTH] [HEIGHT]字段*/
        var AUDIO_PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
            DURATION,
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
                LTime.tick()
                val selection = selectionCreator.createSelection(_loaderConfig)
                onLoaderStart()
                val uri = when (_loaderConfig.mediaLoaderType) {
                    //仅加载图片
                    LoaderConfig.LOADER_TYPE_IMAGE -> ONLY_IMAGE_URI
                    //仅加载视频
                    LoaderConfig.LOADER_TYPE_VIDEO -> ONLY_VIDEO_URI
                    //仅加载音频
                    LoaderConfig.LOADER_TYPE_AUDIO -> ONLY_AUDIO_URI
                    else -> ALL_QUERY_URI
                }
                val projection = when (_loaderConfig.mediaLoaderType) {
                    LoaderConfig.LOADER_TYPE_IMAGE -> IMAGE_PROJECTION
                    LoaderConfig.LOADER_TYPE_VIDEO -> VIDEO_PROJECTION
                    LoaderConfig.LOADER_TYPE_AUDIO -> AUDIO_PROJECTION
                    else -> ALL_PROJECTION
                }
                L.i("DslLoader创建加载器:${_loaderConfig.mediaLoaderType}->$selection")
                return CursorLoader(
                    _activity, uri, projection,
                    selection,
                    null,
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
                )
            }

            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                val count = data?.count ?: 0
                L.w("DslLoader媒体数量:$count")
                launchGlobal {
                    var allFolder = listOf<LoaderFolder>()
                    onBack {
                        val allMedias = mutableListOf<LoaderMedia>()
                        if (data != null && count > 0) {
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
                            L.w("DslLoader耗时:${LTime.time()} $count")
                            allFolder =
                                folderCreator.creatorFolder(_activity, _loaderConfig, allMedias)
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

        val id = data.getLongOrDef(ALL_PROJECTION[0], -1)
        val path = data.getStringOrDef(ALL_PROJECTION[1], "")
        val displayName = data.getStringOrDef(ALL_PROJECTION[2], "")
        val addTime = data.getLongOrDef(ALL_PROJECTION[3], -1)
        val mimeType = data.getStringOrDef(
            ALL_PROJECTION[4], when (_loaderConfig.mediaLoaderType) {
                LoaderConfig.LOADER_TYPE_IMAGE -> "image/*"
                LoaderConfig.LOADER_TYPE_VIDEO -> "video/*"
                LoaderConfig.LOADER_TYPE_AUDIO -> "audio/*"
                else -> ""
            }
        )

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

        val uri = if (path.isFilePath()) { //isFileExist
            Uri.fromFile(File(path))
        } else {
            loaderUri
        }

        val size = data.getLongOrDef(ALL_PROJECTION[5], -1)
        val width = data.getIntOrDef(ALL_PROJECTION[6], -1)
        val height = data.getIntOrDef(ALL_PROJECTION[7], -1)
        val duration = data.getLongOrDef(ALL_PROJECTION[8], -1)
        val orientation = data.getIntOrDef(ALL_PROJECTION[9], 0)
        val modifyTime = data.getLongOrDef(ALL_PROJECTION[10], -1)

        //经纬度Android Q中查询会崩溃, 需要通过Exif查询
        val latitude = 0.0
        val longitude = 0.0

        val loaderMedia = LoaderMedia().apply {
            this.id = "$id"
            this.localUri = uri
            this.loaderUri = loaderUri
            this.localPath = path
            this.displayName = displayName
            this.addTime = addTime
            this.modifyTime = modifyTime
            this.mimeType = mimeType
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

    fun Cursor.getLongOrDef(columnName: String, def: Long): Long {
        val index = getColumnIndex(columnName)
        return getLongOrNull(index) ?: def
    }

    fun Cursor.getIntOrDef(columnName: String, def: Int): Int {
        val index = getColumnIndex(columnName)
        return getIntOrNull(index) ?: def
    }

    fun Cursor.getStringOrDef(columnName: String, def: String): String {
        val index = getColumnIndex(columnName)
        return getStringOrNull(index) ?: def
    }

}