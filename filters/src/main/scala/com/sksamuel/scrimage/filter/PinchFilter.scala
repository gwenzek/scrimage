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

import com.sksamuel.scrimage.{ Image, Filter }
import com.sksamuel.scrimage.filter.TransformFilter._
import com.sksamuel.scrimage.filter.util._
import PinchFilter._

object PinchFilter {

  def apply(amount: Double = 0.5f,
            radius: Int = 100,
            angle: Float = 0,
            centerX: Float = 0.5f,
            centerY: Float = 0.5f): PinchFilter =
    new PinchFilter(amount.toFloat, radius, angle, centerX, centerY)
}

class PinchFilter(
    amount: Float = 0.5f,
    radius: Float = 100,
    angle: Float = 0,
    centerX: Float = 0.5f,
    centerY: Float = 0.5f,
    width: Int = 0,
    height: Int = 0) extends ContextualizedFilter with TransformFilter {

  private[this] val iCenterX = width * centerX
  private[this] val iCenterY = height * centerY
  private[this] val iRadius = if (radius == 0) math.min(iCenterX, iCenterY) else radius
  private[this] val iRadius2 = iRadius * iRadius

  override val edgeAction = Clamp

  def transformInverse(x: Int, y: Int) = {
    val dx = x - iCenterX
    val dy = y - iCenterY
    val distance2 = dx * dx + dy * dy

    if (distance2 > iRadius2) (x, y)
    else {
      val d = math.sqrt(distance2 / iRadius2).toFloat
      val t = math.pow(math.sin(math.Pi * 0.5 * d), -amount).toFloat

      val e = 1 - d
      val a = angle * e * e
      val s = math.sin(a).toFloat * t
      val c = math.cos(a).toFloat * t

      (iCenterX + c * dx - s * dy, iCenterY + s * dx + c * dy)
    }
  }

  def prepare(src: Image) =
    new PinchFilter(amount, radius, angle, centerX, centerY, src.width, src.height)

}
