package net.c7j.wna.huawei

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import net.c7j.wna.huawei.location.R


class LocationMainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.locationmain_activity)

        findViewById<MaterialButton>(R.id.btnSimpleLocation).setOnClickListener {
            navigate("net.c7j.wna.huawei.SimpleLocationActivity")
        }

        findViewById<MaterialButton>(R.id.btnActivityIdentification).setOnClickListener {
            navigate("net.c7j.wna.huawei.ActivityRecognitionAndPeriodicLocationActivity")
        }

        findViewById<MaterialButton>(R.id.btnGeofence).setOnClickListener {
            navigate("net.c7j.wna.huawei.GeofenceActivity")
        }
    }
}