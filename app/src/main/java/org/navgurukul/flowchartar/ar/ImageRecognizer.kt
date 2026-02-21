package org.navgurukul.flowchartar.ar

import android.graphics.Bitmap
import android.graphics.Color
import org.navgurukul.flowchartar.model.ShapeType

data class DetectedShape(
    val type: ShapeType,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

class ImageRecognizer {
    
    fun detectShapes(bitmap: Bitmap): List<DetectedShape> {
        val shapes = mutableListOf<DetectedShape>()
        val width = bitmap.width
        val height = bitmap.height
        
        // Simple color-based detection
        // In production, you'd use ML Kit or TensorFlow Lite
        val colorRegions = findColorRegions(bitmap)
        
        colorRegions.forEach { region ->
            val shapeType = classifyShape(region)
            if (shapeType != null) {
                shapes.add(
                    DetectedShape(
                        type = shapeType,
                        x = region.centerX / width.toFloat(),
                        y = region.centerY / height.toFloat(),
                        width = region.width / width.toFloat(),
                        height = region.height / height.toFloat()
                    )
                )
            }
        }
        
        return shapes
    }
    
    private fun findColorRegions(bitmap: Bitmap): List<ColorRegion> {
        val regions = mutableListOf<ColorRegion>()
        val visited = Array(bitmap.height) { BooleanArray(bitmap.width) }
        
        for (y in 0 until bitmap.height step 10) {
            for (x in 0 until bitmap.width step 10) {
                if (!visited[y][x]) {
                    val pixel = bitmap.getPixel(x, y)
                    if (isColoredPixel(pixel)) {
                        val region = floodFill(bitmap, x, y, visited)
                        if (region.pixelCount > 100) {
                            regions.add(region)
                        }
                    }
                }
            }
        }
        
        return regions
    }
    
    private fun isColoredPixel(pixel: Int): Boolean {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        
        // Check if it's not grayscale (has color)
        val maxDiff = maxOf(
            kotlin.math.abs(r - g),
            kotlin.math.abs(g - b),
            kotlin.math.abs(b - r)
        )
        
        return maxDiff > 30 && (r > 100 || g > 100 || b > 100)
    }
    
    private fun floodFill(
        bitmap: Bitmap,
        startX: Int,
        startY: Int,
        visited: Array<BooleanArray>
    ): ColorRegion {
        var minX = startX
        var maxX = startX
        var minY = startY
        var maxY = startY
        var pixelCount = 0
        
        val queue = mutableListOf(Pair(startX, startY))
        val targetColor = bitmap.getPixel(startX, startY)
        
        while (queue.isNotEmpty() && pixelCount < 10000) {
            val (x, y) = queue.removeAt(0)
            
            if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) continue
            if (visited[y][x]) continue
            
            val currentColor = bitmap.getPixel(x, y)
            if (!isSimilarColor(targetColor, currentColor)) continue
            
            visited[y][x] = true
            pixelCount++
            
            minX = minOf(minX, x)
            maxX = maxOf(maxX, x)
            minY = minOf(minY, y)
            maxY = maxOf(maxY, y)
            
            queue.add(Pair(x + 1, y))
            queue.add(Pair(x - 1, y))
            queue.add(Pair(x, y + 1))
            queue.add(Pair(x, y - 1))
        }
        
        return ColorRegion(
            minX, minY, maxX - minX, maxY - minY,
            (minX + maxX) / 2, (minY + maxY) / 2,
            targetColor, pixelCount
        )
    }
    
    private fun isSimilarColor(color1: Int, color2: Int, threshold: Int = 50): Boolean {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        
        return kotlin.math.abs(r1 - r2) < threshold &&
               kotlin.math.abs(g1 - g2) < threshold &&
               kotlin.math.abs(b1 - b2) < threshold
    }
    
    private fun classifyShape(region: ColorRegion): ShapeType? {
        val aspectRatio = region.width.toFloat() / region.height.toFloat()
        val dominantColor = region.color
        
        // Classify based on color
        val hue = getHue(dominantColor)
        
        return when {
            hue in 100f..140f -> ShapeType.START_END  // Green
            hue in 200f..240f -> ShapeType.PROCESS    // Blue
            hue in 40f..60f -> ShapeType.DECISION     // Yellow
            hue in 280f..320f -> ShapeType.INPUT_OUTPUT // Purple
            hue in 0f..20f || hue in 340f..360f -> ShapeType.CONNECTOR // Red/Orange
            else -> null
        }
    }
    
    private fun getHue(color: Int): Float {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        
        if (delta == 0f) return 0f
        
        val hue = when (max) {
            r -> 60f * (((g - b) / delta) % 6f)
            g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }
        
        return if (hue < 0) hue + 360f else hue
    }
    
    data class ColorRegion(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val centerX: Int,
        val centerY: Int,
        val color: Int,
        val pixelCount: Int
    )
}
