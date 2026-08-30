package com.novabrowser.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: Bookmark): Long

    @Delete
    suspend fun delete(bookmark: Bookmark)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url LIMIT 1)")
    suspend fun isBookmarked(url: String): Boolean

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteByUrl(url: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 500")
    fun observeAll(): Flow<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntry): Long

    @Query("DELETE FROM history")
    suspend fun clearAll()

    @Query("SELECT * FROM history WHERE url LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY visitedAt DESC LIMIT 20")
    suspend fun search(query: String): List<HistoryEntry>
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun observeAll(): Flow<List<DownloadRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DownloadRecord): Long

    @Update
    suspend fun update(record: DownloadRecord)

    @Delete
    suspend fun delete(record: DownloadRecord)
}

@Dao
interface UserScriptDao {
    @Query("SELECT * FROM userscripts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<UserScript>>

    @Query("SELECT * FROM userscripts WHERE enabled = 1")
    suspend fun getEnabled(): List<UserScript>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(script: UserScript): Long

    @Update
    suspend fun update(script: UserScript)

    @Delete
    suspend fun delete(script: UserScript)
}
