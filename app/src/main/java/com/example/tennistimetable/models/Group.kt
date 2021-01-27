package com.example.tennistimetable.models

import android.os.Parcel
import android.os.Parcelable

data class Group(
    val name: String = "",
    val groupType: Int = 0,
    val assignedTo: ArrayList<String> = ArrayList(),
    val coachAdded: Int = 0,
    var documentId: String = ""

) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readInt(),
        source.createStringArrayList()!!,
        source.readInt(),
        source.readString()!!

    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeInt(groupType)
        writeStringList(assignedTo)
        writeInt(coachAdded)
        writeString(documentId)

    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Group> = object : Parcelable.Creator<Group> {
            override fun createFromParcel(source: Parcel): Group = Group(source)
            override fun newArray(size: Int): Array<Group?> = arrayOfNulls(size)
        }
    }
}