package com.example.gestioncontactjc.data.dao

import androidx.room.*
import com.example.gestioncontactjc.data.model.Contact
import com.example.gestioncontactjc.data.model.User

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

}
