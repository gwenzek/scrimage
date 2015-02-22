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

import com.sksamuel.scrimage.{ Color, Raster }
import com.sksamuel.scrimage.filter.util._
import com.sksamuel.scrimage.filter.CellularFilter._

object CrystallizeFilter {
  def apply(
    scale: Double = 16,
    edgeThickness: Double = 0.4,
    edgeColor: Color = Color.Black,
    fadeEdges: Boolean = false,
    randomness: Double = 0.2): CrystallizeFilter =
    new CrystallizeFilter(scale.toFloat, edgeThickness.toFloat, edgeColor, fadeEdges, randomness.toFloat)
}

class CrystallizeFilter(
  scale: Float = 16f,
  edgeThickness: Float = 0.4f,
  edgeColor: Color = Color.Black,
  fadeEdges: Boolean = false,
  randomness: Float = 0f)
    extends CellularFilter(scale = scale, randomness = randomness) {

  override def apply(x: Int, y: Int, src: Raster): Color = {
    val nx = (cos * x + sin * y) / scale + 1000
    val ny = (-sin * x + cos * y) / (scale * stretch) + 1000

    val results = Array.fill(3) { new Point }
    findPoints(nx, ny, results)
    val f1 = results(0).distance
    val f2 = results(1).distance

    val v = readPointColor(results(0), src)
    val f = smoothStep(0, edgeThickness, (f2 - f1) / edgeThickness)
    if (fadeEdges) {
      val v2 = mixColors(0.5f, readPointColor(results(1), src), v)
      mixColors(f, v2, v)
    } else
      mixColors(f, edgeColor, v)
  }

  override def toString(): String = "Pixellate/Crystallize..."
}
