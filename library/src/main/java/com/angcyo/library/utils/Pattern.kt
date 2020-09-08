package com.angcyo.library.utils


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/15
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**
 * http://tool.chinaz.com/regex/
 * */

/**手机号码简单*/
const val PATTERN_MOBILE_SIMPLE = "^[1]\\d{10}$" //"^1[3-9]\\d{9}$"

/**手机号码精准*/
const val PATTERN_MOBILE_EXACT =
    "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,3,5-8])|(18[0-9])|(147))\\d{8}$"

/**座机电话, 加区号*/
const val PATTERN_TEL = "^0\\d{2,3}[- ]?\\d{7,8}$"

/**邮箱*/
const val PATTERN_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"

/**网址, 必须带协议(支持任意协议), http等*/
const val PATTERN_URL = "[a-zA-z]+://[^\\s]*"

fun patternOnlyMobile() = mutableSetOf(PATTERN_MOBILE_EXACT)
fun patternTelAndMobile() = mutableSetOf(PATTERN_MOBILE_EXACT, PATTERN_TEL)
fun patternEmail() = mutableSetOf(PATTERN_EMAIL)
fun patternUrl() = mutableSetOf(PATTERN_URL)

