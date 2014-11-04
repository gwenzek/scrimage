package com.sksamuel.scrimage

import thirdparty.marvin_scala
import thirdparty.marvin_scala.{Dithering, NoiseReductionSimple}

/**
 * Alias for some filters of the marvin_scala package
 */
package object filter {

  // color filters
  val EmbossFilter = marvin_scala.EmbossFilter
  val SepiaFilter = marvin_scala.Sepia(20)

  // edge filters
  val SobelsFilter = marvin_scala.Sobel
  val PrewittFilter = marvin_scala.Prewitt
  val RobertsFilter = marvin_scala.Roberts

  // halftone filters
  val DitherFilter = Dithering()
  val ErrorDiffusionHalftoneFilter = marvin_scala.ErrorDiffusion
  val RylandersFilter = marvin_scala.Rylanders()

  //miscanellous
  val TelevisionFilter = marvin_scala.TelevisionFilter
  val NoiseReductionFilter = new NoiseReductionSimple(20)
}
