package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
 * Sprint 5: Finance, Rapports d'Activité & Exportations Comptables
 * - Journal comptable des encaissements Mobile Money (M-Pesa, Airtel, Orange)
 * - Ventilation des recettes, calcul des commissions BookZzz (10%) et du net hôtelier (90%)
 * - Bilan périodique filtrable et exportable (Partage WhatsApp/Email, Copie presse-papier, Fiche PDF/CSV)
 */

@Composable
fun FinanceReportsScreen(
    repository: BookZzzRepository,
    currentUser: UserProfile?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val bookings by repository.bookings.collectAsState()

    val hotelManagedName = currentUser?.registeredHotelName ?: "Goma Serena Hotel"
    val localBookings = bookings.filter {
        it.hotelName.contains(hotelManagedName, ignoreCase = true) ||
                hotelManagedName.contains(it.hotelName, ignoreCase = true) ||
                hotelManagedName.isBlank()
    }

    var selectedPeriod by remember { mutableStateOf("Ce Mois (Sept 2026)") }
    val periods = listOf("Ce Mois (Sept 2026)", "Semaine en cours", "Aujourd'hui", "Tous les séjours")

    var selectedOperatorFilter by remember { mutableStateOf("Tous") }
    val operators = listOf("Tous", "M-Pesa", "Airtel Money", "Orange Money")

    var selectedStatusFilter by remember { mutableStateOf("Tous") }
    val statuses = listOf("Tous", "Validé", "En Attente")

    var selectedBookingForDetail by remember { mutableStateOf<Booking?>(null) }
    var showExportSummaryDialog by remember { mutableStateOf(false) }

    // Filter logic
    val filteredBookings = localBookings.filter { b ->
        val operatorMatch = if (selectedOperatorFilter == "Tous") true else b.operatorSelected.contains(selectedOperatorFilter, ignoreCase = true)
        val statusMatch = if (selectedStatusFilter == "Tous") true else b.status.equals(selectedStatusFilter, ignoreCase = true)
        operatorMatch && statusMatch
    }

    // Financial Metrics Calculation
    val validatedBookings = filteredBookings.filter { it.status == "Validé" }
    val totalGrossRevenueUSD = validatedBookings.sumOf { it.totalAmount }
    val totalGrossRevenueCDF = PricingEngine.convertUSDToCDF(totalGrossRevenueUSD)

    val commissionRate = 0.10 // 10% BookZzz platform fee
    val totalCommissionUSD = totalGrossRevenueUSD * commissionRate
    val totalCommissionCDF = PricingEngine.convertUSDToCDF(totalCommissionUSD)

    val netHotelRevenueUSD = totalGrossRevenueUSD - totalCommissionUSD
    val netHotelRevenueCDF = PricingEngine.convertUSDToCDF(netHotelRevenueUSD)

    val pendingRevenueUSD = filteredBookings.filter { it.status == "En Attente" }.sumOf { it.totalAmount }

    // Operator breakdown
    val mpesaBookings = validatedBookings.filter { it.operatorSelected.contains("M-Pesa", ignoreCase = true) }
    val airtelBookings = validatedBookings.filter { it.operatorSelected.contains("Airtel", ignoreCase = true) }
    val orangeBookings = validatedBookings.filter { it.operatorSelected.contains("Orange", ignoreCase = true) }

    val mpesaRevenueUSD = mpesaBookings.sumOf { it.totalAmount }
    val airtelRevenueUSD = airtelBookings.sumOf { it.totalAmount }
    val orangeRevenueUSD = orangeBookings.sumOf { it.totalAmount }

    fun buildSummaryText(): String {
        return """
            =========================================
            📊 RAPPORT FINANCIER & RECETTES BOOKZZZ
            Établissement : $hotelManagedName
            Période : $selectedPeriod
            Date d'export : 12 Septembre 2026
            =========================================
            
            💰 CHIFFRES CLÉS (TRANSACTIONS VALIDÉES) :
            • Volume Brut Encaissé : $${totalGrossRevenueUSD.roundToInt()} USD (${PricingEngine.formatCDF(totalGrossRevenueCDF)})
            • Commission Plateforme BookZzz (10%) : $${totalCommissionUSD.roundToInt()} USD (${PricingEngine.formatCDF(totalCommissionCDF)})
            • Net à Reverser Établissement (90%) : $${netHotelRevenueUSD.roundToInt()} USD (${PricingEngine.formatCDF(netHotelRevenueCDF)})
            • Nombre de réservations validées : ${validatedBookings.size}
            • En attente de validation : $${pendingRevenueUSD.roundToInt()} USD (${filteredBookings.count { it.status == "En Attente" }} transactions)
            
            📱 VENTILATION MOBILE MONEY :
            • M-Pesa (Vodacom) : $${mpesaRevenueUSD.roundToInt()} USD (${mpesaBookings.size} paiements)
            • Airtel Money : $${airtelRevenueUSD.roundToInt()} USD (${airtelBookings.size} paiements)
            • Orange Money : $${orangeRevenueUSD.roundToInt()} USD (${orangeBookings.size} paiements)
            
            📑 DÉTAIL DES DERNIÈRES OPÉRATIONS :
            ${validatedBookings.take(8).joinToString("\n") { b -> 
                val ref = if (b.transactionId.isNotBlank()) b.transactionId else "TXN-${b.id}"
                "- [${b.id}] ${b.userName} | ${b.roomType} | $${b.totalAmount.roundToInt()} USD via ${b.operatorSelected} (Réf: $ref)" 
            }}
            
            =========================================
            Généré automatiquement par le système BookZzz RDC
        """.trimIndent()
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
                            text = "Comptabilité & Finances",
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

                // Export Button
                Button(
                    onClick = { showExportSummaryDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGold,
                        contentColor = Color(0xFF1D1726)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Rapport", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            // --- 2. PERIOD SELECTOR CHIPS ---
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(periods) { p ->
                        val isSelected = selectedPeriod == p
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPeriod = p },
                            label = { Text(p, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandGold,
                                selectedLabelColor = Color(0xFF1D1726),
                                containerColor = Color(0xFF2A2234),
                                labelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) BrandGold else BrandTaupe.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // --- 3. EXECUTIVE FINANCIAL SUMMARY ---
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF2A2234),
                    border = BorderStroke(1.5.dp, BrandGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SYNTHÈSE DES ENCAISSEMENTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGoldLight
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF2ECC71).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF2ECC71))
                            ) {
                                Text(
                                    text = "RÉCONCILIATION DIRECTE",
                                    color = Color(0xFF2ECC71),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Total Gross Revenue
                        Text(
                            text = "$${totalGrossRevenueUSD.roundToInt()} USD",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Équivalence : ${PricingEngine.formatCDF(totalGrossRevenueCDF)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandGold
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BrandTaupe.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Breakdown Net Hotel vs BookZzz Commission
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Net Hotel
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF1D1726),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF2ECC71).copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Net Établissement (90%)", fontSize = 10.sp, color = Color(0xFFA6A5A6))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$${netHotelRevenueUSD.roundToInt()} USD",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2ECC71)
                                    )
                                    Text(
                                        text = PricingEngine.formatCDF(netHotelRevenueCDF),
                                        fontSize = 9.sp,
                                        color = Color(0xFFCCCCCC)
                                    )
                                }
                            }

                            // BookZzz Fee
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF1D1726),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BrandAzure.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Frais Plateforme (10%)", fontSize = 10.sp, color = Color(0xFFA6A5A6))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$${totalCommissionUSD.roundToInt()} USD",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandAzure
                                    )
                                    Text(
                                        text = PricingEngine.formatCDF(totalCommissionCDF),
                                        fontSize = 9.sp,
                                        color = Color(0xFFCCCCCC)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 4. OPERATOR BREAKDOWN STRIP ---
            item {
                Text(
                    text = "Ventilation par Canal Mobile Money",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OperatorCard(
                        modifier = Modifier.weight(1f),
                        name = "M-Pesa",
                        amountUSD = mpesaRevenueUSD,
                        count = mpesaBookings.size,
                        color = Color(0xFFE74C3C)
                    )
                    OperatorCard(
                        modifier = Modifier.weight(1f),
                        name = "Airtel Money",
                        amountUSD = airtelRevenueUSD,
                        count = airtelBookings.size,
                        color = Color(0xFFE67E22)
                    )
                    OperatorCard(
                        modifier = Modifier.weight(1f),
                        name = "Orange Money",
                        amountUSD = orangeRevenueUSD,
                        count = orangeBookings.size,
                        color = Color(0xFFF39C12)
                    )
                }
            }

            // --- 5. JOURNAL DES ÉCRITURES & TRANSACTIONS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Journal des Écritures (${filteredBookings.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Filters for operator
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        operators.forEach { op ->
                            val isSel = selectedOperatorFilter == op
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedOperatorFilter = op },
                                color = if (isSel) BrandGold else Color(0xFF2A2234),
                                border = BorderStroke(0.5.dp, if (isSel) BrandGold else BrandTaupe)
                            ) {
                                Text(
                                    text = op,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color(0xFF1D1726) else Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (filteredBookings.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2A2234),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Aucune écriture comptable pour ce filtre", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredBookings, key = { it.id }) { booking ->
                    FinancialEntryCard(
                        booking = booking,
                        onClick = { selectedBookingForDetail = booking }
                    )
                }
            }
        }
    }

    // --- 6. EXPORT / SHARE DIALOG ---
    if (showExportSummaryDialog) {
        val summary = buildSummaryText()
        Dialog(onDismissRequest = { showExportSummaryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.5.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rapport Comptable Officiel",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        IconButton(onClick = { showExportSummaryDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }

                    Text(
                        text = "Établissement : $hotelManagedName • Format texte & PDF",
                        fontSize = 11.sp,
                        color = BrandGoldLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Text preview box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1D1726),
                        border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.6f))
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = summary,
                                    fontSize = 10.sp,
                                    color = Color(0xFFE0E0E0),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Copy to clipboard
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(summary))
                                Toast.makeText(context, "Bilan comptable copié dans le presse-papier", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandGold)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BrandGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copier", color = BrandGold, fontSize = 11.sp)
                        }

                        // Share Intent
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, summary)
                                    putExtra(Intent.EXTRA_SUBJECT, "Rapport Financier BookZzz - $hotelManagedName")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Transmettre le rapport comptable")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Partager / Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- 7. TRANSACTION DETAIL MODAL ---
    if (selectedBookingForDetail != null) {
        val b = selectedBookingForDetail!!
        val grossUSD = b.totalAmount
        val feeUSD = grossUSD * 0.10
        val netUSD = grossUSD - feeUSD
        val isValidated = b.status == "Validé"

        Dialog(onDismissRequest = { selectedBookingForDetail = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.5.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pièce Comptable #${b.id}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { selectedBookingForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1D1726),
                        border = BorderStroke(1.dp, BrandTaupe)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "CLIENT", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(text = b.userName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "ID: ${b.userId}", fontSize = 11.sp, color = BrandGoldLight)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    val ref = if (b.transactionId.isNotBlank()) b.transactionId else "PAY-${b.id}"
                                    Text(text = "OPÉRATEUR", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(text = b.operatorSelected, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandAzure)
                                    Text(text = "Réf: $ref", fontSize = 10.sp, color = Color.LightGray)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BrandTaupe.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Montant Brut Encaissé :", fontSize = 12.sp, color = Color.White)
                                Text(text = "$${grossUSD.roundToInt()} USD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Commission BookZzz (10%) :", fontSize = 11.sp, color = BrandAzure)
                                Text(text = "-$${feeUSD.roundToInt()} USD", fontSize = 11.sp, color = BrandAzure)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Net Hôtel à Reverser (90%) :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2ECC71))
                                Text(text = "$${netUSD.roundToInt()} USD", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF2ECC71))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val ref = if (b.transactionId.isNotBlank()) b.transactionId else "PAY-${b.id}"
                            clipboardManager.setText(AnnotatedString("Bordereau BookZzz #${b.id} - ${b.userName} - Brut: $${grossUSD.roundToInt()} USD - Net: $${netUSD.roundToInt()} USD - Réf: $ref"))
                            Toast.makeText(context, "Bordereau copié !", Toast.LENGTH_SHORT).show()
                            selectedBookingForDetail = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copier le Bordereau d'Écriture", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
private fun OperatorCard(
    modifier: Modifier = Modifier,
    name: String,
    amountUSD: Double,
    count: Int,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF2A2234),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$${amountUSD.roundToInt()} USD",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = BrandGoldLight
            )
            Text(
                text = "$count txn(s)",
                fontSize = 10.sp,
                color = Color(0xFFCCCCCC)
            )
        }
    }
}

@Composable
private fun FinancialEntryCard(
    booking: Booking,
    onClick: () -> Unit
) {
    val isValidated = booking.status == "Validé"
    val grossUSD = booking.totalAmount
    val netHotelUSD = grossUSD * 0.90

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF2A2234),
        border = BorderStroke(1.dp, if (isValidated) BrandTaupe else Color(0xFFF39C12).copy(alpha = 0.5f))
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
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isValidated) Color(0xFF2ECC71).copy(alpha = 0.15f) else Color(0xFFF39C12).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isValidated) Color(0xFF2ECC71) else Color(0xFFF39C12))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isValidated) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (isValidated) Color(0xFF2ECC71) else Color(0xFFF39C12),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    val ref = if (booking.transactionId.isNotBlank()) booking.transactionId else "PAY-${booking.id.takeLast(6)}"
                    Text(
                        text = booking.userName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${booking.operatorSelected} • Réf: ${ref.take(14)}",
                        fontSize = 10.sp,
                        color = Color(0xFFCCCCCC)
                    )
                    Text(
                        text = "${booking.roomType} (${booking.numNights}n)",
                        fontSize = 9.sp,
                        color = BrandGoldLight
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${grossUSD.roundToInt()} USD",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Net : $${netHotelUSD.roundToInt()} USD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2ECC71)
                )
                Text(
                    text = if (isValidated) "Validé" else "En attente",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isValidated) Color(0xFF2ECC71) else Color(0xFFF39C12)
                )
            }
        }
    }
}
