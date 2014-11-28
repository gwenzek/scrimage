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

import com.sksamuel.scrimage.{ Filter, Raster }
import com.sksamuel.scrimage.filter.util.{ IndependentBlockByBlock, CopyingFilter }

/** @author Stephen Samuel */
class PixelateFilter(val blockWidth: Int, val blockHeight: Int) extends Filter with IndependentBlockByBlock with CopyingFilter {

  def treatBlock(x: Int, y: Int, c: Int, src: Raster, dst: Raster): Unit = {
    var s = 0
    val xMax = math.min(x + blockWidth, src.width)
    val yMax = math.min(y + blockHeight, src.height)
    for (xi <- x until xMax; yi <- y until yMax) {
      s += src.readChannel(xi, yi, c)
    }
    s /= (xMax - x) * (yMax - y)
    for (xi <- x until xMax; yi <- y until yMax) {
      dst.writeChannel(xi, yi, c, s)
    }
  }

}

object PixelateFilter {
  def apply(): PixelateFilter = apply(2)
  def apply(blockSize: Int): PixelateFilter = new PixelateFilter(blockSize, blockSize)
  def apply(blockWidth: Int, blockHeight: Int): PixelateFilter =
    new PixelateFilter(blockWidth, blockHeight)
}
