package com.angcyo.glide.base64

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.nio.ByteBuffer


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */

class Base64ModelLoaderFactory : ModelLoaderFactory<String, ByteBuffer> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<String, ByteBuffer> {
        return Base64ModelLoader()
    }

    override fun teardown() {
        // Do nothing.
    }
}