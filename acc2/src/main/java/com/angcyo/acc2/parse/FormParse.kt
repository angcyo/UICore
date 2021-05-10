package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.bean.ActionBean
import com.angcyo.acc2.bean.FormBean
import com.angcyo.acc2.bean.FormResultBean
import com.angcyo.acc2.bean.HandleBean
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.control.actionLog

/**
 * 表单解析器
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/05/10
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class FormParse : BaseParse() {

    /**请求监听*/
    var formRequestListener: RequestListener? = null

    //<editor-fold desc="触发">

    /**表单请求
     * [com.angcyo.acc2.bean.TaskBean.form]
     * */
    fun parseTaskForm(control: AccControl, controlState: Int) {
        control._taskBean?.form?.let {
            if (controlState >= AccControl.CONTROL_STATE_FINISH) {
                //结束之后, 才请求form
                val params = hashMapOf<String, Any?>()
                when (controlState) {
                    AccControl.CONTROL_STATE_FINISH -> params[FormBean.KEY_CODE] = 200
                    AccControl.CONTROL_STATE_STOP -> params[FormBean.KEY_CODE] = 300 //本地执行中断, 任务终止.
                    AccControl.CONTROL_STATE_ERROR -> params[FormBean.KEY_CODE] =
                        500 //本地执行错误, 任务终止.
                }
                params[FormBean.KEY_MSG] = control.finishReason
                params[FormBean.KEY_DATA] = controlState

                request(control, it, params)
            }
        }
    }

    /**表单请求
     * [com.angcyo.acc2.bean.ActionBean.form]
     * */
    fun parseActionForm(
        controlContext: ControlContext,
        control: AccControl,
        actionBean: ActionBean,
        handleResult: HandleResult
    ) {
        actionBean.form?.let {
            handleForm(controlContext, control, it, actionBean, null, handleResult)
        }
    }

    /**表单请求
     * [com.angcyo.acc2.bean.OperateBean.form]
     * */
    fun parseOperateForm(
        controlContext: ControlContext,
        control: AccControl,
        handleBean: HandleBean,
        originList: List<AccessibilityNodeInfoCompat>?,
        handleResult: HandleResult
    ) {
        handleBean.operate?.form?.let {
            handleForm(controlContext, control, it, controlContext.action, originList, handleResult)
        }
    }

    //</editor-fold desc="触发">

    //<editor-fold desc="处理">

    fun handleForm(
        controlContext: ControlContext,
        control: AccControl,
        formBean: FormBean,
        actionBean: ActionBean?,
        originList: List<AccessibilityNodeInfoCompat>?,
        handleResult: HandleResult,
    ) {
        val params = hashMapOf<String, Any?>()
        if (handleResult.success || handleResult.forceSuccess) {
            params[FormBean.KEY_CODE] = 200
        } else {
            params[FormBean.KEY_CODE] = 301 //本地执行中断, 但是需要继续任务
        }
        params[FormBean.KEY_MSG] = actionBean?.title
        params[FormBean.KEY_DATA] = actionBean?.actionLog()

        var formResultBean: FormResultBean? = null

        if (formBean.checkSuccess) {
            if (handleResult.success || handleResult.forceSuccess) {
                formResultBean = request(control, formBean, params)
            }
        } else {
            formResultBean = request(control, formBean, params)
        }

        //result
        formResultBean?.apply {

            //1.
            actions?.forEach {
                formRequestListener?.initAction(it)?.let { actionBean ->
                    control.accSchedule.addTargetAction(actionBean)
                }
            }

            val handleParse = control.accSchedule.accParse.handleParse
            //2.
            handleList?.let {
                val result = handleParse.parse(controlContext, null, it)
                _handleResult(handleResult, result)
            }

            //3.
            actionList?.let {
                val textParam = controlContext.handle?.textParam ?: control._taskBean?.textParam
                val result = handleParse.handleAction(
                    controlContext,
                    controlContext.handle,
                    textParam,
                    originList,
                    it
                )
                _handleResult(handleResult, result)
            }
        }
    }

    /**交换数据*/
    fun _handleResult(handleResult: HandleResult, result: HandleResult) {
        handleResult.forceFail = handleResult.forceFail || result.forceFail
        handleResult.forceSuccess = handleResult.forceSuccess || result.forceSuccess
        if (handleResult.forceSuccess) {
            handleResult.nodeList = result.nodeList
        }
    }

    fun request(
        control: AccControl,
        formBean: FormBean,
        params: HashMap<String, Any?>? = null
    ): FormResultBean? {
        return formRequestListener?.request(control, formBean, params)
    }

    //</editor-fold desc="处理">

    abstract class RequestListener {

        /**参数配置*/
        var configParams: ((formBean: FormBean, params: HashMap<String, Any?>) -> Unit)? = null

        open fun initAction(actionBean: ActionBean): ActionBean {
            return actionBean
        }

        /**发送表单请求*/
        open fun request(
            control: AccControl,
            formBean: FormBean,
            params: HashMap<String, Any?>? = null
        ): FormResultBean? {
            return null
        }
    }
}