package net.c7j.wna.huawei

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability

//::created by c7j at 28.02.2023 5:12 PM
abstract class BaseActivity : AppCompatActivity() {

    //Cross-module navigation
    protected fun navigate(navigationTarget: String) {
        try {
            val intent = Intent()
            intent.setClassName(packageName, navigationTarget)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("Error", "Exception: $e")
            toast("Exception: $e")
        }
    }

    protected fun openWeblink(link: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        } catch (anfe: ActivityNotFoundException) {
            toast("Failed to open web browser")
        }
    }

    protected fun log(text: String) = Log.e("HMS Examples", "" + text)

    protected fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()


    protected fun isHmsAvailable(context: Context?) = HuaweiApiAvailability.getInstance()
        .isHuaweiMobileServicesAvailable(context) == ConnectionResult.SUCCESS

}