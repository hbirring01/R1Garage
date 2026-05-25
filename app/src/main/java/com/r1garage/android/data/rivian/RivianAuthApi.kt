package com.r1garage.android.data.rivian

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Rivian's *gateway* GraphQL endpoint. The auth mutations (CSRF bootstrap,
 * login, OTP, sign-out) live here — separate from the *consumer* gateway in
 * [RivianApi] which serves signed-in vehicle queries.
 *
 * Base URL: https://rivian.com/api/gql/gateway/
 */
interface RivianAuthApi {

    @POST("graphql")
    suspend fun graphql(@Body request: GraphQlRequest): GraphQlResponse
}
