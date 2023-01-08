package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.withRotation
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.core.ICanvasListener
import com.angcyo.canvas.core.IRenderer
import com.angcyo.canvas.core.OffsetItemData
import com.angcyo.canvas.core.RenderParams
import com.angcyo.canvas.core.component.ControlPoint
import com.angcyo.canvas.core.component.control.ScaleControlPoint
import com.angcyo.canvas.data.CanvasProjectItemBean
import com.angcyo.canvas.data.RendererBounds
import com.angcyo.canvas.items.SelectGroupItem
import com.angcyo.canvas.items.data.DataItem
import com.angcyo.canvas.items.data.DataItemRenderer
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.getAllDependRendererList
import com.angcyo.canvas.utils.isJustGroupRenderer
import com.angcyo.drawable.*
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

/**
 * 一组一组的渲染器, 由多个子的渲染器组成
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/01/06
 */
open class GroupRenderer(canvasView: CanvasDelegate) :
    BaseItemRenderer<SelectGroupItem>(canvasView), ICanvasListener {

    companion object {

        /**更新列表中的groupId*/
        fun updateListGroupId(newList: List<BaseItemRenderer<*>>, groupId: String? = null) {
            newList.forEach {
                if (it is DataItemRenderer) {
                    it.dataItem?.dataBean?.groupId = groupId
                }
            }
        }

        /**恢复列表中的groupId*/
        fun restoreListGroupId(newList: List<BaseItemRenderer<*>>) {
            newList.forEach { group ->
                if (group is GroupRenderer) {
                    group.subItemList.forEach { sub ->
                        if (sub is DataItemRenderer) {
                            sub.dataItem?.dataBean?.groupId = group.groupId
                        }
                    }
                }
            }
        }
    }

    /**分组的id*/
    var groupId: String = uuid()

    /**真实渲染的子渲染项集合*/
    val subItemList = mutableSetOf<BaseItemRenderer<*>>()

    init {
        canvasDelegate.addCanvasListener(this)
    }

    override fun getDrawRotate(): Float = 0f

    /**渲染*/
    override fun render(canvas: Canvas, renderParams: RenderParams) {
        subItemList.forEach { renderer ->
            if (renderer.isVisible(renderParams)) {
                //item的旋转, 在此处理
                val bounds = renderer.getRenderBounds()
                canvas.withRotation(
                    renderer.getDrawRotate(),
                    bounds.centerX(),
                    bounds.centerY()
                ) {
                    renderer.render(canvas, renderParams)
                }
            }
        }
    }

    /**预览*/
    override fun preview(renderParams: RenderParams): Drawable? {
        val bounds = getRotateBounds()
        return canvasDelegate.getBitmap(bounds, renderList = subItemList.toList())
            ?.toDrawable(canvasDelegate.view.resources)
    }

    override fun isSupportControlPoint(type: Int): Boolean {
        /*return when (type) {
            ControlPoint.POINT_TYPE_DELETE -> true
            ControlPoint.POINT_TYPE_SCALE -> true
            else -> false
        }*/
        //return super.isSupportControlPoint(type)
        if (!isDebug() && type == ControlPoint.POINT_TYPE_LOCK) {
            //正式包:不支持任意比例缩放
            return false
        }
        return super.isSupportControlPoint(type)
    }

    override fun renderItemRotateChanged(oldRotate: Float, newRotate: Float, rotateFlag: Int) {
        super.renderItemRotateChanged(oldRotate, newRotate, rotateFlag)
        val degrees = rotate - oldRotate
        val bounds = getBounds()
        canvasDelegate.itemsOperateHandler.rotateItemList(
            subItemList.getAllDependRendererList(),
            degrees,
            bounds.centerX(),
            bounds.centerY(),
            Reason(Reason.REASON_CODE, flag = Reason.REASON_FLAG_ROTATE)
        )
    }

    override fun getDependRendererList(): List<BaseItemRenderer<*>> {
        val result = mutableListOf<BaseItemRenderer<*>>()
        for (item in subItemList) {
            result.addAll(item.getDependRendererList())
        }
        return result
    }

    override fun copyItemRendererData(strategy: Strategy): List<CanvasProjectItemBean>? {
        val result = mutableListOf<CanvasProjectItemBean>()
        val groupId = uuid()
        for (sub in getDependRendererList()) {
            sub.copyItemRendererData(strategy)?.let { list ->
                list.forEach { it.groupId = groupId }
                result.addAll(list)
            }
        }
        return result
    }

    //---

    override fun onCanvasBoxMatrixUpdate(
        canvasView: CanvasDelegate,
        matrix: Matrix,
        oldMatrix: Matrix,
        isEnd: Boolean
    ) {
        //super.onCanvasBoxMatrixUpdate(canvasView, matrix, oldMatrix, isEnd)
        if (isJustGroupRenderer()) {
            for (renderer in getAllDependRendererList()) {
                if (renderer != this) {
                    //转发事件
                    renderer.onCanvasBoxMatrixUpdate(canvasView, matrix, oldMatrix, isEnd)
                }
            }
        }
        updateGroupBounds(false)
    }

    override fun onRenderItemBoundsChanged(
        itemRenderer: IRenderer,
        reason: Reason,
        oldBounds: RectF
    ) {
        if (subItemList.contains(itemRenderer)) {
            if (reason.reason == Reason.REASON_USER) {
                updateGroupBounds()
            }
        }
    }

    //---

    /**按下时, 最开始的Bounds*/
    val groupTouchDownBounds = acquireTempRectF()

    /**按下时, 选中元素开始的Bounds*/
    val groupTouchDownItemBoundsList = mutableListOf<RendererBounds>()

    override fun onScaleControlStart(controlPoint: ScaleControlPoint) {
        super.onScaleControlStart(controlPoint)
        //记录开始时所有item的bounds
        groupTouchDownBounds.set(getBounds())
        groupTouchDownItemBoundsList.clear()
        subItemList.getAllDependRendererList().forEach {
            groupTouchDownItemBoundsList.add(RendererBounds(it))
        }
    }

    override fun onScaleControlFinish(controlPoint: ScaleControlPoint, rect: RectF, end: Boolean) {
        super.onScaleControlFinish(controlPoint, rect, end)
        if (end) {
            for (render in subItemList) {
                if (render is DataItemRenderer) {
                    val renderItem = render.rendererItem
                    val reason = Reason(Reason.REASON_USER, false, Reason.REASON_FLAG_BOUNDS)
                    if (renderItem is DataItem && renderItem.needUpdateOfBoundsChanged(reason)) {
                        renderItem.updateRenderItem(render, reason)
                    }
                }
            }
            groupTouchDownItemBoundsList.clear()
        }
    }

    override fun renderItemBoundsChanged(reason: Reason, oldBounds: RectF) {
        super.renderItemBoundsChanged(reason, oldBounds)
        if (subItemList.isEmpty()) {
            return
        }
        val renderers = subItemList.getAllDependRendererList()
        if (reason.reason == Reason.REASON_USER) {
            if (reason.flag.have(Reason.REASON_FLAG_TRANSLATE) || reason.flag.have(Reason.REASON_FLAG_BOUNDS)) {
                if (groupTouchDownItemBoundsList.isEmpty()) {
                    //如果不是手势调整的Bounds
                    canvasDelegate.itemsOperateHandler.changeBoundsItemList(
                        renderers,
                        oldBounds,
                        getBounds(),
                        getBoundsScaleAnchor(),
                        rotate,
                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_BOUNDS)
                    )
                } else {
                    //如果是手势调整的Bounds
                    val itemOriginBoundsList = mutableListOf<RectF>()
                    for (render in renderers) {
                        groupTouchDownItemBoundsList.find { it.renderer == render }?.let {
                            itemOriginBoundsList.add(it.bounds)
                        }
                    }
                    canvasDelegate.itemsOperateHandler.changeBoundsItemList(
                        renderers,
                        itemOriginBoundsList,
                        groupTouchDownBounds,
                        getBounds(),
                        getBoundsScaleAnchor(),
                        rotate,
                        Reason(Reason.REASON_CODE, false, Reason.REASON_FLAG_BOUNDS)
                    )
                }
            }
        }
    }

    override fun onRendererVisibleChanged(from: Boolean, to: Boolean, strategy: Strategy) {
        super.onRendererVisibleChanged(from, to, strategy)
        getDependRendererList().forEach { item ->
            item.setVisible(to, Strategy.preview)
        }
    }

    //---

    /**更新组的可见性, 组内有一个不可见, 则不可见*/
    fun updateGroupProperty(list: List<BaseItemRenderer<*>>) {
        //可见性
        var isVisible = true
        for (renderer in list) {
            if (!renderer.isVisible()) {
                isVisible = false
                break
            }
        }
        setVisible(isVisible, Strategy.preview)
    }

    /**更新选中的bounds大小, 需要包含所有选中的元素*/
    fun updateGroupBounds(resetRotate: Boolean = true) {
        if (subItemList.isEmpty()) {
            return
        }
        if (resetRotate) {
            rotate = 0f//重置旋转
        }
        changeBoundsAction(Reason(Reason.REASON_CODE, true)) {
            getSubItemBounds(this)
        }
    }

    /**重置[subItemList]
     * [groupId] 如果指定了则会覆盖[GroupRenderer.groupId]
     * */
    fun resetAllSubList(newList: List<BaseItemRenderer<*>>, groupId: String? = null) {
        subItemList.resetAll(newList)
        if (groupId != null) {
            this.groupId = groupId
        }
        updateGroupProperty(newList)
        updateListGroupId(newList, this.groupId)
        updateGroupBounds(true)
    }

    /**获取所有子元素的外边框bounds*/
    fun getSubItemBounds(result: RectF = RectF()): RectF {
        var l: Float? = null
        var t: Float? = null
        var r: Float? = null
        var b: Float? = null
        subItemList.forEach { renderer ->
            val rotateBounds = renderer.getRotateBounds().adjustFlipRect(acquireTempRectF())
            l = min(l ?: rotateBounds.left, rotateBounds.left)
            t = min(t ?: rotateBounds.top, rotateBounds.top)
            r = max(r ?: rotateBounds.right, rotateBounds.right)
            b = max(b ?: rotateBounds.bottom, rotateBounds.bottom)
            rotateBounds.release()
        }
        result.set(l ?: 0f, t ?: 0f, r ?: 0f, b ?: 0f)
        return result
    }

    /**获取所有依赖的渲染器, 拆组后的所有渲染器
     * [com.angcyo.canvas.CanvasDelegate.getAllDependRendererList]*/
    fun getAllDependRendererList(): List<BaseItemRenderer<*>> =
        subItemList.getAllDependRendererList()

    //---

    /**更新选中子项的对齐方式
     * [align] [Gravity.LEFT]*/
    fun updateAlign(align: Int = Gravity.LEFT, strategy: Strategy = Strategy.normal) {
        val list = subItemList
        if (list.size() <= 1) {
            return
        }

        //寻找定位锚点item
        var anchorItemRenderer: BaseItemRenderer<*>? = null
        if (align.isGravityCenter()) {
            //找出距离中心点最近的Item
            val centerX = getBounds().centerX()
            val centerY = getBounds().centerY()

            //2点之间的最小距离
            var minR = Float.MAX_VALUE

            list.forEach {
                val bounds = it.getRotateBounds()
                val r = c(centerX, centerY, bounds.centerX(), bounds.centerY()).absoluteValue
                if (r < minR) {
                    anchorItemRenderer = it
                    minR = r
                }
            }
        } else if (align.isGravityCenterHorizontal()) {
            //水平居中, 找出最大的高度item
            var maxHeight = Float.MIN_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.height() > maxHeight) {
                    anchorItemRenderer = it
                    maxHeight = bounds.height()
                }
            }
        } else if (align.isGravityCenterVertical()) {
            //垂直居中, 找出最大的宽度item
            var maxWidth = Float.MIN_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.width() > maxWidth) {
                    anchorItemRenderer = it
                    maxWidth = bounds.width()
                }
            }
        } else if (align.isGravityTop()) {
            var minTop = Float.MAX_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.top < minTop) {
                    anchorItemRenderer = it
                    minTop = bounds.top
                }
            }
        } else if (align.isGravityBottom()) {
            var maxBottom = Float.MIN_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.bottom > maxBottom) {
                    anchorItemRenderer = it
                    maxBottom = bounds.bottom
                }
            }
        } else if (align.isGravityLeft()) {
            var minLeft = Float.MAX_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.left < minLeft) {
                    anchorItemRenderer = it
                    minLeft = bounds.left
                }
            }
        } else if (align.isGravityRight()) {
            var maxRight = Float.MIN_VALUE
            list.forEach {
                val bounds = it.getRotateBounds()
                if (bounds.right > maxRight) {
                    anchorItemRenderer = it
                    maxRight = bounds.right
                }
            }
        }

        if (anchorItemRenderer == null) {
            return
        }

        val offsetList = mutableListOf<OffsetItemData>()

        val anchorBounds = anchorItemRenderer!!.getRotateBounds()
        for (item in list) {
            if (item != anchorItemRenderer) {
                val itemBounds = item.getRotateBounds()
                //开始调整
                var dx = 0f
                var dy = 0f

                if (align.isGravityCenter()) {
                    dx = anchorBounds.centerX() - itemBounds.centerX()
                    dy = anchorBounds.centerY() - itemBounds.centerY()
                } else if (align.isGravityCenterHorizontal()) {
                    dy = anchorBounds.centerY() - itemBounds.centerY()
                } else if (align.isGravityCenterVertical()) {
                    dx = anchorBounds.centerX() - itemBounds.centerX()
                } else if (align.isGravityTop()) {
                    dy = anchorBounds.top - itemBounds.top
                } else if (align.isGravityBottom()) {
                    dy = anchorBounds.bottom - itemBounds.bottom
                } else if (align.isGravityLeft()) {
                    dx = anchorBounds.left - itemBounds.left
                } else if (align.isGravityRight()) {
                    dx = anchorBounds.right - itemBounds.right
                }
                offsetList.add(OffsetItemData(item, dx, dy))
            }
        }
        canvasDelegate.itemsOperateHandler.offsetItemList(
            canvasDelegate,
            this,
            offsetList,
            strategy
        )
    }

    /**水平分布/垂直分布*/
    fun updateFlat(flat: Int, strategy: Strategy = Strategy.normal) {
        val list = subItemList
        val count = list.size()
        if (count <= 2) {
            return
        }

        val sortList = subItemList.toMutableList()
        //先排序
        if (flat == LinearLayout.VERTICAL) {
            sortList.sortBy { it.getBounds().centerY() }
        } else if (flat == LinearLayout.HORIZONTAL) {
            sortList.sortBy { it.getBounds().centerX() }
        } else {
            return
        }

        val first = sortList.first()
        val last = sortList.last()

        val firstX = first.getBounds().centerX()
        val firstY = first.getBounds().centerY()
        //步长
        val step = when (flat) {
            LinearLayout.HORIZONTAL -> last.getBounds().centerX() - firstX
            LinearLayout.VERTICAL -> last.getBounds().centerY() - firstY
            else -> 0f
        } / (count - 1)

        val offsetList = mutableListOf<OffsetItemData>()
        sortList.forEachIndexed { index, renderer ->
            if (renderer != first && renderer != last) {
                when (flat) {
                    LinearLayout.VERTICAL -> {
                        val dy = firstY + (step * index) - renderer.getBounds().centerY()
                        offsetList.add(OffsetItemData(renderer, 0f, dy))
                    }
                    LinearLayout.HORIZONTAL -> {
                        val dx = firstX + (step * index) - renderer.getBounds().centerX()
                        offsetList.add(OffsetItemData(renderer, dx, 0f))
                    }
                }
            }
        }

        if (offsetList.isNotEmpty()) {
            canvasDelegate.itemsOperateHandler.offsetItemList(
                canvasDelegate,
                this,
                offsetList,
                strategy
            )
        }
    }

    //---
}