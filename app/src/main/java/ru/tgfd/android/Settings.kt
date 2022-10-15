package ru.tgfd.android

import android.content.Context

class Settings(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        DEFAULT_PREFERENCES,
        Context.MODE_PRIVATE
    )

    fun isExperimentalFacade(): Boolean =
        sharedPreferences.getBoolean(EXPERIMENTAL_FEED_FACADE, false)

    fun setExperimentalFacade(stateValue: Boolean) {
        sharedPreferences.edit()
            .putBoolean(EXPERIMENTAL_FEED_FACADE, stateValue)
            .apply()
    }

    companion object {
        const val DEFAULT_PREFERENCES = "main_preferences"

        const val EXPERIMENTAL_FEED_FACADE = "experimental_feed_facade"
    }
}