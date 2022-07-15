package com.app.demo.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Pref  @Inject constructor(@ApplicationContext private val mContext: Context){
    private val TAG = Pref::class.java.simpleName
//    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun openPref_(contex: Context, mPrefName: String?): SharedPreferences {
        return contex.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
    }

    fun getValue(context: Context? = mContext, key: String, defaultValue: String, mFileName: String? = Constant.PREF_FILE): String? {
        val sharedPreferences = context!!.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        val result = sharedPreferences.getString(key, defaultValue)
        Log.d(TAG, "getValue() called with: key = [$key], value = [$result]")
        return result
    }

    fun setValue(context: Context = mContext, key: String, value: String, mFileName: String? = Constant.PREF_FILE) {
        Log.d(TAG, "setValue() called with: key = [$key], value = [$value]")
        val sharedPreferences =
            context.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        val prefsPrivateEditor = sharedPreferences?.edit()
        prefsPrivateEditor?.putString(key, value)
        prefsPrivateEditor?.apply()
//        prefsPrivateEditor.commit()
    }

    fun getValue(context: Context = mContext, key: String, defaultValue: Int, mFileName: String? = Constant.PREF_FILE): Int {
        val sharedPreferences = context.getSharedPreferences(
            mFileName,
            Context.MODE_PRIVATE
        )
        val result = sharedPreferences.getInt(key, defaultValue)
        //		Pref.sharedPreferences = null;
        Log.d(TAG, "getValue() called with: key = [$key], value = [$result]")
        return result
    }

    fun setValue(context: Context = mContext, key: String, value: Int, mFileName: String? = Constant.PREF_FILE) {
        Log.d(TAG, "getValue() called with: key = [$key], value = [$value]")
        val sharedPreferences = context.getSharedPreferences(
            mFileName,
            Context.MODE_PRIVATE
        )
        val prefsPrivateEditor = sharedPreferences.edit()
        prefsPrivateEditor.putInt(key, value)
        prefsPrivateEditor.apply()
    }


    fun getValue(context: Context? = mContext, key: String?, defaultValue: Boolean, mFileName: String? = Constant.PREF_FILE): Boolean {
        val sharedPreferences = context!!.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        //		Pref.sharedPreferences = null;
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun setValue(context: Context = mContext, key: String?, value: Boolean, mFileName: String? = Constant.PREF_FILE) {
        val sharedPreferences = context.getSharedPreferences(
            mFileName,
            Context.MODE_PRIVATE
        )
        val prefsPrivateEditor = sharedPreferences.edit()
        prefsPrivateEditor.putBoolean(key, value)
        prefsPrivateEditor.apply()
        //		prefsPrivateEditor = null;
//		Pref.sharedPreferences = null;
    }


    fun clearAllPref(context: Context = mContext, mFileName: String?) {
        val sharedPreferences = context.getSharedPreferences(mFileName, Context.MODE_PRIVATE)
        val prefsPrivateEditor = sharedPreferences.edit()
        prefsPrivateEditor.clear()
        prefsPrivateEditor.apply()
    }


}