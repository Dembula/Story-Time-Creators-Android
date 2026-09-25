package com.storytime.creators.core.billing

import com.storytime.creators.core.network.AppConfig

/** Maps Play products to backend package keys (mirrors iOS CreatorStoreProduct). */
enum class CreatorStoreProduct(
    val productId: String,
    val title: String,
    val detail: String,
    val fallbackPriceLabel: String,
    val licensePackage: String?,
    val licenseBilling: String?,
    val isSubscription: Boolean,
) {
    uploadYearly(
        productId = AppConfig.IAP.uploadYearly,
        title = "Catalogue unlimited",
        detail = "Unlimited catalogue uploads for one year. Charged through Google Play.",
        fallbackPriceLabel = AppConfig.IAP.FallbackPrice.uploadYearly,
        licensePackage = "UPLOAD_YEARLY",
        licenseBilling = "YEARLY",
        isSubscription = true,
    ),
    pipelineMonthly(
        productId = AppConfig.IAP.pipelineMonthly,
        title = "Full pipeline · monthly",
        detail = "Pre-production, production & post tools. Billed monthly via Google Play.",
        fallbackPriceLabel = AppConfig.IAP.FallbackPrice.pipelineMonthly,
        licensePackage = "PIPELINE",
        licenseBilling = "MONTHLY",
        isSubscription = true,
    ),
    pipelineYearly(
        productId = AppConfig.IAP.pipelineYearly,
        title = "Full pipeline · yearly",
        detail = "Same pipeline access with annual billing via Google Play.",
        fallbackPriceLabel = AppConfig.IAP.FallbackPrice.pipelineYearly,
        licensePackage = "PIPELINE",
        licenseBilling = "YEARLY",
        isSubscription = true,
    ),
    perFilmUpload(
        productId = AppConfig.IAP.perFilmUpload,
        title = "Per-film upload fee",
        detail = "Required before a pay-per-film title is submitted for admin review.",
        fallbackPriceLabel = AppConfig.IAP.FallbackPrice.perFilmUpload,
        licensePackage = null,
        licenseBilling = null,
        isSubscription = false,
    );

    val isUploadFee: Boolean get() = this == perFilmUpload

    companion object {
        val planProducts = listOf(uploadYearly, pipelineMonthly, pipelineYearly)

        fun fromProductId(id: String): CreatorStoreProduct? =
            entries.firstOrNull { it.productId == id }
    }
}

/** Free plan — no Play product; activates pay-per-film with zero signup fee. */
object CreatorFreePlanOption {
    const val title = "Pay per film"
    const val detail =
        "No yearly fee. Pay a per-title upload fee with Google Play when you submit each film for review."
    const val packageKey = "PER_FILM"
    const val priceLabel = "Free to start"
    const val badge = "Pay as you upload"
}

enum class PurchaseKind(val raw: String) {
    creatorLicense("creator_license"),
    contentUpload("content_upload"),
}
