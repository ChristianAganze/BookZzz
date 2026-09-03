package com.example.network

import android.content.Context
import android.util.Log
import com.example.data.Booking
import com.example.data.BookZzzRepository
import com.example.data.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseClient {
    private const val TAG = "FirebaseClient"

    var isFirebaseAvailable: Boolean = false
        private set

    init {
        try {
            // Check if Firebase is initialized. It will throw an exception if google-services.json is missing or not configured.
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            isFirebaseAvailable = true
            Log.i(TAG, "Firebase SDKs are successfully initialized and active!")
        } catch (e: Throwable) {
            isFirebaseAvailable = false
            Log.w(TAG, "Firebase is not initialized (likely missing google-services.json). Using Local Secure Cache fallback: ${e.message}")
        }
    }

    /**
     * Sign in user. If Firebase is active, we can sign in via Firebase, and also save to Firestore.
     * Otherwise we do our robust offline sign in simulation.
     */
    fun performLogin(
        email: String,
        name: String,
        context: Context,
        repository: BookZzzRepository,
        onSuccess: (UserProfile) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Advanced Role Detection Simulation
        val detectedRole = when {
            email.contains("superadmin", ignoreCase = true) || email == "aganzec29@gmail.com" -> "SuperAdmin"
            email.contains("hotel", ignoreCase = true) || email.contains("manager", ignoreCase = true) -> "HotelAdmin"
            else -> "Client"
        }
        
        val detectedHotel = if (detectedRole == "HotelAdmin") {
            if (email.contains("serena", ignoreCase = true)) "Goma Serena Hotel" else "Fleuve Congo Hotel"
        } else null

        val user = UserProfile(
            id = "U_${System.currentTimeMillis()}",
            name = name,
            email = email,
            role = detectedRole,
            registeredHotelName = detectedHotel
        )

        if (isFirebaseAvailable) {
            try {
                val db = FirebaseFirestore.getInstance()
                // Save profile to Firestore
                db.collection("users")
                    .document(user.id)
                    .set(
                        mapOf(
                            "id" to user.id,
                            "name" to user.name,
                            "email" to user.email,
                            "role" to user.role,
                            "registeredHotelName" to user.registeredHotelName
                        )
                    )
                    .addOnSuccessListener {
                        Log.i(TAG, "User profile successfully saved to Firestore.")
                        repository.saveUser(user)
                        onSuccess(user)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Failed to save profile to Firestore, falling back to local storage.", e)
                        repository.saveUser(user)
                        onSuccess(user)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase error during login, falling back to local storage.", e)
                repository.saveUser(user)
                onSuccess(user)
            }
        } else {
            // Offline local mode
            repository.saveUser(user)
            onSuccess(user)
        }
    }

    /**
     * Synchronize bookings with Firestore if available, otherwise read/write locally
     */
    fun syncBookingToFirestore(booking: Booking) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("bookings")
                .document(booking.id)
                .set(
                    mapOf(
                        "id" to booking.id,
                        "hotelId" to booking.hotelId,
                        "hotelName" to booking.hotelName,
                        "userId" to booking.userId,
                        "userName" to booking.userName,
                        "arrivalDate" to booking.arrivalDate,
                        "departureDate" to booking.departureDate,
                        "numNights" to booking.numNights,
                        "pricePerNight" to booking.pricePerNight,
                        "totalAmount" to booking.totalAmount,
                        "status" to booking.status,
                        "transactionId" to booking.transactionId,
                        "operatorSelected" to booking.operatorSelected,
                        "paymentDate" to booking.paymentDate,
                        "screenshotName" to booking.screenshotName,
                        "taxiRequested" to booking.taxiRequested,
                        "taxiStatus" to booking.taxiStatus
                    )
                )
                .addOnSuccessListener {
                    Log.i(TAG, "Booking saved to Firestore successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save booking to Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore sync exception: ${e.message}")
        }
    }

    /**
     * Live listener simulation - if Firestore was active we would add a real-time snapshot listener,
     * but since offline capability is critical in RDC, we elegantly sync and handle updates locally as well.
     */
    fun updateBookingStatusInFirestore(bookingId: String, status: String) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("bookings")
                .document(bookingId)
                .update("status", status)
                .addOnSuccessListener {
                    Log.i(TAG, "Booking status updated to $status in Firestore.")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore booking update failed: ${e.message}")
        }
    }
}
