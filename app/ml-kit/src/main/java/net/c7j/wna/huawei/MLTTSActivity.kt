package net.c7j.wna.huawei

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.tts.MLTtsAudioFragment
import com.huawei.hms.mlsdk.tts.MLTtsCallback
import com.huawei.hms.mlsdk.tts.MLTtsConfig
import com.huawei.hms.mlsdk.tts.MLTtsConstants
import com.huawei.hms.mlsdk.tts.MLTtsEngine
import com.huawei.hms.mlsdk.tts.MLTtsError
import com.huawei.hms.mlsdk.tts.MLTtsWarn
import net.c7j.wna.huawei.ml.R

/**
 * This example performs ONLINE text to speech! For OFFLINE text to speech (on device tts):
 * Please update your code as follows in this document (language model will be downloaded on device)
 * https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/on-device-text-to-speech-0000001058761998
 *
 * Important! Please check which languages at which region are supported:
 * https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/ml-tts-0000001050068169#section1664951017435
 */
class MLTTSActivity : BaseActivity() {

    private var mlTtsEngine : MLTtsEngine? = null
    private var etTTSContent : EditText? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ml_tts)
        initMLKit()
        initTTS()
        etTTSContent = findViewById(R.id.etTTSContent)
        etTTSContent?.setText("Reading test record here 1 2 3")
        findViewById<MaterialButton>(R.id.btnTTS).setOnClickListener { readText() }
        findViewById<MaterialButton>(R.id.btnPauseTTS).setOnClickListener { mlTtsEngine?.pause() }
        findViewById<MaterialButton>(R.id.btnResumeTTS).setOnClickListener { mlTtsEngine?.resume() }
    }

    /**
     * MLTtsEngine.QUEUE_APPEND: After an audio synthesis task is generated, the audio synthesis task is processed as follows:
     * - If playback is going on, the task is added to the queue for execution in sequence;
     * - if playback pauses, the playback is resumed and the task is added to the queue for execution in sequence;
     * - if there is no playback, the audio synthesis task is executed immediately.
     * MLTtsEngine.QUEUE_FLUSH: The ongoing TTS task and playback are stopped immediately,
     * all TTS tasks in the queue are cleared, and the current TTS task is executed immediately and played.
     * MLTtsEngine.OPEN_STREAM: The synthesized audio data is output through onAudioAvailable.
     * MLTtsEngine.EXTERNAL_PLAYBACK: external playback mode. The player provided by the SDK is shielded.
     * You need to process the audio output by the onAudioAvailable callback API. In this case,
     * the playback-related APIs in the callback APIs become invalid and only the callback APIs related to audio synthesis can be listened.
     */
    private fun readText() {
        val sourceText = etTTSContent?.text.toString()
        val id = mlTtsEngine?.speak(
            sourceText,                 // text to read - a maximum of 500 characters
            MLTtsEngine.QUEUE_APPEND    //
            // | MLTtsEngine.OPEN_STREAM | MLTtsEngine.EXTERNAL_PLAYBACK)
            // In queuing mode, the synthesized audio stream is output through onAudioAvailable, and the built-in player of the SDK is used to play the speech.
            // | MLTtsEngine.OPEN_STREAM);
            // In queuing mode, the synthesized audio stream is output through onAudioAvailable, and the audio stream is not played, but controlled by you.
        )
        log("Speaking: $id")
    }

    private fun initTTS() {
        val mlTtsConfig = MLTtsConfig()
            .setLanguage(MLTtsConstants.TTS_EN_US)  // Set the text converted from speech to English.
            .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN) // Set the Chinese timbre.
            .setSpeed(1.0f)  // Set the speech speed.   The range is (0.0..5.0]. 1.0 indicates a normal speed.
            .setVolume(1.0f) // Set the volume.         The range is (0.0..2.0). 1.0 indicates a normal volume.

        mlTtsEngine = MLTtsEngine(mlTtsConfig)
        // Set the volume of the built-in player, in dBs. The value is in the range of [0, 100]:
        mlTtsEngine?.setPlayerVolume(20)
        // Update the configuration when the engine is running.
        mlTtsEngine?.updateConfig(mlTtsConfig)
        mlTtsEngine?.setTtsCallback(callback)
    }

    private val callback: MLTtsCallback = object : MLTtsCallback {

        override fun onError(taskId: String, err: MLTtsError) {
            log("Task: $taskId got error: $err")
        }

        override fun onWarn(taskId: String, warn: MLTtsWarn) {
            log("Task: $taskId got warning: $warn") // Alarm handling without affecting service logic
        }

        /**
         * Mapping between the currently played segment and text
         * start: start position of the audio segment in the input text
         * end (excluded): end position of the audio segment in the input text
         */
        override fun onRangeStart(taskId: String, start: Int, end: Int) { /* NO OP */ }

        override fun onAudioAvailable(
            taskId: String?,
            audioFragment: MLTtsAudioFragment?,
            // offset: offset of the audio segment to be transmitted in the queue. One audio synthesis task corresponds to an audio synthesis queue
            offset: Int,
            // range: text area where the audio segment to be transmitted is located; range.first (included): start position; range.second (excluded): end position
            range: android.util.Pair<Int, Int>?,
            bundle: Bundle?
        ) { /* NO OP */ }

        override fun onEvent(taskId: String?, eventId: Int, bundle: Bundle?) {
            when (eventId) {
                MLTtsConstants.EVENT_PLAY_START -> log("play started")
                MLTtsConstants.EVENT_PLAY_STOP -> log("stopped, is interrupted: " +
                        "${bundle?.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED) ?: false}")
                MLTtsConstants.EVENT_PLAY_RESUME -> log("play resumed")
                MLTtsConstants.EVENT_PLAY_PAUSE -> log("play paused")
                MLTtsConstants.EVENT_SYNTHESIS_START -> log("play synthesis started")
                MLTtsConstants.EVENT_SYNTHESIS_END -> log("play synthesis ended")
                MLTtsConstants.EVENT_SYNTHESIS_COMPLETE -> log("completed, is interrupted: " +
                        "${bundle?.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED) ?: false}")
                else -> log("unsupported event occur")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mlTtsEngine?.stop()
        mlTtsEngine?.shutdown()
    }

    private fun initMLKit() {
        val apiKey = AGConnectOptionsBuilder().build(this@MLTTSActivity).getString("client/api_key")
        MLApplication.getInstance().apiKey = apiKey
    }
}