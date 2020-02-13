package com.angcyo.http

import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.angcyo.http.rx.runRx
import com.angcyo.library.app
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.library.ex.use
import okhttp3.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * 获取url对应的图片类型
 * Email:angcyo@126.com
 * @author angcyo
 * @date 017/05/10
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

object OkType {
    private val imageTypeCache: MutableMap<String, ImageType> = ConcurrentHashMap()

    val client: OkHttpClient
        get() {
            DslHttp.init()
            return DslHttp.dslHttpConfig.okHttpClient!!
        }

    val mainHandle = Handler(Looper.getMainLooper())

    private fun getCall(url: String, listener: OnImageTypeListener?): Call? {
        var call: Call? = null
        try {
            val mRequest =
                Request.Builder().url(url).build() //如果url不是 网址, 会报错
            call = client.newCall(mRequest)
        } catch (e: Exception) {
            e.printStackTrace()
            if (listener != null) {
                typeCheckEnd(url, "UNKNOWN", listener)
            }
        }
        return call
    }

    /**
     * 根据url, 回调对应图片的类型
     */
    fun type(uri: Uri?, listener: OnImageTypeListener?): Call? {
        if (uri == null) {
            listener?.onImageType("", ImageType.UNKNOWN)
            return null
        }

        val url: String = uri.toString()

        val type = imageTypeCache[url]

        if (!uri.isHttpScheme()) {
            runRx({
                type ?: uri.use(app()) {
                    ImageType.of(
                        ImageTypeUtil.getImageType(it)
                    )
                }
                //ImageType.UNKNOWN
            }) {
                typeCheckEnd(url, it.toString(), listener)
            }
            return null
        }

        if (TextUtils.isEmpty(url)) {
            listener?.onImageType(url, ImageType.UNKNOWN)
            return null
        }
        listener?.onLoadStart()
        if (type == null) {
            val file = File(url)
            if (file.exists()) {
                runRx({
                    ImageTypeUtil.getImageType(file)
                }) {
                    typeCheckEnd(url, it, listener)
                }
            } else {
                val call = getCall(url, listener)
                if (call == null) {
                    typeCheckEnd(url, "UNKNOWN", listener)
                } else {
                    call.enqueue(object : Callback {
                        override fun onFailure(
                            call: Call,
                            e: IOException
                        ) {
                            typeCheckEnd(url, Build.UNKNOWN, listener)
                        }

                        @Throws(IOException::class)
                        override fun onResponse(
                            call: Call,
                            response: Response
                        ) {
                            val imageType =
                                ImageTypeUtil.getImageType(response.body!!.byteStream())
                            typeCheckEnd(url, imageType, listener)
                        }
                    })
                }
                return call
            }
        } else {
            listener?.onImageType(url, type)
        }
        return null
    }

    private fun typeCheckEnd(url: String, imageType: String?, listener: OnImageTypeListener?) {
        val imageType1 =
            ImageType.of(imageType)
        imageTypeCache[url] = imageType1
        if (listener != null) {
            mainHandle.post { listener.onImageType(url, imageType1) }
        }
    }

    enum class ImageType {
        JPEG, GIF, PNG, BMP, WEBP, UNKNOWN;

        companion object {
            fun of(type: String?): ImageType {
                if (TextUtils.isEmpty(type)) {
                    return UNKNOWN
                }
                if ("JPEG".equals(type, ignoreCase = true)) {
                    return JPEG
                }
                if ("GIF".equals(type, ignoreCase = true)) {
                    return GIF
                }
                if ("PNG".equals(type, ignoreCase = true)) {
                    return PNG
                }
                if ("BMP".equals(type, ignoreCase = true)) {
                    return BMP
                }
                return if ("WEBP".equals(type, ignoreCase = true)) {
                    WEBP
                } else UNKNOWN
            }
        }
    }

    interface OnImageTypeListener {
        fun onImageType(imageUrl: String, imageType: ImageType)

        fun onLoadStart()
    }

    object ImageTypeUtil {
        fun getImageType(file: File?): String? {
            if (file == null || !file.isFile || !file.canRead()) return null
            var `is`: InputStream? = null
            return try {
                `is` = FileInputStream(file)
                getImageType(`is`)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    `is`!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun getImageType(`is`: InputStream?): String? {
            return if (`is` == null) null else try {
                val bytes = ByteArray(8)
                if (`is`.read(bytes, 0, 8) != -1) getImageType(bytes) else null
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        private fun getImageType(bytes: ByteArray): String? {
            if (isJPEG(bytes)) return "JPEG"
            if (isGIF(bytes)) return "GIF"
            if (isPNG(bytes)) return "PNG"
            if (isBMP(bytes)) return "BMP"
            return if (isWebP(bytes)) "WEBP" else null
        }

        private fun isJPEG(b: ByteArray): Boolean {
            return b.size >= 2 && b[0] == 0xFF.toByte() && b[1] == 0xD8.toByte()
        }

        private fun isGIF(b: ByteArray): Boolean {
            return b.size >= 6 && b[0] == 'G'.toByte() &&
                    b[1] == 'I'.toByte() && b[2] == 'F'.toByte() &&
                    b[3] == '8'.toByte() && (b[4] == '7'.toByte() ||
                    b[4] == '9'.toByte()) && b[5] == 'a'.toByte()
        }

        private fun isPNG(b: ByteArray): Boolean {
            return (b.size >= 8 && b[0] == 137.toByte() &&
                    b[1] == 80.toByte() && b[2] == 78.toByte() &&
                    b[3] == 71.toByte() && b[4] == 13.toByte() &&
                    b[5] == 10.toByte() && b[6] == 26.toByte() &&
                    b[7] == 10.toByte())
        }

        private fun isBMP(b: ByteArray): Boolean {
            return b.size >= 2 && b[0] == 0x42.toByte() && b[1] == 0x4d.toByte()
        }

        private fun isWebP(b: ByteArray): Boolean {
            return b.size >= 4 && b[0] == 82.toByte() && b[1] == 73.toByte() && b[2] == 70.toByte() && b[3] == 70.toByte()
        }
    }
}