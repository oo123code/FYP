package com.navigine.indoornavigationdemo

import android.app.Application
import com.navigine.indoornavigationdemo.data.NavigineSdkManager
import com.navigine.indoornavigationdemo.data.PreferenceManager

class App : Application() {

    companion object {
        lateinit var preferenceManager: PreferenceManager
            private set
    }

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(applicationContext)
        try {
            NavigineSdkManager.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}