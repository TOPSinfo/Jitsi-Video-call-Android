@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.app.demo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


object Utility {
    val storageRef = FirebaseStorage.getInstance().reference


    fun log(message: String) {
        android.util.Log.d("TAG", "rs $message")
    }


    fun emailValidator(email: String?): Boolean {
        val pattern: Pattern
        val matcher: Matcher
        val EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        matcher = pattern.matcher(email)
        return matcher.matches()
    }


    /**
     * hide keybord from screen
     * @param context Context of activity
     * @param view view of activity
     */
    fun hideKeyboard(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * hide keybord from screen
     * @param context Context of activity
     * @param view view of activity
     */
    fun showKeyboard(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }


    fun checkConnection(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val activeNetwork =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }
        return result
    }

    fun twoDigitString(number: Int): String {
        if (number == 0) {
            return "00"
        }
        return if (number / 10 == 0) {
            "0$number"
        } else number.toString()
    }


    fun getDate(mills: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy hh:mm a")
        return formatter.format(Date(mills))
    }

    fun getRandomNumber(min: Int, max: Int): Int {
        return Random().nextInt((max - min) + 1) + min
    }

    fun getVideoFileExtension(videoPath: String): String {
        val file = Uri.fromFile(File(videoPath))
        return MimeTypeMap.getFileExtensionFromUrl(file.toString())
    }

    fun isVideoFile(path: String?): Boolean {
        val mimeType: String = URLConnection.guessContentTypeFromName(path)
        return mimeType.startsWith("video")
    }

    fun getOutputVideoFile(): File? {
        var mediaFile: File? = null
        try {
            val mediaStorageDir = File(Environment.getExternalStorageDirectory(), ".TopsDemo")
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null
                }
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            mediaFile = File(mediaStorageDir.path + File.separator + "VID_" + timeStamp + ".mp4")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mediaFile
    }

    // Get image path

    // Get image path
    fun getAndroidDefaultMediaPath(imageName: String?): File? {
        val file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/.TopsDemo"


        val dir = File(file_path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, imageName)
    }
}



