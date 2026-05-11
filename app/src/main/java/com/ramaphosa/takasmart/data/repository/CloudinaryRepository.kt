package com.ramaphosa.takasmart.data.repository


import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryRepository(private val context: Context) {

    companion object {
        // Replace these with your actual Cloudinary values
        const val CLOUD_NAME    = "demgqld3r"
        const val UPLOAD_PRESET = "takasmart_items"

        private var isInitialized = false

        fun initialize(context: Context) {
            if (isInitialized) return
            val config = mapOf(
                "cloud_name" to CLOUD_NAME
            )
            MediaManager.init(context, config)
            isInitialized = true
        }
    }

    // Upload a photo and return the Cloudinary URL
    // This is a suspend function — call it from a coroutine
    suspend fun uploadPhoto(imageUri: Uri): String =
        suspendCancellableCoroutine { continuation ->
            val requestId = MediaManager.get()
                .upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .option("folder", "ewaste_items")
                .callback(object : UploadCallback {

                    override fun onStart(requestId: String) {
                        // Upload started
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // You can use this to show a progress bar
                        // progress = bytes.toFloat() / totalBytes.toFloat()
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        // Extract the secure URL from the result
                        val url = resultData["secure_url"] as? String ?: ""
                        continuation.resume(url)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(
                            Exception(error.description)
                        )
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(
                            Exception("Upload rescheduled: ${error.description}")
                        )
                    }
                })
                .dispatch(context)
        }
}