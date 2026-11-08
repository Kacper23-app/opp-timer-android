
package pl.opptimer.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class TrackingService : Service() {

    private val channelId = "opp_tracking"

    override fun onCreate() {
        super.onCreate()

        val manager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "OPP TIMER — lokalizacja",
            NotificationManager.IMPORTANCE_LOW
        )

        manager.createNotificationChannel(channel)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val notification = Notification.Builder(
            this,
            channelId
        )
            .setContentTitle("OPP TIMER 5.0")
            .setContentText("Usługa przygotowana do monitorowania GPS")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
