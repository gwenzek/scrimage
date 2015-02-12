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

import com.sksamuel.scrimage.filter.TransformFilter._

object OffsetFilter {
  def apply(x: Int, y: Int): OffsetFilter = new OffsetFilter(x, y)
}

case class OffsetFilter(xOffset: Int, yOffset: Int, wrap: Boolean = true)
    extends TransformFilter {

  override def edgeAction = if (wrap) Wrap else Zero
  override val interpolation = NearestNeighbor

  def transformInverse(x: Int, y: Int) = (x - xOffset, y - yOffset)
}
