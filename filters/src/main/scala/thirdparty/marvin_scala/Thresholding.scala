/**
Marvin Project <2007-2009>

 Initial version by:

 Danilo Rosetto Munoz
 Fabio Andrijauskas
 Gabriel Ambrosio Archanjo

 site: http://marvinproject.sourceforge.net

 GPL
 Copyright (C) <2007>

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
package thirdparty.marvin_scala

import com.sksamuel.scrimage._

/**
 * Thresholding
 *
 * @author Gabriel Ambrosio Archanjo
 */

object Thresholding {
  def apply(threshold: Int = 125, neighborhood: Int = 0, range: Int = 1) =
    new Thresholding(threshold, neighborhood, range)

  def onGray(threshold: Int = 125) = new Thresholding(threshold, 0, 0)

}

class Thresholding(threshold: Int, neighborhood: Int, range: Int) extends Filter {

  def apply(srcImage: Image) = {
    val grayImage = MarvinGrayScaleFilter(srcImage)

    if (neighborhood > 0)
      ContrastThreshold(neighborhood, range)(grayImage)
    else
      HardThreshold(threshold).in_place(grayImage)
  }
}

case class HardThreshold(threshold: Int) extends GrayPixelByPixelFilter with InPlaceFilter {
  def toGray(x: Int, y: Int, src: Raster) =
    if (src.readChannel(x, y, 0) < threshold) 0 else 255
}

case class ContrastThreshold(neighborhood: Int, range: Int = 1) extends GrayPixelByPixelFilter {
  def toGray(x: Int, y: Int, src: Raster) =
    if (checkNeighbors(x, y, neighborhood, neighborhood, src)) 0 else 255

  private def checkNeighbors(x: Int, y: Int, neighborhoodX: Int, neighborhoodY: Int, src: Raster): Boolean = {
    var z, i, j = 0
    val color = src.readChannel(x, y, 0)
    i = x - neighborhoodX
    while (i <= x + neighborhoodX) {
      j = y - neighborhoodY
      while (j <= y + neighborhoodY) {
        if (i != x || j != y) {
          if (i >= 0 && i < src.width && j >= 0 && j < src.height) {
            if (color < src.readChannel(i, j, 0) - range) z += 1
          }
        }
        j += 1
      }
      i += 1
    }
    z > (neighborhoodX * neighborhoodY) * 0.5
  }
}


class Dithering(val blockWidth: Int, val blockHeight: Int, val thresholds: Array[Int])
  extends GrayPixelByPixelFilter with GrayOutput {

  require(thresholds.length == blockHeight * blockWidth)

  def toGray(x: Int, y: Int, src: Raster): Int = {
    if (MarvinGrayScaleFilter.toGray(src.read(x, y)) <
      thresholds((y % blockHeight) * blockWidth + (x % blockWidth)))
      0
    else
      255
  }
}

object Dithering {
  def apply(): Dithering = Dithering(3, 3, Array(
    88, 55, 25,
    39, 74, 161,
    183, 62, 13)
  )

  def apply(blockWidth: Int, blockHeight: Int, thresholds: Array[Int]): Dithering =
    new Dithering(blockWidth, blockHeight, thresholds)

  def apply(blockWidth: Int, thresholds: Array[Int]): Dithering =
    new Dithering(blockWidth, blockWidth, thresholds)
}

object Rylanders {
  def apply(): Rylanders = Rylanders(4, 4, Array(
    239, 103, 205, 69,
    171, 35, 137, 14,
    188, 52, 222, 86,
    120, 0, 154, 18)
  )

  def apply(blockWidth: Int, blockHeight: Int, thresholds: Array[Int]): Rylanders =
    new Rylanders(blockWidth, blockHeight, thresholds)

  def apply(blockWidth: Int, thresholds: Array[Int]): Rylanders =
    new Rylanders(blockWidth, blockWidth, thresholds)
}

class Rylanders(val blockWidth: Int, val blockHeight: Int, val thresholds: Array[Int]) extends BlockByBlock with GrayOutput {

  require(thresholds.length == blockHeight * blockWidth)

  def treatBlock(x: Int, y: Int, src: Raster, dst: Raster): Unit = {
    var i, j = 0
    val l = level(x, y, src)
    while (j < blockHeight) {
      i = 0
      while (i < blockWidth) {
        if (x + i < src.width && y + j < src.height) {
          writeGray(x + i, y + j, if (l <= thresholds(j * blockWidth + i)) 0 else 255, dst)
        }
        i += 1
      }
      j += 1
    }
  }

  def level(x: Int, y: Int, src: Raster): Int = {
    var total, i, j = 0
    while (j < blockHeight) {
      i = 0
      while (i < blockWidth) {
        if (x + i < src.width && y + j < src.height) {
          total += MarvinGrayScaleFilter.toGray(src.read(x + i, y + j))
        }
        i += 1
      }
      j += 1
    }
    total / (math.min(blockWidth, src.width - x) * math.min(blockHeight, src.height - y))
//    total / (blockWidth * blockHeight)
  }
}

