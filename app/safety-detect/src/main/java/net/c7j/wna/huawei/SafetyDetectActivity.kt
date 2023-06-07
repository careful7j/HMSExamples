package net.c7j.wna.huawei

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.core.CommonCode
import com.huawei.hms.support.api.entity.safetydetect.MaliciousAppsData
import com.huawei.hms.support.api.entity.safetydetect.SysIntegrityRequest
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import net.c7j.wna.huawei.safety_detect.R
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

/**
 * This sdk provides 4 features which can improve your application safety:
 * @see invokeSysIntegrity()- check system integrity (check if device is rooted)
 * @see getMaliciousApps()  - check if there are known malicious apps installed on a device
 * @see performUrlCheck()   - check url of a website for malicious/virus/phishing content
 * @see performUserDetect() - check if a user currently interacting with the app is a robot (captcha)
 */
class SafetyDetectActivity : BaseActivity() {

    private var tvInfo: TextView? = null
    private var appId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safety_detect)
        findViewById<MaterialButton>(R.id.btnRootCheck).setOnClickListener { invokeSysIntegrity() }
        findViewById<MaterialButton>(R.id.btnMaliciousAppCheck).setOnClickListener { getMaliciousApps() }
        findViewById<MaterialButton>(R.id.btnUrlCheck).setOnClickListener { performUrlCheck() }
        findViewById<MaterialButton>(R.id.btnUserDetect).setOnClickListener { performUserDetect() }
        tvInfo = findViewById(R.id.tvInfo)
        appId = AGConnectOptionsBuilder().build(this@SafetyDetectActivity).getString("client/app_id")
    }

    /**
     * In simple - checks if device is rooted (user received root access to device system and can do literally everything)
     *
     * The API evaluates the system integrity in the secure boot process in the Trusted Execution Environment (TEE)
     * and will dynamically evaluate the system integrity. System integrity check results request the certificate signature.
     * The server uses the X.509 digital certificate to sign the system integrity check results and
     * transfers the signed JWS-format results to the SysIntegrity API.
     */
    @SuppressLint("ObsoleteSdkInt", "SetTextI18n")
    private fun invokeSysIntegrity() {
        // (Recommended) nonce value to be derived from data sent to your server
        val nonce = ByteArray(24) // A nonce value must contain 16 to 66 bytes
        try {
            val random: SecureRandom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SecureRandom.getInstanceStrong()
            } else {
                SecureRandom.getInstance("SHA1PRNG")
            }
            random.nextBytes(nonce)
        } catch (e: NoSuchAlgorithmException) {
            log("NoSuchAlgorithmException: ${e.message}")
        }
        val sysIntegrityRequest = SysIntegrityRequest()
        sysIntegrityRequest.appId = appId
        sysIntegrityRequest.nonce = nonce
        sysIntegrityRequest.alg = "PS256"
        SafetyDetect.getClient(this@SafetyDetectActivity)
            .sysIntegrity(sysIntegrityRequest)
            .addOnSuccessListener { response -> // Indicates communication with the service was successful.
                val jwsStr = response.result
                val jwsSplit = jwsStr.split(".").toTypedArray()
                val jwsPayloadStr = jwsSplit[1]
                val payloadDetail = String(
                    Base64.decode(
                        jwsPayloadStr.toByteArray(StandardCharsets.UTF_8),
                        Base64.URL_SAFE
                    ), StandardCharsets.UTF_8
                )
                try {
                    val jsonObject = JSONObject(payloadDetail)
                    val basicIntegrity = jsonObject.getBoolean("basicIntegrity")
                    tvInfo?.text = "System integrity confirmed: $basicIntegrity"
                    if (!basicIntegrity) tvInfo?.append("\nAdvice: " + jsonObject.getString("advice"))
                } catch (e: JSONException) {
                    tvInfo?.text = e.message.also { log(e.message) }
                }
            }
            .addOnFailureListener { e ->
                val errorMsg: String? =
                    if (e is ApiException) { // An error with the HMS API contains some additional details.
                        SafetyDetectStatusCodes.getStatusCodeString(e.statusCode) + ": " + e.message
                    } else e.message
                log(errorMsg).also { tvInfo?.text = errorMsg }
                toast(errorMsg ?: "no error message")
            }
    }

    // Obtain a list of malicious apps and evaluate whether to restrict your app's behavior based on this risks
    private fun getMaliciousApps() {
        tvInfo?.text = ""
        SafetyDetect.getClient(this@SafetyDetectActivity)
            .maliciousAppsList
            .addOnSuccessListener { maliciousAppsListResp ->
                val appsDataList: List<MaliciousAppsData> = maliciousAppsListResp.maliciousAppsList
                if (maliciousAppsListResp.rtnCode == CommonCode.OK) {
                    if (appsDataList.isEmpty()) {
                        val out = "No known potentially malicious apps are installed"
                        tvInfo?.text = out.also{ toast(out) }
                    } else {
                        for (maliciousApp in appsDataList) {
                            val out = """
                                Information about a malicious app:
                                APK: " + ${maliciousApp.apkPackageName}
                                SHA-256: " + ${maliciousApp.apkSha256}
                                Category: " + ${maliciousApp.apkCategory}
                                """
                            tvInfo?.append(out).also { log(out) }
                        }
                    }
                } else {
                    val msg = ("Get malicious apps list failed! Message: ${maliciousAppsListResp.errorReason}")
                    log(msg).also { toast(msg) }
                }
            }
            .addOnFailureListener { e ->
                val errorMsg = if (e is ApiException) { // An error with the HMS API contains some additional details.
                    SafetyDetectStatusCodes.getStatusCodeString(e.statusCode) + ": " + e.message
                } else "unknown error ${e.message}"
                val msg = "Get malicious apps list failed! Message: $errorMsg"
                toast(msg).also { log(msg) }
            }
    }

    // When a user visits a URL, this API checks whether the URL is a malicious one.
    // If so, you can evaluate the risk and either warn the user of the risk or block the URL.
    private fun performUrlCheck() {
        val client = SafetyDetect.getClient(this@SafetyDetectActivity)
        client.initUrlCheck()
        tvInfo?.text = ""
        val url = "https://developer.huawei.com/consumer/cn/"
        client.urlCheck(url, appId, MALWARE, PHISHING)
            .addOnSuccessListener {
                val list = it.urlCheckResponse
                tvInfo?.text = if (list.isEmpty()) "$url\n= has NO known threats" else "$url\n= Dangerous URL!"
            }.addOnFailureListener {
                if (it is ApiException) { // HMS Core (APK) error code and corresponding error description.
                    tvInfo?.text = it.message
                    log(it.message)
                    // Note: If the status code is SafetyDetectStatusCode.CHECK_WITHOUT_INIT,
                    // you did not call the initUrlCheck() method or you have initiated a URL check request before the call is completed.
                    // If an internal error occurs during the initialization, you need to call the initUrlCheck() method again to initialize the API.
                } else { // Unknown error
                    tvInfo?.text = it.message
                    log(it.message)
                }
            }
        client.shutdownUrlCheck()
    }

    // This feature is designed to check if user operating the app is a robot.
    // Outside the Chinese mainland, users can be verified based on verification codes (captcha).
    // https://developer.huawei.com/consumer/en/doc/development/Security-Guides/userdetect-0000001050154382
    private fun performUserDetect() {

        fun shutDownUserDetect() {
            val client = SafetyDetect.getClient(this@SafetyDetectActivity)
            client.shutdownUserDetect()
                .addOnSuccessListener { log("shutdown success") }
                .addOnFailureListener { log("shutdown fail") }
        }

        val client = SafetyDetect.getClient(this@SafetyDetectActivity)
        client.initUserDetect()
            .addOnSuccessListener { log("init success") }
            .addOnFailureListener { log("init fail") }

        client.userDetection(appId)
            .addOnSuccessListener { userDetectResponse ->
                val responseToken = userDetectResponse.responseToken
                if (responseToken.isNotEmpty()) {
                    // Send the response token to your app server, and call the cloud API of HMS Core
                    // on your server to obtain the fake user detection result.
                    log("verified: OK").also { toast("verified: OK") }
                    shutDownUserDetect()
                }
            }
            .addOnFailureListener {
                val errorMsg: String? = if (it is ApiException) {
                    (SafetyDetectStatusCodes.getStatusCodeString(it.statusCode) + ": " + it.message)
                } else it.message
                log("User detection fail. Error info: $errorMsg").also { toast("User detection fail") }
                shutDownUserDetect()
            }

    }


    companion object {
        // Pages containing potentially malicious apps
        // (such as home page tampering URLs, Trojan-infected URLs, and malicious app download URLs).
        const val MALWARE = 1
        // URLs of this type are marked as phishing and spoofing URLs.
        const val PHISHING = 3
    }
}