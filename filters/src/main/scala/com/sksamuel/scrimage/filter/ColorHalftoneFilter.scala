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

import com.sksamuel.scrimage.Raster
import com.sksamuel.scrimage.filter.util._
import ColorHalftoneFilter._

/** @author Stephen Samuel */

object ColorHalftoneFilter {
  val cyan = math.toRadians(108).toFloat
  val magenta = math.toRadians(162).toFloat
  val yellow = math.toRadians(90).toFloat
  val angles = Array(cyan, magenta, yellow)
  val sin = angles.map(x => math.sin(x).toFloat)
  val cos = angles.map(x => math.cos(x).toFloat)

  val mx = Array(0, -1, 1, 0, 0)
  val my = Array(0, 0, 0, -1, 1)

  def apply(radius: Double = 1.2): ColorHalftoneFilter =
    new ColorHalftoneFilter(radius)
}

class ColorHalftoneFilter(radius: Double)
    extends IndependentPixelByPixel with CopyingFilter {

  val treat_alpha = false
  val gridSize: Float = 2 * radius.toFloat * 1.414f
  val halfGridSize: Float = gridSize / 2

  def apply(x: Int, y: Int, c: Int, src: Raster): Int = {
    val width = src.width
    val height = src.height
    var tx = x * cos(c) + y * sin(c)
    var ty = -x * sin(c) + y * cos(c)
    tx = tx - mod(tx - halfGridSize, gridSize) + halfGridSize
    ty = ty - mod(ty - halfGridSize, gridSize) + halfGridSize
    var f = 1f
    for (i <- 0 until 5) {
      val ttx = tx + mx(i) * gridSize
      val tty = ty + my(i) * gridSize
      val ntx = ttx * cos(c) - tty * sin(c)
      val nty = ttx * sin(c) + tty * cos(c)
      val nx = clamp(ntx.toInt, width)
      val ny = clamp(nty.toInt, height)
      var l = src.readChannel(nx, ny, c) / 255.0f
      l = 1 - l * l
      l *= (halfGridSize * 1.414).toFloat
      val dx = x - ntx
      val dy = y - nty
      val dx2 = dx * dx
      val dy2 = dy * dy
      val R = math.sqrt(dx2 + dy2).toFloat
      val f2 = 1 - smoothStep(R, R + 1, l)
      f = math.min(f, f2)
    }
    (255 * f).toInt
  }
}
