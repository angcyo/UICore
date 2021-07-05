package com.angcyo.core.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.angcyo.base.dslFHelper
import com.angcyo.core.R
import com.angcyo.core.activity.PermissionBean
import com.angcyo.dsladapter.renderItem
import com.angcyo.library.ex.isDebugType
import com.angcyo.library.getAppName
import com.angcyo.tablayout.screenWidth
import com.angcyo.widget.recycler.dslAdapter


/**
 * 权限申请界面
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/05/29
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class PermissionFragment : BaseFragment() {

    companion object {
        var permissionGridStrategy: (context: Context, count: Int) -> Int = { context, count ->
            when {
                context.screenWidth <= 640 && count % 2 == 0 -> 2
                context.screenWidth <= 640 && count >= 4 -> 2
                context.screenWidth <= 640 -> 1
                count > 4 -> 3
                count == 4 -> 2
                count <= 3 -> 1
                else -> 2
            }
        }
    }

    init {
        fragmentLayoutId = R.layout.lib_permission_fragment
    }

    //需要的权限列表
    val permissions = mutableListOf<PermissionBean>()

    //触发权限请求
    var onPermissionRequest: (View, List<PermissionBean>) -> Unit = { _, _ -> }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        baseViewHolder?.tv(R.id.permission_title)?.text =
            "\n为了更好的服务体验,\n${getAppName()} 需要以下权限"

        baseViewHolder?.rv(R.id.recycler_view)?.apply {
            val filterPermissions = permissions.filter { it.icon > 0 }

            val count = filterPermissions.size

            layoutManager = GridLayoutManager(
                fContext(), permissionGridStrategy(fContext(), count)
            )

            dslAdapter {
                for (i in 0 until count) {
                    renderItem {
                        itemLayoutId = R.layout.dsl_item_permission

                        itemBindOverride = { itemHolder, itemPosition, _, _ ->
                            if (filterPermissions[itemPosition].icon > 0) {
                                itemHolder.img(R.id.lib_image_view)
                                    ?.setImageResource(filterPermissions[itemPosition].icon)
                            }
                            itemHolder.tv(R.id.lib_text_view)?.text =
                                filterPermissions[itemPosition].des
                        }
                    }
                }
            }
        }

        baseViewHolder?.throttleClick(R.id.enable_button) {
            onPermissionRequest(it, permissions)
        }

        if (isDebugType()) {
            baseViewHolder?.longClick(R.id.enable_button) {
                dslFHelper {
                    removeLastFragmentOnBack = true
                    finishActivityOnLastFragmentRemove = true
                    noAnim()
                    remove(this@PermissionFragment)
                }
            }
        }
    }

    override fun canSwipeBack(): Boolean {
        return false
    }

    override fun onBackPressed(): Boolean {
        if (isDebugType()) {
            return true
        }
        return false
    }
}