package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Design
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepGold
import com.example.ui.theme.MutedTextDark
import com.example.ui.theme.MutedTextLight
import com.example.ui.theme.SoftRose
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val app = application as BoutiqueApplication
                val viewModel: BoutiqueViewModel = viewModel(
                    factory = BoutiqueViewModelFactory(app.repository)
                )
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BoutiqueAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoutiqueAppContent(viewModel: BoutiqueViewModel) {
    val currentScreenPath = viewModel.navigationStack.lastOrNull() ?: Screen.Home
    val context = LocalContext.current
    val activeNotification = viewModel.activeNotification

    // Intercept hardware back events
    BackHandler(enabled = viewModel.navigationStack.size > 1) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            if (currentScreenPath != Screen.AdminLogin && currentScreenPath != Screen.AdminDashboard && currentScreenPath !is Screen.AdminAddEditDesign) {
                BoutiqueExtendedBottomNavigation(viewModel = viewModel, currentScreen = currentScreenPath)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Router Container with smooth fade animations
            AnimatedContent(
                targetState = currentScreenPath,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "ScreenNavigation"
            ) { screen ->
                when (screen) {
                    is Screen.Home -> {
                        BoutiqueHomeScreen(viewModel = viewModel)
                    }
                    is Screen.CategoryDetail -> {
                        BoutiqueCategoryScreen(viewModel = viewModel, categoryName = screen.categoryName)
                    }
                    is Screen.DesignDetail -> {
                        BoutiqueDesignDetailScreen(viewModel = viewModel, designId = screen.designId)
                    }
                    is Screen.Search -> {
                        BoutiqueSearchScreen(viewModel = viewModel)
                    }
                    is Screen.Contact -> {
                        BoutiqueContactScreen(viewModel = viewModel)
                    }
                    is Screen.AdminLogin -> {
                        BoutiqueAdminLoginScreen(viewModel = viewModel)
                    }
                    is Screen.AdminDashboard -> {
                        BoutiqueAdminDashboardScreen(viewModel = viewModel)
                    }
                    is Screen.AdminAddEditDesign -> {
                        BoutiqueAdminAddEditScreen(viewModel = viewModel, designId = screen.designId)
                    }
                }
            }

            // High-fidelity Floating Push Notification Dropdown Banner
            AnimatedVisibility(
                visible = activeNotification != null,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(250)
                ) + fadeOut()
            ) {
                if (activeNotification != null) {
                    PushNotificationBanner(
                        notification = activeNotification,
                        onDismiss = { viewModel.dismissNotification() }
                    )
                }
            }

            // Luxury Double-Tap Pinch Image Zoom Overlay Modal
            val zoomUrl = viewModel.zoomedImageUrl
            if (zoomUrl != null) {
                ImageZoomDialog(
                    imageUrl = zoomUrl,
                    onDismiss = { viewModel.closeZoom() }
                )
            }
        }
    }
}

// PREMIUM GRADIENT APP NOTIFICATION BAR SIMULATING REAL GOOGLE PUSH NOTIFICATION
@Composable
fun PushNotificationBanner(
    notification: InAppNotification,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Auto collapse after 5 seconds
    LaunchedEffect(notification.id) {
        delay(5000)
        onDismiss()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .border(1.dp, PrimaryGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = "Notification Icon",
                    tint = PrimaryGold,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    color = PrimaryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_notification_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Notification",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// MULTI-IMAGE ADVANCED PINCH ZOOM OVERLAY WITH ROTATION & TWISS PAN SUPPORT
@Composable
fun ImageZoomDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val transformState = rememberTransformableState { zoomChange, panChange, rotationChange ->
            scale = (scale * zoomChange).coerceIn(1f, 6f)
            offset += panChange
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 3f
                            }
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformState),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Zoomed Design Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentScale = ContentScale.Fit
                )
            }

            // Top Control indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PINCH TO ZOOM",
                        color = PrimaryGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Double tap to reset",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                        .testTag("close_zoom_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// CUSTOM HAND-STYLISH BLACK & GOLD NAVIGATION BAR
@Composable
fun BoutiqueExtendedBottomNavigation(
    viewModel: BoutiqueViewModel,
    currentScreen: Screen
) {
    val goldBrush = Brush.horizontalGradient(
        colors = listOf(PrimaryGold, SoftRose)
    )

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .topBorder(t = 1.dp, brush = Brush.linearGradient(listOf(PrimaryGold.copy(0.3f), Color.Transparent)))
            .shadow(8.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val darkTheme = isSystemInDarkTheme()

        // Home Navigation Item
        NavigationBarItem(
            selected = currentScreen is Screen.Home || currentScreen is Screen.CategoryDetail,
            onClick = { viewModel.navigateHome() },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Shop", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = if (darkTheme) Color.Black else Color.Black,
                selectedTextColor = PrimaryGold,
                indicatorColor = PrimaryGold,
                unselectedIconColor = if (darkTheme) MutedTextDark else MutedTextLight,
                unselectedTextColor = if (darkTheme) MutedTextDark else MutedTextLight
            ),
            modifier = Modifier.testTag("nav_home_button")
        )

        // Search Navigation Item
        NavigationBarItem(
            selected = currentScreen is Screen.Search,
            onClick = { viewModel.navigateTo(Screen.Search) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Search) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            label = { Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = if (darkTheme) Color.Black else Color.Black,
                selectedTextColor = PrimaryGold,
                indicatorColor = PrimaryGold,
                unselectedIconColor = if (darkTheme) MutedTextDark else MutedTextLight,
                unselectedTextColor = if (darkTheme) MutedTextDark else MutedTextLight
            ),
            modifier = Modifier.testTag("nav_search_button")
        )

        // Contact / Info Navigation Item
        NavigationBarItem(
            selected = currentScreen is Screen.Contact,
            onClick = { viewModel.navigateTo(Screen.Contact) },
            icon = {
                Icon(
                    imageVector = if (currentScreen is Screen.Contact) Icons.Filled.Info else Icons.Outlined.Info,
                    contentDescription = "Contact"
                )
            },
            label = { Text("Contact", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = if (darkTheme) Color.Black else Color.Black,
                selectedTextColor = PrimaryGold,
                indicatorColor = PrimaryGold,
                unselectedIconColor = if (darkTheme) MutedTextDark else MutedTextLight,
                unselectedTextColor = if (darkTheme) MutedTextDark else MutedTextLight
            ),
            modifier = Modifier.testTag("nav_contact_button")
        )

        // Owner Account Menu Access
        NavigationBarItem(
            selected = currentScreen is Screen.AdminLogin || currentScreen is Screen.AdminDashboard || currentScreen is Screen.AdminAddEditDesign,
            onClick = {
                if (viewModel.isAdminLoggedIn) {
                    viewModel.navigateTo(Screen.AdminDashboard)
                } else {
                    viewModel.navigateTo(Screen.AdminLogin)
                }
            },
            icon = {
                Icon(
                    imageVector = if (viewModel.isAdminLoggedIn) Icons.Filled.AdminPanelSettings else Icons.Outlined.Lock,
                    contentDescription = "Admin"
                )
            },
            label = { Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = PrimaryGold,
                indicatorColor = PrimaryGold,
                unselectedIconColor = if (darkTheme) MutedTextDark else MutedTextLight,
                unselectedTextColor = if (darkTheme) MutedTextDark else MutedTextLight
            ),
            modifier = Modifier.testTag("nav_admin_button")
        )
    }
}

// 1. BOUTIQUE HOME SCREEN
@Composable
fun BoutiqueHomeScreen(viewModel: BoutiqueViewModel) {
    val featured by viewModel.featuredDesigns.collectAsStateWithLifecycle()
    val recentArrivals by viewModel.recentDesigns.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // MAJESTIC LUXURY BOUTIQUE HERO INTRO
        item {
            ElevatedBoutiqueHeroSection()
        }

        // FEATURED COLLECTIONS CAROUSEL
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FEATURED DESIGNS",
                        color = if (isDark) AccentGold else DeepGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Box(
                        modifier = Modifier
                            .height(1.8.dp)
                            .weight(1f)
                            .padding(start = 12.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        PrimaryGold.copy(0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                if (featured.isEmpty()) {
                    EmptyCollectionState(tip = "No featured items yet.")
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(featured) { design ->
                            FeaturedDesignCard(
                                design = design,
                                onClick = { viewModel.navigateTo(Screen.DesignDetail(design.id)) }
                            )
                        }
                    }
                }
            }
        }

        // CATEGORY MULTI-GALLERY BUTTONS
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "EXPLORE CATEGORIES",
                    color = if (isDark) AccentGold else DeepGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Render aesthetic categories grid
                BoutiqueCategoriesGrid(
                    categories = viewModel.categories,
                    onCategoryClick = { category ->
                        viewModel.navigateTo(Screen.CategoryDetail(category))
                    }
                )
            }
        }

        // NEW ARRIVALS GRID PANELS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEW ARRIVALS",
                    color = if (isDark) AccentGold else DeepGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Box(
                    modifier = Modifier
                        .height(1.8.dp)
                        .weight(1f)
                        .padding(start = 12.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    PrimaryGold.copy(0.5f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        if (recentArrivals.isEmpty()) {
            item {
                EmptyCollectionState(tip = "Upload designs in the admin section.")
            }
        } else {
            items(recentArrivals.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (design in pair) {
                        Box(modifier = Modifier.weight(1f)) {
                            ArrivalDesignCard(
                                design = design,
                                onClick = { viewModel.navigateTo(Screen.DesignDetail(design.id)) }
                            )
                        }
                    }
                    if (pair.size < 2) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Bottom aesthetic buffer
        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// GORGEOUS LUXURY BANNER WITH LAYERED TEXT SHADOWS
@Composable
fun ElevatedBoutiqueHeroSection() {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) listOf(Color.Black, DarkSurface) else listOf(Color(0xFF1E1E1E), Color(0xFF111111))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Luxury graphic brush behind title
        Canvas(modifier = Modifier.fillMaxSize().blur(20.dp)) {
            drawCircle(
                color = PrimaryGold.copy(alpha = 0.15f),
                radius = size.minDimension / 1.5f,
                center = Offset(size.width / 2, size.height / 2)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Z I V A",
                color = PrimaryGold,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 12.sp,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "THE DESIGNER BOUTIQUE",
                color = Color.White.copy(0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(1.dp)
                    .background(PrimaryGold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Exclusive Sarees · Masterpiece Bridal · Custom Lehengas",
                color = AccentGold.copy(0.7f),
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )
        }
    }
}

// HORIZONTAL FEATURED DESIGNS VIEW CARD
@Composable
fun FeaturedDesignCard(
    design: Design,
    onClick: () -> Unit
) {
    val images = design.getImageList()
    val displayUrl = images.firstOrNull() ?: ""

    Card(
        modifier = Modifier
            .width(260.dp)
            .height(340.dp)
            .clickable(onClick = onClick)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = displayUrl,
                contentDescription = design.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Dynamic Gradient Overlays for High-End luxury look
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.1f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Category overlay tag
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd)
                    .background(PrimaryGold, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = design.category.uppercase(),
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Text Metadata overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = design.title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to view options",
                    color = PrimaryGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// DESIGN CATEGORIES GRID LAYOUT
@Composable
fun BoutiqueCategoriesGrid(
    categories: List<String>,
    onCategoryClick: (String) -> Unit
) {
    val imagePresets = mapOf(
        "Sarees" to "https://images.unsplash.com/photo-1610030469983-98e550d6193c?auto=format&fit=crop&w=400&q=80",
        "Bridal Wear" to "https://images.unsplash.com/photo-1595777457583-95e059d581b8?auto=format&fit=crop&w=400&q=80",
        "Lehengas" to "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?auto=format&fit=crop&w=400&q=80",
        "Salwars" to "https://images.unsplash.com/photo-1609357518652-6cf0416f0cbe?auto=format&fit=crop&w=400&q=80",
        "Blouse Designs" to "https://images.unsplash.com/photo-1621184455862-c163dfb30e0f?auto=format&fit=crop&w=400&q=80",
        "Kids Collection" to "https://images.unsplash.com/photo-1519689680058-324335c77eba?auto=format&fit=crop&w=400&q=80"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        val rows = categories.chunked(2)
        for (row in rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (category in row) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCategoryClick(category) }
                            .shadow(3.dp)
                    ) {
                        val imageUrl = imagePresets[category] ?: ""
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = category,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(0.2f),
                                            Color.Black.copy(0.75f)
                                        )
                                    )
                                )
                        )
                        Text(
                            text = category.uppercase(),
                            color = PrimaryGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// RECENT ARRIVALS INDIVIDUAL GRID CARD
@Composable
fun ArrivalDesignCard(
    design: Design,
    onClick: () -> Unit
) {
    val images = design.getImageList()
    val displayUrl = images.firstOrNull() ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = displayUrl,
                contentDescription = design.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(0.1f),
                                Color.Black.copy(0.8f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = design.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = design.category,
                    color = PrimaryGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// 2. BOUTIQUE CATEGORY SPECIFIC SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoutiqueCategoryScreen(
    viewModel: BoutiqueViewModel,
    categoryName: String
) {
    val allList by viewModel.allDesigns.collectAsStateWithLifecycle()
    val designs = allList.filter { it.category.equals(categoryName, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Luxury Top Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = categoryName.uppercase(),
                    color = PrimaryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    fontSize = 16.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go Back",
                        tint = PrimaryGold
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (designs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyCollectionState(tip = "No $categoryName creations online.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(designs) { design ->
                    ArrivalDesignCard(
                        design = design,
                        onClick = { viewModel.navigateTo(Screen.DesignDetail(design.id)) }
                    )
                }
            }
        }
    }
}

// Empty State helper
@Composable
fun EmptyCollectionState(tip: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.SentimentDissatisfied,
            contentDescription = "Empty",
            tint = PrimaryGold.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = tip,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

// 3. DESIGN DETAILED EXHIBITION SCREEN
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BoutiqueDesignDetailScreen(
    viewModel: BoutiqueViewModel,
    designId: Int
) {
    val context = LocalContext.current
    val designState = viewModel.getDesignFlow(designId).collectAsState(initial = null)
    val design = designState.value

    if (design == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryGold)
        }
        return
    }

    val imageList = design.getImageList()
    var selectedImageIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // IMAGE CORNER DISPLAY & NAV
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            val displayUrl = imageList.getOrNull(selectedImageIndex) ?: ""
            
            AsyncImage(
                model = displayUrl,
                contentDescription = design.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { viewModel.openZoom(displayUrl) },
                contentScale = ContentScale.Crop
            )

            // Soft Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(0.4f),
                                Color.Transparent,
                                Color.Black.copy(0.75f)
                            )
                        )
                    )
            )

            // Back button override
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = PrimaryGold
                )
            }

            // Overlay indicator for Pinch Zoom instructions
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ZoomIn,
                        contentDescription = "Zoom Icon",
                        tint = PrimaryGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TAP IMAGE TO ZOOM",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    )
                }
            }

            // Category overlay tag
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.BottomStart)
                    .background(PrimaryGold, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = design.category.uppercase(),
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // SWIPE PAGER EXTRA IMAGES PREVIEWS
        if (imageList.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                imageList.forEachIndexed { index, url ->
                    val isSelected = index == selectedImageIndex
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) PrimaryGold else Color.Gray.copy(0.4f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedImageIndex = index }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = "Previews",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // EXQUISITE TEXT METADATA WRAPPERS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = design.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                fontFamily = FontFamily.Serif
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "DESIGN HIGHLIGHTS & DESCRIPTION",
                color = PrimaryGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = design.description,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontSize = 15.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = PrimaryGold.copy(0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            // WHATSAPP CONTACT ACTION CARD
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E2F23) // High contrast premium dark green representing WA
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { launchWhatsApp(context, design.title, design.category) }
                    .shadow(4.dp)
                    .testTag("whatsapp_inquiry_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = "WhatsApp Inquiry",
                        tint = Color(0xFF25D366), // Signal bright green
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "INQUIRE VIA WHATSAPP",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Consult pricing & custom matching colors",
                            color = Color.White.copy(0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// 4. SEARCH & CATALOG SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoutiqueSearchScreen(viewModel: BoutiqueViewModel) {
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSearchCategory by viewModel.selectedSearchCategory.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search header title
        Text(
            text = "CATALOG SEARCH",
            color = if (isDark) AccentGold else DeepGold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        // Luxury text input
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search by name, fabric, or description...", color = Color.Gray) },
            prefix = { Icon(Icons.Default.Search, "Search", tint = PrimaryGold, modifier = Modifier.padding(end = 6.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, PrimaryGold.copy(0.4f), RoundedCornerShape(12.dp))
                .testTag("catalogue_search_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Horizontal Category Quick Filter Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val filterCategories = listOf("All") + viewModel.categories
            items(filterCategories) { category ->
                val isSelected = selectedSearchCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) PrimaryGold else MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            if (isSelected) PrimaryGold else Color.Gray.copy(0.3f),
                            RoundedCornerShape(50)
                        )
                        .clickable { viewModel.updateSelectedSearchCategory(category) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Exhibition Grid Results
        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyCollectionState(tip = "No designer match found. Try another phrase!")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_results_grid")
            ) {
                items(searchResults) { design ->
                    ArrivalDesignCard(
                        design = design,
                        onClick = { viewModel.navigateTo(Screen.DesignDetail(design.id)) }
                    )
                }
            }
        }
    }
}

// 5. CONTACT & STORE LOCATIONS SCREEN
@Composable
fun BoutiqueContactScreen(viewModel: BoutiqueViewModel) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Page Title
        Text(
            text = "OUR SHOWROOM",
            color = if (isDark) AccentGold else DeepGold,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Showroom visual card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=800&q=80", // boutique store front
                    contentDescription = "Ziva Showroom",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.8f))
                            )
                        )
                )
                Text(
                    text = "VISIT ZIVA THE DESIGNER BOUTIQUE",
                    color = PrimaryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Store address and contact cards
        Text(
            text = "STORE SPECIFICATIONS & HOURS",
            color = PrimaryGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ElevatedContactRow(icon = Icons.Default.LocationOn, label = "Flagship Address", value = "No 15, Premium Fashion Arcade, Jubilee Hills, Hyderabad - 500033")
        ElevatedContactRow(icon = Icons.Default.Phone, label = "Inquiries & Bookings", value = "+91 98765-43210 / +91 91234-56789")
        ElevatedContactRow(icon = Icons.Default.Email, label = "Official Email", value = "concierge@zivaboutique.com")
        ElevatedContactRow(icon = Icons.Filled.Schedule, label = "Showroom Hours", value = "Monday - Sunday: 10:30 AM - 08:30 PM")

        Spacer(modifier = Modifier.height(24.dp))

        // ACTION BUTTONS FOR GPS DIRECTION AND WHATSAPP
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("geo:17.4375,78.4011?q=Ziva+Designer+Boutique+Jubilee+Hills+Hyderabad")
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Directions intent launched safely.", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("directions_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Navigation, "Directions", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GET DIRECTIONS VIA GPS", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:+919876543210")
                }
                context.startActivity(intent)
            },
            border = BorderStroke(1.dp, PrimaryGold),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("phone_call_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PhoneCallback, "Call Showroom", tint = PrimaryGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CALL CONCIERGE DESK", color = PrimaryGold, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ElevatedContactRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = PrimaryGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label.uppercase(),
                    color = PrimaryGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// 6. ADMlN SECURITY ENTRY SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoutiqueAdminLoginScreen(viewModel: BoutiqueViewModel) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Dark cinematic portal background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Luxury Icon header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PrimaryGold.copy(0.15f))
                .border(1.dp, PrimaryGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AdminPanelSettings,
                contentDescription = "Admin lock logo",
                tint = PrimaryGold,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ZIVA BOUTIQUE OWNER GATEWAY",
            color = PrimaryGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.5.sp
        )
        Text(
            text = "Please enter key to unlock management console",
            color = Color.White.copy(0.6f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Passcode entry
        TextField(
            value = password,
            onValueChange = { password = it },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            placeholder = { Text("Passcode (admin)", color = Color.Gray) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Visibility",
                        tint = PrimaryGold
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PrimaryGold.copy(0.7f), RoundedCornerShape(12.dp))
                .testTag("admin_password_input"),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (viewModel.loginError != null) {
            Text(
                text = viewModel.loginError ?: "",
                color = Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Button(
            onClick = { viewModel.loginAdmin(password) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("admin_login_submit_button")
        ) {
            Text("AUTHENTICATE", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { viewModel.navigateHome() }
        ) {
            Text("CANCEL & DEPART", color = PrimaryGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 7. ADMIN DASHBOARD SHOWING STATISTICS & CRUD OPERATIONS
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BoutiqueAdminDashboardScreen(viewModel: BoutiqueViewModel) {
    val designs by viewModel.allDesigns.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aesthetic Top Panel
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "OWNER DASHBOARD",
                    color = PrimaryGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 16.sp
                )
            },
            actions = {
                IconButton(onClick = { viewModel.logoutAdmin() }) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = "Log out",
                        tint = PrimaryGold
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Management Analytics Overview Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsCard(
                title = "TOTAL CREATIONS",
                value = designs.size.toString(),
                icon = Icons.Filled.Inventory,
                modifier = Modifier.weight(1f)
            )
            AnalyticsCard(
                title = "FEATURED DESIGNS",
                value = designs.count { it.isFeatured }.toString(),
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Header for recent listings plus visual FAB Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MANAGE DESIGN ENTRIES",
                color = if (isDark) AccentGold else DeepGold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            // Dynamic Action FAB Button
            Button(
                onClick = { viewModel.navigateTo(Screen.AdminAddEditDesign(null)) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_add_design_mains_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ADD DESIGN", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Table List of live designs with Edit & Delete actions
        if (designs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyCollectionState(tip = "No designer creations exist yet. Tap Add above!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(designs) { design ->
                    AdminDesignRowItem(
                        design = design,
                        onEdit = { viewModel.navigateTo(Screen.AdminAddEditDesign(design.id)) },
                        onDelete = { viewModel.deleteDesign(design.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, color = PrimaryGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AdminDesignRowItem(
    design: Design,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val imageUrl = design.getImageList().firstOrNull() ?: ""
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = design.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = design.category.uppercase(),
                            color = PrimaryGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle logs",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Action Buttons
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("admin_edit_${design.id}_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryGold)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("admin_delete_${design.id}_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(0.8f))
                    }
                }
            }
            
            // Expanded tracking panel with timestamp & action history details
            AnimatedVisibility(visible = isExpanded) {
                val formattedLastModified = remember(design.lastModified) {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(design.lastModified))
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CHANGE LOG HISTORY",
                            color = PrimaryGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Last Change: $formattedLastModified",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val logs = design.actionHistory.split("\n").filter { it.isNotBlank() }
                    logs.forEach { log ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                color = PrimaryGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = log,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 8. ADD/EDIT FULL DESIGN SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoutiqueAdminAddEditScreen(
    viewModel: BoutiqueViewModel,
    designId: Int?
) {
    val isEdit = designId != null
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(viewModel.categories.first()) }
    var description by remember { mutableStateOf("") }
    var imageUrls by remember { mutableStateOf("") }
    var isFeatured by remember { mutableStateOf(false) }

    // Preset luxury images suggestions row in add design flow, allowing easy preview loading inside emulator!
    val premiumPresets = listOf(
        "https://images.unsplash.com/photo-1610030469983-98e550d6193c?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1595777457583-95e059d581b8?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1609357518652-6cf0416f0cbe?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1621184455862-c163dfb30e0f?auto=format&fit=crop&w=400&q=80"
    )

    // Load initial edit values if applicable
    LaunchedEffect(designId) {
        if (isEdit && designId != null) {
            viewModel.allDesigns.value.find { it.id == designId }?.let { design ->
                title = design.title
                category = design.category
                description = design.description
                imageUrls = design.imageUrls
                isFeatured = design.isFeatured
            }
        }
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Mini Top Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = if (isEdit) "EDIT DESIGN ENTRY" else "NEW DESIGN ENTRY",
                    color = PrimaryGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.5.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Cancel", tint = PrimaryGold)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text values inputs
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Design Title", color = PrimaryGold) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_field_title"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = Color.Gray.copy(0.4f),
                    focusedLabelColor = PrimaryGold
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            // Category Picker
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Display Category", color = PrimaryGold) },
                    trailingIcon = {
                        IconButton(onClick = { dropdownExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = PrimaryGold)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_field_category_trigger"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGold,
                        unfocusedBorderColor = Color.Gray.copy(0.4f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    viewModel.categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                category = cat
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Description Box
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Exquisite Description & Specifications", color = PrimaryGold) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("admin_field_description"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = Color.Gray.copy(0.4f),
                    focusedLabelColor = PrimaryGold
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 5
            )

            // Images URLs Box
            OutlinedTextField(
                value = imageUrls,
                onValueChange = { imageUrls = it },
                label = { Text("Image URLs (for multiple, separate with commas)", color = PrimaryGold) },
                placeholder = { Text("https://example.com/dress.jpg") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_field_image_urls"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGold,
                    unfocusedBorderColor = Color.Gray.copy(0.4f),
                    focusedLabelColor = PrimaryGold
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Dynamic preset loader row helper to bypass typing long URLs
            Column {
                Text(
                    text = "TAP A PRESET BOUTIQUE IMAGE TO AUTO-FILL:",
                    color = PrimaryGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(premiumPresets) { url ->
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, PrimaryGold.copy(0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    if (imageUrls.isBlank()) {
                                        imageUrls = url
                                    } else {
                                        if (!imageUrls.contains(url)) {
                                            imageUrls = "$imageUrls,$url"
                                        }
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Is Featured switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryGold.copy(0.08f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "FEATURE ON HOME PAGE", color = PrimaryGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Displays top in customer collections carousel", color = MaterialTheme.colorScheme.onBackground.copy(0.5f), fontSize = 10.sp)
                }
                Switch(
                    checked = isFeatured,
                    onCheckedChange = { isFeatured = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = PrimaryGold
                    ),
                    modifier = Modifier.testTag("admin_featured_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save Submissions FAB Trigger Button
            Button(
                onClick = {
                    if (title.isBlank() || description.isBlank() || imageUrls.isBlank()) {
                        // Empty validations toast
                        return@Button
                    }
                    viewModel.saveDesign(
                        id = designId ?: 0,
                        title = title,
                        category = category,
                        description = description,
                        imageUrls = imageUrls,
                        isFeatured = isFeatured
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_design_button"),
                enabled = title.isNotBlank() && description.isNotBlank() && imageUrls.isNotBlank()
            ) {
                Text(
                    text = if (isEdit) "UPDATE WEAVING ENTRY" else "PUBLISH DESIGN IN CATALOG",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// Extension to avoid XML layouts or unstylish border modifiers
fun Modifier.topBorder(t: androidx.compose.ui.unit.Dp, brush: Brush): Modifier = this.drawBehind {
    drawLine(
        brush = brush,
        start = Offset(0f, 0f),
        end = Offset(this@drawBehind.size.width, 0f),
        strokeWidth = t.toPx()
    )
}

// WhatsApp launch helper function
fun launchWhatsApp(context: Context, designName: String, category: String) {
    val message = "Hi Ziva Boutique! I am extremely interested in your design: $designName ($category). Please share pricing and availability details."
    val url = "https://api.whatsapp.com/send?phone=919876543210&text=${Uri.encode(message)}"
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: Copy to clipboard and alert
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ziva Boutique Message", message)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Link copied! Design details copied to your clipboard.", Toast.LENGTH_LONG).show()
    }
}
