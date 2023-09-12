package com.angcyo.library.component.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import com.angcyo.library.ex.file
import com.angcyo.library.ex.pfd


/**
 * Pdf 解析
 *
 * https://github.com/barteksc/PdfiumAndroid
 * https://github.com/barteksc/AndroidPdfViewer
 *
 * https://developer.android.com/reference/android/graphics/pdf/PdfDocument
 * https://developer.android.com/reference/android/graphics/pdf/PdfRenderer
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/09/12
 */
object Pdf {

    /**将pdf文件, 读取成[Bitmap]对象
     *
     * [android.os.ParcelFileDescriptor.open]
     * [android.content.ContentResolver.openFile]
     * */
    fun readPdfToBitmap(input: ParcelFileDescriptor?): List<Bitmap>? {
        input ?: return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val renderer = PdfRenderer(input)
                val result = mutableListOf<Bitmap>()
                // let us just render all pages
                val pageCount = renderer.getPageCount()
                for (i in 0 until pageCount) {
                    try {
                        val page = renderer.openPage(i)
                        val bitmap =
                            Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)

                        // say we render for showing on the screen
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        // do stuff with the bitmap

                        // close the page
                        page.close()

                        result.add(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // close the renderer
                renderer.close()
                return result
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        } else {
            //不支持
            return null
        }
    }

    fun readPdfToBitmap(path: String?): List<Bitmap>? = readPdfToBitmap(path?.file()?.pfd())

    fun readPdfToBitmap(uri: Uri?): List<Bitmap>? = readPdfToBitmap(uri?.pfd())
}