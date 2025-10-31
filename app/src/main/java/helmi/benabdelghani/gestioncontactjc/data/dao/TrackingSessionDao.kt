package helmi.benabdelghani.gestioncontactjc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingPoint
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingSession


@Dao
interface TrackingSessionDao {
    @Insert
    fun insertSession(session: TrackingSession): Long

    @Insert fun insertPoint(point: TrackingPoint)

    @Query("SELECT * FROM tracking_sessions ORDER BY id DESC")
    fun getAllSessions(): List<TrackingSession>
    @Query("SELECT * FROM tracking_points WHERE sessionId = :id ORDER BY timestamp")
    fun getSessionPoints(id: Int): List<TrackingPoint>
    @Update
    fun updateSession(session: TrackingSession)

    @Query("SELECT * FROM tracking_sessions WHERE id = :id LIMIT 1")
    fun getSessionById(id: Int): TrackingSession

    @Query("UPDATE tracking_sessions SET totalDistance = :distance WHERE id = :sessionId")
    fun updateSessionDistance(sessionId: Int, distance: Double)
}
