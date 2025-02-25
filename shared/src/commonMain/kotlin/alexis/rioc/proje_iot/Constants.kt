package alexis.rioc.proje_iot

import kotlinx.serialization.Serializable

const val IP_MACHINE = "10.114.44.180"
const val SERVER_PORT = 8080

@Serializable
data class UserCredentials(val login: String, val password: String)