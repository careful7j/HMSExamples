package net.c7j.wna.huawei

import android.app.Activity
import android.widget.Toast
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_HWID_NOT_LOGIN
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_PRODUCT_CONSUMED
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_PRODUCT_NOT_OWNED
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_PRODUCT_OWNED
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_STATE_CANCEL
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_STATE_NET_ERROR
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_STATE_PARAM_ERROR
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_VR_UNINSTALL_ERROR

// This handler is used for all types of purchases
object IapExceptionHandler {
    /** requestCode for pull up the pmsPay page */
    const val REQ_CODE_BUY = 4002
    /** requestCode for pull up the login page for isEnvReady interface  */
    const val REQ_CODE_LOGIN = 2001


    @JvmStatic
    fun handle(activity: Activity?, e: Exception?, isSilent: Boolean) {

        fun toast(activity: Activity?, text: String) = Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()

        if (e is IapApiException) {
            log("IAP return code: " + e.statusCode)
        } else {
            log("Unexpected non-IAPException: ${e?.message}")
            return
        }
        when (e.statusCode) {
            ORDER_STATE_CANCEL -> toast(activity, "Order has been canceled!")
            ORDER_STATE_PARAM_ERROR -> toast(activity, "Order state param error!")
            ORDER_STATE_NET_ERROR -> toast(activity, "Order state net error!")
            ORDER_VR_UNINSTALL_ERROR -> toast(activity, "Order vr uninstall error!")
            ORDER_PRODUCT_OWNED -> toast(activity, "Product already owned error!")
            ORDER_PRODUCT_NOT_OWNED -> toast(activity, "Product not owned error!")
            ORDER_PRODUCT_CONSUMED -> toast(activity, "Product consumed error!")
            ORDER_ACCOUNT_AREA_NOT_SUPPORTED -> toast(activity, "Order account area not supported error!")
            ORDER_NOT_ACCEPT_AGREEMENT -> toast(activity, "User does not agree the agreement")
            ORDER_HWID_NOT_LOGIN -> {
                if (!isSilent) { // To avoid annoying sign in request, it is recommended to ask only when necessary
                    toast(activity, "Sign in is required to perform purchase")
                    IapRequestHelper.startResolutionForResult(activity, e.status, REQ_CODE_LOGIN)
                }
            }
            else -> toast(activity, "Order unknown error!")
        }
    }
}
