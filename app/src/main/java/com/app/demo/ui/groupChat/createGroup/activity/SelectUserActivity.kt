package com.app.demo.ui.groupChat.createGroup.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.demo.R
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivitySelectUserBinding
import com.app.demo.model.user.UserModel
import com.app.demo.network.Status
import com.app.demo.ui.dashboard.viewModel.DashboardViewModel
import com.app.demo.ui.groupChat.createGroup.adapter.SelectUserAdapter
import com.app.demo.utils.toast
import com.google.gson.Gson

class SelectUserActivity : BaseActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    private lateinit var binding: ActivitySelectUserBinding
    private var userArrayList = ArrayList<UserModel>()
    private var selectUserAdapter: SelectUserAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * Initialize ui interface and all methods
     */
    private fun initUI() {
        setObserver()
        getUsersList()

        binding.rlCreateGroupChat.setOnClickListener {
            redirectCreateGroupActivity()
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Getting registered users list
     */
    private fun getUsersList() {
        dashboardViewModel.getUsersList()
    }

    /**
     * Set up observer
     */
    private fun setObserver() {

        dashboardViewModel.userDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { user ->
                        userArrayList.addAll(user)
                        setAdapter()
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
     * Set user adapter
     */
    private fun setAdapter() {
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        selectUserAdapter = SelectUserAdapter(this, userArrayList)
        binding.rvUserList.adapter = selectUserAdapter
    }


    /**
     * Redirecting create group screen
     */
    private fun redirectCreateGroupActivity() {
        if (selectUserAdapter?.getSelectedUserList()?.size!! >= 2) {

            val selectedUserLists = ArrayList<UserModel>()

            for (i in userArrayList.indices) {
                if (userArrayList[i].isSelectedForCall) {
                    selectedUserLists.add(userArrayList[i])
                }
            }

            val intent = Intent(this, CreateGroupActivity::class.java)
            intent.putExtra("UserList", Gson().toJson(selectedUserLists))
            intent.putExtra("group_image", "")
            intent.putExtra("group_name", "")
            intent.putExtra("isForDisplay", false)
            startActivity(intent)

        } else {
            toast(getString(R.string.create_group_validation))
        }
    }

}