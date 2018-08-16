package com.yeagle.library.http

import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.exceptions.CompositeException
import io.reactivex.functions.Function
import org.apache.http.conn.ConnectTimeoutException
import java.io.InterruptedIOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

/**
 * Created by yeagle on 2018/8/7.
 */
class RequestRetryHandler : Function<Observable<Throwable>, ObservableSource<*>>{
    private val TAG: String = this.javaClass.simpleName
    private val maxRetries: Int
    private val retryDelayMs: Long

    private var retryCount: Int? = 0

//    set(value) {retryCount = value}

    public constructor(maxRetries: Int, retryDelayMs: Long) {
        this.maxRetries = maxRetries
        this.retryDelayMs = retryDelayMs
    }

    /**
     * 这个函数写了好久啊 Java那边有提示 这边语法不熟一脸懵逼
     */
    override fun apply(throwableObservable: Observable<Throwable>): Observable<Long>? {
        return throwableObservable.flatMap { throwable ->
            var t  = throwable
            if (t is CompositeException) { // 这里都是通过CompositeException封装的
                val list = t.exceptions
                if (list != null && list.size > 0) {
                    t = list[0]
                }
            }
            if (t is InterruptedIOException ||
                    throwable is UnknownHostException ||
                    throwable is ConnectTimeoutException ||
                    throwable is SSLException ||
                    retryCount!! > maxRetries) {
                Log.e(TAG, "throwable" + t.javaClass.simpleName)
                Observable.error<Long>(t) //@throwable Observable.flatMap
            }
            //                        if (++retryCount <= maxRetries) {
            // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
            Log.d(TAG, "Observable get error, it will try after " + retryDelayMs
                    + " ms, retry count " + retryCount)
            Observable.timer(retryDelayMs, TimeUnit.MILLISECONDS)
        }
    }
}


