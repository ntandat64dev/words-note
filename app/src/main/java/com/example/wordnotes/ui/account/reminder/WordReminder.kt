package com.example.wordnotes.ui.account.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * This class is used to schedule reminders for words in the application.
 */
class WordReminder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderPreferences: ReminderPreferences
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(next: Boolean = false) {
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTriggerTime(next = next), getInterval(), getPendingIntent()!!)
        enableBootReceiver()
    }

    fun cancel() {
        getPendingIntent(forCanceling = true)?.let { alarmManager.cancel(it) }
        disableBootReceiver()
    }

    private fun enableBootReceiver() {
        val receiver = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    private fun disableBootReceiver() {
        val receiver = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
    }

    private fun getTriggerTime(valueIfError: Long = System.currentTimeMillis(), next: Boolean = false): Long {
        return try {
            val startTime = TimePickerPreference.Formatter.parse(reminderPreferences.getStartTime()!!)
            SystemClock.elapsedRealtime()
                .plus(ChronoUnit.MILLIS.between(LocalTime.now(), startTime))
                .plus(if (next) TimeUnit.DAYS.toMillis(1) else 0)
        } catch (_: Exception) {
            valueIfError
        }
    }

    private fun getInterval(valueIfError: Long = AlarmManager.INTERVAL_HOUR): Long {
        return try {
            val startTime = TimePickerPreference.Formatter.parse(reminderPreferences.getStartTime()!!)
            val endTime = TimePickerPreference.Formatter.parse(reminderPreferences.getEndTime()!!)
            val remindTimes = reminderPreferences.getRemindTimes()

            val interval = ChronoUnit.MILLIS.between(startTime, endTime) / remindTimes
            if (interval < 0) throw IllegalStateException() else interval
        } catch (_: Exception) {
            valueIfError
        }
    }

    private fun getPendingIntent(forCanceling: Boolean = false): PendingIntent? {
        val intent = Intent(context, RemindReceiver::class.java)
        val flags = if (forCanceling) PendingIntent.FLAG_NO_CREATE else PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    fun isTimeOut(): Boolean {
        val endTime = TimePickerPreference.Formatter.parse(reminderPreferences.getEndTime()!!)
        return LocalTime.now().isAfter(endTime)
    }
}