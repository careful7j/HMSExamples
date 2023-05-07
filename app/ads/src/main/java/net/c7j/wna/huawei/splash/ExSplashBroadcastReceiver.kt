package net.c7j.wna.huawei.splash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ExSplashBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "com.huawei.hms.ads.EXSPLASH_DISPLAYED") {
            Log.i("EXSPLASH_DISPLAYED", "Received the exsplash ad broadcast, action:$action")
            // Received the broadcast, exsplash ad is displayed.
        }
    }
}