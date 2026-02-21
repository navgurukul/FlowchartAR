package org.navgurukul.flowchartar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.navgurukul.flowchartar.model.FlowchartShape
import org.navgurukul.flowchartar.model.ShapeType
import org.navgurukul.flowchartar.ui.components.ShapeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeListScreen(
    onShapeSelected: (FlowchartShape) -> Unit
) {
    val shapes = listOf(
        FlowchartShape(ShapeType.START_END, "Marks the beginning or end of a process"),
        FlowchartShape(ShapeType.PROCESS, "Represents a process or action step"),
        FlowchartShape(ShapeType.DECISION, "Shows a decision point with yes/no paths"),
        FlowchartShape(ShapeType.INPUT_OUTPUT, "Indicates input or output operation"),
        FlowchartShape(ShapeType.CONNECTOR, "Connects different parts of the flowchart")
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flowchart Shapes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            items(shapes) { shape ->
                ShapeCard(
                    shape = shape,
                    onClick = { onShapeSelected(shape) }
                )
            }
        }
    }
}
