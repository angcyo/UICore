package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.angcyo.core.component.accessibility.action.*
import com.angcyo.core.component.accessibility.base.BaseFloatInterceptor
import com.angcyo.core.component.accessibility.parse.*
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.wrapData
import com.angcyo.library.LTime
import com.angcyo.library.ex.elseNull
import com.angcyo.library.ex.file
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.toElapsedTime
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.FileUtils
import kotlin.math.max

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class AutoParseInterceptor(val taskBean: TaskBean) : BaseFloatInterceptor() {

    companion object {
        const val LOG_TASK_FILE_NAME = "task.log"

        /**当前运行的[ActionBean]*/
        var currentActionBean: ActionBean? = null

        /**执行任务时的日志输出*/
        fun log(data: CharSequence?) {
            if (!data.isNullOrEmpty()) {
                DslFileHelper.write(
                    AccessibilityHelper.logFolderName,
                    LOG_TASK_FILE_NAME,
                    data.wrapData()
                )
            }
        }

        fun logPath() = FileUtils.appRootExternalFolderFile(
            folder = AccessibilityHelper.logFolderName,
            name = LOG_TASK_FILE_NAME
        )?.absolutePath

        /**读取日志文本*/
        fun readLog(): String? {
            return logPath()?.file()?.readText()
        }
    }

    /**请求表单时, 配置表单数据的回调*/
    var onConfigParams: ((params: HashMap<String, Any>) -> Unit)? = null

    init {
        //固定通知id
        notifyId = 999
    }

    //发送通知
    fun notify(
        content: CharSequence?,
        title: CharSequence? = "${taskBean.name}(${max(0, actionIndex)}/${actionList.size})"
    ) {
        sendNotify(title, content)
    }

    override fun startAction(restart: Boolean) {
        super.startAction(restart)
    }

    override fun onDoActionStart() {
        super.onDoActionStart()

        //清空缓存
        taskBean.getTextResultMap = null

        notify("就绪")

        //立即启动对应app
        //taskBean.packageName?.openApp(0)
    }

    override fun handleFilterNode(
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        super.handleFilterNode(service, nodeList)
    }

    override fun onDoAction(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        //save
        if (action is AutoParseAction) {
            currentActionBean = action.actionBean
        }
        notify(action.actionTitle)
        super.onDoAction(action, service, nodeList)
    }

    override fun onDoActionFinish(action: BaseAccessibilityAction?, error: ActionException?) {
        handleFormRequest(error)

        log("[${taskBean.name}]执行结束:${actionStatus.toActionStatusStr()} ${error ?: ""} 耗时:${(nowTime() - _actionStartTime).toElapsedTime()}")
        if (actionStatus == ACTION_STATUS_ERROR) {
            //出现异常
            notify("异常:${error?.message}")
        } else if (actionStatus == ACTION_STATUS_FINISH) {
            //流程结束
            notify("执行完成!")
            //lastService?.home()
        }

        if (taskBean.finishToApp) {
            openApp()
        }

        super.onDoActionFinish(action, error)
    }

    override fun checkLeave(
        service: BaseAccessibilityService,
        mainPackageName: CharSequence?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        //actionList.getOrNull(actionIndex)
        return super.checkLeave(service, mainPackageName, nodeList).apply {

            var needDefaultHandle = true

            if (interceptorLeaveCount.isMaxLimit()) {
                //超限
                if (taskBean.leave != null) {
                    currentAccessibilityAction?.also { action ->
                        if (action is AutoParseAction) {
                            if (action._actionFinish == null) {
                                action._actionFinish = {
                                    _actionFinish(action, service, it)
                                }
                            }
                            val result = action.parseHandleAction(service, nodeList, taskBean.leave)
                            if (result) {
                                needDefaultHandle = false
                                interceptorLeaveCount.clear()
                            }
                        }
                    }
                }
            }

            if (filterPackageNameList.isNotEmpty()) {
                interceptorLog?.log("离开目标:$filterPackageNameList ${LTime.time(_lastLeaveTime)} 停留在:$_lastLeavePackageName")

                if (needDefaultHandle && nowTime() - _lastLeaveTime > 10 * 60 * 1000) {
                    //离开停留在同一个界面时间超过10分钟,强制重新开始
                    interceptorLog?.log("超过10分钟,即将重新开始!")
                    _lastLeaveTime = 0
                    restart()
                }
            }
        }
    }

    override fun onLeavePackageName(
        service: BaseAccessibilityService,
        fromPackageName: CharSequence?,
        toPackageName: CharSequence?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        //super.onLeavePackageName(service, fromPackageName, toPackageName, nodeList)
        PermissionsAction().apply {
            if (doActionWidth(this, service, lastEvent, nodeList)) {
                log("[${taskBean.name}]权限请求:true")
            }
        }
    }

    override fun onNoOtherActionHandle(
        action: BaseAccessibilityAction,
        service: BaseAccessibilityService,
        event: AccessibilityEvent?,
        nodeList: List<AccessibilityNodeInfo>
    ) {
        super.onNoOtherActionHandle(action, service, event, nodeList)
    }

    override fun onDestroy() {
        //clear
        currentActionBean = null

        val isInterrupt = actionStatus.isActionStart()
        if (isInterrupt) {
            val actionInterruptedException = ActionInterruptedException("拦截器被中断[onDestroy]!")
            currentAccessibilityAction?.let {
                it.doActionFinish(actionInterruptedException)
            }.elseNull {
                onDoActionFinish(null, actionInterruptedException)
            }
        }

        //super
        super.onDestroy()
        if (isInterrupt) {
            notify("中止")
        }
    }

    /**表单请求*/
    fun handleFormRequest(error: ActionException?) {
        taskBean.form?.let {
            //指定了表单处理
            it.request(result = { data, error ->
                if (error != null) {
                    //拦截器,接口请求失败
                    toastQQ(error.message)
                }
            }) { map ->

                //params
                handleFormParams(map)

                //action执行结果, 执行成功发送 200
                if (error == null) {
                    map[FormBean.KEY_MSG] =
                        "${taskBean.name} 执行完成,耗时${nowTime() - _actionStartTime}"
                } else {
                    if (error is ErrorActionException) {
                        map[FormBean.KEY_MSG] = error.message ?: "${taskBean.name} 执行失败!"
                    } else {
                        map[FormBean.KEY_MSG] = "${taskBean.name} 执行失败,${error.message}"
                    }
                }

                //错误码绑定
                map.bindErrorCode(error)

                //额外配置
                onConfigParams?.apply {
                    invoke(map)
                }
            }
        }
    }

    /**表单参数*/
    fun handleFormParams(params: HashMap<String, Any>) {
        //将action获取到的所有文本, 打包进参数
        taskBean.getTextResultMap?.forEach { entry ->
            params[entry.key] = "${entry.value.firstOrNull()}"
        }
    }
}