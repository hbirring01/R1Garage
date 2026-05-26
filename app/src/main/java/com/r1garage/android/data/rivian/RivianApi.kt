package com.r1garage.android.data.rivian

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Rivian's consumer endpoint is a GraphQL gateway. The signed-in queries
 * (vehicle state, vehicle list, charging history) live here.
 *
 * Base URL: https://rivian.com/api/gql/consumer/
 *
 * Auth tokens (Csrf-Token / A-Sess / U-Sess) are attached by
 * [RivianAuthInterceptor]; this interface stays pure transport.
 */
interface RivianApi {

    @POST("graphql")
    suspend fun graphql(@Body request: GraphQlRequest): GraphQlResponse
}

@Serializable
data class GraphQlRequest(
    val operationName: String,
    val query: String,
    val variables: JsonElement,
)

@Serializable
data class GraphQlResponse(
    val data: JsonElement? = null,
    val errors: List<GraphQlError>? = null,
)

@Serializable
data class GraphQlError(val message: String)

// --- Vehicle state response shapes --------------------------------------

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

// --- User info / enrolled vehicles --------------------------------------

@Serializable
data class UserInfoData(val currentUser: CurrentUserDto? = null)

@Serializable
data class CurrentUserDto(
    val id: String? = null,
    val firstName: String? = null,
    val vehicles: List<EnrolledVehicleDto>? = null,
)

@Serializable
data class EnrolledVehicleDto(
    val id: String? = null,
    val vin: String? = null,
    val name: String? = null,
    val vehicle: EnrolledVehicleInnerDto? = null,
)

@Serializable
data class EnrolledVehicleInnerDto(
    val model: String? = null,
)

// --- Vehicle image CDN response ----------------------------------------

@Serializable
data class VehicleImagesData(
    val getVehicleImages: List<VehicleImageDto>? = null,
)

@Serializable
data class VehicleImageDto(
    val orderId: String? = null,
    val vehicleId: String? = null,
    val extension: String? = null,
    val resolution: String? = null,
    val size: String? = null,
    /** e.g. "main_exterior_front_3qtr" */
    val design: String? = null,
    /** e.g. "exterior", "interior" */
    val placement: String? = null,
    val url: String? = null,
)
