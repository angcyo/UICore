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

    /**多个范围用空格隔开*/
    internal const val RS = " "

    /**min和max用波浪号隔开*/
    internal const val VS = "~"

    /**解析范围, 格式 [ x x~ ~x xxx~xxx xxx~xxx]*/
    fun parseRange(config: String?): List<VersionRange> {
        val rangeStringList = config?.split(RS)
        val list = mutableListOf<VersionRange>()
        rangeStringList?.forEach { range ->
            if (range == "*") {
                //适配*
                list.add(VersionRange(Long.MIN_VALUE, Long.MAX_VALUE))
            } else {
                val rangeString = range.split(VS)
                rangeString.firstOrNull()?.toLongOrNull()?.let { min ->
                    if (rangeString.size >= 2) {
                        list.add(
                            VersionRange(
                                min,
                                rangeString.getOrNull(1)?.toLongOrNull() ?: Long.MAX_VALUE
                            )
                        )
                    } else {
                        if (range.contains(VS)) {
                            if (range.startsWith(VS)) {
                                //[~xxx] 的格式
                                list.add(VersionRange(Long.MIN_VALUE, min))
                            } else {
                                //[x~] [x] 的格式
                                list.add(VersionRange(min, Long.MAX_VALUE))
                            }
                        } else {
                            //不包含 ~
                            list.add(VersionRange(min, min))
                        }
                    }
                }
            }
        }
        return list
    }

    /**[matches]*/
    fun matches(
        version: Int?,
        config: String?,
        defOrNull: Boolean = false,
        defOrEmpty: Boolean = true
    ): Boolean = matches(version?.toLong(), config, defOrNull, defOrEmpty)

    /**当前的版本[version]适配满足配置的规则[min~max]
     * [version] 当前的版本 比如:678
     * [config] 版本配置 比如:xxx~xxx ~xxx xxx~
     *
     * [defOrNull] 默认值, 当版本号[version]未指定时, 或者匹配范围未指定时, 返回的默认值
     * [defOrEmpty] 当匹配范围为空时的默认值
     * */
    fun matches(
        version: Long?,
        config: String?,
        defOrNull: Boolean = false,
        defOrEmpty: Boolean = true
    ): Boolean {
        version ?: return defOrNull
        config ?: return defOrNull

        if (config.isBlank()) {
            //无规则, 则返回默认值
            return defOrEmpty
        }

        val versionRangeList = parseRange(config)
        if (versionRangeList.isEmpty()) {
            //无规则, 则返回默认值
            return defOrEmpty
        }

        return matches(version, versionRangeList)
    }

    /**匹配*/
    fun matches(version: Long?, rangeList: List<VersionRange>): Boolean {
        var targetRange: VersionRange? = null //匹配到的固件版本范围
        for (range in rangeList) {
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
    data class VersionRange(val min: Long, val max: Long)
}