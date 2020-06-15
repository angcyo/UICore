package retrofit2

import android.text.TextUtils
import java.lang.reflect.InvocationTargetException

/**
 * Created by angcyo on 2016-11-26.
 */
object Reflect {
    /**
     * 从一个对象中, 获取指定的成员对象
     */
    fun getMember(target: Any?, member: String?): Any? {
        return if (target == null) {
            null
        } else getMember(target.javaClass, target, member)
    }

    fun getMember(
        cls: Class<*>,
        target: Any?,
        member: String?
    ): Any? {
        var result: Any? = null
        try {
            val memberField = cls.getDeclaredField(member!!)
            memberField.isAccessible = true
            result = memberField[target]
        } catch (e: Exception) {
            //L.i("错误:" + cls.getSimpleName() + " ->" + e.getMessage());
        }
        return result
    }

    /**
     * 只判断String类型的字段, 是否相等
     */
    fun areContentsTheSame(target1: Any?, target2: Any?): Boolean {
        if (target1 == null || target2 == null) {
            return false
        }
        if (!target1.javaClass.isAssignableFrom(target2.javaClass) ||
            !target2.javaClass.isAssignableFrom(target1.javaClass)
        ) {
            return false
        }
        var result = true
        try {
            val declaredFields1 =
                target1.javaClass.declaredFields
            val declaredFields2 =
                target2.javaClass.declaredFields
            for (i in declaredFields1.indices) {
                declaredFields1[i].isAccessible = true
                val v1 = declaredFields1[i][target1]
                declaredFields2[i].isAccessible = true
                val v2 = declaredFields2[i][target2]
                result = if (v1 is String && v2 is String) {
                    TextUtils.equals(v1, v2)
                } else if (v1 is Number && v2 is Number) {
                    v1 === v2
                } else {
                    false
                }
                if (!result) {
                    break
                }
            }
        } catch (e: Exception) {
            //L.i("错误: ->" + e.getMessage());
        }
        return result
    }

    fun setMember(
        cls: Class<*>,
        target: Any?,
        member: String?,
        value: Any?
    ) {
        try {
            val memberField = cls.getDeclaredField(member!!)
            memberField.isAccessible = true
            memberField[target] = value
        } catch (e: Exception) {
            //L.e("错误:" + e.getMessage());
        }
    }

    fun setMember(target: Any, member: String?, value: Any?) {
        setMember(target.javaClass, target, member, value)
    }

    /**
     * 获取调用堆栈上一级的方法名称
     */
    val methodName: String
        get() {
            val stackTraceElements =
                Exception().stackTrace
            return stackTraceElements[1].methodName
        }

    /**
     * 通过类对象，运行指定方法
     *
     * @param obj        类对象
     * @param methodName 方法名
     * @param params     参数值
     * @return 失败返回null
     */
    fun invokeMethod(
        obj: Any?,
        methodName: String?,
        params: Array<Any?>?
    ): Any? {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null
        }
        val clazz: Class<*> = obj.javaClass
        return invokeMethod(clazz, obj, methodName, params)
    }

    fun invokeMethod(
        obj: Any?,
        methodName: String?,
        paramTypes: Array<Class<*>?>,
        params: Array<Any?>?
    ): Any? {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null
        }
        val clazz: Class<*> = obj.javaClass
        return invokeMethod(clazz, obj, methodName, paramTypes, params)
    }

    fun invokeMethod(
        cls: Class<*>,
        obj: Any?,
        methodName: String?,
        params: Array<Any?>?
    ): Any? {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null
        }
        try {
            var paramTypes: Array<Class<*>?>? = null
            if (params != null && params.isNotEmpty()) {
                paramTypes = arrayOfNulls<Class<*>?>(params.size)
                for (i in params.indices) {
                    val pClass: Class<*> = params[i]!!.javaClass
                    if (pClass.name.contains("Integer")) {
                        paramTypes[i] = Int::class.javaPrimitiveType
                    } else if (pClass.name.contains("Long")) {
                        paramTypes[i] = Long::class.javaPrimitiveType
                    } else if (pClass.name.contains("Float")) {
                        paramTypes[i] = Float::class.javaPrimitiveType
                    } else if (pClass.name.contains("Double")) {
                        paramTypes[i] = Double::class.javaPrimitiveType
                    } else {
                        paramTypes[i] = pClass
                    }
                }
            }
            val method = cls.getDeclaredMethod(methodName!!, *paramTypes!!)
            method.isAccessible = true
            return method.invoke(obj, params)
        } catch (e: Exception) {
            //L.e("错误:" + e.getMessage());
            e.printStackTrace()
        }
        return null
    }

    fun invokeMethod(
        cls: Class<*>,
        obj: Any?,
        methodName: String?,
        paramTypes: Array<Class<*>?>,
        params: Array<Any?>?
    ): Any? {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null
        }
        try {
            val method = cls.getDeclaredMethod(methodName!!, *paramTypes)
            method.isAccessible = true
            return method.invoke(obj, params)
        } catch (e: Exception) {
            //L.e("错误:" + e.getMessage());
        }
        return null
    }

    /**
     * 通过反射, 获取obj对象的 指定成员变量的值
     */
    fun getFieldValue(obj: Any?, fieldName: String?): Any? {
        if (obj == null || TextUtils.isEmpty(fieldName)) {
            return null
        }
        var clazz: Class<*>? = obj.javaClass
        while (clazz != Any::class.java) {
            try {
                val field = clazz!!.getDeclaredField(fieldName!!)
                field.isAccessible = true
                return field[obj]
            } catch (e: Exception) {
                //L.e("错误:" + e.getMessage());
            }
            clazz = clazz!!.superclass
        }
        return null
    }

    /**
     * 设置字段的值
     */
    fun setFieldValue(
        obj: Any?,
        fieldName: String?,
        value: Any?
    ) {
        if (obj == null || TextUtils.isEmpty(fieldName)) {
            return
        }
        var clazz: Class<*>? = obj.javaClass
        while (clazz != Any::class.java) {
            try {
                val field = clazz!!.getDeclaredField(fieldName!!)
                field.isAccessible = true
                field[obj] = value
                return
            } catch (e: Exception) {
                //L.e("错误:" + e.getMessage());
            }
            clazz = clazz!!.superclass
        }
    }

    /**
     * 通过类型, 创建实例
     */
    fun <T> newObject(cls: Class<*>): T? {
        var obj: T? = null
        try {
            val constructor = cls.getDeclaredConstructor()
            constructor.isAccessible = true
            obj = constructor.newInstance() as T
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return obj
    }

    fun logException(e: Exception): String? {
        var message = e.message
        if (e is InvocationTargetException) {
            message = e.targetException.message
        }
        if (TextUtils.isEmpty(message)) {
            message = e.toString()
        }
        return message
    }
}