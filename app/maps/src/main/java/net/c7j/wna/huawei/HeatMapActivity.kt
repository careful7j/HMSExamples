package net.c7j.wna.huawei

import android.graphics.Color
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.HeatMap
import com.huawei.hms.maps.model.HeatMapOptions
import net.c7j.wna.huawei.maps.R

//::created by c7j at 12.05.2023 04:55
class HeatMapActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMapView: MapView
    private var huaweiMap: HuaweiMap? = null
    private var heatMap: HeatMap? = null

    /**
     * Important! Map sdk was already initialized at:
     * @see net.c7j.wna.huawei.MapsMainActivity.initializeMapSdk
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heat_map)

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

        findViewById<MaterialButton>(R.id.btnHeatMapAdd).setOnClickListener { addHeatMap() }
        findViewById<MaterialButton>(R.id.btnHeatMapModify).setOnClickListener { modifyHeatMap() }
        findViewById<MaterialButton>(R.id.btnHeatMapRemove).setOnClickListener {
            heatMap?.remove()
            heatMap = null
        }
    }

    private fun addHeatMap() {
        if (heatMap != null) return
        // How to customize your heatmap:
        // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-heatmap-0000001147145582#section149701732141418
        val heatMapOptions = HeatMapOptions()
        heatMapOptions.intensity(2f)
        heatMapOptions.dataSet(R.raw.population_density)
        heatMap = huaweiMap?.addHeatMap("id", heatMapOptions)
    }

    private fun modifyHeatMap() {
        val colorMap: HashMap<Float, Int> = HashMap()
        colorMap[1.0f] = Color.GREEN
        heatMap?.setColor(colorMap)
        heatMap?.setIntensity(10f)
        heatMap?.setOpacity(0.5f)
        heatMap?.setRadius(10f)
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