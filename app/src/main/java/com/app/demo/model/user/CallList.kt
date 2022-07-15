package com.app.demo.model.user

import com.app.demo.model.CallListModel
import com.app.demo.model.chat.MessagesModel
import com.app.demo.utils.Constant
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

/**
 * object is used to get data of video call from firebase snapshot
 */
object CallList {

    fun getCallList(
        querySnapshot: QueryDocumentSnapshot
    ): CallListModel {

        val callModel = CallListModel()

        querySnapshot.get(Constant.FIELD_CALL_STATUS)?.let {
            callModel.callStatus = it.toString()
        }
        querySnapshot.get(Constant.FIELD_HOST_ID)?.let {
            callModel.HostId = it.toString()
        }
        querySnapshot.get(Constant.FIELD_USERIDS)?.let {
            callModel.userIds = it as ArrayList<String>
        }
        querySnapshot.get(Constant.FIELD_HOST_NAME)?.let {
            callModel.hostName = it.toString()
        }
        callModel.docId = querySnapshot.id

        return callModel
    }
}