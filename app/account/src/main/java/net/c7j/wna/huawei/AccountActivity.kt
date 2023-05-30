package net.c7j.wna.huawei

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.result.AuthAccount
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.api.entity.common.CommonConstant
import net.c7j.wna.huawei.account.R


// Auth scenarios are described here:
// https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-scenario-summary-0000001115918594
// 1. silentSignInWithoutIdVerification() - Your app can obtain basic information about their HUAWEI IDs,
// including nickname, profile picture, email address, UnionID, and OpenID.
// 2. silentSignInViaIdToken() - Sign in via ID token through the OpenID Connect protocol, Your app will obtain
// the user's ID token for identity verification, enabling the user to securely sign in.
// 3. silentSignInOauth() - Your app will obtain the user's authorization code (temporary authorization credential)
// for identity verification, enabling secure sign-in. After the user's identity verification information expires,
// your app server will use the refresh token to request a new access token from the Account Kit server.
// 4. Quick HUAWEI ID Sign-In to Apps That Apply for Only the OpenID or UnionID (is not covered by this example):
// https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-scenario-openid-unionid-0000001210738701
// 5. Independent Authorization (is not covered by this example), see more at official documentation:
// https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/independentsignin-0000001140395573
//
// Important! Please use HUAWEI ID button according to the guidelines:
// https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/huaweiidauthbutton-0000001050179025
class AccountActivity : BaseActivity() {

    private lateinit var mAuthManager: AccountAuthService
    private lateinit var mAuthParam: AccountAuthParams
    private var appId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        findViewById<View>(R.id.HuaweiIdAuthButton).setOnClickListener { silentSignInWithoutIdVerification() }
        findViewById<View>(R.id.btnCancelAuth).setOnClickListener { revokeAuth() }
        findViewById<View>(R.id.btnSignOut).setOnClickListener { signOut() }
        findViewById<View>(R.id.btnSignInViaIdToken).setOnClickListener { silentSignInViaIdToken() }
        findViewById<View>(R.id.btnSignInViaOauth).setOnClickListener { silentSignInOauth() }
        appId = AGConnectOptionsBuilder().build(this@AccountActivity).getString("client/app_id")
    }

    // Signs user out from his account.
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

    /**
     * With this method your app can obtain basic information about their HUAWEI IDs,
     * including nickname, profile picture, email address, UnionID, and OpenID.
     * Silent sign-in: If a user has authorized your app and signed in -
     * no authorization or sign-in screen will appear during subsequent sign-ins, and the user will directly sign in.
     * If the user has not authorized your app or signed in, your app will show the authorization or sign-in screen.
     */
    @Deprecated("current version of auth sdk yet not supports the new OS method")
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
                startActivityForResult(signInIntent, REQUEST_SIGN_IN_LOGIN)
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
    @Deprecated("current version of auth sdk yet not supports the new OS method")
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
                startActivityForResult(signInIntent, REQUEST_SIGN_IN_LOGIN)
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
    @Deprecated("current version of auth sdk yet not supports the new OS method")
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
                startActivityForResult(signInIntent, REQUEST_SIGN_IN_LOGIN)
            }
        }
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

    // prints successful authorization AuthAccount object's content to the log
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


    @Deprecated("current version of auth sdk yet not supports the new OS method")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SIGN_IN_LOGIN) { // Login success, get user message by parseAuthResultFromIntent
            val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
            if (authAccountTask.isSuccessful) {
                val authAccount = authAccountTask.result
                printSuccessAuth(authAccount)
            } else log("sign in failed: " + (authAccountTask.exception as ApiException).statusCode)
        }
    }

    companion object {
        const val REQUEST_SIGN_IN_LOGIN = 1002
    }
}