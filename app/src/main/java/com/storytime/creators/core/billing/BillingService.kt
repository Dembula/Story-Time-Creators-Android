package com.storytime.creators.core.billing

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.storytime.creators.core.model.AndroidPurchaseBody
import com.storytime.creators.core.model.AndroidPurchaseResponse
import com.storytime.creators.core.model.DistributionLicenseBody
import com.storytime.creators.core.model.DistributionLicenseResponse
import com.storytime.creators.core.network.ApiClient
import com.storytime.creators.core.network.ApiException
import com.storytime.creators.core.network.AppConfig
import com.storytime.creators.core.network.post
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Google Play Billing service mirroring iOS StoreKitService.
 * Purchases are reported to POST /api/creator/android/purchase (with distribution-license fallback).
 */
class BillingService(
    context: Context,
    private val client: ApiClient,
) : PurchasesUpdatedListener {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    var isReady by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var lastError by mutableStateOf<String?>(null)
    var productDetails by mutableStateOf<Map<String, ProductDetails>>(emptyMap())
        private set

    private var purchaseContinuation: CancellableContinuation<Purchase>? = null
    private var awaitingProductId: String? = null

    fun start() {
        if (billingClient.isReady) {
            isReady = true
            scope.launch { loadProducts() }
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                isReady = result.responseCode == BillingClient.BillingResponseCode.OK
                if (isReady) {
                    scope.launch { loadProducts() }
                } else {
                    lastError = "Play Billing unavailable (${result.debugMessage})."
                }
            }

            override fun onBillingServiceDisconnected() {
                isReady = false
            }
        })
    }

    fun displayPrice(kind: CreatorStoreProduct): String {
        val details = productDetails[kind.productId] ?: return kind.fallbackPriceLabel
        return if (kind.isSubscription) {
            details.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
                ?.formattedPrice
                ?: kind.fallbackPriceLabel
        } else {
            details.oneTimePurchaseOfferDetails?.formattedPrice ?: kind.fallbackPriceLabel
        }
    }

    suspend fun loadProducts() {
        if (!AppConfig.Features.playBillingEnabled) return
        isLoading = true
        lastError = null
        try {
            ensureConnected()
            val subs = queryDetails(AppConfig.IAP.subscriptionProductIds, BillingClient.ProductType.SUBS)
            val ones = queryDetails(AppConfig.IAP.consumableProductIds, BillingClient.ProductType.INAPP)
            productDetails = (subs + ones).associateBy { it.productId }
            if (productDetails.isEmpty()) {
                lastError =
                    "Store products unavailable. Create the Play Console IAPs listed in PLAY_BILLING_PRODUCTS.md."
            }
        } catch (e: Exception) {
            lastError = e.message
        } finally {
            isLoading = false
        }
    }

    /** Activate pay-per-film plan on the server (no Play charge). */
    suspend fun activateFreePerFilmPlan() {
        val res: DistributionLicenseResponse = client.post(
            "/api/creator/distribution-license",
            DistributionLicenseBody(packageKey = CreatorFreePlanOption.packageKey, source = "android_play"),
        )
        if (!res.error.isNullOrBlank()) throw BillingError.Server(res.error)
        if (res.requiresPayment == true && AppConfig.Features.playBillingEnabled) {
            throw BillingError.Server("Pay-per-film should not require an upfront fee. Contact support.")
        }
    }

    /**
     * Launch Play purchase UI. Caller must [reportPurchaseToServer] then consume/acknowledge.
     */
    suspend fun purchase(activity: Activity, kind: CreatorStoreProduct): Purchase {
        ensureConnected()
        if (productDetails.isEmpty()) loadProducts()
        val details = productDetails[kind.productId]
            ?: throw BillingError.ProductUnavailable
        val productParams = if (kind.isSubscription) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
                ?: throw BillingError.ProductUnavailable
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .setOfferToken(offerToken)
                .build()
        } else {
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()

        return suspendCancellableCoroutine { cont ->
            purchaseContinuation = cont
            awaitingProductId = kind.productId
            cont.invokeOnCancellation {
                purchaseContinuation = null
                awaitingProductId = null
            }
            val result = billingClient.launchBillingFlow(activity, flowParams)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                purchaseContinuation = null
                awaitingProductId = null
                cont.resumeWithException(BillingError.LaunchFailed(result.debugMessage))
            }
        }
    }

    suspend fun reportPurchaseToServer(
        purchase: Purchase,
        kind: PurchaseKind,
        product: CreatorStoreProduct,
        contentId: String? = null,
    ): AndroidPurchaseResponse {
        val productId = purchase.products.firstOrNull() ?: product.productId
        val body = AndroidPurchaseBody(
            productId = productId,
            purchaseToken = purchase.purchaseToken,
            orderId = purchase.orderId,
            packageName = purchase.packageName.ifBlank { appContext.packageName },
            kind = kind.raw,
            packageKey = product.licensePackage,
            billing = product.licenseBilling,
            contentId = contentId,
            source = "android_app",
        )

        val response = try {
            client.post<AndroidPurchaseResponse, AndroidPurchaseBody>("/api/creator/android/purchase", body)
        } catch (e: ApiException.Http) {
            if (e.code == 404 && kind == PurchaseKind.creatorLicense && product.licensePackage != null) {
                legacyActivateLicense(product, purchase)
                AndroidPurchaseResponse(ok = true, packageComplete = true)
            } else if (e.code == 404 && kind == PurchaseKind.contentUpload) {
                finishLocalPurchase(purchase, product.isUploadFee)
                throw BillingError.Server(
                    "Upload fee was charged, but the server could not mark the title paid. " +
                        "Deploy /api/creator/android/purchase, then contact support with order ${purchase.orderId}.",
                )
            } else {
                throw e
            }
        }

        if (!response.error.isNullOrBlank()) throw BillingError.Server(response.error)
        finishLocalPurchase(purchase, product.isUploadFee)
        return response
    }

    suspend fun restore(): List<Purchase> {
        ensureConnected()
        lastError = null
        val subs = queryPurchases(BillingClient.ProductType.SUBS)
        val ones = queryPurchases(BillingClient.ProductType.INAPP)
        return (subs + ones).filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
    }

    /** Re-report active subscriptions so the backend unlocks the matching license. */
    suspend fun restoreAndReport(): Boolean {
        val purchases = restore()
        var unlocked = false
        for (purchase in purchases) {
            val productId = purchase.products.firstOrNull() ?: continue
            val product = CreatorStoreProduct.fromProductId(productId) ?: continue
            if (product.isUploadFee) continue
            runCatching {
                reportPurchaseToServer(purchase, PurchaseKind.creatorLicense, product)
                unlocked = true
            }.onFailure { lastError = it.message }
        }
        return unlocked
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val cont = purchaseContinuation ?: return
        purchaseContinuation = null
        val wanted = awaitingProductId
        awaitingProductId = null

        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val match = purchases?.firstOrNull { p ->
                    wanted == null || p.products.contains(wanted)
                } ?: purchases?.firstOrNull()
                if (match != null) cont.resume(match)
                else cont.resumeWithException(BillingError.Unknown)
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                cont.resumeWithException(BillingError.UserCancelled)
            else ->
                cont.resumeWithException(BillingError.LaunchFailed(result.debugMessage))
        }
    }

    // region Private

    private suspend fun ensureConnected() {
        if (billingClient.isReady) return
        suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    isReady = result.responseCode == BillingClient.BillingResponseCode.OK
                    if (isReady) cont.resume(Unit)
                    else cont.resumeWithException(BillingError.LaunchFailed(result.debugMessage))
                }

                override fun onBillingServiceDisconnected() {
                    isReady = false
                }
            })
        }
    }

    private suspend fun queryDetails(ids: List<String>, type: String): List<ProductDetails> {
        if (ids.isEmpty()) return emptyList()
        val productList = ids.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(type)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        val result = billingClient.queryProductDetails(params)
        return if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            result.productDetailsList.orEmpty()
        } else {
            emptyList()
        }
    }

    private suspend fun queryPurchases(type: String): List<Purchase> {
        val result = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(type).build(),
        )
        return if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            result.purchasesList
        } else {
            emptyList()
        }
    }

    private suspend fun finishLocalPurchase(purchase: Purchase, consumable: Boolean) {
        if (consumable) {
            billingClient.consumePurchase(
                ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(),
            )
        } else if (!purchase.isAcknowledged) {
            billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build(),
            )
        }
    }

    private suspend fun legacyActivateLicense(product: CreatorStoreProduct, purchase: Purchase) {
        val res: DistributionLicenseResponse = client.post(
            "/api/creator/distribution-license",
            DistributionLicenseBody(
                packageKey = product.licensePackage,
                billing = product.licenseBilling,
                source = "android_play",
                googleOrderId = purchase.orderId,
                googleProductId = product.productId,
                googlePurchaseToken = purchase.purchaseToken,
            ),
        )
        if (!res.error.isNullOrBlank()) throw BillingError.Server(res.error)
        if (res.requiresPayment == true) {
            throw BillingError.Server(
                "Play purchase succeeded, but the studio still requires web payment. " +
                    "Deploy /api/creator/android/purchase, then Restore purchases.",
            )
        }
        finishLocalPurchase(purchase, consumable = false)
    }

    // endregion
}

sealed class BillingError(message: String) : Exception(message) {
    data object UserCancelled : BillingError("Purchase cancelled.")
    data object ProductUnavailable : BillingError("This product is not available in the store yet.")
    data object Unknown : BillingError("Purchase failed.")
    class LaunchFailed(detail: String?) : BillingError(detail?.ifBlank { null } ?: "Could not open Google Play.")
    class Server(msg: String) : BillingError(msg)
}
