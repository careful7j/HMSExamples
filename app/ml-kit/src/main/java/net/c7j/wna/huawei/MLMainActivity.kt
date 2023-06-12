package net.c7j.wna.huawei

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import net.c7j.wna.huawei.ml.R


class MLMainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ml_main)
        initNavigation()
    }

    private fun initNavigation() {
        findViewById<MaterialButton>(R.id.btnNavigateMLBank).setOnClickListener {
            navigate("net.c7j.wna.huawei.MLBankActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateMLVoice).setOnClickListener {
            navigate("net.c7j.wna.huawei.MLVoiceActivity")
        }
        findViewById<MaterialButton>(R.id.btnNavigateMLTTS).setOnClickListener {
            navigate("net.c7j.wna.huawei.MLTTSActivity")
        }
    }
}