package com.angcyo.loader

import com.angcyo.library.model.LoaderMedia

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/30
 */

class DefaultSelectorFilter(val config: LoaderConfig) : SelectorFilter {
    override fun filter(media: LoaderMedia): Boolean {
        return false
    }
}