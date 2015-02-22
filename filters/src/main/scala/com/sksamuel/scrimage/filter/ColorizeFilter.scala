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

import com.sksamuel.scrimage.{ Image, Filter, Color, RGBColor }
import com.sksamuel.scrimage.filter.util._

class ColorizeFilter(color: RGBColor) extends PixelMapperFilter with ExhaustiveFilter {

  private[this] val weight0 = color.alpha / 255f
  private[this] val weight1 = 1 - weight0

  def apply(c: RGBColor): Color = {
    val r = weight1 * c.red + weight0 * color.red + 0.5f
    val g = weight1 * c.green + weight0 * color.green + 0.5f
    val b = weight1 * c.blue + weight0 * color.blue + 0.5f
    val a = Math.max(color.alpha, c.alpha)

    Color(r.toInt, g.toInt, b.toInt, a)
  }
}

object ColorizeFilter {
  def apply(color: Color) = new ColorizeFilter(color)
  def apply(r: Int, g: Int, b: Int, a: Int = 255) = new ColorizeFilter(Color(r, g, b, a))
}
