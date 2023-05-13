package net.c7j.wna.huawei

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.LatLng
import net.c7j.wna.huawei.maps.R

//::created by c7j at 10.05.2023 22:34
// Lite map shows static map image of a specified location at a specified zoom level to a user.
// It is useful when the user wants to show multiple maps on one screen or share location in chat.
// List of supported functions can be found here:
// https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-lite-mode-0000001061562041
class LiteMapActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMapView: MapView
    private var huaweiMap: HuaweiMap? = null

    /**
     * Important! Map sdk was already initialized at:
     * @see net.c7j.wna.huawei.MapsMainActivity.initializeMapSdk
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lite_map)

        val huaweiMapOptions = HuaweiMapOptions()
        huaweiMapOptions.liteMode(true)

        mMapView = MapView(this, huaweiMapOptions)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView.onCreate(mapViewBundle)
        mMapView.getMapAsync(this)
        (findViewById<ConstraintLayout>(R.id.mapHostLayout)).addView(mMapView, 0)
    }

    override fun onMapReady(huaweiMap: HuaweiMap?) {
        this.huaweiMap = huaweiMap
        huaweiMap?.isMyLocationEnabled = false
        huaweiMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(PARIS, 9.0f))
        huaweiMap?.setOnMapClickListener { toast("this is static lite map") }
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
        private val PARIS = LatLng(48.893478, 2.334595)
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }
}