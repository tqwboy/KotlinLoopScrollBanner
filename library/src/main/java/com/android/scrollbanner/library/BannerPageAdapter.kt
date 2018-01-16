package com.android.scrollbanner.library

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Created by Hohenheim on 2018/1/12.
 */
class BannerPageAdapter(fm: FragmentManager, itemCallback: BannerItemCallback)
    : FragmentStatePagerAdapter(fm) {

    private var mItemCallback: BannerItemCallback? = itemCallback
    private var mItemCount: Int = 0
    private var mRealItemCount: Int = 0
    private var mIsLoop: Boolean = false

    override fun getItem(position: Int): Fragment? {
        val dataPosition: Int
        if(mItemCount>1 && mIsLoop) {
            dataPosition = when(position) {
                0 -> mRealItemCount - 1
                mItemCount - 1 -> 0
                else -> position - 1
            }
        }
        else {
            dataPosition = position
        }

        return mItemCallback?.getFragment(position, dataPosition)
    }

    override fun getCount(): Int {
        return mItemCount
    }

    override fun getItemPosition(item: Any): Int {
        return POSITION_NONE
    }

    /**
     * 设置显示页面的数量
     *
     * @param itemCount 要显示的页面的数量
     * @param isLoop 是否要循环显示
     */
    fun setItemCount(itemCount: Int, isLoop: Boolean) {
        mIsLoop = isLoop
        mRealItemCount = itemCount
        mItemCount = mRealItemCount

        if (mIsLoop && mRealItemCount>1)
            mItemCount += 2

        notifyDataSetChanged()
    }

    fun isLoop(): Boolean {
        return mIsLoop
    }

    fun getRealPosition(position: Int): Int {
        var realPosition = position

        if (mItemCount>1 && mIsLoop) {
            if(position == 0)
                realPosition = mItemCount - 2
            else if (position == mItemCount - 1)
                realPosition = 1
        }

        return realPosition
    }
}