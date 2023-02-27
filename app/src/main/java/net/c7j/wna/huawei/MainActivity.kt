package net.c7j.wna.huawei

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability


class MainActivity : AppCompatActivity() {

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

        findViewById<TextView>(R.id.btnHMSAreMissing).setOnClickListener { handleNoHMSAvailable() }
    }


    private fun navigate(navigationTarget: String) {
        try {
            val intent = Intent()
            intent.setClassName(packageName, navigationTarget)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("Error", "Exception: $e")
            toast("Exception: $e")
        }
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
            toast("HMS is ready")
        }
    }

    private fun openWeblink(link: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        } catch (anfe: ActivityNotFoundException) {
            toast("Failed to open web browser")
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()


    private fun isHmsAvailable(context: Context?) = HuaweiApiAvailability.getInstance()
        .isHuaweiMobileServicesAvailable(context) == ConnectionResult.SUCCESS


    companion object {
        const val APPGALLERY_DOWNLOAD_LINK = "https://consumer.huawei.com/en/mobileservices/appgallery/"
    }
}