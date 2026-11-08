package pl.opptimer.app

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.*
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.*
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale
import kotlin.math.ceil

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var map: MapView
    private lateinit var overlay: MyLocationNewOverlay
    private lateinit var clock: TextView
    private lateinit var speedView: TextView
    private lateinit var status: TextView
    private lateinit var startButton: Button
    private lateinit var gpsButton: Button
    private lateinit var distanceInput: EditText
    private lateinit var limitInput: EditText
    private val handler = Handler(Looper.getMainLooper())
    private var startedAt = 0L
    private var running = false
    private var requiredSeconds = 0L
    private var voice: TextToSpeech? = null
    private var voiceReady = false
    private var notified = false
    private var lastSpeed = -1f

    private val ticker = object : Runnable {
        override fun run() {
            if (!running) return
            val elapsed = (SystemClock.elapsedRealtime() - startedAt) / 1000
            val left = (requiredSeconds - elapsed).coerceAtLeast(0)
            clock.text = formatTime(left)
            status.text = if (left > 0) "TEST: pozostały czas orientacyjny • $elapsed s od startu" else
                "TEST: czas upłynął. To NIE oznacza końca odcinka OPP."
            if (left == 0L && !notified) {
                notified = true
                say("Obliczony czas minął. Nadal przestrzegaj ograniczenia prędkości.")
            }
            handler.postDelayed(this, 250L)
        }
    }

    private val gpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            lastSpeed = intent?.getFloatExtra("speed", -1f) ?: -1f
            showSpeed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        window.statusBarColor = Color.rgb(15, 23, 42)
        window.navigationBarColor = Color.rgb(15, 23, 42)
        val bg = Color.rgb(15, 23, 42)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        root.setOnApplyWindowInsetsListener { view, insets ->
            val i = if (Build.VERSION.SDK_INT >= 30) insets.getInsets(WindowInsets.Type.systemBars()) else {
                @Suppress("DEPRECATION")
                android.graphics.Insets.of(insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                    insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            view.setPadding(i.left, i.top, i.right, i.bottom)
            insets
        }
        fun label(text: String, size: Float, color: Int = Color.WHITE): TextView = TextView(this).apply {
            this.text = text; textSize = size; setTextColor(color); gravity = Gravity.CENTER
            setPadding(8, 5, 8, 5)
        }
        root.addView(label("OPP TIMER 6.0 • POLSKA", 22f))
        clock = label("00:00", 40f, Color.rgb(52, 211, 153))
        root.addView(clock)
        speedView = label("Prędkość GPS: — km/h", 17f)
        root.addView(speedView)
        val inputs = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        distanceInput = EditText(this).apply {
            hint = "Długość km"; inputType = 8194; setSingleLine(true)
            setText("10"); setTextColor(Color.WHITE); setHintTextColor(Color.LTGRAY)
        }
        limitInput = EditText(this).apply {
            hint = "Limit km/h"; inputType = 2; setSingleLine(true)
            setText("100"); setTextColor(Color.WHITE); setHintTextColor(Color.LTGRAY)
        }
        inputs.addView(distanceInput, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        inputs.addView(limitInput, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(inputs)
        startButton = Button(this).apply { text = "START TESTOWEGO ODLICZANIA"; setOnClickListener { toggleTimer() } }
        root.addView(startButton)
        map = MapView(this).apply {
            setTileSource(TileSourceFactory.MAPNIK); setMultiTouchControls(true)
            controller.setZoom(6.0); controller.setCenter(GeoPoint(52.1, 19.4))
        }
        root.addView(map, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        overlay = MyLocationNewOverlay(map)
        map.overlays.add(overlay)
        gpsButton = Button(this).apply { text = "WŁĄCZ GPS / MONITOROWANIE W TLE"; setOnClickListener { enableGps() } }
        root.addView(gpsButton)
        val stopGps = Button(this).apply { text = "ZATRZYMAJ MONITOROWANIE GPS"; setOnClickListener {
            stopService(Intent(this@MainActivity, TrackingService::class.java))
            overlay.disableMyLocation(); overlay.disableFollowLocation()
            gpsButton.text = "WŁĄCZ GPS / MONITOROWANIE W TLE"
            speedView.text = "Prędkość GPS: — km/h"
        } }
        root.addView(stopGps)
        val count = try { JSONArray(assets.open("opp_segments.json").bufferedReader().use { it.readText() }).length() } catch (_: Exception) { 0 }
        status = label(if (count == 0) "TRYB TESTOWY — brak zweryfikowanych odcinków OPP" else
            "W bazie $count odcinków • automatyczne wykrywanie jeszcze nieaktywne", 12f)
        root.addView(status)
        setContentView(root)
        voice = TextToSpeech(this, this)
    }

    private fun toggleTimer() {
        if (running) {
            running = false; handler.removeCallbacks(ticker)
            startButton.text = "START TESTOWEGO ODLICZANIA"
            status.text = "Odliczanie zatrzymane"
            return
        }
        val km = distanceInput.text.toString().replace(',', '.').toDoubleOrNull()
        val limit = limitInput.text.toString().toDoubleOrNull()
        if (km == null || limit == null || km <= 0.0 || limit <= 0.0 || km > 500 || limit > 200) {
            Toast.makeText(this, "Wpisz poprawną długość (0–500 km) i limit (0–200 km/h)", Toast.LENGTH_LONG).show()
            return
        }
        requiredSeconds = ceil(km / limit * 3600.0).toLong()
        startedAt = SystemClock.elapsedRealtime()
        notified = false; running = true
        startButton.text = "STOP ODLICZANIA"
        say("Rozpoczęto testowe odliczanie")
        handler.post(ticker)
    }

    private fun enableGps() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        overlay.enableMyLocation(); overlay.enableFollowLocation()
        try {
            startForegroundService(Intent(this, TrackingService::class.java))
            gpsButton.text = "GPS AKTYWNY"
            say("GPS aktywny")
        } catch (e: Exception) {
            Toast.makeText(this, "Nie udało się uruchomić GPS: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showSpeed() {
        speedView.text = if (lastSpeed >= 0) "Prędkość GPS: %.0f km/h".format(Locale.getDefault(), lastSpeed)
            else "Prędkość GPS: brak pomiaru"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) enableGps()
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) voiceReady = voice?.setLanguage(Locale("pl", "PL")) != TextToSpeech.LANG_MISSING_DATA
    }
    private fun say(message: String) { if (voiceReady) voice?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "opp_message") }
    private fun formatTime(seconds: Long): String = "%02d:%02d".format(Locale.getDefault(), seconds / 60, seconds % 60)

    override fun onResume() {
        super.onResume(); map.onResume()
        val filter = IntentFilter(TrackingService.ACTION_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(gpsReceiver, filter, RECEIVER_NOT_EXPORTED)
        else { @Suppress("DEPRECATION") registerReceiver(gpsReceiver, filter) }
        val pref = getSharedPreferences("gps_live", MODE_PRIVATE)
        if (SystemClock.elapsedRealtime() - pref.getLong("updated", 0) < 5000) {
            lastSpeed = pref.getFloat("speed", -1f); showSpeed()
        }
    }
    override fun onPause() {
        try { unregisterReceiver(gpsReceiver) } catch (_: Exception) { }
        map.onPause(); super.onPause()
    }
    override fun onDestroy() {
        running = false; handler.removeCallbacks(ticker)
        overlay.disableMyLocation(); map.onDetach()
        voice?.stop(); voice?.shutdown()
        super.onDestroy()
    }
}
