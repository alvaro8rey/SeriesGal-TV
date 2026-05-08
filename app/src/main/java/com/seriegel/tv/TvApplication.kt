package com.seriegel.tv

import android.app.Application
import com.seriegel.tv.core.di.AppContainer

class TvApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
