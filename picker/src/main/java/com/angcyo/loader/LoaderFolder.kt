package com.angcyo.loader

import com.angcyo.library.model.LoaderMedia

/**
 * 加载后的媒体信息, 都分组在相同的文件夹中
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

data class LoaderFolder(
    /**文件夹名*/
    var folderName: String? = "",
    /**文件夹路径
     * [com.angcyo.loader.FolderCreator.ALL_IMAGE]
     * [com.angcyo.loader.FolderCreator.ALL_VIDEO]
     * [com.angcyo.loader.FolderCreator.ALL_AUDIO]
     * [com.angcyo.loader.FolderCreator.ALL_IMAGE_AND_VIDEO]
     * */
    var folderPath: String? = "",
    /**所有媒体*/
    var mediaItemList: MutableList<LoaderMedia> = mutableListOf()
)

/**媒体数量*/
fun LoaderFolder.mediaCount() = mediaItemList.size