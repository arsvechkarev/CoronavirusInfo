package com.arsvechkarev.views.drawables

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Region
import android.graphics.Shader
import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import com.arsvechkarev.views.R
import core.extenstions.execute
import core.extenstions.f
import core.extenstions.getDimen
import core.extenstions.retrieveColor

class GradientHeaderDrawable(
  private val startColor: Int,
  private val endColor: Int,
  private val curveSize: Float
) : Drawable() {
  
  private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val gradientPath = Path()
  private var yScaleCoefficient: Float = 0f
  
  override fun onBoundsChange(bounds: Rect) {
    val w = bounds.width()
    val h = bounds.height()
    gradientPath.reset()
    gradientPath.moveTo(0f, 0f)
    gradientPath.lineTo(w.f, 0f)
    gradientPath.lineTo(w.f, h - curveSize)
    gradientPath.quadTo(w / 2f, h.f, 0f, h.f - curveSize)
    gradientPath.close()
    gradientPaint.shader = LinearGradient(
      0f, h.f, w.f, 0f,
      intArrayOf(startColor, endColor), null,
      Shader.TileMode.CLAMP
    )
    Region().apply {
      setPath(gradientPath, Region(bounds))
      yScaleCoefficient = h.f / this.bounds.height()
    }
  }
  
  override fun draw(canvas: Canvas) = canvas.execute {
    scale(1f, yScaleCoefficient, 0f, bounds.height() / 2f)
    drawPath(gradientPath, gradientPaint)
  }
  
  override fun setAlpha(alpha: Int) {
    gradientPaint.alpha = alpha
  }
  
  override fun setColorFilter(colorFilter: ColorFilter?) {
    gradientPaint.colorFilter = colorFilter
  }
  
  override fun getOpacity() = PixelFormat.OPAQUE
  
  companion object {
  
    fun Fragment.createGradientHeaderDrawable(): GradientHeaderDrawable {
      return GradientHeaderDrawable(
        requireContext().retrieveColor(R.color.dark_gradient_header_start),
        requireContext().retrieveColor(R.color.dark_gradient_header_end),
        requireContext().getDimen(R.dimen.gradient_header_curve_size)
      )
    }
  }
}