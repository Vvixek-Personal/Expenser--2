package com.example

import com.google.firebase.appcheck.FirebaseAppCheck

object AppCheckDebugHelper {
    @Suppress("UNUSED_PARAMETER")
    fun installDebugProvider(firebaseAppCheck: FirebaseAppCheck) {
        // No-op in release builds: Release builds use PlayIntegrityAppCheckProviderFactory only
    }
}
