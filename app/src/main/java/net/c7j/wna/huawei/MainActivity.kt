package net.c7j.wna.huawei

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleNoHMSAvailable()
    }

    fun btnClicked(v: View) {
        navigate(when (v.id) {
            R.id.btnNavigateToAnalytics -> "net.c7j.wna.huawei.AnalyticsActivity"
            R.id.btnNavigateToAds -> "net.c7j.wna.huawei.AdsRewardedNavigationActivity"
            R.id.btnNavigateToLocation -> "net.c7j.wna.huawei.LocationMainActivity"
            R.id.btnNavigateToMaps -> "net.c7j.wna.huawei.MapsMainActivity"
            R.id.btnNavigateToPush -> "net.c7j.wna.huawei.PushMainActivity"
            R.id.btnNavigateToIap -> "net.c7j.wna.huawei.IapMainActivity"
            R.id.btnNavigateToFingerprint -> "net.c7j.wna.huawei.FingerprintActivity"
            R.id.btnNavigateToSafetyDetect -> "net.c7j.wna.huawei.SafetyDetectActivity"
            R.id.btnNavigateToAccount -> "net.c7j.wna.huawei.AccountActivity"
            R.id.btnNavigateToScan -> "net.c7j.wna.huawei.ScanActivity"
            R.id.btnNavigateToML -> "net.c7j.wna.huawei.MLMainActivity"
            R.id.btnHMSAreMissing -> "".also {handleNoHMSAvailable() }
            else -> ""
        })
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