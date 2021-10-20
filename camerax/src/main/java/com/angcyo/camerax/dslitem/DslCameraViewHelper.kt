package com.angcyo.camerax.dslitem

import android.Manifest
import android.annotation.SuppressLint
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileOptions
import androidx.camera.view.video.OutputFileResults
import com.angcyo.library.L
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex.havePermission
import com.angcyo.library.ex.saveToDCIM
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.fileName
import com.angcyo.library.utils.filePath
import java.io.File

/**
 * [captureMode]之间切换, 会黑屏一会儿,
 * 如果使用[MIXED]模式, 则拍照和录像之间切换不会黑屏, 但是有些设备不支持[MIXED]模式
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/14
 */

class DslCameraViewHelper {

    /**需要的权限*/
    var recordPermissionList = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    /**核心[CameraView]*/
    var cameraView: PreviewView? = null

    /**是否要在DCIM中显示*/
    var saveToDCIM: Boolean = false

    /**拍照, 拍照*/
    fun takePicture(
        file: File? = null,
        autoEnableCase: Boolean = true,
        onResult: (File, Exception?) -> Unit
    ) {
        val saveFile =
            file ?: File(filePath(Constant.CAMERA_FOLDER_NAME, fileName(suffix = ".jpg")))
        cameraView?.controller?.run {

            if (autoEnableCase && !isImageCaptureEnabled) {
                setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            }

            takePicture(
                ImageCapture.OutputFileOptions.Builder(saveFile).build(),
                MainExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        L.i(saveFile)
                        if (saveToDCIM) {
                            cameraView?.context?.saveToDCIM(saveFile)
                            //cameraView?.context?.scanFile(saveFile)
                        }
                        onResult(saveFile, null)
                        //L.i(outputFileResults.savedUri) null
                    }

                    override fun onError(exception: ImageCaptureException) {
                        L.w(exception)
                        onResult(saveFile, exception)
                    }
                })
        }
    }

    /**录像, 需要录音权限*/
    @SuppressLint("UnsafeOptInUsageError")
    fun startRecording(
        file: File? = null,
        autoEnableCase: Boolean = true,
        onResult: (File, Exception?) -> Unit
    ): Boolean {
        val saveFile =
            file ?: File(filePath(Constant.CAMERA_FOLDER_NAME, fileName(suffix = ".mp4")))
        if (cameraView?.context?.havePermission(recordPermissionList) == true) {
            cameraView?.controller?.run {

                if (autoEnableCase && !isVideoCaptureEnabled) {
                    setEnabledUseCases(CameraController.VIDEO_CAPTURE)
                }

                startRecording(
                    OutputFileOptions.builder(saveFile).build(),
                    MainExecutor,
                    object : OnVideoSavedCallback {

                        /*override fun onVideoSaved(file: File) {
                            L.i(saveFile)
                            if (saveToDCIM) {
                                cameraView?.context?.saveToDCIM(saveFile)
                                //cameraView?.context?.scanFile(saveFile)
                            }
                            onResult(saveFile, null)
                        }*/

                        override fun onVideoSaved(outputFileResults: OutputFileResults) {
                            L.i(saveFile)
                            if (saveToDCIM) {
                                cameraView?.context?.saveToDCIM(saveFile)
                                //cameraView?.context?.scanFile(saveFile)
                            }
                            onResult(saveFile, null)
                        }

                        override fun onError(
                            videoCaptureError: Int,
                            message: String,
                            cause: Throwable?
                        ) {
                            L.w("$videoCaptureError $message $cause")
                            onResult(saveFile, Exception(message, cause))
                        }
                    })
            }
            return true
        } else {
            L.w("请检查权限:", recordPermissionList)
            onResult(saveFile, Exception("无权限"))
            return false
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun stopRecording() {
        cameraView?.controller?.stopRecording()
    }
}