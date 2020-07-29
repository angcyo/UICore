package com.angcyo.library.utils

import android.net.Uri
import java.net.URL
import java.net.URLDecoder
import java.util.*

/**
 * 类：UrlParse
 * 作者： qxc
 * 日期：2017/5/22.
 * https://www.jianshu.com/p/f2f4bed578b2
 */
object UrlParse {
    /**
     * 获得解析后的URL参数
     *
     * @param url url对象
     * @return URL参数map集合
     */
    fun getUrlParams(url: String?): Map<String, Any> {
        val queryMap: MutableMap<String, Any> = LinkedHashMap()
        val mUrl = stringToURL(url) ?: return queryMap
        try {
            var query = mUrl.query ?: return queryMap
            //判断是否包含url=,如果是url=后面的内容不用解析
            if (query.contains("url=")) {
                val index = query.indexOf("url=")
                val urlValue = query.substring(index + 4)
                queryMap["url"] = URLDecoder.decode(urlValue, "UTF-8")
                query = query.substring(0, index)
            }
            //除url之外的参数进行解析
            if (query.isNotEmpty()) {
                val pairs = query.split("&".toRegex()).toTypedArray()
                for (pair in pairs) {
                    val idx = pair.indexOf("=")
                    //如果等号存在且不在字符串两端，取出key、value
                    if (idx > 0 && idx < pair.length - 1) {
                        val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                        val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")

                        val longValue = value.toLongOrNull()
                        if (longValue == null) {
                            val doubleValue = value.toDoubleOrNull()
                            if (doubleValue == null) {
                                if (value == "true") {
                                    queryMap[key] = true
                                } else if (value == "false") {
                                    queryMap[key] = false
                                } else {
                                    //字符串数据
                                    queryMap[key] = value
                                }
                            } else {
                                //浮点型数据
                                queryMap[key] = doubleValue
                            }
                        } else {
                            //整型数据
                            queryMap[key] = longValue
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return queryMap
    }

    /**
     * 获得Url参数字符串
     *
     * @param url url地址
     * @return 参数字符串
     */
    fun getUrlParamStr(url: String?): String {
        val mUrl = stringToURL(url) ?: return ""
        try {
            return mUrl.query
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ""
    }

    /**
     * 获得url的协议+域+路径（即url路径问号左侧的内容）
     *
     * @param url url地址
     * @return url的协议+域+路径
     */
    fun getUrlHostAndPath(url: String): String {
        return if (url.contains("?")) {
            url.substring(0, url.indexOf("?"))
        } else url
    }

    /**
     * 获得Uri参数值
     *
     * @param uri      uri
     * @param paramKey 参数名称
     * @return 参数值
     */
    fun getUriParam(uri: Uri?, paramKey: String?): String {
        if (uri == null || paramKey == null || paramKey.isEmpty()) {
            return ""
        }
        var paramValue = uri.getQueryParameter(paramKey)
        if (paramValue == null) {
            paramValue = ""
        }
        return paramValue
    }

    /**
     * 获得Uri参数值
     *
     * @param uri      uri
     * @param paramKey 参数名称
     * @return 参数值
     */
    fun getIntUriParam(uri: Uri?, paramKey: String?): Int {
        if (uri == null || paramKey == null || paramKey.isEmpty()) {
            return 0
        }
        try {
            val paramValue = uri.getQueryParameter(paramKey)
            return if (paramValue == null || paramValue.isEmpty()) {
                0
            } else paramValue.toInt()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return 0
    }

    /**
     * 字符串转为URL对象
     *
     * @param url url字符串
     * @return url对象
     */
    private fun stringToURL(url: String?): URL? {
        return if (url == null || url.isEmpty() || !url.contains("://")) {
            null
        } else try {
            val sbUrl = StringBuilder("http")
            sbUrl.append(url.substring(url.indexOf("://")))
            URL(sbUrl.toString())
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}