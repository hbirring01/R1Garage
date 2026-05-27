package com.r1garage.android.data.rivian

import kotlinx.serialization.json.Json

/**
 * Lenient Json instance for decoding Rivian GraphQL response payloads.
 *
 * - `ignoreUnknownKeys`: Rivian's union-flattened responses always carry
 *   extra fields the specific data class doesn't claim.
 * - `explicitNulls = false`: tolerate missing optional fields without
 *   requiring every property to be nullable explicitly.
 * - `isLenient = true`: `vehicleState` returns a polymorphic `value`
 *   field — numeric for SoC / mileage / range, boolean for door /
 *   lock state, string for gear / power / charger state. Our DTO
 *   models them uniformly as `String?` and the mapper parses with
 *   `toDoubleOrNull()` / `equals(...)`, so lenient mode lets the
 *   decoder coerce unquoted JSON primitives into the string form
 *   instead of throwing
 *   "string literal for value of key 'value' should be quoted".
 */
val AuthDtosJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
}
