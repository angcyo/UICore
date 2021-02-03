package com.angcyo.http

import com.angcyo.http.base.bean
import com.angcyo.http.base.getString
import com.angcyo.library.component.toTime
import com.angcyo.library.ex.nowTime
import com.google.gson.JsonObject

/**
 * 获取北京时间
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object SystemTime {

    /**最后一次获取的北京时间*/
    var lastSystemTime = nowTime()

    /**获取北京时间
     * https://www.jianshu.com/p/4a68e6c261ae
     * 13位时间戳
     * */
    fun get(end: (Long) -> Unit) {
        _getByTaobao {
            if (it > 0 && it.toString().length == 13) {
                lastSystemTime = it
                end(lastSystemTime)
            } else {
                _getBySuning {
                    if (it > 0 && it.toString().length == 13) {
                        lastSystemTime = it
                        end(lastSystemTime)
                    } else {
                        lastSystemTime = nowTime()
                        end(lastSystemTime)
                    }
                }
            }
        }
    }

    fun _getByTaobao(end: (Long) -> Unit) {
        request {
            url = "http://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp"
            onEndAction = { response, exception ->
                var time = -1L
                try {
                    time = response?.toBean<JsonObject>(bean(JsonObject::class.java))
                        ?.getAsJsonObject("data")?.getString("t")?.toLongOrNull() ?: -1
                } catch (e: Exception) {
                }
                end(time)
            }
        }
    }

    fun _getBySuning(end: (Long) -> Unit) {
        request {
            url = "http://quan.suning.com/getSysTime.do"
            onEndAction = { response, exception ->
                var time = -1L
                try {
                    time = response?.toBean<JsonObject>(bean(JsonObject::class.java))
                        ?.getString("sysTime1")?.toTime("yyyyMMddHHmmss") ?: -1
                } catch (e: Exception) {
                }
                end(time)
            }
        }
    }
}