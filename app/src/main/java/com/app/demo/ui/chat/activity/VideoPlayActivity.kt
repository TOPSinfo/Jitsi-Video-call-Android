package com.app.demo.ui.chat.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivityVideoPlayBinding

/**
 * Activity is used to play video attached in chat
 */
class VideoPlayActivity : BaseActivity() {

    private lateinit var binding: ActivityVideoPlayBinding

    var mediaController: MediaController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }
    /**
     * Initialize ui and call method
     */
    private fun initUI() {

        binding.imgBack.setOnClickListener {
            finish()
        }

        mediaController = MediaController(this, true)

        binding.videoView.setOnPreparedListener { mp: MediaPlayer? ->
            mediaController!!.show()
            binding.videoView.start()
            binding.progressBar.setVisibility(View.GONE)
        }

        binding.videoView.setVideoPath(intent.getStringExtra("VideoUrl"))

        mediaController!!.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        mediaController!!.setMediaPlayer(binding.videoView)
        binding.videoView.requestFocus()

        binding.videoView.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
            Log.d("video", "setOnErrorListener ")
            true
        }

    }

}

