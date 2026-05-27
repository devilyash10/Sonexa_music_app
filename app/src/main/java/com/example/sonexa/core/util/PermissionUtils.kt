package com.example.sonexa.core.util

import android.Manifest
import android.os.Build

object PermissionUtils {
    /**
     * Returns the correct permission string based on the device's Android version.
     */
    val audioPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
}
