package com.app.demo.ui.dashboard.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.demo.R
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivityDashboardBinding
import com.app.demo.model.CallListModel
import com.app.demo.model.user.UserModel
import com.app.demo.network.Status
import com.app.demo.ui.authentication.login.activity.LoginActivity
import com.app.demo.ui.authentication.profile.activity.EditProfileActivity
import com.app.demo.ui.authentication.profile.viewModel.ProfileViewModel
import com.app.demo.ui.chat.activity.ChatActivity
import com.app.demo.ui.dashboard.adapter.ActiveCallListAdapter
import com.app.demo.ui.dashboard.adapter.UserListAdapter
import com.app.demo.ui.dashboard.listener.ClickListeners
import com.app.demo.ui.dashboard.viewModel.DashboardViewModel
import com.app.demo.ui.groupChat.createGroup.activity.SelectUserActivity
import com.app.demo.ui.jitsiCall.activity.JitsiCallActivity
import com.app.demo.utils.Constant
import com.app.demo.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import org.jitsi.meet.sdk.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Activity is used to handle following things
 * - List of User
 * - list of active call
 * - start video call
 * - create group chat redirection
 * - Redirection of user for edit user profile
 */
class DashboardActivity : BaseActivity(), ClickListeners {

    lateinit var userlist: ArrayList<String>
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var binding: ActivityDashboardBinding
    private var userListAdapter: UserListAdapter? = null

    private var userArrayList = ArrayList<UserModel>()
    lateinit var dialogCallList: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * Initializing ui and call all method
     */
    private fun initUI() {

        userArrayList = ArrayList()
        getUsersList()
        setObserver()

        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
        //This listener will call whenever someone wants to connect with you via group call
        dashboardViewModel.receiveGroupCallInvitation(
            FirebaseAuth.getInstance().currentUser?.uid.toString(),
            "Active"
        )

        dashboardViewModel.getLIstOfActiveCall(
            FirebaseAuth.getInstance().currentUser?.uid.toString(),
            "Active"
        )

        dashboardViewModel.getLIstOfActiveCall(
            FirebaseAuth.getInstance().currentUser?.uid.toString(),
            "InActive"
        )

        binding.imgSelectUsersForVideoCall.setOnClickListener {
            if (userListAdapter != null) {
                if (userListAdapter!!.getIsSelectionAvailable()) {
                    userListAdapter!!.setIsSelectionEnable(false)
                    binding.imgStartVideoCall.visibility = View.GONE
                    userListAdapter?.resetUserList()
                }
                callListDialog()
            }
        }

        binding.imgLogout.setOnClickListener {
            setUserPresenceOnlineOffline(false)
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        binding.imgStartVideoCall.setOnClickListener {
            setupVideoCall()
        }


        binding.imgCreateGroupChat.setOnClickListener {
            val intent = Intent(this, SelectUserActivity::class.java)
            startActivity(intent)
        }

        binding.imgUserProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        setUserPresenceOnlineOffline(true)
    }

    /**
     * updating user presence in firebase
     */
    private fun setUserPresenceOnlineOffline(isOnline: Boolean) {
        profileViewModel.updateUserPresence(
            isOnline,
            FirebaseAuth.getInstance().currentUser?.uid.toString()
        )

    }

    /**
     * showing user logout dialog
     */
    private fun confirmProceedDialog(docId: String) {

        val dialogBuilder =
            MaterialAlertDialogBuilder(this@DashboardActivity, R.style.CutShapeTheme)

        val customView =
            LayoutInflater.from(this@DashboardActivity).inflate(R.layout.dialog_logout, null)
        val dialogLogout = dialogBuilder.setView(customView).show()


        val txtNo = dialogLogout.findViewById<View>(R.id.txtNo) as TextView
        val txtYes = dialogLogout.findViewById<View>(R.id.txtYes) as TextView
        val tvTitle = dialogLogout.findViewById<View>(R.id.tvTitle) as TextView
        val tvDesc = dialogLogout.findViewById<View>(R.id.tvDesc) as TextView

        txtNo.setOnClickListener { dialogLogout.dismiss() }

        txtYes.setOnClickListener {
            dialogLogout.dismiss()
            val intent = Intent(this, JitsiCallActivity::class.java)
            intent.putExtra("RoomId", docId)
            intent.putExtra("OpponentUserName", "")
            intent.putExtra("isGroupCall", true)
            startActivity(intent)
        }
        dialogLogout.show()
    }


    /**
     * setting video call
     */
    private fun setupVideoCall() {

        val selectedUserList = userListAdapter?.getSelectedUserList()
        if (selectedUserList?.size!! >= 2) {
            val userIds = ArrayList<String>()
            if (selectedUserList != null) {
                if (selectedUserList.size > 1) {
                    for (i in selectedUserList) {
                        userIds.add(i + "___InActive")
                    }
                }
            }
            var currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            userIds.add(0, currentUserId + "___Active")
            dashboardViewModel.setupGroupCallData(
                userIds,
                "Active",
                currentUserId,
                Constant.USER_NAME
            )

        } else {
            toast(getString(R.string.user_selection_validation))
        }
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


        dashboardViewModel.userGroupDataResponse.observe(this, {
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

        dashboardViewModel.addGroupCallDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data.let {
                        TedPermission.with(this@DashboardActivity)
                            .setPermissionListener(object : PermissionListener {
                                override fun onPermissionGranted() {
                                    val intent = Intent(
                                        this@DashboardActivity,
                                        JitsiCallActivity::class.java
                                    )
                                    intent.putExtra("RoomId", it!!)
                                    intent.putExtra("OpponentUserName", "")
                                    intent.putExtra("isGroupCall", true)
                                    startActivity(intent)
                                    Handler().postDelayed(Runnable {
                                        userListAdapter?.resetUserList()
                                        userListAdapter?.setIsSelectionEnable(false)
                                        binding.imgStartVideoCall.visibility = View.GONE
                                    }, 1000)

                                }

                                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                                }

                            }).setDeniedMessage(getString(R.string.permission_denied))
                            .setPermissions(
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.CAMERA
                            )
                            .check()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> toast(it1) }
                }
            }
        })

        dashboardViewModel.receiveGroupCallInvitationResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data.let {
                        confirmProceedDialog(it!!)
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> toast(it1) }
                }
            }
        })

        dashboardViewModel.updatedUserDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { user ->
                        updateUserData(user)
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
     * Update user list on runtime
     */
    private fun updateUserData(updateUserList: ArrayList<UserModel>) {

        for (i in updateUserList.indices) {

            for (j in userArrayList.indices) {
                if (userArrayList[j].uid?.equals(updateUserList[i].uid.toString()) == true) {
                    userArrayList[j] = updateUserList[i]
                }
            }
        }

        userListAdapter?.notifyDataSetChanged()
    }

    /**
     * Getting registered users list
     */
    private fun getUsersList() {
        dashboardViewModel.getUsersList()
        dashboardViewModel.getListOfGroup(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    /**
     * Set user adapter
     */
    private fun setAdapter() {
        if (userArrayList.size > 0) {
            userArrayList.sortByDescending { it.createdAt }
        }
        binding.rvUserList.layoutManager = LinearLayoutManager(this)
        userListAdapter = UserListAdapter(this, userArrayList)
        binding.rvUserList.adapter = userListAdapter

    }

    /**
     * Redirect to chat activity after click on user
     */
    fun redirectChatActivity(position: Int) {
        userlist = ArrayList()
        userlist.add(0, FirebaseAuth.getInstance().currentUser?.uid.toString())
        userlist.add(1, userArrayList[position].uid.toString())

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("isGroup", userArrayList[position].isGroup)
        intent.putExtra("userList", Gson().toJson(userArrayList))
        if (userArrayList[position].isGroup) {
            intent.putExtra("user_id", userArrayList[position].groupDetailModel!!.id)
            intent.putExtra("user_name", userArrayList[position].groupDetailModel!!.name)
            intent.putExtra("group_image", userArrayList[position].groupDetailModel!!.groupIcon)
            intent.putExtra("memberIdList",
                Gson().toJson(userArrayList[position].groupDetailModel!!.members))

        } else {
            intent.putExtra("user_id", userArrayList[position].uid)
            intent.putExtra("user_name",
                userArrayList[position].firstName + " " + userArrayList[position].lastName)
            intent.putExtra("group_image", "")
        }

        Handler().postDelayed(Runnable {
            userListAdapter?.resetUserList()
        }, 1000)
        startActivity(intent)
    }

    /**
     * showing active group call and create new group call option
     */
    private fun callListDialog() {
        dialogCallList = Dialog(this, R.style.DialogSlideAnim)
        dialogCallList!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCallList!!.setContentView(R.layout.dialog_display_call_list)


        dialogCallList.getWindow()!!.setGravity(Gravity.BOTTOM)
        dialogCallList.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialogCallList!!.window!!.attributes)
        dialogCallList.setCancelable(true)
        dialogCallList.getWindow()!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialogCallList!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogCallList!!.window!!.attributes = lp
        dialogCallList!!.show()

        val tvNewCall: TextView = dialogCallList!!.findViewById(R.id.txt_new_call)
        val rvCallList: RecyclerView = dialogCallList!!.findViewById(R.id.recyclerCallList)
        val txtCancel: TextView = dialogCallList!!.findViewById(R.id.txt_cancel)

        val txtViewCallList: View = dialogCallList!!.findViewById(R.id.view_list_call)

        if (Constant.listOfActiveCall.size > 0) {
            var groupCallList = ArrayList<CallListModel>()
            for (i in Constant.listOfActiveCall) {
                if (i.userIds.size > 2) {
                    groupCallList.add(i)
                }
            }

            var adapter = ActiveCallListAdapter(this, groupCallList, this)
            rvCallList.layoutManager = LinearLayoutManager(this)
            rvCallList.adapter = adapter
            txtViewCallList.visibility = View.GONE
        } else {
            txtViewCallList.visibility = View.GONE
        }

        tvNewCall.setOnClickListener {
            dialogCallList!!.dismiss()
            userListAdapter!!.setIsSelectionEnable(true)
            binding.imgStartVideoCall.visibility = View.VISIBLE

        }
        txtCancel.setOnClickListener {
            dialogCallList!!.dismiss()
            if (userListAdapter!!.getIsSelectionAvailable()) {
                userListAdapter!!.setIsSelectionEnable(false)
                binding.imgStartVideoCall.visibility = View.GONE
                userListAdapter?.resetUserList()
            }
        }
    }

    /**
     * starting video call
     */
    override fun startCall(roomId: String) {

        if (dialogCallList != null && dialogCallList.isShowing) {
            dialogCallList.dismiss()
        }

        TedPermission.with(this@DashboardActivity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {

                    val intent = Intent(this@DashboardActivity, JitsiCallActivity::class.java)
                    intent.putExtra("RoomId", roomId)
                    intent.putExtra("OpponentUserName", "")
                    intent.putExtra("isGroupCall", true)
                    startActivity(intent)

                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                }

            }).setDeniedMessage(getString(R.string.permission_denied))
            .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
            .check()

    }

}