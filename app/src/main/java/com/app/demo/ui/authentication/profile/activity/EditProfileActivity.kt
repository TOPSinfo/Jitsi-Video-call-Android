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
import com.app.demo.databinding.ActivityEditProfileBinding
import com.app.demo.model.user.UserModel
import com.app.demo.network.Status
import com.app.demo.ui.authentication.profile.viewModel.ProfileViewModel
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
 * Activity is used to allow user to edit his profile data
 */
class EditProfileActivity : BaseActivity() {

    lateinit var binding: ActivityEditProfileBinding
    private val profileViewModel: ProfileViewModel by viewModels()

    var userModel: UserModel? = null
    var profileImagePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    /**
     * initialize all methods
     */
    private fun init() {
        setObserver()
        clickListener()
        profileViewModel.getUserDetail(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }

    /**
     * set button click listener
     */
    private fun clickListener() {

        binding.imgUser.setOnClickListener {
            pickImage()
        }

        binding.btnUpdate.setOnClickListener {
            if (checkValidation()) {
                userModel!!.firstName = binding.edFName.text.toString()
                userModel!!.lastName = binding.edLastName.text.toString()
                if (profileImagePath != null) {


                    profileImagePath?.let { it1 ->
                        profileViewModel.updateProfilePicture(
                            userModel!!,
                            it1,
                            true
                        )
                    }
                } else {
                    profileViewModel.updateUserData(userModel!!)
                }
            }
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    /**
     * set observer
     */
    private fun setObserver() {

        profileViewModel.userDetailResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        userModel = it
                        if (userModel != null) {
                            setUserData()
                        }

                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> toast(it1) }
                }
            }
        })

        profileViewModel.userDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let {
                        toast(it)
                        finish()

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
     * Setting user data
     */

    private fun setUserData() {
        userModel!!.firstName.let {
            binding.edFName.setText(it)
        }

        userModel!!.lastName.let {
            binding.edLastName.setText(it)
        }

        userModel!!.email.let {
            binding.edEmail.setText(it)
        }

        userModel!!.profileImage.let {
            Glide.loadGlideProfileImage(
                this,
                it,
                binding.imgUser
            )
        }
    }

    /**
     * Check camera and read write permission and open camera and image picker
     */

    private fun pickImage() {
        TedPermission.with(this@EditProfileActivity)
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
                        .pickPhoto(this@EditProfileActivity, 100)
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
            .start(this@EditProfileActivity)
    }


    /**
     * Checking validation
     */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(binding.edFName.text.toString().trim())) {
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


}