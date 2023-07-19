package com.orhanobut.hawk

import com.angcyo.library.L
import com.angcyo.library.app
import com.angcyo.library.component.xml.Xml
import com.angcyo.library.ex.file
import com.angcyo.library.ex.hawkGetBoolean
import com.angcyo.library.ex.hawkGetFloat
import com.angcyo.library.ex.hawkGetInt
import com.angcyo.library.ex.hawkGetLong
import com.angcyo.library.ex.hawkGetString
import org.xmlpull.v1.XmlPullParser

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
 *
 * [com.orhanobut.hawk.DefaultHawkFacade.get]
 * [com.orhanobut.hawk.DefaultHawkFacade.put]
 *
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

    /**从xml文件中解析Hawk的值到map对象*/
    fun parseFromXml(path: String): Map<String, Any?> {
        val result = hashMapOf<String, Any?>()
        try {
            Xml.read(path.file()) { parser, eventType ->
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.name == "string") {
                        val key = parser.getAttributeValue(null, "name")
                        if (!key.isNullOrBlank() && parser.next() == XmlPullParser.TEXT) {
                            //java.lang.Boolean##0V@AQIxTnYfHMZbbzJbLajahKPAOUbrJ+SvxXTbg76poB4WCzs=
                            //java.lang.Integer##0V@AQJUqsN55fFdKhwUfdEmgeLXEQc8kA9yjxxT6gbEn2s=
                            //java.lang.String##0V@AQLMzVfd/j8Bi/FgLSp5ue54sJLrtUNl0ww5QTTN2df+PrQB
                            //java.lang.Float
                            //java.lang.Long
                            val text = parser.text
                            if (text.startsWith("java.lang.Boolean")) {
                                result[key] = key.hawkGetBoolean()
                            } else if (text.startsWith("java.lang.Integer")) {
                                result[key] = key.hawkGetInt()
                            } else if (text.startsWith("java.lang.String")) {
                                result[key] = key.hawkGetString()
                            } else if (text.startsWith("java.lang.Float")) {
                                result[key] = key.hawkGetFloat()
                            } else if (text.startsWith("java.lang.Long")) {
                                result[key] = key.hawkGetLong()
                            } else {
                                result[key] = text
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

}