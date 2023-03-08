package com.huawei.hms.ads.sdk.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import net.c7j.wna.huawei.ads.R
import net.c7j.wna.huawei.consent.AdsConstant


class ProtocolDialog(private val mContext: Context)
    : Dialog(mContext, net.c7j.wna.huawei.box.R.style.ads_consent_dialog)
{
    private var titleTv: TextView? = null
    private var protocolTv: TextView? = null
    private var confirmButton: Button? = null
    private var cancelButton: Button? = null
    private var inflater: LayoutInflater? = null
    private var mCallback: ProtocolDialogCallback? = null


    interface ProtocolDialogCallback {
        fun onAgree()
        fun onCancel()
    }

    fun setCallback(callback: ProtocolDialogCallback?) { mCallback = callback }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        inflater = LayoutInflater.from(mContext)
        val fl = findViewById<LinearLayout>(R.id.base_dialog_layout)
        val rootView = inflater?.inflate(R.layout.dialog_privacy_huawei, fl,false) as LinearLayout
        setContentView(rootView)
        titleTv = findViewById(R.id.uniform_dialog_title)
        titleTv?.text = mContext.getString(net.c7j.wna.huawei.box.R.string.protocol_title)
        protocolTv = findViewById(R.id.protocol_center_content)
        initClickableSpan()
        initButtonBar(rootView)
    }

    private fun initButtonBar(rootView: LinearLayout) {
        confirmButton = rootView.findViewById(R.id.base_okBtn)
        confirmButton?.setOnClickListener {
            mContext.getSharedPreferences(AdsConstant.SP_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(AdsConstant.SP_PROTOCOL_KEY, AdsConstant.SP_PROTOCOL_AGREE)
                .apply()
            dismiss()
            mCallback?.onAgree()
        }
        cancelButton = rootView.findViewById(R.id.base_cancelBtn)
        cancelButton?.setOnClickListener {
            dismiss()
            mCallback?.onCancel()
        }
    }

    private fun initClickableSpan() {
        protocolTv?.movementMethod = ScrollingMovementMethod.getInstance()
        val privacyInfoText = mContext.getString(net.c7j.wna.huawei.box.R.string.protocol_content_text)
        val spanPrivacyInfoText = SpannableStringBuilder(privacyInfoText)
        val personalizedAdsTouchHere: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) { startActivity(ACTION_OAID_SETTING) }
        }
        val privacyStart = mContext.resources.getInteger(net.c7j.wna.huawei.box.R.integer.privacy_start)
        val privacyEnd = mContext.resources.getInteger(net.c7j.wna.huawei.box.R.integer.privacy_end)
        val privacySpan = StyleSpan(Typeface.BOLD)
        spanPrivacyInfoText.setSpan(privacySpan, privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val colorPersonalize = ForegroundColorSpan(Color.BLUE)
        val personalizedStart = mContext.resources.getInteger(net.c7j.wna.huawei.box.R.integer.personalized_start)
        val personalizedEnd = mContext.resources.getInteger(net.c7j.wna.huawei.box.R.integer.personalized_end)
        spanPrivacyInfoText.setSpan(personalizedAdsTouchHere, personalizedStart, personalizedEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanPrivacyInfoText.setSpan(colorPersonalize, personalizedStart, personalizedEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        protocolTv?.text = spanPrivacyInfoText
        protocolTv?.movementMethod = LinkMovementMethod.getInstance()
    }

    fun startActivity(action: String) {
        val enterNative = Intent(action)
        val pkgMng = mContext.packageManager
        if (pkgMng != null) {
            val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(
                    enterNative, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
            } else {
                context.packageManager.queryIntentActivities(enterNative, 0)
            }
            if (list.isNotEmpty()) {
                enterNative.setPackage("com.huawei.hwid")
                mContext.startActivity(enterNative)
            } else {
                addAlertView()
            }
        }
    }

    /** No function is available, ask user to install Huawei Mobile Services (APK) of the latest version */
    private fun addAlertView() {
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle(mContext.getString(net.c7j.wna.huawei.box.R.string.alert_title))
        builder.setMessage(mContext.getString(net.c7j.wna.huawei.box.R.string.alert_content))
        builder.setPositiveButton(mContext.getString(net.c7j.wna.huawei.box.R.string.alert_confirm), null)
        builder.show()
    }

    companion object {
        // private const val ACTION_SIMPLE_PRIVACY = "com.huawei.hms.ppskit.ACTION.SIMPLE_PRIVACY"
        private const val ACTION_OAID_SETTING = "com.huawei.hms.action.OAID_SETTING"
    }

}