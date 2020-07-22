package com.angcyo.core.component.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.angcyo.core.component.accessibility.action.ActionException
import com.angcyo.core.component.accessibility.action.PermissionsAction
import com.angcyo.core.component.accessibility.base.BaseFloatInterceptor
import com.angcyo.core.component.accessibility.parse.TaskBean
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.wrapData
import com.angcyo.library.ex.nowTime
import com.angcyo.library.ex.toElapsedTime
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

        /**执行任务时的日志输出*/
        fun log(data: String) {
            DslFileHelper.write(
                AccessibilityHelper.logFolderName,
                LOG_TASK_FILE_NAME,
                data.wrapData()
            )
        }
    }

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

    override fun onDoActionStart() {
        super.onDoActionStart()

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
        notify(action.actionTitle)
        super.onDoAction(action, service, nodeList)
    }

    override fun onDoActionFinish(action: BaseAccessibilityAction?, error: ActionException?) {
        log("[${taskBean.name}]执行结束:${actionStatus.toActionStatusStr()} ${error ?: ""} 耗时:${(nowTime() - _actionStartTime).toElapsedTime()}")
        if (actionStatus == ACTION_STATUS_ERROR) {
            //出现异常
            notify("异常:${error?.message}")
        } else if (actionStatus == ACTION_STATUS_FINISH) {
            //流程结束
            notify("执行完成!")
            //lastService?.home()
        }
        openApp()
        super.onDoActionFinish(action, error)
    }

    override fun checkLeave(
        service: BaseAccessibilityService,
        mainPackageName: CharSequence?,
        nodeList: List<AccessibilityNodeInfo>
    ): Boolean {
        //actionList.getOrNull(actionIndex)
        return super.checkLeave(service, mainPackageName, nodeList)
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
        val isInterrupt = actionStatus.isActionStart()
        super.onDestroy()
        if (isInterrupt) {
            notify("中止")
        }
    }
}