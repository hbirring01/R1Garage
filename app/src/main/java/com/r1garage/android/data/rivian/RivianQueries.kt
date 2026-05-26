package com.r1garage.android.data.rivian

/** Hand-written GraphQL queries. Mirrors the community schema. */
object RivianQueries {
    /**
     * Returns the enrolled vehicle list for the signed-in user. We use this
     * once per session to discover the user's `vehicleId`, which every
     * other query (vehicle state, charging history, …) requires.
     *
     * Read-only — does not wake the car.
     */
    const val GET_USER_INFO = """
        query GetUserInfo {
            currentUser {
                __typename
                id
                firstName
                vehicles {
                    id
                    vin
                    name
                    vehicle {
                        model
                    }
                }
            }
        }
    """

    const val GET_VEHICLE_STATE = """
        query GetVehicleState(${'$'}vehicleID: String!) {
            vehicleState(id: ${'$'}vehicleID) {
                gnssLocation { latitude longitude }
                batteryLevel { value timeStamp }
                distanceToEmpty { value timeStamp }
                vehicleMileage { value timeStamp }
                powerState { value timeStamp }
                chargerState { value timeStamp }
                chargerStatus { value timeStamp }
                gearStatus { value timeStamp }
                doorFrontLeftLocked { value timeStamp }
                twelveVoltBatteryHealth { value timeStamp }
            }
        }
    """
}
