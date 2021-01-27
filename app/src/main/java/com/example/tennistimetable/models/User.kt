package com.example.tennistimetable.models

import android.os.Parcel
import android.os.Parcelable

data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val mobile: Long = 0,
    val userType: Int = 0,
    val image: String = "",
    val fcmToken: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readLong(),
        source.readInt(),
        source.readString()!!,
        source.readString()!!
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with (dest) {
        writeString(id)
        writeString(name)
        writeString(email)
        writeLong(mobile)
        writeInt(userType)
        writeString(image)
        writeString(fcmToken)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(source: Parcel): User = User(source)
            override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
        }
    }
}