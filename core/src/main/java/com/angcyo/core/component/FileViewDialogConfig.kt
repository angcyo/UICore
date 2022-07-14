package com.angcyo.core.component

import android.app.Dialog
import android.content.Context
import com.angcyo.core.R
import com.angcyo.coroutine.launchLifecycle
import com.angcyo.coroutine.withBlock
import com.angcyo.dialog.BaseDialogConfig
import com.angcyo.dialog.configBottomDialog
import com.angcyo.library.ex.*
import com.angcyo.widget.DslViewHolder

/**
 * 简单的文件查看对话框
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/07/14
 */
class FileViewDialogConfig(context: Context? = null) : BaseDialogConfig(context) {

    /**需要打开的文件路径*/
    var filePath: String? = null

    /**读取文件的行数*/
    var readFileLines: Int = 500

    /**反向读取文件*/
    var readReversed: Boolean = true

    init {
        dialogLayoutId = R.layout.lib_dialog_file_view_layout
    }

    override fun initDialogView(dialog: Dialog, dialogViewHolder: DslViewHolder) {
        super.initDialogView(dialog, dialogViewHolder)

        dialogViewHolder.visible(R.id.loading_wrap_layout)
        launchLifecycle {
            val file = filePath?.file()
            dialogViewHolder.tv(R.id.lib_title_view)?.text = file?.name

            val support = file?.canRead() == true

            val text = withBlock {
                if (support) {
                    file.readTextLines(readFileLines, reversed = readReversed) //读取最后100行的数据
                } else {
                    "file does not exist!"
                }
            }
            //result
            dialogViewHolder.gone(R.id.loading_wrap_layout)
            dialogViewHolder.tv(R.id.lib_text_view)?.text = text
            dialogViewHolder.tv(R.id.lib_des_view)?.text = file?.length()?.fileSizeString()

            dialogViewHolder.gone(R.id.open_view, !support)
            dialogViewHolder.gone(R.id.share_view, !support)

            //打开文件
            dialogViewHolder.click(R.id.open_view) {
                file?.open()
            }
            //分享分享
            dialogViewHolder.click(R.id.share_view) {
                file?.shareFile(dialogViewHolder.context)
            }

            /*dialogViewHolder.v<WebView>(R.id.web_view)?.apply {
                Web.initWebView(this)
                //loadData(text, "text/html", "utf-8")
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        view.loadUrl(url)
                        return true
                    }
                }
                loadDataWithBaseURL(null, text, "text/html", "utf-8", null)
            }*/
        }
    }
}

/**
 * 简单的查看文件内容的对话框
 * [filePath] 文件的全路径
 * */
fun Context.fileViewDialog(
    filePath: String? = null,
    config: FileViewDialogConfig.() -> Unit = {}
): Dialog {
    return FileViewDialogConfig(this).run {
        configBottomDialog()
        this.filePath = filePath
        config()
        show()
    }
}
