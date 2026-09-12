package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// --- Core Models ---

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: String, // "Client", "HotelAdmin", "SuperAdmin"
    val registeredHotelName: String? = null // For HotelAdmin
)

data class Hotel(
    val id: String,
    val name: String,
    val description: String,
    val city: String,
    val basePricePerNight: Double,
    val rating: Float,
    val imageDescription: String, // Description of the hotel layout (or simulated UI image color)
    val colorAccentHex: String, // Color representation
    val phonePaymentNumber: String,
    val operatorName: String, // "M-Pesa", "Airtel Money", "Orange Money"
    val isSuspended: Boolean = false
)

data class Room(
    val id: String,
    val hotelId: String,
    val type: String, // "Standard", "Deluxe", "Suite"
    val pricePerNight: Double,
    val isAvailable: Boolean = true
)

data class Booking(
    val id: String,
    val hotelId: String,
    val hotelName: String,
    val userId: String,
    val userName: String,
    val arrivalDate: String, // JJ/MM/AAAA
    val departureDate: String, // JJ/MM/AAAA
    val numNights: Int,
    val pricePerNight: Double,
    val totalAmount: Double,
    val status: String, // "En Attente", "Validé", "Refusé"
    val transactionId: String = "",
    val operatorSelected: String = "",
    val paymentDate: String = "",
    val screenshotName: String = "", // Used to refer to our sample receipts or user-picked
    val taxiRequested: Boolean = false,
    val taxiStatus: String? = null, // "Coordination du taxi...", "Taxi Confirmé", etc.
    val roomType: String = "Chambre Standard"
)

// --- Sample Data ---

object SampleData {
    val hotels = listOf(
        Hotel(
            id = "H1",
            name = "Goma Serena Hotel",
            description = "Hôtel 5 étoiles d'exception niché sur les rives du lac Kivu. Cadre calme et verdoyant, piscine extérieure chauffée, services de haut standing.",
            city = "Goma",
            basePricePerNight = 150.0,
            rating = 4.8f,
            imageDescription = "Vue panoramique sur le lac Kivu",
            colorAccentHex = "FF2C3E50",
            phonePaymentNumber = "+243 812 345 678",
            operatorName = "M-Pesa"
        ),
        Hotel(
            id = "H2",
            name = "Fleuve Congo Hotel",
            description = "Situé dans la commune résidentielle de la Gombe à Kinshasa, cet hôtel offre une vue imprenable sur le fleuve Congo et des prestations haut de gamme.",
            city = "Kinshasa",
            basePricePerNight = 220.0,
            rating = 4.7f,
            imageDescription = "Reflets sur le fleuve Congo",
            colorAccentHex = "FF1C3A5F",
            phonePaymentNumber = "+243 998 765 432",
            operatorName = "Airtel Money"
        ),
        Hotel(
            id = "H3",
            name = "Orchids Safari Club",
            description = "Une oasis florale au bord du lac Kivu à Bukavu. Parfait pour les voyages de détente ou les expéditions vers le parc national de Kahuzi-Biega.",
            city = "Bukavu",
            basePricePerNight = 110.0,
            rating = 4.6f,
            imageDescription = "Jardin d'orchidées tropicales",
            colorAccentHex = "FF2E7D32",
            phonePaymentNumber = "+243 898 123 456",
            operatorName = "Orange Money"
        ),
        Hotel(
            id = "H4",
            name = "Hôtel Karibu",
            description = "Un havre de paix traditionnel à Goma avec des bungalows chaleureux en bois, entourés d'un somptueux parc de plusieurs hectares.",
            city = "Goma",
            basePricePerNight = 85.0,
            rating = 4.3f,
            imageDescription = "Chalet de charme et jardins",
            colorAccentHex = "FFD35400",
            phonePaymentNumber = "+243 811 222 333",
            operatorName = "M-Pesa"
        )
    )

    val sampleBookings = listOf(
        Booking(
            id = "BK-8941",
            hotelId = "H1",
            hotelName = "Goma Serena Hotel",
            userId = "U2",
            userName = "Patrick Lumumba",
            arrivalDate = "15/09/2026",
            departureDate = "18/09/2026",
            numNights = 3,
            pricePerNight = 150.0,
            totalAmount = 450.0,
            status = "En Attente",
            transactionId = "MP260912.8942.A1",
            operatorSelected = "M-Pesa",
            paymentDate = "12/09/2026 à 14:20",
            screenshotName = "recu_mpesa_450usd.png",
            roomType = "Chambre Standard"
        ),
        Booking(
            id = "BK-8935",
            hotelId = "H1",
            hotelName = "Goma Serena Hotel",
            userId = "U3",
            userName = "Nathalie Kanyere",
            arrivalDate = "20/09/2026",
            departureDate = "22/09/2026",
            numNights = 2,
            pricePerNight = 225.0,
            totalAmount = 450.0,
            status = "En Attente",
            transactionId = "AIRTEL-TX-99214",
            operatorSelected = "Airtel Money",
            paymentDate = "12/09/2026 à 11:05",
            screenshotName = "recu_airtel_450usd.png",
            roomType = "Suite Deluxe"
        ),
        Booking(
            id = "BK-8910",
            hotelId = "H1",
            hotelName = "Goma Serena Hotel",
            userId = "U4",
            userName = "Michel Kasongo",
            arrivalDate = "10/09/2026",
            departureDate = "12/09/2026",
            numNights = 2,
            pricePerNight = 150.0,
            totalAmount = 300.0,
            status = "Validé",
            transactionId = "OM-RDC-77401",
            operatorSelected = "Orange Money",
            paymentDate = "09/09/2026 à 16:30",
            screenshotName = "recu_orange_300usd.png",
            roomType = "Chambre Standard"
        )
    )

    fun createRoomsForHotel(hotelId: String, basePrice: Double): List<Room> {
        return listOf(
            Room("R_${hotelId}_1", hotelId, "Chambre Standard", basePrice, true),
            Room("R_${hotelId}_2", hotelId, "Suite Deluxe", basePrice * 1.5, true),
            Room("R_${hotelId}_3", hotelId, "Suite Présidentielle", basePrice * 2.5, true)
        )
    }
}

// --- Local Repository with Persistence ---

class BookZzzRepository(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("bookzzz_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val hotelListAdapter = moshi.adapter<List<Hotel>>(Types.newParameterizedType(List::class.java, Hotel::class.java))
    private val bookingListAdapter = moshi.adapter<List<Booking>>(Types.newParameterizedType(List::class.java, Booking::class.java))
    private val userAdapter = moshi.adapter(UserProfile::class.java)
    private val roomListAdapter = moshi.adapter<List<Room>>(Types.newParameterizedType(List::class.java, Room::class.java))

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _hotels = MutableStateFlow<List<Hotel>>(emptyList())
    val hotels: StateFlow<List<Hotel>> = _hotels.asStateFlow()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val userJson = sharedPrefs.getString("current_user", null)
        _currentUser.value = if (userJson != null) userAdapter.fromJson(userJson) else UserProfile("U1", "Christian Aganze", "aganzec29@gmail.com", "Client")

        val hotelsJson = sharedPrefs.getString("hotels_list", null)
        _hotels.value = if (hotelsJson != null) hotelListAdapter.fromJson(hotelsJson) ?: SampleData.hotels else SampleData.hotels

        val bookingsJson = sharedPrefs.getString("bookings_list", null)
        _bookings.value = if (bookingsJson != null) bookingListAdapter.fromJson(bookingsJson) ?: SampleData.sampleBookings else SampleData.sampleBookings
        
        val roomsJson = sharedPrefs.getString("rooms_list", null)
        _rooms.value = if (roomsJson != null) roomListAdapter.fromJson(roomsJson) ?: emptyList() else SampleData.hotels.flatMap { SampleData.createRoomsForHotel(it.id, it.basePricePerNight) }
    }

    fun saveUser(user: UserProfile) {
        _currentUser.value = user
        sharedPrefs.edit().putString("current_user", userAdapter.toJson(user)).apply()
    }

    fun logout() {
        _currentUser.value = null
        sharedPrefs.edit().remove("current_user").apply()
    }

    fun updateHotels(updatedList: List<Hotel>) {
        _hotels.value = updatedList
        sharedPrefs.edit().putString("hotels_list", hotelListAdapter.toJson(updatedList)).apply()
    }
    
    fun updateRoom(room: Room) {
        val updated = _rooms.value.map { if (it.id == room.id) room else it }
        _rooms.value = updated
        sharedPrefs.edit().putString("rooms_list", roomListAdapter.toJson(updated)).apply()
    }

    fun toggleHotelSuspension(hotelId: String) {
        val updated = _hotels.value.map { if (it.id == hotelId) it.copy(isSuspended = !it.isSuspended) else it }
        updateHotels(updated)
    }

    fun updateHotelPrice(hotelId: String, newPrice: Double) {
        val updated = _hotels.value.map { if (it.id == hotelId) it.copy(basePricePerNight = newPrice) else it }
        updateHotels(updated)
    }

    fun addBooking(booking: Booking) {
        val updated = _bookings.value + booking
        _bookings.value = updated
        sharedPrefs.edit().putString("bookings_list", bookingListAdapter.toJson(updated)).apply()
        NotificationHelper.scheduleReminder(context, booking)
    }

    fun updateBookingStatus(bookingId: String, newStatus: String) {
        val updated = _bookings.value.map { if (it.id == bookingId) it.copy(status = newStatus) else it }
        _bookings.value = updated
        sharedPrefs.edit().putString("bookings_list", bookingListAdapter.toJson(updated)).apply()
    }
    
    fun requestTaxi(bookingId: String) {
        val updated = _bookings.value.map { if (it.id == bookingId) it.copy(taxiRequested = true, taxiStatus = "Coordination d'un taxi local en cours...") else it }
        _bookings.value = updated
        sharedPrefs.edit().putString("bookings_list", bookingListAdapter.toJson(updated)).apply()
    }

    fun updateTaxiStatus(bookingId: String, status: String) {
        val updated = _bookings.value.map { if (it.id == bookingId) it.copy(taxiStatus = status) else it }
        _bookings.value = updated
        sharedPrefs.edit().putString("bookings_list", bookingListAdapter.toJson(updated)).apply()
    }

    fun resetAllData() {
        sharedPrefs.edit().clear().apply()
        _currentUser.value = UserProfile("U1", "Christian Aganze", "aganzec29@gmail.com", "Client")
        _hotels.value = SampleData.hotels
        _bookings.value = emptyList()
        _rooms.value = SampleData.hotels.flatMap { SampleData.createRoomsForHotel(it.id, it.basePricePerNight) }
    }
}
