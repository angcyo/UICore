package com.angcyo.http.base

import com.hwangjr.rxbus.RxBus

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/26
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**RxBus*/
public fun Any.busRegister() {
    RxBus.get().register(this)
}

/**RxBus*/
public fun Any.busUnRegister() {
    RxBus.get().unregister(this)
}

/**RxBus*/
public fun Any.busPost() {
    RxBus.get().post(this)
}