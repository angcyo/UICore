package com.angcyo.core.component.step

/**
 * 步数更新回调
 * Created by dylan on 16/9/27.
 */
interface StepUpdateUiCallBack {
    /**
     * 更新UI步数
     *
     * @param stepCount 步数
     */
    fun updateUi(stepCount: Long)
}