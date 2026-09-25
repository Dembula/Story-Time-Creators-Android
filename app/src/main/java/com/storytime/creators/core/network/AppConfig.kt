package com.storytime.creators.core.network

/** Production Story Time API origin (same backend as the creator web portal). */
object AppConfig {
    const val API_BASE_URL = "https://story-time.online"
    const val APP_NAME = "Story Time Creators"

    /** Only content creator accounts are supported in this app. */
    const val CREATOR_ROLE = "CONTENT_CREATOR"

    const val TERMS_URL = "https://story-time.online/legal/terms"
    const val PRIVACY_URL = "https://story-time.online/legal/privacy"
    const val MANAGE_SUBS_URL = "https://play.google.com/store/account/subscriptions"

    /**
     * Google Play product IDs — keep in sync with iOS App Store IDs and
     * Play Console listings (see PLAY_BILLING_PRODUCTS.md).
     */
    object IAP {
        const val planPerFilmPackage = "PER_FILM"

        const val uploadYearly = "online.storytime.creators.sub.upload.yearly"
        const val pipelineMonthly = "online.storytime.creators.sub.pipeline.monthly"
        const val pipelineYearly = "online.storytime.creators.sub.pipeline.yearly"
        const val perFilmUpload = "online.storytime.creators.upload.perfilm"

        val subscriptionProductIds = listOf(uploadYearly, pipelineMonthly, pipelineYearly)
        val consumableProductIds = listOf(perFilmUpload)
        val allProductIds = subscriptionProductIds + consumableProductIds

        /**
         * Fallback display prices when Play Catalog has not returned localized
         * prices yet. Matches iOS Configuration.storekit (USD).
         * Web/ZAR equivalents: R99.99 · R599.99/yr · R209.99/mo · R1,999.99/yr.
         */
        object FallbackPrice {
            const val perFilmUpload = "$9.99"
            const val uploadYearly = "$59.99/yr"
            const val pipelineMonthly = "$19.99/mo"
            const val pipelineYearly = "$199.99/yr"
        }
    }

    object Features {
        /** Creator plans + per-film upload fee via Google Play Billing. */
        const val playBillingEnabled = true
        const val webDigitalCheckoutEnabled = false

        const val marketplacePaymentsEnabled = false
        const val auditionListingPaymentsEnabled = false
        const val executiveScriptReviewPaymentsEnabled = false
        const val catalogueUploadCheckoutEnabled = true
        const val licensePurchaseEnabled = true
        const val ipMarketplacePurchaseEnabled = false
        const val walletPayoutUIEnabled = false
    }
}
