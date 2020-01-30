package com.angcyo.loader

/**
 * 加载后的媒体信息, 都分组在相同的文件夹中
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

class LoaderFolder {
    //文件夹包含哪些媒体类型
    var mediaFolderType = 0 //MediaLoaderConfig.LOADER_TYPE_ALL 默认为0

    //文件夹名
    var folderName = ""
    //文件夹路径
    var folderPath = ""

    var mediaItemList = mutableListOf<LoaderMedia>()

    //媒体数量
    val mediaCount: Int
        get() = mediaItemList.size

    override fun equals(other: Any?): Boolean {
        return if (other is LoaderFolder) {
            folderPath == other.folderPath
        } else {
            false
        }
    }
}