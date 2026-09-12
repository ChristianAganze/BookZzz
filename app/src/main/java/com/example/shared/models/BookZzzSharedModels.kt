package com.example.shared.models

/**
 * Shared Data Models for BookZzz (Mobile Android Client & Web Admin React)
 * Covers the 3 Core Services:
 * 1. Hotel & Room Bookings (Short stay)
 * 2. Furnished Apartments & Guest Houses (Medium stay)
 * 3. Residential Rentals & Real Estate Publications (Long stay)
 * + Vehicle Fleet & Shuttle Transfers
 */

enum class ServiceCategory {
    HOTEL_ROOMS,          // Service 1: Hôtels & Chambres
    FURNISHED_APARTMENTS, // Service 2: Appartements & Maisons d'Hôtes
    REAL_ESTATE_RENTALS,  // Service 3: Publications de Maisons/Appartements à louer
    VEHICLE_SHUTTLE       // Service 4: Navettes & Location Véhicules
}

enum class BookingStatus {
    PENDING_PAYMENT, // En attente de validation Mobile Money
    CONFIRMED,       // Validé par l'hôtelier / propriétaire
    CHECKED_IN,      // Client actuellement en séjour
    COMPLETED,       // Séjour terminé
    RESCHEDULED,     // Dates modifiées
    CANCELLED,       // Annulé
    REJECTED         // Rejeté (preuve invalide)
}

enum class MobileMoneyOperator {
    MPESA,        // Vodacom M-Pesa
    AIRTEL_MONEY, // Airtel Money
    ORANGE_MONEY  // Orange Money
}

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val city: String,
    val commune: String? = null
)

// --- SERVICE 1: HÔTELLERIE ---

data class SharedHotel(
    val id: String,
    val name: String,
    val description: String,
    val city: String,
    val address: String = "",
    val basePricePerNight: Double,
    val rating: Float,
    val phonePaymentNumber: String,
    val operatorName: String,
    val coordinates: GeoCoordinates? = null,
    val amenities: List<String> = listOf("Wi-Fi", "Piscine", "Restaurant", "Groupe 24/7", "Navette"),
    val images: List<String> = emptyList(),
    val isSuspended: Boolean = false,
    val ownerId: String = ""
)

data class SharedRoom(
    val id: String,
    val hotelId: String,
    val roomNumber: String = "",
    val type: String, // "Standard", "Deluxe", "Suite", "Suite Présidentielle"
    val pricePerNight: Double,
    val capacityAdults: Int = 2,
    val capacityChildren: Int = 1,
    val amenities: List<String> = listOf("Climatisation", "Wi-Fi Gratuit", "Douche Italienne", "Smart TV"),
    val isAvailableByDefault: Boolean = true
)

// --- SERVICE 2: APPARTEMENTS & MAISONS D'HÔTES (Court/Moyen séjour) ---

data class FurnishedApartment(
    val id: String,
    val title: String,
    val description: String,
    val city: String,
    val address: String,
    val pricePerNight: Double,
    val pricePerMonth: Double? = null,
    val numberOfRooms: Int,
    val numberOfBathrooms: Int,
    val maxGuests: Int,
    val isWaterAndElectricity247: Boolean = true,
    val images: List<String> = emptyList(),
    val ownerPhone: String,
    val isAvailable: Boolean = true
)

// --- SERVICE 3: PUBLICATION IMMOBILIÈRE (Location Longue Durée) ---

data class RealEstateListing(
    val id: String,
    val title: String,
    val propertyType: String, // "Villa", "Appartement 3 pièces", "Maison basse", "Studio"
    val city: String,
    val commune: String,
    val monthlyRentUSD: Double,
    val cautionMonths: Int = 3, // Nombre de mois de garantie locative
    val advanceMonths: Int = 1, // Mois de loyer anticipé
    val description: String,
    val features: List<String> = listOf("Cour clôturée", "Citerne d'eau", "Compteur prépayé SNEL", "Gardiennage"),
    val agencyName: String,
    val contactPhone: String,
    val availableFromDate: String, // JJ/MM/AAAA
    val isPublished: Boolean = true
)

// --- SERVICE 4: FLOTTE DE VÉHICULES & NAVETTES ---

data class SharedVehicle(
    val id: String,
    val modelName: String, // ex: "Toyota Prado TXL", "Land Cruiser V8", "Coaster VIP"
    val category: String, // "SUV 4x4", "Berline", "Minibus"
    val seats: Int = 5,
    val pricePerDayUSD: Double = 120.0,
    val priceAirportTransferUSD: Double = 35.0,
    val hasAirConditioning: Boolean = true,
    val hasChauffeurIncluded: Boolean = true,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val isAvailable: Boolean = true
)

// --- RÉSERVATION AVEC HISTORIQUE DE DATES & GESTION DES MODIFICATIONS ---

data class DateShiftRecord(
    val previousArrival: String,
    val previousDeparture: String,
    val newArrival: String,
    val newDeparture: String,
    val shiftedAt: String,
    val reason: String = "Modification demandée par le client"
)

data class SharedBooking(
    val id: String,
    val serviceCategory: ServiceCategory = ServiceCategory.HOTEL_ROOMS,
    val targetItemId: String, // HotelId, ApartmentId, ou VehicleId
    val targetItemName: String,
    val roomId: String? = null,
    val roomType: String = "Chambre Standard",
    val userId: String,
    val userName: String,
    val userPhone: String = "",
    val arrivalDate: String,   // Format: AAAA-MM-JJ ou JJ/MM/AAAA
    val departureDate: String, // Format: AAAA-MM-JJ ou JJ/MM/AAAA
    val numNights: Int,
    val pricePerNight: Double,
    val totalAmount: Double,
    val currency: String = "USD",
    val status: BookingStatus = BookingStatus.PENDING_PAYMENT,
    val transactionId: String = "",
    val operatorSelected: String = "",
    val paymentDate: String = "",
    val dateChangeHistory: List<DateShiftRecord> = emptyList(),
    val taxiRequested: Boolean = false,
    val createdAt: String = ""
)
