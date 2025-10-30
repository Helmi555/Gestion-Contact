package helmi.benabdelghani.gestioncontactjc.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true) val id: Int =0 ,
    val username:String,
    val password: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null


)