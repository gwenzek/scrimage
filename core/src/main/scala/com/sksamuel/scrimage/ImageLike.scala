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

import java.io.{File, OutputStream}
import javax.imageio.metadata.IIOMetadata

import com.sksamuel.scrimage.Format.PNG
import com.sksamuel.scrimage.PixelTools._
import com.sksamuel.scrimage.Position.Center
import com.sksamuel.scrimage.ScaleMethod.Bicubic
import com.sksamuel.scrimage.io.ImageWriter
import org.apache.commons.io.{FileUtils, IOUtils}

import scala.concurrent.{ExecutionContext, Future}

/** @author Stephen Samuel */
trait ImageLike extends WritableImageLike { self =>
  type Self <: ImageLike { type Self = self.Self }

  lazy val points: Seq[(Int, Int)] = for (x <- 0 until width; y <- 0 until height) yield (x, y)
  lazy val center: (Int, Int) = (width / 2, height / 2)
  lazy val radius: Int = Math.sqrt(Math.pow(width / 2.0, 2) + Math.pow(height / 2.0, 2)).toInt
  lazy val dimensions: (Int, Int) = (width, height)

  /** @return Returns the aspect ratio for this image.
    */
  lazy val ratio: Double = if (height == 0) 0 else width / height.toDouble

  /** Clears all image data to the given color
    */
  @deprecated("use filled", "1.4")
  def clear(color: Color) = filled(color)
  def width: Int
  def height: Int

  def forall(f: (Int, Int, Int) => Boolean): Boolean = points.forall(p => f(p._1, p._2, pixel(p)))
  def foreach(f: (Int, Int, Int) => Unit): Unit = points.foreach(p => f(p._1, p._2, pixel(p)))

  def row(y: Int): Array[Int] = pixels(0, y, width, 1)
  def col(x: Int): Array[Int] = pixels(x, 0, 1, height)

  /** Returns the pixel at the given coordinates as a integer in ARGB format.
    *
    * @param x the x coordinate of the pixel to grab
    * @param y the y coordinate of the pixel to grab
    *
    * @return the ARGB value of the pixel
    */
  def pixel(x: Int, y: Int): Int

  /** Returns the color at the given coordinates.
    *
    * @return the RGBColor value of the pixel
    */
  def color(x: Int, y: Int): RGBColor = pixel(x, y)

  /** Returns the ARGB components for the pixel at the given coordinates
    *
    * @param x the x coordinate of the pixel component to grab
    * @param y the y coordinate of the pixel component to grab
    *
    * @return an array containing ARGB components in that order.
    */
  def argb(x: Int, y: Int): Array[Int] = {
    val p = pixel(x, y)
    Array(alpha(p), red(p), green(p), blue(p))
  }

  /** Returns the ARGB components for all pixels in this image
    *
    * @return an array containing ARGB components in that order.
    */
  def argb: Array[Array[Int]] = {
    pixels.map(p => Array(alpha(p), red(p), green(p), blue(p)))
  }

  def rgb(x: Int, y: Int): Array[Int] = {
    val p = pixel(x, y)
    Array(red(p), green(p), blue(p))
  }

  def rgb: Array[Array[Int]] = {
    pixels.map(p => Array(red(p), green(p), blue(p)))
  }

  /** Returns a new image that is scaled to fit the specified bounds while retaining the same aspect ratio
    * as the original image. The dimensions of the returned image will be the same as the result of the
    * scaling operation. That is, no extra padding will be added to match the bounded width and height. For an
    * operation that will scale an image as well as add padding to fit the dimensions perfectly, then use fit()
    *
    * Requesting a bound of 200,200 on an image of 300,600 will result in a scale to 100,200.
    * Eg, the original image will be scaled down to fit the bounds.
    *
    * Requesting a bound of 150,200 on an image of 150,150 will result in the same image being returned.
    * Eg, the original image cannot be scaled up any further without exceeding the bounds.
    *
    * Requesting a bound of 300,300 on an image of 100,150 will result in a scale to 200,300.
    *
    * Requesting a bound of 100,1000 on an image of 50,50 will result in a scale to 100,100.
    *
    * @param boundedWidth the maximum width
    * @param boundedHeight the maximum height
    *
    * @return A new image that is the result of the binding.
    */
  def bound(boundedWidth: Int, boundedHeight: Int): Self

  /** Returns a copy of the canvas with the given dimensions where the
    * original image has been scaled to completely cover the new dimensions
    * whilst retaining the original aspect ratio.
    *
    * If the new dimensions have a different aspect ratio than the old image
    * then the image will be cropped so that it still covers the new area
    * without leaving any background.
    *
    * @param targetWidth the target width
    * @param targetHeight the target height
    * @param scaleMethod the type of scaling method to use. Defaults to Bicubic
    * @param position where to position the image inside the new canvas
    *
    * @return a new Image with the original image scaled to cover the new dimensions
    */
  def cover(targetWidth: Int,
            targetHeight: Int,
            scaleMethod: ScaleMethod = Bicubic,
            position: Position = Center): Self

  /** Return a new Image of the same size, with all pixels set to the supplied colour.
    *
    * @param color the color to set all pixels to
    *
    * @return the new Image
    */
  def filled(color: Color = Color.White): Self

  /** Returns the pixel at the given coordinates as a integer in RGB format.
    *
    * @param p the pixel as an integer tuple
    *
    * @return the ARGB value of the pixel
    */
  def pixel(p: (Int, Int)): Int = pixel(p._1, p._2)

  /** Returns a rectangular region within the given boundaries as a single
    * dimensional array of integers.
    *
    * Eg, pixels(10, 10, 30, 20) would result in an array of size 600 with
    * the first row of the region in indexes 0,..,29, second row 30,..,59 etc.
    *
    * @param x the start x coordinate
    * @param y the start y coordinate
    * @param w the width of the region
    * @param h the height of the region
    * @return an Array of pixels for the region
    */
  def pixels(x: Int, y: Int, w: Int, h: Int): Array[Int] = {
    for (
      y1 <- Array.range(y, y + h);
      x1 <- Array.range(x, x + w)
    ) yield pixel(x1, y1)
  }

  /** Returns a new Image that is a subimage or region of the original image.
    *
    * @param x the start x coordinate
    * @param y the start y coordinate
    * @param w the width of the subimage
    * @param h the height of the subimage
    * @return a new Image that is the subimage
    */
  def subimage(x: Int, y: Int, w: Int, h: Int): Self

  /** Uses linear interpolation to get a sub-pixel.
    *
    * Legal values for `x` and `y` are in [0, width) and [0, height),
    * respectively.
    */
  def subpixel(x: Double, y: Double): Int

  /** Extracts a subimage, but using subpixel interpolation.
    */
  def subpixelSubimage(x: Double, y: Double, subWidth: Int, subHeight: Int): Self

  /** Extract a patch, centered at a subpixel point.
    */
  def subpixelSubimageCenteredAtPoint(x: Double,
                                      y: Double,
                                      xRadius: Double,
                                      yRadius: Double): Self = {
    val xWidth = 2 * xRadius
    val yWidth = 2 * yRadius

    // The dimensions of the extracted patch must be integral.
    require(xWidth == xWidth.round)
    require(yWidth == yWidth.round)

    subpixelSubimage(x - xRadius, y - yRadius, xWidth.round.toInt, yWidth.round.toInt)
  }

  /** Creates a new image which is the result of this image
    * padded with the given number of pixels on each edge.
    *
    * Eg, requesting a pad of 30 on an image of 250,300 will result
    * in a new image with a canvas size of 310,360
    *
    * @param size the number of pixels to add on each edge
    * @param color the background of the padded area.
    *
    * @return A new image that is the result of the padding
    */
  def pad(size: Int, color: Color = X11Colorlist.White): Self = {
    padTo(width + size * 2, height + size * 2, color)
  }

  /** Creates a new image which is the result of this image padded to the canvas size specified.
    * If this image is already larger than the specified pad then the sizes of the existing
    * image will be used instead.
    *
    * Eg, requesting a pad of 200,200 on an image of 250,300 will result
    * in keeping the 250,300.
    *
    * Eg2, requesting a pad of 300,300 on an image of 400,250 will result
    * in the width staying at 400 and the height padded to 300.
    *
    * @param targetWidth the size of the output canvas width
    * @param targetHeight the size of the output canvas height
    * @param color the background of the padded area.
    *
    * @return A new image that is the result of the padding
    */
  def padTo(targetWidth: Int, targetHeight: Int, color: Color = X11Colorlist.White): Self

  /** Creates an empty Image with the same dimensions of this image.
    *
    * @return a new Image that is a clone of this image but with uninitialized data
    */
  def empty: Self

  /** Returns the number of pixels in the image.
    *
    * @return the number of pixels
    */
  def count: Int = pixels.size

  /** Returns a set of the distinct colours used in this image.
    *
    * @return the set of distinct Colors
    */
  def colours: Set[Color] = pixels.map(argb => Color(argb)).toSet

  /** Counts the number of pixels with the given colour.
    *
    * @param color the colour to detect.
    * @return the number of pixels that matched the colour of the given pixel
    */
  def count(color: Color) = pixels.find(_ == color.toInt).size

  /** Creates a new image with the same data as this image.
    * Any operations to the copied image will not write back to the original.
    * Images can be copied multiple times as well as copies copied etc.
    *
    * @return A copy of this image.
    */
  def copy: Self

  /** Maps the pixels of this image into another image by applying the given function to each point.
    *
    * The function accepts three parameters: x,y,p where x and y are the coordinates of the pixel
    * being transformed and p is the current pixel value in ABGR format.
    *
    * @param f the function to transform pixel x,y with existing value p into new pixel value p' (p prime)
    * @return
    */
  def map(f: (Int, Int, Int) => Int): Self

  /** Creates a copy of this image with the given filter applied.
    * The original (this) image is unchanged.
    *
    * @param filter the filter to apply. See com.sksamuel.scrimage.Filter.
    *
    * @return A new image with the given filter applied.
    */
  def filter(filter: Filter): Self

  /** Apply a sequence of filters in sequence.
    * This is sugar for image.filter(filter1).filter(filter2)....
    *
    * @param filters the sequence filters to apply
    * @return the result of applying each filter in turn
    */
  def filter(filters: Filter*): ImageLike = filters.foldLeft(this)((image, filter) => image.filter(filter)): ImageLike

  def fit(targetWidth: Int, targetHeight: Int,
          color: Color = X11Colorlist.White,
          scaleMethod: ScaleMethod = Bicubic,
          position: Position = Center): Self

  def fitToHeight(targetHeight: Int,
                  color: Color = X11Colorlist.White,
                  scaleMethod: ScaleMethod = Bicubic,
                  position: Position = Center): Self =
    fit((targetHeight / height.toDouble * height).toInt, targetHeight, color, scaleMethod, position)

  def fitToWidth(targetWidth: Int,
                 color: Color = X11Colorlist.White,
                 scaleMethod: ScaleMethod = Bicubic,
                 position: Position = Center): Self =
    fit(targetWidth, (targetWidth / width.toDouble * height).toInt, color, scaleMethod, position)

  /** Flips this image horizontally.
    *
    * @return The result of flipping this image horizontally.
    */
  def flipX: Self

  /** Flips this image vertically.
    *
    * @return The result of flipping this image vertically.
    */
  def flipY: Self

  def removeTransparency(color: Color): Self

  def resizeTo(targetWidth: Int, targetHeight: Int, position: Position = Center, background: Color = X11Colorlist.White): Self

  /** Resize will resize the canvas, it will not scale the image.
    * This is like a "canvas resize" in Photoshop.
    *
    * @param scaleFactor the scaleFactor. 1 retains original size. 0.5 is half. 2 double. etc
    * @param position where to position the original image after the canvas size change. Defaults to centre.
    * @param background the color to use for expande background areas. Defaults to White.
    *
    * @return a new Image that is the result of resizing the canvas.
    */
  def resize(scaleFactor: Double, position: Position = Center, background: Color = X11Colorlist.White): Self =
    resizeTo((width * scaleFactor).toInt, (height * scaleFactor).toInt, position, background)

  /** Resize will resize the canvas, it will not scale the image.
    * This is like a "canvas resize" in Photoshop.
    *
    * @param position where to position the original image after the canvas size change
    *
    * @return a new Image that is the result of resizing the canvas.
    */
  def resizeToHeight(targetHeight: Int, position: Position = Center, background: Color = X11Colorlist.White): Self =
    resizeTo((targetHeight / height.toDouble * height).toInt, targetHeight, position, background)

  /** Resize will resize the canvas, it will not scale the image.
    * This is like a "canvas resize" in Photoshop.
    *
    * @param position where to position the original image after the canvas size change
    *
    * @return a new Image that is the result of resizing the canvas.
    */
  def resizeToWidth(targetWidth: Int, position: Position = Center, background: Color = X11Colorlist.White): Self =
    resizeTo(targetWidth, (targetWidth / width.toDouble * height).toInt, position, background)

  /** Returns a copy of this image rotated 90 degrees anti-clockwise (counter clockwise to US English speakers).
    *
    * @return
    */
  def rotateLeft: Self

  /** Returns a copy of this image rotated 90 degrees clockwise. */
  def rotateRight: Self

  /** Scale will resize the canvas and scale the image to match.
    * This is like a "image resize" in Photoshop.
    *
    * @param targetWidth the target width
    * @param targetHeight the target width
    * @param scaleMethod the type of scaling method to use.
    *
    * @return a new Image that is the result of scaling this image
    */
  def scaleTo(targetWidth: Int, targetHeight: Int, scaleMethod: ScaleMethod = Bicubic): Self

  /** Scale will resize the canvas and scale the image to match.
    * This is like a "image resize" in Photoshop.
    *
    * This overloaded version of scale will scale the image so that the new image
    * has a width that matches the given targetWidth
    * and the same aspect ratio as the original.
    *
    * Eg, an image of 200,300 with a scaleToWidth of 400 will result
    * in a scaled image of 400,600
    *
    * @param targetWidth the target width
    * @param scaleMethod the type of scaling method to use.
    *
    * @return a new Image that is the result of scaling this image
    */
  def scaleToWidth(targetWidth: Int, scaleMethod: ScaleMethod = Bicubic): Self =
    scaleTo(targetWidth, (targetWidth / width.toDouble * height).toInt, scaleMethod)

  /** Scale will resize the canvas and scale the image to match.
    * This is like a "image resize" in Photoshop.
    *
    * This overloaded version of scale will scale the image so that the new image
    * has a height that matches the given targetHeight
    * and the same aspect ratio as the original.
    *
    * Eg, an image of 200,300 with a scaleToHeight of 450 will result
    * in a scaled image of 300,450
    *
    * @param targetHeight the target height
    * @param scaleMethod the type of scaling method to use.
    *
    * @return a new Image that is the result of scaling this image
    */
  def scaleToHeight(targetHeight: Int, scaleMethod: ScaleMethod = Bicubic): Self =
    scaleTo((targetHeight / height.toDouble * width).toInt, targetHeight, scaleMethod)

  /** Scale will resize the canvas and the image.
    * This is like a "image resize" in Photoshop.
    *
    * @param scaleFactor the target increase or decrease. 1 is the same as original.
    * @param scaleMethod the type of scaling method to use.
    *
    * @return a new Image that is the result of scaling this image
    */
  def scale(scaleFactor: Double, scaleMethod: ScaleMethod = Bicubic): Self =
    scaleTo((width * scaleFactor).toInt, (height * scaleFactor).toInt, scaleMethod)

  /** Removes the given amount of pixels from each edge; like a crop operation.
    *
    * @param amount the number of pixels to trim from each edge
    *
    * @return a new Image with the dimensions width-trim*2, height-trim*2
    */
  def trim(amount: Int): Self = trim(amount, amount, amount, amount)

  /** Removes the given amount of pixels from each edge; like a crop operation.
    *
    * @param left the number of pixels to trim from the left
    * @param top the number of pixels to trim from the top
    * @param right the number of pixels to trim from the right
    * @param bottom the number of pixels to trim from the bottom
    *
    * @return a new Image with the dimensions width-trim*2, height-trim*2
    */
  def trim(left: Int, top: Int, right: Int, bottom: Int): Self

  /** Crops an image by removing cols and rows that are composed only of a single
    * given color.
    *
    * Eg, if an image had a 20 pixel border of white at the top, and this method was
    * invoked with Color.White then the image returned would have that 20 pixel border
    * removed.
    *
    * @param color the color to match
    * @return
    */
  def autocrop(color: Color): Self

  def pixels: Array[Int]

  /** Returns true if a pixel with the given color exists.
    *
    * @param color the pixel colour to look for.
    * @return true if there exists at least one pixel that has the given pixels color
    */
  def exists(color: Color) = pixels.exists(argb => Color(argb) == color)

  def toImage: Image
  def toBufferedImage: java.awt.image.BufferedImage

  override def equals(that: Any) = that match {
    case other: ImageLike => this.toImage.imageState == other.toImage.imageState
    case _ => false
  }

  /** Creates a MutableImage instance backed by this images raster.
    *
    * Note, any changes to the mutable image write back to this Image.
    * If you want a mutable copy then you must first copy this image
    * before invoking this operation.
    *
    * @return
    */
  @deprecated
  def toMutable: MutableImage

  /** Creates an AsyncImage instance backed by this image.
    *
    * The returned AsyncImage will contain the same backing array
    * as this image.
    *
    * To return back to an image instance use asyncImage.toImage
    *
    * @return an AsyncImage wrapping this image.
    */
  def toAsync(implicit executionContext: ExecutionContext): AsyncImage = AsyncImage(this)

  def withMeta(metadata: IIOMetadata): Self with Metadated = ???

  def getMeta: Option[IIOMetadata] = this match {
    case i : Metadated => Some(i.metadata)
    case _ => None
  }
}

trait WritableImageLike {

  def writer[T <: ImageWriter](format: Format[T]): T

  def write: Array[Byte] = write(Format.PNG)
  def write(format: Format[_ <: ImageWriter]): Array[Byte] = writer(format).write()

  def write(path: String) {
    write(path, Format.PNG)
  }
  def write(path: String, format: Format[_ <: ImageWriter]) {
    write(new File(path), format)
  }
  def write(file: File) {
    write(file, Format.PNG)
  }
  def write(file: File, format: Format[_ <: ImageWriter]) {
    val fos = FileUtils.openOutputStream(file)
    write(fos, format)
    IOUtils.closeQuietly(fos)
  }
  def write(out: OutputStream) {
    write(out, PNG)
  }
  def write(out: OutputStream, format: Format[_ <: ImageWriter]) {
    writer(format).write(out)
  }

}

trait EndoFunctor[A] {
  type Self <: EndoFunctor[A]
  def fmap(f: A => A): Self
  def apply[B](f: A => B): B
}

trait ImageFunctor[T <: ImageLike { type Self = T }] extends ImageLike with EndoFunctor[T] { self =>
  type Self <: ImageFunctor[T] { type Self = self.Self }

  def autocrop(color: Color) = fmap(_.autocrop(color))
  def bound(boundedWidth: Int, boundedHeight: Int) = fmap(_.bound(boundedWidth, boundedHeight))
  def cover(targetWidth: Int, targetHeight: Int, scaleMethod: ScaleMethod, position: Position) =
    fmap(_.cover(targetWidth, targetHeight, scaleMethod, position))
  def copy = fmap(_.copy)
  def empty = fmap(_.empty)

  def filled(color: Color) = fmap(_.filled(color))
  def filter(filter: Filter) = fmap(_.filter(filter))

  def fit(targetWidth: Int, targetHeight: Int, color: Color, scaleMethod: ScaleMethod, position: Position) =
    fmap(_.fit(targetWidth, targetHeight, color, scaleMethod, position))

  def flipX = fmap(_.flipX)
  def flipY = fmap(_.flipY)

  def map(f: (Int, Int, Int) => Int) = fmap(_.map(f))

  def padTo(targetWidth: Int, targetHeight: Int, color: Color) =
    fmap(_.padTo(targetWidth, targetHeight))

  def scaleTo(targetWidth: Int, targetHeight: Int, scaleMethod: ScaleMethod) =
    fmap(_.scaleTo(targetWidth, targetHeight, scaleMethod))

  def resizeTo(targetWidth: Int, targetHeight: Int, position: Position, background: Color) =
    fmap(_.resizeTo(targetWidth, targetHeight, position))
  def removeTransparency(color: Color) = fmap(_.removeTransparency(color))
  def rotateLeft = fmap(_.rotateLeft)
  def rotateRight = fmap(_.rotateRight)

  def subimage(x: Int, y: Int, w: Int, h: Int) = fmap(_.subimage(x, y, w, h))
  def subpixelSubimage(x: Double, y: Double, subWidth: Int, subHeight: Int) =
    fmap(_.subpixelSubimage(x, y, subWidth, subHeight))

  def trim(left: Int, top: Int, right: Int, bottom: Int) = fmap(_.trim(left, top, right, bottom))

  def height = apply(_.height)
  def width = apply(_.width)
  def pixels = apply(_.pixels)
  def pixel(x: Int, y: Int) = apply(_.pixel(x, y))
  def subpixel(x: Double, y: Double): Int = apply(_.subpixel(x, y))

  def toBufferedImage = apply(_.toBufferedImage)
  def toMutable = apply(_.toMutable)

  def writer[U <: ImageWriter](format: Format[U]): U = apply(_.writer[U](format))
}

trait Metadated {
  type Self
  def metadata: IIOMetadata
  def enrich(meta: IIOMetadata): Self with Metadated
}

class ImageWithMeta(override val raster: Raster, val metadata: IIOMetadata) extends Image(raster) with Metadated {
  def enrich(meta: IIOMetadata) = new ImageWithMeta(raster.copy, meta)

  override def copy = new ImageWithMeta(raster.copy, metadata)
  override def copy(raster: Raster) = new ImageWithMeta(raster, metadata)
}

class AsyncWithMeta(override val image: Future[Image], val metadata: IIOMetadata)(implicit executionContext: ExecutionContext)
      extends AsyncImage(image)(executionContext) with Metadated {
  def enrich(meta: IIOMetadata) = new AsyncWithMeta(image, meta)
}

object ImageWithMeta {
  def apply(image: Image, metadata: IIOMetadata) = new ImageWithMeta(image.raster, metadata)
  def apply(image: AsyncImage, metadata: IIOMetadata)(implicit executionContext: ExecutionContext) =
    new AsyncWithMeta(image, metadata)
}