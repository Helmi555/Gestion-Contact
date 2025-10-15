package com.example.gestioncontactjc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gestioncontactjc.data.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id:Int): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun userExists(username: String): Boolean

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

}