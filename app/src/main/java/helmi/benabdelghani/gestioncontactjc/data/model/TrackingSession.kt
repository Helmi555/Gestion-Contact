package helmi.benabdelghani.gestioncontactjc.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "tracking_sessions",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class TrackingSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val startTime: Long,
    val endTime: Long? = null,
    val totalDistance: Double = 0.0,
    val points: Int = 0
)
