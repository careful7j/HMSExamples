package net.c7j.wna.huawei

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.hms.iap.entity.ProductInfoResult
import net.c7j.wna.huawei.iap.R

// This activity shows how obtain purchase history of products of all kinds
class IapHistoryActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iap_history)

        queryPurchasedItems(IapClient.PriceType.IN_APP_SUBSCRIPTION)

        findViewById<MaterialButton>(R.id.btnIapConsumable).setOnClickListener {
            queryPurchasedItems(IapClient.PriceType.IN_APP_CONSUMABLE)
        }
        findViewById<MaterialButton>(R.id.btnIapNonConsumable).setOnClickListener {
            queryPurchasedItems(IapClient.PriceType.IN_APP_NONCONSUMABLE)
        }
        findViewById<MaterialButton>(R.id.btnIapSubscription).setOnClickListener {
            queryPurchasedItems(IapClient.PriceType.IN_APP_SUBSCRIPTION)
        }
    }

    @Suppress("CascadeIf")
    @SuppressLint("SetTextI18n")
    // Obtains the purchase information of all consumed products or the receipts of all subscriptions
    // For consumables (type = 0) and subscriptions (type = 2): obtainOwnedPurchaseRecord()
    // For non-consumable (type = 1): obtainOwnedPurchases()
    private fun queryPurchasedItems(type: Int) {
        val textView = findViewById<TextView>(R.id.tvIapInfoHistory)
        val iapClient = Iap.getIapClient(this)

        if (type == IapClient.PriceType.IN_APP_NONCONSUMABLE) {
            IapRequestHelper.obtainOwnedPurchases(this@IapHistoryActivity, object : IapResultCallback {

                override fun onIapDataReceived(result: OwnedPurchasesResult?) {
                    log( "obtainOwnedPurchaseRecord, success")
                    val inAppPurchaseDataList = result?.inAppPurchaseDataList
                    var output = "Owned products: \n"
                    if (inAppPurchaseDataList.isNullOrEmpty()) {
                        textView.text = "no data"
                    } else {
                        for (i in inAppPurchaseDataList.indices) {
                            output = output + "\n\n" + inAppPurchaseDataList[i].toString()
                        }
                        log(output)
                        textView.text = output
                    }
                    log("list size: " + inAppPurchaseDataList?.size)
                }

                override fun onIapDataFailed(e: Exception?) {
                    log( "obtainOwnedPurchaseRecord, " + e?.message)
                    IapExceptionHandler.handle(this@IapHistoryActivity, e, false)
                }

                override fun onProductsLoaded(result: ProductInfoResult?) { /* NO OP */ }

                override fun onProductsLoadFailed(e: Exception?) { /* NO OP */ }

                override fun onEnvironmentReady() { /* NO OP */ }

            },IapClient.PriceType.IN_APP_NONCONSUMABLE )

        } else if (type == IapClient.PriceType.IN_APP_CONSUMABLE || type == IapClient.PriceType.IN_APP_SUBSCRIPTION) {
            IapRequestHelper.obtainOwnedPurchaseRecord(
                iapClient,
                type,
                null,
                object : IapApiCallback<OwnedPurchasesResult?>
                {

                    override fun onSuccess(result: OwnedPurchasesResult?) {
                        log( "obtainOwnedPurchaseRecord, success")
                        val inAppPurchaseDataList = result?.inAppPurchaseDataList
                        var output = "Owned products: \n"
                        if (inAppPurchaseDataList.isNullOrEmpty()) {
                            textView.text = "no data"
                        } else {
                            for (i in inAppPurchaseDataList.indices) {
                                output = output + "\n\n" + inAppPurchaseDataList[i].toString()
                            }
                            log(output)
                            textView.text = output
                        }
                        log("list size: " + inAppPurchaseDataList?.size)
                    }

                    override fun onFail(e: Exception?) {
                        log( "obtainOwnedPurchaseRecord, " + e?.message)
                        IapExceptionHandler.handle(this@IapHistoryActivity, e, false)
                    }
                })

        } else log("Unsupported \"type\" of product, shall be one of: 0, 1 or 2")
    }

}