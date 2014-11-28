package com.sksamuel.scrimage

/** Created by guw on 04/11/14.
  */
trait Blender {
  def blend(applicative: Image, base: Image, alpha: Float = 1f): Image
}

