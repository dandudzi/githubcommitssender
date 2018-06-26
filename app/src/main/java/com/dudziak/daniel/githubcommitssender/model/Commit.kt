package com.dudziak.daniel.githubcommitssender.model

import android.os.Parcel
import android.os.Parcelable

data class Commit(val message: String, val shaValue: String, val authorName: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
        parcel.writeString(shaValue)
        parcel.writeString(authorName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Commit> {
        override fun createFromParcel(parcel: Parcel): Commit {
            return Commit(parcel)
        }

        override fun newArray(size: Int): Array<Commit?> {
            return arrayOfNulls(size)
        }
    }

    fun toMessage(): String {
        return """Author: $authorName
            |SHA value: $shaValue
            |Message: "$message"
            |""".trimMargin()
    }
}