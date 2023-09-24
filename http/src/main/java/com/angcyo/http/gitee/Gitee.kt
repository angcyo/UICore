package com.angcyo.http.gitee

import com.angcyo.http.BaseRequestConfig
import com.angcyo.http.RequestBodyConfig
import com.angcyo.http.RequestConfig
import com.angcyo.http.base.getBoolean
import com.angcyo.http.base.getLong
import com.angcyo.http.base.getString
import com.angcyo.http.base.readString
import com.angcyo.http.exception.HttpResponseException
import com.angcyo.http.interceptor.LogInterceptor
import com.angcyo.http.rx.doBack
import com.angcyo.http.rx.observer
import com.angcyo.library.ex.isHttpScheme
import com.angcyo.library.ex.killCurrentProcess
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.sleep
import com.angcyo.library.ex.toUri
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.disposables.Disposable
import retrofit2.Response

/**
 * 获取数据
 *
 * https://gitee.com/angcyo/json
 * https://gitcode.net/angcyo/json
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/11
 */
object Gitee {

    //无/结尾
    //https://gitee.com/angcyo/json/raw/master/accauto
    //https://gitee.com/angcyo/json/raw/master/accauto/memory_config.json
    var BASE = "https://gitee.com/angcyo/json/raw/master"

    //备用地址, 当BASE访问403时, 自动将BACKUP替换之
    //https://gitcode.net/angcyo/json/-/raw/master/accauto
    //https://gitcode.net/angcyo/json/-/raw/master/accauto/memory_config.json
    var BASE_BACKUP = "https://gitcode.net/angcyo/json/-/raw/master"

    /**从gitee获取Json数据*/
    fun get(
        json: String,
        end: (data: Response<JsonElement>?, error: Throwable?) -> Unit
    ): Disposable {
        var url = json
        return com.angcyo.http.get {
            configRequest(url)
            url = this.url
        }.map {
            if (!it.isSuccessful) {
                throw IllegalArgumentException("${url}:${it.message()}")
            }
            it
        }.observer {
            onObserverEnd = { data, error ->
                if (error is HttpResponseException &&
                    (error.code == 404 || error.code == 403) &&
                    BASE != BASE_BACKUP
                ) {
                    //替换备用地址, 重新请求
                    BASE = BASE_BACKUP
                    get(json, end)
                } else {
                    end(data, error)
                }
            }
        }
    }

    /**从gitee获取字符串数据*/
    fun getString(
        api: String,
        end: (data: String?, error: Throwable?) -> Unit
    ): Disposable {
        var url = api
        return com.angcyo.http.get2Body {
            configRequest(url)
            url = this.url
        }.map {
            if (!it.isSuccessful) {
                throw IllegalArgumentException("${url}:${it.message()}")
            }
            it
        }.observer {
            onObserverEnd = { data, error ->
                if (error is HttpResponseException &&
                    (error.code == 404 || error.code == 403) &&
                    BASE != BASE_BACKUP
                ) {
                    //替换备用地址, 重新请求
                    BASE = BASE_BACKUP
                    getString(api, end)
                } else {
                    end(data?.body()?.readString(), error)
                }
            }
        }
    }

    /**初始化请求的配置*/
    private fun BaseRequestConfig.configRequest(json: String, log: Boolean = true) {
        val url = if (json.isHttpScheme()) {
            json
        } else {
            "$BASE/${json}"
        }
        this.url = url
        query = hashMapOf("time" to nowTime()) //带上时间参数, 避免缓存
        header = hashMapOf(
            LogInterceptor.closeLog(!log),
            "Host" to (url.toUri()?.host ?: "gitee.com"),
            "Referer" to (url.toUri()?.host ?: "gitee.com"),
            "Cache-Control" to "no-cache",
            "Cookie" to "gitee-session-n=MEl6NWFQd3RVMjhuL1pjWEJzWWE3MGpXMXFCcXMvRjdEWHl4ZXdQSVhVbFpQdTJtdVdWMVloYmYxWVQ4ZTkvVWFrRnl4WTdtazBpNTZpZkllUnFhTHc9PS0tMUdwM2JSM1R0VnRMNzhmamR6c081dz09--555bc1476152411c5402088a42058307da19dc8e; oschina_new_user=false",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.64"
        )
        if (this is RequestBodyConfig) {
            isSuccessful = {
                it.isSuccessful
            }
        } else if (this is RequestConfig) {
            isSuccessful = {
                it.isSuccessful
            }
        }
    }

    /**检查验证隐逸数据, 杀掉程序本身*/
    fun checkConceal(json: String, block: (notify: String?) -> Unit) {
        get(json) { data, error ->
            data?.body()?.let {
                if (it is JsonObject) {
                    val msg = it.getString("msg", "kill down!")
                    val enable = it.getBoolean("enable", true)
                    val lastTime = it.getLong("lastTime", Long.MAX_VALUE)
                    val killDelay = it.getLong("killDelay", 1_000)
                    if (enable && lastTime > nowTime()) {
                        //通过
                        block(null)
                    } else {
                        //不通过
                        if (killDelay > 0) {
                            block(msg)
                            doBack {
                                sleep(1_000)
                                killCurrentProcess()
                            }
                        } else {
                            killCurrentProcess()
                        }
                    }
                }
            }
        }
    }
}
