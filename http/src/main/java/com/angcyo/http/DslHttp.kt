package com.angcyo.http

import com.angcyo.http.DslHttp.DEFAULT_CODE_KEY
import com.angcyo.http.DslHttp.DEFAULT_MSG_KEY
import com.angcyo.http.DslHttp.retrofit
import com.angcyo.http.base.fromJson
import com.angcyo.http.base.getBoolean
import com.angcyo.http.base.getInt
import com.angcyo.http.base.getString
import com.angcyo.http.base.isBoolean
import com.angcyo.http.base.isSuccess
import com.angcyo.http.base.readString
import com.angcyo.http.base.toFilePart
import com.angcyo.http.base.toJson
import com.angcyo.http.exception.HttpDataException
import com.angcyo.http.exception.HttpResponseException
import com.angcyo.http.rx.errorMessage
import com.angcyo.http.rx.observableToBack
import com.angcyo.http.rx.observableToMain
import com.angcyo.library.L
import com.angcyo.library.ex._string
import com.angcyo.library.ex.connectUrl
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.QueryMap
import retrofit2.http.Url
import java.io.File
import java.lang.reflect.Type
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


/**
 * 网络请求库
 * https://www.jianshu.com/p/865e9ae667a0
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

//<editor-fold desc="通用网络接口">

interface Api {

    /*------------以下是[POST]请求-----------------*/

    //Field map contained null value for key
    //{@code null} value for the map, as a key, or as a value is not allowed.

    /**Content-Type: application/json;charset=UTF-8*/
    @POST
    fun post(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<JsonElement>>

    /**直接发送请求体
     * [RequestBody]*/
    @POST
    fun postBody(
        @Url url: String,
        @Body body: RequestBody?,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<JsonElement>>

    @POST
    fun post2Body(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<ResponseBody>>

    /**直接发送请求体
     * [RequestBody]*/
    @POST
    fun postBody2Body(
        @Url url: String,
        @Body body: RequestBody?,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<ResponseBody>>

    /**application/x-www-form-urlencoded MIME类型
     * Field map contained null value for key*/
    @POST
    @FormUrlEncoded
    fun postForm(
        @Url url: String,
        @FieldMap formMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<JsonElement>>

    /*------------以下是[GET]请求-----------------*/

    /**Content-Type: application/json;charset=UTF-8*/
    @GET
    fun get(
        @Url url: String,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<JsonElement>>

    @GET
    fun get2Body(
        @Url url: String,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<ResponseBody>>

    /*------------以下是[PUT]请求-----------------*/

    @PUT
    fun put(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<JsonElement>>

    @PUT
    fun put2Body(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<ResponseBody>>

    /*------------以下是[PATCH]请求-----------------*/

    @PATCH
    fun patch(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<JsonElement>>

    @PATCH
    fun patchBody(
        @Url url: String,
        @Body body: RequestBody?,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<ResponseBody>>

    @PATCH
    fun patch2Body(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<ResponseBody>>

    /*------------以下是上传文件[POST]请求-----------------*/

    @Multipart
    @POST
    fun uploadFile(
        @Url url: String,
        @Part file: MultipartBody.Part?,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<JsonElement>>

    @Multipart
    @POST
    fun uploadFiles(
        @Url url: String,
        @Part fileList: List<MultipartBody.Part>?,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<JsonElement>>

    @Multipart
    @POST
    fun uploadFile2Body(
        @Url url: String,
        @Part file: MultipartBody.Part?,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<ResponseBody>>

    @Multipart
    @POST
    fun uploadFiles2Body(
        @Url url: String,
        @Part fileList: List<MultipartBody.Part>?,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Observable<Response<ResponseBody>>

}

//</editor-fold desc="通用网络接口">

//<editor-fold desc="通用网络接口(协程)">

interface ApiKt {

    @POST
    suspend fun post(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Response<JsonElement>

    @POST
    suspend fun post2Body(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Response<ResponseBody>

    @GET
    suspend fun get(
        @Url url: String,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Response<JsonElement>

    @GET
    suspend fun get2Body(
        @Url url: String,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Response<ResponseBody>

    @PUT
    suspend fun put(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Response<JsonElement>

    @PUT
    suspend fun put2Body(
        @Url url: String,
        @Body json: JsonElement? = null,
        @QueryMap queryMap: HashMap<String, Any> = hashMapOf(),
        @HeaderMap headerMap: HashMap<String, String> = hashMapOf()
    ): Response<ResponseBody>
}

//</editor-fold desc="通用网络接口(协程)">

//<editor-fold desc="基础">

object DslHttp {

    /**逻辑错误code-key*/
    var DEFAULT_CODE_KEY = "code"

    /**提示消息msg-key*/
    var DEFAULT_MSG_KEY = "msg"

    /**默认的错误提示*/
    var DEFAULT_ERROR_MSG = _string(R.string.http_exception)

    val dslHttpConfig = DslHttpConfig()

    /**获取客户端实例*/
    val client: OkHttpClient
        get() {
            init()
            return dslHttpConfig.okHttpClient!!
        }

    /**上传文件的实现方法
     * [filePath] 需要上传的文件路径
     * [callback] 回调
     *   [url] 可以下载的服务器地址
     *   [error] 是否失败*/
    var uploadFileAction: ((filePath: String, callback: (url: String?, error: Exception?) -> Unit) -> Unit)? =
        null

    /**自定义配置, 否则使用库中默认配置*/
    fun config(action: DslHttpConfig.() -> Unit) {
        dslHttpConfig.reset()
        dslHttpConfig.action()
    }

    /**初始化和缓存客户端*/
    fun init() {
        val baseUrl = dslHttpConfig.onGetBaseUrl()

        if (baseUrl.isEmpty()) {
            throw NullPointerException("请先初始化[DslHttp.config{ ... }]")
        }

        //缓存客户端
        val client = dslHttpConfig.okHttpClient ?: dslHttpConfig.onBuildHttpClient(
            dslHttpConfig.defaultOkHttpClientBuilder.apply {
                dslHttpConfig.onConfigOkHttpClient.forEach {
                    it(this)
                }
            }
        )
        dslHttpConfig.okHttpClient = client

        //缓存retrofit
        val retrofit = dslHttpConfig.retrofit
            ?: dslHttpConfig.onBuildRetrofit(dslHttpConfig.defaultRetrofitBuilder, client)
        dslHttpConfig.retrofit = retrofit
    }

    /**获取[OkHttpClient]对象*/
    fun httpClient(rebuild: Boolean = false): OkHttpClient {
        if (rebuild) {
            dslHttpConfig.okHttpClient = null
        }
        init()
        return dslHttpConfig.okHttpClient!!
    }

    /**根据配置, 创建一个[OkHttpClient]客户端*/
    fun createClient(): OkHttpClient {
        val client = dslHttpConfig.defaultOkHttpClientBuilder.apply {
            dslHttpConfig.onConfigOkHttpClient.forEach {
                it(this)
            }
        }.build()
        return client
    }

    /**获取[Retrofit]对象*/
    @Synchronized
    fun retrofit(rebuild: Boolean = false): Retrofit {
        if (rebuild) {
            dslHttpConfig.retrofit = null
        } else {
            val oldUrl = dslHttpConfig.retrofit?.baseUrl()?.toString()
            val newUrl = dslHttpConfig.onGetBaseUrl()
            if (oldUrl != null && oldUrl != newUrl) {
                //url改变之后,也需要重建
                dslHttpConfig.reset()
            }
        }
        init()
        return dslHttpConfig.retrofit!!
    }

    /**去掉ssl验证*/
    fun noSSL() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true")
        val trm: TrustManager = object : X509TrustManager {
            override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
            override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate>? = null
        }
        val sc: SSLContext = SSLContext.getInstance("SSL")
        sc.init(null, arrayOf(trm), null)
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
    }
}

/**
 * 通用接口请求
 * */
fun <T> dslHttp(service: Class<T>): T? {
    return try {
        val retrofit = retrofit(false)
        /*如果单例API对象的话, 就需要在动态切换BaseUrl的时候, 重新创建. 否则不会生效*/
        retrofit.create(service)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**将请求路径转换成api接口地址*/
fun String?.toApi(host: String? = DslHttp.dslHttpConfig.onGetBaseUrl()): String {
    return connectUrl(host, this)
}

/**拼接 host 和 api接口*/
fun connectUrl(host: String?, url: String?): String {
//    val h = host?.trimEnd('/') ?: ""
//    val u = url?.trimStart('/') ?: ""
//    return "$h/$u"
    return host.connectUrl(url)
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

    //智能调整请求方式
    if (requestConfig.formMap.isNotEmpty()) {
        requestConfig.method = POST_FORM
    }

    val observable = when (requestConfig.method) {
        POST -> {
            if (requestConfig.filePart != null) {
                //单文件上传
                dslHttp(Api::class.java)?.uploadFile(
                    requestConfig.url,
                    requestConfig.filePart,
                    requestConfig.query,
                    requestConfig.header
                )
            } else if (!requestConfig.filePartList.isNullOrEmpty()) {
                //多文件上传
                dslHttp(Api::class.java)?.uploadFiles(
                    requestConfig.url,
                    requestConfig.filePartList,
                    requestConfig.query,
                    requestConfig.header
                )
            } else if (requestConfig.requestBody == null) {
                dslHttp(Api::class.java)?.post(
                    requestConfig.url,
                    requestConfig.body,
                    requestConfig.query,
                    requestConfig.header
                )
            } else {
                dslHttp(Api::class.java)?.postBody(
                    requestConfig.url,
                    requestConfig.requestBody,
                    requestConfig.query,
                    requestConfig.header
                )
            }
        }

        PATCH -> {
            dslHttp(Api::class.java)?.patch(
                requestConfig.url,
                requestConfig.body,
                requestConfig.query,
                requestConfig.header
            )
        }

        POST_FORM -> {
            dslHttp(Api::class.java)?.postForm(
                requestConfig.url,
                requestConfig.formMap,
                requestConfig.header
            )
        }

        PUT -> {
            dslHttp(Api::class.java)?.put(
                requestConfig.url,
                requestConfig.body,
                requestConfig.query,
                requestConfig.header
            )
        }

        else -> {
            dslHttp(Api::class.java)?.get(
                requestConfig.url,
                requestConfig.query,
                requestConfig.header
            )
        }
    } ?: Observable.error(NullPointerException("retrofit创建异常"))
    //observable
    return observable.compose(if (requestConfig.observableOnMain) observableToMain() else observableToBack())
        .doOnSubscribe {
            requestConfig.onStart(it)
        }
        .doOnNext {
            if (it.isSuccessful) {
                val body = it.body()
                when {
                    requestConfig.isSuccessful(it) -> {
                        requestConfig.onSuccess(it)
                    }

                    body is JsonObject -> {
                        L.e(requestConfig.url)
                        if (body.has(requestConfig.codeKey)) {
                            throw HttpDataException(
                                body.getString(requestConfig.msgKey) ?: DslHttp.DEFAULT_ERROR_MSG,
                                body.getInt(requestConfig.codeKey, -200)
                            )
                        } else {
                            throw HttpDataException("返回体无[${requestConfig.codeKey}]", -200)
                        }
                    }

                    else -> {
                        requestConfig.onSuccess(it)
                    }
                }
            } else {
                val code = it.code()
                val message = it.errorMessage() ?: it.message()
                //throw HttpResponseException("[$code]${message}", code, it)
                throw HttpResponseException(message, code, it)
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

    val observable = when (requestConfig.method) {
        POST -> {
            if (requestConfig.filePart != null) {
                //单文件上传
                dslHttp(Api::class.java)?.uploadFile2Body(
                    requestConfig.url,
                    requestConfig.filePart,
                    requestConfig.query,
                    requestConfig.header
                )
            } else if (!requestConfig.filePartList.isNullOrEmpty()) {
                //多文件上传
                dslHttp(Api::class.java)?.uploadFiles2Body(
                    requestConfig.url,
                    requestConfig.filePartList,
                    requestConfig.query,
                    requestConfig.header
                )
            } else if (requestConfig.requestBody == null) {
                dslHttp(Api::class.java)?.post2Body(
                    requestConfig.url,
                    requestConfig.body,
                    requestConfig.query,
                    requestConfig.header
                )
            } else {
                dslHttp(Api::class.java)?.postBody2Body(
                    requestConfig.url,
                    requestConfig.requestBody,
                    requestConfig.query,
                    requestConfig.header
                )
            }
        }

        PUT -> {
            dslHttp(Api::class.java)?.put2Body(
                requestConfig.url,
                requestConfig.body,
                requestConfig.query,
                requestConfig.header
            )
        }

        PATCH -> {
            if (requestConfig.requestBody == null) {
                dslHttp(Api::class.java)?.patch2Body(
                    requestConfig.url,
                    requestConfig.body,
                    requestConfig.query,
                    requestConfig.header
                )
            } else {
                dslHttp(Api::class.java)?.patchBody(
                    requestConfig.url,
                    requestConfig.requestBody,
                    requestConfig.query,
                    requestConfig.header
                )
            }
        }

        else -> {
            dslHttp(Api::class.java)?.get2Body(
                requestConfig.url,
                requestConfig.query,
                requestConfig.header
            )
        }
    } ?: Observable.error(NullPointerException("retrofit创建异常"))
    //observable
    return observable.compose(observableToMain())
        .doOnSubscribe {
            requestConfig.onStart(it)
        }
        .doOnNext {
            if (it.isSuccessful) {
                when {
                    requestConfig.isSuccessful(it) -> {
                        requestConfig.onSuccess(it)
                    }

                    else -> {
                        requestConfig.onSuccess(it)
                    }
                }
            } else {
                val code = it.code()
                throw HttpDataException("[$code]${it.message()}", code)
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
fun Response<JsonElement>?.isSucceed(
    codeKey: String? = DEFAULT_CODE_KEY,
    onResult: (succeed: Boolean, codeErrorJson: JsonObject?) -> Unit = { _, _ -> } /*code码异常时codeErrorJson才有值*/
): Boolean {
    val bodyData = this?.body()

    var result = false
    if (this == null || bodyData == null) {
        //空数据
        result = this?.isSuccessful == true
        onResult(result, null)
        return result
    }

    var errorJson: JsonObject? = null

    if (codeKey.isNullOrEmpty()) {
        result = isSuccessful
    } else if (isSuccessful && bodyData is JsonObject) {
        if (bodyData.isCodeSuccess(codeKey)) {
            result = true
        } else {
            errorJson = bodyData
        }
    }
    onResult(result, errorJson)
    return result
}

/**返回值是否成功*/
fun JsonObject.isCodeSuccess(key: String?): Boolean {
    var result = false
    if (isBoolean(key)) {
        result = getBoolean(key)
    } else if (getInt(key).isSuccess()) {
        result = true
    }
    return result
}

//</editor-fold desc="基础">

//<editor-fold desc="RxJava异步网络请求">

/**快速发送一个[get]请求*/
fun get(config: RequestConfig.() -> Unit): Observable<Response<JsonElement>> {
    return http(config)
}

/**快速发送一个[post]请求
 * 支持
 * [com.angcyo.http.BaseRequestConfig.body]
 * [com.angcyo.http.BaseRequestConfig.requestBody]
 * */
fun post(config: RequestConfig.() -> Unit): Observable<Response<JsonElement>> {
    return http {
        method = POST
        this.config()
    }
}

fun patch(config: RequestConfig.() -> Unit): Observable<Response<JsonElement>> {
    return http {
        method = PATCH
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

/**快速发送一个[post]请求*/
fun postBody2Body(
    body: RequestBody,
    config: RequestBodyConfig.() -> Unit
): Observable<Response<ResponseBody>> {
    return http2Body {
        method = POST
        requestBody = body
        this.config()
    }
}

fun put2Body(config: RequestBodyConfig.() -> Unit): Observable<Response<ResponseBody>> {
    return http2Body {
        method = PUT
        this.config()
    }
}

/**上传文件, 并且返回json结果*/
fun uploadFile(config: RequestConfig.() -> Unit): Observable<Response<JsonElement>> {
    return http {
        method = POST
        this.config()
    }
}

/**上传文件, 并且返回json结果
 * [file] 单文件上传的文件对象
 * [formMap] 表单数据
 * */
fun uploadFile(
    file: File,
    formMap: Map<String, String>? = null,
    config: RequestConfig.() -> Unit
): Observable<Response<JsonElement>> {
    return http {
        method = POST
        val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        if (formMap != null) {
            val list = mutableListOf<MultipartBody.Part>()
            list.add(requestBody.toFilePart(file.name))
            list.addAll(formMap.map { MultipartBody.Part.createFormData(it.key, it.value) })
            filePartList = list
        } else {
            filePart = requestBody.toFilePart(file.name)
        }
        this.config()
    }
}

/**上传文件, 并且返回ResponseBody结果
 *
 * ```
 * task.savePath.file().inputStream().asRequestBody().let { body ->
 *     val coverUpdateApi = "cover_update".toApi(cameraHost)
 *     uploadFile2Body {
 *         this.url = coverUpdateApi
 *         header = hashMapOf(LogInterceptor.closeLog(true))
 *         filePart = body.toFilePart(
 *             task.savePath.getFileAttachmentName() ?: "firmware.bin", name = "update"
 *         )
 *     }.observe { data, error ->
 *         action(task.savePath, error)
 *     }
 * }
 * ```
 *
 * */
fun uploadFile2Body(
    config: RequestBodyConfig.() -> Unit
): Observable<Response<ResponseBody>> {
    return http2Body {
        method = POST
        //filePart 赋值此属性即可
        this.config()
    }
}

fun uploadFile2Body(
    file: File,
    config: RequestBodyConfig.() -> Unit
): Observable<Response<ResponseBody>> {
    return http2Body {
        method = POST
        val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
        filePart = requestBody.toFilePart(file.name)
        this.config()
    }
}

//</editor-fold desc="RxJava异步网络请求">

//<editor-fold desc="协程同步网络请求">

suspend fun String.get(
    queryMap: HashMap<String, Any> = hashMapOf(),
    headerMap: HashMap<String, String> = hashMapOf()
): Response<JsonElement>? {
    return dslHttp(ApiKt::class.java)?.get(this, queryMap, headerMap)
}

suspend fun String.post(
    json: JsonElement = JsonObject(),
    queryMap: HashMap<String, Any> = hashMapOf(),
    headerMap: HashMap<String, String> = hashMapOf()
): Response<JsonElement>? {
    return dslHttp(ApiKt::class.java)?.post(this, json, queryMap, headerMap)
}

suspend fun String.put(
    json: JsonElement = JsonObject(),
    queryMap: HashMap<String, Any> = hashMapOf(),
    headerMap: HashMap<String, String> = hashMapOf()
): Response<JsonElement>? {
    return dslHttp(ApiKt::class.java)?.put(this, json, queryMap, headerMap)
}

//--

suspend fun String.get2Body(
    queryMap: HashMap<String, Any> = hashMapOf(),
    headerMap: HashMap<String, String> = hashMapOf()
): Response<ResponseBody>? {
    return dslHttp(ApiKt::class.java)?.get2Body(this, queryMap, headerMap)
}

suspend fun String.post2Body(
    json: JsonElement = JsonObject(),
    queryMap: HashMap<String, Any> = hashMapOf(),
    headerMap: HashMap<String, String> = hashMapOf()
): Response<ResponseBody>? {
    return dslHttp(ApiKt::class.java)?.post2Body(this, json, queryMap, headerMap)
}

suspend fun String.put2Body(
    json: JsonElement = JsonObject(),
    queryMap: HashMap<String, Any> = hashMapOf(),
    headerMap: HashMap<String, String> = hashMapOf()
): Response<ResponseBody>? {
    return dslHttp(ApiKt::class.java)?.put2Body(this, json, queryMap, headerMap)
}

//</editor-fold desc="协程同步网络请求">

//<editor-fold desc="JsonElement to Bean">

/**[JsonElement]转换成数据bean
 * [parseError] 失败时, 读取errorBody, 并且json解析失败抛出异常
 * data?.toBean<Map<String, Any>>(mapType(String::class.java, Any::class.java))
 * data?.toBean<HttpBean<Any>>(beanType(Any::class.java))?.data
 * */
fun <T> Response<JsonElement>.toBean(
    type: Type,
    parseError: Boolean = false,
    exception: Boolean = false
): T? {
    return when {
        isSuccessful -> {
            when (val bodyJson = body().toJson()) {
                null -> null
                else -> bodyJson.fromJson<T>(type, exception)
            }
        }

        parseError -> {
            when (val bodyJson = errorBody()?.readString()) {
                null -> null
                else -> bodyJson.fromJson<T>(type, parseError)
            }
        }

        else -> null
    }
}

fun <T> Response<JsonElement>.toBean(
    bean: Class<T>,
    parseError: Boolean = false,
    throwError: Boolean = false
): T? {
    return when {
        isSuccessful -> {
            when (val bodyJson = body().toJson()) {
                null -> null
                else -> bodyJson.fromJson(bean, throwError)
            }
        }

        parseError -> {
            when (val bodyJson = errorBody()?.readString()) {
                null -> null
                else -> bodyJson.fromJson(bean)
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
const val PATCH = 4

//如果[formMap]有数据, 则会优先使用[POST_FORM]
const val POST_FORM = 22

open class BaseRequestConfig {
    //请求方法
    var method: Int = GET

    //接口api, 可以是全路径, 也可以是相对于baseUrl的路径
    var url: String = ""

    //自动根据url不是http开头,拼接上baseUrl
    var autoConnectUrl: Boolean = true

    //body数据, 仅用于post请求. @Body
    var body: JsonElement? = JsonObject()

    /**直接发送请求体
     * [com.angcyo.http.Api.postBody]*/
    var requestBody: RequestBody? = null

    /**文件上传使用的body, 单文件上传*/
    var filePart: MultipartBody.Part? = null

    /**多文件上传*/
    var filePartList: List<MultipartBody.Part>? = null

    //url后面拼接的参数列表
    var query: HashMap<String, Any> = hashMapOf()

    //表单格式请求数据 method使用[POST_FORM]
    var formMap: HashMap<String, Any> = hashMapOf()

    //请求头
    var header: HashMap<String, String> = hashMapOf()

    //解析请求返回的json数据, 判断code是否是成功的状态, 否则走异常流程.
    var codeKey: String? = DEFAULT_CODE_KEY
    var msgKey: String? = DEFAULT_MSG_KEY

    var onStart: (Disposable) -> Unit = {}

    //请求结束, 网络状态成功, 但是数据状态不一定成功
    var onComplete: () -> Unit = {}

    //异常处理
    var onError: (Throwable) -> Unit = {}

    //在主线程观察
    var observableOnMain: Boolean = true
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

