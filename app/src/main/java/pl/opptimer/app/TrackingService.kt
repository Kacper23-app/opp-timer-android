package pl.opptimer.app

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*

/** GPS foreground service. Starts only on an explicit user action while app is visible. */
class TrackingService : Service(), LocationListener {
    private val channelId = "opp_tracking_6"
    private lateinit var manager: LocationManager

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(channelId, "OPP TIMER — GPS", NotificationManager.IMPORTANCE_LOW))
        manager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return START_NOT_STICKY
        }
        val stopIntent = Intent(this, TrackingService::class.java).apply { action = ACTION_STOP }
        val pending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("OPP TIMER 6.0 — GPS")
            .setContentText("Monitorowanie lokalizacji aktywne • brak zweryfikowanej bazy OPP")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Zatrzymaj GPS", pending).build())
            .build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 2f, this)
        } catch (_: SecurityException) { stopSelf() }
        return START_NOT_STICKY
    }

    override fun onLocationChanged(location: Location) {
        val speed = if (location.hasSpeed()) (location.speed * 3.6f).coerceAtLeast(0f) else -1f
        getSharedPreferences("gps_live", MODE_PRIVATE).edit()
            .putFloat("speed", speed)
            .putLong("updated", SystemClock.elapsedRealtime())
            .apply()
        sendBroadcast(Intent(ACTION_LOCATION).setPackage(packageName)
            .putExtra("speed", speed))
    }

    override fun onDestroy() {
        try { manager.removeUpdates(this) } catch (_: Exception) { }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "pl.opptimer.app.STOP_GPS"
        const val ACTION_LOCATION = "pl.opptimer.app.GPS_UPDATE"
    }
}
