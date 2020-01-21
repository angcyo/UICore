package com.angcyo.glide.base64

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.signature.ObjectKey
import java.nio.ByteBuffer


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */

/**
 * Loads an [InputStream] from a Base 64 encoded String.
 */
class Base64ModelLoader : ModelLoader<String, ByteBuffer> {

    override fun buildLoadData(
        model: String,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<ByteBuffer> {
        return LoadData<ByteBuffer>(ObjectKey(model), Base64DataFetcher(model))
    }

    override fun handles(model: String): Boolean {
        return model.startsWith(DATA_URI_PREFIX)
    }

    companion object {
        // From: https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs.
        private const val DATA_URI_PREFIX = "data:"
    }
}