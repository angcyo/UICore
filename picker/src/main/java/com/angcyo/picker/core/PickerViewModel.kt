package com.angcyo.picker.core

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.angcyo.library.model.LoaderMedia
import com.angcyo.loader.LoaderConfig
import com.angcyo.loader.LoaderFolder
import com.angcyo.loader.canSelectorMedia
import com.angcyo.loader.isSingleModel

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/31
 */
class PickerViewModel : ViewModel() {

    /**配置信息*/
    val loaderConfig = MutableLiveData<LoaderConfig>()

    /**选中的媒体*/
    val selectorMediaList = MutableLiveData<MutableList<LoaderMedia>>(mutableListOf())

    /**所有媒体*/
    val loaderFolderList = MutableLiveData<List<LoaderFolder>>()

    /**当前显示的文件夹*/
    val currentFolder = MutableLiveData<LoaderFolder>()

    /**选择原图*/
    val selectorOrigin = MutableLiveData<Boolean>(false)

    //录像单独保存的视频
    val takeVideoList = mutableListOf<LoaderMedia>()

    //拍照单独保存的图片
    val takeImageList = mutableListOf<LoaderMedia>()

    /**添加选中项*/
    fun addSelectedMedia(media: LoaderMedia?) {
        media?.run {
            selectorMediaList.value?.add(this)
            selectorMediaList.value = selectorMediaList.value
        }
    }

    /**移除所有选中*/
    fun removeSelectedAll() {
        selectorMediaList.value?.clear()
        selectorMediaList.value = selectorMediaList.value
    }

    /**移除选中项*/
    fun removeSelectedMedia(media: LoaderMedia?) {
        media?.run {
            selectorMediaList.value?.remove(this)
            selectorMediaList.value = selectorMediaList.value
        }
    }

    /**是否可以选中*/
    fun canSelectorMedia(media: LoaderMedia?): Boolean {
        if (media == null) {
            return false
        }
        if (selectorMediaList.value.isNullOrEmpty()) {
            return true
        }
        if (loaderConfig.value?.canSelectorMedia(selectorMediaList.value!!, media) == true) {
            return true
        }
        return false
    }

    /**是否是单选模式*/
    fun isSingleModel() = loaderConfig.value?.isSingleModel() == true
}