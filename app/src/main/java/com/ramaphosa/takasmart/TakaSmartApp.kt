package com.ramaphosa.takasmart

import android.app.Application
import com.ramaphosa.takasmart.data.repository.CloudinaryRepository

class TakaSmartApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Cloudinary once when the app starts
        // Replace "your_cloud_name" with your actual Cloudinary cloud name
        CloudinaryRepository.initialize(this)
    }
}