package com.angcyo.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.annotation.LayoutRes

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/01/23
 */
open class RArrayAdapter<T> : ArrayAdapter<T> {
    var thisContext: Context
    private var thisDataList: List<T>
    var layoutInflater: LayoutInflater

    //用于在Spinner中显示的视图
    var thisResource: Int
    //用于在下拉window中显示的视图
    var thisDropDownResource: Int

    constructor(context: Context) : this(context, mutableListOf())

    constructor(context: Context, datas: List<T>) : this(
        context,
        R.layout.lib_item_single_view_layout,
        R.layout.lib_item_single_drop_down_layout,
        datas
    )

    constructor(context: Context, @LayoutRes resource: Int, @LayoutRes dropResource: Int) : this(
        context,
        resource,
        dropResource,
        mutableListOf()
    )

    constructor(
        context: Context,
        @LayoutRes resource: Int,
        @LayoutRes dropResource: Int,
        datas: List<T>
    ) : super(
        context,
        resource,
        datas
    ) {
        this.thisContext = context
        this.thisDataList = datas
        this.thisResource = resource
        this.thisDropDownResource = dropResource
        this.layoutInflater = LayoutInflater.from(context)
    }

    override fun setDropDownViewResource(resource: Int) {
        super.setDropDownViewResource(resource)
        this.thisDropDownResource = resource
    }

    /**根据[layoutId]创建视图*/
    open fun createView(convertView: View?, parent: ViewGroup, layoutId: Int): View {
        return if (convertView == null) {
            val inflate = layoutInflater.inflate(thisResource, parent, false)
            inflate.tag = DslViewHolder(inflate)
            inflate
        } else {
            convertView
        }
    }

    override fun getCount(): Int {
        return super.getCount()
    }

    override fun getViewTypeCount(): Int {
        return super.getViewTypeCount()
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    /**用于创建选中后, 在[RSpinner]中显示的视图*/
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //super.getView(position, convertView, parent)
        val itemView = createView(convertView, parent, thisResource)

        onBindItemView(itemView.tag as DslViewHolder, position, getItem(position))

        return itemView
    }

    /**用于创建, 在下拉窗口中显示的视图*/
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        //return super.getDropDownView(position, convertView, parent)

        val itemView = createView(convertView, parent, thisDropDownResource)

        onBindDropDownItemView(itemView.tag as DslViewHolder, position, getItem(position))

        return itemView
    }

    /**
     * 重写此方法, 绑定布局
     * */
    open fun onBindItemView(itemViewHolder: DslViewHolder, position: Int, itemBean: T? = null) {
        onBindDropDownItemView(itemViewHolder, position, itemBean)
    }

    /**
     * 下拉弹窗item,布局绑定
     * */
    open fun onBindDropDownItemView(
        itemViewHolder: DslViewHolder,
        position: Int,
        itemBean: T? = null
    ) {
        if (itemBean is CharSequence) {
            itemViewHolder.tv(R.id.lib_text_view)?.text = itemBean
        }
    }

    /**
     * 重置数据源
     * */
    fun resetData(datas: List<T>) {
        thisDataList = datas
        clear()
        addAll(datas)
    }

    override fun getFilter(): Filter {
        return super.getFilter()
    }
}