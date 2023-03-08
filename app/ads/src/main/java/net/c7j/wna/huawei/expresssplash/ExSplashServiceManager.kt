package net.c7j.wna.huawei.expresssplash

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import com.huawei.hms.ads.ExSplashService


class ExSplashServiceManager(private var context: Context?) {

    private var serviceConnection: ServiceConnection? = null
    private var exSplashService: ExSplashService? = null
    private var enable = false

    fun enableUserInfo(enable: Boolean) {
        this.enable = enable
        bindService()
    }

    private fun bindService(): Boolean {
        log( "bindService")
        serviceConnection = ExSplashServiceConnection(context)
        val intent = Intent(ACTION_EXPRESS_SPLASH)
        intent.setPackage(PACKAGE_NAME)
        val result = (context as Context).bindService(
            intent, serviceConnection as ServiceConnection, Context.BIND_AUTO_CREATE)
        log( "bindService result: $result")
        return result
    }

    private fun unbindService() {
        log( "unbindService")
        if (context == null) {
            log( "context is null")
            return
        }
        serviceConnection?.let {
            (context as Context).unbindService(it)
            exSplashService = null
            context = null
        }
    }

    inner class ExSplashServiceConnection(private val context: Context?) : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            logi( "onServiceConnected")
            exSplashService = ExSplashService.Stub.asInterface(service)
            exSplashService?.let {
                try {
                    (exSplashService as ExSplashService).enableUserIxnfo(enable)
                    logi( "enableUserInfo done")
                } catch (e: RemoteException) {
                    logi( "enableUserInfo error")
                } finally {
                    context?.unbindService(this)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            log( "onServiceDisconnected")
        }
    }

    private fun log(text: String) = Log.e(ExSplashServiceManager::class.java.simpleName, "" + text)
    private fun logi(text: String) = Log.e("ExSplashConnection", "" + text)


    companion object {

        private const val ACTION_EXPRESS_SPLASH = "com.huawei.hms.ads.EXSPLASH_SERVICE"
        private const val PACKAGE_NAME = "com.huawei.hwid"

    }

}