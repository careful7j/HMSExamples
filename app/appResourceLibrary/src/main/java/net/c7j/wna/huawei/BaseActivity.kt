package net.c7j.wna.huawei

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability


abstract class BaseActivity : AppCompatActivity() {

    //Cross-module navigation
    protected fun navigate(destination: String) {
        if (destination == "") return
        try {
            val intent = Intent()
            intent.setClassName(packageName, destination)
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

    protected fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()


    protected fun isHmsAvailable(context: Context?) = HuaweiApiAvailability.getInstance()
        .isHuaweiMobileServicesAvailable(context) == ConnectionResult.SUCCESS


    protected fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

}

fun log(message: Any?) = Log.e("HMS Examples", "$message")