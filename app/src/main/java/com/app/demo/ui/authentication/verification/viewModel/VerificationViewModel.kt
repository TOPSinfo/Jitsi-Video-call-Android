package com.app.demo.ui.authentication.verification.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.demo.data.repository.UserRepository
import com.app.demo.model.user.UserModel
import com.app.demo.ui.authentication.verification.navigator.VerificationNavigator
import com.app.demo.utils.Constant
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class VerificationViewModel @Inject constructor(private val userRepository: UserRepository) :
    ViewModel() {
    var verificationNavigator: VerificationNavigator? = null

    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var varificationID: String = ""
    lateinit var resentToken: PhoneAuthProvider.ForceResendingToken
    var isOTPFilled: MutableLiveData<Boolean> = MutableLiveData()
    var isStopTimer: MutableLiveData<Boolean> = MutableLiveData()
    var OTPCode = MutableLiveData<String>()
    var isExpire = false
    var isVerifyOTPcall = false
    private var auth = FirebaseAuth.getInstance()

    var user: UserModel = UserModel()

    init {
        initVerificationCallback()
    }

    /**
     * Sending code to entered mobile number
     */
    private fun initVerificationCallback() {

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                if (credential.smsCode != null && !credential.smsCode.toString().equals("null")) {
                    OTPCode.value = credential.smsCode.toString()
                    isOTPFilled.value = true
                    isStopTimer.value = true
                }
                if (!isVerifyOTPcall) {
                    verifyOTPinFirebase(credential)
                }

            }

            override fun onVerificationFailed(p0: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                // [END_EXCLUDE]

                verificationNavigator?.hideDialog()
                verificationNavigator?.enableResendOTPView()
                if (p0 is FirebaseAuthInvalidCredentialsException) {
                    verificationNavigator?.onFailure(Constant.validation_mobile_number)
                } else if (p0 is FirebaseAuthUserCollisionException) {
                    verificationNavigator?.showLinkErrormsg()
                } else {
                    verificationNavigator?.onFailure(p0.message.toString())
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                verificationNavigator?.hideDialog()
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                verificationNavigator?.showMessage("The SMS verification code has been sent to the provided phone number")
                varificationID = verificationId
                resentToken = token
                verificationNavigator?.startCountdownTimer()
            }
        }
    }

    /**
     * Verifying otp in firebase
     */
    private fun verifyOTPinFirebase(credential: PhoneAuthCredential) {


        verificationNavigator?.showDialog()

        val userRef = auth.signInWithCredential(credential)
        userRef.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                isVerifyOTPcall = false
                val firebaseUser = task.result?.user
                user.uid = firebaseUser?.uid.toString()

                checkUserRegisterOrNot(firebaseUser?.uid.toString())

            } else {
                isVerifyOTPcall = false
                OTPCode.value = ""
                isOTPFilled.value = true
                verificationNavigator?.hideDialog()
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    verificationNavigator?.onFailure(Constant.val_optInvalid)
                } else if (task.exception is FirebaseAuthUserCollisionException) {
                    verificationNavigator?.showLinkErrormsg()
                } else
                    verificationNavigator?.onFailure(task.exception?.message.toString())
            }
        }

    }

    /**
     * Verifying OTP number
     */
    fun verifyPhoneNumberWithCode() {
        verificationNavigator?.showDialog()

        isVerifyOTPcall = true
        val phoneAuthCredential =
            PhoneAuthProvider.getCredential(varificationID, OTPCode.value.toString())
        verifyOTPinFirebase(phoneAuthCredential)

    }

    /**
     * Resending otp to number
     */
    fun resendOTP() {
        isExpire = false
        OTPCode.value = ""
        isOTPFilled.value = true
        if (::resentToken.isInitialized) {
            verificationNavigator?.showDialog()
            verificationNavigator?.resendVerificationCode(resentToken)
        } else {
            verificationNavigator?.showDialog()
            verificationNavigator?.startPhoneNumberVerification()
        }
    }

    /**
     * Checking validation
     */
    fun checkValidation(): Boolean {

        if (OTPCode.value.isNullOrEmpty() || OTPCode.value?.length!! < 6) {
            verificationNavigator?.onFailure(Constant.validation_OTP)
            return false
        } else if (varificationID.isEmpty()) {
            verificationNavigator?.onFailure(Constant.val_optInvalid)
            return false
        } else if (isExpire) {
            verificationNavigator?.onFailure(Constant.val_optExpired)
            return false
        }
        return true
    }

    /**
     * Checking user already registered or not
     */
    private fun checkUserRegisterOrNot(userId: String) {
        val docIdRef: DocumentReference = userRepository.getUserProfileRepository(userId)

        docIdRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                verificationNavigator?.hideDialog()
                val document: DocumentSnapshot? = task.result
                if (document?.exists() == true) {
                    verificationNavigator?.redirectToDashboard()
                } else {
                    verificationNavigator?.onSuccess()
                }
            } else {
                verificationNavigator?.hideDialog()
                verificationNavigator?.onFailure(Constant.validation_error)
            }
        }
    }
}