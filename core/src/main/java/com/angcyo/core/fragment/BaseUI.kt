package com.angcyo.core.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.angcyo.base.back
import com.angcyo.base.dslFHelper
import com.angcyo.base.isInDetailContainer
import com.angcyo.core.R
import com.angcyo.core.component.model.NightModel
import com.angcyo.core.vmApp
import com.angcyo.library.ex._color
import com.angcyo.library.ex._string
import com.angcyo.library.ex.colorDrawable
import com.angcyo.library.ex.colorFilter
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.find
import com.angcyo.library.ex.getDimen
import com.angcyo.library.ex.loadDrawable
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.span.span

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 */

object BaseUI {
    var fragmentUI = FragmentUI()

    var onFragmentShow: ((fragment: BaseFragment) -> Unit)? = null

    var onFragmentHide: ((fragment: BaseFragment) -> Unit)? = null
}

open class FragmentUI {

    //<editor-fold desc="成员区">

    /**[BaseTitleFragment.onCreate]中触发*/
    var fragmentCreateBefore: (fragment: BaseTitleFragment, fragmentConfig: FragmentConfig, savedInstanceState: Bundle?) -> Unit =
        { fragment, fragmentConfig, savedInstanceState ->
            onFragmentCreateBefore(fragment, fragmentConfig, savedInstanceState)
        }

    var fragmentCreateAfter: (fragment: BaseTitleFragment, fragmentConfig: FragmentConfig, savedInstanceState: Bundle?) -> Unit =
        { fragment, fragmentConfig, savedInstanceState ->
            onFragmentCreateAfter(fragment, fragmentConfig, savedInstanceState)
        }

    /**[BaseTitleFragment.onCreateView]中触发*/
    var fragmentCreateViewAfter: (fragment: BaseTitleFragment) -> Unit = {
        onFragmentCreateViewAfter(it)
    }

    /**创建返回按钮
     * [com.angcyo.core.fragment.BaseTitleFragment.onCreateBackItem]*/
    var fragmentCreateBackItem: (fragment: BaseTitleFragment) -> View? = {
        onFragmentCreateBackItem(it)
    }

    //</editor-fold desc="成员区">

    //<editor-fold desc="方法区">

    open fun onFragmentCreateBefore(
        fragment: BaseTitleFragment,
        fragmentConfig: FragmentConfig,
        savedInstanceState: Bundle?
    ) {
    }

    open fun onFragmentCreateAfter(
        fragment: BaseTitleFragment,
        fragmentConfig: FragmentConfig,
        savedInstanceState: Bundle?
    ) {

    }

    open fun onFragmentCreateViewAfter(fragment: BaseTitleFragment) {

    }

    open fun onFragmentCreateBackItem(fragment: BaseTitleFragment): View? {
        return fragment.leftControl()?.inflate(R.layout.lib_text_layout, false) {
            find<TextView>(R.id.lib_text_view)?.apply {
                id = R.id.lib_title_back_view
                setTextColor(fragment.fragmentConfig.titleItemTextColor)
                text = span {
                    drawable {
                        backgroundDrawable =
                            loadDrawable(fragment.fragmentConfig.backIconDrawableId)
                                .colorFilter(fragment.fragmentConfig.titleItemIconColor)
                    }
                    if (fragment.fragmentConfig.showBackText) {
                        drawable(_string(R.string.ui_back)) {
                            textSize = fragment.fragmentConfig.backTextSize
                            marginLeft = -8 * dpi
                            marginTop = 1 * dpi
                            textGravity = Gravity.CENTER
                        }
                    }
                }
                clickIt {
                    if (fragment.isInDetailContainer()) {
                        fragment.dslFHelper {
                            configDetailContainer()
                            remove(fragment)
                        }
                    } else {
                        fragment.back()
                    }
                }
            }
        }
    }

    //</editor-fold desc="方法区">
}

/**白底黑字样式
 * [com.angcyo.fragment.AbsFragment.onCreate]前调用*/
fun FragmentConfig.lightStyle() {
    titleBarBackgroundDrawable = colorDrawable(R.color.lib_light_title_bar_bg)
    titleTextColor = _color(R.color.lib_light_title_text_color)
    titleTextType = Typeface.BOLD
    if (vmApp<NightModel>().isDarkMode) {
        //深色模式
        titleItemIconColor = _color(R.color.lib_theme_icon_color)
    } else {
        titleItemIconColor = _color(R.color.lib_light_title_icon_color)
        isLightStyle = true //白色底标题栏
    }
    titleItemTextColor = titleItemIconColor
}

/**大标题布局*/
fun BaseTitleFragment.bigTitleLayout() {
    titleLayoutId = R.layout.lib_title_big_layout
    fragmentConfig.showBackText = false
    fragmentConfig.backIconDrawableId = R.drawable.lib_close
    fragmentConfig.titleTextSize = getDimen(R.dimen.text_primary_size).toFloat()
    //fragmentConfig.titleTextType = Typeface.BOLD
}