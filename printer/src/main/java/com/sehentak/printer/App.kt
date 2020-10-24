package com.sehentak.printer

import android.app.Application
import android.content.Context

class App: Application() {
    companion object {
        lateinit var mContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
    }
}