package com.yeagle.library.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.View

/**
 * Created by yeagle on 2018/8/15.
 *  全是静态函数的用object 非 class
 */
object ActivityUtils {
    public fun addFragmentToActivity(fragmentManager: FragmentManager, fragment: Fragment,
                                    fragmentId : Int, tag: String) {
        val transition: FragmentTransaction = fragmentManager.beginTransaction();
        transition.add(fragmentId, fragment, tag)
        transition.commit()
    }

    public fun addFragmentToActivity(fragmentManager: FragmentManager,
                                    fragment: Fragment, tag: String) {
        val transition: FragmentTransaction = fragmentManager.beginTransaction();
        transition.add(fragment, tag)
        transition.commit()
    }

    fun replaceFragmentActivity(fragmentManager: FragmentManager,
                                fragment: Fragment, fragmentId : Int) {
        val transition: FragmentTransaction = fragmentManager.beginTransaction();
        transition.replace(fragmentId, fragment)
        transition.commit()
    }

    public fun getActivityFromView(view: View) : Activity? {
        var context: Context = view.context

        while (context != null && context is ContextWrapper) {
            if (context is Activity)
                return context

            context = context.baseContext
        }

        return null
    }
}