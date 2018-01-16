package com.android.scrollbanner.library

import android.support.v4.app.Fragment

/**
 * Created by Hohenheim on 2018/1/12.
 */
interface BannerItemCallback {
    /**
     * 页面位置回调接口
     * @param itemPosition 页面索引
     * @param dataPosition 外部传入的数据的索引
     */
    fun getFragment(itemPosition: Int, dataPosition: Int): Fragment
}