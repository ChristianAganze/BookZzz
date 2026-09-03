package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.BookZzzRepository
import com.example.data.Booking
import com.example.data.Hotel
import com.example.data.Room
import com.example.data.SampleData
import com.example.network.FirebaseClient
import com.example.network.GeminiClient
import com.example.ui.components.ReceiptGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Full-screen Hotel Details & Room Selection Screen.
 * Adapts responsively for both Mobile (compact single-column) and Tablet (wide two-column).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailBookingScreen(
    hotel: Hotel,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    repository: BookZzzRepository,
    currentLanguage: String = "Français",
    isWideScreen: Boolean = false,
    onBackClick: () -> Unit,
    onBookingSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Rooms dynamically created for the hotel
    val rooms = remember(hotel.id) { SampleData.createRoomsForHotel(hotel.id, hotel.basePricePerNight) }
    var selectedRoom by remember { mutableStateOf<Room?>(rooms.firstOrNull()) }

    var arrivalDateStr by remember { mutableStateOf("16/06/2026") }
    var departureDateStr by remember { mutableStateOf("18/06/2026") }

    var numNights by remember { mutableIntStateOf(2) }
    var totalAmount by remember { mutableStateOf(2 * (rooms.firstOrNull()?.pricePerNight ?: hotel.basePricePerNight)) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var parseError by remember { mutableStateOf<String?>(null) }

    fun recalculateStay() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val arrDate = format.parse(arrivalDateStr)
            val depDate = format.parse(departureDateStr)

            if (arrDate != null && depDate != null) {
                if (depDate.before(arrDate) || depDate.equals(arrDate)) {
                    parseError = when (currentLanguage) {
                        "English" -> "Departure date must be after arrival date."
                        "Swahili" -> "Tarehe ya kuondoka lazima iwe baada ya tarehe ya kuwasili."
                        else -> "La date de départ doit être postérieure à la date d'arrivée."
                    }
                } else {
                    val diffMs = depDate.time - arrDate.time
                    val nights = (diffMs / (1000 * 60 * 60 * 24)).toInt()
                    numNights = nights.coerceAtLeast(1)
                    val roomPrice = selectedRoom?.pricePerNight ?: hotel.basePricePerNight
                    totalAmount = numNights * roomPrice
                    parseError = null
                }
            }
        } catch (e: Exception) {
            parseError = when (currentLanguage) {
                "English" -> "Invalid format (Example: DD/MM/YYYY)"
                "Swahili" -> "Muundo si sahihi (Mfano: SS/MM/YYYY)"
                else -> "Format invalide (Exemple requis: JJ/MM/AAAA)"
            }
        }
    }

    // Payment state
    var selectedOperator by remember { mutableStateOf(hotel.operatorName) }
    var inputTxId by remember { mutableStateOf("") }
    var uploadedScreenshot by remember { mutableStateOf<Bitmap?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    val hotelImageUrl = remember(hotel.id) {
        when (hotel.id) {
            "H1" -> "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80"
            "H2" -> "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80"
            "H3" -> "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=1200&q=80"
            else -> "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=1200&q=80"
        }
    }

    val labelRooms = when (currentLanguage) {
        "English" -> "Available Room Categories"
        "Swahili" -> "Vyumba Vinavyopatikana"
        else -> "Catégories de Chambres Disponibles"
    }

    val labelSummary = when (currentLanguage) {
        "English" -> "Booking Summary"
        "Swahili" -> "Muhtasari wa Uhifadhi"
        else -> "Récapitulatif du Séjour"
    }

    val labelConfirm = when (currentLanguage) {
        "English" -> "Confirm Booking"
        "Swahili" -> "Thibitisha Uhifadhi"
        else -> "Confirmer la Réservation"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = hotel.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = hotel.city,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
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
            // === TABLET / EXPANDED LAYOUT: 2-COLUMN RESPONSIVE VIEW ===
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // LEFT PANE: Hotel Presentation, Amenities, Room Selection
                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HotelHeroBanner(hotel = hotel, imageUrl = hotelImageUrl)

                    HotelOverviewSection(hotel = hotel, currentLanguage = currentLanguage)

                    HotelAmenitiesSection(currentLanguage = currentLanguage)

                    Text(
                        text = labelRooms,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    rooms.forEach { room ->
                        RoomSelectionCard(
                            room = room,
                            isSelected = selectedRoom?.id == room.id,
                            currentLanguage = currentLanguage,
                            onSelect = {
                                selectedRoom = room
                                val roomPrice = room.pricePerNight
                                totalAmount = numNights * roomPrice
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // RIGHT PANE: Sticky / Dedicated Booking, Stay Calculation & Payment Panel
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = labelSummary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Selected Room chip
                        selectedRoom?.let { room ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = room.type,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = "${room.pricePerNight.roundToInt()} USD / nuit",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        // Dates Input & Presets
                        DatesSelectionSection(
                            arrivalDate = arrivalDateStr,
                            departureDate = departureDateStr,
                            onArrivalChange = {
                                arrivalDateStr = it
                                recalculateStay()
                            },
                            onDepartureChange = {
                                departureDateStr = it
                                recalculateStay()
                            },
                            onPresetDays = { days ->
                                try {
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    val arr = sdf.parse(arrivalDateStr) ?: Date()
                                    val cal = Calendar.getInstance().apply {
                                        time = arr
                                        add(Calendar.DAY_OF_MONTH, days)
                                    }
                                    departureDateStr = sdf.format(cal.time)
                                    recalculateStay()
                                } catch (e: Exception) {}
                            },
                            parseError = parseError,
                            numNights = numNights,
                            totalAmount = totalAmount,
                            currentLanguage = currentLanguage
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        // Mobile Money Payment Section
                        PaymentSection(
                            hotel = hotel,
                            totalAmount = totalAmount,
                            selectedOperator = selectedOperator,
                            onOperatorChange = { selectedOperator = it },
                            inputTxId = inputTxId,
                            onTxIdChange = { inputTxId = it },
                            uploadedScreenshot = uploadedScreenshot,
                            onGenerateScreenshot = {
                                val paintedReceipt = ReceiptGenerator.generateMobileMoneyReceipt(
                                    operator = selectedOperator,
                                    amountUsd = totalAmount,
                                    hotelName = hotel.name,
                                    phonePaidTo = hotel.phonePaymentNumber
                                )
                                uploadedScreenshot = paintedReceipt
                                Toast.makeText(context, "Preuve générée avec succès !", Toast.LENGTH_SHORT).show()
                            },
                            isScanning = isScanning,
                            onScanAI = {
                                if (uploadedScreenshot == null) {
                                    Toast.makeText(context, "Veuillez d'abord générer la preuve !", Toast.LENGTH_SHORT).show()
                                    return@PaymentSection
                                }
                                isScanning = true
                                scope.launch {
                                    val scanResult = GeminiClient.scanReceipt(uploadedScreenshot!!)
                                    isScanning = false
                                    if (scanResult.errorMessage != null) {
                                        Toast.makeText(context, scanResult.errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                    inputTxId = scanResult.transactionId
                                    selectedOperator = scanResult.operator
                                }
                            },
                            currentLanguage = currentLanguage
                        )

                        Button(
                            onClick = {
                                if (parseError != null) {
                                    Toast.makeText(context, parseError, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (inputTxId.isBlank()) {
                                    Toast.makeText(context, "Saisissez ou scannez le code de transaction !", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                showConfirmationDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_booking_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(labelConfirm, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        } else {
            // === MOBILE / COMPACT SCREEN: SEAMLESS FULL SCREEN FLOW ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HotelHeroBanner(hotel = hotel, imageUrl = hotelImageUrl)

                HotelOverviewSection(hotel = hotel, currentLanguage = currentLanguage)

                HotelAmenitiesSection(currentLanguage = currentLanguage)

                Text(
                    text = labelRooms,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                rooms.forEach { room ->
                    RoomSelectionCard(
                        room = room,
                        isSelected = selectedRoom?.id == room.id,
                        currentLanguage = currentLanguage,
                        onSelect = {
                            selectedRoom = room
                            val roomPrice = room.pricePerNight
                            totalAmount = numNights * roomPrice
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = labelSummary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                DatesSelectionSection(
                    arrivalDate = arrivalDateStr,
                    departureDate = departureDateStr,
                    onArrivalChange = {
                        arrivalDateStr = it
                        recalculateStay()
                    },
                    onDepartureChange = {
                        departureDateStr = it
                        recalculateStay()
                    },
                    onPresetDays = { days ->
                        try {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val arr = sdf.parse(arrivalDateStr) ?: Date()
                            val cal = Calendar.getInstance().apply {
                                time = arr
                                add(Calendar.DAY_OF_MONTH, days)
                            }
                            departureDateStr = sdf.format(cal.time)
                            recalculateStay()
                        } catch (e: Exception) {}
                    },
                    parseError = parseError,
                    numNights = numNights,
                    totalAmount = totalAmount,
                    currentLanguage = currentLanguage
                )

                PaymentSection(
                    hotel = hotel,
                    totalAmount = totalAmount,
                    selectedOperator = selectedOperator,
                    onOperatorChange = { selectedOperator = it },
                    inputTxId = inputTxId,
                    onTxIdChange = { inputTxId = it },
                    uploadedScreenshot = uploadedScreenshot,
                    onGenerateScreenshot = {
                        val paintedReceipt = ReceiptGenerator.generateMobileMoneyReceipt(
                            operator = selectedOperator,
                            amountUsd = totalAmount,
                            hotelName = hotel.name,
                            phonePaidTo = hotel.phonePaymentNumber
                        )
                        uploadedScreenshot = paintedReceipt
                        Toast.makeText(context, "Preuve générée avec succès !", Toast.LENGTH_SHORT).show()
                    },
                    isScanning = isScanning,
                    onScanAI = {
                        if (uploadedScreenshot == null) {
                            Toast.makeText(context, "Veuillez d'abord générer la preuve !", Toast.LENGTH_SHORT).show()
                            return@PaymentSection
                        }
                        isScanning = true
                        scope.launch {
                            val scanResult = GeminiClient.scanReceipt(uploadedScreenshot!!)
                            isScanning = false
                            if (scanResult.errorMessage != null) {
                                Toast.makeText(context, scanResult.errorMessage, Toast.LENGTH_LONG).show()
                            }
                            inputTxId = scanResult.transactionId
                            selectedOperator = scanResult.operator
                        }
                    },
                    currentLanguage = currentLanguage
                )

                Button(
                    onClick = {
                        if (parseError != null) {
                            Toast.makeText(context, parseError, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (inputTxId.isBlank()) {
                            Toast.makeText(context, "Saisissez ou scannez le code de transaction !", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        showConfirmationDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_booking_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(labelConfirm, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Confirmation Alert Dialog before saving
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = when (currentLanguage) {
                        "English" -> "Confirm Your Booking"
                        "Swahili" -> "Thibitisha Uhifadhi Wako"
                        else -> "Confirmer votre réservation"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hôtel: ${hotel.name}", color = MaterialTheme.colorScheme.onSurface)
                    Text("Chambre: ${selectedRoom?.type ?: "Standard"}", color = MaterialTheme.colorScheme.onSurface)
                    Text("Dates: $arrivalDateStr au $departureDateStr ($numNights nuits)", color = MaterialTheme.colorScheme.onSurface)
                    Text("Montant Total: ${totalAmount.roundToInt()} USD", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text("ID Transaction: $inputTxId", color = MaterialTheme.colorScheme.onSurface)
                    Text("Opérateur: $selectedOperator", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        val finalBooking = Booking(
                            id = "BK_${System.currentTimeMillis()}",
                            hotelId = hotel.id,
                            hotelName = hotel.name,
                            userId = repository.currentUser.value?.id ?: "U_GUEST",
                            userName = repository.currentUser.value?.name ?: "Christian",
                            arrivalDate = arrivalDateStr,
                            departureDate = departureDateStr,
                            numNights = numNights,
                            pricePerNight = selectedRoom?.pricePerNight ?: hotel.basePricePerNight,
                            totalAmount = totalAmount,
                            status = "En Attente",
                            transactionId = inputTxId,
                            operatorSelected = selectedOperator,
                            paymentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            screenshotName = "capture_${selectedOperator.lowercase()}.png",
                            roomType = selectedRoom?.type ?: "Chambre Standard"
                        )

                        repository.addBooking(finalBooking)
                        FirebaseClient.syncBookingToFirestore(finalBooking)

                        Toast.makeText(context, "Réservation transmise pour validation hôtelière !", Toast.LENGTH_LONG).show()
                        onBookingSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Annuler", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

// Subcomponents for HotelDetailBookingScreen
@Composable
private fun HotelHeroBanner(hotel: Hotel, imageUrl: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = hotel.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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

                Column {
                    Text(
                        text = hotel.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "À partir de $${hotel.basePricePerNight.roundToInt()} USD / nuit",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun HotelOverviewSection(hotel: Hotel, currentLanguage: String) {
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
                    text = "Contact direct & Mobile Money : ${hotel.phonePaymentNumber} (${hotel.operatorName})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HotelAmenitiesSection(currentLanguage: String) {
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

@Composable
private fun RoomSelectionCard(
    room: Room,
    isSelected: Boolean,
    currentLanguage: String,
    onSelect: () -> Unit
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("room_card_${room.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${room.pricePerNight.roundToInt()} USD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Room Amenities chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                roomAmenities.take(3).forEach { amenity ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = amenity,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chambre Sélectionnée", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onSelect,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Choisir cette chambre", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DatesSelectionSection(
    arrivalDate: String,
    departureDate: String,
    onArrivalChange: (String) -> Unit,
    onDepartureChange: (String) -> Unit,
    onPresetDays: (Int) -> Unit,
    parseError: String?,
    numNights: Int,
    totalAmount: Double,
    currentLanguage: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = arrivalDate,
                onValueChange = onArrivalChange,
                label = { Text("Arrivée (JJ/MM/AAAA)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = departureDate,
                onValueChange = onDepartureChange,
                label = { Text("Départ (JJ/MM/AAAA)") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Quick preset buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(1 to "+1 Nuit", 2 to "+2 Nuits", 3 to "+3 Nuits", 7 to "+1 Semaine").forEach { preset ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPresetDays(preset.first) }
                ) {
                    Text(
                        text = preset.second,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        if (parseError != null) {
            Text(
                text = parseError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp
            )
        }

        // Stay Calculation Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Durée du séjour (N = D_D - D_A)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("$numNights nuit${if (numNights > 1) "s" else ""}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Montant Total (MT = N * PU)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${totalAmount.roundToInt()} USD",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "~ ${(totalAmount * 2800).roundToInt()} CDF",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentSection(
    hotel: Hotel,
    totalAmount: Double,
    selectedOperator: String,
    onOperatorChange: (String) -> Unit,
    inputTxId: String,
    onTxIdChange: (String) -> Unit,
    uploadedScreenshot: Bitmap?,
    onGenerateScreenshot: () -> Unit,
    isScanning: Boolean,
    onScanAI: () -> Unit,
    currentLanguage: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = when (currentLanguage) {
                "English" -> "Mobile Money Payment"
                "Swahili" -> "Malipo ya Mobile Money"
                else -> "Paiement Mobile Money RDC"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Envoyez ${totalAmount.roundToInt()} USD au gérant de l'hôtel :\n👉 ${hotel.phonePaymentNumber} (${hotel.name}) via $selectedOperator",
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("M-Pesa", "Airtel Money", "Orange Money").forEach { op ->
                val isChosen = selectedOperator == op
                FilterChip(
                    selected = isChosen,
                    onClick = { onOperatorChange(op) },
                    label = { Text(op, fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Action Buttons for Preuve & AI Scan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onGenerateScreenshot,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Générer Preuve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onScanAI,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF1C40F),
                    contentColor = Color(0xFF1F3A5F)
                ),
                enabled = uploadedScreenshot != null && !isScanning
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFF1F3A5F))
                } else {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scanner via IA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (uploadedScreenshot != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            ) {
                Image(
                    bitmap = uploadedScreenshot.asImageBitmap(),
                    contentDescription = "Simulated receipt screenshot",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }
        }

        OutlinedTextField(
            value = inputTxId,
            onValueChange = onTxIdChange,
            label = { Text("Code de transaction (ID)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tx_id_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}
