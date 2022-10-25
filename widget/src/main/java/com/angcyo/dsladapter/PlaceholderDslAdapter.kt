package com.angcyo.dsladapter

import com.angcyo.library.L
import com.angcyo.library.annotation.Implementation

/**
 * 占位的[DslAdapter]适配器
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/25
 */
@Implementation
class PlaceholderDslAdapter : DslAdapter() {
    init {
        L.e("注意:访问目标[_adapter]不存在!")
    }
}