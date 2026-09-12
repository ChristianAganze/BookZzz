package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.network.FirebaseClient
import com.example.network.GeminiClient
import com.example.ui.components.ReceiptGenerator
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Navigation Targets
enum class Screen {
    Splash,
    Auth,
    ClientDashboard,
    HotelAdminDashboard,
    SuperAdminDashboard
}

@Composable
fun MainScreen(
    repository: BookZzzRepository,
    preferencesManager: PreferencesManager = PreferencesManager(LocalContext.current)
) {
    val navController = rememberNavController()
    val currentUser by repository.currentUser.collectAsState()
    val hotels by repository.hotels.collectAsState()
    val bookings by repository.bookings.collectAsState()
    val rooms by repository.rooms.collectAsState()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        composable("splash") {
            SplashScreen(onTimeout = {
                val user = repository.currentUser.value
                val dest = if (user != null) {
                    when (user.role.uppercase()) {
                        "CLIENT" -> "client_dashboard"
                        "HOTEL_ADMIN" -> "hotel_admin_dashboard"
                        "SUPER_ADMIN" -> "super_admin_dashboard"
                        else -> "showcase"
                    }
                } else {
                    "showcase"
                }
                navController.navigate(dest) {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("showcase") {
            ShowcaseLandingScreen(
                repository = repository,
                onExploreClientClick = {
                    navController.navigate("client_dashboard")
                },
                onPartnerLoginSuccess = { user ->
                    Toast.makeText(context, "Bienvenue ${user.name} !", Toast.LENGTH_SHORT).show()
                    val dest = if (user.registeredHotelName.isNullOrBlank()) {
                        "partner_onboarding"
                    } else {
                        "hotel_admin_dashboard"
                    }
                    navController.navigate(dest)
                }
            )
        }

        composable("partner_onboarding") {
            val user = repository.currentUser.value ?: UserProfile("U_TEMP", "Partenaire", "partenaire@bookzzz.com", "HotelAdmin")
            PartnerOnboardingScreen(
                currentUser = user,
                repository = repository,
                onOnboardingFinished = {
                    Toast.makeText(context, "Établissement initialisé avec succès !", Toast.LENGTH_LONG).show()
                    navController.navigate("hotel_admin_dashboard") {
                        popUpTo("partner_onboarding") { inclusive = true }
                    }
                },
                onCancel = {
                    navController.navigate("showcase") {
                        popUpTo("partner_onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("auth") {
            AuthScreen(
                repository = repository,
                onLoginSuccess = { user ->
                    Toast.makeText(context, "Bienvenue ${user.name} !", Toast.LENGTH_SHORT).show()
                    val dest = when (user.role.uppercase()) {
                        "CLIENT" -> "client_dashboard"
                        "HOTEL_ADMIN" -> "hotel_admin_dashboard"
                        "SUPER_ADMIN" -> "super_admin_dashboard"
                        else -> "client_dashboard"
                    }
                    navController.navigate(dest) {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("client_dashboard") {
            ClientDashboard(
                repository = repository,
                hotels = hotels,
                bookings = bookings,
                onLogout = {
                    repository.logout()
                    navController.navigate("showcase") {
                        popUpTo("client_dashboard") { inclusive = true }
                    }
                },
                onOpenShowcase = {
                    navController.navigate("showcase")
                },
                preferencesManager = preferencesManager
            )
        }

        composable("hotel_admin_dashboard") {
            HotelAdminDashboardScreen(
                repository = repository,
                hotels = hotels,
                bookings = bookings,
                rooms = rooms,
                currentUser = currentUser,
                onLogout = {
                    repository.logout()
                    navController.navigate("showcase") {
                        popUpTo("hotel_admin_dashboard") { inclusive = true }
                    }
                },
                onOpenShowcase = {
                    navController.navigate("showcase")
                }
            )
        }

        composable("super_admin_dashboard") {
            SuperAdminDashboard(
                repository = repository,
                hotels = hotels,
                bookings = bookings,
                onLogout = {
                    repository.logout()
                    navController.navigate("auth") {
                        popUpTo("super_admin_dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}

// --- 2. Authentication & Onboarding Screen ---
@Composable
fun AuthScreen(
    repository: BookZzzRepository,
    onLoginSuccess: (UserProfile) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    var authTab by remember { mutableStateOf("EMAIL") } // "PHONE", "EMAIL", "GOOGLE"
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpCountdown by remember { mutableStateOf(0) }
    var generatedOtp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun validateInputs(): Boolean {
        var isValid = true
        nameError = null
        emailError = null
        phoneError = null
        passwordError = null

        if (isSignUpMode && name.isBlank()) {
            nameError = "Le nom est requis"
            isValid = false
        }

        if (authTab == "EMAIL") {
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailError = "Format d'email invalide"
                isValid = false
            }
            if (password.length < 6) {
                passwordError = "Minimum 6 caractères"
                isValid = false
            }
        } else if (authTab == "PHONE") {
            if (phoneNumber.length < 8) {
                phoneError = "Numéro trop court"
                isValid = false
            }
        }
        return isValid
    }

    // OTP Countdown Timer Logic
    LaunchedEffect(isOtpSent, otpCountdown) {
        if (isOtpSent && otpCountdown > 0) {
            delay(1000L)
            otpCountdown -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Hotel,
                    contentDescription = "BookZZZ Logo",
                    tint = Color(0xFF3498DB),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF2C3E50), fontWeight = FontWeight.Black)) {
                            append("Book")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF3498DB), fontWeight = FontWeight.Black)) {
                            append("ZZZ")
                        }
                    },
                    fontSize = 36.sp,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = if (isSignUpMode) "Rejoignez la révolution hôtelière" else "Bon retour parmi nous",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            // Main Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isSignUpMode) "Créer un compte" else "Connexion",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; nameError = null },
                            label = { Text("Nom complet") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            isError = nameError != null,
                            supportingText = nameError?.let { { Text(it) } }
                        )
                    }

                    // Authentification Methods Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        val tabs = listOf("EMAIL" to "Email", "PHONE" to "Mobile", "GOOGLE" to "Google")
                        tabs.forEach { (tabId, label) ->
                            val isSelected = authTab == tabId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable { 
                                        authTab = tabId
                                        isOtpSent = false
                                        generatedOtp = ""
                                        otpInput = ""
                                        nameError = null
                                        emailError = null
                                        phoneError = null
                                        passwordError = null
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(targetState = authTab, label = "AuthForms") { targetTab ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            when (targetTab) {
                                "EMAIL" -> {
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it; emailError = null },
                                        label = { Text("Adresse Email") },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF3498DB)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        singleLine = true,
                                        isError = emailError != null,
                                        supportingText = emailError?.let { { Text(it) } }
                                    )

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it; passwordError = null },
                                        label = { Text("Mot de passe") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3498DB)) },
                                        trailingIcon = {
                                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                                Icon(icon, contentDescription = "Toggle password", tint = Color.Gray)
                                            }
                                        },
                                        visualTransformation = if (passwordVisible) {
                                            androidx.compose.ui.text.input.VisualTransformation.None
                                        } else {
                                            androidx.compose.ui.text.input.PasswordVisualTransformation()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        singleLine = true,
                                        isError = passwordError != null,
                                        supportingText = passwordError?.let { { Text(it) } }
                                    )
                                }
                                "PHONE" -> {
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it; phoneError = null },
                                        label = { Text("Numéro de Téléphone") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF3498DB)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        placeholder = { Text("+243 ...") },
                                        singleLine = true,
                                        isError = phoneError != null,
                                        supportingText = phoneError?.let { { Text(it) } }
                                    )

                                    if (isOtpSent) {
                                        OutlinedTextField(
                                            value = otpInput,
                                            onValueChange = { otpInput = it },
                                            label = { Text("Code OTP") },
                                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF3498DB)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (phoneNumber.length < 8) {
                                                phoneError = "Format invalide"
                                                return@Button
                                            }
                                            val otp = (1000..9999).random().toString()
                                            generatedOtp = otp
                                            isOtpSent = true
                                            otpCountdown = 60
                                            Toast.makeText(context, "OTP de test : $otp", Toast.LENGTH_LONG).show()
                                        },
                                        enabled = otpCountdown == 0,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F4F8), contentColor = Color(0xFF3498DB)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(if (otpCountdown > 0) "Renvoyer dans ${otpCountdown}s" else "Envoyer OTP")
                                    }
                                }
                                "GOOGLE" -> {
                                    Button(
                                        onClick = { Toast.makeText(context, "Service bientôt disponible", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Gray),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, Color.LightGray)
                                    ) {
                                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("Continuer avec Google")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!validateInputs()) return@Button
                            
                            if (authTab == "PHONE" && !isOtpSent) {
                                Toast.makeText(context, "Envoyez d'abord le code !", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (authTab == "PHONE" && otpInput != generatedOtp) {
                                Toast.makeText(context, "OTP incorrect", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val userEmail = when(authTab) {
                                "EMAIL" -> email
                                "PHONE" -> "$phoneNumber@bookzzz.com"
                                else -> "google@bookzzz.com"
                            }

                            FirebaseClient.performLogin(
                                email = userEmail,
                                name = if (isSignUpMode) name else "Utilisateur",
                                context = context,
                                repository = repository,
                                onSuccess = { onLoginSuccess(it) },
                                onFailure = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3498DB))
                    ) {
                        Text(
                            text = if (isSignUpMode) "S'inscrire" else "Se connecter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    TextButton(
                        onClick = { isSignUpMode = !isSignUpMode },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isSignUpMode) "Déjà un compte ? Connectez-vous" else "Pas de compte ? Inscrivez-vous",
                            color = Color(0xFF3498DB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "BookZZZ • Expertise RDC • © 2026",
                fontSize = 12.sp,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}

// --- 3. Client Dashboard & Flow ---
@Composable
fun ClientDashboard(
    repository: BookZzzRepository,
    hotels: List<Hotel>,
    bookings: List<Booking>,
    onLogout: () -> Unit,
    onOpenShowcase: () -> Unit = {},
    preferencesManager: PreferencesManager = PreferencesManager(LocalContext.current)
) {
    val coroutineScope = rememberCoroutineScope()
    val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = false)
    val currentLanguage by preferencesManager.language.collectAsState(initial = "Français")
    val notificationsEnabled by preferencesManager.notificationsEnabled.collectAsState(initial = true)
    
    val simulatedOffline by preferencesManager.simulatedOffline.collectAsState(initial = false)
    // Force rebuild
    var isDeviceOnline by remember { mutableStateOf(preferencesManager.isDeviceOnline()) }
    LaunchedEffect(Unit) {
        while (true) {
            isDeviceOnline = preferencesManager.isDeviceOnline()
            kotlinx.coroutines.delay(3000)
        }
    }
    val isAppOnline = !simulatedOffline && isDeviceOnline
    val context = LocalContext.current

    var showLanguageDialog by remember { mutableStateOf(false) }

    val labelAccueil = when (currentLanguage) {
        "English" -> "Home"
        "Swahili" -> "Mwanzo"
        else -> "Accueil"
    }
    val labelFavoris = when (currentLanguage) {
        "English" -> "Favorites"
        "Swahili" -> "Vipendwa"
        else -> "Favoris"
    }
    val labelFAQ = "FAQ"
    val labelParametres = when (currentLanguage) {
        "English" -> "Settings"
        "Swahili" -> "Mipangilio"
        else -> "Paramètres"
    }

    var selectedCityTab by remember { mutableStateOf("Tous") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedHotelForDetail by remember { mutableStateOf<Hotel?>(null) }
    var selectedRoomForBooking by remember { mutableStateOf<Pair<Hotel, Room>?>(null) }
    
    // Tab selector state based on screenshot: 
    // 0 = Accueil (Hotels Explore)
    // 1 = Favoris (Favorite hotels)
    // 2 = FAQ (Frequently Asked Questions & AI Agent chat bridge)
    // 3 = Paramètres (Settings screen from the user's uploaded image)
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Favorites set state (pre-load hotel H1 and H3 as a delightful default!)
    var favoriteHotelIds by remember { mutableStateOf(setOf<String>("H1", "H3")) }
    
    // Support nested screens and flows
    var showMyBookingsInFullScreen by remember { mutableStateOf(false) }
    var showAIChatInFullScreen by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }

    val cities = listOf("Tous", "Goma", "Kinshasa", "Bukavu")
    
    val filteredHotels = hotels.filter {
        val matchesTab = (selectedCityTab == "Tous" || it.city.equals(selectedCityTab, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() || 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.city.contains(searchQuery, ignoreCase = true) || 
                it.description.contains(searchQuery, ignoreCase = true)
        
        matchesTab && matchesSearch && !it.isSuspended
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Row(modifier = Modifier.fillMaxSize()) {
        if (isWideScreen) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxHeight(),
                header = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 24.dp)
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Landscape,
                            contentDescription = "BookZZZ Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            ) {
                Spacer(modifier = Modifier.weight(1f))
                NavigationRailItem(
                    selected = selectedTab == 0 && selectedHotelForDetail == null && selectedRoomForBooking == null && !showMyBookingsInFullScreen && !showAIChatInFullScreen,
                    onClick = {
                        selectedTab = 0
                        selectedHotelForDetail = null
                        selectedRoomForBooking = null
                        showMyBookingsInFullScreen = false
                        showAIChatInFullScreen = false
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = labelAccueil) },
                    label = { Text(labelAccueil, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_rail_explore")
                )
                Spacer(modifier = Modifier.height(16.dp))
                NavigationRailItem(
                    selected = selectedTab == 1 && selectedHotelForDetail == null && selectedRoomForBooking == null && !showMyBookingsInFullScreen && !showAIChatInFullScreen,
                    onClick = {
                        selectedTab = 1
                        selectedHotelForDetail = null
                        selectedRoomForBooking = null
                        showMyBookingsInFullScreen = false
                        showAIChatInFullScreen = false
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = labelFavoris) },
                    label = { Text(labelFavoris, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_rail_favorites")
                )
                Spacer(modifier = Modifier.height(16.dp))
                NavigationRailItem(
                    selected = selectedTab == 2 && selectedHotelForDetail == null && selectedRoomForBooking == null && !showMyBookingsInFullScreen && !showAIChatInFullScreen,
                    onClick = {
                        selectedTab = 2
                        selectedHotelForDetail = null
                        selectedRoomForBooking = null
                        showMyBookingsInFullScreen = false
                        showAIChatInFullScreen = false
                    },
                    icon = { Icon(Icons.Default.HelpOutline, contentDescription = labelFAQ) },
                    label = { Text(labelFAQ, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_rail_faq")
                )
                Spacer(modifier = Modifier.height(16.dp))
                NavigationRailItem(
                    selected = selectedTab == 3 && selectedHotelForDetail == null && selectedRoomForBooking == null && !showMyBookingsInFullScreen && !showAIChatInFullScreen,
                    onClick = {
                        selectedTab = 3
                        selectedHotelForDetail = null
                        selectedRoomForBooking = null
                        showMyBookingsInFullScreen = false
                        showAIChatInFullScreen = false
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = labelParametres) },
                    label = { Text(labelParametres, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_rail_settings")
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Scaffold(
            bottomBar = {
                if (!isWideScreen && selectedHotelForDetail == null && selectedRoomForBooking == null && !showMyBookingsInFullScreen && !showAIChatInFullScreen) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        NavigationBarItem(
                            selected = selectedTab == 0 && selectedHotelForDetail == null && selectedRoomForBooking == null && !showMyBookingsInFullScreen && !showAIChatInFullScreen,
                            onClick = {
                                selectedTab = 0
                                selectedHotelForDetail = null
                                selectedRoomForBooking = null
                                showMyBookingsInFullScreen = false
                                showAIChatInFullScreen = false
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = labelAccueil) },
                            label = { Text(labelAccueil) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1 && selectedHotelForDetail == null && selectedRoomForBooking == null && !showMyBookingsInFullScreen && !showAIChatInFullScreen,
                            onClick = {
                                selectedTab = 1
                                selectedHotelForDetail = null
                                selectedRoomForBooking = null
                                showMyBookingsInFullScreen = false
                                showAIChatInFullScreen = false
                            },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = labelFavoris) },
                            label = { Text(labelFavoris) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2 && selectedHotelForDetail == null && selectedRoomForBooking == null && !showMyBookingsInFullScreen && !showAIChatInFullScreen,
                            onClick = {
                                selectedTab = 2
                                selectedHotelForDetail = null
                                selectedRoomForBooking = null
                                showMyBookingsInFullScreen = false
                                showAIChatInFullScreen = false
                            },
                            icon = { Icon(Icons.Default.HelpOutline, contentDescription = labelFAQ) },
                            label = { Text(labelFAQ) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3 && selectedHotelForDetail == null && selectedRoomForBooking == null && !showMyBookingsInFullScreen && !showAIChatInFullScreen,
                            onClick = {
                                selectedTab = 3
                                selectedHotelForDetail = null
                                selectedRoomForBooking = null
                                showMyBookingsInFullScreen = false
                                showAIChatInFullScreen = false
                            },
                            icon = { Icon(Icons.Default.Settings, contentDescription = labelParametres) },
                            label = { Text(labelParametres) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isWideScreen) PaddingValues(0.dp) else innerPadding)
            ) {
                if (selectedRoomForBooking != null) {
                    val (currentHotel, currentRoom) = selectedRoomForBooking!!
                    RoomBookingScreen(
                        hotel = currentHotel,
                        room = currentRoom,
                        repository = repository,
                        currentLanguage = currentLanguage,
                        isWideScreen = isWideScreen,
                        onBackClick = { selectedRoomForBooking = null },
                        onBookingSuccess = {
                            selectedRoomForBooking = null
                            selectedHotelForDetail = null
                            showMyBookingsInFullScreen = true
                        }
                    )
                } else if (selectedHotelForDetail != null) {
                    val currentHotel = selectedHotelForDetail!!
                    val isFav = favoriteHotelIds.contains(currentHotel.id)
                    HotelDetailScreen(
                        hotel = currentHotel,
                        isFavorite = isFav,
                        onFavoriteToggle = {
                            favoriteHotelIds = if (isFav) favoriteHotelIds - currentHotel.id else favoriteHotelIds + currentHotel.id
                        },
                        repository = repository,
                        currentLanguage = currentLanguage,
                        isWideScreen = isWideScreen,
                        onBackClick = { selectedHotelForDetail = null },
                        onBookRoom = { hotel, room ->
                            selectedRoomForBooking = Pair(hotel, room)
                        }
                    )
                } else if (showMyBookingsInFullScreen) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        ) {
                            IconButton(onClick = { showMyBookingsInFullScreen = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (currentLanguage) {
                                    "English" -> "My Hotel Bookings"
                                    "Swahili" -> "Uhifadhi Wangu wa Hoteli"
                                    else -> "Mes Réservations d'Hôtels"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        MyBookingsScreen(
                            bookings = bookings,
                            repository = repository,
                            currentLanguage = currentLanguage,
                            isAppOnline = isAppOnline
                        )
                    }
                } else if (showAIChatInFullScreen) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        ) {
                            IconButton(onClick = { showAIChatInFullScreen = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Assistant IA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        TravelAgentChatScreen()
                    }
                } else {
                    when (selectedTab) {
                        0 -> {
                            // TAB 0: EXPLORE / ACCUEIL
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))

                                // Head Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF221C2B),
                                            border = BorderStroke(1.dp, Color(0xFFCCA865).copy(alpha = 0.5f)),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Hotel,
                                                    contentDescription = "BookZzz Logo",
                                                    tint = Color(0xFFCCA865),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            val user = repository.currentUser.value
                                            Text(
                                                text = buildAnnotatedString {
                                                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)) {
                                                        append("BOOK")
                                                    }
                                                    withStyle(style = SpanStyle(color = Color(0xFFCCA865), fontWeight = FontWeight.Black)) {
                                                        append("ZZZ")
                                                    }
                                                },
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "L'art de bien dormir • ${when (currentLanguage) {
                                                    "English" -> "Hello, ${user?.name ?: "Traveler"}"
                                                    "Swahili" -> "Jambo, ${user?.name ?: "Msafiri"}"
                                                    else -> "Bonjour, ${user?.name ?: "Voyageur"}"
                                                }}",
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val userInitials = repository.currentUser.value?.name
                                            ?.split(" ")
                                            ?.mapNotNull { it.firstOrNull()?.toString() }
                                            ?.take(2)
                                            ?.joinToString("") ?: "VO"
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                                .border(2.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = userInitials.uppercase(),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = onOpenShowcase,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF221C2B))
                                                .border(1.dp, Color(0xFFCCA865).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Language,
                                                contentDescription = "Site Vitrine & Pro",
                                                tint = Color(0xFFCCA865),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { showMap = !showMap },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (showMap) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Icon(
                                                imageVector = if (showMap) Icons.Default.List else Icons.Default.Map,
                                                contentDescription = "Toggle Map/List",
                                                tint = if (showMap) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        IconButton(
                                            onClick = onLogout,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.errorContainer)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Logout,
                                                contentDescription = "Logout",
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Search Bar
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            text = when (currentLanguage) {
                                                "English" -> "Search Goma, Bukavu..."
                                                "Swahili" -> "Tafuta Goma, Bukavu..."
                                                else -> "Rechercher à Goma, Bukavu..."
                                            },
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 13.sp
                                        )
                                    },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .testTag("search_bar"),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    cities.forEach { city ->
                                        val isSelected = selectedCityTab == city
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                                .clickable { selectedCityTab = city }
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = if (city == "Tous") {
                                                    when (currentLanguage) {
                                                        "English" -> "All"
                                                        "Swahili" -> "Zote"
                                                        else -> "Tous"
                                                    }
                                                } else city,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Hotels List
                                if (filteredHotels.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Outlined.Hotel,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = Color.LightGray
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Aucun établissement disponible",
                                                fontWeight = FontWeight.Medium,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        if (isWideScreen) {
                                            val chunks = filteredHotels.chunked(2)
                                            items(chunks) { rowHotels ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    rowHotels.forEach { hotel ->
                                                        val isFav = favoriteHotelIds.contains(hotel.id)
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            HotelCard(
                                                                hotel = hotel,
                                                                isFavorite = isFav,
                                                                onFavoriteToggle = {
                                                                    favoriteHotelIds = if (isFav) favoriteHotelIds - hotel.id else favoriteHotelIds + hotel.id
                                                                },
                                                                onHotelClick = {
                                                                    selectedHotelForDetail = hotel
                                                                },
                                                                onLocateClick = {
                                                                    openHotelLocationInMaps(context, hotel)
                                                                }
                                                            )
                                                        }
                                                    }
                                                    if (rowHotels.size < 2) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        } else {
                                            items(filteredHotels) { hotel ->
                                                val isFav = favoriteHotelIds.contains(hotel.id)
                                                HotelCard(
                                                    hotel = hotel,
                                                    isFavorite = isFav,
                                                    onFavoriteToggle = {
                                                        favoriteHotelIds = if (isFav) favoriteHotelIds - hotel.id else favoriteHotelIds + hotel.id
                                                    },
                                                    onHotelClick = {
                                                        selectedHotelForDetail = hotel
                                                    },
                                                    onLocateClick = {
                                                        openHotelLocationInMaps(context, hotel)
                                                    }
                                                )
                                            }
                                        }
                                        item { Spacer(modifier = Modifier.height(24.dp)) }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // TAB 1: FAVORIS
                            val favoriteHotels = hotels.filter { favoriteHotelIds.contains(it.id) && !it.isSuspended }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (currentLanguage) {
                                            "English" -> "Your Favorites"
                                            "Swahili" -> "Vipendwa Vyako"
                                            else -> "Vos Favoris"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                if (favoriteHotels.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FavoriteBorder,
                                                contentDescription = null,
                                                modifier = Modifier.size(72.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = when (currentLanguage) {
                                                    "English" -> "No favorite hosting yet"
                                                    "Swahili" -> "Hakuna vipendwa bado"
                                                    else -> "Aucun hébergement favori"
                                                },
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = when (currentLanguage) {
                                                    "English" -> "Tap the heart icon on any hotel photo on home tab to add them here."
                                                    "Swahili" -> "Gonga aikoni ya moyo kwenye picha yoyote ya hoteli ili kuongeza hapa."
                                                    else -> "Touchez le cœur blanc sur la photo des hôtels de l'accueil pour les sauvegarder ici de manière permanente."
                                                },
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(favoriteHotels) { hotel ->
                                            HotelCard(
                                                hotel = hotel,
                                                isFavorite = true,
                                                onFavoriteToggle = {
                                                    favoriteHotelIds = favoriteHotelIds - hotel.id
                                                },
                                                onHotelClick = {
                                                    selectedHotelForDetail = hotel
                                                },
                                                onLocateClick = {
                                                    openHotelLocationInMaps(context, hotel)
                                                }
                                            )
                                        }
                                        item { Spacer(modifier = Modifier.height(24.dp)) }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // TAB 2: FAQ & ASSISTANT IA (Dynamic JSON Multilingual)
                            var expandedFAQIndex by remember { mutableStateOf<Int?>(null) }
                            val context = LocalContext.current
                            val faqs = remember(currentLanguage) {
                                FaqManager.getFaqs(context, currentLanguage)
                            }

                            val faqTitle = when (currentLanguage) {
                                "English" -> "Frequently Asked Questions"
                                "Swahili" -> "Maswali Yanayoulizwa Mara kwa Mara"
                                else -> "Foire Aux Questions"
                            }
                            val faqSubtitle = when (currentLanguage) {
                                "English" -> "Answers to frequently asked questions about BookZzz:"
                                "Swahili" -> "Majibu ya maswali ya kawaida kuhusu BookZzz:"
                                else -> "Réponses aux questions les plus fréquentes sur l'application BookZzz :"
                            }
                            val aiButtonText = when (currentLanguage) {
                                "English" -> "Chat with AI Travel Assistant"
                                "Swahili" -> "Zungumza na Msaidizi wa AI"
                                else -> "Discuter avec l'IA Guide de Voyage"
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = faqTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    item {
                                        Text(
                                            text = faqSubtitle,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    items(faqs.size) { index ->
                                        val faqItem = faqs[index]
                                        val isExpanded = expandedFAQIndex == index

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { expandedFAQIndex = if (isExpanded) null else index },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = faqItem.question,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                if (isExpanded) {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Text(
                                                        text = faqItem.answer,
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        lineHeight = 18.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    item { Spacer(modifier = Modifier.height(16.dp)) }
                                }

                                // Interactive Gemini AI Bridge button at the bottom of the FAQ list
                                Button(
                                    onClick = { showAIChatInFullScreen = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(aiButtonText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                        3 -> {
                            // TAB 3: PARAMÈTRES (Matches the uploaded reference image perfectly but fully localized & interactive!)
                            val localeParam = when (currentLanguage) {
                                "English" -> "Settings"
                                "Swahili" -> "Mipangilio"
                                else -> "Paramètres"
                            }
                            val localePref = when (currentLanguage) {
                                "English" -> "PREFERENCES"
                                "Swahili" -> "VIPENDELEO"
                                else -> "PRÉFÉRENCES"
                            }
                            val localeLang = when (currentLanguage) {
                                "English" -> "Language"
                                "Swahili" -> "Lugha"
                                else -> "Langue"
                            }
                            val localeNotif = when (currentLanguage) {
                                "English" -> "Notifications"
                                "Swahili" -> "Arifa"
                                else -> "Notifications"
                            }
                            val localeNotifSub = when (currentLanguage) {
                                "English" -> "Manage your notifications"
                                "Swahili" -> "Dhibiti arifa zako"
                                else -> "Gérez vos notifications"
                            }
                            val localeDark = when (currentLanguage) {
                                "English" -> "Dark Mode"
                                "Swahili" -> "Njia ya giza"
                                else -> "Mode sombre"
                            }
                            val localeDarkSub = when (currentLanguage) {
                                "English" -> if (isDarkMode) "Enabled" else "Disabled"
                                "Swahili" -> if (isDarkMode) "Imewezeshwa" else "Imezimwa"
                                else -> if (isDarkMode) "Activé" else "Désactivé"
                            }
                            val localeBook = when (currentLanguage) {
                                "English" -> "My Bookings"
                                "Swahili" -> "Uhifadhi Wangu"
                                else -> "Mes Réservations"
                            }
                            val localeBookSub = when (currentLanguage) {
                                "English" -> "Histories, statuses and receipts"
                                "Swahili" -> "Historia, hali na stakabadhi"
                                else -> "Historiques, statuts et tickets"
                            }
                            val localeAbout = when (currentLanguage) {
                                "English" -> "ABOUT"
                                "Swahili" -> "KUHUSU"
                                else -> "À PROPOS"
                            }
                            val localeVersion = when (currentLanguage) {
                                "English" -> "App Version"
                                "Swahili" -> "Toleo la programu"
                                else -> "Version de l'application"
                            }
                            val localeFooter = when (currentLanguage) {
                                "English" -> "© 2026 BookZzz Co. All rights reserved."
                                "Swahili" -> "© 2026 BookZzz Co. Haki zote zimehifadhiwa."
                                else -> "© 2026 BookZzz Co. Tous droits réservés."
                            }

                            val localeConnTitle = when (currentLanguage) {
                                "English" -> "CONNECTIVITY"
                                "Swahili" -> "MUUNGANISHO"
                                else -> "CONNECTIVITÉ"
                            }
                            val localeConnStatusLabel = when (currentLanguage) {
                                "English" -> "Connection Status"
                                "Swahili" -> "Hali ya Muunganisho"
                                else -> "État de la Connexion"
                            }
                            val localeConnOnline = when (currentLanguage) {
                                "English" -> "Online (Active)"
                                "Swahili" -> "Kwenye Mtandao (Inafanya kazi)"
                                else -> "En ligne (Opérationnel)"
                            }
                            val localeConnOffline = when (currentLanguage) {
                                "English" -> "Offline (Local mode)"
                                "Swahili" -> "Nje ya Mtandao (Nia ya ndani)"
                                else -> "Hors-ligne (Actif)"
                            }
                            val localeSimulateOffline = when (currentLanguage) {
                                "English" -> "Simulate Offline Mode"
                                "Swahili" -> "Iga Hali ya Nje ya Mtandao"
                                else -> "Simuler le Mode Hors-ligne"
                            }
                            val localeSimulateOfflineSub = when (currentLanguage) {
                                "English" -> "Force app to simulate no internet"
                                "Swahili" -> "Lazimisha programu kuiga hakuna mtandao"
                                else -> "Simuler l'absence de réseau internet"
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                // Header: Gear icon and title
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Gear",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = localeParam,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // CATEGORY: PRÉFÉRENCES
                                Text(
                                    text = localePref,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        // Row 1: Langue
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showLanguageDialog = true }
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Language,
                                                contentDescription = "Language",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = localeLang,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = currentLanguage,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )

                                        // Row 2: Notifications
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Notifications",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = localeNotif,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = localeNotifSub,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = notificationsEnabled,
                                                onCheckedChange = { newVal ->
                                                    coroutineScope.launch {
                                                        preferencesManager.setNotificationsEnabled(newVal)
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                                )
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )

                                        // Row 3: Mode sombre
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    coroutineScope.launch {
                                                        preferencesManager.setDarkMode(!isDarkMode)
                                                    }
                                                }
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DarkMode,
                                                contentDescription = "Dark Mode",
                                                tint = if (isDarkMode) Color(0xFFF1C40F) else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = localeDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = localeDarkSub,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = isDarkMode,
                                                onCheckedChange = { checked ->
                                                    coroutineScope.launch {
                                                        preferencesManager.setDarkMode(checked)
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                                )
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )

                                        // Row 3.b: Mes Réservations
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showMyBookingsInFullScreen = true }
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = "Bookings",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = localeBook,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = localeBookSub,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (bookings.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = bookings.size.toString(),
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(28.dp))

                                // CATEGORY: CONNECTIVITY / CONNECTIVITÉ
                                Text(
                                    text = localeConnTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        // Row 1: Connection Status Indicator
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isAppOnline) Color(0xFF2ECC71).copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isAppOnline) Color(0xFF2ECC71) else MaterialTheme.colorScheme.error)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = localeConnStatusLabel,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = if (isAppOnline) localeConnOnline else localeConnOffline,
                                                    fontSize = 13.sp,
                                                    color = if (isAppOnline) Color(0xFF27AE60) else MaterialTheme.colorScheme.error,
                                                    fontWeight = FontWeight.Bold
                                                 )
                                            }
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )

                                        // Row 2: Offline Simulation Toggle Switch
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudOff,
                                                contentDescription = "Offline Mode",
                                                tint = if (simulatedOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = localeSimulateOffline,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = localeSimulateOfflineSub,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Switch(
                                                checked = simulatedOffline,
                                                onCheckedChange = { checked ->
                                                    coroutineScope.launch {
                                                        preferencesManager.setSimulatedOffline(checked)
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = MaterialTheme.colorScheme.error,
                                                    checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(28.dp))

                                // CATEGORY: À PROPOS
                                Text(
                                    text = localeAbout,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Version",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = localeVersion,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "1.0.0",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(36.dp))

                                // Sphere BookZzz Centered Brand Logo
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Surface(
                                            modifier = Modifier.size(90.dp),
                                            shape = CircleShape,
                                            color = Color(0xFF221C2B),
                                            border = BorderStroke(2.dp, Color(0xFFCCA865).copy(alpha = 0.6f)),
                                            shadowElevation = 4.dp
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxSize().padding(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Hotel,
                                                    contentDescription = "BookZzz",
                                                    tint = Color(0xFFCCA865),
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "BOOKZZZ",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFFCCA865),
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "« L'art de bien dormir »",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFCCA865),
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = localeFooter,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Language Selection Dialog
            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = {
                        Text(
                            text = when (currentLanguage) {
                                "English" -> "Select Language"
                                "Swahili" -> "Chagua Lugha"
                                else -> "Choisir la langue"
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Français", "English", "Swahili").forEach { lang ->
                                val isSelected = currentLanguage == lang
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                        .clickable {
                                            coroutineScope.launch {
                                                preferencesManager.setLanguage(lang)
                                                showLanguageDialog = false
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = lang,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text(
                                text = when (currentLanguage) {
                                    "English" -> "Close"
                                    "Swahili" -> "Funga"
                                    else -> "Fermer"
                                },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(28.dp)
                )
            }
        }
    }
}

// Custom Hotel Card UI Component (Full Bleed Image with text and controls overlaid)
@Composable
fun HotelCard(
    hotel: Hotel,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onHotelClick: () -> Unit,
    onLocateClick: () -> Unit = {}
) {
    val hotelImageUrl = remember(hotel.id) {
        when (hotel.id) {
            "H1" -> "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80"
            "H2" -> "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80"
            "H3" -> "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=800&q=80"
            else -> "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .clickable { onHotelClick() }
            .testTag("hotel_card_${hotel.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full card image
            AsyncImage(
                model = hotelImageUrl,
                contentDescription = hotel.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            // Dynamic scrim gradient overlay for top & bottom contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.60f),
                                Color.Black.copy(alpha = 0.20f),
                                Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            // Content container on top of image
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP OVERLAY ROW: Badges + Action Buttons (Locate & Favorite)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // "POPULAIRE" Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "POPULAIRE",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // City Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.55f)
                        ) {
                            Text(
                                text = hotel.city,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Action buttons (Locate + Favorite)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Locate / Map Button
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.60f),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { onLocateClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Localiser l'hôtel",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Favorite Heart Button
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.60f),
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { onFavoriteToggle() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favoris",
                                    tint = if (isFavorite) Color.Red else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // BOTTOM OVERLAY SECTION: Hotel name, star rating, address, price and CTA button
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = hotel.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        fontFamily = FontFamily.Serif,
                        maxLines = 1
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Star Rating
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val ratingFloor = hotel.rating.toInt().coerceIn(1, 5)
                            val starText = "★".repeat(ratingFloor) + "☆".repeat(5 - ratingFloor)
                            Text(
                                text = starText,
                                color = Color(0xFFF1C40F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${hotel.rating} (128 avis)",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text("•", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)

                        Text(
                            text = if (hotel.city.equals("Goma", ignoreCase = true)) {
                                "Boulevard Kanyamuhanga"
                            } else if (hotel.city.equals("Kinshasa", ignoreCase = true)) {
                                "Boulevard du 30 Juin"
                            } else {
                                "Avenue Maniema, Lac Kivu"
                            },
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    // Available Rooms Badge Indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.20f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "🛏️ 3 Types de chambres : Standard • Deluxe • Suite",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "À partir de",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$${hotel.basePricePerNight.roundToInt()} USD / nuit",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        }

                        Button(
                            onClick = onHotelClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("book_button_${hotel.id}"),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text("Voir les chambres", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Helper Date Comparison Functions for Booking History ---
fun isFutureBooking(arrivalDateStr: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val bookingDate = sdf.parse(arrivalDateStr) ?: return true
        
        // Base our simulation date around June 15, 2026
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JUNE)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val bookingCal = Calendar.getInstance().apply {
            time = bookingDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        !bookingCal.before(todayCal)
    } catch (e: Exception) {
        true
    }
}

fun getStayBadgeInfo(arrivalDateStr: String, currentLanguage: String): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val bookingDate = sdf.parse(arrivalDateStr) ?: return ""
        
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JUNE)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val bookingCal = Calendar.getInstance().apply {
            time = bookingDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val diffMs = bookingCal.timeInMillis - todayCal.timeInMillis
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        
        if (diffDays == 0) {
            when (currentLanguage) {
                "English" -> "Today"
                "Swahili" -> "Leo"
                else -> "Aujourd'hui"
            }
        } else if (diffDays > 0) {
            when (currentLanguage) {
                "English" -> "In $diffDays days"
                "Swahili" -> "Katika siku $diffDays"
                else -> "Dans $diffDays jours"
            }
        } else {
            val absDays = kotlin.math.abs(diffDays)
            when (currentLanguage) {
                "English" -> "Completed $absDays days ago"
                "Swahili" -> "Ilikamilika siku $absDays zilizopita"
                else -> "Terminé il y a $absDays jours"
            }
        }
    } catch (e: Exception) {
        ""
    }
}

// --- 3.b My Bookings UI View ---
@Composable
fun MyBookingsScreen(
    bookings: List<Booking>,
    repository: BookZzzRepository,
    currentLanguage: String = "Français",
    isAppOnline: Boolean = true
) {
    val titleText = when (currentLanguage) {
        "English" -> "My Reservations History"
        "Swahili" -> "Historia ya Uhifadhi"
        else -> "Historique des Réservations"
    }
    val noBookingsText = when (currentLanguage) {
        "English" -> "No bookings found for this selection."
        "Swahili" -> "Hakuna uhifadhi uliopatikana kwa chaguo hili."
        else -> "Aucune réservation trouvée pour cette sélection."
    }
    val offlineBannerText = when (currentLanguage) {
        "English" -> "🟠 Offline Mode Active — Local bookings consultation is live"
        "Swahili" -> "🟠 Nje ya Mtandao — Uhifadhi uliyohifadhiwa unapatikana"
        else -> "🟠 Mode hors-ligne actif — Consultation des réservations locales active"
    }

    val tabAllLabel = when (currentLanguage) {
        "English" -> "All"
        "Swahili" -> "Zote"
        else -> "Toutes"
    }
    val tabUpcomingLabel = when (currentLanguage) {
        "English" -> "Upcoming"
        "Swahili" -> "Zijazo"
        else -> "À venir"
    }
    val tabPastLabel = when (currentLanguage) {
        "English" -> "Past"
        "Swahili" -> "Zilizopita"
        else -> "Passées"
    }

    var selectedFilter by remember { mutableStateOf("All") } // "All", "Upcoming", "Past"

    val filteredBookings = when (selectedFilter) {
        "Upcoming" -> bookings.filter { isFutureBooking(it.arrivalDate) }
        "Past" -> bookings.filter { !isFutureBooking(it.arrivalDate) }
        else -> bookings
    }

    val allCount = bookings.size
    val upcomingCount = bookings.count { isFutureBooking(it.arrivalDate) }
    val pastCount = bookings.count { !isFutureBooking(it.arrivalDate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Class, contentDescription = null, tint = Color(0xFF1F3A5F))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = titleText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1F3A5F)
                )
            }
            if (!isAppOnline) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5B041).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Offline",
                        color = Color(0xFFD35400),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (!isAppOnline) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF2E9)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF5CBA7))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline Mode",
                        tint = Color(0xFFD35400),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = offlineBannerText,
                        fontSize = 12.sp,
                        color = Color(0xFF7E5109),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Premium Navigation/Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filterOptions = listOf(
                Triple("All", tabAllLabel, allCount),
                Triple("Upcoming", tabUpcomingLabel, upcomingCount),
                Triple("Past", tabPastLabel, pastCount)
            )

            filterOptions.forEach { (filterType, label, count) ->
                val isSelected = selectedFilter == filterType
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFF1F3A5F) else Color.White)
                        .clickable { selectedFilter = filterType }
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF1F3A5F) else Color(0xFFE5EFF9),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF7F8C8D),
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF5DADE2) else Color(0xFFEBF5FB))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF1F3A5F)
                            )
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        if (filteredBookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CardTravel, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(noBookingsText, color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredBookings.reversed()) { booking ->
                    BookingItemCard(
                        booking = booking,
                        repository = repository,
                        currentLanguage = currentLanguage,
                        isAppOnline = isAppOnline
                    )
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(
    booking: Booking,
    repository: BookZzzRepository,
    currentLanguage: String = "Français",
    isAppOnline: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showReceiptDetail by remember { mutableStateOf(false) }

    val categoryLabel = when (currentLanguage) {
        "English" -> "Category"
        "Swahili" -> "Aina"
        else -> "Catégorie"
    }
    val séjourLabel = when (currentLanguage) {
        "English" -> "Stay: from"
        "Swahili" -> "Kukaa: kuanzia"
        else -> "Séjour : du"
    }
    val auLabel = when (currentLanguage) {
        "English" -> "to"
        "Swahili" -> "hadi"
        else -> "au"
    }
    val nuitsLabel = when (currentLanguage) {
        "English" -> "nights"
        "Swahili" -> "usiku"
        else -> "nuits"
    }
    val montantLabel = when (currentLanguage) {
        "English" -> "Amount"
        "Swahili" -> "Kiasi"
        else -> "Montant"
    }
    val refLabel = when (currentLanguage) {
        "English" -> "Reference"
        "Swahili" -> "Marejeleo"
        else -> "Référence"
    }
    val taxiServiceLabel = when (currentLanguage) {
        "English" -> "Taxi Service"
        "Swahili" -> "Huduma ya Teksi"
        else -> "Service Taxi"
    }
    val taxiStatusNotReq = when (currentLanguage) {
        "English" -> "Not Requested"
        "Swahili" -> "Haijaombwa"
        else -> "Non Sollicité"
    }
    val buttonRequestTaxi = when (currentLanguage) {
        "English" -> "Request Taxi"
        "Swahili" -> "Omba Teksi"
        else -> "Demander Taxi"
    }
    val buttonViewTicket = when (currentLanguage) {
        "English" -> "View Ticket (Offline)"
        "Swahili" -> "Angalia Risiti (Nje ya mtandao)"
        else -> "Reçu détaillé (Hors-ligne)"
    }
    val savedLocallyText = when (currentLanguage) {
        "English" -> "Saved Offline"
        "Swahili" -> "Inehifadhiwa chini"
        else -> "Sécurisé hors-ligne"
    }

    val isFuture = isFutureBooking(booking.arrivalDate)
    val stayBadge = getStayBadgeInfo(booking.arrivalDate, currentLanguage)

    val cardBorderColor = if (isFuture) {
        Color(0xFF3498DB).copy(alpha = 0.6f)
    } else {
        Color.LightGray.copy(alpha = 0.4f)
    }

    val cardBgColor = if (isFuture) {
        Color.White
    } else {
        Color(0xFFF8F9F9)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showReceiptDetail = true },
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.hotelName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F3A5F),
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Colored State Tags
                    val (colorCode, textColor) = when (booking.status) {
                        "En Attente" -> Color(0xFFF1C40F) to Color(0xFF7E5109)
                        "Validé" -> Color(0xFF2ECC71) to Color(0xFF145A32)
                        else -> Color(0xFFE74C3C) to Color(0xFF78281F)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorCode.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when (booking.status) {
                                "En Attente" -> {
                                    when (currentLanguage) {
                                        "English" -> "Pending"
                                        "Swahili" -> "Subiri"
                                        else -> "En Attente"
                                    }
                                }
                                "Validé" -> {
                                    when (currentLanguage) {
                                        "English" -> "Validated"
                                        "Swahili" -> "Imethibitishwa"
                                        else -> "Validé"
                                    }
                                }
                                else -> booking.status
                            },
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (stayBadge.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFuture) Color(0xFF3498DB).copy(alpha = 0.15f) else Color(0xFFBDC3C7).copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isFuture) Icons.Default.Event else Icons.Default.History,
                            contentDescription = null,
                            tint = if (isFuture) Color(0xFF2980B9) else Color(0xFF7F8C8D),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = stayBadge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFuture) Color(0xFF2980B9) else Color(0xFF7F8C8D)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = "$categoryLabel : ${booking.roomType}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2980B9)
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text("$séjourLabel ${booking.arrivalDate} $auLabel ${booking.departureDate} (${booking.numNights} $nuitsLabel)", fontSize = 12.sp, color = Color.Gray)
            Text("$montantLabel: ${booking.totalAmount.roundToInt()} USD - $refLabel: ${booking.transactionId} (${booking.operatorSelected})", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Persistent offline saved indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Saved offline",
                        tint = Color(0xFF2ECC71),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = savedLocallyText,
                        fontSize = 11.sp,
                        color = Color(0xFF27AE60),
                        fontWeight = FontWeight.Medium
                    )
                }

                TextButton(
                    onClick = { showReceiptDetail = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF5DADE2))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(buttonViewTicket, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5DADE2))
                }
            }

            if (booking.status == "Validé") {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(taxiServiceLabel, fontSize = 10.sp, color = Color.Gray)
                        Text(
                            text = booking.taxiStatus ?: taxiStatusNotReq,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (booking.taxiRequested) Color(0xFF2E7D32) else Color.DarkGray
                        )
                    }

                    if (!booking.taxiRequested && isAppOnline) {
                        Button(
                            onClick = {
                                repository.requestTaxi(booking.id)
                                FirebaseClient.updateBookingStatusInFirestore(booking.id, "Validé - Taxi Sollicité")
                                val successTaxiMsg = when (currentLanguage) {
                                    "English" -> "Taxi requested! Coordinating local driver..."
                                    "Swahili" -> "Teksi imeombwa! Tunaratibu dereva wa ndani..."
                                    else -> "Soutien Taxi Réclamé ! Liaison locale en cours..."
                                }
                                Toast.makeText(context, successTaxiMsg, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5DADE2), contentColor = Color(0xFF1F3A5F)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LocalTaxi, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(buttonRequestTaxi, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (booking.taxiRequested && booking.taxiStatus == "Coordination d'un taxi local en cours...") {
                        // Simulate dispatching response
                        LaunchedEffect(Unit) {
                            delay(4000)
                            repository.updateTaxiStatus(booking.id, "Confirmé (Chauffeur Christian Aganze - Toyota Jaune)")
                        }
                    }
                }
            }
        }
    }

    // Receipt Detail Dialog - Consult your booking in deep Offline Mode!
    if (showReceiptDetail) {
        ReceiptDialog(
            booking = booking,
            currentLanguage = currentLanguage,
            isAppOnline = isAppOnline,
            onDismiss = { showReceiptDetail = false }
        )
    }
}

@Composable
fun ReceiptDialog(
    booking: Booking,
    currentLanguage: String,
    isAppOnline: Boolean,
    onDismiss: () -> Unit
) {
    val dialogTitle = when (currentLanguage) {
        "English" -> "Official Receipt"
        "Swahili" -> "Stakabadhi Rasmi"
        else -> "Reçu Officiel de Réservation"
    }
    val codeLabel = when (currentLanguage) {
        "English" -> "Booking ID"
        "Swahili" -> "Nambari ya Uhifadhi"
        else -> "Code de Réservation"
    }
    val guestLabel = when (currentLanguage) {
        "English" -> "Guest Profile"
        "Swahili" -> "Profaili ya Wageni"
        else -> "Bénéficiaire"
    }
    val arrivalLabel = when (currentLanguage) {
        "English" -> "Arrival Date"
        "Swahili" -> "Tarehe ya Kufika"
        else -> "Date d'Arrivée"
    }
    val departureLabel = when (currentLanguage) {
        "English" -> "Departure Date"
        "Swahili" -> "Tarehe ya Kuondoka"
        else -> "Date de Départ"
    }
    val totalLabel = when (currentLanguage) {
        "English" -> "Total Paid"
        "Swahili" -> "Jumla ya Malipo"
        else -> "Total Payé"
    }
    val mMoneyRefLabel = when (currentLanguage) {
        "English" -> "Mobile Money Code"
        "Swahili" -> "Nambari ya Malipo"
        else -> "Code Mobile Money"
    }
    val validationLabel = when (currentLanguage) {
        "English" -> "Validation Status"
        "Swahili" -> "Hali ya Uthibitisho"
        else -> "Statut d'approbation"
    }
    val taxiLabel = when (currentLanguage) {
        "English" -> "Taxi Assignment"
        "Swahili" -> "Ugawaji wa Teksi"
        else -> "Liaison Taxi"
    }
    val verifiedOfflineNotice = when (currentLanguage) {
        "English" -> "🔒 Secured Offline Receipt — Verified locally via data-integrity code."
        "Swahili" -> "🔒 Stakabadhi salama ya nje ya mtandao — Imethibitishwa ndani."
        else -> "🔒 Reçu hors-ligne sécurisé — Validé localement via intégrité des données."
    }
    val presentationNotice = when (currentLanguage) {
        "English" -> "Present this receipt at the hotel reception upon check-in. Work completely offline without any internet connection."
        "Swahili" -> "Onyesha risiti hii kwenye mapokezi ya hoteli ya kukagua. Inafanya kazi kabisa nje ya mtandao."
        else -> "Présentez ce reçu à l'accueil de l'établissement lors de votre arrivée. Fonctionne entièrement hors-ligne sans connexion."
    }
    val closeButtonText = when (currentLanguage) {
        "English" -> "Close"
        "Swahili" -> "Funga"
        else -> "Fermer"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = Color(0xFF1F3A5F),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dialogTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1F3A5F)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // QR bar visual outline representation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F4F4), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Custom ticket decoration outline
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(24) { index ->
                                val heightVal = if (index % 3 == 0) 40.dp else if (index % 2 == 0) 25.dp else 15.dp
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(heightVal)
                                        .background(Color(0xFF1F3A5F))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "*BZZZ-${booking.id.uppercase()}*",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Structured details cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Hotel Name
                    Text(
                        text = booking.hotelName,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color(0xFF2E86C1)
                    )

                    HorizontalDivider(color = Color(0xFFEBF5FB))

                    // Booking ID code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = codeLabel, color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = booking.id,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1F3A5F)
                        )
                    }

                    // Room Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Type", color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = booking.roomType,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1F3A5F)
                        )
                    }

                    // Dates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = arrivalLabel, color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = booking.arrivalDate,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF1F3A5F)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = departureLabel, color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = booking.departureDate,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFF1F3A5F)
                        )
                    }

                    // Total amounts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = totalLabel, color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = "${booking.totalAmount.roundToInt()} USD",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF27AE60)
                        )
                    }

                    // Mobile Money Payment specifics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = mMoneyRefLabel, color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = "${booking.transactionId} (${booking.operatorSelected})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1F3A5F)
                        )
                    }

                    // Validation profile
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = validationLabel, color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = when (booking.status) {
                                "En Attente" -> {
                                    when (currentLanguage) {
                                        "English" -> "Pending"
                                        "Swahili" -> "Chini ya tathmini"
                                        else -> "En Attente"
                                    }
                                }
                                "Validé" -> {
                                    when (currentLanguage) {
                                        "English" -> "Approved & Confirmed"
                                        "Swahili" -> "Imethibitishwa kabisa"
                                        else -> "Validé (Approuvé)"
                                    }
                                }
                                else -> booking.status
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (booking.status == "Validé") Color(0xFF27AE60) else Color(0xFFD35400)
                        )
                    }

                    // Taxi liaison details (offline)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = taxiLabel, color = Color.Gray, fontSize = 12.sp)
                        Text(
                            text = booking.taxiStatus ?: "Non planifié",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1F3A5F),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Security validation box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F8F5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = verifiedOfflineNotice,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF117864)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = presentationNotice,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = closeButtonText, color = Color(0xFF5DADE2), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

// --- 3.c AI Grounded Travel Agent and Hotel Search Screen ---
@Composable
fun TravelAgentChatScreen() {
    var query by remember { mutableStateOf("") }
    var chatResponse by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat screen Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F3A5F)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFF5DADE2), modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Recherche & Assistance IA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Recherches géolocalisées en RDC par Gemini 3.5", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Query input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Rechercher hôtel, quartier calme à Goma...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_query_input"),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF5DADE2),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Button(
                onClick = {
                    if (query.isBlank()) return@Button
                    isSearching = true
                    chatResponse = null
                    coroutineScope.launch {
                        val response = GeminiClient.groundedSearch(query)
                        chatResponse = response
                        isSearching = false
                    }
                },
                modifier = Modifier
                    .height(54.dp)
                    .width(74.dp)
                    .testTag("ai_send_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5DADE2), contentColor = Color(0xFF1F3A5F)),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (isSearching) {
                    CircularProgressIndicator(color = Color(0xFF1F3A5F), modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat Result output
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (chatResponse == null && !isSearching) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Exemples de demandes:\n" +
                                    "• \"Quels sont les hôtels populaires à Kinshasa vers la Gombe ?\"\n" +
                                    "• \"Trouve-moi un hébergement paisible et sécurisé près du lac Kivu à Bukavu.\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    Text(
                        text = chatResponse ?: "",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF1F3A5F)
                    )
                }
            }
        }
    }
}

// --- 4. Hotel Admin Dashboard ---
@Composable
fun HotelAdminDashboard(
    repository: BookZzzRepository, // Update this signature to have access to rooms
    hotels: List<Hotel>,
    bookings: List<Booking>,
    rooms: List<Room>, // Add this
    currentUser: UserProfile?,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val hotelManagedName = currentUser?.registeredHotelName ?: "Goma Serena Hotel"
    val localBookings = bookings.filter { it.hotelName.contains(hotelManagedName, ignoreCase = true) }
    val localRooms = rooms.filter { hotel -> hotels.find { it.id == hotel.hotelId }?.name?.contains(hotelManagedName, ignoreCase = true) == true }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Console Gérant", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF1F3A5F))
                Text(text = "Hôtel : $hotelManagedName", fontSize = 12.sp, color = Color.DarkGray)
            }
            IconButton(onClick = onLogout, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFADBD8))) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color.Red)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))
        
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Réservations") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Gestion Chambres") })
        }

        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                items(localBookings.reversed()) { booking ->
                    AdminBookingVerifyCard(booking = booking, repository = repository)
                }
            }
        } else {
            LazyColumn {
                items(localRooms) { room ->
                    RoomItem(room = room, onUpdate = { repository.updateRoom(it) })
                }
            }
        }
    }
}

@Composable
fun RoomItem(room: Room, onUpdate: (Room) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(room.type, fontWeight = FontWeight.Bold)
                Text("${room.pricePerNight.roundToInt()} USD")
            }
            Switch(checked = room.isAvailable, onCheckedChange = { onUpdate(room.copy(isAvailable = it)) })
        }
    }
}

@Composable
fun AdminBookingVerifyCard(booking: Booking, repository: BookZzzRepository) {
    var showImage by remember { mutableStateOf(false) }

    if (showImage) {
        Dialog(onDismissRequest = { showImage = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Preuve de paiement", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.size(200.dp).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Text(booking.screenshotName, fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp))
                    }
                    TextButton(onClick = { showImage = false }) { Text("Fermer") }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = booking.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F3A5F))
                    Text(text = "Réf: ${booking.id}", fontSize = 11.sp, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (booking.status == "Validé") Color(0xFF27AE60).copy(alpha = 0.1f) else Color(0xFFD35400).copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = booking.status,
                        color = if (booking.status == "Validé") Color(0xFF27AE60) else Color(0xFFD35400),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Type: ${booking.roomType}", fontSize = 13.sp)
            Text("Dates: ${booking.arrivalDate} -> ${booking.departureDate} (${booking.numNights} nuits)", fontSize = 13.sp)
            Text("Transaction: ${booking.transactionId} (${booking.operatorSelected})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(12.dp))

            if (booking.screenshotName.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showImage = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vérifier la preuve (Screenshot)")
                }
            }

            if (booking.status == "En Attente") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { repository.updateBookingStatus(booking.id, "Validé") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Valider la réservation")
                }
            }
        }
    }
}

// --- 5. Super Admin Dashboard ---
@Composable
fun SuperAdminDashboard(
    repository: BookZzzRepository,
    hotels: List<Hotel>,
    bookings: List<Booking>,
    onLogout: () -> Unit
) {
    val totalRevenue = bookings.filter { it.status == "Validé" }.sumOf { it.totalAmount }
    val totalHotels = hotels.size
    val activeHotels = hotels.count { !it.isSuspended }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Super Administrateur", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF1F3A5F))
                Text(text = "Contrôle Global Réseau BookZZZ", fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onLogout, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFADBD8))) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF5FB))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Revenu", fontSize = 11.sp)
                    Text("${totalRevenue.roundToInt()} USD", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFFE9F7EF))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Hôtels", fontSize = 11.sp)
                    Text("$activeHotels / $totalHotels", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Gestion des Établissements", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(hotels) { hotel ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(hotel.name, fontWeight = FontWeight.Bold)
                            Text(hotel.city, fontSize = 12.sp, color = Color.Gray)
                        }
                        
                        Button(
                            onClick = { repository.toggleHotelSuspension(hotel.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hotel.isSuspended) Color(0xFF27AE60) else Color(0xFFE74C3C)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (hotel.isSuspended) "Activer" else "Suspendre", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
