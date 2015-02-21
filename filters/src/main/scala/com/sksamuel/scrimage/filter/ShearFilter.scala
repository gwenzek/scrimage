package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.{ Image, Filter, AbstractImageFilter }
import com.sksamuel.scrimage.filter.util._

import ShearFilter._

object ShearFilter {

  def apply(xAngle: Double, yAngle: Double) =
    new ShearFilter(xAngle, yAngle)
}

class ShearFilter(
    xAngle: Double,
    yAngle: Double,
    width: Int = 0,
    height: Int = 0) extends TransformFilter with ContextualizedFilter with ResizeFilter {

  private[this] val xOff = -height * math.tan(xAngle).toFloat
  private[this] val yOff = -width * math.tan(yAngle).toFloat
  private[this] val shx = math.sin(xAngle).toFloat
  private[this] val shy = math.sin(yAngle).toFloat

  def transformInverse(x: Int, y: Int) =
    (x + xOff + (y * shx), y + yOff + (x * shy))

  def resize(width: Int, height: Int) = {
    val tanX = math.tan(xAngle).toFloat
    val w = (height * math.abs(tanX) + width + 0.9999999f).toInt

    val tanY = math.tan(yAngle).toFloat
    val h = (width * math.abs(tanY) + height + 0.9999999f).toInt
    (w, h)
  }

  def prepare(src: Image) =
    new ShearFilter(xAngle, yAngle, src.width, src.height)

  override def defaultDst(src: Image) = super[ResizeFilter].defaultDst(src)
}

