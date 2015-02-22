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

import com.sksamuel.scrimage.filter.util._
import com.sksamuel.scrimage.{ Color, RGBColor }

class DissolveFilter(density: Float = 1, softness: Float = 0) extends PixelMapperFilter {

  private[this] val d = (1 - density) * (1 + softness)
  private[this] val minDensity = d - softness
  private[this] val maxDensity = d
  private[this] val randomNumbers = new java.util.Random(0)

  def apply(rgb: RGBColor): Color = {
    val v = randomNumbers.nextFloat()
    val f = smoothStep(minDensity, maxDensity, v)
    rgb.copy(alpha = (rgb.alpha * f).toInt)
  }
}

object DissolveFilter {
  def apply(density: Double) = new DissolveFilter(density.toFloat)
  def apply(density: Double, softness: Double) =
    new DissolveFilter(density.toFloat, softness.toFloat)
}
