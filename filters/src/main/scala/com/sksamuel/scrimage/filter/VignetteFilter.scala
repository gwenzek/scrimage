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

import java.awt.geom.Point2D
import java.awt.{Color, Graphics2D, RadialGradientPaint}

import com.sksamuel.scrimage.{Filter, Image}
import thirdparty.romainguy.BlendComposite
import thirdparty.romainguy.BlendComposite.BlendingMode

/** @author Stephen Samuel */
class VignetteFilter(start: Double, end: Double, blur: Double, color: Color = Color.BLACK) extends Filter {
  require(start >= 0)
  require(start <= 1)
  require(blur >= 0)
  require(blur <= 1)

  def apply(image: Image) = {
    val blend = image.empty
    val g2 = blend.awt.getGraphics.asInstanceOf[Graphics2D]
    val radius = image.radius * end
    val p = new RadialGradientPaint(new Point2D.Float(blend.center._1, blend.center._2),
      radius.toInt,
      Array(0.0f, if (start == 0) 0.01f else if (start == 1) 0.999f else start.toFloat, 1f),
      Array(Color.WHITE, Color.WHITE, color))
    g2.setPaint(p)
    g2.fillRect(0, 0, blend.width, blend.height)
    g2.dispose()
    blend.updateFromAWT()

    val blured = blend.filter(GaussianBlurFilter((image.radius * blur).toInt))
    val buffered = image.toBufferedImage
    val g3 = buffered.getGraphics.asInstanceOf[Graphics2D]
    g3.setComposite(new BlendComposite(BlendingMode.MULTIPLY, 1.0f))
    g3.drawImage(blured.toBufferedImage, 0, 0, null)
    g3.dispose()
    Image(buffered)
  }
}

object VignetteFilter {
  def apply(): VignetteFilter = apply(0.85f, 0.95f, 0.3)
  def apply(start: Double, end: Double, blur: Double, color: Color = Color.BLACK): VignetteFilter =
    new VignetteFilter(start, end, blur, color)
}
