package com.app.demo.model.user

import com.app.demo.model.chat.MessagesModel
import com.app.demo.utils.Constant
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot

object UsersList {

    /**
     * make array list of user from firestore snapshot
     */

//    fun getUserArrayList(
//        querySnapshot: QuerySnapshot,
//        userId: String
//    ): ArrayList<UserModel> {
//        val userArrayList = ArrayList<UserModel>()
//        for (doc in querySnapshot.documents) {
//            val userModel = UserModel()
//
//            doc.get(Constant.FIELD_UID)?.let {
//                userModel.uid = it.toString()
//            }
//            doc.get(Constant.FIELD_EMAIL)?.let {
//                userModel.email = it.toString()
//            }
//            doc.get(Constant.FIELD_LAST_NAME)?.let {
//                userModel.lastName = it.toString()
//            }
//            doc.get(Constant.FIELD_FIRST_NAME)?.let {
//                userModel.firstName = it.toString()
//            }
//            doc.get(Constant.FIELD_PHONE)?.let {
//                userModel.phone = it.toString()
//            }
//            if (doc.get(Constant.FIELD_UID) != userId) {
//                userArrayList.add(userModel)
//            }
//
//        }
//        return userArrayList
//    }

    /**
     * make message object and get message data from firestore snapshot
     */
    fun getMessagesModel(
        querySnapshot: QueryDocumentSnapshot
    ): MessagesModel {

        val messagesModel = MessagesModel()

        messagesModel.messageId = querySnapshot.id

        querySnapshot.get(Constant.FIELD_MESSAGE)?.let {
            messagesModel.message = it.toString()
        }
        querySnapshot.get(Constant.FIELD_SENDER_ID)?.let {
            messagesModel.senderId = it.toString()
        }
        querySnapshot.get(Constant.FIELD_RECEIVER_ID)?.let {
            messagesModel.receiverId = it.toString()
        }
        querySnapshot.get(Constant.FIELD_TIMESTAMP)?.let {
            messagesModel.timeStamp = it as Timestamp
        }

        querySnapshot.get(Constant.FIELD_MESSAGE_TYPE)?.let {
            messagesModel.messageType = it.toString()
        }

        querySnapshot.get(Constant.FIELD_URL)?.let {
            messagesModel.url = it.toString()
        }

        querySnapshot.get(Constant.FIELD_VIDEO_URL)?.let {
            messagesModel.videoUrl = it.toString()
        }
        querySnapshot.get(Constant.FIELD_MESSAGE_STATUS)?.let {
            messagesModel.status = it.toString()
        }

        return messagesModel
    }

    /**
     * make user object and get user data from firestore snapshot
     */
    fun getUserModel(
        querySnapshot: QueryDocumentSnapshot
    ): UserModel {

        val userModel = UserModel()

        querySnapshot.get(Constant.FIELD_UID)?.let {
            userModel.uid = it.toString()
        }
        querySnapshot.get(Constant.FIELD_EMAIL)?.let {
            userModel.email = it.toString()
        }
        querySnapshot.get(Constant.FIELD_LAST_NAME)?.let {
            userModel.lastName = it.toString()
        }
        querySnapshot.get(Constant.FIELD_FIRST_NAME)?.let {
            userModel.firstName = it.toString()
        }

        querySnapshot.get(Constant.FIELD_PHONE)?.let {
            userModel.phone = it.toString()
        }

        querySnapshot.get(Constant.FIELD_PROFILE_IMAGE)?.let {
            userModel.profileImage = it.toString()
        }

        querySnapshot.get(Constant.FIELD_IS_ONLINE)?.let {
            userModel.isOnline = it as Boolean
        }
        querySnapshot.get(Constant.FIELD_GROUP_CREATED_AT)?.let {
            userModel.createdAt = it as Timestamp
        }

        return userModel
    }


    /**
     * make user object and get user data from firestore snapshot
     */
    fun getUserDetail(
        querySnapshot: DocumentSnapshot
    ): UserModel {

        val userModel = UserModel()

        querySnapshot.get(Constant.FIELD_UID)?.let {
            userModel.uid = it.toString()
        }
        querySnapshot.get(Constant.FIELD_EMAIL)?.let {
            userModel.email = it.toString()
        }
        querySnapshot.get(Constant.FIELD_LAST_NAME)?.let {
            userModel.lastName = it.toString()
        }
        querySnapshot.get(Constant.FIELD_FIRST_NAME)?.let {
            userModel.firstName = it.toString()
        }

        querySnapshot.get(Constant.FIELD_PHONE)?.let {
            userModel.phone = it.toString()
        }

        querySnapshot.get(Constant.FIELD_PROFILE_IMAGE)?.let {
            userModel.profileImage = it.toString()
        }

        querySnapshot.get(Constant.FIELD_IS_ONLINE)?.let {
            userModel.isOnline = it as Boolean
        }
        querySnapshot.get(Constant.FIELD_GROUP_CREATED_AT)?.let {
            userModel.createdAt = it as Timestamp
        }

        return userModel
    }
}