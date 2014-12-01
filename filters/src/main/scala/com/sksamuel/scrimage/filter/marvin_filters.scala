package com.sksamuel.scrimage

import thirdparty.marvin_scala
import thirdparty.marvin_scala.{ Dithering, NoiseReductionSimple }
import thirdparty.jhlabscala.image.ConvolveFilter

/** Alias for some filters of the marvin_scala package
  */
package object filter {

  // color filters
  def EmbossFilter = marvin_scala.EmbossFilter
  def SepiaFilter = marvin_scala.Sepia(20)

  // edge filters
  def SobelsFilter = marvin_scala.Sobel
  def PrewittFilter = marvin_scala.Prewitt
  def RobertsFilter = marvin_scala.Roberts

  // halftone filters
  def DitherFilter = Dithering()
  def ErrorDiffusionHalftoneFilter = marvin_scala.ErrorDiffusion
  def RylandersFilter = marvin_scala.Rylanders()

  //miscanellous
  def TelevisionFilter = marvin_scala.TelevisionFilter
  def NoiseReductionFilter = new NoiseReductionSimple(20)

  def BlurFilter = ConvolveFilter(Array(
    1 / 14f, 2 / 14f, 1 / 14f,
    2 / 14f, 2 / 14f, 2 / 14f,
    1 / 14f, 2 / 14f, 1 / 14f
  ))

  def BumpFilter = ConvolveFilter(Array(
    -1.0f, -1.0f, 0.0f,
    -1.0f, 1.0f, 1.0f,
    0.0f, 1.0f, 1.0f
  ))
}
