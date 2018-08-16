package com.yeagle.library.base

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.trello.rxlifecycle2.LifecycleProvider
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.android.RxLifecycleAndroid
import com.yeagle.library.fragment.FragmentInfo
import com.yeagle.library.utils.ActivityUtils
import com.yeagle.library.utils.PhoneSysUtil
import com.yeagle.library.utils.StatusBarUtil
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

/**
 * Created by yeagle on 2018/8/14.
 */
abstract class BaseActivity : AppCompatActivity(), LifecycleProvider<ActivityEvent>, HasSupportFragmentInjector{
    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    private val lifecycleSubject : BehaviorSubject<ActivityEvent> = BehaviorSubject.create()

    protected var mBackView: View? = null
    protected var mTitle: TextView? = null
    protected var mBackTv: TextView? = null
    protected var mToolbar: Toolbar? = null

    protected var mBinder: Unbinder? = null
    protected var hasSaveInstance: Boolean = false

    private var mResumeFragments = ArrayList<FragmentInfo>() //listOf<FragmentInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (useInject())
            AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        lifecycleSubject.onNext(ActivityEvent.CREATE)

        initStatusBar()
        initLayout()
    }

    private fun initLayout() {
        val layoutResId = getLayoutId()
        if (layoutResId > 0) {
            setContentView(layoutResId)
            mToolbar = findViewById(getToolbarId())
            mBinder = ButterKnife.bind(this)

            initCustomTitleBar()
            initToolBar()
        }
    }

    protected fun initCustomTitleBar() {
        mTitle = findViewById(getTitleId())
        mBackView = findViewById(getBackViewId())
        mBackTv = findViewById(getBackTvId())

        if (mBackView != null) {
            mBackView!!.visibility = getBackViewVisible()
            mBackView!!.setOnClickListener {
                onBackPressed()
            }
        }

        mBackTv!!.visibility = getBackViewVisible()
    }

    protected fun initToolBar() {
        if (mToolbar == null)
            return
        setSupportActionBar(mToolbar)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(false)
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)
        getSupportActionBar()!!.setDisplayShowHomeEnabled(false)
    }

    protected abstract fun getLayoutId():Int
    protected abstract fun getToolbarId():Int
    protected abstract fun getTitleId(): Int
    protected abstract fun getBackTvId(): Int

    protected fun getBackViewId(): Int {
        return 0
    }

    protected fun getBackViewVisible(): Int {
        return View.VISIBLE
    }

    private fun initStatusBar() {
        if (!isImmersiveStatusBar())mBackView = findViewById(getBackViewId());
            return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS )
        }
        if (PhoneSysUtil.isMIUI()!!)
            StatusBarUtil.statusBarLightMode(this)
    }

    protected fun isImmersiveStatusBar() : Boolean {
        return false
    }

    fun useInject(): Boolean {
        return true
    }

    override fun lifecycle(): Observable<ActivityEvent> {
        return lifecycleSubject.hide()
    }

    override fun <T : Any?> bindUntilEvent(event: ActivityEvent): LifecycleTransformer<T> {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event)
    }

    override fun <T : Any?> bindToLifecycle(): LifecycleTransformer<T> {
        return RxLifecycleAndroid.bindActivity(lifecycleSubject)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return supportFragmentInjector
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        hasSaveInstance = true
        super.onSaveInstanceState(outState)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()

        if (mResumeFragments.isEmpty())
            return

        for (info in mResumeFragments) {
            if (info.action == FragmentInfo.Action.ADD) {
                addFragmentInternal(info.fragment, info.id, info.tag)
            } else {
                ActivityUtils.replaceFragmentActivity(supportFragmentManager, info.fragment, info.id)
            }
        }

//        listOf(Collection)
        mResumeFragments.clear()
    }

    public fun addFragment(fragment: Fragment, tag: String)  {
        addFragment(fragment, 0, tag)
    }

    public fun addFragment(fragment: Fragment, id: Int, tag: String)  {
        if (!checkSaveInstance(fragment, tag, FragmentInfo.Action.ADD))
            addFragmentInternal(fragment, id, tag)
    }

    protected fun addFragmentInternal(fragment: Fragment, id: Int, tag: String){
        val fragmentManager: FragmentManager = supportFragmentManager
        if (!isExistFragment(fragmentManager, tag)) {
            if (id > 0) {
                ActivityUtils.addFragmentToActivity(fragmentManager, fragment, id, tag)
            } else {
                ActivityUtils.addFragmentToActivity(fragmentManager, fragment, tag)
            }
        }
    }

    protected fun replaceFragment(fragment: Fragment, id: Int) {
        if (!checkSaveInstance(fragment, id, null, FragmentInfo.Action.REPLACE))
            ActivityUtils.replaceFragmentActivity(supportFragmentManager, fragment, id)
    }

    private fun isExistFragment(fragmentManager: FragmentManager, tag: String) : Boolean{
        if (fragmentManager.findFragmentByTag(tag) == null)
            return false

        return true
    }

    private fun checkSaveInstance(fragment: Fragment, tag: String, action: FragmentInfo.Action) : Boolean{
        return checkSaveInstance(fragment, 0, tag, action)
    }

    private fun checkSaveInstance(fragment: Fragment, id: Int, tag: String?, action: FragmentInfo.Action) : Boolean{
        if (hasSaveInstance) {
            mResumeFragments.plus(FragmentInfo(fragment, id, tag!!, action))
            return true
        }

        return false
    }

    override fun onResume() {
        super.onResume()
        lifecycleSubject.onNext(ActivityEvent.RESUME)
        hasSaveInstance = false
    }

    override fun onPause() {
        lifecycleSubject.onNext(ActivityEvent.PAUSE)
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        lifecycleSubject.onNext(ActivityEvent.START)
    }

    /**
     * 这个是根据onStop函数里面，是否正在销毁
     * 根据阿里巴巴的手册说明，资源释放操作建议在这里
     */
    protected fun trash() {
        mResumeFragments.clear()
        if (mBinder != null)
            mBinder!!.unbind()
    }

    override fun onStop() {
        lifecycleSubject.onNext(ActivityEvent.STOP)
        super.onStop()
        if (isFinishing)
            trash()
    }

    override fun onDestroy() {
        lifecycleSubject.onNext(ActivityEvent.DESTROY)
        super.onDestroy()
    }
}