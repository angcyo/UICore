package com.angcyo.http

import com.angcyo.http.base.*
import com.angcyo.http.exception.HttpDataException
import com.angcyo.http.rx.observableToMain
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
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

//<editor-fold desc="通用网络接口">

interface Api {

    /*------------以下是[POST]请求-----------------*/

    /**Content-Type: application/json;charset=UTF-8*/
    @POST
    fun post(
        @Url url: String,
        @Body json: JsonElement = JsonObject(),
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Observable<Response<JsonElement>>

    @POST
    fun post2Body(
        @Url url: String,
        @Body json: JsonElement = JsonObject(),
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Observable<Response<ResponseBody>>

    /*------------以下是[GET]请求-----------------*/

    /**Content-Type: application/json;charset=UTF-8*/
    @GET
    fun get(
        @Url url: String,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Observable<Response<JsonElement>>

    @GET
    fun get2Body(
        @Url url: String,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Observable<Response<ResponseBody>>

    @PUT
    fun put(
        @Url url: String,
        @Body json: JsonElement = JsonObject(),
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Observable<Response<JsonElement>>

    @PUT
    fun put2Body(
        @Url url: String,
        @Body json: JsonElement = JsonObject(),
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Observable<Response<ResponseBody>>
}

//</editor-fold desc="通用网络接口">

//<editor-fold desc="通用网络接口(协程)">

interface ApiKt {

    @POST
    suspend fun post(
        @Url url: String,
        @Body json: JsonElement = JsonObject(),
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Response<JsonElement>

    @POST
    suspend fun post2Body(
        @Url url: String,
        @Body json: JsonElement = JsonObject(),
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Response<ResponseBody>

    @GET
    suspend fun get(
        @Url url: String,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Response<JsonElement>

    @GET
    suspend fun get2Body(
        @Url url: String,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Response<ResponseBody>

    @PUT
    suspend fun put(
        @Url url: String,
        @Body json: JsonElement = JsonObject(),
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Response<JsonElement>

    @PUT
    suspend fun put2Body(
        @Url url: String,
        @Body json: JsonElement = JsonObject(),
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf()
    ): Response<ResponseBody>
}

//</editor-fold desc="通用网络接口(协程)">

//<editor-fold desc="基础">

object DslHttp {
    val dslHttpConfig = DslHttpConfig()

    /**自定义配置, 否则使用库中默认配置*/
    fun config(action: DslHttpConfig.() -> Unit) {
        dslHttpConfig.action()
    }

    fun init() {
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
    }
}

/**
 * 通用接口请求
 * */
fun <T> dslHttp(service: Class<T>): T {
    DslHttp.init()
    val retrofit = DslHttp.dslHttpConfig.retrofit!!
    /*如果单例API对象的话, 就需要在动态切换BaseUrl的时候, 重新创建. 否则不会生效*/
    return retrofit.create(service)
}

/**拼接 host 和 api接口*/
fun connectUrl(host: String?, url: String?): String {
    val h = host?.trimEnd('/') ?: ""
    val u = url?.trimStart('/') ?: ""
    return "$h/$u"
}

/**根据[RequestConfig]发送网络请求*/
fun http(config: RequestConfig.() -> Unit): Observable<Response<JsonElement>> {
    val requestConfig = RequestConfig()
    requestConfig.method = GET
    requestConfig.config()

    if (requestConfig.autoConnectUrl && !requestConfig.url.startsWith("http")) {
        requestConfig.url =
            connectUrl(DslHttp.dslHttpConfig.onGetBaseUrl(), requestConfig.url)
    }

    return when (requestConfig.method) {
        POST -> {
            dslHttp(Api::class.java).post(
                requestConfig.url,
                requestConfig.body,
                requestConfig.query
            )
        }
        PUT -> {
            dslHttp(Api::class.java).put(
                requestConfig.url,
                requestConfig.body,
                requestConfig.query
            )
        }
        else -> {
            dslHttp(Api::class.java).get(requestConfig.url, requestConfig.query)
        }
    }
        .compose(observableToMain())
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

/**根据[RequestConfig]发送网络请求*/
fun http2Body(config: RequestBodyConfig.() -> Unit): Observable<Response<ResponseBody>> {
    val requestConfig = RequestBodyConfig()
    requestConfig.method = GET
    requestConfig.config()

    if (requestConfig.autoConnectUrl && !requestConfig.url.startsWith("http")) {
        requestConfig.url =
            connectUrl(DslHttp.dslHttpConfig.onGetBaseUrl(), requestConfig.url)
    }

    return when (requestConfig.method) {
        POST -> {
            dslHttp(Api::class.java).post2Body(
                requestConfig.url,
                requestConfig.body,
                requestConfig.query
            )
        }
        PUT -> {
            dslHttp(Api::class.java).put2Body(
                requestConfig.url,
                requestConfig.body,
                requestConfig.query
            )
        }
        else -> {
            dslHttp(Api::class.java).get2Body(requestConfig.url, requestConfig.query)
        }
    }
        .compose(observableToMain())
        .doOnSubscribe {
            requestConfig.onStart(it)
        }
        .doOnNext {
            when {
                requestConfig.isSuccessful(it) -> {
                    requestConfig.onSuccess(it)
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

/**判断http状态码为成功, 并且接口返回状态也为成功*/
fun Response<JsonElement>?.isSucceed(codeKey: String = "code"): Boolean {
    val bodyData = this?.body() ?: return false
    var result = false
    if (isSuccessful && bodyData is JsonObject) {
        if (bodyData.getInt(codeKey) in 200..299) {
            result = true
        }
    }
    return result
}

//</editor-fold desc="基础">

//<editor-fold desc="RxJava异步网络请求">

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

fun put(config: RequestConfig.() -> Unit): Observable<Response<JsonElement>> {
    return http {
        method = PUT
        this.config()
    }
}

fun get2Body(config: RequestBodyConfig.() -> Unit): Observable<Response<ResponseBody>> {
    return http2Body(config)
}

/**快速发送一个[post]请求*/
fun post2Body(config: RequestBodyConfig.() -> Unit): Observable<Response<ResponseBody>> {
    return http2Body {
        method = POST
        this.config()
    }
}

fun put2Body(config: RequestBodyConfig.() -> Unit): Observable<Response<ResponseBody>> {
    return http2Body {
        method = PUT
        this.config()
    }
}

//</editor-fold desc="RxJava异步网络请求">

//<editor-fold desc="协程同步网络请求">

suspend fun String.get(queryMap: HashMap<String, Any> = hashMapOf()): Response<JsonElement> {
    return dslHttp(ApiKt::class.java).get(this, queryMap)
}

suspend fun String.post(
    json: JsonElement = JsonObject(),
    queryMap: HashMap<String, Any> = hashMapOf()
): Response<JsonElement> {
    return dslHttp(ApiKt::class.java).post(this, json, queryMap)
}

suspend fun String.put(
    json: JsonElement = JsonObject(),
    queryMap: HashMap<String, Any> = hashMapOf()
): Response<JsonElement> {
    return dslHttp(ApiKt::class.java).put(this, json, queryMap)
}

//--

suspend fun String.get2Body(queryMap: HashMap<String, Any> = hashMapOf()): Response<ResponseBody> {
    return dslHttp(ApiKt::class.java).get2Body(this, queryMap)
}

suspend fun String.post2Body(
    json: JsonElement = JsonObject(),
    queryMap: HashMap<String, Any> = hashMapOf()
): Response<ResponseBody> {
    return dslHttp(ApiKt::class.java).post2Body(this, json, queryMap)
}

suspend fun String.put2Body(
    json: JsonElement = JsonObject(),
    queryMap: HashMap<String, Any> = hashMapOf()
): Response<ResponseBody> {
    return dslHttp(ApiKt::class.java).put2Body(this, json, queryMap)
}

//</editor-fold desc="协程同步网络请求">

//<editor-fold desc="JsonElement to Bean">

/**[JsonElement]转换成数据bean*/
fun <T> Response<JsonElement>.toBean(type: Type, parseError: Boolean = false): T? {
    return when {
        isSuccessful -> {
            when (val bodyJson = body().toJson()) {
                null -> null
                else -> bodyJson.fromJson(type)
            }
        }
        parseError -> {
            when (val bodyJson = errorBody()?.readString()) {
                null -> null
                else -> bodyJson.fromJson(type)
            }
        }
        else -> null
    }
}

inline fun <reified Bean> Response<JsonElement>.toBean(parseError: Boolean = false): Bean? {
    return when {
        isSuccessful -> {
            when (val bodyJson = body().toJson()) {
                null -> null
                else -> bodyJson.fromJson(Bean::class.java)
            }
        }
        parseError -> {
            when (val bodyJson = errorBody()?.readString()) {
                null -> null
                else -> bodyJson.fromJson(Bean::class.java)
            }
        }
        else -> null
    }
}

//</editor-fold desc="JsonElement to Bean">


//<editor-fold desc="网络请求配置项">

const val GET = 1
const val POST = 2
const val PUT = 3

open class BaseRequestConfig {
    //请求方法
    var method: Int = GET

    //接口api, 可以是全路径, 也可以是相对于baseUrl的路径
    var url: String = ""

    //自动根据url不是http开头,拼接上baseUrl
    var autoConnectUrl: Boolean = true

    //body数据, 仅用于post请求
    var body: JsonElement = JsonObject()

    //url后面拼接的参数列表
    var query: HashMap<String, Any> = hashMapOf()

    //解析请求返回的json数据, 判断code是否是成功的状态, 否则走异常流程.
    var codeKey: String = "code"
    var msgKey: String = "msg"

    var onStart: (Disposable) -> Unit = {}

    //请求结束, 网络状态成功, 但是数据状态不一定成功
    var onComplete: () -> Unit = {}

    //异常处理
    var onError: (Throwable) -> Unit = {}
}

open class RequestConfig : BaseRequestConfig() {
    //判断返回的数据
    var isSuccessful: (Response<JsonElement>) -> Boolean = {
        it.isSucceed(codeKey)
    }

    //http状态请求成功才回调
    var onSuccess: (Response<JsonElement>) -> Unit = {}
}

open class RequestBodyConfig : BaseRequestConfig() {
    //判断返回的数据
    var isSuccessful: (Response<ResponseBody>) -> Boolean = {
        it.isSuccessful
    }

    //http状态请求成功才回调
    var onSuccess: (Response<ResponseBody>) -> Unit = {}
}

//</editor-fold desc="网络请求配置项">

