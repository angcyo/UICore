package com.angcyo.library.utils

import android.text.TextUtils
import com.angcyo.library.L
import com.angcyo.library.L.e
import com.angcyo.library.L.i
import dalvik.system.PathClassLoader
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

/**
 *
 * getFields()与getDeclaredFields()区别:
 * getFields()只能访问类中声明为公有的字段,私有的字段它无法访问，能访问从其它类继承来的公有方法.
 * getDeclaredFields()能访问类中所有的字段,与public,private,protect无关，不能访问从其它类继承来的方法

 * getMethods()与getDeclaredMethods()区别:
 * getMethods()只能访问类中声明为公有的方法,私有的方法它无法访问,能访问从其它类继承来的公有方法.
 * getDeclaredMethods()能访问类中所有的字段,与public,private,protect无关,不能访问从其它类继承来的方法

 * getConstructors()与getDeclaredConstructors()区别:
 * getConstructors()只能访问类中声明为public的构造函数.
 * getDeclaredConstructors()能访问类中所有的构造函数,与public,private,protect无关
 *
 *
 * 反射操作相关类
 * Created by angcyo on 2016-11-26.
 */
object Reflect {

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
            i("错误: ->" + e.message)
        }
        return result
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

    /**获取指定对象[obj]的指定字段名称[fieldName]的值*/
    fun getField(obj: Any, fieldName: String): Any? {
        try {
            obj.javaClass.apply {
                val field = getDeclaredField(fieldName) ?: getField(fieldName)
                field.isAccessible = true
                return field.get(obj)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**设置指定对象[obj]的指定字段名称[fieldName]的值*/
    fun setField(obj: Any, fieldName: String, value: Any?): Any? {
        try {
            obj.javaClass.apply {
                val field = getDeclaredField(fieldName) ?: getField(fieldName)
                field.isAccessible = true
                field.set(obj, value)
                return value
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**获取指定类的静态字段, 内部类需要使用`$`符号连接
     * ```
     * "android.os.Build$VERSION", "SDK_INT"
     * ```
     * */
    fun getStaticField(className: String, fieldName: String): Any? {
        try {
            Class.forName(className).apply {
                val field = getDeclaredField(fieldName) ?: getField(fieldName)
                field.isAccessible = true
                return field.get(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun setStaticField(className: String, fieldName: String, value: Any?): Any? {
        try {
            Class.forName(className).apply {
                val field = getDeclaredField(fieldName) ?: getField(fieldName)
                field.isAccessible = true
                field.set(null, value)
                return value
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**通过反射调用指定对象的指定方法*/
    fun invokeMethod(
        obj: Any,
        methodName: String,
        vararg args: Any?
    ): Any? {
        try {
            val argTypes = args.map { it?.javaClass }.toTypedArray()
            obj.javaClass.apply {
                val method = getDeclaredMethod(methodName, *argTypes)
                    ?: getMethod(methodName, *argTypes)
                method.isAccessible = true
                return method.invoke(obj, *args)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**通过反射调用静态方法*/
    fun invokeStaticMethod(
        className: String,
        methodName: String,
        vararg args: Any?
    ): Any? {
        try {
            val argTypes = args.map { it?.javaClass }.toTypedArray()
            Class.forName(className).apply {
                val method = getDeclaredMethod(methodName, *argTypes)
                    ?: getMethod(methodName, *argTypes)
                method.isAccessible = true
                return method.invoke(null, *args)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

}

/**
 * 从一个对象中, 获取指定的成员对象
 */
fun Any?.getMember(member: String): Any? {
    return this?.run { this.getMember(this.javaClass, member) }
}

fun makeAccessible(field: Field) {
    if ((!Modifier.isPublic(field.modifiers) ||
                !Modifier.isPublic(field.declaringClass.modifiers) ||
                Modifier.isFinal(field.modifiers)) && !field.isAccessible
    ) {
        field.isAccessible = true
    }
}

fun Any?.getMember(
    cls: Class<*>,
    member: String
): Any? {
    var result: Any? = null
    try {
        var cl: Class<*>? = cls
        while (cl != null) {
            try {
                val memberField = cls.getDeclaredField(member)
                //memberField.isAccessible = true
                makeAccessible(memberField)
                result = memberField[this]
                return result
            } catch (e: NoSuchFieldException) {
                cl = cl.superclass
            }
        }
    } catch (e: Exception) {
        //L.i("错误:" + cls.getSimpleName() + " ->" + e.getMessage());
    }
    return result
}

/**设置对象的指定成员变量的值*/
fun Any?.setMember(
    cls: Class<*>,
    member: String,
    value: Any?
) {
    try {
        val memberField = cls.getDeclaredField(member)
        memberField.isAccessible = true
        memberField[this] = value
    } catch (e: Exception) {
        e("错误:" + e.message)
    }
}

fun Any?.setMember(member: String, value: Any?) {
    this?.run { this.setMember(this.javaClass, member, value) }
}


/**
 * 通过类对象，运行指定方法
 *
 * @param obj        类对象
 * @param methodName 方法名
 * @param params     参数值
 * @return 失败返回null
 */
fun Any?.invokeMethod(methodName: String, vararg params: Any): Any? {
    if (this == null || TextUtils.isEmpty(methodName)) {
        return null
    }
    val clazz: Class<*> = this.javaClass
    return this.invokeMethod(clazz, methodName, *params)
}

fun Any?.invokeMethod(methodName: String, paramTypes: Array<Class<*>>, vararg params: Any): Any? {
    if (this == null || TextUtils.isEmpty(methodName)) {
        return null
    }
    val clazz: Class<*> = this.javaClass
    return this.invokeMethod(
        clazz,
        methodName,
        paramTypes,
        *params
    )
}

fun Any?.invokeMethod(cls: Class<*>, methodName: String, vararg params: Any): Any? {
    if (this == null || TextUtils.isEmpty(methodName)) {
        return null
    }
    try {
        val paramTypes: Array<Class<*>> = Array(params.size) {
            val pClass: Class<*> = params[it].javaClass
            return when {
                pClass.name.contains("Integer") -> {
                    Int::class.javaPrimitiveType
                }

                pClass.name.contains("Long") -> {
                    Long::class.javaPrimitiveType
                }

                pClass.name.contains("Float") -> {
                    Float::class.javaPrimitiveType
                }

                pClass.name.contains("Double") -> {
                    Double::class.javaPrimitiveType
                }

                else -> {
                    pClass
                }
            }
        }
        val method = cls.getDeclaredMethod(methodName, *paramTypes)
        method.isAccessible = true
        return method.invoke(this, *params)
    } catch (e: Exception) {
        e("错误:" + e.message)
        e.printStackTrace()
    }
    return null
}

fun Any?.invokeMethod(
    cls: Class<*>,
    methodName: String,
    paramTypes: Array<Class<*>>,
    vararg params: Any
): Any? {
    if (this == null || TextUtils.isEmpty(methodName)) {
        return null
    }
    try {
        val method = cls.getDeclaredMethod(methodName, *paramTypes)
        method.isAccessible = true
        return method.invoke(this, *params)
    } catch (e: Exception) {
        e("错误:" + e.message)
    }
    return null
}

/**
 * 通过反射, 获取obj对象的 指定成员变量的值
 */
fun Any?.getFieldValue(fieldName: String): Any? {
    if (this == null || TextUtils.isEmpty(fieldName)) {
        return null
    }
    val clazz: Class<*> = this.javaClass
    return this.getFieldValue(clazz, fieldName)
}

fun Any?.getFieldValue(cls: Class<*>, fieldName: String): Any? {
    if (this == null || TextUtils.isEmpty(fieldName)) {
        return null
    }
    var clazz = cls
    while (clazz != Any::class.java) {
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            return field[this]
        } catch (e: Exception) {
            e("错误:" + e.message)
        }
        clazz = clazz.superclass!!
    }
    return null
}

/**
 * 设置字段的值
 */
fun Any?.setFieldValue(fieldName: String, value: Any?) {
    if (this == null || TextUtils.isEmpty(fieldName)) {
        return
    }
    val clazz: Class<*> = this.javaClass
    this.setFieldValue(clazz, fieldName, value)
}

fun Any?.setFieldValue(clazz: Class<*>, fieldName: String, value: Any?) {
    var cls = clazz
    if (this == null || TextUtils.isEmpty(fieldName)) {
        return
    }
    var error: Exception? = null
    while (cls != Any::class.java) {
        try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            field[this] = value
            error = null
            break
        } catch (e: Exception) {
            error = e
        }
        cls = clazz.superclass!!
    }
    if (error != null) {
        e("错误:" + error.message)
    }
}

/**将对象的成员, 放在map里面*/
fun Any.toMap(): Map<String, String> {
    val objFields = this.javaClass.declaredFields
    val map = hashMapOf<String, String>()
    for (f in objFields) {
        try {
            val name = f.name
            f.isAccessible = true
            val o = f[this]
            if (o is String || o is Number) {
                map[name] = o.toString()
            } else {
                L.d("不支持的类型[${name}]:${o?.javaClass?.name}")
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
    return map
}

fun Any.eachField(each: (field: Field, value: Any?) -> Unit) {
    val objFields = this.javaClass.declaredFields
    for (f in objFields) {
        try {
            f.isAccessible = true
            val o = f[this]
            each(f, o)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }
}

/**
 * 将一个对象的成员属性, 赋值给另一个对象, 按照key type 相同的原则匹配
 * [ignoreFiledNameList] 需要忽略的属性字段
 * [ignoreNull]  如果toValue是null, 是否需要忽略
 * [jumpValue] 如果fromValue不是null, 是否跳过
 * [appendValue] 如果目标是数组, 列表, Map, 是否追加数据
 */
fun Any?.fillTo(
    to: Any?,
    ignoreNull: Boolean = false,
    jumpValue: Boolean = false,
    appendValue: Boolean = false,
    ignoreFiledNameList: List<String>? = null
) {
    val from = this
    if (from == null || to == null || from == to) {
        return
    }
    val fromFields = from.javaClass.declaredFields
    for (f in fromFields) {
        val fName = f.name
        val t = to.javaClass.getDeclaredField(fName) ?: continue
        val tName = t.name
        if (fName.equals(tName, ignoreCase = true) &&
            ignoreFiledNameList?.contains(tName) != true
        ) {
            L.i("开始赋值属性:${fName} ${f.type.name}")
            try {
                f.isAccessible = true
                t.isAccessible = true
                val fromValue = f[from]
                val toValue = t[to]
                if (t.isTransient()) {
                    //跳过
                    L.w("跳过字段名:$tName transient")
                } else if (fromValue == toValue) {
                    //两个值一样
                    L.w("字段名:$tName fromValue == toValue")
                } else if (ignoreNull && fromValue == null) {
                    //不将null值赋值给属性
                } else {
                    if (f.type == t.type) {
                        val loader = f.type.classLoader
                        if (loader is PathClassLoader) {
                            //自定义的类型
                            L.i("开始填充属性:${fName} ${f.type.name}")
                            fromValue.fillTo(toValue, ignoreNull, jumpValue, appendValue)
                        } else if (!appendValue || toValue == null) {
                            //内部类, 基础类型
                            if (jumpValue && toValue != null) {
                                //跳过已经有值的属性
                            } else {
                                t[to] = fromValue
                            }
                        } else if (appendValue) {
                            try {
                                if (f.type.isSuperClassBy(List::class.java)) {
                                    (t[to] as MutableList<*>).addAll(fromValue as Collection<Nothing>)
                                } else if (f.type.isSuperClassBy(Map::class.java)) {
                                    (t[to] as MutableMap<*, *>).putAll(fromValue as Map<Nothing, Nothing>)
                                } else {
                                    L.w("不支持的集合类型:${f.type.name}, 使用赋值语句")
                                    if (jumpValue) {
                                        //跳过已经有值的属性
                                    } else {
                                        t[to] = fromValue
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        L.w("操作字段名:$tName 类型不匹配, From:${f.type} To:${t.type}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    L.i("填充完成:${from.javaClass.name} $to")
}

/**判断当前类, 是否是[subclass]的超类
 * Integer::class.java.isAssignableFrom(Number::class.java) false
 * Number::class.java.isAssignableFrom(Integer::class.java) true
 * */
fun Class<*>.isSuperClassBy(subclass: Class<*>) = this.isAssignableFrom(subclass)

/**判断当前类是否是[parentClass]的子类*/
fun Class<*>.isChildClassOf(parentClass: Class<*>) = parentClass.isSuperClassBy(this)

fun Field.isPublic() = Modifier.isPublic(modifiers)

fun Field.isTransient() = Modifier.isTransient(modifiers)

fun Method.isPublic() = Modifier.isPublic(modifiers)