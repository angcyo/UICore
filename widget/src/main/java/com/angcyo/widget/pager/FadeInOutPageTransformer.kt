package com.angcyo.widget.pager

import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs

/**
 * Viewpager 页面切换动画，只支持3.0以上版本
 *
 *
 * [-∞，-1]完全不可见
 * [-1,  0]从不可见到完全可见
 * [0,1]从完全可见到不可见
 * [1,∞]完全不可见
 *
 *
 * Created by doc on 15/1/6.
 */
class FadeInOutPageTransformer : ViewPager.PageTransformer {
    /**
     * @see ViewPager.setCurrentItem
     */
    override fun transformPage(
        page: View,
        position: Float
    ) {
        if (abs(position) > 1 ||
            page.measuredWidth == 0 &&
            page.measuredHeight == 0
        ) {
            //smoothScroll为false时, 这个方法也会回调
            //但是此时, position 上一页和下一页都是负数.
            // 如果调用了page.setAlpha(0);那么界面就看不见东西了
            page.alpha = 1f
        } else if (position < -1) {
            page.alpha = 0f
        } else if (position < 0) {
            page.alpha = 1 + position
        } else if (position < 1) {
            page.alpha = 1 - position
        } else {
            page.alpha = 0f
        }
    }
}