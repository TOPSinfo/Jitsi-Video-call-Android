package com.app.demo.data.repository

import com.app.demo.utils.Constant
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject

class UserRepository @Inject constructor() {

    private var firestoreDB = FirebaseFirestore.getInstance()

    /**
     * Used to get user profile detail
     */
    fun getUserProfileRepository(userId: String): DocumentReference {
        return firestoreDB.collection(Constant.TABLE_USER).document(userId)
    }


}