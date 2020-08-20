package com.angcyo.camerax

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.angcyo.camerax.dslitem.DslCameraViewHelper
import com.angcyo.core.fragment.BaseFragment
import com.angcyo.dialog.hideLoading
import com.angcyo.dialog.loadingDialog
import com.angcyo.library.ex.file
import com.angcyo.library.ex.have
import com.angcyo.library.ex.save
import com.angcyo.library.ex.toBitmap
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.fileNameUUID
import com.angcyo.library.utils.filePath
import com.angcyo.media.video.record.control.PreviewPictureLayoutControl
import com.angcyo.media.video.record.control.PreviewVideoLayoutControl
import com.angcyo.media.video.record.inner.RecordVideoCallback
import com.angcyo.widget.layout.ExpandRecordLayout
import java.io.File

/**
 * 使用[CameraX]实现的视频录制, 手动控制移除[CameraXRecordVideoFragment]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020-4-23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class CameraXRecordVideoFragment : BaseFragment() {
    var callback: RecordVideoCallback? = null

    /**预览控制*/
    var _previewPictureLayoutControl: PreviewPictureLayoutControl? = null
    var _previewVideoLayoutControl: PreviewVideoLayoutControl? = null

    /**记录总录制时长, 秒 */
    var _recordTime = 0

    /**拍照, 录像助手*/
    val dslCameraViewHelper = DslCameraViewHelper()

    init {
        fragmentLayoutId = R.layout.camerax_fragment_record_video
    }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)
        _vh.enable(R.id.record_control_layout, false)
        _vh.itemView.keepScreenOn = true
        if (haveCameraPermission()) {
            _vh.postDelay(360, Runnable { onDelayInitView() })
        } else {
            requestPermission()
        }
    }

    private fun onDelayInitView() {
        dslCameraViewHelper.cameraView = _vh.v(R.id.lib_camera_view)
        dslCameraViewHelper.cameraView?.bindToLifecycle(this)

        _previewPictureLayoutControl =
            PreviewPictureLayoutControl(_vh.view(R.id.camera_confirm_layout)!!)
        _previewVideoLayoutControl =
            PreviewVideoLayoutControl(_vh.view(R.id.video_confirm_layout)!!)

        val recordLayout: ExpandRecordLayout? = _vh.v(R.id.record_control_layout)
        callback?.initConfig() ?: return

        recordLayout?.apply {
            maxTime = callback?.maxRecordTime ?: 10
            listener = object : ExpandRecordLayout.OnRecordListener() {
                override fun onTouchDown(): Boolean {
                    if (checkPermission()) {
                        _vh.enable(R.id.record_control_layout, true)
                        return true
                    }
                    _vh.enable(R.id.record_control_layout, false)
                    requestPermission()
                    return false
                }

                override fun onTick(layout: ExpandRecordLayout) {
                    if (callback!!.takeModel.have(RecordVideoCallback.TAKE_MODEL_PHOTO)) {
                        fContext().loadingDialog(R.layout.camerax_loading_layout) {
                            loadingText = "处理中..."
                        }
                        //拍照
                        dslCameraViewHelper.takePicture { file, exception ->
                            hideLoading()
                            if (exception == null && isResumed) {
                                onTakePhoto(file.toBitmap())
                            }
                        }
                    }
                }

                override fun onRecordStart() {
                    super.onRecordStart()

                    dslCameraViewHelper.startRecording { file, exception ->
                        if (exception == null && isResumed) {
                            onRecordFinish(file.absolutePath)
                        }
                    }
                }

                override fun onRecording(progress: Int) {
                    super.onRecording(progress)
                }

                override fun onRecordEnd(progress: Int) {
                    super.onRecordEnd(progress)
                    _recordTime = progress
                    dslCameraViewHelper.stopRecording()
                }

                override fun onExpandStateChange(fromState: Int, toState: Int) {
                    super.onExpandStateChange(fromState, toState)
                }
            }
            recordLayout.enableLongPress =
                callback!!.takeModel.have(RecordVideoCallback.TAKE_MODEL_VIDEO)

            when (callback!!.takeModel) {
                RecordVideoCallback.TAKE_MODEL_PHOTO -> {
                    recordLayout.drawTipString = callback!!.modelPhotoText
                }
                RecordVideoCallback.TAKE_MODEL_VIDEO -> {
                    recordLayout.drawTipString = callback!!.modelVideoText
                }
                else -> {
                    recordLayout.drawTipString = callback!!.modelPhotoVideoText
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        _vh.gone(R.id.camera_confirm_layout)
        _vh.gone(R.id.video_confirm_layout)
        if (_previewVideoLayoutControl != null) {
            _previewVideoLayoutControl!!.stop()
        }
    }

    /**录像之后的处理流程*/
    fun onRecordFinish(videoPath: String) {
        if (_recordTime < callback!!.minRecordTime) {
            toastQQ("至少需要录制 " + callback!!.minRecordTime + " 秒")
            return
        }

        val targetFile = File(callback!!.onTakeVideoAfter(videoPath))

        val newFileName: String = fileNameUUID("_t_$_recordTime.mp4")
        val newFilePath = targetFile.parent!! + File.separator + newFileName

        targetFile.renameTo(newFilePath.file())

        //L.i("文件:" + new File(videoPath).exists());
        _previewVideoLayoutControl!!.showPreview(newFilePath, Runnable {

        })
        _vh.click(R.id.video_confirm_button, View.OnClickListener {
//            dslFHelper {
//                finishActivityOnLastFragmentRemove = false
//                remove(this@RecordVideoFragment)
//            }
            callback!!.onTakeVideo(newFilePath)
        })
    }

    /**拍摄图片之后的处理流程*/
    fun onTakePhoto(bitmap: Bitmap) {
        try {
            var result: Bitmap = bitmap
            val width = bitmap.width
            val height = bitmap.height
            val builder = StringBuilder()
            builder.append("_s_")
            builder.append(width)
            builder.append("x")
            builder.append(height)
            builder.append(".jpg")
            val outputFile =
                filePath(Constant.CAMERA_FOLDER_NAME, fileNameUUID(builder.toString())).file()!!
            var oldBitmap = bitmap
            //水印处理
            result = callback!!.onTakePhotoBefore(bitmap, width, height)
            if (oldBitmap != result && !oldBitmap.isRecycled) {
                oldBitmap.recycle()
            }
            //显示图片预览
            showPhotoPreview(result, outputFile)
            oldBitmap = result
            result = callback!!.onTakePhotoAfter(result, width, height)
            if (oldBitmap != result && !oldBitmap.isRecycled) {
                oldBitmap.recycle()
            }

            result.save(outputFile.absolutePath, Bitmap.CompressFormat.JPEG, 70)
//            Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            Uri fileContentUri = Uri.fromFile(mediaFile);
//            mediaScannerIntent.setData(fileContentUri);
//            getActivity().sendBroadcast(mediaScannerIntent);
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun showPhotoPreview(bitmap: Bitmap, outputFile: File) {
        _previewPictureLayoutControl!!.showPreview(bitmap)
        _vh.click(
            R.id.camera_confirm_button,
            View.OnClickListener {
//                dslFHelper {
//                    finishActivityOnLastFragmentRemove = false
//                    remove(this@RecordVideoFragment)
//                }
                callback!!.onTakePhoto(bitmap, outputFile)
            })
    }

    private fun haveCameraPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            fContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * 是否有权限
     */
    private fun checkPermission(): Boolean {
        return (haveCameraPermission() && ActivityCompat.checkSelfPermission(
            fContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ),
            999
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermission()) {
            onDelayInitView()
        }
    }
}