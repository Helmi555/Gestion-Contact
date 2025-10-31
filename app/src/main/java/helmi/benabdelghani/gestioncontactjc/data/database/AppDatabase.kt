package helmi.benabdelghani.gestioncontactjc.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import helmi.benabdelghani.gestioncontactjc.data.dao.ContactDao
import helmi.benabdelghani.gestioncontactjc.data.dao.SmsDao
import helmi.benabdelghani.gestioncontactjc.data.dao.TrackingPointDao
import helmi.benabdelghani.gestioncontactjc.data.dao.TrackingSessionDao
import helmi.benabdelghani.gestioncontactjc.data.dao.UserDao
import helmi.benabdelghani.gestioncontactjc.data.model.Contact
import helmi.benabdelghani.gestioncontactjc.data.model.Sms
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingPoint
import helmi.benabdelghani.gestioncontactjc.data.model.TrackingSession
import helmi.benabdelghani.gestioncontactjc.data.model.User

@Database(
    entities = [User::class, Contact::class, Sms::class, TrackingSession::class, TrackingPoint::class],
    version  = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun smsDao(): SmsDao
    abstract fun trackingPointDao(): TrackingPointDao
    abstract fun trackingSessionDao(): TrackingSessionDao
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