package net.c7j.wna.huawei

import android.os.Bundle
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import net.c7j.wna.huawei.maps.R

//::created by c7j at 08.05.2023 11:55 PM
class SimpleMapActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMapView: MapView
    private var huaweiMap: HuaweiMap? = null

    /**
     * Important! Map sdk was already initialized at:
     * @see net.c7j.wna.huawei.MapsMainActivity.initializeMapSdk
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_map)

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
        huaweiMap?.isMyLocationEnabled = false
        setEventListeners()
    }

    private fun setEventListeners() {
        huaweiMap?.setOnMapClickListener { latLng -> toast("onMapClick: $latLng") }
        huaweiMap?.setOnMapLongClickListener { latLng -> toast("onMapLongClick: $latLng") }
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
    }
}