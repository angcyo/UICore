package com.angcyo.core.component.accessibility.action.a

import com.angcyo.core.component.accessibility.parse.ConstraintBean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/21
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

abstract class BaseConstraintAction : BaseAction() {
    var onGetConstraintList: (() -> List<ConstraintBean>)? = null
}