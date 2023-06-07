package net.c7j.wna.huawei

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnCallback
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnPrompt
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnResult
import com.huawei.hms.support.api.fido.bioauthn.CryptoObject
import com.huawei.hms.support.api.fido.bioauthn.FaceManager
import net.c7j.wna.huawei.fido_fingerprint.R

// FIDO Kit represents here fingerprint auth and face auth capabilities
class FingerprintActivity : BaseActivity() {

    // If your application is still supporting 4+ year old firmware devices (as for 2023) like EMUI 9.x or older:
    // In the scenario where BioAuthnPrompt.PromptInfo.Builder.setDeviceCredentialAllowed(true) is used,
    // fingerprint authentication may work once only. You can solve this problem in one of the following ways:
    // 1. The activity has only one singleton BioAuthnPrompt object. Do not create the object repeatedly.
    // 2. After the authentication is complete, call the recreate() method of the activity.
    // 3. Close the activity and open it again.
    private var bioAuthPrompt: BioAuthnPrompt? = null
    private var tvStatus: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)

        tvStatus = findViewById(R.id.tvStatus)
        findViewById<MaterialButton>(R.id.btnFingerprint).setOnClickListener { runFingerprint() }
        findViewById<MaterialButton>(R.id.btnFaceAuth).setOnClickListener { runFaceAuthWithoutCryptoObject() }
    }

    @SuppressLint("SetTextI18n")
    private fun runFingerprint() {
        tvStatus?.text = ""
        val builder = BioAuthnPrompt.PromptInfo.Builder().setTitle("This is the title.")
            .setSubtitle("This is the subtitle")
            .setDescription("This is the description")
        // The user will first be prompted to authenticate with biometrics, but also given the option to
        // authenticate with their device PIN, pattern, or password. setNegativeButtonText(CharSequence) should
        // not be set if this is set to true.
        builder.setDeviceCredentialAllowed(true)
        // setDeviceCredentialAllowed(true) should NOT be set if you set negative button text:
        // builder.setNegativeButtonText("This is the 'Cancel' button.");
        val info = builder.build()
        tvStatus?.append("Start fingerprint authentication without CryptoObject.\nAuthenticating......\n")
        bioAuthPrompt = createBioAuthPrompt()
        bioAuthPrompt?.auth(info)
    }

    // Sets up fingerprint bio auth callback
    private fun createBioAuthPrompt(): BioAuthnPrompt {
        val callback = object : BioAuthnCallback() {

            // If error code 11 = fingerprint is not set, please go to the device settings and add one.
            // See: https://developer.huawei.com/consumer/en/doc/development/Security-References/bioauthnprompt_x-0000001050267874#section17341532162818
            override fun onAuthError(errMsgId: Int, errString: CharSequence) {
                tvStatus?.append("Authentication error. errorCode=$errMsgId, errorMessage=$errString")
            }

            override fun onAuthSucceeded(result: BioAuthnResult) {
                if (result.cryptoObject != null) {
                    tvStatus?.append("Authentication succeeded. CryptoObject=" + result.cryptoObject)
                } else {
                    tvStatus?.append("Authentication succeeded. CryptoObject=null")
                }
            }

            override fun onAuthFailed() {
                tvStatus?.append("Authentication failed.")
            }
        }
        return BioAuthnPrompt(this, ContextCompat.getMainExecutor(this), callback)
    }

    // Face recognition auth is supported only since EMUI 10.0 (API Level 29) inclusive.
    // In addition, the device hardware must support facial authentication.
    @SuppressLint("SetTextI18n")
    fun runFaceAuthWithoutCryptoObject() {
        tvStatus?.text = ""
        val permissionCheck = ContextCompat.checkSelfPermission(this@FingerprintActivity, Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            tvStatus?.append("The camera permission is not enabled. Please enable it.")
            if (!checkPermission(Manifest.permission.CAMERA)) cameraPermission.launch(Manifest.permission.CAMERA)
            return
        }

        val callback = object : BioAuthnCallback() {
            override fun onAuthError(errMsgId: Int, errString: CharSequence) {
                tvStatus?.append("Authentication error. errorCode=" + errMsgId + ",errorMessage=" + errString
                        + if (errMsgId == 1012) " The camera permission may not be enabled." else "")
            }

            override fun onAuthHelp(helpMsgId: Int, helpString: CharSequence) {
                tvStatus?.append("Authentication help. helpMsgId=$helpMsgId,helpString=$helpString\n")
            }

            override fun onAuthSucceeded(result: BioAuthnResult) {
                tvStatus?.append("Authentication succeeded.")
            }

            override fun onAuthFailed() {
                tvStatus?.append("Authentication failed.")
            }
        }

        val cancellationSignal = CancellationSignal()
        val faceManager = FaceManager(this)
        // Checks whether 3D facial authentication can be performed
        val errorCode = faceManager.canAuth()
        // If error code 11 = face is not set, please go to the device settings and add one. API Reference:
        // https://developer.huawei.com/consumer/en/doc/development/Security-References/facemanager_x-0000001050418949#section14551182920586
        if (errorCode != 0) {
            tvStatus?.append("Can not authenticate. errorCode=$errorCode")
            return
        }

        val flags = 0
        val handler: Handler? = null
        // Recommended CryptoObject to be set to null. KeyStore is not associated with face authentication in current
        // version. KeyGenParameterSpec.Builder.setUserAuthenticationRequired() must be set false in this scenario.
        val crypto: CryptoObject? = null
        tvStatus?.append("Start face authentication.\nAuthenticating......\n")
        faceManager.auth(crypto, cancellationSignal, flags, callback, handler)
    }

    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) toast("Grant camera access permission, please")
    }

}