package com.example.attendance

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class YourNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        // Check if your focus mode is active
        if (isFocusModeActive()) {
            // Block the notification
            cancelNotification(sbn.key)
        }
    }

    private fun isFocusModeActive(): Boolean {
        val sharedPreferences = getSharedPreferences("FocusModePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("focusModeActive", false)
        // Implement your focus mode state check logic here
        // Return true if focus mode is active, false otherwise
    }
}
