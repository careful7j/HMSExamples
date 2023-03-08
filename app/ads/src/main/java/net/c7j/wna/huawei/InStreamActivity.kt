package net.c7j.wna.huawei

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.MediaMuteListener
import com.huawei.hms.ads.instreamad.*
import net.c7j.wna.huawei.ads.R
import kotlin.math.roundToInt

class InStreamActivity : BaseActivity() {

    private var maxAdDuration = 0
    private var whyThisAdUrl: String? = null
    private var isMuted = false
    private var adLoader: InstreamAdLoader? = null
    private var inStreamAds: List<InstreamAd>? = ArrayList()
    private var videoContent: TextView? = null
    private var skipAd: TextView? = null
    private var countDown: TextView? = null
    private var callToAction: TextView? = null
    private var loadButton: MaterialButton? = null
    private var registerButton: MaterialButton? = null
    private var muteButton: MaterialButton? = null
    private var pauseButton: MaterialButton? = null
    private var inStreamContainer: ConstraintLayout? = null
    private var inStreamView: InstreamView? = null
    private var whyThisAd: ImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(net.c7j.wna.huawei.box.R.string.in_stream_ad_id)
        setContentView(R.layout.activity_in_stream)
        initInStreamAdView()
        initButtons()
        configAdLoader()
    }

    private val mediaChangeListener: InstreamMediaChangeListener =

        InstreamMediaChangeListener { inStreamAd ->
            whyThisAdUrl = null
            whyThisAdUrl = inStreamAd.whyThisAd
            log( "onSegmentMediaChange, whyThisAd: $whyThisAdUrl")
            if (!whyThisAdUrl.isNullOrEmpty()) {
                whyThisAd?.visibility = View.VISIBLE
                whyThisAd?.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whyThisAdUrl)))
                }
            } else whyThisAd?.visibility = View.GONE

            val cta: String = inStreamAd.callToAction
            if (!TextUtils.isEmpty(cta)) {
                callToAction?.visibility = View.VISIBLE
                callToAction?.text = cta
                inStreamView?.callToActionView = callToAction
            }
        }

    private val mediaStateListener = object : InstreamMediaStateListener {

        override fun onMediaProgress(per: Int, playTime: Int) {
            updateCountDown(playTime)
        }

        override fun onMediaStart(playTime: Int) {
            updateCountDown(playTime)
        }

        override fun onMediaPause(playTime: Int) {
            updateCountDown(playTime)
        }

        override fun onMediaStop(playTime: Int) {
            updateCountDown(playTime)
        }

        override fun onMediaCompletion(playTime: Int) {
            updateCountDown(playTime)
            playVideo()
        }

        override fun onMediaError(playTime: Int, errorCode: Int, extra: Int) {
            updateCountDown(playTime)
        }
    }

    private val mediaMuteListener: MediaMuteListener = object : MediaMuteListener {

        override fun onMute() {
            isMuted = true
            toast("Ad muted")
        }

        override fun onUnmute() {
            isMuted = false
            toast("Ad unmuted")
        }
    }


    private val clickListener = View.OnClickListener { view ->
        when (view.id) {

            R.id.in_stream_load -> adLoader?.let {
                loadButton?.text = getString(net.c7j.wna.huawei.box.R.string.in_stream_loading)
                it.loadAd(AdParam.Builder().build())
            }

            R.id.in_stream_register ->
                if (inStreamAds.isNullOrEmpty()) playVideo() else playInStreamAds(inStreamAds!!)

            R.id.in_stream_mute ->
                if (isMuted) {
                    inStreamView?.unmute()
                    muteButton?.text = getString(net.c7j.wna.huawei.box.R.string.in_stream_mute)
                } else {
                    inStreamView?.mute()
                    muteButton?.text = getString(net.c7j.wna.huawei.box.R.string.in_stream_unmute)
                }

            R.id.in_stream_pause_play -> inStreamView?.let {
                if (it.isPlaying) {
                    it.pause()
                    pauseButton?.text = getString(net.c7j.wna.huawei.box.R.string.in_stream_play)
                } else {
                    it.play()
                    pauseButton?.text = getString(net.c7j.wna.huawei.box.R.string.in_stream_pause)
                }
            }

            else -> {}
        }
    }


    private val inStreamAdLoadListener = object : InstreamAdLoadListener {

        override fun onAdLoaded(ads: MutableList<InstreamAd>) {
            if (ads.isEmpty()) {
                playVideo()
                return
            }
            val it: MutableIterator<InstreamAd> = ads.iterator()
            while (it.hasNext()) {
                val ad: InstreamAd = it.next()
                if (ad.isExpired) it.remove()
            }
            if (ads.isEmpty()) {
                playVideo()
                return
            }
            loadButton?.text = getString(net.c7j.wna.huawei.box.R.string.in_stream_loaded)
            inStreamAds = ads
            toast("onAdLoaded, ad size: " + ads.size + ", click REGISTER to play.")
        }

        override fun onAdFailed(errorCode: Int) {
            loadButton?.text = getString(net.c7j.wna.huawei.box.R.string.in_stream_load)
            log( "onAdFailed: $errorCode")
            toast("onAdFailed: $errorCode")
            playVideo()
        }
    }

    private fun configAdLoader() {
        /**
         * if the maximum total duration is 60 seconds and the maximum number of roll ads is eight,
         * at most four 15-second roll ads or two 30-second roll ads will be returned.
         * If the maximum total duration is 120 seconds and the maximum number of roll ads is four,
         * no more roll ads will be returned after whichever is reached.
         */
        val totalDuration = 60
        val maxCount = 4
        val builder = InstreamAdLoader.Builder(
                    applicationContext,
                    getString(net.c7j.wna.huawei.box.R.string.in_stream_ad_id)
        )
        adLoader = builder
            .setTotalDuration(totalDuration)
            .setMaxCount(maxCount)
            .setInstreamAdLoadListener(inStreamAdLoadListener)
            .build()
    }

    // play normal video content (no Ads)
    private fun playVideo() {
        inStreamContainer?.visibility = View.GONE  //hides Ad views
        videoContent?.setText(net.c7j.wna.huawei.box.R.string.in_stream_normal_video_playing)
        toast("play video")
    }


    private fun playInStreamAds(ads: List<InstreamAd>) {
        maxAdDuration = getMaxInStreamDuration(ads)
        inStreamContainer?.visibility = View.VISIBLE
        loadButton?.text = getString(net.c7j.wna.huawei.box.R.string.in_stream_load)
        inStreamView?.setInstreamAds(ads)
        toast("playInStreamAds $maxAdDuration")
    }


    private fun updateCountDown(playTime: Int) {
        val time = ((maxAdDuration - playTime) / 1000).toFloat().roundToInt().toString()
        runOnUiThread { countDown?.text = time }
    }


    private fun getMaxInStreamDuration(ads: List<InstreamAd>): Int =
        ads.sumOf { it.duration.toInt() }


    override fun onPause() {
        super.onPause()
        inStreamView?.let {
            if (it.isPlaying) {
                it.pause()
                pauseButton?.text = getText(net.c7j.wna.huawei.box.R.string.in_stream_play)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        inStreamView?.let {
            if (!it.isPlaying) {
                it.play()
                pauseButton?.text = getText(net.c7j.wna.huawei.box.R.string.in_stream_pause)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        inStreamView?.let {
            it.removeInstreamMediaStateListener()
            it.removeInstreamMediaChangeListener()
            it.removeMediaMuteListener()
            it.destroy()
        }
    }


    private fun initInStreamAdView() {
        inStreamContainer = findViewById(R.id.in_stream_ad_container)
        videoContent = findViewById(R.id.in_stream_video_content)
        countDown = findViewById(R.id.in_stream_count_down)
        callToAction = findViewById(R.id.in_stream_call_to_action)
        whyThisAd = findViewById(R.id.in_stream_why_this_ad)
        inStreamView = findViewById(R.id.in_stream_view)
        skipAd = findViewById(R.id.in_stream_skip)

        skipAd?.setOnClickListener {
            inStreamView?.let {
                it.onClose()
                it.destroy()
                inStreamContainer?.visibility = View.GONE
            }
        }
        inStreamView?.let {
            it.setInstreamMediaChangeListener(mediaChangeListener)
            it.setInstreamMediaStateListener(mediaStateListener)
            it.setMediaMuteListener(mediaMuteListener)
            it.setOnInstreamAdClickListener { toast("instream clicked.") }
        }
    }


    private fun initButtons() {
        loadButton = findViewById(R.id.in_stream_load)
        registerButton = findViewById(R.id.in_stream_register)
        muteButton = findViewById(R.id.in_stream_mute)
        pauseButton = findViewById(R.id.in_stream_pause_play)
        loadButton?.setOnClickListener(clickListener)
        registerButton?.setOnClickListener(clickListener)
        muteButton?.setOnClickListener(clickListener)
        pauseButton?.setOnClickListener(clickListener)
    }
}