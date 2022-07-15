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
import com.angcyo.library.utils.ImageTypeUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * 获取url对应的图片类型
 * Email:angcyo@126.com
 * @author angcyo
 * @date 017/05/10
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

object OkType {

    /**缓存*/
    private val imageTypeCache: MutableMap<String, ImageTypeUtil.ImageType> = ConcurrentHashMap()

    val mainHandle = Handler(Looper.getMainLooper())

    private fun getCall(url: String, listener: OnImageTypeListener?): Call? {
        var call: Call? = null
        try {
            val mRequest =
                Request.Builder().url(url).build() //如果url不是 网址, 会报错
            call = DslHttp.client.newCall(mRequest)
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
            listener?.onImageType("", ImageTypeUtil.ImageType.UNKNOWN)
            return null
        }

        val url: String = uri.toString()

        val type = imageTypeCache[url]

        if (!uri.isHttpScheme()) {
            listener?.onLoadStart()
            runRx({
                type ?: uri.use(app()) {
                    ImageTypeUtil.ImageType.of(
                        ImageTypeUtil.getImageType(it)
                    )
                }
                //ImageType.UNKNOWN
            }) {
                it?.run {
                    typeCheckEnd(url, this.toString(), listener)
                } ?: typeCheckEnd(url, "", listener)
            }
            return null
        }

        if (TextUtils.isEmpty(url)) {
            listener?.onImageType(url, ImageTypeUtil.ImageType.UNKNOWN)
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
        val imageType1 = ImageTypeUtil.ImageType.of(imageType)
        imageTypeCache[url] = imageType1
        if (listener != null) {
            mainHandle.post { listener.onImageType(url, imageType1) }
        }
    }
    
    /**回调*/
    interface OnImageTypeListener {
        fun onImageType(imageUrl: String, imageType: ImageTypeUtil.ImageType)

        fun onLoadStart()
    }
}