package net.c7j.wna.huawei

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.button.MaterialButton
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzer
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.huawei.hms.mlsdk.common.MLFrame
import net.c7j.wna.huawei.scan.R
import java.io.IOException


/**
 * This activity is designed to demonstrate RemoteView mode
 * (when you want to build your own UI for code reading instead of default one)
 * @see configureRemoteViewForCustomizedScan() method
 * To read code from your pictures taken from gallery:
 * @see setUpGalleryImagePicker()
 * To decode multiple codes from one image taken from gallery in ASYNC mode:
 * @see decodeMultiAsync()
 * To decode multiple codes from one image taken from gallery in SYNC mode:
 * @see decodeMultiSync()
 */
class CustomizedScanActivity : BaseActivity() {

    private var tvInfo : TextView? = null
    private var remoteView: RemoteView? = null
    private var frameLayout: FrameLayout? = null

    private var mScreenWidth = 0
    private var mScreenHeight = 0

    private var pickerMode = SINGLE_CODE_PHOTO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customized_scan)
        tvInfo = findViewById(R.id.tvInfo)
        if (!checkPermission(Manifest.permission.CAMERA)) cameraPermission.launch(Manifest.permission.CAMERA)
        configureRemoteViewForCustomizedScan(savedInstanceState)
        setUpGalleryImagePicker()
    }

    // Prompts user to pick a picture from his device's gallery
    private val mediaPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (pickerMode == SINGLE_CODE_PHOTO) try {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            val hmsScans = ScanUtil.decodeWithBitmap(
                this@CustomizedScanActivity,
                bitmap,
                HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create())

            val operationSuccess = hmsScans != null && hmsScans.isNotEmpty() && hmsScans[0] != null &&
                    !TextUtils.isEmpty(hmsScans[0].getOriginalValue())

            if (operationSuccess) {
                val intent = Intent()
                intent.putExtra(ScanUtil.RESULT, hmsScans[0])
                setResult(RESULT_OK, intent)
                finish()
            } else log("operation failed")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (pickerMode == MULTIPROCESSOR_ASYNC_CODE || pickerMode == MULTIPROCESSOR_SYNC_CODE) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                if (pickerMode == MULTIPROCESSOR_ASYNC_CODE) decodeMultiAsync(bitmap) else decodeMultiSync(bitmap)
            } catch (e: IOException) { e.printStackTrace().also{ log(e) } }
        }
    }

    // Configures RemoteView to build custom UI for code reading
    private fun configureRemoteViewForCustomizedScan(savedInstanceState: Bundle?) {
        frameLayout = findViewById(R.id.frameForCustomizedScan)
        val dm = resources.displayMetrics
        val density = dm.density
        mScreenWidth = resources.displayMetrics.widthPixels
        mScreenHeight = resources.displayMetrics.heightPixels
        val scanFrameSize = (SCAN_FRAME_SIZE * density).toInt()
        val rect = Rect() // can be null, then will be centered at the middle of the layout
        rect.left = mScreenWidth / 2 - scanFrameSize / 2
        rect.right = mScreenWidth / 2 + scanFrameSize / 2
        rect.top = mScreenHeight / 2 - scanFrameSize / 2
        rect.bottom = mScreenHeight / 2 + scanFrameSize / 2
        remoteView = RemoteView.Builder()
            .setContext(this)
            .setBoundingBox(rect)
            .setFormat(HmsScan.ALL_SCAN_TYPE)
            .build()

        remoteView?.setOnResultCallback { result -> //Check the result.
            val operationSuccess = result != null && result.isNotEmpty() && result[0] != null &&
                !TextUtils.isEmpty(result[0].getOriginalValue())

            if (operationSuccess) {
                    val intent = Intent()
                    intent.putExtra(ScanUtil.RESULT, result[0])
                    setResult(RESULT_OK, intent)
                    finish()
            } else log("operation failed")
        }
        remoteView?.onCreate(savedInstanceState)
        val matchParent = LinearLayout.LayoutParams.MATCH_PARENT
        val params = FrameLayout.LayoutParams(matchParent, matchParent)
        frameLayout?.addView(remoteView, params)
    }

    private fun setUpGalleryImagePicker() {
        findViewById<MaterialButton>(R.id.btnScanPicture)?.setOnClickListener {
            pickerMode = SINGLE_CODE_PHOTO           // run to get one qr code from the picture
            mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        findViewById<MaterialButton>(R.id.btnMultiASync)?.setOnClickListener {
            pickerMode = MULTIPROCESSOR_ASYNC_CODE  // run to get multiple qr code in async mode from the picture
            mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        findViewById<MaterialButton>(R.id.btnMultiSync)?.setOnClickListener {
            pickerMode = MULTIPROCESSOR_SYNC_CODE   // run to get multiple qr code in sync mode from the picture
            mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    // Reads multiple codes from a bitmap in async mode
    private fun decodeMultiAsync(bitmap: Bitmap) {
        val image = MLFrame.fromBitmap(bitmap)
        val analyzer = HmsScanAnalyzer.Creator(this).setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create()
        analyzer.analyzInAsyn(image).addOnSuccessListener { hmsScans ->
            val operationSuccess = hmsScans != null && hmsScans.size > 0 && hmsScans[0] != null &&
                    !TextUtils.isEmpty(hmsScans[0].getOriginalValue())

            if (operationSuccess) {
                val intent = Intent()
                intent.putExtra(MULTI_RESULT, hmsScans.toTypedArray())
                setResult(RESULT_OK, intent)
                finish()
            } else log("operation failed")
        }.addOnFailureListener { e -> log(e) }
    }

    // Reads multiple codes from a bitmap in synchronized mode
    private fun decodeMultiSync(bitmap: Bitmap) {
        val image = MLFrame.fromBitmap(bitmap)
        val analyzer = HmsScanAnalyzer.Creator(this).setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create()
        /**
         * Important! SparseArray is returned at analyzer.analyseFrame(image) below:
         * That means if you have 2,3,4 or more SAME QR codes on your picture, sdk will just return only one.
         * Async mode doesn't have such behavior, if it is sensitive for your business scenario consider using:
         * @see CustomizedScanActivity.decodeMultiAsync()
         */
        val result = analyzer.analyseFrame(image)
        val operationSuccess = result != null && result.size() > 0 && result.valueAt(0) != null &&
                !TextUtils.isEmpty(result.valueAt(0).getOriginalValue())

        if (operationSuccess) {
            val info = arrayOfNulls<HmsScan?>(result.size())
            for (index in 0 until result.size()) {
                info[index] = result.valueAt(index)
            }
            val intent = Intent()
            intent.putExtra(MULTI_RESULT, info)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) toast("Grant camera access permission, please")
    }

    override fun onStart() {
        super.onStart()
        remoteView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        remoteView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        remoteView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        remoteView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        remoteView?.onDestroy()
    }

    companion object {
        const val SINGLE_CODE_PHOTO = 0X1113
        const val MULTIPROCESSOR_SYNC_CODE = 0X1114
        const val MULTIPROCESSOR_ASYNC_CODE = 0X1115
        const val SCAN_FRAME_SIZE = 240
        const val MULTI_RESULT = "MULTI_RESULT"
    }

}