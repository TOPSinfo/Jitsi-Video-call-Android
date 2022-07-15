package com.app.demo.ui.groupChat.createGroup.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.demo.data.repository.ChatRepository
import com.app.demo.model.group.GroupList
import com.app.demo.model.group.GroupModel
import com.app.demo.model.user.UserModel
import com.app.demo.model.user.UsersList
import com.app.demo.network.NetworkHelper
import com.app.demo.network.Resource
import com.app.demo.utils.Constant
import com.app.demo.utils.Utility
import com.google.firebase.firestore.DocumentChange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {

    private val _groupDataResponse: MutableLiveData<Resource<String>> = MutableLiveData()
    val groupDataResponse: LiveData<Resource<String>> get() = _groupDataResponse


    private val _memberDetailsDataResponse: MutableLiveData<Resource<ArrayList<UserModel>>> =
        MutableLiveData()
    val memberDetailsDataResponse: LiveData<Resource<ArrayList<UserModel>>> get() = _memberDetailsDataResponse


    fun getFirebaseDB() = chatRepository.firestoreDB

    /**
     * uploading profile picture to firebase storage
     */
    fun updateGroupProfilePicture(group: GroupModel, profileImagePath: Uri?) {
        if (networkHelper.isNetworkConnected()) {
            _groupDataResponse.value = Resource.loading(null)

            if(profileImagePath!=null) {
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
                            group.groupIcon = it.toString()
                            Log.e("Group Profile Image", "======" + group.groupIcon)
                            createGroupData(group)
                        }
                    }
                }
            }
            else
            {
                createGroupData(group)
            }

        } else {
            _groupDataResponse.postValue(Resource.error(Constant.MSG_NO_INTERNET_CONNECTION, null))
        }
    }

    /**
     * Adding group info in firebase
     */
    private fun createGroupData(group: GroupModel) {
        val doc = getFirebaseDB().collection(Constant.TABLE_GROUPDETAIL).document()
        _groupDataResponse.value = Resource.loading(null)
        val data = hashMapOf(
            Constant.FIELD_ADMIN_ID to group.adminId,
            Constant.FIELD_ADMIN_NAME to group.adminName,
            Constant.FIELD_GROUP_CREATED_AT to group.createdAt!!.toDate(),
            Constant.FIELD_GROUP_ICON to group.groupIcon,
            Constant.FIELD_GROUP_ID to doc.id,
            Constant.FIELD_GEOUP_MEMBERS to group.members,
            Constant.FIELD_GROUP_NAME to group.name,
        )

        doc.set(data)
            .addOnSuccessListener {
                _groupDataResponse.postValue(Resource.success(("")))
            }
            .addOnFailureListener {
                _groupDataResponse.postValue(Resource.error(it.localizedMessage, null))
            }
    }

    /**
     * get list of group member details
     */
    fun getListOfMemberInGroup(membersId:ArrayList<String>) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            var query = getFirebaseDB().collection(Constant.TABLE_USER)
                .whereIn(Constant.FIELD_UID, membersId)

            query.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                val userList = ArrayList<UserModel>()
                if (snapshot !== null) {
                    for(i in snapshot) {
                        val userModel = UsersList.getUserModel(i)
                        userList.add(userModel)
                    }
                    if (userList.size > 0) {
                        _memberDetailsDataResponse.postValue(Resource.success(userList))
                    }
                }
            }

        } else _memberDetailsDataResponse.postValue(
            Resource.error(
                Constant.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

}