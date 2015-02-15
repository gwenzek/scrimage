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
import WaterFilter._

object WaterFilter {

  def apply(radius: Int = 50,
            waveLength: Float = 16f,
            amplitude: Float = 10f,
            phase: Float = 0f,
            centerX: Float = 0.5f,
            centerY: Float = 0.5f): WaterFilter =
    new WaterFilter(radius, waveLength, amplitude, phase, centerX, centerY)

  private class SpeWaterFilter(
      radius: Int,
      waveLength: Float,
      amplitude: Float,
      phase: Float,
      centerX: Float,
      centerY: Float,
      width: Int, height: Int) extends TransformFilter {

    private[this] val iCenterX = width * centerX
    private[this] val iCenterY = height * centerY
    private[this] val radius2 = radius * radius

    override val edgeAction = Clamp

    def transformInverse(x: Int, y: Int) = {
      val dx = x - iCenterX
      val dy = y - iCenterY
      val distance2 = dx * dx + dy * dy

      if (distance2 > radius2) (x, y)
      else {
        val d = math.sqrt(distance2).toFloat
        var a = amplitude * math.sin(d / waveLength * TwoPi - phase).toFloat
        a *= (radius - d) / radius
        if (d != 0) a *= waveLength / d
        (x + dx * a, y + dy * a)
      }
    }
  }
}

class WaterFilter(
    radius: Int = 50,
    waveLength: Float = 16f,
    amplitude: Float = 10f,
    phase: Float = 0f,
    centerX: Float = 0.5f,
    centerY: Float = 0.5f) extends Filter {

  def apply(src: Image) = {
    new SpeWaterFilter(radius, waveLength, amplitude, phase, centerX, centerY, src.width, src.height)(src)
  }
}
