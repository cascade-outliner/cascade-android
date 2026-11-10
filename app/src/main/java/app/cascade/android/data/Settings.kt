package app.cascade.android.data

import android.content.Context
import androidx.core.content.edit

class Settings(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("cascade", Context.MODE_PRIVATE)

    var onboarded: Boolean
        get() = preferences.getBoolean("onboarded", false)
        set(value) = preferences.edit { putBoolean("onboarded", value) }
}
