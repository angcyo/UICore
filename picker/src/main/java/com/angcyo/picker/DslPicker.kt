package com.angcyo.picker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.angcyo.DslAHelper
import com.angcyo.base.checkAndRequestPermission
import com.angcyo.fragment.FragmentBridge
import com.angcyo.fragment.IFragmentBridge
import com.angcyo.library.L
import com.angcyo.library.ex.fileUri
import com.angcyo.library.ex.loadUrl
import com.angcyo.library.ex.takePhotoIntent
import com.angcyo.library.ex.takeVideoIntent
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.fileName
import com.angcyo.library.utils.filePath
import com.angcyo.loader.LoaderConfig
import com.angcyo.picker.DslPicker.picker
import com.angcyo.picker.core.PickerActivity
import com.angcyo.picker.core.PickerActivity.Companion.KEY_LOADER_CONFIG
import com.angcyo.picker.core.PickerActivity.Companion.KEY_SELECTOR_MEDIA_LIST
import com.angcyo.picker.core.PickerActivity.Companion.PICKER_REQUEST_CODE
import java.io.File


/**
 * 图片选择器
 * https://github.com/LuckSiege/PictureSelector
 *
 * 图片加载库
 * https://github.com/panpf/sketch
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/31
 */
object DslPicker {

    /**开始选择器*/
    fun picker(context: Context?, config: LoaderConfig) {
        context?.run {
            DslAHelper(this).apply {
                start(PickerActivity::class.java) {
                    requestCode = config.requestCode
                    intent.putExtra(KEY_LOADER_CONFIG, config)
                    enterAnim = R.anim.lib_picker_enter_anim
                    exitAnim = R.anim.lib_picker_other_exit_anim
                }
                doIt()
            }
        }
    }

    /**开始选择器*/
    fun picker(context: Context?, action: LoaderConfig.() -> Unit) {
        val config = LoaderConfig()
        config.action()
        picker(context, config)
    }

    /**获取选择的媒体数据*/
    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        requestCodeRaw: Int = PICKER_REQUEST_CODE
    ): List<LoaderMedia>? {
        if (data == null || resultCode != Activity.RESULT_OK || requestCode != requestCodeRaw) {
            return null
        }
        return data.getParcelableArrayListExtra(KEY_SELECTOR_MEDIA_LIST)
    }

    /**拍照
     * [com.angcyo.component.getPhoto]
     * [com.angcyo.picker.DslPicker.takePhoto]
     * */
    fun takePhoto(activity: FragmentActivity?, action: (Uri?) -> Unit) {
        activity?.checkAndRequestPermission(arrayOf(Manifest.permission.CAMERA)) { grant ->
            if (grant) {
                val filePath = filePath(Constant.CAMERA_FOLDER_NAME, fileName(suffix = ".jpeg"))
                val uri = fileUri(activity, File(filePath))
                takePhotoIntent(activity, uri)?.run {
                    FragmentBridge.install(activity.supportFragmentManager)
                        .startActivityForResult(
                            this,
                            FragmentBridge.generateCode(),
                            null,
                            object : IFragmentBridge {
                                override fun onActivityResult(resultCode: Int, data: Intent?) {
                                    if (data?.data == null) {
                                        data?.data = uri
                                    }
                                    if (resultCode == Activity.RESULT_OK) {
                                        //不指定uri时, 可以使用此方式获取缩略图
                                        //https://developer.android.google.cn/training/camera/photobasics#TaskPhotoView
                                        //val imageBitmap = data?.extras?.get("data") as Bitmap
                                        L.i(uri.loadUrl())
                                        //activity.scanFile2(uri)
                                        action(uri)
                                    } else {
                                        action(null)
                                    }
                                }
                            })
                }
            } else {
                toastQQ("相机权限被禁用,请在权限设置中打开.")
            }
        }
    }

    /**从摄像头获取图片对象[Bitmap]*/
    fun takePhotoBitmap(activity: FragmentActivity?, action: (Bitmap?) -> Unit) {
        activity?.checkAndRequestPermission(arrayOf(Manifest.permission.CAMERA)) { grant ->
            if (grant) {
                takePhotoIntent(activity, null)?.run {
                    FragmentBridge.install(activity.supportFragmentManager)
                        .startActivityForResult(
                            this,
                            FragmentBridge.generateCode(),
                            null,
                            object : IFragmentBridge {
                                override fun onActivityResult(resultCode: Int, data: Intent?) {
                                    val bitmap: Bitmap? =
                                        data.takeIf { resultCode == Activity.RESULT_OK }
                                            ?.getParcelableExtra("data")
                                    action(bitmap)
                                }
                            })
                }
            } else {
                toastQQ("相机权限被禁用,请在权限设置中打开.")
            }
        }
    }

    /**录像*/
    fun takeVideo(
        activity: FragmentActivity?,
        videoQuality: Int = 1, //视频质量, 0:低质量, 1:高质量
        maxSize: Long = Long.MAX_VALUE, //字节
        maxDuration: Int = -1,//秒,
        action: (Uri?) -> Unit
    ) {
        activity?.checkAndRequestPermission(arrayOf(Manifest.permission.CAMERA)) { grant ->
            if (grant) {
                val filePath = filePath(Constant.CAMERA_FOLDER_NAME, fileName(suffix = ".mp4"))
                val uri = fileUri(activity, File(filePath))
                takeVideoIntent(activity, uri, videoQuality, maxSize, maxDuration)?.run {
                    FragmentBridge.install(activity.supportFragmentManager)
                        .startActivityForResult(this,
                            FragmentBridge.generateCode(),
                            null,
                            object : IFragmentBridge {
                                override fun onActivityResult(resultCode: Int, data: Intent?) {
                                    if (data?.data == null) {
                                        data?.data = uri
                                    }
                                    if (resultCode == Activity.RESULT_OK) {
                                        //https://developer.android.google.cn/training/camera/videobasics#TaskVideoView
                                        //不指定uri, 可以使用以下方式获取uri
                                        //val videoUri: Uri = intent.data
                                        L.i(uri.loadUrl())
                                        //activity.scanUri(uri)
                                        action(uri)
                                    } else {
                                        action(null)
                                    }
                                }
                            })
                }
            } else {
                toastQQ("相机权限被禁用,请在权限设置中打开.")
            }
        }
    }
}

/**使用[LoaderConfig]启动媒体选择器*/
fun Fragment.dslPicker(config: LoaderConfig) {
    picker(context, config)
}

/**配置启动媒体选择器*/
fun Fragment.dslPicker(action: LoaderConfig.() -> Unit) {
    picker(context, action)
}

fun Fragment.dslPicker(
    config: LoaderConfig.() -> Unit,
    onResult: (List<LoaderMedia>?) -> Unit
) {
    dslPicker(LoaderConfig().apply(config), onResult)
}

/**选择图片和视频*/
fun Fragment.dslPickerImageVideo(
    count: Int = 1,
    videoMinDuration: Long = 3 * 1000L, //3秒的视频
    videoMaxDuration: Long = 30 * 1000L, //30秒的视频
    config: LoaderConfig.() -> Unit = {},
    onResult: (List<LoaderMedia>?) -> Unit
) {
    dslPicker(LoaderConfig().apply {
        mediaLoaderType = LoaderConfig.LOADER_TYPE_IMAGE or LoaderConfig.LOADER_TYPE_VIDEO
        maxSelectorLimit = count
        enableImageEdit = false
        enableCamera = false
        limitVideoMinDuration = videoMinDuration
        limitVideoMaxDuration = videoMaxDuration
        config()
    }, onResult)
}

fun Fragment.dslPicker(config: LoaderConfig, onResult: (List<LoaderMedia>?) -> Unit) {
    context?.dslPicker(parentFragmentManager, config, onResult)
}

fun Context.dslPicker(
    fragmentManager: FragmentManager,
    config: LoaderConfig,
    onResult: (List<LoaderMedia>?) -> Unit
) {
    DslAHelper(this).apply {
        start(PickerActivity::class.java) {
            requestCode = config.requestCode
            intent.putExtra(KEY_LOADER_CONFIG, config)
            enterAnim = R.anim.lib_picker_enter_anim
            exitAnim = R.anim.lib_picker_other_exit_anim

            FragmentBridge.install(fragmentManager)
                .startActivityForResult(intent, requestCode) { resultCode, data ->
                    onResult(
                        DslPicker.onActivityResult(
                            requestCode,
                            resultCode,
                            data,
                            requestCode
                        )
                    )
                }
        }
    }
}

/**简单的选择图片*/
fun Fragment.dslSinglePickerImage(
    count: Int = 1,
    config: LoaderConfig.() -> Unit = {},
    onResult: (List<LoaderMedia>?) -> Unit
) {
    dslPicker(LoaderConfig().apply {
        mediaLoaderType = LoaderConfig.LOADER_TYPE_IMAGE
        maxSelectorLimit = count
        enableImageEdit = true
        enableCamera = true
        config()
    }, onResult)
}

fun Context.dslSinglePickerImage(
    fragmentManager: FragmentManager,
    count: Int = 1,
    config: LoaderConfig.() -> Unit = {},
    onResult: (List<LoaderMedia>?) -> Unit
) {
    dslPicker(fragmentManager, LoaderConfig().apply {
        mediaLoaderType = LoaderConfig.LOADER_TYPE_IMAGE
        maxSelectorLimit = count
        enableImageEdit = true
        enableCamera = true
        config()
    }, onResult)
}