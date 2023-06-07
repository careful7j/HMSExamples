package net.c7j.wna.huawei

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.GroundOverlay
import com.huawei.hms.maps.model.GroundOverlayOptions
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Tile
import com.huawei.hms.maps.model.TileOverlay
import com.huawei.hms.maps.model.TileOverlayOptions
import com.huawei.hms.maps.model.TileProvider
import net.c7j.wna.huawei.maps.R
import java.io.ByteArrayOutputStream
import kotlin.math.pow


class MapOverlayActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var mMapView: MapView
    private var huaweiMap: HuaweiMap? = null

    private var groundOverlay: GroundOverlay? = null
    private var mTileOverlay: TileOverlay? = null

    /**
     * Important! Map sdk was already initialized at:
     * @see net.c7j.wna.huawei.MapsMainActivity.initializeMapSdk
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_overlay)

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

        findViewById<MaterialButton>(R.id.btnGroundOverlay).setOnClickListener { applyGroundOverlay() }
        findViewById<MaterialButton>(R.id.btnTileOverlayAdd).setOnClickListener { addTileOverlay() }
        findViewById<MaterialButton>(R.id.btnTileOverlayModify).setOnClickListener { modifyTileOverlay() }
        findViewById<MaterialButton>(R.id.btnTileOverlayRemove).setOnClickListener { removeTileOverlay() }
    }

    // TODO: check this function again with next sdk version
    private fun applyGroundOverlay() {
//        val descriptorFromAsset = BitmapDescriptorFactory.fromAsset("images/name.jpg")
//        val descriptorFromBitmap = BitmapDescriptorFactory.fromBitmap(bitmap)
//        val descriptorFromFile = BitmapDescriptorFactory.fromFile(fileName)
//        val descriptorFromPath = BitmapDescriptorFactory.fromPath(path)
        val resourceId = net.c7j.wna.huawei.box.R.drawable.ic_launcher_foreground
        val descriptorFromResource = BitmapDescriptorFactory.fromResource(resourceId)
        val options = GroundOverlayOptions().position(PARIS, 64f, 64f).image(descriptorFromResource)
        groundOverlay = huaweiMap?.addGroundOverlay(options)
        groundOverlay?.setImage(BitmapDescriptorFactory.fromResource(resourceId))
        huaweiMap?.setOnGroundOverlayClickListener {
            toast("GroundOverlay is clicked and removed")
            groundOverlay?.remove()
        }
    }

    private fun addTileOverlay() {
        if (mTileOverlay != null) return
        val mTileSize = 256 // Set the tile size to 256 x 256.
        val mScale = 1
        val mDimension = mScale * mTileSize
        // Create a TileProvider object. This tile is locally generated.
        val mTileProvider = TileProvider { x, y, zoom ->
            val matrix = Matrix()
            val scale = 2.0.pow(zoom.toDouble()).toFloat() * mScale
            matrix.postScale(scale, scale)
            matrix.postTranslate((-x * mDimension).toFloat(), (-y * mDimension).toFloat())
            // Generate a Bitmap image.
            val bitmap = Bitmap.createBitmap(mDimension, mDimension, Bitmap.Config.RGB_565)
            bitmap.eraseColor(Color.parseColor("#024CFF"))
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            Tile(mDimension, mDimension, stream.toByteArray())
        }
        // Determine the tile overlay attributes, such as transparency and fade-in and fade-out animations
        val options = TileOverlayOptions().tileProvider(mTileProvider).transparency(0.5f).fadeIn(true)
        mTileOverlay = huaweiMap?.addTileOverlay(options)
    }

    private fun modifyTileOverlay() {
        mTileOverlay?.transparency = 0.8f
        mTileOverlay?.fadeIn = false
        mTileOverlay?.isVisible = true
    }

    private fun removeTileOverlay() {
        mTileOverlay?.remove()
        mTileOverlay?.clearTileCache()
        mTileOverlay = null
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