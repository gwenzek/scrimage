package thirdparty

import com.sksamuel.scrimage._
import com.sksamuel.scrimage.filter.util.{ BlockByBlock, CopyingFilter, GrayPixelByPixelFilter, GrayPixelMapper }

/** Marvin Project <2007-2009>
  *
  * Initial version by:
  *
  * Danilo Rosetto Munoz
  * Fabio Andrijauskas
  * Gabriel Ambrosio Archanjo
  *
  * site: http://marvinproject.sourceforge.net
  *
  * Ported to scala by gwenzek
  *
  * GPL
  * Copyright (C) <2007>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */

package object marvin_scala {
  def Sepia(depth: Int): Filter = Filter(sepiaMap(depth)(_))

  def sepiaMap(depth: Int)(color: Color): Color = {
    val rgb = color.toRGB
    val avg = (rgb.red + rgb.green + rgb.blue) / 3
    Color(truncate(avg + depth * 2), truncate(avg + depth), avg, rgb.alpha)
  }

  @inline
  def truncate(a: Int): Int = {
    if (a < 0) 0
    else if (a > 255) 255
    else a
  }

  /** Invert the pixels color to create an emboss effect.
    *
    * @author Chris Mack
    * @version 1.0 12/07/2011
    */
  object EmbossFilter extends GrayPixelByPixelFilter {
    def toGray(x: Int, y: Int, src: Raster): Int = {
      var c, diff, maxDiff = 0
      if (y > 0 && x > 0) {
        while (c < src.n_real_channel) {
          diff = src.readChannel(x, y, c) - src.readChannel(x - 1, y - 1, c)
          if (math.abs(diff) > math.abs(maxDiff)) maxDiff = diff
          c += 1
        }
      }
      truncate(128 + maxDiff)
    }
  }

  object MarvinGrayScaleFilter extends GrayPixelMapper {
    def toGray(rgb: RGBColor): Int = ((rgb.red * 0.3) + (rgb.green * 0.59) + (rgb.blue * 0.11)).toInt
  }

  object TelevisionFilter extends CopyingFilter with BlockByBlock {
    val blockWidth = 1
    val blockHeight = 3

    def treatBlock(x: Int, y: Int, src: Raster, dst: Raster): Unit = {
      var r, g, b, w: Int = 0
      while (w < 3) {
        if (y + w < src.height) {
          r += src.readChannel(x, y + w, 0) / 2
          g += src.readChannel(x, y + w, 1) / 2
          b += src.readChannel(x, y + w, 2) / 2
        }
        w += 1
      }
      w = 0
      while (w < 3) {
        if (y + w < dst.height) {
          if (w == 0) {
            dst.write(x, y + w, Color(truncate(r), 0, 0))
          } else if (w == 1) {
            dst.write(x, y + w, Color(0, truncate(g), 0))
          } else if (w == 2) {
            dst.write(x, y + w, Color(0, 0, truncate(b)))
          }
        }
        w += 1
      }
    }
  }

  def Prewitt = new XYConvolution(
    Array(Array(1.0, 0, -1), Array(1.0, 0, -1), Array(1.0, 0, -1)),
    Array(Array(1.0, 1, 1), Array(0.0, 0, 0), Array(-1.0, -1, -1))
  )

  def Roberts = new XYConvolution(
    Array(Array(1.0, 0), Array(0.0, -1)),
    Array(Array(0.0, 1), Array(-1.0, 0))
  )

  def Sobel = new XYConvolution(
    Array(Array(1.0, 0, -1), Array(2.0, 0, -2), Array(1.0, 0, -1)),
    Array(Array(-1.0, -2, -1), Array(0.0, 0, 0), Array(1.0, 2, 1))
  )

}
