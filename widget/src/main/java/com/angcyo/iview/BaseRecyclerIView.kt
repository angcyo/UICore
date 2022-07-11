package com.angcyo.iview

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import com.angcyo.dsladapter.DslAdapter
import com.angcyo.widget.R
import com.angcyo.widget.recycler.noItemChangeAnim
import com.angcyo.widget.recycler.renderDslAdapter

/**
 * [DslRecyclerView]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/06
 */
abstract class BaseRecyclerIView : IView() {

    /**标题*/
    var iViewTitle: CharSequence? = null
        set(value) {
            field = value
            updateTitle(value)
        }

    init {
        iViewLayoutId = R.layout.lib_recycler_iview_layout
    }

    override fun onIViewCreate() {
        super.onIViewCreate()
        if (iViewTitle == null) {
            iViewTitle = viewHolder?.tv(R.id.lib_title_view)?.text
        }
        updateTitle(iViewTitle)

        //init
        viewHolder?.throttleClick(R.id.lib_close_view) {
            hide()
        }
    }

    open fun updateTitle(title: CharSequence?) {
        viewHolder?.tv(R.id.lib_title_view)?.text = title
    }

    /**显示关闭按钮*/
    @MainThread
    open fun showCloseView(show: Boolean = true) {
        cancelable = show
        viewHolder?.visible(R.id.lib_close_view, show)
    }

    /**DslAdapter*/
    @AnyThread
    open fun renderDslAdapter(
        append: Boolean = false, //当已经是adapter时, 是否追加item. 需要先关闭 new
        new: Boolean = false, //始终创建新的adapter, 为true时, 则append无效
        updateState: Boolean = true,
        action: DslAdapter.() -> Unit = {}
    ) {
        viewHolder?.rv(R.id.lib_recycler_view)?.apply {
            noItemChangeAnim()
            this.renderDslAdapter(append, new, updateState, action)
        }
    }

}