package com.angcyo.http.gitee

import com.angcyo.http.base.getBoolean
import com.angcyo.http.base.getLong
import com.angcyo.http.base.getString
import com.angcyo.http.exception.HttpResponseException
import com.angcyo.http.interceptor.LogInterceptor
import com.angcyo.http.rx.doBack
import com.angcyo.http.rx.observer
import com.angcyo.library.ex.*
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

    /**从gitee获取数据*/
    fun get(
        json: String,
        end: (data: Response<JsonElement>?, error: Throwable?) -> Unit
    ): Disposable {
        val url = if (json.isHttpScheme()) {
            json
        } else {
            "$BASE/${json}"
        }
        return com.angcyo.http.get {
            this.url = url
            query = hashMapOf("time" to nowTime()) //带上时间参数, 避免缓存
            header = hashMapOf(
                LogInterceptor.closeLog(false),
                "Host" to (url.toUri()?.host ?: "gitee.com"),
                "Referer" to (url.toUri()?.host ?: "gitee.com"),
                "Cache-Control" to "no-cache",
                "Cookie" to "gitee-session-n=MEl6NWFQd3RVMjhuL1pjWEJzWWE3MGpXMXFCcXMvRjdEWHl4ZXdQSVhVbFpQdTJtdVdWMVloYmYxWVQ4ZTkvVWFrRnl4WTdtazBpNTZpZkllUnFhTHc9PS0tMUdwM2JSM1R0VnRMNzhmamR6c081dz09--555bc1476152411c5402088a42058307da19dc8e; oschina_new_user=false",
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.64"
            )
            isSuccessful = {
                it.isSuccessful
            }
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

    /**检查验证隐逸数据*/
    fun checkConceal(json: String, block: (notify: String?) -> Unit) {
        get(json) { data, error ->
            data?.body()?.let {
                if (it is JsonObject) {
                    val msg = it.getString("msg", "kill down!")
                    val enable = it.getBoolean("enable", true)
                    val lastTime = it.getLong("lastTime", Long.MAX_VALUE)
                    if (enable && lastTime > nowTime()) {
                        //通过
                        block(null)
                    } else {
                        //不通过
                        block(msg)
                        doBack {
                            sleep(1_000)
                            killCurrentProcess()
                        }
                    }
                }
            }
        }
    }
}
