package org.navgurukul.flowchartar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.navgurukul.flowchartar.model.FlowchartShape
import org.navgurukul.flowchartar.ui.screens.ARViewScreen
import org.navgurukul.flowchartar.ui.screens.ShapeListScreen
import org.navgurukul.flowchartar.ui.theme.FlowchartARTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowchartARTheme {
                FlowchartARApp()
            }
        }
    }
}

@Composable
fun FlowchartARApp() {
    val navController = rememberNavController()
    var selectedShape by remember { mutableStateOf<FlowchartShape?>(null) }
    
    NavHost(navController = navController, startDestination = "shape_list") {
        composable("shape_list") {
            ShapeListScreen(
                onShapeSelected = { shape ->
                    selectedShape = shape
                    navController.navigate("ar_view")
                }
            )
        }
        
        composable("ar_view") {
            selectedShape?.let { shape ->
                ARViewScreen(
                    shape = shape,
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }
    }
}
