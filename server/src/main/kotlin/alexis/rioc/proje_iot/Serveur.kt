package alexis.rioc.proje_iot

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import io.ktor.server.plugins.contentnegotiation.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import io.ktor.server.request.receive
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.request.receiveText
import kotlinx.serialization.json.Json


fun main() {
    initDatabase();

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun addTestUser() {
    transaction{
        // Ajouter un utilisateur de test
        Users.insert {
            it[login] = "alexis"
            it[passwordHash] = hashPassword("example")
        }
    }
}

fun initDatabase() {
    // Connecte-toi à la base de données PostgreSQL
    Database.connect(
        "jdbc:postgresql://localhost:5432/projet-iot",
        driver = "org.postgresql.Driver",
        user = "alexispinoteaux",
        password = "Karamel-032"
    )

    transaction {
        SchemaUtils.create(Users, Data)

        //addTestUser()
    }
}

fun hashPassword(password: String): String {
    // Utilisation de BCrypt pour hacher le mot de passe
    return BCrypt.hashpw(password, BCrypt.gensalt())
}

fun checkPassword(enteredPassword: String, storedHash: String): Boolean {
    // Vérification du mot de passe en comparant avec le hachage stocké
    return BCrypt.checkpw(enteredPassword, storedHash)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(io.ktor.server.plugins.cors.routing.CORS) {
        anyHost() // Autorise toutes les origines (en prod, limiter à certaines IP ou domaines)
        allowHeader(HttpHeaders.ContentType) // Autorise les en-têtes Content-Type
    }

    routing {
        post("/authenticate") {
            val userCredentials = call.receive<UserCredentials>()
            val user = transaction {
                Users.select { Users.login eq userCredentials.login }
                    .mapNotNull { it[Users.passwordHash] }
                    .singleOrNull()
            }

            if (user != null && checkPassword(userCredentials.password, user)) {
                call.respond(HttpStatusCode.OK, "Authentification réussie")
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Identifiants incorrects")
            }
        }

        post("/register") {
            val userCredentials = call.receive<UserCredentials>()

            val existingUser = transaction {
                Users.select { Users.login eq userCredentials.login }
                    .singleOrNull()
            }

            if (existingUser != null) {
                call.respond(HttpStatusCode.Conflict, "Login déjà utilisé")
            } else {
                transaction {
                    Users.insert {
                        it[login] = userCredentials.login
                        it[passwordHash] = hashPassword(userCredentials.password)
                    }
                }
                call.respond(HttpStatusCode.Created, "Compte créé avec succès")
            }
        }

        post("/data") {
            val jsonString = call.receiveText()
            processJson(jsonString)
        }

        get("/") {
            // Simuler la récupération des données
            val result = transaction {
                // Interroge la base de données pour récupérer des données
                Users.selectAll().map {
                    it[Users.login]
                }
            }

            call.respond(HttpStatusCode.OK, "Données: ${result.joinToString(", ")}")
        }
    }
}

fun processJson(inputJson: String) {
    val json = Json { ignoreUnknownKeys = true }
    val parsedData = json.decodeFromString<UplinkMessage>(inputJson)

    // Extraire uniquement ce qui t'intéresse
    val bpm = parsedData.uplink_message.decoded_payload.bpm
    val temperature = parsedData.uplink_message.decoded_payload.temperature
    val vitesse = parsedData.uplink_message.decoded_payload.vitesse
    val device = parsedData.end_device_ids.device_id

    val currentTime = System.currentTimeMillis()

    transaction {
        Data.insert {
            it[userLogin] = "alexis"  // Remplacer par le login utilisateur approprié
            it[this.device] = device
            it[this.bpm] = bpm
            it[this.temperature] = temperature
            it[this.vitesse] = vitesse
            it[time] = currentTime
        }
    }
}

// Table Utilisateur
object Users : Table("users") {
    val login = varchar("login", 50)
    val passwordHash = varchar("password_hash", 255)
    override val primaryKey = PrimaryKey(login)
}

// Table Données
object Data : Table("data") {
    private val id = integer("id").autoIncrement()
    val userLogin = varchar("user_login", 50) references Users.login
    val device = varchar("device_id", 255)
    val bpm = integer("bpm")
    val temperature = integer("temperature")
    val vitesse = integer("vitesse")
    val time = long("temps") // Timestamp UNIX
    override val primaryKey = PrimaryKey(id)
}