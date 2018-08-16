package com.yeagle.library.fragment

import android.support.v4.app.Fragment

/**
 * Created by yeagle on 2018/8/15.
 */
class FragmentInfo {
    enum class Action {
        ADD, REPLACE
    }

    public val fragment: Fragment
    public val tag: String
    public val action: Action

    public var id: Int = 0

    constructor(fragment: Fragment, tag: String, action: Action){
        this.fragment = fragment
        this.tag = tag
        this.action = action
    }

    constructor(fragment: Fragment, id: Int, tag: String, action: Action) {
        this.fragment = fragment
        this.tag = tag
        this.action = action

        this.id = id
    }
}