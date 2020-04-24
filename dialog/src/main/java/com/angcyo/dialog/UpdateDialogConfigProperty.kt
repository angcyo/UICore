package com.angcyo.dialog

import com.angcyo.widget.DslViewHolder
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020-4-24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class UpdateDialogConfigProperty<T>(var value: T) : ReadWriteProperty<DslDialogConfig, T> {
    override fun getValue(thisRef: DslDialogConfig, property: KProperty<*>): T = value

    override fun setValue(thisRef: DslDialogConfig, property: KProperty<*>, value: T) {
        this.value = value
        thisRef._dialog?.apply {
            window?.decorView?.tag?.let {
                if (it is DslViewHolder) {
                    thisRef.initDialogView(this, it)
                }
            }
        }
    }
}