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

import com.sksamuel.scrimage.{ Image, Raster }
import com.sksamuel.scrimage.filter.util._

object MinimumFilter extends IndependentPixelByPixel {

  def apply(x: Int, y: Int, c: Int, src: Raster): Int = {
    var min = 255
    for (
      xi <- math.max(0, x - 1) to math.min(src.width - 1, x + 1);
      yi <- math.max(0, y - 1) to math.min(src.height - 1, y + 1)
    ) min = math.min(min, src.readChannel(xi, yi, c))
    min
  }
}
