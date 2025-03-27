package com.example.trackify

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp //compile time injected
class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }






}