package com.angcyo.core.dslitem

import androidx.fragment.app.Fragment
import com.angcyo.dsladapter.item.IDslItem

/**
 * 带有[Fragment]的item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/07/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
interface IFragmentItem : IDslItem {

    /**[DslFHelper]*/
    var itemFragment: Fragment?

}