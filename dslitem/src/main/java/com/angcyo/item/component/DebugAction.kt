package com.angcyo.item.component

import com.angcyo.core.fragment.BaseDslFragment

/**
 * 调试入口配置点
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/18
 *
 * - [DebugFragment]
 * - [HttpRequestTestFragment]
 */
data class DebugAction(
    //region Button

    /**按钮的名字*/
    var name: String = "",
    /**日志的路径, 如果设置了则会直接显示对应的日志内容
     * 设置了[action]会覆盖默认的点击行为*/
    var logPath: String? = null,
    /**
     * 是否强制反向读取文件? 否则文件很小时才会逆序读取数据
     * */
    var readReversed: Boolean? = null,
    /**按钮的点击回调, 或者属性item的回调
     *
     * - [DebugFragment]
     * - [HttpRequestTestFragment]
     * */
    var action: ((BaseDslFragment, value: Any?) -> Unit)? = null,

    //endregion Button

    //region Hawk开关/数值属性控制

    /**显示的属性标签*/
    var label: CharSequence? = null,
    /**属性描述内容*/
    var des: CharSequence? = null,
    /**属性类型, 暂且只支持bool类型*/
    var type: Class<*> = Boolean::class.java,
    /**存储的key, 有这个值时, 才会激活属性item*/
    var key: String? = null,
    /**[key]对应的默认值*/
    var defValue: Any? = null,

    /**使用新的数字键盘输入*/
    var useNewNumberKeyboardDialog: Boolean = false,

    //endregion Hawk开关/数值属性控制

    //region HttpRequestTestFragment

    /**配置主机地址, 不指定不设置*/
    var host: String? = null,

    /**配置请求接口地址, 不指定不设置*/
    var requestUrl: String? = null,

    /**配置请求体jsonString, 不指定不设置*/
    var requestBody: String? = null

    //endregion HttpRequestTestFragment

)
