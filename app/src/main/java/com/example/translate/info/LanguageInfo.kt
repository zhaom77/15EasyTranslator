package com.example.translate.info

import android.os.Parcel
import android.os.Parcelable

data class LanguageInfo(val code: String, val name: String): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()?:"",
        parcel.readString()?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(code)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LanguageInfo> {
        override fun createFromParcel(parcel: Parcel): LanguageInfo {
            return LanguageInfo(parcel)
        }

        override fun newArray(size: Int): Array<LanguageInfo?> {
            return arrayOfNulls(size)
        }
    }
}