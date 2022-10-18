package com.angcyo.svg

import com.angcyo.vector.VectorWriteHandler

/**
 * 将[Path]输出成[svg]path数据
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/10/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class SvgWriteHandler : VectorWriteHandler() {

    override fun onPathStart() {
        super.onPathStart()
        //viewBox ?
    }

    override fun onPathEnd() {
        super.onPathEnd()
        //closeSvg() //need?
    }

    override fun onNewPoint(x: Float, y: Float) {
        writer?.append("M${x} $y")
    }

    override fun onLineToPoint(x: Float, y: Float) {
        //super.onLineToPoint(x, y)
        writer?.append("L${x} $y")
    }


    /**关闭路径*/
    fun closeSvg() {
        writer?.appendLine("Z")
    }
}