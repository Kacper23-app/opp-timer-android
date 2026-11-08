
package pl.opptimer.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : Activity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var gpsButton: Button
    private lateinit var timerText: TextView

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var timerStartedAt = 0L
    private var timerRunning = false

    private val timerUpdate = object : Runnable {
        override fun run() {
            if (!timerRunning) return

            val seconds = (SystemClock.elapsedRealtime() - timerStartedAt) / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60

            timerText.text = String.format(
                java.util.Locale.getDefault(),
                "%02d:%02d",
                minutes,
                remainingSeconds
            )

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        window.statusBarColor = Color.rgb(15, 23, 42)
        window.navigationBarColor = Color.rgb(15, 23, 42)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(15, 23, 42))
        }

        layout.setOnApplyWindowInsetsListener { view, insets ->
            val bars = if (android.os.Build.VERSION.SDK_INT >= 30) {
                insets.getInsets(WindowInsets.Type.systemBars())
            } else {
                @Suppress("DEPRECATION")
                val left = insets.systemWindowInsetLeft
                @Suppress("DEPRECATION")
                val top = insets.systemWindowInsetTop
                @Suppress("DEPRECATION")
                val right = insets.systemWindowInsetRight
                @Suppress("DEPRECATION")
                val bottom = insets.systemWindowInsetBottom
                android.graphics.Insets.of(left, top, right, bottom)
            }

            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val title = TextView(this).apply {
            text = "OPP TIMER 5.1"
            textSize = 23f
            setTextColor(Color.WHITE)
            setPadding(24, 16, 24, 12)
        }
        layout.addView(title)

        timerText = TextView(this).apply {
            text = "00:00"
            textSize = 42f
            setTextColor(Color.rgb(52, 211, 153))
            gravity = android.view.Gravity.CENTER
        }
        layout.addView(timerText)

        val timerButton = Button(this).apply {
            text = "START / STOP LICZNIKA"
            setOnClickListener {
                if (timerRunning) {
                    timerRunning = false
                    handler.removeCallbacks(timerUpdate)
                    text = "START LICZNIKA"
                } else {
                    timerStartedAt = SystemClock.elapsedRealtime()
                    timerRunning = true
                    timerText.text = "00:00"
                    text = "STOP LICZNIKA"
                    handler.post(timerUpdate)
                }
            }
        }
        layout.addView(timerButton)

        map = MapView(this).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(11.0)
            controller.setCenter(GeoPoint(52.2297, 21.0122))
            zoomController.setVisibility(
                CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
            )
        }

        layout.addView(
            map,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        locationOverlay = MyLocationNewOverlay(map)
        map.overlays.add(locationOverlay)

        gpsButton = Button(this).apply {
            text = "WŁĄCZ GPS"
            setOnClickListener { enableGps() }
        }
        layout.addView(gpsButton)

        val status = TextView(this).apply {
            text = "Tryb testowy — brak zweryfikowanych odcinków OPP"
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(12, 8, 12, 12)
        }
        layout.addView(status)

        setContentView(layout)
    }

    private fun enableGps() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()
            gpsButton.text = "GPS AKTYWNY"
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableGps()
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onDestroy() {
        timerRunning = false
        handler.removeCallbacks(timerUpdate)
        locationOverlay.disableMyLocation()
        map.onDetach()
        super.onDestroy()
    }
}
