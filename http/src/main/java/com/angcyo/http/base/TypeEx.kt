package com.angcyo.http.base

import ikidou.reflect.TypeBuilder
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * https://github.com/ikidou/TypeBuilder/
 *
 * 针对[TypeBuilder]的一些扩展
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**String*/
fun bean(rootClass: Class<*>, typeClass: Class<*>? = null): Type {
    return TypeBuilder.newInstance(rootClass).apply {
        if (typeClass != null) {
            addTypeParam(typeClass)
        }
    }.build()
}

fun bean(rootClass: KClass<*>, typeClass: KClass<*>? = null): Type =
    bean(rootClass.java, typeClass?.java)

/**List<String>*/
fun type(rootClass: Class<*>, typeClass: Class<*>): Type {
    return bean(rootClass, typeClass)
}

fun type(rootClass: KClass<*>, typeClass: KClass<*>): Type = type(rootClass.java, typeClass.java)

fun listType(typeClass: Class<*>): Type = type(List::class.java, typeClass)

fun listType(typeClass: KClass<*>): Type = listType(typeClass.java)

/**List<? super String>*/
fun typeSuper(rootClass: Class<*>, typeClass: Class<*>): Type {
    return TypeBuilder.newInstance(rootClass).addTypeParamSuper(typeClass).build()
}

/**List<? extends String>*/
fun typeExtends(rootClass: Class<*>, typeClass: Class<*>): Type {
    return TypeBuilder.newInstance(rootClass).addTypeParamExtends(typeClass).build()
}

/**List<List<String>>*/
fun type(rootClass: Class<*>, type: Type): Type {
    return TypeBuilder.newInstance(rootClass).addTypeParam(type).build()
}

/**Map<String, String[]*/
fun type(rootClass: Class<*>, typeClass1: Class<*>, typeClass2: Class<*>): Type {
    return TypeBuilder.newInstance(rootClass)
        .addTypeParam(typeClass1)
        .addTypeParam(typeClass2)
        .build()
}

fun mapType(key: Class<*>, value: Class<*>) = type(Map::class.java, key, value)

/**Map<String, List<String>*/
fun type(
    rootClass1: Class<*>,
    typeClass1: Class<*>,
    rootClass2: Class<*>,
    typeClass2: Class<*>
): Type {
    return TypeBuilder.newInstance(rootClass1)
        .addTypeParam(typeClass1)
        .beginSubType(rootClass2)
        .addTypeParam(typeClass2)
        .endSubType()
        .build()
}

/**Map<String, List<String>*/
fun type(
    rootClass: Class<*>,
    typeClass1: Class<*>,
    type2: Type
): Type {
    return TypeBuilder.newInstance(rootClass)
        .addTypeParam(typeClass1)
        .addTypeParam(type2)
        .build()
}





