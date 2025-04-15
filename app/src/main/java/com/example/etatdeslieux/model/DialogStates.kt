package com.example.etatdeslieux.model

import android.os.Parcel
import android.os.Parcelable

data class DialogStates(
    val showDeleteDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showCameraPermission: Boolean = false,
    val showPhotoCommentDialog: Boolean = false,
    val showEditCommentDialog: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (showDeleteDialog) 1 else 0)
        parcel.writeByte(if (showEditDialog) 1 else 0)
        parcel.writeByte(if (showCameraPermission) 1 else 0)
        parcel.writeByte(if (showPhotoCommentDialog) 1 else 0)
        parcel.writeByte(if (showEditCommentDialog) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DialogStates> {
        override fun createFromParcel(parcel: Parcel): DialogStates {
            return DialogStates(parcel)
        }

        override fun newArray(size: Int): Array<DialogStates?> {
            return arrayOfNulls(size)
        }
    }
}
