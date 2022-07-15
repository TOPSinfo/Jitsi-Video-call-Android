package com.app.demo.ui.authentication.verification.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.app.demo.R
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivityVerificationBinding
import com.app.demo.ui.authentication.profile.activity.ProfileActivity
import com.app.demo.ui.authentication.verification.navigator.VerificationNavigator
import com.app.demo.ui.authentication.verification.viewModel.VerificationViewModel
import com.app.demo.ui.dashboard.activity.DashboardActivity
import com.app.demo.utils.Constant
import com.app.demo.utils.Utility
import com.app.demo.utils.toast
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

/**
 * Activity is used to verify user mobile number
 */
class VerificationActivity : BaseActivity(), VerificationNavigator {

    private lateinit var binding: ActivityVerificationBinding
    private val verificationViewModel: VerificationViewModel by viewModels()
    private var timer: CountDownTimer? = null
    private var phoneNumber: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        verificationViewModel.verificationNavigator = this
        initUI()
    }

    /**
     * Initialize user interface and all methods
     */
    private fun initUI() {
        phoneNumber = intent.getStringExtra("phone_number")
        setObserver()
        setClickListener()
        enableResendOTPView()
        startPhoneNumberVerification()
    }

    /**
     * Initialize click listener
     */
    private fun setClickListener() {

        binding.btnVerify.setOnClickListener {
            signInWithPhoneAuth()
        }

        binding.txtResend.setOnClickListener {
            verificationViewModel.resendOTP()
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.otpView.setOnEditorActionListener { _, _, _ ->
            if (binding.btnVerify.text.toString().trim().length == 6) {
                Utility.hideKeyboard(this@VerificationActivity, binding.otpView)
                if (!verificationViewModel.isVerifyOTPcall) {
                    binding.btnVerify.performClick()
                }
            }

            false
        }

        binding.otpView.doAfterTextChanged {
            verificationViewModel.OTPCode.postValue(binding.otpView.text.toString().trim())
        }
    }

    /**
     * set up observer
     */
    private fun setObserver() {
        verificationViewModel.isOTPFilled.observe(this@VerificationActivity, {
            if (it) {
                binding.otpView.setText(verificationViewModel.OTPCode.value.toString())
            }
        })

        verificationViewModel.isStopTimer.observe(this@VerificationActivity, {
            if (it) {
                timer?.cancel()
                enableResendOTPView()
            }
        })
    }

    /**
     * Shwoing progress dialog
     */
    override fun onStarted() {
        showProgress(this@VerificationActivity)
    }

    /**
     * After verify OTP redirecting to profile activity
     */
    override fun onSuccess() {
        hideDialog()
        val intent = Intent(this@VerificationActivity, ProfileActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        timer?.cancel()
        finish()
    }

    /**
     * After verify OTP redirecting to dashboard activity if user already added info
     */
    override fun redirectToDashboard() {
        verificationViewModel.isStopTimer.value = true
        val intent = Intent(this@VerificationActivity, DashboardActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finishAffinity()
        timer?.cancel()
    }

    /**
     * Sending OTP To entered mobile number
     */
    override fun startPhoneNumberVerification() {
        if (Utility.checkConnection(this@VerificationActivity)) {

            val options = phoneNumber?.let {
                PhoneAuthOptions.newBuilder()
                    .setPhoneNumber(it)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(verificationViewModel.callbacks)          // OnVerificationStateChangedCallbacks
                    .build()
            }
            if (options != null) {
                PhoneAuthProvider.verifyPhoneNumber(options)
                showProgress(this@VerificationActivity)
            }

        } else
            toast(getString(R.string.internet_not_available))
    }

    /**
     * Hiding progress dialog
     */
    override fun hideDialog() {
        hideProgress()
    }

    /**
     * Showing progress dialog
     */
    override fun showDialog() {
        showProgress(this@VerificationActivity)
    }

    /**
     * Enabling resend button
     */
    override fun enableResendOTPView() {
        binding.txtResend.isClickable = true
        binding.txtResend.alpha = 1f
        binding.txtTimer.text = getString(R.string.one)
        binding.txtTimer.visibility = View.GONE
    }

    /**
     * Disabling resend button
     */
    private fun disableResendOTPView() {

        binding.txtTimer.text = getString(R.string.one)
        binding.txtResend.isClickable = false
        binding.txtResend.alpha = 0.3f
        binding.txtTimer.visibility = View.VISIBLE

    }

    /**
     * Starting timer for resend button enable and disable
     */
    override fun startCountdownTimer() {
        disableResendOTPView()
        timer?.cancel()
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                verificationViewModel.isExpire = false

                val totalSecs = millisUntilFinished / 1000
                val minutes = (totalSecs % 3600) / 60
                val seconds = totalSecs % 60;
                binding.txtTimer.text = "" + minutes + ":" + Utility.twoDigitString(seconds.toInt())
            }

            override fun onFinish() {
                verificationViewModel.isExpire = true
                enableResendOTPView()
            }
        }
        (timer as CountDownTimer).start()
    }

    /**
     * Showing toast message
     */
    override fun showMessage(message: String) {
        toast(message)
    }

    /**
     * Showing toast failure message
     */
    override fun onFailure(message: String) {
        hideProgress()
        toast(message)
    }

    /**
     * Showing firebase link message
     */
    override fun showLinkErrormsg() {
        toast(Constant.validation_error)
    }

    /**
     * Verifying user entered otp
     */
    override fun signInWithPhoneAuth() {
        if (Utility.checkConnection(this@VerificationActivity)) {
            if (verificationViewModel.checkValidation()) {
                verificationViewModel.verifyPhoneNumberWithCode()
            }
        } else
            showMessage(getString(R.string.internet_not_available))
    }

    /**
     * Resending otp to entered mobile number
     */
    override fun resendVerificationCode(resentToken: PhoneAuthProvider.ForceResendingToken) {
        if (Utility.checkConnection(this@VerificationActivity)) {
            val options = phoneNumber?.let {
                PhoneAuthOptions.newBuilder()
                    .setPhoneNumber(it)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this)                 // Activity (for callback binding)
                    .setCallbacks(verificationViewModel.callbacks)          // OnVerificationStateChangedCallbacks
                    .setForceResendingToken(resentToken)
                    .build()
            }
            if (options != null) {
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        } else
            showMessage(getString(R.string.internet_not_available))
    }

    /**
     * On back button cancelling timer
     */
    override fun onBackPressed() {
        timer?.cancel()
        super.onBackPressed()
    }

}