package com.app.demo.model.user

import android.os.Parcel
import android.os.Parcelable
import com.app.demo.model.group.GroupModel
import com.google.firebase.Timestamp

/**
 * Model class is used to handle data of chat user
 */
data class UserModel(
    var uid: String? = "",
    var email: String? = "",
    var firstName: String? = "",
    var lastName: String? = "",
    var phone: String? = "",
    var profileImage: String? = "",
    var groupDetailModel: GroupModel? = GroupModel(),
    var createdAt: Timestamp? = null,
    var isSelectedForCall: Boolean = false,
    var isOnline: Boolean = false,
    var isGroup: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Timestamp::class.java.classLoader)

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(email)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(phone)
        parcel.writeString(profileImage)
        parcel.writeTypedObject(groupDetailModel,flags)
        parcel.writeParcelable(createdAt, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserModel> {
        override fun createFromParcel(parcel: Parcel): UserModel {
            return UserModel(parcel)
        }

        override fun newArray(size: Int): Array<UserModel?> {
            return arrayOfNulls(size)
        }
    }
}
