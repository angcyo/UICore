package com.angcyo.glide.base64

import android.content.Context
import androidx.annotation.Keep
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule
import java.nio.ByteBuffer

/**
 * https://muyangmin.github.io/glide-docs-cn/tut/custom-modelloader.html
 *
 * https://github.com/sjudd/Base64ModelLoaderExample
 *
 * https://muyangmin.github.io/glide-docs-cn/doc/configuration.html#%E6%8E%92%E5%BA%8F%E7%BB%84%E4%BB%B6
 *
 * 没有成功.
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */

@GlideModule
@Keep
class Base64LibraryGlideModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(String::class.java, ByteBuffer::class.java, Base64ModelLoaderFactory())
    }
}