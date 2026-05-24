package com.r1garage.android.data.rivian

/** Hand-written GraphQL queries. Mirrors the community schema. */
object RivianQueries {
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
