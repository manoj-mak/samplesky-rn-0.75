package com.samplesky.modules

import com.fulcroworld.samplesky.BuildConfig
import timber.log.Timber

object TimberUtils {

    @JvmStatic
    fun configTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
