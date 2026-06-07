package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Design
import com.example.data.DesignRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    data class CategoryDetail(val categoryName: String) : Screen()
    data class DesignDetail(val designId: Int) : Screen()
    object Search : Screen()
    object Contact : Screen()
    object AdminLogin : Screen()
    object AdminDashboard : Screen()
    data class AdminAddEditDesign(val designId: Int? = null) : Screen() // null means Add, otherwise Edit
}

data class InAppNotification(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val message: String,
    val icon: String? = null
)

class BoutiqueViewModel(private val repository: DesignRepository) : ViewModel() {

    // Custom Backstack Navigation State
    val navigationStack = mutableStateListOf<Screen>(Screen.Home)

    fun navigateTo(screen: Screen) {
        // Prevent duplicate consecutive screens
        if (navigationStack.isNotEmpty() && navigationStack.last() == screen) {
            return
        }
        navigationStack.add(screen)
    }

    fun navigateBack(): Boolean {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.size - 1)
            return true
        }
        return false
    }

    fun navigateHome() {
        navigationStack.clear()
        navigationStack.add(Screen.Home)
    }

    // Interactive Image Zoom support state
    var zoomedImageUrl by mutableStateOf<String?>(null)
        private set

    fun openZoom(url: String) {
        zoomedImageUrl = url
    }

    fun closeZoom() {
        zoomedImageUrl = null
    }

    // Admin Auth State
    var isAdminLoggedIn by mutableStateOf(false)
        private set

    var loginError by mutableStateOf<String?>(null)

    fun loginAdmin(password: String): Boolean {
        return if (password == "ziva2026" || password == "admin") {
            isAdminLoggedIn = true
            loginError = null
            navigateTo(Screen.AdminDashboard)
            true
        } else {
            loginError = "Incorrect Access Key"
            false
        }
    }

    fun logoutAdmin() {
        isAdminLoggedIn = false
        navigateHome()
    }

    // In-App Simulated App Notification state (for rich push feedback)
    var activeNotification by mutableStateOf<InAppNotification?>(null)
        private set

    fun triggerNotification(title: String, message: String) {
        viewModelScope.launch {
            activeNotification = InAppNotification(title = title, message = message)
        }
    }

    fun dismissNotification() {
        activeNotification = null
    }

    // Catalog States & Flows
    val allDesigns: StateFlow<List<Design>> = repository.allDesigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredDesigns: StateFlow<List<Design>> = repository.featuredDesigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDesigns: StateFlow<List<Design>> = repository.getRecentDesigns(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live search query and search category filter
    val searchQuery = MutableStateFlow("")
    val selectedSearchCategory = MutableStateFlow("All")

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateSelectedSearchCategory(category: String) {
        selectedSearchCategory.value = category
    }

    val searchResults: StateFlow<List<Design>> = combine(
        repository.allDesigns,
        searchQuery,
        selectedSearchCategory
    ) { designs, query, category ->
        designs.filter { design ->
            val matchesQuery = query.isBlank() || 
                    design.title.contains(query, ignoreCase = true) || 
                    design.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || design.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected design flow for detail views
    fun getDesignFlow(id: Int): Flow<Design?> {
        return repository.getDesignById(id)
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    // Add and edit designs
    fun saveDesign(
        id: Int = 0,
        title: String,
        category: String,
        description: String,
        imageUrls: String,
        isFeatured: Boolean
    ) {
        viewModelScope.launch {
            val isNew = id == 0
            val now = System.currentTimeMillis()
            val dateStr = formatTimestamp(now)
            
            val design = if (isNew) {
                Design(
                    id = 0,
                    title = title,
                    category = category,
                    description = description,
                    imageUrls = imageUrls,
                    isFeatured = isFeatured,
                    uploadDate = now,
                    lastModified = now,
                    actionHistory = "$dateStr: Published in catalog"
                )
            } else {
                val oldDesign = allDesigns.value.find { it.id == id }
                if (oldDesign != null) {
                    val changes = mutableListOf<String>()
                    if (oldDesign.title != title) changes.add("Title updated to '$title'")
                    if (oldDesign.category != category) changes.add("Category updated to '$category'")
                    if (oldDesign.description != description) changes.add("Description updated")
                    if (oldDesign.imageUrls != imageUrls) changes.add("Images updated")
                    if (oldDesign.isFeatured != isFeatured) {
                        changes.add(if (isFeatured) "Featured on home page" else "Removed from home page featured")
                    }
                    val changeLog = if (changes.isNotEmpty()) changes.joinToString(", ") else "Saved without changes"
                    val newHistory = "${oldDesign.actionHistory}\n$dateStr: $changeLog"
                    Design(
                        id = id,
                        title = title,
                        category = category,
                        description = description,
                        imageUrls = imageUrls,
                        isFeatured = isFeatured,
                        uploadDate = oldDesign.uploadDate,
                        lastModified = now,
                        actionHistory = newHistory
                    )
                } else {
                    Design(
                        id = id,
                        title = title,
                        category = category,
                        description = description,
                        imageUrls = imageUrls,
                        isFeatured = isFeatured,
                        uploadDate = now,
                        lastModified = now,
                        actionHistory = "$dateStr: Edited design"
                    )
                }
            }
            repository.insert(design)
            
            if (isNew) {
                // Trigger Simulated Push Notification for New Arrivals
                triggerNotification(
                    title = "✨ New Arrival in $category! ✨",
                    message = "\"$title\" is now live. Tap to explore!"
                )
            }
            navigateBack()
        }
    }

    fun deleteDesign(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    // Preselected Category listing helper
    val categories = listOf(
        "Sarees",
        "Bridal Wear",
        "Lehengas",
        "Salwars",
        "Blouse Designs",
        "Kids Collection"
    )
}

class BoutiqueViewModelFactory(private val repository: DesignRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BoutiqueViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BoutiqueViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
