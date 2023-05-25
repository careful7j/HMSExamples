package net.c7j.wna.huawei

import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.hms.iap.entity.ProductInfoResult

interface IapResultCallback {

    fun onEnvironmentReady()

    fun onIapDataReceived(result: OwnedPurchasesResult?)

    fun onIapDataFailed(e: Exception?)

    fun onProductsLoaded(result: ProductInfoResult?)

    fun onProductsLoadFailed(e: Exception?)

}