package com.angcyo.core.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.angcyo.core.R
import com.angcyo.core.activity.PermissionBean
import com.angcyo.dsladapter.renderItem
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

    init {
        fragmentLayoutId = R.layout.lib_permission_fragment
    }

    //需要的权限列表
    val permissions = mutableListOf<PermissionBean>()

    //触发权限请求
    var onPermissionRequest: (View, List<PermissionBean>) -> Unit = { _, _ -> }

    override fun initBaseView(savedInstanceState: Bundle?) {
        super.initBaseView(savedInstanceState)

        baseViewHolder.tv(R.id.permission_title)?.text =
            "\n为了更好的服务体验,\n${getAppName()} 需要以下权限"

        baseViewHolder.rv(R.id.recycler_view)?.apply {
            val filterPermissions = permissions.filter { it.icon > 0 }

            val count = filterPermissions.size

            layoutManager = GridLayoutManager(
                fContext(), when {
                    screenWidth <= 640 && count % 2 == 0 -> 2
                    screenWidth <= 640 && count >= 4 -> 2
                    screenWidth <= 640 -> 1
                    count > 4 -> 3
                    count == 4 -> 2
                    count <= 3 -> 1
                    else -> 2
                }
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

        baseViewHolder.click(R.id.enable_button) {
            onPermissionRequest(it, permissions)
        }
    }

    override fun canSwipeBack(): Boolean {
        return false
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}