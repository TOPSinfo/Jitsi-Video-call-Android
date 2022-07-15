package com.app.demo.utils

import android.content.Context
import android.text.Spanned
import android.view.View
import android.widget.Toast
import androidx.core.text.HtmlCompat


fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.toast(message: String, duration : Int) {
    Toast.makeText(this, message, duration).show()
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.disable() {
    alpha = .3f
    isClickable = false
}

fun View.enable() {
    alpha = 1f
    isClickable = true
}

// with no alpha
fun View.isDisable() {
    isClickable = false
    isEnabled = false
}

fun View.isEnable() {
    isClickable = true
    isEnabled = true
}

fun String?.toHtml(): Spanned? {
    if (this.isNullOrEmpty()) return null
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT)
}

