package com.angcyo.http.base

import android.text.TextUtils
import com.angcyo.library.L
import com.google.gson.*
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

class JsonBuilder {

    lateinit var _rootElement: JsonElement

    var _subElementStack: Stack<JsonElement> = Stack()

    /**
     * 当对象的值为null时, 是否忽略, 如果是json对象, 那么"{}"也会被忽略
     */
    var ignoreNull = true

    /**
     * 当调用 get() 或者 build() 时, 是否自动调用 endAdd().
     *
     *
     * 这种情况只适合 末尾全是 endAdd().endAdd().endAdd().endAdd()...的情况
     */
    var autoEnd = true

    /**
     * 构建一个Json对象
     */
    fun json(): JsonBuilder {
        _rootElement = JsonObject()
        return this
    }

    /**
     * 构建一个Json数组
     */
    fun array(): JsonBuilder {
        _rootElement = JsonArray()
        return this
    }

    fun ignoreNull(ignoreNull: Boolean): JsonBuilder {
        this.ignoreNull = ignoreNull
        return this
    }

    fun autoEnd(autoEnd: Boolean): JsonBuilder {
        this.autoEnd = autoEnd
        return this
    }

    private val operateElement: JsonElement?
        get() {
            checkRootElement()
            return if (_subElementStack.isEmpty()) {
                _rootElement
            } else {
                _subElementStack.lastElement()
            }
        }

    private val isArray: Boolean
        get() = isArray(operateElement)

    private val isObj: Boolean
        get() = isObj(operateElement)

    /**
     * 产生一个新的Json对象子集, 适用于 Json对象中 key 对应的 值是json
     */
    fun addJson(key: String): JsonBuilder {
        require(!isArray) { "不允许在Json数组中, 使用此方法. 请尝试使用 addJson() 方法" }
        _subElementStack.push(WrapJsonObject(operateElement!!, key))
        return this
    }

    /**
     * 产生一个新的Json对象子集, 适用于 Json 数组中 值是json
     */
    fun addJson(): JsonBuilder {
        require(!isObj) { "不允许在Json对象中, 使用此方法. 请尝试使用 addJson(String) 方法" }
        _subElementStack.push(WrapJsonObject(operateElement!!, null))
        return this
    }

    /**
     * 产生一个新的Json数组对象子集, 适用于Json中添加数组
     */
    fun addArray(key: String): JsonBuilder {
        require(!isArray) { "不允许在Json数组中, 使用此方法. 请尝试使用 addArray() 方法" }
        _subElementStack.push(WrapJsonArray(operateElement!!, key))
        return this
    }

    /**
     * 产生一个新的Json数组对象子集, 适用于数组中添加数组
     */
    fun addArray(): JsonBuilder {
        require(!isObj) { "不允许在Json对象中, 使用此方法. 请尝试使用 addArray(String) 方法" }
        _subElementStack.push(WrapJsonArray(operateElement!!, null))
        return this
    }

    fun addArray(key: String, call: JsonBuilder.() -> Unit): JsonBuilder {
        addArray(key)
        call(call)
        return this
    }

    fun addArray(call: JsonBuilder.() -> Unit): JsonBuilder {
        addArray()
        call(call)
        return this
    }

    /**
     * 结束新对象, 有多少个add操作, 就需要有多少个end操作
     */
    fun endAdd(): JsonBuilder {
        if (!_subElementStack.isEmpty()) {
            val pop = _subElementStack.pop()
            if (pop is WrapJsonElement) {
                val origin = pop.originElement
                val parent = pop.parentElement
                val key = pop.key
                if (isArray(parent)) {
                    operateElement(
                        parent,
                        origin,
                        ignoreNull
                    )
                } else {
                    operateElement(
                        parent,
                        key!!,
                        origin,
                        ignoreNull
                    )
                }
            }
        } else {
            L.w(TAG, "不合法的操作, 请检查!")
        }
        return this
    }

    fun add(key: String, bool: Boolean?): JsonBuilder {
        operateElement(
            operateElement,
            key,
            bool,
            ignoreNull
        )
        return this
    }

    fun add(key: String, character: Char?): JsonBuilder {
        operateElement(
            operateElement,
            key,
            character,
            ignoreNull
        )
        return this
    }

    fun add(key: String, number: Number?): JsonBuilder {
        operateElement(
            operateElement,
            key,
            number,
            ignoreNull
        )
        return this
    }

    fun add(key: String, string: String?): JsonBuilder {
        operateElement(
            operateElement,
            key,
            string,
            ignoreNull
        )
        return this
    }

    fun add(
        key: String,
        element: JsonElement?
    ): JsonBuilder {
        operateElement(
            operateElement,
            key,
            element,
            ignoreNull
        )
        return this
    }

    fun add(bool: Boolean?): JsonBuilder {
        operateElement(operateElement, bool, ignoreNull)
        return this
    }

    fun add(character: Char?): JsonBuilder {
        operateElement(
            operateElement,
            character,
            ignoreNull
        )
        return this
    }

    fun add(number: Number?): JsonBuilder {
        operateElement(
            operateElement,
            number,
            ignoreNull
        )
        return this
    }

    fun add(string: String?): JsonBuilder {
        operateElement(
            operateElement,
            string,
            ignoreNull
        )
        return this
    }

    fun add(element: JsonElement?): JsonBuilder {
        operateElement(
            operateElement,
            element,
            ignoreNull
        )
        return this
    }

    /**
     * 回调出去
     */
    fun call(action: JsonBuilder.() -> Unit): JsonBuilder {
        action(this)
        return this
    }

    /**
     * 结束所有
     */
    fun endAll(): JsonBuilder {
        while (!_subElementStack.isEmpty()) {
            endAdd()
        }
        return this
    }

    private fun checkEnd() {
        if (autoEnd) {
            endAll()
        }
    }

    fun build(): JsonElement {
        checkEnd()
        return _rootElement
    }

    fun get(): String {
        checkEnd()
        return _rootElement.toString()
    }

    private fun checkRootElement() {
//        if (_rootElement == null) {
//            throw NullPointerException("你需要先调用 json() or array() 方法.")
//        }
    }

    private open class WrapJsonElement(
        var parentElement: JsonElement,
        var originElement: JsonElement,
        var key: String?
    ) : JsonElement() {
        override fun deepCopy(): JsonElement {
            return originElement.deepCopy()
        }

        override fun isJsonArray(): Boolean {
            return originElement.isJsonArray
        }

        override fun isJsonObject(): Boolean {
            return originElement.isJsonObject
        }

        override fun isJsonPrimitive(): Boolean {
            return originElement.isJsonPrimitive
        }

        override fun isJsonNull(): Boolean {
            return originElement.isJsonNull
        }

        override fun getAsJsonObject(): JsonObject {
            return originElement.asJsonObject
        }

        override fun getAsJsonArray(): JsonArray {
            return originElement.asJsonArray
        }

        override fun getAsJsonPrimitive(): JsonPrimitive {
            return originElement.asJsonPrimitive
        }

        override fun getAsJsonNull(): JsonNull {
            return originElement.asJsonNull
        }

        override fun getAsBoolean(): Boolean {
            return originElement.asBoolean
        }

        override fun getAsNumber(): Number {
            return originElement.asNumber
        }

        override fun getAsString(): String {
            return originElement.asString
        }

        override fun getAsDouble(): Double {
            return originElement.asDouble
        }

        override fun getAsFloat(): Float {
            return originElement.asFloat
        }

        override fun getAsLong(): Long {
            return originElement.asLong
        }

        override fun getAsInt(): Int {
            return originElement.asInt
        }

        override fun getAsByte(): Byte {
            return originElement.asByte
        }

        override fun getAsCharacter(): Char {
            return originElement.asCharacter
        }

        override fun getAsBigDecimal(): BigDecimal {
            return originElement.asBigDecimal
        }

        override fun getAsBigInteger(): BigInteger {
            return originElement.asBigInteger
        }

        override fun getAsShort(): Short {
            return originElement.asShort
        }

        override fun toString(): String {
            return originElement.toString()
        }

    }

    private class WrapJsonArray(
        parentElement: JsonElement,
        key: String?
    ) : WrapJsonElement(parentElement, JsonArray(), key) {

        fun add(value: JsonElement?) {
            (originElement as JsonArray).add(value)
        }

        fun addProperty(value: String?) {
            (originElement as JsonArray).add(value)
        }

        fun addProperty(value: Number?) {
            (originElement as JsonArray).add(value)
        }

        fun addProperty(value: Boolean?) {
            (originElement as JsonArray).add(value)
        }

        fun addProperty(value: Char?) {
            (originElement as JsonArray).add(value)
        }
    }

    private class WrapJsonObject(
        parentElement: JsonElement,
        key: String?
    ) : WrapJsonElement(parentElement, JsonObject(), key) {
        fun add(property: String?, value: JsonElement?) {
            (originElement as JsonObject).add(property, value)
        }

        fun addProperty(property: String?, value: String?) {
            (originElement as JsonObject).addProperty(property, value)
        }

        fun addProperty(property: String?, value: Number?) {
            (originElement as JsonObject).addProperty(property, value)
        }

        fun addProperty(property: String?, value: Boolean?) {
            (originElement as JsonObject).addProperty(property, value)
        }

        fun addProperty(property: String?, value: Char?) {
            (originElement as JsonObject).addProperty(property, value)
        }
    }

    companion object {
        const val TAG = "JsonBuilder"
        /**
         * 操作[JsonObject]
         */
        private fun operateElement(
            element: JsonElement?,
            key: String,
            obj: Any?,
            ignoreNull: Boolean
        ) {
            if (element is JsonObject) {
                if (obj == null) {
                    if (!ignoreNull) {
                        element.add(key, null)
                    }
                } else if (obj is String) {
                    element.addProperty(key, obj as String?)
                } else if (obj is Number) {
                    element.addProperty(key, obj as Number?)
                } else if (obj is Char) {
                    element.addProperty(key, obj as Char?)
                } else if (obj is Boolean) {
                    element.addProperty(key, obj as Boolean?)
                } else if (obj is JsonElement) {
                    if (ignoreNull) {
                        if (!TextUtils.equals(obj.toString(), "{}")) {
                            element.add(
                                key,
                                obj as JsonElement?
                            )
                        }
                    } else {
                        element.add(
                            key,
                            obj as JsonElement?
                        )
                    }
                } else {
                    element.addProperty(key, obj.toString())
                }
            } else if (element is WrapJsonObject) {
                operateElement(
                    element.originElement,
                    key,
                    obj,
                    ignoreNull
                )
            } else {
                L.w(
                    TAG,
                    " 当前操作已被忽略:$key->$obj"
                )
            }
        }

        /**
         * 操作[JsonArray]
         */
        private fun operateElement(
            element: JsonElement?,
            obj: Any?,
            ignoreNull: Boolean
        ) {
            if (element is JsonArray) {
                if (obj == null) {
                    if (!ignoreNull) {
                        element.add(null as String?)
                    }
                } else if (obj is String) {
                    element.add(obj as String?)
                } else if (obj is Number) {
                    element.add(obj as Number?)
                } else if (obj is Char) {
                    element.add(obj as Char?)
                } else if (obj is Boolean) {
                    element.add(obj as Boolean?)
                } else if (obj is JsonElement) {
                    if (ignoreNull) {
                        if (!TextUtils.equals(obj.toString(), "{}")) {
                            element.add(obj as JsonElement?)
                        }
                    } else {
                        element.add(obj as JsonElement?)
                    }
                } else {
                    element.add(obj.toString())
                }
            } else if (element is WrapJsonArray) {
                operateElement(
                    element.originElement,
                    obj,
                    ignoreNull
                )
            } else {
                L.w(TAG, " 当前操作已被忽略:$obj")
            }
        }

        private fun isArray(element: JsonElement?): Boolean {
            return element is JsonArray ||
                    element is WrapJsonArray
        }

        private fun isObj(element: JsonElement?): Boolean {
            return element is JsonObject ||
                    element is WrapJsonObject
        }
    }
}