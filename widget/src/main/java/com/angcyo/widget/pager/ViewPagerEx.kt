package com.angcyo.widget.pager

import android.graphics.Rect
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/23
 */

/**获取[ViewPager]中当前主要显示的[View]*/
fun ViewPager.getPrimaryChild(): View? {
    val scrollX = scrollX
    val scrollY = scrollY

    var result: View? = null
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        val left = child.left
        val top = child.top
        val right = child.right
        val bottom = child.bottom

        val rect = Rect()
        child.getLocalVisibleRect(rect)

        if (left >= scrollX &&
            right <= scrollX + measuredWidth &&
            top >= scrollY &&
            bottom <= scrollY + measuredHeight
        ) {
            result = child
            break
        }
    }
    return result
}

fun ViewPager.getPrimaryViewHolder(): DslViewHolder? {
    val primaryChild = getPrimaryChild()
    if (primaryChild != null) {
        if (primaryChild.tag is DslViewHolder) {
            return primaryChild.tag as DslViewHolder
        }
        return DslViewHolder(primaryChild)
    }
    return null
}