package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookZzzRepository
import com.example.data.UserProfile
import com.example.ui.theme.BrandAzure
import com.example.ui.theme.BrandCaramel
import com.example.ui.theme.BrandEspresso
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGoldLight
import com.example.ui.theme.BrandTaupe

/**
 * Sprint 1: Page d'Accueil Site Vitrine (Grand Public)
 * Conçu avec une tonalité chaleureuse, des mots du langage public,
 * une rassurance complète sur les paiements locaux et un accès d'authentification Google
 * pour l'onboarding des partenaires / hôteliers.
 */

data class ShowcaseServiceItem(
    val id: String,
    val title: String,
    val badge: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val features: List<String>,
    val priceStartingFrom: String
)

@Composable
fun ShowcaseLandingScreen(
    repository: BookZzzRepository,
    onExploreClientClick: () -> Unit,
    onPartnerLoginSuccess: (UserProfile) -> Unit
) {
    var selectedCity by remember { mutableStateOf("Goma") }
    var selectedServiceCategory by remember { mutableStateOf("Hôtels") }
    var showGoogleLoginDialog by remember { mutableStateOf(false) }
    var isLoggingInWithGoogle by remember { mutableStateOf(false) }

    val cities = listOf("Goma", "Kinshasa", "Bukavu", "Lubumbashi")
    val services = listOf("Hôtels", "Appartements Meublés", "Maisons à Louer", "Navettes 4x4")

    val showcaseServices = listOf(
        ShowcaseServiceItem(
            id = "hotel",
            title = "Hôtels & Suites de Prestige",
            badge = "Court Séjour • 5 Étoiles",
            subtitle = "L'excellence du repos et du service hôtelier",
            description = "Profitez de chambres luxueuses, suites climatisées, piscines, restaurants gastronomiques et électricité garantie 24h/24 grâce à nos groupes autonomes.",
            icon = Icons.Default.Hotel,
            features = listOf("Électricité 24/7 garantie", "Wi-Fi Fibre Haut Débit", "Service d'étage & Petit-déjeuner", "Piscine & Spa"),
            priceStartingFrom = "À partir de 85 $ / nuit"
        ),
        ShowcaseServiceItem(
            id = "apartment",
            title = "Résidences & Appartements Meublés",
            badge = "Moyen Séjour • Tout Confort",
            subtitle = "Sentez-vous comme chez vous en toute liberté",
            description = "Idéal pour vos séjours en famille ou vos missions professionnelles. Appartements entièrement équipés avec cuisine moderne, salon spacieux et autonomie totale.",
            icon = Icons.Default.Apartment,
            features = listOf("Cuisine complète équipée", "Eau & Énergie en continu", "Nettoyage & Blanchisserie", "Sécurité gardée 24/7"),
            priceStartingFrom = "À partir de 60 $ / nuit"
        ),
        ShowcaseServiceItem(
            id = "rental",
            title = "Maisons & Baux Résidentiels",
            badge = "Longue Durée • Logements Vérifiés",
            subtitle = "Trouvez votre futur foyer en toute sérénité",
            description = "Consultez des villas et appartements à louer avec des conditions transparentes. Zéro mauvaise surprise sur les cautions et visites accompagnées.",
            icon = Icons.Default.House,
            features = listOf("Titres & Baux vérifiés", "Caution transparente", "Quartiers sécurisés", "Compteurs indépendants"),
            priceStartingFrom = "À partir de 350 $ / mois"
        ),
        ShowcaseServiceItem(
            id = "shuttle",
            title = "Transferts Aéroport & Véhicules VIP",
            badge = "Mobilité & Sécurité",
            subtitle = "Votre chauffeur vous attend dès l'atterrissage",
            description = "Flotte de 4x4 climatisés, SUV de luxe et minibus avec chauffeurs professionnels pour tous vos déplacements urbains et interurbains.",
            icon = Icons.Default.DirectionsCar,
            features = listOf("Accueil personnalisé avec pancarte", "Véhicules 4x4 tout terrain", "Chauffeurs expérimentés", "Tarif fixe sans surprise"),
            priceStartingFrom = "Transfert dès 25 $"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D1726)) // Luxury Dark Plum Background
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. TOP BAR LUXURY BRANDING ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.6f)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Hotel,
                            contentDescription = "BookZzz",
                            tint = BrandGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                                append("BOOK")
                            }
                            withStyle(SpanStyle(color = BrandGold, fontWeight = FontWeight.Black)) {
                                append("ZZZ")
                            }
                        },
                        fontSize = 20.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "L'art de bien dormir",
                        fontSize = 11.sp,
                        color = Color(0xFFCCCCCC),
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Partner Login Button (Opens Google Sign In modal)
            Button(
                onClick = { showGoogleLoginDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGold,
                    contentColor = Color(0xFF1D1726)
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Espace Partenaire",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- 2. HERO SECTION VITRINE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A2234),
                            Color(0xFF221C2B)
                        )
                    )
                )
                .border(1.dp, BrandTaupe.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = BrandGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "✨ La Référence du Séjour & Logement en RDC",
                        fontSize = 12.sp,
                        color = BrandGoldLight,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "L'art de bien dormir,\noù que vous soyez.",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Hôtels 5 étoiles, appartements meublés grand confort, résidences et navettes VIP. Réservez en toute sérénité avec vos moyens de paiement Mobile Money habituels.",
                    fontSize = 14.sp,
                    color = Color(0xFFCCCCCC),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Search Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1D1726),
                    border = BorderStroke(1.dp, BrandTaupe)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OÙ SOUHAITEZ-VOUS SÉJOURNER ?",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandGold,
                            letterSpacing = 0.8.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cities) { city ->
                                val isSelected = city == selectedCity
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCity = city },
                                    label = { Text(city, fontSize = 13.sp) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandGold,
                                        selectedLabelColor = Color(0xFF1D1726),
                                        selectedLeadingIconColor = Color(0xFF1D1726),
                                        containerColor = Color(0xFF2A2234),
                                        labelColor = Color.White,
                                        iconColor = BrandGold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onExploreClientClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandAzure,
                                contentColor = Color(0xFF1D1726)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Voir les établissements disponibles à $selectedCity",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 3. LES 3 SERVICES & MOBILITÉ EN VEDETTE ---
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "NOS PRESTATIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandGold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Une solution adaptée à chaque séjour",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            showcaseServices.forEach { service ->
                ShowcaseServiceCard(
                    service = service,
                    onBookClick = onExploreClientClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 4. RASSURANCE PAIEMENT MOBILE MONEY ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = Color(0xFF2A2234),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = BrandGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Paiement 100% Simple & Sécurisé",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Réglez vos réservations directement avec votre téléphone",
                            fontSize = 12.sp,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OperatorBadge(name = "M-Pesa", color = Color(0xFFE74C3C), note = "Vodacom RDC")
                    OperatorBadge(name = "Airtel Money", color = Color(0xFFE67E22), note = "Airtel RDC")
                    OperatorBadge(name = "Orange Money", color = Color(0xFFF39C12), note = "Orange RDC")
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "🔒 Vos paiements sont validés manuellement par la réception ou l'hôtelier avec transmission instantanée de votre reçu officiel.",
                    fontSize = 12.sp,
                    color = Color(0xFFA6A5A6),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 5. BANNIÈRE APPEL AUX PARTENAIRES & HÔTELIERS ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = Color(0xFF4F3F31), // Brand Espresso
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = BrandGold,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Vous gérez un hôtel ou un bien immobilier ?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Rejoignez le réseau BookZzz pour digitaliser vos réservations, éliminer les risques de double-réservation et recevoir vos paiements directement.",
                    fontSize = 13.sp,
                    color = Color(0xFFCCCCCC),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Prominent Google Login button for Partners
                Button(
                    onClick = { showGoogleLoginDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF221C2B)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    // Google icon simulation
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = Color(0xFF4285F4)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "G",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continuer avec Google (Espace Pro)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1D1726)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- 6. FOOTER VITRINE ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF16111D))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BOOKZZZ — L'art de bien dormir",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BrandGold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Goma • Kinshasa • Bukavu • Lubumbashi",
                fontSize = 12.sp,
                color = Color(0xFFA6A5A6)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "© 2026 BookZzz Technologies. Tous droits réservés.",
                fontSize = 11.sp,
                color = Color(0xFF7A7085)
            )
        }
    }

    // --- GOOGLE LOGIN SIMULATION MODAL ---
    if (showGoogleLoginDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                if (!isLoggingInWithGoogle) showGoogleLoginDialog = false
            },
            containerColor = Color(0xFF2A2234),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "G",
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4285F4),
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Connexion avec Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Connectez-vous à votre compte Google pour accéder à la gestion de vos établissements ou initialiser un nouveau service BookZzz.",
                        fontSize = 13.sp,
                        color = Color(0xFFCCCCCC),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1D1726),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandTaupe)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = BrandAzure
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("CA", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Christian Aganze",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "aganzec29@gmail.com",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA6A5A6)
                                )
                            }
                        }
                    }

                    if (isLoggingInWithGoogle) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = BrandGold,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Authentification sécurisée...",
                                fontSize = 12.sp,
                                color = BrandGoldLight
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isLoggingInWithGoogle = true
                        val partnerUser = UserProfile(
                            id = "U_PARTNER_1",
                            name = "Christian Aganze",
                            email = "aganzec29@gmail.com",
                            role = "HotelAdmin",
                            registeredHotelName = "Goma Serena Hotel"
                        )
                        repository.saveUser(partnerUser)
                        showGoogleLoginDialog = false
                        isLoggingInWithGoogle = false
                        onPartnerLoginSuccess(partnerUser)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGold,
                        contentColor = Color(0xFF1D1726)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continuer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showGoogleLoginDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BrandTaupe)
                ) {
                    Text("Annuler", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun ShowcaseServiceCard(
    service: ShowcaseServiceItem,
    onBookClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2A2234),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BrandTaupe.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1D1726),
                        border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.4f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = service.icon,
                                contentDescription = null,
                                tint = BrandGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = service.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            color = BrandCaramel.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, BrandCaramel.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = service.badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandGoldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = service.description,
                fontSize = 13.sp,
                color = Color(0xFFCCCCCC),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Key bullet points
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                service.features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF27AE60),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = Color(0xFFE0E0E0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BrandTaupe.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.priceStartingFrom,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGold
                )

                Button(
                    onClick = onBookClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandAzure,
                        contentColor = Color(0xFF1D1726)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Consulter", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OperatorBadge(name: String, color: Color, note: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1D1726),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = note,
                fontSize = 9.sp,
                color = Color(0xFFA6A5A6)
            )
        }
    }
}
