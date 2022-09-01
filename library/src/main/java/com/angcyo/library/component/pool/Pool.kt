package com.angcyo.library.component.pool

import android.graphics.*
import androidx.core.util.Pools

/**
 * 临时对象 池
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/01
 */

//region ---Rect 池---

private val sRectPool: Pools.Pool<Rect> by lazy { Pools.SynchronizedPool(12) }

/**获取一个[Rect]*/
fun acquireTempRect(): Rect = sRectPool.acquire() ?: Rect()

/**释放一个[Rect], 并且放入池子*/
fun releaseTempRect(rect: Rect) {
    rect.setEmpty()
    sRectPool.release(rect)
}

fun Rect.release() {
    releaseTempRect(this)
}

//endregion ---Rect 池---

//region ---RectF 池---

private val sRectFPool: Pools.Pool<RectF> by lazy { Pools.SynchronizedPool(12) }

fun acquireTempRectF(): RectF = sRectFPool.acquire() ?: RectF()

fun releaseTempRectF(rect: RectF) {
    rect.setEmpty()
    sRectFPool.release(rect)
}

fun RectF.release() {
    releaseTempRectF(this)
}

//endregion ---RectF 池---

//region ---Point 池---

private val sPointPool: Pools.Pool<Point> by lazy { Pools.SynchronizedPool(12) }

fun acquireTempPoint(): Point = sPointPool.acquire() ?: Point()

fun releaseTempPoint(point: Point) {
    point.set(0, 0)
    sPointPool.release(point)
}

fun Point.release() {
    releaseTempPoint(this)
}

//endregion ---Point 池---

//region ---PointF 池---

private val sPointFPool: Pools.Pool<PointF> by lazy { Pools.SynchronizedPool(12) }

fun acquireTempPointF(): PointF = sPointFPool.acquire() ?: PointF()

fun releaseTempPointF(point: PointF) {
    point.set(0f, 0f)
    sPointFPool.release(point)
}

fun PointF.release() {
    releaseTempPointF(this)
}

//endregion ---PointF 池---

//region ---Matrix 池---

private val sMatrixFPool: Pools.Pool<Matrix> by lazy { Pools.SynchronizedPool(12) }

fun acquireTempMatrix(): Matrix = sMatrixFPool.acquire() ?: Matrix()

fun releaseTempMatrix(matrix: Matrix) {
    matrix.reset()
    sMatrixFPool.release(matrix)
}

fun Matrix.release() {
    releaseTempMatrix(this)
}

//endregion ---Matrix 池---

//region ---Path 池---

private val sPathPool: Pools.Pool<Path> by lazy { Pools.SynchronizedPool(12) }

fun acquireTempPath(): Path = sPathPool.acquire() ?: Path()

fun releaseTempPath(path: Path) {
    path.rewind()
    sPathPool.release(path)
}

fun Path.release() {
    releaseTempPath(this)
}

//endregion ---Path 池---

// region ---Region 池---

private val sRegionPool: Pools.Pool<Region> by lazy { Pools.SynchronizedPool(12) }

fun acquireTempRegion(): Region = sRegionPool.acquire() ?: Region()

fun releaseTempRegion(region: Region) {
    region.setEmpty()
    sRegionPool.release(region)
}

fun Region.release() {
    releaseTempRegion(this)
}

//endregion ---Path 池---