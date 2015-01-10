/*
   Copyright 2013 Stephen K Samuel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.filter.util.StaticImageFilter
import com.sksamuel.scrimage.geom.Point2D

// /** @author Stephen Samuel */
// class MotionBlurFilter(angle: Double, distance: Double, rotation: Double, zoom: Double) extends StaticImageFilter {
//   val op = new thirdparty.jhlabs.image.MotionBlurFilter()
//   op.setAngle(angle.toFloat)
//   op.setDistance(distance.toFloat)
//   op.setRotation(rotation.toFloat)
//   op.setZoom(zoom.toFloat)
// }

import com.sksamuel.scrimage.{ Image, Raster }
import com.sksamuel.scrimage.filter.util._

class MotionBlurFilter(angle: Double, distance: Double, rotation: Double, zoom: Double)
    extends IndependentPixelByPixel with CopyingFilter {

  val treat_alpha = true

  private[this] val translateX = (distance * math.cos(angle)).toFloat
  private[this] val translateY = (distance * -math.sin(angle)).toFloat

  def apply(x: Int, y: Int, c: Int, src: Raster): Int = {
    val cx = src.width / 2
    val cy = src.height / 2
    val imageRadius = math.sqrt(cx * cx + cy * cy)
    val repetitions = (distance + math.abs(rotation * imageRadius) + zoom * imageRadius).toInt
    var i = 0
    var count = 0
    var acc = 0
    while (i < repetitions) {
      val f = i.toFloat / repetitions
      var p = Point2D(x, y).toDouble.
        +(cx + f * translateX, cy + f * translateY).
        *(1 - zoom * f).
        -(cx, cy).
        toInt
      if (p in (0, 0, src.width, src.height)) {
        acc += (src.readChannel(p.x, p.y, c) * src.readAlpha(p.x, p.y) / 255f).toInt
        count += 1
      } else
        i = repetitions
      i += 1
    }
    if (count == 0) src.readChannel(x, y, c)
    else acc / count
  }
}

object MotionBlurFilter {
  def apply(angle: Double, distance: Double, rotation: Double = 0, zoom: Double = 0) =
    new MotionBlurFilter(angle, distance, rotation, zoom)
}
