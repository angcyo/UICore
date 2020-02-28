package com.angcyo.ucrop

import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.angcyo.base.enableLayoutFullScreen
import com.angcyo.base.setNavigationBarColor
import com.angcyo.base.setStatusBarColor
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.Media
import com.angcyo.library.utils.folderPath
import com.angcyo.picker.R
import com.angcyo.widget.base.getChildOrNull
import com.yalantis.ucrop.UCropActivity
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/20
 */
class RCropActivity : UCropActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableLayoutFullScreen()
        setStatusBarColor()
        setNavigationBarColor(com.angcyo.library.ex.getColor(R.color.picker_bottom_bar_bg_color))

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar?.fitsSystemWindows = true


        findViewById<ViewGroup>(R.id.layout_aspect_ratio)?.run {
            //默认选中1:1
            getChildOrNull(0)?.performClick()
        }
    }

    override fun cropAndSaveImage() {
        super.cropAndSaveImage()
    }

    override fun setResultError(throwable: Throwable?) {
        super.setResultError(throwable)
    }

    override fun setResultUri(
        uri: Uri,
        resultAspectRatio: Float,
        offsetX: Int,
        offsetY: Int,
        imageWidth: Int,
        imageHeight: Int
    ) {
        try {
            val sourcePath = uri.path
            val file = File(sourcePath!!)
            val targetFile =
                Media.copyFrom(file, folderPath(Constant.CROP_FOLDER_NAME), imageWidth, imageHeight)
            if (targetFile.exists()) {
                super.setResultUri(
                    Uri.fromFile(targetFile),
                    resultAspectRatio,
                    offsetX,
                    offsetY,
                    imageWidth,
                    imageHeight
                )
                file.delete()
            } else {
                super.setResultUri(
                    Uri.fromFile(file),
                    resultAspectRatio,
                    offsetX,
                    offsetY,
                    imageWidth,
                    imageHeight
                )
            }
        } catch (e: Exception) {
            super.setResultUri(uri, resultAspectRatio, offsetX, offsetY, imageWidth, imageHeight)
        }

    }

    override fun finish() {
        super.finish()
        _animOverride()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        _animOverride()
    }

    var exitAnim = R.anim.lib_picker_exit_anim
    var enterAnim = R.anim.lib_picker_other_enter_anim

    fun _animOverride() {
        if (enterAnim != -1 || exitAnim != -1) {
            overridePendingTransition(enterAnim, exitAnim)
        }
    }
}