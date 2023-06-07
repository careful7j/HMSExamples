package net.c7j.wna.huawei

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.button.MaterialButton
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import net.c7j.wna.huawei.scan.R

// This activity is designed to show basic use of scan code feature in "Default mode"
class ScanActivity : BaseActivity() {

    private var tvInfo : TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        tvInfo = findViewById(R.id.tvInfo)
        initViews()
        if (!checkPermission(Manifest.permission.CAMERA)) cameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun initViews() {
        findViewById<MaterialButton>(R.id.btnScanDefault).setOnClickListener { defaultScan() }
        findViewById<MaterialButton>(R.id.btnScanCustomized).setOnClickListener {
            startForResult.launch(Intent(this, CustomizedScanActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnGenerateCode).setOnClickListener {
            navigate("net.c7j.wna.huawei.ScanCodeGenerateActivity")
        }
    }

    // Method show basic use of scan code feature in "Default mode"
    private fun defaultScan() {
        val options = HmsScanAnalyzerOptions.Creator()
            .setErrorCheck(true)    // Sdk error handler configuration set up
            // If you set exact type of barcode/qrcode the scanning speed increases:
            .setHmsScanTypes(HmsScan.ALL_SCAN_TYPE) // This value is meaningless to set, since it's a default
            .create()

        ScanUtil.startScan(         // this method runs UI in "Default mode"
            this,                   // activity to return result to via onActivityResult()
            REQUEST_CODE_SCAN_ONE,  // requestCode of onActivityResult() when result returns
            options)                // scanner settings configuration
    }

    // Here is a result returned of "default mode" scan called in method defaultScan() right above
    @Deprecated("currently scan sdk version not yet support new OS method")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CODE_SCAN_ONE) && !(resultCode != RESULT_OK || data == null))
        {
            val errorCode: Int = data.getIntExtra(ScanUtil.RESULT_CODE, ScanUtil.SUCCESS)
            if (errorCode == ScanUtil.SUCCESS) handleSingleResult(data)
        }
    }

    // Activity contract to handle results returned from CustomizedScanActivity.kt
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: Intent()
            val errorCode: Int = data.getIntExtra(ScanUtil.RESULT_CODE, ScanUtil.SUCCESS)
            if (errorCode == ScanUtil.SUCCESS) {
                val multiResult = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                    @Suppress("DEPRECATION") data.getParcelableArrayExtra(CustomizedScanActivity.MULTI_RESULT)
                } else {
                    intent.getParcelableArrayExtra(CustomizedScanActivity.MULTI_RESULT, Array<HmsScan>::class.java)
                }
                if (multiResult == null) {  // is a single-mode result (just one qr/bar or whatever code is returned)
                    handleSingleResult(data)
                } else {                    // is a multi-mode result (multiple qr codes were scanned from one image)
                    handleMultipleResult(multiResult)
                }
            }
        }
    }

    // This method handles the result of single code reading
    private fun handleSingleResult(data: Intent) {
        val scanResult: HmsScan? = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            @Suppress("DEPRECATION") data.getParcelableExtra(ScanUtil.RESULT)
        } else { data.getParcelableExtra(ScanUtil.RESULT, HmsScan::class.java) }
        if (scanResult != null) {
            tvInfo?.text = scanResult.showResult
        }
    }

    // This method handles the result of a multi-mode reading (multiple codes can be returned)
    private fun handleMultipleResult(obj: Array<out Any>) {
        tvInfo?.text = ""
        for (i in obj.indices) {
            if (obj[i] is HmsScan && !TextUtils.isEmpty((obj[i] as HmsScan).getOriginalValue())) {
                tvInfo?.append((obj[i] as HmsScan).showResult + "\n")
            }
        }
    }


    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) toast("Grant camera access permission, please")
    }

    companion object {
        const val REQUEST_CODE_SCAN_ONE = 0x012
    }

}