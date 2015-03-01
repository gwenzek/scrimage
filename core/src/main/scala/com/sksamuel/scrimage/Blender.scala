package com.sksamuel.scrimage

/** Created by guw on 04/11/14.
  */
trait Blender {
  def apply(applicative: Image, base: Image, alpha: Float = 1f) =
    blend(applicative, base, alpha)

  def apply(applicative: Image, alpha: Float)(base: Image) =
    blend(applicative, base, alpha)

  def blend(applicative: Image, base: Image, alpha: Float = 1f): Image
}

