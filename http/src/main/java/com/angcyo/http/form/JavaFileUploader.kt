package com.angcyo.http.form

import com.angcyo.http.DslHttp
import com.angcyo.http.interceptor.logRequestBody
import com.angcyo.http.rx.runRx
import com.angcyo.library.L
import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.ex.md5
import com.angcyo.library.ex.nowTimeString
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Java 文件上传
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/13
 */
class JavaFileUploader {

    /**需要配置, 上传文件请求的地址. 必须*/
    var attachUploadUrl: String = ""

    /**文件key*/
    var attachFormKey: String = "file"

    /**是否激活秒传*/
    var enablePassCheck: Boolean = true

    /**配置请求参数回调*/
    var configMultipartBody: MultipartBody.Builder.() -> Unit = {}

    @CallPoint
    fun uploadFile(filePath: String): FileUploadInfoBean? {
        val pair = filePath.parseFileId()
        if (pair.first != null) {
            return FileUploadInfoBean("${pair.first}", filePath, pair.second)
        }

        val targetFile = File(filePath)

        if (!targetFile.exists()) {
            L.i("文件不存在:$filePath 跳过.")
            uploadFileEnd(filePath, null)
            return null
        }

        //秒传检查
        val checkOkHttpClient: OkHttpClient = DslHttp.client

        if (enablePassCheck) {
            val md5 = targetFile.md5() ?: nowTimeString()
            L.i("开始秒传检查:${md5} $filePath")
            val checkRequest = Request.Builder()
                .url(attachUploadUrl)
                .logRequestBody(false)
                .method(
                    "POST",
                    MultipartBody.Builder()
                        .addFormDataPart("md5Check", md5)
                        .setType(MultipartBody.FORM)
                        .apply(configMultipartBody)
                        .build()
                )
                .build()

            val checkResponse = checkOkHttpClient.newCall(checkRequest).execute()

            if (checkResponse.isSuccessful) {
                val bodyString = checkResponse.body?.string() ?: "{}"

                val infoBean = parseBody(filePath, bodyString)

                if (!infoBean.fileId.isNullOrEmpty()) {
                    L.i("文件秒传:$filePath -> ${infoBean.filePath}:${infoBean.fileId}")
                    uploadFileEnd(filePath, infoBean)
                    return infoBean
                }
            }
        }

        //真正的上传

        val okHttpClient = DslHttp.httpClient()

        //上传文件
        val fileBody = targetFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        //上传表单
        val request = Request.Builder()
            .url(attachUploadUrl)
            .logRequestBody(false)
            .method(
                "POST",
                MultipartBody.Builder().run {
                    addFormDataPart(attachFormKey, targetFile.name, fileBody)
                    setType(MultipartBody.FORM)
                    apply(configMultipartBody)
                    build()
                }
            )
            .build()

        val response = okHttpClient.newCall(request).execute()

        // once
        val bodyString = response.body?.string() ?: ""

        L.i("文件上传返回:$filePath -> $bodyString")

        if (response.isSuccessful) {
            val infoBean = parseBody(filePath, bodyString)
            uploadFileEnd(filePath, infoBean)

            /* if (!infoBean.fileId.isNullOrEmpty()) {
                 uploadFileEnd(filePath, infoBean)
             } else {
                 uploadFileError(filePath, IllegalStateException("$response"))
             }*/
            return infoBean
        } else {
            uploadFileError(filePath, IllegalStateException("$response"))
            return null
        }
    }

    fun parseBody(filePath: String, body: String): FileUploadInfoBean {
        val infoBean = FileUploadInfoBean(filePath = filePath, responseBody = body)
        /*val infoBean = when {
            body.isJsonObject() -> {
                body.fromJson<HttpBean<FileEntity>>(beanType(FileEntity::class.java))?.data
            }
            body.isJsonArray() -> {
                body.fromJson(JsonArray::class.java)?.get(0)?.toJson()
                    ?.fromJson(FileEntity::class.java)
            }
            else -> throw IllegalStateException("格式异常,非[json]格式:$body")
        } ?: FileUploadInfoBean(filePath = filePath)*/

//        val code = resultJson?.getInt("code") ?: -1
//        val data = resultJson?.getJson("data")
//        val path = data?.getString("filePath")?.toUrl()
//        val id = data?.getLong("id") ?: -1
//
//        if (code in 200..299 && data?.getLong("id") ?: -1 > 0) {
//            return FileEntity(id = id, filePath = path ?: "", diskPath = filePath)
//        }
//        return FileEntity(diskPath = filePath)

        return infoBean
    }

    /**文件上传结束, 调用此方法, 上传下一个*/
    fun uploadFileEnd(filePath: String, fileEntity: FileUploadInfoBean?) {

    }


    /**本地上传失败*/
    fun uploadFileError(filePath: String, error: Exception) {

    }
}

/**快速上传单个文件*/
fun String.uploadFile(
    url: String,
    config: JavaFileUploader.() -> Unit = {},
    action: (FileUploadInfoBean?) -> Unit
) = runRx({
    JavaFileUploader().apply {
        attachUploadUrl = url
        apply(config)
    }.uploadFile(this)
}) {
    action(it)
}
