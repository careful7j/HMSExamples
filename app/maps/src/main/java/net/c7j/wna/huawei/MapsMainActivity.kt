package net.c7j.wna.huawei

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.hms.maps.MapsInitializer
import net.c7j.wna.huawei.maps.R

//::created by c7j at 08.05.2023 11:55 PM
class MapsMainActivity : BaseActivity() {

    private fun initializeMapSdk() {
        // 1. "api_key" value can be found at your agconnect-services.json
        // 2. make sure you have already enabled Huawei maps toggle in your AppGallery console at:
        //      All Services -> My projects -> Project settings -> Manage APIs -> Map Kit (enable)
        // 3. You are free to either initialize maps in your Activity or in your Application class
        val apiKey = "CV5gn129T6MZZmwblKw76s4Rf8zzVKqs7gMNKjYrJzSHIMEZD17zF9tjUcIbNypIBLQvRsq1+8pzt2BWHPcJyRDCnOwS"
        MapsInitializer.initialize(this, apiKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_main)
        initializeMapSdk()

        findViewById<MaterialButton>(R.id.btnSimpleMap).setOnClickListener {
            navigate("net.c7j.wna.huawei.SimpleMapActivity")
        }
        findViewById<MaterialButton>(R.id.btnMapSupportFragment).setOnClickListener {
            navigate("net.c7j.wna.huawei.MapFragmentActivity")
        }
        findViewById<MaterialButton>(R.id.btnMarkerLocationMapActivity).setOnClickListener {
            navigate("net.c7j.wna.huawei.MarkerLocationMapActivity")
        }
        findViewById<MaterialButton>(R.id.btnStyleAndCameraMapActivity).setOnClickListener {
            navigate("net.c7j.wna.huawei.StyleAndCameraMapActivity")
        }
        findViewById<MaterialButton>(R.id.btnLiteMapActivity).setOnClickListener {
            navigate("net.c7j.wna.huawei.LiteMapActivity")
        }
        findViewById<MaterialButton>(R.id.btnMapDrawShape).setOnClickListener {
            navigate("net.c7j.wna.huawei.MapDrawShapesActivity")
        }
        findViewById<MaterialButton>(R.id.btnMapOverlayActivity).setOnClickListener {
            navigate("net.c7j.wna.huawei.MapOverlayActivity")
        }
        findViewById<MaterialButton>(R.id.btnHeatMapActivity).setOnClickListener {
            navigate("net.c7j.wna.huawei.HeatMapActivity")
        }
    }
}