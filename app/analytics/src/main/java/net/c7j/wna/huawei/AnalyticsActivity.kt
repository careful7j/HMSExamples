package net.c7j.wna.huawei

import android.os.Build
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import net.c7j.wna.huawei.analytics.R


@Suppress("MemberVisibilityCanBePrivate")
class AnalyticsActivity : BaseActivity() {

    private var hiAnalyticsInstance: HiAnalyticsInstance? = null
    private lateinit var bundle1 : Bundle
    private lateinit var bundle2 : Bundle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        hiAnalyticsInstance = HiAnalytics.getInstance(applicationContext)

        // Test events sent in real time in AGC console:
        // adb shell setprop debug.huawei.hms.analytics.app <package_name>
        // adb shell setprop debug.huawei.hms.analytics.app .none
        // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-accessing-0000001050161888#section1284414416588
        setUpUserId()
        initViews()
    }

    // (Optional) Binds user session with some identifier, for instance you can use user's AAID, more about AAID:
    // https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/Development-Guide#h1-6-accessing-analytics
    fun setUpUserId() {
        hiAnalyticsInstance?.setUserId(HmsInstanceId.getInstance(applicationContext).id)
        with(HmsInstanceId.getInstance(applicationContext).id) {
            log("AAID: $this")
            toast("AAID: $this")
        }
    }

    // Events limitations: 500 events per application, event name max length is 256 symbols
    fun reportEventNoBundle() = hiAnalyticsInstance?.onEvent(EVENT_NO_BUNDLE, null)

    // Analytics will display event bundles' parameters ONLY AFTER you add this parameters at:
    // All services -> My Projects -> HUAWEI Analytics -> Behavior Analysis -> Event Analysis
    // One event can have no more then 25 parameters,
    // One application can have no more then 100 parameters,
    // Parameter name max length is 256 symbols
    fun reportEventBundle() {
        bundle1 = Bundle()
        bundle1.putString(PARAM_1, "PEW PEW")
        bundle1.putString(PARAM_2, "PEW PEW")
        hiAnalyticsInstance?.onEvent(EVENT_WITH_BUNDLE, bundle1)
        toast("Sent EVENT_WITH_BUNDLE")

        bundle2 = Bundle()
        bundle2.putString(PARAM_DEVICE, Build.DEVICE)
        bundle2.putString(PARAM_MANUFACTURER, Build.MANUFACTURER)
        bundle2.putString(PARAM_MODEL, Build.MODEL)
        hiAnalyticsInstance?.onEvent(EVENT_WITH_DEVICE_INFO_BUNDLE, bundle2)
        toast("Sent EVENT_WITH_DEVICE_INFO_BUNDLE")
    }


    // Sends Arraylist<Bundle> of events:
    // https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/complex-data-type-0000001125647717
    // The outermost Bundle supports only one nested Bundle and an ArrayList object.
    // Parameters of the nested Bundle support only basic data types, while elements in the ArrayList
    // object support only the Bundle type or basic data types. In addition, parameters of the Bundle
    // in the ArrayList object support only basic data types, and the ArrayList object supports a maximum of 50 elements.
    fun reportArraylistBundle() {
        if (::bundle1.isInitialized && ::bundle2.isInitialized) {
            val listBundle: ArrayList<Bundle> = ArrayList()
            listBundle.add(bundle1)
            listBundle.add(bundle2)

            val commonBundle1 = Bundle()
            commonBundle1.putParcelableArrayList("items", listBundle)
            hiAnalyticsInstance?.onEvent(EVENT_WITH_ARRAYLIST_BUNDLE, commonBundle1)

            toast("Sent EVENT_WITH_ARRAYLIST_BUNDLE")
        } else toast("""Use "Send Bundle event" first, please""")
    }

    override fun onStart() {
        super.onStart()
        // Collects information which application screens are popular to spend time by user.
        // Activities are tracked automatically, Don't use it to track Activities!
        // If you want to track exact custom window or fragment, you can use:
        // https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/android-api-hianalytics-instance-0000001050987219#section111581350115113
        hiAnalyticsInstance?.pageStart("screen-name1", "net.c7j.wna.huawei.AnalyticsActivity")
    }

    override fun onStop() {
        super.onStop()
        // After pageStart() you shall not to forget pageEnd() accordingly:
        hiAnalyticsInstance?.pageEnd("screen-name1")
    }

    private fun initViews() {
        findViewById<MaterialButton>(R.id.btnSendEvent).setOnClickListener { reportEventNoBundle() }
        findViewById<MaterialButton>(R.id.btnSendEventBundle).setOnClickListener { reportEventBundle() }
        findViewById<MaterialButton>(R.id.btnSendEventArraylist).setOnClickListener { reportArraylistBundle() }
        findViewById<MaterialButton>(R.id.btnForceCrash).setOnClickListener {
            toast("threw java.lang.RuntimeException()")
            throw java.lang.RuntimeException()
        }
    }


    companion object {
        //Event names:
        const val EVENT_NO_BUNDLE = "EVENT_NO_BUNDLE"
        const val EVENT_WITH_BUNDLE = "EVENT_WITH_BUNDLE"
        const val EVENT_WITH_DEVICE_INFO_BUNDLE = "EVENT_WITH_DEVICE_INFO_BUNDLE"
        const val EVENT_WITH_ARRAYLIST_BUNDLE = "EVENT_WITH_ARRAYLIST_BUNDLE"

        //Event parameters:
        const val PARAM_1 = "ONE"
        const val PARAM_2 = "TWO"
        const val PARAM_DEVICE = "Device"
        const val PARAM_MANUFACTURER = "Manufacturer"
        const val PARAM_MODEL = "Model"
    }
}