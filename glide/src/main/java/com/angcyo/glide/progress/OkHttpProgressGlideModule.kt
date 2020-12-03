package com.angcyo.glide.progress

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import java.io.InputStream

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
@Deprecated("")
class OkHttpProgressGlideModule : com.bumptech.glide.module.GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Do nothing.
        //L.e("test...")
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        //L.e("test...")
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(GlideProgress.okHttpClient!!)
        )
    }
}
