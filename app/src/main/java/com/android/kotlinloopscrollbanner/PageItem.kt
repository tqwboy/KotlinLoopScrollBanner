package com.android.kotlinloopscrollbanner

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

/**
 * Created by Hohenheim on 2018/1/16.
 */
class PageItem : Fragment() {
    private var mColorId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mColorId = arguments!!.getInt(hashCode().toString())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_page_item, container, false)
        val imageView = view.findViewById(R.id.test_image_view) as ImageView
        imageView.setBackgroundColor(ContextCompat.getColor(context!!, mColorId))
        return view
    }
}