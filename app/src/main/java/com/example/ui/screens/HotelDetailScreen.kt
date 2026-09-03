package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.BookZzzRepository
import com.example.data.Hotel
import com.example.data.Room
import com.example.data.SampleData
import kotlin.math.roundToInt

/**
 * Helper to launch map intent for the selected hotel
 */
fun openHotelLocationInMaps(context: Context, hotel: Hotel) {
    val address = when (hotel.city.lowercase()) {
        "goma" -> "${hotel.name}, Boulevard Kanyamuhanga, Quartier Les Volcans, Goma, RDC"
        "kinshasa" -> "${hotel.name}, Boulevard du 30 Juin, Commune de la Gombe, Kinshasa, RDC"
        "bukavu" -> "${hotel.name}, Avenue Maniema, Rive du Lac Kivu, Bukavu, RDC"
        else -> "${hotel.name}, ${hotel.city}, RDC"
    }
    
    val geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address))
        val fallbackIntent = Intent(Intent.ACTION_VIEW, webUri)
        try {
            context.startActivity(fallbackIntent)
        } catch (err: Exception) {
            Toast.makeText(context, "Impossible d'ouvrir l'application de cartographie", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Screen 1: Hotel Details & Available Rooms List.
 * Follows Google Developer Expert (GDE) Android architecture standards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    hotel: Hotel,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    repository: BookZzzRepository,
    currentLanguage: String = "Français",
    isWideScreen: Boolean = false,
    onBackClick: () -> Unit,
    onBookRoom: (hotel: Hotel, room: Room) -> Unit
) {
    val context = LocalContext.current
    val rooms = remember(hotel.id) { SampleData.createRoomsForHotel(hotel.id, hotel.basePricePerNight) }
    var showLocationSheet by remember { mutableStateOf(false) }

    val hotelImageUrl = remember(hotel.id) {
        when (hotel.id) {
            "H1" -> "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"
            "H2" -> "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"
            "H3" -> "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"
            else -> "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80"
        }
    }

    val labelRoomsAvailable = when (currentLanguage) {
        "English" -> "Available Rooms & Suites"
        "Swahili" -> "Vyumba na Vyumba vya Kifahari"
        else -> "Chambres & Suites Disponibles"
    }

    var selectedDetailTab by remember { mutableStateOf(0) } // 0: Rooms, 1: Presentation & Amenities, 2: Location

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = hotel.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${hotel.city}, RDC • 3 Chambres disponibles",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("hotel_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { openHotelLocationInMaps(context, hotel) }) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Localiser",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoris",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isWideScreen) {
            // === TABLET / EXPANDED: 2-COLUMN VIEW ===
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // LEFT COLUMN: Immersive Overview & Amenities
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HotelHeroSection(hotel = hotel, imageUrl = hotelImageUrl, onMapClick = { openHotelLocationInMaps(context, hotel) })
                    HotelLocationCard(hotel = hotel, onMapClick = { openHotelLocationInMaps(context, hotel) })
                    HotelOverviewCard(hotel = hotel, currentLanguage = currentLanguage)
                    HotelAmenitiesGrid(currentLanguage = currentLanguage)
                }

                // RIGHT COLUMN: Available Rooms List
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "🛏️ $labelRoomsAvailable (${rooms.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(rooms) { room ->
                            RoomItemCard(
                                room = room,
                                hotel = hotel,
                                currentLanguage = currentLanguage,
                                onBookClick = { onBookRoom(hotel, room) }
                            )
                        }
                    }
                }
            }
        } else {
            // === MOBILE / COMPACT SCREEN: TABBED VIEW FOR INSTANT ROOM VISIBILITY ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Compact Hotel Banner Header
                Card(
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = hotelImageUrl,
                            contentDescription = hotel.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Black.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = hotel.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "📍 ${hotel.city}, RDC • Note ${hotel.rating} ★",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "Dès $${hotel.basePricePerNight.roundToInt()}/nuit",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // PRIMARY NAVIGATION TABS (Chambres / Présentation / Localisation)
                TabRow(
                    selectedTabIndex = selectedDetailTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedDetailTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedDetailTab == 0,
                        onClick = { selectedDetailTab = 0 },
                        text = {
                            Text(
                                text = "🛏️ Chambres (${rooms.size})",
                                fontWeight = if (selectedDetailTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedDetailTab == 1,
                        onClick = { selectedDetailTab = 1 },
                        text = {
                            Text(
                                text = "✨ Services",
                                fontWeight = if (selectedDetailTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedDetailTab == 2,
                        onClick = { selectedDetailTab = 2 },
                        text = {
                            Text(
                                text = "📍 Carte",
                                fontWeight = if (selectedDetailTab == 2) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }

                // TAB CONTENT
                when (selectedDetailTab) {
                    0 -> {
                        // TAB 0: CHAMBRES DISPONIBLES (INSTANTLY VISIBLE)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sélectionnez une chambre ci-dessous pour continuer votre réservation immédiate :",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            items(rooms) { room ->
                                RoomItemCard(
                                    room = room,
                                    hotel = hotel,
                                    currentLanguage = currentLanguage,
                                    onBookClick = { onBookRoom(hotel, room) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }
                    1 -> {
                        // TAB 1: PRÉSENTATION & ÉQUIPEMENTS
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                HotelOverviewCard(hotel = hotel, currentLanguage = currentLanguage)
                            }
                            item {
                                HotelAmenitiesGrid(currentLanguage = currentLanguage)
                            }
                            item {
                                Button(
                                    onClick = { selectedDetailTab = 0 },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Voir les chambres & réserver", fontWeight = FontWeight.Bold)
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }
                    2 -> {
                        // TAB 2: LOCALISATION & CARTE
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                HotelLocationCard(hotel = hotel, onMapClick = { openHotelLocationInMaps(context, hotel) })
                            }
                            item {
                                HotelHeroSection(hotel = hotel, imageUrl = hotelImageUrl, onMapClick = { openHotelLocationInMaps(context, hotel) })
                            }
                            item {
                                Button(
                                    onClick = { selectedDetailTab = 0 },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Hotel, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Voir les chambres de cet hôtel", fontWeight = FontWeight.Bold)
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HotelHeroSection(
    hotel: Hotel,
    imageUrl: String,
    onMapClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = hotel.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Scrim gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onMapClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(hotel.city, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1C40F)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF1F3A5F), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("4.8 (128 avis)", color = Color(0xFF1F3A5F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bottom Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = hotel.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "À partir de $${hotel.basePricePerNight.roundToInt()} USD / nuit",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HotelLocationCard(
    hotel: Hotel,
    onMapClick: () -> Unit
) {
    val fullAddress = when (hotel.city.lowercase()) {
        "goma" -> "Boulevard Kanyamuhanga, Quartier Les Volcans, Goma, Nord-Kivu"
        "kinshasa" -> "Boulevard du 30 Juin, Commune de la Gombe, Kinshasa"
        "bukavu" -> "Avenue Maniema, Rive du Lac Kivu, Bukavu, Sud-Kivu"
        else -> "${hotel.city}, République Démocratique du Congo"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Localisation de l'hôtel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = fullAddress,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
                onClick = onMapClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Carte", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HotelOverviewCard(hotel: Hotel, currentLanguage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = when (currentLanguage) {
                    "English" -> "About this Hotel"
                    "Swahili" -> "Kuhusu Hoteli Hii"
                    else -> "Présentation de l'établissement"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = hotel.description,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Contact direct : ${hotel.phonePaymentNumber} (${hotel.operatorName})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HotelAmenitiesGrid(currentLanguage: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = when (currentLanguage) {
                    "English" -> "Services & Amenities"
                    "Swahili" -> "Huduma na Vistawishi"
                    else -> "Services & Équipements Inclus"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            val amenities = listOf(
                Pair(Icons.Default.Wifi, "Wi-Fi 5G Illimité"),
                Pair(Icons.Default.Pool, "Piscine Extérieure"),
                Pair(Icons.Default.Restaurant, "Restaurant & Bar"),
                Pair(Icons.Default.AcUnit, "Climatisation"),
                Pair(Icons.Default.LocalParking, "Parking Sécurisé 24/7"),
                Pair(Icons.Default.AirportShuttle, "Navette Aéroport"),
                Pair(Icons.Default.FitnessCenter, "Salle de Sport"),
                Pair(Icons.Default.RoomService, "Service d'Étage 24h/24")
            )

            val rows = amenities.chunked(2)
            rows.forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { item ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(item.first, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.second,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Room Item with direct booking button
 */
@Composable
fun RoomItemCard(
    room: Room,
    hotel: Hotel,
    currentLanguage: String,
    onBookClick: () -> Unit
) {
    val isStandard = room.type.contains("Standard", ignoreCase = true)
    val isSuiteDeluxe = room.type.contains("Deluxe", ignoreCase = true)
    val capacity = when {
        isStandard -> "Max 2 Personnes • Lit Double Confort"
        isSuiteDeluxe -> "Max 3 Personnes • Lit King Size"
        else -> "Max 4 Personnes • Suite Exécutive & Luxe"
    }

    val roomAmenities = when {
        isStandard -> listOf("Wi-Fi Gratuit", "Climatisation", "Douche Italienne")
        isSuiteDeluxe -> listOf("Wi-Fi Rapide", "Lit King Size", "Smart TV", "Minibar")
        else -> listOf("Lit Impérial", "Salon Privé", "Jacuzzi", "Vue Panoramique")
    }

    val roomImageUrl = when {
        isStandard -> "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=600&q=80"
        isSuiteDeluxe -> "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=600&q=80"
        else -> "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=600&q=80"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("room_item_${room.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Room Image Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = roomImageUrl,
                    contentDescription = room.type,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Price chip overlay
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "${room.pricePerNight.roundToInt()} USD / nuit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Room content & action
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = room.type,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "👥 $capacity",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Amenities chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    roomAmenities.forEach { amenity ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = amenity,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("book_room_button_${room.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "English" -> "Book This Room"
                            "Swahili" -> "Hifadhi Chumba Hiki"
                            else -> "Réserver cette chambre"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
