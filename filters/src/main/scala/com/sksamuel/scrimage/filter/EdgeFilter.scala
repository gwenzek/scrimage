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

import EdgeFilter._
import com.sksamuel.scrimage.{ Raster, Filter }
import com.sksamuel.scrimage.filter.util._

object EdgeFilter {

  private val R2 = math.sqrt(2).toFloat

  private val ROBERTS_V: Array[Float] = Array(0, 0, -1, 0, 1, 0, 0, 0, 0)
  private val ROBERTS_H: Array[Float] = Array(-1, 0, 0, 0, 1, 0, 0, 0, 0)

  private val PREWITT_V: Array[Float] = Array(-1, 0, 1, -1, 0, 1, -1, 0, 1)
  private val PREWITT_H: Array[Float] = Array(-1, -1, -1, 0, 0, 0, 1, 1, 1)

  private val SOBEL_V: Array[Float] = Array(-1, 0, 1, -2, 0, 2, -1, 0, 1)
  private val SOBEL_H: Array[Float] = Array(-1, -2, -1, 0, 0, 0, 1, 2, 1)

  private val FREI_CHEN_V: Array[Float] = Array(-1, 0, 1, -R2, 0, R2, -1, 0, 1)
  private val FREI_CHEN_H: Array[Float] = Array(-1, -R2, -1, 0, 0, 0, 1, R2, 1)

  def roberts = new EdgeFilter(ROBERTS_V, ROBERTS_H)
  def prewitt = new EdgeFilter(PREWITT_V, PREWITT_H)
  def sobel = new EdgeFilter(SOBEL_V, SOBEL_H)
  def freiChen = new EdgeFilter(FREI_CHEN_V, FREI_CHEN_H)

  def apply() = sobel
}

class EdgeFilter(vEdgeMatrix: Array[Float], hEdgeMatrix: Array[Float])
    extends IndependentPixelByPixel {

  def apply(x: Int, y: Int, c: Int, src: Raster) = {
    val width = src.width
    val height = src.height
    var (rh, rv, k) = (0, 0, 0)

    for (
      i <- x - 1 to x + 1;
      j <- y - 1 to y + 1
    ) {
      val r = if (i >= 0 && i < width) {
        if (j >= 0 && j < height) src.readChannel(i, j, c)
        else src.readChannel(i, y, c)
      } else {
        if (j >= 0 && j < height) src.readChannel(x, j, c)
        else src.readChannel(x, y, c)
      }
      rh += (hEdgeMatrix(k) * r).toInt
      rv += (vEdgeMatrix(k) * r).toInt
      k += 1
    }

    clamp((math.sqrt(rh * rh + rv * rv) / 1.8).toInt)
  }
}
