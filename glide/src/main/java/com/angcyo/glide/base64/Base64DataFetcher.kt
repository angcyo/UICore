package com.angcyo.glide.base64

import android.util.Base64
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.nio.ByteBuffer


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/21
 */

internal class Base64DataFetcher(val model: String) : DataFetcher<ByteBuffer> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in ByteBuffer>) {
        val base64Section = getBase64SectionOfModel()
        val data: ByteArray = Base64.decode(base64Section, Base64.DEFAULT)
        val byteBuffer: ByteBuffer = ByteBuffer.wrap(data)
        callback.onDataReady(byteBuffer)
    }

    private fun getBase64SectionOfModel(): String {
        // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs.
        val startOfBase64Section = model.indexOf(',')
        return model.substring(startOfBase64Section + 1)
    }

    override fun cleanup() {
        // Intentionally empty only because we're not opening an InputStream or another I/O resource!
    }

    override fun cancel() {
        // Intentionally empty.
    }

    override fun getDataClass(): Class<ByteBuffer> {
        return ByteBuffer::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}