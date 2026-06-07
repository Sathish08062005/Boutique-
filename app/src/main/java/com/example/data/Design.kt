package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "designs")
data class Design(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val description: String,
    val imageUrls: String, // Comma-separated list of image URLs (or local storage file URIs)
    val uploadDate: Long = System.currentTimeMillis(),
    val isFeatured: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val actionHistory: String = "Created initially"
) {
    // Helper to get image URL list
    fun getImageList(): List<String> {
        if (imageUrls.isBlank()) return emptyList()
        return imageUrls.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
