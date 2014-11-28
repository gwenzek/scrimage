package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.filter.util.StaticImageFilter

/** @author Stephen Samuel */
class QuantizeFilter(colors: Int, dither: Boolean) extends StaticImageFilter {
  val op = new thirdparty.jhlabs.image.QuantizeFilter
  op.setNumColors(colors)
  op.setDither(dither)
}
object QuantizeFilter {
  def apply(colors: Int = 256, dither: Boolean = false): QuantizeFilter = new QuantizeFilter(colors, dither)
}

