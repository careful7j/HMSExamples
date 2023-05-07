package net.c7j.wna.huawei.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import com.huawei.hms.ads.sdk.dialogs.ProtocolDialog
import net.c7j.wna.huawei.BaseActivity
import net.c7j.wna.huawei.ads.R
import net.c7j.wna.huawei.consent.AdsConstant


@SuppressLint("CustomSplashScreen")
// For More settings of Express Splash Ads
// Don't mix with Splash Ads (this two are different)
// see: https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/publisher-service-exsplash-0000001051056577
class ExSplashActivity : BaseActivity() {

    private var exSplashService: ExSplashServiceManager? = null
    private var exSplashBroadcastReceiver: ExSplashBroadcastReceiver? = null
    private var showDialog: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exsplash)

        exSplashBroadcastReceiver = ExSplashBroadcastReceiver()
        val filter = IntentFilter(ACTION_EXPRESS_SPLASH_DISPLAYED)
        registerReceiver(exSplashBroadcastReceiver, filter)
        exSplashService = ExSplashServiceManager(this)
        showDialog = findViewById(R.id.show_dialog)
        showDialog?.setOnClickListener { showProtocolDialog() }

        checkUserConsent()
    }

    override fun onDestroy() {
        super.onDestroy()
        exSplashBroadcastReceiver?.let {
            unregisterReceiver(exSplashBroadcastReceiver)
            exSplashBroadcastReceiver = null
        }
    }

    /** You should show the user protocol dialog and receive user's selection results.*/
    private fun checkUserConsent() {
        val preferences: SharedPreferences = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        when (preferences.getInt(SP_PROTOCOL_KEY, AdsConstant.DEFAULT_SP_CONSENT_VALUE)) {
            // First launch App
            AdsConstant.DEFAULT_SP_CONSENT_VALUE -> showProtocolDialog()
            // The user does not consent agreement.
            AdsConstant.SP_CONSENT_DISAGREE -> exSplashService?.enableUserInfo(false)
            // The user consent agreement.
            else -> exSplashService?.enableUserInfo(true)

        }
    }


    private fun showProtocolDialog() {
        val dialog = ProtocolDialog(this)
        dialog.setCallback(object : ProtocolDialog.ProtocolDialogCallback {

            override fun onAgree() {
                exSplashService?.enableUserInfo(true)
                toast("Try restart app and check the express splash ad.")
            }

            override fun onCancel() {
                exSplashService?.enableUserInfo(false)
                finishAffinity() // Exit app.
            }
        })
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    companion object {
        private const val ACTION_EXPRESS_SPLASH_DISPLAYED = "com.huawei.hms.ads.EXSPLASH_DISPLAYED"
        private const val SP_NAME = "ExSplashSharedPreferences"
        private const val SP_PROTOCOL_KEY = "user_consent_status"
    }
}
