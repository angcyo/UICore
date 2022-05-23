package com.angcyo.http.base

import com.hwangjr.rxbus.RxBus

/**
 *
 * https://github.com/AndroidKnife/RxBus/tree/2.x
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

/**
 * ```
 * //默认线程
 * @Subscribe
 * public void eat(String food) {
 *   // purpose
 * }
 *
 * //后台线程
 * @Subscribe(
 *   thread = EventThread.IO,
 *   tags = {
 *     @Tag(BusAction.EAT_MORE)
 * })
 * public void eat(String food) {
 *   // purpose
 * }
 *
 * //支持返回值
 * @Produce(
 *   thread = EventThread.IO,
 *   tags = {
 *     @Tag(BusAction.EAT_MORE)
 *   }
 * )
 * public List<String> produceMoreFood() {
 *   return Arrays.asList("This is breads!");
 * }
 *
 * ```
 * */

