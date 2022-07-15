package com.app.demo

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.app.demo.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application(), LifecycleObserver {


    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        updateUserPresence(false)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        updateUserPresence(true)
    }

    /**
     * Update user status in firebase
     */
    private fun updateUserPresence(isOnline: Boolean) {
        try {
            if (FirebaseAuth.getInstance().currentUser != null) {
                FirebaseFirestore.getInstance().collection(Constant.TABLE_USER)
                    .document(FirebaseAuth.getInstance().currentUser?.uid.toString()).update(
                        mapOf(
                            Constant.FIELD_IS_ONLINE to isOnline,
                        )
                    )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}