package net.c7j.wna.huawei.consent

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.huawei.hms.ads.consent.bean.AdProvider
import com.huawei.hms.ads.consent.constant.ConsentStatus
import com.huawei.hms.ads.consent.inter.Consent
import net.c7j.wna.huawei.ads.R

// This dialog obtains consent from your users for the collection, use and sharing their personal data for personalized ads
class ConsentDialog(
    private val mContext: Context,
    private val madProviders: List<AdProvider>
    ) : Dialog(mContext, net.c7j.wna.huawei.box.R.style.ads_consent_dialog) {

        private var inflater: LayoutInflater? = null
        private var contentLayout: LinearLayout? = null
        private var titleTv: TextView? = null
        private var initInfoTv: TextView? = null
        private var moreInfoTv: TextView? = null
        private var partnersListTv: TextView? = null
        private var consentDialogView: View? = null
        private var initView: View? = null
        private var moreInfoView: View? = null
        private var partnersListView: View? = null
        private var consentYesBtn: Button? = null
        private var consentNoBtn: Button? = null
        private var moreInfoBackBtn: Button? = null
        private var partnerListBackBtn: Button? = null
        private var consentDialogCallback: ConsentDialogCallback? = null

    interface ConsentDialogCallback {
        fun updateConsentStatus(consentStatus: ConsentStatus)
    }

    fun setCallback(callback: ConsentDialogCallback?) { consentDialogCallback = callback }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        inflater = LayoutInflater.from(mContext)
        val fl = findViewById<LinearLayout>(R.id.consent_base_dialog_layout)
        titleTv = findViewById(R.id.consent_dialog_title_text)
        titleTv?.text = mContext.getString(net.c7j.wna.huawei.box.R.string.consent_title)
        consentDialogView = inflater?.inflate(R.layout.dialog_consent, fl, false)
        consentDialogView?.let { setContentView(it) }
        initView = inflater?.inflate(R.layout.dialog_consent_content, fl, false)
        moreInfoView = inflater?.inflate(R.layout.dialog_consent_moreinfo, fl, false)
        partnersListView = inflater?.inflate(R.layout.dialog_consent_partner_list, fl, false)
        showInitConsentInfo()
    }


    private fun updateConsentStatus(consentStatus: ConsentStatus) {
        Consent.getInstance(mContext).setConsentStatus(consentStatus)
        mContext.getSharedPreferences(AdsConstant.SP_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(AdsConstant.SP_CONSENT_KEY, consentStatus.value)
            .apply()
        consentDialogCallback?.updateConsentStatus(consentStatus)
    }


    private fun showInitConsentInfo() {
        addContentView(initView)
        addInitButtonAndLinkClick(consentDialogView)
    }

    private fun addInitButtonAndLinkClick(rootView: View?) {
        consentYesBtn = rootView?.findViewById(R.id.btn_consent_init_yes)
        consentYesBtn?.setOnClickListener {
            dismiss()
            updateConsentStatus(ConsentStatus.PERSONALIZED)
        }
        consentNoBtn = rootView?.findViewById(R.id.btn_consent_init_skip)
        consentNoBtn?.setOnClickListener {
            dismiss()
            updateConsentStatus(ConsentStatus.NON_PERSONALIZED)
        }
        initInfoTv = rootView?.findViewById(R.id.consent_center_init_content)
        initInfoTv?.movementMethod = ScrollingMovementMethod.getInstance()
        val initText = mContext.getString(net.c7j.wna.huawei.box.R.string.consent_init_text)
        val spanInitText = SpannableStringBuilder(initText)
        val initTouchHere: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) { showTouchHereInfo() }
        }
        val colorSpan = ForegroundColorSpan(Color.parseColor("#0000FF"))
        val initTouchHereStart = mContext.resources.getInteger(net.c7j.wna.huawei.box.R.integer.init_here_start)
        val initTouchHereEnd = mContext.resources.getInteger(net.c7j.wna.huawei.box.R.integer.init_here_end)
        spanInitText.setSpan(initTouchHere, initTouchHereStart, initTouchHereEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanInitText.setSpan(colorSpan, initTouchHereStart, initTouchHereEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        initInfoTv?.text = spanInitText
        initInfoTv?.movementMethod = LinkMovementMethod.getInstance()
    }


    fun showTouchHereInfo() {
        addContentView(moreInfoView)
        addMoreInfoButtonAndLinkClick(consentDialogView)
    }


    private fun addMoreInfoButtonAndLinkClick(rootView: View?) {
        moreInfoBackBtn = rootView!!.findViewById(R.id.btn_consent_more_info_back)
        moreInfoBackBtn?.setOnClickListener { showInitConsentInfo() }
        moreInfoTv = rootView.findViewById(R.id.consent_center_more_info_content)
        moreInfoTv?.movementMethod = ScrollingMovementMethod.getInstance()
        val moreInfoText = mContext.getString(net.c7j.wna.huawei.box.R.string.consent_more_info_text)
        val spanMoreInfoText = SpannableStringBuilder(moreInfoText)

        // Set the listener on the event for tapping some text.
        val moreInfoTouchHere: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showPartnersListInfo()
            }
        }
        val colorSpan = ForegroundColorSpan(Color.parseColor("#0000FF"))
        val moreInfoTouchHereStart = mContext.resources.getInteger(net.c7j.wna.huawei.box.R.integer.more_info_here_start)
        val moreInfoTouchHereEnd = mContext.resources.getInteger(net.c7j.wna.huawei.box.R.integer.more_info_here_end)
        spanMoreInfoText.setSpan(
            moreInfoTouchHere, moreInfoTouchHereStart, moreInfoTouchHereEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanMoreInfoText.setSpan(
            colorSpan, moreInfoTouchHereStart, moreInfoTouchHereEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        moreInfoTv?.text = spanMoreInfoText
        moreInfoTv?.movementMethod = LinkMovementMethod.getInstance()
    }


    fun showPartnersListInfo() {
        partnersListTv = partnersListView?.findViewById(R.id.partners_list_content)
        partnersListTv?.movementMethod = ScrollingMovementMethod.getInstance()
        partnersListTv?.text = ""
        val learnAdProviders = madProviders
        if (learnAdProviders.isNotEmpty()) {
            for (learnAdProvider in learnAdProviders) {
                val link = ("<font color='#0000FF'><a href=" + learnAdProvider.privacyPolicyUrl + ">"
                        + learnAdProvider.name + "</a>")
                partnersListTv?.append(Html.fromHtml(link, Html.FROM_HTML_MODE_LEGACY))
                partnersListTv?.append("  ")
            }
        } else {
            partnersListTv?.append(" 3rd party’s full list of advertisers is empty")
        }
        partnersListTv?.movementMethod = LinkMovementMethod.getInstance()
        addContentView(partnersListView)
        addPartnersListButtonAndLinkClick(consentDialogView)
    }


    private fun addPartnersListButtonAndLinkClick(rootView: View?) {
        partnerListBackBtn = rootView?.findViewById(R.id.btn_partners_list_back)
        partnerListBackBtn?.setOnClickListener { showTouchHereInfo() }
    }


    private fun addContentView(view: View?) {
        contentLayout = findViewById(R.id.consent_center_layout)
        contentLayout?.removeAllViews()
        contentLayout?.addView(view)
    }
}