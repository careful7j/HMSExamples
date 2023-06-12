package net.c7j.wna.huawei

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import com.huawei.hms.location.*
import net.c7j.wna.huawei.LocationBroadcastReceiver.Companion.ACTION_DELIVER_LOCATION
import net.c7j.wna.huawei.LocationBroadcastReceiver.Companion.ACTION_PROCESS_LOCATION
import net.c7j.wna.huawei.LocationBroadcastReceiver.Companion.EXTRA_HMS_LOCATION_CONVERSION
import net.c7j.wna.huawei.LocationBroadcastReceiver.Companion.EXTRA_HMS_LOCATION_RECOGNITION
import net.c7j.wna.huawei.LocationBroadcastReceiver.Companion.EXTRA_HMS_LOCATION_RESULT
import net.c7j.wna.huawei.LocationBroadcastReceiver.Companion.LOCATION_REQUEST_PERIOD
import net.c7j.wna.huawei.location.R

// This activity shows how to:
// - periodically receive location
// - recognize user current physical activity and physical activity conversion (change from one to another)
class ActivityRecognitionAndPeriodicLocationActivity : BaseActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var activityIdentificationService: ActivityIdentificationService
    private var transitions: MutableList<ActivityConversionInfo> = mutableListOf()

    private var pendingIntent: PendingIntent? = null

    private lateinit var toggleRecognition : SwitchCompat
    private lateinit var tvRecognition : TextView
    private lateinit var tvConversion : TextView
    private lateinit var tvLocations : TextView

    private lateinit var strActivityLocationsFailed : String
    private lateinit var strActivityRecognitionFailed : String
    private lateinit var strActivityConversionFailed : String

    private val activityRecognitionPermissionName = if (SDK_INT <= Build.VERSION_CODES.P)
        "com.huawei.hms.permission.ACTIVITY_RECOGNITION" else Manifest.permission.ACTIVITY_RECOGNITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recognition_conversion_activity)

        initViews()
        requestBackgroundLocationPermission()

        pendingIntent = getPendingIntent()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        activityIdentificationService = ActivityIdentification.getService(this)

        toggleRecognition.setOnCheckedChangeListener { _: CompoundButton, enabled: Boolean ->
            activityRecognitionPermission.launch(activityRecognitionPermissionName)
            if (enabled) startUserActivityTracking() else stopUserActivityTracking()
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(
            this@ActivityRecognitionAndPeriodicLocationActivity, LocationBroadcastReceiver::class.java)

        intent.action = ACTION_PROCESS_LOCATION
        return if (SDK_INT <= Build.VERSION_CODES.R) {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }
    }


    private val gpsReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_DELIVER_LOCATION) {
                updateActivityIdentificationUI(intent.parcelableArrayList(EXTRA_HMS_LOCATION_RECOGNITION))
                updateActivityConversionUI(intent.parcelableArrayList(EXTRA_HMS_LOCATION_CONVERSION))
                updateLocationsUI(intent.parcelableArrayList(EXTRA_HMS_LOCATION_RESULT))
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        toggleRecognition.isChecked = false
    }


    fun updateActivityIdentificationUI(statuses: ArrayList<ActivityIdentificationData>?) {
        statuses?.let {
            var out = ""
            statuses.forEach { out = out + "NOW: " + statusFromCode(it.identificationActivity) + " " }
            tvRecognition.text = out
        }
    }


    fun updateActivityConversionUI(statuses: ArrayList<ActivityConversionData>?) {
        statuses?.let {
            var out = ""
            statuses.forEach {
                out = out + statusFromCode(it.activityType) + " " +
                        (if (it.conversionType == 0) "START" else "END") + " "
            }
            tvConversion.text = out
        }
    }


    fun updateLocationsUI(locations: ArrayList<Location>?) {
        locations?.let {
            var out = ""
            locations.forEach { out = out + it.latitude + ", " + it.longitude + " " }
            tvLocations.text = out
        }
    }

    private fun startUserActivityTracking() {
        registerReceiver(gpsReceiver, IntentFilter(ACTION_DELIVER_LOCATION))
        requestActivityUpdates()
    }


    private fun stopUserActivityTracking() {
        unregisterReceiver(gpsReceiver)
        removeActivityUpdates()
    }

    private fun requestActivityUpdates() {
        try {
            if (pendingIntent != null) removeActivityUpdates()
            pendingIntent = getPendingIntent()
            isListenActivityIdentification = true

            activityIdentificationService.createActivityIdentificationUpdates(LOCATION_REQUEST_PERIOD, pendingIntent)
                .addOnSuccessListener { log("createActivityIdentificationUpdates onSuccess") }
                .addOnFailureListener { e -> log("createActivityIdentificationUpdates onFailure:" + e.message) }

            if (transitions.isEmpty()) transitions = getTransitions()
            val activityTransitionRequest = ActivityConversionRequest(transitions)
            activityIdentificationService
                .createActivityConversionUpdates(activityTransitionRequest, pendingIntent)
                .addOnSuccessListener { log("createActivityConversionUpdates onSuccess") }
                .addOnFailureListener { e -> log("createActivityConversionUpdates onFailure:" + e.message) }

            val locationRequest = LocationRequest().apply {
                this.interval = 5000
                this.needAddress = true
                this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient
                .requestLocationUpdates(locationRequest, pendingIntent)
                .addOnSuccessListener { log("requestLocationUpdatesWithIntent onSuccess") }
                .addOnFailureListener { e -> log("requestLocationUpdatesWithIntent onFailure:" + e.message) }

        } catch (e: java.lang.Exception) { log("Exception:" + e.message) }
    }


    private fun getTransitions(): MutableList<ActivityConversionInfo> {
        val activityTransition = ActivityConversionInfo.Builder()
        val transitionOptions : ArrayList<Pair<Int, Int>> = arrayListOf(
            100 to 0, 100 to 1, 101 to 0, 101 to 1, 102 to 0, 102 to 1, 103 to 0, 103 to 1,
            107 to 0, 107 to 1, 108 to 0, 108 to 1 )

        for (index in transitionOptions.indices) {
            val temp = transitionOptions[index]
            activityTransition.apply {
                setActivityType(temp.first)
                setConversionType(temp.second)
            }
            transitions.add(activityTransition.build())
        }
        return transitions
    }


    private fun removeActivityUpdates() {
        try {
            isListenActivityIdentification = false

            activityIdentificationService.deleteActivityIdentificationUpdates(pendingIntent)
                .addOnSuccessListener { log("deleteActivityIdentificationUpdates onSuccess") }
                .addOnFailureListener { e -> log("deleteActivityIdentificationUpdates onFailure:" + e.message) }

            activityIdentificationService.deleteActivityConversionUpdates(pendingIntent)
                .addOnSuccessListener { log("deleteActivityConversionUpdates onSuccess") }
                .addOnFailureListener { e -> log("deleteActivityConversionUpdates onFailure:" + e.message) }

            fusedLocationProviderClient.removeLocationUpdates(pendingIntent)
                .addOnSuccessListener { log("removeLocationUpdatesWithIntent onSuccess") }
                .addOnFailureListener { e -> log("removeLocationUpdatesWithIntent onFailure:" + e.message) }

        } catch (e: java.lang.Exception) { log("removeActivityUpdates Exception:" + e.message) }
    }


    private fun initViews() {
        tvRecognition = findViewById(R.id.tvRecognition)
        tvConversion = findViewById(R.id.tvConversion)
        tvLocations = findViewById(R.id.tvLocations)
        toggleRecognition = findViewById(R.id.toggleRecognition)

        strActivityLocationsFailed = getString(net.c7j.wna.huawei.box.R.string.str_activity_locations_failed)
        strActivityConversionFailed = getString(net.c7j.wna.huawei.box.R.string.str_activity_conversion_failed)
        strActivityRecognitionFailed = getString(net.c7j.wna.huawei.box.R.string.str_activity_recognition_failed)
    }

    private val activityRecognitionPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) toast("Grant physical activity recognition permission, please")
    }

    // For parcelableArrayList workaround as here parcelableArrayList() see:
    // https://stackoverflow.com/questions/73019160/android-getparcelableextra-deprecated/73311814#73311814
    inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
        SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
    }

    private fun statusFromCode(var1: Int): String = when (var1) {
        100 -> "VEHICLE"
        101 -> "BIKE"
        102 -> "FOOT"
        103 -> "STILL"
        104 -> "OTHERS"
        105 -> "TILTING"
        107 -> "WALKING"
        108 -> "RUNNING"
        else -> "UNDEFINED"
    }

    companion object {
        var isListenActivityIdentification = true
    }

}