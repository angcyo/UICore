package com.angcyo.glide

import android.content.Context
import com.angcyo.library.L
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.LibraryGlideModule
import okhttp3.OkHttpClient
import java.io.InputStream

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
@GlideModule
class OkHttpProgressGlideModule : LibraryGlideModule() {

    companion object {
        var okHttpClient: OkHttpClient? = null
            get() {
                if (field == null) {
                    field = defaultOkHttpClientBuilder.build()
                }
                return field
            }

        val defaultOkHttpClientBuilder = OkHttpClient.Builder().apply {
            /*connectTimeout(DslHttpConfig.TIME_OUT, TimeUnit.SECONDS)
            readTimeout(DslHttpConfig.TIME_OUT, TimeUnit.SECONDS)
            writeTimeout(DslHttpConfig.TIME_OUT, TimeUnit.SECONDS)
            proxy(Proxy.NO_PROXY)
            followRedirects(true)
            followSslRedirects(true)
            addInterceptor(UUIDInterceptor())
            addInterceptor(LogInterceptor())*/
            //addInterceptor(ProIn)
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        L.e("test...registerComponents")
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(okHttpClient!!)
        )
    }
}