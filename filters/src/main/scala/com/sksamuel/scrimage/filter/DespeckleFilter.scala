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

import com.sksamuel.scrimage.{ Image, Raster, AbstractImageFilter }
import com.sksamuel.scrimage.filter.util._

object DespeckleFilter extends AbstractImageFilter with CopyingFilter {

  final private[this] def pepperAndSalt(c0: Int, v1: Int, v2: Int) = {
    var c = c0
    if (c < v1) c += 1
    if (c < v2) c += 1
    if (c > v1) c -= 1
    if (c > v2) c -= 1
    c
  }

  def filter(srcImg: Image, dstImg: Image) = {
    val src = srcImg.raster
    val dst = dstImg.raster
    val width = src.width
    val height = src.height

    (0 to src.n_real_channel).par.foreach { c =>
      var r0 = Array.ofDim[Int](width)
      var r1 = Array.ofDim[Int](width)
      var r2 = Array.ofDim[Int](width)

      for (x <- 0 until width) r1(x) = src.readChannel(x, 0, c)

      for (y <- 0 until height) {
        val yIn = y > 0 && y < height - 1

        if (y < height - 1)
          for (x <- 0 until width) r2(x) = src.readChannel(x, y + 1, c)

        for (x <- 0 until width) {
          val xIn = x > 0 && x < width - 1
          var or = r1(x)
          if (yIn) or = pepperAndSalt(or, r0(x), r2(x))
          if (xIn) or = pepperAndSalt(or, r1(x - 1), r1(x + 1))
          if (xIn && yIn) {
            or = pepperAndSalt(or, r0(x - 1), r2(x + 1))
            or = pepperAndSalt(or, r2(x - 1), r0(x + 1))
          }

          dst.writeChannel(x, y, c, or)
        }

        val t = r0
        r0 = r1
        r1 = r2
        r2 = t
      }
    }
    dstImg
  }
}
