package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MontanteDao {
    @Query("SELECT * FROM montantes ORDER BY createdAt DESC")
    fun getAllMontantes(): Flow<List<Montante>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMontante(montante: Montante): Long

    @Update
    suspend fun updateMontante(montante: Montante)

    @Delete
    suspend fun deleteMontante(montante: Montante)

    @Query("SELECT * FROM montantes WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveMontante(): Montante?

    @Query("SELECT * FROM montantes WHERE isActive = 1 LIMIT 1")
    fun getActiveMontanteFlow(): Flow<Montante?>

    @Query("UPDATE montantes SET isActive = 0")
    suspend fun deactivateAll()

    @Transaction
    suspend fun createAndActivateMontante(montante: Montante): Long {
        deactivateAll()
        return insertMontante(montante.copy(isActive = true))
    }

    @Transaction
    suspend fun selectActiveMontante(montanteId: Int) {
        deactivateAll()
        QuerySetActive(montanteId)
    }

    @Query("UPDATE montantes SET isActive = 1 WHERE id = :montanteId")
    suspend fun QuerySetActive(montanteId: Int)
}

@Dao
interface BetDao {
    @Query("SELECT * FROM bets WHERE montanteId = :montanteId ORDER BY timestamp ASC")
    fun getBetsForMontante(montanteId: Int): Flow<List<Bet>>

    @Query("SELECT * FROM bets WHERE montanteId = :montanteId ORDER BY timestamp ASC")
    suspend fun getBetsForMontanteSync(montanteId: Int): List<Bet>

    @Query("SELECT * FROM bets ORDER BY timestamp DESC")
    fun getAllBetsFlow(): Flow<List<Bet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBet(bet: Bet)

    @Update
    suspend fun updateBet(bet: Bet)

    @Delete
    suspend fun deleteBet(bet: Bet)
}

@Database(entities = [Montante::class, Bet::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun montanteDao(): MontanteDao
    abstract fun betDao(): BetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stadium_montante_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
