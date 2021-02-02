package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.library.ex.*
import com.angcyo.library.utils.getLongNum

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
class PathParse(val accParse: AccParse) {

    /**通过路径[+1 -2 >3 <4]获取对应的节点*/
    fun parse(node: AccessibilityNodeInfoCompat?, path: String?): AccessibilityNodeInfoCompat? {
        if (node == null || path.isNullOrEmpty()) {
            return node
        }
        var target: AccessibilityNodeInfoCompat? = node
        //格式: +1 -2 >3 <4
        val paths = path.split(" ").toList()
        for (p in paths) {
            target = parsePath(target, p)

            if (target == null) {
                break
            }
        }
        return target
    }

    /**@param path +1 / -1  / >1 / <1 */
    fun parsePath(node: AccessibilityNodeInfoCompat?, path: String): AccessibilityNodeInfoCompat? {
        return if (node == null || path.isEmpty()) {
            node
        } else {
            //[+1] 兄弟下1个的节点
            //[-2] 兄弟上2个的节点
            //[>3] child第3个节点
            //[<4] 第4个parent

            val num = path.getLongNum()?.toInt() ?: 0 //path.substring(1, path.length).toIntOrNull()
            val stateList = path.patternList("[a-zA-Z]+") //clickable等状态匹配

            if (stateList.isEmpty()) {
                val numAbs = num.abs()
                //不需要匹配状态
                when {
                    path.startsWith("+") -> node.getBrotherNode(numAbs)
                    path.startsWith("-") -> node.getBrotherNode(-numAbs)
                    path.startsWith(">") -> node.getParentOrChildNode(numAbs)
                    path.startsWith("<") -> node.getParentOrChildNode(-numAbs)
                    else -> null
                }
            } else {
                //需要匹配状态
                if (num > 1) {
                    var result: AccessibilityNodeInfoCompat? = node
                    for (i in 0 until num) {
                        result = when {
                            path.startsWith("+") -> result?.getBrotherNodeNext {
                                accParse.findParse.matchNodeStateOr(it, stateList)
                            }
                            path.startsWith("-") -> result?.getBrotherNodePrev {
                                accParse.findParse.matchNodeStateOr(it, stateList)
                            }
                            path.startsWith(">") -> result?.getParentOrChildNodeDown {
                                accParse.findParse.matchNodeStateOr(it, stateList)
                            }
                            path.startsWith("<") -> result?.getParentOrChildNodeUp {
                                accParse.findParse.matchNodeStateOr(it, stateList)
                            }
                            else -> null
                        }
                        if (result == null) {
                            break
                        }
                    }
                    result
                } else {
                    when {
                        path.startsWith("+") -> node.getBrotherNodeNext {
                            accParse.findParse.matchNodeStateOr(it, stateList)
                        }
                        path.startsWith("-") -> node.getBrotherNodePrev {
                            accParse.findParse.matchNodeStateOr(it, stateList)
                        }
                        path.startsWith(">") -> node.getParentOrChildNodeDown {
                            accParse.findParse.matchNodeStateOr(it, stateList)
                        }
                        path.startsWith("<") -> node.getParentOrChildNodeUp {
                            accParse.findParse.matchNodeStateOr(it, stateList)
                        }
                        else -> null
                    }
                }
            }
        }
    }

}