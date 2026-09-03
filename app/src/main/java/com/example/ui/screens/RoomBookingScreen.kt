package com.example.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.BookZzzRepository
import com.example.data.Booking
import com.example.data.Hotel
import com.example.data.Room
import com.example.network.FirebaseClient
import com.example.network.GeminiClient
import com.example.ui.components.ReceiptGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Screen 2: Dedicated Room Booking, Duration Calculation & Mobile Money Checkout Screen.
 * Architected according to GDE (Google Developer Expert) standards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomBookingScreen(
    hotel: Hotel,
    room: Room,
    repository: BookZzzRepository,
    currentLanguage: String = "Français",
    isWideScreen: Boolean = false,
    onBackClick: () -> Unit,
    onBookingSuccess: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var arrivalDateStr by remember { mutableStateOf("16/06/2026") }
    var departureDateStr by remember { mutableStateOf("18/06/2026") }

    var numNights by remember { mutableIntStateOf(2) }
    var totalAmount by remember { mutableStateOf(2 * room.pricePerNight) }
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
                    totalAmount = numNights * room.pricePerNight
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
            "H1" -> "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80"
            "H2" -> "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80"
            "H3" -> "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=800&q=80"
            else -> "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80"
        }
    }

    val labelTitle = when (currentLanguage) {
        "English" -> "Complete Booking"
        "Swahili" -> "Kamilisha Uhifadhi"
        else -> "Finaliser la Réservation"
    }

    val labelConfirm = when (currentLanguage) {
        "English" -> "Confirm & Pay ${totalAmount.roundToInt()} USD"
        "Swahili" -> "Thibitisha na Ulipe ${totalAmount.roundToInt()} USD"
        else -> "Confirmer & Payer ${totalAmount.roundToInt()} USD"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = labelTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${hotel.name} • ${room.type}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("booking_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = hotelImageUrl,
                        contentDescription = hotel.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = hotel.city,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = hotel.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${room.type} • $${room.pricePerNight.roundToInt()} USD / nuit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Stay Dates & Dynamic Calculation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = when (currentLanguage) {
                            "English" -> "Stay Dates & Duration"
                            "Swahili" -> "Tarehe na Muda wa Kukaa"
                            else -> "Dates du séjour & Calcul automatique"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = arrivalDateStr,
                            onValueChange = {
                                arrivalDateStr = it
                                recalculateStay()
                            },
                            label = { Text("Arrivée (JJ/MM/AAAA)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = departureDateStr,
                            onValueChange = {
                                departureDateStr = it
                                recalculateStay()
                            },
                            label = { Text("Départ (JJ/MM/AAAA)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    // Quick duration presets
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
                                    .clickable {
                                        try {
                                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                            val arr = sdf.parse(arrivalDateStr) ?: Date()
                                            val cal = Calendar.getInstance().apply {
                                                time = arr
                                                add(Calendar.DAY_OF_MONTH, preset.first)
                                            }
                                            departureDateStr = sdf.format(cal.time)
                                            recalculateStay()
                                        } catch (e: Exception) {}
                                    }
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
                        Text(text = parseError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    }

                    // Dynamic Pricing calculation banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Durée calculée ($numNights nuit${if (numNights > 1) "s" else ""})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$numNights x $${room.pricePerNight.roundToInt()} USD", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Montant Total (USD)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "${totalAmount.roundToInt()} USD",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 19.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "~ ${(totalAmount * 2800).roundToInt()} CDF",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // RDC Mobile Money Payment Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Paiement Mobile Money RDC",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Operator Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("M-Pesa", "Airtel Money", "Orange Money").forEach { op ->
                            val isChosen = selectedOperator == op
                            FilterChip(
                                selected = isChosen,
                                onClick = { selectedOperator = op },
                                label = { Text(op, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Direct payment phone container with copy button
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Numéro officiel du gérant :",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "${hotel.phonePaymentNumber} ($selectedOperator)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(hotel.phonePaymentNumber))
                                Toast.makeText(context, "Numéro copié : ${hotel.phonePaymentNumber}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Proof Generator & AI Scanner Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val paintedReceipt = ReceiptGenerator.generateMobileMoneyReceipt(
                                    operator = selectedOperator,
                                    amountUsd = totalAmount,
                                    hotelName = hotel.name,
                                    phonePaidTo = hotel.phonePaymentNumber
                                )
                                uploadedScreenshot = paintedReceipt
                                Toast.makeText(context, "Preuve générée avec succès !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Générer reçu", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (uploadedScreenshot == null) {
                                    Toast.makeText(context, "Veuillez d'abord générer ou choisir un reçu !", Toast.LENGTH_SHORT).show()
                                    return@Button
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
                                    Toast.makeText(context, "Code extrait : ${scanResult.transactionId}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isScanning,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scanner IA", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Display Receipt Preview if present
                    if (uploadedScreenshot != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = uploadedScreenshot!!.asImageBitmap(),
                                    contentDescription = "Preuve",
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(90.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Reçu de paiement prêt", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Opérateur: $selectedOperator", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Montant: ${totalAmount.roundToInt()} USD", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Transaction ID Input Field
                    OutlinedTextField(
                        value = inputTxId,
                        onValueChange = { inputTxId = it },
                        label = { Text("Code de transaction (ex: MP24389012)") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_id_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Confirm & Finalize Button
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
                    .testTag("submit_final_booking_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(labelConfirm, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Confirmation Alert Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = when (currentLanguage) {
                        "English" -> "Confirm Your Stay"
                        "Swahili" -> "Thibitisha Kukaa Kwako"
                        else -> "Confirmer votre séjour"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hôtel: ${hotel.name}", color = MaterialTheme.colorScheme.onSurface)
                    Text("Chambre: ${room.type}", color = MaterialTheme.colorScheme.onSurface)
                    Text("Dates: $arrivalDateStr au $departureDateStr ($numNights nuits)", color = MaterialTheme.colorScheme.onSurface)
                    Text("Montant Total: ${totalAmount.roundToInt()} USD", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text("Opérateur: $selectedOperator", color = MaterialTheme.colorScheme.onSurface)
                    Text("ID Transaction: $inputTxId", color = MaterialTheme.colorScheme.onSurface)
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
                            pricePerNight = room.pricePerNight,
                            totalAmount = totalAmount,
                            status = "En Attente",
                            transactionId = inputTxId,
                            operatorSelected = selectedOperator,
                            paymentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                            screenshotName = "capture_${selectedOperator.lowercase()}.png",
                            roomType = room.type
                        )

                        repository.addBooking(finalBooking)
                        FirebaseClient.syncBookingToFirestore(finalBooking)

                        Toast.makeText(context, "Réservation pour ${hotel.name} transmise avec succès !", Toast.LENGTH_LONG).show()
                        onBookingSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Valider")
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
