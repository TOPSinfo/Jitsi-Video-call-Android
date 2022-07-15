package com.app.demo.ui.dashboard.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.demo.data.repository.ChatRepository
import com.app.demo.model.group.GroupList
import com.app.demo.model.user.CallList
import com.app.demo.model.user.UserModel
import com.app.demo.model.user.UsersList
import com.app.demo.network.NetworkHelper
import com.app.demo.network.Resource
import com.app.demo.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {


    private val _userDataResponse: MutableLiveData<Resource<ArrayList<UserModel>>> =
        MutableLiveData()
    val userDataResponse: LiveData<Resource<ArrayList<UserModel>>> get() = _userDataResponse

    private val _addGroupCallDataResponse: MutableLiveData<Resource<String>> =
        MutableLiveData()

    val addGroupCallDataResponse: LiveData<Resource<String>> get() = _addGroupCallDataResponse

    private val _receiveGroupCallInvitationResponse: MutableLiveData<Resource<String>> =
        MutableLiveData()

    val receiveGroupCallInvitationResponse: LiveData<Resource<String>> get() = _receiveGroupCallInvitationResponse

    private val _updatedUserDataResponse: MutableLiveData<Resource<ArrayList<UserModel>>> =
        MutableLiveData()
    val updatedUserDataResponse: LiveData<Resource<ArrayList<UserModel>>> get() = _updatedUserDataResponse

    private val _userGroupDataResponse: MutableLiveData<Resource<ArrayList<UserModel>>> =
        MutableLiveData()
    val userGroupDataResponse: LiveData<Resource<ArrayList<UserModel>>> get() = _userGroupDataResponse


    fun getFirebaseDB() = chatRepository.firestoreDB

    /**
     * Getting registered user list from firebase
     */
    fun getUsersList() = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _userDataResponse.value = Resource.loading(null)

            chatRepository.getUserList()
                .addSnapshotListener(object : EventListener<QuerySnapshot?> {

                    override fun onEvent(
                        value: QuerySnapshot?,
                        error: FirebaseFirestoreException?
                    ) {
                        if (error != null) {
                            return
                        }

                        val userList = ArrayList<UserModel>()
                        val updateUserList = ArrayList<UserModel>()

                        for (dc in value?.documentChanges!!) {
                            when (dc.type) {

                                DocumentChange.Type.ADDED -> {
                                    val messageModel = UsersList.getUserModel(dc.document)
                                    if (messageModel.uid != FirebaseAuth.getInstance().currentUser?.uid.toString()) {
                                        userList.add(messageModel)
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {

                                    val messageModel = UsersList.getUserModel(dc.document)
                                    if (messageModel.uid != FirebaseAuth.getInstance().currentUser?.uid.toString()) {
                                        updateUserList.add(messageModel)
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                }
                            }
                        }

                        if (userList.size > 0) {
                            _userDataResponse.postValue(Resource.success(userList))
                        }
                        if (updateUserList.size > 0) {
                            _updatedUserDataResponse.postValue(Resource.success(updateUserList))
                        }
                    }
                })
        } else _userDataResponse.postValue(
            Resource.error(
                Constant.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }


    /**
     * Add GroupCall Data in database
     */
    fun setupGroupCallData(userIds: ArrayList<String>, callStatus: String, hostUserId: String,hostname:String) =
        viewModelScope.launch {
            if (networkHelper.isNetworkConnected()) {
                val doc = getFirebaseDB().collection(Constant.TABLE_GROUPCALL).document()
                val map = hashMapOf(
                    Constant.FIELD_USERIDS to userIds,
                    Constant.FIELD_CALL_STATUS to callStatus,
                    Constant.FIELD_HOST_ID to hostUserId,
                    Constant.FIELD_HOST_NAME to hostname
                )

                doc.set(map).addOnSuccessListener {
                    _addGroupCallDataResponse.postValue(Resource.success(doc.id))
                }.addOnFailureListener {
                    _addGroupCallDataResponse.postValue(Resource.success("Failed to add Data"))
                }
            } else _addGroupCallDataResponse.postValue(
                Resource.error(
                    Constant.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }


    /**
     * Receive notification when any one try to add you in group call
     */
    fun receiveGroupCallInvitation(currentUserId: String, callStatus: String) =
        viewModelScope.launch {
            if (networkHelper.isNetworkConnected()) {
                val doc = getFirebaseDB().collection(Constant.TABLE_GROUPCALL)
                    .whereArrayContains(Constant.FIELD_USERIDS, currentUserId)
                    .whereEqualTo(Constant.FIELD_CALL_STATUS, callStatus)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            _receiveGroupCallInvitationResponse.postValue(
                                Resource.error(
                                    "No Data",
                                    "Failed to connect"
                                )
                            )
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.documents.size > 0) {
                            for (i in 0..snapshot.documentChanges.size - 1) {
                                if (snapshot.documentChanges.get(i).type == DocumentChange.Type.ADDED) {
                                    _receiveGroupCallInvitationResponse.postValue(
                                        Resource.success(
                                            snapshot.documents.get(i).id
                                        )
                                    )
                                } else if (snapshot.documentChanges.get(i).type == DocumentChange.Type.MODIFIED) {
                                } else if (snapshot.documentChanges.get(i).type == DocumentChange.Type.REMOVED) {
                                }
                            }
                        } else {
                        }
                    }

            } else _addGroupCallDataResponse.postValue(
                Resource.error(
                    Constant.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }


    /**
     * get List of active call
     */
    fun getLIstOfActiveCall(currentUserId: String, callStatus: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {

            var userIdStatusCheckArray = ArrayList<String>()
            userIdStatusCheckArray.add(currentUserId + "___InActive")
            userIdStatusCheckArray.add(currentUserId + "___Active")

            var query = getFirebaseDB().collection(Constant.TABLE_GROUPCALL)
                .whereArrayContainsAny(Constant.FIELD_USERIDS, userIdStatusCheckArray)
                .whereEqualTo(Constant.FIELD_CALL_STATUS, callStatus)

            query.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _receiveGroupCallInvitationResponse.postValue(
                        Resource.error(
                            "No Data",
                            "Failed to connect"
                        )
                    )
                    return@addSnapshotListener
                }

                if (snapshot !== null) {
//                    Constant.listOfActiveCall.clear()
                    for (dc in snapshot.documentChanges!!) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val callModel = CallList.getCallList(dc.document)
                                if(callStatus.equals("Active")) {
                                    if(!Constant.listOfActiveCall.contains(callModel)) {
                                        Constant.listOfActiveCall.add(callModel)
                                    }
                                    Log.e("List Of New Active Call ADD===", "===" +callModel.docId)
                                }
                                else
                                {
                                    var positionOfInactiveCall=-1
                                    for(j in Constant.listOfActiveCall)
                                    {
                                        if(j.docId.equals(dc.document.id))
                                        {
                                            positionOfInactiveCall=Constant.listOfActiveCall.indexOf(j)
                                            if(positionOfInactiveCall!=-1)
                                            {
                                                Constant.listOfActiveCall.removeAt(positionOfInactiveCall)
                                            }
                                            break
                                        }
                                    }
                                }
                                Log.e("List Of Active Call===", "===" + Constant.listOfActiveCall.size)
                            }
                            DocumentChange.Type.MODIFIED -> {
                            }
                            DocumentChange.Type.REMOVED -> {

                            }
                        }
                    }
                }
            }

        } else _addGroupCallDataResponse.postValue(
            Resource.error(
                Constant.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    /**
     * get List of active call
     */
    fun getListOfGroup(currentUserId: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {

            var query = getFirebaseDB().collection(Constant.TABLE_GROUPDETAIL)
                .whereArrayContains(Constant.FIELD_GEOUP_MEMBERS, currentUserId)

            query.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                val groupList = ArrayList<UserModel>()
                if (snapshot !== null) {
                    for (dc in snapshot.documentChanges!!) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED -> {
                                val groupModel = GroupList.getGroupList(dc.document)
                                var userModel=UserModel()
                                userModel.isGroup=true
                                userModel.groupDetailModel=groupModel
                                userModel.createdAt=groupModel.createdAt
                                groupList.add(userModel)
                            }
                            DocumentChange.Type.MODIFIED -> {
                            }
                            DocumentChange.Type.REMOVED -> {

                            }
                        }
                    }

                    if (groupList.size > 0) {
                        _userGroupDataResponse.postValue(Resource.success(groupList))
                    }
                }
            }

        } else _userGroupDataResponse.postValue(
            Resource.error(
                Constant.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }


}