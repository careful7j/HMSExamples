package net.c7j.wna.huawei

import android.content.Intent
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.InAppPurchaseData
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.hms.iap.entity.ProductInfoResult
import com.huawei.hms.iap.entity.PurchaseIntentResult
import net.c7j.wna.huawei.IapExceptionHandler.REQ_CODE_BUY
import net.c7j.wna.huawei.iap.R

// This activity shows how to purchase/consume consumable goods
// Consumable goods can be consumed and purchased again and again
class IapConsumableActivity : BaseActivity(), IapResultCallback {

    private var iapClient : IapClient? = null
    private val testConsumable = IapRequestHelper.TEST_CON2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iap_consumable)
        iapClient = Iap.getIapClient(this)
        /**
         * If true: confirms country/region where HUAWEI ID is signed-in is located supports HUAWEI IAP, otherwise:
         * @see IapExceptionHandler.handle
         */
        IapRequestHelper.isEnvironmentReady(this, this)
        IapRequestHelper.obtainOwnedPurchases(this, this, IapClient.PriceType.IN_APP_CONSUMABLE)

        findViewById<MaterialButton>(R.id.btnPurchaseConsumable).setOnClickListener { purchase() }
    }

    private fun purchase() {
        IapRequestHelper.createPurchaseIntent(
            this,
            testConsumable,
            IapClient.PriceType.IN_APP_CONSUMABLE,
            object : IapApiCallback<PurchaseIntentResult?> {

                override fun onSuccess(result: PurchaseIntentResult?) {
                    toast("purchase onSuccess()")
                    IapRequestHelper.obtainOwnedPurchases(this@IapConsumableActivity, object : IapResultCallback {

                        override fun onEnvironmentReady() {}

                        override fun onIapDataReceived(result: OwnedPurchasesResult?) {
                            IapRequestHelper.ownedPurchasesResult = result
                            when (result?.returnCode) {
                                OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                                    toast("cannot create order: item was already purchased")
                                    log("cannot create order: item was already purchased")
                                }
                                OrderStatusCode.ORDER_STATE_SUCCESS -> {
                                    toast("purchase order created")
                                    log("purchase order created, purchase window opened")
                                }
                                else -> log("Unexpected returnCode: ${result?.returnCode}")
                            }
                        }

                        override fun onIapDataFailed(e: Exception?) {
                            toast("Iap data failed to load")
                        }

                        override fun onProductsLoaded(result: ProductInfoResult?) {}

                        override fun onProductsLoadFailed(e: Exception?) {}

                    }, IapClient.PriceType.IN_APP_CONSUMABLE)
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
        log("ownedPurchasesResult is: ${result?.inAppPurchaseDataList.toString()}")
//        consumeAllNotYetConsumed(result)
    }

    // returns owned purchases Exception after IapRequestHelper.obtainOwnedPurchases() was called
    override fun onIapDataFailed(e: Exception?) {
        log("failed to load products data: $e")
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
        if (requestCode == REQ_CODE_BUY) {
            if (data == null) {
                log( "data is null")
                return
            }
            val purchaseResultInfo = Iap.getIapClient(this).parsePurchaseResultInfoFromIntent(data)
            when (purchaseResultInfo.returnCode) {
                OrderStatusCode.ORDER_STATE_CANCEL -> log("Order has been canceled!")
                OrderStatusCode.ORDER_STATE_FAILED, OrderStatusCode.ORDER_PRODUCT_OWNED -> log("Order failed or already owned!")
                OrderStatusCode.ORDER_STATE_SUCCESS -> {
                    log("Purchase success!")
                    // Here we consume the product we purchased. Can consume it later, depending on a business logic
                    val inAppPurchaseData = InAppPurchaseData(purchaseResultInfo.inAppPurchaseData)
                    inAppPurchaseData.purchaseToken
                    IapRequestHelper.consumeOwnedPurchase(iapClient, inAppPurchaseData.purchaseToken)
                    log("product consumed")
                }
                else -> {}
            }
            return
        }
    }

    // You can use this method in case you have lost some consumable in non-consumed state
    private fun consumeAllNotYetConsumed(result: OwnedPurchasesResult?) {
        if (result?.inAppPurchaseDataList == null) return
        for (i in result.inAppPurchaseDataList.indices) {
            IapRequestHelper.consumeOwnedPurchase(
                iapClient, (InAppPurchaseData(result.inAppPurchaseDataList[i])).purchaseToken)
            log("product consumed")
        }
    }
}