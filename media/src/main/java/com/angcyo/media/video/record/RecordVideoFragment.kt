package com.angcyo.media.video.record

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.angcyo.core.fragment.BaseFragment
import com.angcyo.library.L.i
import com.angcyo.library.ex.file
import com.angcyo.library.ex.have
import com.angcyo.library.ex.save
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.fileNameUUID
import com.angcyo.library.utils.filePath
import com.angcyo.media.R
import com.angcyo.media.video.record.control.PreviewPictureLayoutControl
import com.angcyo.media.video.record.control.PreviewVideoLayoutControl
import com.angcyo.media.video.record.inner.RecordVideoCallback
import com.angcyo.media.video.record.inner.RecordVideoControl
import com.angcyo.media.video.record.inner.RecordVideoInterface
import com.angcyo.media.video.record.inner.SizeSurfaceView
import com.angcyo.widget.layout.ExpandRecordLayout
import java.io.File

/**
 * 使用[MediaRecorder]实现的视频录制, 手动控制移除[RecordVideoFragment]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class RecordVideoFragment : BaseFragment(), RecordVideoInterface {
    var callback: RecordVideoCallback? = null

    /**预览控制*/
    var _previewPictureLayoutControl: PreviewPictureLayoutControl? = null
    var _previewVideoLayoutControl: PreviewVideoLayoutControl? = null

    /** 记录总录制时长, 秒 */
    var _recordTime = 0

    var _recordView: SizeSurfaceView? = null

    var _recordControl: RecordVideoControl? = null

    init {
        fragmentLayoutId = R.layout.camera_fragment_record_video
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
        _recordView = _vh.v(R.id.recorder_view)
        _previewPictureLayoutControl =
            PreviewPictureLayoutControl(_vh.view(R.id.camera_confirm_layout)!!)
        _previewVideoLayoutControl =
            PreviewVideoLayoutControl(_vh.view(R.id.video_confirm_layout)!!)
        _recordControl =
            RecordVideoControl(requireActivity(), _recordView!!, this@RecordVideoFragment)
        if (isResumed) {
            _recordControl!!.surfaceChanged(
                _recordView!!.holder,
                0,
                _recordView!!.width,
                _recordView!!.height
            )
        }
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
                        //拍照
                        _recordControl!!.takePhoto()
                    }
                }

                override fun onRecordStart() {
                    super.onRecordStart()
                    if (!_recordControl!!.isRecording) {
                        _recordTime = 0
                        _recordControl!!.startRecording(
                            filePath(
                                Constant.CAMERA_FOLDER_NAME,
                                fileNameUUID(".mp4")
                            )
                        )
                    }
                }

                override fun onRecording(progress: Int) {
                    super.onRecording(progress)
                }

                override fun onRecordEnd(progress: Int) {
                    super.onRecordEnd(progress)
                    _recordTime = progress
                    _recordControl!!.stopRecording(progress >= callback!!.minRecordTime)
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

    override fun startRecord() {}

    override fun onRecording(recordTime: Long) {}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        _vh.gone(R.id.camera_confirm_layout)
        _vh.gone(R.id.video_confirm_layout)
        if (_previewVideoLayoutControl != null) {
            _previewVideoLayoutControl!!.stop()
        }
    }

    override fun onRecordFinish(videoPath: String) {
        val targetFile = File(callback!!.onTakeVideoAfter(videoPath))

        val newFileName: String = fileNameUUID("_t_$_recordTime.mp4")
        val newFilePath = targetFile.parent!! + File.separator + newFileName

        targetFile.renameTo(newFilePath.file())

        //L.i("文件:" + new File(videoPath).exists());
        _previewVideoLayoutControl!!.showPreview(newFilePath, Runnable {
            if (isResumed) {
                _recordControl!!.surfaceChanged(
                    _recordView!!.holder,
                    0,
                    _recordView!!.width,
                    _recordView!!.height
                )
            }
        })
        _vh.click(R.id.video_confirm_button, View.OnClickListener {
//            dslFHelper {
//                finishActivityOnLastFragmentRemove = false
//                remove(this@RecordVideoFragment)
//            }
            callback!!.onTakeVideo(newFilePath)
        })
    }

    override fun onRecordError() {
        toastQQ("至少需要录制 " + callback!!.minRecordTime + " 秒")
        if (isResumed) {
            _recordControl!!.surfaceChanged(
                _recordView!!.holder,
                0,
                _recordView!!.width,
                _recordView!!.height
            )
        }
    }

    override fun onTakePhoto(data: ByteArray) {
        i("onTakePhoto")
        try {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            var bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)
            val displayOrientation = _recordControl!!.displayOrientation
            if (displayOrientation != 0) {
                val matrix = Matrix()
                matrix.postRotate(displayOrientation.toFloat())
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false
                )
                if (bitmap != rotatedBitmap) {
                    // 有时候 createBitmap会复用对象
                    bitmap.recycle()
                }
                bitmap = rotatedBitmap
            }
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
            bitmap = callback!!.onTakePhotoBefore(bitmap, width, height)
            if (oldBitmap != bitmap && !oldBitmap.isRecycled) {
                oldBitmap.recycle()
            }
            //显示图片预览
            showPhotoPreview(bitmap, outputFile)
            oldBitmap = bitmap
            bitmap = callback!!.onTakePhotoAfter(bitmap, width, height)
            if (oldBitmap != bitmap && !oldBitmap.isRecycled) {
                oldBitmap.recycle()
            }

            bitmap.save(outputFile.absolutePath, Bitmap.CompressFormat.JPEG, 70)
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