package net.c7j.wna.huawei

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.reward.Reward
import com.huawei.hms.ads.reward.RewardAd
import com.huawei.hms.ads.reward.RewardAdLoadListener
import com.huawei.hms.ads.reward.RewardAdStatusListener
import net.c7j.wna.huawei.ads.R
import net.c7j.wna.huawei.consent.ProtocolDialog

// This is main menu activity of Ads Kit example, below you can find Rewarded ads example
class AdsRewardedNavigationActivity : BaseActivity(), ProtocolDialog.ProtocolDialogCallback {

    private lateinit var rewardedAd: RewardAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ads_navigation)
        initViews()
        loadRewardAd()
    }

    //-------------------------------
    // Important! Error codes: https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/android-error-code-0000001130129080
    //
    // Additional reasons you may face Error code: 3, NOT mentioned in the documentation:
    // - there is no SIM card in your device
    // - banner type you created miss-matches with type of slot you are using e.g. you have created
    // Native-type Ad id and trying to use it in Splash-type Ad.
    // - your application yet was not approved by Ads team (not to mention your app have to be
    // already published in AppGallery). If after a few days nothing happens submit a trouble ticket.
    // - region miss-match: For example: you app developer account is registered in Russia, but you are
    // now working from lets say from Turkey or Thailand - in this situation there might be no content.
    // To fix it - turn on VPN of your app registered account original location before testing.
    // - there is no advertisements at the moment. The most sad case you can not really check well,
    // in this situation try to create a banner of the other type - normally Ads network can't run out of Ads
    // of all types at the same moment. At least some shall be available.
    //----------------------------------

    private fun initViews() {
        findViewById<MaterialButton>(R.id.btn_banner_ads).setOnClickListener {
            navigate("net.c7j.wna.huawei.BannerActivity")
        }
        findViewById<MaterialButton>(R.id.btn_native_ads).setOnClickListener {
            navigate("net.c7j.wna.huawei.NativeActivity")
        }
        findViewById<MaterialButton>(R.id.btn_interstitial_ads).setOnClickListener {
            navigate("net.c7j.wna.huawei.InterstitialActivity")
        }
        findViewById<MaterialButton>(R.id.btn_rewarded_ads).setOnClickListener {
            if (rewardedAd.isLoaded) rewardedAd.show(this, rewardAdStatusListener)
        }
        findViewById<MaterialButton>(R.id.btn_splash_ads).setOnClickListener {
            navigate("net.c7j.wna.huawei.SplashActivity")
        }
        findViewById<MaterialButton>(R.id.btn_instream_ads).setOnClickListener {
            navigate("net.c7j.wna.huawei.InStreamActivity")
        }
        findViewById<MaterialButton>(R.id.btn_consent_non_personalised).setOnClickListener {
            navigate("net.c7j.wna.huawei.consent.ConsentActivity")
        }
        findViewById<MaterialButton>(R.id.btn_express_splash_ads).setOnClickListener {
            navigate("net.c7j.wna.huawei.splash.ExSplashActivity")
        }
        findViewById<MaterialButton>(R.id.btn_privacy_agreement).setOnClickListener {
            showProtocolDialog()
        }
    }

    // ----------------------------
    //  Rewarded Ads:
    // ----------------------------

    private fun loadRewardAd() {
        // Landscape orientation support requires additional Ad id, here we assume it is vertical
        rewardedAd = RewardAd(this, getString(net.c7j.wna.huawei.box.R.string.ad_rewarded_vertical))
        rewardedAd.loadAd(AdParam.Builder().build(), rewardAdLoadListener)
    }

    private val rewardAdStatusListener = object : RewardAdStatusListener() {
        override fun onRewardAdClosed() = loadRewardAd()
        override fun onRewardAdFailedToShow(errorCode: Int) = toast("Error code: $errorCode")
        override fun onRewardAdOpened() = toast("onRewardAdOpened")
        override fun onRewarded(reward: Reward) {
            toast("Ad finished, you get a reward")
            loadRewardAd()
        }
    }

    private val rewardAdLoadListener = object : RewardAdLoadListener() {
        override fun onRewardAdFailedToLoad(errorCode: Int) = toast("Error code: $errorCode")
        override fun onRewardedLoaded() {}
    }

    // ----------------------------
    //  Privacy (protocol) dialog:
    // ----------------------------
    private fun showProtocolDialog() {
        val dialog = ProtocolDialog(this)
        dialog.setCallback(this)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun onAgree() {
        /* User agree with policy, - handle as you find appropriate */
    }

    override fun onCancel() {
        /** User disagree with policy - handle as you find appropriate */
    }

}