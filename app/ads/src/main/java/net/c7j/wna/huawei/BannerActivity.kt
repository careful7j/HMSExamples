package net.c7j.wna.huawei

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioGroup
import com.google.android.material.button.MaterialButton
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.BannerAdSize
import com.huawei.hms.ads.banner.BannerView
import net.c7j.wna.huawei.ads.R

// This activity shows example of standard banner Ads usage
class BannerActivity : BaseActivity() {

    private lateinit var bannerView: BannerView
    private lateinit var adFrame: FrameLayout
    private lateinit var btnLoadAd: MaterialButton
    private lateinit var sizeRadioGroup: RadioGroup
    private lateinit var colorRadioGroup: RadioGroup

    private lateinit var loadAd: View.OnClickListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner)
        initViews()
        loadAd = View.OnClickListener {

            if (::bannerView.isInitialized) {
                adFrame.removeView(bannerView)
                bannerView.destroy()
            }
            // Create banner view and put it on our layout
            bannerView = BannerView(this)
            adFrame.addView(bannerView)
            bannerView.adId = getString(net.c7j.wna.huawei.box.R.string.ad_banner)
            bannerView.bannerAdSize = getBannerSize(sizeRadioGroup.checkedRadioButtonId)
            bannerView.setBackgroundColor(getColorBackGround(colorRadioGroup.checkedRadioButtonId))
            bannerView.adListener = adListener
            // Load ad request
            bannerView.loadAd(AdParam.Builder().build())
        }
        btnLoadAd.setOnClickListener(loadAd)
        btnLoadAd.performClick()
    }


    private fun getBannerSize(id: Int): BannerAdSize {
        return when (id) {
            R.id.size_160_600 -> BannerAdSize.BANNER_SIZE_160_600
            R.id.size_300_250 -> BannerAdSize.BANNER_SIZE_300_250
            R.id.size_320_100 -> BannerAdSize.BANNER_SIZE_320_100
            R.id.size_320_50 -> BannerAdSize.BANNER_SIZE_320_50
            R.id.size_360_57 -> BannerAdSize.BANNER_SIZE_360_57
            R.id.size_360_144 -> BannerAdSize.BANNER_SIZE_360_144
            R.id.size_468_60 -> BannerAdSize.BANNER_SIZE_468_60
            R.id.size_728_90 -> BannerAdSize.BANNER_SIZE_728_90
            R.id.size_smart -> BannerAdSize.BANNER_SIZE_SMART
            R.id.size_dynamic -> BannerAdSize.BANNER_SIZE_DYNAMIC
            else -> BannerAdSize.BANNER_SIZE_320_50
        }
    }

    private fun getColorBackGround(id: Int): Int {
        return when (id) {
            R.id.color_white -> Color.WHITE
            R.id.color_black -> Color.BLACK
            R.id.color_red -> Color.RED
            R.id.color_transparent -> Color.TRANSPARENT
            else -> Color.TRANSPARENT
        }
    }

    private val adListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            log( "onAdLoaded")
        }

        override fun onAdFailed(errorCode: Int) {
            log("onAdFailed $errorCode")
            toast("Error code: $errorCode")
        }

        override fun onAdOpened() {
            log("onAdOpened")
        }

        override fun onAdClicked() {
            log("onAdClicked")
        }

        override fun onAdLeave() {
            log("onAdLeave")
        }

        override fun onAdClosed() {
            log("onAdClosed")
        }
    }

    private fun initViews() {
        adFrame = findViewById(R.id.adFrame)
        btnLoadAd = findViewById(R.id.btnLoadAd)
        sizeRadioGroup = findViewById(R.id.sizeRadioGroup)
        colorRadioGroup = findViewById(R.id.colorRadioGroup)
    }

}