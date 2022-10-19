package com.angcyo.library.component

import android.animation.LayoutTransition
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.core.view.forEach
import com.angcyo.library.R
import com.angcyo.library.ex.*

/**状态提示布局管理
 * [group] 推荐使用 [LinearLayout]
 * @author <a href="LinearLayout:angcyo@126.com">angcyo</a>
 * @since 2022/07/21
 */
class StateLayoutManager {

    /**容器*/
    var group: ViewGroup? = null

    /**状态提示布局*/
    var stateLayoutId: Int = R.layout.lib_state_tip_layout

    init {
        //激活布局动画
        group?.layoutTransition = LayoutTransition()
    }

    /**添加一个状态提示*/
    fun addState(info: StateLayoutInfo) {
        val rootView = group?.inflate(stateLayoutId)
        rootView?.tag = info

        val imageView = rootView.find<ImageView>(R.id.lib_state_image_view)
        updateStateLayout(rootView, info)

        group?.doOnPreDraw {
            if (info.clipAnim) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rootView?.cancelAnimator()
                    rootView?.clipBoundsAnimatorFromLeft()
                }
            }
            if (info.rotateAnim) {
                //imageView?.rotateYAnimator()
                imageView?.rotateYAnimation()
            }
        }
    }

    /**更新一个状态提示, 如果状态不存在, 则会add*/
    fun updateState(info: StateLayoutInfo) {
        val viewList = findViewByState(info)
        if (viewList.isEmpty()) {
            addState(info)
        } else {
            viewList.forEach {
                updateStateLayout(it, info)
                if (info.updateAnim) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        it.cancelAnimator()
                        it.clipBoundsAnimatorFromLeft()
                    }
                }
            }
        }
    }

    /**更新控件信息*/
    fun updateStateLayout(rootView: View?, info: StateLayoutInfo) {
        val imageView = rootView.find<ImageView>(R.id.lib_state_image_view)
        imageView?.setImageResource(info.ico)
        rootView.find<TextView>(R.id.lib_state_text_view)?.text = info.text
    }

    /**移除一个状态提示*/
    fun removeState(info: StateLayoutInfo) {
        val viewList = findViewByState(info)
        viewList.forEach { rootView ->
            rootView.tag = null //清除状态
            val imageView = rootView.find<ImageView>(R.id.lib_state_image_view)
            imageView?.cancelAnimator()
            rootView.cancelAnimator()

            //动画结束后, 移除view
            if (info.clipAnim && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rootView.clipBoundsAnimatorFromRightHide {
                    group?.removeView(rootView)
                }
            } else {
                group?.removeView(rootView)
            }
        }
    }

    fun findViewByState(info: StateLayoutInfo): List<View> {
        val viewList = mutableListOf<View>()
        group?.forEach {
            if (it.tag == info) {
                viewList.add(it)
            }
        }
        return viewList
    }
}

data class StateLayoutInfo(
    /**状态文本*/
    var text: CharSequence? = null,
    /**状态图标*/
    var ico: Int = R.drawable.lib_state_tip_ico,
    /**激活添加时的clip动画*/
    var clipAnim: Boolean = true,
    /**激活更新时的clip动画*/
    var updateAnim: Boolean = false,
    /**图标旋转动画*/
    var rotateAnim: Boolean = true,
    /**uuid*/
    val uuid: String = uuid()
)