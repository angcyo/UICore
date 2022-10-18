package com.angcyo.core.component.model

import com.angcyo.core.lifecycle.LifecycleViewModel
import com.angcyo.coroutine.withBlock
import com.angcyo.coroutine.withMain
import com.angcyo.http.rx.doMain
import com.angcyo.library.ex.file
import com.angcyo.viewmodel.vmData
import com.angcyo.viewmodel.vmDataOnce

/**
 * 缓存管理
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/17
 */
class CacheModel : LifecycleViewModel() {

    /**缓存信息集合*/
    val cacheInfoListData = vmData(mutableListOf<CacheInfo>())

    /**缓存总大小数据*/
    val cacheSumData = vmData(-1L)

    /**缓存大小计算通知*/
    val cacheSizeOnceData = vmDataOnce<CacheInfo>()

    /**添加一个缓存信息*/
    fun addCacheInfo(info: CacheInfo) {
        cacheInfoListData.apply {
            value?.add(info)
            postValue(value)
        }
    }

    /**开始估算所有缓存的大小*/
    fun computeCacheSize() {
        for (cache in cacheInfoListData.value ?: emptyList()) {
            //清理数据
            computeCacheSize(cache)
        }
    }

    /**计算指定的缓存信息*/
    fun computeCacheSize(cache: CacheInfo) {
        //清理数据
        cache._size = -1
        cache._cacheFileList.clear()
        _computeSumSize()
        doMain {
            cacheSizeOnceData.setValue(cache)
        }
        _computeCacheSize(cache)
    }

    /**清理缓存*/
    fun clearCache(cache: CacheInfo) {
        _clearCache(cache)
    }

    /**计算缓存总大小*/
    fun _computeSumSize() {
        val size = cacheInfoListData.value?.sumOf { it._size } ?: -1L
        cacheSumData.postValue(size)
    }

    /**协程内计算大小*/
    fun _computeCacheSize(cacheInfo: CacheInfo) {
        launchLifecycle {
            withBlock {
                //先枚举所有文件, 再访问文件夹
                cacheInfo.path.file().walkBottomUp().forEach {
                    if (it.isFile) {
                        cacheInfo._cacheFileList.add(it.absolutePath)
                        cacheInfo._size += it.length()
                    } else {
                        if (cacheInfo._size < 0) {
                            cacheInfo._size = 0
                        }
                    }
                    _computeSumSize()
                    withMain {
                        cacheSizeOnceData.setValue(cacheInfo)
                    }
                }
            }
        }
    }

    /**协程内清理缓存*/
    fun _clearCache(cacheInfo: CacheInfo) {
        launchLifecycle {
            withBlock {
                val deleteList = mutableListOf<String>()
                cacheInfo._cacheFileList.forEach {
                    try {
                        val file = it.file()
                        val length = file.length()
                        file.delete()
                        cacheInfo._size -= length
                        deleteList.add(it)
                        withMain {
                            cacheSizeOnceData.setValue(cacheInfo)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                //清空文件, 但是不清空大小, 这样可以从来检查是否有删除失败的文件
                cacheInfo._cacheFileList.removeAll(deleteList)
            }
            _computeSumSize()
            withMain {
                cacheSizeOnceData.setValue(cacheInfo)
            }
        }
    }

}

data class CacheInfo(
    /**缓存标签*/
    val label: CharSequence?,
    /**缓存描述*/
    val des: CharSequence?,
    /**缓存路径, 支持文件和文件夹*/
    val path: String,

    //计算属性
    /**缓存总大小, byte字节*/
    var _size: Long = -1,
    /**参与计算的文件路径*/
    val _cacheFileList: MutableList<String> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CacheInfo

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}