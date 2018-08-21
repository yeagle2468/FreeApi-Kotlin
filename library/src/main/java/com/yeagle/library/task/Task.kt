package com.yeagle.library.task

/**
 * Created by yeagle on 2018/8/21.
 */
abstract class Task : Runnable {
    enum class State {
        IDLE, RUNNING, COMPLETE, CANCEL
    }

    private var taskName : String? = null
    private var taskListener : OnTaskListener? = null

    private var state : State = State.IDLE



    public interface OnTaskListener {
        fun onFinish(task: Task)
    }
}