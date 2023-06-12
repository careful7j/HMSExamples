package net.c7j.wna.huawei.consent

import android.os.Bundle
import android.widget.TextView
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.BannerAdSize
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.RequestOptions
import com.huawei.hms.ads.UnderAge
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.ads.consent.bean.AdProvider
import com.huawei.hms.ads.consent.constant.ConsentStatus
import com.huawei.hms.ads.consent.constant.DebugNeedConsent
import com.huawei.hms.ads.consent.inter.Consent
import com.huawei.hms.ads.consent.inter.ConsentUpdateListener
import net.c7j.wna.huawei.BaseActivity
import net.c7j.wna.huawei.ads.R
import net.c7j.wna.huawei.consent.ConsentDialog.ConsentDialogCallback
import net.c7j.wna.huawei.log

/**
 * Obtain consent from your users for the collection, use and sharing their personal data for personalized ads
 * This example shows how to manage your ads based on user's consent status
 */
class ConsentActivity : BaseActivity(), ConsentDialogCallback {

    private var adView: BannerView? = null
    private var adTypeTv: TextView? = null
    private var requestOptions: RequestOptions? = null
    private val mAdProviders: MutableList<AdProvider> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(net.c7j.wna.huawei.box.R.string.consent_settings)
        setContentView(R.layout.activity_consent)
        adTypeTv = findViewById(R.id.ad_load_tip)
        adView = findViewById(R.id.consent_ad_view)

        checkConsentStatus()
    }


    private fun checkConsentStatus() {
        val consentInfo = Consent.getInstance(this)
        // To ensure that a dialog box is displayed each time you access the code demo,
        // set ConsentStatus to UNKNOWN. In normal cases, the code does not need to be added.
        consentInfo.setConsentStatus(ConsentStatus.UNKNOWN)
        val testDeviceId = consentInfo.testDeviceId
        consentInfo.addTestDeviceId(testDeviceId)
        // After DEBUG_NEED_CONSENT is set, ensure consent is required even if a device is not located in a specified area.
        consentInfo.setDebugNeedConsent(DebugNeedConsent.DEBUG_NEED_CONSENT)
        consentInfo.requestConsentUpdate(   object : ConsentUpdateListener {

            override fun onSuccess(consentStatus: ConsentStatus, isNeedConsent: Boolean, adProviders: List<AdProvider>?) {
                log("ConsentStatus: $consentStatus, isNeedConsent: $isNeedConsent")
                // The parameter indicating whether the consent is required is returned.
                if (isNeedConsent) {
                    // If ConsentStatus is set to UNKNOWN, re-collect user consent.
                    if (consentStatus == ConsentStatus.UNKNOWN) {
                        mAdProviders.clear()
                        adProviders?.let { mAdProviders.addAll(it) }
                        showConsentDialog()
                    } else {
                        // If ConsentStatus is set to PERSONALIZED or NON_PERSONALIZED, no dialog box is displayed to collect user consent.
                        loadBannerAd(consentStatus.value)
                    }
                } else {
                    // If a country does not require your app to collect user consent before displaying ads, your app can request a personalized ad directly.
                    log("User doesn't need Consent")
                    loadBannerAd(ConsentStatus.PERSONALIZED.value)
                }
            }

            override fun onFail(errorDescription: String) {
                log( "User's consent status failed to update: $errorDescription")
                toast("User's consent status failed to update: $errorDescription")
                // In this demo,if the request fails ,you can load a non-personalized ad by default.
                loadBannerAd(ConsentStatus.NON_PERSONALIZED.value)
            }
        })
    }



    private fun showConsentDialog() {
        val dialog = ConsentDialog(this, mAdProviders)
        dialog.setCallback(this)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun updateConsentStatus(consentStatus: ConsentStatus) {
        loadBannerAd(consentStatus.value)
    }

    private fun loadBannerAd(consentStatus: Int) {
        log("Load banner ad, consent status: $consentStatus")
        if (consentStatus == ConsentStatus.UNKNOWN.value) removeBannerAd()
        // Obtain global ad singleton variables and add personalized ad request parameters.
        requestOptions = if (HwAds.getRequestOptions() == null) RequestOptions() else HwAds.getRequestOptions()
        // More info about requestOptions: https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/publisher-service-audience-settings-0000001076462332
        requestOptions = requestOptions?.toBuilder()
                ?.setTagForUnderAgeOfPromise(UnderAge.PROMISE_TRUE)
                ?.setNonPersonalizedAd(consentStatus)
                ?.build()

        HwAds.setRequestOptions(requestOptions)
        val adParam = AdParam.Builder().build()
        adView?.adId = getString(net.c7j.wna.huawei.box.R.string.ad_banner)
        adView?.bannerAdSize = BannerAdSize.BANNER_SIZE_SMART
        adView?.adListener = adListener
        adView?.loadAd(adParam)
        updateTextViewTips(consentStatus)
    }

    private val adListener: AdListener = object : AdListener() {

        override fun onAdLoaded() { toast("Ad loaded successfully") }
        override fun onAdFailed(errorCode: Int) { toast("Ad failed to load") }

    }

    private fun removeBannerAd() {
        adView?.removeAllViews()
        updateTextViewTips(ConsentStatus.UNKNOWN.value)
    }

    private fun updateTextViewTips(consentStatus: Int) {
        when {
            ConsentStatus.NON_PERSONALIZED.value == consentStatus -> {
                adTypeTv?.text = getString(net.c7j.wna.huawei.box.R.string.load_non_personalized_text)
            }
            ConsentStatus.PERSONALIZED.value == consentStatus -> {
                adTypeTv?.text = getString(net.c7j.wna.huawei.box.R.string.load_personalized_text)
            }
            else -> // ConsentStatus.UNKNOWN
                adTypeTv?.text = getString(net.c7j.wna.huawei.box.R.string.no_ads_text)
        }
    }
}
