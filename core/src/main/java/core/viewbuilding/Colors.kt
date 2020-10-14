package core.viewbuilding

import android.content.Context
import com.arsvechkarev.core.R
import core.extenstions.getAttrColor

object Colors {
  
  private val colors = IntArray(28) { 0 }
  
  val Background get() = colors[0]
  val BackgroundLight get() = colors[1]
  val Overlay get() = colors[2]
  val OverlayShine get() = colors[3]
  val OverlayShineLight get() = colors[4]
  val OverlayAccent get() = colors[5]
  val Dialog get() = colors[6]
  val GradientHeaderStart get() = colors[7]
  val GradientHeaderEnd get() = colors[8]
  val Ripple get() = colors[9]
  val IconDefault get() = colors[10]
  val TextPrimary get() = colors[11]
  val TextSecondary get() = colors[12]
  
  val Confirmed get() = colors[13]
  val Recovered get() = colors[14]
  val Deaths get() = colors[15]
  val PercentByCountry get() = colors[16]
  val DeathRate get() = colors[17]
  
  val MapWater get() = colors[18]
  val MapLandscape get() = colors[19]
  val MapCircleDefault get() = colors[20]
  val MapCircleStrokeDefault get() = colors[21]
  val MapCircleSelected get() = colors[22]
  val MapCircleStrokeSelected get() = colors[23]
  
  val Failure get() = colors[24]
  val FailureRipple get() = colors[25]
  val Divider get() = colors[26]
  val DividerDark get() = colors[27]
  
  fun init(context: Context) {
    colors[0] = context.getAttrColor(R.attr.colorBackground)
    colors[1] = context.getAttrColor(R.attr.colorBackgroundLight)
    colors[2] = context.getAttrColor(R.attr.colorOverlay)
    colors[3] = context.getAttrColor(R.attr.colorOverlayShine)
    colors[4] = context.getAttrColor(R.attr.colorOverlayShineLight)
    colors[5] = context.getAttrColor(R.attr.colorOverlayAccent)
    colors[6] = context.getAttrColor(R.attr.colorDialog)
    colors[7] = context.getAttrColor(R.attr.colorGradientHeaderStart)
    colors[8] = context.getAttrColor(R.attr.colorGradientHeaderEnd)
    colors[9] = context.getAttrColor(R.attr.colorRipple)
    colors[10] = context.getAttrColor(R.attr.colorIconDefault)
    colors[11] = context.getAttrColor(R.attr.colorTextPrimary)
    colors[12] = context.getAttrColor(R.attr.colorTextSecondary)
    colors[13] = context.getAttrColor(R.attr.colorConfirmed)
    colors[14] = context.getAttrColor(R.attr.colorRecovered)
    colors[15] = context.getAttrColor(R.attr.colorDeaths)
    colors[16] = context.getAttrColor(R.attr.colorPercentByCountry)
    colors[17] = context.getAttrColor(R.attr.colorDeathRate)
    colors[18] = context.getAttrColor(R.attr.colorMapWater)
    colors[19] = context.getAttrColor(R.attr.colorMapLandscape)
    colors[20] = context.getAttrColor(R.attr.colorMapCircleDefault)
    colors[21] = context.getAttrColor(R.attr.colorMapCircleStrokeDefault)
    colors[22] = context.getAttrColor(R.attr.colorMapCircleSelected)
    colors[23] = context.getAttrColor(R.attr.colorMapCircleStrokeSelected)
    colors[24] = context.getAttrColor(R.attr.colorFailure)
    colors[25] = context.getAttrColor(R.attr.colorFailureRipple)
    colors[26] = context.getAttrColor(R.attr.colorDivider)
    colors[27] = context.getAttrColor(R.attr.colorDividerDark)
  }
}