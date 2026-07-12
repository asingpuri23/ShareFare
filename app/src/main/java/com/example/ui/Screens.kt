package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MockRider
import com.example.Place
import com.example.Screen
import com.example.ShareFareViewModel
import com.example.data.DisputeEntity
import com.example.data.MessageEntity
import com.example.data.RideMatchEntity
import com.example.data.UserEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppNavigation(viewModel: ShareFareViewModel) {
    val currentScreen = viewModel.currentScreen

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                Screen.LOGIN -> LoginScreen(viewModel)
                Screen.SET_PROFILE -> SetProfileScreen(viewModel)
                Screen.MAIN -> MainScreen(viewModel)
                Screen.SET_PAYMENTS_LIST -> SettlePaymentsScreen(viewModel)
                Screen.MATCH_RIDERS -> MatchRidersScreen(viewModel)
                Screen.MATCHED_USER -> MatchedUserScreen(viewModel)
                Screen.MATCHED_CHAT -> MatchedUserChatScreen(viewModel)
                Screen.SET_PAYMENTS_SCREEN -> SetPaymentsScreen(viewModel)
            }
        }
    }
}

// Global top bar with Cancel Button (Feature 20)
@Composable
fun ScreenHeader(
    title: String,
    onCancel: () -> Unit,
    showBackButton: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showBackButton && onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("header_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        IconButton(
            onClick = onCancel,
            modifier = Modifier
                .size(48.dp)
                .testTag("header_cancel_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel current action",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

// 1. LOGIN SCREEN (Feature 1)
@Composable
fun LoginScreen(viewModel: ShareFareViewModel) {
    var phone by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F9))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Elegant Icon with Geometric Balance shape
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "ShareFare Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "ShareFare",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF1D1B20),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Smart commuting. Shared ride fares.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } && it.length <= 10) {
                            phone = it
                            viewModel.loginPhone = it
                        }
                    },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("Enter 10 digit number") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone Icon", tint = Color(0xFF64748B))
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_phone_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.loginOrRegister()
                    },
                    enabled = phone.length == 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// 2. REGISTER / SET PROFILE SCREEN (Feature 1, Feature 7)
@Composable
fun SetProfileScreen(viewModel: ShareFareViewModel) {
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val isEditing = loggedInUser != null

    var name by remember { mutableStateOf(loggedInUser?.name ?: "") }
    var email by remember { mutableStateOf(loggedInUser?.email ?: "") }
    var gender by remember { mutableStateOf(loggedInUser?.gender ?: "Male") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        ScreenHeader(
            title = if (isEditing) "Edit Profile" else "Create Profile",
            onCancel = {
                if (isEditing) viewModel.navigateBack() else viewModel.logout()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp)
                .widthIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (isEditing) "Update your profile information" else "Complete your registration to start sharing fares",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_email_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = "Gender",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Male", "Female", "Other").forEach { option ->
                    val selected = gender == option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { gender = option }
                            .testTag("gender_option_$option"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isEditing) {
                        viewModel.updateProfile(name, email, gender)
                    } else {
                        viewModel.registerName = name
                        viewModel.registerEmail = email
                        viewModel.registerGender = gender
                        viewModel.completeRegistration()
                    }
                },
                enabled = name.isNotBlank() && email.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("profile_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save Profile",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// 3. MAIN SCREEN (MainActivity Page - Features 2, 3, 4, 5, 6, 7, 19)
@Composable
fun MainScreen(viewModel: ShareFareViewModel) {
    val user by viewModel.loggedInUser.collectAsStateWithLifecycle()
    val activeRide by viewModel.activeRide.collectAsStateWithLifecycle()
    val disputesAgainstUser by viewModel.disputesAgainstUser.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var sourceDropdownOpen by remember { mutableStateOf(false) }
    var destDropdownOpen by remember { mutableStateOf(false) }

    val disputeCount = disputesAgainstUser.size
    val isBlocked = disputeCount >= 3
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F9))
            .navigationBarsPadding()
    ) {
        // --- FEATURE 2: Accurate user location displayed on simulated live map ---
        SimulatedMapCanvas(
            userLat = viewModel.userLat,
            userLng = viewModel.userLng,
            isBroadcasting = viewModel.isBroadcasting,
            sourceSelected = viewModel.selectedSourcePlace != null,
            destSelected = viewModel.selectedDestPlace != null,
            modifier = Modifier.fillMaxSize()
        )

        // Centered Map Pin (Geometric Balance style)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-20).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pin head with shadow effect
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(primaryColor, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }
            // Little downward triangle at the bottom of the pin:
            Canvas(modifier = Modifier.size(12.dp, 8.dp)) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height)
                    close()
                }
                drawPath(path, color = primaryColor)
            }
            Spacer(modifier = Modifier.height(6.dp))
            // YOU ARE HERE badge
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "YOU ARE HERE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF1D1B20),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Floating UI Column (Top Area: Search Inputs and Header)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Main Top Bar with More Menu (Feature 7)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Car Logo",
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ShareFare",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color(0xFF1D1B20)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFF1F5F9), CircleShape)
                            .testTag("more_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color(0xFF1D1B20)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Set Profile Information") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                viewModel.navigateTo(Screen.SET_PROFILE)
                            },
                            modifier = Modifier.testTag("menu_profile")
                        )
                        DropdownMenuItem(
                            text = { Text("Settle Payments (${disputeCount})") },
                            leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                viewModel.navigateTo(Screen.SET_PAYMENTS_LIST)
                            },
                            modifier = Modifier.testTag("menu_settle_payments")
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                viewModel.logout()
                            },
                            modifier = Modifier.testTag("menu_logout")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- FEATURE 19: Notification regarding active disputes ---
            if (disputeCount > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBlocked) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.SET_PAYMENTS_LIST) }
                        .testTag("dispute_alert_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Dispute Warning",
                            tint = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBlocked) "Rides Blocked (3+ Disputes)" else "Pending Disputes Detected",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isBlocked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = if (isBlocked) "You cannot search or request rides. Click to resolve your $disputeCount disputes."
                                else "You have $disputeCount payment disputes. Settle them to avoid search blocks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isBlocked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- FEATURE 3 & 4: Search Bars with manual inputs & Google Maps simulated suggestion ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Source search bar
                    Box {
                        TextField(
                            value = viewModel.sourceQuery,
                            onValueChange = {
                                viewModel.sourceQuery = it
                                sourceDropdownOpen = true
                            },
                            placeholder = { Text("Where are you now?", color = Color.Gray) },
                            leadingIcon = {
                                // Geometric Balance Source Icon: Blue dot inside a container
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color(0xFF3B82F6), CircleShape)
                                    )
                                }
                            },
                            trailingIcon = {
                                if (viewModel.sourceQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        viewModel.sourceQuery = ""
                                        viewModel.selectedSourcePlace = null
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            },
                            singleLine = true,
                            enabled = !isBlocked,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("source_search_input")
                        )

                        // Autocomplete list
                        if (sourceDropdownOpen && viewModel.sourceQuery.isNotEmpty() && viewModel.selectedSourcePlace == null) {
                            val filtered = viewModel.popularPlaces.filter {
                                it.name.lowercase().contains(viewModel.sourceQuery.lowercase())
                            }
                            if (filtered.isNotEmpty()) {
                                DropdownMenu(
                                    expanded = true,
                                    onDismissRequest = { sourceDropdownOpen = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    filtered.forEach { place ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(place.name, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        "Lat: ${String.format("%.4f", place.lat)}, Lng: ${String.format("%.4f", place.lng)}",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectSource(place)
                                                sourceDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Separator line exactly matching HTML:ml-5
                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(start = 40.dp, end = 16.dp)
                    )

                    // Destination search bar
                    Box {
                        TextField(
                            value = viewModel.destQuery,
                            onValueChange = {
                                viewModel.destQuery = it
                                destDropdownOpen = true
                            },
                            placeholder = { Text("Where to?", color = Color.Gray) },
                            leadingIcon = {
                                // Geometric Balance Destination Icon: Rotated gray square (diamond)
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(8.dp)) {
                                        val path = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(size.width / 2f, 0f)
                                            lineTo(size.width, size.height / 2f)
                                            lineTo(size.width / 2f, size.height)
                                            lineTo(0f, size.height / 2f)
                                            close()
                                        }
                                        drawPath(path, color = Color(0xFF94A3B8))
                                    }
                                }
                            },
                            trailingIcon = {
                                if (viewModel.destQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        viewModel.destQuery = ""
                                        viewModel.selectedDestPlace = null
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            },
                            singleLine = true,
                            enabled = !isBlocked,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dest_search_input")
                        )

                        // Autocomplete list
                        if (destDropdownOpen && viewModel.destQuery.isNotEmpty() && viewModel.selectedDestPlace == null) {
                            val filtered = viewModel.popularPlaces.filter {
                                it.name.lowercase().contains(viewModel.destQuery.lowercase())
                            }
                            if (filtered.isNotEmpty()) {
                                DropdownMenu(
                                    expanded = true,
                                    onDismissRequest = { destDropdownOpen = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    filtered.forEach { place ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(place.name, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        "Lat: ${String.format("%.4f", place.lat)}, Lng: ${String.format("%.4f", place.lng)}",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectDest(place)
                                                destDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Controls Container (Geometric Balance Style - Full Bleed Sheet)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- FEATURE 5: Option to enter number of riders (Polished Pill Row) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "NUMBER OF RIDERS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (viewModel.numRiders == 1) "1 Passenger" else "${viewModel.numRiders} Passengers",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1D1B20)
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Styled decrease button (White circle with outline)
                        IconButton(
                            onClick = { if (viewModel.numRiders > 1) viewModel.numRiders-- },
                            enabled = !isBlocked,
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                                .background(Color.White, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = Color(0xFF1D1B20)
                            )
                        }

                        // Styled increase button (Solid dark circle)
                        IconButton(
                            onClick = { if (viewModel.numRiders < 4) viewModel.numRiders++ },
                            enabled = !isBlocked,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1D1B20), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = Color.White
                            )
                        }
                    }
                }

                // --- FEATURE 6: Activated search similar riders button ---
                val isSearchEnabled = viewModel.selectedSourcePlace != null && viewModel.selectedDestPlace != null && !isBlocked
                Button(
                    onClick = { viewModel.searchSimilarRiders() },
                    enabled = isSearchEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("search_similar_riders_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (viewModel.isBroadcasting) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Broadcasting request within 200m...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.People, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search Similar Riders",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Dispute Warning / Info Badge at the base
                if (disputeCount == 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your profile is in good standing (0 disputes)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.SET_PAYMENTS_LIST) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFEF4444), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pending Disputes Detected ($disputeCount) - Settle Now",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }

    // Modal popup warning on open (Feature 19)
    if (viewModel.showDisputeAlertOnOpen && disputeCount > 0) {
        AlertDialog(
            onDismissRequest = { viewModel.showDisputeAlertOnOpen = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unresolved Disputes!")
                }
            },
            text = {
                Text(
                    text = if (isBlocked)
                        "You currently have $disputeCount raised disputes against you. The app features are BLOCKED. You must resolve disputes to 1 or less to unlock ride searching."
                        else "You have $disputeCount pending payment disputes. Please settle them at the earliest to prevent your account from being locked."
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.showDisputeAlertOnOpen = false
                    viewModel.navigateTo(Screen.SET_PAYMENTS_LIST)
                }) {
                    Text("Settle Now")
                }
            },
            dismissButton = {
                if (!isBlocked) {
                    TextButton(onClick = { viewModel.showDisputeAlertOnOpen = false }) {
                        Text("Later")
                    }
                }
            }
        )
    }
}

// 4. SETTLE PAYMENTS SCREEN (SettlePaymentsActivity Window - Feature 8, Feature 19)
@Composable
fun SettlePaymentsScreen(viewModel: ShareFareViewModel) {
    val disputesAgainstUser by viewModel.disputesAgainstUser.collectAsStateWithLifecycle()
    val currentDispute = viewModel.activeDisputeToSettle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        ScreenHeader(
            title = "Settle Disputes",
            onCancel = { viewModel.navigateBack() }
        )

        if (currentDispute != null) {
            // Dispute Settle Chat view inside SettlePaymentsActivity
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                // Partner details header
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = viewModel.currentDisputeUser?.name?.take(2) ?: "P",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dispute raised by ${currentDispute.raisedByName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Disputed Amount: ₹${String.format("%.2f", currentDispute.amount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.error)
                            )
                        }
                    }
                }

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(viewModel.disputeChatMessages) { (text, isMe) ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 16.dp
                                ),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = text,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Attach screenshot options (Feature 8)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.attachScreenshotToDispute() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("attach_screenshot_button")
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Attach Proof")
                    }

                    // Resolve button (Feature 8)
                    Button(
                        onClick = { viewModel.resolveActiveDispute() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("resolve_dispute_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Resolve")
                    }
                }

                // Chat Input box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = viewModel.disputeChatInput,
                        onValueChange = { viewModel.disputeChatInput = it },
                        placeholder = { Text("Message to settle...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dispute_message_input"),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.sendDisputeChatMessage() },
                        enabled = viewModel.disputeChatInput.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .testTag("dispute_message_send")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        } else {
            // Main list of disputes
            if (disputesAgainstUser.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = "No Disputes",
                            tint = Color.Gray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No Disputes Raised Against You!", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Keep up the great commute behavior!", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                Text(
                    text = "The following users raised payment disputes against you. Select any user to chat, attach screenshots of payments, and resolve the dispute.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(disputesAgainstUser) { dispute ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openDisputeChat(dispute) }
                                .testTag("dispute_item_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dispute.raisedByName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("Disputed amount: ₹${String.format("%.2f", dispute.amount)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = "Resolve Chat")
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. MATCH RIDERS SCREEN (MatchRidersActivity Window - Feature 10)
@Composable
fun MatchRidersScreen(viewModel: ShareFareViewModel) {
    val riders = viewModel.matchingRiders

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        ScreenHeader(
            title = "Nearby Riders Match",
            onCancel = { viewModel.navigateBack() }
        )

        Text(
            text = "Found riders within 200m of your source whose destinations are within 500m of your destination.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (riders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Searching nearby commuters...", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(riders) { rider ->
                    var isMatchingByMe by remember { mutableStateOf(false) }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rider_match_card_${rider.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (rider.gender == "Female") Icons.Default.Face else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rider.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("${rider.gender} • ${rider.numRiders} Riders", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${rider.initialDistanceMeters.toInt()}m away",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    isMatchingByMe = true
                                    viewModel.initiateMatch(rider)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("match_button_${rider.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isMatchingByMe) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isMatchingByMe) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onSecondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Waiting for Rider's click...")
                                } else {
                                    Icon(Icons.Default.Handshake, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Match and Share Fare")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. MATCHED USER SCREEN (MatchedRidersActivity / MatchedUserActivity - Features 11, 13)
@Composable
fun MatchedUserScreen(viewModel: ShareFareViewModel) {
    val activeRide by viewModel.activeRide.collectAsStateWithLifecycle()
    val partner = viewModel.selectedMockRider

    if (activeRide == null || partner == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val distanceMeters = viewModel.simulatedDistanceMeters
    val isWithin20m = distanceMeters <= 20.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        // Top Header with Unmatch (Feature 11)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Matched Route",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Unmatch button
            Button(
                onClick = { viewModel.unmatchActiveRide() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("unmatch_button")
            ) {
                Text("Unmatch", fontWeight = FontWeight.Bold)
            }
        }

        // Relative map visualization (Feature 11)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.DarkGray)
        ) {
            RelativeMapCanvas(
                distanceMeters = distanceMeters,
                partnerName = partner.name,
                modifier = Modifier.fillMaxSize()
            )

            // Floating details & Chat button (Feature 12)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(partner.name.take(2), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(partner.name, fontWeight = FontWeight.Bold)
                                Text("Going to: ${activeRide?.destName?.take(28)}...", fontSize = 11.sp, color = Color.Gray)
                            }

                            // Chat trigger
                            IconButton(
                                onClick = { viewModel.navigateTo(Screen.MATCHED_CHAT) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .testTag("chat_trigger_button")
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Walk simulator control (Feature 13)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Distance between you:", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    if (distanceMeters > 0.0) "${distanceMeters.toInt()} meters" else "Arrived (0 meters)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = if (isWithin20m) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!isWithin20m) {
                                Button(
                                    onClick = {
                                        if (viewModel.isWalkingSimulated) viewModel.stopWalkSimulation()
                                        else viewModel.simulateWalkCloser()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                    modifier = Modifier.testTag("walk_simulator_button")
                                ) {
                                    Text(if (viewModel.isWalkingSimulated) "Stop" else "Simulate Walk", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Within 20m!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // --- FEATURE 13: Activated Start Ride Share Button ---
                Button(
                    onClick = { viewModel.startRideShare() },
                    enabled = isWithin20m,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_ride_share_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (isWithin20m) "Start Ride Share" else "Meet Rider (Within 20m) to Start",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// 7. MATCHED USER CHAT SCREEN (MatchedUserChatActivity - Feature 12)
@Composable
fun MatchedUserChatScreen(viewModel: ShareFareViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val partner = viewModel.selectedMockRider
    val user by viewModel.loggedInUser.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    if (partner == null || user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        ScreenHeader(
            title = "Chat: ${partner.name}",
            onCancel = { viewModel.navigateBack() },
            showBackButton = true,
            onBack = { viewModel.navigateBack() }
        )

        // Suggestion chips at top of chat window to discuss address (Feature 12)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("I'm near the main entrance.", "Meet me at the auto stand.", "Booking cab now!").forEach { phrase ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .clickable { viewModel.sendMessage(phrase) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(phrase, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Chat list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                val currentUser = user
                val isMe = currentUser != null && msg.senderId == currentUser.id
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Column(
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 0.dp,
                                bottomEnd = if (isMe) 0.dp else 16.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Input field row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Discuss pickup address (< 5 min walk)...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input"),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    viewModel.sendMessage(input)
                    input = ""
                },
                enabled = input.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .testTag("chat_message_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

// 8. SET PAYMENTS SCREEN (SetPaymentsScreenActivity - Features 14, 15, 16, 17, 18, 19)
@Composable
fun SetPaymentsScreen(viewModel: ShareFareViewModel) {
    val activeRide by viewModel.activeRide.collectAsStateWithLifecycle()
    val partner = viewModel.selectedMockRider
    val focusManager = LocalFocusManager.current

    if (activeRide == null || partner == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        ScreenHeader(
            title = "Set Ride Payments",
            onCancel = { viewModel.navigateBack() }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!viewModel.showPaymentReceipt) {
                // Input and calculations
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Set Ride Payment Amount",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = viewModel.setPaymentAmountInput,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() || char == '.' }) {
                                    viewModel.setPaymentAmountInput = it
                                    viewModel.proposePaymentAmount()
                                }
                            },
                            label = { Text("Total Fare Amount (₹)") },
                            prefix = { Text("₹ ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("payment_amount_input")
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.proposePaymentAmount()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Calculate Fare Splits")
                        }
                    }
                }

                // Split display results (Feature 17)
                viewModel.paymentCalculationResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Distance-Weighted Ride Splits",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Accept and Deny buttons (Feature 16)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.denyPaymentProposal() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("deny_payment_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Deny")
                        }

                        Button(
                            onClick = { viewModel.acceptPaymentProposal(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("accept_payment_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Accept")
                        }
                    }

                    // Acceptance status list
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (viewModel.paymentAcceptedByUser1) Icons.Default.CheckCircle else Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = if (viewModel.paymentAcceptedByUser1) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Your acceptance")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (viewModel.paymentAcceptedByUser2) Icons.Default.CheckCircle else Icons.Default.Circle,
                                    contentDescription = null,
                                    tint = if (viewModel.paymentAcceptedByUser2) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${partner.name}'s acceptance")
                            }
                        }
                    }

                    if (viewModel.paymentAcceptedByUser1 && viewModel.paymentAcceptedByUser2) {
                        Button(
                            onClick = { viewModel.submitRidePayment() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("confirm_ride_start_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Split Accepted! Start Shared Trip", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // --- FEATURE 18: Payment done or Raise dispute buttons at the end of the ride ---
                Text(
                    text = "Ride in Progress...",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                }

                Text(
                    text = "Arrived at Destination!\nTotal Ride Fare: ₹${viewModel.setPaymentAmountInput}",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.completePayment(raisedDispute = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("payment_done_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payment Done (Paid Share)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.completePayment(raisedDispute = true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("raise_dispute_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Raise Payment Dispute", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// SIMULATED MAP CANVAS (Feature 2)
@Composable
fun SimulatedMapCanvas(
    userLat: Double,
    userLng: Double,
    isBroadcasting: Boolean,
    sourceSelected: Boolean,
    destSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f

        // Draw ambient grid (Simulated Map streets)
        val gridSpacing = 80f
        val gridColor = Color(0x1F808080)
        for (i in 0..(width / gridSpacing).toInt()) {
            val x = i * gridSpacing
            drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
        }
        for (i in 0..(height / gridSpacing).toInt()) {
            val y = i * gridSpacing
            drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
        }

        // Draw some streets
        drawRect(
            color = Color(0x0C808080),
            topLeft = Offset(cx - 30f, 0f),
            size = androidx.compose.ui.geometry.Size(60f, height)
        )
        drawRect(
            color = Color(0x0C808080),
            topLeft = Offset(0f, cy - 30f),
            size = androidx.compose.ui.geometry.Size(width, 60f)
        )

        // Draw 200m search radius circle if broadcasting
        if (isBroadcasting) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.15f),
                radius = 250f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = primaryColor,
                radius = 250f,
                center = Offset(cx, cy),
                style = Stroke(width = 3f)
            )
        }

        // If source or destination are selected, show them with Geometric Balance designs
        if (sourceSelected) {
            // Blue circle for source matching text input indicator
            drawCircle(
                color = Color(0xFF3B82F6),
                radius = 16f,
                center = Offset(cx - 150f, cy - 100f)
            )
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = Offset(cx - 150f, cy - 100f)
            )
        }

        if (destSelected) {
            // Rotated slate square (diamond) for destination matching text input indicator
            val destCenter = Offset(cx + 180f, cy + 220f)
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(destCenter.x, destCenter.y - 14f)
                lineTo(destCenter.x + 14f, destCenter.y)
                lineTo(destCenter.x, destCenter.y + 14f)
                lineTo(destCenter.x - 14f, destCenter.y)
                close()
            }
            drawPath(path, color = Color(0xFF64748B))
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = destCenter
            )
        }
    }
}

// RELATIVE MAP CANVAS (Feature 11)
@Composable
fun RelativeMapCanvas(
    distanceMeters: Double,
    partnerName: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f

        // Draw grid
        val gridSpacing = 80f
        val gridColor = Color(0x1F808080)
        for (i in 0..(width / gridSpacing).toInt()) {
            val x = i * gridSpacing
            drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
        }
        for (i in 0..(height / gridSpacing).toInt()) {
            val y = i * gridSpacing
            drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
        }

        // Map road connecting User and Partner
        // Scale offset based on simulated distance (150m maps to 200px max)
        val maxOffsetPx = 220f
        val scaleVal = (distanceMeters / 150.0).toFloat()
        val scale = if (scaleVal > 1f) 1f else scaleVal
        val currentOffsetPx = maxOffsetPx * scale

        val partnerX = cx + currentOffsetPx
        val partnerY = cy - currentOffsetPx

        // Path between them
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(cx, cy),
            end = Offset(partnerX, partnerY),
            strokeWidth = 6f
        )

        // Current User Pin (Blue / Primary)
        drawCircle(
            color = primaryColor,
            radius = 12f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = Offset(cx, cy)
        )

        // Partner Pin (Green / Tertiary)
        drawCircle(
            color = tertiaryColor,
            radius = 12f,
            center = Offset(partnerX, partnerY)
        )
        drawCircle(
            color = Color.White,
            radius = 6f,
            center = Offset(partnerX, partnerY)
        )

        // Distance text overlay in canvas
        // This makes it look like a professional mapping HUD
        if (distanceMeters > 0.0) {
            drawCircle(
                color = tertiaryColor.copy(alpha = 0.15f),
                radius = 30f + currentOffsetPx,
                center = Offset(cx, cy),
                style = Stroke(width = 1f)
            )
        }
    }
}
