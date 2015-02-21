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
import thirdparty.jhlabs.math.Noise

object SwimFilter {
  def apply(): SwimFilter = apply(6f, 2f)

  def apply(amount: Double, stretch: Double): SwimFilter =
    new SwimFilter(amount.toFloat, stretch.toFloat)
}

class SwimFilter(
    amount: Float = 1,
    stretch: Float = 1,
    scale: Float = 32,
    angle: Float = 0,
    turbulence: Float = 1,
    time: Float = 0) extends TransformFilter {

  private[this] val cos = math.cos(angle).toFloat
  private[this] val sin = math.sin(angle).toFloat

  // override val edgeAction = Clamp

  def transformInverse(x: Int, y: Int) = {
    val nx = (cos * x + sin * y) / scale
    val ny = (cos * y - sin * x) / (scale * stretch)

    if (turbulence == 1f) (
      x + amount * Noise.noise3(nx + 0.5f, ny, time),
      y + amount * Noise.noise3(nx, ny + 0.5f, time)
    )
    else (
      x + amount * Noise.turbulence3(nx + 0.5f, ny, turbulence, time),
      y + amount * Noise.turbulence3(nx, ny + 0.5f, turbulence, time)
    )
  }
}
