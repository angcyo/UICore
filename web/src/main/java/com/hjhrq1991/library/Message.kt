package com.hjhrq1991.library

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * data of bridge
 * @author haoqing
 */
class Message {

    companion object {
        private const val CALLBACK_ID_STR = "callbackId"
        private const val RESPONSE_ID_STR = "responseId"
        private const val RESPONSE_DATA_STR = "responseData"
        private const val DATA_STR = "data"
        private const val HANDLER_NAME_STR = "handlerName"

        fun toObject(jsonStr: String?): Message {
            val m = Message()
            try {
                val jsonObject = JSONObject(jsonStr)
                m.handlerName = if (jsonObject.has(HANDLER_NAME_STR)) jsonObject.getString(
                    HANDLER_NAME_STR
                ) else null
                m.callbackId = if (jsonObject.has(CALLBACK_ID_STR)) jsonObject.getString(
                    CALLBACK_ID_STR
                ) else null
                m.responseData = if (jsonObject.has(RESPONSE_DATA_STR)) jsonObject.getString(
                    RESPONSE_DATA_STR
                ) else null
                m.responseId = if (jsonObject.has(RESPONSE_ID_STR)) jsonObject.getString(
                    RESPONSE_ID_STR
                ) else null
                m.data = if (jsonObject.has(DATA_STR)) jsonObject.getString(DATA_STR) else null
                return m
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return m
        }

        @JvmStatic
        fun toArrayList(jsonStr: String?): List<Message> {
            val list: MutableList<Message> = ArrayList()
            try {
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until jsonArray.length()) {
                    val m = Message()
                    val jsonObject = jsonArray.getJSONObject(i)
                    m.handlerName = if (jsonObject.has(HANDLER_NAME_STR)) jsonObject.getString(
                        HANDLER_NAME_STR
                    ) else null
                    m.callbackId = if (jsonObject.has(CALLBACK_ID_STR)) jsonObject.getString(
                        CALLBACK_ID_STR
                    ) else null
                    m.responseData = if (jsonObject.has(RESPONSE_DATA_STR)) jsonObject.getString(
                        RESPONSE_DATA_STR
                    ) else null
                    m.responseId = if (jsonObject.has(RESPONSE_ID_STR)) jsonObject.getString(
                        RESPONSE_ID_STR
                    ) else null
                    m.data = if (jsonObject.has(DATA_STR)) jsonObject.getString(DATA_STR) else null
                    list.add(m)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return list
        }
    }

    @JvmField
    var callbackId: String? = null //callbackId

    @JvmField
    var responseId: String? = null //responseId

    @JvmField
    var responseData: String? = null //responseData

    @JvmField
    var data: String? = null //data of message

    @JvmField
    var handlerName: String? = null //name of handler
    fun toJson(): String? {
        val jsonObject = JSONObject()
        try {
            jsonObject.put(CALLBACK_ID_STR, callbackId)
            jsonObject.put(DATA_STR, data)
            jsonObject.put(HANDLER_NAME_STR, handlerName)
            jsonObject.put(RESPONSE_DATA_STR, responseData)
            jsonObject.put(RESPONSE_ID_STR, responseId)
            return jsonObject.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }
}