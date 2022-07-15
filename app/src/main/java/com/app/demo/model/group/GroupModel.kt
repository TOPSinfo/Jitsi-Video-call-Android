package com.app.demo.model.group

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

/**
 * Model class is used to handle details of chat group list
 */
data class GroupModel(
    var adminId: String? = "",
    var adminName: String? = "",
    var createdAt: Timestamp? = null,
    var groupIcon: String? = "",
    var id: String? = "",
    var members: ArrayList<String> = ArrayList(),
    var name: String? = "",
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(adminId)
        parcel.writeString(adminName)
        parcel.writeParcelable(createdAt, flags)
        parcel.writeString(groupIcon)
        parcel.writeString(id)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GroupModel> {
        override fun createFromParcel(parcel: Parcel): GroupModel {
            return GroupModel(parcel)
        }

        override fun newArray(size: Int): Array<GroupModel?> {
            return arrayOfNulls(size)
        }
    }
}
