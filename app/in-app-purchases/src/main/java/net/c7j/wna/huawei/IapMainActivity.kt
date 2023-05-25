package net.c7j.wna.huawei

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import net.c7j.wna.huawei.iap.R


class IapMainActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iap_main)
        initNavigation()
    }


    private fun initNavigation() {
        findViewById<MaterialButton>(R.id.btnIapConsumable).setOnClickListener {
            navigate("net.c7j.wna.huawei.IapConsumableActivity")
        }
        findViewById<MaterialButton>(R.id.btnIapNonConsumable).setOnClickListener {
            navigate("net.c7j.wna.huawei.IapNonConsumableActivity")
        }
        findViewById<MaterialButton>(R.id.btnIapSubscription).setOnClickListener {
            navigate("net.c7j.wna.huawei.IapSubscriptionActivity")
        }
        findViewById<MaterialButton>(R.id.btnIapHistory).setOnClickListener {
            navigate("net.c7j.wna.huawei.IapHistoryActivity")
        }
    }
}