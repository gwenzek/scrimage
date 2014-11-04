package thirdparty.marvin_scala

import com.sksamuel.scrimage.{ AbstractImageFilter, Image }

/** Created by guw on 11/10/14.
  */
case class ErrorDiffusion(threshold: Int) extends AbstractImageFilter {
  def defaultDst(src: Image): Image = MarvinGrayScaleFilter(src)

  def filter(srcImage: Image, dstImage: Image): Image = {
    val dst = dstImage.raster
    var color = 0
    var y: Int = 0
    while (y < dst.height) {
      var x: Int = 0
      while (x < dst.width) {
        color = dst.readChannel(x, y, 0)
        val dif = if (color > threshold) color - 255 else color
        dst.writeChannel(x, y, 0, color - dif)
        if (x + 1 < dst.width) {
          color = dst.readChannel(x + 1, y, 0) + (0.4375 * dif).toInt
          dst.writeChannel(x + 1, y, 0, getValidGray(color))
          if (y + 1 < dst.height) {
            color = dst.readChannel(x + 1, y + 1, 0) + (0.0625 * dif).toInt
            dst.writeChannel(x + 1, y + 1, 0, getValidGray(color))
          }
        }
        if (y + 1 < dst.height) {
          color = dst.readChannel(x, y + 1, 0) + (0.3125 * dif).toInt
          dst.writeChannel(x, y + 1, 0, getValidGray(color))
          if (x - 1 >= 0) {
            color = dst.readChannel(x - 1, y + 1, 0) + (0.1875 * dif).toInt
            dst.writeChannel(x - 1, y + 1, 0, getValidGray(color))
          }
        }
        x += 1
      }
      y += 1
    }
    dstImage
  }

  @inline
  private[this] def getValidGray(a_value: Int): Int = {
    if (a_value < 0) 0
    else if (a_value > 255) 255
    else a_value
  }
}
