package org.navgurukul.flowchartar.ar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import org.navgurukul.flowchartar.model.ShapeType

class Simple3DShapeRenderer {
    
    fun drawShape3D(
        canvas: Canvas,
        shapeType: ShapeType,
        x: Float,
        y: Float,
        size: Float
    ) {
        when (shapeType) {
            ShapeType.START_END -> drawOval3D(canvas, x, y, size, shapeType)
            ShapeType.PROCESS -> drawCube3D(canvas, x, y, size, shapeType)
            ShapeType.DECISION -> drawDiamond3D(canvas, x, y, size, shapeType)
            ShapeType.INPUT_OUTPUT -> drawParallelogram3D(canvas, x, y, size, shapeType)
            ShapeType.CONNECTOR -> drawSphere3D(canvas, x, y, size, shapeType)
        }
    }
    
    private fun drawOval3D(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        shapeType: ShapeType
    ) {
        val baseColor = android.graphics.Color.argb(
            220,
            (shapeType.color.red * 255).toInt(),
            (shapeType.color.green * 255).toInt(),
            (shapeType.color.blue * 255).toInt()
        )
        
        // Shadow
        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(100, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawOval(
            x - size * 1.1f,
            y + size * 0.8f,
            x + size * 1.1f,
            y + size * 1.2f,
            shadowPaint
        )
        
        // 3D effect with gradient
        val gradient = RadialGradient(
            x - size * 0.3f,
            y - size * 0.3f,
            size * 1.5f,
            lightenColor(baseColor),
            darkenColor(baseColor),
            Shader.TileMode.CLAMP
        )
        
        val paint = Paint().apply {
            shader = gradient
            style = Paint.Style.FILL
        }
        
        // Main oval
        canvas.drawOval(
            x - size,
            y - size * 0.6f,
            x + size,
            y + size * 0.6f,
            paint
        )
        
        // Highlight
        val highlightPaint = Paint().apply {
            color = android.graphics.Color.argb(150, 255, 255, 255)
            style = Paint.Style.FILL
        }
        canvas.drawOval(
            x - size * 0.6f,
            y - size * 0.4f,
            x - size * 0.2f,
            y - size * 0.2f,
            highlightPaint
        )
        
        // Outline
        val outlinePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawOval(
            x - size,
            y - size * 0.6f,
            x + size,
            y + size * 0.6f,
            outlinePaint
        )
    }
    
    private fun drawCube3D(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        shapeType: ShapeType
    ) {
        val baseColor = android.graphics.Color.argb(
            220,
            (shapeType.color.red * 255).toInt(),
            (shapeType.color.green * 255).toInt(),
            (shapeType.color.blue * 255).toInt()
        )
        
        // Shadow
        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(100, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val shadowPath = Path().apply {
            moveTo(x - size * 1.1f, y + size * 0.9f)
            lineTo(x + size * 1.1f, y + size * 0.9f)
            lineTo(x + size * 1.3f, y + size * 1.1f)
            lineTo(x - size * 0.9f, y + size * 1.1f)
            close()
        }
        canvas.drawPath(shadowPath, shadowPaint)
        
        val offset = size * 0.3f
        
        // Back face (darker)
        val backPaint = Paint().apply {
            color = darkenColor(baseColor)
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            x - size + offset,
            y - size * 0.7f + offset,
            x + size + offset,
            y + size * 0.7f + offset,
            backPaint
        )
        
        // Top face (lighter)
        val topPaint = Paint().apply {
            color = lightenColor(baseColor)
            style = Paint.Style.FILL
        }
        val topPath = Path().apply {
            moveTo(x - size, y - size * 0.7f)
            lineTo(x + size, y - size * 0.7f)
            lineTo(x + size + offset, y - size * 0.7f + offset)
            lineTo(x - size + offset, y - size * 0.7f + offset)
            close()
        }
        canvas.drawPath(topPath, topPaint)
        
        // Right face (medium)
        val rightPaint = Paint().apply {
            color = baseColor
            style = Paint.Style.FILL
        }
        val rightPath = Path().apply {
            moveTo(x + size, y - size * 0.7f)
            lineTo(x + size + offset, y - size * 0.7f + offset)
            lineTo(x + size + offset, y + size * 0.7f + offset)
            lineTo(x + size, y + size * 0.7f)
            close()
        }
        canvas.drawPath(rightPath, rightPaint)
        
        // Front face (main)
        val frontGradient = RadialGradient(
            x - size * 0.3f,
            y - size * 0.3f,
            size * 1.5f,
            lightenColor(baseColor),
            baseColor,
            Shader.TileMode.CLAMP
        )
        val frontPaint = Paint().apply {
            shader = frontGradient
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            x - size,
            y - size * 0.7f,
            x + size,
            y + size * 0.7f,
            frontPaint
        )
        
        // Highlight
        val highlightPaint = Paint().apply {
            color = android.graphics.Color.argb(120, 255, 255, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            x - size * 0.8f,
            y - size * 0.6f,
            x - size * 0.3f,
            y - size * 0.3f,
            highlightPaint
        )
        
        // Outlines
        val outlinePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        canvas.drawRect(x - size, y - size * 0.7f, x + size, y + size * 0.7f, outlinePaint)
        canvas.drawPath(topPath, outlinePaint)
        canvas.drawPath(rightPath, outlinePaint)
    }
    
    private fun drawDiamond3D(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        shapeType: ShapeType
    ) {
        val baseColor = android.graphics.Color.argb(
            220,
            (shapeType.color.red * 255).toInt(),
            (shapeType.color.green * 255).toInt(),
            (shapeType.color.blue * 255).toInt()
        )
        
        // Shadow
        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(100, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val shadowPath = Path().apply {
            moveTo(x, y - size * 1.1f + size * 0.2f)
            lineTo(x + size * 1.1f, y + size * 0.2f)
            lineTo(x, y + size * 1.1f + size * 0.2f)
            lineTo(x - size * 1.1f, y + size * 0.2f)
            close()
        }
        canvas.drawPath(shadowPath, shadowPaint)
        
        val offset = size * 0.2f
        
        // Back faces (darker)
        val backPaint = Paint().apply {
            color = darkenColor(baseColor)
            style = Paint.Style.FILL
        }
        
        // Back left
        val backLeftPath = Path().apply {
            moveTo(x, y - size)
            lineTo(x - size + offset, y + offset)
            lineTo(x + offset, y + offset)
            close()
        }
        canvas.drawPath(backLeftPath, backPaint)
        
        // Back right
        val backRightPath = Path().apply {
            moveTo(x, y - size)
            lineTo(x + offset, y + offset)
            lineTo(x + size + offset, y + offset)
            close()
        }
        canvas.drawPath(backRightPath, backPaint)
        
        // Front faces with gradient
        val gradient = RadialGradient(
            x - size * 0.3f,
            y - size * 0.3f,
            size * 1.5f,
            lightenColor(baseColor),
            baseColor,
            Shader.TileMode.CLAMP
        )
        val frontPaint = Paint().apply {
            shader = gradient
            style = Paint.Style.FILL
        }
        
        // Front left
        val frontLeftPath = Path().apply {
            moveTo(x, y - size)
            lineTo(x - size, y)
            lineTo(x, y)
            close()
        }
        canvas.drawPath(frontLeftPath, frontPaint)
        
        // Front right
        val frontRightPath = Path().apply {
            moveTo(x, y - size)
            lineTo(x, y)
            lineTo(x + size, y)
            close()
        }
        canvas.drawPath(frontRightPath, frontPaint)
        
        // Bottom left
        val bottomLeftPath = Path().apply {
            moveTo(x, y)
            lineTo(x - size, y)
            lineTo(x, y + size)
            close()
        }
        canvas.drawPath(bottomLeftPath, frontPaint)
        
        // Bottom right
        val bottomRightPath = Path().apply {
            moveTo(x, y)
            lineTo(x + size, y)
            lineTo(x, y + size)
            close()
        }
        canvas.drawPath(bottomRightPath, frontPaint)
        
        // Highlight
        val highlightPaint = Paint().apply {
            color = android.graphics.Color.argb(150, 255, 255, 255)
            style = Paint.Style.FILL
        }
        val highlightPath = Path().apply {
            moveTo(x, y - size * 0.9f)
            lineTo(x - size * 0.3f, y - size * 0.3f)
            lineTo(x, y - size * 0.2f)
            close()
        }
        canvas.drawPath(highlightPath, highlightPaint)
        
        // Outlines
        val outlinePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val outlinePath = Path().apply {
            moveTo(x, y - size)
            lineTo(x + size, y)
            lineTo(x, y + size)
            lineTo(x - size, y)
            close()
        }
        canvas.drawPath(outlinePath, outlinePaint)
    }
    
    private fun drawParallelogram3D(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        shapeType: ShapeType
    ) {
        val baseColor = android.graphics.Color.argb(
            220,
            (shapeType.color.red * 255).toInt(),
            (shapeType.color.green * 255).toInt(),
            (shapeType.color.blue * 255).toInt()
        )
        
        val skew = size * 0.3f
        val offset = size * 0.25f
        
        // Shadow
        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(100, 0, 0, 0)
            style = Paint.Style.FILL
        }
        val shadowPath = Path().apply {
            moveTo(x - size + skew, y + size * 0.8f)
            lineTo(x + size + skew, y + size * 0.8f)
            lineTo(x + size - skew + offset, y + size * 1.0f)
            lineTo(x - size - skew + offset, y + size * 1.0f)
            close()
        }
        canvas.drawPath(shadowPath, shadowPaint)
        
        // Top face
        val topPaint = Paint().apply {
            color = lightenColor(baseColor)
            style = Paint.Style.FILL
        }
        val topPath = Path().apply {
            moveTo(x - size + skew, y - size * 0.6f)
            lineTo(x + size + skew, y - size * 0.6f)
            lineTo(x + size + skew + offset, y - size * 0.6f + offset)
            lineTo(x - size + skew + offset, y - size * 0.6f + offset)
            close()
        }
        canvas.drawPath(topPath, topPaint)
        
        // Right face
        val rightPaint = Paint().apply {
            color = baseColor
            style = Paint.Style.FILL
        }
        val rightPath = Path().apply {
            moveTo(x + size + skew, y - size * 0.6f)
            lineTo(x + size + skew + offset, y - size * 0.6f + offset)
            lineTo(x + size - skew + offset, y + size * 0.6f + offset)
            lineTo(x + size - skew, y + size * 0.6f)
            close()
        }
        canvas.drawPath(rightPath, rightPaint)
        
        // Front face with gradient
        val gradient = RadialGradient(
            x - size * 0.3f,
            y - size * 0.3f,
            size * 1.5f,
            lightenColor(baseColor),
            baseColor,
            Shader.TileMode.CLAMP
        )
        val frontPaint = Paint().apply {
            shader = gradient
            style = Paint.Style.FILL
        }
        val frontPath = Path().apply {
            moveTo(x - size + skew, y - size * 0.6f)
            lineTo(x + size + skew, y - size * 0.6f)
            lineTo(x + size - skew, y + size * 0.6f)
            lineTo(x - size - skew, y + size * 0.6f)
            close()
        }
        canvas.drawPath(frontPath, frontPaint)
        
        // Highlight
        val highlightPaint = Paint().apply {
            color = android.graphics.Color.argb(120, 255, 255, 255)
            style = Paint.Style.FILL
        }
        val highlightPath = Path().apply {
            moveTo(x - size * 0.6f + skew, y - size * 0.5f)
            lineTo(x - size * 0.2f + skew, y - size * 0.5f)
            lineTo(x - size * 0.3f - skew, y - size * 0.2f)
            lineTo(x - size * 0.7f - skew, y - size * 0.2f)
            close()
        }
        canvas.drawPath(highlightPath, highlightPaint)
        
        // Outlines
        val outlinePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        canvas.drawPath(frontPath, outlinePaint)
        canvas.drawPath(topPath, outlinePaint)
        canvas.drawPath(rightPath, outlinePaint)
    }
    
    private fun drawSphere3D(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        shapeType: ShapeType
    ) {
        val baseColor = android.graphics.Color.argb(
            220,
            (shapeType.color.red * 255).toInt(),
            (shapeType.color.green * 255).toInt(),
            (shapeType.color.blue * 255).toInt()
        )
        
        // Shadow
        val shadowPaint = Paint().apply {
            color = android.graphics.Color.argb(100, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawOval(
            x - size * 0.8f,
            y + size * 0.6f,
            x + size * 0.8f,
            y + size * 1.0f,
            shadowPaint
        )
        
        // Sphere with radial gradient for 3D effect
        val gradient = RadialGradient(
            x - size * 0.3f,
            y - size * 0.3f,
            size * 1.2f,
            lightenColor(baseColor),
            darkenColor(baseColor),
            Shader.TileMode.CLAMP
        )
        
        val paint = Paint().apply {
            shader = gradient
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        canvas.drawCircle(x, y, size * 0.7f, paint)
        
        // Highlight for glossy effect
        val highlightPaint = Paint().apply {
            color = android.graphics.Color.argb(180, 255, 255, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(x - size * 0.25f, y - size * 0.25f, size * 0.25f, highlightPaint)
        
        // Outline
        val outlinePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }
        canvas.drawCircle(x, y, size * 0.7f, outlinePaint)
    }
    
    private fun lightenColor(color: Int): Int {
        val r = android.graphics.Color.red(color)
        val g = android.graphics.Color.green(color)
        val b = android.graphics.Color.blue(color)
        val a = android.graphics.Color.alpha(color)
        
        return android.graphics.Color.argb(
            a,
            minOf(255, (r * 1.4f).toInt()),
            minOf(255, (g * 1.4f).toInt()),
            minOf(255, (b * 1.4f).toInt())
        )
    }
    
    private fun darkenColor(color: Int): Int {
        val r = android.graphics.Color.red(color)
        val g = android.graphics.Color.green(color)
        val b = android.graphics.Color.blue(color)
        val a = android.graphics.Color.alpha(color)
        
        return android.graphics.Color.argb(
            a,
            (r * 0.6f).toInt(),
            (g * 0.6f).toInt(),
            (b * 0.6f).toInt()
        )
    }
}
