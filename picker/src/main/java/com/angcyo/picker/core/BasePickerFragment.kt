package com.angcyo.picker.core

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import com.angcyo.base.back
import com.angcyo.core.fragment.BaseDslFragment
import com.angcyo.dsladapter.DslAdapterStatusItem
import com.angcyo.library.ex.Anim
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.getColor
import com.angcyo.library.ex.size
import com.angcyo.library.model.LoaderMedia
import com.angcyo.library.model.isImage
import com.angcyo.loader.LoaderConfig
import com.angcyo.picker.R
import com.angcyo.picker.dslitem.DslPickerStatusItem
import com.angcyo.transition.dslTransition
import com.angcyo.viewmodel.VMAProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/18
 */
abstract class BasePickerFragment : BaseDslFragment() {

    /**数据共享*/
    val pickerViewModel: PickerViewModel by VMAProperty(PickerViewModel::class.java)

    //Loader配置
    val loaderConfig: LoaderConfig? get() = pickerViewModel.loaderConfig.value

    init {
        fragmentConfig.apply {
            fragmentBackgroundDrawable = ColorDrawable(getColor(R.color.picker_fragment_bg_color))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //监听原图选择
        pickerViewModel.selectorOrigin.observe {
            _vh.cb(R.id.origin_cb)?.isChecked = it ?: false
        }

        /*选中改变*/
        pickerViewModel.selectorMediaList.observe {
            onSelectorMediaListChange(it)
        }

        //样式调整
        _adapter.dslAdapterStatusItem = DslPickerStatusItem()
        _adapter.render {
            setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING)
        }

        _vh.visible(R.id.origin_cb, loaderConfig?.enableOrigin == true)

        _vh.click(R.id.picker_close_view) {
            //关闭
            back()
        }
        _vh.click(R.id.send_button) {
            //发送选择
            PickerActivity.send(this)
        }
        _vh.click(R.id.origin_cb) {
            _showOriginSize()
        }
    }

    /**显示原图大小*/
    fun _showOriginSize() {

        //只计算所有图片的大小,排除音视频
        val size = pickerViewModel.selectorMediaList.value?.sumOf {
            if (it.isImage()) it.fileSize else 0L
        } ?: 0L

        if (_vh.isChecked(R.id.origin_cb) && size > 0) {
            _vh.tv(R.id.origin_cb)?.text =
                getString(R.string.picker_origin_format, size.toLong().fileSizeString())
        } else {
            _vh.tv(R.id.origin_cb)?.text = getString(R.string.picker_origin)
        }

        //原图
        pickerViewModel.selectorOrigin.value = _vh.isChecked(R.id.origin_cb)
    }

    override fun onFragmentShow(bundle: Bundle?) {
        super.onFragmentShow(bundle)
        _showOriginSize()
    }

    open fun onSelectorMediaListChange(mediaList: MutableList<LoaderMedia>?) {

        if (mediaList.isNullOrEmpty()) {
            _vh.enable(R.id.preview_text_view, false)
            _vh.tv(R.id.preview_text_view)?.text = getString(R.string.picker_preview)

            dslTransition(_vh.group(R.id.title_wrap_layout)) {
                transitionDuration = Anim.ANIM_DURATION
                onCaptureEndValues = { _ ->
                    _vh.enable(R.id.send_button, false)
                    _vh.tv(R.id.send_button)?.text = getString(R.string.picker_send)
                }
            }
        } else {
            _vh.enable(R.id.preview_text_view)
            _vh.tv(R.id.preview_text_view)?.text =
                getString(R.string.picker_preview_format, mediaList.size())

            dslTransition(_vh.group(R.id.title_wrap_layout)) {
                transitionDuration = Anim.ANIM_DURATION
                onCaptureEndValues = { _ ->
                    _vh.enable(R.id.send_button)
                    _vh.tv(R.id.send_button)?.text = getString(
                        R.string.picker_send_format,
                        "${mediaList.size}/${loaderConfig?.maxSelectorLimit ?: -1}"
                    )
                }
            }
        }
        _showOriginSize()
    }
}