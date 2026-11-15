package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    FavoriteWallpaperEntity::class,
    CustomPaletteEntity::class,
    WidgetConfigEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class AuraDatabase : RoomDatabase() {
  abstract fun auraDao(): AuraDao

  companion object {
    @Volatile private var INSTANCE: AuraDatabase? = null

    fun getInstance(context: Context): AuraDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                AuraDatabase::class.java,
                "auraspace.db",
              )
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
