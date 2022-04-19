package com.angcyo.pager

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.base.dslFHelper
import com.angcyo.base.interceptTouchEvent
import com.angcyo.library.ex.resetAll
import com.angcyo.library.model.LoaderMedia

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/16
 */

/**[Fragment]中, 快速启动[Pager]大图视频浏览界面*/
fun Fragment.dslPager(
    fragment: Class<out ViewTransitionFragment> = PagerTransitionFragment::class.java,
    action: PagerTransitionCallback.() -> Unit
) {
    //禁止touch事件
    activity?.interceptTouchEvent()
    dslFHelper {
        noAnim()
        show<ViewTransitionFragment>(fragment) {
            transitionCallback = PagerTransitionCallback().apply {
                action()
            }
        }
    }
}

/**
 * 单图预览:
 * ```
 *  dslPager {
 *      fromView = it
 *      addMedia(codeSavePath)
 *  }
 * ```
 * */

/**单图预览*/
fun Fragment.dslSinglePager(
    fromView: View?,
    path: String?,
    action: PagerTransitionCallback.() -> Unit = {}
) {
    if (path.isNullOrEmpty()) {
        return
    }
    dslPager {
        this.fromView = fromView
        addMedia(path)
        action()
    }
}

fun Fragment.dslSinglePager(
    fromView: View?,
    uri: Uri?,
    action: PagerTransitionCallback.() -> Unit = {}
) {
    if (uri == null) {
        return
    }
    dslPager {
        this.fromView = fromView
        addMedia(uri)
        action()
    }
}

fun Fragment.dslSinglePager(
    fromView: View?,
    bitmap: Bitmap?,
    action: PagerTransitionCallback.() -> Unit = {}
) {
    if (bitmap == null) {
        return
    }
    dslPager {
        this.fromView = fromView
        addMedia(bitmap)
        action()
    }
}

/**
 * 结合RecyclerView预览:
 * ```
 *  dslPager {
 *      fromRecyclerView = recyclerView
 *      startPosition = itemIndexPosition()
 *      loaderMediaList = itemMediaList
 *  }
 * ```
 * */

/**列表预览*/
fun Fragment.dslRecyclerPager(
    recyclerView: RecyclerView?,
    startPosition: Int,
    mediaList: List<LoaderMedia>,
    action: PagerTransitionCallback.() -> Unit = {}
) {
    if (mediaList.isNullOrEmpty()) {
        return
    }
    dslPager {
        fromRecyclerView = recyclerView
        this.startPosition = startPosition
        loaderMediaList.resetAll(mediaList)
        action()
    }
}
