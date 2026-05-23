package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = 1,
    val iban: String,
    val ownerName: String
)

@Entity(tableName = "payment")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val bankName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val explanation: String = "Bayram Harçlığı",
    val isSuccess: Boolean = true
)

@Dao
interface PosDao {
    @Query("SELECT * FROM profile WHERE id = 1")
    fun getProfileFlow(): Flow<Profile?>

    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun getProfile(): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    @Query("SELECT * FROM payment ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Insert
    suspend fun insertPayment(payment: Payment)

    @Query("DELETE FROM payment")
    suspend fun clearPayments()
}

@Database(entities = [Profile::class, Payment::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bayram_pos_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
