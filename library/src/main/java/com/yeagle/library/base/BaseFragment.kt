package com.yeagle.library.base

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import com.trello.rxlifecycle2.LifecycleProvider
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle
import com.trello.rxlifecycle2.android.FragmentEvent
import com.trello.rxlifecycle2.android.RxLifecycleAndroid
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by yeagle on 2018/8/13.
 */
public abstract class BaseFragment : Fragment(), LifecycleProvider<FragmentEvent>, HasSupportFragmentInjector{
    private val lifecycleSubject : BehaviorSubject<FragmentEvent> = BehaviorSubject.create()
    protected val TAG : String = this.javaClass.simpleName

    protected var mRootView : View? = null
    protected var mUnbinder : Unbinder? = null

    @Inject
    lateinit var childFragmentInjctor: DispatchingAndroidInjector<Fragment>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
        if (mRootView == null) {
            var rootView: View = inflater.inflate(getLayoutId(), null)
            mRootView = rootView
        } else if (mRootView!!.parent != null) {
            (mRootView!!.parent as ViewGroup).removeView(mRootView)
        }

        mUnbinder = ButterKnife.bind(this, mRootView!!)
        initViews()

        return mRootView
    }

    override fun lifecycle(): Observable<FragmentEvent> {
        return lifecycleSubject.hide()
    }

    override fun <T : Any?> bindUntilEvent(event: FragmentEvent): LifecycleTransformer<T> {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event)
    }

    override fun <T : Any?> bindToLifecycle(): LifecycleTransformer<T> {
        return RxLifecycleAndroid.bindFragment(lifecycleSubject)
    }

    override fun onAttach(context: Context?) {
        if (useInject())
            AndroidSupportInjection.inject(this)

        super.onAttach(context)
        lifecycleSubject.onNext(FragmentEvent.ATTACH)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return childFragmentInjctor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleSubject.onNext(FragmentEvent.CREATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleSubject.onNext(FragmentEvent.CREATE_VIEW)
    }

    override fun onStart() {
        super.onStart()
        lifecycleSubject.onNext(FragmentEvent.START)
    }

    override fun onPause() {
        lifecycleSubject.onNext(FragmentEvent.PAUSE)
        super.onPause()
    }

    override fun onStop() {
        lifecycleSubject.onNext(FragmentEvent.STOP)
        super.onStop()
    }

    override fun onDestroyView() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY_VIEW)
        super.onDestroyView()
    }

    override fun onDetach() {
        lifecycleSubject.onNext(FragmentEvent.DETACH)
        super.onDetach()
    }

    override fun onResume() {
        super.onResume()
        lifecycleSubject.onNext(FragmentEvent.RESUME)
    }

    override fun onDestroy() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY)
        super.onDestroy()

        if (mUnbinder != null)
            mUnbinder!!.unbind()

        mUnbinder = null
    }

    public fun useInject(): Boolean {
        return true
    }

    protected fun initViews() {

    }

    public abstract fun getLayoutId(): Int
}