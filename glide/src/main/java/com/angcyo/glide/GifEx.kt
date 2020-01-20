package com.angcyo.glide

import android.content.res.AssetManager
import android.content.res.Resources
import androidx.annotation.DrawableRes
import com.angcyo.library.L
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifDrawableBuilder
import java.io.File

/**
 * https://github.com/koral--/android-gif-drawable#animation-control
 *
 * Animation control
 * GifDrawable implements an Animatable and MediaPlayerControl so you can use its methods and more:
 *
 * stop() - stops the animation, can be called from any thread
 * start() - starts the animation, can be called from any thread
 * isRunning() - returns whether animation is currently running or not
 * reset() - rewinds the animation, does not restart stopped one
 * setSpeed(float factor) - sets new animation speed factor, eg. passing 2.0f will double the animation speed
 * seekTo(int position) - seeks animation (within current loop) to given position (in milliseconds)
 * getDuration() - returns duration of one loop of the animation
 * getCurrentPosition() - returns elapsed time from the beginning of a current loop of animation
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

fun gifOfAssert(assetManager: AssetManager, assetName: String): GifDrawable? {
    return try {
        GifDrawableBuilder().from(assetManager, assetName).build()
    } catch (e: Exception) {
        L.w(e)
        null
    }
}

fun gifOfRes(resources: Resources, @DrawableRes resourceId: Int): GifDrawable? {
    return try {
        GifDrawableBuilder().from(resources, resourceId).build()
    } catch (e: Exception) {
        L.w(e)
        null
    }
}

fun gifOfPath(filePath: String): GifDrawable? {
    return try {
        GifDrawableBuilder().from(filePath).build()
    } catch (e: Exception) {
        L.w(e)
        null
    }
}

fun gifOfFile(file: File): GifDrawable? {
    return try {
        GifDrawableBuilder().from(file).build()
    } catch (e: Exception) {
        L.w(e)
        null
    }
}

