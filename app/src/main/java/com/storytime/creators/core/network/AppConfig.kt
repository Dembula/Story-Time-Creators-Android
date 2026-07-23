package com.storytime.creators.core.network

/** Production Story Time API origin (same backend as the creator web portal). */
object AppConfig {
    const val API_BASE_URL = "https://story-time.online"
    const val APP_NAME = "Story Time Creators"

    /** Only content creator accounts are supported in this app. */
    const val CREATOR_ROLE = "CONTENT_CREATOR"

    /** Feature flags for this native client. Marketplace payments stay disabled. */
    object Features {
        const val marketplacePaymentsEnabled = false
        const val auditionListingPaymentsEnabled = false
        const val executiveScriptReviewPaymentsEnabled = false
        const val catalogueUploadCheckoutEnabled = false
        const val licensePurchaseEnabled = false
        const val ipMarketplacePurchaseEnabled = false
        const val walletPayoutUIEnabled = false
    }
}
