package com.example.wordnotes.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName
import java.util.UUID

@Entity(tableName = "words")
data class Word(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val word: String = "",
    val pos: String = "",
    val ipa: String = "",
    val meaning: String = "",
    @PropertyName("remind") @ColumnInfo("remind") val isRemind: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        word = parcel.readString() ?: "",
        pos = parcel.readString() ?: "",
        ipa = parcel.readString() ?: "",
        meaning = parcel.readString() ?: "",
        isRemind = parcel.readByte() != 0.toByte(),
        timestamp = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.apply {
            writeString(id)
            writeString(word)
            writeString(pos)
            writeString(ipa)
            writeString(meaning)
            writeByte(if (isRemind) 1 else 0)
            writeLong(timestamp)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Word> {
        override fun createFromParcel(parcel: Parcel): Word {
            return Word(parcel)
        }

        override fun newArray(size: Int): Array<Word?> {
            return arrayOfNulls(size)
        }
    }
}