package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CampaignEntity::class, MediumShotEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BrandDatabase : RoomDatabase() {
    abstract fun brandDao(): BrandDao

    companion object {
        @Volatile
        private var INSTANCE: BrandDatabase? = null

        fun getInstance(context: Context): BrandDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrandDatabase::class.java,
                    "brand_builder_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
