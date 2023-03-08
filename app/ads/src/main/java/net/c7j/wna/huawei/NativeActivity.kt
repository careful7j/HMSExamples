package net.c7j.wna.huawei

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.VideoOperator.VideoLifecycleListener
import com.huawei.hms.ads.nativead.*
import net.c7j.wna.huawei.ads.R

class NativeActivity : BaseActivity() {

    private lateinit var nativeAd: NativeAd
    private lateinit var loadAd: View.OnClickListener
    private lateinit var nativeAdScrollView: ScrollView
    private lateinit var btnLoadAd: MaterialButton
    private lateinit var typeRadioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native)
        initViews()
        loadAd = View.OnClickListener {
            // Configuration settings: https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/nativeadconfiguration-builder-0000001050064912
            val adConfiguration = NativeAdConfiguration.Builder().build()
            NativeAdLoader.Builder(applicationContext, getAdId())
                .setNativeAdLoadedListener(nativeAdLoadedListener)
                .setAdListener(adListener)
                .setNativeAdOptions(adConfiguration)
                .build()
                .loadAd(AdParam.Builder().build())
        }
        btnLoadAd.setOnClickListener(loadAd)
        btnLoadAd.performClick() // Load first add on Activity created
    }

    private val adListener = object : AdListener() {
        override fun onAdFailed(errorCode: Int) = toast("Error code: $errorCode")
    }

    private val nativeAdLoadedListener = NativeAd.NativeAdLoadedListener {
        showNativeAd(it)
    }

    private fun showNativeAd(nativeAd: NativeAd) {
        if (::nativeAd.isInitialized) this.nativeAd.destroy()
        this.nativeAd = nativeAd
        val nativeView = layoutInflater.inflate(getLayoutType(), null) as NativeView
        initNativeAdView(this.nativeAd, nativeView)
        nativeAdScrollView.removeAllViews()
        nativeAdScrollView.addView(nativeView)
    }

    private fun initNativeAdView(nativeAd: NativeAd?, nativeView: NativeView) {
        // Init Views inside NativeView
        nativeView.titleView = nativeView.findViewById(R.id.ad_title)
        nativeView.mediaView = nativeView.findViewById<View>(R.id.ad_media) as MediaView
        nativeView.adSourceView = nativeView.findViewById(R.id.ad_source)
        nativeView.callToActionView = nativeView.findViewById(R.id.ad_call_to_action)

        // Fill it with data
        (nativeView.titleView as TextView).text = nativeAd?.title
        nativeView.mediaView.setMediaContent(nativeAd?.mediaContent)
        when {
            nativeAd?.adSource != null -> {
                (nativeView.adSourceView as TextView).text = nativeAd.adSource
                nativeView.adSourceView.visibility = View.VISIBLE
            } else -> nativeView.adSourceView.visibility = View.INVISIBLE
        }
        when {
            nativeAd?.callToAction != null -> {
                (nativeView.callToActionView as Button).text = nativeAd.callToAction
                nativeView.callToActionView.visibility = View.VISIBLE
            } else -> nativeView.callToActionView.visibility = View.INVISIBLE
        }

        // If Ad contains Video, we shall set up VideoLifecycleListener
        val videoOperator = nativeAd?.videoOperator
        if (videoOperator != null && videoOperator.hasVideo()) {
            videoOperator.videoLifecycleListener = videoLifecycleListener
        }
        nativeView.setNativeAd(nativeAd)
    }

    private val videoLifecycleListener: VideoLifecycleListener = object : VideoLifecycleListener() {
        override fun onVideoStart() {}
        override fun onVideoPlay() {}
        override fun onVideoEnd() {}
    }

    // Small layout for ads, large layout for videos and large ads
    private fun getLayoutType(): Int {
        return when (typeRadioGroup.checkedRadioButtonId) {
            R.id.radio_button_small -> R.layout.native_small_template
            else -> R.layout.native_large_template
        }
    }

    // Advertisement unit id depending on type of ad slot
    private fun getAdId(): String {
        return when (typeRadioGroup.checkedRadioButtonId) {
            R.id.radio_button_small -> getString(net.c7j.wna.huawei.box.R.string.ad_native_small)
            R.id.radio_button_large -> getString(net.c7j.wna.huawei.box.R.string.ad_native_large)
            R.id.radio_button_video -> getString(net.c7j.wna.huawei.box.R.string.ad_native_video)
            else -> ""
        }
    }

    private fun initViews() {
        btnLoadAd = findViewById(R.id.btnLoadAd)
        nativeAdScrollView = findViewById(R.id.nativeAdScrollView)
        typeRadioGroup = findViewById(R.id.typeRadioGroup)
    }
}