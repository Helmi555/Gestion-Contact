package helmi.benabdelghani.gestioncontactjc.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "tracking_points",
    foreignKeys = [ForeignKey(
        entity = TrackingSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class TrackingPoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val lat: Double,
    val lon: Double,
    val timestamp: Long
)
