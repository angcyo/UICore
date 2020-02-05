package com.angcyo.loader

import com.angcyo.library.L
import java.io.File

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */
open class FolderCreator {
    companion object {
        const val ALL_IMAGE_AND_VIDEO = "allImageAndVideo"
        const val ALL_IMAGE = "allImage"
        const val ALL_VIDEO = "allVideo"
        const val ALL_AUDIO = "allAudio"
    }

    open fun creatorFolder(config: LoaderConfig, allMedia: List<LoaderMedia>): List<LoaderFolder> {
        val result = mutableListOf<LoaderFolder>()
        var allImage: LoaderFolder? = null
        var allVideo: LoaderFolder? = null
        var allAudio: LoaderFolder? = null
        var allImageAndVideo: LoaderFolder? = null

        val mediaLoaderType = config.mediaLoaderType
        if (mediaLoaderType and LoaderConfig.LOADER_TYPE_IMAGE == LoaderConfig.LOADER_TYPE_IMAGE) {
            if (mediaLoaderType and LoaderConfig.LOADER_TYPE_VIDEO == LoaderConfig.LOADER_TYPE_VIDEO) {
                allImageAndVideo = LoaderFolder("图片和视频", ALL_IMAGE_AND_VIDEO)
                result.add(allImageAndVideo)
            }

            allImage = LoaderFolder("所有图片", ALL_IMAGE)
            result.add(allImage)
        }

        if (mediaLoaderType and LoaderConfig.LOADER_TYPE_VIDEO == LoaderConfig.LOADER_TYPE_VIDEO) {
            allVideo = LoaderFolder("所有视频", ALL_VIDEO)
            result.add(allVideo)
        }

        if (mediaLoaderType and LoaderConfig.LOADER_TYPE_AUDIO == LoaderConfig.LOADER_TYPE_AUDIO) {
            allAudio = LoaderFolder("所有音频", ALL_AUDIO)
            result.add(allAudio)
        }

        loop@ for (media in allMedia) {
            try {
                val file = File(media.localPath!!)

                if (!file.exists()) {
                    continue
                }

                when {
                    media.isImage() -> {
                        allImage?.mediaItemList?.add(media)
                        allImageAndVideo?.mediaItemList?.add(media)
                    }
                    media.isVideo() -> {
                        if (media.duration <= 0) {
                            continue@loop
                        }

                        allImageAndVideo?.mediaItemList?.add(media)
                        allVideo?.mediaItemList?.add(media)
                    }
                    media.isAudio() -> {
                        if (media.duration <= 0) {
                            continue@loop
                        }

                        allAudio?.mediaItemList?.add(media)
                    }
                }

                val folderPath = file.parentFile?.absolutePath
                val folderName = file.parentFile?.name

                val folder =
                    result.find { it.folderName == folderName } ?: LoaderFolder(
                        folderName,
                        folderPath
                    ).apply {
                        result.add(this)
                    }
                folder.mediaItemList.add(media)
            } catch (e: Exception) {
                L.w(e)
            }
        }

        allAudio?.run {
            if (mediaCount() <= 0) {
                result.remove(this)
            }
        }

        allVideo?.run {
            if (mediaCount() <= 0) {
                result.remove(this)
            }
        }

        allImage?.run {
            if (mediaCount() <= 0) {
                result.remove(this)
            }
        }

        allImageAndVideo?.run {
            if (mediaCount() <= 0) {
                result.remove(this)
            }
        }

        return result
    }
}