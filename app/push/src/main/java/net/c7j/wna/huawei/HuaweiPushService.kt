package net.c7j.wna.huawei

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import com.huawei.hms.push.SendException
import java.util.Arrays

// ::created by c7j at 17.05.2023 19:38
// A basic class of Push Kit for receiving downlink messages or updated tokens.
class HuaweiPushService : HmsMessageService() {

    /**
     * Called after the Push Kit server updates the token
     *
     * If the getToken method fails to be called, HUAWEI Push Kit automatically caches the token request
     * and calls the method again. A token will then be returned through the onNewToken method.
     * If the EMUI version on a Huawei device is earlier than 10.0 and no token is returned
     * using the getToken method, a token will be returned using the onNewToken method.
     */
    override fun onNewToken(token: String) {
        log("OS version: ${Build.DISPLAY} received refresh token: $token")
        if (!TextUtils.isEmpty(token)) {
            // This method callback must be completed in 10 seconds.
            // Otherwise, you need to start a new Job for callback processing.
            refreshedTokenToServer(token)
        }
        val intent = Intent()
        intent.action = HUAWEI_PUSH_ACTION
        intent.putExtra("method", "onNewToken")
        intent.putExtra("msg", token)
        sendBroadcast(intent)
    }

    // Optional, only for those one who really needs it!
    // Multiple sender case. If there are 2,3 or more companies/providers who can deliver
    // push messages to your application, you provide a separate device/project token for each company
    override fun onNewToken(token: String, bundle: Bundle) {
        //Basically all the same, except the Bundle
    }

    private fun refreshedTokenToServer(token: String) {
        log("token refreshed, send to server token: $token")
    }

    /**
     * Receives downstream data messages (messages sent by your server). Callback must be
     * completed in 10 seconds. Otherwise, you need to start a new Job for callback processing.
     *
     * NOTE: You don't need a real server to test your integration -
     * Data type push can also be sent from AppGallery Connect Console
     */
    override fun onMessageReceived(message: RemoteMessage?) {
        log("onMessageReceived is called")
        if (message == null) {
            log("Received message entity is null!")
            return
        }

        // API Reference:
        // https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/remotemessage-builder-0000001050413831#section62580523210
        with(message) {
            log("""
            collapse key (overwriting): $collapseKey
            payload: $data
            source: $from
            recipient: $to
            message id: $messageId
            message type: $messageType
            sent time: $sentTime
            time to live (maximum cache duration): $ttl
            send mode: $sendMode
            receipt mode: $receiptMode
            original urgency: $originalUrgency
            urgency: $urgency
            token: $token""${'"'}.trimIndent())
            """)
        }

        // API Reference:
        // https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/remotemessage-notification-0000001050255662
        val notification: RemoteMessage.Notification = message.notification
        with (notification) {
            log("""
                Title: $title
                TitleLocalizationKey: $titleLocalizationKey
                TitleLocalizationArgs: ${Arrays.toString(titleLocalizationArgs)}
                Body: $body
                BodyLocalizationKey: $bodyLocalizationKey
                BodyLocalizationArgs: ${Arrays.toString(bodyLocalizationArgs)}
                Icon: $icon                
                ImageUrl: $imageUrl
                Sound: $sound
                Tag: $tag
                Color: $color
                ClickAction: $clickAction
                IntentUri: $intentUri
                ChannelId: $channelId
                Link: $link
                NotifyId: $notifyId
                isDefaultLight: $isDefaultLight
                isDefaultSound: $isDefaultSound
                isDefaultVibrate: $isDefaultVibrate
                When: $`when`
                LightSettings: ${Arrays.toString(lightSettings)}
                isLocalOnly: $isLocalOnly
                BadgeNumber: $badgeNumber
                isAutoCancel: $isAutoCancel
                Importance: $importance
                Ticker: $ticker
                VibrateConfig: $vibrateConfig
                Visibility: $visibility""${'"'}.trimIndent())
            """)
        }

        val intent = Intent()
        intent.action = HUAWEI_PUSH_ACTION
        intent.putExtra("method", "onMessageReceived")
        intent.putExtra("msg", "onMessageReceived called, message id:" +
                message.messageId + ", payload data:" + message.data)
        sendBroadcast(intent)

        // If the messages are not processed in 10 seconds, the app needs to use WorkManager
        val judgeWhetherIn10s = false
        if (judgeWhetherIn10s) {
            startWorkManagerJob(message)
        } else { // Process message within 10s
            processWithin10s(message)
        }
    }

    private fun startWorkManagerJob(message: RemoteMessage) {
        log("Start new Job processing $message")
    }

    private fun processWithin10s(message: RemoteMessage) {
        log("Processing now $message")
    }

    // Called after an uplink message is successfully sent
    override fun onMessageSent(msgId: String) {
        log("onMessageSent called, Message id:$msgId")
        val intent = Intent()
        intent.action = HUAWEI_PUSH_ACTION
        intent.putExtra("method", "onMessageSent")
        intent.putExtra("msg", "onMessageSent called, Message id:$msgId")
        sendBroadcast(intent)
    }

    // Called after an uplink message fails to be sent
    override fun onSendError(msgId: String, exception: Exception) {
        log("onSendError called, message id: $msgId, ErrCode:"
                    + (exception as SendException).errorCode + "description:" + exception.message)

        val intent = Intent()
        intent.action = HUAWEI_PUSH_ACTION
        intent.putExtra("method", "onSendError")
        intent.putExtra("msg", "onSendError called, message id:" + msgId + ", ErrCode:"
                    + exception.errorCode + ", description:" + exception.message)
        sendBroadcast(intent)
    }

    // Called when a device token fails to be obtained.
    override fun onTokenError(p0: Exception) {
        super.onTokenError(p0)
        log("Receive token failed: $p0")
    }

    // Multiple sender case. If there are 2,3 or more companies/providers who can deliver push messages
    override fun onTokenError(p0: Exception, b: Bundle) {
        super.onTokenError(p0)
        log("Receive token failed: $p0")
    }


    companion object {
        const val HUAWEI_PUSH_ACTION = "ru.huawei.push.action"
    }
}