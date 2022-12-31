package com.orhanobut.hawk

import com.angcyo.library.L
import com.angcyo.library.app

/**
 * 默认情况下, hawk 存储的值是加密. 此工具类可以直接解析这个加密的数据
 *
 * ```
 * <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
 * <map>
 *   <string name="test">java.lang.String##0V@AQKBm+sMjpgPV3DLAkHfHJYOOJBNdOBIRGqndhMXHHKSjhc=</string>
 *   <string name="KEY_COMPLIANCE_STATE_1">java.lang.String##0V@AQLf4+PnoAYmLEofEnsL5bXhT3D/UTk5WUYjVN17gWIAmg4P</string>
 * </map>
 * ```
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/12/31
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
object HawkValueParserHelper {

    /**解析value
     * [key] 此值只用来计算解密,不用来获取[value]
     * [value] xml文件中的值
     * */
    fun parse(key: String, value: String?): String? {
        if (value.isNullOrBlank()) {
            return null
        }

        val hawkBuilder = HawkBuilder(app())

        // 1. Get serialized text from the storage
        val serializedText: String = value

        // 2. Deserialize
        val dataInfo: DataInfo? = hawkBuilder.serializer.deserialize(serializedText)
        if (dataInfo == null) {
            L.e("Hawk.get -> Deserialization failed")
            return null
        }

        // 3. Decrypt
        var plainText: String? = null
        try {
            plainText = hawkBuilder.encryption.decrypt(key, dataInfo.cipherText)
            L.i("Hawk.get -> Decrypted to : $plainText")
        } catch (e: Exception) {
            L.e("Hawk.get -> Decrypt failed: " + e.message)
        }
        if (plainText == null) {
            L.e("Hawk.get -> Decrypt failed")
            return null
        }

        return plainText
    }


}