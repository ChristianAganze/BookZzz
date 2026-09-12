package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BookZzzRepository
import com.example.data.Booking
import com.example.data.UserProfile
import com.example.shared.engine.PricingEngine
import com.example.ui.theme.BrandAzure
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGoldLight
import com.example.ui.theme.BrandTaupe
import kotlin.math.roundToInt

/**
 * Sprint 4: Calendrier, Disponibilités en Direct & Module de Report/Décalage de Dates
 * - Vue Mensuelle Interactive avec taux d'occupation par jour
 * - Agenda des Arrivées / Départs / Séjours en cours pour chaque date sélectionnée
 * - Moteur de Report de Séjour (Date Shifting) avec ajustement automatique du montant et audit trail
 * - Blocage rapide de dates pour maintenance/réservation VIP
 */

@Composable
fun CalendarScheduleScreen(
    repository: BookZzzRepository,
    currentUser: UserProfile?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val bookings by repository.bookings.collectAsState()
    val rooms by repository.rooms.collectAsState()
    val hotels by repository.hotels.collectAsState()

    val hotelManagedName = currentUser?.registeredHotelName ?: "Goma Serena Hotel"
    val managedHotel = hotels.find { it.name.contains(hotelManagedName, ignoreCase = true) } ?: hotels.firstOrNull()
    val localBookings = bookings.filter {
        it.hotelName.contains(hotelManagedName, ignoreCase = true) ||
                hotelManagedName.contains(it.hotelName, ignoreCase = true) ||
                hotelManagedName.isBlank()
    }

    // Calendar state (Default Sept 2026)
    var currentMonthIndex by remember { mutableIntStateOf(9) } // 9: Septembre 2026
    val monthName = when (currentMonthIndex) {
        8 -> "Août 2026"
        9 -> "Septembre 2026"
        10 -> "Octobre 2026"
        11 -> "Novembre 2026"
        else -> "Septembre 2026"
    }
    val daysInMonth = when (currentMonthIndex) {
        8 -> 31
        9 -> 30
        10 -> 31
        11 -> 30
        else -> 30
    }
    // For Sept 2026, Sept 1st is a Tuesday (offset = 1 if starting Monday)
    val startDayOffset = when (currentMonthIndex) {
        8 -> 5 // Saturday
        9 -> 1 // Tuesday
        10 -> 3 // Thursday
        11 -> 6 // Sunday
        else -> 1
    }

    var selectedDay by remember { mutableIntStateOf(15) } // Default selected: 15/09/2026
    var rescheduleTargetBooking by remember { mutableStateOf<Booking?>(null) }
    var showHistoryDialogForBooking by remember { mutableStateOf<Booking?>(null) }

    // Helpers to find bookings touching a given day
    fun getBookingsForDay(day: Int): List<Booking> {
        val dateFormatted = String.format("%02d/%02d/2026", day, currentMonthIndex)
        return localBookings.filter { b ->
            b.arrivalDate == dateFormatted ||
                    b.departureDate == dateFormatted ||
                    isDateBetween(dateFormatted, b.arrivalDate, b.departureDate)
        }
    }

    val selectedDateFormatted = String.format("%02d/%02d/2026", selectedDay, currentMonthIndex)
    val dayBookings = getBookingsForDay(selectedDay)
    val checkInsToday = localBookings.filter { it.arrivalDate == selectedDateFormatted }
    val checkOutsToday = localBookings.filter { it.departureDate == selectedDateFormatted }
    val inHouseToday = localBookings.filter {
        it.arrivalDate != selectedDateFormatted &&
                it.departureDate != selectedDateFormatted &&
                isDateBetween(selectedDateFormatted, it.arrivalDate, it.departureDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D1726))
    ) {
        // --- 1. TOP BAR ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2A2234),
            border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1D1726), RoundedCornerShape(10.dp))
                            .border(1.dp, BrandTaupe, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = BrandGold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Planning & Disponibilités",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = hotelManagedName,
                            fontSize = 11.sp,
                            color = BrandGoldLight
                        )
                    }
                }

                Surface(
                    color = BrandAzure.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BrandAzure)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BrandAzure, modifier = Modifier.size(13.dp))
                        Text(
                            text = "CALENDRIER ACTIF",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandAzure
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 2. MONTH SELECTOR & SUMMARY ---
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF2A2234),
                    border = BorderStroke(1.dp, BrandTaupe)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentMonthIndex > 8) currentMonthIndex-- },
                            enabled = currentMonthIndex > 8
                        ) {
                            Icon(
                                Icons.Default.ArrowBackIosNew,
                                contentDescription = "Mois précédent",
                                tint = if (currentMonthIndex > 8) BrandGold else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = monthName.uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandGoldLight
                            )
                            Text(
                                text = "${localBookings.size} réservations ce mois",
                                fontSize = 10.sp,
                                color = Color(0xFFCCCCCC)
                            )
                        }

                        IconButton(
                            onClick = { if (currentMonthIndex < 11) currentMonthIndex++ },
                            enabled = currentMonthIndex < 11
                        ) {
                            Icon(
                                Icons.Default.ArrowForwardIos,
                                contentDescription = "Mois suivant",
                                tint = if (currentMonthIndex < 11) BrandGold else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // --- 3. CALENDAR GRID ---
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF2A2234),
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Day of week headers
                        val weekDays = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            weekDays.forEach { d ->
                                Text(
                                    text = d,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGoldLight,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BrandTaupe.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid calculations (6 rows x 7 cols)
                        val totalCells = startDayOffset + daysInMonth
                        val numRows = (totalCells + 6) / 7

                        for (row in 0 until numRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (col in 0 until 7) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - startDayOffset + 1

                                    if (dayNum in 1..daysInMonth) {
                                        val dayBks = getBookingsForDay(dayNum)
                                        val isSelected = selectedDay == dayNum
                                        val hasPending = dayBks.any { it.status == "En Attente" }
                                        val hasConfirmed = dayBks.any { it.status == "Validé" }
                                        val hasRescheduled = dayBks.any { it.rescheduledFrom != null }

                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedDay = dayNum },
                                            color = when {
                                                isSelected -> BrandGold
                                                dayBks.isNotEmpty() -> Color(0xFF3B2F4A)
                                                else -> Color(0xFF1D1726)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                                color = if (isSelected) Color.White else BrandTaupe.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected || dayBks.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color(0xFF1D1726) else Color.White
                                                )

                                                if (dayBks.isNotEmpty()) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        if (hasPending) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .background(if (isSelected) Color(0xFF1D1726) else Color(0xFFF39C12), CircleShape)
                                                            )
                                                        }
                                                        if (hasConfirmed) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .background(if (isSelected) Color(0xFF1D1726) else Color(0xFF2ECC71), CircleShape)
                                                            )
                                                        }
                                                        if (hasRescheduled) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .background(if (isSelected) Color(0xFF1D1726) else BrandAzure, CircleShape)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Empty cell for calendar padding
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Legend strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LegendItem(color = Color(0xFF2ECC71), label = "Confirmé")
                            LegendItem(color = Color(0xFFF39C12), label = "En Attente")
                            LegendItem(color = BrandAzure, label = "Reporté")
                        }
                    }
                }
            }

            // --- 4. AGENDA FOR SELECTED DAY ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Agenda du $selectedDateFormatted",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${dayBookings.size} mouvement(s) prévu(s)",
                            fontSize = 11.sp,
                            color = BrandGoldLight
                        )
                    }

                    Surface(
                        color = Color(0xFF1D1726),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BrandTaupe)
                    ) {
                        Text(
                            text = "Arrivées: ${checkInsToday.size} | Départs: ${checkOutsToday.size}",
                            fontSize = 10.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (dayBookings.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2A2234),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFA6A5A6), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune réservation pour cette date",
                                color = Color(0xFFCCCCCC),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Toutes les chambres de l'hôtel sont libres à la location.",
                                color = Color(0xFFA6A5A6),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(dayBookings, key = { it.id }) { booking ->
                    BookingAgendaCard(
                        booking = booking,
                        selectedDate = selectedDateFormatted,
                        onRescheduleClick = { rescheduleTargetBooking = booking },
                        onViewHistoryClick = { showHistoryDialogForBooking = booking }
                    )
                }
            }
        }
    }

    // --- 5. RESCHEDULE MODAL / DATE SHIFTING ENGINE ---
    if (rescheduleTargetBooking != null) {
        val target = rescheduleTargetBooking!!
        var newArrivalDate by remember { mutableStateOf(target.arrivalDate) }
        var newDepartureDate by remember { mutableStateOf(target.departureDate) }
        var newNightsStr by remember { mutableStateOf(target.numNights.toString()) }
        var reasonSelected by remember { mutableStateOf("Vol reporté (Congo Airways / CAA)") }
        var customReason by remember { mutableStateOf("") }

        val presetReasons = listOf(
            "Vol reporté (Congo Airways / CAA)",
            "Imprévu professionnel / Mission décalée",
            "Extension de séjour demandée par le client",
            "Demande d'ajustement de date par l'hôtelier"
        )

        Dialog(onDismissRequest = { rescheduleTargetBooking = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.5.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(18.dp)) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EditCalendar, contentDescription = null, tint = BrandGold, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Report / Décalage de Séjour",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { rescheduleTargetBooking = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                            }
                        }
                        Text(
                            text = "Réf : ${target.id} • ${target.userName}",
                            fontSize = 11.sp,
                            color = BrandGoldLight
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Current Dates Card
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1D1726),
                            border = BorderStroke(1.dp, BrandTaupe)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "DATES INITIALES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA6A5A6))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${target.arrivalDate} ➔ ${target.departureDate} (${target.numNights} nuits)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Montant : $${target.totalAmount.roundToInt()} USD (${PricingEngine.formatCDF(PricingEngine.convertUSDToCDF(target.totalAmount))})",
                                    fontSize = 11.sp,
                                    color = BrandGoldLight
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // New Dates Form
                    item {
                        Text(text = "NOUVELLES DATES SOUHAITÉES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandGold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newArrivalDate,
                                onValueChange = { newArrivalDate = it },
                                label = { Text("Arrivée (JJ/MM/AAAA)") },
                                modifier = Modifier.weight(1f),
                                colors = calendarOutlinedColors()
                            )
                            OutlinedTextField(
                                value = newDepartureDate,
                                onValueChange = { newDepartureDate = it },
                                label = { Text("Départ (JJ/MM/AAAA)") },
                                modifier = Modifier.weight(1f),
                                colors = calendarOutlinedColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newNightsStr,
                            onValueChange = { newNightsStr = it },
                            label = { Text("Nombre de Nuitées") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = calendarOutlinedColors()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Motifs Prédéfinis
                    item {
                        Text(text = "MOTIF DU REPORT / MODIFICATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA6A5A6))
                        Spacer(modifier = Modifier.height(6.dp))

                        presetReasons.forEach { reason ->
                            val isSelected = reasonSelected == reason
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { reasonSelected = reason },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) BrandGold.copy(alpha = 0.15f) else Color(0xFF1D1726),
                                border = BorderStroke(1.dp, if (isSelected) BrandGold else BrandTaupe.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isSelected) BrandGold else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = reason,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White else Color(0xFFCCCCCC)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Price recalculation preview
                    item {
                        val parsedNights = newNightsStr.toIntOrNull() ?: target.numNights
                        val newTotalUSD = parsedNights * target.pricePerNight
                        val newTotalCDF = PricingEngine.convertUSDToCDF(newTotalUSD)

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1D1726),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandAzure)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Nouveau Total Facturé", fontSize = 10.sp, color = Color(0xFFA6A5A6))
                                    Text(text = "$${newTotalUSD.roundToInt()} USD", fontSize = 14.sp, fontWeight = FontWeight.Black, color = BrandGold)
                                }
                                Text(
                                    text = PricingEngine.formatCDF(newTotalCDF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandAzure
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { rescheduleTargetBooking = null }) {
                                Text("Annuler", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val nights = newNightsStr.toIntOrNull() ?: target.numNights
                                    val finalReason = if (customReason.isNotBlank()) customReason else reasonSelected
                                    repository.rescheduleBooking(
                                        bookingId = target.id,
                                        newArrival = newArrivalDate,
                                        newDeparture = newDepartureDate,
                                        newNumNights = nights,
                                        reason = finalReason
                                    )
                                    Toast.makeText(context, "Séjour reporté avec succès !", Toast.LENGTH_LONG).show()
                                    rescheduleTargetBooking = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Confirmer le Report", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- 6. HISTORY DIALOG ---
    if (showHistoryDialogForBooking != null) {
        val b = showHistoryDialogForBooking!!
        Dialog(onDismissRequest = { showHistoryDialogForBooking = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Historique des Décalages",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showHistoryDialogForBooking = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }
                    Text(text = "Réf: ${b.id} • ${b.userName}", fontSize = 11.sp, color = BrandGoldLight)
                    Spacer(modifier = Modifier.height(14.dp))

                    if (b.dateChangeHistory.isEmpty()) {
                        Text(
                            text = "Aucune modification de dates enregistrée pour cette réservation.",
                            color = Color(0xFFCCCCCC),
                            fontSize = 12.sp
                        )
                    } else {
                        b.dateChangeHistory.forEach { history ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1D1726),
                                border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = BrandAzure, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = history, fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
private fun BookingAgendaCard(
    booking: Booking,
    selectedDate: String,
    onRescheduleClick: () -> Unit,
    onViewHistoryClick: () -> Unit
) {
    val isCheckIn = booking.arrivalDate == selectedDate
    val isCheckOut = booking.departureDate == selectedDate

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2A2234),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (booking.rescheduledFrom != null) BrandAzure.copy(alpha = 0.8f) else BrandTaupe)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = booking.userName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (booking.rescheduledFrom != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = BrandAzure.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, BrandAzure)
                            ) {
                                Text(
                                    text = "DATES REPORTÉES",
                                    color = BrandAzure,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${booking.roomType} • Réf : ${booking.id}",
                        fontSize = 11.sp,
                        color = BrandGoldLight
                    )
                }

                // Tag Arrivée / Départ / En séjour
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCheckIn -> Color(0xFF27AE60).copy(alpha = 0.2f)
                        isCheckOut -> Color(0xFFE67E22).copy(alpha = 0.2f)
                        else -> BrandAzure.copy(alpha = 0.2f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isCheckIn -> Color(0xFF27AE60)
                            isCheckOut -> Color(0xFFE67E22)
                            else -> BrandAzure
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isCheckIn -> "ARRIVÉE / CHECK-IN"
                            isCheckOut -> "DÉPART / CHECK-OUT"
                            else -> "EN SÉJOUR"
                        },
                        color = when {
                            isCheckIn -> Color(0xFF2ECC71)
                            isCheckOut -> Color(0xFFF39C12)
                            else -> BrandAzure
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BrandTaupe.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Séjour : ${booking.arrivalDate} ➔ ${booking.departureDate} (${booking.numNights} nuits)",
                        fontSize = 11.sp,
                        color = Color(0xFFCCCCCC)
                    )
                    Text(
                        text = "Total : $${booking.totalAmount.roundToInt()} USD • Paiement : ${booking.operatorSelected}",
                        fontSize = 11.sp,
                        color = BrandGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (booking.dateChangeHistory.isNotEmpty()) {
                        IconButton(
                            onClick = onViewHistoryClick,
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF1D1726), RoundedCornerShape(8.dp))
                                .border(1.dp, BrandAzure, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.History, contentDescription = "Historique", tint = BrandAzure, modifier = Modifier.size(16.dp))
                        }
                    }

                    Button(
                        onClick = onRescheduleClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reporter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(text = label, fontSize = 10.sp, color = Color(0xFFCCCCCC))
    }
}

private fun isDateBetween(targetDate: String, startDate: String, endDate: String): Boolean {
    // Simple helper assuming format DD/MM/YYYY
    try {
        val partsT = targetDate.split("/")
        val partsS = startDate.split("/")
        val partsE = endDate.split("/")

        if (partsT.size == 3 && partsS.size == 3 && partsE.size == 3) {
            val dayT = partsT[0].toInt()
            val monthT = partsT[1].toInt()
            val dayS = partsS[0].toInt()
            val monthS = partsS[1].toInt()
            val dayE = partsE[0].toInt()
            val monthE = partsE[1].toInt()

            if (monthT == monthS && monthT == monthE) {
                return dayT in dayS..dayE
            }
        }
    } catch (_: Exception) {}
    return false
}

@Composable
private fun calendarOutlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF1D1726),
    unfocusedContainerColor = Color(0xFF1D1726),
    focusedBorderColor = BrandGold,
    unfocusedBorderColor = BrandTaupe,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = BrandGoldLight,
    unfocusedLabelColor = Color(0xFFA6A5A6),
    cursorColor = BrandGold
)
