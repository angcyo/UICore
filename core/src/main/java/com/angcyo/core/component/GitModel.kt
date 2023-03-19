package com.angcyo.core.component

import android.os.Build
import com.angcyo.core.lifecycle.LifecycleViewModel
import com.angcyo.core.vmApp
import com.angcyo.http.*
import com.angcyo.http.base.*
import com.angcyo.http.rx.observe
import com.angcyo.library.L
import com.angcyo.library.ex.nowTimeString
import com.angcyo.library.model.Page
import com.google.gson.JsonElement

/**
 *
 * Git Open Api
 *
 * https://gitee.com/api/v5/swagger
 *
 * https://docs.github.com/cn/rest
 *
 * ---
 *
 * Gitee gits
 *
 * https://gitee.com/
 *
 * https://gitee.com/angcyo/dashboard
 *
 * https://gitee.com/angcyo/dashboard/codes
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

    /**获取单条代码片段
     * https://gitee.com/api/v5/swagger#/getV5GistsId
     * */
    fun getGist(id: String, result: HttpCallback<Map<String, Any>>? = null) {
        get {
            url = "gists/${id}".toApi(GIT_OPEN_API_URL)
            query = hashMapOf("access_token" to ACCESS_TOKEN)
            codeKey = null
        }.observe { data, error ->
            val map = data?.toBean<Map<String, Any>>(mapType(String::class.java, Any::class.java))
            L.i(map)
            result?.invoke(map, error)
        }
    }

    /**获取代码片段分页列表
     * https://gitee.com/api/v5/swagger#/getV5Gists
     * */
    fun getGistList(page: Page, result: HttpCallback<List<Any>>? = null) {
        get {
            url = "gists".toApi(GIT_OPEN_API_URL)
            query = hashMapOf(
                "access_token" to ACCESS_TOKEN,
                "page" to page.requestPageIndex,
                "per_page" to page.requestPageSize
            )
            codeKey = null
        }.observe { data, error ->
            val list = data?.toBean<List<Any>>(listType(Any::class.java))
            L.i(list)
            result?.invoke(list, error)
        }
    }

    /**创建一个代码片段
     * https://gitee.com/api/v5/swagger#/postV5Gists
     * */
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

    /**
     * 修改代码片段
     *
     * [id] 要修改的片段id
     * [body] 内容可以包括 [files] 和 [description] 字段
     *
     * https://gitee.com/api/v5/swagger#/patchV5GistsId
     * */
    fun patchGist(id: String, body: JsonElement, result: HttpCallback<Map<String, Any>>? = null) {
        patch {
            url = "gists/${id}".toApi(GIT_OPEN_API_URL)
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
 *   "access_token": "xxx",
 *   "files": {
 *     "file1.txt": {
 *       "content": "String file contents"
 *       }
 *   },
 *   "description": "2021-12-22",
 *   "public": "false"
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

//---

/**将文本推送到Gist
 * [gistName] Gist列表当中显示的文本
 * [content] 一段内容
 * */
fun pushToGist(content: String, gistName: String = "${nowTimeString()}/${Build.MODEL}/gist") {
    vmApp<GitModel>().postGist(gistBodyBuilder(gistName) {
        addGistFile(gistName, content)
    })
}

fun pushToGist(
    gistName: String = "${nowTimeString()}/${Build.MODEL}/gist",
    contentAction: JsonBuilder.() -> Unit
) {
    vmApp<GitModel>().postGist(gistBodyBuilder(gistName) {
        //addGistFile()
        contentAction()
    })
}