package helmi.benabdelghani.gestioncontactjc.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingPoint

@Dao
interface TrackingPointDao {
    @Insert
    fun insertPoint(point: TrackingPoint): Long

    @Query("SELECT * FROM tracking_points WHERE sessionId = :sessionId ORDER BY timestamp")
    fun getPointsForSession(sessionId: Int): List<TrackingPoint>

    @Query("SELECT * FROM tracking_points ORDER BY timestamp")
    fun getAllPoints(): List<TrackingPoint>

    @Query("SELECT * FROM tracking_points WHERE id = :id LIMIT 1")
    fun getPointById(id: Int): TrackingPoint?

    @Update
    fun updatePoint(point: TrackingPoint)

    @Delete
    fun deletePoint(point: TrackingPoint)

    @Query("DELETE FROM tracking_points WHERE sessionId = :sessionId")
    fun deletePointsForSession(sessionId: Int)
}