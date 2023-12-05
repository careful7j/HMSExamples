package net.c7j.wna.huawei

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huawei.hms.location.*
import net.c7j.wna.huawei.location.R
import kotlin.random.Random

// This activity shows how to use geofence feature (enter/exit predefined coordinates zone)
// NOT EVERY DEVICE SUPPORTS THIS FEATURE!
// Please check the list of supported devices at here (App Services > Location Kit > Appendixies):
// https://developer.huawei.com/consumer/en/doc/HMSCore-Guides/supported-geofencing-devices-0000001674873585
class GeofenceActivity : BaseActivity() {

    private var geofenceService: GeofenceService? = null
    private var idList: ArrayList<String> = ArrayList()
    private var geofenceList: ArrayList<Geofence> = ArrayList()
    private var pendingIntent: PendingIntent? = null

    // Is only required to mock user location.
    // Seems mock location may not have effect on geofence at some devices
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null


    //  It is recommended that the geofence radius be at least 200 meters.
    //  Precision cannot be assured if the geofence radius is less than 200 meters.
    //  A maximum of 100 geo fences can be added for each app on a device.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geofence_activity)
        initViews()
        geofenceService = LocationServices.getGeofenceService(this)
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationPermissions.launch(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        )
    }

    private fun addGeofence() {
        val uniqueId = Random(10000).toString()
        pendingIntent = getPendingIntent()
        geofenceList.add(
            Geofence.Builder()
                .setUniqueId(uniqueId)
                .setValidContinueTime(-1) // negative value means endless life time
                .setDwellDelayTime(3) // "stays within geofence" is triggered after 3 seconds
                .setRoundArea(BANGKOK_LATITUDE, BANGKOK_LONGITUDE, 1000f) // 1000f = 1.0 km radius
                // Trigger a callback when the user enters or leaves the geofence:
                .setConversions(
                        Geofence.ENTER_GEOFENCE_CONVERSION or
                        Geofence.EXIT_GEOFENCE_CONVERSION or
                        Geofence.DWELL_GEOFENCE_CONVERSION) // stays within Geofence
                .build())

        // If same id is created twice - IllegalArgumentException is thrown
        idList.add(uniqueId)
        geofenceService?.createGeofenceList(getAddGeofenceRequest(), pendingIntent)
            ?.addOnCompleteListener { task -> toast("Geofence added result: ${task.isSuccessful}") }
        registerReceiver(geofenceReceiver, IntentFilter(GeofenceBroadcastReceiver.ACTION_DELIVER_GEOFENCE_INFO))
    }

    private fun getAddGeofenceRequest(): GeofenceRequest? {
        val builder = GeofenceRequest.Builder()
        // Triggers callback immediately after geofence is added if user is within or outside geofence
        builder.setInitConversions(
                Geofence.ENTER_GEOFENCE_CONVERSION or
                Geofence.EXIT_GEOFENCE_CONVERSION or
                Geofence.DWELL_GEOFENCE_CONVERSION) // stays within Geofence
        builder.createGeofenceList(geofenceList)
        return builder.build()
    }

    private fun removeGeofence() {
        geofenceService?.deleteGeofenceList(idList)?.addOnCompleteListener { task ->
            toast("Geofence removed result: ${task.isSuccessful}")
            if (task.isSuccessful) {
                idList.clear()
                geofenceList.clear()
            }
        }
        try {
            unregisterReceiver(geofenceReceiver)
        } catch (iae: IllegalArgumentException) { toast("No geofence registered") }
    }

    // Dynamically register GeoFenceBroadcastReceiver through PendingIntent.
    // A broadcast message will be sent when the geofence is triggered.
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.action = GeofenceBroadcastReceiver.ACTION_PROCESS_GEOFENCE
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }
    }


    private val geofenceReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == GeofenceBroadcastReceiver.ACTION_DELIVER_GEOFENCE_INFO) {
                val geofenceData = GeofenceData.getDataFromIntent(intent)
                if (geofenceData != null) {
                    var geofenceOutput = when (geofenceData.conversion) {
                        1 -> "Enters geofence"
                        2 -> "Exits geofence"
                        4 -> "Stays within geofence"
                        else -> "Unsupported event"
                    }
                    geofenceOutput += " at: \n${geofenceData.convertingLocation.latitude}, " +
                            "\n${geofenceData.convertingLocation.longitude}"
                    geofenceOutput += "\n\nerror code: " +
                            if (geofenceData.errorCode == -1) "no error" else geofenceData.errorCode
                    geofenceOutput += "\noperation success: ${geofenceData.isSuccess}"
                    geofenceOutput += "\n\ngeofence list:\n ${geofenceData.convertingGeofenceList}"

                    MaterialAlertDialogBuilder(context)
                        .setTitle("Geofence event")
                        .setMessage(geofenceOutput)
                        .setNegativeButton("exit") { dialog, _ ->
                            dialog?.dismiss()
                        }
                        .setCancelable(true)
                        .show()
                }
            }
        }
    }

    private val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        var count = 0
        if (map[Manifest.permission.ACCESS_FINE_LOCATION] == true) count++
        if (map[Manifest.permission.ACCESS_COARSE_LOCATION] == true) count++
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) if (map[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true) count++
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && count < 2 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && count < 3) {
            toast("Not all location permissions are granted, functions are limited")
        }
    }

    private fun initViews() {
        findViewById<MaterialButton>(R.id.btnAddGeofence).setOnClickListener { addGeofence() }
        findViewById<MaterialButton>(R.id.btnRemoveGeofence).setOnClickListener { removeGeofence() }
        findViewById<MaterialButton>(R.id.btnMockBangkok).setOnClickListener {
            setMockLocation(BANGKOK_LATITUDE, BANGKOK_LONGITUDE)
        }
        findViewById<MaterialButton>(R.id.btnMockNorthPole).setOnClickListener {
            setMockLocation(NORTH_POLE_LATITUDE, NORTH_POLE_LONGITUDE)
        }
        findViewById<MaterialButton>(R.id.btnStopMockLocation).setOnClickListener {
            mFusedLocationProviderClient?.setMockMode(false)
        }
    }

    // To enable the mock function:
    // 1. add the android.permission.ACCESS_MOCK_LOCATION permission to your AndroidManifest.xml
    // 2. set the application to the mock location app at device's developer options
    private fun setMockLocation(latitude: Double, longitude: Double) {
        try {
            mFusedLocationProviderClient?.setMockMode(true)
            val mockLocation = Location(LocationManager.GPS_PROVIDER)
            mockLocation.longitude = longitude
            mockLocation.latitude = latitude
            val voidTask = mFusedLocationProviderClient?.setMockLocation(mockLocation)
            voidTask
                ?.addOnSuccessListener { toast("mock location success") }
                ?.addOnFailureListener { e -> toast("mock location onFailure:${e.message}") }
        } catch (e: Exception) { toast("setMockLocation exception:${e.message}") }
    }

    companion object {
        // You can replace it with your own coordinates to test at your location
        const val BANGKOK_LATITUDE = 13.756
        const val BANGKOK_LONGITUDE = 100.533

        const val NORTH_POLE_LATITUDE = 90.0
        const val NORTH_POLE_LONGITUDE = 135.0
    }
}

