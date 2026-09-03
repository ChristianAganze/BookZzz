package com.example.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NotificationHelper {
    private const val CHANNEL_ID = "booking_reminders"
    private const val CHANNEL_NAME = "Rappels de Réservation"
    private const val CHANNEL_DESC = "Notifications de rappels de vos séjours"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun isFutureBooking(arrivalDateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val bookingDate = sdf.parse(arrivalDateStr) ?: return true
            
            // Simulation time around June 15, 2026
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2026)
                set(Calendar.MONTH, Calendar.JUNE)
                set(Calendar.DAY_OF_MONTH, 15)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val bookingCal = Calendar.getInstance().apply {
                time = bookingDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            !bookingCal.before(todayCal)
        } catch (e: Exception) {
            true
        }
    }

    fun scheduleReminder(context: Context, booking: Booking) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val arrivalDateUtc = sdf.parse(booking.arrivalDate) ?: return
            
            // Check-in standard at 14:00 (2:00 PM) on arrival day.
            // 24 hours before is 14:00 of the previous day, which equals arrival day midnight - 10 hours.
            val reminderTimeMs = arrivalDateUtc.time - 10 * 60 * 60 * 1000L

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

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val now = System.currentTimeMillis()
            if (reminderTimeMs > now) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMs, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMs, pendingIntent)
                }
                Log.d("NotificationHelper", "Scheduled reminder 24h before for ${booking.hotelName} on ${booking.arrivalDate}")
            } else {
                // For past simulation dates, check if it's upcoming in reality
                // For demonstration, if upcoming (e.g. tomorrow), trigger 5 seconds from now as a showcase reminder
                if (isFutureBooking(booking.arrivalDate)) {
                    val demoTriggerTime = now + 5000L // 5 seconds
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, demoTriggerTime, pendingIntent)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, demoTriggerTime, pendingIntent)
                    }
                    Log.d("NotificationHelper", "Scheduled showcase reminder in 5 seconds for ${booking.hotelName}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scheduleAllReminders(context: Context, bookings: List<Booking>) {
        bookings.forEach { booking ->
            scheduleReminder(context, booking)
        }
    }

    fun showNotification(context: Context, bookingId: String, hotelName: String, arrivalDate: String, roomType: String) {
        val prefManager = PreferencesManager(context)
        val notificationsEnabled = try {
            runBlocking { prefManager.notificationsEnabled.first() }
        } catch (e: Exception) {
            true
        }
        
        // Stop if user disabled notifications in Settings
        if (!notificationsEnabled) return

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            bookingId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val appLanguage = try {
            runBlocking { prefManager.language.first() }
        } catch (e: Exception) {
            "Français"
        }

        val title = when (appLanguage) {
            "English" -> "🔔 Hotel Stay Reminder"
            "Swahili" -> "🔔 Kikumbusho cha Uhifadhi"
            else -> "🔔 Rappel de Séjour d'Hôtel"
        }

        val message = when (appLanguage) {
            "English" -> "Your reservation at $hotelName ($roomType) begins tomorrow, $arrivalDate!"
            "Swahili" -> "Uhifadhi wako katika $hotelName ($roomType) unaanza kesho, $arrivalDate!"
            else -> "Votre reservation au $hotelName ($roomType) commence demain, le $arrivalDate !"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(bookingId.hashCode(), builder.build())
    }
}
