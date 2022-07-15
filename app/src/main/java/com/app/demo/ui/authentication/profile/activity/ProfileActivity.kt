package com.app.demo.ui.authentication.profile.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.viewModels
import com.app.demo.R
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivityProfileBinding
import com.app.demo.model.user.UserModel
import com.app.demo.network.Status
import com.app.demo.ui.authentication.profile.viewModel.ProfileViewModel
import com.app.demo.ui.dashboard.activity.DashboardActivity
import com.app.demo.utils.Glide
import com.app.demo.utils.Utility
import com.app.demo.utils.toast
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils

/**
 * Activity class is used to create use profile
 */
class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var userId: String? = null
    private var phoneNumber: String? = null

    private val profileViewModel: ProfileViewModel by viewModels()
    var profileImagePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * Initialize user interface
     */
    private fun initUI() {
        val fb = FirebaseAuth.getInstance().currentUser
        userId = fb?.uid.toString()
        phoneNumber = fb?.phoneNumber.toString()
        setClickListener()
        setObserver()
    }

    /**
     * set observer
     */
    private fun setObserver() {

        profileViewModel.userDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        redirectDashboard()
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
     * Set click listener
     */
    private fun setClickListener() {
        binding.btnRegister.setOnClickListener {
            if (checkValidation()) {
                val userModel = UserModel()
                userModel.uid = userId
                userModel.email = binding.edEmail.text.toString().trim()
                userModel.firstName = binding.edFName.text.toString().trim()
                userModel.lastName = binding.edLastName.text.toString().trim()
                userModel.phone = phoneNumber
                userModel.isOnline = false
                profileImagePath?.let { it1 ->
                    profileViewModel.updateProfilePicture(
                        userModel,
                        it1,
                        false
                    )
                }
            }
        }
        binding.imgUser.setOnClickListener {
            pickImage()
        }
    }

    /**
     * Check camera and read write permission and open camera and image picker
     */
    private fun pickImage() {
        TedPermission.with(this@ProfileActivity)
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
                        .setCameraPlaceholder(R.drawable.ic_camera)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .pickPhoto(this@ProfileActivity, 100)
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
                        binding.imgUser
                    )
                    profileImagePath = resultUri

                }
            }
        }
    }

    /**
     * Opening image cropper
     */
    private fun openCropper(uri: Uri) {
        CropImage.activity(uri).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1)
            .start(this@ProfileActivity)
    }

    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        if (profileImagePath == null) {
            toast(getString(R.string.please_select_profile_picture))
            return false
        } else if (TextUtils.isEmpty(binding.edFName.text.toString().trim())) {
            toast(getString(R.string.please_enter_first_name))
            return false
        } else if (TextUtils.isEmpty(binding.edLastName.text.toString().trim())) {
            toast(getString(R.string.please_enter_last_name))
            return false
        } else if (TextUtils.isEmpty(binding.edEmail.text.toString().trim())) {
            toast(getString(R.string.please_enter_email_address))
            return false
        } else if (!Utility.emailValidator(binding.edEmail.text.toString().trim())) {
            toast(getString(R.string.please_enter_valid_email_address))
            return false
        }
        return true
    }

    /**
     * Redirecting to dashboard
     */
    private fun redirectDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}