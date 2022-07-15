package com.app.demo.ui.authentication.verification.navigator

import com.google.firebase.auth.PhoneAuthProvider

/**
 * Interface to handle navigation for verification module
 */
interface VerificationNavigator {
    fun onStarted()
    fun onSuccess()
    fun redirectToDashboard()
    fun startPhoneNumberVerification()
    fun hideDialog()
    fun showDialog()
    fun enableResendOTPView()
    fun startCountdownTimer()
    fun showMessage(message: String)
    fun onFailure(message: String)
    fun showLinkErrormsg()
    fun signInWithPhoneAuth()
    fun resendVerificationCode(resentToken: PhoneAuthProvider.ForceResendingToken)
}