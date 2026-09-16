package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedJobDao {
    @Query("SELECT * FROM saved_jobs ORDER BY savedAt DESC")
    fun getAllSavedJobs(): Flow<List<SavedJobEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_jobs WHERE id = :id)")
    fun isJobSaved(id: String): Flow<Boolean>

    @Query("SELECT * FROM saved_jobs WHERE id = :id LIMIT 1")
    suspend fun getSavedJobById(id: String): SavedJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: SavedJobEntity)

    @Query("DELETE FROM saved_jobs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE saved_jobs SET userNotes = :notes WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String)

    @Query("DELETE FROM saved_jobs")
    suspend fun deleteAll()
}
