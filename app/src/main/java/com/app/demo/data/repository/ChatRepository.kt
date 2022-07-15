package com.app.demo.data.repository

import com.app.demo.utils.Constant
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject

class ChatRepository @Inject constructor() {

    var firestoreDB = FirebaseFirestore.getInstance()

    /**
     * Used to get chat message from firestore document id
     */
    fun getChatMessageRepository(docId: String): Query {
        return firestoreDB.collection(Constant.TABLE_MESSAGES)
            .document(docId)
            .collection(Constant.TABLE_MESSAGE_COLLECTION)
            .orderBy(Constant.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
    }

    /**
     * Used to get user list from firestore document id
     */
    fun getUserList(): CollectionReference {
        return firestoreDB.collection(Constant.TABLE_USER)
    }

    /**
     * Used to get path of message form firestore document id
     */
    fun getSendMessagePath(docId: String): CollectionReference {
        return firestoreDB.collection(Constant.TABLE_MESSAGES).document(docId)
            .collection(Constant.TABLE_MESSAGE_COLLECTION)
    }

    /**
     * Used to get detail of message
     */
    fun getMessageDetailData(messageDocId: String, messageId: String): DocumentReference {
        return firestoreDB.collection(Constant.TABLE_MESSAGES).document(messageDocId)
            .collection(Constant.TABLE_MESSAGE_COLLECTION).document(messageId)
    }

    /**
     * Used to get message id from firestore
     */
    fun getMessageDocumentId(messageDocId: String): String {
        return firestoreDB.collection(Constant.TABLE_MESSAGES).document(messageDocId)
            .collection(Constant.TABLE_MESSAGE_COLLECTION).document().id
    }

}