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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BookZzzRepository
import com.example.data.Hotel
import com.example.data.Room
import com.example.data.UserProfile
import com.example.shared.engine.PricingEngine
import com.example.shared.models.FurnishedApartment
import com.example.shared.models.RealEstateListing
import com.example.shared.models.SharedVehicle
import com.example.ui.theme.BrandAzure
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandGoldLight
import com.example.ui.theme.BrandTaupe
import kotlin.math.roundToInt

/**
 * Sprint 3: Gestion Complète du Catalogue & Hébergements (CRUD)
 * Couvre les 4 gammes de services :
 * 1. Chambres d'Hôtel & Tarifs
 * 2. Appartements & Maisons Meublés
 * 3. Baux Résidentiels & Immobilier
 * 4. Flotte de Véhicules & Navettes VIP
 */

@Composable
fun CatalogManagementScreen(
    repository: BookZzzRepository,
    currentUser: UserProfile?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hotels by repository.hotels.collectAsState()
    val rooms by repository.rooms.collectAsState()
    val apartments by repository.apartments.collectAsState()
    val realEstates by repository.realEstateListings.collectAsState()
    val vehicles by repository.vehicles.collectAsState()

    val hotelManagedName = currentUser?.registeredHotelName ?: "Goma Serena Hotel"
    val managedHotel = hotels.find { it.name.contains(hotelManagedName, ignoreCase = true) } ?: hotels.firstOrNull()
    val hotelRooms = rooms.filter { it.hotelId == (managedHotel?.id ?: "H1") }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Chambres & Tarifs", "Appartements Meublés", "Baux Résidentiels", "Flotte & Navettes", "Infos Établissement")

    // Dialog state handlers
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showAddApartmentDialog by remember { mutableStateOf(false) }
    var showAddRealEstateDialog by remember { mutableStateOf(false) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showEditHotelDialog by remember { mutableStateOf(false) }

    var editingRoom by remember { mutableStateOf<Room?>(null) }
    var editingApartment by remember { mutableStateOf<FurnishedApartment?>(null) }
    var editingRealEstate by remember { mutableStateOf<RealEstateListing?>(null) }
    var editingVehicle by remember { mutableStateOf<SharedVehicle?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D1726))
    ) {
        // --- 1. HEADER ---
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
                            text = "Gestion du Catalogue & Tarifs",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = managedHotel?.name ?: "Établissement Partenaire",
                            fontSize = 11.sp,
                            color = BrandGoldLight
                        )
                    }
                }

                Surface(
                    color = BrandGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BrandGold)
                ) {
                    Text(
                        text = "RDC CATALOGUE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGoldLight,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // --- 2. CATEGORY TABS ---
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF2A2234),
            contentColor = BrandGold,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BrandGold,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) BrandGoldLight else Color(0xFFCCCCCC)
                        )
                    }
                )
            }
        }

        // --- 3. TAB CONTENT ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> RoomsManagementTab(
                    hotel = managedHotel,
                    rooms = hotelRooms,
                    onAddRoomClick = { showAddRoomDialog = true },
                    onEditRoomClick = { editingRoom = it },
                    onToggleAvailability = { room ->
                        repository.updateRoom(room.copy(isAvailable = !room.isAvailable))
                        Toast.makeText(context, "Disponibilité mise à jour", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteRoom = { room ->
                        repository.deleteRoom(room.id)
                        Toast.makeText(context, "Chambre supprimée", Toast.LENGTH_SHORT).show()
                    }
                )

                1 -> ApartmentsManagementTab(
                    apartments = apartments,
                    onAddApartmentClick = { showAddApartmentDialog = true },
                    onEditApartmentClick = { editingApartment = it },
                    onToggleAvailability = { apt ->
                        repository.updateApartment(apt.copy(isAvailable = !apt.isAvailable))
                        Toast.makeText(context, "Statut de l'appartement mis à jour", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteApartment = { apt ->
                        repository.deleteApartment(apt.id)
                        Toast.makeText(context, "Appartement supprimé", Toast.LENGTH_SHORT).show()
                    }
                )

                2 -> RealEstateManagementTab(
                    listings = realEstates,
                    onAddListingClick = { showAddRealEstateDialog = true },
                    onEditListingClick = { editingRealEstate = it },
                    onTogglePublish = { item ->
                        repository.updateRealEstate(item.copy(isPublished = !item.isPublished))
                        Toast.makeText(context, "Publication mise à jour", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteListing = { item ->
                        repository.deleteRealEstate(item.id)
                        Toast.makeText(context, "Publication supprimée", Toast.LENGTH_SHORT).show()
                    }
                )

                3 -> VehiclesManagementTab(
                    vehicles = vehicles,
                    onAddVehicleClick = { showAddVehicleDialog = true },
                    onEditVehicleClick = { editingVehicle = it },
                    onToggleAvailability = { veh ->
                        repository.updateVehicle(veh.copy(isAvailable = !veh.isAvailable))
                        Toast.makeText(context, "Disponibilité du véhicule modifiée", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteVehicle = { veh ->
                        repository.deleteVehicle(veh.id)
                        Toast.makeText(context, "Véhicule retiré de la flotte", Toast.LENGTH_SHORT).show()
                    }
                )

                4 -> HotelSettingsTab(
                    hotel = managedHotel,
                    onEditClick = { showEditHotelDialog = true },
                    onToggleSuspension = {
                        managedHotel?.let {
                            repository.toggleHotelSuspension(it.id)
                            Toast.makeText(context, "Statut de l'hôtel modifié", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // --- MODALS / DIALOGS FOR CRUD ---

    // 1. Add / Edit Room Dialog
    if (showAddRoomDialog || editingRoom != null) {
        val isEditing = editingRoom != null
        var roomType by remember { mutableStateOf(editingRoom?.type ?: "Chambre Standard") }
        var roomPrice by remember { mutableStateOf(editingRoom?.pricePerNight?.roundToInt()?.toString() ?: (managedHotel?.basePricePerNight?.roundToInt()?.toString() ?: "150")) }

        Dialog(onDismissRequest = {
            showAddRoomDialog = false
            editingRoom = null
        }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isEditing) "Modifier la Chambre" else "Ajouter une Chambre",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = roomType,
                        onValueChange = { roomType = it },
                        label = { Text("Type de Chambre (ex: Suite Deluxe)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = roomPrice,
                        onValueChange = { roomPrice = it },
                        label = { Text("Tarif par nuitée (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedTextFieldColors()
                    )

                    val priceVal = roomPrice.toDoubleOrNull() ?: 0.0
                    if (priceVal > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "≈ ${PricingEngine.formatCDF(PricingEngine.convertUSDToCDF(priceVal))} par nuit",
                            fontSize = 11.sp,
                            color = BrandGoldLight
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showAddRoomDialog = false
                            editingRoom = null
                        }) {
                            Text("Annuler", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val price = roomPrice.toDoubleOrNull() ?: 100.0
                                val hId = managedHotel?.id ?: "H1"
                                if (isEditing) {
                                    repository.updateRoom(editingRoom!!.copy(type = roomType, pricePerNight = price))
                                    Toast.makeText(context, "Chambre modifiée", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newRoom = Room(
                                        id = "R_${hId}_${System.currentTimeMillis() % 10000}",
                                        hotelId = hId,
                                        type = roomType,
                                        pricePerNight = price,
                                        isAvailable = true
                                    )
                                    repository.addRoom(newRoom)
                                    Toast.makeText(context, "Chambre ajoutée au catalogue", Toast.LENGTH_SHORT).show()
                                }
                                showAddRoomDialog = false
                                editingRoom = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Enregistrer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 2. Add / Edit Furnished Apartment Dialog
    if (showAddApartmentDialog || editingApartment != null) {
        val isEditing = editingApartment != null
        var title by remember { mutableStateOf(editingApartment?.title ?: "") }
        var city by remember { mutableStateOf(editingApartment?.city ?: "Goma") }
        var address by remember { mutableStateOf(editingApartment?.address ?: "") }
        var priceNight by remember { mutableStateOf(editingApartment?.pricePerNight?.roundToInt()?.toString() ?: "80") }
        var priceMonth by remember { mutableStateOf(editingApartment?.pricePerMonth?.roundToInt()?.toString() ?: "1500") }
        var roomsCount by remember { mutableStateOf(editingApartment?.numberOfRooms?.toString() ?: "2") }
        var maxGuests by remember { mutableStateOf(editingApartment?.maxGuests?.toString() ?: "4") }
        var phone by remember { mutableStateOf(editingApartment?.ownerPhone ?: "+243 ") }

        Dialog(onDismissRequest = {
            showAddApartmentDialog = false
            editingApartment = null
        }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(
                            text = if (isEditing) "Modifier Appartement Meublé" else "Ajouter un Appartement Meublé",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Titre de l'appartement / maison d'hôte") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("Ville (ex: Goma)") },
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Quartier / Adresse") },
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = priceNight,
                                onValueChange = { priceNight = it },
                                label = { Text("Prix/Nuit (USD)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = priceMonth,
                                onValueChange = { priceMonth = it },
                                label = { Text("Prix/Mois (USD)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = roomsCount,
                                onValueChange = { roomsCount = it },
                                label = { Text("Nb Chambres") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = maxGuests,
                                onValueChange = { maxGuests = it },
                                label = { Text("Max Occupants") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Contact Téléphone Propriétaire") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                showAddApartmentDialog = false
                                editingApartment = null
                            }) {
                                Text("Annuler", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val apt = FurnishedApartment(
                                        id = editingApartment?.id ?: "APT-${System.currentTimeMillis() % 10000}",
                                        title = title.ifBlank { "Appartement Meublé $city" },
                                        description = "Meublé haut standing, eau et électricité 24/7, Wi-Fi inclus.",
                                        city = city,
                                        address = address,
                                        pricePerNight = priceNight.toDoubleOrNull() ?: 80.0,
                                        pricePerMonth = priceMonth.toDoubleOrNull(),
                                        numberOfRooms = roomsCount.toIntOrNull() ?: 2,
                                        numberOfBathrooms = 1,
                                        maxGuests = maxGuests.toIntOrNull() ?: 4,
                                        isWaterAndElectricity247 = true,
                                        ownerPhone = phone,
                                        isAvailable = true
                                    )
                                    if (isEditing) repository.updateApartment(apt) else repository.addApartment(apt)
                                    showAddApartmentDialog = false
                                    editingApartment = null
                                    Toast.makeText(context, "Appartement enregistré !", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Enregistrer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Add / Edit Real Estate Dialog
    if (showAddRealEstateDialog || editingRealEstate != null) {
        val isEditing = editingRealEstate != null
        var title by remember { mutableStateOf(editingRealEstate?.title ?: "") }
        var propType by remember { mutableStateOf(editingRealEstate?.propertyType ?: "Villa Résidentielle") }
        var city by remember { mutableStateOf(editingRealEstate?.city ?: "Goma") }
        var commune by remember { mutableStateOf(editingRealEstate?.commune ?: "Himbi") }
        var monthlyRent by remember { mutableStateOf(editingRealEstate?.monthlyRentUSD?.roundToInt()?.toString() ?: "800") }
        var cautionMonths by remember { mutableStateOf(editingRealEstate?.cautionMonths?.toString() ?: "3") }
        var agencyPhone by remember { mutableStateOf(editingRealEstate?.contactPhone ?: "+243 ") }

        Dialog(onDismissRequest = {
            showAddRealEstateDialog = false
            editingRealEstate = null
        }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(
                            text = if (isEditing) "Modifier le Bail Résidentiel" else "Publier une Location Longue Durée",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Titre de l'annonce (ex: Villa 4 Chambres)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = propType,
                                onValueChange = { propType = it },
                                label = { Text("Type de Bien") },
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("Ville") },
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = commune,
                                onValueChange = { commune = it },
                                label = { Text("Commune / Quartier") },
                                modifier = Modifier.weight(1.2f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = monthlyRent,
                                onValueChange = { monthlyRent = it },
                                label = { Text("Loyer/Mois ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cautionMonths,
                                onValueChange = { cautionMonths = it },
                                label = { Text("Mois de Garantie") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = agencyPhone,
                                onValueChange = { agencyPhone = it },
                                label = { Text("Tél Agence / Bailleur") },
                                modifier = Modifier.weight(1.5f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                showAddRealEstateDialog = false
                                editingRealEstate = null
                            }) {
                                Text("Annuler", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val listing = RealEstateListing(
                                        id = editingRealEstate?.id ?: "IMM-${System.currentTimeMillis() % 10000}",
                                        title = title.ifBlank { "$propType à louer à $city" },
                                        propertyType = propType,
                                        city = city,
                                        commune = commune,
                                        monthlyRentUSD = monthlyRent.toDoubleOrNull() ?: 500.0,
                                        cautionMonths = cautionMonths.toIntOrNull() ?: 3,
                                        advanceMonths = 1,
                                        description = "Propriété clôturée, compteur individuel, parking et eau courante.",
                                        agencyName = "BookZzz Immo Partenaire",
                                        contactPhone = agencyPhone,
                                        availableFromDate = "Immédiate",
                                        isPublished = true
                                    )
                                    if (isEditing) repository.updateRealEstate(listing) else repository.addRealEstate(listing)
                                    showAddRealEstateDialog = false
                                    editingRealEstate = null
                                    Toast.makeText(context, "Annonce immobilière enregistrée !", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Publier", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Add / Edit Vehicle Dialog
    if (showAddVehicleDialog || editingVehicle != null) {
        val isEditing = editingVehicle != null
        var modelName by remember { mutableStateOf(editingVehicle?.modelName ?: "Toyota Prado TXL") }
        var category by remember { mutableStateOf(editingVehicle?.category ?: "SUV 4x4 Luxe") }
        var seats by remember { mutableStateOf(editingVehicle?.seats?.toString() ?: "7") }
        var priceDay by remember { mutableStateOf(editingVehicle?.pricePerDayUSD?.roundToInt()?.toString() ?: "120") }
        var priceTransfer by remember { mutableStateOf(editingVehicle?.priceAirportTransferUSD?.roundToInt()?.toString() ?: "35") }
        var driverName by remember { mutableStateOf(editingVehicle?.driverName ?: "") }
        var driverPhone by remember { mutableStateOf(editingVehicle?.driverPhone ?: "+243 ") }

        Dialog(onDismissRequest = {
            showAddVehicleDialog = false
            editingVehicle = null
        }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(
                            text = if (isEditing) "Modifier Véhicule" else "Ajouter un Véhicule / Navette",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text("Modèle (ex: Toyota Land Cruiser)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Catégorie") },
                                modifier = Modifier.weight(1.2f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = seats,
                                onValueChange = { seats = it },
                                label = { Text("Places") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.8f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = priceDay,
                                onValueChange = { priceDay = it },
                                label = { Text("Tarif/Jour ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = priceTransfer,
                                onValueChange = { priceTransfer = it },
                                label = { Text("Navette Aéroport ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = driverName,
                                onValueChange = { driverName = it },
                                label = { Text("Nom Chauffeur") },
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = driverPhone,
                                onValueChange = { driverPhone = it },
                                label = { Text("Tél Chauffeur") },
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                showAddVehicleDialog = false
                                editingVehicle = null
                            }) {
                                Text("Annuler", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val veh = SharedVehicle(
                                        id = editingVehicle?.id ?: "VEH-${System.currentTimeMillis() % 10000}",
                                        modelName = modelName,
                                        category = category,
                                        seats = seats.toIntOrNull() ?: 5,
                                        pricePerDayUSD = priceDay.toDoubleOrNull() ?: 120.0,
                                        priceAirportTransferUSD = priceTransfer.toDoubleOrNull() ?: 35.0,
                                        hasAirConditioning = true,
                                        hasChauffeurIncluded = true,
                                        driverName = driverName,
                                        driverPhone = driverPhone,
                                        isAvailable = true
                                    )
                                    if (isEditing) repository.updateVehicle(veh) else repository.addVehicle(veh)
                                    showAddVehicleDialog = false
                                    editingVehicle = null
                                    Toast.makeText(context, "Véhicule mis à jour dans la flotte !", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Enregistrer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // 5. Edit Hotel Profile Dialog
    if (showEditHotelDialog && managedHotel != null) {
        var hName by remember { mutableStateOf(managedHotel.name) }
        var hDesc by remember { mutableStateOf(managedHotel.description) }
        var hBasePrice by remember { mutableStateOf(managedHotel.basePricePerNight.roundToInt().toString()) }
        var hPhone by remember { mutableStateOf(managedHotel.phonePaymentNumber) }
        var hOperator by remember { mutableStateOf(managedHotel.operatorName) }

        Dialog(onDismissRequest = { showEditHotelDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF2A2234),
                border = BorderStroke(1.dp, BrandGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Text(
                            text = "Paramètres de l'Établissement",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = hName,
                            onValueChange = { hName = it },
                            label = { Text("Nom de l'Hôtel") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = hDesc,
                            onValueChange = { hDesc = it },
                            label = { Text("Description & Standing") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = outlinedTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = hBasePrice,
                            onValueChange = { hBasePrice = it },
                            label = { Text("Prix de base standard (USD)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = outlinedTextFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = hPhone,
                                onValueChange = { hPhone = it },
                                label = { Text("N° Réception Paiement") },
                                modifier = Modifier.weight(1.3f),
                                colors = outlinedTextFieldColors()
                            )
                            OutlinedTextField(
                                value = hOperator,
                                onValueChange = { hOperator = it },
                                label = { Text("Opérateur") },
                                modifier = Modifier.weight(1f),
                                colors = outlinedTextFieldColors()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showEditHotelDialog = false }) {
                                Text("Annuler", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val updated = managedHotel.copy(
                                        name = hName,
                                        description = hDesc,
                                        basePricePerNight = hBasePrice.toDoubleOrNull() ?: managedHotel.basePricePerNight,
                                        phonePaymentNumber = hPhone,
                                        operatorName = hOperator
                                    )
                                    repository.updateHotel(updated)
                                    showEditHotelDialog = false
                                    Toast.makeText(context, "Informations de l'établissement enregistrées !", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Sauvegarder", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS FOR EACH CATALOGUE CATEGORY ---

@Composable
private fun RoomsManagementTab(
    hotel: Hotel?,
    rooms: List<Room>,
    onAddRoomClick: () -> Unit,
    onEditRoomClick: (Room) -> Unit,
    onToggleAvailability: (Room) -> Unit,
    onDeleteRoom: (Room) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Inventaire des Chambres (${rooms.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Gérez les tarifs par nuitée et les disponibilités",
                        fontSize = 11.sp,
                        color = Color(0xFFCCCCCC)
                    )
                }

                Button(
                    onClick = onAddRoomClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nouvelle Chambre", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(rooms, key = { it.id }) { room ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2A2234),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (room.isAvailable) BrandTaupe else Color(0xFFE74C3C).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1D1726),
                                border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.4f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Bed, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = room.type,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Réf: ${room.id}",
                                    fontSize = 10.sp,
                                    color = Color(0xFFA6A5A6)
                                )
                            }
                        }

                        // Availability chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (room.isAvailable) Color(0xFF27AE60).copy(alpha = 0.15f) else Color(0xFFE74C3C).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (room.isAvailable) Color(0xFF27AE60) else Color(0xFFE74C3C)),
                            modifier = Modifier.clickable { onToggleAvailability(room) }
                        ) {
                            Text(
                                text = if (room.isAvailable) "DISPONIBLE" else "OCCUPÉE / ARRÊT",
                                color = if (room.isAvailable) Color(0xFF2ECC71) else Color(0xFFFF7675),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BrandTaupe.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${room.pricePerNight.roundToInt()} USD / nuit",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandGold
                            )
                            Text(
                                text = "≈ ${PricingEngine.formatCDF(PricingEngine.convertUSDToCDF(room.pricePerNight))} CDF",
                                fontSize = 11.sp,
                                color = Color(0xFFCCCCCC)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { onEditRoomClick(room) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF1D1726), RoundedCornerShape(8.dp))
                                    .border(1.dp, BrandTaupe, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { onDeleteRoom(room) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFE74C3C).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE74C3C).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFFF7675), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApartmentsManagementTab(
    apartments: List<FurnishedApartment>,
    onAddApartmentClick: () -> Unit,
    onEditApartmentClick: (FurnishedApartment) -> Unit,
    onToggleAvailability: (FurnishedApartment) -> Unit,
    onDeleteApartment: (FurnishedApartment) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Appartements & Maisons Meublés (${apartments.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Court & moyen séjour avec services inclus",
                        fontSize = 11.sp,
                        color = Color(0xFFCCCCCC)
                    )
                }

                Button(
                    onClick = onAddApartmentClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter Meublé", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(apartments, key = { it.id }) { apt ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2A2234),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (apt.isAvailable) BrandTaupe else Color(0xFFE74C3C).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = apt.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${apt.city} • ${apt.address}",
                                fontSize = 11.sp,
                                color = BrandGoldLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (apt.isAvailable) Color(0xFF27AE60).copy(alpha = 0.15f) else Color(0xFFE74C3C).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (apt.isAvailable) Color(0xFF27AE60) else Color(0xFFE74C3C)),
                            modifier = Modifier.clickable { onToggleAvailability(apt) }
                        ) {
                            Text(
                                text = if (apt.isAvailable) "DISPONIBLE" else "LOUÉ / INDISPO",
                                color = if (apt.isAvailable) Color(0xFF2ECC71) else Color(0xFFFF7675),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "🛏️ ${apt.numberOfRooms} Chambres", fontSize = 11.sp, color = Color(0xFFCCCCCC))
                        Text(text = "👥 Jusqu'à ${apt.maxGuests} pers.", fontSize = 11.sp, color = Color(0xFFCCCCCC))
                        Text(text = "⚡ Eau & Élec 24/7", fontSize = 11.sp, color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold)
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
                                text = "${apt.pricePerNight.roundToInt()} USD / nuit" + (if (apt.pricePerMonth != null) " • ${apt.pricePerMonth.roundToInt()} $/mois" else ""),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandGold
                            )
                            Text(
                                text = "Contact : ${apt.ownerPhone}",
                                fontSize = 10.sp,
                                color = Color(0xFFA6A5A6)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { onEditApartmentClick(apt) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF1D1726), RoundedCornerShape(8.dp))
                                    .border(1.dp, BrandTaupe, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { onDeleteApartment(apt) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFE74C3C).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE74C3C).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFFF7675), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealEstateManagementTab(
    listings: List<RealEstateListing>,
    onAddListingClick: () -> Unit,
    onEditListingClick: (RealEstateListing) -> Unit,
    onTogglePublish: (RealEstateListing) -> Unit,
    onDeleteListing: (RealEstateListing) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Baux Résidentiels & Immobilier (${listings.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Publications de villas et appartements à louer",
                        fontSize = 11.sp,
                        color = Color(0xFFCCCCCC)
                    )
                }

                Button(
                    onClick = onAddListingClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nouveau Bail", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(listings, key = { it.id }) { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2A2234),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (item.isPublished) BrandTaupe else Color(0xFFE74C3C).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${item.propertyType} • ${item.commune}, ${item.city}",
                                fontSize = 11.sp,
                                color = BrandGoldLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (item.isPublished) Color(0xFF27AE60).copy(alpha = 0.15f) else Color(0xFFE67E22).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (item.isPublished) Color(0xFF27AE60) else Color(0xFFE67E22)),
                            modifier = Modifier.clickable { onTogglePublish(item) }
                        ) {
                            Text(
                                text = if (item.isPublished) "PUBLIÉ" else "BROUILLON",
                                color = if (item.isPublished) Color(0xFF2ECC71) else Color(0xFFF39C12),
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
                                text = "${item.monthlyRentUSD.roundToInt()} USD / mois",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandGold
                            )
                            Text(
                                text = "Garantie : ${item.cautionMonths} mois • Agence : ${item.agencyName}",
                                fontSize = 10.sp,
                                color = Color(0xFFCCCCCC)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { onEditListingClick(item) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF1D1726), RoundedCornerShape(8.dp))
                                    .border(1.dp, BrandTaupe, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { onDeleteListing(item) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFE74C3C).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE74C3C).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFFF7675), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiclesManagementTab(
    vehicles: List<SharedVehicle>,
    onAddVehicleClick: () -> Unit,
    onEditVehicleClick: (SharedVehicle) -> Unit,
    onToggleAvailability: (SharedVehicle) -> Unit,
    onDeleteVehicle: (SharedVehicle) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Flotte & Véhicules VIP (${vehicles.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Navettes aéroport et location journalière 4x4",
                        fontSize = 11.sp,
                        color = Color(0xFFCCCCCC)
                    )
                }

                Button(
                    onClick = onAddVehicleClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGold, contentColor = Color(0xFF1D1726)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter Véhicule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(vehicles, key = { it.id }) { veh ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2A2234),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (veh.isAvailable) BrandTaupe else Color(0xFFE74C3C).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                modifier = Modifier.size(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1D1726),
                                border = BorderStroke(1.dp, BrandGold.copy(alpha = 0.4f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = veh.modelName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${veh.category} • ${veh.seats} places",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA6A5A6)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (veh.isAvailable) Color(0xFF27AE60).copy(alpha = 0.15f) else Color(0xFFE74C3C).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (veh.isAvailable) Color(0xFF27AE60) else Color(0xFFE74C3C)),
                            modifier = Modifier.clickable { onToggleAvailability(veh) }
                        ) {
                            Text(
                                text = if (veh.isAvailable) "DISPONIBLE" else "EN COURSE / INDISPO",
                                color = if (veh.isAvailable) Color(0xFF2ECC71) else Color(0xFFFF7675),
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
                                text = "${veh.pricePerDayUSD.roundToInt()} USD / jour • Navette: ${veh.priceAirportTransferUSD.roundToInt()} $",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandGold
                            )
                            if (veh.driverName != null) {
                                Text(
                                    text = "Chauffeur : ${veh.driverName} (${veh.driverPhone})",
                                    fontSize = 10.sp,
                                    color = Color(0xFFCCCCCC)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { onEditVehicleClick(veh) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF1D1726), RoundedCornerShape(8.dp))
                                    .border(1.dp, BrandTaupe, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { onDeleteVehicle(veh) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFE74C3C).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE74C3C).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFFF7675), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HotelSettingsTab(
    hotel: Hotel?,
    onEditClick: () -> Unit,
    onToggleSuspension: () -> Unit
) {
    if (hotel == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2A2234),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BrandGold)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = hotel.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = BrandGold)
                        }
                    }

                    Text(
                        text = "${hotel.city} • Note: ${hotel.rating}/5.0",
                        fontSize = 12.sp,
                        color = BrandGoldLight
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = hotel.description,
                        fontSize = 12.sp,
                        color = Color(0xFFCCCCCC),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BrandTaupe.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "COORDONNÉES DE PAIEMENT OFFICIELLES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA6A5A6)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${hotel.operatorName} : ${hotel.phonePaymentNumber}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGold
                    )
                    Text(
                        text = "Numéro communiqué aux clients pour les dépôts M-Pesa / Airtel Money / Orange Money",
                        fontSize = 10.sp,
                        color = Color(0xFFCCCCCC)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Statut en Ligne", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = if (hotel.isSuspended) "Hôtel suspendu temporairement" else "Visible et réservable sur l'application",
                                fontSize = 10.sp,
                                color = if (hotel.isSuspended) Color(0xFFFF7675) else Color(0xFF2ECC71)
                            )
                        }

                        Switch(
                            checked = !hotel.isSuspended,
                            onCheckedChange = { onToggleSuspension() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF27AE60),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE74C3C)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
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
