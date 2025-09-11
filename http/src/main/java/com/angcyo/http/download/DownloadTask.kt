package com.angcyo.http.download

import com.angcyo.http.DslHttp
import com.angcyo.library.component.Web
import com.angcyo.library.ex.uuid
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * 下载任务
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/09
 */
class DownloadTask(
    /**需要下载的地址*/
    val url: String,
    /**下载保存的文件路径, 全路径*/
    val savePath: String,
    /**下载监听*/
    val listener: DownloadListener,
    /**如果下载的文件已存在, 是否覆写文件*/
    val overwrite: Boolean = false,
) {

    //---

    /**下载内容的长度, 自动赋值*/
    var contentLength: Long = -1

    /**下载进度[0~100]*/
    var progress: Int = -1

    /**下载是否完成, 正常/异常都属于完成*/
    var isFinish: Boolean = false

    /**异常的信息*/
    var error: Exception? = null

    /**请求客户端*/
    var okHttpClient: OkHttpClient = DslHttp.client

    /**请求参数配置*/
    var requestBuilderConfig: Request.Builder.() -> Unit = {
        header("Accept", "*/*")
        header("User-Agent", Web.CUSTOM_UA ?: "OkHttp3 ${uuid()} ${Web.UA_EXTEND}")
    }

    /**用于取消*/
    var call: Call? = null

    /**开始下载*/
    fun download() {
        if (!overwrite && File(savePath).exists()) {
            progress = 100
            isFinish = true
            listener.onDownloading(this@DownloadTask, progress)
            listener.onDownloadSuccess(this@DownloadTask)
            return
        }
        progress = -1
        contentLength = -1
        isFinish = false
        error = null
        val request: Request = Request.Builder().url(url).apply(requestBuilderConfig).build()
        call = okHttpClient.newCall(request)
        call?.enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                // 下载失败
                isFinish = true
                error = e
                listener.onDownloadFailed(this@DownloadTask, e)
            }

            override fun onResponse(call: Call, response: Response) {
                var `is`: InputStream? = null
                val buf = ByteArray(2048)
                var len = 0
                var fos: FileOutputStream? = null
                try {
                    `is` = response.body?.byteStream()
                    contentLength = response.body?.contentLength() ?: -1
                    val file = File(savePath)
                    fos = FileOutputStream(file)
                    var sum: Long = 0
                    while (`is`?.read(buf)?.also { len = it } != -1 && !call.isCanceled()) {
                        fos.write(buf, 0, len)
                        sum += len.toLong()
                        val progress = if (contentLength < 0) {
                            -1
                        } else {
                            (sum * 1.0f / contentLength * 100).toInt()
                        }
                        // 下载中
                        if (this@DownloadTask.progress != progress) {
                            this@DownloadTask.progress = progress
                            listener.onDownloading(this@DownloadTask, progress)
                        }
                    }
                    fos.flush()
                    // 下载完成
                    isFinish = true
                    listener.onDownloadSuccess(this@DownloadTask)
                } catch (e: Exception) {
                    error = e
                    isFinish = true
                    listener.onDownloadFailed(this@DownloadTask, e)
                } finally {
                    try {
                        `is`?.close()
                    } catch (e: IOException) {
                    }
                    try {
                        fos?.close()
                    } catch (e: IOException) {
                    }
                }
            }
        })
    }

    /**取消下载*/
    fun cancel() {
        isFinish = true
        call?.cancel()
    }
}