package com.yongxiongli.officeproductivity.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TaskEntity::class, MeetingEntity::class], version = 3, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun meetingDao(): MeetingDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN priority TEXT NOT NULL DEFAULT 'MEDIUM'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN assignee TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tasks ADD COLUMN meetingId INTEGER")
                db.execSQL("ALTER TABLE tasks ADD COLUMN completedAtMillis INTEGER")
                db.execSQL("CREATE TABLE IF NOT EXISTS meetings (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, meetingAtMillis INTEGER NOT NULL, participants TEXT NOT NULL, notes TEXT NOT NULL, createdAtMillis INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN reminderAtMillis INTEGER")
                db.execSQL("ALTER TABLE meetings ADD COLUMN reminderAtMillis INTEGER")
            }
        }

        @Volatile private var instance: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                TaskDatabase::class.java,
                "office_productivity.db",
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
        }
    }
}
