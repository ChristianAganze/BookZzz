package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BookZzzRepository
import com.example.data.Booking
import com.example.data.Hotel
import com.example.data.Room
import com.example.data.UserProfile
import com.example.shared.engine.PricingEngine
import com.example.ui.theme.BrandAzure
import com.example.ui.theme.BrandCaramel
import com.example.ui.theme.BrandEspresso
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGoldLight
import com.example.ui.theme.BrandTaupe
import kotlin.math.roundToInt

/**
 * Sprint 2: Espace d'Administration & Validation des Paiements Mobile Money
 * - Tableau de bord temps réel avec métriques clés ($ et CDF)
 * - Validation fluide des preuves de transaction (M-Pesa, Airtel Money, Orange Money)
 * - Rejet avec motif et notification
 * - Émission de bons de séjour / Vouchers officiels
 */

@Composable
fun HotelAdminDashboardScreen(
    repository: BookZzzRepository,
    hotels: List<Hotel>,
    bookings: List<Booking>,
    rooms: List<Room>,
    currentUser: UserProfile?,
    onLogout: () -> Unit,
    onOpenShowcase: () -> Unit,
    onOpenCatalog: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenFinance: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val hotelManagedName = currentUser?.registeredHotelName ?: "Goma Serena Hotel"
    val localBookings = bookings.filter {
        it.hotelName.contains(hotelManagedName, ignoreCase = true) ||
                hotelManagedName.contains(it.hotelName, ignoreCase = true) ||
                hotelManagedName.isBlank()
    }
    val currentHotel = hotels.find { it.name.contains(hotelManagedName, ignoreCase = true) } ?: hotels.firstOrNull()

    // Filters and search
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0: Tous, 1: En Attente, 2: Validés, 3: Refusés
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var selectedProofBooking by remember { mutableStateOf<Booking?>(null) }
    var selectedVoucherBooking by remember { mutableStateOf<Booking?>(null) }
    var rejectDialogBooking by remember { mutableStateOf<Booking?>(null) }
    var rejectReason by remember { mutableStateOf("ID de transaction non reçu sur le relevé") }

    // Metrics calculations
    val totalCount = localBookings.size
    val pendingCount = localBookings.count { it.status == "En Attente" }
    val validatedCount = localBookings.count { it.status == "Validé" }
    val totalRevenueUSD = localBookings.filter { it.status == "Validé" }.sumOf { it.totalAmount }
    val totalRevenueCDF = PricingEngine.convertUSDToCDF(totalRevenueUSD)

    // Filtered bookings
    val displayedBookings = localBookings.filter { booking ->
        val matchesTab = when (selectedFilterIndex) {
            1 -> booking.status == "En Attente"
            2 -> booking.status == "Validé"
            3 -> booking.status == "Refusé"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                booking.userName.contains(searchQuery, ignoreCase = true) ||
                booking.transactionId.contains(searchQuery, ignoreCase = true) ||
                booking.id.contains(searchQuery, ignoreCase = true)

        matchesTab && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D1726)) // Luxury Dark Theme
    ) {
        // --- 1. TOP BAR ADMIN ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2A2234),
            border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1D1726),
                        border = BorderStroke(1.dp, BrandGold)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = BrandGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Console Réception & Gérance",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = BrandAzure.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandAzure,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = hotelManagedName,
                            fontSize = 12.sp,
                            color = BrandGoldLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Open Planning / Calendar
                    Button(
                        onClick = onOpenCalendar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandAzure,
                            contentColor = Color(0xFF1D1726)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Planning",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Open Catalogue Management
                    Button(
                        onClick = onOpenCatalog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGold,
                            contentColor = Color(0xFF1D1726)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Catalogue",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Open Finance & Accounting
                    IconButton(
                        onClick = onOpenFinance,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2ECC71).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF2ECC71).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Finances & Comptabilité",
                            tint = Color(0xFF2ECC71),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Back to showcase site button
                    IconButton(
                        onClick = onOpenShowcase,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1D1726))
                            .border(1.dp, BrandTaupe, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Site Vitrine",
                            tint = BrandGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Logout
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE74C3C).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFE74C3C).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Déconnexion",
                            tint = Color(0xFFFF7675),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 2. KPI METRICS STRIP ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pending validations (highlighted)
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "En Attente",
                        value = pendingCount.toString(),
                        subtitle = "Paiements à valider",
                        icon = Icons.Default.HourglassTop,
                        containerColor = if (pendingCount > 0) Color(0xFF4F3F31) else Color(0xFF2A2234),
                        accentColor = if (pendingCount > 0) BrandGold else Color(0xFFCCCCCC),
                        hasGlow = pendingCount > 0
                    )

                    // Confirmed arrivals
                    KpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Validées",
                        value = validatedCount.toString(),
                        subtitle = "Arrivées confirmées",
                        icon = Icons.Default.CheckCircle,
                        containerColor = Color(0xFF2A2234),
                        accentColor = Color(0xFF27AE60)
                    )

                    // Total Revenue
                    KpiCard(
                        modifier = Modifier.weight(1.2f),
                        title = "Recettes Validées",
                        value = "$${totalRevenueUSD.roundToInt()}",
                        subtitle = PricingEngine.formatCDF(totalRevenueCDF),
                        icon = Icons.Default.AttachMoney,
                        containerColor = Color(0xFF2A2234),
                        accentColor = BrandAzure
                    )
                }
            }

            // --- 2b. SHORTCUTS BANNER (PLANNING & CATALOGUE) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Planning card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenCalendar() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF2A2234),
                        border = BorderStroke(1.dp, BrandAzure.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = BrandAzure.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, BrandAzure)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = BrandAzure,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Planning & Report",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Décalage & Dispos",
                                    fontSize = 10.sp,
                                    color = Color(0xFFCCCCCC)
                                )
                            }
                        }
                    }

                    // Catalogue card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenCatalog() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF2A2234),
                        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = BrandGold.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, BrandGold)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = BrandGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Catalogue & Tarifs",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "CRUD Chambres & Meublés",
                                    fontSize = 10.sp,
                                    color = BrandGoldLight
                                )
                            }
                        }
                    }
                }
            }

            // Finance & Accounting Shortcut Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFinance() },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2A2234),
                    border = BorderStroke(1.dp, Color(0xFF2ECC71).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF2ECC71).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF2ECC71))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color(0xFF2ECC71),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Comptabilité, Finances & Reversements",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Journal des encaissements, commissions (10%) & export du bilan",
                                    fontSize = 10.sp,
                                    color = Color(0xFFCCCCCC)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF2ECC71),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // --- 3. SEARCH & OPERATOR RASSURANCE ---
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher par client, Réf ou Code Transaction...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = BrandGold,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer", tint = Color.White)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2234),
                        unfocusedContainerColor = Color(0xFF2A2234),
                        focusedBorderColor = BrandGold,
                        unfocusedBorderColor = BrandTaupe,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = BrandGold
                    ),
                    singleLine = true
                )
            }

            // --- 4. FILTER TABS ---
            item {
                val tabs = listOf(
                    "Toutes ($totalCount)",
                    "À Valider ($pendingCount)",
                    "Validées ($validatedCount)",
                    "Refusées"
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2A2234),
                    border = BorderStroke(1.dp, BrandTaupe)
                ) {
                    TabRow(
                        selectedTabIndex = selectedFilterIndex,
                        containerColor = Color.Transparent,
                        contentColor = BrandGold,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedFilterIndex]),
                                color = BrandGold,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedFilterIndex == index,
                                onClick = { selectedFilterIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedFilterIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedFilterIndex == index) BrandGoldLight else Color(0xFFCCCCCC)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // --- 5. BOOKING LIST FOR VALIDATION ---
            if (displayedBookings.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        color = Color(0xFF2A2234),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFFA6A5A6),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aucune réservation dans cette catégorie",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Les nouvelles demandes de réservation apparaîtront automatiquement ici dès leur soumission.",
                                fontSize = 12.sp,
                                color = Color(0xFFCCCCCC),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(displayedBookings.reversed(), key = { it.id }) { booking ->
                    AdminBookingValidationCard(
                        booking = booking,
                        onValidate = {
                            repository.updateBookingStatus(booking.id, "Validé")
                            Toast.makeText(context, "✅ Réservation ${booking.id} validée avec succès !", Toast.LENGTH_SHORT).show()
                        },
                        onRejectClick = {
                            rejectDialogBooking = booking
                        },
                        onViewProof = {
                            selectedProofBooking = booking
                        },
                        onViewVoucher = {
                            selectedVoucherBooking = booking
                        },
                        onCopyTxId = {
                            clipboardManager.setText(AnnotatedString(booking.transactionId))
                            Toast.makeText(context, "ID de transaction copié !", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // --- MODAL 1: PREUVE DE TRANSACTION & REÇU MOBILE MONEY ---
    if (selectedProofBooking != null) {
        val b = selectedProofBooking!!
        Dialog(onDismissRequest = { selectedProofBooking = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preuve de Paiement Mobile Money",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { selectedProofBooking = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Simulated Mobile Money SMS / Receipt Screen
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1D1726),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BrandTaupe)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val opColor = when (b.operatorSelected.uppercase()) {
                                    "M-PESA" -> Color(0xFFE74C3C)
                                    "AIRTEL MONEY" -> Color(0xFFE67E22)
                                    else -> Color(0xFFF39C12)
                                }
                                Surface(
                                    color = opColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, opColor)
                                ) {
                                    Text(
                                        text = b.operatorSelected.ifBlank { "Mobile Money RDC" },
                                        color = opColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = "Reçu Officiel",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA6A5A6)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "MONTANT ENCAISSÉ",
                                fontSize = 10.sp,
                                color = Color(0xFFA6A5A6),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${b.totalAmount.roundToInt()} USD",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandGold
                            )
                            Text(
                                text = "≈ ${PricingEngine.formatCDF(PricingEngine.convertUSDToCDF(b.totalAmount))}",
                                fontSize = 12.sp,
                                color = Color(0xFFCCCCCC)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = BrandTaupe.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            DetailRow(label = "ID de Transaction", value = b.transactionId.ifBlank { "MP260912.8942.A1" })
                            DetailRow(label = "Client Payeur", value = b.userName)
                            DetailRow(label = "Date & Heure", value = b.paymentDate.ifBlank { "12/09/2026 à 15:45" })
                            DetailRow(label = "Établissement", value = b.hotelName)
                            DetailRow(label = "Séjour", value = "${b.arrivalDate} -> ${b.departureDate} (${b.numNights} nuits)")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (b.status == "En Attente") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedProofBooking = null
                                    rejectDialogBooking = b
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE74C3C))
                            ) {
                                Text("Rejeter", color = Color(0xFFFF7675), fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    repository.updateBookingStatus(b.id, "Validé")
                                    selectedProofBooking = null
                                    Toast.makeText(context, "✅ Réservation validée !", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1.5f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF27AE60),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Valider maintenant", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { selectedProofBooking = null },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Fermer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- MODAL 2: REJET AVEC MOTIF ---
    if (rejectDialogBooking != null) {
        val b = rejectDialogBooking!!
        val reasons = listOf(
            "ID de transaction non reçu sur le relevé Mobile Money",
            "Montant transféré inférieur au tarif du séjour",
            "Chambre indisponible pour cause de maintenance imprévue",
            "Autre anomalie de paiement"
        )

        AlertDialog(
            onDismissRequest = { rejectDialogBooking = null },
            containerColor = Color(0xFF2A2234),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE74C3C))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rejeter la réservation ${b.id}", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Veuillez préciser le motif de refus. Ce message sera transmis au client pour lui permettre de corriger son paiement :",
                        fontSize = 12.sp,
                        color = Color(0xFFCCCCCC)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { rejectReason = r }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = rejectReason == r,
                                onClick = { rejectReason = r },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandGold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(r, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateBookingStatus(b.id, "Refusé")
                        rejectDialogBooking = null
                        Toast.makeText(context, "Réservation marquée comme refusée.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmer le Refus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectDialogBooking = null }) {
                    Text("Annuler", color = Color.White)
                }
            }
        )
    }

    // --- MODAL 3: VOUCHER / BON DE SÉJOUR OFFICIEL ---
    if (selectedVoucherBooking != null) {
        val b = selectedVoucherBooking!!
        Dialog(onDismissRequest = { selectedVoucherBooking = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with official seal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BOOKZZZ VOUCHER",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF1D1726)
                            )
                            Text(
                                text = "BON DE SÉJOUR OFFICIEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFCCA865)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF27AE60).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF27AE60))
                        ) {
                            Text(
                                text = "CONFIRMÉ & PAYÉ",
                                color = Color(0xFF27AE60),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Voucher Content
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoucherRow("Code Réservation :", b.id)
                        VoucherRow("Nom du Voyageur :", b.userName)
                        VoucherRow("Établissement :", b.hotelName)
                        VoucherRow("Hébergement :", b.roomType)
                        VoucherRow("Dates :", "${b.arrivalDate} au ${b.departureDate} (${b.numNights} nuits)")
                        VoucherRow("Montant Réglé :", "${b.totalAmount.roundToInt()} USD (${b.operatorSelected})")
                        VoucherRow("ID Transaction :", b.transactionId.ifBlank { "MP-VALIDATED-2026" })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF9F9F9),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ℹ️ Consignes d'accueil à la Réception :",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Le client est prié de présenter ce bon sur son smartphone ou sa pièce d'identité à l'arrivée. L'accès à la chambre et aux services inclus est garanti.",
                                fontSize = 10.sp,
                                color = Color(0xFF666666),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            selectedVoucherBooking = null
                            Toast.makeText(context, "Impression du voucher simulée avec succès !", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D1726),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Imprimer / Partager le Voucher", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminBookingValidationCard(
    booking: Booking,
    onValidate: () -> Unit,
    onRejectClick: () -> Unit,
    onViewProof: () -> Unit,
    onViewVoucher: () -> Unit,
    onCopyTxId: () -> Unit
) {
    val isPending = booking.status == "En Attente"
    val isValidated = booking.status == "Validé"
    val isRejected = booking.status == "Refusé"

    val opColor = when (booking.operatorSelected.uppercase()) {
        "M-PESA" -> Color(0xFFE74C3C)
        "AIRTEL MONEY" -> Color(0xFFE67E22)
        else -> Color(0xFFF39C12)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2A2234),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (isPending) 1.5.dp else 1.dp,
            color = if (isPending) BrandGold else BrandTaupe.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Guest name & Status badge
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
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color(0xFF1D1726),
                        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = booking.userName.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = BrandGoldLight,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = booking.userName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Réf: ${booking.id}",
                            fontSize = 11.sp,
                            color = Color(0xFFA6A5A6)
                        )
                    }
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isValidated -> Color(0xFF27AE60).copy(alpha = 0.15f)
                        isRejected -> Color(0xFFE74C3C).copy(alpha = 0.15f)
                        else -> BrandGold.copy(alpha = 0.15f)
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = when {
                            isValidated -> Color(0xFF27AE60)
                            isRejected -> Color(0xFFE74C3C)
                            else -> BrandGold
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isValidated -> "✓ VALIDÉ"
                            isRejected -> "✕ REFUSÉ"
                            else -> "⏳ EN ATTENTE"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isValidated -> Color(0xFF2ECC71)
                            isRejected -> Color(0xFFFF7675)
                            else -> BrandGoldLight
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BrandTaupe.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Booking Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Hébergement", fontSize = 10.sp, color = Color(0xFFA6A5A6))
                    Text(text = booking.roomType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text(text = "Arrivée / Départ", fontSize = 10.sp, color = Color(0xFFA6A5A6))
                    Text(text = "${booking.arrivalDate} -> ${booking.departureDate}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Montant Total", fontSize = 10.sp, color = Color(0xFFA6A5A6))
                    Text(text = "$${booking.totalAmount.roundToInt()}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = BrandGold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mobile Money Details Strip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1D1726),
                border = BorderStroke(1.dp, BrandTaupe)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = opColor,
                            modifier = Modifier.size(8.dp)
                        ) {}

                        Column {
                            Text(
                                text = "${booking.operatorSelected.ifBlank { "Mobile Money" }} • ID: ${booking.transactionId.ifBlank { "Tx ID en attente" }}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Équivalent : ${PricingEngine.formatCDF(PricingEngine.convertUSDToCDF(booking.totalAmount))}",
                                fontSize = 10.sp,
                                color = Color(0xFFA6A5A6)
                            )
                        }
                    }

                    if (booking.transactionId.isNotBlank()) {
                        IconButton(onClick = onCopyTxId, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier l'ID",
                                tint = BrandGoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Strip
            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewProof,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandTaupe),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preuve", fontSize = 12.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onRejectClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE74C3C).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF7675))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rejeter", fontSize = 12.sp, color = Color(0xFFFF7675))
                    }

                    Button(
                        onClick = onValidate,
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF27AE60),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Valider", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (isValidated) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onViewProof,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandTaupe),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFCCCCCC))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reçu", fontSize = 11.sp, color = Color(0xFFCCCCCC))
                    }

                    Button(
                        onClick = onViewVoucher,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGold,
                            contentColor = Color(0xFF1D1726)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voir le Bon / Voucher", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    accentColor: Color,
    hasGlow: Boolean = false
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (hasGlow) 1.5.dp else 1.dp,
            color = if (hasGlow) accentColor else BrandTaupe.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 10.sp, color = Color(0xFFA6A5A6), fontWeight = FontWeight.SemiBold)
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 9.sp, color = Color(0xFFCCCCCC), maxLines = 1)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFFA6A5A6))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun VoucherRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF666666))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1726))
    }
}
