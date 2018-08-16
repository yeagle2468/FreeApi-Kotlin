package com.yeagle.library.mvp

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trello.rxlifecycle2.LifecycleProvider
import com.trello.rxlifecycle2.LifecycleTransformer
import com.yeagle.library.http.RequestRetryHandler
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber
import org.greenrobot.eventbus.EventBus

/**
 * Created by yeagle on 2018/8/7.
 */
abstract class BasePresenter<V : IView> : IPresenter<V>{
    public val TAG : String = this.javaClass.simpleName
    protected var mView : V? = null
        set(value) {mView = value}

    /**
     * 相当于静态的写法
     */
    companion object {
        var mGson: Gson = Gson()
        public val retryHandler = RequestRetryHandler(3, 200)
    }

    /**
     * 赋值要先提供set方法，还有这个操作
     */
    override fun takeView(view : V) {
        mView = view
    }

    public fun useEventBus() : Boolean {
        return false
    }

    private fun convertViewToLifeCycle() : LifecycleProvider<*> {
        if (mView == null || mView !is LifecycleProvider<*>) {
            if (mView == null)
                throw NullPointerException("mView is null NullPointerException")
            else
                throw IllegalArgumentException("View is not implements LifecycleProvider")
        }

        return mView as LifecycleProvider<*> // 这样强转
    }

    protected fun lifecycle() : Observable<*> {
        return convertViewToLifeCycle().lifecycle()
    }

    protected fun <T> bindUntilEvent(event: Nothing) : LifecycleTransformer<T> {
        return convertViewToLifeCycle().bindUntilEvent(event)
    }

    protected fun getRetryDefaultHandler(): RequestRetryHandler {
        return retryHandler
    }

    protected fun doRequest(observable: Observable<*>, subscriber: ErrorHandleSubscriber<Any>, token: TypeToken<*>) {
        doRequest(observable, null, subscriber, token)
    }

    protected fun doRequest(observable: Observable<*>, onFinally: Action?, subscriber: ErrorHandleSubscriber<Any>, token: TypeToken<*>) {
        doRequest(observable, false, onFinally, subscriber, token)
    }

    protected fun doRequest(observable: Observable<*>, retry: Boolean, onFinally: Action?, subscriber: ErrorHandleSubscriber<Any>, token: TypeToken<*>) {
        var handler: RequestRetryHandler? = if (retry) getRetryDefaultHandler() else null
        observable.map {
            obj ->
                    if (token == null)
                        obj
                    else
                        parseBean(convertToString(obj), token)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(getLifecycleTransformer())
                .subscribe(subscriber)

        if (handler != null)
            observable.retryWhen(handler)

        if (onFinally != null)
            observable.doFinally(onFinally)
    }

    public fun <T> bindToLifecycle(): LifecycleTransformer<T> {
        return convertViewToLifeCycle().bindToLifecycle()
    }

    protected fun <T> getLifecycleTransformer(): LifecycleTransformer<T> {
        return bindToLifecycle()
    }

    protected fun <T> parseBean(string: String, token: TypeToken<*> ): T {
        return mGson.fromJson<T>(string, token.type)
    }

    protected fun convertToString(any: Any) : String {
        return any.toString()
    }

    override fun start() {
        if (useEventBus())
            EventBus.getDefault().register(this)
    }

    protected fun dropView() {
        mView = null
    }

    override fun destroy() {
        if (useEventBus())
            EventBus.getDefault().unregister(this)
    }
}