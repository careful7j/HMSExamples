package net.c7j.wna.huawei

import android.graphics.Color
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.*
import net.c7j.wna.huawei.maps.R

//::created by c7j at 10.05.2023 23:21 PM
class MapDrawShapesActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMapView: MapView
    private var huaweiMap: HuaweiMap? = null

    private var mPolyline : Polyline? = null
    private var mPolyline2 : Polyline? = null
    private var mPolyline3 : Polyline? = null
    private var mPolygon : Polygon? = null
    private var mCircle : Circle? = null

    /**
     * Important! Map sdk was already initialized at:
     * @see net.c7j.wna.huawei.MapsMainActivity.initializeMapSdk
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_draw_shapes)

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
        findViewById<MaterialButton>(R.id.btnDrawShapes).setOnClickListener { drawShapes() }
        findViewById<MaterialButton>(R.id.btnModifyShapes).setOnClickListener { modifyShapes() }
    }

    private fun drawShapes() {
        mPolyline?.remove()
        mPolyline2?.remove()
        mPolyline3?.remove()
        mPolygon?.remove()
        mCircle?.remove()
        drawPolyline()
        drawPolygon()
        drawCircle()
        drawLinePattern()
        drawVertexType()
    }

    private fun drawPolyline() {
        mPolyline = huaweiMap?.addPolyline(PolylineOptions()
            .add(LatLng(47.893478, 2.334595), LatLng(48.993478, 3.434595),
                LatLng(48.693478, 2.134595), LatLng(48.793478, 2.334595))
            .color(Color.BLUE)
            .width(3f))
        mPolyline?.isClickable = true
        huaweiMap?.setOnPolylineClickListener { toast("Polyline is clicked") }
    }

    private fun drawPolygon() {

        fun createRectangle(center: LatLng, halfWidth: Double, halfHeight: Double): List<LatLng> {
            return listOf(LatLng(center.latitude - halfHeight, center.longitude - halfWidth),
                LatLng(center.latitude - halfHeight, center.longitude + halfWidth),
                LatLng(center.latitude + halfHeight, center.longitude + halfWidth),
                LatLng(center.latitude + halfHeight, center.longitude - halfWidth))
        }

        mPolygon = huaweiMap?.addPolygon(PolygonOptions()
                .addAll(createRectangle(LatLng(48.893478, 2.334595), 0.1, 0.1))
                .fillColor(Color.GREEN)
                .strokeColor(Color.BLACK))

        mPolygon?.isClickable = true
        huaweiMap?.setOnPolygonClickListener { toast("Polygon is clicked") }
    }

    private fun drawCircle() {
        mCircle = huaweiMap?.addCircle(CircleOptions()
            .center(LatLng(48.893478, 2.334595))
            .radius(3500.0)
            .fillColor(Color.GREEN))

        mCircle?.isClickable = true
        mCircle?.fillColor = Color.TRANSPARENT
        mCircle?.strokeColor = Color.RED
        mCircle?.strokeWidth = 10.0f
        huaweiMap?.setOnCircleClickListener { toast("Circle is clicked") }
    }

    private fun drawLinePattern() {
        val linePattern: MutableList<PatternItem> = ArrayList()
        val dash: PatternItem = Dash(100f)
        val dot: PatternItem = Dot()
        val gap: PatternItem = Gap(40f)
        linePattern.add(dash)
        linePattern.add(dot)
        linePattern.add(gap)

        mPolyline2 = huaweiMap?.addPolyline(PolylineOptions().add(
            LatLng(47.896478, 2.234595), LatLng(48.996478, 3.334595),
            LatLng(48.693478, 2.434595), LatLng(48.793478, 2.234595)))
        mPolyline2?.pattern = linePattern
    }

    private fun drawVertexType() {
        mPolyline3 = huaweiMap?.addPolyline(PolylineOptions().add(
            LatLng(47.899478, 2.434595), LatLng(48.999478, 3.534595),
            LatLng(48.693478, 2.234595), LatLng(48.793478, 2.134595)))

        // Set the start vertex of the polyline to a square.
        mPolyline3?.startCap = ButtCap()
        // Set the end vertex of the polyline to a semicircle.
        mPolyline3?.endCap = RoundCap()
    }

    private fun modifyShapes() {
        mPolygon?.strokeColor = Color.RED
        mPolygon?.fillColor = Color.WHITE
        mCircle?.strokeColor = Color.GREEN
        mPolyline?.color = Color.RED
        mPolyline?.width = 10f
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