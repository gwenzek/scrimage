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
import com.sksamuel.scrimage.{ Raster, Color, RGBColor }
import com.sksamuel.scrimage.filter.util._
import PageCurlFilter._

object PageCurlFilter {

  def apply(angle: Double = 0, transition: Double = 0, radius: Float = 100): PageCurlFilter =
    new PageCurlFilter(angle.toFloat, transition.toFloat)

}

class PageCurlFilter(
    angle: Float = 0.5f,
    transition: Float = 10f,
    radius: Float = 1,
    width: Int = 0,
    height: Int = 0) extends PixelByPixelFilter with ExhaustiveFilter with ContextualizedFilter {

  def prepare(src: Image) =
    new PageCurlFilter(angle, transition, radius, src.width, src.height)

  def apply(x: Int, y: Int, src: Raster): Color = {
    val p = transformInverse(x, y)
    val x0 = math.floor(p(0)).toInt
    val y0 = math.floor(p(1)).toInt
    val xWeight = p(0) - x0
    val yWeight = p(1) - y0
    val shade = p(2)
    val oncurl = p(3) == 1

    val nw = read(src, x0, y0)
    val ne = read(src, x0 + 1, y0)
    val sw = read(src, x0, y0 + 1)
    val se = read(src, x0 + 1, y0 + 1)
    val rgb = scale(shade, bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se))

    if (oncurl) blend(rgb, src.read(x, y))
    else rgb
  }

  private[this] def read(src: Raster, x: Int, y: Int) = {
    if (x < 0 || x >= src.width || y < 0 || y >= src.height)
      Color(0, 0, 0, 0)
    else
      src.read(x, y)
  }

  def blend(c1: RGBColor, c2: RGBColor) = {
    if (c1.alpha != 0xff) {
      val a1 = c1.alpha
      val a2 = (255 - c1.alpha) * c2.alpha / 255
      Color(
        clamp((c1.red * a1 + c2.red * a2) / 255),
        clamp((c1.green * a1 + c2.green * a2) / 255),
        clamp((c1.blue * a1 + c2.blue * a2) / 255),
        clamp(a1 + a2)
      )
    } else c1
  }

  protected def transformInverse(x: Int, y: Int) = {
    val out = Array.ofDim[Float](4)
    var px = x.toFloat
    var py = y.toFloat
    val s = Math.sin(angle).toFloat
    val c = Math.cos(angle).toFloat
    var tx = transition * Math.sqrt(width * width + height * height).toFloat

    // Start from the correct corner according to the angle
    val xoffset = if (c < 0) width else 0
    val yoffset = if (s < 0) height else 0

    // Transform into unrotated coordinates
    px -= xoffset
    py -= yoffset

    var qx = px * c + py * s
    val qy = -px * s + py * c
    val outside = qx < tx
    val unfolded = qx > tx * 2
    val oncurl = !(outside || unfolded)
    qx = if (qx > tx * 2) qx else 2 * tx - qx

    // Transform back into rotated coordinates
    px = qx * c - qy * s
    py = qx * s + qy * c
    px += xoffset
    py += yoffset

    // See if we're off the edge of the page
    val offpage = px < 0 || py < 0 || px >= width || py >= height
    if (offpage && oncurl) {
      px = x
      py = y
    }

    // Shade the curl
    val shade = if (!offpage && oncurl)
      1.9f * (1.0f - math.cos(math.exp((qx - tx) / radius)).toFloat)
    else 0

    out(2) = 1 - shade

    if (outside) {
      out(0) = -1
      out(1) = -1
    } else {
      out(0) = px
      out(1) = py
    }
    out(3) = if (!offpage && oncurl) 1 else 0
    out
  }
}
