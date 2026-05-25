package com.r1garage.android.data.rivian

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds the standard Rivian GraphQL gateway headers. Header names taken from
 * the published unofficial-API community work (bretterer/rivian-python-api,
 * the apollographql client name comes from the iOS app's bundle).
 *
 * Headers are only added if the corresponding token is present, which is
 * what makes the unauthenticated CSRF-bootstrap call work transparently.
 */
@Singleton
class RivianAuthInterceptor @Inject constructor(
    private val tokenStore: RivianTokenStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val builder = req.newBuilder()
            .header("User-Agent", "R1Garage/0.1 (Android)")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Apollographql-Client-Name", "com.rivian.ios.consumer-apollo-ios")

        tokenStore.csrfToken?.let { builder.header("Csrf-Token", it) }
        tokenStore.appSessionToken?.let { builder.header("A-Sess", it) }
        tokenStore.userSessionToken?.let { builder.header("U-Sess", it) }

        return chain.proceed(builder.build())
    }
}
