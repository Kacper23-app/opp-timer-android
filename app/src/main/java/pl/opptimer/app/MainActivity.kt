
package pl.opptimer.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.ViewGroup
import android.graphics.Color
import android.content.Context
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MainActivity : Activity() {

    private lateinit var map: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(15, 23, 42))
        }

        val title = TextView(this).apply {
            text = "OPP TIMER 5.0"
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(24, 32, 24, 24)
        }
        layout.addView(title)

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

        val gpsButton = Button(this).apply {
            text = "Włącz GPS"
            setOnClickListener {
                if (checkSelfPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation()
                    text = "GPS aktywny"
                } else {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        100
                    )
                }
            }
        }
        layout.addView(gpsButton)

        val status = TextView(this).apply {
            text = "Mapa Warszawy — baza OPP do dodania"
            setTextColor(Color.WHITE)
            setPadding(24, 16, 24, 24)
        }
        layout.addView(status)

        setContentView(layout)
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
        locationOverlay.disableMyLocation()
        map.onDetach()
        super.onDestroy()
    }
}
