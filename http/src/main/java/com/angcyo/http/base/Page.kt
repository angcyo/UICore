package com.angcyo.http.base

/**
 * 网络请求, 页面操作参数
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class Page {
    companion object {
        /**默认一页请求的数量*/
        var PAGE_SIZE: Int = 20

        /**默认第一页的索引*/
        var FIRST_PAGE_INDEX: Int = 1
    }

    /** 当前请求完成的页 */
    var _currentPageIndex: Int = FIRST_PAGE_INDEX

    /** 正在请求的页 */
    var requestPageIndex: Int = FIRST_PAGE_INDEX

    /** 每页请求的数量 */
    var requestPageSize: Int = PAGE_SIZE

    /**页面刷新, 重置page index*/
    fun pageRefresh() {
        _currentPageIndex = FIRST_PAGE_INDEX
        requestPageIndex = FIRST_PAGE_INDEX
    }

    /**页面加载更多*/
    fun pageLoadMore() {
        requestPageIndex = _currentPageIndex + 1
    }

    /**页面加载结束, 刷新结束/加载更多结束*/
    fun pageLoadEnd() {
        _currentPageIndex = requestPageIndex
    }
}