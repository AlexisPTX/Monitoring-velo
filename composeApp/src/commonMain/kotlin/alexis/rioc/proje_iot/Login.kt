package alexis.rioc.proje_iot

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        var isAuthenticated by remember { mutableStateOf(false) }
        var login by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }
        var isRegistering by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isAuthenticated) {
                AuthScreen(
                    login = login,
                    password = password,
                    errorMessage = errorMessage,
                    isRegistering = isRegistering,
                    onLoginChange = { login = it },
                    onPasswordChange = { password = it },
                    onToggleMode = { isRegistering = !isRegistering },
                    onAuthenticate = {
                        scope.launch {
                            val result = if (isRegistering) {
                                registerUser(login, password)
                            } else {
                                authenticateUser(login, password)
                            }
                            if (result == "success" || result == "Compte créé") {
                                isAuthenticated = true
                                errorMessage = ""
                                SessionManager.setUser(login)
                            } else {
                                errorMessage = result
                            }
                        }
                    }
                )
            } else {
                MainScreen(onLogout = {
                    isAuthenticated = false
                    login = ""
                    password = ""
                    errorMessage = ""
                    SessionManager.setUser("")
                })
            }
        }
    }
}

@Composable
fun AuthScreen(
    login: String,
    password: String,
    errorMessage: String,
    isRegistering: Boolean,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onAuthenticate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegistering) "Inscription" else "Authentification",
            style = MaterialTheme.typography.h5
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = login,
            onValueChange = onLoginChange,
            label = { Text("Login") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colors.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onAuthenticate,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        ) {
            Text(text = if (isRegistering) "Créer un compte" else "Se connecter")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onToggleMode) {
            Text(if (isRegistering) "Déjà un compte ? Se connecter" else "Créer un compte")
        }
    }
}

suspend fun authenticateUser(login: String, password: String): String {
    val client = HttpClient() {
        install(ContentNegotiation) {
            json()
        }
    }

    return try {
        val response: HttpResponse = client.post("http://$IP_MACHINE:8080/authenticate") {
            contentType(ContentType.Application.Json)
            setBody(UserCredentials(login, password))
        }

        if (response.status == HttpStatusCode.OK) {
            "success"
        } else {
            "Identifiants incorrects"
        }
    } catch (e: Exception) {
        "Serveur éteint"
    }
}

suspend fun registerUser(login: String, password: String): String {
    val client = HttpClient() {
        install(ContentNegotiation) {
            json()
        }
    }

    return try {
        val response: HttpResponse = client.post("http://$IP_MACHINE:8080/register") {
            contentType(ContentType.Application.Json)
            setBody(UserCredentials(login, password))
        }

        when (response.status) {
            HttpStatusCode.Created -> "Compte créé"
            HttpStatusCode.Conflict -> "Login déjà utilisé"
            else -> "Erreur inconnue"
        }
    } catch (e: Exception) {
        "Serveur éteint"
    }
}
