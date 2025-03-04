package alexis.rioc.proje_iot

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

enum class MenuScreen {
    Home, Data, CyclingProgram
}

@Composable
fun AuthenticatedScreen() {
    var currentScreen by remember { mutableStateOf(MenuScreen.Home) }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") }, label = { Text("Accueil") }, selected = currentScreen == MenuScreen.Home, onClick = { currentScreen = MenuScreen.Home })
                BottomNavigationItem(icon = { Icon(Icons.Default.Info, contentDescription = "Données") }, label = { Text("Données") }, selected = currentScreen == MenuScreen.Data, onClick = { currentScreen = MenuScreen.Data })
                BottomNavigationItem(icon = { Icon(Icons.Default.Favorite, contentDescription = "Vélo") }, label = { Text("Vélo") }, selected = currentScreen == MenuScreen.CyclingProgram, onClick = { currentScreen = MenuScreen.CyclingProgram })
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            when (currentScreen) {
                MenuScreen.Home -> HomeScreen()
                MenuScreen.Data -> DataScreen()
                MenuScreen.CyclingProgram -> CyclingScreen()
            }
        }
    }
}
