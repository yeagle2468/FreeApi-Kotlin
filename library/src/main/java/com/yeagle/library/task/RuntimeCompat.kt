package com.yeagle.library.task

import android.os.Build
import android.os.StrictMode
import java.io.File
import java.util.regex.Pattern

/**
 * Compatibility methods for {@link java.lang.Runtime}.
 * from glide
 */
object RuntimeCompat {
    private val TAG = "RuntimeCompat"
    private val CPU_NAME_REGEX = "cpu[0-9]+"
    private val CPU_LOCATION = "/sys/devices/system/cpu/"

    public fun availableProcessors() : Int{
        var cpus = Runtime.getRuntime().availableProcessors()
        if (Build.VERSION.SDK_INT < 17) {
            cpus = Math.max(getCoreCountPre17(), cpus)
        }

        return cpus;
    }

    private fun getCoreCountPre17() : Int {
        var cpus: Array<File>? = null
        val originalPolicy : StrictMode.ThreadPolicy = StrictMode.allowThreadDiskReads()

        try {
            val cpuInfo = File(CPU_LOCATION)
            val cpuNamePattern = Pattern.compile(CPU_NAME_REGEX)

            cpus = cpuInfo.listFiles { _, s ->  cpuNamePattern.matcher(s).matches()}
        } catch (t: Throwable) {

        } finally {
            StrictMode.setThreadPolicy(originalPolicy)
        }


        return Math.max(1, cpus!!.size)
    }
}