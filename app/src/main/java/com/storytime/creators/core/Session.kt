package com.storytime.creators.core

import android.content.Context
import com.storytime.creators.core.auth.AuthStore
import com.storytime.creators.core.billing.BillingService
import com.storytime.creators.core.model.AppRouter
import com.storytime.creators.core.network.ApiClient
import com.storytime.creators.core.network.PersistentCookieJar
import com.storytime.creators.features.va.VAController

/**
 * App-wide singletons mirroring the iOS `.shared` services + environment objects.
 */
object Session {
    lateinit var api: ApiClient
        private set
    lateinit var auth: AuthStore
        private set
    lateinit var router: AppRouter
        private set
    lateinit var va: VAController
        private set
    lateinit var billing: BillingService
        private set

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val cookieJar = PersistentCookieJar(context.applicationContext)
        api = ApiClient(cookieJar)
        auth = AuthStore(api)
        router = AppRouter()
        va = VAController(api)
        billing = BillingService(context.applicationContext, api)
        billing.start()
        initialized = true
    }
}
