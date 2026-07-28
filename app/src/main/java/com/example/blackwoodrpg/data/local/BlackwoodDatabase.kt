package com.example.blackwoodrpg.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "player_save")
data class PlayerEntity(
    @PrimaryKey val id: String = "hero_player",
    val name: String,
    val skillsJson: String,
    val equipmentJson: String,
    val inventoryJson: String
)

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_save WHERE id = :id")
    suspend fun getPlayerSave(id: String = "hero_player"): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayer(playerEntity: PlayerEntity)
}

@Database(entities = [PlayerEntity::class], version = 1, exportSchema = false)
abstract class BlackwoodDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    companion object {
        @Volatile
        private var INSTANCE: BlackwoodDatabase? = null

        fun getDatabase(context: Context): BlackwoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlackwoodDatabase::class.java,
                    "blackwood_rpg.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
