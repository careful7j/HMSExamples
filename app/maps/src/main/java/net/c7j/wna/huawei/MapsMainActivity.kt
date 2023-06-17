package net.c7j.wna.huawei

import android.os.Bundle
import android.view.View
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.maps.MapsInitializer
import net.c7j.wna.huawei.maps.R


class MapsMainActivity : BaseActivity() {

    private fun initializeMapSdk() {
        // Make sure you have already enabled Huawei maps toggle in your AppGallery console at:
        // All Services -> My projects -> Project settings -> Manage APIs -> Map Kit (enable)
        // You are free to either initialize maps in your Activity or in your Application class
        val apiKey = AGConnectOptionsBuilder().build(this@MapsMainActivity).getString("client/api_key")
        MapsInitializer.initialize(this, apiKey)
    }

    fun btnClicked(v: View) {
        navigate(when (v.id) {
            R.id.btnSimpleMap -> "net.c7j.wna.huawei.SimpleMapActivity"
            R.id.btnMapSupportFragment -> "net.c7j.wna.huawei.MapFragmentActivity"
            R.id.btnMarkerLocationMapActivity -> "net.c7j.wna.huawei.MarkerLocationMapActivity"
            R.id.btnStyleAndCameraMapActivity -> "net.c7j.wna.huawei.StyleAndCameraMapActivity"
            R.id.btnLiteMapActivity -> "net.c7j.wna.huawei.LiteMapActivity"
            R.id.btnMapDrawShape -> "net.c7j.wna.huawei.MapDrawShapesActivity"
            R.id.btnMapOverlayActivity -> "net.c7j.wna.huawei.MapOverlayActivity"
            R.id.btnHeatMapActivity -> "net.c7j.wna.huawei.HeatMapActivity"
            else -> ""
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_main)
        initializeMapSdk()
    }
}