package net.c7j.wna.huawei

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.CameraPosition
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.LatLngBounds
import com.huawei.hms.maps.model.MapStyleOptions
import net.c7j.wna.huawei.maps.R

// This example shows how to apply style to a map and how to operate map camera (viewport)
class StyleAndCameraMapActivity : BaseActivity(),
    OnMapReadyCallback,
    HuaweiMap.OnCameraMoveStartedListener,
    HuaweiMap.OnCameraIdleListener,
    HuaweiMap.OnCameraMoveListener
{

    private lateinit var mMapView: MapView
    private var huaweiMap: HuaweiMap? = null
    private var tvCameraStatus: TextView? = null
    private var mapStyleId = 0

    /**
     * Important! Map sdk was already initialized at:
     * @see net.c7j.wna.huawei.MapsMainActivity.initializeMapSdk
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style_camera_map)

        tvCameraStatus = findViewById(R.id.tvCameraStatus)
        mMapView = findViewById(R.id.huawei_mapview)
        var mapViewBundle: Bundle? = null
        savedInstanceState?.let {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView.onCreate(mapViewBundle)
        mMapView.getMapAsync(this)
    }

    override fun onMapReady(huaweiMap: HuaweiMap?) {
        this.huaweiMap = huaweiMap
        huaweiMap?.mapType = HuaweiMap.MAP_TYPE_NORMAL
        enableUIGestures()
        enableCameraEventListeners()

        findViewById<MaterialButton>(R.id.btnApplyStyle).setOnClickListener { applyStyle() }
        findViewById<MaterialButton>(R.id.btnAnimateCamera).setOnClickListener { animateCamera() }
        findViewById<MaterialButton>(R.id.btnZoomCamera).setOnClickListener { zoomCamera() }
        findViewById<MaterialButton>(R.id.btnMoveCamera).setOnClickListener { moveCamera() }
        findViewById<MaterialButton>(R.id.btnCameraRegion).setOnClickListener { setCameraRegion() }
    }

    private fun enableUIGestures() {
        huaweiMap?.uiSettings?.let {
            it.isZoomControlsEnabled = true
            it.isMyLocationButtonEnabled = false

            it.isCompassEnabled = true
            it.isZoomGesturesEnabled = true
            it.isScrollGesturesEnabled = true
            it.isTiltGesturesEnabled = true
            it.isRotateGesturesEnabled = true
        }
        // changes map inbuilt UI (like zoom or location buttons) position
        huaweiMap?.setPadding(16, 16, 48, 16) // left, top, right, bottom
    }

    private fun enableCameraEventListeners() {
        huaweiMap?.setOnCameraMoveStartedListener(this)
        huaweiMap?.setOnCameraIdleListener(this)
        huaweiMap?.setOnCameraMoveListener(this)
        huaweiMap?.setOnMapLoadedCallback { log("onMapLoaded: successful") }
    }

    // Map style reference is here:
    // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-map-style-customization-reference-0000001063047122
    private fun applyStyle() {
        mapStyleId++
        mapStyleId %= 3
        val mapStyles = listOf(R.raw.mapstyle_grayscale_hms, R.raw.mapstyle_night_hms, R.raw.mapstyle_retro_hms)
        val style: MapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, mapStyles[mapStyleId])
        huaweiMap?.setMapStyle(style)
    }

    private fun animateCamera() {
        huaweiMap?.resetMinMaxZoomPreference()
        huaweiMap?.setMinZoomPreference(3f)
        huaweiMap?.setMaxZoomPreference(14f)

        val tilt = 4.2f
        val bearing = 60.5f
        val cameraPosition = CameraPosition(PARIS, 5f, tilt, bearing)
        // animateCamera() - makes smooth camera transition (while .moveCamera() makes it instant)
        huaweiMap?.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            1000,  // (optional) 1000 ms duration
            null   // "null" if you don't require cancellable callback
        )
    }

    private fun zoomCamera() {
        // moveCamera() - makes instant camera transition (while .animateCamera() makes it smooth)
        huaweiMap?.moveCamera(CameraUpdateFactory.zoomIn())      // equal to zoom "+"
        huaweiMap?.moveCamera(CameraUpdateFactory.zoomOut())     // equal to zoom "-"
        huaweiMap?.moveCamera(CameraUpdateFactory.zoomTo(8.0f))  // sets zoom level TO 8
        huaweiMap?.moveCamera(CameraUpdateFactory.zoomBy(-2.0f)) // reduce current zoom level BY 2
    }

    private fun moveCamera() {
        // moves camera to exact position and sets current zoom level to 4
        huaweiMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(BANGKOK, 4.0f))
        // moves camera at the same time retaining all other camera settings (i.n. zoom level)
        huaweiMap?.moveCamera(CameraUpdateFactory.newLatLng(BANGKOK))
        // moves (scrolls) camera by 100 pixel x axis, y axis
        huaweiMap?.moveCamera(CameraUpdateFactory.scrollBy(100.0f, 100.0f))
    }

    private fun setCameraRegion() {
        val padding = 100
        val latLng1 = LatLng(51.48, -0.05)
        val latLng2 = LatLng(51.58, 0.05)
        val latLngBounds = LatLngBounds(latLng1, latLng2)
        huaweiMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding))
    }

    @SuppressLint("SetTextI18n")
    override fun onCameraMoveStarted(reason: Int) {
        tvCameraStatus?.text = "onCameraMoveStarted()"
    }

    @SuppressLint("SetTextI18n")
    override fun onCameraIdle() {
        tvCameraStatus?.text = "onCameraIdle()"
    }

    @SuppressLint("SetTextI18n")
    override fun onCameraMove() {
        tvCameraStatus?.text = "onCameraMove()"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle: Bundle? = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mMapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onStart() {
        super.onStart()
        mMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }


    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private val PARIS = LatLng(48.893478, 2.334595)
        private val BANGKOK = LatLng(13.756, 100.533)
    }
}