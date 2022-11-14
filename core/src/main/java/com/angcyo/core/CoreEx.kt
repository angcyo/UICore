package com.angcyo.core

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.angcyo.base.show
import com.angcyo.core.activity.BaseCoreAppCompatActivity
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.fileViewDialog
import com.angcyo.dialog.normalDialog
import com.angcyo.fragment.AbsLifecycleFragment
import com.angcyo.iview.IView
import com.angcyo.library.app
import com.angcyo.library.component.toAppDetail
import com.angcyo.library.ex.*
import com.angcyo.library.utils.RUtils
import com.angcyo.widget.DslButton
import com.angcyo.widget.DslGroupHelper
import com.angcyo.widget.image.DslImageView
import com.angcyo.widget.span.span
import com.angcyo.widget.text.DslTextView
import ezy.assist.compat.RomUtil


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun coreApp() = app() as CoreApplication

/**在[Fragment]中检查crash*/
fun Fragment.checkCrash() {
    if (!isRelease()) {
        val act = activity
        if (act is BaseCoreAppCompatActivity) {
            act.showCrashDialog()
        }
    }
}

/**崩溃对话框, 带复制/查看/分享功能*/
fun Context.checkShowCrashDialog(clear: Boolean = true) {
    BaseCoreAppCompatActivity.haveLastCrash =
        DslCrashHandler.checkCrash(clear) { filePath, message, crashTime ->
            val file = filePath?.file()
            file?.readText()?.copy(this)

            normalDialog {
                canceledOnTouchOutside = false
                dialogTitle = "发生了什么^_^"
                dialogMessage = span {
                    append(crashTime) {
                        foregroundColor = _color(R.color.colorAccent)
                    }
                    appendln()
                    append(message)
                }
                /*positiveButton("粘贴给开发?") { _, _ ->
                    RUtils.chatQQ(this@checkShowCrashDialog)
                }
                negativeButton("加入QQ群?") { _, _ ->
                    RUtils.joinQQGroup(this@checkShowCrashDialog)
                }
                neutralButton("分享文件?") { _, _ ->
                    file?.shareFile(this@checkShowCrashDialog)
                }*/
                negativeButton("无视") { dialog, _ ->
                    dialog.dismiss()
                }
                positiveButton("分享") { _, _ ->
                    file?.shareFile(this@checkShowCrashDialog)
                }
                onDialogInitListener = { _, dialogViewHolder ->
                    dialogViewHolder.click(R.id.dialog_message_view) {
                        //file?.open(this@checkShowCrashDialog)
                        file?.let { fileViewDialog(it.absolutePath) }
                    }
                }
            }
        }
}

/**
 * [com.angcyo.base.FragmentExKt.show]
 * */
fun IView.showIn(fragment: AbsLifecycleFragment, group: ViewGroup? = null) {
    var parentView: ViewGroup? = group
    if (parentView == null) {
        parentView = fragment._vh.group(R.id.lib_content_overlay_wrap_layout)
        parentView.visible()
    }
    if (parentView == null) {
        parentView = fragment._vh.itemView as ViewGroup
    }
    show(fragment, parentView)
}

//<editor-fold desc="Application级别的单例模式">

fun Activity.core(action: CoreApplication.() -> Unit = {}): Application {
    if (application is CoreApplication) {
        (application as CoreApplication).action()
    }
    return application
}

inline fun <reified Obj> Activity.hold(): Obj {
    return (application as CoreApplication).holdGet(Obj::class.java)
}

fun Fragment.core(action: CoreApplication.() -> Unit = {}): Application {
    return requireActivity().core(action)
}

inline fun <reified Obj> Fragment.hold(): Obj {
    return (requireActivity().application as CoreApplication).holdGet(Obj::class.java)
}

/**[CoreApplication]中的[ViewModel]*/
inline fun <reified VM : ViewModel> Activity.vmCore(): VM {
    return ViewModelProvider(
        coreApp(),
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    ).get(VM::class.java)
}

/**返回CoreApplication级别的[ViewModel]*/
inline fun <reified VM : ViewModel> Fragment.vmCore(): VM {
    return requireActivity().vmCore()
}

/**返回CoreApplication级别的[ViewModel]*/
inline fun <reified VM : ViewModel> vmCore(): VM {
    return ViewModelProvider(
        coreApp(),
        ViewModelProvider.AndroidViewModelFactory.getInstance(app() as Application)
    )[VM::class.java]
}

inline fun <reified VM : ViewModel> vmApp(): VM = vmCore()

//</editor-fold desc="Application级别的单例模式">

//<editor-fold desc="Append操作">

fun DslGroupHelper.appendButtonItem(
    attachToRoot: Boolean = true,
    action: DslButton.() -> Unit
): View? {
    return inflate(R.layout.core_button_layout, attachToRoot) {
        find<DslButton>(R.id.lib_button)?.apply {
            this.action()
        }
    }
}

fun DslGroupHelper.appendTextItem(
    attachToRoot: Boolean = true,
    action: DslTextView.() -> Unit
): View? {
    return inflate(R.layout.lib_text_layout, attachToRoot) {
        find<DslTextView>(R.id.lib_text_view)?.apply {
            this.action()
        }
    }
}

fun DslGroupHelper.appendIconItem(
    attachToRoot: Boolean = true,
    action: DslImageView.() -> Unit
): View? {
    return inflate(R.layout.lib_icon_layout, attachToRoot) {
        find<DslImageView>(R.id.lib_icon_view)?.apply {
            this.action()
        }
    }
}

fun DslGroupHelper.appendItem(
    @LayoutRes
    layoutId: Int = R.layout.lib_text_layout,
    attachToRoot: Boolean = true,
    action: View.() -> Unit
): View? {
    return inflate(layoutId, attachToRoot) {
        this.action()
    }
}

//</editor-fold desc="Append操作">

/**跳转app权限管理页
 * https://www.jianshu.com/p/b5c494dba0bc
 * */
fun Context.toAppPermissionsDetail(packageName: String = this.packageName) {
    try {
        if (RomUtil.isEmui()) {
            Intent().apply {
                putExtra("packageName", packageName)
                val comp = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity"
                )
                component = comp
                baseConfig(this@toAppPermissionsDetail)
                startActivity(this)
            }
        } else if (RomUtil.isFlyme()) {
            Intent("com.meizu.safe.security.SHOW_APPSEC").apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                putExtra("packageName", packageName)
                baseConfig(this@toAppPermissionsDetail)
                startActivity(this)
            }
        } else if (RomUtil.isMiui()) {

            val miuiVersion = RUtils.getMIUIVersion() ?: 0

            if (miuiVersion >= 12) {
                Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    val componentName = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    )
                    component = componentName
                    putExtra("extra_pkgname", packageName)
                    baseConfig(this@toAppPermissionsDetail)
                    startActivity(this)
                }
            } else if (miuiVersion >= 8) {
                Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity"
                    );
                    putExtra("extra_pkgname", packageName)
                    baseConfig(this@toAppPermissionsDetail)
                    startActivity(this)
                }
            } else if (miuiVersion >= 6) {
                Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
                    );
                    putExtra("extra_pkgname", packageName)
                    baseConfig(this@toAppPermissionsDetail)
                    startActivity(this)
                }
            } else {
                val packageURI: Uri = Uri.parse("package:$packageName")
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI).apply {
                    baseConfig(this@toAppPermissionsDetail)
                    startActivity(this)
                }
            }
        } else if (RomUtil.isQiku()) {
            Intent("android.intent.action.MAIN").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("packageName", packageName)
                val comp = ComponentName(
                    "com.qihoo360.mobilesafe",
                    "com.qihoo360.mobilesafe.ui.index.AppEnterActivity"
                )
                component = comp
                baseConfig(this@toAppPermissionsDetail)
                startActivity(this)
            }
        } /*else if (RomUtil.isOppo()) {
        } else if (RomUtil.isVivo()) */ else {
            packageName.toAppDetail(this)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}