package com.angcyo.media.audio

import android.net.Uri
import com.angcyo.dsladapter.DslAdapterItem

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/22
 */

abstract class DslBaseAudioItem : DslAdapterItem() {
    /**音频地址*/
    var itemAudioUri: Uri? = null
}