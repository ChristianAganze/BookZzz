package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookZzzRepository
import com.example.data.Hotel
import com.example.data.UserProfile
import com.example.shared.engine.PricingEngine
import com.example.ui.theme.BrandAzure
import com.example.ui.theme.BrandCaramel
import com.example.ui.theme.BrandEspresso
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGoldLight
import com.example.ui.theme.BrandTaupe

/**
 * Sprint 1: Onboarding Continu & Progressif pour Partenaires & Hôteliers
 * Conçu selon les meilleures pratiques UX/UI & GDE (Google Developer Expert) :
 * - Découpage en étapes claires et digestes (évite la fatigue cognitive des longs formulaires)
 * - Validation instantanée et bienveillante
 * - Conversion multi-devises intégrée ($ USD et équivalent Franc Congolais)
 * - Confirmation visuelle et publication immédiate dans la base de données
 */

enum class OnboardingServiceType(
    val code: String,
    val title: String,
    val subtitle: String,
    val defaultPrice: Double,
    val icon: ImageVector
) {
    HOTEL(
        code = "HOTEL",
        title = "Hôtel & Chambres",
        subtitle = "Pour les hôtels, auberges et complexes hôteliers",
        defaultPrice = 120.0,
        icon = Icons.Default.Hotel
    ),
    APARTMENT(
        code = "APARTMENT",
        title = "Appartement & Résidence Meublée",
        subtitle = "Logements autonomes pour courts & moyens séjours",
        defaultPrice = 75.0,
        icon = Icons.Default.Apartment
    ),
    REAL_ESTATE(
        code = "REAL_ESTATE",
        title = "Maison / Bail Résidentiel",
        subtitle = "Publication de maisons et appartements à louer à l'année",
        defaultPrice = 400.0,
        icon = Icons.Default.House
    ),
    VEHICLE(
        code = "VEHICLE",
        title = "Flotte de Véhicules & Navettes",
        subtitle = "4x4 VIP, berlines et minibus avec chauffeurs",
        defaultPrice = 30.0,
        icon = Icons.Default.DirectionsCar
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartnerOnboardingScreen(
    currentUser: UserProfile,
    repository: BookZzzRepository,
    onOnboardingFinished: () -> Unit,
    onCancel: () -> Unit
) {
    // Current step index: 1 to 4
    var currentStep by remember { mutableIntStateOf(1) }

    // Form states
    var selectedService by remember { mutableStateOf(OnboardingServiceType.HOTEL) }
    var establishmentName by remember { mutableStateOf("") }
    var establishmentDescription by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("Goma") }
    var fullAddress by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("+243 812 345 678") }
    var paymentOperator by remember { mutableStateOf("M-Pesa") }
    var paymentPhoneNumber by remember { mutableStateOf("+243 812 345 678") }

    var basePriceUSD by remember { mutableStateOf("120") }
    var selectedAmenities by remember {
        mutableStateOf(
            setOf(
                "Groupe électrogène 24/7",
                "Wi-Fi Fibre Gratuit",
                "Climatisation",
                "Eau courante 24/7",
                "Gardiennage sécurisé"
            )
        )
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val allCities = listOf("Goma", "Kinshasa", "Bukavu", "Lubumbashi", "Autre ville")
    val allOperators = listOf("M-Pesa", "Airtel Money", "Orange Money")
    val availableAmenities = listOf(
        "Groupe électrogène 24/7",
        "Wi-Fi Fibre Gratuit",
        "Climatisation",
        "Eau courante 24/7",
        "Piscine",
        "Restaurant & Bar",
        "Navette Aéroport",
        "Gardiennage sécurisé",
        "Parking privé gratuit",
        "Cuisine équipée",
        "Smart TV & Câble"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D1726)) // Luxury Dark Plum
    ) {
        // --- STEPPER HEADER ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2A2234),
            border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) {
                                currentStep -= 1
                            } else {
                                onCancel()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentStep > 1) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Initialisation Partenaire",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Étape $currentStep sur 4 : " + when (currentStep) {
                                1 -> "Choix du Service"
                                2 -> "Identité & Contact"
                                3 -> "Tarifs & Équipements"
                                else -> "Récapitulatif & Mise en ligne"
                            },
                            fontSize = 12.sp,
                            color = BrandGoldLight
                        )
                    }

                    Box(modifier = Modifier.size(40.dp)) // Spacer for alignment
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { currentStep / 4f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BrandGold,
                    trackColor = Color(0xFF1D1726)
                )
            }
        }

        // --- STEP CONTENT WITH SMOOTH ANIMATION ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn(tween(300))).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut(tween(300))
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn(tween(300))).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut(tween(300))
                        )
                    }
                },
                label = "OnboardingStepperAnimation"
            ) { targetStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    when (targetStep) {
                        1 -> {
                            // --- ÉTAPE 1: CHOIX DU SERVICE ---
                            Text(
                                text = "Quel type de service souhaitez-vous proposer ?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Sélectionnez la catégorie principale de votre établissement. Vous pourrez en ajouter d'autres plus tard.",
                                fontSize = 13.sp,
                                color = Color(0xFFCCCCCC)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OnboardingServiceType.values().forEach { service ->
                                val isSelected = service == selectedService
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                        .clickable {
                                            selectedService = service
                                            basePriceUSD = service.defaultPrice.toInt().toString()
                                        },
                                    color = if (isSelected) Color(0xFF382E44) else Color(0xFF2A2234),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) BrandGold else BrandTaupe.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(46.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) BrandGold else Color(0xFF1D1726)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = service.icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color(0xFF1D1726) else BrandGold,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = service.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = service.subtitle,
                                                fontSize = 12.sp,
                                                color = Color(0xFFCCCCCC)
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Sélectionné",
                                                tint = BrandGold,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // --- ÉTAPE 2: IDENTITÉ & CONTACT ---
                            Text(
                                text = "Identité & Contact Réception",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ces coordonnées permettront aux clients de localiser votre bien et à votre équipe de recevoir les notifications.",
                                fontSize = 13.sp,
                                color = Color(0xFFCCCCCC)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Nom de l'établissement
                            OutlinedTextField(
                                value = establishmentName,
                                onValueChange = {
                                    establishmentName = it
                                    errorMessage = null
                                },
                                label = { Text("Nom de l'établissement / Propriété *") },
                                placeholder = { Text("Ex: Hôtel Lac Kivu Palace ou Villa Bel Horizon") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGold,
                                    unfocusedBorderColor = BrandTaupe,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = BrandGold,
                                    unfocusedLabelColor = Color(0xFFCCCCCC)
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Ville
                            Text(
                                text = "Ville d'implantation *",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allCities.forEach { city ->
                                    val isSelected = city == selectedCity
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCity = city },
                                        label = { Text(city) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BrandGold,
                                            selectedLabelColor = Color(0xFF1D1726),
                                            containerColor = Color(0xFF2A2234),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Adresse / Quartier
                            OutlinedTextField(
                                value = fullAddress,
                                onValueChange = { fullAddress = it },
                                label = { Text("Quartier & Adresse précise") },
                                placeholder = { Text("Ex: Quartier Himbi, Avenue de la Paix, n°14") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGold,
                                    unfocusedBorderColor = BrandTaupe,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = BrandGold,
                                    unfocusedLabelColor = Color(0xFFCCCCCC)
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Numéro Mobile Money pour encaissements
                            Text(
                                text = "Encaissement des Réservations (Mobile Money) *",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allOperators.forEach { op ->
                                    val isSelected = op == paymentOperator
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { paymentOperator = op },
                                        label = { Text(op, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BrandAzure,
                                            selectedLabelColor = Color(0xFF1D1726),
                                            containerColor = Color(0xFF2A2234),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = paymentPhoneNumber,
                                onValueChange = { paymentPhoneNumber = it },
                                label = { Text("Numéro Mobile Money Récepteur ($paymentOperator)") },
                                placeholder = { Text("+243 812 345 678") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGold,
                                    unfocusedBorderColor = BrandTaupe,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = BrandGold,
                                    unfocusedLabelColor = Color(0xFFCCCCCC)
                                ),
                                singleLine = true
                            )
                        }

                        3 -> {
                            // --- ÉTAPE 3: TARIFS & ÉQUIPEMENTS ---
                            Text(
                                text = "Tarification & Commodités",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Définissez votre tarif de base et cochez les atouts qui valorisent votre bien auprès des voyageurs.",
                                fontSize = 13.sp,
                                color = Color(0xFFCCCCCC)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Tarif en USD avec équivalent FC automatique
                            val parsedUSD = basePriceUSD.toDoubleOrNull() ?: 0.0
                            val equivalentCDF = PricingEngine.convertUSDToCDF(parsedUSD)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF2A2234),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "PRIX DE BASE DE LA NUITÉE / SÉJOUR",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandGold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = basePriceUSD,
                                        onValueChange = { basePriceUSD = it },
                                        label = { Text("Tarif en Dollar US ($)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BrandGold,
                                            unfocusedBorderColor = BrandTaupe,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedLabelColor = BrandGold,
                                            unfocusedLabelColor = Color(0xFFCCCCCC)
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Surface(
                                        color = Color(0xFF1D1726),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = BrandGoldLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Équivalent automatique : ${PricingEngine.formatCDF(equivalentCDF)} (Taux : 1$ = 2850 FC)",
                                                fontSize = 12.sp,
                                                color = Color(0xFFE0E0E0),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Équipements & Prestations Incluses",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availableAmenities.forEach { amenity ->
                                    val isChecked = selectedAmenities.contains(amenity)
                                    FilterChip(
                                        selected = isChecked,
                                        onClick = {
                                            selectedAmenities = if (isChecked) {
                                                selectedAmenities - amenity
                                            } else {
                                                selectedAmenities + amenity
                                            }
                                        },
                                        label = { Text(amenity, fontSize = 12.sp) },
                                        leadingIcon = if (isChecked) {
                                            {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BrandGold,
                                            selectedLabelColor = Color(0xFF1D1726),
                                            selectedLeadingIconColor = Color(0xFF1D1726),
                                            containerColor = Color(0xFF2A2234),
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        4 -> {
                            // --- ÉTAPE 4: RÉCAPITULATIF & MISE EN LIGNE ---
                            Text(
                                text = "Prêt à publier votre fiche !",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Vérifiez les informations ci-dessous. Dès validation, votre établissement sera immédiatement visible sur l'application et le portail web.",
                                fontSize = 13.sp,
                                color = Color(0xFFCCCCCC)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Preview Card
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF2A2234),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, BrandGold)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                text = establishmentName.ifBlank { "Mon Nouvel Établissement" },
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "$selectedCity • ${fullAddress.ifBlank { "Quartier Résidentiel" }}",
                                                fontSize = 12.sp,
                                                color = Color(0xFFA6A5A6)
                                            )
                                        }

                                        Surface(
                                            color = BrandGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, BrandGold)
                                        ) {
                                            Text(
                                                text = "⭐ NOUVEAU",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BrandGoldLight,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = BrandTaupe.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Catégorie", fontSize = 11.sp, color = Color(0xFFA6A5A6))
                                            Text(selectedService.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Column {
                                            Text("Prix / Nuitée", fontSize = 11.sp, color = Color(0xFFA6A5A6))
                                            Text("$${basePriceUSD.ifBlank { "100" }}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandGold)
                                        }
                                        Column {
                                            Text("Encaissement", fontSize = 11.sp, color = Color(0xFFA6A5A6))
                                            Text(paymentOperator, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF27AE60))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "Équipements déclarés :",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = selectedAmenities.joinToString(" • "),
                                        fontSize = 11.sp,
                                        color = Color(0xFFCCCCCC)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF1D1726),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BrandTaupe)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = BrandGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Compte administrateur lié à : ${currentUser.email}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFCCCCCC)
                                    )
                                }
                            }
                        }
                    }

                    // Error Toast Message if validation fails
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = Color(0xFFE74C3C).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE74C3C))
                        ) {
                            Text(
                                text = "⚠️ $errorMessage",
                                color = Color(0xFFFF7675),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

        // --- BOTTOM NAVIGATION ACTION BAR ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2A2234),
            border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            currentStep -= 1
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BrandTaupe)
                    ) {
                        Text("Précédent", color = Color.White)
                    }
                } else {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BrandTaupe)
                    ) {
                        Text("Annuler", color = Color(0xFFA6A5A6))
                    }
                }

                Button(
                    onClick = {
                        when (currentStep) {
                            1 -> {
                                currentStep = 2
                            }
                            2 -> {
                                if (establishmentName.isBlank()) {
                                    errorMessage = "Veuillez saisir le nom de votre établissement."
                                } else {
                                    errorMessage = null
                                    currentStep = 3
                                }
                            }
                            3 -> {
                                val price = basePriceUSD.toDoubleOrNull()
                                if (price == null || price <= 0) {
                                    errorMessage = "Veuillez indiquer un tarif valide en Dollar US."
                                } else {
                                    errorMessage = null
                                    currentStep = 4
                                }
                            }
                            4 -> {
                                // Final submission
                                isSubmitting = true
                                val newHotelId = "H_NEW_${System.currentTimeMillis()}"
                                val newHotel = Hotel(
                                    id = newHotelId,
                                    name = establishmentName.ifBlank { "Hôtel Prestige BookZzz" },
                                    description = establishmentDescription.ifBlank {
                                        "Établissement de qualité à $selectedCity avec commodités modernes, électricité garantie et sécurité."
                                    },
                                    city = selectedCity,
                                    basePricePerNight = basePriceUSD.toDoubleOrNull() ?: 120.0,
                                    rating = 5.0f,
                                    imageDescription = "Vue de l'établissement $establishmentName",
                                    colorAccentHex = "FFCCA865",
                                    phonePaymentNumber = paymentPhoneNumber,
                                    operatorName = paymentOperator
                                )

                                // Add to database & save
                                repository.updateHotels(repository.hotels.value + newHotel)
                                repository.saveUser(
                                    currentUser.copy(
                                        role = "HotelAdmin",
                                        registeredHotelName = newHotel.name
                                    )
                                )
                                isSubmitting = false
                                onOnboardingFinished()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentStep == 4) Color(0xFF27AE60) else BrandGold,
                        contentColor = Color(0xFF1D1726)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF1D1726),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (currentStep == 4) "Publier mon établissement 🚀" else "Étape suivante",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (currentStep < 4) {
                            Spacer(modifier = Modifier.width(6.dp))
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
