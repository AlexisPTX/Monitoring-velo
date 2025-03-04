package alexis.rioc.proje_iot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*

@Composable
fun CyclingScreen() {
    var selectedOption by remember { mutableStateOf("Distance") }
    var inputValue by remember { mutableStateOf(TextFieldValue("")) }
    var message by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("km") }
    var expanded by remember { mutableStateOf(false) }

    val unitsDistance = listOf("km", "m")
    val unitsTime = listOf("h", "min")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Choix du programme", style = MaterialTheme.typography.h5)

        // Sélecteur Distance ou Durée
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(
                onClick = {
                    selectedOption = "Distance"
                    selectedUnit = "km"
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (selectedOption == "Distance") MaterialTheme.colors.primary else Color.Gray
                )
            ) {
                Text("Distance")
            }

            Button(
                onClick = {
                    selectedOption = "Durée"
                    selectedUnit = "h"
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (selectedOption == "Durée") MaterialTheme.colors.primary else Color.Gray
                )
            ) {
                Text("Durée")
            }
        }

        // Champ de saisie avec menu déroulant
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                textStyle = MaterialTheme.typography.body1.copy(fontSize = 18.sp),
                modifier = Modifier
                    .weight(1f)
                    .background(Color.LightGray)
                    .padding(10.dp)
            )

            Box {
                Text(
                    text = selectedUnit,
                    modifier = Modifier
                        .background(MaterialTheme.colors.primary)
                        .padding(10.dp)
                        .clickable { expanded = true },
                    color = Color.White
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val units = if (selectedOption == "Distance") unitsDistance else unitsTime
                    units.forEach { unit ->
                        DropdownMenuItem(onClick = {
                            selectedUnit = unit
                            expanded = false
                        }) {
                            Text(text = unit)
                        }
                    }
                }
            }
        }

        // Bouton Envoyer
        Button(
            onClick = {
                message = "Envoyé : $selectedOption = ${inputValue.text} $selectedUnit"
                sendDataToServer(selectedOption, inputValue.text, selectedUnit)
            },
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Text("Envoyer")
        }

        // Message de confirmation
        if (message.isNotEmpty()) {
            Text(message, color = ColorDarkBlue)
        }
    }
}

fun sendDataToServer(option: String, value: String, unit: String) {
    //Envoie choix utilisateur vers serveur Ktor
}
