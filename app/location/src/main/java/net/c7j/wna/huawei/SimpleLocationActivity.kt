package net.c7j.wna.huawei

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.Task
import com.huawei.hms.location.*
import net.c7j.wna.huawei.location.R

// This activity shows how to get simple location
class SimpleLocationActivity : BaseActivity() {

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient

    private lateinit var tvPosition : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_location_activity)
        initViews()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)

        configureOneTimeLocationRequest()
        initLocationCallback()
    }

    private fun initViews() {
        findViewById<MaterialButton>(R.id.btnCheckLocation).setOnClickListener { requestLocationUpdatesWithCallback() }
        findViewById<MaterialButton>(R.id.btnLastLocation).setOnClickListener { getLastLocation() }
        tvPosition = findViewById(R.id.tvPosition)
    }


    private fun configureOneTimeLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = 1000
            needAddress = true
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            // Sets the type of the returned coordinate:
            // COORDINATE_TYPE_WGS84 - An international standard is WGS-84(Earth coordinates) or
            // World Geodetic System, where 84 is the latest revision of this standard applied in 1984.
            // This standard is used by GPS. Each time you use Google Maps(or almost any maps) you can thank WGS-84.
            // COORDINATE_TYPE_GCJ02 - GCJ-02 is based on WGS-84 and uses an obfuscation algorithm,
            // which adds random offsets to both the latitude and longitude to improve China national security.
            // If the WGS-84 point is placed on the GCJ-02 map, on average, there will be
            // a location offset for about 300-500m. The default value is COORDINATE_TYPE_WGS84.
            coordinateType = LocationRequest.COORDINATE_TYPE_WGS84
            numUpdates = 1 // We want to receive location only once
        }

        // SDK supports conversion of the obtained WGS84 coordinates into GCJ02 coordinates:
        // As .convertCoord(latitude, longitude, convert mode shall be = 1 if WGS84 to GCJ02)
        val convertedLonLat = LocationUtils.convertCoord(39.9042 ,116.4074, 1)
    }


    private fun initLocationCallback() {
        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {

                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult != null) {
                        val locations: List<Location> = locationResult.locations
                        if (locations.isNotEmpty()) {
                            tvPosition.text = ""
                            for (location in locations) {
                                val locationStr = "" + tvPosition.text + "${location.longitude}, ${location.latitude}\n"
                                tvPosition.text = locationStr
                            }
                        } else tvPosition.text = getString(net.c7j.wna.huawei.box.R.string.get_loc_failed)
                    } else tvPosition.text = getString(net.c7j.wna.huawei.box.R.string.get_loc_failed)
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                    locationAvailability?.let {
                        log("onLocationAvailability isLocationAvailable:${locationAvailability.isLocationAvailable}")
                    }
                }
            }
        }
    }

    /**
     * Requests location updates with a callback on the specified Looper thread (Recommended).
     * 1. Call SettingsClient.checkLocationSettings to check/validate devices settings/parameters
     * 2. Call FusedLocationProviderClient object requestLocationUpdates() method
     */
    private fun requestLocationUpdatesWithCallback() {
        try {
            tvPosition.text = getString(net.c7j.wna.huawei.box.R.string.get_loc_searching)
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(locationRequest)
            val locationSettingsRequest = builder.build()

            // Checks whether relevant location settings are valid (necessary according to the doc)
            // Related documentation: https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/settingsclient-0000001051066118#section1653772245611
            val locationSettingsResponseTask: Task<LocationSettingsResponse> =
                settingsClient.checkLocationSettings(locationSettingsRequest)

            locationSettingsResponseTask
                .addOnSuccessListener { locationSettingsResponse: LocationSettingsResponse? ->
                    log( "Settings validation result: OK  {$locationSettingsResponse}")
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest, locationCallback, Looper.getMainLooper())
                        .addOnSuccessListener { log("requestLocationUpdatesWithCallback onSuccess") }
                        .addOnFailureListener { e -> log("requestLocationUpdatesWithCallback onFailure:${e.message}") }
                }
                .addOnFailureListener { e: Exception -> log( "Settings validation result: FAIL :${e.message}") }
        } catch (e: Exception) {
            log("requestLocationUpdatesWithCallback Exception: ${e.message}")
            tvPosition.text = getString(net.c7j.wna.huawei.box.R.string.get_loc_failed)
        }
    }


    @SuppressLint("SetTextI18n")
    // The value NULL may be returned in the following scenarios:
    // - The location function has never been used.
    // - The location function is disabled.
    // - The device was restored to factory settings.
    private fun getLastLocation() {
        try {
            tvPosition.text = getString(net.c7j.wna.huawei.box.R.string.get_loc_searching)
            val lastLocation: Task<Location> = fusedLocationProviderClient.lastLocation
            lastLocation.addOnSuccessListener(OnSuccessListener { location ->
                if (location == null) {
                    tvPosition.text = getString(net.c7j.wna.huawei.box.R.string.get_loc_failed)
                    log("location is null - is location permission granted?")
                    return@OnSuccessListener
                }
                with(location) { tvPosition.text = "$longitude, $latitude" }
            }).addOnFailureListener { e ->
                tvPosition.text = getString(net.c7j.wna.huawei.box.R.string.get_loc_null)
                requestLocationPermissions()
                log("failed: $e")
            }
        } catch (e: Exception) { log("Exception: $e") }
    }

    // Handle user's answer on location permission request dialog
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_FINE_COARSE_LOCATION) {
            if ( grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                log("onRequestPermissionsResult: apply LOCATION PERMISSION successful")
                requestLocationUpdatesWithCallback()
            } else {
                if ( grantResults.size > 1 && (grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                            grantResults[1] == PackageManager.PERMISSION_GRANTED)
                ) {
                    log("onRequestPermissionsResult: apply LOCATION PERMISSION partially successful")
                    requestLocationUpdatesWithCallback()
                } else {
                    log("onRequestPermissionsResult: apply LOCATION PERMISSION failed")
                }
            }
        }

    }

}