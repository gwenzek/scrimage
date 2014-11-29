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

import com.sksamuel.scrimage.{ Image, Raster, Color }
import com.sksamuel.scrimage.filter.util._

class BorderFilter(width: Int, height: Int, color: Color = Color.Black)
    extends CopyingFilter with PixelByPixelFilter {

  def apply(x: Int, y: Int, src: Raster) =
    if (x < width || x >= src.width - width || y < height || y >= src.height - height)
      color
    else
      src.read(x, y)
}

object BorderFilter {
  def apply(width: Int, color: Color = Color.Black) =
    new BorderFilter(width, width, color)

  def apply(width: Int, height: Int, color: Color) =
    new BorderFilter(width, height, color)
}
