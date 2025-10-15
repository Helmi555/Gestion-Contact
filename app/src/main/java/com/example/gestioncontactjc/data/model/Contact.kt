package com.example.gestioncontactjc.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "contacts",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["phoneNumber"], unique = true)]

)
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val nom: String,
    val pseudo: String,
    val phoneNumber: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    val callCount:Int=0
)
