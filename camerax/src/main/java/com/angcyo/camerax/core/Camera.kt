package com.angcyo.camerax.core

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import com.angcyo.library.component.lastContext


/**
 * 摄像头分辨率
 * https://developer.android.com/training/camerax/configuration?hl=zh-cn#resolution
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/21
 */
object Camera {

    @SuppressLint("UnsafeOptInUsageError")
    fun selectExternalOrBestCamera(provider: ProcessCameraProvider): CameraSelector? {
        val cam2Infos = provider.availableCameraInfos.map {
            Camera2CameraInfo.from(it)
        }.sortedByDescending {
            // HARDWARE_LEVEL is Int type, with the order of:
            // LEGACY < LIMITED < FULL < LEVEL_3 < EXTERNAL
            it.getCameraCharacteristic(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        }

        return when {
            cam2Infos.isNotEmpty() -> {
                CameraSelector.Builder()
                    .addCameraFilter {
                        it.filter { camInfo ->
                            // cam2Infos[0] is either EXTERNAL or best built-in camera
                            val thisCamId = Camera2CameraInfo.from(camInfo).cameraId
                            thisCamId == cam2Infos[0].cameraId
                        }
                    }.build()
            }

            else -> null
        }
    }

    /**[getStreamConfigurationMap]*/
    @SuppressLint("UnsafeOptInUsageError")
    fun getStreamConfigurationMap(cameraInfo: CameraInfo?): StreamConfigurationMap? {
        return if (cameraInfo != null) {
            getStreamConfigurationMap(Camera2CameraInfo.from(cameraInfo).cameraId)
        } else {
            null
        }
    }

    /**
     * [cameraId] "1"
     * [StreamConfigurationMap]
     * https://developer.android.com/reference/android/hardware/camera2/params/StreamConfigurationMap
     * */
    fun getStreamConfigurationMap(cameraId: String): StreamConfigurationMap? {
        val cameraManager =
            lastContext.getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager
        val characteristics: CameraCharacteristics =
            cameraManager.getCameraCharacteristics(cameraId)
        val configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        //val outputSizes = configs?.getOutputSizes(ImageFormat.YUV_420_888)
        //Pixel 6
        //[w:3264, h:2448, format:JPEG(256), min_duration:33333333, stall:0],
        //[w:3264, h:1836, format:JPEG(256), min_duration:33333333, stall:0],
        //[w:2560, h:1920, format:JPEG(256), min_duration:33333333, stall:0],
        //[w:2688, h:1512, format:JPEG(256), min_duration:33333333, stall:0],
        //[w:1920, h:1920, format:JPEG(256), min_duration:33333333, stall:0],
        //[w:2560, h:1280, format:JPEG(256), min_duration:33333333, stall:0],
        //[w:2048, h:1536, format:JPEG(256), min_duration:33333333, stall:0],
        //[w:1920, h:1440, format:JPEG(256), min_duration:33333333, stall:0],
        //[w:1920, h:1080, format:JPEG(256), min_duration:33333333, stall:0],
        //[w:3280, h:2464, format:JPEG(256), min_duration:33333333, stall:0],

        //[w:3264, h:2448, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:3264, h:1836, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2560, h:1920, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2688, h:1512, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1920, h:1920, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2560, h:1280, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2048, h:1536, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1920, h:1440, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1920, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1600, h:1200, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1920, h:960, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1440, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1280, h:960, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1080, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1280, h:720, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1024, h:768, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:800, h:600, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:720, h:480, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:640, h:480, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:640, h:360, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:352, h:288, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:320, h:240, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:176, h:144, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:3280, h:2464, format:YUV_420_888(35), min_duration:33333333, stall:0],

        //realme 11 Pro+
        //[w:3840, h:2160, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:3264, h:2448, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:3264, h:1840, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:3264, h:1632, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:3264, h:1504, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:3264, h:1472, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:2912, h:1344, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:2560, h:1920, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:2560, h:1440, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:2560, h:1080, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:2416, h:1080, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:2400, h:1080, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:2280, h:1080, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1920, h:1920, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1920, h:1440, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1920, h:1088, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1920, h:1080, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1872, h:864, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1600, h:720, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1440, h:1088, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1280, h:960, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1280, h:720, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:1024, h:768, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:960, h:720, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:960, h:544, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:960, h:540, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:720, h:720, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:640, h:480, format:JPEG(256), min_duration:33333333, stall:33333333],
        //[w:320, h:240, format:JPEG(256), min_duration:33333333, stall:33333333],

        //[w:4080, h:2296, format:YUV_420_888(35), min_duration:50000000, stall:0],
        //[w:3840, h:2160, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:3264, h:2448, format:YUV_420_888(35), min_duration:50000000, stall:0],
        //[w:3264, h:1840, format:YUV_420_888(35), min_duration:50000000, stall:0],
        //[w:3264, h:1632, format:YUV_420_888(35), min_duration:50000000, stall:0],
        //[w:3264, h:1572, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:3264, h:1504, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:3264, h:1472, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2912, h:1344, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2560, h:1920, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2560, h:1440, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2560, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2416, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2400, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2400, h:1028, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2340, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2304, h:1728, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2280, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2160, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:2080, h:960, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1920, h:1920, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1920, h:1440, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1920, h:1088, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1920, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1872, h:864, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1600, h:1200, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1600, h:720, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1560, h:720, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1560, h:702, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1440, h:1088, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1440, h:1080, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1280, h:960, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1280, h:768, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1280, h:720, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1088, h:1088, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:1024, h:768, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:960, h:720, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:960, h:544, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:960, h:540, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:800, h:400, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:720, h:720, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:720, h:480, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:640, h:480, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:352, h:288, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:320, h:240, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:192, h:144, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:192, h:108, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:176, h:144, format:YUV_420_888(35), min_duration:33333333, stall:0],
        //[w:160, h:96, format:YUV_420_888(35), min_duration:33333333, stall:0],

        return configs
    }
}