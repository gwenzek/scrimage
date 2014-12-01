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

import com.sksamuel.scrimage.filter.util._

/** @author Stephen Samuel */
class ChromeFilter(amount: Float, exposure: Float) extends StaticImageFilter {
  val op = new thirdparty.jhlabs.image.ChromeFilter()
  op.setAmount(amount)
  op.setExposure(exposure)
}

object ChromeFilter {
  def apply(): ChromeFilter = apply(0.5f, 1.0f)
  def apply(amount: Float, exposure: Float): ChromeFilter = new ChromeFilter(amount, exposure)

  def chrome(amount: Float, exposure: Float) = new SampledChannelMapper(x =>
    1 - math.exp(-exposure * (x + amount * math.sin(2 * math.Pi * x).toFloat)).toFloat
  )
}

