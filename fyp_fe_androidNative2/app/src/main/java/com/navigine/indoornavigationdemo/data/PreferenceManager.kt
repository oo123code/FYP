package com.navigine.indoornavigationdemo.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager (context: Context) {
    private val sharedPreferences: SharedPreferences
    private val PREFS_NAME = "app_prefs"
    private val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"

    init {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setHasCompletedOnboarding(hasCompleted: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_HAS_COMPLETED_ONBOARDING, hasCompleted)
            .apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)
    }
}