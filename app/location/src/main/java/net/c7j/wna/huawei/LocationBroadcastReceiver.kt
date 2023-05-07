package net.c7j.wna.huawei

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.huawei.hms.location.*

/** @See net.c7j.wna.huawei.ActivityRecognitionAndPeriodicLocationActivity */
class LocationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val deliverIntent = Intent(ACTION_DELIVER_LOCATION)
        val action = intent.action
        if (action != null && action == ACTION_PROCESS_LOCATION) {
            // handle activity identification response
            val activityRecognitionResult = ActivityIdentificationResponse.getDataFromIntent(intent)
            if (activityRecognitionResult != null && ActivityRecognitionAndPeriodicLocationActivity.isListenActivityIdentification) {
                val list = activityRecognitionResult.activityIdentificationDatas as ArrayList<ActivityIdentificationData>
                deliverIntent.putParcelableArrayListExtra(EXTRA_HMS_LOCATION_RECOGNITION, list)
            }

            // handle activity conversion response
            val activityConversionResult = ActivityConversionResponse.getDataFromIntent(intent)
            if (activityConversionResult != null && ActivityRecognitionAndPeriodicLocationActivity.isListenActivityIdentification) {
                val list = activityConversionResult.activityConversionDatas as ArrayList<ActivityConversionData>
                deliverIntent.putParcelableArrayListExtra(EXTRA_HMS_LOCATION_CONVERSION, list)
            }

            // handle location coordinates response
            if (LocationResult.hasResult(intent)) {
                val result = LocationResult.extractResult(intent)
                if (result != null) {
                    val list = result.locations as ArrayList<Location>
                    deliverIntent.putParcelableArrayListExtra(EXTRA_HMS_LOCATION_RESULT, list)
                }
            }

            // handle location request availability response
            if (LocationAvailability.hasLocationAvailability(intent)) {
                val locationAvailability = LocationAvailability.extractLocationAvailability(intent)
                deliverIntent.putExtra(
                    EXTRA_HMS_LOCATION_AVAILABILITY, locationAvailability?.isLocationAvailable == true)
            }
        }
        //send local broadcast within our app to handle in related activity
        context.sendBroadcast(deliverIntent)
    }


    companion object {
        const val ACTION_PROCESS_LOCATION = "com.hms.locationkit.activity.location.ACTION_PROCESS_LOCATION"
        const val ACTION_DELIVER_LOCATION = "ACTION_DELIVER_LOCATION"
        const val EXTRA_HMS_LOCATION_RECOGNITION = "EXTRA_HMS_LOCATION_RECOGNITION"
        const val EXTRA_HMS_LOCATION_CONVERSION = "EXTRA_HMS_LOCATION_CONVERSION"
        const val EXTRA_HMS_LOCATION_RESULT = "EXTRA_HMS_LOCATION_RESULT"
        const val EXTRA_HMS_LOCATION_AVAILABILITY = "EXTRA_HMS_LOCATION_AVAILABILITY"

        const val LOCATION_REQUEST_PERIOD = 3000L
    }
}
