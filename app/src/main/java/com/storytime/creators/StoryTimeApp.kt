package com.storytime.creators

import android.app.Application
import com.storytime.creators.core.Session

class StoryTimeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Session.init(this)
    }
}
