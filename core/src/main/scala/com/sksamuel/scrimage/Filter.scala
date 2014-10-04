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
package com.sksamuel.scrimage

import java.awt.Graphics2D
import java.awt.image.BufferedImageOp

import com.sksamuel.scrimage.geom.Rectangle

/** @author Stephen Samuel */
trait Filter {
  def apply(src: Image): Image

  def then(other: Filter): Filter = {
    val that = this
    new Filter {
      def apply(src: Image): Image = other.apply(that(src))
    }
  }
}

/** Extension of Filter that applies its filters using a standard java BufferedImageOp.
  *
  * Filters that wish to provide an awt BufferedImageOp need to simply extend this class.
  */
@deprecated("java awt is to be removed", since = "28/08/2014")
abstract class BufferedOpFilter extends Filter {
  val op: BufferedImageOp

  def apply(image: Image): Image = {
    val g2 = image.awt.getGraphics.asInstanceOf[Graphics2D]
    g2.drawImage(image.awt, op, 0, 0)
    g2.dispose()
    Image(image.awt)
  }
}

/** A convenience class which implements those methods of BufferedImageOp which are rarely changed.
  */
trait AbstractImageFilter extends Filter {
  def defaultDst(src: Image): Image

  def filter(src: Image, dst: Image): Image

  def apply(src: Image): Image = filter(src, defaultDst(src))
}

/* Abstract class so java filters can inherit from it */
abstract class JavaAbstractImageFilter extends AbstractImageFilter {
  def createCompatibleDestImage(src: Image) = new Image(src.raster.mimic)

  def createCompatibleDestImage(src: Image, colorModel: Raster.RasterType) =
    new Image(Raster(src.width, src.height, colorModel))

  def getBounds2D(src: Image) = Rectangle(0, 0, src.width, src.height)

  def defaultDst(src: Image): Image = null

  def filter(src: Image, dst: Image): Image
}

trait CopyingFilter extends AbstractImageFilter {
  def defaultDst(src: Image) = src.copy
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

trait BeforeFilter extends AbstractImageFilter {
  def before: Filter

  override def apply(src: Image) = filter(before(src), defaultDst(src))
}

/* Returns a Gray image by default with the copied alpha channel */
trait GrayOutput extends AbstractImageFilter {
  def defaultDst(src: Image) = {
    if (src.raster.has_alpha)
      CopyAlpha.filter(src, new Image(Raster(src.width, src.height,  Raster.GRAY_ALPHA)))
    else
      new Image(Raster(src.width, src.height,  Raster.GRAY))
  }

  def writeGray(x: Int, y: Int, gray: Int, dst: Raster): Unit = {
    var c = 0
    while (c < dst.n_real_channel) {
      dst.writeChannel(x, y, c, gray)
      c += 1
    }
  }
}

object CopyAlpha extends AbstractImageFilter with LineByLine{

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

/* Chained filters */
class PipelineFilter(filters: Filter*) extends Filter {
  def apply(image: Image): Image = filters.foldLeft(image)((img, f) => f.apply(img))
}

object Filter {
  def apply(f: Color => Color) = new Filter {
    def apply(src: Image): Image = src.map(f)
  }

  def apply(f: (Int, Int, Int) => Int) = new Filter {
    def apply(src: Image): Image = src.map(f)
  }
}
