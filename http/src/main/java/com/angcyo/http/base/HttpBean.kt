package com.angcyo.http.base

import java.lang.reflect.Type

/**
 * 网络请求数据结构
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class HttpBean<T> {

    //200..299 表示成功
    var code: Int = -1

    var msg: String? = null
    var message: String? = null

    //数据的版本, 高版本的数据会覆盖低版本
    var version: Long = -1

    //任务集合
    var data: T? = null

    //新数据格式支持
    var statusCode: Int = -1
    var errors: String? = null
}

class PageBean<T> {
    /**数据总量*/
    var total: Long = 0

    /**当前返回的数量*/
    var size: Long = 0

    /**当前页*/
    var current: Long = 0

    /**总页数*/
    var pages: Long = 0

    /**数据集合*/
    var records: List<T>? = null
}

/**是否请求成功*/
fun HttpBean<*>.isSuccess() = this.code in 200..299 || this.statusCode in 200..299

class HttpPageBean<T> : HttpBean<PageBean<T>>()

/**
 * HttpBean<Bean>
 *
 * 基础类型转换
 * HttpBean<Boolean> beanType(Any::class.java)
 * */
fun beanType(typeClass: Class<*>): Type = type(HttpBean::class.java, typeClass)

/**
 * HttpPageBean<Bean>
 * */
fun pageBeanType(typeClass: Class<*>): Type = type(HttpPageBean::class.java, typeClass)
//type(HttpBean::class.java, type(PageBean::class.java, typeClass))

/**
 * HttpBean<List<Bean>>
 * */
fun listBeanType(typeClass: Class<*>): Type =
    type(HttpBean::class.java, type(List::class.java, typeClass))