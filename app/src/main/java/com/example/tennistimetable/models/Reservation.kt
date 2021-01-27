package com.example.tennistimetable.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter.*

data class Reservation(

    val courtNumber: Int = 0,
    val assignedTo: ArrayList<String> = ArrayList(),
    val reservationDate: Long = 0,
    val hourStart: Long = 0,
    val hourEnd: Long = 0,
    var coachId: String = "",
    var assignedGroup: String = "",
    var documentId: String = ""





): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.createStringArrayList()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeInt(courtNumber)
        writeStringList(assignedTo)
        writeLong(reservationDate)
        writeLong(hourStart)
        writeLong(hourEnd)
        writeString(coachId)
        writeString(assignedGroup)
        writeString(documentId)

    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Reservation> = object : Parcelable.Creator<Reservation> {
            override fun createFromParcel(source: Parcel): Reservation = Reservation(source)
            override fun newArray(size: Int): Array<Reservation?> = arrayOfNulls(size)
        }
    }
}
