package core.extenstions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun View.visible() {
  visibility = View.VISIBLE
}

fun View.invisible() {
  visibility = View.INVISIBLE
}

fun View.gone() {
  visibility = View.GONE
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View {
  return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun View.dp(value: Int) = resources.displayMetrics.density * value

fun View.sp(value: Int) = resources.displayMetrics.scaledDensity * value

val Int.f get() = toFloat()

val Float.i get() = toInt()