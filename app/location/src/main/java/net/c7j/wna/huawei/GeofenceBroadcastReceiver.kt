package net.c7j.wna.huawei

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huawei.hms.location.GeofenceData

//::created by c7j at 08.04.2023 9:34 PM
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action == ACTION_PROCESS_GEOFENCE) {
            val geoFenceData = GeofenceData.getDataFromIntent(intent)
            geoFenceData?.let {
                val deliverIntent = Intent(ACTION_DELIVER_GEOFENCE_INFO)
                deliverIntent.putExtras(intent)
                //send local broadcast within our app to handle in related activity
                context.sendBroadcast(deliverIntent)
            }
        }
    }


    companion object {
        const val ACTION_PROCESS_GEOFENCE =
            "com.hms.locationkit.geofence.GeoFenceBroadcastReceiver.ACTION_PROCESS_LOCATION"
        const val ACTION_DELIVER_GEOFENCE_INFO = "ACTION_DELIVER_GEOFENCE_INFO"
    }
}