package com.angcyo.widget.pager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.collection.ArrayMap
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.angcyo.widget.DslViewHolder

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

abstract class RPagerAdapter : PagerAdapter(), ViewPager.OnPageChangeListener {

    //简单的缓存
    val itemCache: ArrayMap<Int, DslViewHolder> = ArrayMap()

    //<editor-fold desc="基本方法">

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemViewType = getItemViewType(position)
        var viewHolder = itemCache[itemViewType]

        //创建布局
        if (viewHolder == null) {
            //无缓存
            viewHolder = onCreateViewHolder(container, itemViewType)
        }

        //缓存无效
        itemCache[itemViewType] = null

        //添加到界面
        if (viewHolder.itemView.parent == null) {
            if (viewHolder.itemView.layoutParams == null) {
                container.addView(viewHolder.itemView, -1, -1)
            } else {
                container.addView(viewHolder.itemView)
            }
        }

        //position
        (viewHolder.itemView.layoutParams as? DslViewPager.LayoutParams)?.adapterPosition = position

        //绑定布局
        onBindViewHolder(viewHolder, position)

        return viewHolder
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        val viewHolder = item as DslViewHolder
        //移除布局
        container.removeView(viewHolder.itemView)
        itemCache[getItemViewType(position)] = viewHolder
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        super.setPrimaryItem(container, position, item)
    }

    override fun startUpdate(container: ViewGroup) {
        super.startUpdate(container)
    }

    override fun finishUpdate(container: ViewGroup) {
        super.finishUpdate(container)
    }

    override fun isViewFromObject(view: View, item: Any): Boolean {
        return view == (item as DslViewHolder).itemView
    }

    override fun getItemPosition(item: Any): Int {
        return ((item as? DslViewHolder)?.itemView
            ?.layoutParams as? DslViewPager.LayoutParams)?.run { adapterPosition }
            ?: super.getItemPosition(item)
    }

    override fun getPageWidth(position: Int): Float {
        return super.getPageWidth(position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return super.getPageTitle(position)
    }

    //</editor-fold desc="基本方法">

    //<editor-fold desc="界面绑定">

    /**获取类型*/
    open fun getItemViewType(position: Int): Int {
        return 0
    }

    /**获取item数量*/
    abstract override fun getCount(): Int

    /**获取布局*/
    abstract fun getItemLayoutId(viewType: Int): Int

    /**创建[DslViewHolder]*/
    open fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DslViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(getItemLayoutId(viewType), parent, false)
        val viewHolder = DslViewHolder(itemView)
        itemView.tag = viewHolder
        return viewHolder
    }

    /**界面绑定*/
    open fun onBindViewHolder(holder: DslViewHolder, position: Int) {

    }

    //</editor-fold desc="界面绑定">

    //<editor-fold desc="OnPageChangeListener">

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
    }

    //</editor-fold desc="OnPageChangeListener">
}