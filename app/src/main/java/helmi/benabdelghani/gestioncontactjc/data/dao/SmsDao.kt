package helmi.benabdelghani.gestioncontactjc.data.dao

import androidx.room.*
import helmi.benabdelghani.gestioncontactjc.data.model.Sms
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sms: Sms): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(smsList: List<Sms>)

    @Update
    suspend fun update(sms: Sms)

    @Delete
    suspend fun delete(sms: Sms)

    @Query("SELECT * FROM sms_messages WHERE id = :id")
    suspend fun getById(id: Int): Sms?

    @Query("""
        SELECT s.* FROM sms_messages s
        JOIN contacts c ON s.contactId = c.id
        WHERE c.userId = :userId
        ORDER BY s.timestamp DESC
    """)
    fun getAllForUser(userId: Int): Flow<List<Sms>>


    @Query("SELECT * FROM sms_messages WHERE contactId = :contactId ORDER BY timestamp DESC")
    fun getByContactId(contactId: Int): Flow<List<Sms>>

    @Query("DELETE FROM sms_messages WHERE contactId = :contactId")
    suspend fun deleteByContactId(contactId: Int)

    @Query("SELECT * FROM sms_messages WHERE contactId = :contactId ORDER BY timestamp DESC LIMIT 1")
    fun getLastMessageByContactId(contactId: Int): Flow<Sms?>

    @Query("SELECT * FROM sms_messages WHERE contactId=:id ORDER BY timestamp ASC")
    fun getByContactIdFlow(id: Int): Flow<List<Sms>>
}
