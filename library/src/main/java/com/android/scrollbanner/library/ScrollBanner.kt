package com.android.scrollbanner.library

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Message
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Hohenheim on 2018/1/12.
 */
class ScrollBanner: FrameLayout {
    private var mBannerViewPage: ViewPager? = null
    private var mBannerAdapter: BannerPageAdapter? = null
    private var mIndicatorLayout: LinearLayout? = null
    private var mIndicatorNormalColor: Int = 0
    private var mIndicatorSelectedColor: Int = 0
    private var mIndicatorViews: Array<ImageView?>? = null

    private var mExecuteTime: Long = 5 * 1000 //滚屏间隔时间，默认5秒
    private var mHandler: UiThread? = null

    private var mTimer: Timer? = null
    private var mTimerTask: TimerTask? = null
    private var mScrolling: Boolean = false

    private var mCurrent: Int = 0
    private var mTouchScroll: Boolean = false

    constructor(context: Context): super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr) {

        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val inflater = LayoutInflater.from(context)
        var indicatorInnerLayout = true
        if (null != attrs) {
            val layoutTheme = context.theme
            val attributeArray = layoutTheme.obtainStyledAttributes(attrs, R.styleable.ScrollBanner,
                    defStyleAttr, 0)

            val attributeCount = attributeArray.indexCount
            for (i in 0 until attributeCount) {
                val attr = attributeArray.getIndex(i)
                if (attr == R.styleable.ScrollBanner_indicator_relative)
                    indicatorInnerLayout = attributeArray.getBoolean(i, true)
            }

            attributeArray.recycle()
        }

        //根据布局配置，选择对应的布局
        val rootView: View
        if (indicatorInnerLayout)
            rootView = inflater.inflate(R.layout.layout_relative_loop_scroll_banner, this, true)
        else
            rootView = inflater.inflate(R.layout.layout_linear_loop_scroll_banner, this, true)

        mBannerViewPage = rootView.findViewById(R.id.loop_banner_pager)
        mIndicatorLayout = rootView.findViewById(R.id.indicator_layout)

        mIndicatorNormalColor = ContextCompat.getColor(context, android.R.color.darker_gray)
        mIndicatorSelectedColor = ContextCompat.getColor(context, android.R.color.white)

        //设置页面选择监听事件
        mBannerViewPage?.addOnPageChangeListener(object: SimpleOnPageChangeListener() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //滑动结束后，判断滑到的位置是否为第一个或者是最后一个
                if (positionOffset + positionOffsetPixels == 0f) {
                    val current = mBannerAdapter?.getRealPosition(position) ?: position
                    if (current != position) {
                        //延迟跳转，避免闪烁
                        val msg = mHandler?.obtainMessage() ?: Message()
                        msg.what = 1
                        msg.obj = current
                        mHandler?.sendMessageDelayed(msg, 10)
                    }
                    else {
                        changeIndicator(position)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    //滑动结束
                    ViewPager.SCROLL_STATE_IDLE -> {
                        //如果此次滑动为手指拖动，就在滑动结束后，恢复滚动计时器
                        if (mTouchScroll) {
                            mTouchScroll = false
                            startScroll()
                        }
                    }

                    //手势滑动
                    ViewPager.SCROLL_STATE_DRAGGING -> {
                        mTouchScroll = true //记录此次滑动为手指拖动
                        stopScroll()
                    }
                }
            }

            override fun onPageSelected(position: Int) {
                mCurrent = position
            }
        })

        mHandler = UiThread(this)
    }

    //页面滚动定时器
    private inner class BannerScrollTask : TimerTask() {
        override fun run() {
            mHandler?.sendEmptyMessage(0) ?: stopScroll()
        }
    }
    private class UiThread(banner: ScrollBanner) : Handler() {
        val bannerRef: WeakReference<ScrollBanner?> = WeakReference(banner)

        override fun handleMessage(msg: Message) {
            val banner = bannerRef.get() ?: return

            if (msg.what == 0) {
                val pageCount = banner.mBannerAdapter?.count ?: 0

                if(pageCount > 0) {
                    val current = (banner.mCurrent + 1) % pageCount
                    if (current < pageCount)
                        banner.mBannerViewPage?.setCurrentItem(current, true)
                }
                else {
                    banner.stopScroll()
                }
            }
            else {
                val current = msg.obj as Int
                banner.mBannerViewPage?.setCurrentItem(current, false)
                //banner.changeIndicator(current)
            }
        }
    }

    /**
     * 初始化广告栏
     */
    fun initScroll(fm: FragmentManager, pageItemOptCallback: BannerItemCallback) {
        mBannerAdapter = BannerPageAdapter(fm, pageItemOptCallback)
        mBannerViewPage?.adapter = mBannerAdapter
    }

    /**
     * 设置展示页面数量
     *
     * @param count 要显示的页面的数量
     * @param isLoop 是否要循环显示
     */
    @SuppressLint("InflateParams")
    fun setShowItemCount(count: Int, isLoop: Boolean) {
        if (null != mBannerAdapter) {
            //设置指示标示
            mIndicatorLayout?.removeAllViews()
            if (count <= 1) {
                mIndicatorLayout?.visibility = View.GONE
            }
            else {
                mIndicatorLayout?.visibility = View.VISIBLE
                val inflater = LayoutInflater.from(context)
                mIndicatorViews = arrayOfNulls<ImageView?>(count)

                for (i in 0 until count) {
                    val indicatorView = inflater.inflate(R.layout.layout_circle_page_indicator,
                            null) as ImageView
                    mIndicatorViews!![i] = indicatorView

                    val bgShape = indicatorView.background as GradientDrawable
                    if (i == 0)
                        bgShape.setColor(mIndicatorSelectedColor)
                    else
                        bgShape.setColor(mIndicatorNormalColor)

                    mIndicatorLayout?.addView(indicatorView)
                }
            }

            //设置要显示的内容的数量
            mBannerAdapter?.setItemCount(count, isLoop)
            if (count > 1 && isLoop)
                mBannerViewPage?.currentItem = 1
        }
    }

    //变更页码指示器的颜色
    private fun changeIndicator(position: Int) {
        if(null == mIndicatorViews)
            return

        var indicatorPosition = position
        if ((mBannerAdapter?.count ?: 0) > 1 && (mBannerAdapter?.isLoop()==true)) {
            if (position <= 0 || position == mBannerAdapter!!.count - 2)
                indicatorPosition = mBannerAdapter!!.count - 2 - 1
            else if (position >= mBannerAdapter!!.count - 1 || position == 1)
                indicatorPosition = 0
            else
                indicatorPosition = position - 1
        }

        for (i in mIndicatorViews!!.indices) {
            val indicatorIv = mIndicatorViews!![i]
            val bgShape = indicatorIv!!.background as GradientDrawable
            if (i == indicatorPosition)
                bgShape.setColor(mIndicatorSelectedColor)
            else
                bgShape.setColor(mIndicatorNormalColor)
        }
    }

    /**
     * 设置滚动时间，单位：毫秒
     */
    fun setScrollTime(scrollTime: Long) {
        mExecuteTime = scrollTime

        if (mScrolling) {
            stopScroll()
            startScroll()
        }
    }

    /**
     * 启动滚动，滚动间隔时间为已设置好的时间，如果没设置，就用默认时间，默认为5000毫秒
     * @return 如果正在滚动状态、没有初始化、页面数量小于2，就不启动滚动，返回false
     */
    fun startScroll(): Boolean {
        var startSuccess = false

        val pagecount = mBannerAdapter?.count ?:0
        if (!mScrolling && pagecount>1) {
            mScrolling = true
            startSuccess = true

            mTimerTask = BannerScrollTask()
            mTimer = Timer()
            mTimer!!.schedule(mTimerTask, mExecuteTime, mExecuteTime)
        }

        return startSuccess
    }

    /**
     * 启动滚动，并设置滚动间隔时间，设置的间隔时间小于1000毫秒，就放弃设置，使用原时间
     * @return 启动自动滚动成功：true；启动自动滚动失败：false
     */
    fun startScroll(executeTime: Long): Boolean {
        if (executeTime >= 1000)
            setScrollTime(executeTime)

        return startScroll()
    }

    /**
     * 终止滚动
     */
    fun stopScroll() {
        if (mScrolling) {
            mScrolling = false
            mTimerTask?.cancel()
            mTimer?.cancel()
        }
    }

    /**
     * 设置指示器颜色
     *
     * @param normalColor 未选中该页时，显示的颜色
     * @param selectedColor 选中该页时，显示的颜色
     */
    fun setIndicatorColor(normalColor: Int, selectedColor: Int) {
        mIndicatorNormalColor = normalColor
        mIndicatorSelectedColor = selectedColor
        changeIndicator(mCurrent)
    }

    fun isScrolling(): Boolean {
        return mScrolling
    }
}