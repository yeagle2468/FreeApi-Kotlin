package com.yeagle.library.adapter

import android.content.Context
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.zhy.adapter.recyclerview.CommonAdapter

/**
 * Created by yeagle on 2018/8/16.
 */
abstract class BaseRcAdapter<T> : CommonAdapter<T> {
    constructor(context: Context, layoutId: Int, datas: List<T>) : super(context, layoutId, datas){}

    public fun addData(data: List<T>?) {
        if (mDatas != null)
            mDatas.addAll(data!!)
        else
            mDatas = data

        notifyDataSetChanged()
    }

    public fun setData(data: List<T>?) {
        mDatas.clear()
        addData(data)
    }

    protected fun createNewOption(defaultResId: Int) : RequestOptions{
        return RequestOptions().dontAnimate().placeholder(defaultResId).priority(Priority.HIGH)
    }
}