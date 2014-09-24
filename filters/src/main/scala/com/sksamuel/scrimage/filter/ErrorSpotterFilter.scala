package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage.{Color, Filter, Image}

/**
 * Created by guw on 23/09/14.
 */
case class ErrorSpotterFilter(base: Image) extends Filter {
  def apply(src: Image): Image = {
    assert(src.width == base.width)
    assert(src.height == base.height)

    val dst = base.copy
    for(x <- 0 until base.width; y <- 0 until base.height){
      var delta = 0
      var red = 0
      var blue = 0
      for(c <- 0 until base.raster.n_channel){
          delta = base.raster.readChannel(x, y, c) - src.raster.readChannel(x, y, c)
          if(delta > 0)
            red += delta
          if(delta < 0)
            blue -= delta
      }
      dst.raster.write(x, y, Color(math.min(50*red, 255), 0, math.min(50*blue, 255)))
    }
    dst
  }
}
