package com.angcyo.pager

import androidx.fragment.app.Fragment
import com.angcyo.base.dslFHelper
import com.angcyo.base.interceptTouchEvent

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/16
 */

/**[Fragment]中, 快速启动[Pager]大图视频浏览界面*/
fun Fragment.dslPager(action: PagerTransitionCallback.() -> Unit) {
    //禁止touch事件
    activity?.interceptTouchEvent()
    dslFHelper {
        noAnim()
        show(PagerTransitionFragment().apply {
            transitionCallback = PagerTransitionCallback().apply {
                action()
            }
        })
    }
}