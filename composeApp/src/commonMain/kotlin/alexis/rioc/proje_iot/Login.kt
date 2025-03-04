package alexis.rioc.proje_iot

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import io.ktor.http.*
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.launch
import io.ktor.serialization.kotlinx.json.*
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {
    AppTheme {
        var isAuthenticated by remember { mutableStateOf(false) }
        var login by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }

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
                    onLoginChange = { login = it },
                    onPasswordChange = { password = it },
                    onAuthenticate = {
                        scope.launch {
                            val result = authenticateUser(login, password)
                            if (result == "success") {
                                isAuthenticated = true
                                errorMessage = ""
                            } else {
                                errorMessage = result
                            }
                        }
                    }
                )
            } else {
                AuthenticatedScreen()
            }
        }
    }
}

@Composable
fun AuthScreen(
    login: String,
    password: String,
    errorMessage: String,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAuthenticate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Authentification", style = MaterialTheme.typography.h5)
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

        Button(onClick = onAuthenticate, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        ) {
            Text(text = "Se connecter")
        }
    }
}


// Fonction pour authentifier l'utilisateur
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
        }
        else {
            "Identifiants incorrects"
        }
    } catch (e: Exception) {
        "Serveur éteint"
    }
}

