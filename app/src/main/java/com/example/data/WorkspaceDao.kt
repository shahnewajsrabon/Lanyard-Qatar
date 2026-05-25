package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {

    // --- Tasks Queries ---
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDate ASC")
    fun getTasksByStatus(status: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Query("UPDATE tasks SET syncState = 'SYNCED' WHERE syncState = 'PENDING_LOCAL'")
    suspend fun markAllTasksSynced()


    // --- Chat Queries ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE channel = :channel ORDER BY timestamp ASC")
    fun getChatMessagesByChannel(channel: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET syncState = 'SYNCED' WHERE syncState = 'PENDING_LOCAL'")
    suspend fun markAllChatsSynced()


    // --- Meeting Queries ---
    @Query("SELECT * FROM meetings ORDER BY date ASC, time ASC")
    fun getAllMeetings(): Flow<List<Meeting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: Meeting)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: Int)

    @Query("UPDATE meetings SET syncState = 'SYNCED' WHERE syncState = 'PENDING_LOCAL'")
    suspend fun markAllMeetingsSynced()
}
