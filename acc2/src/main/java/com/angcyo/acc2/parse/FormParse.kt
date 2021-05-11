package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.bean.*
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.ControlContext
import com.angcyo.acc2.control.actionLog
import com.angcyo.library.utils.UrlParse

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
     * [com.angcyo.acc2.bean.OperateBean.form]
     * */
    fun parseOperateForm(
        controlContext: ControlContext,
        control: AccControl,
        handleBean: HandleBean?,
        originList: List<AccessibilityNodeInfoCompat>?,
        handleResult: HandleResult
    ): FormResultBean? {
        var result: FormResultBean? = null
        handleBean?.operate?.form?.let {
            result = handleForm(
                controlContext,
                control,
                it,
                controlContext.action,
                originList,
                handleResult
            )
        }
        return result
    }

    /**表单请求
     * [com.angcyo.acc2.bean.HandleBean.form]
     * */
    fun parseHandleForm(
        controlContext: ControlContext,
        control: AccControl,
        handleBean: HandleBean?,
        originList: List<AccessibilityNodeInfoCompat>?,
        handleResult: HandleResult
    ): FormResultBean? {
        var result: FormResultBean? = null
        handleBean?.form?.let {
            result = handleForm(
                controlContext,
                control,
                it,
                controlContext.action,
                originList,
                handleResult
            )
        }
        return result
    }

    /**表单请求
     * [com.angcyo.acc2.bean.ActionBean.form]
     * */
    fun parseActionForm(
        controlContext: ControlContext,
        control: AccControl,
        actionBean: ActionBean?,
        handleResult: HandleResult
    ): FormResultBean? {
        var result: FormResultBean? = null
        actionBean?.form?.let {
            result = handleForm(controlContext, control, it, actionBean, null, handleResult)
        }
        return result
    }

    /**表单请求
     * [com.angcyo.acc2.bean.TaskBean.form]
     * */
    fun parseTaskForm(control: AccControl, controlState: Int): FormResultBean? {
        var result: FormResultBean? = null
        control._taskBean?.form?.let {
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

            result = request(control, it, params)
        }
        return result
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
    ): FormResultBean? {
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

            //wordList
            wordList?.let {
                control._taskBean?.wordList = it
            }

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

        return formResultBean
    }

    /**交换数据*/
    fun _handleResult(handleResult: HandleResult, result: HandleResult) {
        handleResult.forceFail = handleResult.forceFail || result.forceFail
        handleResult.forceSuccess = handleResult.forceSuccess || result.forceSuccess
        if (handleResult.forceSuccess) {
            handleResult.nodeList = result.nodeList
        }
    }


    /**表单参数收集*/
    fun handleParams(
        control: AccControl,
        formBean: FormBean,
        taskBean: TaskBean?
    ): HashMap<String, Any?> {
        //从url中, 获取默认参数
        val urlParams = UrlParse.getUrlQueryParams(formBean.query)

        //请求参数
        return HashMap<String, Any?>().apply {
            putAll(urlParams)

            //add key
            formBean.params?.split(Action.PACKAGE_SPLIT)?.forEach { key ->
                if (key.isNotEmpty()) {
                    //key
                    if (key == Action.LAST_INPUT) {
                        put(key, control.accSchedule.inputTextList.lastOrNull())
                    } else {
                        put(key, taskBean?.getTextList(key)?.firstOrNull())
                    }
                }
            }

            //add key
            formBean.keyList?.forEach { key ->
                if (key.isNotEmpty()) {
                    put(key, taskBean?.getTextList(key)?.firstOrNull())
                }
            }
        }
    }

    /**回调转发表单请求*/
    fun request(
        control: AccControl,
        formBean: FormBean,
        params: HashMap<String, Any?>? = null
    ): FormResultBean? {
        //解析参数
        val requestParams = handleParams(control, formBean, control._taskBean).apply {
            params?.let { putAll(it) }

            formRequestListener?.configParams?.invoke(formBean, this)
        }

        return formRequestListener?.request(control, formBean, requestParams)
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