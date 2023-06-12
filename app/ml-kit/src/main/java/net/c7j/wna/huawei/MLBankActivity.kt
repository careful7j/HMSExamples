package net.c7j.wna.huawei

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.button.MaterialButton
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.mlplugin.card.bcr.MLBcrCapture
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureConfig
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureFactory
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureResult
import com.huawei.hms.mlsdk.common.MLApplication
import net.c7j.wna.huawei.ml.R

/**
 * This activity shows how to launch bank card camera recognition sdk
 *
 * This example shows standard UI option. In case you want to make your own UI, please follow custom view example:
 * https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/bank-card-recognition-0000001050038118#section13505135813179
 */
@SuppressLint("SetTextI18n")
class MLBankActivity : BaseActivity() {

    var resultTextView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ml_bank)
        initMLKit()
        resultTextView = findViewById(R.id.tvInfo)
        findViewById<MaterialButton>(R.id.btnRecognizeBankCard).setOnClickListener {
            if (!checkPermission(Manifest.permission.CAMERA)) {
                cameraPermission.launch(Manifest.permission.CAMERA)
            } else startRecognition()
        }
    }

    private fun startRecognition() {
        val config = MLBcrCaptureConfig.Factory()
            .setOrientation(MLBcrCaptureConfig.ORIENTATION_AUTO)
            .setResultType(MLBcrCaptureConfig.RESULT_ALL)
            .create()
        val bcrCapture = MLBcrCaptureFactory.getInstance().getBcrCapture(config)
        bcrCapture.captureFrame(this, this.callback)
    }

    private val callback: MLBcrCapture.Callback = object : MLBcrCapture.Callback {

        override fun onSuccess(bankCardResult: MLBcrCaptureResult) {
            val text =  "recognition success:\n" +
                        "organization: ${bankCardResult.organization}\n" +
                        "number: ${bankCardResult.number}\n" +
                        "expires: ${bankCardResult.expire}\n" +
                        "issuer: ${bankCardResult.issuer}\n" +
                        "type: ${bankCardResult.type}"
            resultTextView?.text = text
        }

        override fun onCanceled() {
            resultTextView?.text = "User cancelled operation"
        }

        // No text is recognized or a system exception occurs during recognition
        override fun onFailure(retCode: Int, bitmap: Bitmap) {
            resultTextView?.text = "Failed, issue code: $retCode"
        }

        override fun onDenied() {  // Processing deny scenarios, for example, the camera is unavailable
            resultTextView?.text = "User denied operation"
        }
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) toast("Grant camera access permission, please") else startRecognition()
    }

    private fun initMLKit() {
        val apiKey = AGConnectOptionsBuilder().build(this@MLBankActivity).getString("client/api_key")
        MLApplication.getInstance().apiKey = apiKey
    }
}