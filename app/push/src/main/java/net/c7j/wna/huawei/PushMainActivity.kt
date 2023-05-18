package net.c7j.wna.huawei

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.common.ApiException
import com.huawei.hms.push.HmsMessaging
import com.huawei.hms.push.HmsProfile
import com.huawei.hms.push.HmsProfile.HUAWEI_PROFILE
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.c7j.wna.huawei.HuaweiPushService.Companion.HUAWEI_PUSH_ACTION
import net.c7j.wna.huawei.push.R

// ::created by c7j at 17.05.2023 19:35
class PushMainActivity : BaseActivity() {

    var deviceToken : String? = null

    // If backend configures message.android.notification.foreground_show parameter to false,
    // coming push messages will not appear in notification center while app is in foreground
    // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-basic-receivemsg-0000001087370610#section97421726122520
    // https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/https-send-api-0000001050986197#EN-US_TOPIC_0000001503838972__p163211583235
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push_main)
        getToken()
        initTurnOnOffSwitchButtons()
        registerReceiver(pushReceiver, IntentFilter(HUAWEI_PUSH_ACTION))
        findViewById<MaterialButton>(R.id.btnCopyToken).setOnClickListener { copyTokenToClipboard() }
        findViewById<MaterialButton>(R.id.btnPushDeeplinkActivity).setOnClickListener {
            navigate("net.c7j.wna.huawei.PushDeeplinkAndTopicActivity")
        }
    }

    private fun initTurnOnOffSwitchButtons() {
        // "Turned On" is a default value of push kit after initialization
        findViewById<MaterialButton>(R.id.btnPushTurnOn).setOnClickListener {
            HmsMessaging.getInstance(this).turnOnPush()
            toast("Now you can receive push notifications")
        }
        findViewById<MaterialButton>(R.id.btnPushTurnOff).setOnClickListener {
            HmsMessaging.getInstance(this).turnOffPush()
            toast("You will no longer receive push notifications")
        }
    }

    /**
     * General scenario, when you have only one logical backend server which is sending pushes
     * getToken() works on EMUI version 10+. On older EMUI is provided by onNewToken()
     * @see HuaweiPushService.onNewToken
     * In this example token is delivered from service to activity through local broadcast receiver.
     */
    private fun getToken() {
        CoroutineScope(Dispatchers.IO).launch(handler) {
            try {   // read from agconnect-services.json
                val appId = AGConnectOptionsBuilder().build(this@PushMainActivity).getString("client/app_id")
                val token = HmsInstanceId.getInstance(this@PushMainActivity).getToken(appId, "HCM")
                if (!TextUtils.isEmpty(token)) {
                    sendRegTokenToServer(token)
                    deviceToken = token
                }
                log("token received: $token")
            } catch (e: ApiException) {
                log("get token failed, $e")
            }
        }
    }

    // Optional, only for those one who really needs it!
    // Multiple sender case. If there are 2,3 or more companies/providers who can deliver
    // push messages to your application, you provide a separate device/project token for each company
    // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/multisender-0000001057626308
    private fun getTokenMultipleSender() {
        CoroutineScope(Dispatchers.IO).launch(handler) {
            try {
                val token = HmsInstanceId.getInstance(this@PushMainActivity).getToken("Project1Name")
                if (!TextUtils.isEmpty(token)) {
                    val project1Token = token
                    // sendRegTokenToProject1Server(project1Token)
                }
                log("project1Name token received: $token")
            } catch (e: ApiException) {
                log("project1Name get token failed, $e")
            }
        }
    }

    private fun sendRegTokenToServer(token: String?) {
        log("sending token to server. token: $token")
    }

    private fun setAutoInitEnabled(enabled: Boolean) {
        // Can be useful for legal reasons in countries like EU before getting user's privacy consent
        HmsMessaging.getInstance(this).isAutoInitEnabled = enabled
    }

    // To enable enhanced functions like send push to audience or A/B tests
    // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-enhanced-audiencesend-0000001154278143
    private fun enableEnhancedFunctions() {
        HiAnalytics.getInstance(this) // this line requires huawei Analytics Kit integration
    }

    /**
     *  If you do not want your app to receive notification messages, please use turnOffPush() method.
     *  @see initTurnOnOffSwitchButtons
     *  Delete token only when it is really necessary - like: user has logged into another account.
     */
    private fun deleteToken() {
        CoroutineScope(Dispatchers.IO).launch(handler) {
            try {
                // Obtain the app ID from the agconnect-services.json file.
                val appId = AGConnectOptionsBuilder().build(this@PushMainActivity).getString("client/app_id")
                val tokenScope = "HCM"
                HmsInstanceId.getInstance(this@PushMainActivity).deleteToken(appId, tokenScope)
            } catch (e: ApiException) {
                log("delete token failed, $e")
            }
            // You can also support Account verification scenario for multiple users case:
            // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/account-profile-0000001057562781
            // For HUAWEI account user use: HUAWEI_PROFILE, for non-huawei account user: CUSTOM_PROFILE
            HmsProfile.getInstance(this@PushMainActivity).addProfile(HUAWEI_PROFILE,"yourProfileId")
        }
    }

    // This method exists only for your convenience to debug/test push kit
    private fun copyTokenToClipboard() {
        val clipboardManager: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("nonsense_data", deviceToken))
        toast("token copied to clipboard: $deviceToken")
    }

    private val handler = CoroutineExceptionHandler { _, exception -> log(exception.toString()) }

    private val pushReceiver : BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            val method = bundle?.getString("method")
            if (method != null && method == "onNewToken") {
                if (bundle.getString("token") != null) {
                    val token = bundle.getString("token")
                    if (!token.isNullOrEmpty()) { deviceToken = token }
                    Toast.makeText(context, "Device token: ${token.toString()}", Toast.LENGTH_LONG).show()
                }
                return
            } else if (bundle?.getString("msg") != null) {
                val content = bundle.getString("msg")
                Toast.makeText(context, content.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

}