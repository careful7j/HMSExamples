package net.c7j.wna.huawei

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.CameraPosition
import com.huawei.hms.maps.model.LatLng
import net.c7j.wna.huawei.maps.R


class MapFragmentActivity : AppCompatActivity(), OnMapReadyCallback {

    private var huaweiMap: HuaweiMap? = null
    private var mSupportMapFragment: SupportMapFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_fragment)

        val fragmentManager = supportFragmentManager
        mSupportMapFragment = fragmentManager.findFragmentByTag("support_map_fragment") as SupportMapFragment?

        if (mSupportMapFragment == null) {
            val cameraPosition = CameraPosition.builder()
                .target(LatLng(43.0, 2.0))
                .zoom(2f)
                .bearing(2.0f)
                .tilt(2.5f)
                .build()

            val huaweiMapOptions = HuaweiMapOptions().camera(cameraPosition)
            mSupportMapFragment = SupportMapFragment.newInstance(huaweiMapOptions)
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.add(
                R.id.frame_map_fragment,
                mSupportMapFragment as SupportMapFragment,
                "support_map_fragment")
            fragmentTransaction.commit()
        }

        mSupportMapFragment?.getMapAsync(this)
        mSupportMapFragment?.onAttach(this as Context)
    }

    override fun onMapReady(map: HuaweiMap) {
        huaweiMap = map
        huaweiMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(48.893478, 2.334595), 10f))
    }
}