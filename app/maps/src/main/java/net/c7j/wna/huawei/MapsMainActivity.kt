package net.c7j.wna.huawei

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.maps.MapsInitializer
import net.c7j.wna.huawei.maps.R


class MapsMainActivity : BaseActivity() {

    private fun initializeMapSdk() {
        // Make sure you have already enabled Huawei maps toggle in your AppGallery console at:
        // All Services -> My projects -> Project settings -> Manage APIs -> Map Kit (enable)
        //
        // You are free to either initialize maps in your Activity or in your Application class
        // Takes your "api_key" from your agconnect-services.json
        val apiKey = AGConnectOptionsBuilder().build(this@MapsMainActivity).getString("client/api_key")
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