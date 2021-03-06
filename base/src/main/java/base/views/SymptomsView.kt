package base.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.view.View
import base.R
import base.extensions.TEMP_RECT
import base.extensions.TEMP_RECT_F
import base.extensions.f
import base.extensions.getTextHeight
import base.resources.Colors
import base.resources.Fonts
import base.resources.TextSizes

class SymptomsView(context: Context) : View(context) {
  
  private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Colors.TextPrimary
    typeface = Fonts.SegoeUiBold
    textSize = TextSizes.H4
    textAlign = Paint.Align.CENTER
  }
  
  private val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Colors.Overlay
  }
  
  private val coughText = context.getString(R.string.text_cough)
  private val headacheText = context.getString(R.string.text_headache)
  private val feverText = context.getString(R.string.text_fever)
  
  private val coughDrawable = context.getDrawable(R.drawable.image_cough)!!
  private val headacheDrawable = context.getDrawable(R.drawable.image_headache)!!
  private val feverDrawable = context.getDrawable(R.drawable.image_fever)!!
  
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val widthSize = MeasureSpec.getSize(widthMeasureSpec)
    val width = widthSize - paddingStart - paddingEnd
    val itemSize = getItemSize(width)
    val height = itemSize + getItemMargin(width) +
        textPaint.getTextHeight(coughText) + paddingTop + paddingBottom
    setMeasuredDimension(widthMeasureSpec, resolveSize(height, heightMeasureSpec))
  }
  
  override fun onDraw(canvas: Canvas) {
    val width = width - paddingStart - paddingEnd
    val itemSize = getItemSize(width)
    val margin = getItemMargin(width)
    drawItem(canvas, paddingStart, coughText, coughDrawable)
    drawItem(canvas, paddingStart + itemSize + margin, headacheText, headacheDrawable)
    drawItem(canvas, paddingStart + itemSize * 2 + margin * 2, feverText, feverDrawable)
  }
  
  private fun drawItem(canvas: Canvas, start: Int, text: String, drawable: Drawable) {
    val width = width - paddingStart - paddingEnd
    val itemSize = getItemSize(width)
    val radius = getSquareCornersRadius(width)
    TEMP_RECT_F.set(start.f, paddingTop.f,
      start.f + itemSize,
      paddingTop + itemSize.f
    )
    canvas.drawRoundRect(TEMP_RECT_F, radius, radius, rectPaint)
    TEMP_RECT_F.round(TEMP_RECT)
    drawable.bounds = TEMP_RECT
    drawable.draw(canvas)
    val textX = start + itemSize / 2f
    val textY = height - textPaint.getTextHeight(text) / 2f
    canvas.drawText(text, textX, textY, textPaint)
  }
  
  companion object {
    
    fun getItemSize(width: Int): Int {
      return (width - getItemMargin(width) * 2) / 3
    }
    
    fun getSquareCornersRadius(width: Int): Float {
      return width / 35f
    }
    
    fun getItemMargin(width: Int): Int {
      return width / 18
    }
  }
}