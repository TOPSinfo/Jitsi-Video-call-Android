package com.app.demo.utils

import android.content.Context
import android.widget.ImageView
import com.app.demo.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

object Glide {

    /**
     * Used to load profile image in circle view
     */
    fun loadGlideProfileImage(
        context: Context?,
        url: String?,
        imageView: ImageView?
    ) {
        try {
            Glide.with(context!!)
                .load(url)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .apply(
                    RequestOptions.placeholderOf(R.drawable.ic_user_icon)
                        .error(R.drawable.ic_user_icon).diskCacheStrategy(
                            DiskCacheStrategy.ALL
                        )
                )
                .into(imageView!!)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }
    /**
     * Used to load placeholder in circle view
     */
    fun loadPlaceholderImage(
        context: Context?,
        placeHolder: Int,
        imageView: ImageView?
    ) {
        try {
            Glide.with(context!!)
                .load(placeHolder)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .apply(
                    RequestOptions.placeholderOf(R.drawable.ic_user_icon)
                        .error(R.drawable.ic_user_icon).diskCacheStrategy(
                            DiskCacheStrategy.ALL
                        )
                )
                .into(imageView!!)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }
    /**
     * Used to load Other images in application
     */
    fun loadImage(context: Context, imageUrl: String?, imageView: ImageView) {

        try {

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_placeholder)
               // .thumbnail(0.25f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}