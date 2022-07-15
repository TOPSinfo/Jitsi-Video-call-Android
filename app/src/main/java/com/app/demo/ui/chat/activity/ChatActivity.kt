package com.app.demo.ui.chat.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.demo.R
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivityChatBinding
import com.app.demo.model.chat.MessagesModel
import com.app.demo.model.user.UserModel
import com.app.demo.network.Status
import com.app.demo.ui.authentication.profile.viewModel.ProfileViewModel
import com.app.demo.ui.chat.adapter.ChatAdapter
import com.app.demo.ui.chat.viewModel.ChatViewModel
import com.app.demo.ui.groupChat.createGroup.activity.CreateGroupActivity
import com.app.demo.ui.jitsiCall.activity.JitsiCallActivity
import com.app.demo.utils.Constant
import com.app.demo.utils.Utility
import com.app.demo.utils.toast
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.common.reflect.TypeToken
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.gowtham.library.utils.CompressOption
import com.gowtham.library.utils.LogMessage
import com.gowtham.library.utils.TrimType
import com.gowtham.library.utils.TrimVideo
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import droidninja.filepicker.utils.ContentUriUtils
import java.io.File
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList


class ChatActivity : BaseActivity() {

    private val chatViewModel: ChatViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var binding: ActivityChatBinding
    private var otherUserId: String? = null

    private var chatAdapter: ChatAdapter? = null
    private val messageList = ArrayList<MessagesModel>()
    private var opponentUserName: String = ""
    private var isGroup: Boolean = false
    private var groupIcon: String = ""
    private var memberIdList: String = ""
    private var userList = ArrayList<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }

    /**
     * Initialize ui and call method
     */
    private fun initUI() {

        otherUserId = intent.getStringExtra("user_id").toString()
        isGroup = intent.getBooleanExtra("isGroup", false)
        opponentUserName = intent.getStringExtra("user_name").toString()
        groupIcon = intent.getStringExtra("group_image").toString()

        val type: Type = object : TypeToken<ArrayList<UserModel>>() {}.type
        userList = Gson().fromJson(intent.getStringExtra("userList").toString(), type)

        if (isGroup) {
            memberIdList = intent.getStringExtra("memberIdList").toString()
        }

        binding.txtUserName.text = opponentUserName

        setChatAdapter()
        setObserver()
        setClickListener()
        getChatMessagesList()
        if (!isGroup) {
            binding.txtPresence.visibility = View.VISIBLE
            binding.imgGroupInformation.visibility = View.GONE
            getUserPresence()

        } else {
            binding.txtPresence.visibility = View.GONE
            binding.imgGroupInformation.visibility = View.VISIBLE
        }
    }

    /**
     * Get selected user status online/offline
     */
    private fun getUserPresence() {
        profileViewModel.getUserPresenceUpdateListener(otherUserId.toString())
    }


    /**
     * Set up observer
     */
    private fun setObserver() {

        profileViewModel.isUserOnline.observe(this@ChatActivity, {
            binding.txtPresence.text = if (it) {
                getString(R.string.online)
            } else {
                getString(R.string.offline)
            }
        })

        chatViewModel.messageDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { user ->
                        if (user.size > 1) {
                            messageList.addAll(user)
                            setChatAdapter()
                        } else {
                            if (user.size == 1) {
                                if (user[0].messageType == Constant.TYPE_MESSAGE) {
                                    messageList.addAll(user)
                                    chatAdapter?.notifyDataSetChanged()
                                    binding.rvChatMessageList.scrollToPosition(messageList.size - 1)
                                } else if (user[0].messageType == Constant.TYPE_IMAGE || user[0].messageType == Constant.TYPE_VIDEO) {

                                    var isFoundData = false

                                    for (i in messageList.indices) {
                                        if (messageList[i].messageId == user[0].messageId) {
                                            messageList[i] = user[0]
                                            isFoundData = true
                                        }
                                    }
                                    if (!isFoundData) {
                                        messageList.addAll(user)
                                    }
                                    messageList.sortedBy { it.timeStamp }
                                    chatAdapter?.notifyDataSetChanged()
                                    binding.rvChatMessageList.scrollToPosition(messageList.size - 1)
                                }
                            }
                            updateMessageReadStatus()
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> toast(it1) }
                }
            }
        })
        chatViewModel.updatedMessagesResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    it.data?.let { user ->
                        if (user.size > 0) {
                            updateMessagesData(user)
                        }
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> toast(it1) }
                }
            }
        })
        chatViewModel.sendMessagesResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    hideProgress()
                    binding.edMessage.setText("")
                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> toast(it1) }
                }
            }
        })
        chatViewModel.addGroupCallDataResponse.observe(this, {
            when (it.status) {
                Status.LOADING -> {
                    showProgress(this)
                }
                Status.SUCCESS -> {
                    it.data.let {
                        val intent = Intent(
                            this@ChatActivity,
                            JitsiCallActivity::class.java
                        )
                        intent.putExtra("RoomId", it!!)
                        intent.putExtra("OpponentUserName", "")
                        intent.putExtra("isGroupCall", isGroup)
                        startActivity(intent)
                    }

                }
                Status.ERROR -> {
                    hideProgress()
                    it.message?.let { it1 -> toast(it1) }
                }
            }
        })

    }

    /**
     * Initialize click listener
     */
    private fun setClickListener() {

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.rlSend.setOnClickListener {
            sendMessage()
        }
        binding.imgVideoCall.setOnClickListener {

            TedPermission.with(this@ChatActivity)
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                        val userIds = ArrayList<String>()
                        if (isGroup) {
                            var selectedMemberIdList = ArrayList<String>()
                            if (!memberIdList.equals("")) {
                                val type: Type = object : TypeToken<ArrayList<String>>() {}.type
                                selectedMemberIdList = Gson().fromJson(memberIdList, type)
                            }
                            userIds.addAll(selectedMemberIdList)
                        } else {
                            userIds.add(currentUserId)
                            userIds.add(otherUserId!!)
                        }

                        var isCallActive = false
                        for (i in Constant.listOfActiveCall) {
                            var isAllAvailable = true
                            for (j in userIds) {
                                if ((i.userIds.contains(j + "___Active") || i.userIds.contains(j + "___InActive"))) {
                                } else {
                                    isAllAvailable = false
                                    break
                                }
                            }

                            if (isAllAvailable && userIds.size == i.userIds.size) {
                                isCallActive = true
                                val intent =
                                    Intent(this@ChatActivity, JitsiCallActivity::class.java)
                                intent.putExtra("RoomId", i.docId)
                                intent.putExtra("OpponentUserName", opponentUserName)
                                intent.putExtra("isGroupCall", isGroup)
                                startActivity(intent)
                                break
                            }
                        }

                        if (!isCallActive) {
                            setupVideoCall(userIds, currentUserId)
                        }
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    }

                }).setDeniedMessage(getString(R.string.permission_denied))
                .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                .check()
        }

        binding.imgCamera.setOnClickListener {
            pickImage()
        }

        binding.imgGroupInformation.setOnClickListener {
            openGroupDetailForEdit()
        }

    }


    /**
     * Get chat messages list
     */
    private fun getChatMessagesList() {
        messageList.clear()
        chatViewModel.getMessagesList(otherUserId.toString(), isGroup, userList)
    }


    /**
     * Send text message to other user
     */
    private fun sendMessage() {

        if (TextUtils.isEmpty(binding.edMessage.text.toString().trim())) {
            toast(getString(R.string.please_enter_message))
        } else {
            val messagesModel = MessagesModel()

            messagesModel.message = binding.edMessage.text.toString().trim()
            messagesModel.messageType = Constant.TYPE_MESSAGE
            messagesModel.receiverId = otherUserId.toString()
            messagesModel.senderId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            messagesModel.url = ""
            messagesModel.videoUrl = ""
            messagesModel.status = Constant.TYPE_SEND

            chatViewModel.sendMessage(
                messagesModel, false, isGroup
            )
            binding.edMessage.setText("")
        }
    }

    /**
     * Set chat messages adapter
     */
    private fun setChatAdapter() {
        binding.rvChatMessageList.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(this, messageList, isGroup)
        binding.rvChatMessageList.adapter = chatAdapter
        binding.rvChatMessageList.scrollToPosition(messageList.size - 1)

        updateMessageReadStatus()
    }

    /**
     * Update messages already showing in adapter
     */
    private fun updateMessagesData(updateMessagesList: ArrayList<MessagesModel>) {
        for (i in updateMessagesList.indices) {
            for (j in messageList.indices) {
                if (messageList[j].messageId?.equals(updateMessagesList[i].messageId.toString()) == true) {
                    messageList[j] = updateMessagesList[i]
                    chatAdapter?.notifyItemChanged(j)
                }
            }
        }
    }

    /**
     * Update message read status in firebase
     */
    private fun updateMessageReadStatus() {
        val tempMessageList = ArrayList<MessagesModel>()
        for (i in messageList.indices) {
            if (messageList[i].senderId != FirebaseAuth.getInstance().currentUser?.uid.toString()) {
                if (messageList[i].status != Constant.TYPE_READ) {
                    tempMessageList.add(messageList[i])
                }
            }
        }

        if (tempMessageList.size > 0) {
            chatViewModel.setMessagesReadStatus(tempMessageList)
        }
    }

    /**
     * Check camera and read write permission and open camera and image picker
     */
    private fun pickImage() {
        TedPermission.with(this@ChatActivity)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {

                    FilePickerBuilder.instance
                        .setMaxCount(1)
                        .setActivityTheme(R.style.FilePickerTheme)
                        .setActivityTitle("Please select image")
                        .enableVideoPicker(true)
                        .enableCameraSupport(true)
                        .showGifs(false)
                        .showFolderView(true)
                        .enableSelectAll(false)
                        .enableImagePicker(true)
                        .setCameraPlaceholder(R.drawable.ic_camera)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .pickPhoto(this@ChatActivity, 100)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                }

            }).setDeniedMessage(getString(R.string.permission_denied))
            .setPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
            )
            .check()
    }

    /**
     * Checking image picker and cropper result after image selection
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> if (resultCode == Activity.RESULT_OK && data != null) {
                val dataList =
                    data.getParcelableArrayListExtra<Uri>(FilePickerConst.KEY_SELECTED_MEDIA)

                if (Utility.isVideoFile(ContentUriUtils.getFilePath(this, dataList!![0]))) {
                    redirectTrimVideo(dataList[0].toString())
                } else {
                    if (dataList.size > 0) {
                        openCropper(dataList[0])
                    }
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = result.uri
                    uploadImage(resultUri)
                }
            }
        }
    }

    /**
     * Uploading image to firebase storage for chat
     */
    private fun uploadImage(profileImagePath: Uri) {

        val messagesModel = MessagesModel()

        messagesModel.messageId = chatViewModel.getChatDocumentId(otherUserId.toString(), isGroup)
        messagesModel.message = binding.edMessage.text.toString().trim()
        messagesModel.messageType = Constant.TYPE_IMAGE
        messagesModel.receiverId = otherUserId.toString()
        messagesModel.senderId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        messagesModel.url = ContentUriUtils.getFilePath(this, profileImagePath)
        messagesModel.videoUrl = ""
        messagesModel.status = Constant.TYPE_START_UPLOAD
        messageList.add(messagesModel)
        chatAdapter?.notifyItemInserted(messageList.size - 1)
        binding.rvChatMessageList.scrollToPosition(messageList.size - 1)
    }


    /**
     * Redirecting to video play activity
     */
    fun uploadImageWithProgress(
        url: String, messagesModel: MessagesModel,
        progressBar: CircularProgressIndicator,
    ) {
        messagesModel.timeStamp = Timestamp(Date())
        chatViewModel.uploadChatImageWithProgress(
            Uri.fromFile(File(url)),
            messagesModel,
            progressBar, isGroup
        )
    }


    /**
     * Uploading video to firebase storage for chat
     */
    private fun uploadVideo(videoPath: Uri) {

        val messagesModel = MessagesModel()

        messagesModel.messageId = chatViewModel.getChatDocumentId(otherUserId.toString(), isGroup)



        messagesModel.message = binding.edMessage.text.toString().trim()
        messagesModel.messageType = Constant.TYPE_VIDEO
        messagesModel.receiverId = otherUserId.toString()
        messagesModel.senderId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        messagesModel.url = ContentUriUtils.getFilePath(this, videoPath)
        messagesModel.videoUrl = ContentUriUtils.getFilePath(this, videoPath)
        messagesModel.status = Constant.TYPE_START_UPLOAD
        messagesModel.timeStamp = Timestamp(Date())

        messageList.add(messagesModel)
        chatAdapter?.notifyItemInserted(messageList.size - 1)
        binding.rvChatMessageList.scrollToPosition(messageList.size - 1)
    }

    /**
     * Uploading video to firebase storage for chat
     */
    fun uploadVideoWithProgress(
        videoPath: String,
        messagesModel: MessagesModel,
        progressBar: CircularProgressIndicator,
    ) {

        chatViewModel.uploadChatVideoWithProgress(
            Uri.fromFile(File(videoPath)),
            messagesModel,
            Utility.getVideoFileExtension(
                ContentUriUtils.getFilePath(
                    this,
                    Uri.fromFile(File(videoPath))
                ).toString()
            ),
            ContentUriUtils.getFilePath(this, Uri.fromFile(File(videoPath))).toString(),
            progressBar,
            isGroup
        )
    }


    /**
     * Opening image cropper
     */
    private fun openCropper(uri: Uri) {
        CropImage.activity(uri).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(1, 1)
            .setOutputCompressQuality(50).start(this@ChatActivity)
    }


    /**
     * After selecting video to redirecting cropper activity
     */
    private fun redirectTrimVideo(videoPath: String) {
        TrimVideo.activity(videoPath)
            .setCompressOption(CompressOption()) //empty constructor for default compress option
            .setHideSeekBar(true)
            .setTrimType(TrimType.MIN_MAX_DURATION)
            .setMinToMax(5, 30)  //
            .setTitle("Trim Video")
            .start(this, startForResult)

    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data = result.data
                // val uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.data))
                val uri = Uri.fromFile(File(data?.getStringExtra("trimmed_video_path")))
                println("Trimmed path:: $uri")
                uploadVideo(uri)

            } else
                LogMessage.v("videoTrimResultLauncher data is null");
        }

    /**
     * Redirecting to full screen image view activity
     */
    fun redirectImageViewActivity(url: String) {
        val intent = Intent(this, ImageViewActivity::class.java)
        intent.putExtra("ImageUrl", url)
        startActivity(intent)
    }

    /**
     * Redirecting to video play activity
     */
    fun redirectVideoPlayActivity(url: String) {
        val intent = Intent(this, VideoPlayActivity::class.java)
        intent.putExtra("VideoUrl", url)
        startActivity(intent)
    }

    /**
     * Redirecting create group screen
     */
    private fun openGroupDetailForEdit() {
        val intent = Intent(this, CreateGroupActivity::class.java)
        intent.putExtra("memberIdList", memberIdList)
        intent.putExtra("group_image", groupIcon)
        intent.putExtra("group_name", opponentUserName)
        intent.putExtra("isForDisplay", true)
        startActivity(intent)
    }


    /**
     * settting one to one video call
     */
    private fun setupVideoCall(userIds: ArrayList<String>, currentUserId: String) {
        val userIdsWithStatus = ArrayList<String>()
        for (i in userIds) {
            if (i.equals(currentUserId)) {
                userIdsWithStatus.add(i + "___Active")
            } else {
                userIdsWithStatus.add(i + "___InActive")
            }
        }
        chatViewModel.setupVideoCallData(
            userIdsWithStatus,
            "Active",
            currentUserId,
            Constant.USER_NAME
        )

    }
}