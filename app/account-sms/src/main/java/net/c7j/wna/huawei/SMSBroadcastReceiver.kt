package net.c7j.wna.huawei

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import com.huawei.hms.common.api.CommonStatusCodes
import com.huawei.hms.support.api.client.Status
import com.huawei.hms.support.sms.common.ReadSmsConstant


class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val bundle = intent!!.extras
        if (bundle != null) {
            val status: Status? = if (Build.VERSION.SDK_INT >= TIRAMISU) {
                bundle.getParcelable(ReadSmsConstant.EXTRA_STATUS, Status::class.java)
            } else @Suppress("DEPRECATION") bundle.getParcelable(ReadSmsConstant.EXTRA_STATUS)
            if (status?.statusCode == CommonStatusCodes.TIMEOUT) {
                // Process system timeout
            } else if (status?.statusCode == CommonStatusCodes.SUCCESS) {
                if (bundle.containsKey(ReadSmsConstant.EXTRA_SMS_MESSAGE)) {
                    bundle.getString(ReadSmsConstant.EXTRA_SMS_MESSAGE)?.let {
                        val local = Intent()
                        local.action = ACTION_SMS_READ_BROADCAST
                        local.putExtra(KEY_SMS_TEXT, it)
                        context?.sendBroadcast(local)
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_SMS_READ_BROADCAST = "service.to.activity.transfer"
        const val KEY_SMS_TEXT = "sms"
    }
}