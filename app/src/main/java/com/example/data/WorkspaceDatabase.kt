package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Task::class, ChatMessage::class, Meeting::class],
    version = 3,
    exportSchema = false
)
abstract class WorkspaceDatabase : RoomDatabase() {

    abstract fun workspaceDao(): WorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: WorkspaceDatabase? = null

        fun getDatabase(context: Context): WorkspaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkspaceDatabase::class.java,
                    "lanyardqatar_workspace_db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true) // safe for rapid development and updates
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
