package com.angcyo.library

import android.content.Context

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/10
 */
interface IActivityProvider {

    /**获取[Activity]的[Context]*/
    fun getActivityContext(): Context?
}