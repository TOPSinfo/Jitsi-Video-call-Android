package com.app.demo.core

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.app.demo.view.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    private var customProgressDialog: CustomProgressDialog? = null

    /**
     * show progress dialog
     * @param context context of activity
     */
    protected fun showProgress(context: Context) {
        try {
            if (customProgressDialog != null && customProgressDialog!!.isShowing) {
                return
            }
            customProgressDialog = CustomProgressDialog(context)
            customProgressDialog?.show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * hide progress dialog
     */
    protected fun hideProgress() {
        try {
            if (customProgressDialog != null) {
                if (customProgressDialog!!.isShowing) {
                    customProgressDialog!!.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}