package org.navgurukul.flowchartar.model

import androidx.compose.ui.graphics.Color

enum class ShapeType(val displayName: String, val color: Color) {
    START_END("Start/End", Color(0xFF4CAF50)),
    PROCESS("Process", Color(0xFF2196F3)),
    DECISION("Decision", Color(0xFFFFC107)),
    INPUT_OUTPUT("Input/Output", Color(0xFF9C27B0)),
    CONNECTOR("Connector", Color(0xFFFF5722))
}
