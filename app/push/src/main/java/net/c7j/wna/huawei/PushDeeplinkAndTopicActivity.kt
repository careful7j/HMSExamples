package net.c7j.wna.huawei

import android.content.Intent
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.hms.push.HmsMessaging
import net.c7j.wna.huawei.push.R

// Opens a specified page of an app, and receives data in the customized activity class
class PushDeeplinkAndTopicActivity : BaseActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_push_deeplink)
        intent?.let { getIntentData(it) }
        findViewById<MaterialButton>(R.id.btnSubscribePushTopic)
            .setOnClickListener { subscribeTopic() }
        findViewById<MaterialButton>(R.id.btnUnsubscribePushTopic)
            .setOnClickListener { unsubscribeTopic() }
    }

    // URI to test from AGC: pushscheme://com.huawei.codelabpush/deeplink?name=abc&age=180
    private fun getIntentData(intent: Intent) {
        // Obtain data set in way 1.
        var age = 0
        var name1: String? = null
        try {
            val uri = intent.data
            if (uri == null) {
                log("getData is null")
                return
            }
            val age1 = uri.getQueryParameter("age")
            name1 = uri.getQueryParameter("name")
            if (age1 != null) {
                age = age1.toInt()
            }
        } catch (e: Exception) {
            log("Exception: $e")
        } finally {
            log("name $name1,age $age")
            toast("name $name1,age $age")
        }

        // Obtain data set in way 2.
        // String name2 = intent.getStringExtra("name");
        // int age2 = intent.getIntExtra("age", -1);
    }

    // Users subscribed to the exact topic will receive topic related notifications
    private fun subscribeTopic() {
        try {
            HmsMessaging
                .getInstance(this@PushDeeplinkAndTopicActivity)
                .subscribe("test_topic")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        log("subscribe topic: success")
                    } else {
                        log("failed, the return value is " + task.exception.message)
                    }
                }
        } catch (e: Exception) { log("$e") }
    }

    // Users unsubscribed from the exact topic will no longer receive notifications related to this topic
    private fun unsubscribeTopic() {
        try {
            HmsMessaging
                .getInstance(this@PushDeeplinkAndTopicActivity)
                .unsubscribe("test_topic")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        log("unsubscribe topic: success")
                    } else {
                        log("failed, the return value is " + task.exception.message)
                    }
                }
        } catch (e: Exception) { log("$e") }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getIntentData(intent)
    }
}