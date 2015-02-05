package com.sksamuel.scrimage.filter

import com.sksamuel.scrimage._

/** Created by guw on 24/11/14.
  */
package object util {

  def clamp(x: Int) =
    if (x > 255) 255
    else if (x < 0) 0
    else x

  def clamp(x: Int, min: Int, max: Int) =
    if (x > max) max
    else if (x < min) min
    else x

  def clamp(x: Float) =
    if (x > 255) 255
    else if (x < 0) 0
    else (x + 0.5f).toInt

  def clamp(x: Double) =
    if (x > 255) 255
    else if (x < 0) 0
    else (x + 0.5).toInt

  def mod(a: Float, b: Float) = {
    val n = (a / b).toInt
    val r = a - n * b
    if (r < 0) r + b
    else r
  }

  def smoothStep(a: Float, b: Float, x: Float) = {
    if (x < a) 0
    else if (x >= b) 1
    else {
      val y = (x - a) / (b - a)
      y * y * (3 - 2 * y)
    }
  }

  /** Linear interpolation.
    * @param t the interpolation parameter
    * @param a the lower interpolation range
    * @param b the upper interpolation range
    * @return the interpolated value
    */
  def lerp(t: Float, a: Int, b: Int) = (a + t * (b - a)).toInt

  /** Linear interpolation of ARGB values.
    * @param t the interpolation parameter
    * @param rgb1 the lower interpolation range
    * @param rgb2 the upper interpolation range
    * @return the interpolated value
    */
  def mixColors(t: Float, rgb1: RGBColor, rgb2: RGBColor) = Color(
    lerp(t, rgb1.red, rgb2.red),
    lerp(t, rgb1.green, rgb2.green),
    lerp(t, rgb1.blue, rgb2.blue),
    lerp(t, rgb1.alpha, rgb2.alpha)
  )

  trait CopyingFilter {
    def defaultDst(src: Image) = src.copy
  }

  trait ExhaustiveFilter {
    def defaultDst(src: Image) = src.empty
  }

  /* The lines can be computed independently */
  trait LineByLine extends AbstractImageFilter {
    def treatLine(y: Int, src: Raster, dst: Raster): Unit

    def filter(srcImage: Image, dstImage: Image) = {
      val src = srcImage.raster
      val dst = dstImage.raster
      (0 until srcImage.height).par.foreach(treatLine(_, src, dst))
      dstImage
    }
  }

  /* The pixels can be computed independently */
  trait PixelByPixelFilter extends LineByLine {
    def apply(x: Int, y: Int, src: Raster): Color

    def treatLine(y: Int, src: Raster, dst: Raster): Unit = {
      var x = 0
      while (x < src.width) {
        dst.write(x, y, apply(x, y, src))
        x += 1
      }
    }
  }

  /* The new pixel depends only on the previous one */
  trait PixelMapperFilter extends PixelByPixelFilter with InPlaceFilter {
    def apply(x: Int, y: Int, src: Raster): Color = apply(src.read(x, y))

    def apply(color: Color): Color
  }

  trait ChannelMapper extends IndependentPixelByPixel with InPlaceFilter {
    def apply(x: Int, y: Int, c: Int, src: Raster): Int =
      apply(src.readChannel(x, y, c))

    def apply(x: Int): Int
  }

  class SampledChannelMapper(val f: Float => Float)
      extends ChannelMapper with CopyingFilter {

    val treat_alpha = false

    private[this] val sampling = Array.ofDim[Int](256)

    for (i <- 0 to 255) sampling(i) = clamp((255 * f(i / 255f)).toInt)

    def apply(x: Int) = sampling(x)
  }

  trait Independent {
    def treat_alpha: Boolean

    def n_channel_to_treat(src: Raster): Int =
      if (treat_alpha) src.n_channel
      else src.n_real_channel
  }

  trait IndependentPixelByPixel extends LineByLine with Independent {

    def apply(x: Int, y: Int, c: Int, src: Raster): Int

    def treatLine(y: Int, src: Raster, dst: Raster): Unit = {
      var x = 0
      var c = 0
      val n = n_channel_to_treat(src)
      while (x < src.width) {
        c = 0
        while (c < n) {
          dst.writeChannel(x, y, c, apply(x, y, c, src))
          c += 1
        }
        x += 1
      }
    }
  }

  /* A filter that can use the same image for input and output */
  trait InPlaceFilter extends AbstractImageFilter {
    def in_place(src: Image) = filter(src, src)
  }

  /* Each block can be computed with only the pixels of this block */
  trait BlockByBlock extends AbstractImageFilter with InPlaceFilter {
    val blockWidth: Int
    val blockHeight: Int

    def treatBlock(x: Int, y: Int, src: Raster, dst: Raster): Unit

    def treatLine(y: Int, src: Raster, dst: Raster): Unit =
      (0 until src.width by blockWidth).foreach(treatBlock(_, y, src, dst))

    def filter(srcImage: Image, dstImage: Image) = {
      val src = srcImage.raster
      val dst = dstImage.raster
      (0 until src.height by blockHeight).par.foreach(treatLine(_, src, dst))
      dstImage
    }
  }

  trait IndependentBlockByBlock extends AbstractImageFilter with InPlaceFilter {
    val blockWidth: Int
    val blockHeight: Int

    def treatBlock(x: Int, y: Int, c: Int, src: Raster, dst: Raster): Unit

    def treatLine(y: Int, src: Raster, dst: Raster) = {
      var x, c = 0
      while (x < src.width) {
        c = 0
        while (c < src.n_real_channel) { treatBlock(x, y, c, src, dst); c += 1 }
        x += blockWidth
      }
    }

    def filter(srcImage: Image, dstImage: Image) = {
      val src = srcImage.raster
      val dst = dstImage.raster
      (0 until src.height by blockHeight).par.foreach(treatLine(_, src, dst))
      dstImage
    }
  }

  trait BeforeFilter extends AbstractImageFilter {
    def before: Filter

    override def apply(src: Image) = filter(before(src), defaultDst(src))
  }

  /* Returns a Gray image by default with the copied alpha channel */
  trait GrayOutput extends AbstractImageFilter {
    def defaultDst(src: Image) = {
      if (src.raster.has_alpha)
        CopyAlpha.filter(src, new Image(Raster(src.width, src.height, Raster.GRAY_ALPHA)))
      else
        new Image(Raster(src.width, src.height, Raster.GRAY))
    }

    def writeGray(x: Int, y: Int, gray: Int, dst: Raster): Unit = {
      var c = 0
      while (c < dst.n_real_channel) {
        dst.writeChannel(x, y, c, gray)
        c += 1
      }
    }
  }

  object CopyAlpha extends AbstractImageFilter with LineByLine {

    def defaultDst(src: Image): Image = new Image(src.raster.empty(src.width, src.height))

    def treatLine(y: Int, src: Raster, dst: Raster): Unit = {
      (0 until src.width).foreach(x => dst.writeAlpha(x, y, src.readAlpha(x, y)))
    }
  }

  /* Alike to the PixelByPixel but for GrayFilter */
  trait GrayPixelByPixelFilter extends AbstractImageFilter with GrayOutput with LineByLine {
    def toGray(x: Int, y: Int, src: Raster): Int

    def treatLine(y: Int, src: Raster, dst: Raster): Unit = {
      var x = 0
      while (x < src.width) {
        writeGray(x, y, toGray(x, y, src), dst)
        x += 1
      }
    }
  }

  /* Alike to PixelMapper but for GrayImage */
  trait GrayPixelMapper extends GrayPixelByPixelFilter with InPlaceFilter {
    def toGray(rgb: RGBColor): Int

    def toGray(x: Int, y: Int, src: Raster) = toGray(src.read(x, y).toRGB)
  }

  /* Wrapper to convert java filters to scala Object */
  abstract class StaticImageFilter extends Filter {
    val op: JavaAbstractImageFilter

    def apply(src: Image) = op.apply(src)
  }
}
