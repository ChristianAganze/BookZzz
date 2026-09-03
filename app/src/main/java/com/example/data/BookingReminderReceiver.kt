package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BookingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val bookingId = intent.getStringExtra("booking_id") ?: ""
        val hotelName = intent.getStringExtra("hotel_name") ?: ""
        val arrivalDate = intent.getStringExtra("arrival_date") ?: ""
        val roomType = intent.getStringExtra("room_type") ?: ""

        Log.d("BookingReminderReceiver", "Alarm received for hotel booking reminder of $hotelName")
        if (bookingId.isNotEmpty()) {
            NotificationHelper.showNotification(context, bookingId, hotelName, arrivalDate, roomType)
        }
    }
}
