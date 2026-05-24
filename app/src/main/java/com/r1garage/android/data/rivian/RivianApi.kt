package com.r1garage.android.data.rivian

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Rivian's consumer endpoint is a GraphQL gateway. We send raw GraphQL
 * payloads as JSON; the schema/queries are reverse-engineered from the
 * community-maintained unofficial-API repos.
 *
 * Base URL: https://rivian.com/api/gql/consumer/
 *
 * IMPORTANT: This is an unofficial endpoint. Authentication (login,
 * MFA, OTP) is intentionally NOT wired up in the scaffold — implement it
 * once you've decided how to handle the OTP/CAPTCHA challenges. Until
 * then the poller will record an auth-error alert and keep the dashboard
 * in "Not signed in" state.
 */
interface RivianApi {

    @POST("graphql")
    suspend fun graphql(@Body request: GraphQlRequest): GraphQlResponse
}

@Serializable
data class GraphQlRequest(
    val operationName: String,
    val query: String,
    val variables: Map<String, String> = emptyMap(),
)

@Serializable
data class GraphQlResponse(
    val data: VehicleStateData? = null,
    val errors: List<GraphQlError>? = null,
)

@Serializable
data class GraphQlError(val message: String)

@Serializable
data class VehicleStateData(val vehicleState: VehicleStateDto? = null)

/**
 * Subset of fields the poller cares about. Matches the published
 * `getVehicleState` query.
 */
@Serializable
data class VehicleStateDto(
    val gnssLocation: GnssDto? = null,
    val batteryLevel: ValueDto? = null,
    val distanceToEmpty: ValueDto? = null,
    val vehicleMileage: ValueDto? = null,
    val powerState: ValueDto? = null,
    val chargerState: ValueDto? = null,
    val chargerStatus: ValueDto? = null,
    val gearStatus: ValueDto? = null,
    val doorFrontLeftLocked: ValueDto? = null,
    val batteryHvThermalEvent: ValueDto? = null,
    val twelveVoltBatteryHealth: ValueDto? = null,
)

@Serializable
data class GnssDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class ValueDto(
    val value: String? = null,
    val timeStamp: String? = null,
)
