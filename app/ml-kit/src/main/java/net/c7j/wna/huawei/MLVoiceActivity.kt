package net.c7j.wna.huawei

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.button.MaterialButton
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.mlsdk.asr.MLAsrConstants
import com.huawei.hms.mlsdk.asr.MLAsrListener
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer
import com.huawei.hms.mlsdk.common.MLApplication
import net.c7j.wna.huawei.ml.R

/**
 * This example performs human voice speech recognition
 *
 * Important! Please check which languages at which region are supported:
 * https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/ml-asr-0000001050066212
 */
class MLVoiceActivity : BaseActivity() {

    private var mSpeechRecognizer : MLAsrRecognizer? = null
    private var resultTextView : TextView? = null
    private var statusTextView : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ml_voice)
        initMLKit()
        getSupportedLanguages()
        if (!checkPermission(Manifest.permission.RECORD_AUDIO)) {
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
        findViewById<MaterialButton>(R.id.btnRecognizeNoUI).setOnClickListener {
            startASR()
        }
        resultTextView = findViewById(R.id.tvInfo)
        statusTextView = findViewById(R.id.tvStatus)
    }

    // Starts recognition without showing Huawei default recognition UI
    // (seems old method was removed in latest versions, documentation doesn't match sdk)
    private fun startASR() {
        val mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(this@MLVoiceActivity)
        mSpeechRecognizer.setAsrListener(SpeechRecognitionListener())
        val mSpeechRecognizerIntent = Intent(MLAsrConstants.ACTION_HMS_ASR_SPEECH)
        mSpeechRecognizerIntent
            // If this language parameter is not set, English is recognized by default.
            .putExtra(MLAsrConstants.LANGUAGE, "en-US")
            // Set to return the recognition result along with the speech (default). Options are:
            // MLAsrConstants.FEATURE_WORDFLUX: Recognizes and returns texts through onRecognizingResults
            // MLAsrConstants.FEATURE_ALLINONE: After the recognition is complete, texts are returned as onResults
            .putExtra(MLAsrConstants.FEATURE, MLAsrConstants.FEATURE_WORDFLUX)
            // Timeout given to start recognition. Ranges from 3.000 to 60.000, in milliseconds.
            .putExtra(MLAsrConstants.VAD_START_MUTE_DURATION, 6000)
            // Timeout given to continue speaking after stopping, before recognition is terminated.
            .putExtra(MLAsrConstants.VAD_END_MUTE_DURATION, 1000)
            // Whether to punctuate text.
            .putExtra(MLAsrConstants.PUNCTUATION_ENABLE, true)

        mSpeechRecognizer.startRecognizing(mSpeechRecognizerIntent)
    }


    // Use the callback to implement the MLAsrListener API and methods in the API.
    internal inner class SpeechRecognitionListener : MLAsrListener {

        override fun onStartListening() { log("The recorder starts to receive speech") }

        override fun onStartingOfSpeech() { log("User starts to speak") }

        override fun onVoiceDataReceived(   // Non main-thread call (running on non-main thread)
            data: ByteArray,                // original PCM stream, with PCM mono 16-bit depth 16 kHz audio sampling rate
            energy: Float,                  // Audio power
            bundle: Bundle) {               // Reserved fields
            /* NO OP */
        }

        override fun onRecognizingResults(partialResults: Bundle) { // Non main-thread!!!
            // When the speech recognition mode is set to MLAsrConstants.FEATURE_WORDFLUX,
            // the speech recognizer continuously returns the speech recognition result through this API
            val text = partialResults.getString(MLAsrRecognizer.RESULTS_RECOGNIZING)
            runOnUiThread { resultTextView?.text = text }
        }

        override fun onResults(results: Bundle) { // Non main-thread!!!
            // Called when the speech recognition result is received from the speech recognizer
            val text = results.getString(MLAsrRecognizer.RESULTS_RECOGNIZED)
            runOnUiThread { resultTextView?.text = text }
        }

        override fun onError(error: Int, errorMessage: String) { // Non main-thread!!!
            log("Error: " + when (error) {
                MLAsrConstants.ERR_NO_UNDERSTAND -> "Recognition failed"
                MLAsrConstants.ERR_NO_NETWORK -> "No network"
                MLAsrConstants.ERR_INVALIDATE_TOKEN -> "Invalidate token"
                MLAsrConstants.ERR_SERVICE_UNAVAILABLE -> "Service unavailable"
                else -> "Undefined error"
            } + " " + errorMessage)
        }

        // Notifies the recognition status change
        override fun onState(state: Int, params: Bundle) { // Non main-thread!!!
            val status = when (state) {
                MLAsrConstants.STATE_LISTENING -> "the recorder is ready"
                MLAsrConstants.STATE_NO_NETWORK -> "no network is available in the current environment"
                MLAsrConstants.STATE_NO_SOUND -> "no speech is detected within 3s"
                MLAsrConstants.STATE_NO_SOUND_TIMES_EXCEED -> "no result is detected within 6s"
                MLAsrConstants.STATE_NO_UNDERSTAND -> "current frame fails to be detected on the cloud"
                MLAsrConstants.STATE_WAITING -> "awaiting..."
                else -> "unexpected status"
            }
            runOnUiThread {
                val resultText = "status changed: $status $state"
                statusTextView?.text = resultText
            }
        }
    }

    private fun initMLKit() {
        val apiKey = AGConnectOptionsBuilder().build(this@MLVoiceActivity).getString("client/api_key")
        MLApplication.getInstance().apiKey = apiKey
    }

    override fun onDestroy() {
        super.onDestroy()
        mSpeechRecognizer?.destroy()
    }

    private fun getSupportedLanguages() {
        mSpeechRecognizer?.getLanguages(object : MLAsrRecognizer.LanguageCallback {
            override fun onResult(result: List<String>) { log("support languages==$result") }
            override fun onError(errorCode: Int, errorMsg: String) { log("error: $errorCode $errorMsg") }
        })
    }

    private val microphonePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) toast("Grant microphone access permission, please")
    }
}