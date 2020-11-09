package com.arsvechkarev.views.statsviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.arsvechkarev.viewdsl.size
import com.arsvechkarev.views.R
import core.NumberFormatter
import core.extenstions.getTextHeight
import core.viewbuilding.Colors
import core.viewbuilding.Fonts

class CountryGeneralStatsView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
  
  private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    typeface = Fonts.SegoeUiBold
    textAlign = Paint.Align.CENTER
  }
  private val itemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Colors.OverlayDark
  }
  
  private var titleTextSize = -1f
  private var titleTextHeight = -1f
  private var numberTextSize = -1f
  
  private val confirmedTitle = context.getString(R.string.text_confirmed)
  private val recoveredTitle = context.getString(R.string.text_recovered)
  private val deathsTitle = context.getString(R.string.text_deaths)
  private var confirmedText: String? = null
  private var recoveredText: String? = null
  private var deathsText: String? = null
  
  fun updateData(confirmed: Int, recovered: Int, deaths: Int) {
    confirmedText = NumberFormatter.formatNumber(confirmed)
    recoveredText = NumberFormatter.formatNumber(recovered)
    deathsText = NumberFormatter.formatNumber(deaths)
    requestLayout()
  }
  
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val width = widthMeasureSpec.size
    titleTextSize = getTextSizeFor(
      confirmedTitle, recoveredTitle, deathsTitle, width, textPaint
    )
    textPaint.textSize = titleTextSize
    titleTextHeight = textPaint.getTextHeight(confirmedTitle).toFloat()
    setMeasuredDimension(
      resolveSize(width, widthMeasureSpec),
      resolveSize(getItemHeight(titleTextSize).toInt(), heightMeasureSpec)
    )
  }
  
  override fun onDraw(canvas: Canvas) {
    confirmedText ?: return
    numberTextSize = getTextSizeFor(
      confirmedText!!, recoveredText!!, deathsText!!, width, textPaint
    )
    val offset = getItemMargin(width).toFloat()
    val itemSize = getSquareSize(width).toFloat()
    canvas.drawItem(Colors.Confirmed, confirmedTitle, confirmedText!!, 0f)
    canvas.drawItem(Colors.Recovered, recoveredTitle, recoveredText!!,
      itemSize + offset)
    canvas.drawItem(Colors.Deaths, deathsTitle, deathsText!!,
      itemSize * 2 + offset * 2)
  }
  
  private fun Canvas.drawItem(
    color: Int,
    title: String,
    number: String,
    start: Float
  ) {
    val itemWidth = getSquareSize(width).toFloat()
    val itemHeight = getItemHeight(titleTextSize)
    val radius = getItemCornersRadius(width)
    drawRoundRect(start, 0f, start + itemWidth, itemHeight,
      radius, radius, itemPaint)
    val verticalMargin = getVerticalMargin(titleTextSize)
    textPaint.textSize = titleTextSize
    textPaint.color = Colors.TextPrimary
    drawText(title, 0, title.length, start + itemWidth / 2f,
      titleTextHeight / 2 + verticalMargin, textPaint)
    textPaint.textSize = numberTextSize
    textPaint.color = color
    drawText(number, 0, number.length, start + itemWidth / 2f,
      height - verticalMargin, textPaint)
  }
  
  companion object {
    
    private fun getItemMargin(width: Int) = width / 25
    
    private fun getItemHeight(textSize: Float) = textSize * 5
    
    private fun getVerticalMargin(textSize: Float) = textSize * 0.9f
  }
}