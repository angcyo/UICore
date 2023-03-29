package com.angcyo.library.component

/**
 * 版本匹配规则验证
 *
 * 规则 [x~xxx x~xxx x~xxx], 若干组 [x~xxx] 的范围
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/11/04
 */
object VersionMatcher {

    internal const val RS = " "
    internal const val VS = "~"

    /**解析范围, 格式 [ x x~ ~x xxx~xxx xxx~xxx]*/
    fun parseRange(config: String?): List<VersionRange> {
        val rangeStringList = config?.split(RS)
        val list = mutableListOf<VersionRange>()
        rangeStringList?.forEach { range ->
            val rangeString = range.split(VS)
            rangeString.firstOrNull()?.toIntOrNull()?.let { min ->
                if (rangeString.size >= 2) {
                    list.add(
                        VersionRange(
                            min,
                            rangeString.getOrNull(1)?.toIntOrNull() ?: Int.MAX_VALUE
                        )
                    )
                } else {
                    if (range.contains(VS)) {
                        if (range.startsWith(VS)) {
                            //[~xxx] 的格式
                            list.add(VersionRange(Int.MIN_VALUE, min))
                        } else {
                            //[x~] [x] 的格式
                            list.add(VersionRange(min, Int.MAX_VALUE))
                        }
                    } else {
                        //不包含 ~
                        list.add(VersionRange(min, min))
                    }
                }
            }
        }
        return list
    }

    /**当前的版本[version]适配满足配置的规则[min~max]
     * [version] 当前的版本 比如:678
     * [config] 版本配置 比如:xxx~xxx ~xxx xxx~
     * */
    fun matches(version: Int, config: String?, def: Boolean = true): Boolean {
        val versionRangeList = parseRange(config)
        if (versionRangeList.isEmpty()) {
            //无规则, 则通过
            return def
        }

        var targetRange: VersionRange? = null //匹配到的固件版本范围
        for (range in versionRangeList) {
            if (version in range.min..range.max) {
                targetRange = range
                break
            }
        }
        if (targetRange != null) {
            if (version in (targetRange.min..targetRange.max)) {
                return true
            }
        }
        return false
    }

    /**版本规则数据结构[min~max]*/
    data class VersionRange(val min: Int, val max: Int)
}