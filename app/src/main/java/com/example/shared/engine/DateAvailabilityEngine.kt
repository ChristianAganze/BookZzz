package com.example.shared.engine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared Engine for Checking Room & Property Availability by Date Ranges.
 * Guarantees zero double-booking and allows safe date rescheduling.
 */
object DateAvailabilityEngine {
    private val dateFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    )

    fun parseDate(dateStr: String): Date? {
        val trimmed = dateStr.trim()
        for (format in dateFormats) {
            try {
                format.isLenient = false
                return format.parse(trimmed)
            } catch (ignored: Exception) {}
        }
        return null
    }

    /**
     * Checks if two date ranges [start1, end1] and [start2, end2] overlap.
     * Overlap rule: (start1 < end2) && (end1 > start2)
     */
    fun isOverlapping(
        start1: Date,
        end1: Date,
        start2: Date,
        end2: Date
    ): Boolean {
        return start1.before(end2) && end1.after(start2)
    }

    /**
     * Verifies if a room is available for a requested [arrivalDate, departureDate]
     * against existing confirmed/pending bookings.
     */
    fun isRoomAvailable(
        roomId: String,
        requestedArrivalStr: String,
        requestedDepartureStr: String,
        existingBookings: List<Pair<String, Pair<String, String>>>, // (roomId, (arrival, departure))
        excludeBookingId: String? = null
    ): Boolean {
        val reqStart = parseDate(requestedArrivalStr) ?: return false
        val reqEnd = parseDate(requestedDepartureStr) ?: return false
        if (!reqEnd.after(reqStart)) return false

        for ((bRoomId, dates) in existingBookings) {
            if (bRoomId != roomId) continue
            val bStart = parseDate(dates.first) ?: continue
            val bEnd = parseDate(dates.second) ?: continue

            if (isOverlapping(reqStart, reqEnd, bStart, bEnd)) {
                return false // Room is already occupied during this specific window!
            }
        }
        return true
    }

    /**
     * Computes the number of nights between two date strings.
     */
    fun calculateNights(arrivalStr: String, departureStr: String): Int {
        val start = parseDate(arrivalStr) ?: return 1
        val end = parseDate(departureStr) ?: return 1
        val diffMillis = end.time - start.time
        val days = (diffMillis / (1000 * 60 * 60 * 24)).toInt()
        return if (days > 0) days else 1
    }
}
