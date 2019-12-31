package com.angcyo.core.fragment

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 */

object BaseUI {

    /**[BaseTitleFragment.onCreate]中触发*/
    var onFragmentCreateAfter: (fragment: BaseTitleFragment, viewResConfig: ViewResConfig) -> Unit =
        { fragment, viewResConfig ->

        }

    /**[BaseTitleFragment.initBaseView]中触发*/
    var onFragmentInitBaseViewAfter: (fragment: BaseTitleFragment) -> Unit = {

    }
}