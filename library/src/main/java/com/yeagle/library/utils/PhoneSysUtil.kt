package com.yeagle.library.utils

import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Created by yeagle on 2018/8/15.
 */
object PhoneSysUtil {
    private val MIUI_VERSION_CODE    = "ro.miui.ui.version.code"
    private val MIUI_VERSION_NAME    = "ro.miui.ui.version.name"
    private val MIUI_VERSION_STORAGE = "ro.miui.internal.storage"

    private val checkSysMap = HashMap<String, Boolean>()

    private val properties = Properties()
    private var hasLoadedPro = false

    public fun isMIUI() : Boolean? {
        if (checkSysMap.containsKey("miui"))
            return checkSysMap.get("miui")

        loadInfo()

        val flag = properties.getProperty(MIUI_VERSION_CODE, null) != null
                || properties.getProperty(MIUI_VERSION_NAME, null) != null
                || properties.getProperty(MIUI_VERSION_STORAGE, null) != null

        checkSysMap.put("miui", flag)
        return flag
    }

    private fun loadInfo() {
        if (hasLoadedPro)
            return
        properties.load(FileInputStream(File(Environment.getRootDirectory(), "build.prop")))
        hasLoadedPro = true
    }

}