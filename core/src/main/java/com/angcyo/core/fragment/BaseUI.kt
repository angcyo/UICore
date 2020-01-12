package com.angcyo.core.fragment

import android.view.Gravity
import android.widget.TextView
import com.angcyo.core.R
import com.angcyo.drawable.colorFilter
import com.angcyo.drawable.dpi
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

    /**[BaseTitleFragment.initBaseView]中触发*/
    open fun onFragmentInitBaseViewAfter(fragment: BaseTitleFragment) {
        if (fragment.enableBackItem()) {
            fragment.leftControl()?.append(R.layout.lib_text_view) {
                this.find<TextView>(R.id.lib_text_view)?.apply {
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
                        fragment.activity?.onBackPressed()
                    }
                }
            }
        }
    }

}


object BaseUI {
    var fragmentUI = FragmentUI()
}