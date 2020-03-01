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
    var dataList: MutableList<T> = mutableListOf()
    var layoutInflater: LayoutInflater

    //用于在Spinner中显示的视图
    var thisResource: Int
    //用于在下拉window中显示的视图
    var thisDropDownResource: Int

    /**数据类型转换*/
    var onCharSequenceConvert: (data: T?) -> CharSequence? =
        { if (it is CharSequence) it else it?.toString() }

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
        this.dataList.clear()
        this.dataList.addAll(datas)
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
            val inflate = layoutInflater.inflate(layoutId, parent, false)
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
        itemViewHolder.tv(R.id.lib_text_view)?.text = onCharSequenceConvert(itemBean)
    }

    override fun getFilter(): Filter {
        return super.getFilter()
    }

    /** 重置数据源 */
    fun resetData(datas: List<T>) {
        dataList.clear()
        dataList.addAll(datas)
        clear()
        addAll(datas)
    }

    fun remove(position: Int) {
        remove(dataList.getOrNull(position))
    }

    override fun remove(obj: T?) {
        dataList.remove(obj)
        super.remove(obj)
    }
}