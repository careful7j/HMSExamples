package net.c7j.wna.huawei

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.AudioFocusType
import com.huawei.hms.ads.splash.SplashAdDisplayListener
import com.huawei.hms.ads.splash.SplashView
import com.huawei.hms.ads.splash.SplashView.SplashAdLoadListener
import net.c7j.wna.huawei.ads.R

@SuppressLint("CustomSplashScreen")
// This activity shows example of splash screen Ads usage
class SplashActivity: BaseActivity() {

    private lateinit var splashAdView: SplashView
    private lateinit var adDisplayTimeoutHandler : Handler
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        adDisplayTimeoutHandler = Handler(this.mainLooper) {
            if (this.hasWindowFocus()) jump()
            false
        }
        splashAdView = findViewById(R.id.splashAdView)
        loadSplashAd()
    }

    private fun loadSplashAd() {
        splashAdView.setAdDisplayListener(adDisplayListener)
        splashAdView.logoResId = net.c7j.wna.huawei.box.R.mipmap.ic_launcher
        splashAdView.mediaNameResId = net.c7j.wna.huawei.box.R.string.app_name
        splashAdView.audioFocusType = AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            splashAdView.load(
                getString(net.c7j.wna.huawei.box.R.string.ad_splash),
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                AdParam.Builder().build(),
                splashAdLoadListener
            )
        } else {
            splashAdView.load(
                getString(net.c7j.wna.huawei.box.R.string.ad_splash_landscape),
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                AdParam.Builder().build(),
                splashAdLoadListener
            )
        }
        adDisplayTimeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        adDisplayTimeoutHandler.sendEmptyMessageDelayed(MSG_AD_TIMEOUT, AD_TIMEOUT.toLong())
    }

    // Return back to application main screen after Ad show is finished
    private fun jump() {
        if (!isPaused) {
            isPaused = true
            navigate("net.c7j.wna.huawei.AdsNavigationActivity")
            Handler(this.mainLooper).postDelayed({ finish() }, JUMP_TIMEOUT)
        }
    }


    private val splashAdLoadListener: SplashAdLoadListener = object : SplashAdLoadListener() {

        override fun onAdLoaded() {
            toast(getString(net.c7j.wna.huawei.box.R.string.status_load_ad_success))
        }

        override fun onAdFailedToLoad(errorCode: Int) {
            toast(getString(net.c7j.wna.huawei.box.R.string.status_load_ad_fail) + "$errorCode")
            jump()
        }

        override fun onAdDismissed() {
            toast(getString(net.c7j.wna.huawei.box.R.string.status_ad_dismissed))
            jump()
        }
    }

    private val adDisplayListener: SplashAdDisplayListener = object : SplashAdDisplayListener() {
        override fun onAdShowed() {}
        override fun onAdClick() {}
    }

    override fun onStop() {
        adDisplayTimeoutHandler.removeMessages(MSG_AD_TIMEOUT)
        isPaused = true
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
        isPaused = false
        jump()
    }

    override fun onDestroy() {
        super.onDestroy()
        splashAdView.destroyView()
    }

    override fun onPause() {
        super.onPause()
        splashAdView.pauseView()
    }

    override fun onResume() {
        super.onResume()
        splashAdView.resumeView()
    }


    companion object {
        const val JUMP_TIMEOUT = 1000L
        const val AD_TIMEOUT = 7000
        const val MSG_AD_TIMEOUT = 1001
    }
}