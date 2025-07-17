package com.example.snswithai

import android.app.Application

class SNSwithAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TimelineManager.startTimeline()
    }
}
