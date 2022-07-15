package com.app.demo.model.group

import com.app.demo.utils.Constant
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot

/**
 * object is used to get data from firebase snapshot
 */
object GroupList {

    fun getGroupList(
        querySnapshot: QueryDocumentSnapshot
    ): GroupModel {

        val groupModel = GroupModel()

        querySnapshot.get(Constant.FIELD_ADMIN_ID)?.let {
            groupModel.adminId = it.toString()
        }
        querySnapshot.get(Constant.FIELD_ADMIN_NAME)?.let {
            groupModel.adminName = it.toString()
        }
        querySnapshot.get(Constant.FIELD_GROUP_CREATED_AT)?.let {
            groupModel.createdAt = it as Timestamp
        }
        querySnapshot.get(Constant.FIELD_GROUP_ICON)?.let {
            groupModel.groupIcon = it.toString()
        }
        querySnapshot.get(Constant.FIELD_GROUP_ID)?.let {
            groupModel.id = it.toString()
        }
        querySnapshot.get(Constant.FIELD_GEOUP_MEMBERS)?.let {
            groupModel.members = it as ArrayList<String>
        }
        querySnapshot.get(Constant.FIELD_GROUP_NAME)?.let {
            groupModel.name = it.toString()
        }

        return groupModel
    }
}