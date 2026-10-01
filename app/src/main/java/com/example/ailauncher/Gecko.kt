package com.example.ailauncher

import android.content.Context
import org.mozilla.geckoview.GeckoRuntime

// GeckoRuntime must exist only once per process.
object Gecko {
    private var rt: GeckoRuntime? = null
    fun get(c: Context): GeckoRuntime = rt ?: GeckoRuntime.create(c.applicationContext).also { rt = it }
}
