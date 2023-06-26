package com.angcyo.core.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.angcyo.base.back
import com.angcyo.base.dslFHelper
import com.angcyo.base.isInDetailContainer
import com.angcyo.core.R
import com.angcyo.core.component.model.NightModel
import com.angcyo.core.vmApp
import com.angcyo.library.ex.*
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

    /**是否显示返回按钮的文本*/
    var showBackText: Boolean = true

    /**返回按钮的[Drawable]资源*/
    @DrawableRes
    var backIconDrawableId: Int = R.drawable.lib_back
    var backTextSize: Float = _dimen(R.dimen.text_body_size).toFloat()

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
                            loadDrawable(backIconDrawableId).colorFilter(fragment.fragmentConfig.titleItemIconColor)
                    }
                    if (showBackText) {
                        drawable(_string(R.string.ui_back)) {
                            textSize = backTextSize
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