package com.sksamuel.scrimage

import thirdparty.marvin_scala
import thirdparty.marvin_scala.{ Dithering, NoiseReductionSimple }
import thirdparty.jhlabscala.image.ConvolveFilter

/** Alias for some filters of the marvin_scala package
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

  val BlurFilter = ConvolveFilter(Array(
    1 / 14f, 2 / 14f, 1 / 14f,
    2 / 14f, 2 / 14f, 2 / 14f,
    1 / 14f, 2 / 14f, 1 / 14f
  ))

  val BumpFilter = ConvolveFilter(Array(
    -1.0f, -1.0f, 0.0f,
    -1.0f, 1.0f, 1.0f,
    0.0f, 1.0f, 1.0f
  ))
}
