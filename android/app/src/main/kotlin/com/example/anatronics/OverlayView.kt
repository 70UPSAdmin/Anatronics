package com.example.anatronics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private var faces: List<Face> = emptyList()
    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        strokeWidth = 8f
    }

    fun setFaces(faces: List<Face>) {
        this.faces = faces
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (face in faces) {
            val landmarks = listOf(
                face.getLandmark(FaceLandmark.LEFT_EYE),
                face.getLandmark(FaceLandmark.RIGHT_EYE),
                face.getLandmark(FaceLandmark.NOSE_BASE),
                face.getLandmark(FaceLandmark.MOUTH_LEFT),
                face.getLandmark(FaceLandmark.MOUTH_RIGHT)
            )
            for (landmark in landmarks) {
                landmark?.position?.let { point ->
                    canvas.drawCircle(point.x, point.y, 8f, paint)
                }
            }
        }
    }
}