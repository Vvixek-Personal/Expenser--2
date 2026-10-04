package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class FinanceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d("FinanceApp", "Initialized Firebase App Check with Debug provider")
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.d("FinanceApp", "Initialized Firebase App Check with Play Integrity provider")
            }
        } catch (t: Throwable) {
            Log.w("FinanceApp", "Firebase App Check initialization deferred: ${t.message}")
        }
    }
}
