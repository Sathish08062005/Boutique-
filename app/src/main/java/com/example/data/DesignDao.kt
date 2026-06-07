package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DesignDao {
    @Query("SELECT * FROM designs ORDER BY uploadDate DESC")
    fun getAllDesigns(): Flow<List<Design>>

    @Query("SELECT * FROM designs WHERE category = :category ORDER BY uploadDate DESC")
    fun getDesignsByCategory(category: String): Flow<List<Design>>

    @Query("SELECT * FROM designs WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY uploadDate DESC")
    fun searchDesigns(query: String): Flow<List<Design>>

    @Query("SELECT * FROM designs WHERE isFeatured = 1 ORDER BY uploadDate DESC")
    fun getFeaturedDesigns(): Flow<List<Design>>

    @Query("SELECT * FROM designs ORDER BY uploadDate DESC LIMIT :limit")
    fun getRecentDesigns(limit: Int): Flow<List<Design>>

    @Query("SELECT * FROM designs WHERE id = :id")
    fun getDesignById(id: Int): Flow<Design?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesign(design: Design)

    @Delete
    suspend fun deleteDesign(design: Design)

    @Query("DELETE FROM designs WHERE id = :id")
    suspend fun deleteDesignById(id: Int)
}
