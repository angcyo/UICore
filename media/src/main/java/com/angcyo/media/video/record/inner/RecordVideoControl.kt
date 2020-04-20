package com.angcyo.media.video.record.inner

import android.app.Activity
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import com.angcyo.media.video.record.inner.RecordVideoUtils.Companion.getBestCamcorderProfile
import com.angcyo.media.video.record.inner.RecordVideoUtils.Companion.getResolutionList
import com.angcyo.media.video.record.inner.RecordVideoUtils.ResolutionComparator
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

/**
 * 录制视频控制类
 * Created by dalong on 2017/1/3.
 *
 *
 * https://github.com/dalong982242260/SmallVideoRecording
 */
class RecordVideoControl(
    var mActivity: Activity?,
    var mSizeSurfaceView: SizeSurfaceView,
    var mRecordVideoInterface: RecordVideoInterface?
) : SurfaceHolder.Callback, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    companion object {
        const val FLASH_MODE_OFF = 0
        const val FLASH_MODE_ON = 1
        var flashType = FLASH_MODE_OFF
    }

    val TAG = RecordVideoControl::class.java.simpleName

    /**
     * 设置录制保存路径
     *
     * @param videoPath
     */
    var videoPath: String? = null
    var previewWidth = 720 //预览宽
    var previewHeight = 1280 //预览高
    /**
     * 获取最大录制时间
     *
     * @return
     */
    /**
     * 设置录制时间
     *
     * @param maxTime
     */
    var maxTime = 100000 //最大录制时间
    /**
     * 获取最大录制大小
     *
     * @return
     */
    /**
     * 设置录制大小
     *
     * @param maxSize
     */
    var maxSize = 300 * 1024 * 1024 //最大录制大小 默认300m
        .toLong()
    var mSurfaceHolder: SurfaceHolder

    /**
     * 摄像头方向
     *
     * @return
     */
    var cameraFacing: Int

    /**
     * 是否录制
     *
     * @return
     */
    var isRecording = false
    var mCamera: Camera? = null

    //是否预览
    var mIsPreviewing = false
    var mediaRecorder: MediaRecorder? = null
    var defaultVideoFrameRate = 24 //默认的视频帧率

    init {
        mSizeSurfaceView.isUserSize = true
        mSurfaceHolder = mSizeSurfaceView.holder
        mSurfaceHolder.addCallback(this)

        //这里设置当摄像头数量大于1的时候就直接设置后摄像头  否则就是前摄像头
//        if (Build.VERSION.SDK_INT > 8) {
//            if (Camera.getNumberOfCameras() > 1) {
//                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
//            } else {
//                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
//            }
//        }
        cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK
    }


    /**
     * 开启摄像头预览
     *
     * @param holder
     */
    fun startCameraPreview(holder: SurfaceHolder) {
        mIsPreviewing = false
        setCameraParameter()
        mCamera!!.setDisplayOrientation(displayOrientation)
        try {
            mCamera!!.setPreviewDisplay(holder)
        } catch (e: IOException) {
            destroyCamera()
            return
        }
        mCamera!!.startPreview()
        mIsPreviewing = true
        mSizeSurfaceView.setVideoDimension(previewHeight, previewWidth)
        mSizeSurfaceView.requestLayout()
    }

    /**
     * 释放 Camera
     */
    fun destroyCamera() {
        if (mCamera != null) {
            if (mIsPreviewing) {
                mCamera!!.stopPreview()
                mIsPreviewing = false
                mCamera!!.setPreviewCallback(null)
                mCamera!!.setPreviewCallbackWithBuffer(null)
            }
            mCamera!!.release()
            mCamera = null
        }
    }

    /**
     * 切换摄像头
     *
     * @param v 点击切换的view 这里处理了点击事件
     */
    fun changeCamera(v: View?) {
        if (v != null) v.isEnabled = false
        changeCamera()
        Handler().postDelayed({ if (v != null) v.isEnabled = true }, 1000)
    }

    /**
     * 切换摄像头
     */
    fun changeCamera() {
        if (isRecording) {
            Toast.makeText(mActivity, "录制中无法切换", Toast.LENGTH_SHORT).show()
            return
        }
        if (Build.VERSION.SDK_INT < 9) {
            return
        }
        var cameraid = 0
        cameraid = if (Camera.getNumberOfCameras() > 1) {
            if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Camera.CameraInfo.CAMERA_FACING_FRONT
            } else {
                Camera.CameraInfo.CAMERA_FACING_BACK
            }
        } else {
            Camera.CameraInfo.CAMERA_FACING_BACK
        }
        if (cameraFacing == cameraid) {
            return
        } else {
            cameraFacing = cameraid
        }
        destroyCamera()
        try {
            mCamera = Camera.open(cameraFacing)
            if (mCamera != null) {
                startCameraPreview(mSurfaceHolder)
            }
        } catch (e: Exception) {
            destroyCamera()
        }
    }

    /**
     * 设置camera 的 Parameters
     */
    fun setCameraParameter() {
        val parameters = mCamera!!.parameters
        parameters.setPreviewSize(previewWidth, previewHeight)
        if (Build.VERSION.SDK_INT < 9) {
            return
        }
        val supportedFocus =
            parameters.supportedFocusModes
        val isHave =
            if (supportedFocus == null) false else supportedFocus.indexOf(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) >= 0
        if (isHave) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
        }
        parameters.flashMode =
            if (flashType == FLASH_MODE_ON) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
        mCamera!!.parameters = parameters
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {}

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
        try {
            mSurfaceHolder = holder
            if (holder.surface == null) {
                return
            }
            if (mCamera == null) {
                mCamera = if (Build.VERSION.SDK_INT < 9) {
                    Camera.open()
                } else {
                    Camera.open(cameraFacing)
                }
            }
            if (mCamera != null) mCamera!!.stopPreview()
            mIsPreviewing = false
            handleSurfaceChanged(mCamera)
            startCameraPreview(mSurfaceHolder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        try {
            destroyCamera()
            releaseRecorder()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleSurfaceChanged(mCamera: Camera?) {
        //帧率兼容
        if (defaultVideoFrameRate > 0) {
            var hasSupportRate = false
            val supportedPreviewFrameRates = mCamera!!.parameters
                .supportedPreviewFrameRates
            if (supportedPreviewFrameRates != null
                && supportedPreviewFrameRates.size > 0
            ) {
                Collections.sort(supportedPreviewFrameRates)
                for (i in supportedPreviewFrameRates.indices) {
                    val supportRate = supportedPreviewFrameRates[i]
                    if (supportRate == defaultVideoFrameRate) {
                        hasSupportRate = true
                    }
                }

                //如果找到了默认的帧率
                if (hasSupportRate) {
                } else {
                    //否则使用最低的帧率
                    defaultVideoFrameRate = supportedPreviewFrameRates[0]
                }
            }
        }

        // 获取相机提供的所有分辨率
        val resolutionList =
            getResolutionList(mCamera!!)
        if (resolutionList.isNotEmpty()) {
            Collections.sort(
                resolutionList,
                ResolutionComparator()
            )
            var previewSize: Camera.Size? = null
            var hasSize = false
            // 使用 640*480 如果相机支持的话
            for (i in resolutionList.indices) {
                val size = resolutionList[i]
                //WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
                //默认使用720 * 1280预览
                val width = 720 //wm.getDefaultDisplay().getWidth();
                val height = 1280 //wm.getDefaultDisplay().getHeight();
                Log.v(TAG, "width:" + size.width + "   height:" + size.height)
                if (size != null && size.width == height && size.height == width) {
                    previewSize = size
                    previewWidth = previewSize.width
                    previewHeight = previewSize.height
                    hasSize = true
                    break
                }
            }
            //如果相机不支持上述分辨率，使用中分辨率
            if (!hasSize) {
                var mediumResolution = resolutionList.size / 2
                if (mediumResolution >= resolutionList.size) mediumResolution =
                    resolutionList.size - 1
                previewSize = resolutionList[mediumResolution]
                previewWidth = previewSize.width
                previewHeight = previewSize.height
            }
        }
    }

    val displayOrientation: Int
        get() {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(cameraFacing, info)
            val rotation = mActivity!!.windowManager.defaultDisplay.rotation
            var degrees = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
                else -> {
                }
            }
            var result: Int
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360
            } else {
                result = (info.orientation - degrees + 360) % 360
            }
            return result
        }

    /**
     * 开始录制
     *
     * @return
     */
    fun startRecording(videoPath: String): Boolean {
        this.videoPath = videoPath
        isRecording = true
        releaseRecorder()
        //mCamera.stopPreview();
        mCamera!!.unlock()
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setCamera(mCamera)
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
        mediaRecorder!!.setOrientationHint(displayOrientation)
        try {
            mediaRecorder!!.setProfile(getBestCamcorderProfile(cameraFacing))
        } catch (e: Exception) {
            Log.e(TAG, "设置质量出错:" + e.message)
            customMediaRecorder()
        }

        // 设置帧速率，应设置在格式和编码器设置
        if (defaultVideoFrameRate != -1) {
            mediaRecorder!!.setVideoFrameRate(defaultVideoFrameRate)
        }
        mediaRecorder!!.setOnInfoListener(this)
        mediaRecorder!!.setOnErrorListener(this)
        // 设置最大录制时间
        mediaRecorder!!.setMaxFileSize(maxSize)
        mediaRecorder!!.setMaxDuration(maxTime)
        mediaRecorder!!.setPreviewDisplay(mSurfaceHolder.surface)
        mediaRecorder!!.setOutputFile(videoPath)
        try {
            mediaRecorder!!.prepare()
            mediaRecorder!!.start()
            if (mRecordVideoInterface != null) {
                mRecordVideoInterface!!.startRecord()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    /**
     * 自定义的设置mediaeecorder 这里设置视频质量最低  录制出来的视频体积很小 对质量不是要求不高的可以使用
     */
    fun customMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H263)
            //设置分辨率，应设置在格式和编码器设置之后
            mediaRecorder!!.setVideoSize(previewWidth, previewHeight)
            mediaRecorder!!.setVideoEncodingBitRate(800 * 1024)
        }
    }

    /**
     * 停止录制
     */
    fun stopRecording(isSucessed: Boolean) {
        if (!isRecording) {
            return
        }
        try {
            if (mediaRecorder != null && isRecording) {
                isRecording = false
                mediaRecorder!!.stop()
                mediaRecorder!!.release()
                mediaRecorder = null
                if (mCamera != null) {
                    mCamera!!.stopPreview()
                }
                if (isSucessed) {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface!!.onRecordFinish(videoPath!!)
                    }
                } else {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface!!.onRecordError()
                    }
                    updateCallBack(0)
                }
            }
        } catch (e: Exception) {
            updateCallBack(0)
            Log.e(TAG, "stopRecording error:" + e.message)
        }
    }

    /**
     * 设置闪光灯模式
     *
     * @param type
     */
    fun setFlashMode(type: Int) {
        flashType = type
        var flashMode: String? = null
        when (type) {
            FLASH_MODE_ON -> flashMode =
                Camera.Parameters.FLASH_MODE_TORCH
            FLASH_MODE_OFF -> flashMode =
                Camera.Parameters.FLASH_MODE_OFF
            else -> {
            }
        }
        if (flashMode != null) {
            val parameters = mCamera!!.parameters
            parameters.flashMode = flashMode
            mCamera!!.parameters = parameters
        }
    }

    /**
     * 拍照
     */
    fun takePhoto() {
        mCamera!!.setPreviewCallback(Camera.PreviewCallback { data, camera ->
            camera.setPreviewCallback(null)
            if (mCamera == null) return@PreviewCallback
            val parameters = camera.parameters
            val width = parameters.previewSize.width
            val height = parameters.previewSize.height
            val yuv =
                YuvImage(data, parameters.previewFormat, width, height, null)
            val out = ByteArrayOutputStream()
            yuv.compressToJpeg(Rect(0, 0, width, height), 50, out)
            val bytes = out.toByteArray()
            if (mRecordVideoInterface != null) {
                mRecordVideoInterface!!.onTakePhoto(bytes)
            }
            //设置这个可以达到预览的效果
//                mCamera.setPreviewCallback(this);
        })
    }

    /**
     * 释放mediaRecorder
     */
    fun releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder!!.release()
            mediaRecorder = null
        }
    }

    override fun onInfo(mediaRecorder: MediaRecorder, what: Int, extra: Int) {
        Log.v(TAG, "onInfo")
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.v(TAG, "最大录制时间已到")
            stopRecording(true)
        }
    }

    override fun onError(mediaRecorder: MediaRecorder, i: Int, i1: Int) {
        Log.e(TAG, "recording onError:")
        Toast.makeText(mActivity, "录制失败，请重试", Toast.LENGTH_SHORT).show()
        stopRecording(false)
    }

    /**
     * 回调录制时间
     *
     * @param recordTime
     */
    fun updateCallBack(recordTime: Int) {
        if (mActivity != null && !mActivity!!.isFinishing) {
            mActivity!!.runOnUiThread {
                if (mRecordVideoInterface != null) {
                    mRecordVideoInterface!!.onRecording(recordTime.toLong())
                }
            }
        }
    }
}