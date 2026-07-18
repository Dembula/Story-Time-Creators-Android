package online.storytime.creators

import android.app.Application
import online.storytime.creators.core.Session

class StoryTimeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Session.init(this)
    }
}
