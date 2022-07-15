package com.app.demo.ui.authentication.profile.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.demo.data.repository.UserRepository
import com.app.demo.model.user.UserModel
import com.app.demo.model.user.UsersList
import com.app.demo.network.NetworkHelper
import com.app.demo.network.Resource
import com.app.demo.utils.Constant
import com.app.demo.utils.Utility
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper,
) :
    ViewModel() {

    private val _userDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val userDataResponse: LiveData<Resource<String>> get() = _userDataResponse

    var isUserOnline: MutableLiveData<Boolean> = MutableLiveData()

    private val _userDetailResponse: MutableLiveData<Resource<UserModel>> = MutableLiveData()
    val userDetailResponse: LiveData<Resource<UserModel>> get() = _userDetailResponse


    /**
     * uploading profile picture to firebase storage
     */
    fun updateProfilePicture(user: UserModel, profileImagePath: Uri, isForUpdate: Boolean) {

        _userDataResponse.value = Resource.loading(null)
        if (networkHelper.isNetworkConnected()) {

            if (isForUpdate) {
                if (profileImagePath != null) {//Update Data If image is Update


                    var pictureRef =
                        Utility.storageRef.storage.getReferenceFromUrl(user.profileImage!!)

                    // Delete the file
                    pictureRef.delete().addOnSuccessListener {
                        Log.e("Image Deleted", " Successfully")

                    }.addOnFailureListener {
                        Log.e("Image Deleted", " ==" + it.message)
                    }
                }
            }

            val frontCardPath =
                "${Constant.PROFILE_IMAGE_PATH}/${System.currentTimeMillis()}.jpg"
            val filepath = Utility.storageRef.child(frontCardPath)
            filepath.putFile(profileImagePath).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                filepath.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downUri: Uri? = task.result
                    downUri?.let {
                        user.profileImage = it.toString()
                        if (isForUpdate) {
                            updateUserData(user)
                        } else {
                            addUserData(user)
                        }

                    }
                }
            }
        } else {
            _userDataResponse.value = Resource.error(Constant.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Adding user info in firebase
     */
    fun addUserData(user: UserModel) {

        _userDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()
        val data = hashMapOf(
            Constant.FIELD_UID to user.uid,
            Constant.FIELD_FIRST_NAME to user.firstName,
            Constant.FIELD_LAST_NAME to user.lastName,
            Constant.FIELD_PHONE to user.phone,
            Constant.FIELD_EMAIL to user.email,
            Constant.FIELD_PROFILE_IMAGE to user.profileImage,
            Constant.FIELD_IS_ONLINE to user.isOnline,
            Constant.FIELD_GROUP_CREATED_AT to user.createdAt,
        )
        userRepository.getUserProfileRepository(user.uid.toString()).set(data)
            .addOnSuccessListener {
                _userDataResponse.postValue(
                    Resource.success(
                        Constant.validation_error,
                    )
                )
            }.addOnFailureListener {

                _userDataResponse.postValue(
                    Resource.error(
                        Constant.validation_error,
                        null
                    )
                )
            }
    }

    /**
     * Update user presence online/offline
     */
    fun updateUserPresence(isOnline: Boolean, userId: String) {

        userRepository.getUserProfileRepository(userId)
            .update(
                mapOf(
                    Constant.FIELD_IS_ONLINE to isOnline,
                )
            )
    }

    /**
     * Get user presence of selected user
     */
    fun getUserPresenceUpdateListener(otherUserId: String) {

        userRepository.getUserProfileRepository(otherUserId)
            .addSnapshotListener(object : EventListener<DocumentSnapshot?> {

                override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        return
                    }

                    value?.get(Constant.FIELD_IS_ONLINE)?.let {
                        isUserOnline.value = it as Boolean
                    }
                }
            })
    }

    /**
     * Get user presence of selected user
     */
    fun getUserDetail(userId: String) {

        if (networkHelper.isNetworkConnected()) {
            _userDetailResponse.value = Resource.loading(null)
            userRepository.getUserProfileRepository(userId)
                .get().addOnSuccessListener {

                    var userModel = UsersList.getUserDetail(it)
                    it?.get(Constant.FIELD_FIRST_NAME)?.let {
                        Constant.USER_NAME = it.toString()
                    }
                    it?.get(Constant.FIELD_LAST_NAME)?.let {
                        Constant.USER_NAME = Constant.USER_NAME + " " + it.toString()
                    }

                    it?.get(Constant.FIELD_PROFILE_IMAGE)?.let {
                        Constant.USER_PROFILE_IMAGE = it.toString()
                    }

                    _userDetailResponse.postValue(Resource.success(userModel))
                    Log.e("Name====", "======" + Constant.USER_NAME)
                }
        } else {
            _userDetailResponse.value = Resource.error(Constant.MSG_NO_INTERNET_CONNECTION, null)
        }

    }

    /**
     * Updating user info in firebase
     */
    fun updateUserData(user: UserModel) {

        _userDataResponse.value = Resource.loading(null)
        user.createdAt = Timestamp.now()

        var data1 = HashMap<String, Any>()
        user.firstName?.let { data1.put(Constant.FIELD_FIRST_NAME, it) }
        user.lastName?.let { data1.put(Constant.FIELD_LAST_NAME, it) }
        user.profileImage?.let { data1.put(Constant.FIELD_PROFILE_IMAGE, it) }

        userRepository.getUserProfileRepository(user.uid.toString()).update(data1)
            .addOnSuccessListener {
                _userDataResponse.postValue(
                    Resource.success(
                        Constant.MSG_UPDATE_SUCCESSFULL,
                    )
                )
            }.addOnFailureListener {

                _userDataResponse.postValue(
                    Resource.error(
                        Constant.validation_error,
                        null
                    )
                )
            }
    }

}