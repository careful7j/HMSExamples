package net.c7j.wna.huawei

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.BitmapDescriptor
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.maps.model.animation.AlphaAnimation
import com.huawei.hms.maps.model.animation.Animation
import com.huawei.hms.maps.model.animation.AnimationSet
import com.huawei.hms.maps.model.animation.ScaleAnimation
import net.c7j.wna.huawei.maps.R
import kotlin.random.Random

// This activity shows how to use map markers, marker clusters and how to animate markers
class MarkerLocationMapActivity : BaseActivity(), OnMapReadyCallback {

    private var huaweiMap: HuaweiMap? = null
    private lateinit var mMapView: MapView

    private var mParis: Marker? = null
    private var isClusterAdded = false

    /**
     * Important! Map sdk was already initialized at:
     * @see net.c7j.wna.huawei.MapsMainActivity.initializeMapSdk
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marker_location_map)
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
        enableMyLocation()
        assignEventListeners()

        findViewById<MaterialButton>(R.id.btnAddMarker).setOnClickListener { addMarker() }
        findViewById<MaterialButton>(R.id.btnAddClusterMarkers).setOnClickListener { addClusterMarkers() }
        findViewById<MaterialButton>(R.id.btnCustomInfoWindow).setOnClickListener {
            // You can define a custom view as information window when marker is clicked
            huaweiMap?.setInfoWindowAdapter(CustomInfoWindowAdapter())
        }
    }

    private fun addMarker() {
        if (mParis != null) return
        mParis = huaweiMap?.addMarker(MarkerOptions()
            .position(PARIS)        // marker position on the map LatLng(latitude, longitude)
            .title("Paris")         // (optional) is displayed when you click on the marker
            .snippet("Hello")       // (optional) is displayed when you click on the marker
            .rotation(0.0f)         // (optional) rotate marker at some degree
            .visible(true)          // (optional) marker is visible by default (true)
            .zIndex(0.0f)           // (optional) priority defines which marker to draw above other markers (0.0f is default)
            .anchorMarker(0.0f,0.0f)// (optional) see: https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/markeroptions-0000001050150930#section95121330125810
            .alpha(1.0f)            // (optional) alpha channel (transparency): 0.0 - transparent market .. 1.0 - fully visible marker
            .flat(false)            // (optional) false (default) - directly faces the camera, true - is attached flatly on the map
            .infoWindowAnchor(1.0f, 1.0f)   // (optional) anchor point coordinates of the information window of a marker
            .draggable(true)        // (optional) can move this marker with your finger
            .clusterable(false))    // (optional) if you want many markers to be able to unite into cluster on zoom out

        // Marker animations example, not related to the marker above
        animateMarker(huaweiMap?.addMarker(MarkerOptions().position(PARIS)))
    }

    private fun animateMarker(marker: Marker?) {
        val alphaAnimation: Animation = AlphaAnimation(0.2f, 1.0f)
        alphaAnimation.repeatCount = 3
        alphaAnimation.duration = 1000L
        alphaAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart() {}
            override fun onAnimationEnd() {}
        })
        val scaleAnimation: Animation = ScaleAnimation(0f, 2f, 0f, 2f)
        scaleAnimation.repeatCount = 3
        scaleAnimation.duration = 1000L
        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart() {}
            override fun onAnimationEnd() {}
        })
        val animationSet = AnimationSet(true)
        animationSet.interpolator = LinearInterpolator()
        animationSet.addAnimation(alphaAnimation)
        animationSet.addAnimation(scaleAnimation)
        marker?.setAnimation(animationSet)
        marker?.startAnimation()
    }

    private fun addClusterMarkers() {
        if (isClusterAdded) return
        isClusterAdded = true

        // Supported methods of map marker customization:
        // fromAsset(assetName: String) - Bitmap image from the assets directory
        // fromBitmap(bitmap: Bitmap) - using just a Bitmap image
        // fromFile(fileName: String) - file from the internal storage
        // fromPath(path: String) - absolute path of a Bitmap image file
        // fromResource(resId: Int) - based on the resource of a Bitmap image
        // Example:
        // .icon(BitmapDescriptorFactory.fromResource(R.drawable.badge_ph)))
        // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-marker-0000001061779995#section6200183275411
        val icon = bitmapDescriptorFromVector(this, net.c7j.wna.huawei.box.R.mipmap.ic_launcher)
        for (i in 1..10) {
            val latLon = getRandomPositionNearby()
            huaweiMap?.addMarker(MarkerOptions()
                .position(latLon)      // marker position
                .title("Marker $i")    // is displayed when you click on the marker
                .snippet("${latLon.latitude}, ${latLon.longitude}")
                .icon(icon)            // custom icon to use as marker
                .clickable(true)       // clickable by default (true)
                .clusterable(true))    // if you want many markers to be able to unite into cluster on zoom out
        }
        animateCamera()

        huaweiMap?.setMarkersClustering(true)
        huaweiMap?.uiSettings?.setMarkerClusterTextColor(Color.WHITE)
        huaweiMap?.uiSettings?.setMarkerClusterIcon(    // sets custom icon for a cluster
            bitmapDescriptorFromVector(this, net.c7j.wna.huawei.box.R.mipmap.ic_launcher))
    }

    private fun getRandomPositionNearby(): LatLng = LatLng(
            PARIS.latitude + (Random.nextDouble() / 2) - 0.25,
            PARIS.longitude + (Random.nextDouble() / 2) - 0.25)


    private fun animateCamera() {
        huaweiMap?.animateCamera(CameraUpdateFactory.zoomBy(-1.0f))
        Handler(Looper.getMainLooper())
            .postDelayed( {
                huaweiMap?.animateCamera(CameraUpdateFactory.zoomBy(-1.0f))
            }, 500)

        Handler(Looper.getMainLooper())
            .postDelayed( {
                huaweiMap?.animateCamera(CameraUpdateFactory.zoomBy(-1.5f))
                toast("Please try +/- zoom camera")
            } , 1000)
    }

    private fun enableMyLocation() {
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            locationPermissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        } else huaweiMap?.isMyLocationEnabled = true
    }

    private val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        var count = 0
        if (map[Manifest.permission.ACCESS_FINE_LOCATION] == true) count++
        if (map[Manifest.permission.ACCESS_COARSE_LOCATION] == true) count++
        if (count < 2) {
            toast("Not all location permissions are granted, functions are limited")
        } else {
            huaweiMap?.isMyLocationEnabled = true
            toast("Location button enabled")
        }
    }

    private fun assignEventListeners() {
        huaweiMap?.setOnMarkerClickListener { marker ->
            toast("onMarkerClick:${marker.title}")
            false
        }

        huaweiMap?.setOnInfoWindowClickListener { marker ->
            toast("onInfoWindowClick:${marker.title}")
            val isInfoWindowShown: Boolean? = mParis?.isInfoWindowShown
            if (isInfoWindowShown != null && isInfoWindowShown) mParis?.hideInfoWindow()
        }

        huaweiMap?.setOnMyLocationButtonClickListener {
            toast("MyLocation button clicked")
            false
        }

        huaweiMap?.setOnMarkerDragListener(object : HuaweiMap.OnMarkerDragListener {

            override fun onMarkerDragStart(marker: Marker) {
                log( "onMarkerDragStart: ${marker.title}")
            }
            override fun onMarkerDrag(marker: Marker) {
                log( "onMarkerDrag: ${marker.title}")
            }
            override fun onMarkerDragEnd(marker: Marker) {
                log( "onMarkerDragEnd: ${marker.title}")
            }
        })
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

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth / 2, intrinsicHeight / 2)
            val bitmap = Bitmap.createBitmap(
                intrinsicWidth / 2, intrinsicHeight / 2, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    // You can define a custom view as information window when marker is clicked (see code lline 63)
    internal inner class CustomInfoWindowAdapter : HuaweiMap.InfoWindowAdapter {

        private val mWindow: View = layoutInflater.inflate(R.layout.custom_info_window, null)

        override fun getInfoWindow(marker: Marker): View {
            val windowTitle = mWindow.findViewById<TextView>(R.id.window_title)
            val windowSnippet = mWindow.findViewById<TextView>(R.id.window_snippet)
            windowTitle.text = "Paris"
            windowSnippet.text = "hello"
            return mWindow
        }

        override fun getInfoContents(marker: Marker): View? { return null }
    }


    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private val PARIS = LatLng(48.893478, 2.334595)
    }
}