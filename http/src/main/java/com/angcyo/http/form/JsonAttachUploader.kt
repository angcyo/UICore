package com.angcyo.http.form

import com.angcyo.http.base.JsonPathParser
import com.angcyo.http.rx.doBack
import com.angcyo.http.rx.doMain
import com.angcyo.library.L
import com.angcyo.library.ex.connect
import java.io.File

/**
 * 从json字符串中, 获取本地文件路径上传, 并替换原来的字符
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class JsonAttachUploader {

    /**上传完成之后, 请将文件路径和文件id的映射关系保存在此*/
    var fileIdMap = hashMapOf<String, Long>()

    /**上传文件的回调, 子线程回调*/
    var onUploadFile: (uploader: JsonAttachUploader, filePath: String) -> Unit =
        { uploader, filePath ->
            //uploader.uploadNext(null)
            //uploader.stopUpload()
        }

    /**所有附件上传完成回调. 主线程回调*/
    var onUploadFinish: (json: String, error: Throwable?) -> Unit = { _, _ -> }

    /**目标json*/
    var _targetJson: String? = null

    /**开始上传*/
    fun startUpload(json: String) {
        if (_targetJson == json) {
            return
        }
        _targetJson = json
        _parsePathFromJson(json)
        L.i(_attachPathMap)
        //L.i(_attachPathList)
        _startUpload(0)
    }

    /**停止上传*/
    fun stopUpload() {
        _targetJson = null
    }

    /**保存附件在json中对应的key和value*/
    val _attachPathMap = hashMapOf<String, String>()

    /**保存需要上传的附件列表*/
    val _attachPathList = mutableListOf<String>()

    /**从json中找出需要上传的文件路径*/
    fun _parsePathFromJson(json: String) {
        _attachPathMap.clear()
        _attachPathList.clear()
        JsonPathParser.read(json) { jsonPath, value ->

            if (value.startsWith(FormAttachManager.HTTP_PREFIX) || value.startsWith(File.separatorChar)) {
                _attachPathMap[jsonPath] = value

                value.split(FormAttachManager.ATTACH_SPLIT).forEach {
                    _attachPathList.add(it)
                }
            }
        }
    }

    /**当前上传的索引位置*/
    var _uploadIndex = 0

    fun _startUpload(index: Int) {
        if (_targetJson.isNullOrEmpty()) {
            return
        }

        _uploadIndex = index
        if (index >= _attachPathList.size) {
            _uploadFinish(null)
        } else {
            val filePath = _attachPathList[index]
            doBack {
                onUploadFile(this, filePath)
            }
        }
    }

    /**[error]是否有异常*/
    fun uploadNext(filePath: String, fileId: Long = -1, error: Throwable? = null) {
        if (error == null) {
            fileIdMap[filePath] = fileId
            _startUpload(_uploadIndex + 1)
        } else {
            _uploadFinish(error)
        }
    }

    /**所有文件上传结束*/
    fun _uploadFinish(error: Throwable?) {
        //attachUploadFinish

        //将附件上传后的数据, 重新写入json
        val map = hashMapOf<String, String>()
        _attachPathMap.forEach { (key, value) ->
            //json原始数据
            val originList = mutableListOf<String>()
            value.split(FormAttachManager.ATTACH_SPLIT).forEach {
                originList.add(it)
            }

            //url to id
            val idsList = mutableListOf<Long>()
            originList.forEach { path ->
                val id = fileIdMap[path] ?: -1
                if (id != -1L) {
                    idsList.add(id)
                }
            }

            //connect
            map[key] = idsList.connect()
        }

        if (_targetJson.isNullOrEmpty()) {
            //no op
        } else {
            val json = JsonPathParser.write(_targetJson ?: "{}", map)
            L.i(json)
            doMain {
                onUploadFinish(json, error)
            }
        }
    }
}

/**开始上传附件*/
fun String.uploadAttach(config: JsonAttachUploader.() -> Unit): JsonAttachUploader {
    val uploader = JsonAttachUploader()
    uploader.apply(config)
    uploader.startUpload(this)
    return uploader
}