package net.c7j.wna.huawei

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.result.AuthAccount
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.api.entity.common.CommonConstant
import com.huawei.hms.support.sms.ReadSmsManager
import com.huawei.hms.support.sms.common.ReadSmsConstant.READ_SMS_BROADCAST_ACTION
import net.c7j.wna.huawei.account.R

/**
 * Auth the scenarios are described here:
 * https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-scenario-summary-0000001115918594
 *
 * Your app can obtain basic information about their HUAWEI IDs,
 * including nickname, profile picture, email address, UnionID, and OpenID:
 * @see silentSignInWithoutIdVerification()
 *
 * Sign in via ID token through the OpenID Connect protocol, Your app will obtain
 * the user's ID token for identity verification, enabling the user to securely sign in:
 * @see silentSignInViaIdToken()
 *
 * Your app will obtain the user's authorization code (temporary authorization credential)
 * for identity verification, enabling secure sign-in. After the user's identity verification information expires,
 * your app server will use the refresh token to request a new access token from the Account Kit server:
 * @see silentSignInOauth()
 */
 // 4. Quick HUAWEI ID Sign-In to Apps That Apply for Only the OpenID or UnionID (is not covered by this example):
 // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-scenario-openid-unionid-0000001210738701
 //
 // 5. Independent Authorization (is not covered by this example):
 // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/independentsignin-0000001140395573
 //
 // Important! Please use HUAWEI ID button according to the guidelines:
 // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/huaweiidauthbutton-0000001050179025
 //
 // SMS Parser do NOT Require SMS permission in the implementation below. SMS Parser API Reference:
 // https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/account-support-sms-readsmsmanager-0000001050050553-V5#EN-US_TOPIC_0000001050050553__section1866019915120
class AccountActivity : BaseActivity() {

    private lateinit var mAuthManager: AccountAuthService
    private lateinit var mAuthParam: AccountAuthParams
    private var appId: String = ""
    // Your SMS to read shall come from this phone number:
    private var phoneNumberToReceiveSMSFrom : String = "+700000000000"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        initViews()
        appId = AGConnectOptionsBuilder().build(this@AccountActivity).getString("client/app_id")
        registerReceiver(deliverSMSContentReceiver, IntentFilter(SmsBroadcastReceiver.ACTION_SMS_READ_BROADCAST))
    }

    /**
     * With this method your app can obtain basic information about user HUAWEI ID,
     * including nickname, profile picture, email address, UnionID, and OpenID.
     * Silent sign-in: If a user has authorized your app and signed in -
     * no authorization or sign-in screen will appear during subsequent sign-ins, and the user will directly sign in.
     * If the user has not authorized your app or signed in, your app will show the authorization or sign-in screen.
     */
    private fun silentSignInWithoutIdVerification() {
        // Use AccountAuthParams to specify the user information to be obtained after user authorization,
        // including the user ID (OpenID and UnionID), email address, and profile (nickname and picture).
        // By default, DEFAULT_AUTH_REQUEST_PARAM specifies two items to be obtained, that is, the user ID and profile.
        mAuthParam = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setEmail() // If your app needs to obtain the user's email address, call setEmail()
            .createParams()

        mAuthManager = AccountAuthManager.getService(this, mAuthParam)
        val task = mAuthManager.silentSignIn()
        task.addOnSuccessListener { authAccount ->
            // The silent sign-in is successful. Process the returned AuthAccount object to obtain the HUAWEI ID information.
            printSuccessAuth(authAccount)
        }
        task.addOnFailureListener { e ->
            // The silent sign-in fails. Your app will call getSignInIntent() to show the authorization or sign-in screen.
            if (e is ApiException) {
                val signInIntent = mAuthManager.signInIntent
                // If your app appears in full screen mode when a user tries to sign in (Optional):
                signInIntent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true)
                authResult.launch(signInIntent)
            }
        }
    }

    /**
     * Sign in via ID token through the OpenID Connect protocol, Your app will obtain
     * the user's ID token for identity verification, enabling the user to securely sign in.
     * Silent sign-in: If a user has authorized your app and signed in -
     * no authorization or sign-in screen will appear during subsequent sign-ins, and the user will directly sign in.
     * If the user has not authorized your app or signed in, your app will show the authorization or sign-in screen.
     */
    private fun silentSignInViaIdToken() {
        // Use AccountAuthParams to specify the user information to be obtained after user authorization,
        // including the user ID (OpenID and UnionID), email address, and profile (nickname and picture).
        // By default, DEFAULT_AUTH_REQUEST_PARAM specifies two items to be obtained, that is, the user ID and profile.
        mAuthParam = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setEmail()
            .setIdToken() // To support ID token-based HUAWEI ID sign-in, use setIdToken()
            .createParams()

        mAuthManager = AccountAuthManager.getService(this, mAuthParam)
        val task: Task<AuthAccount> = mAuthManager.silentSignIn()
        task.addOnSuccessListener { authAccount ->
            // The silent sign-in is successful. Process the returned AuthAccount object to obtain the HUAWEI ID information.
            printSuccessAuth(authAccount)
        }
        task.addOnFailureListener { e ->
            // The silent sign-in fails. Your app will call getSignInIntent() to show the authorization or sign-in screen.
            if (e is ApiException) {
                val signInIntent: Intent = mAuthManager.signInIntent
                // If your app appears in full screen mode when a user tries to sign in (Optional):
                signInIntent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true)
                authResult.launch(signInIntent)
            }
        }
    }


    /**
     * Your app will obtain the user's authorization code (temporary authorization credential)
     * for identity verification, enabling secure sign-in. After the user's identity verification information expires,
     * your app server will use the refresh token to request a new access token from the Account Kit server.
     * Silent sign-in: If a user has authorized your app and signed in -
     * no authorization or sign-in screen will appear during subsequent sign-ins, and the user will directly sign in.
     * If the user has not authorized your app or signed in, your app will show the authorization or sign-in screen.
     */
    private fun silentSignInOauth() {
        // Use AccountAuthParams to specify the user information to be obtained after user authorization,
        // including the user ID (OpenID and UnionID), email address, and profile (nickname and picture).
        // By default, DEFAULT_AUTH_REQUEST_PARAM specifies two items to be obtained, that is, the user ID and profile.
        // All the user information that your app is authorized to access to can be obtained
        // through the relevant API provided by the Account Kit server.
        mAuthParam = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setEmail()
            .setAuthorizationCode() // To support authorization code-based HUAWEI ID sign-in, use setAuthorizationCode()
            .createParams()

        mAuthManager = AccountAuthManager.getService(this, mAuthParam)
        val task = mAuthManager.silentSignIn()
        task.addOnSuccessListener { authAccount ->
            // The silent sign-in is successful. Process the returned AuthAccount object to obtain the HUAWEI ID information.
            printSuccessAuth(authAccount)
        }
        task.addOnFailureListener { e ->
            // The silent sign-in fails. Your app will call getSignInIntent() to show the authorization or sign-in screen.
            if (e is ApiException) {
                val signInIntent = mAuthManager.signInIntent
                // If your app appears in full screen mode when a user tries to sign in (Optional):
                signInIntent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true)
                authResult.launch(signInIntent)
            }
        }
    }

    // Signs user out from his account
    private fun signOut() {
        mAuthParam = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setIdToken()
            .setAccessToken()
            .createParams()
        mAuthManager = AccountAuthManager.getService(this@AccountActivity, mAuthParam)
        val signOutTask = mAuthManager.signOut()
        signOutTask
            ?.addOnSuccessListener { log("sign out success").also { toast("sign out success") } }
            ?.addOnFailureListener { log("sign out fail") }
    }

    // To improve privacy & security, your app should allow users to cancel authorization
    // Revokes (cancels) authorization of YOUR APP to communicate with huawei ID service
    private fun revokeAuth() {
        mAuthParam = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setProfile()
            .setAuthorizationCode()
            .setIdToken()
            .setAccessToken()
            .createParams()

        mAuthManager = AccountAuthManager.getService(this@AccountActivity, mAuthParam)
        val task = mAuthManager.cancelAuthorization()
        task?.addOnSuccessListener { log("cancelAuthorization success").also { toast("application auth revoked") } }
        task?.addOnFailureListener { e -> log("cancelAuthorization failure：$e") }
    }

    // Prints successful authorization AuthAccount object's content to the log
    private fun printSuccessAuth(authAccount: AuthAccount) {
        log("display name:" + authAccount.displayName)
        log("photo uri string:" + authAccount.avatarUriString)
        log("photo uri:" + authAccount.avatarUri)
        log("email:" + authAccount.email)
        log("openid:" + authAccount.openId)
        log("unionid:" + authAccount.unionId)
        // Id token can be verified as it is posted in the official examples:
        // https://github.com/HMS-Core/huawei-account-demo
        log("idToken:" + authAccount.idToken) // returned only if id token type auth
        // After obtaining the authorization code, your app needs to send it to the app server
        log("Authorization code:" + authAccount.authorizationCode) // returned only if authorization code type auth
        toast("auth success ${authAccount.email}")
    }

    // This method can be used to test enableSMSByConsentParser() method below
    // This method requires SEND_SMS permission (please, see commented lines in AndroidManifest.xml)
    private fun sendSMS() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            @SuppressLint("ObsoleteSdkInt") val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this@AccountActivity.getSystemService(SmsManager::class.java)
            } else SmsManager.getDefault()

            smsManager.sendTextMessage(
                phoneNumberToReceiveSMSFrom,  // phone number to send to (you can send sms to yourself)
                null,
                "Your verification code is 1234",
                null,
                null
            )
        } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1008)
    }

    // This launches SMS listening. After SMS from a defined phone number comes, receiver is triggered
    private fun enableSMSByConsentParser() {
        val intentFilter = IntentFilter(READ_SMS_BROADCAST_ACTION)
        registerReceiver(SmsBroadcastReceiver(), intentFilter)
        // phoneNumberToReceiveSMSFrom - Phone number to await SMS from - sms from other numbers won't be read)
        val task = ReadSmsManager.startConsent(this@AccountActivity, phoneNumberToReceiveSMSFrom) // change it to yours!
        task.addOnCompleteListener {
            if (task.isSuccessful) {
                toast("SMS listener awaiting").also{ log("SMS listener awaiting") }
            } else toast("ReadSmsManager failed to start").also{ log("ReadSmsManager failed to start") }
        }
    }

    private fun initViews() {
        findViewById<View>(R.id.HuaweiIdAuthButton).setOnClickListener { silentSignInWithoutIdVerification() }
        findViewById<View>(R.id.btnCancelAuth).setOnClickListener { revokeAuth() }
        findViewById<View>(R.id.btnSignOut).setOnClickListener { signOut() }
        findViewById<View>(R.id.btnSignInViaIdToken).setOnClickListener { silentSignInViaIdToken() }
        findViewById<View>(R.id.btnSignInViaOauth).setOnClickListener { silentSignInOauth() }
        findViewById<View>(R.id.btnEnableSmsParser).setOnClickListener { enableSMSByConsentParser() }
    }

    // Delivers caught sms content from SMSBroadcastReceiver to AccountActivity
    private val deliverSMSContentReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == SmsBroadcastReceiver.ACTION_SMS_READ_BROADCAST) {
                val sms = intent.extras?.getString(SmsBroadcastReceiver.KEY_SMS_TEXT)

                MaterialAlertDialogBuilder(context)
                    .setTitle("SMS content:")
                    .setMessage(sms)
                    .setNegativeButton("exit") { dialog, _ -> dialog?.dismiss() }
                    .setCancelable(true)
                    .show()
            }
        }
    }

    private val authResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: Intent()
                val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
                if (authAccountTask.isSuccessful) {
                    val authAccount = authAccountTask.result
                    printSuccessAuth(authAccount)
                } else log("sign in failed: " + (authAccountTask.exception as ApiException).statusCode)
            }
        }
}