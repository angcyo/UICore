package com.angcyo.dsladapter

import android.text.TextUtils
import androidx.recyclerview.widget.DiffUtil

open class RDiffCallback<T : Any> : DiffUtil.Callback {
    var oldDatas: List<T>? = null
    var newDatas: List<T>? = null
    var mDiffCallback: RDiffCallback<T>? = null

    constructor() {}
    constructor(
        oldDatas: List<T>?,
        newDatas: List<T>?,
        diffCallback: RDiffCallback<T>?
    ) {
        this.oldDatas = oldDatas
        this.newDatas = newDatas
        mDiffCallback = diffCallback
    }

    override fun getOldListSize(): Int {
        return getListSize(oldDatas)
    }

    override fun getNewListSize(): Int {
        return getListSize(newDatas)
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return mDiffCallback!!.areItemsTheSame(
            oldDatas!![oldItemPosition],
            newDatas!![newItemPosition]
        )
    }

    /**
     * 被DiffUtil调用，用来检查 两个item是否含有相同的数据
     * 这个方法仅仅在areItemsTheSame()返回true时，才调用。
     */
    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return mDiffCallback!!.areContentsTheSame(
            oldDatas!![oldItemPosition],
            newDatas!![newItemPosition]
        )
    }

    /**
     * 重写此方法, 判断数据是否相等,
     * 如果item不相同, 会先调用 notifyItemRangeRemoved, 再调用 notifyItemRangeInserted
     */
    open fun areItemsTheSame(oldData: T, newData: T): Boolean {
        val oldClass: Class<*> = oldData.javaClass
        val newClass: Class<*> = newData.javaClass
        return if (oldClass.isAssignableFrom(newClass) || newClass.isAssignableFrom(oldClass)) {
            true
        } else TextUtils.equals(oldClass.simpleName, newClass.simpleName)
    }

    /**
     * 重写此方法, 判断内容是否相等,
     * 如果内容不相等, 会调用notifyItemRangeChanged
     */
    open fun areContentsTheSame(oldData: T, newData: T): Boolean {
        val oldClass: Class<*> = oldData.javaClass
        val newClass: Class<*> = newData.javaClass
        return if (oldClass.isAssignableFrom(newClass) ||
            newClass.isAssignableFrom(oldClass) ||
            TextUtils.equals(oldClass.simpleName, newClass.simpleName)
        ) {
            oldData == newData
        } else false
    }

    companion object {
        fun getListSize(list: List<*>?): Int {
            return list?.size ?: 0
        }
    }
}