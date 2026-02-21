package org.navgurukul.flowchartar.ar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import org.navgurukul.flowchartar.model.ShapeType

class Shape3DRenderer {
    
    fun drawShape(
        canvas: Canvas,
        shapeType: ShapeType,
        x: Float,
        y: Float,
        size: Float
    ) {
        val paint = Paint().apply {
            color = android.graphics.Color.argb(
                200,
                (shapeType.color.red * 255).toInt(),
                (shapeType.color.green * 255).toInt(),
                (shapeType.color.blue * 255).toInt()
            )
            style = Paint.Style.FILL
            strokeWidth = 4f
        }
        
        val strokePaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        
        when (shapeType) {
            ShapeType.START_END -> drawOval(canvas, x, y, size, paint, strokePaint)
            ShapeType.PROCESS -> drawRectangle(canvas, x, y, size, paint, strokePaint)
            ShapeType.DECISION -> drawDiamond(canvas, x, y, size, paint, strokePaint)
            ShapeType.INPUT_OUTPUT -> drawParallelogram(canvas, x, y, size, paint, strokePaint)
            ShapeType.CONNECTOR -> drawCircle(canvas, x, y, size, paint, strokePaint)
        }
    }
    
    private fun drawOval(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        paint: Paint,
        strokePaint: Paint
    ) {
        val left = x - size
        val top = y - size * 0.6f
        val right = x + size
        val bottom = y + size * 0.6f
        
        canvas.drawOval(left, top, right, bottom, paint)
        canvas.drawOval(left, top, right, bottom, strokePaint)
    }
    
    private fun drawRectangle(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        paint: Paint,
        strokePaint: Paint
    ) {
        val left = x - size
        val top = y - size * 0.7f
        val right = x + size
        val bottom = y + size * 0.7f
        
        canvas.drawRect(left, top, right, bottom, paint)
        canvas.drawRect(left, top, right, bottom, strokePaint)
    }
    
    private fun drawDiamond(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        paint: Paint,
        strokePaint: Paint
    ) {
        val path = Path().apply {
            moveTo(x, y - size)
            lineTo(x + size, y)
            lineTo(x, y + size)
            lineTo(x - size, y)
            close()
        }
        
        canvas.drawPath(path, paint)
        canvas.drawPath(path, strokePaint)
    }
    
    private fun drawParallelogram(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        paint: Paint,
        strokePaint: Paint
    ) {
        val offset = size * 0.3f
        val path = Path().apply {
            moveTo(x - size + offset, y - size * 0.6f)
            lineTo(x + size + offset, y - size * 0.6f)
            lineTo(x + size - offset, y + size * 0.6f)
            lineTo(x - size - offset, y + size * 0.6f)
            close()
        }
        
        canvas.drawPath(path, paint)
        canvas.drawPath(path, strokePaint)
    }
    
    private fun drawCircle(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        paint: Paint,
        strokePaint: Paint
    ) {
        canvas.drawCircle(x, y, size * 0.5f, paint)
        canvas.drawCircle(x, y, size * 0.5f, strokePaint)
    }
}
