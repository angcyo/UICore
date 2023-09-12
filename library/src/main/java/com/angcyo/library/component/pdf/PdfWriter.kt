package com.angcyo.library.component.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.angcyo.library.L
import com.angcyo.library.annotation.DSL
import com.angcyo.library.ex.file
import java.io.File
import java.io.OutputStream


/**
 * pdf 文件写入
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/12
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class PdfWriter {

    var document: PdfDocument? = null

    /**页码*/
    var pageNumber = 0

    /**创建文档*/
    fun init() {
        if (document == null) {
            document = PdfDocument()
        }
    }

    //---

    fun writePage(
        view: View,
        pageWidth: Int = view.width,
        pageHeight: Int = view.height,
        draw: Canvas.() -> Unit = {}
    ) {
        init()
        pageNumber++
        val pageInfo = PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        document?.apply {
            val page = startPage(pageInfo)
            view.draw(page.canvas)
            page.canvas.draw()
            finishPage(page)
        }
    }

    fun writePage(
        bitmap: Bitmap,
        pageWidth: Int = bitmap.width,
        pageHeight: Int = bitmap.height,
        draw: Canvas.() -> Unit = {}
    ) {
        init()
        pageNumber++
        val pageInfo = PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        document?.apply {
            val page = startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            page.canvas.draw()
            finishPage(page)
        }
    }

    /**写入一页
     * 直接在[Canvas]绘制想要的内容即可
     * */
    fun writePage(pageWidth: Int, pageHeight: Int, draw: Canvas.() -> Unit) {
        init()
        pageNumber++
        val pageInfo = PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        document?.apply {
            val page = startPage(pageInfo)
            page.canvas.draw()
            finishPage(page)
        }
    }

    //---

    fun finish(filePath: String) {
        finish(filePath.file())
    }

    fun finish(file: File) {
        file.outputStream().use {
            finish(it)
        }
    }

    /**完成*/
    fun finish(out: OutputStream) {
        document?.writeTo(out)
        document?.close()
    }

}

/**写入内容到pdf文件*/
@DSL
fun pdfWriter(filepath: String, action: PdfWriter.() -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val writer = PdfWriter()
        writer.action()
        writer.finish(filepath)
    } else {
        L.w("当前系统版本不支持Pdf操作")
    }
}