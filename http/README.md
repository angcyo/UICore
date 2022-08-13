# http

2019-12-19

`OkHttp` `Retrofit` `RxJava` `Rx`

- 网络请求
- 权限请求
- RxBus
- 等相关库

# 上传文件示例

```

package com.wayto.core.form.inner

import com.angcyo.http.DslHttp
import com.angcyo.http.base.fromJson
import com.angcyo.http.base.isJsonArray
import com.angcyo.http.base.isJsonObject
import com.angcyo.http.base.toJson
import com.angcyo.http.form.JsonAttachUploader
import com.angcyo.http.form.parseFileId
import com.angcyo.http.form.uploadAttach
import com.angcyo.http.interceptor.logRequestBody
import com.angcyo.http.progress.newProgressUrl
import com.angcyo.http.progress.removeUploadProgress
import com.angcyo.http.progress.uploadProgress
import com.angcyo.http.rx.runRx
import com.angcyo.library.L
import com.angcyo.library.ex.md5
import com.angcyo.library.ex.nowTimeString
import com.google.gson.JsonArray
import com.wayto.core.form.inner.FormManager.Companion.formLog
import com.wayto.core.http.bean.FileEntity
import com.wayto.core.http.bean.HttpBean
import com.wayto.core.http.bean.beanType
import com.wayto.core.inner.toUrl
import me.jessyan.progressmanager.body.ProgressInfo
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019-7-25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class JavaFormAttachManager : FormAttachManager() {

    companion object {
        val JAVA_UPLOAD_URL get() = "/commonFile/uploadSingle".toUrl()
    }

    /**上传请求地址*/
    var attachUploadUrl = JAVA_UPLOAD_URL

    /**文件key*/
    var attachFormKey: String = "file"

    override fun uploadFile(filePath: String): FileEntity? {
        val pair = filePath.parseFileId()
        if (pair.first != null) {
            return FileEntity(id = pair.first, filePath = pair.second)
        }

        val targetFile = File(filePath)

        if (!targetFile.exists()) {
            L.i("文件不存在:$filePath 跳过.")
            uploadFileEnd(filePath, null)
            return null
        }

        //秒传检查
        val checkOkHttpClient = DslHttp.dslHttpConfig.defaultOkHttpClientBuilder.build()
        val checkRequest = Request.Builder()
            .url(attachUploadUrl)
            .logRequestBody(false)
            .method(
                "POST",
                MultipartBody.Builder()
                    .addFormDataPart("md5Check", targetFile.md5() ?: nowTimeString())
                    .setType(MultipartBody.FORM)
                    .build()
            )
            .build()

        val checkResponse = checkOkHttpClient.newCall(checkRequest).execute()

        if (checkResponse.isSuccessful) {
            val bodyString = checkResponse.body?.string() ?: "{}"

            val fileEntity = parseBody(filePath, bodyString)

            if (!fileEntity.id.isNullOrEmpty()) {
                formLog("文件秒传:$filePath -> ${fileEntity.filePath}:${fileEntity.id}")
                uploadFileEnd(filePath, fileEntity)
                return fileEntity
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
                    build()
                }
            )
            .build()

        val response = okHttpClient.newCall(request).execute()

        // once
        val bodyString = response.body?.string() ?: ""

        formLog("文件上传返回:$filePath -> $bodyString")

        if (response.isSuccessful) {
            val fileEntity = parseBody(filePath, bodyString)

            if (!fileEntity.id.isNullOrEmpty()) {
                uploadFileEnd(filePath, fileEntity)
            } else {
                uploadFileError(filePath, IllegalStateException("$response"))
            }
            return fileEntity
        } else {
            uploadFileError(filePath, IllegalStateException("$response"))
            return null
        }
    }

    fun parseBody(filePath: String, body: String): FileEntity {
        val fileEntity = when {
            body.isJsonObject() -> {
                body.fromJson<HttpBean<FileEntity>>(beanType(FileEntity::class.java))?.data
            }
            body.isJsonArray() -> {
                body.fromJson(JsonArray::class.java)?.get(0)?.toJson()
                    ?.fromJson(FileEntity::class.java)
            }
            else -> throw IllegalStateException("格式异常,非[json]格式:$body")
        } ?: FileEntity(diskPath = filePath)

//        val code = resultJson?.getInt("code") ?: -1
//        val data = resultJson?.getJson("data")
//        val path = data?.getString("filePath")?.toUrl()
//        val id = data?.getLong("id") ?: -1
//
//        if (code in 200..299 && data?.getLong("id") ?: -1 > 0) {
//            return FileEntity(id = id, filePath = path ?: "", diskPath = filePath)
//        }
//        return FileEntity(diskPath = filePath)

        return fileEntity
    }
}

/**快速上传单个文件*/
fun String.uploadFile(action: (FileEntity?) -> Unit) =
    runRx({
        JavaFormAttachManager().apply {
            attachUploadUrl = JavaFormAttachManager.JAVA_UPLOAD_URL.newProgressUrl(this@uploadFile)
        }.uploadFile(this)
    }) {
        action(it)
    }

/**开始上传Json当中的文件附件*/
fun String.uploadFileAttach(config: JsonAttachUploader.() -> Unit): JsonAttachUploader {
    return uploadAttach {
        onUploadFile = { filePath ->
            filePath.uploadFile {
                uploadNext(
                    filePath,
                    it?.id,
                    if (it == null) IllegalStateException("上传失败") else null
                )
            }
        }
        config()
    }
}

fun String.removeFileProgress() {
    JavaFormAttachManager.JAVA_UPLOAD_URL.removeUploadProgress(this)
}

/**快速监听上传文件进度*/
fun String.uploadFileProgress(action: (ProgressInfo?, Exception?) -> Unit) =
    JavaFormAttachManager.JAVA_UPLOAD_URL.uploadProgress(key = this, action = action)

```