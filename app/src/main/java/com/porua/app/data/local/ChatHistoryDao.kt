package com.porua.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatHistoryEntity): Long

    @Query("SELECT * FROM chat_history ORDER BY id ASC")
    fun getAllChats(): Flow<List<ChatHistoryEntity>>

    @Query("SELECT reply FROM chat_history WHERE LOWER(TRIM(prompt)) = LOWER(TRIM(:prompt)) AND reply != '' ORDER BY id DESC LIMIT 1")
    suspend fun getCachedReply(prompt: String): String?

    @Query("DELETE FROM chat_history")
    suspend fun clearAllChats()
}
