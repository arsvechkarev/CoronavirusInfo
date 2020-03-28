package core.extenstions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable

fun Drawable.toBitmap(): Bitmap {
  val bitmap = Bitmap.createBitmap(
    intrinsicWidth,
    intrinsicHeight,
    Bitmap.Config.ARGB_8888
  )
  val canvas = Canvas(bitmap)
  setBounds(0, 0, canvas.width, canvas.height)
  draw(canvas)
  return bitmap
}

inline fun Canvas.block(action: Canvas.() -> Unit) {
  save()
  action(this)
  restore()
}