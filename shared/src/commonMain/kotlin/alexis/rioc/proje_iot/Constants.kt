package alexis.rioc.proje_iot

import kotlinx.serialization.Serializable

//const val IP_MACHINE = "10.114.44.180"  //Ecole
//const val IP_MACHINE = "192.168.1.125"  //Corbie
const val IP_MACHINE = "192.168.1.21" //Berck
//const val IP_MACHINE = "192.0.0.2"  //iPhone
const val SERVER_PORT = 8080

enum class DateRangeOption {
    Week, Month
}

@Serializable
data class UserCredentials(val login: String, val password: String)

@Serializable
data class DecodedPayload(
    val bpm: Int,
    val temperature: Int,
    val vitesse: Int
)

@Serializable
data class UplinkMessage(
    val end_device_ids: EndDeviceIds,
    val correlation_ids: List<String>,
    val received_at: String,
    val uplink_message: UplinkMessageData
)

@Serializable
data class EndDeviceIds(val device_id: String, val application_ids: ApplicationIds)

@Serializable
data class ApplicationIds(val application_id: String)

@Serializable
data class UplinkMessageData(
    val decoded_payload: DecodedPayload
)

@Serializable
data class DataModel(
    val bpm: Int,
    val temperature: Int,
    val vitesse: Int,
    val time: Long
)

object SessionManager {
    private var _userLogin: String = ""
    val userLogin: String
        get() = _userLogin

    fun setUser(login: String) {
        _userLogin = login
    }
}