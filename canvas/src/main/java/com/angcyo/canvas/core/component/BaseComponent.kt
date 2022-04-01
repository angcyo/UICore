package com.angcyo.canvas.core.component

import com.angcyo.canvas.core.IComponent

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/01
 */
abstract class BaseComponent : IComponent {

    /**是否激活当前组件*/
    var enable: Boolean = true

}