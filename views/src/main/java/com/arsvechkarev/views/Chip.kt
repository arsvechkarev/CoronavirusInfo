package com.arsvechkarev.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import core.FontManager
import core.extenstions.block
import core.extenstions.dp
import core.extenstions.f
import core.extenstions.sp

class Chip @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : View(context, attrs) {
  
  private val colorFill: Int
  private val colorSecondary: Int
  private val textLayout: Layout
  private var cornerRadius: Float
  
  private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
  private val rect = RectF()
  private val rectPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
  
  var isActive = false
    set(value) {
      field = value
      invalidate()
    }
  
  init {
    val attributes = context.obtainStyledAttributes(attrs, R.styleable.Chip, 0, 0)
    colorFill = attributes.getColor(R.styleable.Chip_android_fillColor, Color.WHITE)
    colorSecondary = attributes.getColor(R.styleable.Chip_colorSecondary, Color.BLACK)
    textPaint.textSize = attributes.getDimension(R.styleable.Chip_android_textSize, 16.sp)
    textPaint.typeface = FontManager.rubik
    rectPaint.strokeWidth = attributes.getDimension(R.styleable.Chip_android_strokeWidth, 2.dp)
    textLayout = boringLayoutOf(textPaint, attributes.getText(R.styleable.Chip_android_text) ?: "")
    cornerRadius = attributes.getDimension(R.styleable.Chip_chipCornerRadius, 10.dp)
    attributes.recycle()
    rectPaint.color = colorFill
    textPaint.color = colorFill
  }
  
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val width = textLayout.width + paddingStart + paddingEnd + rectPaint.strokeWidth * 2
    val height = textLayout.height + paddingTop + paddingBottom + rectPaint.strokeWidth * 2
    setMeasuredDimension(
      resolveSize(width.toInt(), widthMeasureSpec),
      resolveSize(height.toInt(), heightMeasureSpec)
    )
  }
  
  override fun onDraw(canvas: Canvas) {
    val strokeOffset: Float
    if (isActive) {
      strokeOffset = rectPaint.strokeWidth / 2f
      rectPaint.style = Paint.Style.FILL
      textPaint.color = colorSecondary
    } else {
      strokeOffset = rectPaint.strokeWidth
      rectPaint.style = Paint.Style.STROKE
      textPaint.color = colorFill
    }
    rect.set(strokeOffset, strokeOffset, width - strokeOffset, height - strokeOffset)
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, rectPaint)
    canvas.block {
      val strokeOffset = rectPaint.strokeWidth / 2
      translate(paddingStart.f + strokeOffset, paddingTop.f + strokeOffset)
      textLayout.draw(canvas)
    }
  }
}