package com.angcyo.core.component.manage

/**
 * 内部文件管理界面的选择模式下的数据模型
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/07
 */
data class InnerFileSelectParamBean(
    /**最多允许选择多少个文件
     * 0: 不开启选择模式
     * 1: 单选模式
     * >1: 多选模式
     * */
    var maxSelectFileCount: Int = 0,
    /**需要过滤的文件扩展名*/
    var filterFileExtList: List<String>? = null
)
