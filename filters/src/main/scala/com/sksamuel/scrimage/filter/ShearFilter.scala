package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.filter.util.StaticImageFilter

/** @author Stephen Samuel */
class ShearFilter(xAngle: Double, yAngle: Double) extends StaticImageFilter {
  val op = new thirdparty.jhlabs.image.ShearFilter()
  op.setXAngle(xAngle.toFloat)
  op.setYAngle(yAngle.toFloat)
  op.setResize(false)
}
object ShearFilter {
  def apply(xAngle: Double, yAngle: Double) = new ShearFilter(xAngle, yAngle)
}
