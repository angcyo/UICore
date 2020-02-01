package com.angcyo.core.fragment

import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.angcyo.base.back
import com.angcyo.core.R
import com.angcyo.library.ex.colorFilter
import com.angcyo.library.ex.dpi
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.find
import com.angcyo.widget.base.getDrawable
import com.angcyo.widget.span.span

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 */


open class FragmentUI {

    /**[BaseTitleFragment.onCreate]中触发*/
    open fun onFragmentCreateAfter(fragment: BaseTitleFragment, fragmentConfig: FragmentConfig) {

    }

    /**[BaseTitleFragment.onCreateView]中触发*/
    open fun onFragmentCreateViewAfter(fragment: BaseTitleFragment) {

    }

    /**创建返回按钮*/
    open fun onCreateFragmentBackItem(fragment: BaseTitleFragment): View? {
        return fragment.leftControl()?.inflate(R.layout.lib_text_view, false) {
            find<TextView>(R.id.lib_text_view)?.apply {
                setTextColor(fragment.fragmentConfig.titleItemTextColor)
                text = span {
                    drawable {
                        backgroundDrawable =
                            getDrawable(R.drawable.lib_back).colorFilter(fragment.fragmentConfig.titleItemIconColor)
                    }
                    drawable("返回") {
                        marginLeft = -8 * dpi
                        marginTop = 1 * dpi
                        textGravity = Gravity.CENTER
                    }
                }
                clickIt {
                    fragment.back()
                }
            }
        }
    }
}


object BaseUI {
    var fragmentUI = FragmentUI()
}