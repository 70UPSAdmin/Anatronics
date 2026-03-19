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

    private var imageWidth = 0
    private var imageHeight = 0

    fun setFaces(faces: List<Face>, imgWidth: Int, imgHeight: Int) {
        this.faces = faces
        this.imageWidth = imgWidth
        this.imageHeight = imgHeight
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (imageWidth == 0 || imageHeight == 0) return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        // Scale to fill the view (center crop)
        val scale = maxOf(viewWidth / imageWidth, viewHeight / imageHeight)

        val scaledWidth = imageWidth * scale
        val scaledHeight = imageHeight * scale

        // Offsets for center-crop
        val offsetX = (scaledWidth - viewWidth) / 2f
        val offsetY = (scaledHeight - viewHeight) / 2f

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

                    // scale coordinates
                    var x = point.x * scale
                    var y = point.y * scale

                    // subtract crop offsets
                    x -= offsetX
                    y -= offsetY

                    // mirror X for front camera (after offsets)
                    val mirroredX = viewWidth - x

                    canvas.drawCircle(mirroredX, y, 10f, paint)
                }
            }
        }
    }
}