package com.android.kotlinloopscrollbanner

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import com.android.scrollbanner.library.BannerItemCallback
import com.android.scrollbanner.library.ScrollBanner

class MainActivity : AppCompatActivity() {
    private var mBanner: ScrollBanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        mBanner = findViewById(R.id.loop_banner)
        mBanner?.initScroll(supportFragmentManager, object : BannerItemCallback {
            override fun getFragment(itemPosition: Int, dataPosition: Int): Fragment {
                /*
                 * 创建展示内容的Fragment，如果是循环轮播，整个Banner的页面数量会比设置的多2个
                 * 所以，在循环轮播的情况下，第0页和倒数第2页的内容应该设置为一样
                 * 同理，第1页和最后一页的内容应该设置为一样
                 */
                val pageFragment = PageItem()

                val bundle = Bundle()
                val colorId: Int = when (dataPosition) {
                    0 -> android.R.color.holo_red_light

                    1 -> android.R.color.black

                    else -> android.R.color.holo_green_light
                }

                bundle.putInt(Integer.toString(pageFragment.hashCode()), colorId)
                pageFragment.arguments = bundle

                return pageFragment
            }
        })

        mBanner?.setShowItemCount(3, true)
        mBanner?.startScroll()

        //设置指示器颜色
        mBanner?.setIndicatorColor(ContextCompat.getColor(applicationContext,
                android.R.color.holo_blue_light),
                ContextCompat.getColor(applicationContext, android.R.color.holo_orange_dark))
    }
}