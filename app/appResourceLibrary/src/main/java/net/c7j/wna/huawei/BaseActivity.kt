package net.c7j.wna.huawei

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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


    private fun showPermissionBeggingDialog(name : String?) {
        val title = net.c7j.wna.huawei.box.R.string.dialog_permissions_title
        val body = resources.getString(net.c7j.wna.huawei.box.R.string.dialog_permissions_supporting_text)
        val positive = net.c7j.wna.huawei.box.R.string.dialog_permissions_positive
        val negative = net.c7j.wna.huawei.box.R.string.dialog_permissions_negative

        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(title))
            .setMessage("$name $body")
            .setNegativeButton(resources.getString(negative)) { dialog, _ ->
                dialog?.dismiss()
            }
            .setPositiveButton(resources.getString(positive)) { _, _ ->
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            .setCancelable(true)
            .show()
    }


    protected fun requestPermissionByName(name: String) {
        if (shouldShowRequestPermissionRationale(name)) {
            if (ActivityCompat.checkSelfPermission(this, name) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(name), PERMISSION_OTHER)
            }
        } else if (ActivityCompat.checkSelfPermission(this, name) != PackageManager.PERMISSION_GRANTED) {
            showPermissionBeggingDialog(PERMISSIONS_HUMAN_READABLE_NAME[name])
        }
    }


    // In case you don't use Jetpack permissions library in your project
    protected fun requestLocationPermissions() {
        // Hereby assume your app is not necromancer supporting android below 6
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                ActivityCompat.requestPermissions(this, strings, PERMISSION_FINE_COARSE_LOCATION)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                showPermissionBeggingDialog(PERMISSION_NAME_LOCATION)
            }
        }
    }

    protected fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // Below Android 9 inclusive, permission is a part of normal location permissions
            requestLocationPermissions()
        } else { // Android 10+
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            ) {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    val strings = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                    ActivityCompat.requestPermissions(this, strings, PERMISSION_BACKGROUND_LOCATION)
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    showPermissionBeggingDialog(PERMISSION_NAME_BACKGROUND_LOCATION)
                }
            }
        }
    }


    protected fun log(text: String) = Log.e("HMS Examples", "" + text)

    protected fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()


    protected fun isHmsAvailable(context: Context?) = HuaweiApiAvailability.getInstance()
        .isHuaweiMobileServicesAvailable(context) == ConnectionResult.SUCCESS


    companion object {
        const val PERMISSION_FINE_COARSE_LOCATION = 1
        const val PERMISSION_BACKGROUND_LOCATION = 2
        const val PERMISSION_OTHER = 3

        const val PERMISSION_NAME_LOCATION = "Location"
        const val PERMISSION_NAME_BACKGROUND_LOCATION = "Background Location"

        const val HMS_ACTIVITY_RECOGNITION_OLD = "com.huawei.hms.permission.ACTIVITY_RECOGNITION"

        val PERMISSIONS_HUMAN_READABLE_NAME = mapOf(
            "com.huawei.hms.permission.ACTIVITY_RECOGNITION" to "Physical activity and Detect motion status",
            "android.permission.ACTIVITY_RECOGNITION" to "Physical activity and motion status"
        )
    }

}