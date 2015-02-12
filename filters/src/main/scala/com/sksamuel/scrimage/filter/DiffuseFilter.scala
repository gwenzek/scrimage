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

import com.sksamuel.scrimage.filter.TransformFilter._
import com.sksamuel.scrimage.filter.util.TwoPi

object DiffuseFilter {
  def apply(): DiffuseFilter = apply(4)
}

case class DiffuseFilter(scale: Float) extends TransformFilter {

  val sinTable = Array.ofDim[Float](256)
  val cosTable = Array.ofDim[Float](256)
  for (i <- 0 until 256) {
    val angle = TwoPi * i / 256f
    sinTable(i) = (scale * math.sin(angle)).toFloat
    cosTable(i) = (scale * math.cos(angle)).toFloat
  }

  override val edgeAction = Clamp

  def transformInverse(x: Int, y: Int) = {
    val angle = (math.random * 255).toInt
    val distance = math.random.toFloat
    (x + distance * sinTable(angle), y + distance * cosTable(angle))
  }
}
