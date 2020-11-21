package com.angcyo.activity

import android.os.Bundle

/**
 * 透明[Activity], 需要使用主题[@style/BaseTranAppTheme]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/09
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
abstract class BaseTransparentActivity : BaseAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
    }
}