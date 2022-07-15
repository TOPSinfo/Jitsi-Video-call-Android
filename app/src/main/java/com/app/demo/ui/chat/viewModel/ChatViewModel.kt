package com.app.demo.ui.chat.viewModel

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.demo.data.repository.ChatRepository
import com.app.demo.model.chat.MessagesModel
import com.app.demo.model.user.UserModel
import com.app.demo.model.user.UsersList
import com.app.demo.network.NetworkHelper
import com.app.demo.network.Resource
import com.app.demo.utils.Constant
import com.app.demo.utils.Utility
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {


    private val _messagesResponse: MutableLiveData<Resource<ArrayList<MessagesModel>>> =
        MutableLiveData()
    val messageDataResponse: LiveData<Resource<ArrayList<MessagesModel>>> get() = _messagesResponse

    private val _updatedMessagesResponse: MutableLiveData<Resource<ArrayList<MessagesModel>>> =
        MutableLiveData()
    val updatedMessagesResponse: LiveData<Resource<ArrayList<MessagesModel>>> get() = _updatedMessagesResponse

    private val _sendMessagesResponse: MutableLiveData<Resource<Boolean>> =
        MutableLiveData()
    val sendMessagesResponse: LiveData<Resource<Boolean>> get() = _sendMessagesResponse

    private val _addGroupCallDataResponse: MutableLiveData<Resource<String>> =
        MutableLiveData()

    val addGroupCallDataResponse: LiveData<Resource<String>> get() = _addGroupCallDataResponse


    fun getFirebaseDB() = chatRepository.firestoreDB


    /**
     * Get chat messages list and single message also on runtime
     */
    fun getMessagesList(otherUserId: String, isGroup: Boolean,userList:ArrayList<UserModel>) = viewModelScope.launch {

        if (networkHelper.isNetworkConnected()) {
            var docId=""
            if(isGroup)
            {
                 docId = otherUserId
            }
            else
            {
                 docId = getCurrentDocumentId(otherUserId)
            }

            chatRepository.getChatMessageRepository(docId)
                .addSnapshotListener(object : EventListener<QuerySnapshot?> {
                    override fun onEvent(
                        value: QuerySnapshot?,
                        error: FirebaseFirestoreException?
                    ) {
                        if (error != null) {
                            return
                        }
                        val chatList = ArrayList<MessagesModel>()
                        val modifiedChatList = ArrayList<MessagesModel>()

                        for (dc in value?.documentChanges!!) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    println("ABC::Added::" + dc.document.data)
                                    val messageModel = UsersList.getMessagesModel(dc.document)

                                    if(isGroup) {
                                        var user: UserModel? =
                                            userList.find { it.uid == messageModel.senderId }
                                        Log.e("UserData=====", "====" + user?.firstName + " " + user?.lastName)
                                        messageModel.senderName =
                                            user?.firstName + " " + user?.lastName
                                    }
                                    chatList.add(messageModel)
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    println("ABC::Modified::" + dc.document.data)
                                    val messageModel = UsersList.getMessagesModel(dc.document)
                                    if(isGroup) {
                                        var user: UserModel? =
                                            userList.find { it.uid == messageModel.senderId }
                                        Log.e("UserData=====", "====" + user?.firstName + " " + user?.lastName)
                                        messageModel.senderName =
                                            user?.firstName + " " + user?.lastName
                                    }
                                    modifiedChatList.add(messageModel)
                                }
                                DocumentChange.Type.REMOVED -> println("ABC::Removed::" + dc.document.data)
                            }
                        }

                        if (chatList.size > 0) {
                            _messagesResponse.postValue(Resource.success(chatList))
                        }

                        if (modifiedChatList.size > 0) {
                            _updatedMessagesResponse.postValue(Resource.success(modifiedChatList))
                        }
                    }
                })
        } else _messagesResponse.postValue(
            Resource.error(
                Constant.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }


    /**
     * Send message to other user
     */
    fun sendMessage(messagesModel: MessagesModel, isAlreadyMessageId: Boolean,isGroup:Boolean) =
        viewModelScope.launch {
            if (networkHelper.isNetworkConnected()) {
                val doc:CollectionReference
                if(isGroup)
                {
                    doc =
                        chatRepository.getSendMessagePath(messagesModel.receiverId.toString())
                }
                else {
                     doc =
                        chatRepository.getSendMessagePath(getCurrentDocumentId(messagesModel.receiverId.toString()))
                }
                var timestamp = Timestamp(Date())
                if (isAlreadyMessageId) {
                    timestamp = messagesModel.timeStamp!!
                }

                val data = hashMapOf(
                    Constant.FIELD_MESSAGE to messagesModel.message,
                    Constant.FIELD_RECEIVER_ID to messagesModel.receiverId.toString(),
                    Constant.FIELD_SENDER_ID to messagesModel.senderId.toString(),
                    Constant.FIELD_TIMESTAMP to timestamp,
                    Constant.FIELD_MESSAGE_TYPE to messagesModel.messageType,
                    Constant.FIELD_URL to messagesModel.url,
                    Constant.FIELD_VIDEO_URL to messagesModel.videoUrl,
                    Constant.FIELD_MESSAGE_STATUS to messagesModel.status,
                )

                if (isAlreadyMessageId) {

                    doc.document(messagesModel.messageId.toString()).set(data)
                        .addOnSuccessListener {
                            _sendMessagesResponse.postValue(Resource.success(true))
                        }.addOnFailureListener {
                            _sendMessagesResponse.postValue(Resource.success(false))
                        }

                } else {

                    doc.document().set(data).addOnSuccessListener {
                        _sendMessagesResponse.postValue(Resource.success(true))
                    }.addOnFailureListener {
                        _sendMessagesResponse.postValue(Resource.success(false))
                    }
                }

            } else _sendMessagesResponse.postValue(
                Resource.error(
                    Constant.MSG_NO_INTERNET_CONNECTION,
                    null
                )
            )
        }


    /**
     * Get document id of both user of chat
     */
    private fun getCurrentDocumentId(otherUserId: String): String {
        val myUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        return if (myUserId < otherUserId) {
            myUserId + "" + otherUserId
        } else {
            otherUserId + myUserId
        }
    }

    /**
     * Getting video file thumb for upload to firebase
     */

    private fun getVideoThumb(
        messagesModel: MessagesModel,
        localVideoPath: String,
        progressBar: CircularProgressIndicator?,
        isGroup: Boolean
    ) {
        var imageSavedPath: File? = null
        val file = File(localVideoPath)
        val thumbnail = ThumbnailUtils.createVideoThumbnail(
            file.absolutePath,
            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND
        )

        val fOut: FileOutputStream
        try {

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                Date()
            )

            imageSavedPath = Utility.getAndroidDefaultMediaPath("$timeStamp.png")
            fOut = FileOutputStream(imageSavedPath)
            thumbnail!!.compress(Bitmap.CompressFormat.PNG, 85, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        uploadChatImageWithProgress(Uri.fromFile(imageSavedPath), messagesModel, progressBar!!,isGroup)

    }

    /**
     * Set message status READ to firebase
     */
    fun setMessagesReadStatus(messagesList: ArrayList<MessagesModel>) {

        for (i in messagesList.indices) {
            val messagesModel = messagesList[i]
            val doc =
                chatRepository.getMessageDetailData(
                    getCurrentDocumentId(messagesModel.senderId.toString()),
                    messagesModel.messageId.toString()
                )
            doc.update(
                mapOf(
                    Constant.FIELD_MESSAGE_STATUS to Constant.TYPE_READ,
                )
            )
        }
    }

    /**
     * uploading  picture to firebase storage
     */
    fun uploadChatImageWithProgress(
        profileImagePath: Uri,
        messagesModel: MessagesModel,
        progressBar: CircularProgressIndicator,
        isGroup: Boolean
    ) {
        if (networkHelper.isNetworkConnected()) {
            progressBar.progress = 10
            val frontCardPath =
                "${Constant.CHAT_IMAGE_PATH}/${System.currentTimeMillis()}.jpg"
            val filepath = Utility.storageRef.child(frontCardPath)

            val uploadTask = filepath.putFile(profileImagePath)

            try {
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    println("Uploading in progress :$progress% done")
                    progressBar.progress = progress.toInt()
                }

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        _sendMessagesResponse.postValue(
                            Resource.error(
                                Constant.validation_error,
                                null
                            )
                        )
                        throw task.exception!!
                    }
                    filepath.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downUri: Uri? = task.result
                        downUri?.let {
                            messagesModel.url = it.toString()
                            messagesModel.status = Constant.TYPE_SEND
                            sendMessage(messagesModel, true,isGroup)
                            println("Uploading url:$ $it")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


        } else _sendMessagesResponse.postValue(
            Resource.error(
                Constant.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }

    /**
     * Get document id for chat
     */
    fun getChatDocumentId(otherUserId: String,isGroup: Boolean): String {
        if(isGroup)
        {
            return chatRepository.getMessageDocumentId(otherUserId)
        }
        else {
            return chatRepository.getMessageDocumentId(getCurrentDocumentId(otherUserId))
        }
    }

    /**
     * uploading video to firebase storage
     */
    fun uploadChatVideoWithProgress(
        profileImagePath: Uri,
        messagesModel: MessagesModel,
        videoExt: String,
        videoPath: String, progressBar: CircularProgressIndicator,
        isGroup: Boolean
    ) {
        if (networkHelper.isNetworkConnected()) {

            val frontCardPath =
                "${Constant.CHAT_VIDEO_PATH}/${System.currentTimeMillis()}.$videoExt"

            val filepath = Utility.storageRef.child(frontCardPath)

            val uploadTask = filepath.putFile(profileImagePath)

            try {
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    println("Uploading in progress :$progress% done")
                    progressBar.progress = progress.toInt()
                }

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        _sendMessagesResponse.postValue(
                            Resource.error(
                                Constant.validation_error,
                                null
                            )
                        )
                        throw task.exception!!
                    }
                    filepath.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downUri: Uri? = task.result
                        downUri?.let {
                            messagesModel.videoUrl = it.toString()
                            messagesModel.status = Constant.TYPE_SEND
                            //    sendMessage(messagesModel, true)
                            println("Uploading url:$ $it")
                            progressBar.progress = 80
                            getVideoThumb(messagesModel, videoPath, progressBar,isGroup)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else _sendMessagesResponse.postValue(
            Resource.error(
                Constant.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }


    /**
     * Add video Call Data in database
     */
    fun setupVideoCallData(userIds: ArrayList<String>, callStatus: String, hostUserId: String,hostname:String) =
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

}