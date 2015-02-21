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
import com.sksamuel.scrimage.filter.util.ContextuallizedFilter
import TwirlFilter._

object TwirlFilter {
  def apply(radius: Int): TwirlFilter = apply(Math.PI / 1.5, radius)
  def apply(angle: Double, radius: Int, centerX: Float = 0.5f, centerY: Float = 0.5f): TwirlFilter =
    new TwirlFilter(angle, radius, centerX, centerY)
}

class TwirlFilter(
    angle: Double,
    radius: Float,
    centerX: Float,
    centerY: Float,
    width: Int = 0,
    height: Int = 0) extends TransformFilter with ContextuallizedFilter {

  private[this] val iCenterX = width * centerX
  private[this] val iCenterY = height * centerY
  private[this] val radius2 = radius * radius

  override val edgeAction = Clamp

  def transformInverse(x: Int, y: Int) = {
    val dx = x - iCenterX
    val dy = y - iCenterY
    val d2 = dx * dx + dy * dy

    if (d2 > radius2) (x, y)
    else {
      val d = math.sqrt(d2).toFloat
      val a = math.atan2(dy, dx) + angle * (radius - d) / radius
      (iCenterX + d * math.cos(a).toFloat, iCenterY + d * math.sin(a).toFloat)
    }
  }

  def prepare(src: Image) =
    new TwirlFilter(angle, radius, centerX, centerY, src.width, src.height)
}
