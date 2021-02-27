package com.angcyo.http.rsa

import android.util.Base64

/**
 * Base64
 * Author:Bobby
 * DateTime:2019/4/9
 */
object Base64Util {
    /**
     * Decoding to binary
     * @param base64 base64
     * @return byte
     * @throws Exception Exception
     */
    @Throws(Exception::class)
    fun decode(base64: String): ByteArray {
        return Base64.decode(base64, Base64.NO_WRAP)
    }

    /**
     * Binary encoding as a string
     * @param bytes byte
     * @return String
     * @throws Exception Exception
     */
    @Throws(Exception::class)
    fun encode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}