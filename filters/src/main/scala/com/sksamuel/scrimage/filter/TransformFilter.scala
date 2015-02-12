package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.{ Raster, Color }
import com.sksamuel.scrimage.filter.util._

import TransformFilter._

object TransformFilter {
  val NearestNeighbor = 0
  val Bilinear = 1

  val Zero = 0
  val Clamp = 1
  val Wrap = 2
  val RgbClamp = 3
}

trait TransformFilter extends PixelByPixelFilter with ExhaustiveFilter {

  def interpolation: Int = Bilinear
  def edgeAction: Int = Clamp

  def transformInverse(x: Int, y: Int): (Float, Float)

  def apply(x: Int, y: Int, src: Raster): Color = {
    if (interpolation == NearestNeighbor) {
      val (x0, y0) = transformInverse(x, y)
      read(src, x0.toInt, y0.toInt)
    } else {
      val p = transformInverse(x, y)
      val x0 = math.floor(p._1).toInt
      val y0 = math.floor(p._2).toInt
      val xWeight = p._1 - x0
      val yWeight = p._2 - y0
      val nw = read(src, x0, y0)
      val ne = read(src, x0 + 1, y0)
      val sw = read(src, x0, y0 + 1)
      val se = read(src, x0 + 1, y0 + 1)
      bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se)
    }
  }

  private[this] def read(src: Raster, x: Int, y: Int) = {
    if (x < 0 || x >= src.width || y < 0 || y >= src.height) {
      edgeAction match {
        case Wrap => src.read(mod(x, src.width), mod(y, src.height))
        case Clamp => src.read(clamp(x, src.width), clamp(y, src.height))
        case RgbClamp => src.read(clamp(x, src.width), clamp(y, src.height)).copy(alpha = 0)
        case _ => Color(0, 0, 0, 0)
      }
    } else
      src.read(x, y)
  }
}
