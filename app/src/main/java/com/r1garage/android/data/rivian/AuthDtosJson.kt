package com.r1garage.android.data.rivian

import kotlinx.serialization.json.Json

/**
 * Lenient Json instance for decoding auth GraphQL response payloads (the
 * union flattening means there's always going to be extra fields the
 * specific data class doesn't claim).
 */
val AuthDtosJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
