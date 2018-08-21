package com.yeagle.library.mvp

import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Created by yeagle on 2018/8/7.
 */
interface PageContract {
    interface View : IView {
        fun onData(data : List<Any>, refresh: Boolean, path: String)
        fun onComplete(path: String)
    }

    interface Prestener : IPresenter<View> {
        fun loadData(path: String, fresh: Boolean, token: TypeToken<Any>)
        fun loadData(path: String, fresh: Boolean, extraValue: Any, token: TypeToken<Any>)
    }
}