package com.app.demo.ui.groupChat.createGroup.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.app.demo.R
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivityCreateGroupBinding
import com.app.demo.model.group.GroupModel
import com.app.demo.model.user.UserModel
import com.app.demo.network.Status
import com.app.demo.ui.dashboard.activity.DashboardActivity
import com.app.demo.ui.groupChat.createGroup.adapter.ParticipantsAdapter
import com.app.demo.ui.groupChat.createGroup.viewmodel.GroupChatViewModel
import com.app.demo.utils.Constant
import com.app.demo.utils.Glide
import com.app.demo.utils.SpacesItemDecoration
import com.app.demo.utils.toast
import com.google.common.reflect.TypeToken
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import java.lang.reflect.Type

class CreateGroupActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateGroupBinding
    private var userArrayList = ArrayList<UserModel>()
    private val groupChatViewModel: GroupChatViewModel by viewModels()
    var groupProfileImagePath: Uri? = null
    var groupIcon: String = ""
    var groupName: String = ""
    var isForDisplay: Boolean = false
    var memberIdList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * Initialize ui interface and all methods
     */
    private fun initUI() {

        groupIcon = intent.getStringExtra("group_image")!!
        groupName = intent.getStringExtra("group_name")!!
        isForDisplay = intent.getBooleanExtra("isForDisplay", false)
        setClickListener()
        setObserver()

        if (isForDisplay) {
            binding.imgCreateGroup.visibility = View.GONE
            binding.imgGroupIcon.isEnabled = false
            binding.edtGroupName.isEnabled = false
            binding.txtHeader.text = getString(R.string.group_detail)
            binding.edtGroupName.setText(groupName)

            if (intent.hasExtra("memberIdList")) {
                val carListAsString = intent.getStringExtra("memberIdList")
                val type: Type = object : TypeToken<ArrayList<String>>() {}.type
                memberIdList = Gson().fromJson(carListAsString, type)
            }
            groupChatViewModel.getListOfMemberInGroup(memberIdList)
        } else {
            binding.imgCreateGroup.visibility = View.VISIBLE
            binding.imgGroupIcon.isEnabled = true
            binding.edtGroupName.isEnabled = true
            binding.txtHeader.text = getString(R.string.new_group)
            binding.edtGroupName.setText("")

            if (intent.hasExtra("UserList")) {
                val carListAsString = intent.getStringExtra("UserList")
                val type: Type = object : TypeToken<ArrayList<UserModel>>() {}.type
                userArrayList = Gson().fromJson(carListAsString, type)
            }
        }

        if (groupIcon.equals("")) {
            Glide.loadPlaceholderImage(
                this,
                R.drawable.ic_placeholder,
                binding.imgGroupIcon
            )
        } else {
            Glide.loadGlideProfileImage(
                this,
                groupIcon,
                binding.imgGroupIcon
            )
        }

        if (userArrayList.size > 0) {
            binding.txtNumberOfParticipants.text =
                getString(R.string.number_of_participants, userArrayList.size)
            setAdapter()
        }
    }

    /**
     * Initialize click listener
     */
    private fun setClickListener() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.imgGroupIcon.setOnClickListener {
            pickImage()
        }

        binding.imgCreateGroup.setOnClickListener {
            if (checkValidation()) {
                var model = GroupModel()
                model.adminId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                model.adminName = Constant.USER_NAME
                model.name = binding.edtGroupName.text.toString()
                model.createdAt = Timestamp.now()
                for (i in userArrayList) {
                    model.members.add(i.uid!!)
                }
                model.members.add(FirebaseAuth.getInstance().currentUser?.uid.toString())
                groupChatViewModel.updateGroupProfilePicture(model, groupProfileImagePath)
            }
        }
    }

    /**
     * setup adapter
     */
    private fun setAdapter() {
        val participantAdapter = ParticipantsAdapter(this@CreateGroupActivity, userArrayList)
        val layoutManager = GridLayoutManager(this, 4)

        binding.rvPartcipants.layoutManager = layoutManager
        binding.rvPartcipants.addItemDecoration(SpacesItemDecoration(5))
        binding.rvPartcipants.adapter = participantAdapter
    }


    /**
     * Check camera and read write permission and open camera and image picker
     */
    private fun pickImage() {
        TedPermission.with(this@CreateGroupActivity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    FilePickerBuilder.instance
                        .setMaxCount(1)
                        .setActivityTheme(R.style.FilePickerTheme)
                        .setActivityTitle("Please select image")
                        .enableVideoPicker(false)
                        .enableCameraSupport(true)
                        .showGifs(false)
                        .showFolderView(true)
                        .enableSelectAll(false)
                        .enableImagePicker(true)
                        .setCameraPlaceholder(R.drawable.ic_camera_profile)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .pickPhoto(this@CreateGroupActivity, 100)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                }

            }).setDeniedMessage(getString(R.string.permission_denied))
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            .check()
    }

    /**
     * Checking image picker and cropper result after image selection
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> if (resultCode == Activity.RESULT_OK && data != null) {
                val dataList =
                    data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)
                if (dataList != null) {
                    if (dataList.size > 0) {
                        openCropper(dataList[0])
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = result.uri

                    Glide.loadGlideProfileImage(
                        this,
                        ContentUriUtils.getFilePath(this, resultUri),
                        binding.imgGroupIcon
                    )
                    groupProfileImagePath = resultUri

                }
            }
        }
    }

    /**
     * Opening image cropper
     */
    private fun openCropper(uri: Uri) {
        CropImage.activity(uri).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1)
            .start(this@CreateGroupActivity)
    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(binding.edtGroupName.text.toString().trim())) {
            toast(getString(R.string.please_enter_group_name))
            return false
        }
        return true
    }

    /**
     * Set up observer
     */
    private fun setObserver() {

        groupChatViewModel.groupDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    redirectToHomeScreen()
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> toast(it1) }
                }
            }
        })

        groupChatViewModel.memberDetailsDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { user ->
                        userArrayList.addAll(user)
                        binding.txtNumberOfParticipants.text =
                            getString(R.string.number_of_participants, userArrayList.size)
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
     * redirecting home screen
     */
    private fun redirectToHomeScreen() {
        val intent = Intent(this@CreateGroupActivity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}