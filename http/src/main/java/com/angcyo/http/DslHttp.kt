package com.angcyo.http

import com.angcyo.http.DslHttp.dslHttpConfig
import com.angcyo.http.base.*
import com.angcyo.http.exception.HttpDataException
import com.angcyo.http.rx.observableToMain
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import retrofit2.Response
import retrofit2.http.*
import java.lang.reflect.Type

/**
 * 网络请求库
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

interface Api {

    /*------------以下是[POST]请求-----------------*/

    /**Content-Type: application/json;charset=UTF-8*/
    @POST
    fun post(
        @Url url: String,
        @Body json: JsonElement = JsonObject(),
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Observable<Response<JsonElement>>

    /*------------以下是[GET]请求-----------------*/

    /**Content-Type: application/json;charset=UTF-8*/
    @GET
    fun get(
        @Url url: String,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Observable<Response<JsonElement>>

}

object DslHttp {
    val dslHttpConfig = DslHttpConfig()

    /**自定义配置, 否则使用库中默认配置*/
    fun config(action: DslHttpConfig.() -> Unit) {
        dslHttpConfig.action()
    }
}

/**
 * 通用接口请求
 * */
fun dslHttp(): Api {

    val baseUrl = dslHttpConfig.onGetBaseUrl()

    if (baseUrl.isEmpty()) {
        throw NullPointerException("请先初始化[DslHttp.config{ ... }]")
    }

    val client = dslHttpConfig.onBuildHttpClient(
        dslHttpConfig.defaultOkHttpClientBuilder.apply {
            dslHttpConfig.onConfigOkHttpClient.forEach {
                it(this)
            }
        }
    )
    dslHttpConfig.okHttpClient = client

    val retrofit = dslHttpConfig.onBuildRetrofit(dslHttpConfig.defaultRetrofitBuilder, client)
    dslHttpConfig.retrofit = retrofit

    /*如果单例API对象的话, 就需要在动态切换BaseUrl的时候, 重新创建. 否则不会生效*/
    return retrofit.create(Api::class.java)
}

/**拼接 host 和 api接口*/
fun connectUrl(host: String?, url: String?): String {
    val h = host?.trimEnd('/') ?: ""
    val u = url?.trimStart('/') ?: ""
    return "$h/$u"
}

/**根据[RequestConfig]发送网络请求*/
fun http(config: RequestConfig.() -> Unit): Observable<Response<JsonElement>> {
    val requestConfig = RequestConfig(GET)
    requestConfig.config()

    if (requestConfig.autoConnectUrl && !requestConfig.url.startsWith("http")) {
        requestConfig.url =
            connectUrl(dslHttpConfig.onGetBaseUrl(), requestConfig.url)
    }

    return if (requestConfig.method == POST) {
        dslHttp().post(requestConfig.url, requestConfig.body, requestConfig.query)
    } else {
        dslHttp().get(requestConfig.url, requestConfig.query)
    }.compose(observableToMain())
        .doOnSubscribe {
            requestConfig.onStart(it)
        }
        .doOnNext {
            val body = it.body()
            when {
                requestConfig.isSuccessful(it) -> {
                    requestConfig.onSuccess(it)
                }
                body is JsonObject -> {
                    throw HttpDataException(body.getString(requestConfig.msgKey) ?: "数据异常")
                }
                else -> {
                    requestConfig.onSuccess(it)
                }
            }
        }
        .doOnComplete {
            requestConfig.onComplete()
        }
        .doOnError {
            requestConfig.onError(it)
        }
}

/**快速发送一个[get]请求*/
fun get(config: RequestConfig.() -> Unit): Observable<Response<JsonElement>> {
    return http(config)
}

/**快速发送一个[post]请求*/
fun post(config: RequestConfig.() -> Unit): Observable<Response<JsonElement>> {
    return http {
        method = POST
        this.config()
    }
}

/**[JsonElement]转换成数据bean*/
fun <T> Response<JsonElement>.toBean(type: Type): T? {
    return if (isSuccessful) {
        when (val bodyJson = body().toJson()) {
            null -> null
            else -> bodyJson.fromJson(type)
        }
    } else {
        when (val bodyJson = errorBody()?.readString()) {
            null -> null
            else -> bodyJson.fromJson(type)
        }
    }
}

const val GET = 1
const val POST = 2

data class RequestConfig(
    //请求方法
    var method: Int = GET,
    //接口api, 可以是全路径, 也可以是相对于baseUrl的路径
    var url: String = "",
    //自动根据url不是http开头,拼接上baseUrl
    var autoConnectUrl: Boolean = true,
    //body数据, 仅用于post请求
    var body: JsonElement = JsonObject(),
    //url后面拼接的参数列表
    var query: HashMap<String, Any> = hashMapOf(),

    //解析请求返回的json数据, 判断code是否是成功的状态, 否则走异常流程.
    var codeKey: String = "code",
    var msgKey: String = "msg",

    //判断返回的数据
    var isSuccessful: (Response<JsonElement>) -> Boolean = {
        val jsonElement = it.body()
        var result = false
        if (it.isSuccessful && jsonElement is JsonObject) {
            if (jsonElement.getInt(codeKey) in 200..299) {
                result = true
            }
        }
        result
    },

    var onStart: (Disposable) -> Unit = {},
    //http状态请求成功才回调
    var onSuccess: (Response<JsonElement>) -> Unit = {},
    //请求结束, 网络状态成功, 但是数据状态不一定成功
    var onComplete: () -> Unit = {},
    //异常处理
    var onError: (Throwable) -> Unit = {}
)


