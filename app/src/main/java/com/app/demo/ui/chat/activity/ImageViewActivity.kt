package com.app.demo.ui.chat.activity

import android.os.Bundle
import com.app.demo.core.BaseActivity
import com.app.demo.databinding.ActivityImageViewBinding
import com.app.demo.utils.Glide

/**
 * Activity is used to display image attached in chat
 */
class ImageViewActivity : BaseActivity() {
    private lateinit var binding: ActivityImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUI()
    }
    /**
     * Initialize ui and call method
     */
    private fun initUI() {
        Glide.loadImage(this, intent.getStringExtra("ImageUrl"), binding.imgView)
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

}