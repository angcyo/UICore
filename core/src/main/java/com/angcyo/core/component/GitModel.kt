package com.angcyo.core.component

import com.angcyo.core.lifecycle.LifecycleViewModel
import com.angcyo.http.base.HttpCallback
import com.angcyo.http.base.JsonBuilder
import com.angcyo.http.base.jsonObject
import com.angcyo.http.base.mapType
import com.angcyo.http.post
import com.angcyo.http.rx.observe
import com.angcyo.http.toApi
import com.angcyo.http.toBean
import com.angcyo.library.L
import com.google.gson.JsonElement

/**
 *
 * Git Open Api
 *
 * https://gitee.com/api/v5/swagger
 *
 * https://docs.github.com/cn/rest
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class GitModel : LifecycleViewModel() {

    companion object {

        /**
         * https://gitee.com/api/v5
         *
         * https://api.github.com
         *
         * */
        var GIT_OPEN_API_URL = "https://gitee.com/api/v5"

        /**授权码*/
        var ACCESS_TOKEN = ""
    }

    //<editor-fold desc="gist codes 代码片段操作">

    /**创建一个代码片段*/
    fun postGist(body: JsonElement, result: HttpCallback<Map<String, Any>>? = null) {
        post {
            url = "gists".toApi(GIT_OPEN_API_URL)
            this.body = body
            codeKey = null
        }.observe { data, error ->
            val map = data?.toBean<Map<String, Any>>(mapType(String::class.java, Any::class.java))
            L.i(map)
            result?.invoke(map, error)
        }
    }

    //</editor-fold desc="gist codes 代码片段操作">
}

/**
 *
 * {
 * "access_token": "xxx",
 * "files": {
 * "file1.txt": {
 * "content": "String file contents"
 * }
 * },
 * "description": "2021-12-22",
 * "public": "false"
 * }
 *
 * [description] 判断的描述, 也是标题
 * [filesBuilder] 真正的片段在此加入, 如 add("片段1.json", "xxx"), 第一个参数是片段的标题, 第二个是数据内容
 * */
fun gistBodyBuilder(description: String, filesBuilder: JsonBuilder.() -> Unit): JsonElement {
    return jsonObject {
        add("access_token", GitModel.ACCESS_TOKEN)
        add("public", false)
        add("description", description)
        add("files", jsonObject {
            this.filesBuilder()
        })
    }
}

/**注意: content 不能为空, 否则会报错
 * {"message":"文件名或内容不能为空"} */
fun JsonBuilder.addGistFile(name: String, content: Any) {
    add(name, jsonObject {
        add("content", content)
    })
}