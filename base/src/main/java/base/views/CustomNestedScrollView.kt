package base.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

class CustomNestedScrollView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {
  
  override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
    return if (isEnabled) super.onInterceptTouchEvent(ev) else false
  }
  
  override fun onTouchEvent(ev: MotionEvent?): Boolean {
    return if (isEnabled) super.onTouchEvent(ev) else false
  }
}