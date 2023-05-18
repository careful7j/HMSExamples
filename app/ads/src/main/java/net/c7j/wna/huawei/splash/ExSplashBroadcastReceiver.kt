package net.c7j.wna.huawei.splash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.c7j.wna.huawei.log

class ExSplashBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "com.huawei.hms.ads.EXSPLASH_DISPLAYED") {
            log("Received the Ex Splash ad broadcast, displaying: $action")
        }
    }
}