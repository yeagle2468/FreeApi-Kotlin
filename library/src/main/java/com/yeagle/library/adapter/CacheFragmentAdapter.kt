package com.yeagle.library.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.View
import java.lang.ref.WeakReference

/**
 * Created by yeagle on 2018/8/16.
 */
class CacheFragmentAdapter<T : Fragment> : FragmentStatePagerAdapter {
    private val mBundles: List<Bundle>
    private val mClazz: Class<*>
    private val mCacheFragments : ArrayList<WeakReference<T>> = ArrayList()

    constructor(fm : FragmentManager, bundles: List<Bundle>, clazz: Class<*>) : super(fm) {
        this.mBundles = bundles
        this.mClazz = clazz
    }

    override fun getItem(p0: Int): Fragment {
        if(mCacheFragments.size > 1) {
            val fragment = mCacheFragments.removeAt(0).get()
            if (fragment != null) {
                val bundle = fragment.arguments
                bundle!!.clear()
                bundle!!.putAll(mBundles.get(p0))

                return fragment
            }
        }

        val bundle = Bundle(mBundles.get(p0))

        val t = mClazz.newInstance() as T
        t.arguments = bundle

        return t
    }

    override fun getCount(): Int {
        return mBundles.size
    }

    override fun destroyItem(container: View, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        mCacheFragments.plus(WeakReference(`object` as T))
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}