package com.angcyo.http.rsa

import com.angcyo.library.ex.getSafe

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/08
 */
object SecurityCodeUtil {

    /**加密, 将[time]和[securityCode]相加, 隐藏到[contentMd5]中, 并返回
     * [contentMd5] 数据内容的md5值
     * [securityCode] 安全码 不超过13位
     * [time] 13位毫秒时间*/
    fun encode(contentMd5: String, securityCode: String, time: Long): String {
        if (securityCode.isEmpty() || time <= 0) {
            throw IllegalArgumentException("数据参数异常")
        }
        if (contentMd5.length == 32) {
            val result = StringBuilder()
            val timeStr = time.toString()
            if (timeStr.length != 13) {
                throw IllegalArgumentException("时间格式不正确,必须是13位")
            }
            contentMd5.forEachIndexed { index, char ->
                result.append(char)
                if (index < timeStr.length) {
                    val c = securityCode.getSafe(index)!!
                    val t = timeStr.getSafe(index)!!

                    result.append(c.toUpperCase())
                    result.append(t.toUpperCase())
                }
            }
            return result.toString()
        } else {
            throw IllegalArgumentException("内容格式不正确,必须是32位")
        }
    }

    /**解密, 将加密字符串[encodeStr]解密出,内容的md5字符串,安全码和时间数据
     * [encodeStr] 加密后的字符串*/
    fun decode(encodeStr: String): List<String> {
        val contentBuild = StringBuilder()
        val codeBuild = StringBuilder()
        val timeBuild = StringBuilder()

        var index = 0
        while (index < encodeStr.length) {
            contentBuild.append(encodeStr.getSafe(index))
            if (timeBuild.length < 13) {
                index += 1
                codeBuild.append(encodeStr.getSafe(index))
                index += 1
                timeBuild.append(encodeStr.getSafe(index))
            }
            index += 1
        }

        return listOf(contentBuild.toString(), codeBuild.toString(), timeBuild.toString())
    }

}