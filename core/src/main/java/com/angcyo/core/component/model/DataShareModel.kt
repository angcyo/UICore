package com.angcyo.core.component.model

import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import com.angcyo.library.ex.TouchAction
import com.angcyo.viewmodel.updateValue
import com.angcyo.viewmodel.vmData
import com.angcyo.viewmodel.vmDataOnce
import java.io.File

/**
 * App内数据共享模型
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/04/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class DataShareModel : ViewModel() {

    //region ---共享数据---

    /**共享数据通知, [Any]类型*/
    val shareOnceData = vmDataOnce<Any?>()

    /**共享文本通知, 通常用于文本内容*/
    val shareTextOnceData = vmDataOnce<CharSequence?>()

    /**共享消息通知, 通常用于通知*/
    val shareMessageOnceData = vmDataOnce<CharSequence?>()

    /**共享服务地址通知, http://地址*/
    val shareServerAddressOnceData = vmDataOnce<String?>()

    /**共享文件通知*/
    val shareFileOnceData = vmDataOnce<File?>()

    /**共享状态通知*/
    val shareStateOnceData = vmDataOnce<Int?>()

    /**共享需要更新[com.angcyo.dsladapter.DslAdapterItem]状态通知, 当收到此通知时, 表示需要更新对应的item*/
    val shareUpdateAdapterItemOnceData = vmDataOnce<Any?>()

    /**共享字符串数据[key:value]*/
    val shareTextMapData = vmData(mutableMapOf<String, CharSequence>())

    /**共享Map状态通知*/
    val shareMapOnceData = vmDataOnce<Map<String, Any?>?>()

    /**共享Map数据通知*/
    val shareMapData = vmDataOnce<Map<String, Any?>?>()

    /**更新/删除共享[shareTextMapData]
     * [value] 为空时, 删除key*/
    fun updateShareTextMapData(key: String, value: CharSequence?) {
        val map = shareTextMapData.value ?: mutableMapOf()
        if (value == null) {
            map.remove(key)
        } else {
            map[key] = value
        }
        shareTextMapData.updateValue(map)
    }

    /**[updateShareTextMapData]*/
    fun updateShareTextMapData(map: Map<String, Any?>?) {
        map ?: return
        val textMap = shareTextMapData.value ?: mutableMapOf()
        map.forEach { (key, value) ->
            if (value == null) {
                textMap.remove(key)
            } else {
                textMap[key] = value.toString()
            }
        }
        shareTextMapData.updateValue(textMap)
    }

    //endregion ---共享数据---

    //region ---共享回调---

    /**[activityDispatchTouchEvent]*/
    val activityDispatchTouchEventAction = mutableListOf<TouchAction>()

    /**
     * 手势触发时回调
     * [com.angcyo.core.activity.BaseCoreAppCompatActivity.dispatchTouchEvent]
     * */
    @Synchronized
    fun activityDispatchTouchEvent(ev: MotionEvent) {
        for (action in activityDispatchTouchEventAction) {
            try {
                action(ev)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //endregion ---共享回调---

}