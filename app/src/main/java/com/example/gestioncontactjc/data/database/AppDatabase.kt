package com.example.gestioncontactjc.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gestioncontactjc.data.dao.ContactDao
import com.example.gestioncontactjc.data.dao.SmsDao
import com.example.gestioncontactjc.data.dao.UserDao
import com.example.gestioncontactjc.data.model.Contact
import com.example.gestioncontactjc.data.model.Sms
import com.example.gestioncontactjc.data.model.User

@Database(
    entities = [User::class, Contact::class, Sms::class],
    version  = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun smsDao(): SmsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "contact_db"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}