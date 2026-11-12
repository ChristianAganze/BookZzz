package com.example.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

object NotificationHelper {
    private const val CHANNEL_ID = "booking_reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Booking Reminders"
            val descriptionText = "Notifications for hotel booking reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(context: Context, booking: Booking) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BookingReminderReceiver::class.java).apply {
            putExtra("booking_id", booking.id)
            putExtra("hotel_name", booking.hotelName)
            putExtra("arrival_date", booking.arrivalDate)
            putExtra("room_type", booking.roomType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            booking.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse date "dd/MM/yyyy"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = try { sdf.parse(booking.arrivalDate) } catch (e: Exception) { null }
        
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
            // Schedule for the day before at 9:00 AM
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
        } else {
            return // Cannot schedule without a valid date
        }

        if (calendar.timeInMillis > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }

    fun scheduleAllReminders(context: Context, bookings: List<Booking>) {
        bookings.forEach { scheduleReminder(context, it) }
    }

    fun showNotification(
        context: Context,
        bookingId: String,
        hotelName: String,
        arrivalDate: String,
        roomType: String
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Rappel de Réservation")
            .setContentText("Votre séjour à $hotelName ($roomType) commence demain ($arrivalDate) !")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(bookingId.hashCode(), builder.build())
    }
}
