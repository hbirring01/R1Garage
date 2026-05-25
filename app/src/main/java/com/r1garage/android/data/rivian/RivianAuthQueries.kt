package com.r1garage.android.data.rivian

/**
 * Hand-written auth GraphQL mutations. Mirrors the community-maintained
 * Rivian schema (bretterer/rivian-python-api, kaedea/rivian-api).
 *
 * GraphQL union responses are flattened in the DTOs in [AuthDtos.kt] —
 * kotlinx.serialization doesn't grok unions, so we discriminate on the
 * `__typename` field at the call site instead.
 */
object RivianAuthQueries {

    const val CREATE_CSRF_TOKEN = """
        mutation CreateCSRFToken {
            createCsrfToken {
                __typename
                csrfToken
                appSessionToken
            }
        }
    """

    const val LOGIN = """
        mutation Login(${'$'}email: String!, ${'$'}password: String!) {
            login(email: ${'$'}email, password: ${'$'}password) {
                __typename
                ... on MobileLoginResponse {
                    __typename
                    accessToken
                    refreshToken
                    userSessionToken
                }
                ... on MobileMFALoginResponse {
                    __typename
                    otpToken
                }
            }
        }
    """

    const val LOGIN_WITH_OTP = """
        mutation LoginWithOTP(${'$'}email: String!, ${'$'}otpCode: String!, ${'$'}otpToken: String!) {
            loginWithOTP(email: ${'$'}email, otpCode: ${'$'}otpCode, otpToken: ${'$'}otpToken) {
                __typename
                ... on MobileLoginResponse {
                    __typename
                    accessToken
                    refreshToken
                    userSessionToken
                }
            }
        }
    """
}
