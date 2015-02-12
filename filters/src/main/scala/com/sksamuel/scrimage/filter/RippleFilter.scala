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

import com.sksamuel.scrimage.filter.RippleType._
import com.sksamuel.scrimage.filter.util._

object RippleType {
  def Sine(x: Float) = math.sin(x).toFloat
  def Sawtooth(x: Float) = mod(x, 1)
  def Triangle(x: Float) = com.sksamuel.scrimage.filter.util.triangle(x)
  def Noise(x: Float) = thirdparty.jhlabs.math.Noise.noise1(x)
}

object RippleFilter {
  def apply(rippleType: (Float => Float)) = new RippleFilter(rippleType, 2f, 2f, 6f, 6f)
}

case class RippleFilter(ripple: (Float => Float),
                        xAmplitude: Float, yAmplitude: Float,
                        xWavelength: Float, yWavelength: Float) extends TransformFilter {

  def transformInverse(x: Int, y: Int) = {
    val fx: Float = ripple(y.toFloat / xWavelength)
    val fy: Float = ripple(x.toFloat / yWavelength)

    (x + xAmplitude * fx, y + yAmplitude * fy)
  }
}
