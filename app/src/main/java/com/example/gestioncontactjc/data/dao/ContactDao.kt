package com.example.gestioncontactjc.data.dao

import androidx.room.*
import com.example.gestioncontactjc.data.model.Contact
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Dao
interface ContactDao {

    @Insert
    suspend fun insert(contact: Contact): Long

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: Int): Contact?

    @Query("SELECT * FROM contacts WHERE userId = :userId AND phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getContactByPhoneNumber(userId: Int, phoneNumber: String): Contact?



    @Query("SELECT * FROM contacts WHERE userId = :userId")
    suspend fun getContactsForUser(userId: Int): List<Contact>

    @Query("SELECT count(*) FROM contacts where userId = :userId")
    suspend fun countContactsForUser(userId: Int): Int

    @Query("DELETE FROM contacts WHERE userId = :userId")
    suspend fun deleteAllContactsForUser(userId: Int)

    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY nom ASC")
    suspend fun getContactsForUserSorted(userId: Int): List<Contact>

    @Query("UPDATE contacts SET isPinned = CASE WHEN isPinned = 1 THEN 0 ELSE 1 END WHERE id = :contactId")
    suspend fun togglePinContact(contactId: Int)

    @Query("SELECT * FROM contacts where userId = :userId ORDER BY callCount DESC LIMIT 1 ")
    suspend fun getMostContacted(userId: Int): Contact?



    @Query("SELECT COUNT(*) FROM contacts WHERE userId = :userId AND createdAt > :sevenDaysAgo")
    suspend fun getRecentContactedCount(userId: Int, sevenDaysAgo: Long): Int



    @Query("SELECT COUNT(*) FROM contacts WHERE userId = :userId AND isPinned = 1")
    suspend fun getTotalPinnedContacts(userId: Int): Int

    @Query("SELECT * FROM contacts WHERE userId = :userId AND isPinned = 1  ORDER BY nom ASC")
    suspend fun getAllPinnedContacts(userId: Int): List<Contact>



    @Query("SELECT nom FROM contacts WHERE userId = :userId ORDER BY callCount DESC LIMIT 1")
    suspend fun getMostContactedName(userId: Int): String?

    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY id DESC LIMIT 5")
    suspend fun getRecentContacts(userId: Int): List<Contact>


    @Query("SELECT COUNT(*) as totalContacts, SUM(isPinned) as pinnedContacts, SUM(callCount) as totalCalls FROM contacts WHERE userId = :userId")
    suspend fun getUserStats(userId: Int): UserStats?

    data class UserStats(
        val totalContacts: Int,
        val pinnedContacts: Int,
        val totalCalls: Int
    )

    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY isPinned DESC, updatedAt DESC")
    fun getContactsForUserSortedFlow(userId: Int): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE userId = :userId AND isPinned = 1 ORDER BY nom ASC")
    fun getAllPinnedContactsFlow(userId: Int): Flow<List<Contact>>


}
