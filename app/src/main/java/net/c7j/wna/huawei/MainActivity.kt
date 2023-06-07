package net.c7j.wna.huawei

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNavigation()
        handleNoHMSAvailable()
    }


    private fun initNavigation() {
        findViewById<MaterialButton>(R.id.btnNavigateToAnalytics).setOnClickListener {
            navigate("net.c7j.wna.huawei.AnalyticsActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateToAds).setOnClickListener {
            navigate("net.c7j.wna.huawei.AdsNavigationActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateToLocation).setOnClickListener {
            navigate("net.c7j.wna.huawei.LocationMainActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateToMaps).setOnClickListener {
            navigate("net.c7j.wna.huawei.MapsMainActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateToPush).setOnClickListener {
            navigate("net.c7j.wna.huawei.PushMainActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateToIap).setOnClickListener {
            navigate("net.c7j.wna.huawei.IapMainActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateToFingerprint).setOnClickListener {
            navigate("net.c7j.wna.huawei.FingerprintActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateToSafetyDetect).setOnClickListener {
            navigate("net.c7j.wna.huawei.SafetyDetectActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateToAccount).setOnClickListener {
            navigate("net.c7j.wna.huawei.AccountActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateToScan).setOnClickListener {
            navigate("net.c7j.wna.huawei.ScanActivity")
        }
        findViewById<TextView>(R.id.btnHMSAreMissing).setOnClickListener { handleNoHMSAvailable() }
    }


    private fun handleNoHMSAvailable() {
        val title = net.c7j.wna.huawei.box.R.string.ag_dialog_title
        val body = net.c7j.wna.huawei.box.R.string.ag_dialog_supporting_text
        val download = net.c7j.wna.huawei.box.R.string.ag_dialog_go_download
        val later = net.c7j.wna.huawei.box.R.string.ag_dialog_later

        if (!isHmsAvailable(applicationContext)) {
            findViewById<TextView>(R.id.btnHMSAreMissing)?.visibility = View.VISIBLE
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(title))
                .setMessage(resources.getString(body))
                .setNegativeButton(resources.getString(download)) { _, _ ->
                    openWeblink(APPGALLERY_DOWNLOAD_LINK)
                }
                .setPositiveButton(resources.getString(later)) { dialog, _ ->
                    dialog?.dismiss()
                }
                .setCancelable(false)
                .show()

        } else {
            findViewById<TextView>(R.id.btnHMSAreMissing)?.visibility = View.INVISIBLE
        }
    }

    companion object {
        const val APPGALLERY_DOWNLOAD_LINK = "https://consumer.huawei.com/en/mobileservices/appgallery/"
    }
}