package helmi.benabdelghani.gestioncontactjc.data.model


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(
    tableName = "sms_messages",
    foreignKeys = [ForeignKey(
        entity = Contact::class,
        parentColumns = ["id"],
        childColumns = ["contactId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index(value = ["contactId"]), Index(value = ["threadId"]), Index(value = ["address"])]
)
data class Sms(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactId: Int? = null,
    val threadId: Long? = null,
    val address: String,
    val body: String,

    @ColumnInfo(name = "isSender")
    val isSender: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)
