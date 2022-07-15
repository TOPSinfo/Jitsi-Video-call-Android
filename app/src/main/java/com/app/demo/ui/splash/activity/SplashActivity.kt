package com.app.demo.ui.splash.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivitySplashBinding
import com.app.demo.network.Status
import com.app.demo.ui.authentication.login.activity.LoginActivity
import com.app.demo.ui.authentication.profile.activity.ProfileActivity
import com.app.demo.ui.dashboard.activity.DashboardActivity
import com.app.demo.ui.splash.viewModel.SplashViewModel
import com.app.demo.utils.Constant
import com.app.demo.utils.toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * Initialize user interface
     */
    private fun initUI() {
        setObserver()
        Constant.listOfActiveCall.clear()
        lifecycleScope.launch {
            delay(2000L)
            redirectDashBoardActivity()
        }
    }

    /**
     * Initialize observer
     */
    private fun setObserver() {
        splashViewModel.userDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { user ->
                        if (user) {
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        } else {
                            startActivity(Intent(this, ProfileActivity::class.java))
                            finish()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> toast(it1) }
                }
            }
        })
    }


    /**
     * Checking user login or not and redirecting according to screen
     */
    private fun redirectDashBoardActivity() {

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            /**
             * Checking that user is login but user profile info add or not
             */
            splashViewModel.checkUserRegisterOrNot(FirebaseAuth.getInstance().currentUser?.uid.toString())
        }
    }
}