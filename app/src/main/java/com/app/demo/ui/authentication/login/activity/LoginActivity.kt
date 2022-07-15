package com.app.demo.ui.authentication.login.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.app.demo.R
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivityLoginBinding
import com.app.demo.ui.authentication.verification.activity.VerificationActivity
import com.app.demo.utils.Constant
import com.app.demo.utils.Utility
import com.app.demo.utils.toast

/**
 * Activity is used to manage login process of user
 */
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * Initialize ui interface and all methods
     */
    private fun initUI() {
        fetchCountryCode()
        setClickListener()

        binding.ccp.registerPhoneNumberTextView(binding.edtMobileNumber)

        binding.ccp.setOnCountryChangeListener {
            binding.edtMobileNumber.setText("")
        }
        binding.edtMobileNumber.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utility.hideKeyboard(this, binding.edtMobileNumber)
                binding.btnLogin.performClick()
                return@OnEditorActionListener true
            }
            false
        })

        binding.edtMobileNumber.doAfterTextChanged {
            isPhoneValid()
        }

    }

    /**
     * Checking phone number is valid or not set image
     */
    private fun isPhoneValid() {

        if (binding.ccp.isValid) {
            binding.edtMobileNumber.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_check_purple,
                0
            )
        } else {
            binding.edtMobileNumber.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_check_black,
                0
            )
        }
    }


    /**
     * Fetching user country code and set to country picker
     */
    private fun fetchCountryCode() {
        val tm =
            this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tm.networkCountryIso

        if (countryCodeValue != null)
            binding.ccp.setCountryForNameCode(countryCodeValue)
    }

    /**
     * Initialize click listener and redirect to verification activity
     */
    private fun setClickListener() {
        binding.btnLogin.setOnClickListener {
            if (Utility.checkConnection(this@LoginActivity)) {
                if (checkValidation()) {
                    redirectVerificationActivity()
                }
            } else {
                toast(getString(R.string.internet_not_available))
            }
        }
    }

    /**
     * Checking mobile number validation
     */
    private fun checkValidation(): Boolean {
        if (!binding.ccp.isValid) {
            toast(Constant.validation_mobile_number)
            return false
        }
        return true
    }

    /**
     * Redirecting to phone number verification activity
     */

    private fun redirectVerificationActivity() {
        startActivity(
            Intent(this, VerificationActivity::class.java).putExtra(
                "phone_number",
                "+" + binding.ccp.selectedCountryCode + binding.edtMobileNumber.text.toString()
                    .trim()
            )
        )
    }

}