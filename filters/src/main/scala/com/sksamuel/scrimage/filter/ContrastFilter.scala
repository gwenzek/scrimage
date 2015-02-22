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

import com.sksamuel.scrimage.filter.util.SampledChannelMapper

/** @author Stephen Samuel */
class ContrastFilter(contrast: Float, brightness: Float)
    extends SampledChannelMapper(x => (x * brightness - 0.5f) * contrast + 0.5f) {
}

object ContrastFilter {
  def apply(contrast: Double): ContrastFilter =
    new ContrastFilter(contrast.toFloat, 1f)

  def apply(contrast: Float, brightness: Float): ContrastFilter =
    new ContrastFilter(contrast, brightness)
}

object BrightnessFilter {
  def apply(brightness: Double): ContrastFilter =
    new ContrastFilter(1f, brightness.toFloat)
}
