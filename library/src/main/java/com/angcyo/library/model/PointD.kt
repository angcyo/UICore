package com.angcyo.library.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/28
 */
@Parcelize
data class PointD(var x: Double = 0.0, var y: Double = 0.0) : Parcelable {
    fun offset(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    fun offset(dx: Double, dy: Double) {
        x += dx
        y += dy
    }

    fun negate() {
        x = -x
        y = -y
    }

    fun set(p: PointD) {
        x = p.x
        y = p.y
    }

    operator fun set(x: Float, y: Float) {
        this.x = x.toDouble()
        this.y = y.toDouble()
    }

    operator fun set(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

}
