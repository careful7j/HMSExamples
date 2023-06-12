package net.c7j.wna.huawei

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import com.google.android.material.button.MaterialButton
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.InterstitialAd
import net.c7j.wna.huawei.ads.R

// This activity shows example of interstitial Ads usage
class InterstitialActivity : BaseActivity() {

    private lateinit var interstitialAd: InterstitialAd
    private lateinit var loadAd: View.OnClickListener
    private lateinit var loadInterstitialAdBtn: MaterialButton
    private lateinit var interstitialRadioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interstitial)
        loadInterstitialAdBtn = findViewById(R.id.loadInterstitialAdBtn)
        interstitialRadioGroup = findViewById(R.id.interstitialRadioGroup)

        loadAd = View.OnClickListener {
            interstitialAd = InterstitialAd(this)
            interstitialAd.adId = getAdId(interstitialRadioGroup.checkedRadioButtonId)
            interstitialAd.adListener = adListener
            interstitialAd.loadAd(AdParam.Builder().build())
        }
        loadInterstitialAdBtn.setOnClickListener(loadAd)
    }

    private fun getAdId(id: Int): String {
        return when (id) {
            R.id.display_image -> getString(net.c7j.wna.huawei.box.R.string.ad_interestial_img)
            else -> getString(net.c7j.wna.huawei.box.R.string.ad_interestial_vid)
        }
    }

    private fun showInterstitial() {
        when {  // Show ads only when it is loaded
            interstitialAd.isLoaded -> interstitialAd.show(this)
            else -> toast("Ad did not load")
        }
    }


    private val adListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            log("onAdLoaded")
            showInterstitial()
        }

        override fun onAdFailed(errorCode: Int) {
            log("onAdFailed $errorCode")
            toast("$errorCode")
        }

        override fun onAdClosed() {
            log("onAdClosed")
        }
    }

}