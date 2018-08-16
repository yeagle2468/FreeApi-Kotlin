package com.yeagle.library.mvp

/**
 * Created by yeagle on 2018/8/7.
 */
interface IPresenter<V : IView> {
    fun start()
    fun takeView(view : V)
    fun destroy()
}