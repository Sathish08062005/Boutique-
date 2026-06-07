package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.DesignRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BoutiqueApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { DesignRepository(database.designDao()) }
}
