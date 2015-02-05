package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.{ Image, Filter, PipelineFilter, Color }

/** @author Stephen Samuel */
object NashvilleFilter extends PipelineFilter(
  BackgroundBlendFilter,
  HSBFilter(0, -0.2, 0.5),
  GammaFilter(1.2),
  ContrastFilter(1.6),
  VignetteFilter(0.9, 1, 0.6, Color(255, 140, 0))
)

object BackgroundBlendFilter extends Filter {
  def apply(image: Image) = {
    val background = image.filled(Color(51, 0, 0))
    Blenders.add.in_place(image, background, 1f)
  }
}

