package com.angcyo.media.video.record.inner

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.View

/**
 * Created by dalong on 2017/1/3.
 */
class SizeSurfaceView(context: Context, attrs: AttributeSet?) : SurfaceView(context, attrs) {
    var isUserSize = false

    var mVideoWidth = 0
    var mVideoHeight = 0
    var mMeasuredWidth = 0
    var mMeasuredHeight = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isUserSize) {
            doMeasure(widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(mMeasuredWidth, mMeasuredHeight)
            cameraDistance = 0.5f
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * 设置视频宽高
     * @param width
     * @param height
     */
    fun setVideoDimension(width: Int, height: Int) {
        mVideoWidth = width
        mVideoHeight = height
    }

    /**
     * 测量
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    fun doMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = View.getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = View.getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            val specAspectRatio =
                widthSpecSize.toFloat() / heightSpecSize.toFloat()
            val displayAspectRatio =
                mVideoWidth.toFloat() / mVideoHeight.toFloat()
            val shouldBeWider = displayAspectRatio > specAspectRatio
            if (shouldBeWider) {
                // not high enough, fix height
                height = heightSpecSize
                width = (height * displayAspectRatio).toInt()
            } else {
                // not wide enough, fix width
                width = widthSpecSize
                height = (width / displayAspectRatio).toInt()
            }
        }
        mMeasuredWidth = width
        mMeasuredHeight = height
    }
}