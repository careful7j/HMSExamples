package net.c7j.wna.huawei

import android.content.Intent
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient.PriceType
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.hms.iap.entity.ProductInfoResult
import com.huawei.hms.iap.entity.PurchaseIntentResult
import net.c7j.wna.huawei.iap.R

// This activity shows how to purchase subscriptions
class IapSubscriptionActivity : BaseActivity(), IapResultCallback {

    val sub = IapRequestHelper.TEST_SUB1 // you can replace with other products from helper when testing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iap_subscription)
        /**
         * If true: confirms country/region where HUAWEI ID is signed-in is located supports HUAWEI IAP, otherwise:
         * @see IapExceptionHandler.handle
         */
        IapRequestHelper.isEnvironmentReady(this, this)
        IapRequestHelper.obtainOwnedPurchases(this, this, PriceType.IN_APP_SUBSCRIPTION)

        findViewById<MaterialButton>(R.id.btnPurchaseSubscription).setOnClickListener { purchase() }
        findViewById<MaterialButton>(R.id.btnCheckSubscriptions).setOnClickListener {
            IapRequestHelper.showSubscription(this@IapSubscriptionActivity, sub)
        }
    }

    private fun purchase() {
        IapRequestHelper.createPurchaseIntent(
            this, sub, PriceType.IN_APP_SUBSCRIPTION, object : IapApiCallback<PurchaseIntentResult?> {

                override fun onSuccess(result: PurchaseIntentResult?) {
                    IapRequestHelper.obtainOwnedPurchases(this@IapSubscriptionActivity, object : IapResultCallback {

                        override fun onEnvironmentReady() {}

                        override fun onIapDataReceived(result: OwnedPurchasesResult?) {
                            IapRequestHelper.ownedPurchasesResult = result
                            log("result: ${result?.itemList}")
                            if (result?.itemList?.contains(sub) == true) {
                                log("already has active $sub subscription")
                            } else log("Doesn't have active $sub subscription yet")
                        }

                        override fun onIapDataFailed(e: Exception?) {
                            toast("apply subscription failed")
                        }

                        override fun onProductsLoaded(result: ProductInfoResult?) {}

                        override fun onProductsLoadFailed(e: Exception?) {}

                    }, PriceType.IN_APP_SUBSCRIPTION)
                }

                override fun onFail(e: Exception?) {
                    toast("purchase failed: $e")
                }
            })
    }

    /**
     *  When country/region is supported check was finished in success. Otherwise is handled inside:
     *  @see IapRequestHelper.isEnvironmentReady */
    override fun onEnvironmentReady() {
        log("IAP Kit is ready to operate")
    }

    // returns owned purchases after IapRequestHelper.obtainOwnedPurchases() was called
    override fun onIapDataReceived(result: OwnedPurchasesResult?) {
        IapRequestHelper.ownedPurchasesResult = result
        log("$result")
    }

    // returns owned purchases Exception after IapRequestHelper.obtainOwnedPurchases() was called
    override fun onIapDataFailed(e: Exception?) {
        log("failed to load subscription data: $e")
    }

    override fun onProductsLoaded(result: ProductInfoResult?) {
        log("${result?.productInfoList}")
    }

    override fun onProductsLoadFailed(e: Exception?) {
        log("failed to load products: $e")
    }

    // After purchase is finished and you return back to your app, IAP Kit returns purchase result
    @Deprecated("current sdk version sadly yet not support the new way")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        log( "onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IapExceptionHandler.REQ_CODE_BUY) {
            if (data == null) {
                log( "data is null")
                return
            }
            val purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data)
            when (purchaseResultInfo.returnCode) {
                OrderStatusCode.ORDER_STATE_CANCEL -> log("Order has been canceled!")
                OrderStatusCode.ORDER_STATE_FAILED -> log("Order failed!")
                OrderStatusCode.ORDER_PRODUCT_OWNED -> log("Order product is already owned!")
                OrderStatusCode.ORDER_STATE_SUCCESS -> log("Purchase success!")
                else -> {}
            }
            return
        }
    }
}