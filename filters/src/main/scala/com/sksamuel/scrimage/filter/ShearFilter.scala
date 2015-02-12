package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.{ Image, Filter, AbstractImageFilter }

import ShearFilter._

object ShearFilter {

  private class SpecificShearFilter(xAngle: Double, yAngle: Double, width: Int, height: Int)
      extends TransformFilter {

    private[this] val xOff = -height * math.tan(xAngle).toFloat
    private[this] val yOff = -width * math.tan(yAngle).toFloat
    private[this] val shx = math.sin(xAngle).toFloat
    private[this] val shy = math.sin(yAngle).toFloat

    def transformInverse(x: Int, y: Int) =
      (x + xOff + (y * shx), y + yOff + (x * shy))

    override def defaultDst(src: Image) = {
      assert(src.width == width)
      assert(src.height == height)
      val tanX = math.tan(xAngle).toFloat
      val w = (height * math.abs(tanX) + width + 0.9999999f).toInt

      val tanY = math.tan(yAngle).toFloat
      val h = (width * math.abs(tanY) + height + 0.9999999f).toInt
      Image.empty(w, h)
    }
  }
}

case class ShearFilter(xAngle: Double, yAngle: Double) extends Filter {

  def apply(src: Image) = {
    val f = new SpecificShearFilter(xAngle, yAngle, src.width, src.height)
    f(src)
  }
}

