package com.example.data

import kotlinx.coroutines.flow.Flow

class DesignRepository(private val designDao: DesignDao) {
    val allDesigns: Flow<List<Design>> = designDao.getAllDesigns()
    val featuredDesigns: Flow<List<Design>> = designDao.getFeaturedDesigns()

    fun getDesignsByCategory(category: String): Flow<List<Design>> {
        return designDao.getDesignsByCategory(category)
    }

    fun searchDesigns(query: String): Flow<List<Design>> {
        return designDao.searchDesigns(query)
    }

    fun getRecentDesigns(limit: Int): Flow<List<Design>> {
        return designDao.getRecentDesigns(limit)
    }

    fun getDesignById(id: Int): Flow<Design?> {
        return designDao.getDesignById(id)
    }

    suspend fun insert(design: Design) {
        designDao.insertDesign(design)
    }

    suspend fun delete(design: Design) {
        designDao.deleteDesign(design)
    }

    suspend fun deleteById(id: Int) {
        designDao.deleteDesignById(id)
    }
}
