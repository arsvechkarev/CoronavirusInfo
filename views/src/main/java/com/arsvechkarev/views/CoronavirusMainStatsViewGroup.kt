package com.arsvechkarev.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import core.extenstions.assertThat

class CoronavirusMainStatsViewGroup @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {
  
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    assertThat(childCount == 3)
    val width = MeasureSpec.getSize(widthMeasureSpec)
    val offset = getChildMargin(width)
    val childSize = (width - offset * 2) / 3
    val measureSpec = MeasureSpec.makeMeasureSpec(childSize, MeasureSpec.EXACTLY)
    for (i in 0 until childCount) {
      getChildAt(i).measure(measureSpec, measureSpec)
    }
    setMeasuredDimension(
      resolveSize(width, widthMeasureSpec),
      resolveSize(childSize, heightMeasureSpec)
    )
  }
  
  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    assertThat(childCount == 3)
    val offset = getChildMargin(width)
    val childSize = (width - offset * 2) / 3
    var left = 0
    for (i in 0 until childCount) {
      getChildAt(i).layout(left, 0, left + childSize, childSize)
      left += childSize + offset
    }
  }
  
  private fun getChildMargin(width: Int): Int {
    return width / 18
  }
}