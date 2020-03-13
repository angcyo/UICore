package com.angcyo.widget.recycler

import android.view.View
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.angcyo.library.L
import com.angcyo.library.ex.nowTime

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/09/28
 */
class ScrollHelper {

    companion object {
        /**滚动类别: 默认不特殊处理. 滚动到item显示了就完事*/
        const val SCROLL_TYPE_NORMAL = 0

        /**滚动类别: 将item滚动到第一个位置*/
        const val SCROLL_TYPE_TOP = 1

        /**滚动类别: 将item滚动到最后一个位置*/
        const val SCROLL_TYPE_BOTTOM = 2

        /**滚动类别: 将item滚动到居中位置*/
        const val SCROLL_TYPE_CENTER = 3
    }

    internal var recyclerView: RecyclerView? = null

    fun attach(recyclerView: RecyclerView) {
        if (this.recyclerView == recyclerView) {
            return
        }
        detach()
        this.recyclerView = recyclerView
    }

    fun detach() {
        recyclerView = null
    }

    fun itemCount(): Int {
        return recyclerView?.layoutManager?.itemCount ?: 0
    }

    fun lastItemPosition(): Int {
        return itemCount() - 1
    }

    /**负数表示反序, 倒数第几个*/
    fun parsePosition(position: Int): Int {
        return if (position < 0) {
            itemCount() + position
        } else {
            position
        }
    }

    fun scrollToLast(
        scrollParams: ScrollParams = _defaultScrollParams().apply {
            scrollType = SCROLL_TYPE_BOTTOM
        }, action: ScrollParams.() -> Unit = {}
    ) {
        scrollParams.action()
        startScroll(lastItemPosition(), scrollParams)
    }

    fun _defaultScrollParams(): ScrollParams {
        return ScrollParams()
    }

    fun startScroll(scrollParams: ScrollParams = _defaultScrollParams()) {
        startScroll(scrollParams.scrollPosition, scrollParams)
    }

    fun scroll(position: Int, scrollParams: ScrollParams = _defaultScrollParams()) {
        startScroll(position, scrollParams)
    }

    fun startScroll(position: Int, scrollParams: ScrollParams = _defaultScrollParams()) {
        val targetPosition = parsePosition(position)
        if (check(targetPosition)) {
            scrollParams.scrollPosition = targetPosition

            recyclerView?.stopScroll()

            if (isPositionVisible(targetPosition)) {
                scrollWithVisible(scrollParams)
            } else {
                if (scrollParams.scrollAnim) {
                    if (scrollParams.isFromAddItem) {
                        if (recyclerView?.itemAnimator is SimpleItemAnimator) {
                            //itemAnimator 自带动画
                            recyclerView?.scrollToPosition(targetPosition)
                        } else {
                            recyclerView?.smoothScrollToPosition(targetPosition)
                        }
                    } else {
                        recyclerView?.smoothScrollToPosition(targetPosition)
                    }
                } else {
                    if (scrollParams.isFromAddItem) {
                        val itemAnimator = recyclerView?.itemAnimator
                        if (itemAnimator != null) {
                            //有默认的动画
                            recyclerView?.itemAnimator = null
                            OnNoAnimScrollIdleListener(itemAnimator).attach(recyclerView!!)
                        }
                    }
                    recyclerView?.scrollToPosition(targetPosition)
                }
                if (scrollParams.scrollType != SCROLL_TYPE_NORMAL) {
                    //不可见时, 需要现滚动到可见位置, 再进行微调
                    OnScrollIdleListener(scrollParams).attach(recyclerView!!)
                }
            }
        }
    }

    private var lockLayoutListener: LockLayoutListener? = null

    /**短时间之内, 锁定滚动到0的位置*/
    fun scrollToFirst(config: LockDrawListener.() -> Unit = {}) {
        lockPositionByDraw {
            scrollType = SCROLL_TYPE_TOP
            lockPosition = 0
            firstScrollAnim = true
            scrollAnim = true
            force = true
            firstForce = true
            lockDuration = 60
            autoDetach = true
            config()
        }
    }

    /**
     * 当界面有变化时, 自动滚动到最后一个位置
     * [unlockPosition]
     * */
    fun lockPosition(config: LockLayoutListener.() -> Unit = {}) {
        if (lockLayoutListener == null && recyclerView != null) {
            lockLayoutListener = LockLayoutListener().apply {
                scrollType = SCROLL_TYPE_CENTER
                autoDetach = true
                config()
                attach(recyclerView!!)
            }
        }
    }

    fun lockPositionByDraw(config: LockDrawListener.() -> Unit = {}) {
        recyclerView?.let {
            LockDrawListener().apply {
                //默认将目标滚动到中间位置
                scrollType = SCROLL_TYPE_CENTER
                autoDetach = true
                config()
                attach(it)
            }
        }
    }

    fun lockPositionByLayout(config: LockLayoutListener.() -> Unit = {}) {
        recyclerView?.let {
            LockLayoutListener().apply {
                scrollType = SCROLL_TYPE_CENTER
                autoDetach = true
                config()
                attach(it)
            }
        }
    }

    fun unlockPosition() {
        lockLayoutListener?.detach()
        lockLayoutListener = null
    }

    /**当需要滚动的目标位置已经在屏幕上可见*/
    internal fun scrollWithVisible(scrollParams: ScrollParams) {
        when (scrollParams.scrollType) {
            SCROLL_TYPE_NORMAL -> {
                //nothing
            }
            SCROLL_TYPE_TOP -> {
                viewByPosition(scrollParams.scrollPosition)?.also { child ->
                    recyclerView?.apply {
                        val dx = layoutManager!!.getDecoratedLeft(child) -
                                paddingLeft - scrollParams.scrollOffset

                        val dy = layoutManager!!.getDecoratedTop(child) -
                                paddingTop - scrollParams.scrollOffset

                        if (scrollParams.scrollAnim) {
                            smoothScrollBy(dx, dy)
                        } else {
                            scrollBy(dx, dy)
                        }
                    }
                }
            }
            SCROLL_TYPE_BOTTOM -> {
                viewByPosition(scrollParams.scrollPosition)?.also { child ->
                    recyclerView?.apply {
                        val dx =
                            layoutManager!!.getDecoratedRight(child) -
                                    measuredWidth + paddingRight + scrollParams.scrollOffset
                        val dy =
                            layoutManager!!.getDecoratedBottom(child) -
                                    measuredHeight + paddingBottom + scrollParams.scrollOffset

                        if (scrollParams.scrollAnim) {
                            smoothScrollBy(dx, dy)
                        } else {
                            scrollBy(dx, dy)
                        }
                    }
                }
            }
            SCROLL_TYPE_CENTER -> {
                viewByPosition(scrollParams.scrollPosition)?.also { child ->

                    recyclerView?.apply {
                        val recyclerCenterX =
                            (measuredWidth - paddingLeft - paddingRight) / 2 + paddingLeft

                        val recyclerCenterY =
                            (measuredHeight - paddingTop - paddingBottom) / 2 + paddingTop

                        val dx = layoutManager!!.getDecoratedLeft(child) - recyclerCenterX +
                                layoutManager!!.getDecoratedMeasuredWidth(child) / 2 + scrollParams.scrollOffset

                        val dy = layoutManager!!.getDecoratedTop(child) - recyclerCenterY +
                                layoutManager!!.getDecoratedMeasuredHeight(child) / 2 + scrollParams.scrollOffset

                        if (scrollParams.scrollAnim) {
                            smoothScrollBy(dx, dy)
                        } else {
                            scrollBy(dx, dy)
                        }
                    }
                }
            }
        }
    }

    /**位置是否可见*/
    private fun isPositionVisible(position: Int): Boolean {
        return recyclerView?.layoutManager.isPositionVisible(position)
    }

    private fun viewByPosition(position: Int): View? {
        return recyclerView?.layoutManager?.findViewByPosition(position)
    }

    private fun check(position: Int): Boolean {
        if (recyclerView == null) {
            L.e("请先调用[attach]方法.")
            return false
        }

        if (recyclerView?.adapter == null) {
            L.w("忽略, [adapter] is null")
            return false
        }

        if (recyclerView?.layoutManager == null) {
            L.w("忽略, [layoutManager] is null")
            return false
        }

        val itemCount = itemCount()
        val p = parsePosition(position)
        if (p < 0 || p >= itemCount) {
            L.w("忽略, [position] 需要在 [0,$itemCount) 之间.")
            return false
        }

        return true
    }

    fun log(recyclerView: RecyclerView? = this.recyclerView) {
        recyclerView?.viewTreeObserver?.apply {
            this.addOnDrawListener {
                L.i("onDraw")
            }
            this.addOnGlobalFocusChangeListener { oldFocus, newFocus ->
                L.i("on...$oldFocus ->$newFocus")
            }
            this.addOnGlobalLayoutListener {
                L.w("this....")
            }
            //此方法回调很频繁
            this.addOnPreDrawListener {
                //L.v("this....")
                true
            }
            this.addOnScrollChangedListener {
                L.i("this....${recyclerView.scrollState}")
            }
            this.addOnTouchModeChangeListener {
                L.i("this....")
            }
            this.addOnWindowFocusChangeListener {
                L.i("this....")
            }
        }
    }

    private inner abstract class OnScrollListener : ViewTreeObserver.OnScrollChangedListener,
        IAttachListener {
        var attachView: View? = null

        override fun attach(view: View) {
            detach()
            attachView = view
            view.viewTreeObserver.addOnScrollChangedListener(this)
        }

        override fun detach() {
            attachView?.viewTreeObserver?.removeOnScrollChangedListener(this)
        }

        override fun onScrollChanged() {
            onScrollChanged(recyclerView?.scrollState ?: RecyclerView.SCROLL_STATE_IDLE)
            detach()
        }

        abstract fun onScrollChanged(state: Int)
    }

    /**滚动结束之后, 根据类别, 继续滚动.*/
    private inner class OnScrollIdleListener(val scrollParams: ScrollParams) :
        OnScrollListener() {

        override fun onScrollChanged(state: Int) {
            if (state == RecyclerView.SCROLL_STATE_IDLE) {
                scrollWithVisible(scrollParams)
            }
        }
    }

    /**临时去掉动画滚动, 之后恢复动画*/
    private inner class OnNoAnimScrollIdleListener(val itemAnimator: RecyclerView.ItemAnimator?) :
        OnScrollListener() {

        override fun onScrollChanged(state: Int) {
            if (state == RecyclerView.SCROLL_STATE_IDLE) {
                recyclerView?.itemAnimator = itemAnimator
            }
        }
    }

    inner abstract class LockScrollListener : ViewTreeObserver.OnGlobalLayoutListener,
        ViewTreeObserver.OnDrawListener,
        IAttachListener, Runnable {

        /**激活滚动动画*/
        var scrollAnim: Boolean = true
            set(value) {
                field = value
                if (!value) {
                    firstScrollAnim = false
                }
            }

        /**激活第一个滚动的动画*/
        var firstScrollAnim: Boolean = true

        /**不检查界面 情况, 强制滚动到最后的位置. 关闭后. 会智能判断*/
        var force: Boolean = false

        /**第一次时, 是否强制滚动. 先触发一次滚动, 之后再微调至目标*/
        var firstForce: Boolean = true

        /**滚动阈值, 倒数第几个可见时, 就允许滚动*/
        var scrollThreshold = 2

        /**锁定需要滚动的position, 负数表示倒数第几个*/
        var lockPosition = RecyclerView.NO_POSITION

        var scrollType = SCROLL_TYPE_NORMAL
        var scrollOffset = 0
        var isFromAddItem = true

        /**是否激活功能*/
        var enableLock = true

        /**滚动到目标后, 自动调用[detach]*/
        var autoDetach = false

        /**锁定时长, 毫秒*/
        var lockDuration: Long = -1

        //记录开始的统计时间
        var _lockStartTime = 0L

        override fun run() {

            val itemCount = itemCount()
            if (!enableLock || itemCount <= 0) {
                return
            }

            val isScrollAnim = if (firstForce) firstScrollAnim && scrollAnim else scrollAnim

            val position = if (lockPosition < 0) {
                itemCount + lockPosition
            } else {
                lockPosition
            }

            val scrollParams =
                ScrollParams(position, scrollType, isScrollAnim, scrollOffset, isFromAddItem)

            if (force || firstForce) {
                scroll(position, scrollParams)
                onScrollTrigger()
                L.i("锁定滚动至->$position $force $firstForce")
            } else {
                val lastItemPosition = lastItemPosition()
                if (lastItemPosition != RecyclerView.NO_POSITION) {
                    //智能判断是否可以锁定
                    if (position == 0) {
                        //滚动到顶部
                        val findFirstVisibleItemPosition =
                            recyclerView?.layoutManager.findFirstVisibleItemPosition()

                        if (findFirstVisibleItemPosition <= scrollThreshold) {
                            scroll(position, scrollParams)
                            onScrollTrigger()
                            L.i("锁定滚动至->$position")
                        }
                    } else {
                        val findLastVisibleItemPosition =
                            recyclerView?.layoutManager.findLastVisibleItemPosition()

                        if (lastItemPosition - findLastVisibleItemPosition <= scrollThreshold) {
                            //最后第一个或者最后第2个可见, 智能判断为可以滚动到尾部
                            scroll(position, scrollParams)
                            onScrollTrigger()
                            L.i("锁定滚动至->$position")
                        }
                    }
                }
            }

            firstForce = false
        }

        var attachView: View? = null

        override fun attach(view: View) {
            detach()
            attachView = view
        }

        override fun detach() {
            attachView?.removeCallbacks(this)
        }

        /**[ViewTreeObserver.OnDrawListener]*/
        override fun onDraw() {
            initLockStartTime()
            onLockScroll()
        }

        /**[ViewTreeObserver.OnGlobalLayoutListener]*/
        override fun onGlobalLayout() {
            initLockStartTime()
            onLockScroll()
        }

        open fun initLockStartTime() {
            if (_lockStartTime <= 0) {
                _lockStartTime = nowTime()
            }
        }

        open fun isLockTimeout(): Boolean {
            return if (lockDuration > 0) {
                val nowTime = nowTime()
                nowTime - _lockStartTime > lockDuration
            } else {
                false
            }
        }

        open fun onLockScroll() {
            attachView?.removeCallbacks(this)
            if (enableLock) {
                if (isLockTimeout()) {
                    //锁定超时, 放弃操作
                    if (autoDetach) {
                        detach()
                    } else {
                        L.w("锁定已超时, 跳过操作.")
                    }
                } else {
                    attachView?.post(this)
                }
            }
        }

        open fun onScrollTrigger() {
            if (autoDetach) {
                if (isLockTimeout() || lockDuration == -1L) {
                    detach()
                }
            }
        }
    }

    /**锁定滚动到最后一个位置*/
    inner class LockLayoutListener : LockScrollListener() {

        override fun attach(view: View) {
            super.attach(view)
            view.viewTreeObserver.addOnGlobalLayoutListener(this)
        }

        override fun detach() {
            super.detach()
            attachView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
        }
    }

    /**滚动到0*/
    inner class LockDrawListener : LockScrollListener() {

        override fun attach(view: View) {
            super.attach(view)
            view.viewTreeObserver.addOnDrawListener(this)
        }

        override fun detach() {
            super.detach()
            attachView?.viewTreeObserver?.removeOnDrawListener(this)
        }
    }

    private interface IAttachListener {
        fun attach(view: View)

        fun detach()
    }
}

//滚动参数
data class ScrollParams(
    /**滚动目标, 负数反向取值*/
    var scrollPosition: Int = RecyclerView.NO_POSITION,
    /**滚动类型, [可见就行] [贴顶显示] [贴底显示] [居中显示]*/
    var scrollType: Int = ScrollHelper.SCROLL_TYPE_NORMAL,
    /**是否需要动画*/
    var scrollAnim: Boolean = true,
    /**滚动到当前位置时, 额外的偏移*/
    var scrollOffset: Int = 0,
    /**是否由AddItem导致的偏移*/
    var isFromAddItem: Boolean = true
)

fun RecyclerView?.findFirstVisibleItemPosition(): Int {
    return this?.layoutManager.findFirstVisibleItemPosition()
}

fun RecyclerView.LayoutManager?.findFirstVisibleItemPosition(): Int {
    var result = RecyclerView.NO_POSITION
    this?.also { layoutManager ->
        var firstItemPosition: Int = -1
        if (layoutManager is LinearLayoutManager) {
            firstItemPosition = layoutManager.findFirstVisibleItemPosition()
        } else if (layoutManager is StaggeredGridLayoutManager) {
            firstItemPosition =
                layoutManager.findFirstVisibleItemPositions(null).firstOrNull() ?: -1
        }
        result = firstItemPosition
    }
    return result
}

fun RecyclerView?.findLastVisibleItemPosition(): Int {
    return this?.layoutManager.findLastVisibleItemPosition()
}

fun RecyclerView.LayoutManager?.findLastVisibleItemPosition(): Int {
    var result = RecyclerView.NO_POSITION
    this?.also { layoutManager ->
        var lastItemPosition: Int = -1
        if (layoutManager is LinearLayoutManager) {
            lastItemPosition = layoutManager.findLastVisibleItemPosition()
        } else if (layoutManager is StaggeredGridLayoutManager) {
            lastItemPosition =
                layoutManager.findLastVisibleItemPositions(null).lastOrNull() ?: -1
        }
        result = lastItemPosition
    }
    return result
}

fun RecyclerView?.isPositionVisible(position: Int): Boolean {
    return this?.layoutManager.isPositionVisible(position)
}

fun RecyclerView.LayoutManager?.isPositionVisible(position: Int): Boolean {
    return position >= 0 && position in findFirstVisibleItemPosition()..findLastVisibleItemPosition()
}