package com.app.demo.ui.jitsiCall.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.demo.data.repository.ChatRepository
import com.app.demo.model.CallListModel
import com.app.demo.network.NetworkHelper
import com.app.demo.network.Resource
import com.app.demo.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class JitsiViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val networkHelper: NetworkHelper,
) : ViewModel() {

    fun getFirebaseDB() = chatRepository.firestoreDB

    private val _updateUserStatusResponse: MutableLiveData<Resource<String>> =
        MutableLiveData()


    /*
    * Update status of call for user in firestore
    * */
    fun changeStatus(docId: String, isActive: Boolean) {
        if (networkHelper.isNetworkConnected()) {
            val docRef = getFirebaseDB().collection(Constant.TABLE_GROUPCALL).document(docId)

            docRef.get().addOnSuccessListener {
                var model = CallListModel(
                    it[Constant.FIELD_CALL_STATUS].toString(),
                    it[Constant.FIELD_HOST_ID].toString(),
                    it[Constant.FIELD_USERIDS] as ArrayList<String>,
                    it.id,
                    it[Constant.FIELD_HOST_NAME].toString()
                )

                var map = HashMap<String, Any>()


                if (isActive) {
                    var pos =
                        model.userIds.indexOf(FirebaseAuth.getInstance().currentUser?.uid.toString() + "___InActive")
                    if (pos < model.userIds.size && pos != -1) {
                        model.userIds[pos] =
                            FirebaseAuth.getInstance().currentUser?.uid.toString() + "___Active"
                    }
                } else {
                    var pos =
                        model.userIds.indexOf(FirebaseAuth.getInstance().currentUser?.uid.toString() + "___Active")
                    if (pos < model.userIds.size && pos != -1) {
                        model.userIds[pos] =
                            FirebaseAuth.getInstance().currentUser?.uid.toString() + "___InActive"
                    }
                }

                map.put(Constant.FIELD_CALL_STATUS, "InActive")
                for (i in model.userIds) {
                    if (i.endsWith("___Active")) {
                        map.put(Constant.FIELD_CALL_STATUS, "Active")
                    }
                }
                map.put(Constant.FIELD_USERIDS, model.userIds)
                docRef.update(map)

            }

        }
    }

}